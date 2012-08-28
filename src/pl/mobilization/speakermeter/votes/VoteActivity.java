package pl.mobilization.speakermeter.votes;

import java.net.URI;
import java.util.Date;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.dao.DaoMaster;
import pl.mobilization.speakermeter.dao.DaoMaster.DevOpenHelper;
import pl.mobilization.speakermeter.dao.DaoSession;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.dao.SpeakerDao;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.google.gson.Gson;

@ContentView(R.layout.vote)
public class VoteActivity extends RoboActivity implements OnClickListener,
		OnGlobalLayoutListener {
	public static final String SPEAKER_ID = "speaker_id";
	private static final long UKNOWN_SPEAKER_ID = 0;

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
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SpeakerDao speakerDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root.getViewTreeObserver().addOnGlobalLayoutListener(this);

		textViewUp.setOnClickListener(this);
		textViewDown.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "speakers-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		speakerDao = daoSession.getSpeakerDao();

		Intent intent = getIntent();
		long speaker_id = intent.getLongExtra(SPEAKER_ID, UKNOWN_SPEAKER_ID);
		if (speaker_id == UKNOWN_SPEAKER_ID) {
			finish();
			return;
		}

		Speaker speaker = speakerDao.load(speaker_id);

		if (speaker == null) {
			finish();
			return;
		}
		setSpeaker(speaker);

		super.onResume();
	}

	@Override
	protected void onPause() {
		db.close();
		super.onPause();
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
			dialog = ProgressDialog.show(this,
					getString(R.string.sending_vote),
					getString(R.string.speaker_voted_up, speaker.getName()),
					false);
			voteRunnable = new VoteRunnable(dialog, speaker.getId(), true);
		} else if (view == textViewDown) {
			dialog = ProgressDialog.show(this,
					getString(R.string.sending_vote),
					getString(R.string.speaker_voted_down, speaker.getName()),
					false);
			voteRunnable = new VoteRunnable(dialog, speaker.getId(), false);
		}

		if (voteRunnable != null) {
			dialog.setOwnerActivity(this);
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

		if (height == 0 || (votesDown == 0 && votesUp == 0))
			return;

		int votesUpHeight = height / 3 * (votesDown + 2 * votesUp)
				/ (votesDown + votesUp);

		LayoutParams layoutParams = textViewUp.getLayoutParams();
		layoutParams.height = votesUpHeight;
		textViewUp.setLayoutParams(layoutParams);

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
		public void exit() {
			if (dialog.isShowing())
				dialog.dismiss();
		}

		@Override
		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker updatedSpeaker = gson.fromJson(json, Speaker.class);
			if (db.isOpen())
				speakerDao.insertOrReplace(updatedSpeaker);

			runOnUiThread(new Runnable() {
				public void run() {
					setSpeaker(updatedSpeaker);
				}
			});
		}

		public URI createURI() {
			return URI.create(String.format(URL, id, isUp ? UP : DOWN));
		}

		@Override
		public void addCookies(URI uri, CookieStore cookieStore) {
			BasicClientCookie cookie = new BasicClientCookie(SpeakerMeterApplication.UUID,
					SpeakerMeterApplication.getUUID());
			cookie.setDomain(uri.getHost());
			cookie.setPath(uri.getPath());
			cookie.setValue(SpeakerMeterApplication.getUUID());
			cookie.setExpiryDate(new Date(2012, 12, 12));
			cookieStore.addCookie(cookie);
		}

		@Override
		public Activity getEnclosingClass() {
			return VoteActivity.this;
		}
	}
}