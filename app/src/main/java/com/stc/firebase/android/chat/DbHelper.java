package com.stc.firebase.android.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by artem on 10/26/16.
 */

public class DbHelper {
	private static FirebaseUser mFirebaseUser;

	public static String getCurrentUId(){
		if(mFirebaseUser!=null) return mFirebaseUser.getUid();
		FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
		mFirebaseUser = mFirebaseAuth.getCurrentUser();
		if (mFirebaseUser == null) {
			return null;
		} else {
			return mFirebaseUser.getUid();
		}
	}

	public static DatabaseReference getUserChats(){
		if(getCurrentUId()!=null){
			return FirebaseDatabase.getInstance().getReference("users").child(getCurrentUId()).child("user_chats");
		}else return null;
	}



}
