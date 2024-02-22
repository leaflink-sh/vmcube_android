package kr.co.lguplus.mucloud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.navdrawer.SimpleSideDrawer;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import cipher.Cipher;
import common.LogManager;
import common.SQLiteManager;
import common.SystemProperty;
import common.Utils;
import connection.WebDataRequest;
import dto.RET_OI_VMC_01;
import dto.RET_OI_VMC_02;
import dto.RET_OI_VMC_03;
import dto.RET_OI_VMC_04;
import dto.UserInfo;

public class WebViewActivity extends Activity implements View.OnClickListener {

//    private static final String STRPATH = Environment.getExternalStorageDirectory() + "/Download/";
    //region ==== VALIABLES ====

    public android.webkit.CookieManager cookiemanager;
    private ProgressDialog mProgressDialog;

    private WebView mWebView;
    private String  _mimeType;
    private String  _fileName;
    public  Uri     _downUrl;
    private Boolean _isDownloading = false;
    private String  _pageUrl;
    private String  _webMessage; // WEB에서 보내는 ALERT 메시지
    private String  _isPkgList = "";  // 패키지 설치여부 확인 리스트
    private SimpleSideDrawer mNav;  // SideMenu1

    private String  _popupUrl = null;
    private String  _hashedSn = null;
    private int     _loginStep = 0;
    private String  _token2 = null; // 2차 토큰값.
    private boolean _isNextInf = false;    // false 일 경우에만 1차 인증부터 가능..

    private String _authToken = null;
    private String _webResponse = null;

    private SQLiteManager sqLiteManager = null;

    //endregion

    //region ===== FINAL VALIABLES ======
    private final int APP_CLOSE                 = 1000; // 앱 종료 ID
    private final int MSG_CHAGE_PASSWORD        = 1001; // 비밀번호 변경 URL 호출
    private final int MSG_CHECK_APPS            = 1002; // 필수앱설치확인
    private final int MSG_OPEN_LICENSE          = 1003; // 오픈소스 라이센스
    private final int MSG_EXIT                  = 1004; // 종료
    private final int MSG_HOME                  = 1005; // 홈으로..
    private final int MSG_PKGINSTALL_RES        = 1006; // 필수 패키지 설치 리스트 리턴 from web
    private final int MSG_PDIALOG_SHOW          = 1007; // Progress Dialog 호출 from web
    private final int MSG_PDIALOG_HIDE          = 1008; // Progress Dialog 호출 from web
    private final int MSG_AUTH_FAILED           = 1009; // Token 인증 실패
    private final int MSG_AUTH_SUCCESS          = 1010; // Token 인증 성공
    private final int MSG_AUTH_END              = 1011; // Token 모든 인증 후 로그인 페이지로 이동.
    private final int MSG_URL_MAIN_1            = 1012; // 1차 토큰 로그인 메인 페이지로 이동.
    private final int MSG_URL_MAIN_2            = 1013; // 2차 토큰 로그인 메인 페이지로 이동.
    private final int MSG_WEB_POPUP             = 1014; // 팝업뷰 요청(공지사항 등)
    private final int MSG_SET_INIT              = 1015; // 페이지 인증 초기화해서 처음부터 실행..(세션 종료가 되는 문제로 재인증필요해서..)
    private final int APP_IF_ERROR              = 1016; // 웹 인터페이스 서버와 통신이 안되는 경우 종료...
    private final int MSG_DISK_LOCK_EXECUTE     = 1017; // Cloud Disk Lock OR UnLock 실행.
    private final int MSG_SLIDE_MENU_REFRESH    = 1018; // Cloud Disk Lock 메뉴 설정.
    private final int MSG_VMC_04_CALL_SUCCESS   = 1019; // Cloud Disk Lock 잠금 설정 요청 성공.
    private final int MSG_VMC_04_CALL_FAILED    = 1020; // Cloud Disk Lock 잠금 설정 요청 실패.
    private final int MSG_DEVICE_REGSTER        = 1021; // 단말등록 페이지 이동
    private final int MSG_DEVICE_REGSTER_NEED_LOGIN = 1022; // 로그인 필요.

    private final String _url = kr.co.lguplus.mucloud.CPCProperty.getUrl();

    RelativeLayout diskLayout;
    RelativeLayout diskUnLayout;
    LayoutInflater diskFactory;
    View diskView;
    View diskUnView;
    ImageButton diskLockBtn;
    ImageButton diskUnLockBtn;

    RelativeLayout equipmentRegist; // 단말등록

    //endregion

    Utils utils = new Utils(this);
    //private final String KEY_PASS = CPCProperty.getKey();
    private String KEY_PASS = null; //RET_OI_VMC_03.CRYPTO_KEY;
    private byte[] KEY = null; // KEY_PASS.getBytes();

