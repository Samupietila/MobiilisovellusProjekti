package com.example.mobiilisovellusprojekti.ViewModels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiilisovellusprojekti.Permissions.hasBluetoothPermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceUuid
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import java.util.UUID

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

    fun initializeChatBleServer(context: Context) {
        chatBleServer = ChatBleServer(context, viewModelScope)
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



    fun startAdvertising(context: Context, chatViewModel: ChatViewModel, drawingViewModel: DrawingViewModel, gameViewModel: GameViewModel,onDeviceConnected: () -> Unit) {
        if (::chatBleServer.isInitialized) {
            if (chatBleServer.serverBleGatt != null) {
                chatBleServer.startServer(context, viewModelScope, chatViewModel,drawingViewModel,gameViewModel,onDeviceConnected)

            }
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
        return try {
            val connection = ClientBleGatt.connect(context, device.address, viewModelScope)

            Log.d("ConnectToDevice", "Connection successful to ${device.name ?: "Unknown"}")

            connection.requestMtu(512)
            val services = connection.discoverServices()

            val service = services.findService(SERVICE_UUID)
                ?: throw Exception("Service with UUID $SERVICE_UUID not found")

            val characteristic = service.findCharacteristic(CHARACTERISTIC_UUID)
                ?: throw Exception("Message characteristic with UUID $CHARACTERISTIC_UUID not found")

            val coordinates = service.findCharacteristic(COORDINATES_UUID)
                ?: throw Exception("Coordinates characteristic with UUID $COORDINATES_UUID not found")

            _connection = connection
            _characteristic = characteristic
            _coordCharasteristic = coordinates
            connectionState.postValue("Connected to ${device.name ?: "Unknown"}")

            true
        } catch (e: Exception) {
            connectionState.postValue("Failed to connect to ${device.name ?: "Unknown"}: ${e.message}")
            Log.e("BleViewModel", "Connection failed: ${e.message}")
            false
        }
    }



    fun observeChatNotifications(context: Context, chatViewModel: ChatViewModel, gameViewModel: GameViewModel, drawViewModel: DrawingViewModel) {
        val characteristic = connectionCharasteristic
        Log.d("Obv Messages", "Observing notifications on characteristic: $characteristic")
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
                            if (message == "CLEAR_CANVAS") {
                                drawViewModel.onClearCanvas()
                            } else if (message == "GAME_OVER") {
                                if (!gameViewModel.gameOver.value) {
                                    gameViewModel.setGameOver(true)
                                }
                            } else {
                                chatViewModel.addMessage(message, isSentByUser = false)
                                gameViewModel.onNewMessage(message)
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

    fun observeCordinateNotifications(context: Context, drawingViewModel: DrawingViewModel) {
        val characteristic = coordinateCharasteristic
        Log.d("ObvCoords", "Observing notifications on characteristic: $characteristic")
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


                            val isLastChunk = byteArray[0] == 1.toByte()
                            val chunk = byteArray.copyOfRange(1, byteArray.size)

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

    fun sendMessage(message: String, chatViewModel: ChatViewModel) {

        if (isHost.value == true) {
            chatBleServer.sendData(message, viewModelScope)
            chatViewModel.addMessage(message, isSentByUser = true)
            return
        }


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
            Log.d("sendCoordinatesToServer", "Sending coordinates to client")
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

    fun clearScanResults() {
        scanResults.value = emptyList()
    }

    fun clearCanvas() {
        if (isHost.value) {
            chatBleServer.sendData("CLEAR_CANVAS", viewModelScope)
        } else {
            sendMessage("CLEAR_CANVAS", ChatViewModel())
        }
    }

    fun isServerInitialized(): Boolean {
        return ::chatBleServer.isInitialized
    }

    fun resetBleViewModel() {
        _connection?.close()
        _connection?.disconnect()
        _connection = null
        _characteristic = null
        _coordCharasteristic = null
        isHost.value = false
        connectionState.postValue("")
        clearScanResults()
        isScanning.value = false
        isAdvertising.value = false
    }



}