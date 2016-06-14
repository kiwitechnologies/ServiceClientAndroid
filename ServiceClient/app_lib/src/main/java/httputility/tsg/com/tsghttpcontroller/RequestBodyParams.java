package httputility.tsg.com.tsghttpcontroller;

import java.util.HashMap;

/**
 * Created by kiwitech on 08/06/16.
 */

public class RequestBodyParams extends HashMap<String, String> {

    public enum TYPE {
        FORM_DATA,
        RAW_APPLICATION_JSON
    }

    private TYPE type= TYPE.RAW_APPLICATION_JSON;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}