    //region ==== Handler event ====
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == MSG_CHAGE_PASSWORD) { // 비밀번호 변경 URL

                mWebView.loadUrl(_url + CPCProperty.getHost(1));

            } else if (msg.what == SystemProperty.MSG_GET_PKG_INFO_EXIST) {

                mWebView.loadUrl(_url + CPCProperty.getHost(2));

            } else if (msg.what == MSG_CHECK_APPS) {   // 필수 설치 앱 리스트

                mWebView.loadUrl(_url + CPCProperty.getHost(2));

            }
            /*
            else if(msg.what == MSG_HOME){  // LOGO Click -- HOME...

                mWebView.loadUrl(_url + CPCProperty.getHost(5));

            }*/
            else if (msg.what == MSG_PKGINSTALL_RES) {   // WEB에서 패키지 설치 여부 체크해서 결과 리턴...
                LogManager.DEBUG("Installed package result : " + _isPkgList);
                mWebView.loadUrl("javascript:callFromPkgIsInstalled('" + _isPkgList + "');");

            } else if (msg.what == MSG_PDIALOG_SHOW) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog = ProgressDialog.show(WebViewActivity.this, "", _webMessage.toString(), true);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }, 3000); // 3초후에 종료
                    }
                });

            } else if (msg.what == MSG_PDIALOG_HIDE) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } else if (msg.what == MSG_AUTH_SUCCESS) {

                new WebAsyncTask().execute("OI_VMC_02");

            } else if (msg.what == MSG_AUTH_FAILED) {
                showDialog(MSG_AUTH_FAILED);

            } else if (msg.what == MSG_AUTH_END) {
                mWebView.loadUrl(_pageUrl);

            } else if (msg.what == MSG_URL_MAIN_1) {
                //mWebView.loadUrl("https://m-ucloud.lgcns.com:8001/vmcube/login/" + UserInfo.USER_DEVICE_ID + "/" + RET_OI_VMC_01.AUTH_TOKEN);
                _loginStep = 1;

                // null 값이오면 처음부터 다시...
                if (_hashedSn == null || RET_OI_VMC_01.AUTH_TOKEN == null) {
                    mHandler.sendEmptyMessage(MSG_SET_INIT);
                } else {

                    String uri = _url + CPCProperty.getHost(6) + "/" + _hashedSn + "/" + RET_OI_VMC_01.AUTH_TOKEN;

                    LogManager.DEBUG("1차 Login URL : " + uri);

                    mWebView.loadUrl(uri);
                }

            } else if (msg.what == MSG_URL_MAIN_2) {
                //mWebView.loadUrl("https://m-ucloud.lgcns.com:8001/vmcube/login/" + UserInfo.USER_DEVICE_ID + "/" + RET_OI_VMC_02.AUTH_TOKEN);
                _loginStep = 2;

                // null 값이오면 처음부터 다시...
                if (_hashedSn == null || RET_OI_VMC_01.AUTH_TOKEN == null) {
                    mHandler.sendEmptyMessage(MSG_SET_INIT);
                } else {
                    //String uri = _url + CPCProperty.getHost(6) + "/" + _hashedSn + "/" + _token2;//RET_OI_VMC_02.AUTH_TOKEN;
                    String uri = _url + CPCProperty.getHost(6) + "/" + _hashedSn + "/" + _token2 + "/android/" + UserInfo.USER_DEVICE_ID;
                    LogManager.DEBUG("2차 Login URL : " + uri);
                    mWebView.loadUrl(uri);
                }

            } else if (msg.what == MSG_WEB_POPUP) {
                try {
                    if (_popupUrl != null) {
                        Intent itt = new Intent(getApplicationContext(), WebPopupActivity.class);
                        itt.putExtra("SITE", _popupUrl);
                        startActivity(itt);
                    }
                } catch (Exception ex) {
                    //LogManager.ERROR(ex.toString());
                }

            } else if (msg.what == MSG_SET_INIT) {
                _isNextInf = false;
                _loginStep = 0;

                String uri = _url + CPCProperty.getHost(5); // /vmcube 로 이동(home)
                mWebView.loadUrl(uri);

            }else if (msg.what == MSG_DISK_LOCK_EXECUTE) {
                LogManager.DEBUG("Execute Cloud Disk Lock");

                //new WebAsyncTask().execute("OI_VMC_04");
                showDialog(MSG_DISK_LOCK_EXECUTE);

            }else if (msg.what == MSG_SLIDE_MENU_REFRESH) {  // Cloud Disk Lock / Unlock 슬라이드 메뉴 동적 생성..
                LogManager.DEBUG("Start Menu Refresh");

                if (SystemProperty.IS_DISK_LOCK_OWNER) {

                    setSideDiskMenu();
                }
            }else if(msg.what == MSG_VMC_04_CALL_SUCCESS) {  // Cloud Disk Lock 잠금 요청 성공

                showDialog(MSG_VMC_04_CALL_SUCCESS);

                setChangeDiskStateMenu();

            }else if(msg.what == MSG_VMC_04_CALL_FAILED) {   // Cloud Disk Lock 잠금 요청 실패
                showDialog(MSG_VMC_04_CALL_FAILED);

            }else if(msg.what == MSG_DEVICE_REGSTER){   // 단말등록 페이지 이동.

                if(SystemProperty.IS_LOGIN) {
                    String uri = _url + CPCProperty.getHost(8);
                    LogManager.DEBUG("단말등록 페이지 이동 : " + uri);

                    mWebView.loadUrl(uri);
                }else{
                    showDialog(MSG_DEVICE_REGSTER_NEED_LOGIN);
                }

            } else {
                showDialog(APP_CLOSE);
                //processKill();
            }
        }
    };

    //endregion

    private void setSideDiskMenu(){
        diskLayout = (RelativeLayout) findViewById(R.id.diskLockLayout);
        diskUnLayout = (RelativeLayout)findViewById(R.id.diskUnLockLayout);

        diskFactory = LayoutInflater.from(this);

        diskView = diskFactory.inflate(R.layout.clouddisk_lock, null);
        diskUnView = diskFactory.inflate(R.layout.clouddisk_unlock, null);
        diskLayout.addView(diskView);
        diskUnLayout.addView(diskUnView);

        diskLockBtn = (ImageButton) findViewById(R.id.side_menu_diskLock);
        diskLockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogManager.DEBUG("Lock click : " + SystemProperty.IS_DISK_LOCK_STATE);

                mHandler.sendEmptyMessage(MSG_DISK_LOCK_EXECUTE);
            }
        });

        diskUnLockBtn = (ImageButton)findViewById(R.id.side_menu_diskUnLock);
        diskUnLockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogManager.DEBUG("UnLock click : " + SystemProperty.IS_DISK_LOCK_STATE);
                mHandler.sendEmptyMessage(MSG_DISK_LOCK_EXECUTE);
            }
        });

        if(SystemProperty.IS_DISK_LOCK_STATE) {
            diskLayout.setVisibility(View.GONE);
            diskUnLayout.setVisibility(View.VISIBLE);
        }else{
            diskLayout.setVisibility(View.VISIBLE);
            diskUnLayout.setVisibility(View.GONE);
        }


    }

    private void setChangeDiskStateMenu(){
        LogManager.DEBUG("setChangeDiskStateMenu()");

        try {

            if(SystemProperty.IS_DISK_LOCK_STATE) {
                diskLayout.setVisibility(View.GONE);
                diskUnLayout.setVisibility(View.VISIBLE);
            }else{
                diskLayout.setVisibility(View.VISIBLE);
                diskUnLayout.setVisibility(View.GONE);
            }

        }catch (Exception ex){
            LogManager.ERROR(ex.toString());
        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        LogManager.DEBUG("---------------- START WebView Application ----------------");

        // SDK 24 이상에서 ica 를 실행하려면 강제로 아래 처리해야 됨..
        // android.os.FileUriExposedException: file:///storage/emulated/0/Download/CloudPC.ica exposed beyond app through Intent.getData()
        if(Build.VERSION.SDK_INT>=24){
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        try {

            // 롤리팝 이전 Service 호출..
            if(Build.VERSION.SDK_INT < 21) {
                startService(new Intent("kr.co.lguplus.mucloud"));
            }
            else {
                Intent ittService = new Intent().setAction("kr.co.lguplus.mucloud");
                ittService.setPackage("kr.co.lguplus.mucloud");
                startService(ittService);
            }

            tmwStartScreenCaptureProtect2();    // MDM 한번더 호출..
        }
        catch (Exception ex)
        {
            LogManager.ERROR(ex.toString());
        }


        //utils.showProcessList(WebViewActivity.this);
        //utils.showServiceList(WebViewActivity.this);

        try{
            KEY_PASS = RET_OI_VMC_03.CRYPTO_KEY;
            KEY = KEY_PASS.getBytes();
        }
        catch(Exception ex){
            mHandler.sendEmptyMessage(APP_IF_ERROR);
        }

        // menu init
        InitView();

        // Cookie 정보 instance 생성
        CookieSyncManager.createInstance(this);
        cookiemanager = android.webkit.CookieManager.getInstance();
        ///

        // File Download 시 ...
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        ///


        // MainActivity 에서 넘어 오는  값 처리
        Intent intent = this.getIntent();
        _pageUrl = intent.getStringExtra("PAGE_URL");
        LogManager.DEBUG("onCreate() URL : " + _pageUrl);
        ///


        mWebView = (WebView) findViewById(R.id.mWebView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });

        WebSettings wSetting = mWebView.getSettings();
        wSetting.setJavaScriptEnabled(true);
        wSetting.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT > 10) {
            wSetting.setAllowContentAccess(true);   // // Over API level 11
        }
        if (Build.VERSION.SDK_INT > 16) {
            wSetting.setAllowFileAccessFromFileURLs(true);
            wSetting.setAllowUniversalAccessFromFileURLs(true);
        }
        wSetting.setAllowFileAccess(true);
        wSetting.setAppCacheEnabled(true);
        wSetting.setSupportZoom(true);
        wSetting.setBuiltInZoomControls(true);

        wSetting.setUseWideViewPort(true);  // ipin
        wSetting.setLoadWithOverviewMode(true); // ipin


        // 처음으로 가는 페이지...
        //_pageUrl = _pageUrl + "/vmCubeMobileDev/login/appinit";
        _pageUrl = _pageUrl + CPCProperty.getHost(7);

        LogManager.DEBUG("HOME : " + _pageUrl);

        //_pageUrl = _pageUrl + "/vmCubeMobile/login?debug=kk";
        if (_pageUrl.trim().length() > 0)
            mWebView.loadUrl(_pageUrl);
        else
            mWebView.loadUrl("https://mucloud.lguplus.co.kr");


        if (Build.VERSION.SDK_INT > 16) {
            //LogManager.DEBUG("API Level > 16");
            mWebView.addJavascriptInterface(new AndroidBridge(), "CloudPCAppConnector");
        } else {
            //LogManager.DEBUG("API Level <= 16");
            mWebView.loadData("", "text/html", null);
            mWebView.addJavascriptInterface(new AndroidBridgeBelow17(), "CloudPCAppConnector");
        }

        mWebView.setWebViewClient(new MyWebViewClient());


        //region --- Download Listener ---
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                LogManager.DEBUG("(onDownloadStart) url : " + url);
                LogManager.DEBUG("(onDownloadStart) userAgent : " + userAgent);
                LogManager.DEBUG("(onDownloadStart) contentDisposition : " + contentDisposition);
                LogManager.DEBUG("(onDownloadStart) Mime : " + mimetype);
                LogManager.DEBUG("(onDownloadStart) contentLength : " + contentLength);


                if (!_isDownloading) {
                    MimeTypeMap mtm = MimeTypeMap.getSingleton();

                    Uri downUrl = Uri.parse(url);

                    String fileName = downUrl.getLastPathSegment();

                    LogManager.DEBUG("(setDownloadListener) URL :  " + downUrl);
                    LogManager.DEBUG("(setDownloadListener) FileName is " + fileName);
                    LogManager.DEBUG(mimetype);


                    _downUrl = downUrl;
                    _fileName = fileName;
                    _mimeType = mimetype;
                    String filePath = getApplicationContext().getExternalFilesDir("ica").getPath();
                    // 기존 ica 파일 삭제 //
//                    removeFile(STRPATH, _fileName);
                    removeFile(filePath, _fileName);
                    String cookie = android.webkit.CookieManager.getInstance().getCookie(downUrl.toString());

                    DownloadManager.Request request = new DownloadManager.Request(downUrl);
                    //LogManager.DEBUG("Cookie : " + cookie);

                    request.addRequestHeader("cookie", cookie);
                    //request.addRequestHeader("User-Agent", mWebView.getSettings().getUserAgentString());
                    if (Build.VERSION.SDK_INT > 10) {
                        request.allowScanningByMediaScanner(); // Over API level 11
                        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);   // DownloadManager icon 안나오게하기위해 주석..
                    }
                    request.setVisibleInDownloadsUi(false);
                    request.setTitle(fileName);
                    request.setDescription(url);
                    request.setMimeType(mimetype);

                    File desFile = new File(filePath, _fileName);
                    request.setDestinationUri(Uri.fromFile(desFile));

                    //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

                    DownloadManager downManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    downManager.enqueue(request);

                    //_isDownloading = true;    // 계속 접속가능하게 하기 위해.... by 2018-02-23

                }
            }
        });
        //endregion -- Download Listener


        /*
        try {
            Thread.sleep(1000);
            LogManager.DEBUG("init : " + _url + CPCProperty.getHost(7));
            mWebView.loadUrl(_url + CPCProperty.getHost(7));    //mWebView.loadUrl("https://m-ucloud.lgcns.com:8001/vmcube/login/appinit");
            //mWebView.loadUrl("file:///android_res/raw/javascript.htm");
        }catch(Exception ex){}*/

    }

    // 메뉴 초기 설정.
    private void InitView(){

        // Right Side 메뉴 생성
        mNav = new SimpleSideDrawer(this);
        mNav.setRightBehindContentView(R.layout.activity_behind_right);

        // Side 메뉴 숨기기 버튼
        ImageButton imgCloseBehindBtn = (ImageButton) findViewById(R.id.btn_hide_slider);
        imgCloseBehindBtn.setBackgroundColor(Color.TRANSPARENT);
        imgCloseBehindBtn.setOnClickListener(this);


        // TitleBar Backgroud color 변경
        //ImageView imgViewTitle = (ImageView)findViewById(R.id.imageTitleView);
        //imgViewTitle.setBackgroundColor(Color.rgb(58, 63, 66));

        // TitleBar Settings Button
        ImageButton imgSideBtn = (ImageButton) findViewById(R.id.btn_show_sidebar);
        imgSideBtn.setBackgroundColor(Color.TRANSPARENT);
        imgSideBtn.setOnClickListener(this);

        /*
        // 비밀번호 변경 버튼 이벤트
        ImageButton imgSideChangePwd = (ImageButton)findViewById(R.id.side_menu_change_passwd);
        imgSideChangePwd.setOnClickListener(this);

        // 패키지 설치 확인
        ImageButton imgSideCheckApps = (ImageButton)findViewById(R.id.side_menu_check_apps);
        imgSideCheckApps.setOnClickListener(this);
        */

        // 오픈소스 라이센스 버튼 이벤트
        ImageButton imgOpenlicense = (ImageButton) findViewById(R.id.side_menu_openlicense);
        imgOpenlicense.setOnClickListener(this);

        // 종료 버튼 이벤트
        ImageButton imgExit = (ImageButton) findViewById(R.id.side_menu_exit);
        imgExit.setOnClickListener(this);

        // 로고 이벤트
        ImageButton imgLogo = (ImageButton) findViewById(R.id.imageLogo);
        imgLogo.setOnClickListener(this);



        // 단말등록 버튼 이벤트
        //ImageButton imgDeviceRegister = (ImageButton) findViewById(R.id.side_menu_device_register);
        //imgDeviceRegister.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showDialog(APP_CLOSE);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        LogManager.DEBUG("onDestroy() : Clear application data");
        clearAppData();
    }


    // TMW App 실행.
    private void tmwStartScreenCaptureProtect2() {

        LogManager.DEBUG("startScreenCaptureProtect2()");

        try {
            ComponentName compName = new ComponentName("com.teruten.mw.screencheck", "com.teruten.mw.screencheck.activity.MainActivity");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(compName);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception ex) {
            LogManager.ERROR(ex.toString());
        }
    }

    /// 화면 돌릴때 리프레시 방지한다.
    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
