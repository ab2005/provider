package com.seagate.alto.provider.example;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.example.tasks.DownloadFileTask;
import com.seagate.alto.provider.example.tasks.ProviderListFolderTask;
import com.seagate.alto.provider.example.tasks.UploadFileTask;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Activity that displays the content of a path in the cloud account and lets users navigate folders,
 * and upload/download files
 */
public class ProviderFilesActivity extends AuthActivity {

    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;

    private String mPath = "";
    private ProviderFilesAdapter mFilesAdapter;

    public static Intent getIntent(Context context, String path) {
        Intent filesIntent = new Intent(context, ProviderFilesActivity.class);
        filesIntent.putExtra(ProviderFilesActivity.EXTRA_PATH, path);
        return filesIntent;
    }

    @Bind(R.id.files_list)
    RecyclerView recyclerView;

    @OnClick(R.id.fab) public void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(EXTRA_PATH);
        if (path != null) {
            mPath = path;
        }

        setContentView(R.layout.activity_files);
        ButterKnife.bind(this);

        mFilesAdapter = new ProviderFilesAdapter(PicassoClient.getPicasso(), new ProviderFilesAdapter.Callback() {
            @Override
            public void onFolderClicked(Provider.FolderMetadata folder) {
                startActivity(ProviderFilesActivity.getIntent(ProviderFilesActivity.this, folder.pathLower()));
            }

            @Override
            public void onFileClicked(Provider.FileMetadata file) {
                downloadFile(file);

            }
        });

        if (mPath.contains("camera uploads")) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerView.setAdapter(mFilesAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // This is the result of a call to launchFilePicker
                uploadFile(data.getData().toString());
            }
        }
    }

    @Override
    protected void loadData() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading file list");
        dialog.show();

        new ProviderListFolderTask(DropboxClient.Provider(), new ProviderListFolderTask.Callback() {
            @Override
            public void onDataLoaded(Provider.ListFolderResult result) {
                dialog.dismiss();
                mFilesAdapter.setFiles(result.entries());
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Toast.makeText(ProviderFilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(mPath);
    }

    private void downloadFile(Provider.FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading");
        dialog.show();

        new DownloadFileTask(ProviderFilesActivity.this, DropboxClient.Provider(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();
                if (result != null) {
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Toast.makeText(ProviderFilesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute(file);
    }

    private void viewFileInExternalApp(File result) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(result.toURI().toString() );
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(result), type);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);

        if (resolveInfo.size() > 0) {
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(this, "No viewer available for ." + ext + " files", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading");
        dialog.show();

        new UploadFileTask(this, DropboxClient.Provider(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(Provider.FileMetadata result) {
                dialog.dismiss();

                Toast.makeText(ProviderFilesActivity.this,
                        result.name() + " size " + result.size() + " modified " + result.clientModified().toGMTString(),
                        Toast.LENGTH_SHORT)
                        .show();

                // Reload the folder
                loadData();
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Toast.makeText(ProviderFilesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute(fileUri, mPath);
    }
}
