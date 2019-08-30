package com.scanlibrary.ocrme.get_more_requests.row.free_options.option_delegates;

import android.content.SharedPreferences;
import android.util.Log;

import com.scanlibrary.ocrme.OcrRequestsCounter;
import com.scanlibrary.ocrme.get_more_requests.GetMoreRequestsActivity;
import com.scanlibrary.ocrme.get_more_requests.row.free_options.UiFreeOptionManagingDelegate;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import static com.scanlibrary.ocrme.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 3/6/18.
 */

public class LoginToSystemDelegate extends UiFreeOptionManagingDelegate {
    public static final String TAG = DEV_TAG + LoginToSystemDelegate.class.getSimpleName();
    public static final String ID = "login_to_system";
    private static final String LOGIN_TO_SYSTEM_DONE_TAG = "LOGIN_TO_SYSTEM_DONE";
    private final OcrRequestsCounter ocrRequestsCounter;

    private GetMoreRequestsActivity activity;
    private SharedPreferences sharedPreferences;

    @Inject
    public LoginToSystemDelegate(GetMoreRequestsActivity activity, OcrRequestsCounter ocrRequestsCounter,
                                 SharedPreferences sharedPreferences) {
        super(activity);
        this.ocrRequestsCounter = ocrRequestsCounter;
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected void startTask() {
        Log.d(TAG, "onstartTask");
        saveData();
        activity.signIn();

        onTaskDone(ocrRequestsCounter, activity);
    }

    @Override
    public boolean isTaskAvailable() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean loggedIn = auth.getCurrentUser() != null;
        boolean isAlreadyDone = sharedPreferences.getBoolean(LOGIN_TO_SYSTEM_DONE_TAG, false);
        return (!loggedIn) && (!isAlreadyDone);
    }

    private void saveData() {
        sharedPreferences.edit().putBoolean(LOGIN_TO_SYSTEM_DONE_TAG, true).apply();
    }
}
