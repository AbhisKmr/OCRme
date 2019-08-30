package com.scanlibrary.ocrme.ocr_result.translate;

import com.scanlibrary.ocrme.ocr_result.translate.translate_task.translate_task.TranslateHttpClient;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class TranslateModule {


    @Provides
    static TranslateHttpClient provideTranslateHttpClient() {
        return TranslateHttpClient.getInstance();
    }

//    @Provides
//    static @StringRes
//    int provideAdBannerId() {
//        if (BuildConfig.DEBUG) {
//            return R.string.test_banner;
//        } else {
//            return R.string.my_docs_banner;
//        }
//    }

    @Provides
    static String provideSourceText(
            TranslateActivity activity) {
        return activity.getSourceText();
    }

    @Binds
    abstract TranslateContract.Presenter translatePresenter(TranslatePresenter presenter);
}