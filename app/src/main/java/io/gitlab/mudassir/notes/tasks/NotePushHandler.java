package io.gitlab.mudassir.notes.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.gitlab.mudassir.notes.commons.Constants;
import io.gitlab.mudassir.notes.structs.Note;

/**
 *
 */
public class NotePushHandler extends AsyncTask<Void, Void, Void> {

	public static final String TAG = "NotePushHandler";

	public interface NotePushListener {
		void onNotePushed();
		void onNotePushCanceled();
	}

	private int index;
	private Note note;
	private NotePushListener listener;
	private Properties properties;

	public NotePushHandler(NotePushListener listener, Properties properties, Note note, int index) {
		this.listener = listener;
		this.properties = properties;
		this.note = note;
		this.index = index;
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

			/*
			 * Delete old message if it previously existed
			 */
			Message[] messages = emailFolder.getMessages();
			if (index != Constants.INVALID_INDEX && index < messages.length) {
				Message oldMessage = null;
				if (messages[index].getHeader(Constants.HEADER_UNIQUE_IDENTIFIER)[0].equals(note.getIdentifier())) {
					oldMessage = messages[index];
				} else {
					for (Message message : messages) {
						if (message.getHeader(Constants.HEADER_UNIQUE_IDENTIFIER)[0].equals(note.getIdentifier())) {
							oldMessage = message;
							break;
						}
					}
				}
				if (oldMessage != null) {
					oldMessage.setFlag(Flags.Flag.DELETED, true);
					emailFolder.expunge();
				}
			}

			/*
			 * Add updated message (or new message, if nothing previously existed)
			 */
			Message newMessage = new MimeMessage(emailSession);
			newMessage.setFrom(new InternetAddress(user));
			newMessage.setSentDate(note.getDate());
			newMessage.setSubject(note.getTitle());
			newMessage.setContent(note.getBody(), Constants.CONTENT_TYPE);
			newMessage.setHeader(Constants.HEADER_TYPE_IDENTIFIER, Constants.APPLE_PACKAGE);
			newMessage.setHeader(Constants.HEADER_UNIQUE_IDENTIFIER, note.getIdentifier());
			newMessage.setFlag(Flags.Flag.DRAFT, true);
			emailFolder.appendMessages(new Message[] {newMessage});

			emailFolder.close(false);
			store.close();
			listener.onNotePushed();
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "Incorrect provider", e);
		} catch (MessagingException e) {
			Log.e(TAG, "Could not connect to server", e);
		}

		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		listener.onNotePushCanceled();
	}
}
