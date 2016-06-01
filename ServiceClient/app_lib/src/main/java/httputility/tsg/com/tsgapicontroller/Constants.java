package httputility.tsg.com.tsgapicontroller;

/**
 * Created by kiwitech on 03/05/16.
 */
public final class Constants {

    public final static int CONNECTION_TIME_OUT_SEC = 20;
    public final static String URL_GET_API_INFO_VERSION = "http://kiwitechopensource.com/tsg/projects/%s/version";
    public final static String API_VALIDATION_FILE_NAME = "api_validation.json";

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
