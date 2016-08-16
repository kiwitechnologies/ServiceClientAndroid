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

import android.content.Context;

import java.util.HashMap;

import httputility.tsg.com.tsgapicontroller.beans.API;
import httputility.tsg.com.tsghttpcontroller.HttpConstants;
import httputility.tsg.com.tsghttpcontroller.ServiceManager;
import httputility.tsg.com.tsghttpcontroller.RequestBodyParams;

/**
 * Created by kiwitech on 16/05/16.
 */
class TSGHttpHelper {

    private final API action;
    private final HashMap<String, String> query_params;
    private final RequestBodyParams body_params;
    private final HashMap<String, String> header;
    private final String filePathForDownload;
    private final HashMap<String, String> path_params;
    private final boolean executeParallely;
    private Context mContext;

    public TSGHttpHelper(Context context, API action, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params, HashMap<String, String> header, boolean executeParallely, String filePathForDownload) {
        this.mContext = context;
        this.action = action;
        this.path_params = path_params;
        this.query_params = query_params;
        this.body_params = body_params;
        this.header = header;
        this.executeParallely = executeParallely;
        this.filePathForDownload = filePathForDownload;
    }

    public static ServiceManager createRequest(Context context, API action, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params, HashMap<String, String> header, boolean executeParallely) {
        return createRequest(context, action, path_params, query_params, body_params, header, executeParallely, null);
    }

    public static ServiceManager createRequest(Context context, API action, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params, HashMap<String, String> header, boolean executeParallely, String filePathForDownload) {
        TSGHttpHelper tsgHttpHelper = new TSGHttpHelper(context, action, path_params, query_params, body_params, header, executeParallely, filePathForDownload);
        return tsgHttpHelper.createRequest();
    }

    private ServiceManager createRequest() {
        ServiceManager.RequestBuilder requestBuilder = null;
        if (action.getRequest_type().equalsIgnoreCase("GET")) {
            requestBuilder = new ServiceManager.GetRequestBuilder();
        } else if (action.getRequest_type().equalsIgnoreCase("POST")) {
            requestBuilder = new ServiceManager.PostRequestBuilder();
            ((ServiceManager.PostRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("PUT")) {
            requestBuilder = new ServiceManager.PutRequestBuilder();
            ((ServiceManager.PutRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("DELETE")) {
            requestBuilder = new ServiceManager.DeleteRequestBuilder();
            ((ServiceManager.DeleteRequestBuilder) requestBuilder).setRequestBody(body_params);
        } else if (action.getRequest_type().equalsIgnoreCase("UPLOAD")) {
            requestBuilder = new ServiceManager.FileUploadRequestBuilder(HttpConstants.HTTPRequestType.POST);
            ((ServiceManager.FileUploadRequestBuilder) requestBuilder).setRequestBody(body_params);
            if (!executeParallely) {
                ((ServiceManager.FileUploadRequestBuilder) requestBuilder).setSequential(mContext);
            }
        } else if (action.getRequest_type().equalsIgnoreCase("DOWNLOAD")) {
            requestBuilder = new ServiceManager.FileDownloadRequestBuilder();
            ((ServiceManager.FileDownloadRequestBuilder) requestBuilder).setFilePath(filePathForDownload);
            if (!executeParallely) {
                ((ServiceManager.FileUploadRequestBuilder) requestBuilder).setSequential(mContext);
            }
        } else {
            throw new IllegalArgumentException("Invalid action type in api");
        }
        if (action.getParams_parameters() == 1) {
            requestBuilder.setPathParameters(path_params);
        }

        if (TSGAPIController.BUILD_FLAVOR.getBuildFlavor(mContext) == TSGAPIController.BUILD_FLAVOR.DUMMY_SERVER) {
            HashMap<String, String> dummyServerResponse = new HashMap<>();
            dummyServerResponse.put(Constants.STATUS_CODE, TSGAPIController.getDummyServerResponseCode(mContext) + "");
            requestBuilder.setQueryParameters(dummyServerResponse);
        } else {
            requestBuilder.setQueryParameters(query_params);
        }
        requestBuilder.setHeaders(header);
        requestBuilder.setRequestId(action.getAction_id());

        if (TSGAPIController.BUILD_FLAVOR.getBuildFlavor(mContext) == TSGAPIController.BUILD_FLAVOR.DUMMY_SERVER) {
            requestBuilder.setSubURL(action.getBase_url());
        } else {
            requestBuilder.setSubURL(action.getBase_url() + action.getAction());
        }

        return requestBuilder.build();
    }
}
