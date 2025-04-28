package com.example.mobiilisovellusprojekti.ViewModels

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiilisovellusprojekti.Permissions.hasBluetoothPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStarted
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStopped
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingData
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingSettings
import no.nordicsemi.android.kotlin.ble.core.advertiser.ManufacturerData
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceUuid
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.ServerConnectionEvent
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattService
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import java.util.UUID
import kotlin.collections.toString


data class AdvertisingState(
    val isAdvertising: Boolean
)

class ChatBleServer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    chatViewModel: ChatViewModel
) {

    private val _connectedDevices = mutableListOf<ServerConnectionEvent.DeviceConnected>()
    val connectedDevices: List<ServerConnectionEvent.DeviceConnected>
        get() = _connectedDevices

    private val _state = MutableStateFlow(AdvertisingState(isAdvertising = false))
    val state: StateFlow<AdvertisingState> = _state

    private val advertiser = BleAdvertiser.create(context)
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

            val server = ServerBleGatt.create(context, this, serverConfig)
            onServerCreated(server)
        }
    }


    // SET UP THE DATA HERE
    fun setUpServices(services: ServerBleGattService, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel) {

        // Search for Charasteristic
        val messageCharacteristic = services.findCharacteristic(BleViewModel.CHARACTERISTIC_UUID)

        // Handle Messages here and what happens next
        messageCharacteristic?.value?.onEach { data ->
            val message = String(data.value, Charsets.UTF_8)
            Log.d("ChatBleServer", "Received message: $message")
            chatViewModel.addMessage(message, isSentByUser = false)

            // Add the function to check if the guess was correct

        }?.launchIn(viewModelScope)

        // Search for Charasteristic
        val coordinatesCharasteristics = services.findCharacteristic(BleViewModel.COORDINATES_UUID)

        val receivedChunks = mutableListOf<ByteArray>()

        // Handle Coordinate and what happens here
        coordinatesCharasteristics?.value?.onEach { data ->
            try {
                Log.d("ChatBleServer", "Received coordinates characteristic: ${data.toString()}")
                Log.d("ChatBleServer", "Received coordinates data: ${data.value}")

                // Extract the flag and the actual chunk
                val isLastChunk = data.value[0] == 1.toByte()
                val chunk = data.value.copyOfRange(1, data.value.size)

                // Add the chunk to the list
                receivedChunks.add(chunk)

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


    fun observeConnections(server: ServerBleGatt, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel,onDeviceConnected: () -> Unit) {
        server.connectionEvents
            .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
            .map { it.connection }
            .onEach { connection ->
                _connectedDevices.add(ServerConnectionEvent.DeviceConnected(connection))
                connection.services.findService(BleViewModel.SERVICE_UUID)?.let { service ->
                    setUpServices(service, viewModelScope, chatViewModel, drawingViewModel)

                    // Notify the devices that connection has been established
                    onDeviceConnected()
                }
            }.launchIn(viewModelScope)
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
            startAdvertisingProcess()
            Log.e(
                "ChatBleServer",
                "Missing required Bluetooth permissions: $missingPermissions"
            )
        } else {
            // Oikeudet ovat kunnossa, aloita mainostaminen
            startAdvertisingProcess()
        }

        }

    private fun startAdvertisingProcess(){
        try {
            Log.d("ChatBleServer", "Starting Advertiser")
            coroutineScope.launch {
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

    fun startServer(context: Context, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel,onDeviceConnected: () -> Unit) {
        declareServer(context, viewModelScope) { server ->
            observeConnections(server, viewModelScope, chatViewModel, drawingViewModel,onDeviceConnected)
        }
    }

    fun sendCoordinates(drawingState: DrawingState, viewModelScope: CoroutineScope, drawingViewModel: DrawingViewModel) {
        val connectedDevices = _connectedDevices // List of connected devices

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
                        Log.d("SendCoordinates message: ", coordinate.path.toString())

                        if (coordinate != null) {
                            Log.d("SendCoordinates message: ", "We are inside if")
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

}


class BleClient() {

}


@SuppressLint("MissingPermission")
class BleViewModel : ViewModel() {

    companion object {
        // GAME_UUID = This is our programs ID that we want to find to help searching
        val GAME_UUID: UUID = UUID.fromString("a902a33a-7a3a-4937-b4bf-b0cd141346b5")

        // SERVICE_UUID to find the service we are looking for
        val SERVICE_UUID = UUID.fromString("b7ceba11-2542-477f-a2a3-f8012d6ce13c")


        // Messages
        // Characteristic UUID = Where the data will be located at
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("9c0cd23f-44c1-4d3d-aaa3-7678bf19a218")

        // Canvas
        // Coordinates UUID to where the canvas coordinates will be placed
        val COORDINATES_UUID: UUID = UUID.fromString("ba598b1a-2458-48fb-ae8d-ed71b4760cdf")
    }


    // To enable Host
    private lateinit var chatBleServer: ChatBleServer

    fun initializeChatBleServer(context: Context, chatViewModel: ChatViewModel) {
        chatBleServer = ChatBleServer(context, viewModelScope, chatViewModel)
    }



    //  To save the connection for later
    private var _connection: ClientBleGatt? = null
    val connection: ClientBleGatt?
        get() = _connection

    // To save the Charasteristic
    private var _characteristic: ClientBleGattCharacteristic? = null
    val connectionCharasteristic: ClientBleGattCharacteristic?
        get() = _characteristic


    // Coordinates Charasteristic
    private var _coordCharasteristic: ClientBleGattCharacteristic? = null
    val coordinateCharasteristic: ClientBleGattCharacteristic?
        get() = _coordCharasteristic

    val advertisingState: StateFlow<AdvertisingState>
        get() = if (::chatBleServer.isInitialized) {
            chatBleServer.state
        } else {
            MutableStateFlow(AdvertisingState(isAdvertising = false))
        }

    val scanResults = MutableLiveData<List<ServerDevice>>(emptyList())
    val isScanning = MutableLiveData(false)
    val isAdvertising = MutableLiveData(false)
    val connectionState = MutableLiveData<String>()

    // Role of the device
    val isHost = mutableStateOf(false)



    fun startAdvertising(context: Context, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel ,onDeviceConnected: () -> Unit) {
        if (::chatBleServer.isInitialized) {
            chatBleServer.startServer(context, viewModelScope, chatViewModel,drawingViewModel,onDeviceConnected)
            isAdvertising.value = true
            Log.d("DBG?","${isAdvertising.value}")
            chatBleServer.startAdvertising()
        } else {
            Log.e("BleViewModel", "ChatBleServer is not initialized")
        }
    }

    fun scanDevices(context: Context) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.BLUETOOTH
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val aggregator = BleScanResultAggregator()
            isScanning.value = true

            val serviceUuid = FilteredServiceUuid(
                uuid = ParcelUuid(GAME_UUID)
            )

            val scanFilter = BleScanFilter(
                serviceUuid = serviceUuid
            )
            Log.d("DBG", "scanning for $scanFilter")
            BleScanner(context).scan(listOf(scanFilter))
                .map { aggregator.aggregateDevices(it) }
                .onEach {
                    scanResults.value = it
                    isScanning.value = false
                            it.forEach { device ->
                                //val services = device.services.map { it.uuid.toString() }
                                //Log.d("DBG", "Found device: ${device.name}, services: ${device.bondState}")
                        }

                }
                .launchIn(viewModelScope)
        } else {
            Log.e("BleViewModel", "Bluetooth is not enabled")
        }
    }

    suspend fun connectToDevice(context: Context, device: ServerDevice): Boolean {
        try {
            val connection = ClientBleGatt.connect(context, device.address, viewModelScope)
            val services = connection.discoverServices()

            connection.requestMtu(512)

            val service = services.findService(SERVICE_UUID)
            if (service == null) {
                Log.e("ConnectToDevice", "service was not found")
                return false
            }

            val characteristic = service.findCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic == null) {
                Log.e("ConnectToDevice", "No message characteristic found")
                return false
            }

            val coordinates = service.findCharacteristic(COORDINATES_UUID)
            if (coordinates == null) {
                Log.e("ConnectToDevice", "No coordiante characteristic found")
                return false
            }

            _connection = connection
            _characteristic = characteristic
            _coordCharasteristic = coordinates
            connectionState.postValue("Connected to ${device.name ?: "Unknown"}")
        } catch (e: Exception) {
            connectionState.postValue("Failed to connect to ${device.name ?: "Unknown"}: ${e.message}")
            Log.e("BleViewModel", "Connection failed: ${e.message}")
        }
        return true
    }



    fun observeChatNotifications(context: Context, chatViewModel: ChatViewModel) {
        val characteristic = connectionCharasteristic
        if (characteristic != null) {
            if (!hasBluetoothPermissions(context)) {
                Log.e("BleViewModel", "Missing required Bluetooth permissions")
                return
            }
            viewModelScope.launch {
                try {
                    characteristic.getNotifications()
                        .onEach { data ->
                            val message = String(data.value, Charsets.UTF_8)
                            Log.d("BleViewModel", "Notification received: $message")

                            // Handle the received message here
                            chatViewModel.addMessage(message, isSentByUser = false)

                        }
                        .launchIn(this)
                } catch (e: Exception) {
                    Log.e("BleViewModel", "Failed to observe notifications: ${e.message}")
                }
            }
        } else {
            Log.e("BleViewModel", "Characteristic not found to observe notifications")
        }
    }

    fun observeCordinateNotifications(context: Context, drawingViewModel: DrawingViewModel) {
        val characteristic = coordinateCharasteristic
        if (characteristic != null) {
            if (!hasBluetoothPermissions(context)) {
                Log.e("BleViewModel", "Missing required Bluetooth permissions")
                return
            }

            // When receiving the data
            val receivedChunks = mutableListOf<ByteArray>()

            viewModelScope.launch {
                try {
                    characteristic.getNotifications()
                        .onEach { data ->
                            val byteArray = data.value

                            // Extract the flag and the actual chunk
                            val isLastChunk = byteArray[0] == 1.toByte() // First byte is the flag
                            val chunk = byteArray.copyOfRange(1, byteArray.size) // Remaining bytes are the data

                            // Add the chunk to the buffer
                            receivedChunks.add(chunk)

                            // If it's the last chunk, reassemble and process the data
                            if (isLastChunk) {
                                val completeData = receivedChunks.reduce { acc, bytes -> acc + bytes }
                                receivedChunks.clear() // Clear the buffer

                                // Deserialize and process the complete data
                                val coordinate = drawingViewModel.deserializePathDataBinary(completeData)
                                Log.d("BleViewModel", "Notification received: $coordinate")

                                // Add the data into the drawingViewModel
                                drawingViewModel.updatePaths(coordinate)

                            }
                        }
                        .launchIn(this)
                } catch (e: Exception) {
                    Log.e("BleViewModel", "Failed to observe notifications: ${e.message}")
                }
            }
        } else {
            Log.e("BleViewModel", "Characteristic not found to observe notifications")
        }
    }

    fun sendMessageToClient(message: String, chatViewModel: ChatViewModel) {
        if (::chatBleServer.isInitialized) {
            chatViewModel.addMessage(message, isSentByUser = true)
            chatBleServer.sendData(message, viewModelScope)
        } else {
            Log.e("BleViewModel", "ChatBleServer is not initialized")
        }
    }



    fun sendMessageToServer(message: String, chatViewModel: ChatViewModel) {
        val characteristic = connectionCharasteristic
        if (characteristic != null) {
            viewModelScope.launch {
                try {

                    val data = DataByteArray(message.toString().toByteArray())
                    characteristic.write(data)
                    Log.d("BleViewModel", "Message sent to server: $message")
                    chatViewModel.addMessage(message, isSentByUser = true)
                } catch (e: Exception) {
                    Log.e("BleViewModel", "Failed to send message: ${e.message}")
                }
            }
        } else {
            Log.e("BleViewModel", "Characteristic not found for sending data")
        }
    }



    fun sendCoordinatesToServer( drawingState: DrawingState, drawingViewModel: DrawingViewModel) {

        if (isHost.value == true) {

            chatBleServer.sendCoordinates(
                drawingState = drawingState,
                viewModelScope = viewModelScope,
                drawingViewModel = drawingViewModel
            )

        } else {
            Log.d("sendCoordinatesToServer", "Sending coordinates to server")
            val characteristic = coordinateCharasteristic
            if (characteristic != null) {
                viewModelScope.launch {
                    try {
                        val coordinate = drawingState.paths.last()
                        Log.d("Cord", coordinate.toString())
                            // serialisoidaan, en oo tehny mitää vastaanottamiseen
                            val byteData = drawingViewModel.serializePathDataBinary(coordinate)

                            // Chunking data
                            val totalChunks = (byteData.size + 500 - 1) / 500
                            for (i in 0 until totalChunks) {
                                val start = i * 500
                                val end = minOf(start + 500, byteData.size)
                                val chunk = byteData.copyOfRange(start, end)

                                // Add a flag to indicate if this is the last chunk
                                val isLastChunk = (i == totalChunks - 1)
                                val chunkWithFlag = ByteArray(chunk.size + 1).apply {
                                    this[0] = if (isLastChunk) 1 else 0 // 1 for last chunk, 0 otherwise
                                    System.arraycopy(chunk, 0, this, 1, chunk.size)
                                }

                                characteristic.write(DataByteArray(chunkWithFlag))
                            }

                            Log.d(
                                "sendCoordinatesToServer",
                                "Coordinate ${coordinate.id} sent to server"
                            )

                    } catch (e: Exception) {
                        Log.e("sendCoordinatesToServer", "Failed to send coordinates to server")
                        Log.e("sendCoordinatesToServer", "${e.message}")
                    }
                }
            }
        }
    }




}