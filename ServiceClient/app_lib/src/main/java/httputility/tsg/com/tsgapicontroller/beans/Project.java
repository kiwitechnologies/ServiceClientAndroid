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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import httputility.tsg.com.tsgapicontroller.storage.APIContract;
import httputility.tsg.com.tsgapicontroller.storage.APIDBManager;

/**
 * Created by kiwitech on 03/05/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Project {

    private int _id;
    private String project_id;
    private long updated_at;
    private String project_name;
    private String version_no;
    private API actions[];


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getVersion_no() {
        return version_no;
    }

    public void setVersion_no(String version_no) {
        this.version_no = version_no;
    }

    public API[] getActions() {
        return actions;
    }

    public void setActions(API[] actions) {
        this.actions = actions;
    }


    public void insertIntoDB(Context mContext, long updated_at) {

        ContentValues cv = new ContentValues();
        cv.put(APIContract.ApiInfoTable.PROJECT_ID, getProject_id());
        cv.put(APIContract.ApiInfoTable.PROJECT_NAME, getProject_name());
        cv.put(APIContract.ApiInfoTable.UPDATED_AT, getUpdated_at());
        cv.put(APIContract.ApiInfoTable.VERSION_NO, getVersion_no());
        APIDBManager.getInstance(mContext).insert(APIDBManager.KEY_API_INFO, cv);

        for (int i = 0; null != getActions() && i < getActions().length; i++) {
            getActions()[i].insertIntoDB(mContext);
        }
    }


    public static Project getFromDB(Context context) {
        Project project = new Project();
        Cursor curAPIInfo = APIDBManager.getInstance(context).query(APIDBManager.KEY_API_INFO, null, null, null, null);
        if (curAPIInfo != null && curAPIInfo.moveToFirst()) {
            project.set_id(curAPIInfo.getInt(curAPIInfo.getColumnIndex(APIContract.ApiInfoTable._ID)));
            project.setProject_id(curAPIInfo.getString(curAPIInfo.getColumnIndex(APIContract.ApiInfoTable.PROJECT_ID)));
            project.setProject_name(curAPIInfo.getString(curAPIInfo.getColumnIndex(APIContract.ApiInfoTable.PROJECT_NAME)));
            project.setUpdated_at(curAPIInfo.getInt(curAPIInfo.getColumnIndex(APIContract.ApiInfoTable.UPDATED_AT)));
            project.setVersion_no(curAPIInfo.getString(curAPIInfo.getColumnIndex(APIContract.ApiInfoTable.VERSION_NO)));
        }
        closeCursor(curAPIInfo);
        return project;
    }


    public static String getVersionName(Context context) {
        String versionNo = null;
        Cursor cursor = APIDBManager.getInstance(context).query(APIDBManager.KEY_API_INFO, new String[]{APIContract.ApiInfoTable.VERSION_NO}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            versionNo = cursor.getString(cursor.getColumnIndex(APIContract.ApiInfoTable.VERSION_NO));

        }
        closeCursor(cursor);
        return versionNo;
    }

    private static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
