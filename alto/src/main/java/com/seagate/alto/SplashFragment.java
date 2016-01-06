// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// fragment to display splash and authentication

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.seagate.alto.utils.LogUtils;

public class SplashFragment extends Fragment {

    private static final String TAG = LogUtils.makeTag(SplashFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.splash, container, false);

        Button startButton = (Button) view.findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate login
                Log.d(TAG, "start clicked");

                // switch to the main fragment
                if (getActivity() instanceof IContentSwitcher) {
                    ((IContentSwitcher) getActivity()).switchToMain();
                }

            }
        });

        return view;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize");
    }

}
