package jeongwookdongeun.easyqrcheckin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private WebSettings webSettings;

    private AdView adView;

    private CookieManager cookieManager;
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 웹뷰 */
        webView = (WebView) findViewById(R.id.qrWebView);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //네트워크 url일 경우 -> 네이버 홈 으로 갈 경우 return true, 아니면 return false
                return URLUtil.isNetworkUrl(url) ? ("https://m.naver.com/".equals(url) ? true : false) : true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
            }

        });
        webSettings = webView.getSettings(); //세부 세팅 등록
        webSettings.setJavaScriptEnabled(true); //웹페이지 자바스크립트 허용 여부
        webSettings.setSupportMultipleWindows(true); //새창 띄우기 허용 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        webSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(false); // 화면 줌 허용 여부
        webSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        webSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        webView.loadUrl("https://nid.naver.com/login/privacyQR");


        /* 쿠키 */
        try {
            cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            cookie = cookieManager.getCookie("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR");

            //쿠키가 없을 경우
            if("".equals(cookie) || cookie == null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    cookieManager.setAcceptThirdPartyCookies(webView, true);
                }
            }
            webView.loadUrl("https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* 재실행 될 때 확인하기 위한 String 저장 */
        getIntent().setAction("Already created");


        /* 배너 */
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = findViewById(R.id.adView); //배너광고 레이아웃 가져오기
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER); //광고 사이즈는 배너 사이즈로 설정
        adView.setAdUnitId("ca-app-pub-6854372688341522/4174317953");
    }

    @Override
    protected void onResume() {
        super.onResume();

        String action = getIntent().getAction();
        //이미 실행된 적이 있다면 재로드
        if (action == null || !action.equals("Already created")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else
            getIntent().setAction(null);
    }

}