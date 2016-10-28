package com.stc.firebase.android.chat.ui;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stc.firebase.android.chat.R;
import com.stc.firebase.android.chat.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by artem on 10/28/16.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
	private final MainActivity activity;
	ArrayList<User> list;
	@Override
	public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_user, parent, false);
		return new UserViewHolder(view);
	}

	@Override
	public void onBindViewHolder(UserViewHolder holder, int position) {
		final User user = list.get(position);

		holder.userEmail.setText(user.getEmail());
		holder.userName.setText(user.getName());
		if (user.getPhotoUrl() == null) {
			holder.userPhoto.setImageDrawable(ContextCompat.getDrawable(activity,
					R.drawable.ic_account_circle_black_36dp));
		} else {
			Glide.with(activity)
					.load(user.getPhotoUrl())
					.into(holder.userPhoto);
		}
		holder.root.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(activity!=null)	activity.startConversation(user.getUserId());
			}
		});

	}

	@Override
	public int getItemCount() {
		return list.size();
	}
	public void addItem(User user){
		list.add(user);
		notifyItemInserted(list.indexOf(user));
	}

	public UserAdapter(ArrayList<User> list, MainActivity activity) {
		this.activity=activity;
		this.list = list;
	}

	public void removeItem(User user) {
		list.remove(user);
		notifyItemInserted(list.indexOf(user));
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
}
