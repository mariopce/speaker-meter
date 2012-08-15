package pl.mobilization.speakermeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    Intent intent = new Intent(this, SpeakerListActivity.class);
		startActivity(intent);
	}

}
