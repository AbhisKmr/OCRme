package com.scanlibrary.ocrme.get_more_requests;

/**
 * Created by iuliia on 3/2/18.
 */

import androidx.annotation.StringRes;

import com.scanlibrary.ocrme.billing.model.SkuRowData;
import com.scanlibrary.ocrme.di_dagger.BasePresenter;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public class GetMoreRequestsContract {
    interface View {

        void showError(@StringRes int errorMessageRes);

        void showInfo(@StringRes int infoMessageRes);

        void showInfo(String message);

        void updateToolbarText();

        void updatePaidOption(List<SkuRowData> dataList);
    }

    interface Presenter extends BasePresenter<GetMoreRequestsContract.View> {
    }
}
