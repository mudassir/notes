package io.github.mudassir.notes;

import android.os.AsyncTask;
import android.text.Html;
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

/**
 * AsyncTask to fetch the notes using the specified properties.
 */
public class NoteRequestHandler extends AsyncTask<Properties, Void, List<Note>> {

	public static final String PACKAGE = "io.github.mudassir";
	public static final String TAG = "NoteRequestHandler";

	public static final String PROPERTY_HOST = PACKAGE + ".host";
	public static final String PROPERTY_USER = PACKAGE + ".user";
	public static final String PROPERTY_PASSWORD = PACKAGE + ".password";

	public interface NoteRequestReceiver {
		void onNotesReceived(List<Note> notes);
		void onNotesCancelled(Exception e);
	}

	private final NoteRequestReceiver receiver;

	public NoteRequestHandler(NoteRequestReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	protected List<Note> doInBackground(Properties... args) {
		List<Note> notes = new ArrayList<>();

		try {
			Properties properties = args[0];

			String host = (String) properties.remove(PROPERTY_HOST);
			String user = (String) properties.remove(PROPERTY_USER);
			String password = (String) properties.remove(PROPERTY_PASSWORD);

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
						.title(message.getSubject())
						.body(Html.fromHtml(message.getContent().toString()))
						.build();
				notes.add(note);
			}

			emailFolder.close(false);
			store.close();
		} catch (NullPointerException e) {
			receiver.onNotesCancelled(e);
		} catch (NoSuchProviderException e) {
			receiver.onNotesCancelled(e);
		} catch (MessagingException e) {
			receiver.onNotesCancelled(e);
		} catch (IOException e) {
			receiver.onNotesCancelled(e);
		}

		return notes;
	}

	@Override
	protected void onPostExecute(List<Note> notes) {
		super.onPostExecute(notes);
		receiver.onNotesReceived(notes);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		receiver.onNotesCancelled(new NullPointerException("Note fetch cancelled"));
	}
}
