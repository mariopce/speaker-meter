package pl.mobilization.speakermeter;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();
	private Handler handler;
	
	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;
	
	@InjectView(R.id.listViewSpeakers)
	private ListView speakersList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler  = new Handler();
		
		handler.post(new JSonDownloader());
	}
	
	
	private class JSonDownloader implements Runnable{
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
					}

					String json = extractPageAsString(response);
					
					Gson gson = new Gson();
					Speaker[] fromJson = gson.fromJson(json, Speaker[].class);
//					gson.fromJson(json, classOfT);

					progressBar.setVisibility(View.GONE);
					
					ListAdapter listAdapter = new ArrayAdapter<Speaker>(SpeakerListActivity.this, resource, textViewResourceId, objects)
					
					speakersList.setAdapter()
				} catch (ClientProtocolException e) {
					Log.e(TAG, "ClientProtocolException",e);
				} catch (IOException e) {
					Log.e(TAG, "IOException",e);
//				} catch (JSONException e) {
//					Log.e(TAG, "JSONException",e);
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
	
	private static class Speaker {
		public int id;
		public String name;
		public int votes;
		public int votes_up;
	}
}
