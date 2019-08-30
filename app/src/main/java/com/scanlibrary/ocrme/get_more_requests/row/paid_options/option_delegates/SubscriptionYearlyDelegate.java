package com.scanlibrary.ocrme.get_more_requests.row.paid_options.option_delegates;

import android.content.Context;

import com.scanlibrary.ocrme.R;
import com.scanlibrary.ocrme.billing.BillingProviderImpl;
import com.scanlibrary.ocrme.billing.model.SkuRowData;
import com.scanlibrary.ocrme.get_more_requests.row.paid_options.PaidOptionRowViewHolder;
import com.scanlibrary.ocrme.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.scanlibrary.ocrme.billing.BillingProviderImpl.PREMIUM_MONTHLY_SKU_ID;
import static com.scanlibrary.ocrme.utils.LogUtil.DEV_TAG;

public class SubscriptionYearlyDelegate extends UiPaidOptionManagingDelegate {
    public static final String TAG = DEV_TAG + SubscriptionYearlyDelegate.class.getSimpleName();

    @Inject
    public SubscriptionYearlyDelegate(BillingProviderImpl billingProvider, Context context) {
        super(billingProvider, context);
    }

    @Override
    public void onRowClicked(SkuRowData data) {
        if (data != null) {
            if (getBillingProvider().isPremiumMonthlySubscribed()) {
                // If we already subscribed to monthly premium, launch replace flow
                ArrayList<String> currentSubscriptionSku = new ArrayList<>();
                currentSubscriptionSku.add(PREMIUM_MONTHLY_SKU_ID);
                getBillingProvider().getBillingManager().initiatePurchaseFlow(data.getSku(),
                        currentSubscriptionSku, data.getSkuType());
            } else {
                super.onRowClicked(data);
            }
        }
    }

    @Override
    public void onBindViewHolder(SkuRowData data, PaidOptionRowViewHolder holder) {
        super.onBindViewHolder(data, holder);

        holder.getTitle().setText(getContext().getResources().getString(R.string.one_year_premium));
        holder.getSubtitleTop().setText(getContext().getResources().getString(R.string.unlimited_requests));

        String subTitle = getContext().getResources().getString(R.string.price_per_month,
                data.getPriceCurrencyCode(),
                String.format("%.2f", (double) data.getPriceAmountMicros() / 12000000));

        holder.getSubtitleBottom().setText(subTitle);

    }
}
