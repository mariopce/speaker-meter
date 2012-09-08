package pl.mobilization.speakermeter.venues;

import java.util.Collection;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.speakers.SpeakerListActivity;
import roboguice.activity.RoboTabActivity;
import roboguice.inject.ContentView;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@ContentView(R.layout.hall_list)
public class VenueTabActivity extends RoboTabActivity {
	private static final int PROGRESS_DIALOG_ID = 12;

	private static final String START_TIME = "START_TIME";

	public static final String SPEAKER = "SPEAKER";

	private Handler handler;

	private ProgressDialog progressDialog;

	private long startTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TabHost tabHost = getTabHost();

		tabHost.setId(android.R.id.tabhost);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		recreateTabs();

		handler = new Handler();
		new SpeakerUpdateChecker().run();
	}

	private void recreateTabs() {
		TabHost tabHost = getTabHost();
		tabHost.clearAllTabs();

		TabSpec tabSpec = tabHost.newTabSpec("All").setIndicator("All venues");
		tabSpec.setContent(new Intent(this, SpeakerListActivity.class));
		tabHost.addTab(tabSpec);
		
		Collection<String> venueList = ((SpeakerMeterApplication) getApplication())
				.getVenues();

		for (String venue : venueList) {
			TabSpec tabSpec1 = tabHost.newTabSpec(venue).setIndicator(venue);
			Intent intent = new Intent(this, SpeakerListActivity.class);
			intent.putExtra(SpeakerListActivity.VENUE, venue);
			tabSpec1.setContent(intent);
			tabHost.addTab(tabSpec1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.votes_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			launchJsonUpdate();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == PROGRESS_DIALOG_ID) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(getString(R.string.update));
			progressDialog.setMessage(getString(R.string.obtaining_list));
			return progressDialog;
		}
		return super.onCreateDialog(id);
	}

	private void launchJsonUpdate() {
		showDialog(PROGRESS_DIALOG_ID);

		startTime = System.currentTimeMillis();
		handler = new Handler();
		handler.post(new SpeakerUpdateChecker());

		getSpeakerMeterApplication().launchSpeakerUpdate();
	}

	private SpeakerMeterApplication getSpeakerMeterApplication() {
		return (SpeakerMeterApplication) super.getApplication();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(START_TIME, startTime);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		startTime = state.getLong(START_TIME, 0L);
		super.onRestoreInstanceState(state);
	}

	private class SpeakerUpdateChecker implements Runnable {
		private static final int MAX_WAIT_TIME = 10000;
		public static final int CHECK_INTERVAL = 1000;

		public void run() {
			if (hasTimedOut()) {
				removeDialog(PROGRESS_DIALOG_ID);
				return;
			}
			
			if (!hasUpdatePending()) {
				removeDialog(PROGRESS_DIALOG_ID);
				recreateTabs();
				return;
			}
			
			
			handler.postDelayed(this, CHECK_INTERVAL);
		}

		private boolean hasUpdatePending() {
			return getSpeakerMeterApplication().hasSpeakerUpdatePending();
		}

		private boolean hasTimedOut() {
			return System.currentTimeMillis() - startTime > MAX_WAIT_TIME;
		}
	}
}
