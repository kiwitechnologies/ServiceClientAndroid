package httputility.tsg.com.tsgapicontroller.Logger;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kiwitech on 13/05/16.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public final class TSGErrorHelper {

    private HashMap<String, ArrayList<String>> MISSING = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> INVALID = new HashMap<String, ArrayList<String>>();


    public void addErrMissed(String key, String error) {
        ArrayList<String> list = MISSING.get(key);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(error);
        MISSING.put(key, list);
    }

    public void addErrInvalid(String key, String error) {
        ArrayList<String> list = INVALID.get(key);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(error);
        INVALID.put(key, list);
    }

    public HashMap<String, ArrayList<String>> getMISSING() {
        return MISSING;
    }

    public HashMap<String, ArrayList<String>> getINVALID() {
        return INVALID;
    }

}
