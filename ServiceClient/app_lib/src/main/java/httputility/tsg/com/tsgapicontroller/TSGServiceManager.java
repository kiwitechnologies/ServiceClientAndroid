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

import android.app.Service;
import android.content.Context;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import httputility.tsg.com.tsgapicontroller.Logger.TSGErrorManager;
import httputility.tsg.com.tsgapicontroller.beans.API;
import httputility.tsg.com.tsgapicontroller.beans.Project;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;
import httputility.tsg.com.tsgapicontroller.validation.Error;
import httputility.tsg.com.tsgapicontroller.validation.TSGValidatorManager;
import httputility.tsg.com.tsghttpcontroller.HttpConstants;
import httputility.tsg.com.tsghttpcontroller.HttpRequestExecutor;
import httputility.tsg.com.tsghttpcontroller.HttpResponse;
import httputility.tsg.com.tsghttpcontroller.ServiceManager;
import httputility.tsg.com.tsghttpcontroller.RequestBodyParams;
import httputility.tsg.com.tsghttpcontroller.Utility;

/**
 * Created by Ashish Rajvanshi on 04/05/16.
 */

/**
 * <pre>
 * This class provide the functionality to excecute the http request.
 * Whole requests are divided into three parts
 * 1. doRequest
 * 2. enqueRequest
 * 3. enqueMultipartFileUploadRequest
 *
 * <b>doRequest - </b> These type of functions used to call the api from the same thread. So if you are working on background thread and wants to fetch some data only then use this method otherwise in main thread it will throw {@link android.os.NetworkOnMainThreadException} exception.
 * <b>enqueRequest - </b> These type of functions runs on different background thread and return the result in main thread using call back design. So you can safely call this function from the main thread.
 * <b>enqueMultipartFileUploadRequest - </b> These type of functions creates the multipart request to upload a file on server.
 *
 * PS: Some other functions are also here as - to fetch the version name of validation json file and to set/remove/get the headers.
 *
 * </pre>
 */
public final class TSGServiceManager {

    public static TSGErrorManager ERROR_LOGGER = new TSGErrorManager();
    private static HashMap<String, String> headers;
    public static TSGAPIController.BUILD_FLAVOR build_flavor = null;

    private TSGServiceManager() {
    }

    /**
     * call this method incase you want to hit api from the same thread
     *
     * @param context
     * @param actionId     action Id of API present in the api_validation.java file in Assets
     * @param query_params data for query parameters in form of instance of HashMap
     * @throws IOException incase of any IO exception occure.
     */
    public static void doRequest(Context context, String actionId, HashMap<String, String> query_params) throws IOException {
        doRequest(context, actionId, query_params, null);
    }

    /**
     * call this method incase you want to hit api from the same thread
     *
     * @param context
     * @param actionId     action Id of API present in the api_validation.java file in Assets
     * @param query_params data for query parameters in form of instance of HashMap
     * @param body_params  data for body parameters in form of instance of RequestBodyParams
     * @throws IOException incase of any IO exception occure.
     */
    public static void doRequest(Context context, String actionId, HashMap<String, String> query_params, RequestBodyParams body_params) throws IOException {
        doRequest(context, actionId, null, query_params, body_params);
    }

