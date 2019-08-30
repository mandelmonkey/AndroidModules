package com.mandelduck.androidcore;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;

import org.apache.commons.compress.utils.IOUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
    public static boolean HAS_BEEN_STARTED = false;
    private static Timer mTimer;
    static Context thisContext;
    static String thisConfig;
    static Activity thisActivity;
    static CallbackInterface thisCallback;

    private static RPCResponseReceiver mRpcResponseReceiver;

    public MainController() {


    }



    private static void deleteRF(final File f) {

        Log.v(TAG, "Deleting " + f.getAbsolutePath() + "/" + f.getName());
        if (f.isDirectory())
            for (File child : f.listFiles())
                deleteRF(child);

        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }

    public static void deleteCore() {


        final File dir = Utils.getDir(thisActivity);
        deleteRF(new File(dir, "shachecks"));
        deleteRF(new File(dir, "bitcoind"));

        deleteRF(new File(dir, "liquidd"));


    }

    public static void deleteData() {


        deleteRF(new File(Utils.getDataDir(thisActivity)));

    }


    public static boolean isTestnet(){
        return Utils.isTestnet(thisContext);
    }

    public static void onPause() {

        Log.i(TAG, "on pause");

        if (mRpcResponseReceiver != null && thisContext != null) {
            thisContext.unregisterReceiver(mRpcResponseReceiver);
            mRpcResponseReceiver = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }
    public static void readConf(final CallbackInterface callback){

        try{

        final InputStream f = new FileInputStream(Utils.getBitcoinConf(thisContext));

           callback.eventFired(new String(IOUtils.toByteArray(f)));
        IOUtils.closeQuietly(f);

    } catch (final IOException e) {
        Log.i(TAG, e.getMessage());
    }

    }

    public static void saveConf(String conf) {

        if (ContextCompat.checkSelfPermission(thisContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);

        // save file
        OutputStream f = null;
        try {
            f = new FileOutputStream(Utils.getBitcoinConf(thisContext));
            IOUtils.copy(new ByteArrayInputStream(conf.getBytes(StandardCharsets.UTF_8)), f);

        } catch (final IOException e) {
            Log.i(TAG, e.getMessage());
        } finally {
            IOUtils.closeQuietly(f);
        }
    }


    public static void onResume() {

        Log.i(TAG, "on resume");
        /*if (!Utils.isDaemonInstalled(this)) {
            startActivity(new Intent(this, DownloadActivity.class));
            return;
        }*/

        final IntentFilter rpcFilter = new IntentFilter(RPCResponseReceiver.ACTION_RESP);
        if (mRpcResponseReceiver == null) {
            mRpcResponseReceiver = new RPCResponseReceiver();
        }
            rpcFilter.addCategory(Intent.CATEGORY_DEFAULT);
        if(thisContext != null) {
            thisContext.registerReceiver(mRpcResponseReceiver, rpcFilter);

            thisContext.startService(new Intent(thisContext, RPCIntentService.class));

        }


    }


    public static void stopService(){
        thisContext.stopService(new Intent(thisContext, RPCIntentService.class));
    }

    public static void stopCore(){
        final Intent i = new Intent(thisActivity, RPCIntentService.class);
        i.putExtra("stop", "yep");
        thisContext.startService(i);

        thisContext.stopService(new Intent(thisContext, ABCoreService.class));
    }


    private static final String TAG = MainController.class.getName();

    private static boolean isUnpacked(final String sha, final File outputDir) {
        final File shadir = new File(outputDir, "shachecks");
        return new File(shadir, sha).exists();
    }


    public static void callRPC(String command){
        Log.i(TAG,"calling "+command);
        final Intent i = new Intent(thisContext, RPCIntentService.class);
        i.putExtra("CONSOLE_REQUEST", command);
        thisContext.startService(i);
    }

    public static void getBlockchainInfo(){
        final Intent i = new Intent(thisContext, RPCIntentService.class);
        i.putExtra("CONSOLE_REQUEST", "getblockchaininfo");
        thisContext.startService(i);
    }

    public static void generateBlock(){
        final Intent i = new Intent(thisContext, RPCIntentService.class);
        i.putExtra("CONSOLE_REQUEST", "generate 101");
        thisContext.startService(i);
    }
    public static String getDataDir(){
        return Utils.getDir(thisContext).getAbsolutePath();
    }
    public static void configureCore(final Context c) throws IOException {

        final File coreConf = new File(Utils.getBitcoinConf(c));
        //if (coreConf.exists())
        //    return;
        //noinspection ResultOfMethodCallIgnored
        coreConf.getParentFile().mkdirs();

        FileOutputStream outputStream;

        try {


            outputStream = new FileOutputStream(coreConf);
            outputStream.write(thisConfig.getBytes());

           /* outputStream.write("listen=1\n".getBytes());

            //outputStream.write("bind=127.0.0.1\n".getBytes());
            outputStream.write("disablewallet=1\n".getBytes());
            //outputStream.write("testnet=0\n".getBytes());
            outputStream.write("testnet=1\n".getBytes());
            //outputStream.write("addnode=192.168.2.47\n".getBytes());
            outputStream.write("prune=550\n".getBytes());
            //outputStream.write("regtest=1\n".getBytes());
            outputStream.write("upnp=0\n".getBytes());
            // don't attempt onion connections by default
            outputStream.write("validatepegin=0\n".getBytes());
            outputStream.write("lrequesting local oninonistenonion=1\n".getBytes());
            outputStream.write("blocksonly=1\n".getBytes());

            outputStream.write("dbcache=20\n".getBytes());
            outputStream.write("maxsigcachesize=4\n".getBytes());
                    outputStream.write("maxconnections=4\n".getBytes());
                            outputStream.write("rpcthreads=1\n".getBytes());




            // Afaik ipv6 is broken on android, disable by default, user can change this
            // outputStream.write("onlynet=ipv6\n".getBytes());

            */

            for (final File f : c.getExternalFilesDirs(null))
                outputStream.write(String.format("# for external storage try: %s\n", f.getCanonicalPath()).getBytes());

            outputStream.write(String.format("datadir=%s\n", String.format("%s/bitcoinDirec", Utils.getDir(c).getAbsolutePath())).getBytes());


            IOUtils.closeQuietly(outputStream);
        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    private static void refresh() {
        final Intent i = new Intent(thisContext, RPCIntentService.class);
        i.putExtra("REQUEST", "localonion");
        Log.i(TAG,"requesting local oninon");
        thisContext.startService(i);
    }
public static void sendMessage(String message){
     thisCallback.eventFired(message);
}

public static void getProgress(){
    final Intent i = new Intent(thisContext, RPCIntentService.class);
    i.putExtra("REQUEST", "progress");
    thisContext.startService(i);
}
    public static void startCore(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.i(TAG, "start");
            thisContext.startForegroundService(new Intent(thisContext, ABCoreService.class));

        } else {
            thisContext.startService(new Intent(thisContext, ABCoreService.class));
        }
        try {
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", "starting");

            thisCallback.eventFired(json.toString());
        } catch (Exception e2) {
            thisCallback.eventFired("");
        }
        /* if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
       mTimer.schedule(new TimerTask() {
            public void run() {
                thisActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }, 1000, 1000);*/
    }
    public static void setUp(final Context context, final String config, final Activity activity, final CallbackInterface callback) {
        thisContext = context;
        thisActivity = activity;
        thisCallback = callback;
        Log.i(TAG,"config "+config);
        thisConfig = config;

        final String arch = Utils.getArch();
        Log.i(TAG, "arch is "+arch);

        onResume();
        try {
            configureCore(thisContext);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        scheduleJob();
    }

    public static void scheduleJob() {
        if(thisContext == null){
            return;
        }
        ComponentName componentName = new ComponentName(thisContext, SyncJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) thisContext.getSystemService(thisContext.JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public static void cancelJob() {
        JobScheduler scheduler = (JobScheduler) thisContext.getSystemService(thisContext.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job cancelled");
    }

    public static void registerBackgroundSync(){
        if(checkIfDownloaded()){
            Log.d(TAG, "scheduling job");
            scheduleJob();
        }
    }

    public static boolean checkIfDownloaded(){
        return Utils.isDaemonInstalled(thisContext);
    }
    public static void startDownload(){

        HAS_BEEN_STARTED = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
        Log.i(TAG, "made it here");
        Log.i(TAG, "made it herei");
        final File dir = Utils.getDir(thisContext);
        Log.i(TAG, dir.getAbsolutePath());
        final String arch = Utils.getArch();
        Log.i(TAG, arch);

        try {
            final List<String> distro = com.mandelduck.androidcore.Packages.NATIVE_CORE;
            final String useDistribution = "core";
            final String url = com.mandelduck.androidcore.Packages.getPackageUrl(useDistribution, arch);
            final String filePath = Utils.getFilePathFromUrl(thisContext, url);
            String rawSha = null;
            int bs = 0;
            for (final String a : distro) {
                final String hash = a.substring(7);
                bs = Integer.parseInt(a.substring(0, 7));
                if (hash.startsWith(arch)) {
                    rawSha = hash;
                    break;
                }
            }
            if (isUnpacked(rawSha, dir))
                return;

            final int byteSize = bs;
            final Utils.OnDownloadUpdate odsc = new Utils.OnDownloadUpdate() {
                @Override
                public void update(final int bytesPerSecond, final int bytesDownloaded) {
                    Log.i(TAG, "Downloading");
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", false);
                        json.put("response", "download");
                        json.put("bytesPerSecond", bytesPerSecond);
                        json.put("bytesDownloaded", bytesDownloaded);
                        json.put("byteSize", byteSize);

                        thisCallback.eventFired(json.toString());
                    } catch (Exception e2) {
                        thisCallback.eventFired("");
                    }
                }
            };

            if (!new File(filePath).exists() || Utils.isSha256Different(arch, rawSha, filePath) != null) {
                Log.i(TAG, "Downloading here "+url);
                //  sendUpdate("Downloading", useDistribution);
                Utils.downloadFile(url, filePath, odsc);
                Log.i(TAG, "Downloading here2");

                // Verify sha256sum
                // sendUpdate("Verifying", useDistribution);
                Utils.validateSha256sum(arch, rawSha, filePath);

                Log.i(TAG, "Downloading here3");
            }

            try {
                JSONObject json = new JSONObject();
                json.put("error", false);
                json.put("response", "uncompressing");

                thisCallback.eventFired(json.toString());
            } catch (Exception e2) {
                thisCallback.eventFired("");
            }
            //sendUpdate("Uncompressing", useDistribution);

            Utils.extractTarXz(new File(filePath), dir);

            // bitcoin core & deps installed, configure it now
              configureCore(thisContext);

            // notify

       /* // processing done here….
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(DownloadActivity.DownloadInstallCoreResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, "OK");
        sendBroadcast(broadcastIntent);
        HAS_BEEN_STARTED = false;
        markAsDone(rawSha, dir);*/
            HAS_BEEN_STARTED = false;
            try {
                JSONObject json = new JSONObject();
                json.put("error", false);
                json.put("response", "downloaded");

                thisCallback.eventFired(json.toString());
            } catch (Exception e2) {
                thisCallback.eventFired("");
            }

            Log.i(TAG, "Downloading here5");







        } catch (final Utils.ValidationFailure | NoSuchAlgorithmException | IOException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
            final Intent broadcastIntent = new Intent();
           // broadcastIntent.setAction(DownloadActivity.DownloadInstallCoreResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            // broadcastIntent.putExtra(PARAM_OUT_MSG, "exception");
            broadcastIntent.putExtra("exception", e.getMessage());

            //sendBroadcast(broadcastIntent);
        }
        Log.v(TAG, "onHandleIntent END");

            }
        }).start();


    }
    public static void onionMessage(String message){

    }

    public static void postStart() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisContext);

        final String useDistribution = prefs.getString("usedistribution", "core");
       // mTvStatus.setText(getString(R.string.runningturnoff, useDistribution, "knots".equals(useDistribution) ? Packages.BITCOIN_KNOTS_NDK : "liquid".equals(useDistribution) ? Packages.BITCOIN_LIQUID_NDK : Packages.BITCOIN_NDK));

        try {
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", "already started");
            json.put("distribution", useDistribution);

            MainController.sendMessage(json.toString());
        } catch (Exception e2) {
            Log.e(TAG,e2.toString());
        }
    }


    // And From your main() method or any other method

    public static class RPCResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.greenaddress.intent.action.RPC_PROCESSED";

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String text = intent.getStringExtra(RPCIntentService.PARAM_OUT_MSG);
            Log.i(TAG,"did receive "+text);
            switch (text) {
                case "OK":

                    //postStart();
                    break;
                case "exception":
                    final String exe = intent.getStringExtra("exception");
                    if (exe != null)
                        Log.i(TAG, exe);
                    //postConfigure();
                    break;
                case "localonion":
                    /*
                    final String onion = intent.getStringExtra(RPCIntentService.PARAM_ONION_MSG);
                    if (onion != null && mTimer != null) {
                        mTimer.cancel();
                        mTimer.purge();
                    }
                    mQrCodeText.setText(onion);
                    final ByteMatrix matrix;
                    try {
                        Log.d(TAG,"onion"+onion);
                        matrix = Encoder.encode(onion, ErrorCorrectionLevel.M).getMatrix();
                    } catch (final WriterException e) {
                        throw new RuntimeException(e);
                    }
                    final int height = matrix.getHeight() * SCALE;
                    final int width = matrix.getWidth() * SCALE;
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    for (int x = 0; x < width; ++x)
                        for (int y = 0; y < height; ++y)
                            bitmap.setPixel(x, y, matrix.get(x / SCALE, y / SCALE) == 1 ? Color.BLACK : 0);
                    mImageViewQr.setImageBitmap(bitmap);*/
            }
        }
    }




    private static String getLastLines(final File file, final int lines) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            final long fileLength = fileHandler.length() - 1;
            final StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; --filePointer) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength)
                        ++line;
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1)
                        ++line;
                }

                if (line >= lines)
                    break;
                sb.append((char) readByte);
            }

            return sb.reverse().toString();
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (final IOException ignored) {
                }
        }
    }

    public static void getLogs(final CallbackInterface logsCallback){

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisContext);
        final String useDistribution = prefs.getString("usedistribution", "core");
        final String daemon = "liquid".equals(useDistribution) ? "/liquidv1/debug.log" : "/debug.log";

        final File f = new File(Utils.getDataDir(thisContext) + (Utils.isTestnet(thisContext) ? "/testnet3/debug.log" : daemon));
        if (!f.exists()) {
          Log.i(TAG,"No debug file exists yet");
          return;
        }

        for (int lines = 10; lines > 0; --lines) {
            final String txt = getLastLines(f, lines);
            if (txt != null) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", "logs");
                    json.put("res", txt);

                    logsCallback.eventFired(json.toString());
                } catch (Exception e2) {
                   logsCallback.eventFired("");
                }
             }
        }
    }



}
