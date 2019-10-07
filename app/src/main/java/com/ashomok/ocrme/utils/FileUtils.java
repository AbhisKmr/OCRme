package com.ashomok.ocrme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.ashomok.ocrme.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.ashomok.ocrme.utils.LogHelper;

/**
 * Created by iuliia on 5/30/17.
 */

public class FileUtils {
    private static final String TAG = LogHelper.makeLogTag(FileUtils.class);
    private static final String DEFAULT_DIR_NAME = "Camera";
    public static final int maxImageSizeBytes = 200 * 1024; //200 kb

    /**
     * create File in DEFAULT_DIR_NAME
     *
     * @param fileName
     * @return File
     */
    public static File createFile(String fileName) throws Exception {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), DEFAULT_DIR_NAME);

        if (!storageDir.exists()) {
            prepareDirectory(storageDir.getPath());
        }

        return new File(storageDir, fileName);
    }

    /**
     * @param fileName
     * @return File
     */
    public static Uri createFileForUri(String fileName, Context context) throws Exception {
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //explanation https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en

            uri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    createFile(fileName));
        } else {
            uri = Uri.fromFile(createFile(fileName));
        }
        return uri;
    }

    public static byte[] scaleBitmapDown(Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, 100, baos);
        int options = 100;

        while (baos.toByteArray().length > maxImageSizeBytes) {
            baos.reset();
            bitmap.compress(compressFormat, options, baos);
            options -= 10;
            LogHelper.d(TAG, "image byte array size = " + baos.toByteArray().length);
        }

        return baos.toByteArray();
    }


    public static byte[] toBytes(Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, 100, baos);
        LogHelper.d(TAG, "image byte array size = " + baos.toByteArray().length);
        return baos.toByteArray();
    }


    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public static void prepareDirectory(String path) throws Exception {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LogHelper.e(TAG, "ERROR: Creation of directory " + path
                        + " failed");
                throw new Exception(
                        "Could not create folder" + path);
            }
        } else {
            LogHelper.d(TAG, "Created directory " + path);
        }
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

}
