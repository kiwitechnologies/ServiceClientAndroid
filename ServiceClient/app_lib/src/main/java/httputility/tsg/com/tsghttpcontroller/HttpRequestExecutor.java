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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import httputility.tsg.com.tsgapicontroller.Constants;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kiwitech on 11/04/16.
 */
public final class HttpRequestExecutor {

    private static OkHttpClient httpClient;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
        builder.writeTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
        builder.readTimeout(Constants.CONNECTION_TIME_OUT_SEC, TimeUnit.SECONDS);
        httpClient = builder.build();
    }

    private static ConcurrentHashMap<String, ArrayList<CallWrapper>> requestInfo = new ConcurrentHashMap<>();

    public Response execute(ServiceManager serviceManager) throws IOException {
        Response response = null;
        Request request = HttpRequestFactory.getRequest(serviceManager, null);
        response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        return response;
    }

    public void enqueRequest(ServiceManager serviceManager, ServiceManager.RequestCallBack requestCallBack) {
        Request request = null;

        try {
            request = HttpRequestFactory.getRequest(serviceManager, requestCallBack);
        } catch (Exception e) {
            requestCallBack.onFailure(serviceManager.getRequestId(), e, null);
            requestCallBack.onFinish(serviceManager.getRequestId());
            return;
        }

        HttpRequestCallBack httpRequestCallBack = new HttpRequestCallBack(serviceManager, requestCallBack);
        Call call = httpClient.newCall(request);
        if (serviceManager.getRequestId() != null && !serviceManager.getRequestId().equals("")) {
            addCallInMap(serviceManager.getRequestId(), new CallWrapper(serviceManager.getRequestTime(), call));
        }
        call.enqueue(httpRequestCallBack);
    }

    private synchronized void addCallInMap(String requestId, CallWrapper call) {
        ArrayList<CallWrapper> callsList = requestInfo.get(requestId);
        if (callsList == null) {
            callsList = new ArrayList<>();
        }
        callsList.add(call);
        requestInfo.put(requestId, callsList);
    }

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
     * @return
     */
    public synchronized static boolean cancelReqeust(String requestId, long requestTime) {
        ArrayList<CallWrapper> callWrapperList = requestInfo.get(requestId);
        try {
            for (int i = 0; callWrapperList != null && i < callWrapperList.size(); i++) {
                CallWrapper callWrapper = callWrapperList.get(i);
                if (callWrapper.getRequestTime() == requestTime) {
                    callWrapperList.remove(i);
                    callWrapper.cancel();
                    requestInfo.put(requestId, callWrapperList);
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

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
                    requestInfo.put(requestId, callWrapperList);
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

}