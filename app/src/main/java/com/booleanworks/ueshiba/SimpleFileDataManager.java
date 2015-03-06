package com.booleanworks.ueshiba;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.zip.CRC32;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by vortigern on 05/03/15.
 */
public class SimpleFileDataManager {

    protected static SimpleFileDataManager instance;
    public static final String DEFAULT_SERVER_BASE_URL = "https://ueshiba.booleanworks.com";
    public static final String DEFAULT_APPPARAM_FILENAME = "appParam.properties";

    public static final long UESHIBA_DATAMANAER_VERSION = 10;
    public static final String DEFAULT_PARAMFILE_COMMENT = "UESHIBA-" + Long.toString(SimpleFileDataManager.UESHIBA_DATAMANAER_VERSION);

    public static final String PARAM_NAME_BASE_URL = "baseUrl";
    public static final String PARAM_NAME_USERID = "userId";
    public static final String PARAM_NAME_TERMINALID = "terminalId";


    protected File baseStorageDirectory;

    protected AndroidHttpClient httpClient;

    protected Properties appParams;

    protected HashMap<String, File> knownFiles;
    protected HashMap<String, File> knownInternalFiles;

    protected Context context;

    protected SimpleFileDataManager(Context context) {

        this.context = context;

        this.baseStorageDirectory = context.getExternalFilesDir(null);

        this.httpClient = AndroidHttpClient.newInstance("ueshiba", context);
        HttpConnectionParams.setSoTimeout(this.httpClient.getParams(), 60 * 1000);
        HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(), 60 * 1000);

        this.knownFiles = new HashMap<String, File>();

