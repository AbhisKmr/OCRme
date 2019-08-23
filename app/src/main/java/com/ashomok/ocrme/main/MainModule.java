package com.ashomok.ocrme.main;

/**
 * Created by iuliia on 2/14/18.
 */

import android.app.Activity;
import androidx.annotation.StringRes;

import com.ashomok.ocrme.BuildConfig;
import com.ashomok.ocrme.R;
import com.ashomok.ocrme.my_docs.MyDocsPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link MyDocsPresenter}.
 */
@Module
public abstract class MainModule {
    @Provides
    static Activity provideActivity(MainActivity activity) {
        return activity;
    }

    @Provides
    static @StringRes
    int provideAdBannerId() {
        if (BuildConfig.DEBUG) {
            return R.string.test_banner;
        } else {
            return R.string.main_activity_banner;
        }
    }

    @Binds
    abstract MainContract.Presenter mainPresenter(MainPresenter presenter);
}
