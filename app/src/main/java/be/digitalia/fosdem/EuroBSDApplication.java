package me.osorio.eurobsd;

import android.app.Application;
import android.preference.PreferenceManager;
import me.osorio.eurobsd.alarms.FosdemAlarmManager;
import me.osorio.eurobsd.db.DatabaseManager;

public class EuroBSDApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		DatabaseManager.init(this);
		// Initialize settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		// Alarms (requires settings)
		FosdemAlarmManager.init(this);
	}
}
