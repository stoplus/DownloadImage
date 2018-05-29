package com.example.den.downloadimage.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
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

import com.example.den.downloadimage.MyApp;
import com.example.den.downloadimage.R;
import com.example.den.downloadimage.adapter.AdapterImage;
import com.example.den.downloadimage.database.ImageObjDao;
import com.example.den.downloadimage.entity.ImageObj;
import com.example.den.downloadimage.utils.InternetConnection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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
    private int enqueue;
    private View view;
    private boolean flagInstanceState;
    private List<ImageObj> listImageObj = new ArrayList<>();
    private List<Integer> listEnqueue = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(view);

        ButterKnife.bind(this);
        MyApp.app().appComponent().inject(this);

        if (savedInstanceState != null) {
            flagInstanceState = true;
            listImageObj = savedInstanceState.getParcelableArrayList("listImageObj");
            listEnqueue = savedInstanceState.getIntegerArrayList("listEnqueue");
        }
        //is created after compilation
        MainActivityPermissionsDispatcher.startWithPermissionCheck(MainActivity.this);
    }//onCreate


    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                DownloadManager.Query query = new DownloadManager.Query();

                enqueue = listEnqueue.get(0);
                imageObjUpdated = listImageObj.get(0);
                query.setFilterById(enqueue);
                listImageObj.remove(0);
                listEnqueue.remove(0);

                if (mgr != null) {
                    Cursor c = mgr.query(query);
                    if (c.moveToFirst()) {
                        CheckDwnloadStatus(c);
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            linkDevice = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            imageObjUpdated.setDownload(true);
                            imageObjUpdated.setLinkDevice(linkDevice);
                            updateImageObj(imageObjUpdated);//update
                        }
                        if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex) ||
                                DownloadManager.STATUS_PENDING == c.getInt(columnIndex) ||
                                DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {
                            listImageObj.add(imageObjUpdated);
                            listEnqueue.add(enqueue);
                        }
                    }//if
                    c.close();
                }//if
            }//if
        }//onReceive
    };


    private void CheckDwnloadStatus(Cursor cursor) {
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
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        failedReason = "ERROR_DEVICE_NOT_FOUND";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        failedReason = "ERROR_FILE_ALREADY_EXISTS";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        failedReason = "ERROR_FILE_ERROR";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        failedReason = "ERROR_HTTP_DATA_ERROR";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        failedReason = "ERROR_INSUFFICIENT_SPACE";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        failedReason = "ERROR_TOO_MANY_REDIRECTS";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                        Log.d(getClass().getName(), failedReason);
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        failedReason = "ERROR_UNKNOWN";
                        Log.d(getClass().getName(), failedReason);
                        break;
                }//switch

                deleteImageObj(imageObjUpdated);//delete by error
                Snackbar.make(view, getResources().getString(R.string.failed)
                        + failedReason, Snackbar.LENGTH_INDEFINITE).show();
                getListNotDownloadedImageObj();//get the listImageObj of NOT downloaded and start the download

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
    }//CheckDwnloadStatus


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
                        if (!InternetConnection.checkConnection(getApplicationContext())) {
                            Snackbar.make(view, getResources().getString(R.string.no_internet)
                                    , Snackbar.LENGTH_INDEFINITE).show();
                        }
                        getListNotDownloadedImageObj();//get the listImageObj of NOT downloaded and start the download
                    }//onComplete

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
                        getListDownloadedImageObj();//get the listImageObj of downloaded and display them
                    }//onComplete

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }//updateImageObj

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
                        Log.d(getClass().getName(), "imageObjDao.delete");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    //get the listImageObj of downloaded
    public void getListDownloadedImageObj() {
        disposDownloaded = imageObjDao.allDownloaded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listImageObj -> {
                    disposDownloaded.dispose();
                    displayUser(listImageObj);//display them
                });
    }//getListImageObj

    //get the listImageObj of NOT downloaded
    public void getListNotDownloadedImageObj() {
        disposNotDownloaded = imageObjDao.allNotDownloaded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newListImageObj -> {
                    disposNotDownloaded.dispose();
                    if (newListImageObj.length > 0) {
                        imageObjUpdated = newListImageObj[newListImageObj.length - 1];//create changeable object
                        savePicture(imageObjUpdated); // Save file
                    }
                });
    }//getListImageObj


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

            ImageObj imageObj = new ImageObj(linkText, linkDevice, false);
            insertImageObj(imageObj);
        } else
            Snackbar.make(view, getResources().getString(R.string.enterUrlString)
                    , Snackbar.LENGTH_LONG).show();
    }//addnewLink


    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void start() {
        getListDownloadedImageObj();//get the listImageObj of downloaded and display them
        if (!flagInstanceState)
            getListNotDownloadedImageObj();//get the listImageObj of NOT downloaded and start the download
    }//start


    private void savePicture(ImageObj imageObj) {
        int index = imageObj.getLink().lastIndexOf('/');
        String name = imageObj.getLink().substring(index, imageObj.getLink().length());
        Uri downloadUri = Uri.parse(imageObj.getLink());

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
            enqueue = (int) mgr.enqueue(request);

            listEnqueue.add(enqueue);
            listImageObj.add(imageObj);

        } catch (IllegalArgumentException e) {
            Snackbar.make(view, getResources().getString(R.string.onlyHTTP_HTTPS_URI), Snackbar.LENGTH_LONG).show();
        }//try-catch
    }//savePicture


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("listImageObj", (ArrayList<? extends Parcelable>) listImageObj);
        outState.putIntegerArrayList("listEnqueue", (ArrayList<Integer>) listEnqueue);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(onComplete);
    }//onPause


    //refund after agreement / denial of the user
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //is created after compilation
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }//onRequestPermissionsResult


    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void permissionsDenied() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMITIONS);
        finish();
    }//permissionsDenied


    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
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


    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
    void showRationaleForCamera(final PermissionRequest request) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.need_obtain_permissions))
                .setPositiveButton(getResources().getString(R.string.ok), (dialog1, which) -> request.proceed())
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog12, which) -> request.cancel())
                .show();
        dialog.setCancelable(false);
    }
}
