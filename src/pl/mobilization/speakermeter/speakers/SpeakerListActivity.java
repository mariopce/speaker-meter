package pl.mobilization.speakermeter.speakers;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpGet;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity implements
		OnItemClickListener {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerDaoAdapter adapter;

	private Handler handler;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();

		adapter = new SpeakerDaoAdapter(this);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		if (adapter.getCount() == 0) {
			launchJsonUpdate();
		}
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
		progressDialog = ProgressDialog.show(this, "Updating",
				"Obtaining list of speakers");
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
			runOnUiThread(new Runnable() {

				public void run() {
					progressDialog.dismiss();

				}
			});

		}

		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker[] fromJson = gson.fromJson(json, Speaker[].class);
			runOnUiThread(new Runnable() {
				public void run() {
					adapter.addItems(fromJson);
				}
			});

		}

		public HttpGet createRequest() {
			return new HttpGet(URI.create(URL));
		}

		@Override
		protected void exceptionHandler(Exception e) {
			if (e instanceof IOException) {
				Toast.makeText(SpeakerListActivity.this,
						"Problem with connection", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		intent.putExtra(VoteActivity.SPEAKER,
				(Speaker) adapter.getItemAtPosition(position));

		startActivity(intent);
	}
}