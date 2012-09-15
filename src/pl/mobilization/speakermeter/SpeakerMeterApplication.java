package pl.mobilization.speakermeter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloader.SpeakerListDownloader;
import pl.mobilization.speakermeter.downloader.VoteUpdateDownloader;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.gson.JsonParseException;

public class SpeakerMeterApplication extends Application {
	public static final String UUID = "uuid";
	private String uuid;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;
	private SpeakerListDownloader speakerUpdate;
	private VoteUpdateDownloader voteUpdate;
	private String voteError;
	private String speakerError;

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
						pl.mobilization.speakermeter.dao.SpeakerDao.Properties.Start_time, pl.mobilization.speakermeter.dao.SpeakerDao.Properties.Venue )
				.list();
	}

	public Collection<String> getVenues() {
		Set<String> venues = new TreeSet<String>();

		for (Speaker speaker : getSpeakerList()) {
			String venue = speaker.getVenue();
			if (!Strings.isNullOrEmpty(venue))
				venues.add(venue);
		}
		return venues;
	}

	public void launchSpeakersUpdate() {
		final SpeakerListDownloader speakerUpdateDownloader = new SpeakerListDownloader(
				this);

		if (this.speakerUpdate != null) {
			this.speakerUpdate.cancel(true);
		}
		this.speakerUpdate = speakerUpdateDownloader;

		new Thread(speakerUpdateDownloader).start();
		final Handler handler = new Handler();

		handler.postDelayed(new Runnable() {

			public void run() {
				if (!speakerUpdateDownloader.isDone()) {
					handler.postDelayed(this, 1000);
					return;
				}
				try {
					Speaker[] speakers = speakerUpdateDownloader.get();
					speakerDao.insertOrReplaceInTx(speakers);
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					speakerError = getExceptionString(e.getCause());
				} finally {
				}
			}
		}, 1000);

	}

	public boolean hasSpeakerUpdatePending() {
		if (this.speakerUpdate == null)
			return false;

		return !this.speakerUpdate.isDone();
	}

	public Speaker getSpeaker(long speaker_id) {
		return speakerDao.load(speaker_id);
	}

	public void launchVoteUpdate(Speaker speaker, boolean isUp) {
		final VoteUpdateDownloader speakerUpdateDownloader = new VoteUpdateDownloader(
				speaker.getId(), isUp, uuid);

		if (this.voteUpdate != null) {
			this.voteUpdate.cancel(true);
		}
		this.voteUpdate = speakerUpdateDownloader;

		new Thread(speakerUpdateDownloader).start();
		final Handler handler = new Handler();

		handler.postDelayed(new Runnable() {

			public void run() {
				if (!voteUpdate.isDone()) {
					handler.postDelayed(this, 1000);
					return;
				}
				try {
					Speaker speaker = voteUpdate.get();
					speakerDao.insertOrReplace(speaker);
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					voteError = getExceptionString(e.getCause());
				} finally {
				}
			}

		}, 1000);
	}

	public boolean hasVoteUpdatePending() {
		if (this.voteUpdate == null)
			return false;

		return !this.voteUpdate.isDone();
	}

	public String getVoteErrorString() {
		String error = this.voteError;
		this.voteError = null;
		return error;
	}

	public String getSpeakerErrorString() {
		String error = this.speakerError;
		this.speakerError = null;
		return error;
	}

	private String getExceptionString(Throwable e) {
		Throwable rootCause = Throwables.getRootCause(e);
		String exceptionString = getString(R.string.problem_uknown,
				rootCause.getLocalizedMessage());

		if (rootCause instanceof IOException) {
			exceptionString = getString(R.string.problem_connection,
					rootCause.getLocalizedMessage());
		}
		if (rootCause instanceof JsonParseException) {
			exceptionString = getString(R.string.problem_json,
					rootCause.getLocalizedMessage());
		}

		return exceptionString;
	}

	public Collection<Speaker> getSpeakerList(final String venue) {
		Collection<Speaker> speakerList = getSpeakerList();
		if (venue != null) {
			speakerList = Collections2.filter(speakerList,
					new Predicate<Speaker>() {
						public boolean apply(Speaker speaker) {
							String speakerVenue = speaker.getVenue();
							return venue.equals(speakerVenue);
						}
					});
		}
		return speakerList;
	}
}
