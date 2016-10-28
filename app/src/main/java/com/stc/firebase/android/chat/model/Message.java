package com.stc.firebase.android.chat.model;

import android.util.Log;

/**
 * Created by artem on 10/27/16.
 */

public class Message {
	private String recieverId;
	private String senderId;
	private String text;
	private long timestamp;
	private String messageId;


	public String getRecieverId() {
		return recieverId;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getText() {
		return text;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Message(String messageId, String senderId, String recieverId, String text, long timestamp) {
		this.recieverId = recieverId;
		this.senderId = senderId;
		this.text = text;
		this.timestamp = timestamp;
		this.messageId=messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public Message() {

	}

	public static String getMessageId(String id1, String id2){
		String messageId;

		if(id1.compareTo(id2) < 0){
			messageId = id1 + "-" + id2;
		}else if(id1.compareTo(id2) > 0) {
			messageId = id2 + "-" + id1;
		}else{
			messageId = id1;
		}
		Log.w("TAG", "getMessageId="+ messageId);
		return messageId;
	}
}
