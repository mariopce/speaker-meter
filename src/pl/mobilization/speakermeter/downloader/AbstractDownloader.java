package pl.mobilization.speakermeter.downloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public abstract class AbstractDownloader {
	private static final String TAG = AbstractDownloader.class.getName();
	public void run() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			HttpRequestBase request = createRequest();
			request.addHeader("Accept", "application/json");

			response = httpclient.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				// Closes the connection when status is not OK
				response.getEntity().getContent().close();
				return;
			}

			String json = extractPageAsString(response);

			processAnswer(json);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException", e);
			exceptionHandler(e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
			exceptionHandler(e);
		} finally {
			finalizer();
		}
	}
	
	protected  abstract void exceptionHandler(Exception e);

	private String extractPageAsString(HttpResponse response)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		out.close();
		String responseString = out.toString();
		return responseString;
	}

	public abstract void finalizer();

	public abstract void processAnswer(String json);

	public abstract HttpRequestBase createRequest();
}
