package com.booleanworks.ueshiba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
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


        db.beginTransaction();
        db.execSQL("CREATE TABLE appParam (pKey TEXT PRIMARY KEY, pTextValue TEXT,pIntegerValue INTEGER,pFloatValue FLOAT,jsonData BLOB)");
        db.execSQL("CREATE TABLE appPackage (packageId INTEGER PRIMARY KEY, htmlContent BLOB, htmlContentFile TEXT)");
        db.execSQL("CREATE TABLE appPage (pageId INTEGER PRIMARY KEY, packageId INTEGER, htmlContent BLOB, htmlContentFile TEXT)");
        db.execSQL("CREATE TABLE appContent (contentId INTEGER PRIMARY KEY, packageId INTEGER, binaryContent BLOB, binaryContentFile TEXT)");
        db.execSQL("CREATE TABLE appData (dataId INTEGER PRIMARY KEY, packageId INTEGER, userId INTEGER , terminalId INTEGER,jsonContent BLOB)");

        //db.execSQL("INSERT INTO appParam(pkey,pIntegerValue) VALUES ('terminalId',"+terminalId+")");
        //db.execSQL("INSERT INTO appParam(pkey,pIntegerValue) VALUES ('userId',"+userId+")");
        //db.execSQL("INSERT INTO appParam(pkey,pTextValue) VALUES ('serverBaseUrl','http://poc2015a.booleanworks.com')");

        ContentValues terminalIdValue = new ContentValues();
        terminalIdValue.put("pKey", "terminalId");
        terminalIdValue.put("pIntegerValue", terminalId);
        db.insert("appParam", null, terminalIdValue);

        ContentValues userIdValue = new ContentValues();
        userIdValue.put("pKey", "userId");
        userIdValue.put("pIntegerValue", userId);
        db.insert("appParam", null, userIdValue);

        ContentValues serverBaseUrlValue = new ContentValues();
        serverBaseUrlValue.put("pKey", "serverBaseUrl");
        serverBaseUrlValue.put("pTextValue", "http://ueshiba1.booleanworks.com");
        db.insert("appParam", null, serverBaseUrlValue);

        ContentValues package1Value = new ContentValues();
        package1Value.put("packageId", 1);
        package1Value.put("htmlContent", "<html><head><title>TEST PACKAGE 1</title></head><body><p>TEST PACKAGE 1<p></body></html>");
        db.insert("appPackage", null, package1Value);

        ContentValues page1Value = new ContentValues();
        page1Value.put("pageId", 1);
        page1Value.put("packageId", 1);
        page1Value.put("htmlContent", "<html><head><title>TEST PAGE 1</title></head><body><p>TEST PAGE 1<p></body></html>");
        db.insert("appPage", null, page1Value);


        db.endTransaction();


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

    public static int doBasicTest(Context context, int testType) {

        DatabaseManager databaseManager = DatabaseManager.getInstance(context);
        SQLiteDatabase db = databaseManager.getWritableDatabase();

        switch (testType) {
            case 1:

                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables("appParam");
                Cursor cursor1 = builder.query(db, new String[]{"pKey", "pIntegerValue"}, "pKey = ?1", new String[]{"terminalId"}, null, null, null);

                if (cursor1.getCount() == 1) {
                    return 0;
                } else {
                    return -1;
                }


            default:
                return -1;


        }
    }

    public void wireWebView(WebView webView) {
        WebViewClient customClient = new WebViewClient() {

            WebView relatedWebView;
            DatabaseManager databaseManager;

            /**
             * Notify the host application of a resource request and allow the
             * application to return the data.  If the return value is null, the WebView
             * will continue to load the resource as usual.  Otherwise, the return
             * response and data will be used.  NOTE: This method is called on a thread
             * other than the UI thread so clients should exercise caution
             * when accessing private data or the view system.
             *
             * @param view    The {@link android.webkit.WebView} that is requesting the
             *                resource.
             * @param request Object containing the details of the request.
             * @return A {@link android.webkit.WebResourceResponse} containing the
             * response information or null if the WebView should load the
             * resource itself.
             */
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                String referer = request.getRequestHeaders().get("referer");
                Uri uri = request.getUrl();
                String viewUrl = view.getUrl();
                String viewOriginalUrl = view.getOriginalUrl();

                return null;
            }

            /**
             * Notify the host application of a resource request and allow the
             * application to return the data.  If the return value is null, the WebView
             * will continue to load the resource as usual.  Otherwise, the return
             * response and data will be used.  NOTE: This method is called on a thread
             * other than the UI thread so clients should exercise caution
             * when accessing private data or the view system.
             *
             * @param view The {@link android.webkit.WebView} that is requesting the
             *             resource.
             * @param url  The raw url of the resource.
             * @return A {@link android.webkit.WebResourceResponse} containing the
             * response information or null if the WebView should load the
             * resource itself.
             * @deprecated Use {@link #shouldInterceptRequest(android.webkit.WebView, android.webkit.WebResourceRequest)
             * shouldInterceptRequest(WebView, WebResourceRequest)} instead.
             */
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                URI uri = URI.create(url) ;

                if(uri.getScheme().contentEquals("ueshiba"))
                {
                    String[] pathTokens = uri.getPath().split("/");

                    if(pathTokens.length > 1)
                    {

                    }
                }


                return null;
            }

            public WebViewClient setup(WebView webView1, DatabaseManager databaseManager1) {
                this.relatedWebView = webView1;
                this.databaseManager = databaseManager1;
                return this;
            }
        }.setup(webView, this);

        webView.setWebViewClient(customClient);
    }


    public String getHtmlDataFromCode(WebView webView,String code)
    {

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("ueshiba");

        SQLiteDatabase db =  this.getWritableDatabase() ;

        SQLiteQueryBuilder paramQb = new SQLiteQueryBuilder();
        paramQb.setTables("appParam");
        Cursor paramCursor = paramQb.query(db, new String[]{"pKey", "pTextValue","pIntegerValue","pFloatValue","jsonData"}, null, null, null, null, null);

        Integer userId = null ;
        Integer terminalId = null ;
        String serverBaseUrl = "http://ueshiba1.booleanworks.com";

       while(!paramCursor.isAfterLast())
       {
           if(paramCursor.getString(0).equalsIgnoreCase("userId"))
           {
               userId = paramCursor.getInt(2) ;
           }

           if(paramCursor.getString(0).equalsIgnoreCase("terminalId"))
           {
               terminalId= paramCursor.getInt(2) ;
           }

           if(paramCursor.getString(0).equalsIgnoreCase("serverBaseUrl"))
           {
               serverBaseUrl= paramCursor.getString(1) ;
           }

       }

        paramCursor.close();


        if(code.startsWith("PK,"))
        {//Package
            Integer packageId = Integer.getInteger(code.substring(3)) ;

            if(packageId != null)
            {
                SQLiteQueryBuilder packageQb = new SQLiteQueryBuilder();
                packageQb.setTables("appPackage");

                Cursor packageCursor = packageQb.query(db, new String[]{"packageId", "htmlContent"}, "packageId = ?1", new String[]{packageId.toString()}, null, null, null);

                if(packageCursor.getCount() == 1)
                {
                    //TODO: we have the content display it !
                }else
                {
                    HttpPost httpPost = new HttpPost(serverBaseUrl + "/package/get/" + packageId.toString());
                    httpPost.getParams().setIntParameter("terminalId",terminalId);
                    httpPost.getParams().setIntParameter("userId",userId);
                }

            }


        }else if(code.startsWith("PG,"))
        {//Page
            Integer pageId = Integer.getInteger(code.substring(3)) ;

        }else if(code.startsWith("CN,"))
        {//Content
            Integer contentId = Integer.getInteger(code.substring(3)) ;

        }else if(code.startsWith("US,"))
        {//User param
            Integer newUserId = Integer.getInteger(code.substring(3)) ;

        }


        return "" ;
    }


}
