package httputility.tsg.com.tsghttpcontroller;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import httputility.tsg.com.tsgapicontroller.Constants;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by kiwitech on 16/06/16.
 */

public class SequentialUploadRequestExecutorService extends IntentService {

    private static ArrayList<Bundle> bundlesList = new ArrayList<>();

    public static void addRequest(Bundle bundle) {
        if (bundle.getBoolean(Constants.EXTRA_EXECUTE_ON_PRIORITY, false)) {
            bundlesList.add(0, bundle);
        } else {
            bundlesList.add(bundle);
        }
    }

    public SequentialUploadRequestExecutorService() {
        super(SequentialUploadRequestExecutorService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (bundlesList.size() == 0) {
            return;
        }
        Bundle data = bundlesList.get(0);
        bundlesList.remove(0);
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
