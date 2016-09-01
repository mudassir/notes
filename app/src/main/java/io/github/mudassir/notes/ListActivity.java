package io.github.mudassir.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import io.github.mudassir.notes.commons.Constants;
import io.github.mudassir.notes.structs.Note;
import io.github.mudassir.notes.tasks.NoteFetchHandler;
import io.github.mudassir.notes.tasks.NotePushHandler;

public class ListActivity extends AppCompatActivity implements ClickListener, NoteFetchHandler.NoteFetchListener, NotePushHandler.NotePushListener, SwipeRefreshLayout.OnRefreshListener {

	public static final int REQUEST_CODE = 786;

	private List<Note> notes;
	private NoteAdapter adapter;
	private Properties properties;
	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView recyclerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);

		properties = new Properties();
		properties.put(Constants.EMAIL_IMAP_HOST, getString(R.string.incoming_host));
		properties.put(Constants.EMAIL_IMAP_PORT, getString(R.string.incoming_port));
		properties.put(Constants.EMAIL_IMAP_STARTTLS, "true");
		properties.put(Constants.PROPERTY_HOST, getString(R.string.incoming_host));
		properties.put(Constants.PROPERTY_USER, getString(R.string.user));
		properties.put(Constants.PROPERTY_PASSWORD, getString(R.string.password));

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setRefreshing(true);
		onRefresh();

		adapter = new NoteAdapter(this, new ArrayList<Note>());

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
	public void onClick(View view, int position) {
		showNote(notes.get(position), position);
	}

	@Override
	public void onNotesCancelled(Exception e) {
		swipeRefreshLayout.setRefreshing(false);
		// TODO: 2016-08-30 Handle exception
	}

	@Override
	public void onNotesFetched(List<Note> notes) {
		swipeRefreshLayout.setRefreshing(false);
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

	private void showNote(Note note, int index) {
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(Constants.NOTE_INDEX, index);
		intent.putExtra(Constants.NOTE_PARCELABLE_EXTRA, note);
		startActivityForResult(intent, REQUEST_CODE);
	}
}