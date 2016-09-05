package io.gitlab.mudassir.notes.structs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @see <a href="http://www.parcelabler.com">http://www.parcelabler.com</a>
 */
public class Note implements Parcelable {

	public static final class Builder {

		private Date date;
		private String identifier;
		private String title;
		private String body;

		public Builder date(Date date) {
			this.date = date;
			return this;
		}

		public Builder identifier(String identifier) {
			this.identifier = identifier;
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
			return new Note(date, identifier, title, body);
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

	private Date date;
	private String identifier;
	private String title;
	private String body;

	private Note(Date date, String identifier, String title, String body) {
		this.date = date;
		this.identifier = identifier;
		this.title = title;
		this.body = body;
	}

	private Note(Parcel in) {
		date = new Date(in.readLong());
		identifier = in.readString();
		title = in.readString();
		body = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(date.getTime());
		dest.writeString(identifier);
		dest.writeString(title);
		dest.writeString(body);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
