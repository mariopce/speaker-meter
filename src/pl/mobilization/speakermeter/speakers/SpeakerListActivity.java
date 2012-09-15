package pl.mobilization.speakermeter.speakers;

import java.util.Collection;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.votes.VoteActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity implements
		OnItemClickListener {
	private static final String TAG = SpeakerListActivity.class.getSimpleName();

	public static final String VENUE = "venue";

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerSetAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Intent intent = getIntent();
		final String venue = intent.getStringExtra(VENUE);

		Collection<Speaker> speakerList = ((SpeakerMeterApplication) getApplication())
				.getSpeakerList(venue);

		adapter = new SpeakerSetAdapter(this, speakerList);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		super.onResume();
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		long itemIdAtPosition = adapter.getItemIdAtPosition(position);
		intent.putExtra(VoteActivity.SPEAKER_ID,
				itemIdAtPosition);

		startActivity(intent);
	}
}