package com.stc.firebase.android.chat.ui;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stc.firebase.android.chat.R;
import com.stc.firebase.android.chat.model.Message;
import com.stc.firebase.android.chat.model.User;

import java.sql.Date;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ChatMessageViewHolder> {
	private final ChatActivity activity;
	private ArrayList<Message> list;
	private User me;
	private User opponent;
	@Override
	public MessageAdapter.ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(
				R.layout.item_message, parent, false);
		return new MessageAdapter.ChatMessageViewHolder(view);
	}

	 void removeItem(Message item) {
		list.remove(item);
		notifyItemInserted(list.indexOf(item));
	}


	@Override
	public void onBindViewHolder(MessageAdapter.ChatMessageViewHolder viewHolder, int position) {
		final Message message = list.get(position);
		viewHolder.messageTextView.setText(message.getText());
		viewHolder.timestmpTextView.setText(getFormattedDateTime(message.getTimestamp()));

		if(TextUtils.equals(message.getSenderId(), me.getUserId())){
			initUi(viewHolder, me);
		}else if(TextUtils.equals(message.getSenderId(), opponent.getUserId())) {
			initUi(viewHolder, opponent);
		}
	}

	private void initUi(ChatMessageViewHolder viewHolder, User user){
		viewHolder.messengerTextView.setText(user.getName());
		if (user.getPhotoUrl() == null) {
			viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(activity,
					R.drawable.ic_account_circle_black_36dp));
		} else {
			Glide.with(activity)
					.load(user.getPhotoUrl())
					.into(viewHolder.messengerImageView);
		}
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	void addItem(Message message){
		list.add(message);
		notifyItemInserted(list.indexOf(message));
	}

	MessageAdapter(ArrayList<Message> list, ChatActivity activity, User me, User opponent) {
		this.activity=activity;
		this.list = list;
		this.me=me;
		this.opponent=opponent;
	}


	static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
		TextView messageTextView;
		TextView timestmpTextView;
		TextView messengerTextView;
		CircleImageView messengerImageView;


		ChatMessageViewHolder(View v) {
			super(v);
			messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
			timestmpTextView = (TextView) itemView.findViewById(R.id.timestamp);
			messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
			messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
		}
	}
	private String getFormattedDateTime(long timestamp){
		Date date = new Date(timestamp);
		return DateFormat.getDateFormat(activity).format(date);
	}

}
