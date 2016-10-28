/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stc.firebase.android.chat.notification;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import static com.stc.firebase.android.chat.Constants.FIELD_DB_TOKEN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_DB_TOKEN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_DB_UID;
import static com.stc.firebase.android.chat.Constants.TABLE_DB_USERS;

public class FcmTokenRefresher extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private static final String FRIENDLY_ENGAGE_TOPIC = "friendly_engage";
	private SharedPreferences mSharedPreferences;



    /**
     * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
     */
    @Override
    public void onTokenRefresh() {
	    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // If you need to handle the generation of a token, initially or after a refresh this is
        // where you should do that.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FCM Token: " + token);
	    mSharedPreferences.edit().putString(SETTINGS_DB_TOKEN, token).apply();
	    String uId =mSharedPreferences.getString(SETTINGS_DB_UID,null);
	    if(uId!=null){
		    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
		    databaseReference.child(TABLE_DB_USERS).child(uId).child(FIELD_DB_TOKEN).setValue(token);
		    Log.d(TAG, "FCM Token saved: "+token );

	    }else {
		    Log.e(TAG, "FCM Token not saved:" +token);
	    }

	    FirebaseMessaging.getInstance().subscribeToTopic(FRIENDLY_ENGAGE_TOPIC);

    }
}
