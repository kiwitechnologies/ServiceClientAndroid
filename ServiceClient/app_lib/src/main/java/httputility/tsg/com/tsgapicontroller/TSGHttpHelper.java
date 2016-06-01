/*
 * Copyright (c) 2016 Kiwitech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package httputility.tsg.com.tsgapicontroller;

import java.util.HashMap;

import httputility.tsg.com.tsgapicontroller.beans.API;
import httputility.tsg.com.tsghttpcontroller.HttpUtils;

/**
 * Created by kiwitech on 16/05/16.
 */
class TSGHttpHelper {

    private final API action;
    private final HashMap<String, String> query_params;
    private final HashMap<String, String> body_params;
    private final HashMap<String, String> header;
    private final String filePathForDownload;

    public TSGHttpHelper(API action, HashMap<String, String> query_params, HashMap<String, String> body_params, HashMap<String, String> header, String filePathForDownload) {
        this.action = action;
        this.query_params = query_params;
        this.body_params = body_params;
        this.header = header;
        this.filePathForDownload = filePathForDownload;
    }

    public static HttpUtils createRequest(API action, HashMap<String, String> query_params, HashMap<String, String> body_params, HashMap<String, String> header) {
        return createRequest(action, query_params, body_params, header, null);
    }

    public static HttpUtils createRequest(API action, HashMap<String, String> query_params, HashMap<String, String> body_params, HashMap<String, String> header, String filePathForDownload) {
        TSGHttpHelper tsgHttpHelper = new TSGHttpHelper(action, query_params, body_params, header, filePathForDownload);
        return tsgHttpHelper.createRequest();
    }

    private HttpUtils createRequest() {
        HttpUtils.RequestBuilder requestBuilder = null;
        if (action.getRequest_type().equalsIgnoreCase("GET")) {
            requestBuilder = new HttpUtils.GetRequestBuilder();
        } else if (action.getRequest_type().equalsIgnoreCase("POST")) {
            requestBuilder = new HttpUtils.PostRequestBuilder();
            ((HttpUtils.PostRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("PUT")) {
            requestBuilder = new HttpUtils.PutRequestBuilder();
            ((HttpUtils.PutRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("DELETE")) {
            requestBuilder = new HttpUtils.DeleteRequestBuilder();
            ((HttpUtils.DeleteRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("UPLOAD")) {
            requestBuilder = new HttpUtils.FileUploadRequestBuilder();
            ((HttpUtils.FileUploadRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("DOWNLOAD")) {
            requestBuilder = new HttpUtils.FileDownloadRequestBuilder();
            ((HttpUtils.FileDownloadRequestBuilder) requestBuilder).setFilePath(filePathForDownload);
        } else {
            throw new IllegalArgumentException("Invalid action type in api");
        }
        requestBuilder.setQueryParameters(query_params);
        requestBuilder.setHeaders(header);
        requestBuilder.setRequestId(action.getAction_id());
        requestBuilder.setSubURL(action.getBase_url() + action.getAction());

        return requestBuilder.build();
    }
}
