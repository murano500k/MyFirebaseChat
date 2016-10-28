package com.stc.firebase.android.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.stc.firebase.android.chat.model.ChatMessage;
import com.stc.firebase.android.chat.model.User;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.stc.firebase.android.chat.model.Constants.SETTINGS_DB_UID;
import static com.stc.firebase.android.chat.model.Constants.TABLE_DB_MESSAGES;
import static com.stc.firebase.android.chat.model.Constants.TABLE_DB_USERS;
import static junit.framework.Assert.assertNotNull;

public class ChatActivity extends AppCompatActivity implements
		GoogleApiClient.OnConnectionFailedListener {

	private String currentRecieverId;
	private NotificationSender notificationSender;



	private static final String TAG = "ChatActivity";
	public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
	private static final String MESSAGE_SENT_EVENT = "message_sent";
	private String mUsername;
	private String mPhotoUrl;
	private SharedPreferences mSharedPreferences;

	private Button mSendButton;
	private RecyclerView mMessageRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;
	private FirebaseRecyclerAdapter<ChatMessage, ChatActivity.MessageViewHolder> mFirebaseAdapter;
	private ProgressBar mProgressBar;
	private DatabaseReference mFirebaseDatabaseReference;
	private FirebaseAuth mFirebaseAuth;
	private FirebaseUser mFirebaseUser;
	private FirebaseAnalytics mFirebaseAnalytics;
	private EditText mMessageEditText;
	private FirebaseRemoteConfig mFirebaseRemoteConfig;
	private String chattingWithUid;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);



		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setStackFromEnd(true);

		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

		chattingWithUid=getIntent().getExtras().getString(ScrollingActivity.EXTRA_CHAT_WITH, null);
		if(chattingWithUid==null ){
			Log.e(TAG, "ERROR NULL");
			return;
		}else Log.w(TAG, "SUCCESS "+ chattingWithUid);

		updateContent(mSharedPreferences.getString(SETTINGS_DB_UID, null),chattingWithUid);
	}


	private void updateContent(final String senderId, final String recieverId) {
		mFirebaseDatabaseReference.child(TABLE_DB_USERS).addChildEventListener(new ChildEventListener() {
			User me;
			User opponent;
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.w("TAG", "s: "+s);

				User user1 =  dataSnapshot.getValue(User.class);
				if(user1!=null ) {
					if (TextUtils.equals(user1.getUserId(), senderId)) {
						me = user1;
						if(opponent!=null ) initUi(me ,opponent);
					}
					if (TextUtils.equals(user1.getUserId(), recieverId)){
						opponent = user1;
						if(me!=null ) initUi(me ,opponent);
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

			}
		});




	}
	void initUi(final User me , final User opponent){
		final String senderId=me.getUserId();
		final String recieverId=opponent.getUserId();

		mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, ChatActivity.MessageViewHolder>(
				ChatMessage.class,
				R.layout.item_message,
				ChatActivity.MessageViewHolder.class,
				mFirebaseDatabaseReference.child(TABLE_DB_MESSAGES).equalTo(ChatMessage.getMessageId(senderId,recieverId)).getRef()) {

			@Override
			protected void populateViewHolder(ChatActivity.MessageViewHolder viewHolder, ChatMessage chatMessage, int position) {
				viewHolder.messageTextView.setText(chatMessage.getText());
				if(TextUtils.equals(chatMessage.getSenderId(), me.getUserId())){
					viewHolder.messengerTextView.setText(me.getName());
					if (me.getPhotoUrl() == null) {
						viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
								R.drawable.ic_account_circle_black_36dp));
					} else {
						Glide.with(ChatActivity.this)
								.load(me.getPhotoUrl())
								.into(viewHolder.messengerImageView);
					}
				}else if(TextUtils.equals(chatMessage.getSenderId(), opponent.getUserId())){
					viewHolder.messengerTextView.setText(opponent.getName());
					if (opponent.getPhotoUrl() == null) {
						viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
								R.drawable.ic_account_circle_black_36dp));
					} else {
						Glide.with(ChatActivity.this)
								.load(opponent.getPhotoUrl())
								.into(viewHolder.messengerImageView);
					}
				}

			}
		};

		mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				int friendlyMessageCount = mFirebaseAdapter.getItemCount();
				int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
				// If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
				// to the bottom of the list to show the newly added message.
				if (lastVisiblePosition == -1 ||
						(positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
					mMessageRecyclerView.scrollToPosition(positionStart);
				}
			}
		});

		mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
		mMessageRecyclerView.setAdapter(mFirebaseAdapter);

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


		mMessageEditText = (EditText) findViewById(R.id.messageEditText);
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
				ChatMessage chatMessage = new ChatMessage(
						ChatMessage.getMessageId(senderId,recieverId),
						senderId,
						recieverId,
						mMessageEditText.getText().toString(),
						Calendar.getInstance().getTimeInMillis()
				);

				mFirebaseDatabaseReference.child(TABLE_DB_MESSAGES).push().setValue(chatMessage);
				if(notificationSender==null) notificationSender=new NotificationSender();
				String recieverToken=opponent.getToken();
				Log.w(TAG, "token of opponent = "+recieverToken);
				assertNotNull(recieverToken);
				notificationSender.sendMessage(recieverToken);
				mMessageEditText.setText("");
				mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
			}
		});
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
	public static class MessageViewHolder extends RecyclerView.ViewHolder {
		public TextView messageTextView;
		public TextView messengerTextView;
		public CircleImageView messengerImageView;

		public MessageViewHolder(View v) {
			super(v);
			messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
			messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
			messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
		}
	}

}
