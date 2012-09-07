package pl.mobilization.speakermeter;

import pl.mobilization.speakermeter.speakers.SpeakerListActivity;
import roboguice.activity.RoboTabActivity;
import roboguice.inject.ContentView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@ContentView(R.layout.hall_list)
public class HallListActivity extends RoboTabActivity{
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TabHost tabHost = getTabHost();
        
        tabHost.setId(android.R.id.tabhost);
        
        
        TabSpec tabSpec = tabHost.newTabSpec("All").setIndicator("All venues");
        tabSpec.setContent(new Intent(this, SpeakerListActivity.class));
		tabHost.addTab(tabSpec);
    }
  
}
