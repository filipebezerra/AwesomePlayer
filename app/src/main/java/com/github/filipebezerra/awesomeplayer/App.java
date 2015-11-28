package com.github.filipebezerra.awesomeplayer;

import android.app.Application;
import com.squareup.picasso.Picasso;
import timber.log.Timber;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 28/11/2015
 * @since #
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setUpTimber();
        setUpPicasso();
    }

    private void setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //TODO: production logging
        }
    }

    private void setUpPicasso() {
        final Picasso picasso = new Picasso.Builder(this)
                .indicatorsEnabled(BuildConfig.DEBUG)
                .loggingEnabled(BuildConfig.DEBUG)
                .build();

        Picasso.setSingletonInstance(picasso);
    }
}
