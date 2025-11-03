package com.bdajaya.adminku.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bdajaya.adminku.AdminkuApplication;

/**
 * Background worker untuk cleanup share cache setelah share selesai.
 * Menggunakan WorkManager untuk ensure reliability dan scheduling.
 */
public class ShareCacheCleanup extends Worker {

    private static final String TAG = "ShareCacheCleanup";

    public ShareCacheCleanup(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting share cache cleanup");

            Context appContext = getApplicationContext();
            if (appContext instanceof AdminkuApplication) {
                AdminkuApplication app = (AdminkuApplication) appContext;
                app.getImageStorageManager().cleanupSharingCache();

                Log.d(TAG, "Share cache cleanup completed successfully");
                return Result.success();
            } else {
                Log.e(TAG, "Application context is not AdminkuApplication");
                return Result.failure();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during share cache cleanup", e);
            return Result.failure();
        }
    }
}
