package com.stc.firebase.android.chat.model;

/**
 * Created by artem on 10/25/16.
 */

public class User {
	private String name;
	private String email;
	private String photoUrl;
	private String userId;
	private String token;


	public User() {
	}

	public User(String userId, String name, String photoUrl, String email) {
		this.name = name;
		this.photoUrl = photoUrl;
		this.email = email;
		this.userId=userId;








		token=null;
	}

	public String getToken() {
		return token;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getUserId() {
		return userId;
	}
}
