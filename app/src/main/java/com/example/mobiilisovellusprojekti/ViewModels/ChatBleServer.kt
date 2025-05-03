package com.example.mobiilisovellusprojekti.ViewModels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStarted
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStopped
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingData
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingSettings
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.ServerConnectionEvent
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattService
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType

data class AdvertisingState(
    val isAdvertising: Boolean
)

class ChatBleServer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) {

    var serverBleGatt: ServerBleGatt? = null

    private val _connectedDevices = mutableSetOf<ServerConnectionEvent.DeviceConnected>()
    val connectedDevices: Set<ServerConnectionEvent.DeviceConnected>
        get() = _connectedDevices

    private val _state = MutableStateFlow(AdvertisingState(isAdvertising = false))
    val state: StateFlow<AdvertisingState> = _state

    private var advertiser = BleAdvertiser.create(context)
    val advertiserConfig = BleAdvertisingConfig(
        settings = BleAdvertisingSettings(
            deviceName = "Doodle",
            anonymous = false,
        ),
        advertiseData = BleAdvertisingData(
            serviceUuid = ParcelUuid(BleViewModel.GAME_UUID),
            includeDeviceName = true,
        )
    )

    fun declareServer(
        context: Context,
        viewModelScope: CoroutineScope,
        onServerCreated: (ServerBleGatt) -> Unit
    ) {
        viewModelScope.launch {
            val messageCharasteristics = ServerBleGattCharacteristicConfig(
                BleViewModel.CHARACTERISTIC_UUID,
                listOf(
                    BleGattProperty.PROPERTY_READ,
                    BleGattProperty.PROPERTY_WRITE,
                    BleGattProperty.PROPERTY_NOTIFY),
                listOf(
                    BleGattPermission.PERMISSION_READ,
                    BleGattPermission.PERMISSION_WRITE)
            )

            val coordinatesCharasteristics = ServerBleGattCharacteristicConfig(
                BleViewModel.COORDINATES_UUID,
                listOf(
                    BleGattProperty.PROPERTY_READ,
                    BleGattProperty.PROPERTY_WRITE,
                    BleGattProperty.PROPERTY_NOTIFY),
                listOf(
                    BleGattPermission.PERMISSION_READ,
                    BleGattPermission.PERMISSION_WRITE)
            )

            val serverConfig = ServerBleGattServiceConfig(
                BleViewModel.SERVICE_UUID,
                ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
                listOf(messageCharasteristics, coordinatesCharasteristics)
            )

            try {
                val server = ServerBleGatt.create(context, this, serverConfig)
                onServerCreated(server)
            } catch (e: SecurityException) {
                Log.e("ChatBleServer", "SecurityException: ${e.message}")
            }
        }
    }


    private var messageJob: Job? = null
    private var coordinatesJob: Job? = null

    // SET UP THE DATA HERE
    fun setUpServices(services: ServerBleGattService, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel, gameViewModel: GameViewModel) {

        // Search for Charasteristic
        val messageCharacteristic = services.findCharacteristic(BleViewModel.CHARACTERISTIC_UUID)

        // Handle Messages here and what happens next
        messageJob = messageCharacteristic?.value?.onEach { data ->
            val message = String(data.value, Charsets.UTF_8)

            Log.d("Message char",messageJob.toString())


            if (message == "CLEAR_CANVAS") {
                drawingViewModel.onClearCanvas()
            } else if (message == "GAME_OVER") {
                if (!gameViewModel.gameOver.value) {
                    gameViewModel.setGameOver(true)
                }
            } else {
                gameViewModel.onNewMessage(message)
                chatViewModel.addMessage(message, isSentByUser = false)

            }
            Log.d("ChatBleServer", "Received message: $message")
        }?.launchIn(viewModelScope)

        // Search for Charasteristic
        val coordinatesCharasteristics = services.findCharacteristic(BleViewModel.COORDINATES_UUID)

        val receivedChunks = mutableListOf<ByteArray>()


        // Handle Coordinate and what happens here
        coordinatesJob = coordinatesCharasteristics?.value?.onEach { data ->
            try {
                Log.d("ChatBleServer", "Received coordinates characteristic: ${data}")
                Log.d("ChatBleServer", "Received coordinates data: ${data.value}")
                Log.d("Coord Char",coordinatesJob.toString())

                // Extract the flag and the actual chunk
                val isLastChunk = data.value[0] == 1.toByte()
                val chunk = data.value.copyOfRange(1, data.value.size)

                // Add the chunk to the list
                receivedChunks.add(chunk)

                Log.d("CHECK CONNECTIONS","${_connectedDevices}")
                // If it's the last chunk, reassemble and process the data
                if (isLastChunk) {
                    val completeData = receivedChunks.reduce { acc, bytes -> acc + bytes }
                    receivedChunks.clear() // Clear the buffer

                    // Deserialize and update paths
                    val convertedValue = drawingViewModel.deserializePathDataBinary(completeData)
                    drawingViewModel.updatePaths(convertedValue)
                }


            } catch (e: Exception) {
                Log.e("ChatBleServer", "Failed to deserialize coordinates: ${e.message}")
            }
        }?.launchIn(viewModelScope)

    }

