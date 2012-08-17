package pl.mobilization.speakermeter;

import java.util.List;

import org.mobilization.speakermeter.dao.DaoMaster;
import org.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import org.mobilization.speakermeter.dao.DaoSession;
import org.mobilization.speakermeter.dao.Speaker;
import org.mobilization.speakermeter.dao.SpeakerDao;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SimpleCursorAdapter;
import de.greenrobot.dao.AbstractDao;

@ContentView(R.layout.speaker_list)
public class SpeakerListActivity extends RoboActivity {
	private Handler handler;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;
	private AbstractDao<Speaker, Long> noteDao;
	private Cursor cursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		
	}
}
