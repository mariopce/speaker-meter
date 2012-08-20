package pl.mobilization.speakermeter.speakers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity implements OnItemClickListener {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerDaoAdapter adapter;

	private Handler handler;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.votes_menu, menu);
		return true;
	}
	
	private void launchJsonUpdate() {
		progressBar.setVisibility(View.VISIBLE);
		handler.postAtFrontOfQueue(new JSonDownloader());
	}

	private class JSonDownloader extends AbstractDownloader implements Runnable {
		private static final String URL = "http://mobilization.herokuapp.com/speakers/";
		
		public void finalizer() {
			progressBar.setVisibility(View.GONE);
		}

		public void processAnswer(String json) {
			Gson gson = new Gson();
			Speaker[] fromJson = gson.fromJson(json, Speaker[].class);
			adapter.addItems(fromJson);
		}

		public HttpGet createRequest() {
			return new HttpGet(URI.create(URL));
		}
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		intent.putExtra(VoteActivity.SPEAKER, (Speaker)adapter.getItemAtPosition(position));
		
		startActivity(intent);
	}
}