package com.ashomok.ocrme.ocr;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.ashomok.ocrme.utils.FilesProvider.getGcsImageUri;
import static com.ashomok.ocrme.utils.FilesProvider.getTestImages;
import static com.ashomok.ocrme.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 12/25/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OcrActivityTest {

    private static final String TAG = DEV_TAG + OcrActivityTest.class.getSimpleName();

    @Rule
    public ActivityTestRule<OcrActivity> mActivityRule = new ActivityTestRule<>(
            OcrActivity.class, true, false);

    @Test
    public void testOcrWithUri() throws InterruptedException {
        String path = getTestImages().get(0);
        launchActivityWithPath(Uri.fromFile(new File(path)));
        Thread.sleep(10000);//waiting for ocr finished
    }

    @Test
    public void testOcrWithUrl() throws InterruptedException {
        String url = getGcsImageUri();
        launchActivityWithUrl(url);
        Thread.sleep(10000);//waiting for ocr finished
    }

    private void launchActivityWithPath(Uri uri) {
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent intent = new Intent(targetContext, OcrActivity.class);
        intent.putExtra(OcrActivity.EXTRA_IMAGE_URI, uri);

        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        intent.putExtra(OcrActivity.EXTRA_LANGUAGES, languages);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        mActivityRule.launchActivity(intent);
    }

    private void launchActivityWithUrl(String url) {
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent intent = new Intent(targetContext, OcrActivity.class);
        intent.putExtra(OcrActivity.EXTRA_IMAGE_URL, url);

        ArrayList<String> languages = new ArrayList<>();
        languages.add("ru");
        intent.putExtra(OcrActivity.EXTRA_LANGUAGES, languages);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        mActivityRule.launchActivity(intent);
    }

    public static Matcher<View> hasDrawable() {
        return new DrawableMatcher(DrawableMatcher.ANY);
    }

    public static class DrawableMatcher extends TypeSafeMatcher<View> {

        private final int expectedId;
        String resourceName;
        static final int EMPTY = -1;
        static final int ANY = -2;

        public DrawableMatcher(int expectedId) {
            super(View.class);
            this.expectedId = expectedId;
        }

        @Override
        protected boolean matchesSafely(View target) {
            if (!(target instanceof ImageView)) {
                return false;
            }
            ImageView imageView = (ImageView) target;
            if (expectedId == EMPTY) {
                return imageView.getDrawable() == null;
            }
            if (expectedId == ANY) {
                return imageView.getDrawable() != null;
            }
            Resources resources = target.getContext().getResources();
            Drawable expectedDrawable = resources.getDrawable(expectedId);
            resourceName = resources.getResourceEntryName(expectedId);

            if (expectedDrawable == null) {
                return false;
            }

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Bitmap otherBitmap = ((BitmapDrawable) expectedDrawable).getBitmap();
            return bitmap.sameAs(otherBitmap);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with drawable from resource id: ");
            description.appendValue(expectedId);
            if (resourceName != null) {
                description.appendText("[");
                description.appendText(resourceName);
                description.appendText("]");
            }
        }
    }
}