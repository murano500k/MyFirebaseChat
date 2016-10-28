package com.stc.firebase.android.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stc.firebase.android.chat.model.User;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.stc.firebase.android.chat.MainActivity.USERS_CHILD;

public class ScrollingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
	private String mUsername;
	private String mPhotoUrl;
	private SharedPreferences mSharedPreferences;
	public static final String EXTRA_CHAT_WITH= "EXTRA_CHAT_WITH";
	private Button mSendButton;
	private RecyclerView mUsersRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;
	private FirebaseRecyclerAdapter<User, ScrollingActivity.UserViewHolder> mFirebaseAdapter;
	private ProgressBar mProgressBar;
	private DatabaseReference mFirebaseDatabaseReference;
	private static final String TAG = "ScrollingActivity";


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
	}

	public static class UserViewHolder extends RecyclerView.ViewHolder {
		public TextView userName;
		public CircleImageView userPhoto;
		public TextView userEmail;
		public View root;

		public UserViewHolder(View v) {
			super(v);
			root=v;
			userName = (TextView) v.findViewById(R.id.userName);
			userPhoto = (CircleImageView) v.findViewById(R.id.userImage);
			userEmail = (TextView) v.findViewById(R.id.userEmail);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scrolling);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
				NotificationSender sender=new NotificationSender();
				sender.sendMessage();
				Log.w("SEND","message send");
			}
		});

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mUsersRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setStackFromEnd(true);

		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
		mFirebaseAdapter = new FirebaseRecyclerAdapter<User, ScrollingActivity.UserViewHolder>(
				User.class,
				R.layout.item_user,
				ScrollingActivity.UserViewHolder.class,
				mFirebaseDatabaseReference.child(USERS_CHILD)) {

			@Override
			protected void populateViewHolder(ScrollingActivity.UserViewHolder viewHolder, final User user, int position) {
				mProgressBar.setVisibility(ProgressBar.INVISIBLE);
				viewHolder.userEmail.setText(user.getEmail());
				viewHolder.userName.setText(user.getName());
				if (user.getPhotoUrl() == null) {
					viewHolder.userPhoto.setImageDrawable(ContextCompat.getDrawable(ScrollingActivity.this,
							R.drawable.ic_account_circle_black_36dp));
				} else {
					Glide.with(ScrollingActivity.this)
							.load(user.getPhotoUrl())
							.into(viewHolder.userPhoto);
				}
				viewHolder.root.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startConversation(user.getUserId());
					}
				});
			}
		};

		mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				int usersCount = mFirebaseAdapter.getItemCount();
				int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
				// If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
				// to the bottom of the list to show the newly added message.
				if (lastVisiblePosition == -1 ||
						(positionStart >= (usersCount - 1) && lastVisiblePosition == (positionStart - 1))) {
					mUsersRecyclerView.scrollToPosition(positionStart);
				}
			}
		});

		mUsersRecyclerView.setLayoutManager(mLinearLayoutManager);
		mUsersRecyclerView.setAdapter(mFirebaseAdapter);
	}

	private void startConversation(final String userId) {
		Log.w(TAG, "userid= "+userId);
		final NotificationSender notificationSender=null;
		mFirebaseDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				//NotificationSender notificationSender = new NotificationSender();
				//String token = (String) dataSnapshot.child("token").getValue();
				//Log.w(TAG, "token of opponent = " + token);
				//notificationSender.sendMessage("dYETrivYa4c:APA91bHZ1rQX-gq_v2fVMHxkCElMD4Jx801YSlJE0_UENqAYFGV7yMhAHkcdMwq-LCImYwJd4FaHCWrV8r64FHdLWwprgbgHGOy3VlMzmJbRd6rMBOJjJxq6QNUHU87c1O1wVbvdxcjB");
				Toast.makeText(ScrollingActivity.this, "SENT", Toast.LENGTH_SHORT).show();

				startActivity(new Intent(ScrollingActivity.this,ChatActivity.class).putExtra(EXTRA_CHAT_WITH, userId));
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ScrollingActivity.this, "ERROR", Toast.LENGTH_SHORT).show();

			}
		});
	}

}

