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

	private static final String LIST_INDEX = "list.index";

	private static final String LIST_TOP = "list.top";

	@InjectView(R.id.listViewSpeakers)
	private ListView listView;

	private SpeakerSetAdapter adapter;

	private int listViewSavedTop;

	private int listViewSavedIndex;

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

		listView.setSelectionFromTop(listViewSavedIndex, listViewSavedTop);
	}

	@Override
	protected void onPause() {
		saveListState();
		super.onPause();
	}

	private void saveListState() {
		listViewSavedIndex = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		listViewSavedTop = (v == null) ? 0 : v.getTop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveListState();
		outState.putInt(LIST_INDEX, listViewSavedIndex);
		outState.putInt(LIST_TOP, listViewSavedTop);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		listViewSavedIndex = savedInstanceState.getInt(LIST_INDEX, 0);
		listViewSavedTop = savedInstanceState.getInt(LIST_TOP, 0);
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Intent intent = new Intent(this, VoteActivity.class);
		long itemIdAtPosition = adapter.getItemIdAtPosition(position);
		intent.putExtra(VoteActivity.SPEAKER_ID, itemIdAtPosition);

		startActivity(intent);
	}
}