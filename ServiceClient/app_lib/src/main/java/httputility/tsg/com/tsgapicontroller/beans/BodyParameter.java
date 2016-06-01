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

import httputility.tsg.com.tsgapicontroller.Constants;
import httputility.tsg.com.tsgapicontroller.storage.APIContract;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;

/**
 * Created by kiwitech on 03/05/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class BodyParameter {

    private int id;
    private int actionId;
    private String key_name;
    private int validation_data_type;
    private Validations validations;
    private boolean multipartFileRequest;

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

        ContentValues cvBodyParameter = new ContentValues();
        cvBodyParameter.put(APIContract.BodyParametersTable.ACTION_ID, actionId);
        cvBodyParameter.put(APIContract.BodyParametersTable.KEY_NAME, getKey_name());
        cvBodyParameter.put(APIContract.BodyParametersTable.VALIDATION_DATA_TYPE, getValidation_data_type());
        if (getValidations() != null) {
            cvBodyParameter.put(APIContract.BodyParametersTable.FORMAT_STRING, getValidations().getFormat_string());
            cvBodyParameter.put(APIContract.BodyParametersTable.FORMAT_FILE, getValidations().getFormat_file());
            cvBodyParameter.put(APIContract.BodyParametersTable.MAX, getValidations().getMax());
            cvBodyParameter.put(APIContract.BodyParametersTable.MIN, getValidations().getMin());
            cvBodyParameter.put(APIContract.BodyParametersTable.REQUIRED, getValidations().getRequire());
            cvBodyParameter.put(APIContract.BodyParametersTable.SIZE, getValidations().getSize());
        }
        APIDBManager.getInstance(mContext).insert(APIDBManager.KEY_BODY_PARAMETERS, cvBodyParameter);

    }

    public static BodyParameter getFromDB(Context context, int id) {
        BodyParameter bodyParameter = new BodyParameter();
        Cursor curBody = APIDBManager.getInstance(context).query(APIDBManager.KEY_BODY_PARAMETERS, null, APIContract.BodyParametersTable._ID + " =? ", new String[]{id + ""}, null);
        if (curBody != null && curBody.moveToFirst()) {

            bodyParameter.setId(curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable._ID)));
            bodyParameter.setActionId(curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable.ACTION_ID)));
            bodyParameter.setKey_name(curBody.getString(curBody.getColumnIndex(APIContract.BodyParametersTable.KEY_NAME)));
            bodyParameter.setValidation_data_type(curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable.VALIDATION_DATA_TYPE)));


            Validations validations = new Validations();
            validations.setFormat_string(curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable.FORMAT_STRING)));
            validations.setFormat_file(curBody.getString(curBody.getColumnIndex(APIContract.BodyParametersTable.FORMAT_FILE)));
            validations.setMax(curBody.getString(curBody.getColumnIndex(APIContract.BodyParametersTable.MAX)));
            validations.setMin(curBody.getString(curBody.getColumnIndex(APIContract.BodyParametersTable.MIN)));
            validations.setRequire(curBody.getInt(curBody.getColumnIndex(APIContract.BodyParametersTable.REQUIRED)));
            validations.setSize(curBody.getString(curBody.getColumnIndex(APIContract.BodyParametersTable.SIZE)));
            bodyParameter.setValidations(validations);
        } else {
            throw new IllegalArgumentException("BodyParameter id not exist in db");
        }
        closeCursor(curBody);
        return bodyParameter;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public boolean isMultipartFileRequest() {
        return validation_data_type == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.FILE.ordinal();
    }
}
