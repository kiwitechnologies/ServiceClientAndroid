package httputility.tsg.com.tsgapicontroller;

/**
 * Created by kiwitech on 03/05/16.
 */
public final class Constants {

    public final static String URL_GET_API_INFO_VERSION = "http://kiwitechopensource.com/tsg/projects/%s/version";
    public final static String API_VALIDATION_FILE_NAME = "api_validation.json";
    public static final String EXTRA_REQUEST_ID = "extra_request_id";
    public static final String EXTRA_REQUEST_TIME = "extra_request_time";
    public static final String EXTRA_REQUEST_RECEIVER = "extra_request_receiver";
    public static final String EXTRA_EXECUTE_ON_PRIORITY = "extra_execute_on_priority";
    public static final String STATUS_CODE = "status_code";


    public final static class SERVER_CONST {

        public enum VALIDATION_DATA_TYPE {
            INTEGER,
            FLOAT,
            STRING,
            TEXT,
            FILE;
        }

        public enum STRING_FORMAT {
            STRING_FORMAT_ALPHA,
            STRING_FORMAT_NUMERIC,
            STRING_FORMAT_ALPHANUMERIC,
            STRING_FORMAT_EMAIL;
        }
    }

}
