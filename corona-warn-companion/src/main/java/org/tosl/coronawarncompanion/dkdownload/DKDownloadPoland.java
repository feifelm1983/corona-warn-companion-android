package org.tosl.coronawarncompanion.dkdownload;

import android.content.Context;
import android.util.Log;

import org.tosl.coronawarncompanion.R;
import java.util.Date;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class DKDownloadPoland implements DKDownloadCountry {
    private static final String TAG = "DKDownloadPoland";

    private static final String DK_URL = "https://exp.safesafe.app/";

    interface Api {
        @GET
        Maybe<String> getIndex(@Url String uri);

        @GET
        Maybe<ResponseBody> getFile(@Url String uri);
    }


    @Override
    public Observable<byte[]> getDKBytes(Context context, OkHttpClient okHttpClient, Date minDate, int maxNumDownloadDays) {

        // TODO: maxNumDownloadDays (and minDate) not yet used.

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DK_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);

        return DKDownloadUtils.wrapRetrofit(context, api.getIndex(DK_URL + "/index.txt"))
                .doOnSuccess(indexString -> Log.d(TAG, "Downloaded index"))
                .flatMapObservable(indexString -> Observable.fromArray(indexString.split("\n")))
                .flatMapMaybe(availableFile -> DKDownloadUtils.wrapRetrofit(context, api.getFile(DK_URL + availableFile))
                        .doOnSuccess(responseBody -> Log.d(TAG, "Downloaded file: " + availableFile)))
                .map(ResponseBody::bytes);

    }

    @Override
    public String getCountryCode(Context context) {
        return context.getResources().getString(R.string.country_code_poland);
    }
}
