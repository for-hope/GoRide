package me.lamine.goride.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAnIf89yM:APA91bEHT5bPAqEFOeg4C03d6DsZs0HaQ0NC7vECz-aa3R6Mlknsfm4EzkPrseO4WL9061LfGh6aVNYobayGrqvO0-cA3UmXc3akABUdCSsGpdGrDvujaTmFG7XM9iifxnmqbV1GjY4A"
    }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
