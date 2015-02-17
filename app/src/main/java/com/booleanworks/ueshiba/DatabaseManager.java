package com.booleanworks.ueshiba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;

import java.util.Random;
import java.util.zip.CRC32;

/**
 * Created by vortigern on 17/02/15.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    private static DatabaseManager instance = null;

    public static final int CURRENT_DB_VERSION = 1;
    public static final String CURRENT_DB_NAME = "ueshiba_1";

    public static DatabaseManager getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        if (DatabaseManager.instance == null) {
            DatabaseManager.instance = new DatabaseManager(context, name, factory, version);
            return DatabaseManager.instance;
        } else {
            return DatabaseManager.instance;
        }
    }

    public static DatabaseManager getInstance(Context context) {

        if (DatabaseManager.instance == null) {
            DatabaseManager.instance = new DatabaseManager(context, DatabaseManager.CURRENT_DB_NAME, null, DatabaseManager.CURRENT_DB_VERSION);
            return DatabaseManager.instance;
        } else {
            return DatabaseManager.instance;
        }
    }

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *                {@link #onUpgrade} will be used to upgrade the database; if the database is
     *                newer, {@link #onDowngrade} will be used to downgrade the database
     */
    public DatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        Random random = new Random();
        String serialNumber = Build.SERIAL == null ? "UNKNOWN" : Build.SERIAL;
        serialNumber += "-" + Build.FINGERPRINT;
        CRC32 crc32 = new CRC32();
        crc32.update(serialNumber.getBytes());
        long terminalId = crc32.getValue();
        long userId = random.nextLong();


        //db.beginTransaction();
        db.execSQL("CREATE TABLE appParam (pKey TEXT PRIMARY KEY, pTextValue TEXT,pIntegerValue INTEGER,pFloatValue FLOAT)");
        db.execSQL("CREATE TABLE appPage (pageId INTEGER PRIMARY KEY, htmlContent BLOB)");
        db.execSQL("CREATE TABLE appContent (contentId INTEGER PRIMARY KEY, binaryContent BLOB)");
        db.execSQL("CREATE TABLE appData (dataId INTEGER PRIMARY KEY, userId INTEGER , terminalId INTEGER,jsonContent BLOB)");

        //db.execSQL("INSERT INTO appParam(pkey,pIntegerValue) VALUES ('terminalId',"+terminalId+")");
        //db.execSQL("INSERT INTO appParam(pkey,pIntegerValue) VALUES ('userId',"+userId+")");
        //db.execSQL("INSERT INTO appParam(pkey,pTextValue) VALUES ('serverBaseUrl','http://poc2015a.booleanworks.com')");

        ContentValues terminalIdValue = new ContentValues() ;
        terminalIdValue.put("pKey","terminalId");
        terminalIdValue.put("pIntegerValue",terminalId);
        db.insert("appParam",null,terminalIdValue);

        ContentValues userIdValue = new ContentValues() ;
        userIdValue.put("pKey","userId");
        userIdValue.put("pIntegerValue",userId);
        db.insert("appParam",null,userIdValue);

        ContentValues serverBaseUrlValue = new ContentValues() ;
        serverBaseUrlValue.put("pKey","serverBaseUrl");
        serverBaseUrlValue.put("pTextValue","http://poc2015a.booleanworks.com");
        db.insert("appParam",null,serverBaseUrlValue);

        ContentValues page1Value = new ContentValues() ;
        page1Value.put("pageId",1);
        page1Value.put("htmlContent","<html><head><title>TEST PAGE 1</title></head><body><p>TEST PAGE 1<p></body></html>");
        db.insert("appPage",null,page1Value);


        //db.endTransaction();


    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        switch (oldVersion) {
            case 1:
                switch (newVersion) {
                    case 1:
                        //SUCKER !
                        break;
                    case 2:
                        //OK
                        break;
                    default:
                        //TODO
                        break;
                }
                break;

            default:

                break;
        }

    }

    public static int doBasicTest(Context context , int testType)
    {

        DatabaseManager databaseManager = DatabaseManager.getInstance(context) ;
        SQLiteDatabase db = databaseManager.getWritableDatabase() ;

        switch (testType)
        {
            case 1 :

                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables("appParam");
                Cursor cursor1 = builder.query(db,new String[]{"pKey","pIntegerValue"},"pKey = ?1",new String[]{"terminalId"},null,null,null);

                if(cursor1.getCount() == 1)
                {
                    return 0 ;
                }else
                {
                    return -1;
                }



            default:
                return -1 ;


        }
    }
}