*/
    @Override
    protected Dialog onCreateDialog(int id) {
        //return super.onCreateDialog(id);
        AlertDialog dlg = null;

        switch (id) {
            case APP_CLOSE:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_APP_CLOSE))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWebView.loadUrl(_url + CPCProperty.getHost(4));
                                finish();
                                //processKill();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {

                            }
                        })
                        .create();
                break;
            case MSG_AUTH_FAILED:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage("인증정보가 올바르지 않습니다. 다시 이용해주십시오.")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processKill();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {

                            }
                        })
                        .create();
                break;

            case APP_IF_ERROR:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_APP_IF_ERROR))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                                //processKill();
                            }
                        })
                        .create();
                break;

            case MSG_VMC_04_CALL_SUCCESS :
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_DISK_LOCK_REQ_SUCCESS))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNav.closeRightSide();
                                //finish();
                            }
                        })
                        .create();
                break;

            case MSG_VMC_04_CALL_FAILED :
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_DISK_LOCK_REQ_FAILED))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNav.closeRightSide();
                                //finish();
                            }
                        })
                        .create();
                break;

            case MSG_DISK_LOCK_EXECUTE :

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_DISk_LOCK_EXECUTE))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new WebAsyncTask().execute("OI_VMC_04");
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {

                            }
                        })
                        .create();

                break;

            case MSG_DEVICE_REGSTER_NEED_LOGIN :
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALTER_DIALOG_NEED_LOGIN))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mNav.closeRightSide();
                                //finish();
                            }
                        })
                        .create();
                break;

            default:
                dlg = null;
        }

        return dlg;
    }

    @Override
    public void onClick(View v) {

        Intent itt;

        LogManager.DEBUG("ids : " + v.getId());

        switch (v.getId()) {
            case R.id.btn_show_sidebar:    // 슬라이이드바 메뉴 보이기
                mNav.toggleRightDrawer();
                break;

            case R.id.btn_hide_slider: // 슬라이드바 메뉴 숨기기
                mNav.closeRightSide();
                break;

            /*
            case R.id.side_menu_change_passwd : // 슬라이드바-비밀번호변경
                mHandler.sendEmptyMessage(MSG_CHAGE_PASSWORD);
                mNav.closeRightSide();
                break;

            case R.id.side_menu_check_apps :    // 슬라이드바 - 필수설치앱 확인
                mHandler.sendEmptyMessage(MSG_CHECK_APPS);
                mNav.closeRightSide();
                break;
            */
            case R.id.side_menu_openlicense:   // 슬라이드바 - 오픈소스 라이센스
                mNav.closeRightSide();

                _popupUrl = _url + CPCProperty.getHost(3);

                LogManager.DEBUG(_popupUrl);
                mHandler.sendEmptyMessage(MSG_WEB_POPUP);

                break;

            case R.id.side_menu_exit:  // 슬라이드바 - 닫기버튼
                mHandler.sendEmptyMessage(MSG_EXIT);
                mNav.closeRightSide();
                break;

            case R.id.imageLogo:   // LOGO
                //mHandler.sendEmptyMessage(MSG_HOME);
                LogManager.DEBUG("Click Home!");

                LogManager.DEBUG(mWebView.getUrl());
                mHandler.sendEmptyMessage(MSG_SET_INIT);

                //mNav.closeRightSide();
                break;

            case R.id.side_menu_diskLock:    // Cloud Disk Lock 실행.
                LogManager.DEBUG("DISK LOCK");
                mHandler.sendEmptyMessage(MSG_DISK_LOCK_EXECUTE);
                break;

            /*
            case R.id.side_menu_device_register:    // 단말등록 이벤트
                LogManager.DEBUG("Click 단말등록 메뉴");
                mNav.closeRightSide();

                mHandler.sendEmptyMessage(MSG_DEVICE_REGSTER);
                break;*/
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 앱중단시 리시버 등록 해제
        //unregisterReceiver(completeReceiver);

        LogManager.DEBUG("onPause()");

        CookieSyncManager.getInstance().stopSync();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LogManager.DEBUG("onStart()");

        CookieSyncManager.createInstance(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        LogManager.DEBUG("onResume()");
        CookieSyncManager.getInstance().startSync();
    }

    // Physical Back, Menu 버튼 이벤트
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MENU:
                if (mNav.isClosed())
                    mNav.toggleRightDrawer();
                return true;
            case KeyEvent.KEYCODE_BACK:
                showDialog(APP_CLOSE);
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_web_view, menu); // physical 메뉴 숨기기

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //LogManager.DEBUG("SETTINGS 클릭");
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }


    //region ==== Download BroadcastReceiver ====
    // 다운로드 완료 후 이벤트 //
    private BroadcastReceiver completeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Resources res = context.getResources();
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            LogManager.DEBUG("(BroadcastReceiver)Download Complete");
            String filePath = getApplicationContext().getExternalFilesDir("ica").getPath();
            try {
                File icaFile = new File(filePath, _fileName);
                Uri uri = Uri.fromFile(icaFile);
                LogManager.DEBUG("(BroadcastReceiver : " + uri);
                Intent rIntent = new Intent(Intent.ACTION_VIEW);
                rIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri data = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", icaFile);
                rIntent.setDataAndType(data, _mimeType);
                startActivity(rIntent);

                //finish();

                //unregisterReceiver(completeReceiver);
            } catch (Exception ex) {
                LogManager.ERROR(ex.toString());
            }
        }
    };

