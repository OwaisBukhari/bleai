# Add project specific ProGuard rules here.

# Keep BleManager and related classes
-keep class com.example.bledevicesscanner.ble.BleManager { *; }
-keep class com.example.bledevicesscanner.ble.BleManager$* { *; }

# Keep adapter classes
-keep class com.example.bledevicesscanner.adapter.** { *; }

# Keep model classes
-keep class com.example.bledevicesscanner.model.** { *; }

# For lambda expressions in Java 8
-keepclassmembers class * {
    private static synthetic java.lang.Object $deserializeLambda$(java.lang.invoke.SerializedLambda);
}
-keepclassmembernames class * {
    private static synthetic *** lambda$*(...);
}

# Keep annotations used for lambda
-dontwarn java.lang.invoke.**
-dontwarn java.lang.reflect.**
-dontwarn **$$Lambda$*

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Bluetooth related classes
-keep class android.bluetooth.** { *; } 