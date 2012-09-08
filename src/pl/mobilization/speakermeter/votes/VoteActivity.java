package pl.mobilization.speakermeter.votes;

import java.net.URI;
import java.util.Date;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.google.gson.Gson;

@ContentView(R.layout.vote)
public class VoteActivity extends RoboActivity implements OnClickListener,
		OnGlobalLayoutListener {

	private static final String TAG = VoteActivity.class.getName();
	public static final String SPEAKER_ID = "speaker_id";
	private static final long UKNOWN_SPEAKER_ID = 0;
	private static final int PROGRESS_DIALOG_ID = 1;
	private static final String SPEAKER = "speaker";

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
	private boolean isUp = false;
	private String down;
	private String up;
	private String title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root.getViewTreeObserver().addOnGlobalLayoutListener(this);

		textViewUp.setOnClickListener(this);
		textViewDown.setOnClickListener(this);

		title = getString(R.string.sending_vote);
		up = getString(R.string.up);
		down = getString(R.string.down);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		long speaker_id = intent.getLongExtra(SPEAKER_ID, UKNOWN_SPEAKER_ID);
		if (speaker_id == UKNOWN_SPEAKER_ID) {
			finish();
			return;
		}

		setSpeaker(getSpeakerMeterApplication().getSpeaker(speaker_id));

		if (speaker == null) {
			finish();
			return;
		}
	}

	private SpeakerMeterApplication getSpeakerMeterApplication() {
		return (SpeakerMeterApplication) getApplication();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == PROGRESS_DIALOG_ID) {
			String name = speaker.getName();
			String description = getString(R.string.speaker_voted, name,
					isUp ? up : down);
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(title);
			dialog.setMessage(description);
			dialog.setCancelable(true);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	private void setSpeaker(Speaker speaker) {
		if(speaker == null)
			return;
		
		this.speaker = speaker;
		textViewWho.setText(speaker.getName());
		textViewPresentation.setText(speaker.getPresentation());
		adjustVoteSpace();
	}

	public void onClick(View view) {
		VoteRunnable voteRunnable = null;
		isUp = view == textViewUp;
		Log.d(TAG, String.format("%s.showDialog()", this));
		showDialog(PROGRESS_DIALOG_ID);

		voteRunnable = new VoteRunnable(speaker.getId(), isUp);
		new Thread(voteRunnable).start();
	}

	public void onGlobalLayout() {
		root.getViewTreeObserver().removeGlobalOnLayoutListener(this);

		adjustVoteSpace();
	}

	private void adjustVoteSpace() {
		int height = root.getHeight();

		int votesUp = speaker.getVotes_down();
		int votesDown = speaker.getVotes_up();

		if (height == 0) 
			return;

		int votesUpHeight = height / 3 * (3 + votesDown + 2 * votesUp)
				/ (votesDown + votesUp + 2);

		LayoutParams layoutParams = textViewUp.getLayoutParams();
		layoutParams.height = votesUpHeight;
		textViewUp.setLayoutParams(layoutParams);

	}

	private class VoteRunnable extends AbstractDownloader implements Runnable {

		private static final int VOTE_LATTENCY = 5000;
		private static final String URL = "http://mobilization.herokuapp.com/speakers/%d/vote%s";
		private static final String DOWN = "down";
		private static final String UP = "up";
		private long id;
		private boolean isUp;

		public VoteRunnable(long id, boolean isUp) {
			this.id = id;
			this.isUp = isUp;
		}

		public void cleanUp() {
			Log.d(TAG, String.format("%s.remove()", VoteActivity.this));
			removeDialog(PROGRESS_DIALOG_ID);
		}

		@Override
		public void processAnswer(String json) {
			Gson gson = new Gson();
			final Speaker updatedSpeaker = gson.fromJson(json, Speaker.class);

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
			String uuid = ((SpeakerMeterApplication)getApplication()).getUUID();
			BasicClientCookie cookie = new BasicClientCookie(
					SpeakerMeterApplication.UUID,
					uuid);
			cookie.setDomain(uri.getHost());
			cookie.setPath(uri.getPath());
			cookie.setValue(uuid);
			cookie.setExpiryDate(new Date(2012, 12, 12));
			cookieStore.addCookie(cookie);
		}

		public Activity getEnclosingClass() {
			return VoteActivity.this;
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Speaker speaker = (Speaker) savedInstanceState.getSerializable(SPEAKER);
		setSpeaker(speaker);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SPEAKER, speaker);
		super.onSaveInstanceState(outState);
	}
}