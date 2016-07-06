package example.httputility.tsg.com.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;

import httputility.tsg.com.tsgapicontroller.*;
import httputility.tsg.com.tsghttpcontroller.HttpConstants;
import httputility.tsg.com.tsghttpcontroller.HttpResponse;
import httputility.tsg.com.tsghttpcontroller.RequestBodyParams;
import httputility.tsg.com.tsghttpcontroller.ServiceManager;

import static httputility.tsg.com.tsghttpcontroller.HttpConstants.*;

public class MainActivity extends Activity implements View.OnClickListener, ServiceManager.RequestCallBackWithProgress {

    private static final int RESULT_LOAD_IMG = 0;
    private final static String REQ_ID_DOWNLOAD = "mainActivity_req_download";


    private Button btnInitDB;
    private LinearLayout optionsLayout;
    private ContentLoadingProgressBar downloadProgressBar, uploadProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnInitDB = (Button) findViewById(R.id.btnInitDb);
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);

        downloadProgressBar = (ContentLoadingProgressBar) findViewById(R.id.downloadProgress);
        uploadProgressBar = (ContentLoadingProgressBar) findViewById(R.id.uploadProgress);

        findViewById(R.id.btnInitDb).setOnClickListener(this);
        findViewById(R.id.btnGetReq).setOnClickListener(this);
        findViewById(R.id.btnPostReq).setOnClickListener(this);
        findViewById(R.id.btnUploadImg).setOnClickListener(this);
        findViewById(R.id.btnDownloadImg).setOnClickListener(this);
        findViewById(R.id.btnPut).setOnClickListener(this);
        findViewById(R.id.btnDelete).setOnClickListener(this);
        findViewById(R.id.btnPathParameter).setOnClickListener(this);
        findViewById(R.id.btnCancelAll).setOnClickListener(this);

        findViewById(R.id.btnGetReqCancel).setOnClickListener(this);
        findViewById(R.id.btnPostReqCancel).setOnClickListener(this);
        findViewById(R.id.btnUploadReqCancel).setOnClickListener(this);
        findViewById(R.id.btnDownloadReqCancel).setOnClickListener(this);
        findViewById(R.id.btnPutReqCancel).setOnClickListener(this);
        findViewById(R.id.btnDeleteReqCancel).setOnClickListener(this);
        findViewById(R.id.btnPathParamReqCancel).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();
            uploadImage(imgDecodableString);
        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(String imgDecodableString) {
        RequestBodyParams body = new RequestBodyParams();
        body.put("name", "Ashish");
        body.put("user_email", "Ashish@kiwitech.com");
        body.put("age", "23");
        body.put("avatar", imgDecodableString);

        TSGServiceManager.enqueMultipartFileUploadRequest(this, Constants.API_IMAGE_UPLOAD, null, null, body, IMAGE_QUALITY.DEFALUT, true, this);

        uploadProgressBar.show();
        uploadProgressBar.setProgress(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnInitDb:
                TSGAPIController.init(getApplicationContext(), TSGAPIController.BUILD_FLAVOR.DUMMY_SERVER);
                TSGAPIController.setDummyServerResponseCode(this,300);
                btnInitDB.setVisibility(View.GONE);
                optionsLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btnGetReq:
                TSGServiceManager.setEnableDebugging(true);
                TSGServiceManager.enqueRequest(MainActivity.this, Constants.API_GET_All_PROJECT, null, this);
                break;
            case R.id.btnPostReq:
                RequestBodyParams paramMap = new RequestBodyParams();
                paramMap.put("name", "Ashish");
                paramMap.put("user_email", "ashish@kiwitech.com");
                TSGServiceManager.enqueRequest(this, Constants.API_CREATE_PROJECT, null, paramMap, this);
                break;
            case R.id.btnUploadImg:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                break;
            case R.id.btnDownloadImg:
                ServiceManager.FileDownloadRequestBuilder builder = new ServiceManager.FileDownloadRequestBuilder();
                builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash1.pdf");
                builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
                builder.setRequestId(REQ_ID_DOWNLOAD);
                ServiceManager httpUtils = builder.build();
                httpUtils.enqueFileRequestWithProgress(this);
                downloadProgressBar.show();
                downloadProgressBar.setProgress(0);
                break;
            case R.id.btnPut:
                RequestBodyParams paramMapPutReq = new RequestBodyParams();
                paramMapPutReq.put("project_id", "11");
                paramMapPutReq.put("name", "Ashish");
                paramMapPutReq.put("user_email", "ashish@kiwitech.com");
                paramMapPutReq.put("age", "24");
                TSGServiceManager.enqueRequest(this, Constants.API_UPDATE_PROJECT, null, paramMapPutReq, this);
                break;
            case R.id.btnDelete:
                RequestBodyParams paramMapDeleteReq = new RequestBodyParams();
                paramMapDeleteReq.put("project_id", "11");
                TSGServiceManager.enqueRequest(this, Constants.API_DELETE_PROJECT, null, paramMapDeleteReq, MainActivity.this);

                break;
            case R.id.btnPathParameter:
                HashMap<String, String> pathParam = new HashMap<>();
                pathParam.put("version_no", "v1");

                HashMap<String, String> queryParam = new HashMap<>();
                queryParam.put("access_token", "3314130824.5a3f599.a5e68fba664148579ceb2985fb652887");

                TSGServiceManager.enqueRequest(this, Constants.API_PATH_PARAM_REQUEST, pathParam, queryParam, null, this);
                break;
            case R.id.btnCancelAll:
                TSGServiceManager.cancelAllReqeust();
                break;
            case R.id.btnGetReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_GET_All_PROJECT);
                break;
            case R.id.btnPostReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_CREATE_PROJECT);
                break;
            case R.id.btnUploadReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_IMAGE_UPLOAD);
                break;
            case R.id.btnDownloadReqCancel:
                TSGServiceManager.cancelReqeust(REQ_ID_DOWNLOAD);
                break;
            case R.id.btnPutReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_UPDATE_PROJECT);
                break;
            case R.id.btnDeleteReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_DELETE_PROJECT);
                break;
            case R.id.btnPathParamReqCancel:
                TSGServiceManager.cancelReqeust(Constants.API_PATH_PARAM_REQUEST);
                break;
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSuccess(String requestId, HttpResponse response) {
        System.out.println("Success:  " + requestId);
        Toast.makeText(MainActivity.this, "Respoonse successful", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onFailure(String requestId, Throwable throwable, HttpResponse errorResponse) {
        System.out.println("requestId:  " + requestId);
        if (throwable != null) {
            Toast.makeText(MainActivity.this, "Respoonse failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Respoonse failed: " + errorResponse.getCode() + " " + errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (requestId.equals(REQ_ID_DOWNLOAD)) {
            downloadProgressBar.setProgress(0);
        } else if (requestId.equals(Constants.API_IMAGE_UPLOAD)) {
            uploadProgressBar.setProgress(0);
        }
    }

    @Override
    public void onFinish(String requestId) {
        System.out.println("Finish:  " + requestId);
        Toast.makeText(MainActivity.this, "Request finished", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inProgress(String requestId, String fileName, long num, long totalSize) {
        Log.d("log", "Request id: " + requestId + " ,fileName: " + fileName + " ,inProgress: " + num + " ,totalSize: " + totalSize);
        int progress = (int) ((num * 100) / totalSize);
        if (requestId.equals(REQ_ID_DOWNLOAD)) {
            downloadProgressBar.show();
            downloadProgressBar.setProgress(progress);
        } else if (requestId.equals(Constants.API_IMAGE_UPLOAD)) {
            uploadProgressBar.show();
            uploadProgressBar.setProgress(progress);
        }
    }

}

/*
// Sample code for files downloading

    ================================== SEQUENTIALLY DONWLOADING ==================================

        ServiceManager.FileDownloadRequestBuilder builder = new ServiceManager.FileDownloadRequestBuilder();
        builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash1.pdf");
        builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
        builder.setRequestId("aaaaaa");
        builder.setDownloadSequentially(context);
        httpUtils = builder.build();
        httpUtils.enqueFileRequestWithProgress(requestCallBackWithProgress);


        builder = new ServiceManager.FileDownloadRequestBuilder();
        builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash2.pdf");
        builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
        builder.setRequestId("bbbbbb");
        builder.setDownloadSequentially(context);
        httpUtils = builder.build();
        httpUtils.enqueFileRequestWithProgress(requestCallBackWithProgress);



        ********************** SET PRIORITY IN SEQUENTIAL MODE *********************

        builder = new ServiceManager.FileDownloadRequestBuilder();
        builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash3.pdf");
        builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
        builder.setRequestId("cccccc");
        builder.setExecuteOnPriority();
        builder.setDownloadSequentially(this);
        httpUtils = builder.build();
        httpUtils.enqueFileRequestWithProgress(requestCallBackWithProgress);



    ================================== PARALLALY DONWLOADING ==================================


        ServiceManager.FileDownloadRequestBuilder builder = new ServiceManager.FileDownloadRequestBuilder();
        builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash1.pdf");
        builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
        builder.setRequestId("aaaaaa");
        builder.setDownloadSequentially(context);
        httpUtils = builder.build();
        httpUtils.enqueFileRequestWithProgress(requestCallBackWithProgress);


        builder = new ServiceManager.FileDownloadRequestBuilder();
        builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash2.pdf");
        builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
        builder.setRequestId("bbbbbb");
        builder.setDownloadSequentially(context);
        httpUtils = builder.build();
        httpUtils.enqueFileRequestWithProgress(requestCallBackWithProgress);



    ================================== CANCELLATION OF REQUEST ==================================

        *********************** CANCEL PERTICULAR REQUEST *******************

        HttpRequestExecutor.cancelAllRequestWith("aaaaaa");
        HttpRequestExecutor.cancelAllRequestWith("cccccc");


        *********************** CANCEL PERTICULAR REQUEST *******************

        HttpRequestExecutor.cancelAllReqeust();

*/