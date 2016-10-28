/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stc.firebase.android.chat.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stc.firebase.android.chat.R;
import com.stc.firebase.android.chat.model.User;

import java.util.ArrayList;

import static com.stc.firebase.android.chat.Constants.FIELD_DB_TOKEN;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_CHAT;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_MAIN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_DB_TOKEN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_DB_UID;
import static com.stc.firebase.android.chat.Constants.TABLE_DB_USERS;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_INVITE = 1;
    public static final String ANONYMOUS = "anonymous";
    private SharedPreferences prefs;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;
	private RecyclerView mUsersRecyclerView;

	public static final String EXTRA_CHAT_WITH= "EXTRA_CHAT_WITH";

	private UserAdapter mAdapter;
	private String currentUserId;
	private ChildEventListener userListListener;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID_MAIN);
		notificationManager.cancel(NOTIFICATION_ID_CHAT);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
		currentUserId = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(SETTINGS_DB_UID, null);

		if( currentUserId!=null){
		    if(prefs.getString(SETTINGS_DB_TOKEN, null)!=null){
			    Log.w("SAVE SUCCESS", "SAVE SUCCESS");
			    mFirebaseDatabaseReference
					    .child(TABLE_DB_USERS)
					    .child(currentUserId)
					    .child(FIELD_DB_TOKEN)
					    .setValue(prefs.getString(SETTINGS_DB_TOKEN, null));
		    }else Log.e("SAVE ERROR", " token null");
	    }else Log.e("SAVE ERROR", "uid null");
		mUsersRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mAdapter = new UserAdapter(new ArrayList<User>(), MainActivity.this);
		mUsersRecyclerView.setLayoutManager(mLinearLayoutManager);
		mUsersRecyclerView.setAdapter(mAdapter);
		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
	    mFirebaseDatabaseReference.child(TABLE_DB_USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot) {
			    User user = dataSnapshot.getValue(User.class);
			    if(user!=null && user.getName()!=null)
				    setTitle(user.getName());
		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError) {

		    }
	    });
	    mFirebaseDatabaseReference.child(TABLE_DB_USERS).addChildEventListener(new ChildEventListener() {
		    @Override
		    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			    User user = dataSnapshot.getValue(User.class);
			    if(user==null) Log.e("TAG", "NULL USER");
			    Log.w("TAG", "USER: "+user.getEmail());
			    if(!TextUtils.equals(
					    user.getUserId(),
					    currentUserId
			    )) {
				    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
				    mAdapter.addItem(user);
			    }
		    }
		    @Override
		    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

		    }
		    @Override
		    public void onChildRemoved(DataSnapshot dataSnapshot) {
			    User user = dataSnapshot.getValue(User.class);
			    mAdapter.removeItem(user);
		    }

		    @Override
		    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError) {
			    Toast.makeText(MainActivity.this, "CANCELLED", Toast.LENGTH_SHORT).show();
			    Log.e(TAG, "onCancelled");
			    finish();
		    }
	    });
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
	    if(mFirebaseDatabaseReference!=null ) mFirebaseDatabaseReference.onDisconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
	            prefs.edit().putString(SETTINGS_DB_TOKEN, null).apply();
	            prefs.edit().putString(SETTINGS_DB_UID, null).apply();
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                Log.e(TAG, "Failed to send invitation.");
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

	public void startConversation(final String userId) {
		Log.w(TAG, "userid= "+userId);
		mFirebaseDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				Intent intent =new Intent(MainActivity.this,ChatActivity.class).putExtra(EXTRA_CHAT_WITH, userId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(MainActivity.this, "CANCELLED", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onCancelled");
				finish();
			}
		});
	}
}
