package pl.mobilization.speakermeter;

import pl.mobilization.speakermeter.speakers.SpeakerListActivity;
import roboguice.activity.RoboActivity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends RoboActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    Intent intent = new Intent(this, SpeakerListActivity.class);
		startActivity(intent);
	}

}
