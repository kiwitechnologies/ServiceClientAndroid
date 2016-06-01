package httputility.tsg.com.tsgapicontroller.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class APIDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tsgApiInfo";
    private static final int DB_VERSION = 1;
    private Context mContext;
    private static APIDBHelper instance;

    public static APIDBHelper getInstance(Context ctx) {
        if (instance == null || !ctx.equals(instance.mContext)) {
            instance = new APIDBHelper(ctx);
        }
        return instance;
    }

    private APIDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(APIDBQueies.STATEMENT_CREATE_API_INFO_TABLE);
        db.execSQL(APIDBQueies.STATEMENT_CREATE_ACTIONS_TABLE);
        db.execSQL(APIDBQueies.STATEMENT_CREATE_BODY_PARAMETERS_TABLE);
        db.execSQL(APIDBQueies.STATEMENT_CREATE_QUERY_PARAMETERS_TABLE);
        db.execSQL(APIDBQueies.STATEMENT_CREATE_HEADERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Reset all data to get fresh in migration.
     */
    private void resetData(SQLiteDatabase db) {
        db.execSQL("delete from " + APIContract.ApiInfoTable.TABLE_NAME);
        db.execSQL("delete from " + APIContract.ActionsTable.TABLE_NAME);
        db.execSQL("delete from " + APIContract.BodyParametersTable.TABLE_NAME);
        db.execSQL("delete from " + APIContract.QueryParametersTable.TABLE_NAME);
        db.execSQL("delete from " + APIContract.HeadersTable.TABLE_NAME);
    }

    public void clearData() {
        instance.resetData(instance.getWritableDatabase());
    }

}
