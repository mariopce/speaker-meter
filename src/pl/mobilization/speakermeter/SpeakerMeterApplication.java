package pl.mobilization.speakermeter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloaders.JSonDownloader;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.google.common.base.Strings;

public class SpeakerMeterApplication extends Application {
	public static final String UUID = "uuid";
	private String uuid;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;
	
	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences sharedPreferences = getSharedPreferences(
				"speaker-meter", MODE_PRIVATE);
		uuid = sharedPreferences.getString(UUID, null);

		if (uuid == null) {
			uuid = obtainUUID();
			saveUUID(sharedPreferences, uuid);
		}

		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		speakerDao = daoSession.getSpeakerDao();
		
		
	}

	private void saveUUID(SharedPreferences sharedPreferences, String deviceId) {
		Editor edit = sharedPreferences.edit();
		edit.putString(UUID, deviceId);
		edit.commit();
	}

	private String obtainUUID() {
		String deviceId;
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		deviceId = telephonyManager.getDeviceId();
		if (deviceId == null)
			deviceId = java.util.UUID.randomUUID().toString();

		return deviceId;
	}

	public String getUUID() {
		return uuid;
	}

	public List<Speaker> getSpeakerList() {
		return speakerDao
				.queryBuilder()
				.orderAsc(
						pl.mobilization.speakermeter.dao.SpeakerDao.Properties.Name)
				.list();
	}
	
	public Collection<String> getVenues() {
		Set<String> venues = new TreeSet<String>();
		
		for(Speaker speaker : getSpeakerList()) {
			String venue = speaker.getVenue();
			if(!Strings.isNullOrEmpty(venue))
				venues.add(venue);
		}
		return venues;
	}

	public void launchSpeakersUpdate() {
		new Thread
	}

	public boolean hasPendingSpeakersUpdate() {
		return false;
	}
}
