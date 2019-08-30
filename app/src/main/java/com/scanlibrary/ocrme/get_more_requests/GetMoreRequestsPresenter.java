package com.scanlibrary.ocrme.get_more_requests;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import com.scanlibrary.ocrme.R;
import com.scanlibrary.ocrme.billing.BillingProviderCallback;
import com.scanlibrary.ocrme.billing.BillingProviderImpl;
import com.scanlibrary.ocrme.utils.NetworkUtils;

import javax.inject.Inject;

import static com.scanlibrary.ocrme.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 3/2/18.
 */

public class GetMoreRequestsPresenter implements GetMoreRequestsContract.Presenter {

    public static final String TAG = DEV_TAG + GetMoreRequestsPresenter.class.getSimpleName();
    @Nullable
    private GetMoreRequestsContract.View view;

    private BillingProviderImpl billingProvider;
    private Context context;
    private BillingProviderCallback billingProviderCallback = new BillingProviderCallback() {
        @Override
        public void onPurchasesUpdated() {
            if (view != null) {
                view.updateToolbarText();
            }
        }

        @Override
        public void showError(int stringResId) {
            if (view != null) {
                view.showError(stringResId);
            }
        }

        @Override
        public void showInfo(String message) {
            if (view != null) {
                view.showInfo(message);
            }
        }

        @Override
        public void onSkuRowDataUpdated() {
            Log.d(TAG, "onSkuRowDataUpdated called");
            view.updatePaidOption(billingProvider.getSkuRowDataListForInAppPurchases());
        }
    };

    @Inject
    GetMoreRequestsPresenter(BillingProviderImpl billingProvider, Context context) {
        this.billingProvider = billingProvider;
        this.context = context;
    }

    @Override
    public void takeView(GetMoreRequestsContract.View getMoreRequestsActivity) {
        view = getMoreRequestsActivity;
        init();
    }

    private void init() {
        billingProvider.setCallback(billingProviderCallback);
        Log.d(TAG, "billing privider's init called");

        if (view != null) {
            checkConnection();
        }
    }

    @Override
    public void dropView() {
        view = null;
        billingProvider.destroy();
    }

    private boolean isOnline() {
        return NetworkUtils.isOnline(context);
    }

    private void checkConnection() {
        if (view != null) {
            if (!isOnline()) {
                view.showError(R.string.no_internet_connection);
            }
        }
    }
}
