package pl.mobilization.speakermeter.votes;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

@ContentView(R.layout.vote)
public class VoteActivity extends RoboActivity implements OnClickListener,
		OnGlobalLayoutListener {
	public static final String SPEAKER = "speaker";
	@InjectView(R.id.textViewUp)
	private View textViewUp;
	@InjectView(R.id.textViewDown)
	private View textViewDown;
	@InjectView(R.id.root)
	private View root;
	@InjectView(R.id.textViewWho)
	private TextView textViewWho;
	@InjectView(R.id.textViewPresentation)
	private TextView textViewPresentation;

	private Speaker speaker;
	private Handler handler;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();
		
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        speakerDao = daoSession.getSpeakerDao();
        
		root.getViewTreeObserver().addOnGlobalLayoutListener(this);

		textViewUp.setOnClickListener(this);
		textViewDown.setOnClickListener(this);

		Intent intent = getIntent();
		Object object = intent.getExtras().get(SPEAKER);
		if (object == null) {
			finish();
			return;
		}

		setSpeaker((Speaker) object);
	}

	private void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
		textViewWho.setText(speaker.getName());
		textViewPresentation.setText(speaker.getPresentation());
		adjustVoteSpace();
	}

	public void onClick(View view) {
		VoteRunnable voteRunnable = null;
		ProgressDialog dialog = null;
		if (view == textViewUp) {
			dialog = ProgressDialog.show(this, "Sending your vote",
					String.format("You voted %s up", speaker.getName()), false);
			voteRunnable = new VoteRunnable(dialog, speaker.getId(), true);
		} else if (view == textViewDown) {
			dialog = ProgressDialog.show(this, "Sending your vote",
					String.format("You voted %s down", speaker.getName()), false);
			voteRunnable = new VoteRunnable(dialog, speaker.getId(), false);
		}

		if (voteRunnable != null) {
			dialog.show();
			new Thread(voteRunnable).start();
		}
	}

	public void onGlobalLayout() {
		root.getViewTreeObserver().removeGlobalOnLayoutListener(this);

		adjustVoteSpace();
	}

	private void adjustVoteSpace() {
		int height = root.getHeight();

		int votesUp = speaker.getVotesUp();
		int votesDown = speaker.getVotesDown();

		if(height == 0 || (votesDown == 0 && votesUp == 0))
			return;

		int votesUpHeight = height / 3 * (votesDown + 2 * votesUp)
				/ (votesDown + votesUp);
		int votesDownHeight = height  - votesUpHeight;

		LayoutParams layoutParams = textViewUp.getLayoutParams();
		layoutParams.height = votesUpHeight;
		textViewUp.setLayoutParams(layoutParams);

		layoutParams = textViewDown.getLayoutParams();
		layoutParams.height = votesDownHeight;
		textViewDown.setLayoutParams(layoutParams);
	}

	private class VoteRunnable extends AbstractDownloader implements Runnable {

		private static final String URL = "http://mobilization.herokuapp.com/speakers/%d/vote%s";
		private static final String DOWN = "down";
		private static final String UP = "up";
		private long id;
		private boolean isUp;
		private Dialog dialog;

		public VoteRunnable(Dialog dialog, long id, boolean isUp) {
			this.id = id;
			this.isUp = isUp;
			this.dialog = dialog;
		}

		@Override
		public void finalizer() {
			dialog.dismiss();
		}

		@Override
		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker updatedSpeaker = gson.fromJson(json, Speaker.class);
			speakerDao.insertOrReplace(updatedSpeaker);

			runOnUiThread(new Runnable() {
				public void run() {
					setSpeaker(updatedSpeaker);
				}
			});
		}

		@Override
		public HttpRequestBase createRequest() {
			return new HttpGet(URI.create(String.format(URL, id, isUp ? UP
					: DOWN)));
		}

		@Override
		protected void exceptionHandler(Exception e) {
			if(e instanceof IOException) {
				Toast.makeText(VoteActivity.this, "Problem with connection to the Internet", Toast.LENGTH_LONG).show();
			}
			
		}
	}

}