    /**
     * call this method incase you want to hit api from the same thread
     *
     * @param context
     * @param actionId     action Id of API present in the api_validation.java file in Assets
     * @param path_params  key value pair of path values in url
     * @param query_params data for query parameters in form of instance of HashMap
     * @param body_params  data for body parameters in form of instance of RequestBodyParams
     * @throws IOException incase of any IO exception occure.
     */
    public static void doRequest(Context context, String actionId, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params) throws IOException {
        if (build_flavor == null) {
            build_flavor = TSGAPIController.BUILD_FLAVOR.getBuildFlavor(context);
        }
        ERROR_LOGGER = new TSGErrorManager();
        if (null == actionId || "".equals(actionId)) {
            ERROR_LOGGER.getErr_actions().add(String.format(Error.ERR_EMPTY_ACTION_NAME, actionId));
            return;
        }

        TSGServiceManager TSGServiceManager = new TSGServiceManager();
        if (!TSGServiceManager.isDBInitialised(context)) {
            ERROR_LOGGER.getErr_mix().add(Error.ERR_DB_NOT_INITIALISED);
            return;
        }

        if (!Utility.isNetworkAvailable(context)) {
            throw new IOException(Error.ERR_NO_INTERNET_CONNECTION);
        }
        API action = API.getFromDB(context, actionId);
        if (action != null) {
            if (TSGValidatorManager.validate(action, path_params, query_params, body_params, getHeaders())) {
                ServiceManager serviceManager = TSGHttpHelper.createRequest(context, action, path_params, query_params, body_params, getHeaders(), true);
                serviceManager.doRequest();
            }
        } else {
            throw new IOException(ERROR_LOGGER.getLog());
        }
    }


    /**
     * call this method incase you want to hit api from the background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBack}
     */
    public static void enqueRequest(Context context, String actionId, HashMap<String, String> query_params, ServiceManager.RequestCallBack requestCallBack) {
        enqueRequest(context, actionId, query_params, null, requestCallBack);
    }

    /**
     * call this method incase you want to hit api from the background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param body_params     data for body parameters in form of instance of RequestBodyParams
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBack}
     */
    public static void enqueRequest(Context context, String actionId, HashMap<String, String> query_params, RequestBodyParams body_params, ServiceManager.RequestCallBack requestCallBack) {
        enqueRequest(context, actionId, null, query_params, body_params, requestCallBack);
    }

    /**
     * call this method incase you want to hit api from the background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param path_params     key value pair of path values in url
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param body_params     data for body parameters in form of instance of RequestBodyParams
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBack}
     */
    public static void enqueRequest(Context context, String actionId, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params, ServiceManager.RequestCallBack requestCallBack) {
        if (build_flavor == null) {
            build_flavor = TSGAPIController.BUILD_FLAVOR.getBuildFlavor(context);
        }
        ERROR_LOGGER = new TSGErrorManager();
        if (null == actionId || "".equals(actionId)) {
            ERROR_LOGGER.getErr_actions().add(String.format(Error.ERR_EMPTY_ACTION_NAME, actionId));
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(ERROR_LOGGER.getLog()));
            return;
        }

