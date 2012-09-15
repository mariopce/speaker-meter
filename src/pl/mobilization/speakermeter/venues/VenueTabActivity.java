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
import android.widget.Toast;

@ContentView(R.layout.hall_list)
public class VenueTabActivity extends RoboTabActivity {
	private static final int PROGRESS_DIALOG_ID = 12;

	private static final String START_TIME = "START_TIME";

	public static final String SPEAKER = "SPEAKER";

	private static final String SAVED_TAB = "SAVED_TAB";

	private Handler handler;

	private ProgressDialog progressDialog;

	private long startTime;

	private int savedTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();

		TabHost tabHost = getTabHost();
		tabHost.setId(android.R.id.tabhost);
	}

	@Override
	protected void onResume() {
		super.onResume();

		recreateTabs();

		new SpeakerUpdateChecker().run();
	}

	@Override
	protected void onPause() {
		savedTab = getTabHost().getCurrentTab();
		super.onPause();
	}

	private void recreateTabs() {
		TabHost tabHost = getTabHost();

		int currentTab = tabHost.getCurrentTab();

		if (savedTab != 0)
			tabHost.setCurrentTab(0);
		else
			savedTab = currentTab;

		tabHost.clearAllTabs();

		TabSpec tabSpec = tabHost.newTabSpec(getString(R.string.venues))
				.setIndicator(getString(R.string.venues));
		tabSpec.setContent(new Intent(this, SpeakerListActivity.class));
		tabHost.addTab(tabSpec);

		Collection<String> venueList = ((SpeakerMeterApplication) getApplication())
				.getVenues();

		for (String venue : venueList) {
			TabSpec localTabSpec = tabHost.newTabSpec(venue)
					.setIndicator(venue);
			Intent intent = new Intent(this, SpeakerListActivity.class);
			intent.putExtra(SpeakerListActivity.VENUE, venue);
			localTabSpec.setContent(intent);
			tabHost.addTab(localTabSpec);
		}

		if (savedTab != 0)
			tabHost.setCurrentTab(savedTab);

		if (venueList.isEmpty())
			launchJsonUpdate();
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
		handler.post(new SpeakerUpdateChecker());

		getSpeakerMeterApplication().launchSpeakersUpdate();
	}

	private SpeakerMeterApplication getSpeakerMeterApplication() {
		return (SpeakerMeterApplication) super.getApplication();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(START_TIME, startTime);
		savedTab = getTabHost().getCurrentTab();
		outState.putInt(SAVED_TAB, savedTab);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		startTime = state.getLong(START_TIME, 0L);
		savedTab = state.getInt(SAVED_TAB);
		;
		super.onRestoreInstanceState(state);
	}

	private class SpeakerUpdateChecker implements Runnable {
		private static final int MAX_WAIT_TIME = 10000;
		public static final int CHECK_INTERVAL = 1500;

		public void run() {
			if (hasTimedOut()) {
				removeDialog(PROGRESS_DIALOG_ID);
				return;
			}

			if (!hasUpdatePending()) {
				removeDialog(PROGRESS_DIALOG_ID);
				String error = getError();
				if (error == null)
					recreateTabs();
				else
					signalError(error);
				return;
			}

			handler.postDelayed(this, CHECK_INTERVAL);
		}

		private void signalError(String e) {
			Toast.makeText(VenueTabActivity.this, e, Toast.LENGTH_LONG).show();
		}

		private String getError() {
			return getSpeakerMeterApplication().getSpeakerErrorString();
		}

		private boolean hasUpdatePending() {
			return getSpeakerMeterApplication().hasSpeakerUpdatePending();
		}

		private boolean hasTimedOut() {
			return System.currentTimeMillis() - startTime > MAX_WAIT_TIME;
		}
	}
}
