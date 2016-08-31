package io.github.mudassir.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

/**
 * @see <a href="http://www.parcelabler.com">http://www.parcelabler.com</a>
 */
public class Note implements Parcelable {

	public static final class Builder {

		private String date;
		private String title;
		private String body;

		public Builder date(String date) {
			this.date = date;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Note build() {
			return new Note(date, title, body);
		}
	}

	public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
		@Override
		public Note createFromParcel(Parcel in) {
			return new Note(in);
		}

		@Override
		public Note[] newArray(int size) {
			return new Note[size];
		}
	};

	private String date;
	private String title;
	private String body;

	private Note(String date, String title, String body) {
		this.date = date;
		this.title = title;
		this.body = body;
	}

	private Note(Parcel in) {
		date = in.readString();
		title = in.readString();
		body = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(date);
		dest.writeString(title);
		dest.writeString(body);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}
}
