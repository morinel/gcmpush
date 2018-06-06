# Titanium Module for Firebase Cloud Messaging Push Notifications for Android #

A Titanium module for registering a device with Firebase Cloud Messaging and handling push notifications sent to the device.

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

Read the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md).

## Quickstart

1. Install this module using Axway Studio or via the CLI with `gittio install nl.vanvianen.android.gcm`
2. Set up your [Firebase project](https://console.firebase.google.com/)
3. Download the `google-services.json` file and put it in `PROJECT_FOLDER/app/assets/android/`.
4. You are ready to receive Android push notifications! Check the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md) for further instructions and examples.

## GCM to FCM Migration

If you are upgrading this module to version 3.0+, you will need to migrate to Firebase (Google will discontinue GCM in April 2019):

1. In the [Firebase console](https://console.firebase.google.com/), select __Add Project__
2. Select your GCM project from the list of existing Google Cloud Projects, and select __Add Firebase__
3. In the Firebase welcome screen, select __Add Firebase to your Android App__
4. Provide your package name (and optionally a SHA-1) and select __Add App__. A new `google-services.json` file for your Firebase app is downloaded. Place this file in `PROJECT_FOLDER/app/assets/android/`.
5. Update to version 3.0+ of this module
6. Check the [Upgrade Guide](https://github.com/morinel/gcmpush/blob/master/documentation/index.md#version-3-upgrade-guide) for instructions about what needs to be altered in your code

## Build

To build this module, you will need the prerequisites described in the [Android Module Project documentation](https://docs.appcelerator.com/platform/latest/#!/guide/Android_Module_Project).

Then, simply `cd` into the module directory and run the following command:

```
$ appc ti build -p android --build-only
```

OR, if you are using just the open-source Titanium SDK:

```
$ ti build -p android --build-only
```

A zip file will be created in the `dist` folder.
