package io.gitlab.mudassir.notes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;

import java.util.Date;

import io.gitlab.mudassir.notes.commons.Constants;
import io.gitlab.mudassir.notes.structs.Note;
import jp.wasabeef.richeditor.RichEditor;

public class NoteActivity extends AppCompatActivity {

	private int position;
	private Note note;
	private RichEditor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_note);

		getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		editor = (RichEditor) findViewById(R.id.editor);
		editor.setPlaceholder(getString(R.string.editor_placeholder));
		editor.setPadding(16, 16, 16, 16);

		position = getIntent().getIntExtra(Constants.NOTE_INDEX, Constants.INVALID_INDEX);

		note = getIntent().getParcelableExtra(Constants.NOTE_PARCELABLE_EXTRA);
		editor.setHtml(note.getBody());
	}

	@Override
	public void onBackPressed() {
		saveNoteAndFinish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				saveNoteAndFinish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void saveNoteAndFinish() {
		note.setDate(new Date(System.currentTimeMillis()));
		note.setTitle(Html.fromHtml(editor.getHtml()).toString().split("\n")[0]);
		note.setBody(editor.getHtml());

		Intent intent = new Intent();
		intent.putExtra(Constants.NOTE_INDEX, position);
		intent.putExtra(Constants.NOTE_PARCELABLE_EXTRA, note);

		setResult(RESULT_OK, intent);
		finish();
	}
}
