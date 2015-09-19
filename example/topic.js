var gcm = require("nl.vanvianen.android.gcm");

gcm.subscribe({
	/* The Sender ID from Google Developers Console, see https://console.developers.google.com/project/XXXXXXXX/apiui/credential */
	/* It's the same as your project id */
	senderId: 'XXXXXXXX',
    /* Should start with "/topics/" */
	topic: '/topics/mytopic',
	notificationSettings: {
		vibrate: true  /* Whether the phone should vibrate, see the push notification example for more settings */
	},
	success: function (event) {
		Ti.API.info("Topic registration success: " + JSON.stringify(event));
	},
	error: function (event) {
		Ti.API.info("Topic registration error: " + JSON.stringify(event));
		alert(event.error);
	},
	callback: function (event) {
		Ti.API.info("Topic callback = " + JSON.stringify(event));
		/* Called when a notification is received and the app is in the foreground */
		
		var dialog = Ti.UI.createAlertDialog({
			title: 'Topic received',
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