        this.updateKnownFiles();
        this.obtainAppParam();


    }

    public void updateKnownFiles() {
        File[] foundFiles = this.baseStorageDirectory.listFiles();

        for (File cFile : foundFiles) {
            if (cFile.isFile() && cFile.canRead()) {
                this.knownFiles.put(cFile.getName(), cFile);
            }
        }
    }

    public void obtainAppParam() {
        if (this.knownFiles.containsKey(SimpleFileDataManager.DEFAULT_APPPARAM_FILENAME)) {//existing file !
            this.appParams = new Properties();
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

                this.appParams = new Properties();
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL, SimpleFileDataManager.DEFAULT_SERVER_BASE_URL);
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_USERID, Long.toString(userId));
                this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID, serialNumber);

            }
        } else {//no file
            Random random = new Random();
            String serialNumber = Build.SERIAL == null ? "UNKNOWN" : Build.SERIAL;
            serialNumber += "-" + Build.FINGERPRINT;
            CRC32 crc32 = new CRC32();
            crc32.update(serialNumber.getBytes());
            //long terminalId = crc32.getValue();
            long userId = random.nextLong();

            this.appParams = new Properties();
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL, SimpleFileDataManager.DEFAULT_SERVER_BASE_URL);
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_USERID, Long.toString(userId));
            this.appParams.setProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID, serialNumber);

            File newAppParamFile = new File(this.baseStorageDirectory, SimpleFileDataManager.DEFAULT_APPPARAM_FILENAME);
            try {
                this.appParams.store(new FileOutputStream(newAppParamFile, false), SimpleFileDataManager.DEFAULT_PARAMFILE_COMMENT);
            } catch (IOException e) {
                e.printStackTrace();
                //TODO how to handle weird things ? => no drama, we have in-memory default config....
            }
        }
    }


    public static SimpleFileDataManager getInstance(Context context) {

        if (SimpleFileDataManager.instance == null) {
            SimpleFileDataManager.instance = new SimpleFileDataManager(context);
        }

        return SimpleFileDataManager.instance;

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
                if ((uri != null) && (uri.getScheme() != null) && (uri.getScheme().contentEquals("ueshiba"))) {

                    Log.d("Ueshiba","intercept1: " + uri.toString());
                    return this.xmlDataManager.obtainResource(uri.toString());

                }

                Log.d("Ueshiba","no-intercept1: " + uri.toString());
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
                if ((uri != null) && (uri.getScheme() != null) && (uri.getScheme().contentEquals("ueshiba"))) {

                    Log.d("Ueshiba","intercept2: " + url);
                    return this.xmlDataManager.obtainResource(uri.toString());

                }

                Log.d("Ueshiba","no-intercept2: " + url);
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


    protected WebResourceResponse obtainResource(String uri) {

        this.updateKnownFiles();

        URI uriObject = URI.create(uri);

        String targetResource = uriObject.getAuthority();
        String mimeType = "text/plain"; //default MIME

        if (targetResource.endsWith(".xhtml")) {
            mimeType = "text/xhtml";
        } else if (targetResource.endsWith(".txt")) {
            mimeType = "text/plain";
        } else if (targetResource.endsWith(".jpg")) {
            mimeType = "image/jpeg";
        } else if (targetResource.endsWith(".png")) {
            mimeType = "image/png";
        } else if (targetResource.endsWith(".mp4")) {
            mimeType = "video/mpeg";
        } else {
            //illegal mimeTpe (value used later for protection against unwanted files)
            mimeType = "ILLEGAL!!";
        }


        if (this.knownFiles.containsKey(targetResource)) {//We got it
            File targetFile = this.knownFiles.get(targetResource);

            //TODO give the file !

            return null;


        } else {//try to fetch


            try {
                File targetResourceFile = new File(this.baseStorageDirectory, targetResource);
                URL serverURL = new URL(this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_BASE_URL) + "/data/get/" + targetResource);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) serverURL.openConnection();

                httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return hostname.contains("booleanworks.com");
                    }
                });

                //httpsURLConnection.setSSLSocketFactory(SSLContext.getInstance("SSLv3").getSocketFactory());
                SSLCertificateSocketFactory sslCertificateSocketFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(20*1000);
                sslCertificateSocketFactory.setTrustManagers(new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }});

                httpsURLConnection.setSSLSocketFactory(sslCertificateSocketFactory);


                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.addRequestProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID, this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_TERMINALID));
                httpsURLConnection.addRequestProperty(SimpleFileDataManager.PARAM_NAME_USERID, this.appParams.getProperty(SimpleFileDataManager.PARAM_NAME_USERID));

                FileOutputStream fos = new FileOutputStream(targetResourceFile, false);
                InputStream is = httpsURLConnection.getInputStream();
                int burstSize = 16 * 1024 ;
                while (burstSize > 0) {
                    byte[] fileWritebuffer = new byte[16 * 1024];
                    burstSize = is.read(fileWritebuffer);
                    if(burstSize>0){
                        fos.write(fileWritebuffer,0,burstSize);
                    }

                }
                fos.flush();
                fos.close();

                if(mimeType.contains("text/plain"))
                {//process package, fetch all files and render the first
                    BufferedReader targetFileBufferedReader = new BufferedReader(new FileReader(targetResourceFile)) ;
                    String cLine = "" ;
                    String firstLine = null ;
                    while(cLine != null)
                    {
                        cLine = targetFileBufferedReader.readLine() ;
                        if((cLine != null) && (firstLine == null))
                        {
                            firstLine = cLine ;
                        }
                        this.obtainResource("ueshibla://" + cLine) ;
                    }

                    File newTarget = new File(this.baseStorageDirectory,firstLine) ;
                    if(newTarget.exists())
                    {//swap package file with first file of the package
                        mimeType = "text/xhtml" ; // assume it's an html page
                        targetResourceFile = newTarget ;
                    }

                    targetFileBufferedReader.close();

                }

                long fileSize = targetResourceFile.length() ;
                Log.d("ueshiba","fileSize= " + fileSize);

                FileInputStream fis = new FileInputStream(targetResourceFile) ;
                WebResourceResponse webResourceResponse = new WebResourceResponse(mimeType,"UTF-8",fis);
                Log.d("Ueshiba","webResponse OK");
                return webResourceResponse ;


            } catch (IOException e) {
                e.printStackTrace();
                //TODO handle weird things
                File targetResourceFile = new File(this.baseStorageDirectory, targetResource);
                if (targetResourceFile.exists()) {
                    targetResourceFile.delete();
                }
                Log.d("Ueshiba","Exception",e);
                return null;
            }


        }

    }

}
