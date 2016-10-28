package com.stc.firebase.android.chat;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stc.firebase.android.chat.model.Data;
import com.stc.firebase.android.chat.model.Notification;
import com.stc.firebase.android.chat.model.SendMsgRequest;
import com.stc.firebase.android.chat.model.SendMsgResponce;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by artem on 10/26/16.
 */

public class NotificationSender {

	Gson gson;

	public static final String BASE_URL = "https://fcm.googleapis.com/";
	Retrofit retrofit;
	public void sendMessage(){
		if(gson==null)	gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
				.create();

		if(retrofit==null)	retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build();
		FcmRetroInterface fcmRetroInterface =retrofit.create(FcmRetroInterface.class);
		final SendMsgRequest request = new SendMsgRequest("/topics/friendly_engage", new Data("Test message"),new Notification("Test message notification"));
		Call<SendMsgResponce> call = fcmRetroInterface.send(request);
		call.enqueue(new Callback<SendMsgResponce>() {
			@Override
			public void onResponse(Call<SendMsgResponce> call, Response<SendMsgResponce> response) {
				if(response.isSuccessful()) {
					Log.w("RESULT", ""+response.body().message_id);
				}
				else {
					Log.e("RESULT", ""+response.errorBody().toString());
				}
			}

			@Override
			public void onFailure(Call<SendMsgResponce> call, Throwable t) {
				Log.e("RESULT", ""+t.toString());
			}
		});

	}

	public void sendMessage(String token) {
		if(gson==null)	gson = new GsonBuilder()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
				.create();

		if(retrofit==null)	retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build();
		FcmRetroInterface fcmRetroInterface =retrofit.create(FcmRetroInterface.class);
		Log.e("TAG", "TOKEN TOSEND "+token);
		final SendMsgRequest request = new SendMsgRequest(token, new Data("New message"),new Notification("You have new message"));

		Call<SendMsgResponce> call = fcmRetroInterface.send(request);
		call.enqueue(new Callback<SendMsgResponce>() {
			@Override
			public void onResponse(Call<SendMsgResponce> call, Response<SendMsgResponce> response) {
				if(response.isSuccessful()) {
					Log.w("RESULT","SUCCESS");
				}
				else {
					Log.e("RESULT", "FAIL");
				}
			}

			@Override
			public void onFailure(Call<SendMsgResponce> call, Throwable t) {
				Log.e("RESULT","FAIL");
			}
		});
	}
}
/////curl  -X POST --header "Content-Type:application/json" --header "Authorization: key=AIzaSyDKofxbzAOWV3F1Q2V_V1L9eHlPxexkhZs" -d "{\"to\":\"/topics/friendly_engage\",\"data\": {\"message\": \"Test message\"},\"notification\": {\"body\": \"This is Test message body\",}} " https://fcm.googleapis.com/fcm/send