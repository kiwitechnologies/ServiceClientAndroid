package httputility.tsg.com.tsgapicontroller.storage;

/**
 * Created by kiwitech on 05/11/15.
 */
public class APIDBQueies {


        public final static String STATEMENT_CREATE_API_INFO_TABLE = "CREATE TABLE "
                + APIContract.ApiInfoTable.TABLE_NAME
                + " ( " + APIContract.ApiInfoTable._ID + " integer PRIMARY KEY, "
                + APIContract.ApiInfoTable.VERSION_NO + " TEXT, "
                + APIContract.ApiInfoTable.PROJECT_ID + " TEXT, "
                + APIContract.ApiInfoTable.PROJECT_NAME + " TEXT, "
                + APIContract.ApiInfoTable.UPDATED_AT + " TEXT );";


        public final static String STATEMENT_CREATE_ACTIONS_TABLE = "CREATE TABLE "
                + APIContract.ActionsTable.TABLE_NAME
                + " ( " + APIContract.ActionsTable._ID + " integer PRIMARY KEY, "
                + APIContract.ActionsTable.ACTION + " TEXT, "
                + APIContract.ActionsTable.ACTION_ID + " TEXT, "
                + APIContract.ActionsTable.REQUEST_TYPE + " TEXT, "
                + APIContract.ActionsTable.STAGE_BASE_URL + " TEXT, "
                + APIContract.ActionsTable.DEV_BASE_URL + " TEXT, "
                + APIContract.ActionsTable.PROD_BASE_URL + " TEXT, "
                + APIContract.ActionsTable.QA_BASE_URL + " TEXT );";


        public final static String STATEMENT_CREATE_BODY_PARAMETERS_TABLE = "CREATE TABLE "
                + APIContract.BodyParametersTable.TABLE_NAME
                + " ( " + APIContract.BodyParametersTable._ID + " integer PRIMARY KEY, "
                + APIContract.BodyParametersTable.ACTION_ID + " integer, "
                + APIContract.BodyParametersTable.KEY_NAME + " TEXT, "
                + APIContract.BodyParametersTable.VALIDATION_DATA_TYPE + " integer, "
                + APIContract.BodyParametersTable.FORMAT_STRING + " integer, "
                + APIContract.BodyParametersTable.FORMAT_FILE + " TEXT, "
                + APIContract.BodyParametersTable.MAX + " TEXT, "
                + APIContract.BodyParametersTable.MIN + " TEXT, "
                + APIContract.BodyParametersTable.REQUIRED + " integer, "
                + APIContract.BodyParametersTable.SIZE + " TEXT );";

        public final static String STATEMENT_CREATE_QUERY_PARAMETERS_TABLE = "CREATE TABLE "
                + APIContract.QueryParametersTable.TABLE_NAME
                + " ( " + APIContract.QueryParametersTable._ID + " integer PRIMARY KEY, "
                + APIContract.QueryParametersTable.ACTION_ID + " integer, "
                + APIContract.QueryParametersTable.KEY_NAME + " TEXT, "
                + APIContract.QueryParametersTable.VALIDATION_DATA_TYPE + " integer, "
                + APIContract.QueryParametersTable.FORMAT_STRING + " integer, "
                + APIContract.QueryParametersTable.FORMAT_FILE + " TEXT, "
                + APIContract.QueryParametersTable.MAX + " TEXT, "
                + APIContract.QueryParametersTable.MIN + " TEXT, "
                + APIContract.QueryParametersTable.REQUIRED + " integer, "
                + APIContract.QueryParametersTable.SIZE + " TEXT );";


        public final static String STATEMENT_CREATE_HEADERS_TABLE = "CREATE TABLE "
                + APIContract.HeadersTable.TABLE_NAME
                + " ( " + APIContract.HeadersTable._ID + " integer PRIMARY KEY, "
                + APIContract.HeadersTable.ACTION_ID + " integer, "
                + APIContract.HeadersTable.KEY_NAME + " TEXT, "
                + APIContract.HeadersTable.KEY_VALUE + " TEXT );";


}
