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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.gson.Gson;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiRHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String LCAT = "GCMIntentService";

    private static final String UNREGISTER_EVENT = "unregister";

    private static final String DEFAULT_TITLE_KEY = "title";
    private static final String DEFAULT_MESSAGE_KEY = "message";
    private static final String DEFAULT_TICKER_KEY = "ticker";

    public GCMIntentService() {
        super("");
    }

    @Override
    public void onRegistered(Context context, String registrationId) {
        Log.d(LCAT, "Registered: " + registrationId);

        GCMModule.getInstance().sendSuccess(registrationId);
    }

    @Override
    public void onUnregistered(Context context, String registrationId) {
        Log.d(LCAT, "Unregistered");

        GCMModule.getInstance().fireEvent(UNREGISTER_EVENT, new HashMap<String, Object>());
    }

    private int getResource(String type, String name) {
        int icon = 0;
        if (name != null) {
            /* Remove extension from icon */
            int index = name.lastIndexOf(".");
            if (index > 0) {
                name = name.substring(0, index);
            }
            try {
                icon = TiRHelper.getApplicationResource(type + "." + name);
            } catch (TiRHelper.ResourceNotFoundException ex) {
                Log.e(LCAT, type + "." + name + " not found; make sure it's in platform/android/res/" + type);
            }
        }

        return icon;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onMessage(Context context, Intent intent) {
        Log.d(LCAT, "Push notification received");

        boolean isTopic = false;

        HashMap<String, Object> data = new HashMap<String, Object>();
        for (String key : intent.getExtras().keySet()) {
            String value = intent.getExtras().getString(key);
            Log.d(LCAT, "Message key: \"" + key + "\" value: \"" + value + "\"");

            if (key.equals("from") && value != null && value.startsWith("/topics/")) {
                isTopic = true;
            }

            String eventKey = key.startsWith("data.") ? key.substring(5) : key;
            data.put(eventKey, intent.getExtras().getString(key));

            if (value.startsWith("{")) {
                Log.d(LCAT, "Parsing JSON string...");
                try {
                    JSONObject json = new JSONObject(value);

                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String jKey = keys.next();
                        String jValue = json.getString(jKey);
                        Log.d(LCAT, "JSON key: \"" + jKey + "\" value: \"" + jValue + "\"");

                        data.put(jKey, jValue);
                    }
                }
                catch(JSONException e) {
                    Log.d(LCAT, "JSON error: " + e);
                }
            }
        }

        /* Store data to be retrieved when resuming app as a JSON object, serialized as a String, otherwise
         * Ti.App.Properties.getString(GCMModule.LAST_DATA) doesn't work. */
        JSONObject json = new JSONObject(data);
        TiApplication.getInstance().getAppProperties().setString(GCMModule.LAST_DATA, json.toString());

        /* Get settings from notification object */
        int smallIcon = 0;
        int largeIcon = 0;
        String sound = null;
        boolean vibrate = false;
        boolean insistent = false;
        String group = null;
        boolean localOnly = true;
        int priority = 0;
        boolean bigText = false;
        
        Integer ledOn = null;
        Integer ledOff = null;

        String titleKey = DEFAULT_TITLE_KEY;
        String messageKey = DEFAULT_MESSAGE_KEY;
        String tickerKey = DEFAULT_TICKER_KEY;
        String title = null;
        String message = null;
        String ticker = null;

        boolean backgroundOnly = false;

        Map<String, Object> notificationSettings = new Gson().fromJson(TiApplication.getInstance().getAppProperties().getString(GCMModule.NOTIFICATION_SETTINGS, null), Map.class);
        if (notificationSettings != null) {
            if (notificationSettings.get("smallIcon") instanceof String) {
                smallIcon = getResource("drawable", (String) notificationSettings.get("smallIcon"));
            } else {
                Log.e(LCAT, "Invalid setting smallIcon, should be String");
            }

            if (notificationSettings.get("largeIcon") instanceof String) {
                largeIcon = getResource("drawable", (String) notificationSettings.get("largeIcon"));
            } else {
                Log.e(LCAT, "Invalid setting largeIcon, should be String");
            }

            if (notificationSettings.get("sound") != null) {
                if (notificationSettings.get("sound") instanceof String) {
                    sound = (String) notificationSettings.get("sound");
                } else {
                    Log.e(LCAT, "Invalid setting sound, should be String");
                }
            }

            if (notificationSettings.get("vibrate") != null) {
                if (notificationSettings.get("vibrate") instanceof Boolean) {
                    vibrate = (Boolean) notificationSettings.get("vibrate");
                } else {
                    Log.e(LCAT, "Invalid setting vibrate, should be Boolean");
                }
            }

            if (notificationSettings.get("insistent") != null) {
                if (notificationSettings.get("insistent") instanceof Boolean) {
                    insistent = (Boolean) notificationSettings.get("insistent");
                } else {
                    Log.e(LCAT, "Invalid setting insistent, should be Boolean");
                }
            }

            if (notificationSettings.get("group") != null) {
                if (notificationSettings.get("group") instanceof String) {
                    group = (String) notificationSettings.get("group");
                } else {
                    Log.e(LCAT, "Invalid setting group, should be String");
                }
            }

            if (notificationSettings.get("localOnly") != null) {
                if (notificationSettings.get("localOnly") instanceof Boolean) {
                    localOnly = (Boolean) notificationSettings.get("localOnly");
                } else {
                    Log.e(LCAT, "Invalid setting localOnly, should be Boolean");
                }
            }

            if (notificationSettings.get("priority") != null) {
                if (notificationSettings.get("priority") instanceof Integer) {
                    priority = (Integer) notificationSettings.get("priority");
                } else if (notificationSettings.get("priority") instanceof Double) {
                    priority = ((Double) notificationSettings.get("priority")).intValue();
                } else {
                    Log.e(LCAT, "Invalid setting priority, should be an integer, between PRIORITY_MIN (" + NotificationCompat.PRIORITY_MIN + ") and PRIORITY_MAX (" + NotificationCompat.PRIORITY_MAX + ")");
                }
            }

            if (notificationSettings.get("bigText") != null) {
                if (notificationSettings.get("bigText") instanceof Boolean) {
                    bigText = (Boolean) notificationSettings.get("bigText");
                } else {
                    Log.e(LCAT, "Invalid setting bigText, should be Boolean");
                }
            }

            if (notificationSettings.get("titleKey") != null) {
                if (notificationSettings.get("titleKey") instanceof String) {
                    titleKey = (String) notificationSettings.get("titleKey");
                } else {
                    Log.e(LCAT, "Invalid setting titleKey, should be String");
                }
            }

            if (notificationSettings.get("messageKey") != null) {
                if (notificationSettings.get("messageKey") instanceof String) {
                    messageKey = (String) notificationSettings.get("messageKey");
                } else {
                    Log.e(LCAT, "Invalid setting messageKey, should be String");
                }
            }

            if (notificationSettings.get("tickerKey") != null) {
                if (notificationSettings.get("tickerKey") instanceof String) {
                    tickerKey = (String) notificationSettings.get("tickerKey");
                } else {
                    Log.e(LCAT, "Invalid setting tickerKey, should be String");
                }
            }

            if (notificationSettings.get("title") != null) {
                if (notificationSettings.get("title") instanceof String) {
                    title = (String) notificationSettings.get("title");
                } else {
                    Log.e(LCAT, "Invalid setting title, should be String");
                }
            }

            if (notificationSettings.get("message") != null) {
                if (notificationSettings.get("message") instanceof String) {
                    message = (String) notificationSettings.get("message");
                } else {
                    Log.e(LCAT, "Invalid setting message, should be String");
                }
            }

            if (notificationSettings.get("ticker") != null) {
                if (notificationSettings.get("ticker") instanceof String) {
                    ticker = (String) notificationSettings.get("ticker");
                } else {
                    Log.e(LCAT, "Invalid setting ticker, should be String");
                }
            }

            if (notificationSettings.get("ledOn") != null) {
                if (notificationSettings.get("ledOn") instanceof Integer) {
                    ledOn = (Integer) notificationSettings.get("ledOn");
                    if (ledOn < 0) {
                        Log.e(LCAT, "Invalid setting ledOn, should be positive");
                        ledOn = null;
                    }
                } else {
                    Log.e(LCAT, "Invalid setting ledOn, should be Integer");
                }
            }

            if (notificationSettings.get("ledOff") != null) {
                if (notificationSettings.get("ledOff") instanceof Integer) {
                    ledOff = (Integer) notificationSettings.get("ledOff");
                    if (ledOff < 0) {
                        Log.e(LCAT, "Invalid setting ledOff, should be positive");
                        ledOff = null;
                    }
                } else {
                    Log.e(LCAT, "Invalid setting ledOff, should be Integer");
                }
            }

            if (notificationSettings.get("backgroundOnly") != null) {
                if (notificationSettings.get("backgroundOnly") instanceof Boolean) {
                    backgroundOnly = (Boolean) notificationSettings.get("backgroundOnly");
                } else {
                    Log.e(LCAT, "Invalid setting backgroundOnly, should be Boolean");
                }
            }

        } else {
            Log.d(LCAT, "No notification settings found");
        }

        /* If icon not found, default to appicon */
        if (smallIcon == 0) {
            smallIcon = getResource("drawable", "appicon");
        }

        /* If large icon not found, default to icon */
        if (largeIcon == 0) {
            largeIcon = smallIcon;
        }

        /* Create intent to (re)start the app's root activity */
        String pkg = TiApplication.getInstance().getApplicationContext().getPackageName();
        Intent launcherIntent = TiApplication.getInstance().getApplicationContext().getPackageManager().getLaunchIntentForPackage(pkg);
        launcherIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        /* Grab notification content from data according to provided keys if not already set */
        if (title == null && titleKey != null) {
            title = (String) data.get(titleKey);
        }
        if (message == null && messageKey != null) {
            message = (String) data.get(messageKey);
        }
        if (ticker == null && tickerKey != null) {
            ticker = (String) data.get(tickerKey);
        }

        Log.i(LCAT, "Title: " + title);
        Log.i(LCAT, "Message: " + message);
        Log.i(LCAT, "Ticker: " + ticker);

        if (backgroundOnly && GCMModule.getInstance() != null && GCMModule.getInstance().isInForeground()) {
            Log.d(LCAT, "Notification received in foreground, no need for notification.");
            return;
        }

        if (message == null) {
            Log.d(LCAT, "Message received but no 'message' specified in push notification payload, so will make this silent");
        } else {
            Log.d(LCAT, "Creating notification...");

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), largeIcon);
            if (bitmap == null) {
                Log.d(LCAT, "No large icon found");
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setTicker(ticker)
                    .setContentIntent(PendingIntent.getActivity(this, 0, launcherIntent, PendingIntent.FLAG_ONE_SHOT))
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(bitmap);

            /* Name of group to group similar notifications together, can also be set in the push notification payload */
            if (data.get("group") != null) {
                group = (String) data.get("group");
            }
            if (group != null) {
                builder.setGroup(group);
            }
            Log.i(LCAT, "Group: " + group);

            /* Whether notification should be for this device only or bridged to other devices, can also be set in the push notification payload */
            if (data.get("localOnly") != null) {
                localOnly = Boolean.getBoolean((String) data.get("localOnly"));
            }
            builder.setLocalOnly(localOnly);
            Log.i(LCAT, "LocalOnly: " + localOnly);

            /* Specify notification priority, can also be set in the push notification payload */
            if (data.get("priority") != null) {
                priority = Integer.parseInt((String) data.get("priority"));
            }
            if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
                builder.setPriority(priority);
                Log.i(LCAT, "Priority: " + priority);
            } else {
                Log.e(LCAT, "Ignored invalid priority " + priority);
            }

            /* Specify whether bigtext should be used, can also be set in the push notification payload */
            if (data.get("bigText") != null) {
                bigText = Boolean.getBoolean((String) data.get("bigText"));
            }
            if (bigText) {
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
            }
            Log.i(LCAT, "bigText: " + bigText);

            Notification notification = builder.build();

            /* Sound, can also be set in the push notification payload */
            if (data.get("sound") != null) {
                Log.d(LCAT, "Sound specified in notification");
                sound = (String) data.get("sound");
            }

            if ("default".equals(sound)) {
                Log.i(LCAT, "Sound: default sound");
                notification.defaults |= Notification.DEFAULT_SOUND;
            } else if (sound != null) {
                Log.i(LCAT, "Sound " + sound);
                notification.sound = Uri.parse("android.resource://" + pkg + "/" + getResource("raw", sound));
            }

            /* Vibrate, can also be set in the push notification payload */
            if (data.get("vibrate") != null) {
                vibrate = Boolean.getBoolean((String) data.get("vibrate"));
            }
            if (vibrate) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
            Log.i(LCAT, "Vibrate: " + vibrate);

            /* Insistent, can also be set in the push notification payload */
            if ("true".equals(data.get("insistent"))) {
                insistent = true;
            }
            if (insistent) {
                notification.flags |= Notification.FLAG_INSISTENT;
            }
            Log.i(LCAT, "Insistent: " + insistent);

            /* Specify LED flashing */
            if (ledOn != null || ledOff != null) {
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                if (ledOn != null) {
                    notification.ledOnMS = ledOn;
                }
                if (ledOff != null) {
                    notification.ledOffMS = ledOff;
                }
            } else {
                notification.defaults |= Notification.DEFAULT_LIGHTS;
            }

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
        }

        if (GCMModule.getInstance() != null) {
            if (isTopic) {
                GCMModule.getInstance().sendTopicMessage(data);
            } else {
                GCMModule.getInstance().sendMessage(data);
            }
        }
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(LCAT, "Error: " + errorId);

        if (GCMModule.getInstance() != null) {
            GCMModule.getInstance().sendError(errorId);
        }
    }

    @Override
    public boolean onRecoverableError(Context context, String errorId) {
        Log.e(LCAT, "RecoverableError: " + errorId);

        if (GCMModule.getInstance() != null) {
            GCMModule.getInstance().sendError(errorId);
        }

        return true;
    }
}
