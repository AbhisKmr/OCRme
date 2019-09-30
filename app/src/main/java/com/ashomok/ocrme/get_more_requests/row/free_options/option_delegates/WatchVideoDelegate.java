package com.ashomok.ocrme.get_more_requests.row.free_options.option_delegates;

import com.ashomok.ocrme.OcrRequestsCounter;
import com.ashomok.ocrme.R;
import com.ashomok.ocrme.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.ocrme.get_more_requests.row.free_options.UiFreeOptionManagingDelegate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import javax.inject.Inject;

import com.ashomok.ocrme.utils.LogHelper;
import static com.google.android.gms.ads.AdRequest.ERROR_CODE_NO_FILL;

/**
 * Created by iuliia on 3/5/18.
 */

//todo Forward lifecycle events https://developers.google.com/admob/android/rewarded-video -
// impossible now due to no delegate from activity - minor bug
public class WatchVideoDelegate extends UiFreeOptionManagingDelegate implements RewardedVideoAdListener {
    public static final String TAG = LogHelper.makeLogTag(WatchVideoDelegate.class);
    public static final String ID = "watch_video";
    private final GetMoreRequestsActivity activity;
    private final OcrRequestsCounter ocrRequestsCounter;
    private RewardedVideoAd mRewardedVideoAd;

    @Inject
    public WatchVideoDelegate(GetMoreRequestsActivity activity, OcrRequestsCounter ocrRequestsCounter) {
        super(activity);
        this.activity = activity;
        this.ocrRequestsCounter = ocrRequestsCounter;
        String appId = activity.getResources().getString(R.string.app_id);
        MobileAds.initialize(activity, appId);

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()) {
            String rewardedVideoAdAppId =
                    activity.getResources().getString(R.string.rewarded_video_ad_id);
            mRewardedVideoAd.loadAd(rewardedVideoAdAppId, new AdRequest.Builder().build());
        }
    }

    @Override
    protected void startTask() {
        showRewardedVideo();
    }

    private void showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        // Preload the next video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        addRequests(rewardItem.getAmount());
    }

    private void addRequests(int amount) {
        onTaskDone(ocrRequestsCounter, activity);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        if (i == ERROR_CODE_NO_FILL) {
            activity.showError(R.string.no_video_ads_avalible);
        } else {
            activity.showError(R.string.failed_to_load_video_ad);
        }
    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
