package com.example.scopedstorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Image Path";
    PhotoView imageView;
    private int READ_REQUEST_CODE = 55;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.loaded_image);
    }

    public void getTheImage(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    new CopyFileToAppDirTask().execute(data.getData());
                } else {
                    Log.d(TAG, "File uri not found {}");
                }
            } else {
                Log.d(TAG, "User cancelled file browsing {}");
            }
        }
    }

    public static final String FILE_BROWSER_CACHE_DIR = "CacheDirectory";

    @SuppressLint("StaticFieldLeak")
    private class CopyFileToAppDirTask extends AsyncTask<Uri, Void, String> {
        private ProgressDialog mProgressDialog;

        private CopyFileToAppDirTask() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Please Wait..");
            mProgressDialog.show();
        }

        protected String doInBackground(Uri... uris) {
            try {
                return writeFileContent(uris[0]);
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(String cachedFilePath) {
            mProgressDialog.dismiss();
            if (cachedFilePath != null) {
                Glide.with(imageView).load(cachedFilePath).into(imageView);
            } else {
                Log.d(TAG, "Unsuccessful");
            }

        }

        private String writeFileContent(final Uri uri) throws IOException {
            InputStream selectedFileInputStream = getContentResolver().openInputStream(uri);
            if (selectedFileInputStream != null) {
                final File cacheDir = new File(getExternalFilesDir(null), FILE_BROWSER_CACHE_DIR);
                boolean doesCacheDirExists = cacheDir.exists();
                if (!doesCacheDirExists) {
                    doesCacheDirExists = cacheDir.mkdirs();
                }
                if (doesCacheDirExists) {
                    String filePath = cacheDir.getAbsolutePath() + "/6036220_" + System.currentTimeMillis() + ".jpg";
                    OutputStream selectedFileOutPutStream = new FileOutputStream(filePath);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = selectedFileInputStream.read(buffer)) > 0) {
                        selectedFileOutPutStream.write(buffer, 0, length);
                    }
                    selectedFileOutPutStream.flush();
                    selectedFileOutPutStream.close();
                    return filePath;
                }
                selectedFileInputStream.close();
            }
            return null;
        }
    }
}