package com.stc.firebase.android.chat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by artem on 10/26/16.
 */
public class Data{
	@SerializedName("messageText")
	String messageText;

	@SerializedName("fromUid")
	String fromUid;

	@SerializedName("fromName")
	String fromName;


	public Data(String message) {
		this.messageText = message;
	}
	public Data(String message, String fromUid, String fromName) {
		this.messageText = message;
		this.fromUid = fromUid;
		this.fromName = fromName;
	}
}