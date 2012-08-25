package pl.mobilization.speakermeter.speakers;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpGet;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity implements
		OnItemClickListener {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerSetAdapter adapter;

	private ProgressDialog progressDialog;

	private DaoMaster daoMaster;

	private DaoSession daoSession;

	private SpeakerDao speakerDao;

	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        speakerDao = daoSession.getSpeakerDao();
        
		adapter = new SpeakerSetAdapter(this, speakerDao.queryBuilder().orderAsc(pl.mobilization.speakermeter.dao.SpeakerDao.Properties.Name).list());

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		if (adapter.getCount() == 0) {
			launchJsonUpdate();
		}
		super.onResume();
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

	private void launchJsonUpdate() {
		progressDialog = ProgressDialog.show(this, "Update",
				"Obtaining list of speakers");
		progressDialog.setOwnerActivity(this);
		progressDialog.show();
		new Thread((new JSonDownloader(progressDialog))).start();
	}

	private class JSonDownloader extends AbstractDownloader implements Runnable {
		private static final String URL = "http://mobilization.herokuapp.com/speakers/";
		private ProgressDialog progressDialog;

		public JSonDownloader(ProgressDialog progressDialog) {
			this.progressDialog = progressDialog;
		}

		public void finalizer() {
			progressDialog.dismiss();
		}

		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker[] speakerFromJson = gson.fromJson(json, Speaker[].class);
			runOnUiThread(new Runnable() {
				public void run() {
					for(Speaker speaker: speakerFromJson) {
						if(db.isOpen())
							speakerDao.insertOrReplace(speaker);
					}
					adapter.addItems(speakerFromJson);
				}
			});

		}

		@Override
		protected void exceptionHandler(Exception e) {
			if (e instanceof IOException) {
				runOnUiThread(new Runnable() {
					
					public void run() {
						Toast.makeText(SpeakerListActivity.this,
								"Problem with connection to the Internet",
								Toast.LENGTH_LONG).show();
					}
				});				
			}
		}

		@Override
		public URI createURI() {
			return URI.create(URL);
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