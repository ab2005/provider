// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// this class reports data to the seagate servers

// put reports on a queue and push it to the server when it is connected

import android.util.Log;

import com.seagate.alto.utils.LogUtils;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SeagateReporter implements IMetricsReporter{

    private static final String TAG = LogUtils.makeTag(SeagateReporter.class);

    private BlockingQueue<SeagateReport> mQueue = new ArrayBlockingQueue<SeagateReport>(1024);

    private static final int WAIT_MINUTES = 1;      // time between dump sessions

    public SeagateReporter() {
        Log.d(TAG, "Construct");

        Consumer consumer = new Consumer(mQueue);
        new Thread(consumer).start();

        // used for testing
        TestProducer tester = new TestProducer(mQueue);
        new Thread(tester).start();

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

                while (!queue.isEmpty()) {
                    try {
                        SeagateReport sr = queue.take();

                        // wait once a minute to blast these things over to the service
                        if (sr != null) {
                            Log.d(TAG, "report: " + sr.mEvent.getEventName() + " when: " + sr.mStart);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // wait a bit then blast again
                try {
                    Thread.sleep(1000 * 60 * WAIT_MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


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
