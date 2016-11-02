# Titanium Module for Google Cloud Messaging Push Notifications for Android #

A Titanium module for registering a device with Google Cloud Messaging and handling push notifications sent to the device.

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

Read the [documentation](https://github.com/morinel/gcmpush/blob/master/documentation/index.md).

To build, create a `build.properties` file with the following content:

```
titanium.platform=/Users/###USER###/Library/Application Support/Titanium/mobilesdk/osx/5.1.2.GA/android
android.platform=/Users/###USER###/Library/Android/sdk/platforms/android-23
google.apis=/Users/###USER###/Library/Android/sdk/add-ons/addon-google_apis-google-23
android.ndk=/Users/###USER###/Library/Android/ndk
```

Make sure your paths are correct for your system setup. Then run:

```
$ ant clean
$ ant
```

Add the code below to your code to extract data from notification and just forget the callback function that used to get the gcm's lastData.

```
if (Ti.UI.Android) {
    var bc = Ti.Android.createBroadcastReceiver({
        onReceived: function(e) {
            var gcm = require("nl.vanvianen.android.gcm");
            gcm.startApp(); // this line of code get your app to foreground
            var d = JSON.parse(e.intent.getStringExtra("datas")); // now you get the data, do whatever you want
        }
    });
    Ti.Android.registerBroadcastReceiver(bc, ["nl.vanvianen.android.DataReceiver"]);
}
```

A zip file will be created in the `dist` folder.
