package com.scanlibrary.ocrme.main;

/**
 * Created by iuliia on 2/14/18.
 */

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.annimon.stream.Optional;
import com.scanlibrary.ocrme.di_dagger.BasePresenter;
import com.tbruyelle.rxpermissions2.Permission;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public class MainContract {
    interface View {

        void showError(@StringRes int errorMessageRes);

        void showWarning(@StringRes int message);

        void showInfo(@StringRes int infoMessageRes);

        void showInfo(String message);

        void updateLanguageString(String languageString);

        void updateView(boolean isPremium);

        void initRequestsCounter(int requestCount);

        void startCamera();

        void showRequestsCounterDialog(int requestsCount);

        void startGalleryChooser();

        void showAds();
    }

    interface Presenter extends BasePresenter<MainContract.View> {
        void onCheckedLanguageCodesObtained(@Nullable List<String> checkedLanguageCodes);

        void showAdsIfNeeded();

        Optional<List<String>> getLanguageCodes();

        boolean isRequestsAvailable();

        int getRequestsCount();

        void consumeRequest();

        void onPhotoBtnClicked(Permission permission);

        void onGalleryChooserClicked();

        String getUserEmail();
    }
}
