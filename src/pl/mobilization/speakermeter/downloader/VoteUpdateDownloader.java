package pl.mobilization.speakermeter.downloader;

import java.net.URI;
import java.util.Date;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import pl.mobilization.speakermeter.SpeakerMeterApplication;
import pl.mobilization.speakermeter.dao.Speaker;

import com.google.gson.Gson;

public class VoteUpdateDownloader extends AbstractDownloader<Speaker> implements
		Runnable {

	private static final String URL = "http://mobilization.herokuapp.com/speakers/%d/vote%s";
	private static final String DOWN = "down";
	private static final String UP = "up";
	private long id;
	private boolean isUp;
	private String uuid;

	public VoteUpdateDownloader(long id, boolean isUp, String uuid) {
		this.id = id;
		this.isUp = isUp;
		this.uuid = uuid;
	}

	@Override
	public void processAnswer(String json) {
		Gson gson = new Gson();
		final Speaker updatedSpeaker = gson.fromJson(json, Speaker.class);
		setResult(updatedSpeaker);
	}

	public URI createURI() {
		return URI.create(String.format(URL, id, isUp ? UP : DOWN));
	}

	@Override
	public void addCookies(URI uri, CookieStore cookieStore) {
		BasicClientCookie cookie = new BasicClientCookie(
				SpeakerMeterApplication.UUID, uuid);
		cookie.setDomain(uri.getHost());
		cookie.setPath(uri.getPath());
		cookie.setValue(uuid);
		cookie.setExpiryDate(new Date(2012, 12, 12));
		cookieStore.addCookie(cookie);
	}
}