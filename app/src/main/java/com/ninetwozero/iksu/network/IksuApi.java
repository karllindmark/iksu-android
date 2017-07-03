package com.ninetwozero.iksu.network;


import com.ninetwozero.iksu.models.Workout;
import com.ninetwozero.iksu.models.WorkoutReservation;
import com.ninetwozero.iksu.models.WorkoutType;
import com.ninetwozero.iksu.network.dto.BookingResponse;
import com.ninetwozero.iksu.network.dto.CancelReservationResponse;
import com.ninetwozero.iksu.network.dto.LoginResponse;
import com.ninetwozero.iksu.network.dto.TokenResponse;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IksuApi {
    String HOST = "https://intapi.iksu.se/";
    String API_PATH = "booking/v1";

    String TOKEN_URL = "token";
    String WORKOUTS_URL = API_PATH + "/classinformation/classes";
    String WORKOUT_URL = API_PATH + "/classinformation/classes/{id}";
    String WORKOUT_TYPES_URL = API_PATH + "/classinformation/objectClasses?objectType=G&language=en_US";

    String LOGIN_URL = API_PATH + "/customer/login";
    String USER_WORKOUTS_URL = API_PATH + "/customer/classes";
    String USER_RESERVATIONS_URL = API_PATH + "/customer/reservations";
    String USER_CHECKIN_URL = USER_RESERVATIONS_URL + "/{id}/checkin";

    @FormUrlEncoded
    @POST(TOKEN_URL)
    Call<TokenResponse> getAccessToken(@Field("username") String apiUsername, @Field("password") String apiPassword, @Field("grant_type") String grantType);

    @GET(WORKOUTS_URL)
    Call<List<Workout>> getWorkoutsBetweenDates(@Query("firstDate") String firstDate, @Query("lastDate") String lastDate);

    @GET(WORKOUT_URL)
    Call<Workout> getWorkoutById(@Path("id") String workoutId);

    @GET(WORKOUT_TYPES_URL)
    Call<List<WorkoutType>> getWorkoutTypes();

    @GET(LOGIN_URL)
    Call<LoginResponse> login(@Query("username") String username, @Query("password") String password);

    @GET(USER_WORKOUTS_URL)
    Call<List<Workout>> getUserWorkoutsBetweenDates(@Query("sessionId") String sessionId, @Query("firstDate") String firstDate, @Query("lastDate") String lastDate);

    @GET(USER_RESERVATIONS_URL)
    Call<List<WorkoutReservation>> getUserReservations(@Query("sessionId") String sessionId);

    @POST(USER_RESERVATIONS_URL)
    Call<BookingResponse> createReservation(@Body RequestBody body);

    @POST(USER_CHECKIN_URL)
    Call<BookingResponse> checkin(@Path("id") long reservationId, @Body RequestBody body);

    @DELETE(USER_RESERVATIONS_URL)
    Call<CancelReservationResponse> cancelReservation(@Query("sessionId") String sessionId, @Query("classId") String workoutId, @Query("reservationId") long reservationId, @Query("locationId") String facilityId);
}
