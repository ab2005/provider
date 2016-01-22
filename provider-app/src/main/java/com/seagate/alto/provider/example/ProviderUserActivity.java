package com.seagate.alto.provider.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxUsers;
import com.seagate.alto.provider.example.tasks.GetCurrentAccountTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Activity that shows information about the currently logged in user
 */
public class ProviderUserActivity extends AuthActivity {
    @Bind(R.id.login_button)
    View btnLogin;
    @Bind(R.id.provider_files_button)
    View btnFiles;
    @Bind(R.id.email_text)
    TextView labelEmail;
    @Bind(R.id.name_text)
    TextView labelName;
    @Bind(R.id.type_text)
    TextView labelText;

    @OnClick (R.id.login_button) public void login (View v) {
        Auth.startOAuth2Authentication(ProviderUserActivity.this, getString(R.string.app_key));
    }

    @OnClick (R.id.provider_files_button) public void showFiles (View v) {
        startActivity(ProviderFilesActivity.getIntent(ProviderUserActivity.this, ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnLogin.setEnabled(getAccessToken() == null);
        btnFiles.setEnabled(getAccessToken() != null);
    }

    DbxUsers.FullAccount mAccoutInfo;

    @Override
    protected void loadData() {
        if (mAccoutInfo != null) return;

        new GetCurrentAccountTask(DropboxClient.Provider(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(DbxUsers.FullAccount result) {
                mAccoutInfo = result;
                labelEmail.setText(result.email);
                labelName.setText(result.name.displayName);
                labelText.setText(result.accountType.toString());
            }

            @Override
            public void onError(Exception e) {

            }
        }).execute();
    }
}
