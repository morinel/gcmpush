var gcm = require("nl.vanvianen.android.gcm");

/* If the app is started or resumed act on pending data saved when the notification was received */
var lastData = gcm.getLastData();
if (lastData) {
	Ti.API.info("Last notification received " + JSON.stringify(lastData));
	gcm.clearLastData();
}

gcm.registerPush({
	/* The Sender ID from Google Developers Console, see https://console.developers.google.com/project/XXXXXXXX/apiui/credential */
	/* It's the same as your project id */
	senderId: 'XXXXXXXX',
	notificationSettings: {
		sound: 'mysound.mp3', /* Place sound file in platform/android/res/raw/mysound.mp3 */
		smallIcon: 'notification_icon.png',  /* Place icon in platform/android/res/drawable/notification_icon.png */
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
	},
	success: function (event) {
		Ti.API.info("Push registration success: " + JSON.stringify(event));
		/* Add code to send event.registrationId to your server */
	},
	error: function (event) {
		Ti.API.info("Push registration error: " + JSON.stringify(event));
		alert(event.error);
	},
	callback: function (event) {
		Ti.API.info("Push callback = " + JSON.stringify(event));
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
	}
});
