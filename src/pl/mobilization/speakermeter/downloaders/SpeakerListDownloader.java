package pl.mobilization.speakermeter.downloaders;

import java.net.URI;

import pl.mobilization.speakermeter.ResultListener;
import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import pl.mobilization.speakermeter.speakers.SpeakerListActivity;
import pl.mobilization.speakermeter.venues.VenueTabActivity;

import android.app.Application;
import android.content.Intent;

import com.google.gson.Gson;

public class SpeakerListDownloader extends AbstractDownloader<Speaker[]>  {
	private static final String URL = "http://mobilization.herokuapp.com/speakers/";
	private Application application;

	public SpeakerListDownloader(Application context) {
		this.application = context;
	}

	public void processAnswer(String json) {
		Gson gson = new Gson();
		final Speaker[] speakerFromJson = gson.fromJson(json, Speaker[].class);
		setResult(speakerFromJson);
	}

	@Override
	public URI createURI() {
		return URI.create(URL);
	}
}