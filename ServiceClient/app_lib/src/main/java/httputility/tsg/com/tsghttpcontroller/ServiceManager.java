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
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import okhttp3.Response;

public final class ServiceManager {

    private static String BASE_URL;
    private static HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor();
    private final HashMap<String, String> path_parameter;
    private final boolean executeSequentially;
    private Context context; // Only available incase of sequential execution

    private HttpConstants.HTTPRequestType HTTPRequestType;
    private String requestId;
    private String subURL = "";
    private String downloadFilePath;
    private HashMap<String, String> headers;
    private RequestBodyParams body_params;
    private HashMap<String, String> query_params;
    private HashSet<String> multipartKeyNamesSet;
    private HttpConstants.IMAGE_QUALITY image_quality;
    private long requestTime;
    private boolean executeOnPriority = false;

    private ServiceManager(@NonNull HttpConstants.HTTPRequestType httpRequestType, String requestId, @NonNull String subURL, HashMap<String, String> path_parameter, HashMap<String, String> headers, HashMap<String, String> query_params, RequestBodyParams body_params, boolean executeSequentially) {
        this.HTTPRequestType = httpRequestType;
        this.requestId = requestId;
        this.subURL = subURL;
        this.path_parameter = path_parameter;
        this.headers = headers;
        this.query_params = query_params;
        this.body_params = body_params;
        this.executeSequentially = executeSequentially;

        if (this.subURL == null || "".equals(subURL)) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public static void init(String baseURL) {
        BASE_URL = baseURL;
    }


    public Context getContext() {
        return context;
    }

    /**
     * call this method incase you want to hit api from background thread
     *
     * @return Response from the server
     * @throws IOException
     */
    @WorkerThread
    public Response doRequest() throws IOException {
        requestTime = System.currentTimeMillis();
        return httpRequestExecutor.execute(this);
    }

    /**
     * This method can be call from main ui thread to get the response from server
     *
     * @param requestCallBack instance of RequestCallBack
     */
    @MainThread
    public void enqueRequest(RequestCallBack requestCallBack) {
        requestTime = System.currentTimeMillis();
        httpRequestExecutor.enqueParallelRequest(this, requestCallBack);
    }

    /**
     * Call this method in case you want to show progress of your request
     *
     * @param requestCallBackWithProgress instance of {@link RequestCallBackWithProgress} to track progress
     */
    @MainThread
    public void enqueFileRequestWithProgress(RequestCallBackWithProgress requestCallBackWithProgress) {
        enqueFileRequestWithProgress(null, requestCallBackWithProgress, HttpConstants.IMAGE_QUALITY.DEFALUT);
    }

    /**
     * Call this method in case you want to show progress of your request
     *
     * @param multipartKeyNamesSet        instance of {@link HashSet}, it should have all the body parameter name in which there is a file to upload.
     * @param requestCallBackWithProgress instance of {@link RequestCallBackWithProgress} to track progress
     * @param image_quality               member of {@link HttpConstants.IMAGE_QUALITY} to change the image quality before uploading it.
     */
    public void enqueFileRequestWithProgress(HashSet<String> multipartKeyNamesSet, RequestCallBackWithProgress requestCallBackWithProgress, HttpConstants.IMAGE_QUALITY image_quality) {
        requestTime = System.currentTimeMillis();
        this.multipartKeyNamesSet = multipartKeyNamesSet;
        this.image_quality = image_quality;
        if (executeSequentially) {
            httpRequestExecutor.enqueSequentialRequest(this, requestCallBackWithProgress);
        } else {
            httpRequestExecutor.enqueParallelRequest(this, requestCallBackWithProgress);
        }
    }

    public String getRequestedURL() {
        if (subURL.toLowerCase().startsWith("http")) {
            return subURL;
        } else {
            if (BASE_URL != null) {
                return BASE_URL + subURL;
            } else {
                return subURL;
            }
        }
    }

    public long getRequestTime() {
        return requestTime;
    }

    public HttpConstants.HTTPRequestType getHTTPRequestType() {
        return HTTPRequestType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSubURL() {
        return subURL;
    }

    public HashSet<String> getMultipartKeyNamesSet() {
        return multipartKeyNamesSet;
    }

    public HashMap<String, String> getPath_parameter() {
        return path_parameter;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public RequestBodyParams getBody_params() {
        return body_params;
    }

    public HashMap<String, String> getQuery_params() {
        return query_params;
    }

    public String getDownloadFilePath() {
        return downloadFilePath;
    }

    public HttpConstants.IMAGE_QUALITY getImage_quality() {
        return image_quality;
    }

    void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public void setContext(Context context, boolean executeOnPriority) {
        this.context = context;
        this.executeOnPriority = executeOnPriority;
    }

    public boolean isExecuteOnPriority() {
        return executeOnPriority;
    }

    boolean isDonwloadFileRequest() {
        return (downloadFilePath != null && !downloadFilePath.equals(""));
    }

    /**
     * RequestCallBack It listen you back the response
     */
    public interface RequestCallBack extends Serializable {

        void onSuccess(String requestId, HttpResponse response);

        void onFailure(String requestId, Throwable throwable, HttpResponse errorResponse);

        void onFinish(String requestId);
    }

    public interface RequestCallBackWithProgress extends CountingFileRequestBody.ProgressListener, RequestCallBack, Serializable {
    }

    public static abstract class RequestBuilder {

        private String requestId;
        String subURL = "";

        HashMap<String, String> headers;
        HashMap<String, String> queryParameters;
        HashMap<String, String> pathParameters;

        public abstract ServiceManager build();

        public void setSubURL(String subURL) {
            this.subURL = subURL;
        }

        public void setRequestId(@Nullable String requestId) {
            this.requestId = requestId;
        }

        public void setHeaders(@Nullable HashMap<String, String> headers) {
            this.headers = headers;
        }

        public void setQueryParameters(@Nullable HashMap<String, String> queryParameters) {
            this.queryParameters = queryParameters;
        }

        public void setPathParameters(HashMap<String, String> path_params) {
            this.pathParameters = path_params;
        }

        String getRequestId() {
            return (requestId == null || requestId.equals("")) ? "0" : requestId;
        }
    }

    static abstract class SequentialRequestBuilder extends RequestBuilder {
        boolean downloadSequentially;
        Context context;
        boolean executeOnPriority = false;


        public void setDownloadSequentially(Context context) {
            this.context = context;
            downloadSequentially = true;
        }

        public void setDownloadParallaly() {
            downloadSequentially = false;
        }

        public void setExecuteOnPriority() {
            executeOnPriority = true;
        }

    }

    public static class GetRequestBuilder extends RequestBuilder {

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.GET, getRequestId(), subURL, pathParameters, headers, queryParameters, null, false);
        }
    }

    public static class PostRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.POST, getRequestId(), subURL, pathParameters, headers, queryParameters, requestBody, false);
        }
    }

    public static class PutRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.PUT, getRequestId(), subURL, pathParameters, headers, queryParameters, requestBody, false);
        }
    }

    public static class DeleteRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.DELETE, getRequestId(), subURL, pathParameters, headers, queryParameters, requestBody, false);
        }
    }

    public static class FileUploadRequestBuilder extends SequentialRequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            ServiceManager serviceManager = new ServiceManager(HttpConstants.HTTPRequestType.UPLOAD_FILE, getRequestId(), subURL, pathParameters, headers, queryParameters, requestBody, downloadSequentially);
            if (downloadSequentially) {
                serviceManager.setContext(context, executeOnPriority);
            }

            return serviceManager;
        }
    }

    public static class FileDownloadRequestBuilder extends SequentialRequestBuilder {
        private HttpConstants.HTTPRequestType requestType = HttpConstants.HTTPRequestType.GET;
        private RequestBodyParams requestBody;
        private String filePath;


        public FileDownloadRequestBuilder() {
        }

        public FileDownloadRequestBuilder(HttpConstants.HTTPRequestType requestType) {
            this.requestType = requestType;
            if (requestType == HttpConstants.HTTPRequestType.UPLOAD_FILE) {
                throw new IllegalArgumentException("Invalid request type. Supported request types are : GET, POST, PUT, DELETE");
            }
        }

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public void setFilePath(@NonNull String filePath) {
            this.filePath = filePath;
        }

        public void setFilePath(@NonNull String fileDirectory, @NonNull String fileName) {
            this.filePath = fileDirectory + File.separatorChar + fileName;
        }

        @Override
        public ServiceManager build() {
            ServiceManager serviceManager = new ServiceManager(requestType, getRequestId(), subURL, pathParameters, headers, queryParameters, requestBody, downloadSequentially);
            serviceManager.setDownloadFilePath(filePath);
            if (downloadSequentially) {
                serviceManager.setContext(context, executeOnPriority);
            }
            return serviceManager;
        }
    }

}
