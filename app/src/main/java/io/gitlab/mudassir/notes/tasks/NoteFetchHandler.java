package io.gitlab.mudassir.notes.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import io.gitlab.mudassir.notes.commons.Constants;
import io.gitlab.mudassir.notes.structs.Note;

/**
 * AsyncTask to fetch the notes using the specified properties.
 */
public class NoteFetchHandler extends AsyncTask<Properties, Void, List<Note>> {

	public static final String TAG = "NoteFetchHandler";

	public interface Listener {
		void onNotesFetched(List<Note> notes);
		void onNotesCancelled(Exception e);
	}

	private final Listener listener;

	public NoteFetchHandler(Listener listener) {
		this.listener = listener;
	}

	@Override
	protected List<Note> doInBackground(Properties... args) {
		List<Note> notes = new ArrayList<>();

		try {
			Properties properties = args[0];

			String host = properties.getProperty(Constants.PROPERTY_HOST);
			String user = properties.getProperty(Constants.PROPERTY_USER);
			String password = properties.getProperty(Constants.PROPERTY_PASSWORD);

			// Create the POP3 store object and connect with the pop server
			Session emailSession = Session.getInstance(properties);
			Store store = emailSession.getStore("imaps");
			store.connect(host, user, password);
			Log.d(TAG, "connected to server");

			Folder emailFolder = store.getFolder("Notes");
			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = emailFolder.getMessages();
			for (Message message : messages) {
				Note note = new Note.Builder()
						.date(message.getSentDate())
						.identifier(message.getHeader(Constants.HEADER_UNIQUE_IDENTIFIER)[0])
						.title(message.getSubject())
						.body(message.getContent().toString())
						.build();

				notes.add(note);
			}

			emailFolder.close(false);
			store.close();
		} catch (NullPointerException e) {
			Log.e(TAG, "Unexpected null pointer", e);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "Incorrect provider", e);
		} catch (MessagingException e) {
			Log.e(TAG, "Server connection error", e);
		} catch (IOException e) {
			Log.e(TAG, "Invalid message content", e);
		}

		return notes;
	}

	@Override
	protected void onPostExecute(List<Note> notes) {
		super.onPostExecute(notes);
		listener.onNotesFetched(notes);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		listener.onNotesCancelled(new NullPointerException("Note fetch cancelled"));
	}
}