        TSGServiceManager TSGServiceManager = new TSGServiceManager();
        if (!TSGServiceManager.isDBInitialised(context)) {
            ERROR_LOGGER.getErr_mix().add(Error.ERR_DB_NOT_INITIALISED);
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(ERROR_LOGGER.getLog()));
            return;
        }

        if (!Utility.isNetworkAvailable(context)) {
            requestCallBack.onFailure(actionId, new IOException(Error.ERR_NO_INTERNET_CONNECTION), null);
            requestCallBack.onFinish(actionId);
            return;
        }
        API action = API.getFromDB(context, actionId);

        if (action == null) {
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(ERROR_LOGGER.getLog()));
        } else {
            if (TSGValidatorManager.validate(action, path_params, query_params, body_params, getHeaders())) {
                ServiceManager serviceManager = TSGHttpHelper.createRequest(context, action, path_params, query_params, body_params, getHeaders(), true);
                serviceManager.enqueRequest(requestCallBack);
            } else {
                respondFailure(requestCallBack, actionId, new IllegalArgumentException(TSGServiceManager.ERROR_LOGGER.getLog()));
            }
        }
    }

    /**
     * call this method for multipart form data request in background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBackWithProgress}
     */
    public static void enqueMultipartFileUploadRequest(Context context, String actionId, HashMap<String, String> query_params, ServiceManager.RequestCallBackWithProgress requestCallBack) {
        enqueMultipartFileUploadRequest(context, actionId, query_params, null, requestCallBack);
    }

    /**
     * call this method for multipart form data request in background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param body_params     data for body parameters in form of instance of RequestBodyParams
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBackWithProgress}
     */
    public static void enqueMultipartFileUploadRequest(Context context, String actionId, HashMap<String, String> query_params, RequestBodyParams body_params, ServiceManager.RequestCallBackWithProgress requestCallBack) {
        enqueMultipartFileUploadRequest(context, actionId, query_params, body_params, HttpConstants.IMAGE_QUALITY.DEFALUT, requestCallBack);
    }

    /**
     * call this method for multipart form data request in background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param body_params     data for body parameters in form of instance of RequestBodyParams
     * @param image_quality   instance of {@link HttpConstants.IMAGE_QUALITY} incase of image. It will help to compress the image based on you input quality in this
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBackWithProgress}
     */
    public static void enqueMultipartFileUploadRequest(Context context, String actionId, HashMap<String, String> query_params, RequestBodyParams body_params, HttpConstants.IMAGE_QUALITY image_quality, ServiceManager.RequestCallBackWithProgress requestCallBack) {
        enqueMultipartFileUploadRequest(context, actionId, null, query_params, body_params, image_quality, true, requestCallBack);
    }

    /**
     * call this method for multipart form data request in background thread
     *
     * @param context
     * @param actionId        action Id of API present in the api_validation.java file in Assets
     * @param path_params     key value pair of path values in url
     * @param query_params    data for query parameters in form of instance of HashMap
     * @param body_params     data for body parameters in form of instance of RequestBodyParams
     * @param image_quality   instance of {@link HttpConstants.IMAGE_QUALITY} incase of image. It will help to compress the image based on you input quality in this
     * @param requestCallBack callback instance of {@link ServiceManager.RequestCallBackWithProgress}
     */
    public static void enqueMultipartFileUploadRequest(Context context, String actionId, HashMap<String, String> path_params, HashMap<String, String> query_params, RequestBodyParams body_params, HttpConstants.IMAGE_QUALITY image_quality, boolean executeParallely, ServiceManager.RequestCallBackWithProgress requestCallBack) {
        if (build_flavor == null) {
            build_flavor = TSGAPIController.BUILD_FLAVOR.getBuildFlavor(context);
        }
        ERROR_LOGGER = new TSGErrorManager();
        if (null == actionId || "".equals(actionId)) {
            ERROR_LOGGER.getErr_actions().add(String.format(Error.ERR_EMPTY_ACTION_NAME, actionId));
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(ERROR_LOGGER.getLog()));
            return;
        }

        TSGServiceManager TSGServiceManager = new TSGServiceManager();
        if (!TSGServiceManager.isDBInitialised(context)) {
            ERROR_LOGGER.getErr_mix().add(Error.ERR_DB_NOT_INITIALISED);
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(ERROR_LOGGER.getLog()));
            return;
        }

        if (!Utility.isNetworkAvailable(context)) {
            requestCallBack.onFailure(actionId, new IOException(Error.ERR_NO_INTERNET_CONNECTION), null);
            requestCallBack.onFinish(actionId);
            return;
        }
        API action = API.getFromDB(context, actionId);

        if (action == null) {
            respondFailure(requestCallBack, actionId, new IllegalArgumentException(TSGServiceManager.ERROR_LOGGER.getLog()));
        } else {
            if (TSGValidatorManager.validate(action, path_params, query_params, body_params, getHeaders())) {
                action.setRequest_type("UPLOAD");
                ServiceManager serviceManager = TSGHttpHelper.createRequest(context, action, path_params, query_params, body_params, getHeaders(), executeParallely);
                HashSet<String> multipartKeyNamesSet = new HashSet<String>();
                for (int i = 0; i < action.getMultipartFileBody_parameters().length; i++) {
                    multipartKeyNamesSet.add(action.getMultipartFileBody_parameters()[i].getKey_name());
                }
                serviceManager.enqueFileRequestWithProgress(multipartKeyNamesSet, requestCallBack, image_quality);
            } else {
                respondFailure(requestCallBack, actionId, new IllegalArgumentException(TSGServiceManager.ERROR_LOGGER.getLog()));
            }
        }
    }

    /**
     * call this function if you want to cancel all enqued requests
     *
     * @return true if cancelled or false if unsuccessfull
     */
    public static boolean cancelAllReqeust() {
        return HttpRequestExecutor.cancelAllReqeust();
    }

    /**
     * call this function if you want to cancel a perticular enqued request using "actionId"
     *
     * @param actionId pass action id that you have give in case of enqueing a request
     * @return true if successfully cancelled otherwise false
     */
    public static boolean cancelReqeust(String actionId) {
        return HttpRequestExecutor.cancelAllRequestWith(actionId);
    }

    /**
     * return all headers that are currently passing in all request
     *
     * @return instance of HashMap of headers
     */
    public static HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * call this function to set the header for all request. These header will share between all request. You can also modify or remove the headers using {@link TSGServiceManager#setHeaders(HashMap)}  and {@link TSGServiceManager#removeHeaders()} funtion
     *
     * @param headers instance of HashMap of headers to set
     */
    public static void setHeaders(HashMap<String, String> headers) {
        TSGServiceManager.headers = headers;
    }

    /**
     * call this function incase you want to remove all headers
     */
    public static void removeHeaders() {
        TSGServiceManager.headers = null;
    }


    /**
     * This function fetch the version name from local db and from server and return both version name in json response.
     *
     * @param context
     * @param requestCallBack callback to get the response
     * @throws IOException Incase you have not initialise the application from {@link TSGAPIController#init(Context, TSGAPIController.BUILD_FLAVOR)} class
     */
    public static void getVersionName(Context context, final ServiceManager.RequestCallBack requestCallBack) throws IOException {
        final Project apiInfo = Project.getFromDB(context);
        if (apiInfo.getProject_id() == null) {
            throw new IOException("init function not called of TSGAPIController class");
        }

        ServiceManager.GetRequestBuilder builder = new ServiceManager.GetRequestBuilder();

        builder.setSubURL(String.format(Constants.URL_GET_API_INFO_VERSION, apiInfo.getProject_id()));
        builder.build().enqueRequest(new ServiceManager.RequestCallBack() {
            @Override
            public void onFailure(String requestId, Throwable throwable, HttpResponse errorResponse) {
                requestCallBack.onFailure(requestId, throwable, errorResponse);
            }

            @Override
            public void onSuccess(String requestId, HttpResponse response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.getBody().toString());
                    double serverSideVersionNo = jsonObject.getDouble("version_no");

                    JSONObject resultObject = new JSONObject();
                    resultObject.put("server_api_version_no", serverSideVersionNo + "");
                    resultObject.put("local_api_version_no", apiInfo.getVersion_no());

                    if ((serverSideVersionNo + "").equals(apiInfo.getVersion_no())) {
                        resultObject.put("result", "Success! Version numbers are matching");
                    } else {
                        resultObject.put("result", "Error! Version numbers mis-matched");
                    }

                    HttpResponse resultResponse = new HttpResponse(response.getCode(), resultObject.toString(), response.isSuccessful(), response.getMessage(), response.isRedirect());
                    requestCallBack.onSuccess(requestId, resultResponse);
                } catch (JSONException e) {
                    requestCallBack.onFailure(requestId, e, null);
                }
            }

            @Override
            public void onFinish(String requestId) {
                requestCallBack.onFinish(requestId);

            }
        });
    }

    public static void setEnableDebugging(boolean enableDebugging) {
        ServiceManager.setEnableDebugging(enableDebugging);
    }

    private static void respondFailure(ServiceManager.RequestCallBack requestCallBack, String requestId, IllegalArgumentException e) {
        requestCallBack.onFailure(requestId, e, null);
        requestCallBack.onFinish(requestId);
    }

    private boolean isDBInitialised(Context context) {
        boolean dbInitialised = false;
        Cursor cursor = APIDBManager.getInstance(context).query(APIDBManager.KEY_API_INFO, null, null, null, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            dbInitialised = true;
        }
        closeCursor(cursor);
        return dbInitialised;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}