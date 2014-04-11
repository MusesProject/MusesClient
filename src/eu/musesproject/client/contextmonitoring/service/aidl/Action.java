package eu.musesproject.client.contextmonitoring.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class Action implements Parcelable {
	private String type;
	private long timestamp;
	
	
	public Action() {
	}
	
	public Action(String type, long timestamp) {
		this.type = type;
		this.timestamp = timestamp;
	}
	
	public Action(Parcel in) {
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		this.type = in.readString();
		this.timestamp = in.readLong();
		
	}

	public static final Creator<Action> CREATOR = new Parcelable.Creator<Action>() {

		@Override
		public Action createFromParcel(Parcel source) {
			return new Action(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(type);
		out.writeLong(timestamp);
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}