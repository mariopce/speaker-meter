package pl.mobilization.speakermeter.speakers;

import com.google.common.base.Strings;

import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SpeakerDaoAdapter extends BaseAdapter {
	LayoutInflater inflater = null;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;

	public SpeakerDaoAdapter(Context context) {
		inflater = (LayoutInflater) context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "speakers-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        
        speakerDao = daoSession.getSpeakerDao();
	}
	
	public int getCount() {
		return (int) speakerDao.count();
	}

	public Speaker getItem(int position) {
		return speakerDao.queryBuilder().build().listLazy().get(position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);	
		}
		
		Speaker speaker = getItem(position);
		
		TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
		TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
		
		
		convertView.setClickable(false);
		convertView.setFocusable(false);
		text1.setClickable(false);
		text1.setFocusable(false);
		text2.setClickable(false);
		text2.setFocusable(false);
		
		text1.setText(speaker.getName());
		text2.setText(speaker.getPresentation());
		
		convertView.setTag(speaker);
		return convertView;
	}
	
	public void addItem(Speaker speaker) {
		speakerDao.insertOrReplace(ensureDbReady(speaker));
		notifyDataSetChanged();
	}
	
	public void addItems(Speaker[] speakers) {
		for(Speaker speaker: speakers) {
			speakerDao.insertOrReplace(ensureDbReady(speaker));
		}
		notifyDataSetChanged();
	}
	
	private Speaker ensureDbReady(Speaker speaker) {
		speaker.setPresentation(Strings.nullToEmpty(speaker.getPresentation()));
		return speaker;
	}
}
