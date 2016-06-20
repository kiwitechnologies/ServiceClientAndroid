package httputility.tsg.com.tsghttpcontroller;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import httputility.tsg.com.tsgapicontroller.Constants;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by kiwitech on 16/06/16.
 */

public class SequentialDownloadRequestExecutorService extends IntentService {
    private OkHttpClient httpClient;

    private volatile static Vector<Bundle> bundlesList = new Vector<>();

    public synchronized static void addRequest(Bundle bundle) {
        if (bundle.getBoolean(Constants.EXTRA_EXECUTE_ON_PRIORITY, false)) {
            bundlesList.add(0, bundle);
        } else {
            bundlesList.add(bundle);
        }
    }

    public synchronized static Bundle popFirstElement() {
        Bundle bundle = bundlesList.get(0);
        bundlesList.removeElementAt(0);
        bundlesList.removeAll(Collections.singleton(null));
        return bundle;
    }

    public SequentialDownloadRequestExecutorService() {
        super(SequentialDownloadRequestExecutorService.class.getName());

        if (httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
            builder.writeTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
            builder.readTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
            httpClient = builder.build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(Constants.EXTRA_EXECUTE_ON_PRIORITY, false)) {
            bundlesList.add(0, intent.getExtras());
        } else {
            bundlesList.add(intent.getExtras());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (bundlesList.size() < 1) {
            return;
        }
        Bundle data = popFirstElement();
        String requestId = data.getString(Constants.EXTRA_REQUEST_ID);
        long requestTime = data.getLong(Constants.EXTRA_REQUEST_TIME, -1);
        HttpRequestCallBack httpRequestCallBack = (HttpRequestCallBack) data.getSerializable(Constants.EXTRA_REQUEST_RECEIVER);

        Call call = getCall(requestId, requestTime);

        if (call != null) {
            try {
                Response response = call.execute();
                httpRequestCallBack.onResponse(call, response);
            } catch (IOException e) {
                httpRequestCallBack.onFailure(call, e);
            }
        } else {
            httpRequestCallBack.onFailure(null, new IOException("Canceled"));
        }
    }


    static Call getCall(String requestId, long requestTime) {
        ArrayList<CallWrapper> callWrapperList = HttpRequestExecutor.requestInfo.get(requestId);
        try {
            for (int i = 0; callWrapperList != null && i < callWrapperList.size(); i++) {
                CallWrapper callWrapper = callWrapperList.get(i);
                if (callWrapper.getRequestTime() == requestTime) {
                    return callWrapper.getCall();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
