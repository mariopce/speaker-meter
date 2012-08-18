package pl.mobilization.speakermeter.speakers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.R.id;
import pl.mobilization.speakermeter.R.layout;
import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.google.gson.Gson;

import de.greenrobot.dao.AbstractDao;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();
	private Handler handler;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;
	private AbstractDao<Speaker, Long> noteDao;
	private Cursor cursor;
	
	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;
	
	@InjectView(R.id.listViewSpeakers)
	private ListView speakersList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler  = new Handler();
		
	    DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        
        speakerDao = daoSession.getSpeakerDao();
        
        List<Speaker> list = speakerDao.queryBuilder().list();
        
        if(list.size() == 0) {
        	fetchNewList();
        }
        
        String textColumn = SpeakerDao.Properties.Name.columnName;
        String orderBy = textColumn + " COLLATE LOCALIZED ASC";
        cursor = db.query(speakerDao.getTablename(), speakerDao.getAllColumns(), null, null, null, null, orderBy);
        String[] from = { textColumn, SpeakerDao.Properties.Presentation.columnName };
        int[] to = { android.R.id.text1, android.R.id.text2 };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from,
                to);
//        setListAdapter(adapter);
	}
	
	
	private void fetchNewList() {
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

					progressBar.setVisibility(View.GONE);
					
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
}
