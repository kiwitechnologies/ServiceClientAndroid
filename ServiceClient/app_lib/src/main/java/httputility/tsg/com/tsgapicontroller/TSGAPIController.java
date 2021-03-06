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
import android.content.SharedPreferences;

import java.util.HashMap;

import static httputility.tsg.com.tsgapicontroller.TSGAPIController.SharedPrefConstants.KEY_BUILD_FLAVOR;
import static httputility.tsg.com.tsgapicontroller.TSGAPIController.SharedPrefConstants.KEY_DUMMY_RESPONSE_CODE;

/**
 * Created by kiwitech on 03/05/16.
 */
public final class TSGAPIController {

    private TSGAPIController(Context context, BUILD_FLAVOR buildFlavor) {
        TSGAPIController.setBuildFlavor(context, buildFlavor);
    }

    public static void setBuildFlavor(Context context, BUILD_FLAVOR buildFlavor) {
        BUILD_FLAVOR.saveBuildFlavor(context, buildFlavor);
    }

    public static void init(Context context) {
        init(context, BUILD_FLAVOR.DEVELOPMENT);
    }

    public static void init(Context context, BUILD_FLAVOR buildFlavor) {
        TSGAPIController tsgapiController = new TSGAPIController(context, buildFlavor);
        tsgapiController.updateDB(context, buildFlavor);
    }

    public static void setDummyServerResponseCode(Context context, int responseCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_DUMMY_RESPONSE_CODE, responseCode);
        editor.commit();
    }

    static int getDummyServerResponseCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_DUMMY_RESPONSE_CODE, 200);
    }

    public void updateDB(Context context, BUILD_FLAVOR buildFlavor) {
        DBUpdateTask dbUpdateTask = new DBUpdateTask(context, buildFlavor);
        dbUpdateTask.execute();
    }

    public enum BUILD_FLAVOR {
        STAGING,
        PRODUCTION,
        QA,
        DEVELOPMENT,
        DUMMY_SERVER;

        static void saveBuildFlavor(Context context, BUILD_FLAVOR build_flavor) {
            SharedPreferences.Editor editor = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_BUILD_FLAVOR, build_flavor.name());
            editor.commit();
        }

        public static BUILD_FLAVOR getBuildFlavor(Context context) {

            SharedPreferences prefs = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
            String buildFalvor = prefs.getString(KEY_BUILD_FLAVOR, DEVELOPMENT.name());

            if (buildFalvor.equalsIgnoreCase(PRODUCTION.name())) {
                return PRODUCTION;
            } else if (buildFalvor.equalsIgnoreCase(QA.name())) {
                return QA;
            } else if (buildFalvor.equalsIgnoreCase(STAGING.name())) {
                return STAGING;
            } else if (buildFalvor.equalsIgnoreCase(DUMMY_SERVER.name())) {
                return DUMMY_SERVER;
            } else {
                return DEVELOPMENT;
            }
        }
    }


    interface SharedPrefConstants {
        String SHARED_PREF_FILE_NAME = "com.tsg.apiInfo";
        String KEY_UPDATEDAT = "key_updatedat";
        String KEY_APP_VERSION = "key_app_version";
        String KEY_BUILD_FLAVOR = "build_flavor";
        String KEY_DUMMY_RESPONSE_CODE = "dummy_response_code";
    }

}
