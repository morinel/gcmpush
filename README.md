# Titanium Module for Google Cloud Messaging Push Notifications for Android #

A Titanium module for registering a device with Google Cloud Messaging and handling push notifications sent to the device.

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

Read the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md).

To build, create a `build.properties` file with the following content:

```
titanium.platform=/Users/###USER###/Library/Application Support/Titanium/mobilesdk/osx/7.0.0.GA/android
android.platform=/Users/###USER###/Library/Android/sdk/platforms/android-26
google.apis=/Users/###USER###/Library/Android/sdk/add-ons/addon-google_apis-google-23
android.ndk=/Users/###USER###/Library/Android/ndk
```

Make sure your paths are correct for your system setup. Then run:

```
$ appc run -p android --build-only
```

A zip file will be created in the `dist` folder.
