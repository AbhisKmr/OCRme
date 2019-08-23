package com.ashomok.ocrme.update_to_premium;

/**
 * Created by iuliia on 2/2/18.
 */

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.ashomok.ocrme.R;

import java.util.ArrayList;
import java.util.List;

import static com.ashomok.ocrme.utils.LogUtil.DEV_TAG;

public class FeaturesList {
    public static final String TAG = DEV_TAG + FeaturesList.class.getSimpleName();

    public FeaturesList() {
    }

    public static List<FeatureModel> getList() {
        List<FeatureModel> result = new ArrayList<>();
        result.add(new FeatureModel(R.drawable.no_ads, R.string.no_ads));
        result.add(new FeatureModel(R.drawable.unlimited_recognition_requests, R.string.unlimited_ocr_requests));
        result.add(new FeatureModel(R.drawable.unlimited_translate_requests, R.string.unlimited_translate_requests));
        result.add(new FeatureModel(R.drawable.recognition_languages, R.string.languages_supported_for_ocr));
        result.add(new FeatureModel(R.drawable.translation_languages, R.string.languages_supported_for_translate));
        result.add(new FeatureModel(R.drawable.high_precision, R.string.highprecision_recognition_system));
        return result;
    }

    public static class FeatureModel {
        @DrawableRes
        private int drawableId;
        @StringRes
        private int stringId;

        public FeatureModel(@DrawableRes int drawableId, @StringRes int stringId) {
            this.drawableId = drawableId;
            this.stringId = stringId;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public int getStringId() {
            return stringId;
        }
    }
}
