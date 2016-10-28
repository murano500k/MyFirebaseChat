package com.stc.firebase.android.chat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by artem on 10/26/16.
 */

public class Notification{
	@SerializedName("body")
	String body;

	public Notification(String body) {
		this.body = body;
	}
}
