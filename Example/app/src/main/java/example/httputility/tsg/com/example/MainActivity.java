package example.httputility.tsg.com.example;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import httputility.tsg.com.tsghttpcontroller.HttpResponse;
import httputility.tsg.com.tsghttpcontroller.HttpUtils;

public class MainActivity extends Activity implements View.OnClickListener, HttpUtils.RequestCallBackWithProgress {

    private static final int RESULT_LOAD_IMG = 0;
    private final static String REQ_ID_GET = "mainActivity_req_get";
    private final static String REQ_ID_POST = "mainActivity_req_post";
    private final static String REQ_ID_UPLOAD = "mainActivity_req_upload";
    private final static String REQ_ID_DOWNLOAD = "mainActivity_req_download";
    private final static String REQ_ID_PUT = "mainActivity_req_put";
    private final static String REQ_ID_DELETE = "mainActivity_req_delete";


    private Button btnInitDB, btnGetReq, btnPostReq, btnUploadFile, btnDownloadFile, btnCancelAll, btnPut, btnDelete;
    private Button btnGetReqCancel, btnPostReqCancel, btnUploadReqCancel, btnDownloadReqCancel, btnPutReqCancel, btnDeleteReqCancel;
    private LinearLayout optionsLayout;
    private ContentLoadingProgressBar downloadProgressBar, uploadProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        btnInitDB = (Button) findViewById(R.id.btnInitDb);
        btnGetReq = (Button) findViewById(R.id.btnGetReq);
        btnPostReq = (Button) findViewById(R.id.btnPostReq);
        btnUploadFile = (Button) findViewById(R.id.btnUploadImg);
        btnDownloadFile = (Button) findViewById(R.id.btnDownloadImg);
        btnPut = (Button) findViewById(R.id.btnPut);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnCancelAll = (Button) findViewById(R.id.btnExtra);
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);

        btnGetReqCancel = (Button) findViewById(R.id.btnGetReqCancel);
        btnPostReqCancel = (Button) findViewById(R.id.btnPostReqCancel);
        btnUploadReqCancel = (Button) findViewById(R.id.btnUploadReqCancel);
        btnDownloadReqCancel = (Button) findViewById(R.id.btnDownloadReqCancel);
        btnPutReqCancel = (Button) findViewById(R.id.btnPutReqCancel);
        btnDeleteReqCancel = (Button) findViewById(R.id.btnDeleteReqCancel);

        downloadProgressBar = (ContentLoadingProgressBar) findViewById(R.id.downloadProgress);
        uploadProgressBar = (ContentLoadingProgressBar) findViewById(R.id.uploadProgress);

        btnInitDB.setOnClickListener(this);
        btnGetReq.setOnClickListener(this);
        btnPostReq.setOnClickListener(this);
        btnUploadFile.setOnClickListener(this);
        btnDownloadFile.setOnClickListener(this);
        btnPut.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnCancelAll.setOnClickListener(this);

        btnGetReqCancel.setOnClickListener(this);
        btnPostReqCancel.setOnClickListener(this);
        btnUploadReqCancel.setOnClickListener(this);
        btnDownloadReqCancel.setOnClickListener(this);
        btnPutReqCancel.setOnClickListener(this);
        btnDeleteReqCancel.setOnClickListener(this);
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
        HashMap body = new HashMap();
        body.put("name", "Ashish");
        body.put("user_email", "Ashish@kiwitech.com");
        body.put("age", "23");
        body.put("avatar", imgDecodableString);

        TSGHttpUtility.enqueMultipartFileUploadRequest(this, Constants.API_IMAGE_UPLOAD, null, body, this);

        uploadProgressBar.show();
        uploadProgressBar.setProgress(0);
    }

    @Override
    public void onClick(View view) {
        if (view == btnInitDB) {
            TSGAPIController.init(getApplicationContext(), TSGAPIController.BUILD_FLAVOR.DEVELOPMENT);
            btnInitDB.setVisibility(View.GONE);
            optionsLayout.setVisibility(View.VISIBLE);
        } else if (view == btnGetReq) {
                TSGHttpUtility.enqueRequest(MainActivity.this, Constants.API_GET_All_PROJECT, null, this);
        } else if (view == btnPostReq) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("name", "Ashish");
            paramMap.put("user_email", "ashish@kiwitech.com");
            TSGHttpUtility.enqueRequest(this, Constants.API_CREATE_PROJECT, null, paramMap, MainActivity.this);
        } else if (view == btnUploadFile) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        } else if (view == btnDownloadFile) {
            HttpUtils.FileDownloadRequestBuilder builder = new HttpUtils.FileDownloadRequestBuilder();
            builder.setFilePath(Environment.getExternalStorageDirectory() + "/Download", "ash1.pdf");
            builder.setSubURL("http://kmmc.in/wp-content/uploads/2014/01/lesson2.pdf");
            builder.setRequestId(REQ_ID_DOWNLOAD);
            HttpUtils httpUtils = builder.build();
            httpUtils.enqueFileRequestWithProgress(this);
            downloadProgressBar.show();
            downloadProgressBar.setProgress(0);
        } else if (view == btnPut) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("project_id", "13");
            paramMap.put("name", "Ashish");
            paramMap.put("user_email", "ashish@kiwitech.com");
            paramMap.put("age", "24");
            TSGHttpUtility.enqueRequest(this, Constants.API_UPDATE_PROJECT, null, paramMap, MainActivity.this);
        } else if (view == btnDelete) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("project_id", "13");
            TSGHttpUtility.enqueRequest(this, Constants.API_DELETE_PROJECT, null, paramMap, MainActivity.this);
        } else if (view == btnGetReqCancel) {
            TSGHttpUtility.cancelReqeust(Constants.API_GET_All_PROJECT);
        } else if (view == btnPostReqCancel) {
            TSGHttpUtility.cancelReqeust(Constants.API_CREATE_PROJECT);
        } else if (view == btnUploadReqCancel) {
            TSGHttpUtility.cancelReqeust(Constants.API_IMAGE_UPLOAD);
        } else if (view == btnDownloadReqCancel) {
            TSGHttpUtility.cancelReqeust(REQ_ID_DOWNLOAD);
        } else if (view == btnPutReqCancel) {
            TSGHttpUtility.cancelReqeust(Constants.API_UPDATE_PROJECT);
        } else if (view == btnDeleteReqCancel) {
            TSGHttpUtility.cancelReqeust(Constants.API_DELETE_PROJECT);
        } else if (view == btnCancelAll) {
            TSGHttpUtility.cancelAllReqeust();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onFailure(String requestId, Throwable throwable, HttpResponse errorResponse) {
        if (throwable != null) {
            Toast.makeText(this, "Respoonse failed: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Respoonse failed: " + errorResponse.getCode() + " " + errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (requestId == REQ_ID_DOWNLOAD) {
            downloadProgressBar.setProgress(0);
        } else if (requestId == REQ_ID_UPLOAD) {
            uploadProgressBar.setProgress(0);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSuccess(String requestId, HttpResponse response) {
        Toast.makeText(this, "Respoonse successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinish(String requestId) {
        Toast.makeText(this, "Request finished", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inProgress(String requestId, String fileName, long num, long totalSize) {
        Log.d(MainActivity.class.getName(), "Request id: " + requestId + " ,fileName: " + fileName + " ,inProgress: " + num + " ,totalSize: " + totalSize);
        int progress = (int) ((num * 100) / totalSize);
        if (requestId == REQ_ID_DOWNLOAD) {
            downloadProgressBar.show();
            downloadProgressBar.setProgress(progress);
        } else if (requestId == REQ_ID_UPLOAD) {
            uploadProgressBar.show();
            uploadProgressBar.setProgress(progress);
        }
    }
}
