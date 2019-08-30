package com.mandelduck.androidcore;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ABCoreService extends Service {

    private final static String TAG = ABCoreService.class.getName();
    private final static int NOTIFICATION_ID = 922430164;
    private static final String PARAM_OUT_MSG = "rpccore";
    private Process mProcess;
    private Process mProcessTor;
    Timer timer;

    private static void removeNotification(final Context c) {
        ((NotificationManager) c.getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainController.RPCResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, "exception");
        broadcastIntent.putExtra("exception", "");
        c.sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private void setupNotificationAndMoveToForeground() {
        Log.i(TAG, "started");

        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainController.RPCResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, "OK");
        sendBroadcast(broadcastIntent);
        Log.i(TAG, "started2");


        final Intent i = new Intent(this, MainController.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent pI;
        pI = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        final NotificationManager nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String version = com.mandelduck.androidcore.Packages.getVersion(prefs.getString("version", com.mandelduck.androidcore.Packages.BITCOIN_NDK));

        final Notification.Builder b = new Notification.Builder(this)
                .setContentTitle("Nayuta Full Node Is Running")
                .setContentIntent(pI)
                .setContentText(String.format("Version %s", version))
                .setSmallIcon(R.drawable.ic_nayuta_icon)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final int importance = NotificationManager.IMPORTANCE_LOW;

            final NotificationChannel mChannel = new NotificationChannel("channel_00", "ABCore", importance);
            mChannel.setDescription(String.format("Version %s", version));
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nM.createNotificationChannel(mChannel);
            b.setChannelId("channel_00");
        }


        final Notification n = b.build();

        startForeground(NOTIFICATION_ID, n);

        MainController.postStart();

        try {
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", "starting");

            MainController.sendMessage(json.toString());

            timer = new Timer();
            timer.schedule(new GetBlockChainInfo(), 0, 20000);

        } catch (Exception e2) {
           Log.e(TAG,e2.toString());
        }
    }
     class GetBlockChainInfo extends TimerTask {
        public void run() {
            final Intent i = new Intent(getBaseContext(), RPCIntentService.class);
            i.putExtra("CONSOLE_REQUEST", "getblockchaininfo");
            getBaseContext().startService(i);


        }
    }
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i(TAG, "started2");
        if (mProcess != null || intent == null)
            return START_STICKY;


        Log.i(TAG, "Core service msg");

        try {

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            final String path = getNoBackupFilesDir().getCanonicalPath();

             final ProcessBuilder torpb = new ProcessBuilder(
                    String.format("%s/%s", path, "tor"),
                    "SafeSocks",
                    "1",
                    "SocksPort",
                    "auto",
                    "NoExec",
                    "1",
                    "CookieAuthentication",
                    "1",
                    "ControlPort",
                    "9051",
                    "DataDirectory",
                    path + "/tordata"
            );

            torpb.directory(new File(path));

           // mProcessTor = torpb.start();

            final ProcessLogger.OnError er = new ProcessLogger.OnError() {
                @Override
                public void onError(final String[] error) {
                    mProcess = null;
                    final StringBuilder bf = new StringBuilder();
                    for (final String e : error)
                        if (!TextUtils.isEmpty(e))
                            bf.append(String.format("%s%s", e, System.getProperty("line.separator")));

                    Log.i(TAG, bf.toString());
                }
            };
          //  final ProcessLogger torErrorGobbler = new ProcessLogger(mProcessTor.getErrorStream(), er);
            //final ProcessLogger torOutputGobbler = new ProcessLogger(mProcessTor.getInputStream(), er);

           // torErrorGobbler.start();
            //torOutputGobbler.start();

            // allow to pass in a different datadir directory

            // HACK: if user sets a datadir in the bitcoin.conf file that should then be the one
            // used


            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final String useDistribution = prefs.getString("usedistribution", "core");
            final String daemon = "liquid".equals(useDistribution) ? "liquidd" : "bitcoind";
            final ProcessBuilder pb = new ProcessBuilder(
                    String.format("%s/%s", path, daemon),
                    "--server=1",
                    String.format("--datadir=%s", Utils.getDataDir(this)),
                    String.format("--conf=%s", Utils.getBitcoinConf(this)));

            pb.directory(new File(path));

            mProcess = pb.start();

            final ProcessLogger errorGobbler = new ProcessLogger(mProcess.getErrorStream(), er);
            final ProcessLogger outputGobbler = new ProcessLogger(mProcess.getInputStream(), er);

            errorGobbler.start();
            outputGobbler.start();

            setupNotificationAndMoveToForeground();

        } catch (final IOException e) {
            Log.i(TAG, "Native exception!");
            Log.i(TAG, e.getMessage());

            Log.i(TAG, e.getLocalizedMessage());
            removeNotification(this);
            mProcess = null;
            mProcessTor = null;
            e.printStackTrace();
        }
        Log.i(TAG, "background Task finished");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        final Intent i = new Intent(MainController.thisContext, RPCIntentService.class);
        i.putExtra("CONSOLE_REQUEST", "stop");
        MainController.thisContext.startService(i);
        if(timer != null) {
            timer.cancel();
        }
        Log.i(TAG, "destroying core service");

        if (mProcess != null) {
            //mProcess.destroy();
            mProcessTor.destroy();
            //mProcess = null;
            mProcessTor = null;
        }
    }
}