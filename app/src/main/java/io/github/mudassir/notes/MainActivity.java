package io.github.mudassir.notes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

	private static final String PACKAGE = "io.github.mudassir";
	private static final String COUNT_KEY = PACKAGE + ".mail-count";
    private static final String TAG = "MainActivity";

	/**
	 * Simple counter stored in the shared preferences
	 * to keep track of how many emails are being sent
	 */
	private static int getCount(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		int count = preferences.getInt(COUNT_KEY, -1);

		editor.putInt(COUNT_KEY, ++count);
		editor.apply();

		return count;
	}

	private final AsyncTask<Void, Void, Void> fetchAsyncTask = new AsyncTask<Void, Void, Void>() {
		private static final String TAG = "FetchAsyncTask";

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// TODO dynamically obtain values from user
				String incomingHost = getString(R.string.incoming_host);
				String incomingPort = getString(R.string.incoming_port);
				String user = getString(R.string.user);
				String password = getString(R.string.password);

				Properties properties = new Properties();
				properties.put("mail.imaps.host", incomingHost);
				properties.put("mail.imaps.port", incomingPort);
				properties.put("mail.imaps.starttls.enable", "true");

				// Create the POP3 store object and connect with the pop server
				Session emailSession = Session.getInstance(properties);
				Store store = emailSession.getStore("imaps");
				store.connect(incomingHost, user, password);
				Log.d(TAG, "connected to server");

				Folder emailFolder = store.getFolder("Notes");
				emailFolder.open(Folder.READ_WRITE);

				Message[] messages = emailFolder.getMessages();
				for (int i = 0, n = messages.length; i < n; i++) {
					Message message = messages[i];
					Log.d(TAG, "---------------------------------");
					Log.d(TAG, "Email Number " + (i + 1));
					Log.d(TAG, "Subject: " + message.getSubject());
					Log.d(TAG, "Text: " + message.getContent());
				}

				emailFolder.close(false);
				store.close();
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	};

	private final AsyncTask<Void, Void, Void> sendAsyncTask = new AsyncTask<Void, Void, Void>() {
		private static final String TAG = "SendAsyncTask";

		@Override
		protected Void doInBackground(final Void... params) {
			try {
				String outgoingHost = getString(R.string.outgoing_host);
				String outgoingPort = getString(R.string.outgoing_port);
				String recipient = getString(R.string.recipient);

				final String user = getString(R.string.user);
				final String password = getString(R.string.password);

				Properties properties = new Properties();
				properties.put("mail.smtp.host", outgoingHost);
				properties.put("mail.smtp.socketFactory.port", outgoingPort);
				properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				properties.put("mail.smtp.port", outgoingPort);
				properties.put("mail.smtp.auth", "true");

				Session session = Session.getInstance(properties,
						new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(user, password);
							}
						});

				Log.d(TAG, "Session successful");

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(user));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
				message.setSubject("Testing Subject " + getCount(getApplicationContext()));
				message.setText("this is some text");

				Transport.send(message);

				Log.d(TAG, "Message sent");

			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

			return null;
		}
	};

	private final AsyncTask<Void, Void, Void> draftAsyncTask = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(final Void... voids) {
			return null;
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		findViewById(R.id.fetch).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				fetchAsyncTask.execute();
			}
		});

		findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				sendAsyncTask.execute();
			}
		});

		findViewById(R.id.draft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				draftAsyncTask.execute();
			}
		});
    }
}
