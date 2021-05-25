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

    private WebView webView; //웹뷰 선언
    private WebSettings webSettings; //웹뷰 세팅
    private CookieManager cookieManager;

    private AdView adView; //애드뷰 변수 선언
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //웹뷰 시작
        webView = (WebView) findViewById(R.id.qrWebView);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                Uri requested = Uri.parse(url);

                //네트워크 url일 경우
                if( URLUtil.isNetworkUrl(url) ) {
                    if("https://m.naver.com/".equals(url)){
                        //쿠키가 없으면 로그인 페이지
                        if("".equals(cookie) || cookie == null){
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                                cookieManager.setAcceptThirdPartyCookies(webView, true);
                            }
                            webView.loadUrl("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
                            //쿠키가 있으면 자동로그인
                        }else{
                            webView.loadUrl("https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
                        }
                        return true;
                    }
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try{
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nhn.android.search"));
                    if(webIntent.resolveActivity(getPackageManager()) != null){
                        startActivity(webIntent);
                    }
                }

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    CookieSyncManager.getInstance().sync();
                }else{
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
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        webSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        webView.loadUrl("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR");

        if(cookieManager == null){
            cookieManager = CookieManager.getInstance();
            webView.loadUrl("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
        }

        setCookieAllow(cookieManager, webView);

        //재시작 될 때 확인하기 위한 String
        getIntent().setAction("Already created");


        //배너
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

        if (action == null || !action.equals("Already created")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else
            getIntent().setAction(null);
    }

    private void setCookieAllow(CookieManager cookieManager, WebView webView) {

        try {

            cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            cookie = cookieManager.getCookie("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR");
            Log.e("cookie:::", cookie);
            Log.e("hasCookie:::", cookieManager.hasCookies() + "");

            if("".equals(cookie) || cookie == null){
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                    cookieManager.setAcceptThirdPartyCookies(webView, true);
                }
                webView.loadUrl("https://nid.naver.com/nidlogin.login?svctype=262144&url=https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
            }else{
                webView.loadUrl("https://nid.naver.com/login/privacyQR"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}