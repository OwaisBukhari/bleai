package com.example.bledevicesscanner;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bledevicesscanner.adapter.DevicesAdapter;
import com.example.bledevicesscanner.ble.BleManager;
import com.example.bledevicesscanner.model.BleCharacteristic;
import com.example.bledevicesscanner.model.BleDevice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DevicesAdapter.OnDeviceClickListener, BleManager.BleManagerCallback {

    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    private BleManager bleManager;
    private DevicesAdapter devicesAdapter;
    private RecyclerView devicesRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private FloatingActionButton scanFab;

    private final ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    startScan();
                } else {
                    showToast(getString(R.string.bluetooth_disabled));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyView = findViewById(R.id.emptyView);
        scanFab = findViewById(R.id.scanFab);

        // Set up RecyclerView
        devicesAdapter = new DevicesAdapter(this);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(devicesAdapter);

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshDevicesList);

        // Set up FAB
        scanFab.setOnClickListener(v -> {
            if (bleManager != null && bleManager.isScanning()) {
                bleManager.stopScan();
            } else {
                startScan();
            }
        });

        // Initialize BLE Manager
        initBleManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bleManager != null) {
            bleManager.addCallback(this);
        }
        updateEmptyView();
    }

    @Override
    protected void onPause() {
        if (bleManager != null) {
            bleManager.removeCallback(this);
            bleManager.stopScan();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (bleManager != null) {
            bleManager.close();
        }
        super.onDestroy();
    }

    private void initBleManager() {
        // Check if BLE is supported
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(getString(R.string.bluetooth_not_supported));
            finish();
            return;
        }

        // Initialize BluetoothManager and BleManager
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            showToast(getString(R.string.bluetooth_not_supported));
            finish();
            return;
        }

        bleManager = new BleManager(this);
        bleManager.addCallback(this);
    }

    private void startScan() {
        // Check permissions
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bleManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
            return;
        }

        // Clear devices list and start scanning
        devicesAdapter.clearDevices();
        updateEmptyView();
        bleManager.startScan();
    }

    private void refreshDevicesList() {
        devicesAdapter.clearDevices();
        updateEmptyView();
        startScan();
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startScan();
            } else {
                Snackbar.make(devicesRecyclerView, R.string.permission_rationale, Snackbar.LENGTH_LONG)
                        .setAction("Grant", v -> requestPermissions())
                        .show();
            }
        }
    }

    private void updateEmptyView() {
        if (devicesAdapter.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            devicesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            devicesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // DevicesAdapter.OnDeviceClickListener implementation
    @Override
    public void onDeviceClick(BleDevice device) {
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(DeviceDetailsActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());
        intent.putExtra(DeviceDetailsActivity.EXTRA_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceDetailsActivity.EXTRA_DEVICE_RSSI, device.getRssi());
        startActivity(intent);
    }

    // BleManager.BleManagerCallback implementation
    @Override
    public void onScanStarted() {
        swipeRefreshLayout.setRefreshing(true);
        scanFab.setImageResource(android.R.drawable.ic_media_pause);
    }

    @Override
    public void onScanFinished() {
        swipeRefreshLayout.setRefreshing(false);
        scanFab.setImageResource(android.R.drawable.ic_search_category_default);
    }

    @Override
    public void onScanFailed(int errorCode) {
        swipeRefreshLayout.setRefreshing(false);
        scanFab.setImageResource(android.R.drawable.ic_search_category_default);
        showToast("Scan failed with error code: " + errorCode);
    }

    @Override
    public void onDeviceFound(BleDevice device) {
        devicesAdapter.addDevice(device);
        updateEmptyView();
    }

    @Override
    public void onConnecting() {
        // Not used in this activity
    }

    @Override
    public void onDeviceConnected() {
        // Not used in this activity
    }

    @Override
    public void onDeviceDisconnected() {
        // Not used in this activity
    }

    @Override
    public void onConnectionFailed(int status) {
        // Not used in this activity
    }

    @Override
    public void onServicesDiscovered(List<BleCharacteristic> characteristics) {
        // Not used in this activity
    }

    @Override
    public void onCharacteristicRead(BleCharacteristic characteristic) {
        // Not used in this activity
    }

    @Override
    public void onCharacteristicChanged(BleCharacteristic characteristic) {
        // Not used in this activity
    }
} 