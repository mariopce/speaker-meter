package pl.mobilization.speakermeter.tabs;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TabHost;

public class NPEImmuneTabHost extends TabHost {
	private static final String TAG = "NPEImmuneTabHost";
	private Map<String, TabSpec> tabSpecs = new HashMap<String, TabSpec>();

	public NPEImmuneTabHost(Context context) {
		super(context);
	}

	public NPEImmuneTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public TabSpec newTabSpec(String tag) {
		Log.d(TAG, String.format("newTabSpec(%s)", tag));
		TabSpec tabSpec = tabSpecs.get(tag);
		if (tabSpec != null) {
			return tabSpec;
		}
		return super.newTabSpec(tag);
	}
	
	@Override
	public void addTab(TabSpec tabSpec) {
		Log.d(TAG, String.format("addTab(%s)", tabSpec.getTag()));
		TabSpec put = tabSpecs.put(tabSpec.getTag(), tabSpec);
		if (put != null) {;
			Log.w(TAG, "duplicate - not adding");
//			return;//already there
		}
		
		super.addTab(tabSpec);
	}
	
	@Override
	public void clearAllTabs() {
		Log.w(TAG, "supressing clearing tabs to prevent NPE");
		super.clearAllTabs();
	}
}
