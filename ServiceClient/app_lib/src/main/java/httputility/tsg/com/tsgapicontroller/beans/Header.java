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
import android.text.TextUtils;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import httputility.tsg.com.tsgapicontroller.storage.APIContract;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;

/**
 * Created by kiwitech on 03/05/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Header {

    private int id;
    private int actionId;
    private String key_name;
    private String[] key_value;
    private String keyValueCSV;

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

    public String[] getKey_value() {
        return key_value;
    }

    public void setKey_value(String[] key_value) {
        this.key_value = key_value;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getKeyValueCSV() {
        return keyValueCSV;
    }

    public void setKeyValueCSV(String keyValueCSV) {
        this.keyValueCSV = keyValueCSV;
    }

    public void insertIntoDB(Context mContext, long actionId) {
        ContentResolver contentResolver = mContext.getContentResolver();

        ContentValues cvHeader = new ContentValues();
        cvHeader.put(APIContract.HeadersTable.ACTION_ID, actionId);
        cvHeader.put(APIContract.HeadersTable.KEY_NAME, getKey_name());

        String values = TextUtils.join(",", getKey_value());
        cvHeader.put(APIContract.HeadersTable.KEY_VALUE, values);
        APIDBManager.getInstance(mContext).insert(APIDBManager.KEY_HEADERS, cvHeader);
    }

    public static Header getFromDB(Context context, int id) {
        Header header = new Header();
        Cursor curHeader = APIDBManager.getInstance(context).query(APIDBManager.KEY_HEADERS, null, APIContract.HeadersTable._ID + " =? ", new String[]{id + ""}, null);
        if (curHeader != null && curHeader.moveToFirst()) {
            header.setId(id);
            header.setActionId(curHeader.getInt(curHeader.getColumnIndex(APIContract.HeadersTable.ACTION_ID)));
            header.setKey_name(curHeader.getString(curHeader.getColumnIndex(APIContract.HeadersTable.KEY_NAME)));
            header.setKeyValueCSV(curHeader.getString(curHeader.getColumnIndex(APIContract.HeadersTable.KEY_VALUE)));
        } else {
            throw new IllegalArgumentException("Header id not exist in db");
        }
        closeCursor(curHeader);
        return header;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
