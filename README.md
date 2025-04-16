# BLE Device Scanner

A professional, minimal Android application for scanning and interacting with Bluetooth Low Energy (BLE) devices.

## Features

- Scan for nearby BLE devices
- Display device name, address, and signal strength
- Connect to devices and discover services/characteristics
- Read characteristic values
- Simple, clean Material Design UI

## Requirements

- Android Studio 4.2+ 
- Android SDK 21+ (Android 5.0 Lollipop or higher)
- Device with Bluetooth 4.0+ support

## Project Structure

The application follows a clean architecture approach:

- **model:** Data models for BLE devices and characteristics
- **adapter:** RecyclerView adapters for displaying devices and characteristics
- **ble:** Core BLE functionality for scanning, connecting, and reading characteristics

## Permissions

The app requires the following permissions:

- `BLUETOOTH` - Required for using Bluetooth functionality
- `BLUETOOTH_ADMIN` - Required for controlling Bluetooth (scan, connect)
- `BLUETOOTH_SCAN` - Required for scanning on Android 12+ (API 31+)
- `BLUETOOTH_CONNECT` - Required for connecting on Android 12+ (API 31+)
- `ACCESS_FINE_LOCATION` - Required for BLE scanning on Android 6.0+ (API 23+)

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Build and run on a compatible device

## Usage

1. Launch the app
2. Grant necessary permissions when prompted
3. Tap the floating action button to start scanning for BLE devices
4. Tap on a device to view its details and connect
5. Once connected, the app will display the device's characteristics
6. Tap "Read" on readable characteristics to retrieve their values

## License

This project is licensed under the MIT License - see the LICENSE file for details. 