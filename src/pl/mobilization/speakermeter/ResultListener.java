package pl.mobilization.speakermeter;

import pl.mobilization.speakermeter.downloader.AbstractDownloader;

public interface ResultListener<T> {
	public void resultAvailable(AbstractDownloader<T> downloader);
}
