//
//   Copyright 2013 jordi domenech <http://iamyellow.net, jordi@iamyellow.net>
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

/**
 * Code borrowed from gcm.js: https://github.com/iamyellow/gcm.js/blob/master/src/net/iamyellow/gcmjs/AppStateListener.java
 */

package nl.vanvianen.android.gcm;

import org.appcelerator.titanium.TiApplication.ActivityTransitionListener;

public class AppStateListener implements ActivityTransitionListener  {
	public static boolean oneActivityIsResumed = false;
	public static boolean appWasNotRunning = false;

	@Override
	public void onActivityTransition (boolean state) {
		oneActivityIsResumed = !state;
	}
}