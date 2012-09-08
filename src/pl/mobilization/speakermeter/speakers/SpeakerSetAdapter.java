package pl.mobilization.speakermeter.speakers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import pl.mobilization.speakermeter.R;
import pl.mobilization.speakermeter.dao.Speaker;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.Iterators;

public class SpeakerSetAdapter extends BaseAdapter {
	LayoutInflater inflater = null;
	private Set<Speaker> backingSet = new TreeSet<Speaker>(new Comparator<Speaker>() {

		public int compare(Speaker lhs, Speaker rhs) {
			int dateCompare = lhs.getStart_time().compareTo(rhs.getEnd_time());
			if(dateCompare != 0)
				return dateCompare;
			return lhs.getVenue().compareTo(rhs.getVenue());
		}
	});
	
	public SpeakerSetAdapter(Context context, Collection<Speaker> list) {
		inflater = (LayoutInflater) context.getSystemService
			      (Context.LAYOUT_INFLATER_SERVICE);
		
		backingSet.addAll(list);
	}
	
	public int getCount() {
		return backingSet.size();
	}

	public Speaker getItem(int position) {
		return Iterators.get(backingSet.iterator(), position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.speaker_info, null);	
		}
		
		Speaker speaker = getItem(position);
		
		TextView textViewSpeaker = (TextView) convertView.findViewById(R.id.textViewSpeaker);
		TextView textViewPresentation = (TextView) convertView.findViewById(R.id.textViewPresentation);
		TextView textViewRoom = (TextView) convertView.findViewById(R.id.textViewRoom);
		TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);
		
		textViewSpeaker.setText(speaker.getName());
		textViewPresentation.setText(speaker.getPresentation());
		textViewRoom.setText(speaker.getVenue());
		
		CharSequence startTime = DateFormat.format("hh:mm", speaker.getStart_time());
		CharSequence endTime = DateFormat.format("hh:mm", speaker.getEnd_time());
		
		textViewTime.setText(String.format("%s-%s", startTime, endTime));
		
		convertView.setTag(speaker);
		return convertView;
	}
	
	public void addItem(Speaker speaker) {
		backingSet.add(speaker);
		notifyDataSetChanged();
	}
	
	public void addItems(Speaker[] speakers) {
		for(Speaker speaker: speakers) {
			backingSet.add(speaker);
		}
		notifyDataSetChanged();
	}
}
