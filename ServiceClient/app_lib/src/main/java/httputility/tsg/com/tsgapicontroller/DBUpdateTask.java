package httputility.tsg.com.tsgapicontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import httputility.tsg.com.tsgapicontroller.beans.Project;
import httputility.tsg.com.tsgapicontroller.storage.APIDBHelper;

/**
 * Created by kiwitech on 03/05/16.
 */
public final class DBUpdateTask extends AsyncTask<String, Void, Void> {


    private Context mContext;
    private String appversion;
    private TSGAPIController.BUILD_FLAVOR buildFlavor;

    public DBUpdateTask(Context context, TSGAPIController.BUILD_FLAVOR buildFlavor) {
        mContext = context;
        this.buildFlavor = buildFlavor;
    }

    @Override
    protected Void doInBackground(String... jsonData) {

        String fileName = Constants.API_VALIDATION_FILE_NAME;
        ObjectMapper objectMapper = new ObjectMapper();
        Project apiInfo = null;
        try {
            String strResponse = readFromAssetsLocalFile(fileName);
            apiInfo = objectMapper.readValue(strResponse, Project.class);

            if (updateDB(mContext, apiInfo.getUpdated_at())) {
                APIDBHelper.getInstance(mContext).clearData();
                apiInfo.insertIntoDB(mContext, apiInfo.getUpdated_at());

                SharedPreferences.Editor editor = mContext.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
                editor.putLong(TSGAPIController.SharedPrefConstants.KEY_UPDATEDAT, apiInfo.getUpdated_at());
                if (appversion != null) {
                    editor.putString(TSGAPIController.SharedPrefConstants.KEY_APP_VERSION, appversion);
                }
                editor.commit();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to parse API JSON file. Please check %s file exist in proper json format.", fileName));
        }
        return null;
    }

    private boolean updateDB(Context context, long updated_at) {
        if (buildFlavor == TSGAPIController.BUILD_FLAVOR.PRODUCTION) {
            SharedPreferences prefs = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
            long prevUpdateAtVal = prefs.getLong(TSGAPIController.SharedPrefConstants.KEY_UPDATEDAT, -1);
            if (prevUpdateAtVal == -1 || isProjectVersionChanged(context)) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private String readFromAssetsLocalFile(String assetsFileName) throws IOException {

        InputStream inputStream = mContext.getAssets().open(assetsFileName);
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }

        return total.toString();
    }

    private boolean isProjectVersionChanged(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        String prevAppVersion = prefs.getString(TSGAPIController.SharedPrefConstants.KEY_APP_VERSION, null);
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            if (prevAppVersion == null || !info.versionName.equalsIgnoreCase(prevAppVersion)) {
                appversion = info.versionName;
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
        return false;
    }

    /*private boolean isLastUpdatedDateChanged(Context context, long updated_at) {

        SharedPreferences prefs = context.getSharedPreferences(TSGAPIController.SharedPrefConstants.SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        long prevUpdateAtVal = prefs.getLong(TSGAPIController.SharedPrefConstants.KEY_UPDATEDAT, -1);
        if (prevUpdateAtVal == -1 || prevUpdateAtVal != updated_at) {
            return true;
        }
        return false;
    }*/

}
