package com.stc.firebase.android.chat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by artem on 10/26/16.
 */

public class SendMsgResponce {
	@SerializedName("message_id")
	public String message_id;

	public String getMessage_id() {
		return message_id;
	}

	public void setMessage_id(String message_id) {
		this.message_id = message_id;
	}

	public SendMsgResponce() {

	}

	public SendMsgResponce(String message_id) {
		this.message_id = message_id;
	}
}
