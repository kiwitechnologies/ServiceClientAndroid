/*
 * Copyright (c) 2016 Kiwitech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package httputility.tsg.com.tsgapicontroller.beans;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;

import httputility.tsg.com.tsgapicontroller.TSGAPIController;
import httputility.tsg.com.tsgapicontroller.TSGHttpUtility;
import httputility.tsg.com.tsgapicontroller.storage.APIContract;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;
import httputility.tsg.com.tsgapicontroller.validation.Error;

/**
 * Created by kiwitech on 03/05/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class API {

    private int id;
    private String action_id;
    private String action;
    private String request_type;
    private String qa_url;
    private String staging_url;
    private String production_url;
    private String dev_url;
    private BodyParameter[] body_parameters;
    private QueryParameter[] query_parameters;
    private Header[] headers;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction_id() {
        return action_id;
    }

    public void setAction_id(String action_id) {
        this.action_id = action_id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getBase_url() {
        if (TSGHttpUtility.build_flavor == TSGAPIController.BUILD_FLAVOR.PRODUCTION)
            return getProduction_url();
        else if (TSGHttpUtility.build_flavor == TSGAPIController.BUILD_FLAVOR.QA)
            return getQa_url();
        else if (TSGHttpUtility.build_flavor == TSGAPIController.BUILD_FLAVOR.STAGING)
            return getStaging_url();
        return getDev_url();
    }

    public String getQa_url() {
        return qa_url;
    }

    public void setQa_url(String qa_url) {
        this.qa_url = qa_url;
    }

    public String getStaging_url() {
        return staging_url;
    }

    public void setStaging_url(String staging_url) {
        this.staging_url = staging_url;
    }

    public String getProduction_url() {
        return production_url;
    }

    public void setProduction_url(String production_url) {
        this.production_url = production_url;
    }

    public String getDev_url() {
        return dev_url;
    }

    public void setDev_url(String dev_url) {
        this.dev_url = dev_url;
    }

    public BodyParameter[] getAllBody_parameters() {
        return body_parameters;
    }

    public BodyParameter[] getPlainBody_parameters() {
        ArrayList<BodyParameter> bodyParameters = new ArrayList<BodyParameter>();
        for (int i = 0; i < body_parameters.length; i++) {
            if (!body_parameters[i].isMultipartFileRequest()) {
                bodyParameters.add(body_parameters[i]);
            }
        }
        return (BodyParameter[]) bodyParameters.toArray();
    }

    public QueryParameter[] getQuery_parameters() {
        return query_parameters;
    }

    public void setQuery_parameters(QueryParameter[] query_parameters) {
        this.query_parameters = query_parameters;
    }

    public BodyParameter[] getMultipartFileBody_parameters() {
        ArrayList<BodyParameter> bodyParameters = new ArrayList<BodyParameter>();
        for (int i = 0; i < body_parameters.length; i++) {
            if (body_parameters[i].isMultipartFileRequest()) {
                bodyParameters.add(body_parameters[i]);
            }
        }
        BodyParameter[] bodyParametersArr = new BodyParameter[bodyParameters.size()];
        for (int i = 0; i < bodyParameters.size(); i++) {
            bodyParametersArr[i] = bodyParameters.get(i);
        }
        return bodyParametersArr;
    }

    public void setBody_parameters(BodyParameter[] body_parameters) {
        this.body_parameters = body_parameters;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public void insertIntoDB(Context mContext) {

        ContentResolver contentResolver = mContext.getContentResolver();

        ContentValues cvAction = new ContentValues();
        cvAction.put(APIContract.ActionsTable.ACTION_ID, getAction_id());
        cvAction.put(APIContract.ActionsTable.ACTION, getAction());
        cvAction.put(APIContract.ActionsTable.REQUEST_TYPE, getRequest_type());
        cvAction.put(APIContract.ActionsTable.DEV_BASE_URL, getDev_url());
        cvAction.put(APIContract.ActionsTable.QA_BASE_URL, getQa_url());
        cvAction.put(APIContract.ActionsTable.PROD_BASE_URL, getProduction_url());
        cvAction.put(APIContract.ActionsTable.STAGE_BASE_URL, getStaging_url());
        long actionId = APIDBManager.getInstance(mContext).insert(APIDBManager.KEY_ACTION, cvAction);

        for (int i = 0; null != getAllBody_parameters() && i < getAllBody_parameters().length; i++) {
            getAllBody_parameters()[i].insertIntoDB(mContext, actionId);
        }

        for (int j = 0; null != getQuery_parameters() && j < getQuery_parameters().length; j++) {
            getQuery_parameters()[j].insertIntoDB(mContext, actionId);
        }

        for (int k = 0; null != getHeaders() && k < getHeaders().length; k++) {
            getHeaders()[k].insertIntoDB(mContext, actionId);
        }
    }


    public static API getFromDB(Context context, String actionId) {
        Cursor curAction = APIDBManager.getInstance(context).query(APIDBManager.KEY_ACTION, new String[]{APIContract.ActionsTable._ID}, APIContract.ActionsTable.ACTION_ID + " =? ", new String[]{actionId}, null);
        if (curAction != null && curAction.getCount() == 0) {
            TSGHttpUtility.ERROR_LOGGER.getErr_actions().add(String.format(Error.ERR_ACTION_NAME_NOT_FOUND, actionId));
            return null;
        }
        curAction.moveToFirst();

        API api = getFromDB(context, curAction.getInt(curAction.getColumnIndex(APIContract.ActionsTable._ID)));
        closeCursor(curAction);
        return api;
    }

    public static API getFromDB(Context context, int id) {

        Cursor curAction = APIDBManager.getInstance(context).query(APIDBManager.KEY_ACTION, null, APIContract.ActionsTable._ID + " =? ", new String[]{id + ""}, null);
        curAction.moveToFirst();

        API action = new API();
        action.setId(id);
        action.setAction_id(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.ACTION_ID)));
        action.setAction(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.ACTION)));
        action.setRequest_type(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.REQUEST_TYPE)));
        action.setDev_url(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.DEV_BASE_URL)));
        action.setQa_url(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.QA_BASE_URL)));
        action.setProduction_url(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.PROD_BASE_URL)));
        action.setStaging_url(curAction.getString(curAction.getColumnIndex(APIContract.ActionsTable.STAGE_BASE_URL)));
        closeCursor(curAction);

        Cursor curBody = APIDBManager.getInstance(context).query(APIDBManager.KEY_BODY_PARAMETERS, new String[]{APIContract.BodyParametersTable._ID}, APIContract.BodyParametersTable.ACTION_ID + " =? ", new String[]{action.getId() + ""}, null);
        if (curBody != null && curBody.moveToFirst()) {
            BodyParameter[] bodyParameters = new BodyParameter[curBody.getCount()];
            for (int i = 0; i < curBody.getCount(); i++) {
                curBody.moveToPosition(i);
                BodyParameter bodyParameter = BodyParameter.getFromDB(context, curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable._ID)));
                bodyParameters[i] = bodyParameter;
            }
            action.setBody_parameters(bodyParameters);
        }
        closeCursor(curBody);


        Cursor curQuery = APIDBManager.getInstance(context).query(APIDBManager.KEY_QUERY_PARAMETERS, new String[]{APIContract.BodyParametersTable._ID}, APIContract.QueryParametersTable.ACTION_ID + " =? ", new String[]{action.getId() + ""}, null);
        if (curQuery != null && curQuery.moveToFirst()) {
            QueryParameter[] queryParameters = new QueryParameter[curQuery.getCount()];
            for (int i = 0; i < curQuery.getCount(); i++) {
                curQuery.moveToPosition(i);
                QueryParameter queryParameter = QueryParameter.getFromDB(context, curQuery.getInt(curQuery.getColumnIndex(APIContract.BodyParametersTable._ID)));
                queryParameters[i] = queryParameter;
            }
            action.setQuery_parameters(queryParameters);
        }
        closeCursor(curQuery);


        Cursor curHeader = APIDBManager.getInstance(context).query(APIDBManager.KEY_HEADERS, new String[]{APIContract.HeadersTable._ID}, APIContract.HeadersTable.ACTION_ID + " =? ", new String[]{action.getId() + ""}, null);
        if (curHeader != null && curHeader.moveToFirst()) {
            Header headers[] = new Header[curHeader.getCount()];
            for (int i = 0; i < curHeader.getCount(); i++) {
                curHeader.moveToPosition(i);
                Header header = Header.getFromDB(context, curHeader.getInt(curHeader.getColumnIndex(APIContract.HeadersTable._ID)));
                headers[i] = header;
            }
            action.setHeaders(headers);
        }
        closeCursor(curHeader);

        return action;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
