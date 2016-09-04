package io.github.mudassir.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.github.mudassir.notes.commons.Constants;
import io.github.mudassir.notes.tasks.LoginHandler;

/**
 *
 */
public class LoginActivity extends AppCompatActivity implements LoginHandler.Listener, TextView.OnEditorActionListener {

	private LoginHandler authTask = null;
	private EditText emailView;
	private EditText passwordView;
	private EditText hostView;
	private EditText portView;
	private SwitchCompat starttlsView;
	private SharedPreferences settings;
	private String user;
	private String password;
	private String host;
	private int port;
	private boolean starttls;
	private SwipeRefreshLayout refreshLayout;
	private FirebaseAnalytics analytics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		analytics = FirebaseAnalytics.getInstance(this);
		analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);

		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setEnabled(false);

		settings = getSharedPreferences(Constants.PACKAGE, MODE_PRIVATE);

		emailView = (EditText) findViewById(R.id.email);
		passwordView = (EditText) findViewById(R.id.password);
		hostView = (EditText) findViewById(R.id.host);
		portView = (EditText) findViewById(R.id.port);
		starttlsView = (SwitchCompat) findViewById(R.id.starttls);

		// Prepopulate the fields if possible. Some will be prepopulated with default values
		String user = settings.getString(Constants.EMAIL_IMAP_USER, "");
		String host = settings.getString(Constants.EMAIL_IMAP_HOST, "");
		int port = settings.getInt(Constants.EMAIL_IMAP_PORT, Constants.INVALID_INDEX);
		boolean starttls = settings.getBoolean(Constants.EMAIL_IMAP_STARTTLS, true);

		if (!TextUtils.isEmpty(user)) {
			emailView.setText(user);
		}

		if (!TextUtils.isEmpty(host)) {
			hostView.setText(host);
		} else if (!TextUtils.isEmpty(user)) {
			hostView.setText(getImapHostFromUser(user));
		}

		if (port != Constants.INVALID_INDEX) {
			portView.setText(port + "");
		} else {
			portView.setText(Constants.DEFAULT_IMAP_PORT + "");
		}

		starttlsView.setChecked(starttls);

		// Listen for the keyboard action button
		passwordView.setOnEditorActionListener(this);
		portView.setOnEditorActionListener(this);

		findViewById(R.id.email_sign_in_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.configure:
				// TODO: 2016-09-01 Hide the keyboard if it is visible
				// TODO: 2016-09-01 Animate the following
				findViewById(R.id.config_container).setVisibility(View.VISIBLE);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Intercept the keyboard action button to attempt login
	 */
	@Override
	public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
		if (id == EditorInfo.IME_ACTION_GO) {
			attemptLogin();
			return true;
		}
		return false;
	}

	/**
	 * Checks if required fields are valid and proceeds with
	 * login. If not, the user is directed back to the fields
	 * where attention is required.
	 */
	private void attemptLogin() {
		if (authTask != null) {
			return;
		}

		// Clear any preexisting errors
		emailView.setError(null);
		passwordView.setError(null);

		user = emailView.getText().toString();
		password = passwordView.getText().toString();
		host = hostView.getText().toString();
		port = Integer.parseInt(portView.getText().toString());
		starttls = starttlsView.isChecked();

		if (TextUtils.isEmpty(user)) {
			emailView.setError(getString(R.string.error_field_required));
			emailView.requestFocus();
		} else if (TextUtils.isEmpty(password)) {
			passwordView.setError(getString(R.string.error_field_required));
			passwordView.requestFocus();
		} else {
			if (TextUtils.isEmpty(host)) {
				// Attempt to salvage a hostname
				host = getImapHostFromUser(user);
				// Show the user which hostname was attempted
				hostView.setText(host);
			}
			authTask = new LoginHandler(user, password, host, port, starttls, this);
			authTask.execute();
			refreshLayout.setRefreshing(true);
		}
	}

	@Override
	public void onLoginComplete(boolean successful) {
		authTask = null;
		refreshLayout.setRefreshing(false);
		if (successful) {
			// Save values for next launch
			SharedPreferences.Editor edit = settings.edit();
			edit.putString(Constants.EMAIL_IMAP_USER, user);
			edit.putString(Constants.EMAIL_IMAP_HOST, host);
			edit.putInt(Constants.EMAIL_IMAP_PORT, port);
			edit.putBoolean(Constants.EMAIL_IMAP_STARTTLS, starttls);
			edit.apply();

			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Successful login");
			analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

			// Pass properties on to ListActivity for note handling
			Intent intent = new Intent(this, ListActivity.class);
			intent.putExtra(Constants.EMAIL_IMAP_USER, user);
			intent.putExtra(Constants.EMAIL_IMAP_PASSWORD, password);
			intent.putExtra(Constants.EMAIL_IMAP_HOST, host);
			intent.putExtra(Constants.EMAIL_IMAP_PORT, port);
			intent.putExtra(Constants.EMAIL_IMAP_STARTTLS, starttls);
			startActivity(intent);
		} else {
			// Retry

			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Unsuccessful login");
			analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

			emailView.setError(getString(R.string.error_login_failed));
			passwordView.setError(getString(R.string.error_login_failed));
			emailView.requestFocus();
		}
	}

	@Override
	public void onLoginCancelled() {
		authTask = null;
		refreshLayout.setRefreshing(false);

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Login error");
		analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
	}

	/**
	 * Attempts to generate an IMAP hostname
	 *
	 * @param user The IMAP user, assumed to be in valid form
	 * @return A plausible hostname
	 */
	private String getImapHostFromUser(String user) {
		// Split "johndoe@example.com" into {"johndoe", "example.com"}
		return Constants.DEFAULT_IMAP_PREFIX + user.split("@")[1];
	}
}
