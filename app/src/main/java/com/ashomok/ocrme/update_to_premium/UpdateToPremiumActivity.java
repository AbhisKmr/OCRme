package com.ashomok.ocrme.update_to_premium;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ashomok.ocrme.R;
import com.ashomok.ocrme.billing.model.SkuRowData;
import com.ashomok.ocrme.utils.InfoSnackbarUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import com.ashomok.ocrme.utils.LogHelper;

/**
 * Created by iuliia on 1/29/18.
 */

//todo create absctract class for whole app BaseActivity
public class UpdateToPremiumActivity extends RxAppCompatActivity
        implements UpdateToPremiumContract.View {

    private static final String TAG = LogHelper.makeLogTag(UpdateToPremiumActivity.class);
    @Inject
    UpdateToPremiumPresenter mPresenter;

    @Inject
    FeaturesListAdapter featuresListAdapter;

    private View mRootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this); //todo or extends daggerappcompat activity instaead
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_to_premium);
        mRootView = findViewById(android.R.id.content);
        if (mRootView == null) {
            mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        }
        initToolbar();
        initFeaturesList();
        mPresenter.takeView(this);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void initPremiumYearRow(SkuRowData item) {
        View oneYearLayout = findViewById(R.id.one_year_subscription);
        TextView oneYearPrice = findViewById(R.id.one_year_price);
        oneYearPrice.setText(item.getPrice());
        TextView pricePerMonth = findViewById(R.id.price_per_month);

        String subTitle = getString(R.string.price_per_month,
                item.getPriceCurrencyCode(),
                String.format("%.2f", (double) item.getPriceAmountMicros() / 12000000));
        pricePerMonth.setText(subTitle);
        oneYearLayout.setOnClickListener(view -> mPresenter.onOneYearClicked(item));
    }

    @Override
    public void initPremiumMonthRow(SkuRowData item) {
        View oneMonthLayout = findViewById(R.id.one_month_subscription);
        TextView oneMonthPrice = findViewById(R.id.one_month_price);
        oneMonthPrice.setText(item.getPrice());
        oneMonthLayout.setOnClickListener(view -> mPresenter.onOneMonthClicked(item));
    }

    @Override
    public void updateView(boolean isPremium) {
        View truePremium = findViewById(R.id.premium_layout);
        View falsePremium = findViewById(R.id.propose_premium_layout);

        truePremium.setVisibility(isPremium ? View.VISIBLE : View.GONE);
        //truePremium.setVisibility(View.VISIBLE);
        falsePremium.setVisibility(isPremium ? View.GONE : View.VISIBLE);
    }

    private void initFeaturesList() {
        RecyclerView recyclerView = findViewById(R.id.premium_features_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(featuresListAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Show CollapsingToolbarLayout Title ONLY when collapsed
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getResources().getString(R.string.update_to_premium));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }

    @Override
    public void showError(int errorMessageRes) {
        InfoSnackbarUtil.showError(errorMessageRes, mRootView);
    }

    @Override
    public void showInfo(int infoMessageRes) {
        InfoSnackbarUtil.showInfo(infoMessageRes, mRootView);
    }

    @Override
    public void showInfo(String message) {
        InfoSnackbarUtil.showInfo(message, mRootView);
    }
}

