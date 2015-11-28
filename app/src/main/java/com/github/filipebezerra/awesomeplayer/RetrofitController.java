package com.github.filipebezerra.awesomeplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import java.io.File;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 24/11/2015
 * @since #
 */
public class RetrofitController {
    private static Retrofit sRetrofit;

    private static StreamService sStreamService;

    private RetrofitController() {
        // no public constructor
    }

    public static StreamService streamService(@NonNull Context context) {
        if (sRetrofit == null) {
            final File cacheFile = new File(context.getApplicationContext().getCacheDir(), "http");
            final Cache cache = new Cache(cacheFile, 10 * 1024 * 1024); //10MB

            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            final OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setCache(cache);
            okHttpClient.interceptors().add(loggingInterceptor);

            sRetrofit = new Retrofit.Builder()
                    .baseUrl(Config.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();

            sStreamService = sRetrofit.create(StreamService.class);
        }
        return sStreamService;
    }
}
