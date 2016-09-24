package io.gitlab.mudassir.notes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import io.gitlab.mudassir.notes.commons.Constants;
import io.gitlab.mudassir.notes.structs.Note;
import io.gitlab.mudassir.notes.tasks.NoteDeleteHandler;
import io.gitlab.mudassir.notes.tasks.NoteFetchHandler;
import io.gitlab.mudassir.notes.tasks.NotePushHandler;

public class ListActivity extends AppCompatActivity implements NoteAdapter.Listener, NoteFetchHandler.Listener, NotePushHandler.Listener, SwipeRefreshLayout.OnRefreshListener, ActionMode.Callback {

	public static final int REQUEST_CODE = 786;

	private boolean isActionMode;
	private FirebaseAnalytics analytics;
	private List<Note> notes;
	private NoteAdapter adapter;
	private Properties properties;
	private SwipeRefreshLayout swipeRefreshLayout;
	private Toolbar toolbar;
	private RecyclerView recyclerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		analytics = FirebaseAnalytics.getInstance(this);

		Bundle args = getIntent().getExtras();
		properties = new Properties();
		properties.put(Constants.EMAIL_IMAP_HOST, args.getString(Constants.EMAIL_IMAP_HOST));
		properties.put(Constants.EMAIL_IMAP_PORT, args.getInt(Constants.EMAIL_IMAP_PORT));
		properties.put(Constants.EMAIL_IMAP_STARTTLS, args.getBoolean(Constants.EMAIL_IMAP_STARTTLS) + "");
		properties.put(Constants.PROPERTY_HOST, args.getString(Constants.EMAIL_IMAP_HOST));
		properties.put(Constants.PROPERTY_USER, args.getString(Constants.EMAIL_IMAP_USER));
		properties.put(Constants.PROPERTY_PASSWORD, args.getString(Constants.EMAIL_IMAP_PASSWORD));

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setRefreshing(true);
		onRefresh();

		adapter = new NoteAdapter(this, new ArrayList<Note>());

		isActionMode = false;

		recyclerView = (RecyclerView) findViewById(R.id.recycler);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Note note = new Note.Builder()
						.identifier(UUID.randomUUID().toString())
						.date(new Date(System.currentTimeMillis()))
						.title("")
						.body("")
						.build();
				notes.add(note);

				Bundle bundle = new Bundle();
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "New note created");
				analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

				showNote(note, notes.size() - 1);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			Note note = data.getParcelableExtra(Constants.NOTE_PARCELABLE_EXTRA);
			int index = data.getIntExtra(Constants.NOTE_INDEX, Constants.INVALID_INDEX);

			new NotePushHandler(this, properties, note, index).execute();

			// Refresh existing list with data
			notes.remove(index);
			notes.add(index, note);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.dollar:
				startActivity(new Intent(this, DonateActivity.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View view, int position) {
		if (!isActionMode) {
			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Existing note selected");
			analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

			showNote(notes.get(position), position);
		} else {
			toggleSelection(position);
		}
	}

	@Override
	public void onLongClick(View view, int position) {
		if (!isActionMode) {
			toolbar.startActionMode(this);
		}

		toggleSelection(position);
	}

	@Override
	public void onNotesCancelled(Exception e) {
		swipeRefreshLayout.setRefreshing(false);
		// TODO: 2016-08-30 Handle exception
	}

	@Override
	public void onNotesFetched(List<Note> notes) {
		swipeRefreshLayout.setRefreshing(false);
		// The notes are stored from earliest to latest,
		// which is the opposite of how they should be displayed
		Collections.reverse(notes);
		this.notes = notes;
		adapter.update(notes);
	}

	@Override
	public void onNotePushed() {
		// TODO: 2016-08-31 Show confirmation snackbar
	}

	@Override
	public void onNotePushCanceled() {
		// TODO: 2016-08-31 Show retry dialog
	}

	@Override
	public void onRefresh() {
		new NoteFetchHandler(this).execute(properties);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
		setStatusBarColor(getResources().getColor(R.color.grey_700));
		isActionMode = true;
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.bin:
				// Collect notes to delete
				// TODO: 2016-09-24 Refactor this
				Map<String, Note> selections = new HashMap<>();

				for (int i = 0; i < recyclerView.getChildCount(); i++) {
					if (recyclerView.getChildAt(i).isSelected()) {
						selections.put(notes.get(i).getIdentifier(), notes.get(i));
						toggleSelection(i);
					}
				}

				// Using iterator to avoid ConcurrentModificationException
				Iterator<Note> iterator = notes.iterator();
				while (iterator.hasNext()) {
					Note note = iterator.next();
					if (selections.get(note.getIdentifier()) != null) {
						iterator.remove();
					}
				}

				// Delete notes from server
				new NoteDeleteHandler(properties, selections).execute();
				mode.finish();
				adapter.notifyDataSetChanged();
				return true;

			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// Clear selections
		for (int i = 0; i < recyclerView.getChildCount(); i++) {
			if (recyclerView.getChildAt(i).isSelected()) {
				toggleSelection(i);
			}
		}

		setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		isActionMode = false;
	}

	private void showNote(Note note, int index) {
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(Constants.NOTE_INDEX, index);
		intent.putExtra(Constants.NOTE_PARCELABLE_EXTRA, note);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setStatusBarColor(int color) {
		getWindow().setStatusBarColor(color);
	}

	/**
	 * Update the colour of the specified view to indicate status
	 */
	private void toggleSelection(int position) {
		boolean isSelected = !recyclerView.getChildAt(position).isSelected();
		recyclerView.getChildAt(position).setSelected(isSelected);
		recyclerView.getChildAt(position).setBackgroundColor(isSelected ? 0xFFEEEEEE : 0x00000000);
	}
}
