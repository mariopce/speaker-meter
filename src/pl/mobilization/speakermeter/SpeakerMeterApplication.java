package pl.mobilization.speakermeter;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;


public class SpeakerMeterApplication extends Application {
	public static final String UUID = "uuid";
	private String uuid;

	@Override
	public void onCreate() {
		super.onCreate();
		
		SharedPreferences sharedPreferences = getSharedPreferences("speaker-meter", MODE_PRIVATE);
		uuid = sharedPreferences.getString(UUID, null);
		
		if (uuid == null) {
			uuid = obtainUUID();
			saveUUID(sharedPreferences, uuid);	
		}
	}

	private void saveUUID(SharedPreferences sharedPreferences, String deviceId) {
		Editor edit = sharedPreferences.edit();
		edit.putString(UUID, deviceId);
		edit.commit();
	}

	private String obtainUUID() {
		String deviceId;
		TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
		deviceId = telephonyManager.getDeviceId();
		if (deviceId == null)
			deviceId = java.util.UUID.randomUUID().toString();
		
		return deviceId;
	}
	
	public String getUUID() {
		return uuid;
	}
}
