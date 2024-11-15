package com.slide.sync;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class HelpActivity extends AppCompatActivity {

    private WebView webView;
    private TextView noInternetMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helpactivity);

        webView = findViewById(R.id.webview);
        noInternetMessage = findViewById(R.id.no_internet_message);

        // Check internet connection and load URL if connected
        if (isInternetConnected()) {
            webView.setVisibility(WebView.VISIBLE);
            noInternetMessage.setVisibility(TextView.GONE);
            loadWebPage("https://slidesync.web.app");
        } else {
            webView.setVisibility(WebView.GONE);
            noInternetMessage.setVisibility(TextView.VISIBLE);
        }

    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Function to load a URL in the WebView
    private void loadWebPage(String url) {
        webView.getSettings().setJavaScriptEnabled(true);  // Enable JavaScript
        webView.setWebViewClient(new WebViewClient());  // Ensures links open in WebView
        webView.setWebChromeClient(new WebChromeClient());  // Allows for more features like JavaScript dialogs
        webView.loadUrl(url);
    }
}