package com.booleanworks.ueshiba;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.zip.CRC32;


/**
 * Created by vortigern on 05/03/15.
 */
public class SimpleFileDataManager {

    protected static SimpleFileDataManager instance;
    public static final String DEFAULT_SERVER_BASE_URL = "https://ueshiba.booleanworks.com" ;
    public static final String DEFAULT_APPPARAM_FILENAME = "appParam.properties" ;

    public static final long UESHIBA_DATAMANAER_VERSION= 10 ;
    public static final String DEFAULT_PARAMFILE_COMMENT="UESHIBA-" + Long.toString(SimpleFileDataManager.UESHIBA_DATAMANAER_VERSION) ;

    public static final String PARAM_NAME_BASE_URL = "baseUrl" ;
    public static final String PARAM_NAME_USERID = "userId" ;
    public static final String PARAM_NAME_TERMINALID = "terminalId" ;



    protected File baseStorageDirectory ;

    protected AndroidHttpClient  httpClient ;

    protected Properties appParams ;

    protected HashMap<String,File> knownFiles ;
    protected HashMap<String,File> knownInternalFiles ;


    protected SimpleFileDataManager(Context context) {


        this.baseStorageDirectory = context.getExternalFilesDir(null);

        this.httpClient = AndroidHttpClient.newInstance("ueshiba",context);
        HttpConnectionParams.setSoTimeout(this.httpClient.getParams(),60*1000);
        HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(),60*1000);

        this.knownFiles = new HashMap<String,File>();

