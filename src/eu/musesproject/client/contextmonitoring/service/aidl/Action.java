package eu.musesproject.client.contextmonitoring.service.aidl;

/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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