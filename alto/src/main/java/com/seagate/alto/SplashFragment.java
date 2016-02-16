// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// fragment to display splash and sign in

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.seagate.alto.metrics.AltoMetricsEvent;
import com.seagate.alto.metrics.Metrics;
import com.seagate.alto.provider.ListFolderAsyncTask;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;
import com.seagate.alto.utils.LogUtils;

import java.util.List;

// Splash is used at the beginning of the app to get things started -- includes sign in for now

public class SplashFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LogUtils.makeTag(SplashFragment.class);

    private static final String REDIRECT_URI = "http://localhost";
    private WebView mWebView;
    private View mFragView;
    private SharedPreferences mSharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mFragView = inflater.inflate(R.layout.sign_in, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

//        Metrics.getInstance().report(AltoMetricsEvent.ShowSplash);

        // if we're logged in, move on
        String username = mSharedPreferences.getString("username", null);
        if (mSharedPreferences.getBoolean("logged", false)) {
            String token = mSharedPreferences.getString("token."+username, null);
            String uid = mSharedPreferences.getString("uid."+username, null);
            if (token != null) {


                doneSplash(token);
                return null;
            }
        }

        View bySeagate = mFragView.findViewById(R.id.title);
        mWebView = (WebView) mFragView.findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading(): " + url);
                if (url.startsWith(REDIRECT_URI)) {
                    Uri redirect = Uri.parse(REDIRECT_URI + "/?" + url.substring(REDIRECT_URI.length() + 2));
                    String token = redirect.getQueryParameter("access_token");
                    String type = redirect.getQueryParameter("token_type");
                    String uid = redirect.getQueryParameter("uid");
                    EditText usernameView = (EditText) mFragView.findViewById(R.id.username);
                    String username = usernameView.getText().toString();
                    if (token != null && username != null) {
                        SharedPreferences.Editor e = mSharedPreferences.edit();
                        e.putString("username", username);
                        e.putBoolean("logged", true);
                        e.putString("token." + username, token);
                        e.putString("uid." + username, uid);
                        EditText passView = (EditText) mFragView.findViewById(R.id.account_password);
                        String passw = passView.getText().toString();
                        e.putString("pass." + username, passw);
                        e.apply();

                        Metrics.getInstance().report(AltoMetricsEvent.Login);

                        doneSplash(token);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url); // return false;
            }
        });
        View btnLogin = mFragView.findViewById(R.id.login);
        btnLogin.setOnClickListener(this);
        int yValue = 280;

        // at the end of the animation, check to see if the user has a token
        // if not show the username and password fields

        bySeagate.animate().y(yValue).setDuration(1000).setListener(new Animator.AnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                String username = mSharedPreferences.getString("username", null);
                if (mSharedPreferences.getBoolean("logged", false)) {
                    String token = mSharedPreferences.getString("token."+username, null);
                    String uid = mSharedPreferences.getString("uid."+username, null);
                    if (token != null) {
                        doneSplash(token);
                        return;
                    }
                }
                View form = mFragView.findViewById(R.id.login_form);
                EditText usernameView = (EditText)form.findViewById(R.id.username);
                usernameView.setText(username);
                form.setAlpha(0);
                form.setVisibility(View.VISIBLE);
                form.animate().alpha(1).setDuration(700);
            }
            public void onAnimationCancel(Animator animation) {}
            public void onAnimationRepeat(Animator animation) {}
            public void onAnimationStart(Animator animation) {}
        });

        return mFragView;
    }

    private void doneSplash(String token) {
        boolean dropbox = false;
        String path = dropbox ? "/camera uploads" :  "/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test";
        Provider provider = dropbox ? Providers.DROPBOX.provider : Providers.SEAGATE.provider;
        if (dropbox) provider.setAccessToken(token);
        //provider = Providers.LOCAL.provider;
        new ListFolderAsyncTask(provider, new ListFolderAsyncTask.Callback() {
            @Override
            public void onDataLoaded(Provider.ListFolderResult result) {
                Log.d(TAG, "Data loaded!");
                List<Provider.Metadata> list = result.entries();
                PlaceholderContent.setContent(list);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Error : " + e);
            }
        }).execute(path);

        // switch to the main fragment
        if (getActivity() instanceof IContentSwitcher) {
            ((IContentSwitcher) getActivity()).switchToMain();
        }
    }
//
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize");
    }

    @Override
    public void onClick(View xview) {
        if (xview.getId() == R.id.login) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mFragView.getWindowToken(), 0);
            EditText usernameView = (EditText) mFragView.findViewById(R.id.username);
            String username = usernameView.getText().toString();
            if (username == null || username.length() == 0) {
                return;
            }
            EditText passView = (EditText) mFragView.findViewById(R.id.account_password);
            String passw = passView.getText().toString();
            String token = mSharedPreferences.getString("token."+username, null);
            String uid = mSharedPreferences.getString("uid."+username, null);
            if (token != null) {
                if (!passw.equals(mSharedPreferences.getString("pass."+username, ""))) {
                    Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString("username", username);
                e.putBoolean("logged", true);
                e.apply();

                doneSplash(token);
            }
            // invoke cookie monster :P
            CookieManager.getInstance().removeAllCookie();
            mWebView.setVisibility(View.VISIBLE);
            mWebView.loadUrl("https://www.dropbox.com/1/oauth2/authorize?client_id=hnlne4n44rd2spj&response_type=token&redirect_uri=" + REDIRECT_URI);
        }
    }
}
