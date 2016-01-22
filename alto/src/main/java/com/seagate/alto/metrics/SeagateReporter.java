// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// this class reports data to the seagate servers

// put reports on a queue and push it to the server when it is connected

import android.util.Log;

import com.seagate.alto.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SeagateReporter implements IMetricsReporter{

    private static final String TAG = LogUtils.makeTag(SeagateReporter.class);

    private BlockingQueue<SeagateReport> mQueue = new ArrayBlockingQueue<SeagateReport>(1024);

    private static final int WAIT_MINUTES = 1;      // time between dump sessions

    private final boolean TESTING = true;

    public SeagateReporter() {
        Log.d(TAG, "Construct");

        Consumer consumer = new Consumer(mQueue);
        new Thread(consumer).start();

        // used for testing
        if (TESTING) {
            TestProducer tester = new TestProducer(mQueue);
            new Thread(tester).start();
        }
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

        protected BlockingQueue<SeagateReport> queue = null;

        public Consumer(BlockingQueue<SeagateReport> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {

            while (true) {

                if (!queue.isEmpty()) {
                    uploadData();
                }

                // wait a bit then blast again
                try {
                    Thread.sleep(1000 * 60 * WAIT_MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }

        private void uploadData() {
            JSONObject report = new JSONObject();

            try {
                report.put("account id", 94025);
                report.put("device id", 42566);
                report.put("platform", "android");


                JSONArray eventArray = new JSONArray();

                while (!queue.isEmpty()) {
                    try {
                        SeagateReport sr = queue.take();

                        if (sr != null) {
                            JSONArray event = new JSONArray();
                            event.put(sr.mEvent.getEventValue());
                            event.put(sr.mStart);

                            eventArray.put(event);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                report.put("events", eventArray);


                // push JSON to server HERE
                upload(report);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void upload(JSONObject report) {

            final String ENDPOINT = "http://datacollection.dogfood.blackpearlsystems.net/datacollection/rest/v1/noauth/structured/";

//            try {
//                Log.d(TAG, report.toString(4));
//
//                // post json to the endpoint
//                URL url = new URL(ENDPOINT);
////                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
////                conn.setReadTimeout(10000);
////                conn.setConnectTimeout(15000);
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
////                List<NameValuePair> params = new ArrayList<NameValuePair>();
////                params.add(new BasicNameValuePair("firstParam", paramValue1));
////                params.add(new BasicNameValuePair("secondParam", paramValue2));
////                params.add(new BasicNameValuePair("thirdParam", paramValue3));
//
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, "UTF-8"));
////                writer.write(getQuery(params));
//                writer.write(report.toString());
//                writer.flush();
//                writer.close();
//                os.close();
//                conn.connect();
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


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
