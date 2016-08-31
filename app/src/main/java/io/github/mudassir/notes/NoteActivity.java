package io.github.mudassir.notes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.wasabeef.richeditor.RichEditor;

public class NoteActivity extends AppCompatActivity {

	private RichEditor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		editor = (RichEditor) findViewById(R.id.editor);
		editor.setPlaceholder("this is the placeholder");
		editor.setPadding(16, 16, 16, 16);

		Note note = getIntent().getParcelableExtra(ListActivity.NOTE_PARCELABLE_EXTRA);
		editor.getSettings().setJavaScriptEnabled(true);
		editor.loadData(note.getBody(), "text/html", "charset=us-ascii");
	}
}
