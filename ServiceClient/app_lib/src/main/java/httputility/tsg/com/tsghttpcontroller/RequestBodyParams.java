package httputility.tsg.com.tsghttpcontroller;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by kiwitech on 08/06/16.
 */

public class RequestBodyParams extends HashMap<String, Object> {

    public enum TYPE {
        FORM_DATA,
        RAW_APPLICATION_JSON
    }

    private TYPE type = TYPE.RAW_APPLICATION_JSON;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
    
}
