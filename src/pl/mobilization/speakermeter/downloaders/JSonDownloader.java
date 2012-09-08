package pl.mobilization.speakermeter.downloaders;

import java.net.URI;
import java.util.concurrent.Future;

import pl.mobilization.speakermeter.dao.Speaker;
import pl.mobilization.speakermeter.downloader.AbstractDownloader;
import android.app.Activity;

import com.google.gson.Gson;

public class JSonDownloader extends AbstractDownloader<Speaker[]>  {
	private static final String URL = "http://mobilization.herokuapp.com/speakers/";

	public JSonDownloader() {

		
	}

	public void cleanUp() {
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