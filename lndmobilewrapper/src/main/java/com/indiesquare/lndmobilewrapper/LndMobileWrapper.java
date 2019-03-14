package com.indiesquare.lndmobilewrapper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lndmobile.Callback;
import lndmobile.Lndmobile;

import android.util.Log;


public class LndMobileWrapper {


    public LndMobileWrapper() {


    }


    public static void startLnd(final Context context, final String config, final CallbackInterface callback){

        final File appDir =  context.getFilesDir();

        saveConfig(context,config,appDir);

        Runnable startLnd = new Runnable() {
            @Override
            public void run() {
                Lndmobile.start(appDir+"", new Callback() {
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

    public static void subscribeInvoices(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.subscribeInvoices(request, new Callback() {
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

    public static void openChannel(final byte[] request, final CallbackInterface callback){

        Runnable runner = new Runnable() {
            @Override
            public void run() {
                Lndmobile.openChannel(request, new Callback() {
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
                Lndmobile.closeChannel(request, new Callback() {
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




}