        this.updateKnownFiles();
        this.obtainAppParam();


    }

    public void updateKnownFiles()
    {
        File[] foundFiles = this.baseStorageDirectory.listFiles() ;

        for(File cFile: foundFiles)
        {
            if(cFile.isFile() && cFile.canRead())
            {
                this.knownFiles.put(cFile.getName(),cFile) ;
            }
        }
    }

    public void obtainAppParam()
    {
        if(this.knownFiles.containsKey(SimpleFileDataManager.DEFAULT_APPPARAM_FILENAME))
        {//existing file !
            this.appParams = new Properties() ;
            try {
                this.appParams.load(new FileInputStream(this.knownFiles.get(SimpleFileDataManager.DEFAULT_APPPARAM_FILENAME)));
            } catch (IOException e) {
                e.printStackTrace();
                //Only possible whan evil actions happen :-)
                // fallback to dummy default config
                Random random = new Random();
                String serialNumber = Build.SERIAL == null ? "UNKNOWN" : Build.SERIAL;
                serialNumber += "-" + Build.FINGERPRINT;
                CRC32 crc32 = new CRC32();
                crc32.update(serialNumber.getBytes());
                //long terminalId = crc32.getValue();
                long userId = random.nextLong();

                this.appParams = new Properties() ;
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL, SimpleFileDataManager.DEFAULT_SERVER_BASE_URL) ;
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_USERID,Long.toString(userId)) ;
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID,serialNumber) ;

            }
        }else
        {//no file
            Random random = new Random();
            String serialNumber = Build.SERIAL == null ? "UNKNOWN" : Build.SERIAL;
            serialNumber += "-" + Build.FINGERPRINT;
            CRC32 crc32 = new CRC32();
            crc32.update(serialNumber.getBytes());
            //long terminalId = crc32.getValue();
            long userId = random.nextLong();

            this.appParams = new Properties() ;
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL, SimpleFileDataManager.DEFAULT_SERVER_BASE_URL) ;
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_USERID,Long.toString(userId)) ;
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID,serialNumber) ;

            File newAppParamFile = new File(this.baseStorageDirectory, SimpleFileDataManager.DEFAULT_APPPARAM_FILENAME);
            try {
                this.appParams.store(new FileOutputStream(newAppParamFile,false), SimpleFileDataManager.DEFAULT_PARAMFILE_COMMENT);
            } catch (IOException e) {
                e.printStackTrace();
                //TODO how to handle weird things ? => no drama, we have in-memory default config....
            }
        }
    }


    public static SimpleFileDataManager getInstance(Context context) {

        if(SimpleFileDataManager.instance == null){
            SimpleFileDataManager.instance = new SimpleFileDataManager(context);
        }

        return SimpleFileDataManager.instance ;

    }


    public void wireWebView(WebView webView) {
        WebViewClient customClient = new WebViewClient() {

            WebView relatedWebView;
            SimpleFileDataManager xmlDataManager;

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
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                //TODO: ensure LOLLIPOP support !!

                Uri uri = request.getUrl();
                if ((uri != null) && (uri.getScheme() != null)&& (uri.getScheme().contentEquals("ueshiba"))) {

                    return this.xmlDataManager.obtainResource(uri.toString());

                }


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

                URI uri = URI.create(url);
                if ((uri != null) && (uri.getScheme() != null)&& (uri.getScheme().contentEquals("ueshiba"))) {

                    return this.xmlDataManager.obtainResource(uri.toString());

                }


                return null;
            }

            public WebViewClient setup(WebView webView1, SimpleFileDataManager xmlDataManager1) {
                this.relatedWebView = webView1;
                this.xmlDataManager = xmlDataManager1;
                return this;
            }
        }.setup(webView, this);

        webView.setWebViewClient(customClient);
    }


    protected  WebResourceResponse obtainResource(String uri)
    {

        this.updateKnownFiles();

        URI uriObject = URI.create(uri);

        String targetResource = uriObject.getAuthority() ;
        String mimeType = "text/plain" ; //default MIME

        if(targetResource.endsWith(".xhtml"))
        {
            mimeType = "text/xhtml" ;
        }else if(targetResource.endsWith(".txt"))
        {
            mimeType = "text/plain" ;
        }else if (targetResource.endsWith(".jpg"))
        {
            mimeType = "image/jpeg" ;
        }else if (targetResource.endsWith(".png"))
        {
            mimeType = "image/png";
        }else if (targetResource.endsWith(".mp4"))
        {
            mimeType = "video/mpeg" ;
        }else{
            //illegal mimeTpe (value used later for protection against unwanted files)
            mimeType = "ILLEGAL!!" ;
        }



        if(this.knownFiles.containsKey(targetResource))
        {//We got it
            File targetFile = this.knownFiles.get(targetResource) ;

            //TODO give the file !

        }else
        {//try to fetch
            URI ueshibaServerUri = URI.create(this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL) + "/data/get/" + targetResource);
            HttpGet getReQuest = new HttpGet(ueshibaServerUri);
            getReQuest.addHeader(SimpleFileDataManager.PARAM_NAME_USERID,this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_USERID));
            getReQuest.addHeader(SimpleFileDataManager.PARAM_NAME_TERMINALID,this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID));

            HttpResponse httpResponse = null ;
            try {
                 httpResponse = this.httpClient.execute(getReQuest) ;
            } catch (IOException e) {
                e.printStackTrace();
                //TODO ouch... handle bad things !
            }

            if(httpResponse == null)
            {
                //TODO current code prevents only bad exit, please make it better !
                return null;
            }

            if(httpResponse.getStatusLine().getStatusCode() == 200 && !mimeType.equalsIgnoreCase("ILLEGAL!!"))
            {

                try {
                    File newResourceFile = new File(this.baseStorageDirectory,targetResource);
                    FileOutputStream fos = null;
                    fos = new FileOutputStream(newResourceFile,false);
                    httpResponse.getEntity().writeTo(fos);
                    fos.flush();
                    fos.close();

                    //TODO process text/plain ad package => fetch lines as wanted resources + display first html
                    //TODO for other files => pass it to WebResponse




                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //TODO handle bad things !
                } catch (IOException e) {
                    e.printStackTrace();
                    //TODO handle bad things !
                }

            }



        }



        return null ;
    }

}
