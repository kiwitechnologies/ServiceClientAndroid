package httputility.tsg.com.tsghttpcontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by kiwitech on 09/06/16.
 */

public class Utility {

    public static ArrayList<String> getPathParamsInURL(String url) {
        ArrayList<String> pathParamsList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean isStarted = false;
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '{') {
                isStarted = true;
                continue;
            }
            if (c == '}') {
                isStarted = false;
                if (sb.toString().length() > 0) {
                    pathParamsList.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
            if (isStarted) {
                sb.append(c);
            }
        }
        return pathParamsList;
    }

    public static String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
