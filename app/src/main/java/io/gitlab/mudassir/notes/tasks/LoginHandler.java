package io.gitlab.mudassir.notes.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 */
public class LoginHandler extends AsyncTask<Void, Void, Boolean> {

	public static final String TAG = "LoginHandler";

	public interface Listener {
		void onLoginComplete(boolean successful);
		void onLoginCancelled();
	}

	private Listener listener;
	private String user;
	private String password;
	private String host;
	private int port;
	private boolean starttls;

	public LoginHandler (String user, String password, String host, int port, boolean starttls, Listener listener) {
		this.listener = listener;
		this.user = user;
		this.password = password;
		this.host = host;
		this.port = port;
		this.starttls = starttls;
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		try {
			Properties properties = new Properties();
			properties.put("mail.imaps.host", host);
			properties.put("mail.imaps.port", port);
			properties.put("mail.imaps.starttls.enable", starttls + "");

			// Create the POP3 store object and connect with the pop server
			Session emailSession = Session.getInstance(properties);
			Store store = emailSession.getStore("imaps");
			store.connect(host, user, password);
			store.close();

			return true;
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "Incorrect provider", e);
		} catch (MessagingException e) {
			Log.e(TAG, "Could not connect to server", e);
		}

		return false;
	}

	@Override
	protected void onPostExecute(Boolean bool) {
		super.onPostExecute(bool);
		listener.onLoginComplete(bool);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		listener.onLoginCancelled();
	}
}
