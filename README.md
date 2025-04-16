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

## Setup Instructions

### Building the Project

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/bledevicesscanner.git
   ```
   
2. Open the project in Android Studio

3. Run a clean build:
   ```
   ./gradlew clean (Linux/Mac)
   gradlew.bat clean (Windows)
   ```

4. Build the project:
   ```
   ./gradlew build (Linux/Mac)
   gradlew.bat build (Windows)
   ```

### Troubleshooting Common Issues

If you encounter `NullPointerException` during D8 dexing:

1. Make sure MultiDex is enabled in app/build.gradle
2. Verify that ProGuard is properly configured
3. Ensure all the necessary dependencies are included

### Debugging

When debugging Bluetooth functionality:
1. Enable Developer Options on your Android device
2. In Developer Options, enable "Bluetooth HCI snoop log"
3. Use a Bluetooth packet analyzer to view logs

## License

This project is licensed under the MIT License - see the LICENSE file for details. 