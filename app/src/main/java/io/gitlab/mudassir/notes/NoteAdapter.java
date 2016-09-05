package io.gitlab.mudassir.notes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.gitlab.mudassir.notes.structs.Note;

/**
 * Basic implementation of RecyclerView adapter for {@link Note}.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

	public static final class ViewHolder extends RecyclerView.ViewHolder {

		private TextView textView;

		public ViewHolder(View itemView, final ClickListener listener) {
			super(itemView);
			textView = (TextView) itemView.findViewById(R.id.text);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					listener.onClick(view, getAdapterPosition());
				}
			});
		}

		public TextView getTextView() {
			return textView;
		}
	}

	private ClickListener listener;
	private List<Note> notes;

	public NoteAdapter(ClickListener listener, List<Note> notes) {
		this.listener = listener;
		this.notes = notes;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item, parent, false), listener);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.getTextView().setText(notes.get(position).getTitle());
	}

	@Override
	public int getItemCount() {
		return notes.size();
	}

	public void update(List<Note> notes) {
		this.notes.clear();
		this.notes = notes;
		notifyDataSetChanged();
	}
}
