package kr.co.lguplus.mucloud;


import android.app.Activity;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebPopupActivity extends Activity {

    private WebView pWebView;

    ///================================================================================================
    /// WEB Interface
    ///================================================================================================

    ///  WEB To App
    private class AndroidBridgePopup{
        @JavascriptInterface
        public void closePopup(){
            //LogManager.DEBUG("From web : close popup");
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_popup);

        try {

            Intent intent = this.getIntent();
            String pageUrl = intent.getStringExtra("SITE");
            pWebView = (WebView) findViewById(R.id.webView_contents);
            WebSettings wSetting = pWebView.getSettings();
            wSetting.setJavaScriptEnabled(true);
            wSetting.setDomStorageEnabled(true);
            if(Build.VERSION.SDK_INT > 10) {
                wSetting.setAllowContentAccess(true);   // // Over API level 11
            }
            if(Build.VERSION.SDK_INT > 16) {
                wSetting.setAllowFileAccessFromFileURLs(true);
                wSetting.setAllowUniversalAccessFromFileURLs(true);
            }
            wSetting.setAllowFileAccess(true);
            wSetting.setAppCacheEnabled(true);
            wSetting.setSupportZoom(true);
            wSetting.setBuiltInZoomControls(true);

            pWebView.getSettings().setJavaScriptEnabled(true);

            pWebView.loadUrl(pageUrl);
            pWebView.setVerticalScrollbarOverlay(true);

            pWebView.addJavascriptInterface(new AndroidBridgePopup(), "CloudPCAppConnectorPopup");
            pWebView.setWebViewClient(new WebViewClientClass());

        }catch(Exception ex){}
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            // TODO Auto-generated method stub
            //super.onReceivedSslError(view, handler, error);

            handler.proceed();
        }
    }
}
