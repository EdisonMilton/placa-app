package com.placa.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.CookieManager;

public class MainActivity extends Activity {

    WebView webView;

    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings ws = webView.getSettings();

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(webView, true);


        // navegación dentro de la app
        webView.setWebViewClient(new WebViewClient());


        // permite subir archivos
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();

                try {
                    startActivityForResult(intent, FILE_REQUEST);
                } catch (Exception e) {
                    return false;
                }

                return true;
            }
        });


        // mantener presionado para actualizar
        webView.setOnLongClickListener(v -> {
            webView.reload();
            return true;
        });


        webView.loadUrl("https://placa.algoritmo.xyz");
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FILE_REQUEST){

            if(filePathCallback != null){

                Uri[] result = WebChromeClient.FileChooserParams
                        .parseResult(resultCode, data);

                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
        }
    }


    @Override
    public void onBackPressed() {

        if(webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
