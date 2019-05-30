package com.indiesquare.bluetoothiccard;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BluetoothManager {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    static private BluetoothAdapter mBluetoothAdapter;
    static private BluetoothLeScanner mBluetoothScanner;
    static CallbackInterface currentCallback;
    public static Context thisContext;

    static private int mState = UART_PROFILE_DISCONNECTED;
    static private UartService mService = null;
    static private BluetoothDevice mDevice = null;
    static private BluetoothAdapter mBtAdapter = null;

    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    Map<String, Integer> devRssiValues;

    public BluetoothManager() {
    }


    public static boolean isBluetoothEnabled(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
        }

        return true;
    }



    public static void scanDevices(final Context context, final CallbackInterface callback) {
        currentCallback = callback;
        thisContext = context;
        final android.bluetooth.BluetoothManager bluetoothManager =
                (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        service_init();
        // init scanner
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothScanner.startScan(mScanCallback);




    }
    static void service_init(){

        Log.i(TAG,"starting UartService");
        Intent bindIntent = new Intent(thisContext, UartService.class);
        thisContext.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(thisContext).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());

    }
    public static void requestPublicKey(){
        try {
        byte[] value = "p".getBytes("UTF-8");

        mService.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void sendHash(String hash){
        try {
            byte[] value = hash.getBytes("UTF-8");

            mService.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void turnCardOff(){
        try {
            byte[] value = "q".getBytes("UTF-8");

            mService.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void connectToDevice(String deviceAddress){

       // String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.i(TAG,"was called");
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

        Log.i(TAG, "device address" +deviceAddress);
        Log.i(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
        //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
        if(mService == null){
            Log.i(TAG, "mservice is null");
        }
        mService.connect(deviceAddress);
    }

    private static ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed() : Error = " + errorCode);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i(TAG, "onScanResult()");
            if (null != result) {
                BluetoothDevice device = result.getDevice();
                if (null != device) {
                    //   addDevice(device, result.getRssi());
                    //Log.e(TAG, "found device :" + device.getName());
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", "scan");
                        obj.put("name", device.getName());
                        obj.put("address", device.getAddress());

                        currentCallback.eventFired(obj.toString());
                    }
                    catch(Exception e){

                    }
                } else
                    Log.e(TAG, "onScanResult() : Cannot get BluetoothDevice !!!");
            } else {
                Log.e(TAG, "onScanResult() : Cannot get ScanResult !!!");
            }
        }


    };

        class DeviceAdapter extends BaseAdapter {
            Context context;
            List<BluetoothDevice> devices;
            LayoutInflater inflater;

            public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
                this.context = context;
                inflater = LayoutInflater.from(context);
                this.devices = devices;
                Log.i(TAG, devices.toString());
            }

            @Override
            public int getCount() {
                return devices.size();
            }

            @Override
            public Object getItem(int position) {
                return devices.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //ViewGroup vg;


                BluetoothDevice device = devices.get(position);

                return convertView;
            }
        }



    // UART service connected/disconnected
    private static ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.i(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        //Handler events that received from UART service
        public void handleMessage(Message msg) {

            Log.i("Mes",msg.toString());

        }
    };

    private static final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                /*runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });*/

                try {
                    mState = UART_PROFILE_CONNECTED;
                    JSONObject obj = new JSONObject();
                    obj.put("type", "connected");
                    obj.put("device", mDevice.getAddress());
                    currentCallback.eventFired(obj.toString());
                }
                catch(Exception e){

                }
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
               /* runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });*/
                mState = UART_PROFILE_DISCONNECTED;
                mService.close();
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "disconnected");
                    obj.put("name", mDevice.getName());
                    obj.put("address", mDevice.getAddress());
                    currentCallback.eventFired(obj.toString());
                }
                catch(Exception e){

                }
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }

            if (action.equals(UartService.ERROR)) {

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "error");
                    currentCallback.eventFired(obj.toString());
                }
                catch (Exception e){
                    Log.e(TAG, e.toString());
                }
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                /*runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                            Log.d("msg",text);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });*/
                try {
                    try {
                        String text = new String(txValue, "UTF-8");

                        Log.d("msg",text);
                        JSONObject obj = new JSONObject();
                        obj.put("type", "message");
                        obj.put("data", text);
                        currentCallback.eventFired(obj.toString());

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                }
                catch(Exception e){

                }
            }

            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                currentCallback.eventFired("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.EXTRA_DATA);

        return intentFilter;
    }



}
