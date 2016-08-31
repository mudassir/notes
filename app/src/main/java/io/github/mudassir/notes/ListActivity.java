package io.github.mudassir.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ListActivity extends AppCompatActivity implements ClickListener, NoteRequestHandler.NoteRequestReceiver, SwipeRefreshLayout.OnRefreshListener {

	public static final String NOTE_PARCELABLE_EXTRA = "io.github.mudassir.notes.parcelable-note";

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
		properties.put("mail.imaps.host", getString(R.string.incoming_host));
		properties.put("mail.imaps.port", getString(R.string.incoming_port));
		properties.put("mail.imaps.starttls.enable", "true");
		properties.put(NoteRequestHandler.PROPERTY_HOST, getString(R.string.incoming_host));
		properties.put(NoteRequestHandler.PROPERTY_USER, getString(R.string.user));
		properties.put(NoteRequestHandler.PROPERTY_PASSWORD, getString(R.string.password));

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setRefreshing(true);
		onRefresh();

		adapter = new NoteAdapter(this, new ArrayList<Note>());

		recyclerView = (RecyclerView) findViewById(R.id.recycler);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onClick(View view, int position) {
		Intent intent = new Intent(this, NoteActivity.class);
		intent.putExtra(NOTE_PARCELABLE_EXTRA, notes.get(position));
		startActivity(intent);
	}

	@Override
	public void onNotesCancelled(Exception e) {
		swipeRefreshLayout.setRefreshing(false);
		// TODO: 2016-08-30 Handle exception
	}

	@Override
	public void onNotesReceived(List<Note> notes) {
		swipeRefreshLayout.setRefreshing(false);
		this.notes = notes;
		adapter.update(notes);
	}

	@Override
	public void onRefresh() {
		new NoteRequestHandler(this).execute(properties);
	}
}
