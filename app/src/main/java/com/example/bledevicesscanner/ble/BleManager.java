package com.example.bledevicesscanner.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bledevicesscanner.model.BleCharacteristic;
import com.example.bledevicesscanner.model.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class BleManager {
    private static final String TAG = "BleManager";
    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<BleManagerCallback> callbacks = new ArrayList<>();

    private boolean isScanning = false;
    private BluetoothGatt bluetoothGatt;
    private boolean isConnected = false;

    // Callback for BLE scan results
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            byte[] scanRecord = result.getScanRecord() != null ? result.getScanRecord().getBytes() : null;
            BleDevice bleDevice = new BleDevice(device, rssi, scanRecord);

            // Notify listeners
            for (BleManagerCallback callback : callbacks) {
                callback.onDeviceFound(bleDevice);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan failed with error code: " + errorCode);
            for (BleManagerCallback callback : callbacks) {
                callback.onScanFailed(errorCode);
            }
            stopScan();
        }
    };

    // Callback for GATT operations
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    isConnected = true;
                    
                    // Notify listeners
                    handler.post(() -> {
                        for (BleManagerCallback callback : callbacks) {
                            callback.onDeviceConnected();
                        }
                    });
                    
                    // Discover services
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    isConnected = false;
                    
                    // Notify listeners
                    handler.post(() -> {
                        for (BleManagerCallback callback : callbacks) {
                            callback.onDeviceDisconnected();
                        }
                    });
                    
                    // Close GATT
                    gatt.close();
                    bluetoothGatt = null;
                }
            } else {
                Log.w(TAG, "Connection state change failed with status: " + status);
                isConnected = false;
                
                // Notify listeners
                handler.post(() -> {
                    for (BleManagerCallback callback : callbacks) {
                        callback.onConnectionFailed(status);
                    }
                });
                
                // Close GATT
                gatt.close();
                bluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered.");
                List<BluetoothGattService> services = gatt.getServices();
                List<BleCharacteristic> characteristics = new ArrayList<>();
                
                // Extract all characteristics from all services
                for (BluetoothGattService service : services) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        characteristics.add(new BleCharacteristic(characteristic));
                    }
                }
                
                // Notify listeners
                handler.post(() -> {
                    for (BleManagerCallback callback : callbacks) {
                        callback.onServicesDiscovered(characteristics);
                    }
                });
            } else {
                Log.w(TAG, "Service discovery failed with status: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Characteristic read successfully: " + characteristic.getUuid().toString());
                final BleCharacteristic bleCharacteristic = new BleCharacteristic(characteristic);
                
                // Notify listeners
                handler.post(() -> {
                    for (BleManagerCallback callback : callbacks) {
                        callback.onCharacteristicRead(bleCharacteristic);
                    }
                });
            } else {
                Log.w(TAG, "Characteristic read failed with status: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "Characteristic changed: " + characteristic.getUuid().toString());
            final BleCharacteristic bleCharacteristic = new BleCharacteristic(characteristic);
            
            // Notify listeners
            handler.post(() -> {
                for (BleManagerCallback callback : callbacks) {
                    callback.onCharacteristicChanged(bleCharacteristic);
                }
            });
        }
    };

    // Constructor
    public BleManager(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    // Add callback listener
    public void addCallback(BleManagerCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    // Remove callback listener
    public void removeCallback(BleManagerCallback callback) {
        callbacks.remove(callback);
    }

    // Start scanning for BLE devices
    public boolean startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }

        if (isScanning) {
            return true;
        }

        // Configure scan settings
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Start scan
        handler.postDelayed(this::stopScan, SCAN_PERIOD);
        bluetoothLeScanner.startScan(null, settings, scanCallback);
        isScanning = true;

        // Notify listeners
        for (BleManagerCallback callback : callbacks) {
            callback.onScanStarted();
        }

        return true;
    }

    // Stop scanning for BLE devices
    public void stopScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || !isScanning) {
            return;
        }

        isScanning = false;
        bluetoothLeScanner.stopScan(scanCallback);

        // Notify listeners
        for (BleManagerCallback callback : callbacks) {
            callback.onScanFinished();
        }
    }

    // Connect to a BLE device
    public boolean connectToDevice(BleDevice device) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }

        // Disconnect from any existing connection
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        // Connect to the device
        bluetoothGatt = device.getDevice().connectGatt(context, false, gattCallback);
        
        // Notify listeners
        for (BleManagerCallback callback : callbacks) {
            callback.onConnecting();
        }
        
        return true;
    }

    // Disconnect from a BLE device
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    // Read a characteristic
    public boolean readCharacteristic(BleCharacteristic characteristic) {
        if (bluetoothGatt == null || !isConnected) {
            return false;
        }

        BluetoothGattCharacteristic gattCharacteristic = characteristic.getCharacteristic();
        return bluetoothGatt.readCharacteristic(gattCharacteristic);
    }

    // Check if Bluetooth is enabled
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // Check if device is connected
    public boolean isConnected() {
        return isConnected;
    }

    // Check if currently scanning
    public boolean isScanning() {
        return isScanning;
    }

    // Clean up resources
    public void close() {
        stopScan();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        callbacks.clear();
    }

    // Interface for callback listeners
    public interface BleManagerCallback {
        void onScanStarted();
        void onScanFinished();
        void onScanFailed(int errorCode);
        void onDeviceFound(BleDevice device);
        void onConnecting();
        void onDeviceConnected();
        void onDeviceDisconnected();
        void onConnectionFailed(int status);
        void onServicesDiscovered(List<BleCharacteristic> characteristics);
        void onCharacteristicRead(BleCharacteristic characteristic);
        void onCharacteristicChanged(BleCharacteristic characteristic);
    }
} 