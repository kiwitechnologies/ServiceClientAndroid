package httputility.tsg.com.tsgapicontroller.validation;

/**
 * Created by kiwitech on 16/05/16.
 */
public class Error {

    public static final String ERR_DB_NOT_INITIALISED = "DB not initialised. Please initialise it first by calling TSGAPIController.init()";
    public static final String ERR_EMPTY_ACTION_NAME = "“%s” can not be empty.";
    public static final String ERR_ACTION_NAME_NOT_FOUND = "Action id “%s” not found.";
    public static final String ERR_KEYNAME_NOT_FOUND = "Not found. Please send it in the request.";
    public static final String ERR_KEYNAME_WRONG_DATA_TYPE = "Not a valid data type. “%s” expects a “%s” type.";
    public static final String ERR_KEYNAME_WRONG_STRING_FORMAT_TYPE = "Format is not valid. It expects a “%s” format.";
    public static final String ERR_KEYNAME_WRONG_LENGTH = "%s is not correct. It should be between “%s” - “%s”.";
    public static final String ERR_FILE_NOT_FOUND = "File “%s” not found.";
    public static final String ERR_FILE_FORMAT_NOT_SUPPORTABLE = "File format is not supportable.";
    public static final String ERR_INVALID_FILE_FORMAT = "File “%s” format is not valid. It should be one of “%s”.";
    public static final String ERR_KEYNAME_WRONG_SIZE = "“%s”  size is not correct. It should be less than “%s”";

}
