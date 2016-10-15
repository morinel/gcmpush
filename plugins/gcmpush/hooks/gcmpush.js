exports.cliVersion = ">=3.X";

var LCAT = "GCMPush",
    fs = require("fs"),
    path = require("path"),
    AndroidManifest = require("androidmanifest");

exports.init = function(logger, config, cli, nodeappc) {

	var opts = config.appc.opts;

	/**
	 * delete amazon-device-messaging stub library from libs
	 * ToDo: handle with Ant at module build time
	 */
	cli.on("build.pre.compile", function(data, done) {
		if (opts.platform === "android") {
			var moduleId = "nl.vanvianen.android.gcm",
			    modules = cli.tiapp.modules,
			    modulePath;
			for (var i in modules) {
				var module = modules[i];
				if (module.id === moduleId) {
					modulePath = path.join(opts.projectDir, "modules/android", moduleId, module.version, "lib");
					break;
				}
			}
			var jars = fs.readdirSync(modulePath);
			for (var i in jars) {
				var jar = jars[i];
				if (jar.indexOf("amazon-device-messaging") >= 0) {
					fs.unlinkSync(path.join(modulePath, jar));
					break;
				}
			}
		}
		done();
	});

	/**
	 *  Appc cli eliminates custom tags
	 */
	cli.on("build.android.writeAndroidManifest", function(data, done) {
		var manifestFilePath = this.androidManifestFile,
		    manifest = new AndroidManifest().readFile(manifestFilePath);

		manifest.$("manifest").attr("xmlns:amazon", "http://schemas.amazon.com/apk/res/android");
		manifest.$("application").append("<amazon:enable-feature android:name=\"com.amazon.device.messaging\" android:required=\"false\" />");
		manifest.writeFile(manifestFilePath);

		done();
	});
};
