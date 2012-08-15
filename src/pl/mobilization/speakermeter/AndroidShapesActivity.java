package pl.mobilization.speakermeter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

public class AndroidShapesActivity extends Activity implements OnClickListener,
		OnGlobalLayoutListener {
	private View textViewUp;
	private View textViewDown;
	private View root;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textViewUp = findViewById(R.id.textViewUp);
		textViewDown = findViewById(R.id.textViewDown);

		root = findViewById(R.id.root);
		root.getViewTreeObserver().addOnGlobalLayoutListener(this);
		
		textViewUp.setOnClickListener(this);
		textViewDown.setOnClickListener(this);
	}

	public void onClick(View view) {
		if(view == textViewUp) {
			Toast.makeText(this, "You voted speaker up", Toast.LENGTH_SHORT).show();
		}
		else if (view == textViewDown) {
			Toast.makeText(this, "You voted speaker down", Toast.LENGTH_SHORT).show();
		}
	}

	public void onGlobalLayout() {
		root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		LayoutParams layoutParams = textViewUp.getLayoutParams();
		layoutParams.height = root.getHeight() / 2;
		textViewUp.setLayoutParams(layoutParams);

		layoutParams = textViewDown.getLayoutParams();
		layoutParams.height = root.getHeight() / 2;
		textViewDown.setLayoutParams(layoutParams);
	}
}