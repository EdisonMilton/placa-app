package com.placa.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.app.DownloadManager;
import android.os.Environment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ValueCallback;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends Activity {

    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout pantallaCarga;

    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contenedor = new FrameLayout(this);

        swipeRefreshLayout = new SwipeRefreshLayout(this);
        webView = new WebView(this);

        swipeRefreshLayout.addView(webView);
        swipeRefreshLayout.setColorSchemeColors(Color.rgb(98, 0, 238));

        contenedor.addView(swipeRefreshLayout);

        pantallaCarga = new LinearLayout(this);
        pantallaCarga.setOrientation(LinearLayout.VERTICAL);
        pantallaCarga.setGravity(Gravity.CENTER);
        pantallaCarga.setPadding(50, 50, 50, 50);

        GradientDrawable fondo = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.rgb(245, 238, 255),
                        Color.rgb(255, 255, 255),
                        Color.rgb(232, 214, 255)
                }
        );
        pantallaCarga.setBackground(fondo);

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("logo_placa", "drawable", getPackageName()));
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(190, 190);
        logoParams.setMargins(0, 0, 0, 20);
        logo.setLayoutParams(logoParams);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        TextView titulo = new TextView(this);
        titulo.setText("PLACA");
        titulo.setTextSize(38);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setTextColor(Color.rgb(98, 0, 238));
        titulo.setGravity(Gravity.CENTER);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Plataforma de Aprendizaje y Calificaciones");
        subtitulo.setTextSize(16);
        subtitulo.setTextColor(Color.rgb(90, 80, 110));
        subtitulo.setGravity(Gravity.CENTER);
        subtitulo.setPadding(0, 8, 0, 35);

        ProgressBar progreso = new ProgressBar(this);
        progreso.setIndeterminate(true);
        progreso.getIndeterminateDrawable().setTint(Color.rgb(98, 0, 238));

        TextView mensaje = new TextView(this);
        mensaje.setText("Cargando sistema académico...");
        mensaje.setTextSize(17);
        mensaje.setTypeface(Typeface.DEFAULT_BOLD);
        mensaje.setTextColor(Color.rgb(50, 50, 50));
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setPadding(0, 30, 0, 0);

        TextView submensaje = new TextView(this);
        submensaje.setText("Preparando información de PLACA");
        submensaje.setTextSize(14);
        submensaje.setTextColor(Color.rgb(110, 110, 110));
        submensaje.setGravity(Gravity.CENTER);
        submensaje.setPadding(0, 10, 0, 0);

        pantallaCarga.addView(logo);
        pantallaCarga.addView(titulo);
        pantallaCarga.addView(subtitulo);
        pantallaCarga.addView(progreso);
        pantallaCarga.addView(mensaje);
        pantallaCarga.addView(submensaje);

        contenedor.addView(pantallaCarga);
        setContentView(contenedor);

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
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                swipeRefreshLayout.setRefreshing(true);
                pantallaCarga.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                pantallaCarga.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                filePathCallback = callback;

                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "No se pudo abrir el selector de archivos", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("Cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);

                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);

                request.setTitle(fileName);
                request.setDescription("Descargando archivo de PLACA...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                Toast.makeText(this, "Descarga iniciada", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        webView.loadUrl("https://placa.algoritmo.xyz");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST && filePathCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
