package com.stc.firebase.android.chat.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by artem on 10/26/16.
 */

public interface FcmRetroInterface {
	@Headers({"Content-Type:application/json", "Authorization: key=AIzaSyDKofxbzAOWV3F1Q2V_V1L9eHlPxexkhZs"})
	@POST("fcm/send")
	Call<SendMsgResponce> send(@Body SendMsgRequest request);

}
///curl  -X POST --header "Content-Type:application/json" --header "Authorization: key=AIzaSyDKofxbzAOWV3F1Q2V_V1L9eHlPxexkhZs" -d "{\"to\":\"/topics/friendly_engage\",\"data\": {\"message\": \"Test message\"},\"notification\": {\"body\": \"This is Test message body\",}} " https://fcm.googleapis.com/fcm/send