package com.booleanworks.ueshiba;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


public class WebHomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_home);
        WebView webView = (WebView) this.findViewById(R.id.webView) ;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this,"activity");
        webView.loadData("<h1 id='test' onclick='alert(activity.testJs())'>default text 5</h1><script type='text/javascript'>alert(activity.testJs());</script>","text/html","UTF-8");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @JavascriptInterface
    public String testJs()
    {
        return "Hello from activity ! (5)" ;
    }
}
