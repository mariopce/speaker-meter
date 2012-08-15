package pl.mobilization.speakermeter;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import android.os.Bundle;
import android.os.Handler;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity {
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
}
