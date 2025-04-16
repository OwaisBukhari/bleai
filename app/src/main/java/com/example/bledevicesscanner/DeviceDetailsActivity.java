package com.example.bledevicesscanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bledevicesscanner.adapter.CharacteristicsAdapter;
import com.example.bledevicesscanner.ble.BleManager;
import com.example.bledevicesscanner.model.BleCharacteristic;
import com.example.bledevicesscanner.model.BleDevice;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class DeviceDetailsActivity extends AppCompatActivity implements CharacteristicsAdapter.OnCharacteristicActionListener, BleManager.BleManagerCallback {

    public static final String EXTRA_DEVICE_ADDRESS = "extra_device_address";
    public static final String EXTRA_DEVICE_NAME = "extra_device_name";
    public static final String EXTRA_DEVICE_RSSI = "extra_device_rssi";

    private BleManager bleManager;
    private CharacteristicsAdapter characteristicsAdapter;
    private BleDevice currentDevice;

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRssiTextView;
    private TextView connectionStatusTextView;
    private MaterialButton connectButton;
    private RecyclerView characteristicsRecyclerView;
    private TextView emptyCharacteristicsView;

    private String deviceAddress;
    private String deviceName;
    private int deviceRssi;

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getAddress().equals(deviceAddress)) {
                // Found our device
                currentDevice = new BleDevice(result.getDevice(), result.getRssi(), result.getScanRecord() != null ? result.getScanRecord().getBytes() : null);
                bleManager.connectToDevice(currentDevice);
                bluetoothLeScanner.stopScan(this);
            }
        }
    };

    public DeviceDetailsActivity() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        // Extract intent extras
        deviceAddress = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        deviceName = getIntent().getStringExtra(EXTRA_DEVICE_NAME);
        deviceRssi = getIntent().getIntExtra(EXTRA_DEVICE_RSSI, 0);

        if (deviceAddress == null) {
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        deviceNameTextView = findViewById(R.id.deviceNameTextView);
        deviceAddressTextView = findViewById(R.id.deviceAddressTextView);
        deviceRssiTextView = findViewById(R.id.deviceRssiTextView);
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        connectButton = findViewById(R.id.connectButton);
        characteristicsRecyclerView = findViewById(R.id.characteristicsRecyclerView);
        emptyCharacteristicsView = findViewById(R.id.emptyCharacteristicsView);

        // Set up device info
        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRssiTextView.setText(deviceRssi + " dBm");

        // Set up connect button
        connectButton.setOnClickListener(v -> {
            if (bleManager.isConnected()) {
                bleManager.disconnect();
            } else {
                findAndConnectToDevice();
            }
        });

        // Set up RecyclerView
        characteristicsAdapter = new CharacteristicsAdapter(this);
        characteristicsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        characteristicsRecyclerView.setAdapter(characteristicsAdapter);

        // Initialize BLE Manager
        bleManager = new BleManager(this);
        bleManager.addCallback(this);

        updateConnectionStatus(false);
        updateCharacteristicsVisibility();
    }

    @Override
    protected void onDestroy() {
        if (bleManager != null) {
            bleManager.disconnect();
            bleManager.removeCallback(this);
            bleManager.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findAndConnectToDevice() {
        // Set up scan filter for the specific device
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceAddress(deviceAddress)
                .build();

        // Set up scan settings
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Start scan to find the device
        updateConnectionStatus(false);
        connectionStatusTextView.setText(R.string.connecting);
        bluetoothLeScanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback);
    }

    private void updateConnectionStatus(boolean connected) {
        connectionStatusTextView.setText(connected ? R.string.connected : R.string.disconnected);
        connectionStatusTextView.setTextColor(getResources().getColor(connected ? R.color.green : R.color.red, null));
        connectButton.setText(connected ? R.string.disconnect : R.string.connect);
    }

    private void updateCharacteristicsVisibility() {
        if (characteristicsAdapter.isEmpty()) {
            emptyCharacteristicsView.setVisibility(View.VISIBLE);
            characteristicsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyCharacteristicsView.setVisibility(View.GONE);
            characteristicsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // CharacteristicsAdapter.OnCharacteristicActionListener implementation
    @Override
    public void onReadCharacteristic(BleCharacteristic characteristic) {
        if (bleManager.isConnected()) {
            bleManager.readCharacteristic(characteristic);
        } else {
            Snackbar.make(characteristicsRecyclerView, "Device not connected", Snackbar.LENGTH_SHORT).show();
        }
    }

    // BleManager.BleManagerCallback implementation
    @Override
    public void onScanStarted() {
        // Not used in this activity
    }

    @Override
    public void onScanFinished() {
        // Not used in this activity
    }

    @Override
    public void onScanFailed(int errorCode) {
        // Not used in this activity
    }

    @Override
    public void onDeviceFound(BleDevice device) {
        // Not used in this activity
    }

    @Override
    public void onConnecting() {
        connectionStatusTextView.setText(R.string.connecting);
    }

    @Override
    public void onDeviceConnected() {
        updateConnectionStatus(true);
    }

    @Override
    public void onDeviceDisconnected() {
        updateConnectionStatus(false);
        characteristicsAdapter.setCharacteristics(null);
        updateCharacteristicsVisibility();
    }

    @Override
    public void onConnectionFailed(int status) {
        updateConnectionStatus(false);
        Snackbar.make(characteristicsRecyclerView, 
                "Connection failed with status: " + status, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onServicesDiscovered(List<BleCharacteristic> characteristics) {
        characteristicsAdapter.setCharacteristics(characteristics);
        updateCharacteristicsVisibility();
    }

    @Override
    public void onCharacteristicRead(BleCharacteristic characteristic) {
        characteristicsAdapter.updateCharacteristic(characteristic);
    }

    @Override
    public void onCharacteristicChanged(BleCharacteristic characteristic) {
        characteristicsAdapter.updateCharacteristic(characteristic);
    }
} 