package com.mandelduck.androidcore;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class SyncJobService extends JobService {
    private static final String TAG = "SyncJobService";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, "Sync Job started");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.i(TAG, "start");
            startForegroundService(new Intent(this, ABCoreService.class));

        } else {
           startService(new Intent(this, ABCoreService.class));
        }
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    Log.d(TAG, "run: " + i);
                    if (jobCancelled) {
                        return;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}