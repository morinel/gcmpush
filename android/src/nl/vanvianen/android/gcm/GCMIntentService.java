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

import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import org.appcelerator.kroll.common.Log;

import java.util.HashMap;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String LCAT = "GCMIntentService";

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

		GCMModule.getInstance().fireEvent(GCMModule.UNREGISTER_EVENT,
				new HashMap<String, Object>());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onMessage(Context context, Intent intent) {
		Log.d(LCAT, "Push notification received");

		NotificationBuilder.build(this, intent);
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
