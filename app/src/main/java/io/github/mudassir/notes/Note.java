package io.github.mudassir.notes;

import android.text.Spanned;

/**
 *
 */
public class Note {

	public static final class Builder {

		private String date;
		private String title;
		private Spanned body;

		public Builder date(String date) {
			this.date = date;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder body(Spanned body) {
			this.body = body;
			return this;
		}

		public Note build() {
			return new Note(date, title, body);
		}
	}

	private String date;
	private String title;
	private Spanned body;

	private Note(String date, String title, Spanned body) {
		this.date = date;
		this.title = title;
		this.body = body;
	}

	public String getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public Spanned getBody() {
		return body;
	}
}
