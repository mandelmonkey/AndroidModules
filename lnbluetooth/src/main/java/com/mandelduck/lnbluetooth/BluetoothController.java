package com.mandelduck.lnbluetooth;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import android.os.ParcelUuid;
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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class BluetoothController {

    private static final String invoiceCharacteristicUUID =       "e02c57a1-cac8-4edc-ae2a-657cc6c09104";
    private static final String invoiceResultCharacteristicUUID = "e02c57a3-cac8-4edc-ae2a-657cc6c09104";
    private static final String payeeRequestInvoiceUUID =         "e02c57a4-cac8-4edc-ae2a-657cc6c09104";

    private static final UUID UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;

    static private BluetoothAdapter mBluetoothAdapter;
    static private BluetoothLeScanner mBluetoothScanner;
    static CallbackInterface currentCallback;
    static CallbackInterface mScanDeviceCallback;
    public static Context thisContext;


    static private BluetoothDevice mDevice = null;
    static BluetoothGatt currentGatt;

    static byte[] currentChunk = null;
    static byte[] currentMessage = null;
    static List<Byte> currentRecvMessage = new ArrayList<Byte>();

    static byte START_FLAG = 0x02;

    static byte END_FLAG = 0x03;

    public static BluetoothGattCharacteristic invoiceCharacteristic = null;
    public static BluetoothGattCharacteristic invoiceResultCharacteristic = null;
    public static BluetoothGattCharacteristic invoiceRequestCharacteristic = null;


    public BluetoothController() {
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

    public static void setUp(final Context context,final CallbackInterface callback) {

        currentCallback = callback;
        thisContext = context;

        final android.bluetooth.BluetoothManager bluetoothManager =
                (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

    };


        public static void scanDevices(final String udid, final String name, final CallbackInterface callback) {
        Log.i(TAG,"start scan "+udid+" "+name);

            mScanDeviceCallback = callback;
            if(udid.length() > 0 || name.length() > 0) {

                List<ScanFilter> filters = new ArrayList<ScanFilter>();


                if(udid.length() > 0) {

                    ScanFilter.Builder builder = new ScanFilter.Builder();
                    builder.setServiceUuid(ParcelUuid.fromString(udid));
                    filters.add(builder.build());
                }
                if(name.length() > 0) {
                    ScanFilter.Builder builder = new ScanFilter.Builder();
                    builder.setDeviceName(name);
                    filters.add(builder.build());
                }

                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(1000)
                        .build();




                mBluetoothScanner.startScan(filters, new ScanSettings.Builder().build(), mScanCallback);
            }
            else{
                Log.i(TAG,"no filter");
                mBluetoothScanner.startScan(mScanCallback);
            }




    }

    public static void stopScan(){
        mBluetoothScanner.stopScan(mScanCallback);
    }



        public static  void sendNextChunk(byte[] bytes, byte[] original){

            Log.i(TAG,"sending");


            String s = new String(bytes, StandardCharsets.UTF_8);

            Log.i(TAG,s);

            int MTU = 20;
            int startPos = bytes.length;

            if(startPos > original.length){
                startPos = original.length;
            }
            int endPos = original.length-startPos;

            if(endPos > original.length){
                endPos = original.length;
            }
            Log.i(TAG,"original length:"+original.length+"  start pos:"+startPos+"  end pos:"+endPos);


            original = Arrays.copyOfRange(original, startPos,startPos+endPos);

            int sliceAmount = MTU;
            if(sliceAmount > original.length){
                sliceAmount = original.length;
            }

            byte[] slice = Arrays.copyOfRange(original, 0, sliceAmount);

            if(sliceAmount > 0) {

                currentChunk = slice;
                currentMessage = original;

                if(invoiceCharacteristic != null) {
                    invoiceCharacteristic.setValue(currentChunk);
                    currentGatt.writeCharacteristic(invoiceCharacteristic);
                }else{
                    testReceive(currentChunk);
                }


            }else{
                Log.i(TAG,"end");

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "message");
                    obj.put("status", "sent");
                    currentCallback.eventFired(obj.toString());
                }
                catch(Exception e){

                }

            }



        }

        public static void sendTestMessageAndReceive(){






        }


        public static void fakeReceive(int step){
           // byte[] bytes = new byte[]{2, 49, 50, 51, 52, 44, 54, 48, 44, 66, 76, 69, 3};
            byte[] bytes = null;
            if(step == 1) {
                 bytes = new byte[]{2, 49, 48, 48, 48, 48, 44, 51, 54, 48, 48, 44, 98};
            }else {


                 bytes = new byte[]{114, 101, 110, 100, 32, 99, 111, 102, 102, 101, 101, 3};
            }
            String s1 = new String(bytes, StandardCharsets.UTF_8);
            Log.i(TAG,"string is "+s1);

            int startIndex = 0;
            int endIndex = bytes.length;

            if(bytes[0] == START_FLAG){
                currentRecvMessage = new ArrayList<Byte>();
                startIndex = 1;
                Log.i(TAG,"found start flag");

            }

            if(bytes[bytes.length-1] == END_FLAG){
                endIndex = bytes.length-1;
                Log.i(TAG,"found end flag");
            }

            for(int i = startIndex;i<endIndex;i++){
                Log.i(TAG,"adding byte "+bytes[i]);
                currentRecvMessage.add(bytes[i]);
            }

            if(endIndex != bytes.length){

                byte[] finalString = new byte[currentRecvMessage.size()];

                for(int i = 0;i<currentRecvMessage.size();i++){
                    finalString[i] = currentRecvMessage.get(i);
                }

                Log.i(TAG,"finalString "+finalString.length+" "+finalString);

                String s = new String(finalString, StandardCharsets.UTF_8);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", "createInvoice");
                    obj.put("data", s);
                    currentCallback.eventFired(obj.toString());
                } catch (Exception e) {
                    Log.e(TAG,e.getLocalizedMessage());
                }

            }
        }

    public static void testSendMessageBytes(){
        byte[] messageBytes = new byte[]{2, 49, 50, 51, 52, 44, 54, 48, 44, 66, 76, 69, 3};
        int MTU = 20;


            byte[] formatted = new byte[messageBytes.length+2];
            formatted[0] = START_FLAG;
            formatted[formatted.length-1] = END_FLAG;

            for(int i=0;i<messageBytes.length;i++){
                formatted[i+1] = messageBytes[i];
            }

            int sliceAmount = MTU;
            if(sliceAmount > formatted.length){
                sliceAmount =formatted.length;
            }

            byte[] slice = Arrays.copyOfRange(formatted, 0, sliceAmount);

            currentChunk = slice;
            currentMessage = formatted;


                testReceive(currentChunk);




    }
    public static void sendMessage(String message){

        int MTU = 20;
        try {

            byte[] messageBytes = message.getBytes("UTF-8");

            byte[] formatted = new byte[messageBytes.length+2];
            formatted[0] = START_FLAG;
            formatted[formatted.length-1] = END_FLAG;

            for(int i=0;i<messageBytes.length;i++){
                formatted[i+1] = messageBytes[i];
            }

            int sliceAmount = MTU;
            if(sliceAmount > formatted.length){
                sliceAmount =formatted.length;
            }

            byte[] slice = Arrays.copyOfRange(formatted, 0, sliceAmount);

            currentChunk = slice;
            currentMessage = formatted;

            if(invoiceCharacteristic != null) {
                invoiceCharacteristic.setValue(currentChunk);
                currentGatt.writeCharacteristic(invoiceCharacteristic);
            }else{
                testReceive(currentChunk);
            }



        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void sendInvoiceStatus(int status){

        byte[] data = new byte[2];
        data[0] = 0x00;
        data[1] = (byte)status;

        invoiceResultCharacteristic.setValue(data);
        currentGatt.writeCharacteristic(invoiceResultCharacteristic);

    }

    public static void testReceive(byte[] bytes){

        int startIndex = 0;
        int endIndex = bytes.length;

        if(bytes[0] == START_FLAG){
            currentRecvMessage = new ArrayList<Byte>();
            startIndex = 1;

            Log.i(TAG,"receive: got st flag");

        }

        if(bytes[bytes.length-1] == END_FLAG){
            startIndex = 0;
            endIndex = bytes.length-1;
            Log.i(TAG,"receive: got end flag");
        }

        for(int i = startIndex;i<endIndex;i++){
            currentRecvMessage.add(bytes[i]);
            Log.i(TAG,"receive: appending message "+currentRecvMessage.size());
        }

        if(endIndex != bytes.length){
            byte[] finalString = new byte[currentRecvMessage.size()];
            for(int i = 0;i<currentRecvMessage.size();i++){
                finalString[i] = currentRecvMessage.get(i);
            }

            String s = new String(finalString, StandardCharsets.UTF_8);


            Log.i(TAG,"finish: "+s);

        }

        sendNextChunk(currentChunk,currentMessage);
    }
    public static void connectToDevice(String deviceAddress,final CallbackInterface callback){

        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.i(TAG,"connecting to name:"+mDevice.getName());

        BluetoothGattCallback gattCallback =
                new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothGatt.STATE_CONNECTED){
                            currentGatt = gatt;
                            Log.i(TAG,"connected");
                            gatt.discoverServices();

                            try {
                                Log.i(TAG,"sending connected event");
                                JSONObject obj = new JSONObject();
                                obj.put("type", "connected");
                                obj.put("device", mDevice.getAddress());
                                callback.eventFired(obj.toString());
                            }
                            catch(Exception e){
                                Log.i(TAG,e.getLocalizedMessage());
                                Log.e(TAG,e.getLocalizedMessage());
                            }


                        }
                        else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                            Log.i(TAG,"disconnected");

                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("type", "disconnected");
                                currentCallback.eventFired(obj.toString());
                            }
                            catch(Exception e){
                                Log.e(TAG,e.getLocalizedMessage());
                            }


                        }

                        Log.i(TAG,"gatt state"+newState+"");
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);

                        Log.i(TAG,"did write characterstic");


                        sendNextChunk(currentChunk,currentMessage);
                    }



                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        if(characteristic == invoiceRequestCharacteristic) {

                            byte[] bytes = characteristic.getValue();

                            int startIndex = 0;
                            int endIndex = bytes.length;

                            if(bytes[0] == START_FLAG){
                                currentRecvMessage = new ArrayList<Byte>();
                                startIndex = 1;

                            }

                            if(bytes[bytes.length-1] == END_FLAG){
                                //startIndex = 0; because if only one chunk then startIndex needs to start from not 0
                                endIndex = bytes.length-1;
                            }

                            for(int i = startIndex;i<endIndex;i++){
                                currentRecvMessage.add(bytes[i]);
                            }

                            if(endIndex != bytes.length){
                                byte[] finalString = new byte[currentRecvMessage.size()];
                                for(int i = 0;i<currentRecvMessage.size();i++){
                                    finalString[i] = currentRecvMessage.get(i);
                                }

                                String s = new String(finalString, StandardCharsets.UTF_8);


                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("type", "createInvoice");
                                    obj.put("data", s);
                                    currentCallback.eventFired(obj.toString());
                                } catch (Exception e) {
                                    Log.e(TAG,e.getLocalizedMessage());
                                }

                            }

                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status){
                        Log.i(TAG,"service discovered");


                        if(status == BluetoothGatt.GATT_SUCCESS){

                            for (BluetoothGattService gattService : gatt.getServices()) {
                                Log.i(TAG, "onServicesDiscovered: ---------------------");
                                Log.i(TAG, "onServicesDiscovered: service=" + gattService.getUuid());
                                for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                                    Log.i(TAG, "onServicesDiscovered: characteristic=" + characteristic.getUuid().toString());

                                    if (characteristic.getUuid().toString().equals(invoiceCharacteristicUUID)) {

                                        invoiceCharacteristic = characteristic;
                                        Log.i(TAG, "onServicesDiscovered: found invoice");


                                    }
                                    if (characteristic.getUuid().toString().equals(invoiceResultCharacteristicUUID)) {

                                        invoiceResultCharacteristic = characteristic;
                                        Log.i(TAG, "onServicesDiscovered: found invoice result");


                                    }
                                    if (characteristic.getUuid().toString().equals(payeeRequestInvoiceUUID)) {


                                        invoiceRequestCharacteristic = characteristic;
                                        Log.i(TAG, "onServicesDiscovered: found invoice request");



                                        gatt.setCharacteristicNotification(characteristic, true);

                                        BluetoothGattDescriptor descriptor =
                                                characteristic.getDescriptor(UUID_CCC);

                                        descriptor.setValue(
                                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        gatt.writeDescriptor(descriptor);

                                        Log.i(TAG, "registered notifications");



                                    }



                                }
                            }
                        }
                        else {
                            Log.i(TAG, "onServicesDiscovered received: " + status);
                        }


                    }

                };

        mDevice.connectGatt(thisContext,false,gattCallback);

    }
    public static void disconnect(){
        if(currentGatt != null) {
            currentGatt.close();
            currentGatt = null;
        }
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
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", "scan");
                        obj.put("name", device.getName());
                        obj.put("address", device.getAddress());

                        mScanDeviceCallback.eventFired(obj.toString());
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



}

