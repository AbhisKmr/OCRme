package com.ashomok.ocrme.crop_image;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ashomok.ocrme.R;
import com.ashomok.ocrme.ocr.OcrActivity;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.UUID;

import static com.ashomok.ocrme.ocr.OcrActivity.EXTRA_LANGUAGES;
import static com.ashomok.ocrme.utils.FileUtils.createFileForUri;
import static com.ashomok.ocrme.utils.InfoSnackbarUtil.showError;
import static com.ashomok.ocrme.utils.InfoSnackbarUtil.showWarning;
import static com.ashomok.ocrme.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 11/28/17.
 */

public class CropImageActivity extends AppCompatActivity
        implements CropImageView.OnCropImageCompleteListener {

    public static final String EXTRA_IMAGE_URI = "com.ashomokdev.imagetotext.crop_image.IMAGE_URI";
    private static final String TAG = DEV_TAG + CropImageActivity.class.getSimpleName();
    private final static String cropped_file_extension = ".jpg";
    private CropImageView mCropImageView;
    private Uri imageUri;
    private ArrayList<String> sourceLanguageCodes;
    private FloatingActionButton cropBtn;
    private String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String cropped_filename;
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        initToolbar();

        mCropImageView = findViewById(R.id.cropImageView);
        mCropImageView.setOnCropImageCompleteListener(this);

        imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        sourceLanguageCodes = getIntent().getStringArrayListExtra(EXTRA_LANGUAGES);

        mCropImageView.setImageUriAsync(imageUri);
        cropBtn = findViewById(R.id.crop_btn);
        mRootView = findViewById(R.id.root_view);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);

        RxView.clicks(cropBtn)
                .compose(rxPermissions.ensureEach(permission))
                .subscribe(permission -> {
                    if (permission.granted) {
                        try {
                            //todo delete cropped file when processed
                            cropped_filename = UUID.randomUUID().toString() + cropped_file_extension;
                            mCropImageView.saveCroppedImageAsync(
                                    createFileForUri(cropped_filename, this));
                        } catch (Exception e) {
                            showError(e.getMessage(), mRootView);
                        }
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        showWarning(R.string.needs_to_save, mRootView);
                    } else {
                        showWarning(R.string.this_option_is_not_be_avalible, mRootView);
                    }
                }, throwable -> {
                    showError(throwable.getMessage(), mRootView);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_action_rotate) {
            mCropImageView.rotateImage(90);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            //back btn pressed
            //save data if you need here
            finish();
        });
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        handleCropResult(result);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropResult(CropImageView.CropResult result) {
        if (result.getError() == null) {
            Intent intent = new Intent(this, OcrActivity.class);
            if (result.getUri() != null) {

                intent.putExtra(OcrActivity.EXTRA_IMAGE_URI, result.getUri());
                intent.putStringArrayListExtra(OcrActivity.EXTRA_LANGUAGES, sourceLanguageCodes);

                startActivity(intent);
                finish();
            } else {
                showError(R.string.unknown_error, mRootView);
            }
        } else {
            String errorMessage = result.getError().getMessage();
            if (errorMessage != null && errorMessage.length() > 0) {
                showError(errorMessage, mRootView);
            } else {
                showError(R.string.unknown_error, mRootView);
            }
        }
    }
}
