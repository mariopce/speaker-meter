package pl.mobilization.speakermeter.speakers;

import pl.mobilization.speakermeter.R;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;
	
	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private ListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new SpeakerDaoAdapter(this);
		
		listView.setAdapter(adapter);
		
		if(adapter.getCount() == 0) {
			
		}
	}
}