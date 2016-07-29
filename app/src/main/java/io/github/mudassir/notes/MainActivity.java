package io.github.mudassir.notes;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        try {
            // First fetch
            new AsyncTask<Void, Void, Void>() {
                private static final String TAG = "FetchAsyncTask";

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        // TODO dynamically obtain values from user
                        String outgoingHost = getResources().getString(R.string.outgoing_host);
                        String user = getResources().getString(R.string.user);
                        String password = getResources().getString(R.string.password);

                        Properties properties = new Properties();
                        properties.put("mail.imaps.host", outgoingHost);
                        properties.put("mail.imaps.port", "993");
                        properties.put("mail.imaps.starttls.enable", "true");

                        // Create the POP3 store object and connect with the pop server
                        Session emailSession = Session.getDefaultInstance(properties);
                        Store store = emailSession.getStore("imaps");
                        store.connect(outgoingHost, user, password);
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
            }.execute().get();

            // Then send
            new AsyncTask<Void, Void, Void>() {
                private static final String TAG = "SendAsyncTask";

                @Override
                protected Void doInBackground(final Void... pVoids) {
                    return null;
                }
            }.execute().get();

        } catch (InterruptedException e) {

        } catch (ExecutionException e) {

        }
    }
}
