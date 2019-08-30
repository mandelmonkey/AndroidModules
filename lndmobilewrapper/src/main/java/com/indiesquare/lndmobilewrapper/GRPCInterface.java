package com.indiesquare.lndmobilewrapper;
/*
import android.util.Log;
import com.google.protobuf.ByteString;
import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.KeyStore;
import javax.security.auth.x500.X500Principal;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.TlsVersion;
import io.grpc.okhttp.OkHttpChannelBuilder;

import io.grpc.stub.StreamObserver;
import invoicesrpc.InvoicesOuterClass.*;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.Executor;


import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.JSONArray;


import lnrpc.Rpc.PendingChannelsResponse.ClosedChannel;
import lnrpc.Rpc.PendingChannelsResponse.ForceClosedChannel;
import lnrpc.Rpc.PendingChannelsResponse.PendingChannel;
import lnrpc.Rpc.PendingChannelsResponse.PendingOpenChannel;
import lnrpc.Rpc.PendingChannelsResponse.WaitingCloseChannel;


import lnrpc.Rpc.GetInfoRequest;
import lnrpc.Rpc.GetInfoResponse;
import lnrpc.Rpc.*;


import invoicesrpc.InvoicesGrpc;
import invoicesrpc.InvoicesGrpc.InvoicesStub;
import lnrpc.LightningGrpc;
import lnrpc.LightningGrpc.LightningStub;



public class GRPCInterface {

    public GRPCInterface() {


    }
    public static String testString = "hello world";
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String encodeHexString(byte[] bytes){
            final char[] toDigits = "0123456789abcdef".toCharArray();
            int l = bytes.length;
            char[] out = new char[l << 1];

            int i = 0; for (int j = 0; i < l; ++i) {
                out[(j++)] = toDigits[((0xF0 & bytes[i]) >>> 4)];
                out[(j++)] = toDigits[(0xF & bytes[i])];
            }
            return new String(out);

    }
    private static String encodeBase64String(byte[] bytes){

        return Base64.encodeBase64(bytes).toString();

    }
    private static byte[] convertToBytes(String base64){
        return Base64.decodeBase64(base64.getBytes());
    }

    static class MacaroonCallCredential implements CallCredentials {
        private final String macaroon;


        MacaroonCallCredential(String macaroon) {
        this.macaroon = macaroon;
    }

    public void thisUsesUnstableApi() {}

    public void applyRequestMetadata(
            MethodDescriptor< ? , ? > methodDescriptor,
            Attributes attributes,
            Executor executor,
            final MetadataApplier metadataApplier
    ) {
        String authority = attributes.get(ATTR_AUTHORITY);
        System.out.println(authority);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Metadata headers = new Metadata();
                    Metadata.Key < String > macaroonKey = Metadata.Key.of("macaroon", Metadata.ASCII_STRING_MARSHALLER);
                    headers.put(macaroonKey, macaroon);
                    metadataApplier.apply(headers);
                } catch (Throwable e) {
                    metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                }
            }
        });
    }
}




    private static byte[] hexStringToByteArray(String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) +
                    Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
    private static X509Certificate convertToX509Cert(String certificateString) throws CertificateException {

        X509Certificate certificate = null;
        CertificateFactory cf = null;
        try {
            if (certificateString != null && !certificateString.trim().isEmpty()) {

                byte[] certificateData = hexStringToByteArray(certificateString);

                cf = CertificateFactory.getInstance("X509");
                certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));

            }
        } catch (CertificateException e) {
            throw new CertificateException(e);
        }
        return certificate;
    }

    private static SSLSocketFactory newSslSocketFactoryForCa(String certificateString) throws Exception {

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        X509Certificate cert = convertToX509Cert(certificateString);

        X500Principal principal = cert.getSubjectX500Principal();
        ks.setCertificateEntry(principal.getName("RFC2253"), cert);

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagerFactory.getTrustManagers(), null);
        return context.getSocketFactory();
    }



    private static LightningStub stub;

    private static InvoicesStub stubInvoices;



    public static void Connect(final String host, final int port, final String cert, final String macaroon, final CallbackInterface callback) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    OkHttpChannelBuilder builder = OkHttpChannelBuilder.forAddress(host, port)
                            .connectionSpec(new ConnectionSpec.Builder(OkHttpChannelBuilder.DEFAULT_CONNECTION_SPEC)
                                    .tlsVersions(ConnectionSpec.MODERN_TLS.tlsVersions().toArray(new TlsVersion[0]))
                                    .build());



                    if (cert != null) {
                        byte[] decoded = convertToBytes(cert);

                        String hexString = encodeHexString(decoded);

                        try {
                            builder.sslSocketFactory(newSslSocketFactoryForCa(hexString));
                        } catch (Exception e) {
                            System.out.println(e);
                            throw new RuntimeException(e);
                        }
                    }
                    ManagedChannel channel = builder.build();

                    stub = LightningGrpc
                            .newStub(channel)
                            .withCallCredentials(new MacaroonCallCredential(macaroon));

                    stubInvoices = InvoicesGrpc
                            .newStub(channel)
                            .withCallCredentials(new MacaroonCallCredential(macaroon));

                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", false);
                        json.put("response", "connected");


                        callback.eventFired(json.toString());
                    } catch (Exception e) {
                        callback.eventFired("");
                    }
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
        });
        thread.start();

    }




    public static void GetInfo(final CallbackInterface callback) throws IOException {

        stub.getInfo(GetInfoRequest.getDefaultInstance(), new StreamObserver < GetInfoResponse > () {
            @Override
            public void onNext(GetInfoResponse response) {
                JSONObject resJson = parseGetInfo(response);
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {

                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e) {

                }

                // ...
            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void ExportAllChannelBackups(final CallbackInterface callback) throws IOException {


        stub.exportAllChannelBackups(ChanBackupExportRequest.getDefaultInstance(), new StreamObserver < ChanBackupSnapshot > () {
            @Override
            public void onNext(ChanBackupSnapshot response) {
                try {

                    JSONObject resJson = new JSONObject();
                    resJson.put("multi_chan_backup", bytesToHex(response.getMultiChanBackup().getMultiChanBackup().toByteArray()));
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.toString());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }
            }
            @Override
            public void onCompleted() {

            }
        });
    }



    private static JSONArray parseGetTransactions(TransactionDetails response) {
        try {
            JSONArray resJson = new JSONArray();

            List < Transaction > transactions = response.getTransactionsList();

            for (int i = 0; i < transactions.size(); i++) {
                Transaction aTransaction = transactions.get(i);

                JSONObject aTransactionJSON = new JSONObject();
                aTransactionJSON.put("amount", aTransaction.getAmount());
                aTransactionJSON.put("tx_hash", aTransaction.getTxHash());
                aTransactionJSON.put("time_stamp", aTransaction.getTimeStamp());
                aTransactionJSON.put("num_confirmations", aTransaction.getNumConfirmations());

                try {

                    int addressCount = aTransaction.getDestAddressesCount();
                    JSONArray addressArray = new JSONArray();

                    for (int i2 = 0; i2 < addressCount; i2++) {

                        addressArray.put(aTransaction.getDestAddresses(i2));
                    }

                    aTransactionJSON.put("dest_addresses", addressArray);
                } catch (Exception e) {

                }

                resJson.put(aTransactionJSON);

            }
            return resJson;

        } catch (Exception e) {
            return new JSONArray();

        }

    }

    private static JSONArray parseListPayments(ListPaymentsResponse response) {
        try {
            JSONArray resJson = new JSONArray();

            List < Payment > payments = response.getPaymentsList();

            for (int i = 0; i < payments.size(); i++) {
                Payment aPayment = payments.get(i);
                JSONObject aPaymentJSON = new JSONObject();
                aPaymentJSON.put("payment_hash", aPayment.getPaymentHash());
                aPaymentJSON.put("value", aPayment.getValueSat());
                aPaymentJSON.put("creation_date", aPayment.getCreationDate());
                aPaymentJSON.put("payment_preimage", aPayment.getPaymentPreimage());
                aPaymentJSON.put("value_sat", aPayment.getValueSat());

                try {

                    int pathCount = aPayment.getPathCount();
                    JSONArray pathArray = new JSONArray();

                    for (int i2 = 0; i2 < pathCount; i2++) {

                        pathArray.put(aPayment.getPath(i2));
                    }

                    aPaymentJSON.put("path", pathArray);
                } catch (Exception e) {

                }

                resJson.put(aPaymentJSON);

            }


            return resJson;

        } catch (Exception e) {
            return new JSONArray();

        }

    }


    private static JSONArray parseListInvoices(ListInvoiceResponse response) {
        try {
            JSONArray resJson = new JSONArray();

            List < Invoice > invoices = response.getInvoicesList();

            for (int i = 0; i < invoices.size(); i++) {
                Invoice anInvoice = invoices.get(i);
                JSONObject anInvoiceJSON = new JSONObject();
                anInvoiceJSON.put("creation_date", anInvoice.getCreationDate());
                anInvoiceJSON.put("memo", anInvoice.getMemo());
                anInvoiceJSON.put("amt_paid", anInvoice.getAmtPaid()); //deprecated check api lnd
                anInvoiceJSON.put("value", anInvoice.getValue()); //deprecated check api lnd
                anInvoiceJSON.put("payment_request", anInvoice.getPaymentRequest());
                anInvoiceJSON.put("settled", anInvoice.getSettled());
                anInvoiceJSON.put("state", anInvoice.getState());
                anInvoiceJSON.put("r_hash", bytesToHex(anInvoice.getRHash().toByteArray()));

                resJson.put(anInvoiceJSON);

            }
            return resJson;

        } catch (Exception e) {
            return new JSONArray();

        }

    }


    private static JSONObject parseChannelBalance(ChannelBalanceResponse response) {
        try {
            JSONObject resJson = new JSONObject();
            resJson.put("balance", response.getBalance());
            resJson.put("pending_open_balance", response.getPendingOpenBalance());

            return resJson;

        } catch (Exception e) {

            return new JSONObject();
        }
    }


    private static JSONObject parseChannelGraph(ChannelGraph response) {
        try {
            JSONObject resJson = new JSONObject();


            try {

                int nodesCount = response.getNodesCount();
                JSONArray nodesArray = new JSONArray();

                for (int i2 = 0; i2 < nodesCount; i2++) {
                    JSONObject nodeObject = new JSONObject();
                    nodeObject.put("pub_key", response.getNodes(i2).getPubKey());

                    nodesArray.put(nodeObject);
                }
                resJson.put("nodes", nodesArray);
            } catch (Exception e) {

            }

            return resJson;
        } catch (Exception e) {
            return new JSONObject();
        }
    }


    private static JSONObject parseGetInfo(GetInfoResponse response) {
        try {
            JSONObject resJson = new JSONObject();
            resJson.put("identity_pubkey", response.getIdentityPubkey());
            resJson.put("num_active_channels", response.getNumActiveChannels());
            resJson.put("alias", response.getAlias());
            resJson.put("testnet", response.getTestnet());
            resJson.put("synced_to_chain", response.getSyncedToChain());
            resJson.put("block_height", response.getBlockHeight());


            try {

                int urisCount = response.getUrisCount();
                JSONArray urisArray = new JSONArray();

                for (int i2 = 0; i2 < urisCount; i2++) {

                    urisArray.put(response.getUris(i2));
                }

                resJson.put("uris", urisArray);
            } catch (Exception e) {

            }

            return resJson;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static void GetWalletBalance(final CallbackInterface callback) throws IOException {

        stub.walletBalance(WalletBalanceRequest.getDefaultInstance(), new StreamObserver < WalletBalanceResponse > () {
            @Override
            public void onNext(WalletBalanceResponse response) {

                try {
                    JSONObject resJson = new JSONObject();
                    resJson.put("total_balance", response.getTotalBalance());
                    resJson.put("confirmed_balance", response.getConfirmedBalance());
                    resJson.put("unconfirmed_balance", response.getUnconfirmedBalance());


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }
                // ...
            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void GetChannelBalance(final CallbackInterface callback) throws IOException {

        stub.channelBalance(ChannelBalanceRequest.getDefaultInstance(), new StreamObserver < ChannelBalanceResponse > () {
            @Override
            public void onNext(ChannelBalanceResponse response) {

                try {

                    JSONObject resJson = parseChannelBalance(response);
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }
                // ...
            }
            @Override
            public void onCompleted() {

            }
        });


    }


    public static void GetTransactions(final CallbackInterface callback) throws IOException {

        stub.getTransactions(GetTransactionsRequest.getDefaultInstance(), new StreamObserver < TransactionDetails > () {
            @Override
            public void onNext(TransactionDetails response) {
                try {
                    JSONArray resJson = parseGetTransactions(response);



                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {

                    }
                }

            }
            @Override
            public void onError(Throwable t) {

                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {

                }

                // ...
            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void ListPayments(final CallbackInterface callback) throws IOException {

        stub.listPayments(ListPaymentsRequest.getDefaultInstance(), new StreamObserver < ListPaymentsResponse > () {
            @Override
            public void onNext(ListPaymentsResponse response) {

                try {
                    JSONArray resJson = parseListPayments(response);

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }
            }
            @Override
            public void onCompleted() {

            }
        });

    }
    public static void ListInvoices(final CallbackInterface callback) throws IOException {

        ListInvoiceRequest.Builder request = ListInvoiceRequest.newBuilder();

        request.setReversed(true);

        stub.listInvoices(request.build(), new StreamObserver < ListInvoiceResponse > () {
            @Override
            public void onNext(ListInvoiceResponse response) {
                try {
                    JSONArray resJson = parseListInvoices(response);

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }
            }
            @Override
            public void onCompleted() {

            }
        });


    }
    public static void ListChannels(final CallbackInterface callback) throws IOException {


        stub.listChannels(ListChannelsRequest.getDefaultInstance(), new StreamObserver < ListChannelsResponse > () {
            @Override
            public void onNext(ListChannelsResponse response) {
                try {
                    JSONArray resJson = parseListChannels(response);


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {

                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }




    public static void PendingChannels(final CallbackInterface callback) throws IOException {

        stub.pendingChannels(PendingChannelsRequest.getDefaultInstance(), new StreamObserver < PendingChannelsResponse > () {
            @Override
            public void onNext(PendingChannelsResponse response) {
                try {
                    JSONObject resJSON = parsePendingChannels(response);



                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJSON);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void NewAddress(final String type, final CallbackInterface callback) throws IOException {
        AddressType addressType = AddressType.WITNESS_PUBKEY_HASH;
        if ("np2wkh".equals(type)) {
            addressType = AddressType.NESTED_PUBKEY_HASH;
        }


        NewAddressRequest.Builder req = NewAddressRequest.newBuilder().setType(addressType);
        stub.newAddress(req.build(), new StreamObserver < NewAddressResponse > () {
            @Override
            public void onNext(NewAddressResponse response) {

                try {

                    JSONObject resJson = new JSONObject();
                    resJson.put("address", response.getAddress());



                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void DecodePayReq(final String pay_req, final CallbackInterface callback) throws IOException {

        PayReqString payReqReq = PayReqString.newBuilder().setPayReq(pay_req).build();

        stub.decodePayReq(payReqReq, new StreamObserver < PayReq > () {
            @Override
            public void onNext(PayReq response) {

                try {

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parsePayReq(response));

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void SendPaymentSync(final String pay_req, final long amount, final CallbackInterface callback) throws IOException {
        SendRequest.Builder sendReqBuilder = SendRequest.newBuilder();

        sendReqBuilder.setPaymentRequest(pay_req);

        if (amount != -1) {
            sendReqBuilder.setAmt(amount);
        }

        stub.sendPaymentSync(sendReqBuilder.build(), new StreamObserver < SendResponse > () {
            @Override
            public void onNext(SendResponse response) {
                try {

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseSendPayment(response));

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void SendPayment(final String pay_req, final long amount, final CallbackInterface callback) throws IOException {
        SendRequest.Builder sendReqBuilder = SendRequest.newBuilder();

        sendReqBuilder.setPaymentRequest(pay_req);

        if (amount != -1) {
            sendReqBuilder.setAmt(amount);
        }


        StreamObserver < SendRequest > stream = stub.sendPayment(new StreamObserver < SendResponse > () {
            @Override
            public void onNext(SendResponse response) {
                try {

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseSendPayment(response));

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

        stream.onNext(sendReqBuilder.build());


    }

    public static void SendCoins(final long amount, final String address, final long fee, final CallbackInterface callback) throws IOException {
        SendCoinsRequest.Builder sendReqBuilder = SendCoinsRequest.newBuilder();
        sendReqBuilder.setAddr(address);

        if (amount == -1) {
            sendReqBuilder.setSendAll(true);
        } else {
            sendReqBuilder.setAmount(amount);
        }

        if (fee != -1) {
            sendReqBuilder.setSatPerByte(fee);
        }

        stub.sendCoins(sendReqBuilder.build(), new StreamObserver < SendCoinsResponse > () {
            @Override
            public void onNext(SendCoinsResponse response) {
                try {
                    JSONObject resJson = new JSONObject();
                    resJson.put("txid", response.getTxid());


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });
    }

    public static void ConnectPeer(final String pubKey, final String host, final CallbackInterface callback) throws IOException {
        LightningAddress.Builder lightningAddBuilder = LightningAddress.newBuilder().setPubkey(pubKey);
        lightningAddBuilder.setHost(host);
        ConnectPeerRequest connectPeerReq = ConnectPeerRequest.newBuilder().setAddr(lightningAddBuilder.build()).build();

        stub.connectPeer(connectPeerReq, new StreamObserver < ConnectPeerResponse > () {
            @Override
            public void onNext(ConnectPeerResponse response) {
                try {

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", response.toString());

                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void SettleInvoice(final String preimage, final CallbackInterface callback) throws IOException {
        SettleInvoiceMsg.Builder settleInvoiceMsg = SettleInvoiceMsg.newBuilder();
        settleInvoiceMsg.setPreimage(ByteString.copyFrom(hexStringToByteArray(preimage)));

        stubInvoices.settleInvoice(settleInvoiceMsg.build(), new StreamObserver < SettleInvoiceResp > () {
            @Override
            public void onNext(SettleInvoiceResp response) {

                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", "success");

                    callback.eventFired(json.toString());

                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });


    }


    public static void CancelInvoice(final String hash, final CallbackInterface callback) throws IOException {
        CancelInvoiceMsg.Builder cancelInvoiceMsg = CancelInvoiceMsg.newBuilder();
        cancelInvoiceMsg.setPaymentHash(ByteString.copyFrom(hexStringToByteArray(hash)));

        stubInvoices.cancelInvoice(cancelInvoiceMsg.build(), new StreamObserver < CancelInvoiceResp > () {
            @Override
            public void onNext(CancelInvoiceResp response) {

                try {
                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", "success");

                    callback.eventFired(json.toString());

                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });


    }


    public static void AddInvoice(final long amount, final long expiry, final String memo, final CallbackInterface callback) throws IOException {
        Invoice.Builder invoiceReq = Invoice.newBuilder();
        invoiceReq.setValue(amount);
        if (memo != null) {
            invoiceReq.setMemo(memo);
        }

        invoiceReq.setExpiry(expiry);

        stub.addInvoice(invoiceReq.build(), new StreamObserver < AddInvoiceResponse > () {
            @Override
            public void onNext(AddInvoiceResponse response) {

                try {
                    JSONObject resJson = new JSONObject();
                    resJson.put("payment_request", response.getPaymentRequest());
                    resJson.put("r_hash", bytesToHex(response.getRHash().toByteArray()));

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());

                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });


    }

    public static void AddHoldInvoice(final String hash, final long amount, final long expiry, final String memo, final CallbackInterface callback) throws IOException {
        AddHoldInvoiceRequest.Builder holdInvoiceRequest = AddHoldInvoiceRequest.newBuilder();

        holdInvoiceRequest.setHash(ByteString.copyFrom(hexStringToByteArray(hash)));
        holdInvoiceRequest.setValue(amount);

        if (memo != null) {
            holdInvoiceRequest.setMemo(memo);
        }

        holdInvoiceRequest.setExpiry(expiry);

        stubInvoices.addHoldInvoice(holdInvoiceRequest.build(), new StreamObserver < AddHoldInvoiceResp > () {
            @Override
            public void onNext(AddHoldInvoiceResp response) {

                try {
                    JSONObject resJson = new JSONObject();
                    resJson.put("payment_request", response.getPaymentRequest());


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", resJson);

                    callback.eventFired(json.toString());


                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });
    }


    public static void OpenChannel(final String pubKey, final long local_amount, boolean isPrivate, final CallbackInterface callback) throws IOException {

        OpenChannelRequest.Builder openChannelReq = OpenChannelRequest.newBuilder();
        openChannelReq.setNodePubkeyString(pubKey);

        openChannelReq.setNodePubkey(ByteString.copyFrom(hexStringToByteArray(pubKey)));
        openChannelReq.setLocalFundingAmount(local_amount);
        openChannelReq.setPrivate(isPrivate);

        stub.openChannel(openChannelReq.build(), new StreamObserver < OpenStatusUpdate > () {
            @Override
            public void onNext(OpenStatusUpdate response) {

                try {
                    System.out.println("logged " + response.toString());


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseOpenChannel(response));


                    callback.eventFired(json.toString());
                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void LookupInvoice(final String rhash, final CallbackInterface callback) throws IOException {

        PaymentHash.Builder req = PaymentHash.newBuilder();
        req.setRHashStr(rhash);
        stub.lookupInvoice(req.build(), new StreamObserver < Invoice > () {
            @Override
            public void onNext(Invoice response) {

                try {


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseInvoice(response));

                    callback.eventFired(json.toString());

                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void GetNodeInfo(final String pubkey, final CallbackInterface callback) throws IOException {

        System.out.println("start get node info");
        NodeInfoRequest.Builder req = NodeInfoRequest.newBuilder();
        req.setPubKey(pubkey);
        stub.getNodeInfo(req.build(), new StreamObserver < NodeInfo > () {
            @Override
            public void onNext(NodeInfo response) {

                try {


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseNodeInfo(response));


                    callback.eventFired(json.toString());



                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }

    public static void CloseChannel(final String txid, final int output, final boolean force, final CallbackInterface callback) throws IOException {
        CloseChannelRequest.Builder req = CloseChannelRequest.newBuilder();

        ChannelPoint.Builder cp = ChannelPoint.newBuilder();
        cp.setFundingTxidStr(txid);
        cp.setOutputIndex(output);
        req.setChannelPoint(cp.build());
        req.setForce(force);

        stub.closeChannel(req.build(), new StreamObserver < CloseStatusUpdate > () {
            @Override
            public void onNext(CloseStatusUpdate response) {

                try {


                    System.out.println("got close channel");


                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", parseCloseChannel(response));


                    callback.eventFired(json.toString());


                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {
                        callback.eventFired("");
                    }
                }
            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });



    }


    public static void SubscribeSingleInvoice(final String rhash, final CallbackInterface callback) throws IOException {

        SubscribeSingleInvoiceRequest.Builder request = SubscribeSingleInvoiceRequest.newBuilder();
        request.setRHash(ByteString.copyFrom(hexStringToByteArray(rhash)));

        stubInvoices.subscribeSingleInvoice(request.build(), new StreamObserver < Invoice > () {
            @Override
            public void onNext(Invoice response) {

                try {

                    JSONObject anInvoiceJSON = parseInvoice(response);

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", anInvoiceJSON);
                    callback.eventFired(json.toString());



                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        System.out.println("error " + e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {

                        callback.eventFired(e2.toString());
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }


    public static void SubscribeInvoices(final CallbackInterface callback) throws IOException {

        InvoiceSubscription request = InvoiceSubscription.getDefaultInstance();

        stub.subscribeInvoices(request, new StreamObserver < Invoice > () {
            @Override
            public void onNext(Invoice response) {

                try {

                    JSONObject anInvoiceJSON = parseInvoice(response);

                    JSONObject json = new JSONObject();
                    json.put("error", false);
                    json.put("response", anInvoiceJSON);
                    callback.eventFired(json.toString());



                } catch (Exception e) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("error", true);
                        json.put("response", e.toString());

                        System.out.println("error " + e.toString());

                        callback.eventFired(json.toString());
                    } catch (Exception e2) {

                        callback.eventFired(e2.toString());
                    }
                }

            }
            @Override
            public void onError(Throwable t) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("error", true);
                    json.put("response", t.getLocalizedMessage());

                    callback.eventFired(json.toString());
                } catch (Exception e2) {
                    callback.eventFired("");
                }

            }
            @Override
            public void onCompleted() {

            }
        });

    }



    private static JSONObject parseOpenChannel(final OpenStatusUpdate openStatusUpdate) {
        try {
            ChannelOpenUpdate channelOpenUpdate = openStatusUpdate.getChanOpen();
            PendingUpdate pendingUpdate = openStatusUpdate.getChanPending();
            //ConfirmationUpdate confirmationUpdate = openStatusUpdate.getConfirmation();



            ChannelPoint channelPoint = channelOpenUpdate.getChannelPoint();

            JSONObject channelPointJson = new JSONObject();
            channelPointJson.put("funding_txid", channelPoint.getFundingTxidStr());
            channelPointJson.put("output_index", channelPoint.getOutputIndex());

            JSONObject channelOpenUpdateJson = new JSONObject();
            channelOpenUpdateJson.put("channel_point", channelPointJson);

            JSONObject pendingUpdateJson = new JSONObject();
            pendingUpdateJson.put("txid", bytesToHex(pendingUpdate.getTxid().toByteArray()));
            pendingUpdateJson.put("output_index", pendingUpdate.getOutputIndex());

            JSONObject confirmationUpdateJson = new JSONObject();


            JSONObject jsonCombined = new JSONObject();
            jsonCombined.put("channel_open_update", channelOpenUpdateJson);
            jsonCombined.put("pending_update", pendingUpdateJson);
            jsonCombined.put("confirmation_update", confirmationUpdateJson);

            return jsonCombined;
        } catch (Exception e) {

        }

        return new JSONObject();
    }
    private static JSONObject parseInvoice(Invoice anInvoice) {
        try {
            JSONObject anInvoiceJSON = new JSONObject();
            anInvoiceJSON.put("creation_date", anInvoice.getCreationDate());
            anInvoiceJSON.put("memo", anInvoice.getMemo()); //deprecated check api lnd
            anInvoiceJSON.put("amt_paid", anInvoice.getAmtPaid()); //deprecated check api lnd
            anInvoiceJSON.put("payment_request", anInvoice.getPaymentRequest());
            anInvoiceJSON.put("settled", anInvoice.getSettled());
            anInvoiceJSON.put("state", anInvoice.getState());
            anInvoiceJSON.put("r_hash", bytesToHex(anInvoice.getRHash().toByteArray()));
            anInvoiceJSON.put("value", anInvoice.getValue());
            return anInvoiceJSON;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



    public static byte[] makeGenerateSeedRequest() throws IOException {

        return GenSeedRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeStopDaemonRequest() throws IOException {

        return StopRequest.getDefaultInstance().toByteArray();

    }
    public static String parseGenerateSeedResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            GenSeedResponse response = GenSeedResponse.parseFrom(data);

            try {

                int mnemonicCount = response.getCipherSeedMnemonicCount();
                JSONArray mnemonicArray = new JSONArray();

                for (int i = 0; i < mnemonicCount; i++) {

                    mnemonicArray.put(response.getCipherSeedMnemonic(i));
                }

                JSONObject seedJson = new JSONObject();
                seedJson.put("cipherSeedMnemonic", mnemonicArray);

                JSONObject json = new JSONObject();
                json.put("error", false);
                json.put("response", seedJson);

                return json.toString();


            } catch (Exception e) {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();

            }
        } catch (Exception e) {
            return "error" + e.getLocalizedMessage();
        }
    }

    public static byte[] makeUnlockWalletRequest(String password) throws IOException {


        System.out.println("NAYUTATA made it here"+password+" "+testString);
        try {

            UnlockWalletRequest.Builder req = UnlockWalletRequest.newBuilder();

            ByteString passwordBS = ByteString.copyFromUtf8(password);

            req.setWalletPassword(passwordBS);
            //req.setRecoveryWindow(250);

            return req.build().toByteArray();

        }
        catch(Exception e){
            e.printStackTrace();;
        }
        return null;

    }

    public static byte[] makeCreateWalletRequest(String passphrase, String password, int recoveryWindow, String channelBackup) throws IOException {

        InitWalletRequest.Builder req = InitWalletRequest.newBuilder();

        String[] passphraseArray = passphrase.split(" ");


        for (int i = 0; i < passphraseArray.length; i++) {

            req.addCipherSeedMnemonic(passphraseArray[i]);

        }
        System.out.println("starting");
        ByteString passwordBS = ByteString.copyFrom(password, "UTF-8");


        req.setWalletPassword(passwordBS);
        if (recoveryWindow != -1) {
            req.setRecoveryWindow(recoveryWindow);
        }

        if (channelBackup != "") {
            ChanBackupSnapshot.Builder snapShotBuilder = ChanBackupSnapshot.newBuilder();
            MultiChanBackup.Builder multiChanBuilder = MultiChanBackup.newBuilder();
             multiChanBuilder.setMultiChanBackup(ByteString.copyFrom(convertToBytes(channelBackup)));
             snapShotBuilder.setMultiChanBackup(multiChanBuilder.build());
            req.setChannelBackups(snapShotBuilder.build());

        }

        return req.build().toByteArray();

    }

    public static byte[] makeChannelGraphRequest() throws IOException {


        return ChannelGraphRequest.getDefaultInstance().toByteArray();

    }


    public static byte[] makeGetInfoRequest() throws IOException {

        return GetInfoRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeListPaymentsRequest() throws IOException {

        return ListPaymentsRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeListInvoiceRequest() throws IOException {

        return ListInvoiceRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeListChannelsRequest() throws IOException {

        return ListChannelsRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makePendingChannelsRequest() throws IOException {

        return PendingChannelsRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeLookupInvoiceRequest(String rhash) throws IOException {

        PaymentHash.Builder req = PaymentHash.newBuilder();
        req.setRHashStr(rhash);
        return req.build().toByteArray();

    }

    public static byte[] makeGetNodeInfoRequest(String pubKey) throws IOException {

        NodeInfoRequest.Builder req = NodeInfoRequest.newBuilder();
        req.setPubKey(pubKey);
        return req.build().toByteArray();

    }

    public static byte[] makeGetTransactionsRequest() throws IOException {

        return GetTransactionsRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeGetChannelBalanceRequest() throws IOException {

        return ChannelBalanceRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeWalletBalanceRequest() throws IOException {

        return WalletBalanceRequest.getDefaultInstance().toByteArray();

    }

    public static byte[] makeSubscribeInvoicesRequest() throws IOException {

        return InvoiceSubscription.getDefaultInstance().toByteArray();

    }

    public static byte[] makeSubscribeSingleInvoiceRequest(String rhash) throws IOException {

        SubscribeSingleInvoiceRequest.Builder subscribeSingleInvoiceRequest = SubscribeSingleInvoiceRequest.newBuilder();
        subscribeSingleInvoiceRequest.setRHash(ByteString.copyFrom(hexStringToByteArray(rhash)));

        return subscribeSingleInvoiceRequest.build().toByteArray();

    }


    public static byte[] makePayReqString(String payReq) throws IOException {

        return PayReqString.newBuilder().setPayReq(payReq).build().toByteArray();

    }
    private static JSONObject parsePayReq(PayReq response) {
        try {
            JSONObject resJson = new JSONObject();
            resJson.put("destination", response.getDestination());
            resJson.put("payment_hash", response.getPaymentHash());
            resJson.put("num_satoshis", response.getNumSatoshis());
            resJson.put("timestamp", response.getTimestamp());
            resJson.put("expiry", response.getExpiry());
            resJson.put("description", response.getDescription());
            resJson.put("description_hash", response.getDescriptionHash());
            resJson.put("fallback_addr", response.getFallbackAddr());
            resJson.put("cltv_expiry", response.getCltvExpiry());

            return resJson;

        } catch (Exception e) {

        }
        return new JSONObject();
    }
    public static String parsePayReqResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            PayReq response = PayReq.parseFrom(data);

            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", parsePayReq(response));

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }



    public static String parseSettleInvoiceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            SettleInvoiceResp response = SettleInvoiceResp.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("success", true);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseCancelInvoiceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            CancelInvoiceResp response = CancelInvoiceResp.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("success", true);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseAddHoldInvoiceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            AddHoldInvoiceResp response = AddHoldInvoiceResp.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("payment_request", response.getPaymentRequest());


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static byte[] makeSettleInvoiceMsg(final String preimage) throws IOException {

        SettleInvoiceMsg.Builder settleInvoiceMsg = SettleInvoiceMsg.newBuilder();
        settleInvoiceMsg.setPreimage(ByteString.copyFrom(hexStringToByteArray(preimage)));

        return settleInvoiceMsg.build().toByteArray();

    }


    public static byte[] makeCancelInvoiceMsg(final String rhash) throws IOException {

        CancelInvoiceMsg.Builder cancelInvoiceMsg = CancelInvoiceMsg.newBuilder();
        cancelInvoiceMsg.setPaymentHash(ByteString.copyFrom(hexStringToByteArray(rhash)));

        return cancelInvoiceMsg.build().toByteArray();

    }



    public static byte[] makeAddHoldInvoiceRequest(final String hash, final long amount, final long expiry, final String memo) throws IOException {


        AddHoldInvoiceRequest.Builder holdInvoiceRequest = AddHoldInvoiceRequest.newBuilder();

        holdInvoiceRequest.setHash(ByteString.copyFrom(hexStringToByteArray(hash)));
        holdInvoiceRequest.setValue(amount);

        if (memo != null) {
            holdInvoiceRequest.setMemo(memo);
        }

        holdInvoiceRequest.setExpiry(expiry);

        return holdInvoiceRequest.build().toByteArray();

    }


    public static byte[] makeAddInvoiceRequest(final long amount, final long expiry, final String memo) throws IOException {

        Invoice.Builder invoiceReq = Invoice.newBuilder();
        invoiceReq.setValue(amount);
        if (memo != null) {
            invoiceReq.setMemo(memo);
        }

        invoiceReq.setExpiry(expiry);

        return invoiceReq.build().toByteArray();

    }


    public static String parseSubscribeInvoicesResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            Invoice response = Invoice.parseFrom(data);

            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", parseInvoice(response));

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseWalletBalanceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            WalletBalanceResponse response = WalletBalanceResponse.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("total_balance", response.getTotalBalance());
            resJson.put("confirmed_balance", response.getConfirmedBalance());
            resJson.put("unconfirmed_balance", response.getUnconfirmedBalance());

            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseGetChannelBalanceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            ChannelBalanceResponse response = ChannelBalanceResponse.parseFrom(data);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", parseChannelBalance(response));

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseAddInvoiceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            AddInvoiceResponse response = AddInvoiceResponse.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("payment_request", response.getPaymentRequest());
            resJson.put("r_hash", bytesToHex(response.getRHash().toByteArray()));

            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseExportChannelResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            ChanBackupSnapshot response = ChanBackupSnapshot.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("multi_chan_backup", encodeBase64String(response.getMultiChanBackup().getMultiChanBackup().toByteArray()));
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseGetInfoResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            GetInfoResponse response = GetInfoResponse.parseFrom(data);


            JSONObject resJson = parseGetInfo(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseChannelGraphRes(String res) {

        byte[] data = convertToBytes(res);
        try {
            ChannelGraph response = ChannelGraph.parseFrom(data);


            JSONObject resJson = parseChannelGraph(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseListInvoicesResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            ListInvoiceResponse response = ListInvoiceResponse.parseFrom(data);


            JSONArray resJson = parseListInvoices(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }


    public static String parseListPaymentsResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            ListPaymentsResponse response = ListPaymentsResponse.parseFrom(data);


            JSONArray resJson = parseListPayments(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseGetTransactionsResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            TransactionDetails response = TransactionDetails.parseFrom(data);


            JSONArray resJson = parseGetTransactions(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }
    public static String parseConnectPeerResponse(String res) {


        byte[] data = convertToBytes(res);
        try {
            ConnectPeerResponse response = ConnectPeerResponse.parseFrom(data);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", response.toString());

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }
    }


    public static String parseOpenChannelResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            OpenStatusUpdate response = OpenStatusUpdate.parseFrom(data);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", response);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static byte[] makeExportAllChannelBackupsRequest() throws IOException {


        return ChanBackupExportRequest.getDefaultInstance().toByteArray();

    }


    public static byte[] makeSendCoinsRequest(final long amount, final String address, final long fee) throws IOException {

        SendCoinsRequest.Builder sendReqBuilder = SendCoinsRequest.newBuilder();
        sendReqBuilder.setAddr(address);
        if (amount == -1) {
            sendReqBuilder.setSendAll(true);
        } else {
            sendReqBuilder.setAmount(amount);
        }

        if (fee != -1) {
            sendReqBuilder.setSatPerByte(fee);
        }

        return sendReqBuilder.build().toByteArray();

    }


    public static byte[] makeSendPaymentRequest(final String pay_req, final long amount) throws IOException {

        SendRequest.Builder sendReqBuilder = SendRequest.newBuilder();

        sendReqBuilder.setPaymentRequest(pay_req);

        if (amount != -1) {
            sendReqBuilder.setAmt(amount);
        }

        return sendReqBuilder.build().toByteArray();
    }

    public static byte[] makeCloseChannelRequest(final String txid, final int output, final boolean force) throws IOException {

        CloseChannelRequest.Builder req = CloseChannelRequest.newBuilder();

        ChannelPoint.Builder cp = ChannelPoint.newBuilder();
        cp.setFundingTxidStr(txid);
        cp.setOutputIndex(output);
        req.setChannelPoint(cp.build());
        req.setForce(force);

        return req.build().toByteArray();

    }

    public static byte[] makeConnectPeerRequest(final String pubKey, final String host) throws IOException {

        LightningAddress.Builder lightningAddBuilder = LightningAddress.newBuilder().setPubkey(pubKey);
        lightningAddBuilder.setHost(host);
        return ConnectPeerRequest.newBuilder().setAddr(lightningAddBuilder.build()).build().toByteArray();
    }

    public static byte[] makeOpenChannelRequest(final String pubKey, final long local_amount, boolean isPrivate) throws IOException {

        OpenChannelRequest.Builder openChannelReq = OpenChannelRequest.newBuilder();
        openChannelReq.setNodePubkeyString(pubKey);
        openChannelReq.setPrivate(isPrivate);

        openChannelReq.setNodePubkey(ByteString.copyFrom(hexStringToByteArray(pubKey)));
        openChannelReq.setLocalFundingAmount(local_amount);

        return openChannelReq.build().toByteArray();
    }

    public static byte[] makeNewAddressRequest(final String type) throws IOException {

        AddressType addressType = AddressType.WITNESS_PUBKEY_HASH;
        if ("np2wkh".equals(type)) {
            System.out.println("setting address type " + type);
            addressType = AddressType.NESTED_PUBKEY_HASH;
        } else {

            System.out.println("not setting address type " + type);
        }


        NewAddressRequest.Builder req = NewAddressRequest.newBuilder().setType(addressType);

        return req.build().toByteArray();
    }

    public static String parseSendCoinsResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            SendCoinsResponse response = SendCoinsResponse.parseFrom(data);


            JSONObject resJson = new JSONObject();
            resJson.put("txid", response.getTxid());


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }


    }

    private static JSONObject parseSendPayment(SendResponse response) {
        try {
            JSONObject resJson = new JSONObject();
            resJson.put("payment_error", response.getPaymentError());
            resJson.put("payment_preimage", bytesToHex(response.getPaymentPreimage().toByteArray()));
            resJson.put("payment_route", response.getPaymentRoute().toString());
            return resJson;


        } catch (Exception e) {

        }
        return new JSONObject();
    }
    public static String parseCloseChannelResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            CloseStatusUpdate response = CloseStatusUpdate.parseFrom(data);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", parseCloseChannel(response));

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }
    static JSONObject getPendingChannelJSON(PendingChannel pendingChannel) {

        try {
            JSONObject pendingChannelJSON = new JSONObject();

            pendingChannelJSON.put("remote_node_pub", pendingChannel.getRemoteNodePub());
            pendingChannelJSON.put("channel_point", pendingChannel.getChannelPoint());
            pendingChannelJSON.put("capacity", pendingChannel.getCapacity());
            pendingChannelJSON.put("local_balance", pendingChannel.getLocalBalance());
            pendingChannelJSON.put("remote_balance", pendingChannel.getRemoteBalance());

            return pendingChannelJSON;
        } catch (Exception e) {

            return null;
        }
    }
    private static JSONObject parsePendingChannels(PendingChannelsResponse response) {
        try {
            JSONObject resJSON = new JSONObject();
            resJSON.put("total_limbo_balance", response.getTotalLimboBalance());
            JSONArray pendingOpenChannelsArray = new JSONArray();

            List < PendingOpenChannel > pendingOpenChannels = response.getPendingOpenChannelsList();

            for (int i = 0; i < pendingOpenChannels.size(); i++) {
                PendingOpenChannel aPendingOpenChannels = pendingOpenChannels.get(i);
                JSONObject aPendingOpenChannelJSON = new JSONObject();
                aPendingOpenChannelJSON.put("channel", getPendingChannelJSON(aPendingOpenChannels.getChannel()));
                aPendingOpenChannelJSON.put("confirmation_height", aPendingOpenChannels.getConfirmationHeight());
                aPendingOpenChannelJSON.put("commit_fee", aPendingOpenChannels.getCommitFee());
                aPendingOpenChannelJSON.put("commit_weight", aPendingOpenChannels.getCommitWeight());
                aPendingOpenChannelJSON.put("fee_per_kw", aPendingOpenChannels.getFeePerKw());

                pendingOpenChannelsArray.put(aPendingOpenChannelJSON);

            }

            resJSON.put("pending_open_channels", pendingOpenChannelsArray);


            JSONArray pendingClosingChannelsArray = new JSONArray();

            List < ClosedChannel > pendingClosingChannels = response.getPendingClosingChannelsList();

            System.out.println("pending size");
            System.out.println(pendingClosingChannels.size());
            for (int i = 0; i < pendingClosingChannels.size(); i++) {

                ClosedChannel aClosedChannel = pendingClosingChannels.get(i);
                System.out.println(aClosedChannel.toString());
                JSONObject aClosedChannelJSON = new JSONObject();
                aClosedChannelJSON.put("channel", getPendingChannelJSON(aClosedChannel.getChannel()));
                aClosedChannelJSON.put("closing_txid", aClosedChannel.getClosingTxid());
                pendingClosingChannelsArray.put(aClosedChannelJSON);

            }

            resJSON.put("pending_closing_channels", pendingClosingChannelsArray);


            JSONArray pendingForceClosingChannelsArray = new JSONArray();

            List < ForceClosedChannel > pendingForceClosingChannels = response.getPendingForceClosingChannelsList();

            for (int i = 0; i < pendingForceClosingChannels.size(); i++) {
                ForceClosedChannel aForceClosedChannel = pendingForceClosingChannels.get(i);
                JSONObject aForceClosedChannelJSON = new JSONObject();
                aForceClosedChannelJSON.put("channel", getPendingChannelJSON(aForceClosedChannel.getChannel()));
                aForceClosedChannelJSON.put("closing_txid", aForceClosedChannel.getClosingTxid());
                aForceClosedChannelJSON.put("limbo_balance", aForceClosedChannel.getLimboBalance());
                aForceClosedChannelJSON.put("maturity_height", aForceClosedChannel.getMaturityHeight());
                aForceClosedChannelJSON.put("blocks_til_maturity", aForceClosedChannel.getBlocksTilMaturity());
                aForceClosedChannelJSON.put("recovered_balance", aForceClosedChannel.getRecoveredBalance());
                pendingForceClosingChannelsArray.put(aForceClosedChannelJSON);

            }

            resJSON.put("pending_force_closing_channels", pendingForceClosingChannelsArray);




            JSONArray waitingCloseChannelsArray = new JSONArray();

            List < WaitingCloseChannel > waitingCloseChannels = response.getWaitingCloseChannelsList();

            for (int i = 0; i < waitingCloseChannels.size(); i++) {
                WaitingCloseChannel aWaitingCloseChannel = waitingCloseChannels.get(i);
                JSONObject aWaitingCloseChannelJSON = new JSONObject();
                aWaitingCloseChannelJSON.put("channel", getPendingChannelJSON(aWaitingCloseChannel.getChannel()));
                aWaitingCloseChannelJSON.put("limbo_balance", aWaitingCloseChannel.getLimboBalance());


                waitingCloseChannelsArray.put(aWaitingCloseChannelJSON);

            }

            resJSON.put("waiting_close_channels", waitingCloseChannelsArray);

            return resJSON;

        } catch (Exception e) {
            return new JSONObject();
        }


    }
    public static String parseSendPaymentResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            SendResponse response = SendResponse.parseFrom(data);


            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", parseSendPayment(response));

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }


    public static String parseNewAddressResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            NewAddressResponse response = NewAddressResponse.parseFrom(data);

            JSONObject resJson = new JSONObject();
            resJson.put("address", response.getAddress());

            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }


    public static String parseLookupInvoiceResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            Invoice response = Invoice.parseFrom(data);


            JSONObject resJson = parseInvoice(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    public static String parseGetNodeInfoResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            NodeInfo response = NodeInfo.parseFrom(data);


            JSONObject resJson = parseNodeInfo(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }


    public static String parseListChannelsResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            ListChannelsResponse response = ListChannelsResponse.parseFrom(data);


            JSONArray resJson = parseListChannels(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }

    private static JSONObject parseCloseChannel(CloseStatusUpdate closeStatusUpdate) {
        try {

            ChannelCloseUpdate channelCloseUpdate = closeStatusUpdate.getChanClose();
            PendingUpdate pendingUpdate = closeStatusUpdate.getClosePending();
            //ConfirmationUpdate confirmationUpdate = closeStatusUpdate.getConfirmation();

            JSONObject chanCloseJSON = new JSONObject();
            chanCloseJSON.put("closing_txid", bytesToHex(channelCloseUpdate.getClosingTxid().toByteArray()));
            chanCloseJSON.put("success", channelCloseUpdate.getSuccess());

            JSONObject pendingUpdateJson = new JSONObject();
            pendingUpdateJson.put("txid", bytesToHex(pendingUpdate.getTxid().toByteArray()));
            pendingUpdateJson.put("output_index", pendingUpdate.getOutputIndex());

            JSONObject confirmationUpdateJson = new JSONObject();
            //confirmationUpdateJson.put("block_height", confirmationUpdate.getBlockHeight());
            //confirmationUpdateJson.put("num_confs_left", confirmationUpdate.getNumConfsLeft());
            //confirmationUpdateJson.put("block_sha", bytesToHex(confirmationUpdate.getBlockSha().toByteArray()));



            JSONObject jsonCombined = new JSONObject();
            jsonCombined.put("chan_close", chanCloseJSON);
            jsonCombined.put("pending_update", pendingUpdateJson);
            jsonCombined.put("confirmation_update", confirmationUpdateJson);

            return jsonCombined;

        } catch (Exception e) {

        }
        return new JSONObject();
    }
    private static JSONObject parseNodeInfo(NodeInfo nodeInfo) {
        try {
            JSONObject nodeInfoJson = new JSONObject();
            nodeInfoJson.put("alias", nodeInfo.getNode().getAlias());


            JSONObject nodeJson = new JSONObject();
            nodeJson.put("node", nodeInfoJson);

            return nodeJson;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
    public static String parsePendingChannelsResponse(String res) {

        byte[] data = convertToBytes(res);
        try {
            PendingChannelsResponse response = PendingChannelsResponse.parseFrom(data);


            JSONObject resJson = parsePendingChannels(response);
            JSONObject json = new JSONObject();
            json.put("error", false);
            json.put("response", resJson);

            return json.toString();


        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("error", true);
                json.put("response", e.getLocalizedMessage());
                return json.toString();
            } catch (Exception e2) {
                System.out.println(e2);
                return "";
            }

        }

    }




    private static JSONArray parseListChannels(ListChannelsResponse response) {
        try {
            JSONArray resJson = new JSONArray();

            List < Channel > channels = response.getChannelsList();

            for (int i = 0; i < channels.size(); i++) {
                Channel aChannel = channels.get(i);

                JSONObject aChannelJSON = new JSONObject();
                aChannelJSON.put("active", aChannel.getActive());
                aChannelJSON.put("remote_pubkey", aChannel.getRemotePubkey());
                aChannelJSON.put("channel_point", aChannel.getChannelPoint());
                aChannelJSON.put("chan_id", aChannel.getChanId());
                aChannelJSON.put("capacity", aChannel.getCapacity());

                aChannelJSON.put("local_balance", aChannel.getLocalBalance());
                aChannelJSON.put("remote_balance", aChannel.getRemoteBalance());

                resJson.put(aChannelJSON);

            }
            return resJson;
        } catch (Exception e) {
            return new JSONArray();
        }
    }

}*/