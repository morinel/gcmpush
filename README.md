# Titanium Module for Google Cloud Messaging Push Notifications for Android #

A Titanium module for registering a device with Google Cloud Messaging and handling push notifications sent to the device.

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

Read the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md).

## Building

Version 2.0+ of this module is only compatible with Titanium SDK 6+. Currently, Android NDK r11 is required to build the module without errors.

[Windows 32-bit](https://dl.google.com/android/repository/android-ndk-r11-windows-x86.zip) | [Windows 64-bit](https://dl.google.com/android/repository/android-ndk-r11-windows-x86_64.zip) | [Mac OS X 64-bit](https://dl.google.com/android/repository/android-ndk-r11-darwin-x86_64.zip) | [Linux 64-bit](https://dl.google.com/android/repository/android-ndk-r11-linux-x86_64.zip)

Create a `build.properties` file with the following content:

```
titanium.platform=/Users/###USER###/Library/Application Support/Titanium/mobilesdk/osx/6.0.0.GA/android
android.platform=/Users/###USER###/Library/Android/sdk/platforms/android-23
google.apis=/Users/###USER###/Library/Android/sdk/add-ons/addon-google_apis-google-23
android.ndk=/Users/###USER###/Library/Android/android-ndk-r11
```

Make sure your paths are correct for your system setup. Then run:

```
$ ant clean
$ ant
```

A zip file will be created in the `dist` folder.
