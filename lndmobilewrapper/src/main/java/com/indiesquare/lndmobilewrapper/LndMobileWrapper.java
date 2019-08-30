package com.indiesquare.lndmobilewrapper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lndmobile.Callback;
import lndmobile.Lndmobile;
import lndmobile.SendStream;


import lndmobile.RecvStream;



public class LndMobileWrapper {


    private static final String streamEventName = "streamEvent";
    private static final String streamIdKey = "streamId";
    private static final String respB64DataKey = "data";
    private static final String respErrorKey = "error";
    private static final String respEventTypeKey = "event";
    private static final String respEventTypeData = "data";
    private static final String respEventTypeError = "error";
    private static final String logEventName = "logs";



    static class ReceiveStream implements RecvStream {

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onResponse(byte[] bytes) {

        }
    }

    public LndMobileWrapper() {


    }

    public static long checkStorage(){
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());

        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();

        return bytesAvailable;
    }
    @Override
    public void finalize() {
       Log.i("lndmobilewrapper","finalize");
    }

    public static void startLnd(final Context context, final String config, final boolean bootstrap, final CallbackInterface callback){


        final File appDir =  context.getFilesDir();

        saveConfig(context,config,appDir);
        boolean testnet = config.contains("testnet=1");

        if(bootstrap) {

            String neutrinoDB = "mainnet/neutrino.db";
            String blockheadersBin = "mainnet/block_headers.bin";
            String regFilterHeadersBin = "mainnet/reg_filter_headers.bin";
            String directory = "mainnet";

            if(testnet) {
                Log.d("LNDWRAPPER","starting testnet");
                 neutrinoDB = "testnet/neutrino.db";
                 blockheadersBin = "testnet/block_headers.bin";
                 regFilterHeadersBin = "testnet/reg_filter_headers.bin";
                 directory = "testnet";
            }else{
                Log.d("LNDWRAPPER","starting mainnet");
            }

            String outputPath = appDir + "/data/chain/bitcoin";

            moveFile(context, neutrinoDB, outputPath,directory);
            moveFile(context, blockheadersBin, outputPath,directory);
            moveFile(context, regFilterHeadersBin, outputPath,directory);
        }


        Runnable startLnd = new Runnable() {
            @Override
            public void run() {
                String args = "--lnddir=" + appDir;
                Lndmobile.start(args, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {
                        Log.d("LNDWRAPPER","res"+bytes.toString());

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });
            }
        };
        new Thread(startLnd).start();

    }

    public static void generateSeed(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.genSeed(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void stopLND(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.stopDaemon(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void unlockWallet(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.unlockWallet(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void initWallet(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.initWallet(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void describeGraph(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.describeGraph(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void getInfo(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.getInfo(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void exportAllChannelBackups(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.exportAllChannelBackups(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void listPayments(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.listPayments(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void listInvoices(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.listInvoices(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void getTransactions(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.getTransactions(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void getChannelBalance(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.channelBalance(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }
/*
    public static void subscribeSingleInvoice(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.subscribeSingleInvoice(request, new ReceiveStream() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());
                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }
*/
    public static void subscribeInvoices(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {

                Lndmobile.subscribeInvoices(request, new ReceiveStream() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void verifyMessage(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.verifyMessage(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void signMessage(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.signMessage(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }
/*
    public static void settleInvoice(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.settleInvoice(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }*/
/*
    public static void cancelInvoice(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.cancelInvoice(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }
*/
/*
    public static void addHoldInvoice(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.addHoldInvoice(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }
*/
    public static void addInvoice(final byte[] request, final CallbackInterface callback){




Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.addInvoice(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void newAddress(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.newAddress(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void walletBalance(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.walletBalance(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void listChannels(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.listChannels(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void pendingChannels(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.pendingChannels(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void getNodeInfo(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.getNodeInfo(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void lookupInvoice(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.lookupInvoice(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void openChannel(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.openChannel(request, new ReceiveStream() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void sendPaymentSync(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.sendPaymentSync(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void sendPayment(final byte[] request, final CallbackInterface callback){


            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    try {

                   SendStream sendStream = Lndmobile.sendPayment(new ReceiveStream() {
                        @Override
                        public void onError(Exception e) {
                            try {
                                JSONObject json = new JSONObject();
                                json.put("error", true);
                                json.put("response", e.getLocalizedMessage());

                                callback.eventFired(json.toString());
                            } catch (Exception e2) {
                                callback.eventFired("");
                            }
                        }

                        @Override
                        public void onResponse(byte[] bytes) {

                            String b64 = "";
                            if (bytes != null && bytes.length > 0) {
                                b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                            }
                            try {
                                JSONObject json = new JSONObject();
                                json.put("error", false);
                                json.put("response", b64);

                                callback.eventFired(json.toString());
                            } catch (Exception e2) {
                                callback.eventFired("");
                            }

                        }
                    });

                        sendStream.send(request);
                    }
                    catch(Exception e){

                    }

                }

            };
            new Thread(runner).start();



    }


    public static void decodePayReq(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.decodePayReq(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void closeChannel(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.closeChannel(request, new ReceiveStream() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void sendCoins(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.sendCoins(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }


    public static void estimateFee(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.estimateFee(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void connectPeer(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.connectPeer(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    public static void addTower(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.addTower(request, new Callback() {
                    @Override
                    public void onError(Exception e) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", true);
                            json.put("response", e.getLocalizedMessage());

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }
                    }

                    @Override
                    public void onResponse(byte[] bytes) {

                        String b64 = "";
                        if (bytes != null && bytes.length > 0) {
                            b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        }
                        try {
                            JSONObject json = new JSONObject();
                            json.put("error", false);
                            json.put("response", b64);

                            callback.eventFired(json.toString());
                        } catch (Exception e2) {
                            callback.eventFired("");
                        }

                    }
                });

            }
        };
        new Thread(runner).start();

    }

    private static void saveConfig(Context context, String config, File appDir) {

        String filename = "lnd.conf";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(config.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static void moveFile(Context context, String inputPath, String outputPath,String directory) {

        InputStream in = null;
        OutputStream out = null;
        try {
            Log.d("filemove","moving file "+inputPath);
            //create output directory if it doesn't exist
            File dir = new File (outputPath+"/"+directory);
            if (!dir.exists()) {

                dir.mkdirs();

            }
                File file = new File (outputPath+"/"+inputPath);
                if (!file.exists()) {

                    in = context.getAssets().open(inputPath);
                    out = new FileOutputStream(outputPath + "/" + inputPath);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    in = null;
                    // write the output file
                    out.flush();
                    out.close();
                    out = null;
                    Log.d("filemove", "moved file " + inputPath);
                    // delete the original file
                    new File(inputPath).delete();
                }


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("LND FILE NOT FOUND", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static String readLogs(String logsPath) {


        try {

            File file = new File (logsPath);
            if (file.exists()) {

                final InputStream inputStream = new FileInputStream(file);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                final StringBuilder stringBuilder = new StringBuilder();

                boolean done = false;

                while (!done) {
                    final String line = reader.readLine();
                    done = (line == null);

                    if (line != null) {
                        stringBuilder.append(line);
                    }
                }

                reader.close();
                inputStream.close();

                return stringBuilder.toString();

            }
            else{
                Log.i("files","does not exist");
            }


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("LND FILE NOT FOUND", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

        return "";

    }




}
