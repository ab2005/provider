// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// this class reports data to the seagate servers

// put reports on a queue and push it to the server when it is connected

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.seagate.alto.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SeagateReporter implements IMetricsReporter{

    private static final String TAG = LogUtils.makeTag(SeagateReporter.class);

    private BlockingQueue<SeagateReport> mQueue = new ArrayBlockingQueue<SeagateReport>(1024);

    private static final int WAIT_MINUTES = 1;      // time between dump sessions

    private final boolean TESTING = false;

    private final Context mContext;

    public SeagateReporter(Context context) {
        Log.d(TAG, "Construct");

        mContext = context;

        // use the Android ID to generate a client UUID
        // http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id

        String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        BigInteger bigId = new BigInteger(androidId, 16);
        long top = bigId.shiftRight(64).longValue(); // should be 0
        long bottom = bigId.longValue();
        UUID clientUUID = new UUID(top, bottom);

        Consumer consumer = new Consumer(mQueue, clientUUID.toString());
        new Thread(consumer).start();

        // used for testing
        if (TESTING) {
            TestProducer tester = new TestProducer(mQueue);
            new Thread(tester).start();
        }

        disableSSLCertificateChecking();
    }


    private static class SeagateReport {
        IMetricsEvent mEvent;
        long mStart;

        public SeagateReport(IMetricsEvent event, long start) {
            mEvent = event;
            mStart = start;
        }
    }


    @Override
    public void reportEvent(IMetricsEvent metricsEvent, long start) {

        // queue up the report
        if (!mQueue.offer(new SeagateReport(metricsEvent, start))) {
            Log.e(TAG, "queue is full");
        }
    }

    @Override
    public void flush() {
        // make sure the data goes out
    }

    // the consumer sends things to our service
    // this consumer waits for connectivity before
    // sending items to the service

    private static class Consumer implements Runnable {

        public static final String REQUEST_TYPE = "request_type";
        public static final String REQUEST_TIMESTAMP = "request_ts";
        public static final String ACCOUNT_ID = "account_id";
        public static final String CLIENT_ID = "client_id";
        public static final String ACTIVITY_ID = "activity_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String HEADER = "header";
        public static final String PAYLOAD = "payload";

        protected BlockingQueue<SeagateReport> queue = null;
        private String mClientId;

        public Consumer(BlockingQueue<SeagateReport> queue, String clientId) {
            this.queue = queue;
            this.mClientId = clientId;
        }

        @Override
        public void run() {

            while (true) {

                if (!queue.isEmpty()) {
                    sendReport();
                }

                // wait a bit then blast again
                try {
                    Thread.sleep(1000 * 60 * WAIT_MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private JSONObject makeReport() {
            JSONObject report = new JSONObject();

            try {
                JSONObject header = new JSONObject();

                header.put(REQUEST_TYPE, "LyvePhotosActivity");
                header.put(REQUEST_TIMESTAMP, System.currentTimeMillis());
                header.put(ACCOUNT_ID, "94025");

                header.put(CLIENT_ID, mClientId);

                JSONArray eventArray = new JSONArray();

                while (!queue.isEmpty()) {
                    try {
                        SeagateReport sr = queue.take();

                        if (sr != null) {
                            JSONObject event = new JSONObject();
                            event.put(ACTIVITY_ID, sr.mEvent.getEventValue());
                            event.put(TIMESTAMP, sr.mStart);
                            eventArray.put(event);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                report.put(HEADER, header);
                report.put(PAYLOAD, eventArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return report;
        }

        private void sendReport() {

            final String DEVENDPOINT = "https://datacollection.dev.blackpearlsystems.net/datacollection/rest/v1/noauth/structured/";
//            final String ENDPOINT = "https://datacollection.dogfood.blackpearlsystems.net/datacollection/rest/v1/noauth/structured/";

            BufferedWriter writer = null;
            OutputStream os = null;
            HttpsURLConnection conn = null;

            try {

                // post json to the endpoint
                URL url = new URL(DEVENDPOINT);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                os = conn.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                JSONObject report = makeReport();
                Log.d(TAG, report.toString(4));
                writer.write(report.toString());
                writer.flush();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "response: " + responseCode);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // borrowed from arcus
    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    // used to generate and put test events in the queue
    private static class TestProducer implements Runnable {

        Random random;
        protected BlockingQueue<SeagateReport> queue = null;

        public TestProducer(BlockingQueue<SeagateReport> queue) {

            this.queue = queue;
            random = new Random();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    // randomly generate a report and post it to the queue
                    AltoMetricsEvent ame = getRandomEvent();
                    SeagateReport sr = new SeagateReport(ame, System.currentTimeMillis());
                    queue.offer(sr);

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private AltoMetricsEvent getRandomEvent() {
            AltoMetricsEvent[] events = AltoMetricsEvent.values();
            int indx = random.nextInt(events.length);
            AltoMetricsEvent result = events[indx];
            return result;
        }
    }


}
