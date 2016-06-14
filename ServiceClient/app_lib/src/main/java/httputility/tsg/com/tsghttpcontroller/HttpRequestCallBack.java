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

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by kiwitech on 11/04/16.
 */
class HttpRequestCallBack implements Callback {

    private ServiceManager.RequestCallBack requestCallBack;
    private String requestId;
    private HttpConstants.HTTPRequestType requestType;
    private String downloadFilePath;
    private Handler handler;
    private long requestTime;

    public HttpRequestCallBack(ServiceManager serviceManager, ServiceManager.RequestCallBack requestCallBack) {
        handler = new Handler(Looper.getMainLooper());
        this.requestCallBack = requestCallBack;
        this.requestId = serviceManager.getRequestId();
        this.requestType = serviceManager.getHTTPRequestType();
        this.downloadFilePath = serviceManager.getDownloadFilePath();
        this.requestTime = serviceManager.getRequestTime();
    }

    /**
     * @param call it can be null
     * @param e
     */
    @Override
    public final void onFailure(Call call, final IOException e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestCallBack.onFailure(requestId, e, null);
                requestCallBack.onFinish(requestId);
            }
        });
        HttpRequestExecutor.removeRequestIdFromRequestInfo(requestId, requestTime);
    }

    /**
     * @param call
     * @param response
     */
    @Override
    public final void onResponse(Call call, final Response response) {
        // Handled the case if server sends the error code then it should call hte failed response
        if (!isSuccessfullResponseCode(response.code())) {
            postFailedCallbackMsg(requestId, null, response);
            postFinishCallback(requestId);
            HttpRequestExecutor.removeRequestIdFromRequestInfo(requestId, requestTime);
            return;
        }
        if (requestType == HttpConstants.HTTPRequestType.DOWNLOAD_FILE) {
            donwloadFile(call, response);
        } else {
            final HttpResponse httpResponse = new HttpResponse(response);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    requestCallBack.onSuccess(requestId, httpResponse);
                    requestCallBack.onFinish(requestId);
                }
            });
        }
        HttpRequestExecutor.removeRequestIdFromRequestInfo(requestId, requestTime);
    }

    private boolean isSuccessfullResponseCode(int code) {
        return (code >= 600 || code < 400);
    }

    private void donwloadFile(Call call, Response response) {
        long contentLength = response.body().contentLength();
        long downloaded = 0;
        InputStream input = response.body().byteStream();
        try {
            File file = new File(downloadFilePath);
            if (file.exists()) {
                file.delete();
            }
            String fileName = file.getName();
            OutputStream output = new FileOutputStream(file);
            try {
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    postProgresCallbackMsg(requestId, fileName, downloaded, contentLength);

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                        downloaded += read;
                        postProgresCallbackMsg(requestId, fileName, downloaded, contentLength);
                    }
                    output.flush();
                    postSuccessCallbackMsg(requestId, new HttpResponse(response));
                } catch (final SocketException e) {
                    if (call.isCanceled()) {
                        postFailedCallbackMsg(requestId, new IOException("Canceled"), null);
                    } else {
                        postFailedCallbackMsg(requestId, e, null);
                    }
                } finally {
                    output.close();
                    input.close();
                }
            } catch (Exception e) {
                postFailedCallbackMsg(requestId, e, null);
            }
        } catch (FileNotFoundException e) {
            postFailedCallbackMsg(requestId, e, null);
        } finally {
            postFinishCallback(requestId);
        }
    }

    private void postProgresCallbackMsg(final String requestId, final String fileName, final long downloaded, final long contentLength) {
        if (requestCallBack != null && requestCallBack instanceof ServiceManager.RequestCallBackWithProgress) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ((ServiceManager.RequestCallBackWithProgress) requestCallBack).inProgress(requestId, fileName, downloaded, contentLength);
                }
            });
        }
    }

    private void postSuccessCallbackMsg(final String requestId, final HttpResponse response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestCallBack.onSuccess(requestId, response);
            }
        });
    }

    private void postFailedCallbackMsg(final String requestId, final Throwable throwable, final Response response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestCallBack.onFailure(requestId, throwable, (response == null) ? null : new HttpResponse(response));
            }
        });
    }

    private void postFinishCallback(final String requestId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestCallBack.onFinish(requestId);
            }
        });
    }
}