//    private void saveFileUsingMediaStore(context: Context, url: String, fileName: String) {
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//            put(MediaStore.MediaColumns.MIME_TYPE, your_mime_type)
//            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//        }
//        val resolver = context.contentResolver
//        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
//        if (uri != null) {
//            URL(url).openStream().use { input ->
//                    resolver.openOutputStream(uri).use { output ->
//                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
//            }
//            }
//        }
//    }
    //endregion

    // 기존 ica 파일을 삭제한다.
    private void removeFile(String p_path, String p_fileName) {
        File nFile = new File(p_path, p_fileName);
        if (nFile.exists() && nFile.isFile()) {
            Boolean aaa = nFile.canRead();
            Boolean bbb = nFile.canWrite();
            Boolean isDeleted = nFile.delete();
            LogManager.DEBUG("isDeleted"+isDeleted);
        }
    }

    /// 프로세스 종료 ////
    public void processKill() {
        //finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // Application Data 삭제
    public void clearAppData() {
        try {
            File cache = getCacheDir();
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib") && !s.equals("databases")) {
                        deleteDir(new File(appDir, s));
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }

        return dir.delete();
    }

    //-- 1차 인증키 신규 생성 및 반환.
    private void OI_VMC_01() {

        LogManager.DEBUG("=========== START OI_VMC_01 ===========");

        String uri = CPCProperty.getInterfaceUrl(1);

        // phone number가 없는 경우 deviceid 를 사용한다.
        try {
            if (UserInfo.USER_PHONE != null) {
                LogManager.DEBUG("Yes Phone : " + UserInfo.USER_PHONE + " / Key : " + KEY);
                UserInfo.USER_CTN = URLEncoder.encode(Cipher.encrypt(UserInfo.USER_PHONE.replace("-", ""), KEY, KEY.length * 8, null), "UTF-8");
            } else {
                LogManager.DEBUG("No Phone : " + UserInfo.USER_DEVICE_ID + " / Key : " + KEY);
                UserInfo.USER_CTN = URLEncoder.encode(Cipher.encrypt(UserInfo.USER_DEVICE_ID, KEY, KEY.length * 8, null), "UTF-8");
            }
            _hashedSn = URLEncoder.encode(Cipher.encrypt(UserInfo.USER_DEVICE_ID, KEY, KEY.length * 8, null), "UTF-8");

            LogManager.DEBUG("(1)Send OI_VMC_01 : " + uri);
            LogManager.DEBUG("(1)CUST_CD : " + UserInfo.CUST_CD);
            LogManager.DEBUG("(1)HASHED_SN : " + _hashedSn);
            LogManager.DEBUG("(1)CTN : " + UserInfo.USER_CTN);

            JSONObject jsonPostObj = new JSONObject();
            jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);
            jsonPostObj.put("HASHED_SN", _hashedSn);
            jsonPostObj.put("CTN", UserInfo.USER_CTN);

            _webResponse = WebDataRequest.HTTPRequestJsonPost(uri, jsonPostObj);

            LogManager.DEBUG("(OI_VMC_01) Response : " + _webResponse);

            JSONObject jsonObject = new JSONObject(_webResponse);

            RET_OI_VMC_01.STATUS = jsonObject.getString("STATUS");
            RET_OI_VMC_01.AUTH_TOKEN = jsonObject.getString("AUTH_TOKEN");
        } catch (Exception e) {
            LogManager.ERROR(e.toString());
        }

        LogManager.DEBUG("=========== END OI_VMC_01 ===========");
    }

    //-- 2차 인증키 등록 및 결과 반환.
    private void OI_VMC_02() {

        //LogManager.DEBUG("=========== START OI_VMC_02 ===========");
        try {
            if (!RET_OI_VMC_01.AUTH_TOKEN.isEmpty() && RET_OI_VMC_01.AUTH_TOKEN != null) {


                _authToken = URLEncoder.encode(Cipher.encrypt(RET_OI_VMC_02.AUTH_TOKEN, KEY, KEY.length * 8, null), "UTF-8");

                JSONObject jsonPostObj = new JSONObject();
                jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);
                jsonPostObj.put("HASHED_SN", _hashedSn);
                jsonPostObj.put("AUTH_TOKEN", _authToken);

                //LogManager.DEBUG("(2)Send OI_VMC_02");
                //LogManager.DEBUG("(2)CUST_CD : " + UserInfo.CUST_CD);
                //LogManager.DEBUG("(2)HASHED_SN : " + _hashedSn);
                //LogManager.DEBUG("(2)AUTH_TOKEN : " + _authToken);

                //_webResponse = WebDataRequest.HTTPRequestJsonPost("https://ext-ucloud.lgcns.com:8006/vmCubeInf/OIService.svc/OI_VMC_02", jsonPostObj);
                _webResponse = WebDataRequest.HTTPRequestJsonPost(CPCProperty.getInterfaceUrl(2), jsonPostObj);

                //LogManager.DEBUG("OI_VMC_02() Result : " + _webResponse);

                JSONObject jsonObject = new JSONObject(_webResponse);

                RET_OI_VMC_02.STATUS = jsonObject.getString("STATUS");
                RET_OI_VMC_02.TRANSACTION_DT = jsonObject.getString("TRANSACTION_DT");
                RET_OI_VMC_02.TRANSACTION_SEQ = jsonObject.getString("TRANSACTION_SEQ");
            } else {

            }
        } catch (Exception ex) {
            //LogManager.ERROR("OI_VMC_02() " + ex.toString());
        }

        //LogManager.DEBUG("=========== END OI_VMC_02 ===========");
    }

    //-- Cloud Disk 잠금 설정/해지 요청.
    private void OI_VMC_04() {

        LogManager.DEBUG("=========== START OI_VMC_04 ===========");

        LogManager.DEBUG("URL :" + CPCProperty.getInterfaceUrl(4));

        LogManager.DEBUG("Token : " + RET_OI_VMC_01.AUTH_TOKEN);
        try {
            if (!RET_OI_VMC_01.AUTH_TOKEN.isEmpty() && RET_OI_VMC_01.AUTH_TOKEN != null) {

                LogManager.DEBUG("Request to URL :" + CPCProperty.getInterfaceUrl(4));
                //_authToken = URLEncoder.encode(Cipher.encrypt(RET_OI_VMC_02.AUTH_TOKEN, KEY, KEY.length * 8, null), "UTF-8");

                JSONObject jsonPostObj = new JSONObject();
                jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);
                jsonPostObj.put("HASHED_SN", _hashedSn);
                //jsonPostObj.put("AUTH_TOKEN", _authToken);
                jsonPostObj.put("AUTH_TOKEN", RET_OI_VMC_01.AUTH_TOKEN);
                jsonPostObj.put("USER_ID", SystemProperty.USER_ID);

                if(SystemProperty.IS_DISK_LOCK_STATE)
                    jsonPostObj.put("IS_LOCK", "N");
                else
                    jsonPostObj.put("IS_LOCK", "Y");

                LogManager.DEBUG("(4)Send OI_VMC_04");
                LogManager.DEBUG("(4)USER_ID : " + SystemProperty.USER_ID);
                LogManager.DEBUG("(4)IS_LOCK : " + !SystemProperty.IS_DISK_LOCK_STATE);
                //LogManager.DEBUG("(4)AUTH_TOKEN : " + _authToken);

                //_webResponse = WebDataRequest.HTTPRequestJsonPost("https://ext-ucloud.lgcns.com:8006/vmCubeInf/OIService.svc/OI_VMC_02", jsonPostObj);
                _webResponse = WebDataRequest.HTTPRequestJsonPost(CPCProperty.getInterfaceUrl(4), jsonPostObj);

                LogManager.DEBUG("OI_VMC_04() Result : " + _webResponse);

                JSONObject jsonObject = new JSONObject(_webResponse);

                RET_OI_VMC_04.STATUS = jsonObject.getString("STATUS");
                RET_OI_VMC_04.STATUS_DESC = ""; //jsonObject.getString("STATUS_DESC");

                if(RET_OI_VMC_04.STATUS.equals("C") || RET_OI_VMC_04.STATUS.equals("Y")) {
                    SystemProperty.IS_DISK_LOCK_STATE = !SystemProperty.IS_DISK_LOCK_STATE;

                    mHandler.sendEmptyMessage(MSG_VMC_04_CALL_SUCCESS);
                }else{
                    mHandler.sendEmptyMessage(MSG_VMC_04_CALL_FAILED);
                }

            } else {
                LogManager.ERROR("Token is empty : " + CPCProperty.getInterfaceUrl(4));
            }
        } catch (Exception ex) {
            LogManager.ERROR("OI_VMC_04() " + ex.toString());
        }

        LogManager.DEBUG("=========== END OI_VMC_04 ===========");
    }


    // WebView Override methods
    public class MyWebViewClient extends WebViewClient {
        boolean timeout;

        public MyWebViewClient() {
            timeout = true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            LogManager.DEBUG("onPageStarted() : "+ url);

            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(timeout){
                        Log.d(TAG,"===== TimeOut ======");
                    }
                }
            }).start();*/
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //timeout = false;
            LogManager.DEBUG("onPageFinished() : " + url);

            try {
                if (url.contains("appinit")) {
                    LogManager.DEBUG("Here is onPageFinished - APPINIT");


                } else if (url.contains("/login/")) {
                    LogManager.DEBUG("Here is onPageFinished - LOGIN - " + _loginStep);
                    if (_loginStep < 2) // OI_VMC_01 --> /login/{DeviceID}/{Token} --> fnResult()호출.
                        mWebView.loadUrl("javascript:fnResult();");

                }else if(url.contains("setclient")){
                    LogManager.DEBUG("setclient - " + _url + CPCProperty.getHost(7));
                    //https://mucloud.lguplus.co.kr/vmCubeMobile/login/appinit
                    mWebView.loadUrl(_url + CPCProperty.getHost(7));

                } else {
                }


            } catch (Exception ex) {
                //LogManager.DEBUG("[ERROR]onPageFinished() : " + ex.toString());
            }

            CookieSyncManager.getInstance().sync();
        }

        // WebView 에러 들을 처리.
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            LogManager.ERROR("onReceivedError() : " + description + "Failed URL : " + failingUrl);

            //mWebView.loadUrl("file:///android_res/raw/page_error.htm");

            // vmware 호출 //
            if (failingUrl.startsWith("vmware-view://")) {
                Intent itt = new Intent(Intent.ACTION_VIEW);
                itt.setData(Uri.parse(failingUrl));
                startActivity(itt);
                finish();
            } else {
                mWebView.loadUrl("file:///android_res/raw/page_error.htm");
            }
        }

        /// 프로세스 종료 ////
        public void processKill() {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


    ///================================================================================================
    /// WEB Interface 처리
    ///================================================================================================

    //region ==== WEB Interface ====
    ///  WEB To App 통신 정의
    private class AndroidBridge {
        @JavascriptInterface
        public void setMessage(final String arg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //LogManager.DEBUG("From WEB : " + arg.toString());
                }
            });
        }

        // (Web to App) Alert Message 띄움.
        @JavascriptInterface
        public void setCallAlert(final String arg) {
            //LogManager.DEBUG("From WEB : " + arg.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(WebViewActivity.this);
                    alert.setTitle(getResources().getString(R.string.dialog_settitle_notice)); // [알림]
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage(arg.toString());
                    alert.show();
                }
            });
        }

        // (Web to App) 필수 설치앱 체크 요청 (Web to App)
        @JavascriptInterface
        public void setCallAppInstall(final String arg) {
            //LogManager.DEBUG("From WEB : " + arg.toString());
            StringTokenizer tokens = new StringTokenizer(arg, "|");
            String pkgName = "";
            _isPkgList = "";
            boolean isCheck = false;
            for (int i = 1; tokens.hasMoreElements(); i++) {
                pkgName = tokens.nextToken();
                if (!pkgName.isEmpty()) {
                    isCheck = utils.isPakcakgeInstalled(WebViewActivity.this, pkgName);
                    //LogManager.DEBUG(pkgName + "->" + isCheck);
                    _isPkgList += isCheck + "|";
                }
            }
            _isPkgList = _isPkgList.substring(0, _isPkgList.length() - 1);
            mHandler.sendEmptyMessage(MSG_PKGINSTALL_RES);
        }

        // (Web to App) App에서 Web으로 fnResult()를 호출 후 바로 setOI_VMC_02() 호출됨.
        @JavascriptInterface
        public void setOI_VMC_02(final String arg) {
            //URLEncoder.encode(Cipher.encrypt(_phoneNumber, KEY, KEY.length * 8, null), "UTF-8");
            String res = null;
            LogManager.DEBUG("From WEB(setOI_VMC_02) : " + arg.toString());

            try {

                res = URLDecoder.decode(Cipher.decrypt(arg, KEY, KEY.length * 8, null), "UTF-8");
                // res : p0WqoRv_GoJVW8iF11aUSrL7BhB2OR1zaPPvUQRiRNHzDQDSdei4qXh3gOHT9IES,2

                if (res.indexOf(",") > -1) {
                    String[] resArray = res.split(",");
                    //LogManager.DEBUG("New Token : " + resArray[0]);
                    //LogManager.DEBUG("New Result : " + resArray[1]);

                    if (resArray[1].equals("2")) {
                        RET_OI_VMC_02.AUTH_TOKEN = resArray[0].toString();
                        _token2 = resArray[0].toString();
                        mHandler.sendEmptyMessage(MSG_AUTH_SUCCESS);
                    } else
                        mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
                } else {
                    //LogManager.DEBUG("[ERROR]incorrect data");
                    mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
                }
            } catch (Exception ex) {
                //LogManager.ERROR(ex.toString());
                mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
            }
        }

        // (Web to App) Progress Dialog 보여주기 / 숨기기
        @JavascriptInterface
        public void CallProgressDialog(final String arg, final String state) {
            //LogManager.DEBUG("From WEb : " + arg + " / Status : " + state);
            _webMessage = arg;

            if (state.equals("true"))
                mHandler.sendEmptyMessage(MSG_PDIALOG_SHOW);
            else if (state.equals("false"))
                mHandler.sendEmptyMessage(MSG_PDIALOG_HIDE);
        }

        // (Web to App) 팝업 창 띄우기(WebView  창)
        @JavascriptInterface
        public void CallWebPopup(final String url) {

            if (url.isEmpty() || url == null) {
                LogManager.DEBUG("CallWebPopup() URL is empty " );
                return;
            }

            _popupUrl = url.trim();

            if(_popupUrl.trim().length() > 3 && !_popupUrl.substring(0,4).equals("http")){
                _popupUrl = _url + _popupUrl;
            }

            LogManager.DEBUG("CallWebPopup() URL : " + _popupUrl);

            mHandler.sendEmptyMessage(MSG_WEB_POPUP);
        }


        // (Web to App) web에서 init 페이지 완료 후 이 Method 를 호출해줌.
        @JavascriptInterface
        public void CallInitComplete() {
            LogManager.DEBUG("CallInitComplete() " + _isNextInf);
            //if(_isOiVmc01 == false) {
            if (!_isNextInf) {
                LogManager.DEBUG("CallInitComplete() 1차인증요청 from Web");
                new WebAsyncTask().execute("OI_VMC_01"); // 1차 인증키 받기
            }
            //}
        }

        // (Web to App) 초기화 - 1차인증부터 시작
        @JavascriptInterface
        public void setInit() {
            LogManager.DEBUG("setInit() from web : 초기화 진행");
            mHandler.sendEmptyMessage(MSG_SET_INIT);
        }

        // (Web to App) 자가조치
        @JavascriptInterface
        public void CallDesktopRepair(final String msg, final String step) {
            //LogManager.DEBUG("CallAlertConfirm() from web : " + msg + " / step : " + step);
            if (msg.isEmpty() || msg == null) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(WebViewActivity.this);
                    alert.setTitle(getResources().getString(R.string.dialog_settitle_notice)); // [알림]
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWebView.loadUrl("javascript:fnDesktopRepair('" + step + "')");
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alert.setMessage(msg.toString());
                    alert.show();
                }
            });

        }

        // (Web to App) 마켓이동.
        @JavascriptInterface
        public void CallMarket(final String url) {
            //LogManager.DEBUG("CallMarket() URL :" + url);
            if (url.isEmpty() || url == null) {
                return;
            }

            try {
                Intent intt = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + url));
                startActivity(intt);
            } catch (Exception ex) {
                //LogManager.ERROR(ex.toString());
            }
        }

        // (Web to App) 크롬브라우저로 새창 띄우기
        @JavascriptInterface
        public void CallNewBrowser(final String url){


            if(url.isEmpty() || url == null)
                return;

            LogManager.DEBUG("CallNewBrowser() Popup Url : " + url);

            try {
                Intent chromItt = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //chromItt.setPackage("com.android.chrome");
                startActivity(chromItt);
            } catch (Exception ex){
                LogManager.ERROR(ex.toString());
            }

        }

        // (Web to App) 로그인 사용자 아이디와 잠금디스크 개수 가져오기.
        @JavascriptInterface
        public void CallDiskLockState(final String userId, final String auditDiskCnt, final String auditState){

            LogManager.DEBUG("CallDiskLockState()");

            if(userId.isEmpty() || userId == null)
                return;

            LogManager.DEBUG("CallDiskLockState() param - " + userId + " / " + auditDiskCnt + " / " + auditState);

            try {
                // 로그인 사용자 아이디
                SystemProperty.USER_ID = userId.trim();

                // 잠금 디스크 소유자 여부.
                if(auditDiskCnt.isEmpty() || auditDiskCnt == null || auditDiskCnt.equals("0"))
                    SystemProperty.IS_DISK_LOCK_OWNER = false;
                else
                    SystemProperty.IS_DISK_LOCK_OWNER = true;

                // 디스크 잠금 현재 상태.
                if(auditState.isEmpty() || auditState == null || auditState.toUpperCase().equals("N"))
                    SystemProperty.IS_DISK_LOCK_STATE = false;
                else
                    SystemProperty.IS_DISK_LOCK_STATE = true;

            } catch (Exception ex){
                LogManager.ERROR(ex.toString());
            }

            LogManager.DEBUG("CallDiskLockState() - Is disk lock user? " + SystemProperty.IS_DISK_LOCK_OWNER);
            LogManager.DEBUG("CallDiskLockState() - Disk Lock State : " + SystemProperty.IS_DISK_LOCK_STATE);

            mHandler.sendEmptyMessage(MSG_SLIDE_MENU_REFRESH);
        }

        // (Web to App) 웹에서 로그인 완료되면 호출. - by 2017-06-07
        // 로그인이 완료되면 단말인증 메뉴 추가.
        @JavascriptInterface
        public void setLoginComplete(){
            LogManager.DEBUG("setLoginComplete() from web");

            SystemProperty.IS_LOGIN = true;
        }

        // (Web to App) 로그인 아이디 호출.
        @JavascriptInterface
        public void SaveLoginId(final String userId){
            LogManager.DEBUG("SaveLoginId() : " + userId);

            //if(userId.isEmpty() || userId == null)
            //    return;

            saveLoginId(userId);
        }

        @JavascriptInterface
        public void GetLoginId(){
            LogManager.DEBUG("GetLoginId() return : " + SystemProperty.PORTAL_LOGIN_ID);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogManager.DEBUG("Call to web : " + SystemProperty.PORTAL_LOGIN_ID);
                    mWebView.loadUrl("javascript:setLoginId('"+SystemProperty.PORTAL_LOGIN_ID+"');");
                }
            });
        }

    }

    //endregion


    //region ==== WEB Interface SDK 17이하 버전 ====
    ///  WEB To App 통신 정의
    private class AndroidBridgeBelow17 {
        //@JavascriptInterface
        public void setMessage(final String arg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //LogManager.DEBUG("From WEB17 : " + arg.toString());
                }
            });
        }

        // (Web to App) Alert Message 띄움.
        //@JavascriptInterface
        public void setCallAlert(final String arg) {
            //LogManager.DEBUG("From WEB17 : " + arg.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(WebViewActivity.this);
                    alert.setTitle(getResources().getString(R.string.dialog_settitle_notice)); // [알림]
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage(arg.toString());
                    alert.show();
                }
            });
        }

        // (Web to App) 필수 설치앱 체크 요청 (Web to App)
        //@JavascriptInterface
        public void setCallAppInstall(final String arg) {
            //LogManager.DEBUG("From WEB : " + arg.toString());
            StringTokenizer tokens = new StringTokenizer(arg, "|");
            String pkgName = "";
            _isPkgList = "";
            boolean isCheck = false;
            for (int i = 1; tokens.hasMoreElements(); i++) {
                pkgName = tokens.nextToken();
                if (!pkgName.isEmpty()) {
                    isCheck = utils.isPakcakgeInstalled(WebViewActivity.this, pkgName);
                    //LogManager.DEBUG(pkgName + "->" + isCheck);
                    _isPkgList += isCheck + "|";
                }
            }
            _isPkgList = _isPkgList.substring(0, _isPkgList.length() - 1);
            mHandler.sendEmptyMessage(MSG_PKGINSTALL_RES);
        }

        // (Web to App) App에서 Web으로 fnResult()를 호출 후 바로 setOI_VMC_02() 호출됨.
        //@JavascriptInterface
        public void setOI_VMC_02(final String arg) {
            //URLEncoder.encode(Cipher.encrypt(_phoneNumber, KEY, KEY.length * 8, null), "UTF-8");
            String res = null;
            //LogManager.DEBUG("From WEB(setOI_VMC_02) : " + arg.toString());

            try {

                res = URLDecoder.decode(Cipher.decrypt(arg, KEY, KEY.length * 8, null), "UTF-8");
                // res : p0WqoRv_GoJVW8iF11aUSrL7BhB2OR1zaPPvUQRiRNHzDQDSdei4qXh3gOHT9IES,2

                if (res.indexOf(",") > -1) {
                    String[] resArray = res.split(",");
                    //LogManager.DEBUG("New Token : " + resArray[0]);
                    //LogManager.DEBUG("New Result : " + resArray[1]);

                    if (resArray[1].equals("2")) {
                        RET_OI_VMC_02.AUTH_TOKEN = resArray[0].toString();
                        _token2 = resArray[0].toString();
                        mHandler.sendEmptyMessage(MSG_AUTH_SUCCESS);
                    } else
                        mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
                } else {
                    //LogManager.DEBUG("[ERROR]incorrect data");
                    mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
                }
            } catch (Exception ex) {
                //LogManager.ERROR(ex.toString());
                mHandler.sendEmptyMessage(MSG_AUTH_FAILED);
            }


        }

        // (Web to App) Progress Dialog 보여주기 / 숨기기
        //@JavascriptInterface
        public void CallProgressDialog(final String arg, final String state) {
            //LogManager.DEBUG("From WEb : " + arg + " / Status : " + state);
            _webMessage = arg;

            if (state.equals("true"))
                mHandler.sendEmptyMessage(MSG_PDIALOG_SHOW);
            else if (state.equals("false"))
                mHandler.sendEmptyMessage(MSG_PDIALOG_HIDE);
        }

        // (Web to App) 팝업 창 띄우기(WebView  창)
        //@JavascriptInterface
        public void CallWebPopup(final String url) {
            if (url.isEmpty() || url == null) {
                return;
            }

            _popupUrl = url.trim();

            //LogManager.DEBUG("CallWebPopup() URL : " + url);

            mHandler.sendEmptyMessage(MSG_WEB_POPUP);
        }

        // (Web to App) web에서 init 페이지 완료 후 이 Method 를 호출해줌.
        //@JavascriptInterface
        public void CallInitComplete() {
            //LogManager.DEBUG("CallInitComplete() " + _isNextInf);
            //if(_isOiVmc01 == false) {
            if (!_isNextInf) {
                //LogManager.DEBUG("CallInitComplete() 1차인증요청 from Web");
                new WebAsyncTask().execute("OI_VMC_01"); // 1차 인증키 받기
            }
            //}
        }

        // (Web to App) 초기화 - 1차인증부터 시작
        //@JavascriptInterface
        public void setInit() {
            //LogManager.DEBUG("setInit() from web : 초기화 진행");
            mHandler.sendEmptyMessage(MSG_SET_INIT);
        }

        // (Web to App) 자가조치
        //@JavascriptInterface
        public void CallDesktopRepair(final String msg, final String step) {
            //LogManager.DEBUG("CallAlertConfirm() from web : " + msg + " / step : " + step);
            if (msg.isEmpty() || msg == null) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(WebViewActivity.this);
                    alert.setTitle(getResources().getString(R.string.dialog_settitle_notice)); // [알림]
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWebView.loadUrl("javascript:fnDesktopRepair('" + step + "')");
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alert.setMessage(msg.toString());
                    alert.show();
                }
            });

        }

        // (Web to App) 마켓이동.
        //@JavascriptInterface
        public void CallMarket(final String url) {
            //LogManager.DEBUG("CallMarket() URL :" + url);
            if (url.isEmpty() || url == null) {
                return;
            }

            try {
                Intent intt = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + url));
                startActivity(intt);
            } catch (Exception ex) {
                //LogManager.ERROR(ex.toString());
            }
        }


        /*
        // 크롬 브라우저로 해당 URL 실행.
        private void PopupNewBrowser(String pUri){

            LogManager.DEBUG("HERE" + pUri);
            if(pUri.isEmpty())
                return;

            //Intent chromItt = new Intent(Intent.ACTION_VIEW, Uri.parse(pUri));
            //chromItt.setPackage("com.android.chrome");    // Just 크롬만 지정할 경우.
            //startActivity(chromItt);

            Intent webItt = new Intent(Intent.ACTION_VIEW);
            webItt.setData(Uri.parse("http://www.daum.net"));
            webItt.setAction(Intent.ACTION_VIEW);
            webItt.addCategory(Intent.CATEGORY_BROWSABLE);
            getApplicationContext().startActivity(webItt);
        }*/

        // (Web to App) 크롬브라우저로 새창 띄우기
        public void CallNewBrowser(final String url){


            if(url.isEmpty() || url == null)
                return;

            LogManager.DEBUG("CallNewBrowser() Popup Url : " + url);

            try {
                Intent chromItt = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //chromItt.setPackage("com.android.chrome");
                startActivity(chromItt);
            } catch (Exception ex){
                LogManager.ERROR(ex.toString());
            }

        }

        // (Web to App) 로그인 사용자 아이디와 잠금디스크 개수 가져오기.
        public void CallDiskLockState(final String userId, final String auditDiskCnt, final String auditState){

            LogManager.DEBUG("CallDiskLockState()");

            if(userId.isEmpty() || userId == null)
                return;

            LogManager.DEBUG("CallDiskLockState() param - " + userId + " / " + auditDiskCnt + " / " + auditState);

            try {
                // 로그인 사용자 아이디
                SystemProperty.USER_ID = userId.trim();

                // 잠금 디스크 소유자 여부.
                if(auditDiskCnt.isEmpty() || auditDiskCnt == null || auditDiskCnt.equals("0"))
                    SystemProperty.IS_DISK_LOCK_OWNER = false;
                else
                    SystemProperty.IS_DISK_LOCK_OWNER = true;

                // 디스크 잠금 현재 상태.
                if(auditState.isEmpty() || auditState == null || auditState.toUpperCase().equals("N"))
                    SystemProperty.IS_DISK_LOCK_STATE = false;
                else
                    SystemProperty.IS_DISK_LOCK_STATE = true;

            } catch (Exception ex){
                LogManager.ERROR(ex.toString());
            }

            LogManager.DEBUG("CallDiskLockState() - Is disk lock user? " + SystemProperty.IS_DISK_LOCK_OWNER);
            LogManager.DEBUG("CallDiskLockState() - Disk Lock State : " + SystemProperty.IS_DISK_LOCK_STATE);

            mHandler.sendEmptyMessage(MSG_SLIDE_MENU_REFRESH);
        }
    }

    //endregion


    private class WebAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {

                if (params.length > 0 && params[0].toString().equals("OI_VMC_01")) {
                    LogManager.DEBUG("doInBackground(OI_VMC_01)");

                    if (!_isNextInf) {  // true 인경우 1차 인증을 못태운다.
                        _isNextInf = true;
                        OI_VMC_01();
                    }


                    Thread.sleep(500);
                    mHandler.sendEmptyMessage(MSG_URL_MAIN_1);  // /login/ 1차 페이지 GET 호출

                } else if (params.length > 0 && params[0].toString().equals("OI_VMC_02")) {
                    // 2차 인증 절차...
                    //LogManager.DEBUG("doInBackground(OI_VMC_02)");

                    OI_VMC_02();
                    _isNextInf = false; // 2차 끝나면 1차 가능하게 해줌.

                    Thread.sleep(500);
                    //mHandler.sendEmptyMessage(MSG_AUTH_END);
                    mHandler.sendEmptyMessage(MSG_URL_MAIN_2);

                } else if (params.length > 0 && params[0].toString().equals("OI_VMC_04")) {
                // Cloud Disk 잠금 설정/해지 요청
                LogManager.DEBUG("doInBackground(OI_VMC_04)");

                OI_VMC_04();

                Thread.sleep(500);
                //mHandler.sendEmptyMessage(MSG_AUTH_END);
                //mHandler.sendEmptyMessage(MSG_URL_MAIN_2);
            }

            } catch (Exception ex) {
                //LogManager.DEBUG("[ERROR] " + ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }
    }


    private void saveLoginId(String pUserId){
        if(sqLiteManager == null)
            sqLiteManager = new SQLiteManager(this, null, null, 1);
        sqLiteManager.setLoginData(pUserId);
    }
}
