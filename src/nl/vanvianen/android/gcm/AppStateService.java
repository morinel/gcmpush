package nl.vanvianen.android.gcm;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;

public class AppStateService {

	public static boolean initAppStateService(Application app)
	{
		try {
	        boolean foreground = new ForegroundCheckTask().execute(app.getApplicationContext()).get();
	        return foreground;
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	        return false;
	    } catch (ExecutionException e) {
	        e.printStackTrace();
	        return false;
	    }
	}


}
