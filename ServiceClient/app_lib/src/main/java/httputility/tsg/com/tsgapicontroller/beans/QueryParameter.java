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

import httputility.tsg.com.tsgapicontroller.storage.APIContract;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;

/**
 * Created by kiwitech on 23/05/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class QueryParameter {

    private int id;
    private int actionId;
    private String key_name;
    private int validation_data_type;
    private Validations validations;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public int getValidation_data_type() {
        return validation_data_type;
    }

    public void setValidation_data_type(int validation_data_type) {
        this.validation_data_type = validation_data_type;
    }

    public Validations getValidations() {
        return validations;
    }

    public void setValidations(Validations validations) {
        this.validations = validations;
    }

    public void insertIntoDB(Context mContext, long actionId) {

        ContentResolver contentResolver = mContext.getContentResolver();

        ContentValues cvQueryParameter = new ContentValues();
        cvQueryParameter.put(APIContract.QueryParametersTable.ACTION_ID, actionId);
        cvQueryParameter.put(APIContract.QueryParametersTable.KEY_NAME, getKey_name());
        cvQueryParameter.put(APIContract.QueryParametersTable.VALIDATION_DATA_TYPE, getValidation_data_type());
        if (getValidations() != null) {
            cvQueryParameter.put(APIContract.QueryParametersTable.FORMAT_STRING, getValidations().getFormat_string());
            cvQueryParameter.put(APIContract.QueryParametersTable.FORMAT_FILE, getValidations().getFormat_file());
            cvQueryParameter.put(APIContract.QueryParametersTable.MAX, getValidations().getMax());
            cvQueryParameter.put(APIContract.QueryParametersTable.MIN, getValidations().getMin());
            cvQueryParameter.put(APIContract.QueryParametersTable.REQUIRED, getValidations().getRequire());
            cvQueryParameter.put(APIContract.QueryParametersTable.SIZE, getValidations().getSize());
        }
        APIDBManager.getInstance(mContext).insert(APIDBManager.KEY_QUERY_PARAMETERS, cvQueryParameter);

    }

    public static QueryParameter getFromDB(Context context, int id) {
        QueryParameter queryParameter = new QueryParameter();
        Cursor curQuery = APIDBManager.getInstance(context).query(APIDBManager.KEY_QUERY_PARAMETERS, null, APIContract.QueryParametersTable._ID + " =? ", new String[]{id + ""}, null);
        if (curQuery != null && curQuery.moveToFirst()) {

            queryParameter.setId(curQuery.getInt(curQuery.getColumnIndex(APIContract.QueryParametersTable._ID)));
            queryParameter.setActionId(curQuery.getInt(curQuery.getColumnIndex(APIContract.QueryParametersTable.ACTION_ID)));
            queryParameter.setKey_name(curQuery.getString(curQuery.getColumnIndex(APIContract.QueryParametersTable.KEY_NAME)));
            queryParameter.setValidation_data_type(curQuery.getInt(curQuery.getColumnIndex(APIContract.QueryParametersTable.VALIDATION_DATA_TYPE)));


            Validations validations = new Validations();
            validations.setFormat_string(curQuery.getInt(curQuery.getColumnIndex(APIContract.QueryParametersTable.FORMAT_STRING)));
            validations.setFormat_file(curQuery.getString(curQuery.getColumnIndex(APIContract.QueryParametersTable.FORMAT_FILE)));
            validations.setMax(curQuery.getString(curQuery.getColumnIndex(APIContract.QueryParametersTable.MAX)));
            validations.setMin(curQuery.getString(curQuery.getColumnIndex(APIContract.QueryParametersTable.MIN)));
            validations.setRequire(curQuery.getInt(curQuery.getColumnIndex(APIContract.QueryParametersTable.REQUIRED)));
            validations.setSize(curQuery.getString(curQuery.getColumnIndex(APIContract.QueryParametersTable.SIZE)));
            queryParameter.setValidations(validations);
        } else {
            throw new IllegalArgumentException("QueryParameter id not exist in db");
        }
        closeCursor(curQuery);
        return queryParameter;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
