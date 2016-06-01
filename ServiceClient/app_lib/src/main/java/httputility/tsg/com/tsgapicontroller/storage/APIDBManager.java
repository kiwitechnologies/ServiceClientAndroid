package httputility.tsg.com.tsgapicontroller.storage;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public final class APIDBManager {

    public static final int KEY_API_INFO = 10;
    public static final int KEY_ACTION = 11;
    public static final int KEY_BODY_PARAMETERS = 12;
    public static final int KEY_QUERY_PARAMETERS = 13;
    public static final int KEY_HEADERS = 14;

    private APIDBHelper mAPIDBHelper;
    private static APIDBManager instance;
    private Context context;

    private APIDBManager(Context context) {
        this.context = context;
        mAPIDBHelper = APIDBHelper.getInstance(context);
    }

    public static APIDBManager getInstance(Context context) {
        if (instance == null) {
            instance = new APIDBManager(context);
        }
        return instance;
    }


    public int delete(int type, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = mAPIDBHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (type) {
            case KEY_API_INFO:
                rowsDeleted = sqlDB.delete(APIContract.ApiInfoTable.TABLE_NAME, selection, selectionArgs);
                break;
            case KEY_ACTION:
                rowsDeleted = sqlDB.delete(APIContract.ActionsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case KEY_BODY_PARAMETERS:
                rowsDeleted = sqlDB.delete(APIContract.BodyParametersTable.TABLE_NAME, selection, selectionArgs);
                break;
            case KEY_QUERY_PARAMETERS:
                rowsDeleted = sqlDB.delete(APIContract.QueryParametersTable.TABLE_NAME, selection, selectionArgs);
                break;
            case KEY_HEADERS:
                rowsDeleted = sqlDB.delete(APIContract.HeadersTable.TABLE_NAME, selection, selectionArgs);
                break;
        }

        return rowsDeleted;
    }

    public long insert(int type, ContentValues values) {
        SQLiteDatabase sqlDB = mAPIDBHelper.getWritableDatabase();
        long id = 0;
        switch (type) {

            case KEY_API_INFO:
                id = sqlDB.insertWithOnConflict(APIContract.ApiInfoTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case KEY_ACTION:
                id = sqlDB.insertWithOnConflict(APIContract.ActionsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case KEY_BODY_PARAMETERS:
                id = sqlDB.insertWithOnConflict(APIContract.BodyParametersTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case KEY_QUERY_PARAMETERS:
                id = sqlDB.insertWithOnConflict(APIContract.QueryParametersTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case KEY_HEADERS:
                id = sqlDB.insertWithOnConflict(APIContract.HeadersTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
        }
        return id;
    }

    public Cursor query(int type, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            Cursor cursor = null;
            SQLiteDatabase sqlDB = mAPIDBHelper.getWritableDatabase();
            switch (type) {

                case KEY_API_INFO:
                    cursor = sqlDB.query(APIContract.ApiInfoTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                case KEY_ACTION:
                    cursor = sqlDB.query(APIContract.ActionsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                case KEY_BODY_PARAMETERS:
                    cursor = sqlDB.query(APIContract.BodyParametersTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                case KEY_QUERY_PARAMETERS:
                    cursor = sqlDB.query(APIContract.QueryParametersTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;

                case KEY_HEADERS:
                    cursor = sqlDB.query(APIContract.HeadersTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                    break;
            }
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int update(int type, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        SQLiteDatabase sqlDB = mAPIDBHelper.getWritableDatabase();

        switch (type) {

            case KEY_API_INFO:
                rowsUpdated = sqlDB.update(APIContract.ApiInfoTable.TABLE_NAME, values, selection, selectionArgs);
                break;

            case KEY_ACTION:
                rowsUpdated = sqlDB.update(APIContract.ActionsTable.TABLE_NAME, values, selection, selectionArgs);
                break;

            case KEY_BODY_PARAMETERS:
                rowsUpdated = sqlDB.update(APIContract.BodyParametersTable.TABLE_NAME, values, selection, selectionArgs);
                break;

            case KEY_QUERY_PARAMETERS:
                rowsUpdated = sqlDB.update(APIContract.QueryParametersTable.TABLE_NAME, values, selection, selectionArgs);
                break;

            case KEY_HEADERS:
                rowsUpdated = sqlDB.update(APIContract.HeadersTable.TABLE_NAME, values, selection, selectionArgs);
                break;

        }
        return rowsUpdated;
    }

}
