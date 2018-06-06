/**
 * Copyright 2015  Jeroen van Vianen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.vanvianen.android.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Kroll.module(name = "Gcm", id = "nl.vanvianen.android.gcm")
public class GCMModule extends KrollModule {
    // Standard Debugging variables
    private static final String LCAT = "GCMModule";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static GCMModule instance = null;
    private static AppStateListener appStateListener = null;

    /* Callbacks for push notifications */
    private KrollFunction successCallback = null;
    private KrollFunction errorCallback = null;
    private KrollFunction messageCallback = null;
    private KrollFunction tokenCallback = null;

    /* Callbacks for topics */
    private KrollFunction successTopicCallback = null;
    private KrollFunction errorTopicCallback = null;
    private KrollFunction topicCallback = null;


    public static final String LAST_DATA = "nl.vanvianen.android.gcm.last_data";
    public static final String NOTIFICATION_SETTINGS = "nl.vanvianen.android.gcm.notification_settings";

    public GCMModule() {
        super();
        instance = this;
        if (appStateListener == null) {
            appStateListener = new AppStateListener();
            TiApplication.addActivityTransitionListener(appStateListener);
        }
    }

    public boolean isInForeground() {
        return AppStateListener.oneActivityIsResumed;
    }

    public String getToken(){
        // get token and return it
        try {
            return FirebaseInstanceId.getInstance().getToken();
        } catch (Exception ex) {
            return null;
        }
    }

    @Kroll.method
    @SuppressWarnings("unchecked")
    public void registerPush(HashMap options) {

        Log.d(LCAT, "registerPush called");

        Map<String, Object> notificationSettings = (Map<String, Object>) options.get("notificationSettings");

        // Required callback
        messageCallback = (KrollFunction) options.get("callback");

        // Optional callbacks
        successCallback = options.containsKey("success") ? (KrollFunction)options.get("success") : null;
        errorCallback = options.containsKey("error") ? (KrollFunction)options.get("error") : null;
        tokenCallback = options.containsKey("registration") ? (KrollFunction)options.get("registration") : null;

        /* Store notification settings in global Ti.App properties */
        JSONObject json = new JSONObject(notificationSettings);
        TiApplication.getInstance().getAppProperties().setString(GCMModule.NOTIFICATION_SETTINGS, json.toString());

        parseBootIntent();

        String registrationId = getRegistrationId();
        if (registrationId != null && registrationId.length() > 0) {
            sendSuccess(registrationId);
        }
        else {
            sendError(errorCallback, "Registration ID is not (yet) available. The `registration` callback will provide the registration ID when it is available.");
        }
    }

    @Kroll.method
    public void unregister() {
        Log.d(LCAT, "unregister called (" + (instance != null) + ")");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    Log.d(LCAT, "Delete instanceid succeeded");
                } catch (Exception ex) {
                    Log.e(LCAT, "Remove token failed - error: " + ex.getMessage());
                }
                return null;
            }
        }.execute();
    }


    @Kroll.method
    @Kroll.getProperty
    public String getRegistrationId() {
        Log.d(LCAT, "get registrationId property");
        return getToken();
    }


    @Kroll.method
    public void subscribe(final HashMap options) {
        Log.d(LCAT, "subscribe called");

        // subscripe to a topic
        String _topic  = (String) options.get("topic");

        if (options.get("success") != null) {
            successTopicCallback = (KrollFunction) options.get("success");
        }
        if (options.get("error") != null) {
            errorTopicCallback = (KrollFunction) options.get("error");
        }
        if (options.get("callback") != null) {
            topicCallback = (KrollFunction) options.get("callback");
        }

        if (_topic == null) {
            Log.e(LCAT, "No or invalid topic specified");
        }

        if (_topic.startsWith("/topics/")) {
            Log.w(LCAT, "Topic should NOT start with '/topic/'. Please update your implementation.");
            _topic = _topic.substring(8);
        }

        final String topic = _topic;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic);

                    if (successTopicCallback != null) {
                        // send success callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", true);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        successTopicCallback.callAsync(getKrollObject(), data);
                    }
                } catch (Exception ex) {
                    // error
                    Log.e(LCAT, "Subscribe error " + ex.toString());
                    if (errorTopicCallback != null) {
                        // send error callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", false);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        data.put("error", ex.toString());
                        errorCallback.callAsync(getKrollObject(), data);
                    }
                }
                return null;
            }
        }.execute();

        /*
        // Requires Firebase Messaging 17.0.0+ & Play Services 15.0.1+
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    if (errorTopicCallback != null) {
                        // send error callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", false);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        data.put("error", "Cannot subscribe to topic "+topic);
                        errorCallback.callAsync(getKrollObject(), data);
                    }
                    return;
                }

                if (successTopicCallback != null) {
                    // send success callback
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("success", true);
                    data.put("topic", topic);
                    data.put("token", getToken());
                    successTopicCallback.callAsync(getKrollObject(), data);
                }
            }
        });
        */
    }

    @Kroll.method
    public void unsubscribe(final HashMap options) {
        Log.d(LCAT, "unsubscribe called");

        // unsubscripe from a topic
        String _topic  = (String) options.get("topic");
        final KrollFunction callback = (KrollFunction) options.get("callback");

        if (_topic == null) {
            Log.e(LCAT, "No or invalid topic specified");
        }

        if (_topic.startsWith("/topics/")) {
            Log.w(LCAT, "Topic should NOT start with '/topic/'. Please update your implementation.");
            _topic = _topic.substring(8);
        }

        final String topic = _topic;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);

                    if (successTopicCallback != null) {
                        // send success callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", true);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        successTopicCallback.callAsync(getKrollObject(), data);
                    }
                } catch (Exception ex) {
                    // error
                    Log.e(LCAT, "Unsubscribe error " + ex.toString());
                    if (errorTopicCallback != null) {
                        // send error callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", false);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        data.put("error", ex.toString());
                        errorCallback.callAsync(getKrollObject(), data);
                    }
                }
                return null;
            }
        }.execute();

        /*
        // Requires Firebase Messaging 17.0.0+ & Play Services 15.0.1+
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    if (errorTopicCallback != null) {
                        // send error callback
                        HashMap<String, Object> data = new HashMap<String, Object>();
                        data.put("success", false);
                        data.put("topic", topic);
                        data.put("token", getToken());
                        data.put("error", "Cannot unsubscribe from topic "+topic);
                        errorCallback.callAsync(getKrollObject(), data);
                    }
                    return;
                }

                if (successTopicCallback != null) {
                    // send success callback
                    HashMap<String, Object> data = new HashMap<String, Object>();
                    data.put("success", true);
                    data.put("topic", topic);
                    data.put("token", getToken());
                    successTopicCallback.callAsync(getKrollObject(), data);
                }
            }
        });
        */
    }

    @Kroll.method
    @Kroll.getProperty
    @SuppressWarnings("unchecked")
    public KrollDict getLastData() {
        Map map = new Gson().fromJson(TiApplication.getInstance().getAppProperties().getString(LAST_DATA, null), Map.class);
        return map != null ? new KrollDict(map) : null;
    }

    @Kroll.method
    public void clearLastData() {
        TiApplication.getInstance().getAppProperties().removeProperty(LAST_DATA);
    }

    /**
     * Cancel a notification by the id given in the payload.
     * @param notificationId
     */
    @Kroll.method
    public void cancelNotificationById(int notificationId) {
        try {
            NotificationManager notificationManager = (NotificationManager) TiApplication.getInstance().getApplicationContext().getSystemService(TiApplication.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            Log.i(LCAT, "Notification " + notificationId + " cleared successfully");
        } catch (Exception ex) {
            Log.e(LCAT, "Cannot cancel notification:" + notificationId + " Error: " + ex.getMessage());
        }
    }

    @Kroll.method
    @Kroll.getProperty
    @SuppressWarnings("unchecked")
    public KrollDict getNotificationSettings() {
        Log.d(LCAT, "Getting notification settings");
        Map map = new Gson().fromJson(TiApplication.getInstance().getAppProperties().getString(GCMModule.NOTIFICATION_SETTINGS, null), Map.class);
        return map != null ? new KrollDict(map) : null;
    }

    @Kroll.method
    @Kroll.setProperty
    @SuppressWarnings("unchecked")
    public void setNotificationSettings(Map notificationSettings) {
        Log.d(LCAT, "Setting notification settings");
        JSONObject json = new JSONObject(notificationSettings);
        TiApplication.getInstance().getAppProperties().setString(GCMModule.NOTIFICATION_SETTINGS, json.toString());
    }

    public void sendSuccess(String registrationId) {
        if (successCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("success", true);
            data.put("registrationId", registrationId);
            successCallback.callAsync(getKrollObject(), data);
        }
    }

    public void sendError(String error) {
        sendError(errorCallback, error);
    }


    public void sendError(KrollFunction callback, String error) {
        Log.e(LCAT, error);
        if (callback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("success", false);
            data.put("error", error);

            callback.callAsync(getKrollObject(), data);
        }
    }

    public void sendMessage(Map<String, Object> messageData) {
        if (messageCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("data", messageData);
            data.put("inBackground", !isInForeground());

            messageCallback.call(getKrollObject(), data);
        } else {
            Log.e(LCAT, "No callback specified for push notification");
        }
    }

    public void sendTopicMessage(Map<String, Object> messageData) {
        if (topicCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("data", messageData);
            data.put("inBackground", !isInForeground());

            topicCallback.call(getKrollObject(), data);
        } else {
            Log.e(LCAT, "No callback specified for topic subscribe");
        }
    }

    public void sendTokenUpdate(String token) {
        if (tokenCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("success", true);
            data.put("registrationId", token);
            tokenCallback.callAsync(getKrollObject(), data);
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        Log.d(LCAT, "onAppCreate " + app + " (" + (instance != null) + ")");
    }

    @Override
    protected void initActivity(Activity activity) {
        Log.d(LCAT, "initActivity " + activity + " (" + (instance != null) + ")");

        checkPlayServices();

        super.initActivity(activity);
    }

    @Override
    public void onResume(Activity activity) {
        Log.d(LCAT, "onResume " + activity + " (" + (instance != null) + ")");

        checkPlayServices();

        super.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        Log.d(LCAT, "onPause " + activity + " (" + (instance != null) + ")");
        super.onPause(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        Log.d(LCAT, "onDestroy " + activity + " (" + (instance != null) + ")");
        super.onDestroy(activity);
    }

    @Override
    public void onStart(Activity activity) {
        Log.d(LCAT, "onStart " + activity + " (" + (instance != null) + ")");
        super.onStart(activity);
    }

    @Override
    public void onStop(Activity activity) {
        Log.d(LCAT, "onStop " + activity + " (" + (instance != null) + ")");
        super.onStop(activity);
    }

    private void parseBootIntent() {
        try {
            Bundle extras = TiApplication.getAppRootOrCurrentActivity().getIntent().getExtras();
            String notification = "";

            if (extras != null) {
                notification = extras.getString("data");
                for (String key : extras.keySet()) {
                            Object value = extras.get(key);
                            Log.d(LCAT, "Key: " + key + " Value: " + value);
                        }
            }

            if (notification != null && !notification.isEmpty()) {
                /* Store data to be retrieved when resuming app as a JSON object, serialized as a String, otherwise
                 * Ti.App.Properties.getString(GCMModule.LAST_DATA) doesn't work. */
                JSONObject json = new JSONObject(notification);
                TiApplication.getInstance().getAppProperties().setString(GCMModule.LAST_DATA, json.toString());

                Map<String,Object> notificationData = new Gson().fromJson(notification, Map.class);

                sendMessage(notificationData);
            } else {
                Log.d(LCAT, "No notification in Intent");
            }
        } catch (Exception ex) {
            Log.e(LCAT, "parseBootIntent" + ex);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        Activity activity = TiApplication.getAppRootOrCurrentActivity();
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.w(LCAT, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public static GCMModule getInstance() {
        return instance;
    }
}
