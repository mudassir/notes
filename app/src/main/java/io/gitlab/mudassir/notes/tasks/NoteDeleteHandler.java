package io.gitlab.mudassir.notes.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import io.gitlab.mudassir.notes.commons.Constants;
import io.gitlab.mudassir.notes.structs.Note;

public class NoteDeleteHandler extends AsyncTask<Void, Void, Void> {

	public static final String TAG = "NoteDeleteHandler";

	private Map<String, Note> notes;
	private Properties properties;

	public NoteDeleteHandler(Properties properties, Map<String, Note> notes) {
		this.notes = notes;
		this.properties = properties;
	}

	@Override
	protected Void doInBackground(Void... args) {
		try {
			String host = properties.getProperty(Constants.PROPERTY_HOST);
			String user = properties.getProperty(Constants.PROPERTY_USER);
			String password = properties.getProperty(Constants.PROPERTY_PASSWORD);

			// Create the POP3 store object and connect with the pop server
			Session emailSession = Session.getInstance(properties);
			Store store = emailSession.getStore(Constants.EMAIL_IMAP_STORE);
			store.connect(host, user, password);
			Log.d(TAG, "connected to server");

			Folder emailFolder = store.getFolder("Notes");
			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = emailFolder.getMessages();
			for (Message message : messages) {
				if (notes.get(message.getHeader(Constants.HEADER_UNIQUE_IDENTIFIER)[0]) != null) {
					message.setFlag(Flags.Flag.DELETED, true);
				}
			}

			emailFolder.expunge();
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "Incorrect provider", e);
		} catch (MessagingException e) {
			Log.e(TAG, "Could not connect to server", e);
		}

		return null;
	}
}
