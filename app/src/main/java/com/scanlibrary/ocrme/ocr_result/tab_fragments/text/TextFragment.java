package com.scanlibrary.ocrme.ocr_result.tab_fragments.text;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.scanlibrary.ocrme.R;
import com.scanlibrary.ocrme.language_choser.LanguageOcrActivity;
import com.scanlibrary.ocrme.ocr.OcrActivity;
import com.scanlibrary.ocrme.ocr_result.translate.TranslateActivity;
import com.scanlibrary.ocrme.utils.GlideApp;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;
import static com.scanlibrary.ocrme.Settings.appPackageName;
import static com.scanlibrary.ocrme.language_choser.LanguageOcrActivity.CHECKED_LANGUAGE_CODES;
import static com.scanlibrary.ocrme.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 5/31/17.
 */

//todo Forbidd splitter to go out of screen bounds. https://github.com/bieliaievays/OCRme/issues/2

public class TextFragment extends DaggerFragment implements View.OnClickListener, TextContract.View {
    public static final String EXTRA_TEXT = "com.ashomokdev.imagetotext.TEXT";
    public static final String EXTRA_IMAGE_URL = "com.ashomokdev.imagetotext.IMAGE";
    public static final String EXTRA_LANGUAGES = "com.ashomokdev.imagetotext.LANGUAGES";
    private static final int LANGUAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final String TAG = DEV_TAG + TextFragment.class.getSimpleName();
    private String textResult;
    private String imageUrl;
    private ArrayList<String> languages;

    @Inject
    TextContract.Presenter textPresenter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_fragment, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            textResult = bundle.getString(EXTRA_TEXT);
            imageUrl = bundle.getString(EXTRA_IMAGE_URL);
            languages = bundle.getStringArrayList(EXTRA_LANGUAGES);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setImage(view);
        initText(view);
        initBottomPanel(view);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.copy_btn:
                copyTextToClipboard(textResult, view);
                break;
            case R.id.translate_btn:
                onTranslateClicked();
                break;
            case R.id.share_text_btn:
                onShareClicked();
                break;
            case R.id.bad_result_btn:
                onBadResultClicked();
                break;
            default:
                break;
        }
    }

    private void onBadResultClicked() {
        Intent intent = new Intent(getActivity(), LanguageOcrActivity.class);
        if (languages != null && languages.size() > 0) {
            ArrayList<String> extra = Stream.of(languages)
                    .collect(Collectors.toCollection(ArrayList::new));
            intent.putStringArrayListExtra(CHECKED_LANGUAGE_CODES, extra);
        }
        startActivityForResult(intent, LANGUAGE_ACTIVITY_REQUEST_CODE);
    }

    private void initBottomPanel(View view) {
        View copyBtn = view.findViewById(R.id.copy_btn);
        copyBtn.setOnClickListener(this);

        View translateBtn = view.findViewById(R.id.translate_btn);
        translateBtn.setOnClickListener(this);

        View shareBtn = view.findViewById(R.id.share_text_btn);
        shareBtn.setOnClickListener(this);

        View badResult = view.findViewById(R.id.bad_result_btn);
        badResult.setOnClickListener(this);
    }

    private void initText(View view) {
        EditText mTextView = view.findViewById(R.id.text);
        mTextView.setText(textResult);
    }

    private void setImage(View view) {

        final ImageView mImageView = view.findViewById(R.id.source_image);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsReference = storage.getReferenceFromUrl(imageUrl);

        GlideApp.with(this)
                .load(gsReference)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        view.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.ic_broken_image)
                .fitCenter()
                .into(mImageView);

        //scroll to centre
        final ScrollView scrollView = view.findViewById(R.id.image_scroll_view);
        scrollView.addOnLayoutChangeListener((
                v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int centreHeight = mImageView.getHeight() / 2;
            int centreWidth = mImageView.getWidth() / 2;
            scrollView.scrollTo(centreWidth, centreHeight);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LANGUAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            //language was changed - run ocr again for the same image
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                ArrayList<String> newLanguages = bundle.getStringArrayList(CHECKED_LANGUAGE_CODES);
                runOcr(newLanguages);
            }
        }
    }

    private void runOcr(ArrayList<String> languages) {
        if (imageUrl != null) {
            Intent intent = new Intent(getActivity(), OcrActivity.class);
            intent.putExtra(OcrActivity.EXTRA_IMAGE_URL, imageUrl);
            intent.putStringArrayListExtra(OcrActivity.EXTRA_LANGUAGES, languages);

            startActivity(intent);

            if (getActivity()!= null) {
                getActivity().finish();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onShareClicked() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        Resources res = getResources();
        String linkToApp = "https://play.google.com/store/apps/details?id=" + appPackageName;
        String sharedBody =
                String.format(res.getString(R.string.share_text_message), textResult, linkToApp);

        Spanned styledText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            styledText = Html.fromHtml(sharedBody, Html.FROM_HTML_MODE_LEGACY);
        } else {
            styledText = Html.fromHtml(sharedBody);
        }

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, res.getString(R.string.text_result));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, styledText);
        startActivity(Intent.createChooser(sharingIntent, res.getString(R.string.send_text_result_to)));
    }

    private void onTranslateClicked() {
        Intent intent = new Intent(getActivity(), TranslateActivity.class);
        intent.putExtra(EXTRA_TEXT, textResult);
        startActivity(intent);
    }

    private void copyTextToClipboard(CharSequence text, View view) {
        ClipboardManager clipboard =
                (ClipboardManager) view.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.text_result), text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), getString(R.string.copied),
                    Toast.LENGTH_SHORT).show();
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 300 milliseconds
            if (v != null) {
                v.vibrate(300);
            }
        }
    }
}
