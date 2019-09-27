package com.indiesquare.websocket;

import android.app.Activity;
import android.util.Log;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class websocket {
    private Activity activity;
    private WebSocketClient cc;
    private String TAG = "websocketwrapper";
    public CallbackInterface mCallback;

    public websocket(final Activity currentActivity){

        Log.i(TAG,"inti");
        activity = currentActivity;

    }

    public void sendMessage(String message){

        try {
            Log.i("myapp", "sending "+message);
        cc.send( message );

            Log.i("myapp", "sending2 "+message);
        } catch ( Exception ex ) {


            Log.i("myapp", Log.getStackTraceString(ex));
            mCallback.eventFired(Log.getStackTraceString(ex),null);
        }
    }
    public void closeWebSocket() {

        if(  cc != null ) {
            cc.close();
            cc = null;
        }
    }

        public void connect(String uri,CallbackInterface callback){


        Log.i(TAG,"starta: "+uri);
            uri = uri+"";
        mCallback = callback;

            Log.i(TAG,"starta: "+uri);
        try {
            /*
            if(  cc != null ) {
                cc.close();
                cc = null;
            }*/

            Log.i(TAG,"starta: "+uri);

            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance( "TLS" );
                sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
cc.setSocketFactory(new SSLSocketFactory() {
    @Override
    public String[] getDefaultCipherSuites() {
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return null;
    }
});
           cc.setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );

            cc = new WebSocketClient( new URI( uri ) ) {

                @Override
                public void onMessage( String message ) {

                    Log.i(TAG,"message: "+message);
                   mCallback.eventFired(null,message);

                }

                @Override
                public void onOpen( ServerHandshake handshake ) {
                    Log.i(TAG,"connection: "+handshake.getHttpStatusMessage());
                     mCallback.eventFired(null,"connected");
                }

                @Override
                public void onClose( int code, String reason, boolean remote ) {
                    Log.i(TAG,"closed");
                    Log.i(TAG,code+"");
                    Log.i(TAG,"reason "+reason);


                    Log.i(TAG,"remote "+remote);
                    mCallback.eventFired(null,"disconnected");

                }

                @Override
                public void onError( Exception ex ) {

                    Log.d("myapp", Log.getStackTraceString(ex));
                    mCallback.eventFired(Log.getStackTraceString(ex),null);

                }
            };

/*
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                    return myTrustedAnchors;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) {}
            } };
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                SSLSocketFactory factory = sslContext.getSocketFactory();
                cc.setSocketFactory(factory);
                cc.connect();
            }
            catch (Exception e){
                e.printStackTrace();
            }*/

            Log.i(TAG,"connect");
            cc.connect();


            Log.i(TAG,"connecting");
        } catch ( URISyntaxException ex ) {


            Log.e(TAG,"error "+ex.getMessage());

        }
    }
}
