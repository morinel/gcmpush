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
import android.os.AsyncTask;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
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

    private static GCMModule instance = null;

    /* Callbacks for push notifications */
    private KrollFunction successCallback = null;
    private KrollFunction errorCallback = null;
    private KrollFunction messageCallback = null;

    /* Callbacks for topics */
    private KrollFunction successTopicCallback = null;
    private KrollFunction errorTopicCallback = null;
    private KrollFunction topicCallback = null;


    public static final String LAST_DATA = "nl.vanvianen.android.gcm.last_data";
    public static final String NOTIFICATION_SETTINGS = "nl.vanvianen.android.gcm.notification_settings";

    public GCMModule() {
        super();
        instance = this;
    }

    @Kroll.method
    @SuppressWarnings("unchecked")
    public void registerPush(HashMap options) {
        Log.d(LCAT, "registerPush called");

        String senderId = (String) options.get("senderId");
        Map<String, Object> notificationSettings = (Map<String, Object>) options.get("notificationSettings");
        successCallback = (KrollFunction) options.get("success");
        errorCallback = (KrollFunction) options.get("error");
        messageCallback = (KrollFunction) options.get("callback");

        /* Store notification settings in global Ti.App properties */
        JSONObject json = new JSONObject(notificationSettings);
        TiApplication.getInstance().getAppProperties().setString(GCMModule.NOTIFICATION_SETTINGS, json.toString());

        if (senderId != null) {
            GCMRegistrar.register(TiApplication.getInstance(), senderId);

            String registrationId = getRegistrationId();
            if (registrationId != null && registrationId.length() > 0) {
                sendSuccess(registrationId);
            }
        } else {
            sendError(errorCallback, "No GCM senderId specified; get it from the Google Play Developer Console");
        }
    }

    @Kroll.method
    public void unregister() {
        Log.d(LCAT, "unregister called (" + (instance != null) + ")");
        try {
            GCMRegistrar.unregister(TiApplication.getInstance());
        } catch (Exception ex) {
            Log.e(LCAT, "Cannot unregister from push: " + ex.getMessage());
        }
    }


    @Kroll.method
    @Kroll.getProperty
    public String getRegistrationId() {
        Log.d(LCAT, "get registrationId property");
        return GCMRegistrar.getRegistrationId(TiApplication.getInstance());
    }


    @Kroll.method
    public void subscribe(final HashMap options) {
        Log.d(LCAT, "subscribe called");

        // subscripe to a topic
        final String senderId = (String) options.get("senderId");
        final String topic  = (String) options.get("topic");

        if (options.get("success") != null) {
            successTopicCallback = (KrollFunction) options.get("success");
        }
        if (options.get("error") != null) {
            errorTopicCallback = (KrollFunction) options.get("error");
        }
        if (options.get("callback") != null) {
            topicCallback = (KrollFunction) options.get("callback");
        }

        if (topic == null || !topic.startsWith("/topics/")) {
            sendError(errorTopicCallback, "No or invalid topic specified, should start with /topics/");
        }

        if (senderId != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String token = getToken(senderId);
                        GcmPubSub.getInstance(TiApplication.getInstance()).subscribe(token, topic, null);

                        if (successTopicCallback != null) {
                            // send success callback
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("success", true);
                            data.put("topic", topic);
                            successTopicCallback.callAsync(getKrollObject(), data);
                        }
                    } catch (Exception ex) {
                        // error
                        Log.e(LCAT, "Error " + ex.toString());
                        if (errorTopicCallback != null) {
                            // send error callback
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("success", false);
                            data.put("topic", topic);
                            data.put("error", ex.toString());
                            errorCallback.callAsync(getKrollObject(), data);
                        }
                    }
                    return null;
                }
            }.execute();
        } else {
            sendError(errorTopicCallback, "No GCM senderId specified; get it from the Google Play Developer Console");
        }
    }

    @Kroll.method
    public void unsubscribe(final HashMap options) {
        // unsubscripe from a topic
        final String senderId = (String) options.get("senderId");
        final String topic  = (String) options.get("topic");
        final KrollFunction callback = (KrollFunction) options.get("callback");

        if (topic == null || !topic.startsWith("/topics/")) {
            Log.e(LCAT, "No or invalid topic specified, should start with /topics/");
        }

        if (senderId != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String token = getToken(senderId);
                        if (token != null) {
                            GcmPubSub.getInstance(TiApplication.getInstance()).unsubscribe(token, topic);

                            if (callback != null) {
                                // send success callback
                                HashMap<String, Object> data = new HashMap<String, Object>();
                                data.put("success", true);
                                data.put("topic", topic);
                                data.put("token", token);
                                callback.callAsync(getKrollObject(), data);
                            }
                        } else {
                            sendError(callback, "Cannot unsubscribe from topic " + topic);
                        }
                    } catch (Exception ex) {
                        sendError(callback, "Cannot unsubscribe from topic " + topic + ": " + ex.getMessage());
                    }
                    return null;
                }
            }.execute();
        }
    }

    public String getToken(String senderId){
        // get token and return it
        try {
            InstanceID instanceID = InstanceID.getInstance(TiApplication.getInstance());
            return instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (Exception ex) {
            return null;
        }
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

    public void sendMessage(HashMap<String, Object> messageData) {
        if (messageCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("data", messageData);

            messageCallback.call(getKrollObject(), data);
        } else {
            Log.e(LCAT, "No callback specified for push notification");
        }
    }

    public void sendTopicMessage(HashMap<String, Object> messageData) {
        if (topicCallback != null) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("data", messageData);

            topicCallback.call(getKrollObject(), data);
        } else {
            Log.e(LCAT, "No callback specified for topic subscribe");
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        Log.d(LCAT, "onAppCreate " + app + " (" + (instance != null) + ")");
    }

    @Override
    protected void initActivity(Activity activity) {
        Log.d(LCAT, "initActivity " + activity + " (" + (instance != null) + ")");
        super.initActivity(activity);
    }

    @Override
    public void onResume(Activity activity) {
        Log.d(LCAT, "onResume " + activity + " (" + (instance != null) + ")");
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

    public static GCMModule getInstance() {
        return instance;
    }
}
