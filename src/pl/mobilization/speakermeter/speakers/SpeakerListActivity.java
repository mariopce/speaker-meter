package pl.mobilization.speakermeter.speakers;

import java.net.URI;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.activity.RoboTabActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.google.gson.Gson;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity implements
		OnItemClickListener {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	private static final int PROGRESS_DIALOG_ID = 12;

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerSetAdapter adapter;

	private DaoMaster daoMaster;

	private DaoSession daoSession;

	private SpeakerDao speakerDao;

	private SQLiteDatabase db;

	private CharSequence title;

	private CharSequence description;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		
		int schemaVersion = daoMaster.getSchemaVersion();
		
		speakerDao = daoSession.getSpeakerDao();

		adapter = new SpeakerSetAdapter(
				this,
				speakerDao
						.queryBuilder()
						.orderAsc(
								pl.mobilization.speakermeter.dao.SpeakerDao.Properties.Name)
						.list());

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		title = getString(R.string.update);
		description = getString(R.string.obtaining_list);

		super.onResume();

		if (adapter.getCount() == 0) {
			launchJsonUpdate();
		}
	}

	@Override
	protected void onPause() {
		db.close();
		super.onPause();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.votes_menu, menu);
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == PROGRESS_DIALOG_ID) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(title);
			dialog.setMessage(description);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	private void launchJsonUpdate() {
		showDialog(PROGRESS_DIALOG_ID);
		new Thread((new JSonDownloader())).start();
	}

	private class JSonDownloader extends AbstractDownloader implements Runnable {
		private static final String URL = "http://mobilization.herokuapp.com/speakers/";

		public void cleanUp() {
			removeDialog(PROGRESS_DIALOG_ID);
		}

		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker[] speakerFromJson = gson.fromJson(json,
					Speaker[].class);
			runOnUiThread(new Runnable() {
				public void run() {
					for (Speaker speaker : speakerFromJson) {
						if (db.isOpen())
							speakerDao.insertOrReplace(speaker);
					}
					adapter.addItems(speakerFromJson);
				}
			});

		}

		@Override
		public URI createURI() {
			return URI.create(URL);
		}

		@Override
		public Activity getEnclosingClass() {
			return SpeakerListActivity.this;
		}
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		intent.putExtra(VoteActivity.SPEAKER_ID,
				adapter.getItemIdAtPosition(position));

		startActivity(intent);
	}
}