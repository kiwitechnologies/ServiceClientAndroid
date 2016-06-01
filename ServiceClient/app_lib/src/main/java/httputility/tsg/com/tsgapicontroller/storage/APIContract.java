package httputility.tsg.com.tsgapicontroller.storage;


public final class APIContract {

    public final static class ApiInfoTable {
        public static final String TABLE_NAME = "api_info";

        public static final String _ID = "_id";
        public static final String PROJECT_ID = "project_id";
        public static final String PROJECT_NAME = "project_name";
        public static final String UPDATED_AT = "updated_at";
        public static final String VERSION_NO = "version";

    }

    public final static class ActionsTable {
        public static final String TABLE_NAME = "actions";

        public static final String _ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String ACTION = "action";
        public static final String REQUEST_TYPE = "request_type";

        public static final String DEV_BASE_URL = "dev_base_url";
        public static final String QA_BASE_URL = "qa_base_url";
        public static final String PROD_BASE_URL = "prod_base_url";
        public static final String STAGE_BASE_URL = "stage_base_url";
    }

    public final static class BodyParametersTable {
        public static final String TABLE_NAME = "body_parameters";

        public static final String _ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String KEY_NAME = "key_name";
        public static final String VALIDATION_DATA_TYPE = "validation_data_type";
        public static final String FORMAT_STRING = "format_string";
        public static final String FORMAT_FILE = "format_file";
        public static final String MAX = "max";
        public static final String MIN = "min";
        public static final String REQUIRED = "required";
        public static final String SIZE = "size";
    }

    public final static class QueryParametersTable {
        public static final String TABLE_NAME = "query_parameters";

        public static final String _ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String KEY_NAME = "key_name";
        public static final String VALIDATION_DATA_TYPE = "validation_data_type";
        public static final String FORMAT_STRING = "format_string";
        public static final String FORMAT_FILE = "format_file";
        public static final String MAX = "max";
        public static final String MIN = "min";
        public static final String REQUIRED = "required";
        public static final String SIZE = "size";
    }


    public final static class HeadersTable {
        public static final String TABLE_NAME = "headers";

        public static final String _ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String KEY_NAME = "key_name";
        public static final String KEY_VALUE = "key_value";
    }


}
