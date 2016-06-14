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

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import okhttp3.Response;

public final class ServiceManager {

    private static String BASE_URL;
    private static HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor();
    private final HashMap<String, String> path_parameter;

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

    private ServiceManager(@NonNull HttpConstants.HTTPRequestType httpRequestType, String requestId, @NonNull String subURL, HashMap<String, String> path_parameter, HashMap<String, String> headers, HashMap<String, String> query_params, RequestBodyParams body_params) {
        this(httpRequestType, requestId, subURL, path_parameter, headers, query_params, body_params, null);
    }

    public ServiceManager(@NonNull HttpConstants.HTTPRequestType httpRequestType, @Nullable String requestId, @NonNull String subURL, HashMap<String, String> path_parameter, HashMap<String, String> headers, HashMap<String, String> query_params, RequestBodyParams body_params, String downloadFilePath) {
        this.HTTPRequestType = httpRequestType;
        this.requestId = requestId;
        this.subURL = subURL;
        this.path_parameter = path_parameter;
        this.headers = headers;
        this.query_params = query_params;
        this.body_params = body_params;
        this.downloadFilePath = downloadFilePath;

        if (this.subURL == null || "".equals(subURL)) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public static void init(String baseURL) {
        BASE_URL = baseURL;
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
        httpRequestExecutor.enqueRequest(this, requestCallBack);
    }

    /**
     * Call this method in case you want to do a multipart request
     *
     * @param requestCallBackWithProgress instance of {@link RequestCallBackWithProgress} to track progress
     */
    @MainThread
    public void enqueFileRequestWithProgress(RequestCallBackWithProgress requestCallBackWithProgress) {
        enqueFileRequestWithProgress(null, requestCallBackWithProgress, HttpConstants.IMAGE_QUALITY.DEFALUT);
    }

    /**
     * Call this method in case you want to do a multipart request
     *
     * @param multipartKeyNamesSet        instance of {@link HashSet}, it should have all the body parameter name in which there is a file to upload.
     * @param requestCallBackWithProgress instance of {@link RequestCallBackWithProgress} to track progress
     * @param image_quality               member of {@link HttpConstants.IMAGE_QUALITY} to change the image quality before uploading it.
     */
    public void enqueFileRequestWithProgress(HashSet<String> multipartKeyNamesSet, RequestCallBackWithProgress requestCallBackWithProgress, HttpConstants.IMAGE_QUALITY image_quality) {
        requestTime = System.currentTimeMillis();
        this.multipartKeyNamesSet = multipartKeyNamesSet;
        this.image_quality = image_quality;
        httpRequestExecutor.enqueRequest(this, requestCallBackWithProgress);
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

    /**
     * RequestCallBack It listen you back the response
     */
    public interface RequestCallBack {

        void onFailure(String requestId, Throwable throwable, HttpResponse errorResponse);

        void onSuccess(String requestId, HttpResponse response);

        void onFinish(String requestId);
    }

    public interface RequestCallBackWithProgress extends CountingFileRequestBody.ProgressListener, RequestCallBack {
    }

    public static abstract class RequestBuilder {

        String requestId;
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
    }

    public static class GetRequestBuilder extends RequestBuilder {

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.GET, requestId, subURL, pathParameters, headers, queryParameters, null);
        }
    }

    public static class PostRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.POST, requestId, subURL, pathParameters, headers, queryParameters, requestBody);
        }
    }

    public static class PutRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.PUT, requestId, subURL, pathParameters, headers, queryParameters, requestBody);
        }
    }

    public static class DeleteRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.DELETE, requestId, subURL, pathParameters, headers, queryParameters, requestBody);
        }
    }

    public static class FileUploadRequestBuilder extends RequestBuilder {

        private RequestBodyParams requestBody;

        public void setRequestBody(@Nullable RequestBodyParams requestBody) {
            this.requestBody = requestBody;
        }

        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.UPLOAD_FILE, requestId, subURL, pathParameters, headers, queryParameters, requestBody);
        }
    }

    public static class FileDownloadRequestBuilder extends GetRequestBuilder {
        private String filePath;

        public void setFilePath(@NonNull String filePath) {
            this.filePath = filePath;
        }

        public void setFilePath(@NonNull String fileDirectory, @NonNull String fileName) {
            this.filePath = fileDirectory + File.separatorChar + fileName;
        }

        @Override
        public ServiceManager build() {
            return new ServiceManager(HttpConstants.HTTPRequestType.DOWNLOAD_FILE, requestId, subURL, pathParameters, headers, queryParameters, null, filePath);
        }
    }

}
