package com.bhex.wallet.common.api;

import com.bhex.wallet.common.model.BHRates;
import com.bhex.wallet.common.model.BHToken;
import com.bhex.wallet.common.model.GasFee;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by BHEX.
 * User: gdy
 * Date: 2020/4/9
 * Time: 20:22
 */
public interface BHttpApiInterface {

    @POST("api/v1/txs")
    Observable<JsonObject> sendTransaction(@Body RequestBody body);

    @GET("api/v1/tokens")
    Observable<JsonObject> loadSymbol(@Query("page") int page, @Query("page_size") int pageSize);


    @GET("api/v1/cus/{address}")
    Observable<JsonObject> loadAccount(@Path("address") String address);

    //汇率接口
    @POST("api/v1/tokenprices")
    Observable<List<BHRates>> loadRates(@Body RequestBody body);
    /**
     * 某种类型的交易，传空或0为所有交易。0 - 所有交易，1 - 代币转移，2 - 跨链交易，3 - 委托，4 - 收益分配，5 - 链上治理，6 - 削减
     *
     * @param address
     * @return
     */
    @GET("api/v1/cus/{address}/txs")
    Observable<JsonObject> queryTransctionByAddress(@Path("address") String address,
                                                    @Query("page") int page, @Query("page_size") int pageSize,
                                                    @Query("token") String token, @Query("type") String type);

    //验证人接口
    @GET("api/v1/validators")
    Observable<JsonArray> queryValidators(@Query("valid") int valid, @Query("last_blocks") int last_blocks);

    @GET("api/v1/cus/{addr}/delegations")
    Observable<JsonArray> queryCustDelegations(@Path("addr") String address);

    @GET("api/v1/validators/{op_addr}")
    Observable<JsonObject> queryValidator(@Path("op_addr") String address);


    @GET("api/v1/proposals")
    Observable<JsonObject> queryProposals(@Query("page") int page, @Query("page_size") int pageSize, @Query("title") String title);

    @GET("api/v1/proposals/{id}")
    Observable<JsonObject> queryProposal(@Path("id") String id);

    //查询托管单元下委托的验证人列表
    @GET("api/v1/cus/{addr}/delegations")
    Observable<JsonArray> queryValidatorsByAddress(@Path("addr") String address);

    //查询交易详情
    @GET("api/v1/txs/{hash}")
    Observable<JsonObject> queryTranscationView(@Path("hash") String hash);

    //检查更新
    @GET("api/v1/app_version")
    Observable<JsonObject> getUpgradeInfo(@Query("app_id") String app_id,
                                   @Query("app_version") String app_version,
                                   @Query("device_type") String device_type,
                                   @Query("device_version") String device_version);


    //通知查询
    @GET("/api/v1/cus/{addr}/notifications")
    Observable<JsonObject> queryNotificationByAddress(@Path("addr") String address,
                                                      @Query("page") int page,
                                                      @Query("page_size") int page_size,
                                                      @Query("type") String type);

    //通知查询
    @GET("/api/v1/cus/{addr}/notifications")
    Observable<JsonObject> queryNotificationByAddress(@Path("addr") String address,
                                                      @Query("page") int page,
                                                      @Query("page_size") int page_size);
    //更新通知状态
    @POST("/api/v1/cus/{addr}/notifications/{id}/read")
    Observable<JsonObject> updateNotificationStatus(@Path("addr") String address,
                                                      @Path("id") String id);

    @GET("/api/v1/cus/{addr}/send_test_token")
    Observable<JsonObject> send_test_token(@Path("addr") String address, @Query("denom") String denom);

    //映射币对
    @GET("/api/v1/mappings")
    Observable<JsonObject> loadTokenMappings();

    //映射币对
    @GET("/api/v1/chains")
    Observable<JsonArray> loadChain();

    //默认币对
    @GET("/api/v1/default_tokens")
    Observable<JsonArray> loadDefaultToken();

    //官方认证币对
    @GET("/api/v1/verified_tokens")
    Observable<JsonArray> loadVerifiedToken(@Nullable @Query("chain") String chain);

    //搜索token
    @GET("/api/v1/search_tokens")
    Observable<JsonArray> searchToken(@Nullable @Query("chain") String chain,@Query("token") String token);

    //搜索token
    @GET("/api/v1/tokens/{symbol}")
    Observable<BHToken> queryToken(@Path("symbol") String symbol);

    //获取Gasfee
    @GET("/api/v1/default_fee")
    Observable<GasFee> queryGasfee();

    @GET("/api/v1/announcements")
    Observable<JsonArray>  loadAnnouncement();

    //批量获取币种信息
    @GET("/api/v1/batch_tokens/{symbol}")
    Observable<JsonObject> batchQueryToken(@Path("symbol") String symbol);
}
