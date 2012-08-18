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
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
		
		if (adapter.getCount() != 0) {
			return;
		}

		launchJsonUpdate();
	}

	private void launchJsonUpdate() {
		progressBar.setVisibility(View.VISIBLE);
		handler.post(new JSonDownloader());
	}

	private class JSonDownloader implements Runnable {
		private static final String URL = "http://mobilization.herokuapp.com/speakers/";

		public void run() {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			try {
				HttpGet request = new HttpGet(URI.create(URL));
				request.addHeader("Accept", "application/json");

				response = httpclient.execute(request);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					// Closes the connection when status is not OK
					response.getEntity().getContent().close();
					return;
				}

				String json = extractPageAsString(response);

				Gson gson = new Gson();
				Speaker[] fromJson = gson.fromJson(json, Speaker[].class);
				adapter.addItems(fromJson);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			} finally {
				progressBar.setVisibility(View.GONE);
			}

		}

		private String extractPageAsString(HttpResponse response)
				throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			String responseString = out.toString();
			return responseString;
		}
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		intent.putExtra(VoteActivity.SPEAKER, (Speaker)adapter.getItemAtPosition(position));
		
		startActivity(intent);
	}
}