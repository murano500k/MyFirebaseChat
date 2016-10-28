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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stc.firebase.android.chat.R;
import com.stc.firebase.android.chat.model.Message;
import com.stc.firebase.android.chat.model.User;
import com.stc.firebase.android.chat.notification.NotificationSender;

import java.util.ArrayList;

import static com.stc.firebase.android.chat.Constants.FIELD_DB_TIMESTAMP;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_CHAT;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_MAIN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_CHATTING_WITH;
import static com.stc.firebase.android.chat.Constants.SETTINGS_DB_UID;
import static com.stc.firebase.android.chat.Constants.SETTINGS_IS_ACTIVE;
import static com.stc.firebase.android.chat.Constants.TABLE_DB_MESSAGES;
import static com.stc.firebase.android.chat.Constants.TABLE_DB_USERS;
import static junit.framework.Assert.assertNotNull;

public class ChatActivity extends AppCompatActivity implements
		GoogleApiClient.OnConnectionFailedListener {

	private NotificationSender notificationSender;



	private static final String TAG = "ChatActivity";
	private static final String MESSAGE_SENT_EVENT = "message_sent";
	private SharedPreferences prefs;

	private Button mSendButton;
	private RecyclerView mMessageRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;
	private ProgressBar mProgressBar;
	private DatabaseReference mFirebaseDatabaseReference;
	private FirebaseAnalytics mFirebaseAnalytics;
	private EditText mMessageEditText;
	private MessageAdapter mAdapter;
	private String senderId;
	private String recieverId;
	private String messageId;
	User me;
	User opponent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID_MAIN);
		notificationManager.cancel(NOTIFICATION_ID_CHAT);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarChat);
		mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setStackFromEnd(true);
		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
		recieverId=getIntent().getExtras().getString(MainActivity.EXTRA_CHAT_WITH, null);
		if(recieverId==null ){
			Log.e(TAG, "ERROR NULL");
			return;
		}else Log.w(TAG, "SUCCESS "+ recieverId);
		senderId=prefs.getString(SETTINGS_DB_UID, null);
		updateContent();
	}


	private void updateContent() {
		mFirebaseDatabaseReference.child(TABLE_DB_USERS).addChildEventListener(new ChildEventListener() {

			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.w("TAG", "s: "+s);
				User user1 =  dataSnapshot.getValue(User.class);
				if(user1!=null ) {
					if (TextUtils.equals(user1.getUserId(), senderId)) {
						me = user1;
						if(opponent!=null ) initUi(	);
					}
					if (TextUtils.equals(user1.getUserId(), recieverId)){
						opponent = user1;
						if(me!=null ) initUi();
					}
					Log.w("TAG", "user1: " + user1.getUserId());
				}
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ChatActivity.this, "CANCELLED", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onCancelled");
				finish();
			}
		});




	}


	void initUi(){
		senderId=me.getUserId();
		recieverId=opponent.getUserId();
		messageId= Message.getMessageId(senderId,recieverId);
		mAdapter=new MessageAdapter(new ArrayList<Message>(),ChatActivity.this, me, opponent);
		mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
		mMessageRecyclerView.setAdapter(mAdapter);
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		mMessageEditText = (EditText) findViewById(R.id.messageEditText);
		mMessageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if(keyEvent.getAction()==KeyEvent.ACTION_DOWN) {
					onSendButtonClicked();
					return true;
				}
				return false;

			}
		});
		mMessageEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.toString().trim().length() > 0) {
					mSendButton.setEnabled(true);
				} else {
					mSendButton.setEnabled(false);
				}
			}
			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		mSendButton = (Button) findViewById(R.id.sendButton);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSendButtonClicked();
			}
		});

		mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				int friendlyMessageCount = mAdapter.getItemCount();
				int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
				if (lastVisiblePosition == -1 ||
						(positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
					mMessageRecyclerView.scrollToPosition(positionStart);
				}
			}
		});
		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
		Log.w("TAG", "CURRENT MESSAGEID: "+messageId);

		mFirebaseDatabaseReference.child(TABLE_DB_MESSAGES).orderByChild(FIELD_DB_TIMESTAMP).addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Message message = dataSnapshot.getValue(Message.class);
				if(message ==null) {
					Log.e("TAG", "NULL MESSAGE");
					return;
				}
				Log.w("TAG", "MESSAGEID: "+ message.getMessageId());
				Log.w("TAG", "MESSAGETEXT: "+ message.getText());
				if(TextUtils.equals(
						message.getMessageId(),
						messageId
				)) {
					mProgressBar.setVisibility(ProgressBar.INVISIBLE);
					mAdapter.addItem(message);
				}
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			}
			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {
				Message item =  dataSnapshot.getValue(Message.class);
				mAdapter.removeItem(item);

			}
			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {
			}
			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ChatActivity.this, "CANCELLED", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onCancelled");
				finish();
			}
		});

	}
	public void onSendButtonClicked(){
		Message message = new Message(
				messageId,
				senderId,
				recieverId,
				mMessageEditText.getText().toString(),
				System.currentTimeMillis()
		);
		mFirebaseDatabaseReference.child(TABLE_DB_MESSAGES).push().setValue(message);
		if(notificationSender==null) notificationSender=new NotificationSender();
		String recieverToken=opponent.getToken();
		Log.w(TAG, "token of opponent = "+recieverToken);
		assertNotNull(recieverToken);
		notificationSender.sendMessage(recieverToken,mMessageEditText.getText().toString(),  senderId, me.getName() );
		mMessageEditText.setText("");
		mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	@Override
	protected void onResume() {
		super.onResume();
		prefs.edit().putBoolean(SETTINGS_IS_ACTIVE,true).apply();
		prefs.edit().putString(SETTINGS_CHATTING_WITH,recieverId).apply();

	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.edit().putBoolean(SETTINGS_IS_ACTIVE,false).apply();
		prefs.edit().putString(SETTINGS_CHATTING_WITH,null).apply();


	}
}
