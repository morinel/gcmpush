# Titanium Module for Firebase Cloud Messaging Push Notifications for Android #

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

A Titanium module for registering a device with Firebase Cloud Messaging and handling push notifications sent to the device. Both push notifications and topic subscriptions are supported.

1. Install the module as usual in Appcelerator Studio by downloading the zip file of the [latest release](https://github.com/morinel/gcmpush/releases/latest) or use `gittio install nl.vanvianen.android.gcm`
1. Set up your [Firebase project](https://console.firebase.google.com/). You **MUST** provide your SHA1 from your keystore to Firebase in order for messaging to work! See the Firebase SHA1 section below.
1. Download the `google-services.json` file and put it in `PROJECT_FOLDER/app/assets/android/` (or `PROJECT_FOLDER/Resources/android/` for non-Alloy projects).
1. Refer to the examples for possibilities.
1. Send a server push notification with your preferred server-side technology to the registrationId returned from calling `registerPush()`.
1. The callback you specified will then be called.

This module does not require any tiapp.xml properties, all configuration is done in Javascript.


## Firebase SHA1 ##

In order for your app to authenticate properly with Google Play Services and receive messages, you must provide the SHA1 from the keystore used to sign your app in the Firebase project settings. Your debug keystore SHA1 can be found using this command:

```
$ keytool -list -v -keystore "/Users/USERNAME/.android/debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

If you use a custom keystore for release, use a similar command to get that SHA1:

```
$ keytool -list -v -keystore /path/to/yourappkeystore.keystore
```

**IMPORTANT**: REMEMBER TO RE-DOWNLOAD YOUR `google-services.json` FILE AFTER ADDING AN SHA1!


## Version 3 Ugrade Guide ##

The hardest part about upgrading this module to v3 is the external configuration of Firebase. Most of your code should work the same, with one notable addition. You may need to add a `registration` callback in the `registerPush` parameters which handles updating your stored devide token (AKA registration ID) with whatever push notification service you are using. If you are using Firebase directly for sending push notifications, this won't be necessary.

You can also remove the `senderId` option in the `registerPush` parameters, as that is handled with the `google-services.json` file.


## Register your app for push notifications ##

See [this example](https://github.com/morinel/gcmpush/blob/master/example/app.js).

It is not required to define `firebaseFile` or `firebaseConfig` if you have put your `google-services.json` file in the correct place. However, these options are available for advanced configurations. If your json file is named differently or is located in a subfolder, you must pass the path to the `firebaseFile` option.

Alternatively, you can copy the JSON out of the file and pass it directly with the `firebaseConfig` option. This is useful if you have different Firebase projects for dev and production—you can pass different JSON depending on the build type, and if you're using Alloy you can add it to your config.json file and pass `Alloy.CFG.firebase`.

```
var gcm = require("nl.vanvianen.android.gcm");

gcm.registerPush({
    /* Firebase config file (if you've renamed it or if it's in a subfolder) */
    firebaseFile: 'somepath/google-services.json',
    /* Firebase config JSON */
    firebaseConfig: {
        # ...
    },
    notificationSettings: {
        sound: 'mysound.mp3', /* Place sound file in app/platform/android/res/raw/mysound.mp3 */
        smallIcon: 'notification_icon.png',  /* Place icon in app/platform/android/res/drawable/notification_icon.png */
        largeIcon: 'appicon.png',  /* Same */
        vibrate: true,  /* Whether the phone should vibrate */
        insistent: true,  /* Whether the notification should be insistent */
        group: 'MyNotificationGroup',  /* Name of group to group similar notifications together */
        localOnly: false,  /* Whether this notification should be bridged to other devices */
        priority: +2,  /* Notification priority, from -2 to +2 */
        bigText: false,
        /* You can also set a static value for title, message, or ticker. If you set a value here, the key will be ignored. */
        // title: '',
        // message: '',
        // ticker: ''
        /* Add LED flashing */
        ledOn: 200,
        ledOff: 300
        /* Android O channels */
        channelId: 'my_channel',
        channelName: 'My Channel'
    },
    success: function (event) {
        Ti.API.debug("Push registration success: " + JSON.stringify(event));

        /* Add code to send event.registrationId to your server */
    },
    error: function (event) {
        Ti.API.debug("Push registration error: " + JSON.stringify(event));
        alert(event.error);
    },
    callback: function (event) {
        Ti.API.debug("Push callback: " + JSON.stringify(event));
        /* Called when a notification is received and the app is in the foreground */
        
        var dialog = Ti.UI.createAlertDialog({
            title: 'Push received',
            message: JSON.stringify(event.data),
            buttonNames: ['View','Cancel'],
            cancel: 1
        });
        dialog.addEventListener("click", function(event) {
            dialog.hide();
            if (event.index == 0) {
                /* Do stuff to view the notification */
            }
        });
        dialog.show();
    },
    registration: function (event) {
        Ti.API.debug("Registration callback: " + JSON.stringify(event));
        /* Called when the registration token has changed */

        /* Add code to send event.registrationId to your server */
    }
});
```


## Notification settings for push notifications ##

See the [example](https://github.com/morinel/gcmpush/blob/master/example/app.js) for an overview of how to specify the below settings:

1. **smallIcon**: the tiny icon shown at the top of the screen, see this [stackoverflow question](http://stackoverflow.com/questions/28387602/notification-bar-icon-turns-white-in-android-5-lollipop) for details. The file should be placed in the ```platform/android/res/drawable``` directory.
1. **largeIcon**: the large icon shown in the notification bar. If not specified your appicon will be used. The file should be placed in the ```platform/android/res/drawable``` directory.
1. **sound**: the sound file to play while receiving the notification or 'default' for the default sound. The sound file should be placed in the ```platform/android/res/raw``` directory.
1. **vibrate** (true / false): whether vibration should be on, default false.
1. **insistent** (true / false): whether the notification should be [insistent](http://developer.android.com/reference/android/app/Notification.html#FLAG_INSISTENT), default false.
1. **group**: name of group to group similar notifications together, default null.
1. **localOnly** (true / false): whether this notification should be bridged to other devices (false) or is only relevant to this device (true), default true.
1. **backgroundOnly** (true / false): whether the app should only be notified when it's in the background, default false.
1. **priority**: (integer) specifies the priority of the notification, should be between [PRIORITY_MIN](http://developer.android.com/reference/android/support/v4/app/NotificationCompat.html#PRIORITY_MIN) and [PRIORITY_MAX](http://developer.android.com/reference/android/support/v4/app/NotificationCompat.html#PRIORITY_MAX), default 0.
1. **bigText** (true / false): whether this notification should use the [bigText style](http://developer.android.com/reference/android/app/Notification.BigTextStyle.html), default false.
1. **titleKey** (string): specify a custom key name for the notification title sent by the server, default ```title```
1. **messageKey** (string): specify a custom key name for the notification message sent by the server, default ```message```
1. **tickerKey** (string): specify a custom key name for the notification ticker text sent by the server, default ```ticker```
1. **title** (string): specify a static title for the notification (server data will be ignored)
1. **message** (string): specify a static message for the notification (server data will be ignored)
1. **ticker** (string): specify a static ticker for the notification (server data will be ignored)
1. **ledOn** (integer): the number of ms the LED should be on while flashing, see  [javadoc](http://developer.android.com/reference/android/app/Notification.html#ledOnMS)
1. **ledOff** (integer): the number of ms the LED should be off while flashing, see [javadoc](http://developer.android.com/reference/android/app/Notification.html#ledOffMS)
1. **notificationId** (integer): a (unique) integer to identify the notification. If specified, subsequent notifications will not override the previous one.
1. **channelId** (string): [Android O] a unique identifier for the channel the notification belongs to, should be lower-case with underscores
1. **channelName** (string) [Android O] a human-readable name for the notification channel


The settings sound, vibrate, insistent, group, localOnly, priority, bigText and notificationId can also be set as data in the push message being received (see the server-side example above).

If the app is not active when the notification is received, use gcm.getLastData() to retrieve the contents of the notification and act accordingly to start or resume the app in a suitable way. If you're done, call gcm.clearLastData(), otherwise the same logic will happen when resuming the app again, see the [example](https://github.com/morinel/gcmpush/blob/master/example/app.js).


## Testing notifications in your app ##

There currently seems to be an issue which causes the registration token to be invalidated when rebuilding the app after having already built & installed. If you are not receiving notifications during development, you may need to uninstall the app from your device and rebuild/reinstall.


## Example server-side Java code to send a push notification ##

**THIS EXAMPLE NO LONGER APPLIES WITH FCM**

Use the following dependency:

```xml
<dependency>
    <groupId>com.google.android.gcm</groupId>
    <artifactId>gcm-server</artifactId>
    <version>1.0.2</version>
</dependency>
```

```java
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

...

public void sendPush() {
    Sender sender = new Sender(APIKEY);
    Message msg = new Message.Builder()
        .addData("title", "Lorem ipsum dolor sit amet")
        .addData("message", "Lorem ipsum dolor sit amet")
        .addData("sound", "mysound.mp3")
        .addData("vibrate", "true")
        .addData("insistent", "true")
        .addData("priority", "2")
        .addData("localOnly", "false")
        .addData("group", "mygroup")
        .addData("bigText", "true")
        .addData("ledOn", "200")
        .addData("ledOff", "300")
        .addData("notificationId", "12345");
        .build();
    try {
        /* Use the registrationIds returned in the success handler in the apps registerPush() call. */
        List<String> list = new ArrayList<>(REGISTRATION_IDS);
        MulticastResult result = sender.send(msg, list, 1);
        log.info("Total = " + result.getTotal() + ", success = " + result.getSuccess() + ", failure = " + result.getFailure());
    } catch (IOException ex) {
        log.error("Cannot send Android push notification: " + ex.getMessage(), ex);
    }
}
```


## Example server-side code to send message to a topic ##

**THIS EXAMPLE NO LONGER APPLIES WITH FCM**

```java
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

...

public void sendTopic(e) throws Exception {
    // Prepare JSON containing the GCM message content. What to send and where to send.
    JSONObject json = new JSONObject();
    JSONObject data = new JSONObject();
    data.put("message", "Lorem ipsum dolor sit amet");
    // Add any other notification settings here, see the push notification server-side example

    json.put("to", "/topics/mytopic");
    json.put("data", data);

    // Create connection to send GCM Message request.
    URL url = new URL("https://android.googleapis.com/gcm/send");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Authorization", "key=" + APIKEY);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);

    // Send GCM message content.
    String content = json.toString();
    System.out.println(content);
    OutputStream outputStream = conn.getOutputStream();
    outputStream.write(content.getBytes());

    // Read GCM response.
    InputStream inputStream = conn.getInputStream();
    System.out.println(IOUtils.toString(inputStream));
}
```


## Subscribe your app to a topic ##

See [this example](https://github.com/morinel/gcmpush/blob/master/example/topic.js).

The same notification settings apply as for regular push notifications.
