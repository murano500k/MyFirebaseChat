package com.stc.firebase.android.chat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by artem on 10/26/16.
 */
public class Data{
	@SerializedName("message")
	String message;

	public Data(String message) {
		this.message = message;
	}
}