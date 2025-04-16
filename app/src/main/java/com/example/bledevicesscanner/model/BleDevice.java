package com.example.bledevicesscanner.model;

import android.bluetooth.BluetoothDevice;

/**
 * Model class to hold BLE device information
 */
public class BleDevice {
    private final BluetoothDevice device;
    private int rssi;
    private byte[] scanRecord;

    public BleDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getAddress() {
        return device.getAddress();
    }

    public String getName() {
        String name = device.getName();
        return name != null && !name.isEmpty() ? name : "Unknown Device";
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BleDevice bleDevice = (BleDevice) o;
        return device.getAddress().equals(bleDevice.device.getAddress());
    }

    @Override
    public int hashCode() {
        return device.getAddress().hashCode();
    }
} 