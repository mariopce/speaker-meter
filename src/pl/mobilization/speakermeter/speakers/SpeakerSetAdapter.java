package pl.mobilization.speakermeter.speakers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import pl.mobilization.speakermeter.dao.Speaker;
import android.content.Context;
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
			return lhs.getName().compareTo(rhs.getName());
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
			convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);	
		}
		
		Speaker speaker = getItem(position);
		
		TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
		TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
		
		text1.setText(speaker.getName());
		text2.setText(speaker.getPresentation());
		
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
