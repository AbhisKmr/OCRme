package com.ashomok.imagetotext.ocr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.annimon.stream.Optional;
import com.ashomok.imagetotext.R;
import com.ashomok.imagetotext.Settings;
import com.ashomok.imagetotext.ocr.ocr_task.OcrHttpClient;
import com.ashomok.imagetotext.ocr.ocr_task.OcrResponse;
import com.ashomok.imagetotext.ocr_result.OcrResultActivity;
import com.ashomok.imagetotext.ocr_result.tab_fragments.TextFragment;
import com.ashomok.imagetotext.utils.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jakewharton.rxbinding2.view.RxView;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.ashomok.imagetotext.Settings.firebaseFolderURL;
import static com.ashomok.imagetotext.ocr_result.OcrResultActivity.EXTRA_ERROR_MESSAGE;
import static com.ashomok.imagetotext.ocr_result.OcrResultActivity.EXTRA_OCR_RESPONSE;
import static com.ashomok.imagetotext.utils.LogUtil.DEV_TAG;


/**
 * Created by Iuliia on 13.12.2015.
 */

public class OcrActivity extends RxAppCompatActivity {
    public static final int RESULT_CANCELED_BY_USER = 123;
    private Uri imageUri;
    private StorageReference mImageRef;
    private ArrayList<String> sourceLanguageCodes;
    public static final String EXTRA_LANGUAGES = "com.ashomokdev.imagetotext.ocr.LANGUAGES";
    public static final String EXTRA_IMAGE_URI = "com.ashomokdev.imagetotext.ocr.IMAGE_URI";
    public static final String TAG = DEV_TAG + OcrActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ocr_animation_layout);

        imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        sourceLanguageCodes = getIntent().getStringArrayListExtra(EXTRA_LANGUAGES);

        initCancelBtn();
        initImage().subscribe(() -> initAnimatedScanBand(Settings.isTestMode));

        OcrHttpClient httpClient = OcrHttpClient.getInstance();
        callOcr(httpClient);
    }

    public void callOcr(OcrHttpClient httpClient) {
        if (isOnline()) {
            if (imageUri != null) {

                //get user IdToken and upload photo in parallel:
                Single<Pair<Optional<String>, String>> zipped =
                        Single.zip(
                               getIdToken().doOnEvent((a,b) -> Log.d(TAG,
                                       "is main thread: "
                                       + (Thread.currentThread() == Looper.getMainLooper().getThread()))),
                               uploadPhoto(imageUri).doOnEvent((a,b) -> Log.d(TAG,
                                       "is main thread: "
                                               + (Thread.currentThread() == Looper.getMainLooper().getThread()))),
                                (a, b) -> new Pair<>(a, b));

                //upload photo to firebase storage and call ocr on the result
                Single<OcrResponse> ocrSingle = zipped
                        .subscribeOn(Schedulers.io())
                        .compose(bindToLifecycle())
                        .flatMap(pair ->
                                httpClient.ocr(
                                        pair.second,
                                        Optional.ofNullable(sourceLanguageCodes),
                                        pair.first)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .compose(bindToLifecycle())
                        );

                ocrSingle.subscribe(
                        myData -> {
                            Log.d(TAG, "ocr returns " + myData.toString());
                            startOcrResultActivity(myData);
                            finish();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            String errorMessage = throwable.getMessage();
                            if (errorMessage != null && errorMessage.length() > 0) {
                                startOcrResultActivity(errorMessage);
                            }
                            finish();
                        });


            } else {
                startOcrResultActivity(getString(R.string.file_not_found));
            }
        } else {
            startOcrResultActivity(getString(R.string.network_error));
        }
    }

    boolean isOnline() {
        return NetworkUtils.isOnline(this);
    }

    private void startOcrResultActivity(OcrResponse data) {
        Intent intent = new Intent(this, OcrResultActivity.class);
        intent.putExtra(EXTRA_OCR_RESPONSE, data);
        intent.putExtra(TextFragment.EXTRA_IMAGE_URI, imageUri); //todo refactor for better reading
        startActivity(intent);
    }

    private void startOcrResultActivity(String errorMessage) {
        Log.e(TAG, "ERROR: " + errorMessage);
        Intent intent = new Intent(this, OcrResultActivity.class);
        intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        startActivity(intent);
    }

    public void initAnimatedScanBand(boolean isTestMode) {
        if (isTestMode) {
            return;
        }
        ImageView scan_band = findViewById(R.id.scan_band);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        TranslateAnimation animation = new TranslateAnimation(0.0f, width,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(5000);  // animation duration
        animation.setRepeatCount(Animation.INFINITE);  // animation repeat count
        animation.setRepeatMode(2);   // repeat animation (left to right, right to left )

        scan_band.startAnimation(animation);  // start animation
    }

    private void initCancelBtn() {
        Button cancel = findViewById(R.id.cancel_btn);
        RxView.clicks(cancel)
                .subscribe(aVoid -> {
                    Log.d(TAG, "OCR canceled by user");
                    setResult(RESULT_CANCELED_BY_USER);
                    finish();
                });
    }

    private Completable initImage() {
        return Completable.create(emitter -> {
            ImageView imageView = findViewById(R.id.image);

            Glide.with(this)
                    .load(imageUri)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis()))) //needs because image url not changed. It returns the same image all the time if remove this line. It is because default build-in cashe mechanism.
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            emitter.onError(e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            emitter.onComplete();
                            return false;
                        }
                    })
                    .crossFade()
                    .fitCenter()
                    .into(imageView);
        });

    }

    /**
     * async upload photo to firebase storage
     *
     * @param uri
     */
    public Single<String> uploadPhoto(Uri uri) {
        return Single.create(emitter -> {
            String uuid = UUID.randomUUID().toString();
            mImageRef = FirebaseStorage.getInstance().getReference().child(
                    "ocr_request_images/" + uuid + uri.getLastPathSegment());

            mImageRef
                    .putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        String path = taskSnapshot.getMetadata().getReference().getPath();
                        Log.d(TAG, "uploadPhoto:onSuccess:" + path);
                        String gcsImageUri = firebaseFolderURL + path;
                        emitter.onSuccess(gcsImageUri);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "uploadPhoto:onError", e);
                        emitter.onError(e);
                    });
        });
    }

    /**
     * async get idToken, docs: https://firebase.google.com/docs/auth/admin/verify-id-tokens
     */
    public Single<Optional<String>> getIdToken() {
        return Single.create(emitter -> {
            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                mUser.getIdToken(false)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                emitter.onSuccess(Optional.ofNullable(idToken));
                            } else {
                                emitter.onSuccess(Optional.empty());
                            }
                        });
            } else {
                emitter.onSuccess(Optional.empty());
            }
        });
    }
}