package io.gitlab.mudassir.notes;

import android.view.View;

/**
 * Callback for RecyclerView item click
 */
public interface ClickListener {
	void onClick(View view, int position);
}
