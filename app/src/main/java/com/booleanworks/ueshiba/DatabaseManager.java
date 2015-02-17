package com.booleanworks.ueshiba;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vortigern on 17/02/15.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    private static DatabaseManager instance = null;

    public static DatabaseManager getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        if (DatabaseManager.instance == null) {
            DatabaseManager.instance = new DatabaseManager(context, name, factory, version);
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

        db.execSQL("CREATE TABLE appParam (pkey VARCHAR(200) PRIMARY KEY, pvalue VARCHAR(200))");
        db.execSQL("CREATE TABLE appPage (id BIGINT PRIMARY KEY, htmlContent BLOB");
        db.execSQL("CREATE TABLE appContent (id BIGINT PRIMARY KEY, binaryContent BLOB");
        db.execSQL("CREATE TABLE appData (id BIGINT PRIMARY KEY, userId BIGINT , clientId BIGINT,jsonContent BLOB");


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
                    default:
                        //TODO
                        break;
                }
                break;

            default:
                switch (newVersion) {
                    default:
                        //TODO
                        break;
                }
                break;
        }

    }
}
