package com.stc.firebase.android.chat.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.stc.firebase.android.chat.R;
import com.stc.firebase.android.chat.ui.ChatActivity;
import com.stc.firebase.android.chat.ui.MainActivity;

import java.util.Map;

import static com.stc.firebase.android.chat.Constants.INTENT_ACTION_CHAT;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_CHAT;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_ID_MAIN;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_PENDING_INTENT_CHAT;
import static com.stc.firebase.android.chat.Constants.NOTIFICATION_PENDING_INTENT_MAIN;
import static com.stc.firebase.android.chat.Constants.SETTINGS_CHATTING_WITH;
import static com.stc.firebase.android.chat.Constants.SETTINGS_IS_ACTIVE;
import static com.stc.firebase.android.chat.ui.MainActivity.EXTRA_CHAT_WITH;


public class FcmReceiverService extends FirebaseMessagingService {

    private static final String TAG = "FcmReceiverService";
	private SharedPreferences prefs;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());

	    Map<String, String> data = remoteMessage.getData();

	    String chattingWithFromPrefs=prefs.getString(SETTINGS_CHATTING_WITH, null);
	    if(data.containsKey("messageText") && data.containsKey("fromUid") && data.containsKey("fromName")){
		    String stringBody=""+data.get("messageText");
		    String chattingWith=data.get("fromUid");
		    String stringTitle="New message from "+data.get("fromName");
		    Log.w(TAG, "onMessageReceived: stringBody="+ stringBody);
		    Log.w(TAG, "onMessageReceived: chattingWith="+ chattingWith);
		    Log.w(TAG, "onMessageReceived: stringTitle="+ stringTitle);
		    Log.w(TAG, "onMessageReceived: chattingWithFromPrefs="+ chattingWithFromPrefs);
		    Log.w(TAG, "onMessageReceived: SETTINGS_IS_ACTIVE="+ prefs.getBoolean(SETTINGS_IS_ACTIVE, false));
		    if(!prefs.getBoolean(SETTINGS_IS_ACTIVE, false)){
			    if(chattingWithFromPrefs!=null && chattingWith!=null && TextUtils.equals(chattingWith,  chattingWithFromPrefs))
				    Log.w(TAG, "notification not show, dialog is active");
			    else sendNotification(stringTitle, stringBody, chattingWith);
		    }
	    }
	    else {
		    String stringBody = "";
		    for (String s : data.keySet()) stringBody += s + ": " + data.get(s) + "\n";

		    Log.w(TAG, "onMessageReceived: stringBody="+ stringBody);
		    Log.w(TAG, "onMessageReceived: chattingWithFromPrefs="+ chattingWithFromPrefs);
		    Log.w(TAG, "onMessageReceived: SETTINGS_IS_ACTIVE="+ prefs.getBoolean(SETTINGS_IS_ACTIVE, false));
		    Log.w(TAG, "onMessageReceived: NO MORE INFO");
		    sendNotification(stringBody, null ,null);
	    }

    }



	private void sendNotification(String title, String messageBody, String chattingWith) {
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if(title==null)title = "FCM Message";
		PendingIntent pendingIntent=null;
		int id=999;
		if(chattingWith==null){
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_PENDING_INTENT_MAIN, intent,
					PendingIntent.FLAG_ONE_SHOT);
			id=NOTIFICATION_ID_MAIN;
		}else{
			Intent intent = new Intent(this, ChatActivity.class);
			intent.setAction(INTENT_ACTION_CHAT);
			intent.putExtra(EXTRA_CHAT_WITH, chattingWith);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_PENDING_INTENT_CHAT, intent,
					PendingIntent.FLAG_ONE_SHOT);
			id=NOTIFICATION_ID_CHAT;
		}

		notificationManager.cancel(id);
		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_notification_small)
				.setContentTitle(title)
				.setContentText(messageBody)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);


		notificationManager.notify(id, notificationBuilder.build());
	}

}
