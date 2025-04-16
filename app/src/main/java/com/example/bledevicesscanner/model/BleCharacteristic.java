package com.example.bledevicesscanner.model;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to hold BLE characteristic information
 */
public class BleCharacteristic {
    private final BluetoothGattCharacteristic characteristic;
    private byte[] value;

    public BleCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        this.value = characteristic.getValue();
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public String getUuid() {
        return characteristic.getUuid().toString();
    }

    public int getProperties() {
        return characteristic.getProperties();
    }

    public List<String> getPropertiesNames() {
        List<String> propertiesNames = new ArrayList<>();
        int properties = characteristic.getProperties();

        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            propertiesNames.add("READ");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            propertiesNames.add("WRITE");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            propertiesNames.add("WRITE NO RESPONSE");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            propertiesNames.add("NOTIFY");
        }
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            propertiesNames.add("INDICATE");
        }
        if (propertiesNames.isEmpty()) {
            propertiesNames.add("NONE");
        }

        return propertiesNames;
    }

    public boolean isReadable() {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    public boolean isWritable() {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    public boolean isNotifiable() {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getValueAsString() {
        if (value == null) {
            return "No value";
        }

        // Try to convert to UTF-8 string if possible
        try {
            String utf8String = new String(value, "UTF-8");
            if (isPrintable(utf8String)) {
                return utf8String;
            }
        } catch (Exception e) {
            // If not convertible to UTF-8, fall back to hex representation
        }

        // Hex representation
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public String getPropertiesString() {
        return String.join(", ", getPropertiesNames());
    }

    private boolean isPrintable(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isPrintableChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isPrintableChar(char c) {
        return c >= 32 && c < 127;
    }
} 