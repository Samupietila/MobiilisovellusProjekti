package com.example.mobiilisovellusprojekti.ViewModels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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

/**
 * This class is responsible for managing the Bluetooth Low Energy (BLE) server functionality.
 * It handles advertising, connection events, and data transfer between devices.
 *
 * @param context The application context.
 * @param coroutineScope The coroutine scope for launching coroutines.
 */
class ChatBleServer(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    private val _connectedDevices = mutableListOf<ServerConnectionEvent.DeviceConnected>()
    val connectedDevices: List<ServerConnectionEvent.DeviceConnected>
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

    /**
     * Declares a BLE server with the specified characteristics and service configuration.
     * @param context The application context.
     * @param viewModelScope The coroutine scope for launching coroutines.
     * @param onServerCreated Callback function to be called when the server is created.
     */
    fun declareServer(
        context: Context,
        viewModelScope: CoroutineScope,
        onServerCreated: (ServerBleGatt) -> Unit
    ) {
        viewModelScope.launch {

            /**
             * The message characteristic is used for sending and receiving messages between devices.
             * It has read, write, and notify properties, allowing the server to send notifications to clients.
             */
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

            /**
             * The coordinates characteristic is used for sending and receiving coordinates between devices.
             * It has read, write, and notify properties, allowing the server to send notifications to clients.
             */
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

            /**
             * The server is created with a primary service that includes the message and coordinates characteristics.
             * The service UUID is used to identify the service on the server.
             */
            val serverConfig = ServerBleGattServiceConfig(
                BleViewModel.SERVICE_UUID,
                ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
                listOf(messageCharasteristics, coordinatesCharasteristics)
            )

            val server = ServerBleGatt.create(context, this, serverConfig)
            onServerCreated(server)
        }
    }

    /**
     * Sets up the services for the connected devices.
     * It handles the message and coordinates characteristics, processing incoming data and updating the UI.
     *
     * @param services The server GATT service to set up.
     * @param viewModelScope The coroutine scope for launching coroutines.
     * @param chatViewModel The chat view model for handling chat messages.
     * @param drawingViewModel The drawing view model for handling drawing actions.
     * @param gameViewModel The game view model for handling game state.
     */
    fun setUpServices(services: ServerBleGattService,
                      viewModelScope: CoroutineScope,
                      chatViewModel: ChatViewModel,
                      drawingViewModel: DrawingViewModel,
                      gameViewModel: GameViewModel
    ) {

        // Search for Charasteristic
        val messageCharacteristic = services.findCharacteristic(BleViewModel.CHARACTERISTIC_UUID)

        // Byte array where chunks will be stored
        val receivedMessageChunks = mutableListOf<ByteArray>()

        messageCharacteristic?.value?.onEach { data ->
            try {
                // Extract the flag and the actual chunk
                val isLastChunk = data.value[0] == 1.toByte()
                val chunk = data.value.copyOfRange(1, data.value.size)

                // Add the chunk to the buffer
                receivedMessageChunks.add(chunk)

                // If it's the last chunk, reassemble and process the message
                if (isLastChunk) {
                    val completeData = receivedMessageChunks.reduce { acc, bytes -> acc + bytes }
                    receivedMessageChunks.clear() // Clear the buffer

                    // Convert the complete data to a string message
                    val message = String(completeData, Charsets.UTF_8)

                    Log.d("SERVER","Received message: $message")
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

                    Log.d("ChatBleServer", "Received complete message: $message")
                }
            } catch (e: Exception) {
                Log.e("ChatBleServer", "Failed to process message chunk: ${e.message}")
            }
        }?.launchIn(viewModelScope)

        // Search for Charasteristic
        val coordinatesCharasteristics = services.findCharacteristic(BleViewModel.COORDINATES_UUID)

        // Byte array where chunks will be stored
        val receivedCoordinateChunks = mutableListOf<ByteArray>()

        // Handle Coordinate and what happens here
        coordinatesCharasteristics?.value?.onEach { data ->
            try {
                Log.d("ChatBleServer", "Received coordinates data: ${data.value}")

                // Extract the flag and the actual chunk
                val isLastChunk = data.value[0] == 1.toByte()
                val chunk = data.value.copyOfRange(1, data.value.size)

                // Add the chunk to the list
                receivedCoordinateChunks.add(chunk)

                // If it's the last chunk, reassemble and process the data
                if (isLastChunk) {
                    val completeData = receivedCoordinateChunks.reduce { acc, bytes -> acc + bytes }
                    receivedCoordinateChunks.clear() // Clear the buffer

                    // Deserialize and update paths
                    val convertedValue = drawingViewModel.deserializePathDataBinary(completeData)
                    drawingViewModel.updatePaths(convertedValue)
                }


            } catch (e: Exception) {
                Log.e("ChatBleServer", "Failed to deserialize coordinates: ${e.message}")
            }
        }?.launchIn(viewModelScope)

    }

    /**
     * Disconnects all connected devices.
     */
    fun disconnectAllDevices() {
        try {
            _connectedDevices.forEach { device ->
                device.connection.connectionScope.cancel()
                Log.d("ChatBleServer", "Device disconnected: ${device.connection.device.address}")
            }
            _connectedDevices.clear()
            Log.d("ChatBleServer", "All devices have been disconnected")
        } catch (e: Exception) {
            Log.e("ChatBleServer", "Failed to disconnect all devices: ${e.message}")
        }
    }

    /**
     * Observes the connection events from the server and sets up services for connected devices.
     *
     * @param server The server instance to observe.
     * @param viewModelScope The coroutine scope for launching coroutines.
     * @param chatViewModel The chat view model for handling chat messages.
     * @param drawingViewModel The drawing view model for handling drawing actions.
     * @param gameViewModel The game view model for handling game state.
     * @param onDeviceConnected Callback function to be called when a device is connected.
     */
    fun observeConnections(server: ServerBleGatt,
                           viewModelScope: CoroutineScope,
                           chatViewModel: ChatViewModel,
                           drawingViewModel: DrawingViewModel,
                           gameViewModel: GameViewModel,
                           onDeviceConnected: () -> Unit) {
        server.connectionEvents
            .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
            .map { it.connection }
            .onEach { connection -> // When ever a new device connects
                _connectedDevices.add(ServerConnectionEvent.DeviceConnected(connection)) // Add them into the connected devices list
                connection.services.findService(BleViewModel.SERVICE_UUID)?.let { service -> // Set up the services for the device
                    setUpServices(service, viewModelScope, chatViewModel, drawingViewModel, gameViewModel)

                    // Notify the devices that connection has been established
                    onDeviceConnected() // Trigger the callback to move into the next screen
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Starts advertising the BLE server.
     * It checks for required permissions and starts the advertising process.
     */
    fun startAdvertising() {

        // Checking if the device has Bluetooth permissions
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
            // Start advertising
            startAdvertisingProcess()
        }

    }

    /**
     * Starts the advertising process and collects the advertising events.
     * It updates the state based on the advertising events.
     */
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

    /**
     * Starts the BLE server and observes the connection events.
     * It sets up the server and handles incoming connections.
     *
     * @param context The application context.
     * @param viewModelScope The coroutine scope for launching coroutines.
     * @param chatViewModel The chat view model for handling chat messages.
     * @param drawingViewModel The drawing view model for handling drawing actions.
     * @param gameViewModel The game view model for handling game state.
     * @param onDeviceConnected Callback function to be called when a device is connected.
     */
    fun startServer(context: Context,
                    viewModelScope: CoroutineScope,
                    chatViewModel: ChatViewModel,
                    drawingViewModel: DrawingViewModel,
                    gameViewModel: GameViewModel,
                    onDeviceConnected: () -> Unit) {

        // Creating the server
        declareServer(context, viewModelScope) { server ->
            observeConnections(server,
                viewModelScope,
                chatViewModel,
                drawingViewModel,
                gameViewModel,
                onDeviceConnected)
        }
    }

    /**
     * Sends the coordinates to the connected devices.
     * It chunks the data and sends it to each connected device.
     *
     * @param drawingState The current drawing state containing the paths.
     * @param viewModelScope The coroutine scope for launching coroutines.
     * @param drawingViewModel The drawing view model for handling drawing actions.
     */
    fun sendCoordinates(drawingState: DrawingState,
                        viewModelScope: CoroutineScope,
                        drawingViewModel: DrawingViewModel) {

        // Check if there are any connected devices
        val connectedDevices = _connectedDevices

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

    /**
     * Sends a message to the connected devices.
     * It chunks the data and sends it to each connected device.
     *
     * @param message The message to be sent.
     * @param viewModelScope The coroutine scope for launching coroutines.
     */
    fun sendMessage(message: String, viewModelScope: CoroutineScope) {
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
                        // Serialize the message
                        val byteData = message.toByteArray(Charsets.UTF_8)

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

                            // Send the chunk
                            characteristic.setValueAndNotifyClient(DataByteArray(chunkWithFlag))
                        }

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

    /**
     * Stops the BLE server and clears the connected devices list.
     * It updates the state to indicate that advertising has stopped.
     */
    fun stopServer() {
        advertiser = BleAdvertiser.create(context)
        _connectedDevices.clear()
        _state.value = AdvertisingState(isAdvertising = false)
    }
}