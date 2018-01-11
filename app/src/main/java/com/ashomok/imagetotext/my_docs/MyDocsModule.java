package com.ashomok.imagetotext.my_docs;

/**
 * Created by iuliia on 12/27/17.
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.ashomok.imagetotext.R;
import com.ashomok.imagetotext.di_dagger.ActivityScoped;
import com.ashomok.imagetotext.language_choser_mvp_di.LanguageOcrContract;
import com.ashomok.imagetotext.language_choser_mvp_di.LanguageOcrPresenter;
import com.ashomok.imagetotext.my_docs.get_my_docs_task.MyDocsHttpClient;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link MyDocsPresenter}.
 */
@Module
public abstract class MyDocsModule {
    @ActivityScoped
    @Binds
    abstract MyDocsContract.Presenter myDocsPresenter(MyDocsPresenter presenter);

    @Provides
    static MyDocsHttpClient provideMyDocsHttpClient() {
        return MyDocsHttpClient.getInstance();
    }
}
