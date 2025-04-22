package com.example.mobiilisovellusprojekti.ViewModels

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
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


    // List of connections connected to the server
    private val _connectedDevices = mutableListOf<ServerConnectionEvent.DeviceConnected>()
    val connectedDevices: List<ServerConnectionEvent.DeviceConnected>
        get() = _connectedDevices

    private val _state = MutableStateFlow(AdvertisingState(isAdvertising = false))
    val state: StateFlow<AdvertisingState> = _state

    val data = "This is a long message that needs to be truncated"
    val truncatedData = if (data.toByteArray().size > 31) {
        data.toByteArray().copyOf(31) // Limit to 31 bytes
    } else {
        data.toByteArray()
    }

    val advertiseData = BleAdvertisingData(
        serviceUuid = ParcelUuid(BleViewModel.GAME_UUID),
        includeDeviceName = false,
    )


    // Creating advertiser object
    private val advertiser = BleAdvertiser.create(context)
    val advertiserConfig = BleAdvertisingConfig(
        settings = BleAdvertisingSettings(
            deviceName = "Guess my doodle",
            anonymous = false,
        ),
        advertiseData = advertiseData,
    )



    fun declareServer(
        context: Context,
        viewModelScope: CoroutineScope,
        onServerCreated: (ServerBleGatt) -> Unit
    ) {
        viewModelScope.launch {

            // Define the servers charasteristic configuration on what will be used
            val serverCharasteristics = ServerBleGattCharacteristicConfig(
                BleViewModel.CHARACTERISTIC_UUID, // UUID for the charasteristic
                listOf(
                    BleGattProperty.PROPERTY_READ,
                    BleGattProperty.PROPERTY_WRITE,
                    BleGattProperty.PROPERTY_NOTIFY),
                listOf(
                    BleGattPermission.PERMISSION_READ,
                    BleGattPermission.PERMISSION_WRITE) // What kind of permissions are given for the user connected to the server
            )

            // Defining server's configuration
            val serverConfig = ServerBleGattServiceConfig(
                BleViewModel.SERVICE_UUID,
                ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
                listOf(serverCharasteristics)
            )

            // Creating the actual server
            val server = ServerBleGatt.create(context, this, serverConfig)
            onServerCreated(server)
        }
    }


    // SET UP THE DATA HERE
    fun setUpServices(services: ServerBleGattService, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel) {

        // Define the charasteristic whose data will be defined
        val messageCharacteristic = services.findCharacteristic(BleViewModel.CHARACTERISTIC_UUID)

        // Lambda on what will happen when the charasteristic gest written
        messageCharacteristic?.value?.onEach { data ->
            val message = String(data.value, Charsets.UTF_8)
            Log.d("ChatBleServer", "Received message: $message")
            chatViewModel.addMessage(message, isSentByUser = false)


        }?.launchIn(viewModelScope)
    }


    // When a connection is established what will happen is defined in this function
    fun observeConnections(server: ServerBleGatt, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, onDeviceConnected: () -> Unit) {
        server.connectionEvents
            .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
            .map { it.connection }
            .onEach { connection ->
                _connectedDevices.add(ServerConnectionEvent.DeviceConnected(connection))
                connection.services.findService(BleViewModel.SERVICE_UUID)?.let { service ->
                    setUpServices(service, viewModelScope, chatViewModel)

                    // Notify the devices that connection has been established. In this case move into the next screen
                    onDeviceConnected()
                }
            }.launchIn(viewModelScope)
    }

    // Starting the advertisement of the server
    fun startAdvertising() {
        val requiredPermissions =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_ADVERTISE,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            } else {
                arrayOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                )
            }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.e(
                "ChatBleServer",
                "Missing required Bluetooth permissions: $missingPermissions"
            )
            return
        }

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

    fun startServer(context: Context, viewModelScope: CoroutineScope, chatViewModel: ChatViewModel, onDeviceConnected: () -> Unit) {
        declareServer(context, viewModelScope) { server ->
            observeConnections(server, viewModelScope, chatViewModel,onDeviceConnected)
        }
    }

    // Function to send data and notify the clients that are connected to the server
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

        // Characteristic UUID = Where the data will be located at
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("9c0cd23f-44c1-4d3d-aaa3-7678bf19a218")
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
    private var _charasteristic: ClientBleGattCharacteristic? = null
    val connectionCharasteristic: ClientBleGattCharacteristic?
        get() = _charasteristic

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

    fun startAdvertising(context: Context, chatViewModel: ChatViewModel ,onDeviceConnected: () -> Unit) {
        if (::chatBleServer.isInitialized) {
            chatBleServer.startServer(context, viewModelScope, chatViewModel,onDeviceConnected)
            isAdvertising.value = true
            chatBleServer.startAdvertising()
        } else {
            Log.e("BleViewModel", "ChatBleServer is not initialized")
        }
    }

    fun scanDevices(context: Context) {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.Manifest.permission.BLUETOOTH_SCAN
        } else {
            android.Manifest.permission.BLUETOOTH
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val aggregator = BleScanResultAggregator()
            isScanning.value = true

            val serviceUuid = no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceUuid(
                uuid = ParcelUuid(GAME_UUID)
            )

            val scanFilter = no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter(
                serviceUuid = serviceUuid
            )

            BleScanner(context).scan(listOf(scanFilter))
                .map { aggregator.aggregateDevices(it) }
                .onEach {
                    scanResults.value = it
                    Log.d("onEach", "${it.toString()}")
                    it.forEach { }
                    isScanning.value = false
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

            val service = services.findService(BleViewModel.SERVICE_UUID)
            if (service == null) {
                Log.e("ConnectToDevice", "service was not found")
                return false
            }

            val charasteristic = service.findCharacteristic(CHARACTERISTIC_UUID)
            if (charasteristic == null) {
                Log.e("ConnectToDevice", "No charasteristic found")
                return false
            }
            _connection = connection
            _charasteristic = charasteristic
            connectionState.postValue("Connected to ${device.name ?: "Unknown"}")
        } catch (e: Exception) {
            connectionState.postValue("Failed to connect to ${device.name ?: "Unknown"}: ${e.message}")
            Log.e("BleViewModel", "Connection failed: ${e.message}")
        }
        return true
    }

    fun observeNotifications(context: Context, chatViewModel: ChatViewModel) {
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


    // Ei toimi??
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





}