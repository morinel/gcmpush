package nl.vanvianen.android.gcm;

import java.util.HashMap;

import com.amazon.device.messaging.ADMMessageHandlerBase;

import android.content.Intent;
import android.util.Log;

public class ADMMessageHandler extends ADMMessageHandlerBase {

	// Standard Debugging variables
	private static final String LCAT = "ADMMessageHandler";

	public ADMMessageHandler() {
		super(ADMMessageHandler.class.getName());
	}

	public static class ADMMessageReceiver extends
			com.amazon.device.messaging.ADMMessageReceiver {
		public ADMMessageReceiver() {
			super(ADMMessageHandler.class);
		}
	}

	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onRegistered(final String registrationId) {
		Log.d(LCAT, "Registered: " + registrationId);

		if (GCMModule.getInstance() != null) {
			GCMModule.getInstance().sendSuccess(registrationId);
		}
	}

	@Override
	protected void onUnregistered(final String registrationId) {
		Log.i(LCAT, "Unregistered");

		if (GCMModule.getInstance() != null) {
			GCMModule.getInstance().fireEvent(GCMModule.UNREGISTER_EVENT,
					new HashMap<String, Object>());
		}
	}

	@Override
	protected void onRegistrationError(final String errorId) {
		Log.d(LCAT, "RegistrationError: " + errorId);

		if (GCMModule.getInstance() != null) {
			GCMModule.getInstance().sendError(
					"ADM registration failed with code " + errorId);
		}
	}

	@Override
	protected void onMessage(final Intent intent) {
		Log.d(LCAT, "Push notification received");

		NotificationBuilder.build(this, intent);
	}
}
