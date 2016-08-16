/*
 * Copyright (c) 2016 Kiwitech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package httputility.tsg.com.tsghttpcontroller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import httputility.tsg.com.tsgapicontroller.Constants;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kiwitech on 11/04/16.
 */
public final class HttpRequestExecutor {

    static volatile ConcurrentHashMap<String, ArrayList<CallWrapper>> requestInfo = new ConcurrentHashMap<>();

    public Response execute(ServiceManager serviceManager) throws IOException {
        Request request = HttpRequestFactory.getRequest(serviceManager, null);
        logRequest(request, serviceManager);
        Response response = HttpClientConfiguration.getInstance().getOkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        return response;
    }

    public void enqueParallelRequest(ServiceManager serviceManager, ServiceManager.RequestCallBack requestCallBack) {
        Request request = null;

        try {
            request = HttpRequestFactory.getRequest(serviceManager, requestCallBack);
            logRequest(request, serviceManager);
        } catch (Exception e) {
            requestCallBack.onFailure(serviceManager.getRequestId(), e, null);
            requestCallBack.onFinish(serviceManager.getRequestId());
            return;
        }

        HttpRequestCallBack httpRequestCallBack = new HttpRequestCallBack(serviceManager, requestCallBack);
        Call call = HttpClientConfiguration.getInstance().getOkHttpClient().newCall(request);
        if (serviceManager.getRequestId() != null && !serviceManager.getRequestId().equals("")) {
            addCallInMap(serviceManager.getRequestId(), new CallWrapper(serviceManager.getRequestTime(), call));
        }
        call.enqueue(httpRequestCallBack);
    }

    private void logRequest(Request request, ServiceManager serviceManager) {
        try {
            if (ServiceManager.isDebuggingEnable()) {
                Log.d(HttpConstants.KEY_TSG_SERVICE_CLIENT_REQUEST, String.valueOf(request) + "  header:" + request.headers() + "  body:" + serviceManager.getBody_params() + "  tag:" + serviceManager.getRequestId());
            }
        } catch (Exception e) {
            System.out.print("exception in logging");
        }
    }

    public void enqueSequentialRequest(ServiceManager serviceManager, ServiceManager.RequestCallBack requestCallBack) {
        Request request = null;

        try {
            request = HttpRequestFactory.getRequest(serviceManager, requestCallBack);
            logRequest(request, serviceManager);
        } catch (Exception e) {
            requestCallBack.onFailure(serviceManager.getRequestId(), e, null);
            requestCallBack.onFinish(serviceManager.getRequestId());
            return;
        }

        HttpRequestCallBack httpRequestCallBack = new HttpRequestCallBack(serviceManager, requestCallBack);
        Call call = HttpClientConfiguration.getInstance().getOkHttpClient().newCall(request);
        addCallInMap(serviceManager.getRequestId(), new CallWrapper(serviceManager.getRequestTime(), call));


        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_REQUEST_ID, serviceManager.getRequestId());
        bundle.putLong(Constants.EXTRA_REQUEST_TIME, serviceManager.getRequestTime());
        bundle.putBoolean(Constants.EXTRA_EXECUTE_ON_PRIORITY, serviceManager.isExecuteOnPriority());
        bundle.putSerializable(Constants.EXTRA_REQUEST_RECEIVER, httpRequestCallBack);

        Intent intent = null;
        if (serviceManager.getHTTPRequestType() == HttpConstants.HTTPRequestType.UPLOAD_FILE) {
            intent = new Intent(serviceManager.getContext(), SequentialUploadRequestExecutorService.class);
            SequentialUploadRequestExecutorService.addRequest(bundle);
        } else if (serviceManager.isDonwloadFileRequest()) {
            intent = new Intent(serviceManager.getContext(), SequentialDownloadRequestExecutorService.class);
            SequentialDownloadRequestExecutorService.addRequest(bundle);
        }


        serviceManager.getContext().startService(intent);

    }


    private synchronized void addCallInMap(String requestId, CallWrapper call) {
        ArrayList<CallWrapper> callsList = requestInfo.get(requestId);
        if (callsList == null) {
            callsList = new ArrayList<>();
        }
        callsList.add(call);
        requestInfo.put(requestId, callsList);
    }

    /**
     * It will cancel all the request that are pending or being executing
     *
     * @return
     */
    public synchronized static boolean cancelAllReqeust() {
        Iterator<String> requestIdIterator = requestInfo.keySet().iterator();
        while (requestIdIterator.hasNext()) {
            String requestId = requestIdIterator.next();
            cancelAllRequestWith(requestId);
        }
        return true;
    }


    /**
     * cancel all request which is having same requestId
     *
     * @param requestId
     * @return
     */
    public synchronized static boolean cancelAllRequestWith(String requestId) {
        ArrayList<CallWrapper> callsList = requestInfo.get(requestId);
        if (callsList != null) {
            for (int i = 0; i < callsList.size(); i++) {
                CallWrapper call = callsList.get(i);
                call.cancel();
            }
            requestInfo.remove(requestId);
            return true;
        }
        return false;
    }

    /**
     * cancel the request with requestId and requestTime
     *
     * @param requestId
     * @param requestTime
     * @return public synchronized static boolean cancelReqeust(String requestId, long requestTime) {
     * ArrayList<CallWrapper> callWrapperList = requestInfo.get(requestId);
     * try {
     * for (int i = 0; callWrapperList != null && i < callWrapperList.size(); i++) {
     * CallWrapper callWrapper = callWrapperList.get(i);
     * if (callWrapper.getRequestTime() == requestTime) {
     * callWrapperList.remove(i);
     * callWrapper.cancel();
     * requestInfo.put(requestId, callWrapperList);
     * return true;
     * }
     * }
     * } catch (Exception e) {
     * }
     * return false;
     * }
     */

    static synchronized void removeRequestIdFromRequestInfo(String requestId, long requestTime) {
        if (requestId == null) {
            return;
        }
        ArrayList<CallWrapper> callWrapperList = requestInfo.get(requestId);

        try {
            for (int i = 0; callWrapperList != null && i < callWrapperList.size(); i++) {
                CallWrapper callWrapper = callWrapperList.get(i);
                if (callWrapper.getRequestTime() == requestTime) {
                    callWrapperList.remove(i);
                    if (callWrapperList.size() == 0) {
                        requestInfo.remove(requestId);
                    } else {
                        requestInfo.put(requestId, callWrapperList);
                    }
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

}