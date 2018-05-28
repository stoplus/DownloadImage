package com.example.den.downloadimage.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.den.downloadimage.MyApp;
import com.example.den.downloadimage.R;
import com.example.den.downloadimage.adapter.AdapterImage;
import com.example.den.downloadimage.database.ImageObjDao;
import com.example.den.downloadimage.entity.ImageObj;
import com.example.den.downloadimage.utils.InternetConnection;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Inject
    ImageObjDao imageObjDao;
    @BindView(R.id.uid)
    EditText textLink;
    @BindView(R.id.button)
    Button buttonFind;
    @BindView(R.id.idRecycler)
    RecyclerView recycler;
    @BindView(R.id.appBar)
    AppBarLayout appBarLayout;
    private final int REQUEST_PERMITIONS = 1100;
    private final static String DIR_SD = "/MyApp";
    private Disposable disposDownloaded;
    private Disposable disposNotDownloaded;
    private String linkDevice = "";
    private DownloadManager mgr;
    private ImageObj imageObjUpdated;
    private long enqueue;
    private int count;
    private View view;
    private boolean flag;
    private boolean flagDownload;
    private ContentObserver contObserverInternal = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            String path = lastPathFromMediaStore(
                    MainActivity.this, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            afterDownload(Uri.fromFile(new File(path)));
        }
    };
    private ContentObserver contObserverExternal = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            String path = lastPathFromMediaStore(
                    MainActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            afterDownload(Uri.fromFile(new File(path)));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);

        ButterKnife.bind(this);
        MyApp.app().appComponent().inject(this);

        if (savedInstanceState != null) {
            flag = true;
            imageObjUpdated = savedInstanceState.getParcelable("imageObjUpdated");
        }
        //is created after compilation
        MainActivityPermissionsDispatcher.startWithPermissionCheck(MainActivity.this);
    }//onCreate


    private void afterDownload(Uri uri) {
        if (flagDownload) {
            linkDevice = String.valueOf(uri);
            imageObjUpdated.setDownload(true);
            imageObjUpdated.setLinkDevice(linkDevice);
            updateImageObj(imageObjUpdated);//update
        }
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            flagDownload = true;
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enqueue);

                if (mgr != null) {
                    Cursor c = mgr.query(query);
                    if (c.moveToFirst()) {
                        CheckDownloadStatus(c);
                    }
                    c.close();
                }
            }//if
        }//onReceive
    };


    private String lastPathFromMediaStore(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");
        String dateAdded = "";
        if (cursor != null && cursor.moveToNext()) {
            dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
        }
        Objects.requireNonNull(cursor).close();
        return dateAdded;
    }


    private void CheckDownloadStatus(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                String failedReason = "";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        failedReason = "ERROR_CANNOT_RESUME";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        failedReason = "ERROR_DEVICE_NOT_FOUND";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        failedReason = "ERROR_FILE_ALREADY_EXISTS";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        failedReason = "ERROR_FILE_ERROR";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        failedReason = "ERROR_HTTP_DATA_ERROR";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        failedReason = "ERROR_INSUFFICIENT_SPACE";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        failedReason = "ERROR_TOO_MANY_REDIRECTS";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                        Log.d("ddd", failedReason);
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        failedReason = "ERROR_UNKNOWN";
                        Log.d("ddd", failedReason);
                        break;
                }//switch

                deleteImageObj(imageObjUpdated);//delete by error
                Snackbar.make(view, getResources().getString(R.string.failed)
                        + failedReason, Snackbar.LENGTH_INDEFINITE).show();
                getListNotDownloadedImageObj();//get the list of NOT downloaded and start the download

                break;
            case DownloadManager.STATUS_PAUSED:
                String pausedReason = "";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        pausedReason = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        pausedReason = "PAUSED_WAITING_TO_RETRY";
                        break;
                }//switch
                Snackbar.make(view, "PAUSED: " + pausedReason, Snackbar.LENGTH_LONG).show();
                break;

            case DownloadManager.STATUS_PENDING:
                Snackbar.make(view, "PENDING", Snackbar.LENGTH_LONG).show();
                break;
            case DownloadManager.STATUS_RUNNING:
                Snackbar.make(view, "RUNNING", Snackbar.LENGTH_LONG).show();
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                Snackbar.make(view, "SUCCESSFUL", Snackbar.LENGTH_LONG).show();
                break;
        }//switch
    }//CheckDownloadStatus


    private void insertImageObj(final ImageObj imageObj) {
        Completable.fromAction(() -> imageObjDao.insert(imageObj))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {//Вставляем новую
                        Log.d("MainActivityClass", "imageObjDao.insert");
                        getListNotDownloadedImageObj();//get the list of NOT downloaded and start the download
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }//addProductForList


    private void updateImageObj(final ImageObj imageObj) {
        Completable.fromAction(() -> imageObjDao.update(imageObj))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers
                        .mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Log.d("MainActivityClass", "imageObjDao.update");
//                        getListNotDownloadedImageObj();//get the list of NOT downloaded and start the download
                        getListDownloadedImageObj();//get the list of downloaded and display them
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void deleteImageObj(final ImageObj imageObj) {
        Completable.fromAction(() -> imageObjDao.delete(imageObj))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Log.d("MainActivityClass", "imageObjDao.delete");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    //get the list of downloaded
    public void getListDownloadedImageObj() {
        disposDownloaded = imageObjDao.allDownloaded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listImageObj -> {
                    disposDownloaded.dispose();
                    displayUser(listImageObj);//display them
                });
    }//getListImageObj

    //get the list of NOT downloaded
    public void getListNotDownloadedImageObj() {
        disposNotDownloaded = imageObjDao.allNotDownloaded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listImageObj -> {
                    disposNotDownloaded.dispose();
                    if (listImageObj.length > 0) {
                        if (InternetConnection.checkConnection(getApplicationContext())) {

                            imageObjUpdated = listImageObj[0];//create changeable object
                        } else {
                            Snackbar.make(view, getResources().getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE).show();
                            imageObjUpdated = listImageObj[listImageObj.length - 1];//create changeable object
                        }
                        savePicture(imageObjUpdated.getLink()); // Save file
                    }
                });
    }//getListImageObj


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageObjUpdated", imageObjUpdated);
    }

    private void displayUser(ImageObj[] imageObj) {
        AdapterImage adapterImage = new AdapterImage(MainActivity.this, imageObj);
        recycler.setAdapter(adapterImage);
    }//displayUser


    public void addnewLink(View view) {
        String linkText = textLink.getText().toString();

        if (android.util.Patterns.WEB_URL.matcher(linkText).matches()
                && linkText.matches(".*\\.(jpg|png|bmp|jpeg|gif)$")) {

            linkText = linkText.replace("[", "%5B")
                    .replace("]", "%5D")
                    .replace("-", "%2D")
                    .replace("–", "%96")
                    .replace("—", "%97")
                    .replace("_", "%5F");

            ImageObj imageObj = new ImageObj(linkText, "", false);
            insertImageObj(imageObj);
        } else
            Snackbar.make(view, getResources().getString(R.string.enterUrlString), Snackbar.LENGTH_LONG).show();
    }//addnewLink


    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void start() {
        getListDownloadedImageObj();//get the list of downloaded and display them
        if (!flag)
            getListNotDownloadedImageObj();//get the list of NOT downloaded and start the download
    }//start


    private void savePicture(String url) {
        int index = url.lastIndexOf('/');
        String name = url.substring(index, url.length());
        Uri downloadUri = Uri.parse(url);

        //check the availability of SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Snackbar.make(view, getResources().getString(R.string.sdCardNotAvailable) +
                    Environment.getExternalStorageState(), Snackbar.LENGTH_LONG).show();
            return;
        }//if

        try {//Running the download of the file on the specified path
            mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri)
                    .setDestinationInExternalPublicDir(DIR_SD, name);
            enqueue = mgr.enqueue(request);
            count++;
        } catch (IllegalArgumentException e) {
            Snackbar.make(view, getResources()
                    .getString(R.string.onlyHTTP_HTTPS_URI), Snackbar.LENGTH_LONG).show();
        }//try-catch
    }//savePicture


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        getContentResolver().registerContentObserver(
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                true, contObserverInternal
        );
        getContentResolver().registerContentObserver(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true, contObserverExternal
        );
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onComplete);
        this.getContentResolver().unregisterContentObserver(contObserverExternal);
        this.getContentResolver().unregisterContentObserver(contObserverInternal);
    }//onPause


    //refund after agreement / denial of the user
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //is created after compilation
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }//onRequestPermissionsResult


    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void permissionsDenied() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMITIONS);
    }//permissionsDenied


    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void onNeverAskAgain() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.attention))
                .setIcon(R.mipmap.warning)
                .setMessage(getResources().getString(R.string.need_get_permissions))
                .setPositiveButton(getResources().getString(R.string.ok), (dialog1, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog12, which) -> {
                    dialog12.dismiss();
                    finish();
                })
                .show();
        dialog.setCancelable(false);
    }


    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void showRationaleForCamera(final PermissionRequest request) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.need_obtain_permissions))
                .setPositiveButton(getResources().getString(R.string.ok), (dialog1, which) -> request.proceed())
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog12, which) -> request.cancel())
                .show();
        dialog.setCancelable(false);
    }
}
