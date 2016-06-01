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

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by kiwitech on 12/04/16.
 */
public class HttpRequestFactory {

    private final HttpUtils httpUtils;
    private final HttpUtils.RequestCallBack requestCallBack;
    private Request.Builder requestBuilder;

    private HttpRequestFactory(HttpUtils httpUtils, HttpUtils.RequestCallBack requestCallBack) {
        this.httpUtils = httpUtils;
        this.requestCallBack = requestCallBack;
        requestBuilder = new Request.Builder();
    }

    public static Request getRequest(HttpUtils httpUtils, HttpUtils.RequestCallBack requestCallBack) throws IOException {
        if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.GET) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createGetRequest();
        } else if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.POST) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createPostRequest();
        } else if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.PUT) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createPutRequest();
        } else if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.DELETE) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createDeleteRequest();
        } else if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.DOWNLOAD_FILE) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createGetRequest();
        } else if (httpUtils.getHTTPRequestType() == HttpConstants.HTTPRequestType.UPLOAD_FILE) {
            return new HttpRequestFactory(httpUtils, requestCallBack).createPostMultipartFileRequest();
        }
        return null;
    }

    private Request createGetRequest() {
        addHeaders();
        addQueryStrings();
        return requestBuilder.build();
    }

    private Request createPostRequest() {
        addHeaders();
        addQueryStrings();
        RequestBody requestBody = addBodyParametersJSONData();
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }

    private Request createPutRequest() {
        addHeaders();
        addQueryStrings();
        RequestBody requestBody = addBodyParametersJSONData();
        requestBuilder.put(requestBody);
        return requestBuilder.build();
    }

    private Request createDeleteRequest() {
        addHeaders();
        addQueryStrings();
        RequestBody requestBody = addBodyParametersJSONData();
        requestBuilder.delete(requestBody);
        return requestBuilder.build();
    }

    private Request createPostMultipartFileRequest() throws IOException {
        addHeaders();
        addQueryStrings();
        addMultipartPostBodyData();
        return requestBuilder.build();
    }

    private void addHeaders() {
        //Add header data
        if (httpUtils.getHeaders() != null && httpUtils.getHeaders().size() > 0) {
            HashMap<String, String> headersParam = httpUtils.getHeaders();

            ArrayList keys = new ArrayList(headersParam.keySet());
            for (int i = 0; i < keys.size(); i++) {
                requestBuilder.addHeader(keys.get(i).toString(), headersParam.get(keys.get(i)).toString());
            }
        }
    }

    private FormBody addBodyParametersData() {
        //Add post body data
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (httpUtils.getBody_params() != null && httpUtils.getBody_params().size() > 0) {
            HashMap<String, String> requestBody = httpUtils.getBody_params();
            ArrayList keys = new ArrayList(requestBody.keySet());
            for (int i = 0; i < keys.size(); i++) {
                formBodyBuilder.add(keys.get(i).toString(), requestBody.get(keys.get(i)).toString());
            }
        }
        return formBodyBuilder.build();
    }

    private RequestBody addBodyParametersJSONData() {
        if (httpUtils.getBody_params() != null && httpUtils.getBody_params().size() > 0) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = httpUtils.getBody_params().toString();
            try {
                String mapAsJson = new ObjectMapper().writeValueAsString(httpUtils.getBody_params());
                RequestBody body = RequestBody.create(JSON, mapAsJson);
                return body;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return new FormBody.Builder().build();
        }
    }

    private void addQueryStrings() {
        //Add get query string
        HttpUrl.Builder urlBuilder = HttpUrl.parse(httpUtils.getRequestedURL()).newBuilder();
        if (httpUtils.getQuery_params() != null && httpUtils.getQuery_params().size() > 0) {
            HashMap<String, String> queryParam = httpUtils.getQuery_params();
            ArrayList keys = new ArrayList(queryParam.keySet());
            for (int i = 0; i < keys.size(); i++) {
                urlBuilder.addQueryParameter(keys.get(i).toString(), queryParam.get(keys.get(i)).toString());
            }
        }
        requestBuilder.url(urlBuilder.build());
    }

    private void addMultipartPostBodyData() {

        MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder();
        multipartBodybuilder.setType(MultipartBody.FORM);

        //Add post body data
        if (httpUtils.getBody_params() != null && httpUtils.getBody_params().size() > 0) {

            HashMap<String, String> requestBodyParams = httpUtils.getBody_params();
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            ArrayList keys = new ArrayList(requestBodyParams.keySet());

            for (int i = 0; i < keys.size(); i++) {
                if (httpUtils.getMultipartKeyNamesSet().contains(keys.get(i))) {
                    File file = new File(requestBodyParams.get(keys.get(i)).toString());
                    multipartBodybuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + keys.get(i) + "\"; filename=\"" + file.getName() + "\""),
                            new CountingFileRequestBody(file, httpUtils.getImage_quality(), HttpConstants.getMimeType(file.getName()), new CountingFileRequestBody.ProgressListener() {
                                @Override
                                public void inProgress(String requestId, String fileName, long num, long totalSize) {
                                    if (requestCallBack != null && requestCallBack instanceof HttpUtils.RequestCallBackWithProgress) {
                                        ((HttpUtils.RequestCallBackWithProgress) requestCallBack).inProgress(httpUtils.getRequestId(), fileName, num, totalSize);
                                    }
                                }
                            })
                    );
                } else {
                    multipartBodybuilder.addFormDataPart(keys.get(i).toString(), requestBodyParams.get(keys.get(i)).toString());
                }
            }

            RequestBody requestBody = multipartBodybuilder.build();
            requestBuilder.post(requestBody);
        }
    }
}
