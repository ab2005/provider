// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// add a class header comment here

import android.content.Context;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.seagate.alto.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MixpanelReporter implements IMetricsReporter{

    private static final String TAG = LogUtils.makeTag(MixpanelReporter.class);

    private MixpanelAPI mMixpanel;
    private Context mContext;

    public MixpanelReporter(Context context) {
        Log.d(TAG, "construct");
        mContext = context;

        // initialize MixPanel, this is frank's test token
        final String mixPanelToken = "f9d1b110c52d26def75e7313eac0fe97";

        // TODO: this is slooow, do it in the background
        mMixpanel = MixpanelAPI.getInstance(mContext, mixPanelToken);

    }


    @Override
    public void reportEvent(IMetricsEvent metricsEvent, long start, long duration) {

        Log.d(TAG, "report: " + metricsEvent.getEventName() + " start: " + start + " duration: " + duration);

        JSONObject props = new JSONObject();
        try {
            props.put("platform", "Android");
            // TODO: DATA-1080
//            props.put(ACCOUNT_IDENTIFIER, accountId);
//            props.put(AGENT_IDENTIFIER, agentId);
            if (duration > 0) {
                props.put("duration", duration);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mMixpanel.track(metricsEvent.getEventName(), props);

    }

    @Override
    public void flush() {
        Log.d(TAG, "flush");

        mMixpanel.flush();
    }
}
