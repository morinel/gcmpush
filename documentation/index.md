# Titanium Module for Google Cloud Messaging Push Notifications for Android #

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/nl.vanvianen.android.gcm)

A Titanium module for registering a device with Google Cloud Messaging and handling push notifications sent to the device. Both push notifications and topic subscriptions are supported.

1. Install the module as usual in Titanium Studio by downloading the [zip file](https://github.com/morinel/gcmpush/releases/download/1.2/nl.vanvianen.android.gcm-android-1.2.zip) or use ```gittio install nl.vanvianen.android.gcm```
1. Refer to the examples for possibilities.
1. Send a server push notification with your preferred server-side technology to the registrationId returned while registering your device.
1. The callback you specified will then be called.

This module does not require any tiapp.xml properties, all configuration is done in Javascript.

## Example server-side code to send a push notification ##

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


## Register your app for push notifications ##

See [this example](https://github.com/morinel/gcmpush/blob/master/example/app.js).


## Notification settings for push notifications ##

See the [example](https://github.com/morinel/gcmpush/blob/master/example/app.js) for an overview of how to specify the below settings:

1. **smallIcon**: the tiny icon shown at the top of the screen, see this [stackoverflow question](http://stackoverflow.com/questions/28387602/notification-bar-icon-turns-white-in-android-5-lollipop) for details. The file should be placed in the ```platform/android/res/drawable``` directory.
1. **largeIcon**: the large icon shown in the notification bar. If not specified your appicon will be used. The file should be placed in the ```platform/android/res/drawable``` directory.
1. **sound**: the sound file to play while receiving the notification or 'default' for the default sound. The sound file should be placed in the ```platform/android/res/raw``` directory.
1. **vibrate** (true / false): whether vibration should be on, default false.
1. **insistent** (true / false): whether the notification should be [insistent](http://developer.android.com/reference/android/app/Notification.html#FLAG_INSISTENT), default false.
1. **group**: name of group to group similar notifications together, default null.
1. **localOnly** (true / false): whether this notification should be bridged to other devices (false) or is only relevant to this device (true), default true.
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


The settings sound, vibrate, insistent, group, localOnly, priority and bigText can also be set as data in the push message being received (see the server-side example above).

If the app is not active when the notification is received, use gcm.getLastData() to retrieve the contents of the notification and act accordingly to start or resume the app in a suitable way. If you're done, call gcm.clearLastData(), otherwise the same logic will happen when resuming the app again, see the [example](https://github.com/morinel/gcmpush/blob/master/example/app.js).



## Example server-side code to send a message to a topic ##

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
