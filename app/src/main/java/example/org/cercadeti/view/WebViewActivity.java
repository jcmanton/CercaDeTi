package example.org.cercadeti.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

import example.org.cercadeti.R;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = "";
        if (getIntent().getExtras() != null) {
            url = getIntent().getExtras().getString("url");
        }
        setContentView(R.layout.activity_webview);
        WebView mywebview = (WebView) findViewById(R.id.webView);
        mywebview.loadUrl(url);
    }

    public void back(View v) {
        finish();
    }
}