package com.mavimdev.fitnessh.network;

import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.FitClient;
import com.mavimdev.fitnessh.model.FitClube;
import com.mavimdev.fitnessh.model.FitStatus;

import java.util.ArrayList;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by migue on 18/02/2018.
 */

public interface FitnessDataService {
    @FormUrlEncoded
    @POST("aulas-today-json.php")
    Observable<ArrayList<FitClass>> getTodayClasses(@Field("id") String clubId,
                                                    @Field("pack") String pack);

    @FormUrlEncoded
    @POST("aulas-tomorrow-json.php")
    Observable<ArrayList<FitClass>> getTomorrowClasses(@Field("id") String clubId,
                                                 @Field("pack") String pack);

    @FormUrlEncoded
    @POST("aulas-reservadas-json.php")
    Observable<ArrayList<FitClass>> getReservedClasses(@Field("id") String userId);

    @FormUrlEncoded
    @POST("aulas-marcacao-json.php")
    Observable<ArrayList<FitStatus>> bookClass(@Field("id") String userId,
                                               @Field("aid") String classId,
                                               @Field("password") String password);

    @FormUrlEncoded
    @POST("aulas-cancelar-json.php")
    Observable<ArrayList<FitStatus>> unbookClass(@Field("aid") String classId);

    @GET("club-list-json.php")
    Observable<ArrayList<FitClube>> getClubList();

    @FormUrlEncoded
    @POST("login-json.php")
    Observable<ArrayList<FitClient>> doLogin(@Field("email") String email,
                                             @Field("password") String password);

    @FormUrlEncoded
    @POST("user-check-json.php")
    Observable<ArrayList<FitStatus>> checkUser(@Field("id") String clientId);

    @FormUrlEncoded
    @POST("set-favclub-json.php")
    Observable<ArrayList<FitClient>> setFavoriteClub(@Field("id") String clientId,
                                                     @Field("cid") String clubId);
}
