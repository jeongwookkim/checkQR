package jeongwookdongeun.easyqrcheckin;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private WebSettings webSettings;

    private AdView adView;

    private CookieManager cookieManager;

    private SharedPreferences preferences;
    private boolean isNaverMode;
    private boolean isClickedKakaoCheckInShortcut;

    private AppUpdateManager appUpdateManager;
    private int REQUEST_CODE = 366;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* 웹뷰 */
        webView = (WebView) findViewById(R.id.qrWebView);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            //모든 링크가 WebView내에서 동작하게 함
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //네트워크 url 경우
                if( URLUtil.isNetworkUrl(url) ) {
                    //네이버 홈으로 가는 경우 true, 아니면 false
                    return "https://m.naver.com/".equals(url) ? true : false;
                }

                //Intent.ACTION_VIEW 사용으로 핸드폰 기본 웹 브라우저로 검색 된 페이지(현재 URL)가 나옴
               Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try{
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.nhn.android.search"));
                    if(webIntent.resolveActivity(getPackageManager()) != null){
                        Toast.makeText(getApplicationContext(), "네이버 앱 설치 및 업데이트, 로그인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        startActivity(webIntent);
                    }
                }

                return true;
            }

            @Override
            //url 로딩 완료되면 호출 되는 함수
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //Webview의 RAM과 영구 저장소 사이에 쿠키를 강제로 동기화 시켜줌 (버전에 따라 선언을 다르게 해줌)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    //롤리팝 이상에서는 CookieManager의 flush를 하도록변경됨
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




        /* 쿠키 허용 설정 */
        try {
            cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            } else {
                CookieSyncManager.createInstance(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




        /* 액션바 */
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.


        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.actionbar, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Toolbar parent = (Toolbar) actionbar.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }




        /* 토글 값, 카카오 QR 바로가기 클릭 여부 저장 하기 위한 초기 setting */
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        isNaverMode = preferences.getBoolean("NAVER_MODE", true);
        isClickedKakaoCheckInShortcut = preferences.getBoolean("KAKAO_SHORTCUT", false);



        /* 토글 */
        ToggleButton toggleButton = findViewById(R.id.togle);
        toggleButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if(isChecked){ //토글이 ON일 때

                            webView.setVisibility(View.INVISIBLE);
                            findViewById(R.id.logoutBtn).setVisibility(View.INVISIBLE);
                            findViewById(R.id.kakaoqrBtn).setVisibility(View.VISIBLE);

                            //토글 값 저장
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("NAVER_MODE", false);
                            editor.commit();

                        } else { //토글이 OFF 일 때

                            webView.setVisibility(View.VISIBLE);
                            findViewById(R.id.logoutBtn).setVisibility(View.VISIBLE);
                            findViewById(R.id.kakaoqrBtn).setVisibility(View.INVISIBLE);

                            //토글 값 저장
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("NAVER_MODE", true);
                            editor.commit();

                        }

                    }

                }
        );
        if (isNaverMode) {
            toggleButton.setChecked(false);
        } else {
            toggleButton.setChecked(true);
        }




        /* 카카오 QR 바로가기 버튼 */
        Button kakaoqrBtn = (Button) findViewById(R.id.kakaoqrBtn);
        kakaoqrBtn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                //카카오 QR 체크인 바로가기 버튼 클릭 여부 저장
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("KAKAO_SHORTCUT", true);
                editor.commit();

                //카카오 QR 체크인 URL 이동
                moveKakaoQRCheckIn();
            }

        });




        /* 카카오 모드이고 체크인 바로가기 버튼을 이미 한번 눌렀을 경우 (KAKAO_SHORTCUT가 true로 저장된 경우) */
        if(!isNaverMode && isClickedKakaoCheckInShortcut) {
            moveKakaoQRCheckIn();
        }




        /* 로그아웃 버튼 */
        Button logoutBtn = (Button) findViewById(R.id.logoutBtn) ;
        logoutBtn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                webView.clearCache(true);
                webView.clearHistory();

                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                webView.loadUrl("https://nid.naver.com/login/privacyQR");
            }

        });




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




        /* 최초 실행 확인 여부를 위한 String 저장 */
        getIntent().setAction("First created");




        /* 앱 업데이트 */
        // 앱 업데이트 매니저 초기화
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        // 업데이트를 체크하는데 사용되는 인텐트를 리턴한다.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> { // appUpdateManager이 추가되는데 성공하면 발생하는 이벤트
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // UpdateAvailability.UPDATE_AVAILABLE == 2 이면 앱 true
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) { // 허용된 타입의 앱 업데이트이면 실행 (AppUpdateType.IMMEDIATE || AppUpdateType.FLEXIBLE)
                // 업데이트가 가능하고, 상위 버전 코드의 앱이 존재하면 업데이트를 실행한다.
                requestUpdate (appUpdateInfo);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }

        String action = getIntent().getAction();

        //이미 실행된 적이 있다면 재로드
        if(isNaverMode) {
            if (action == null || !action.equals("First created")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                getIntent().setAction(null);
            }
        }

        //앱에 설치 대기 중인 업데이트가 있는지 확인
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                requestUpdate(appUpdateInfo);
                            }
                        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /* 카카오 모드인 경우 뒤로 가기 버튼을 눌렀을 때 KAKAO_SHORTCUT false로 초기화 */
        if(!isNaverMode) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("KAKAO_SHORTCUT", false);
            editor.commit();
        }
    }

    // 카카오 QR 체크인 URL로 이동
    public void moveKakaoQRCheckIn() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("kakaotalk://inappbrowser?url=https://accounts.kakao.com/qr_check_in"));

        try{
            startActivity(intent);
        }catch (ActivityNotFoundException e) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            webIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.kakao.talk"));
            if (webIntent.resolveActivity(getPackageManager()) != null) {
                Toast.makeText(getApplicationContext(), "카카오톡 앱 설치 및 업데이트, 로그인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                startActivity(webIntent);
            }
        }
    }

    // 업데이트 요청
    private void requestUpdate (AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // 'getAppUpdateInfo()' 에 의해 리턴된 인텐트
                    appUpdateInfo,
                    // 'AppUpdateType.FLEXIBLE': 사용자에게 업데이트 여부를 물은 후 업데이트 실행 가능
                    // 'AppUpdateType.IMMEDIATE': 사용자가 수락해야만 하는 업데이트 창을 보여줌
                    AppUpdateType.IMMEDIATE,
                    // 현재 업데이트 요청을 만든 액티비티, 여기선 MainActivity.
                    this,
                    // onActivityResult 에서 사용될 REQUEST_CODE.
                    REQUEST_CODE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //onActivityResult() 콜백을 사용해서 업데이트 실패 또는 취소 처리
        if (requestCode == REQUEST_CODE) {
            Toast myToast = Toast.makeText(this.getApplicationContext(), "REQUEST_CODE", Toast.LENGTH_SHORT);
            myToast.show();

            // 업데이트가 성공적으로 끝나지 않은 경우
            if (resultCode != RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Update flow failed! Result code: " + resultCode, Toast.LENGTH_LONG).show();
                // 업데이트가 취소되거나 실패하면 업데이트를 다시 요청할 수 있다.,
                // 업데이트 타입을 선택한다 (IMMEDIATE || FLEXIBLE).
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            // flexible한 업데이트를 위해서는 AppUpdateType.FLEXIBLE을 사용한다.
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // 업데이트를 다시 요청한다.
                        requestUpdate(appUpdateInfo);
                    }
                });
            }
        }
    }
}