package pl.mobilization.speakermeter.votes;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.dao.Speaker;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@ContentView(R.layout.vote)
public class VoteActivity extends RoboActivity implements OnClickListener,
		OnGlobalLayoutListener {

	private static final String TAG = VoteActivity.class.getName();
	public static final String SPEAKER_ID = "speaker_id";
	private static final long UKNOWN_SPEAKER_ID = 0;
	private static final int PROGRESS_DIALOG_ID = 1;
	private static final String SPEAKER = "speaker";
	private static final String UPDATE_TIME = "time";

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
	@InjectView(R.id.imageWho)
	private ImageView imageViewSoldier;

	private Speaker speaker;
	private boolean isUp = false;
	private String down;
	private String up;
	private String title;
	private long startTime;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();
		root.getViewTreeObserver().addOnGlobalLayoutListener(this);

		textViewUp.setOnClickListener(this);
		textViewDown.setOnClickListener(this);
		imageViewSoldier.setOnClickListener(this);

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

		Speaker speaker = getSpeakerMeterApplication().getSpeaker(speaker_id);
		if (speaker == null) {
			finish();
			return;
		}

		setSpeaker(speaker);
		new VoteUpdateChecker().run();
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
			dialog.setCancelable(false);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	private void setSpeaker(Speaker speaker) {
		if (speaker == null)
			return;

		this.speaker = speaker;
		textViewWho.setText(speaker.getName());
		textViewPresentation.setText(speaker.getPresentation());
		adjustVoteSpace();
	}

	public void onClick(View view) {
		if (view.equals(textViewUp) || view.equals(textViewDown)) {
			isUp = (view == textViewUp);
			Log.d(TAG, String.format("%s.showDialog()", this));
			showDialog(PROGRESS_DIALOG_ID);

			startTime = System.currentTimeMillis();
			handler.post(new VoteUpdateChecker());
			getSpeakerMeterApplication().launchVoteUpdate(speaker, isUp);
		} else if (view.equals(imageViewSoldier)) {
			Dialog dialog = new AlertDialog.Builder(this)
					.setIcon(R.id.imageWho)
					.setTitle(
							String.format("%s - %s", speaker.getPresentation(),
									speaker.getName()))
					.setMessage(speaker.getDescription())
					.setPositiveButton(R.string.ok, new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
			dialog.setOwnerActivity(this);
			dialog.show();
		}
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(UPDATE_TIME, startTime);
		outState.putSerializable(SPEAKER, speaker);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		startTime = state.getLong(UPDATE_TIME, 0L);
		speaker = (Speaker) state.getSerializable(SPEAKER);
		super.onRestoreInstanceState(state);
	}

	private class VoteUpdateChecker implements Runnable {
		private static final int MAX_WAIT_TIME = 30000;
		public static final int CHECK_INTERVAL = 1000;

		public void run() {
			if (hasTimedOut()) {
				Speaker updated_speaker = getSpeakerMeterApplication()
						.getSpeaker(speaker.getId());
				setSpeaker(updated_speaker);

				removeDialog(PROGRESS_DIALOG_ID);
				return;
			}

			if (!hasUpdatePending()) {
				String errorString = getSpeakerMeterApplication()
						.getVoteErrorString();
				if (didErrorOccured(errorString)) {
					removeDialog(PROGRESS_DIALOG_ID);
					signalError(errorString);
					return;
				}
			}

			handler.postDelayed(this, CHECK_INTERVAL);
		}

		private void signalError(String e) {
			Toast.makeText(VoteActivity.this, e, Toast.LENGTH_LONG).show();
		}

		private boolean didErrorOccured(String errorString) {
			return errorString != null;
		}

		private boolean hasUpdatePending() {
			return getSpeakerMeterApplication().hasVoteUpdatePending();
		}

		private boolean hasTimedOut() {
			return System.currentTimeMillis() - startTime > MAX_WAIT_TIME;
		}
	}
}