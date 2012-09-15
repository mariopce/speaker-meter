package pl.mobilization.speakermeter.downloader;

import java.net.URI;

import pl.mobilization.speakermeter.dao.Speaker;
import android.app.Application;

import com.google.gson.Gson;

public class SpeakerListDownloader extends AbstractDownloader<Speaker[]> {
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