    fun stopServices() {
        coordinatesJob?.cancel()
        messageJob?.cancel()
        coordinatesJob = null
        messageJob = null
    }


    private var observeConnectionsJob: Job? = null

    fun observeConnections(server: ServerBleGatt,
                           viewModelScope: CoroutineScope,
                           chatViewModel: ChatViewModel,
                           drawingViewModel: DrawingViewModel,
                           gameViewModel: GameViewModel,
                           onDeviceConnected: () -> Unit
    )
    {
        observeConnectionsJob = server.connectionEvents
            .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
            .map { it.connection }
            .onEach { connection ->
                _connectedDevices.add(ServerConnectionEvent.DeviceConnected(connection))
                connection.services.findService(BleViewModel.SERVICE_UUID)?.let { service ->
                    setUpServices(service, viewModelScope, chatViewModel, drawingViewModel, gameViewModel)

                    Log.d("New Connection","${connection.device.name} connected: ${connection.device.address}")
                    Log.d("New Connection",_connectedDevices.toString())
                    // Notify the devices that connection has been established
                    onDeviceConnected()
                }
            }.launchIn(viewModelScope)
    }

    fun cancelObserveConnections() {
        observeConnectionsJob?.cancel()
        observeConnectionsJob = null
    }

    fun startAdvertising() {
        val requiredPermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            } else {
                arrayOf(Manifest.permission.BLUETOOTH)
            }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            // Oikeudet ovat kunnossa, aloita mainostaminen
            startAdvertisingProcess()

        } else {
            Log.e(
                "ChatBleServer",
                "Missing required Bluetooth permissions: $missingPermissions"
            )
        }

    }

    private var advertisingJob: Job? = null

    private fun startAdvertisingProcess(){
        try {
            Log.d("ChatBleServer", "Starting Advertiser")
            advertisingJob = coroutineScope.launch {
                advertiser.advertise(advertiserConfig)
                    .cancellable()
                    .catch { it.printStackTrace() }
                    .collect {
                        when (it) {
                            is OnAdvertisingSetStarted -> {
                                _state.value = _state.value.copy(isAdvertising = true)
                                Log.d("ChatBleServer", "Advertising started")
                                Log.d("DBG", "${Build.VERSION.SDK_INT} & ${Build.VERSION_CODES.S}")
                                Log.d("DBG!", "AdvertiseData serviceUuid: ${advertiserConfig.advertiseData?.serviceUuid}")
                                Log.d("DBG", "Advertising with UUID: ${BleViewModel.GAME_UUID}")
                                Log.d("DBG!","${advertiser}")
                                Log.d("DBG!","$connectedDevices")
                                Log.d("DBG!","${advertiserConfig.advertiseData}")
                            }

                            is OnAdvertisingSetStopped -> {
                                _state.value = _state.value.copy(isAdvertising = false)
                                Log.d("ChatBleServer", "Advertising stopped")
                            }

                            else -> {
                                Log.w("ChatBleServer", "Unhandled advertising event: $it")
                            }
                        }
                    }
            }
        } catch (e: SecurityException) {
            Log.e("ChatBleServer", "SecurityException: ${e.message}")
        }
    }

    fun stopAdvertisingProcess() {
        advertisingJob?.cancel()
        advertisingJob = null
        _state.value = _state.value.copy(isAdvertising = false)
        Log.d("ChatBleServer", "Advertising process cancelled")
    }

    fun startServer(context: Context, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel, gameViewModel: GameViewModel, onDeviceConnected: () -> Unit) {
        declareServer(context, viewModelScope) { server ->
            observeConnections(server, viewModelScope, chatViewModel, drawingViewModel,gameViewModel, onDeviceConnected)
            serverBleGatt = server
        }
    }

    fun sendCoordinates(drawingState: DrawingState, viewModelScope: CoroutineScope, drawingViewModel: DrawingViewModel) {
        val connectedDevices = _connectedDevices

        Log.d("SendCoordinates", "Connected devices: $connectedDevices")

        if (connectedDevices.isEmpty()) {
            Log.e("ChatBleServer", "No connected devices to send data to")
            return
        }

        viewModelScope.launch {
            try {
                connectedDevices.forEach { device ->
                    val service = device.connection.services.findService(BleViewModel.SERVICE_UUID)
                    val characteristic = service?.findCharacteristic(BleViewModel.COORDINATES_UUID)

                    if (characteristic != null) {
                        val coordinate = drawingState.paths.last()
                        Log.d("SendCoordinates", coordinate.path.toString())

                        if (coordinate != null) {
                            val byteData = drawingViewModel.serializePathDataBinary(coordinate)

                            // Chunking data
                            val chunkSize = 500
                            val totalChunks = (byteData.size + chunkSize - 1) / chunkSize
                            for (i in 0 until totalChunks) {
                                val start = i * chunkSize
                                val end = minOf(start + chunkSize, byteData.size)
                                val chunk = byteData.copyOfRange(start, end)

                                // Add a flag to indicate if this is the last chunk
                                val isLastChunk = (i == totalChunks - 1)
                                val chunkWithFlag = ByteArray(chunk.size + 1).apply {
                                    this[0] = if (isLastChunk) 1 else 0 // 1 for last chunk, 0 otherwise
                                    System.arraycopy(chunk, 0, this, 1, chunk.size)
                                }

                                Log.d("CHECK CONNECTIONS","${_connectedDevices}")
                                Log.d("sendCoords","Send and notify client")
                                characteristic.setValueAndNotifyClient(DataByteArray(chunkWithFlag))
                            }
                        }

                        Log.d("ChatBleServer", "Coordinate sent to device: ${device.connection.device.address}")
                    } else {
                        Log.e("ChatBleServer", "Characteristic not found for device: ${device.connection.device.address}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatBleServer", "Failed to send data: ${e.message}")
            }
        }
    }

    fun sendData(message: String, viewModelScope: CoroutineScope) {
        val connectedDevices = _connectedDevices // List of connected devices
        if (connectedDevices.isEmpty()) {
            Log.e("ChatBleServer", "No connected devices to send data to")
            return
        }

        viewModelScope.launch {
            try {
                connectedDevices.forEach { device ->
                    val service = device.connection.services.findService(BleViewModel.SERVICE_UUID)
                    val characteristic = service?.findCharacteristic(BleViewModel.CHARACTERISTIC_UUID)

                    if (characteristic != null) {

                        Log.d("SendData message: ", message)

                        val data = DataByteArray(message.toByteArray())

                        characteristic.setValueAndNotifyClient(data)
                        Log.d("ChatBleServer", "Message sent to device: ${device.connection.device.address}")
                    } else {
                        Log.e("ChatBleServer", "Characteristic not found for device: ${device.connection.device.address}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatBleServer", "Failed to send data: ${e.message}")
            }
        }
    }

    fun clearConnectedDevices() {
        _connectedDevices.clear()
    }


}