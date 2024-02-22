package kr.co.lguplus.mucloud;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import com.gun0912.tedpermission.BuildConfig;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import common.LogManager;
import common.SystemProperty;
import common.Utils;
import connection.WebDataRequest;
import dto.RET_OI_VMC_03;
import dto.RET_OI_VMC_05;
import dto.UserInfo;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

public class MainActivity extends Activity {

    //region ==== FINAL VALIABLES ====
    private final int TIME_OUT                  = 1001;
    private final int ROOTING_Y                 = 1002;
    private final int ROOTING_N                 = 1003;
    private final int GOTO_WEBVIEW              = 1004;
    private final int RECEIVER_PKG_NO           = 1005;
    private final int RECOMMEND_PKG_INSTALL_YES = 1006; // 필수 패키지 설치가 된 경우
    private final int RECOMMEND_PKG_INSTALL_NO  = 1007; // 필수 패키지 설치가 안된 경우
    private final int MSG_MDM_NO                = 1008;
    private final int MSG_MDM_ERROR             = 1009; // 앱인증
    private final int MSG_MDM_INIT_NO           = 1010; // 무결성 체크
    private final int MSG_IF_ERROR              = 1011;  // Interface 호출 에러.
    private final int MSG_UPDATE_LAUNCHER       = 1012;   // App Update...
    private final int INSTALL_PACKAGE_RESULT    = 1013;
    private final int MSG_NEED_INSTALL_MDM      = 1014;     // MDM 설치 필요..
    private final int MSG_TMW_ACCESSBILITY_NEED = 1015;     // TMW 의 접근성 권한 필요..
    private final int MSG_TMW_VERION_RESULT     = 1016;     // TMW 의 최신 패지지 정보를 받아온다..
    private final int MSG_NEED_UPGRADE_MDM      = 1017;     // TMW 의 업그레이드가 존재하는 경우...
    private final int MSG_NEED_INSTALL_AHNLAB   = 1018;     // AhnLab 설치해야 하는 경우..
    private final int MSG_NEED_INSTALL_INTUNE   = 1019;     // MS Intune 회사 포털 설치해야 하는 경우..
    private final int MSG_NEED_INTUNE_AUTH   = 1020;     // MS Intune 인증해야 하는 경우..
    //private final String KEY_PASS =  CPCProperty.getKey();
    //private final byte[] KEY = KEY_PASS.getBytes();

    //endregion

    //region ==== VALIABLES =====

    private boolean _isRooting = false;
    private boolean _isMdmInstalled = false;
    private boolean _isAhnlabInstalled = false;
    private boolean _isINTUNEInstalled = false;
    private boolean _isIntuneAuth = false;
    private boolean _isInstallPkg = false;
    private boolean _isSkipMdmUpgrade = false;
    private SharedPreferences _pref;
    private boolean _isShortcut = false;
    private String _webResponse = null;
    private ProgressDialog mDialog;
    private final String _url = CPCProperty.getUrl();
    //private boolean _isWebReqSuccess = false;
    private boolean _isVMC03 = false;
    private boolean _isVMC05 = false;
    private boolean _isUpdateInfo = false;  // 최신버전 업데이트 정보가 있는지...
    private boolean _isCallGoWeb = false;   // WebView 한번 호출..

    private ProgressDialog mDownloadDialog; // Download Progress
    private String _apkDownloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    private String _apkFileName = "CloudPC.apk";
    private byte[] KEY = null;
    private String KEY_PASS = null;

    final Utils utils = new Utils(this);

    private RET_OI_VMC_05 MdmInfo = null;
    private String _mdmInstallUrl = "https://ext-iucloud.lguplus.co.kr/vmCubeMobile/apk/tmw/index.htm";

    final int PERMISSION_REQUEST_COARSE_LOCATION = 100001;
    //endregion
    //start MSAL
    private IMultipleAccountPublicClientApplication mMultipleAccountApp;
    private List<IAccount> mAccountList;
    private IAccount mAccount;
    private final String[] mScopes = new String[] { "User.Read" };
    //end MSAL

    //region === Handler Events ===

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == TIME_OUT) {
                // mProgressDialog.dismiss();
            } else if (msg.what == ROOTING_Y) {
                showDialog(ROOTING_Y);
            } else if (msg.what == GOTO_WEBVIEW) {

                if (!_isCallGoWeb){
                    _isCallGoWeb = true;
                    Intent itt = new Intent(getApplicationContext(), WebViewActivity.class);
                    String homeUrl = _url; // + CPCProperty.getHost(6); //"https://m-ucloud.lgcns.com:8001/vmcube/login";
                    LogManager.DEBUG("Main URL A: " + homeUrl);
                    itt.putExtra("PAGE_URL", homeUrl);
                    startActivity(itt);
                    finish();
                }

            } else if (msg.what == ROOTING_N) {
                //mProgressDialog.dismiss();
            } else if (msg.what == RECEIVER_PKG_NO) {   // Citrix Receiver 설치가 필요한 경우 사용
                showDialog(RECEIVER_PKG_NO);
            }
            /*
            else if(msg.what == RECOMMEND_PKG_INSTALL_YES){ // 필수 패키지가 설치 완료된 경우 Action
                Intent itt = new Intent(getApplicationContext(), WebViewActivity.class);
                itt.putExtra("PAGE_URL","https://m-ucloud.lgcns.com");
                startActivity(itt);
                finish();
            }*/
            else if (msg.what == RECOMMEND_PKG_INSTALL_NO) { // 필수 패키지가 설치가 안된 경우 Action
                Intent itt = new Intent(Intent.ACTION_VIEW);
                itt.setData(Uri.parse("market://details?id=com.citrix.Receiver"));
                startActivity(itt);
                finish();
            } else if (msg.what == MSG_MDM_NO) {

                showDialog(MSG_MDM_NO);
            } else if (msg.what == MSG_MDM_ERROR) {
                showDialog(MSG_MDM_ERROR);
            } else if (msg.what == MSG_MDM_INIT_NO) {
                showDialog(MSG_MDM_INIT_NO);
            }
            else if(msg.what == MSG_IF_ERROR){
                showDialog(MSG_IF_ERROR);
            }
            else if(msg.what == MSG_UPDATE_LAUNCHER){
                showDialog(MSG_UPDATE_LAUNCHER);
            }
            else if(msg.what == MSG_NEED_INSTALL_MDM){
                showDialog(MSG_NEED_INSTALL_MDM);
            }
            else if(msg.what == MSG_TMW_ACCESSBILITY_NEED){
                showDialog(MSG_TMW_ACCESSBILITY_NEED);
            }
            else if(msg.what == MSG_TMW_VERION_RESULT){
                _mdmMessage = getResources().getString(R.string.ALERT_DIALOG_MDM_IF_FAIL);
                showDialog(MSG_MDM_ERROR);
            }
            else if(msg.what == MSG_NEED_UPGRADE_MDM){
                showDialog(MSG_NEED_UPGRADE_MDM);
            }
            else if(msg.what == MSG_NEED_INSTALL_AHNLAB){
                showDialog(MSG_NEED_INSTALL_AHNLAB);
            }
            else if(msg.what == MSG_NEED_INSTALL_INTUNE){
                showDialog(MSG_NEED_INSTALL_INTUNE);
            }
            else if(msg.what == MSG_NEED_INTUNE_AUTH){
                showDialog(MSG_NEED_INTUNE_AUTH);
            }

        }
    };

    //endregion

    private void progressDialogShow(String pMessage) {
        mDialog = ProgressDialog.show(MainActivity.this, null, pMessage);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogManager.DEBUG("=========== START Main Application onCreate() =======================");
        LogManager.DEBUG("=========== Current package version : " + utils.getAppVersionCode(this, SystemProperty.PACAKGE_NAME));

        // Android OS Version Oreo 이상만 지원
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            showDialog(SystemProperty.ALERT_DIALOG_OS_NOTSUPPORT);
            return;
        }

        // 앱 권한 체크 및 요청..
        requestPermission("TED");

        //initialize MSAL
        processMSAL();

    }

    // Ted Permission 라이브러리
    private void requestPermission(String pType){

        if(pType.equals("TED")) {

            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    LogManager.DEBUG("앱 권한 승인");
                    StartApplication();
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    LogManager.ERROR("앱 권한 거부");
                    finish();
                }
            };

            TedPermission.with(this)
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage(R.string.msg_req_permission_grant)
                    .setDeniedMessage("[설정] > [앱 권한] 에서 권한을 허용해주세요.")
                    .setPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE})
                    .check();
        }
        else{

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                StartApplication();

            }else{

                LogManager.ERROR("Permission is denied.");

                ActivityCompat.requestPermissions(this
                        , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    private void StartApplication(){
        try{
            // 단말기 정보 가져오기
            if(Build.VERSION.SDK_INT < 28) {    // Android 9 미만일때...
                final HashMap<String, String> devHash = SystemProperty.getDeviceInfo(MainActivity.this);
                UserInfo.USER_DEVICE_ID = devHash.get("DEVICE_ID");
                UserInfo.USER_PHONE = devHash.get("PHONE_NUMBER");
                UserInfo.CUST_CD = CPCProperty.getCustCd();
            }else{
                UserInfo.USER_DEVICE_ID = UUID.randomUUID().toString();
                UserInfo.USER_PHONE = "010-0000-0000";
                UserInfo.CUST_CD = CPCProperty.getCustCd();
            }
        }
        catch (Exception ex){
            UserInfo.USER_DEVICE_ID = UUID.randomUUID().toString();
            UserInfo.USER_PHONE = "010-0000-0000";
            UserInfo.CUST_CD = CPCProperty.getCustCd();
        }

        LogManager.DEBUG("Device ID : " + UserInfo.USER_DEVICE_ID);
        LogManager.DEBUG("Phone no : " + UserInfo.USER_PHONE);

        // 전화번호를 뒤에서 8자리를 가져온다.
        if (UserInfo.USER_PHONE != null && UserInfo.USER_PHONE.length() > 7) {
            UserInfo.USER_PHONE = UserInfo.USER_PHONE.replace("-", "");
            UserInfo.USER_PHONE = UserInfo.USER_PHONE.substring(UserInfo.USER_PHONE.length() - 8, UserInfo.USER_PHONE.length());

            LogManager.DEBUG("Phone no : " + UserInfo.USER_PHONE);
        }

        // 기존에 파일이 존재하면 삭제.
        RmFile(_apkDownloadPath + "/" + _apkFileName);

        new MainAsyncTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LogManager.DEBUG("onResume()");

        new MainAsyncTask().execute();
    }

    //region === DIALOG ===
    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        if(mDialog.isShowing())
            mDialog.dismiss();

        AlertDialog dlg = null;

        switch (id) {
            // ??? OS 이하는 지원하지 않는다.
            case SystemProperty.ALERT_DIALOG_OS_NOTSUPPORT:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_OS_NOTSUPPORT))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processKill();
                            }
                        })
                        .create();
                break;

            // 루팅된 폰은 지원 안함..
            case ROOTING_Y:
                //LogManager.DEBUG("ROOTING AREA");
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_ROOTING))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processKill();
                            }
                        })
                        .create();
                break;

            // Citrix Receiver 설치하러 Go
            case RECEIVER_PKG_NO:
                //LogManager.DEBUG("Citrix Receiver install");
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_INSTALL_RECEIVER))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intt = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.citrix.Receiver"));
                                startActivity(intt);
                                processKill();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {
                                processKill();
                            }
                        })
                        .create();
                break;

            case MSG_MDM_NO:   // MDM이 설치가 안되었으니 설치하러 보냄.
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.mdm_no_installed))
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intt = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cloudstore.miraeasset.com/mobile/download.php"));
                                startActivity(intt);
                                //processKill();
                                finish();
                            }
                        })
                        .create();

                //finish();

                break;

            case MSG_MDM_ERROR:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(_mdmMessage)
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();

                break;

            case MSG_MDM_INIT_NO:
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(R.string.mdm_init_error)
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                                Intent intt = new Intent(Intent.ACTION_VIEW, Uri.parse("mobilekeeper://redirect?page=applist"));
                                startActivity(intt);
                                //processKill();
                                finish();
                            }
                        })
                        .create();


                break;

            case MSG_IF_ERROR :
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(R.string.ALERT_DIALOG_IF_ERROR)
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //processKill();
                                finish();
                            }
                        })
                        .create();
                break;

            case MSG_UPDATE_LAUNCHER :

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_NEW_PKG))
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DownloadFileAsync().execute();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {
                                mHandler.sendEmptyMessage(GOTO_WEBVIEW);
                            }
                        })
                        .create();
                break;

            case MSG_NEED_INSTALL_MDM : // 필수 MDM 설치하기.
                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_NEED_INSTALL_MDM))
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    Intent itt = new Intent(Intent.ACTION_VIEW, Uri.parse(_mdmInstallUrl));
                                    startActivity(itt);

                                    processKill();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {
                                processKill();
                            }
                        })
                        .create();
                break;

            case MSG_TMW_ACCESSBILITY_NEED :    // TMW의 접근성 권한 필요..

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.tmw_alert_access_title))
                        .setMessage(getResources().getString(R.string.tmw_alert_access_message))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    // 접근성 화면으로 이동...
                                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    //processKill();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {
                                dialog.dismiss();
                                processKill();
                            }
                        })
                        .create();
                break;

            case MSG_NEED_UPGRADE_MDM : // TMW의 신규 버전이 존재할 경우 설치할지...

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_settitle_notice)) // [알림]
                        .setMessage(getResources().getString(R.string.ALERT_DIALOG_UPGRADE_MDM_PKG))
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    Intent itt = new Intent(Intent.ACTION_VIEW, Uri.parse(_mdmInstallUrl));
                                    startActivity(itt);

                                    processKill();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int bt) {
                                _isSkipMdmUpgrade = true;
                                new MainAsyncTask().execute();
                            }
                        })
                        .create();
                break;

            case MSG_NEED_INSTALL_AHNLAB :

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.ahnlab_alert_title))
                        .setMessage(getResources().getString(R.string.ahnlab_alert_message))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    Intent itt = new Intent(Intent.ACTION_VIEW);
                                    itt.setData(Uri.parse("market://details?id=" + SystemProperty.AHNLAB_NAME));
                                    startActivity(itt);
                                    finish();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .create();



                break;

            case MSG_NEED_INSTALL_INTUNE :

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.intune_alert_message))
                        .setMessage(getResources().getString(R.string.intune_alert_message))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    Intent itt = new Intent(Intent.ACTION_VIEW);
                                    itt.setData(Uri.parse("market://details?id=" + SystemProperty.INTUNE_NAME));
                                    startActivity(itt);
                                    finish();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .create();



                break;
            case MSG_NEED_INTUNE_AUTH :

                dlg = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.msauth_alert_title))
                        .setMessage(getResources().getString(R.string.msauth_alert_message))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    // Microsoft Intune 회사포털앱 열기
//                                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.azure.authenticator");
                                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.microsoft.windowsintune.companyportal");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
//                                    finish();
                                }
                                catch (Exception ex){
                                    LogManager.ERROR(ex.toString());
                                }
                            }
                        })
                        .create();



                break;
            default:
                dlg = null;
        }

        return dlg;
    }

    //endregion


    // Get Token & Key
    private void OI_VMC_03() {

        LogManager.DEBUG("=========== START OI_VMC_03 ===========");

        try {

            JSONObject jsonPostObj = new JSONObject();
            jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);

            LogManager.DEBUG("OI_VMC_03 URL : " + CPCProperty.getInterfaceUrl(3));
            _webResponse = WebDataRequest.HTTPRequestJsonPost(CPCProperty.getInterfaceUrl(3), jsonPostObj);

            if(_webResponse == null || _webResponse.isEmpty()){
                _isVMC03 = false;
            }

            LogManager.DEBUG("OI_VMC_03() Result : " + _webResponse + " - " + _webResponse.length());

            JSONObject jsonObject = new JSONObject(_webResponse);

            RET_OI_VMC_03.SECURE_TOKEN = jsonObject.getString("SECURE_TOKEN");
            RET_OI_VMC_03.CRYPTO_KEY = jsonObject.getString("CRYPTO_KEY");

            LogManager.DEBUG(RET_OI_VMC_03.CRYPTO_KEY);

            _isVMC03 = true;

        } catch (Exception ex) {
            LogManager.ERROR("OI_VMC_03() : " + ex.toString());

            _isVMC03 = false;
        }
    }


    // Android 최신 버전 가져오가..
    private void OI_VMC_05(){

        LogManager.DEBUG("=========== START OI_VMC_05 ===========");
        String strUrl;

        try{
            KEY_PASS = RET_OI_VMC_03.CRYPTO_KEY;
            KEY = KEY_PASS.getBytes();
        }
        catch(Exception ex){
            LogManager.ERROR(ex.toString());
        }

        /*
        if(SystemProperty.IS_DEBUG){
            strUrl = "http://pengking.cafe24.com/apk/lgu/version.php";
        }else{
            strUrl = CPCProperty.getInterfaceUrl(5);
        }
        */
        strUrl = CPCProperty.getInterfaceUrl(5);

        try {

            JSONObject jsonPostObj = new JSONObject();
            jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);
            jsonPostObj.put("APP_NAME", SystemProperty.APP_NAME);
            //jsonPostObj.put("OS_TYPE", "ANDROID_TEST");
            jsonPostObj.put("OS_TYPE", "ANDROID");

            LogManager.DEBUG("OI_VMC_05 URL : " + strUrl);

            _webResponse = WebDataRequest.HTTPRequestJsonPost(strUrl, jsonPostObj);

            // 최신 버전이 없으니까 통과..
            if(_webResponse == null || _webResponse.isEmpty()){
                _isVMC05 = true;
                return;
            }

            LogManager.DEBUG("OI_VMC_05() Result : " + _webResponse + " - " + _webResponse.length());

            JSONObject jsonObject = new JSONObject(_webResponse);

            RET_OI_VMC_05.STATUS = jsonObject.getString("STATUS");

            if(RET_OI_VMC_05.STATUS.equals("C")){
                RET_OI_VMC_05.APP_NAME      = jsonObject.getString("APP_NAME");
                RET_OI_VMC_05.VERSION_CODE  = jsonObject.getString("VERSION_CODE");
                RET_OI_VMC_05.HASH_CODE     = jsonObject.getString("HASH_CODE");
                RET_OI_VMC_05.APP_URL       = jsonObject.getString("APP_URL");
                RET_OI_VMC_05.IS_SKIPPABLE  = jsonObject.getString("IS_SKIPPABLE");
            }
            else{
                LogManager.ERROR("OI_VMC_05 Interface STATUS result is 'F'");
                return;
            }

            _isVMC05 = true;

        }
        catch (Exception ex) {

            LogManager.ERROR("OI_VMC_05() : " + ex.toString());

            _isVMC05 = false;

            return;
        }

        LogManager.DEBUG("Current package version : " + utils.getAppVersionCode(this, SystemProperty.PACAKGE_NAME));


        String hashCode = utils.getAppHash(getApplicationContext());

        try{

            int newVersion = Integer.parseInt(RET_OI_VMC_05.VERSION_CODE.trim());

            if(utils.getAppVersionCode(this, SystemProperty.PACAKGE_NAME) < newVersion){
                LogManager.DEBUG("Is New Version ? Yes" + utils.getAppVersionCode(this, SystemProperty.PACAKGE_NAME) + " < " + newVersion);
                _isUpdateInfo = true;
            }else{
                LogManager.DEBUG("Is New Version ? No." + utils.getAppVersionCode(this, SystemProperty.PACAKGE_NAME) + " < " + newVersion);
                _isUpdateInfo = false;
            }
        }
        catch (Exception ex) {
            LogManager.ERROR(ex.toString());
        }
    }

    // MDM APK 정보 받아오기.
    private RET_OI_VMC_05 getMdmInfo(){

        LogManager.DEBUG("=========== Get MDM Information ===========");

        try{
            KEY_PASS = RET_OI_VMC_03.CRYPTO_KEY;
            KEY = KEY_PASS.getBytes();
        }
        catch(Exception ex){
            LogManager.ERROR(ex.toString());
        }

        if(MdmInfo == null)
            MdmInfo = new RET_OI_VMC_05();

        try {

            JSONObject jsonPostObj = new JSONObject();
            jsonPostObj.put("CUST_CD", UserInfo.CUST_CD);
            jsonPostObj.put("APP_NAME", SystemProperty.MDM_NAME);
            jsonPostObj.put("OS_TYPE", "ANDROID");

            LogManager.DEBUG("getMdmInfo() URL : " + CPCProperty.getInterfaceUrl(5));
            _webResponse = WebDataRequest.HTTPRequestJsonPost(CPCProperty.getInterfaceUrl(5), jsonPostObj);

            LogManager.DEBUG("getMdmInfo() Result : " + _webResponse + " - " + _webResponse.length());

            // 최신 버전이 없으니까 통과..
            if(_webResponse == null || _webResponse.isEmpty()){
                return null;
            }

            JSONObject jsonObject = new JSONObject(_webResponse);

            MdmInfo.STATUS = jsonObject.getString("STATUS");

            if(MdmInfo.STATUS.equals("C")){
                MdmInfo.APP_NAME      = jsonObject.getString("APP_NAME");
                MdmInfo.VERSION_CODE  = jsonObject.getString("VERSION_CODE");
                MdmInfo.HASH_CODE     = jsonObject.getString("HASH_CODE");
                MdmInfo.APP_URL       = jsonObject.getString("APP_URL");
                MdmInfo.IS_SKIPPABLE  = jsonObject.getString("IS_SKIPPABLE");
            }
            else{
                LogManager.ERROR("OI_VMC_05 Interface STATUS result is 'F'");
                return null;
            }

        }
        catch (Exception ex) {

            LogManager.ERROR("OI_VMC_05() : " + ex.toString());

            return null;
        }

        return MdmInfo;
    }

    /// 프로세스 종료 ////
    public void processKill() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /// 바탕화면에 ShortCut 생성
    private void addShortcut(Context context) {

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(context, getClass().getName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher));
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        sendBroadcast(intent);

        SharedPreferences.Editor editor = _pref.edit();
        editor.putBoolean("isShortcut", true);
        editor.commit();
    }

    private void ThreadSleep(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //-- Rooting Check
    private void CheckRooting() {
        if (utils.isRootingCheck()) {
            //LogManager.DEBUG("루팅 여부 : YES");
            _isRooting = true;
        } else {
            //LogManager.DEBUG("루팅 여부 : NO");
            _isRooting = false;
        }
    }

    //-- 필수설치 패키지 정보를 가져온다
    // return : TRUE,FALSE,TRUE
    private void GetInstalledPackages() {
        SystemProperty.GET_PACKAGE_LIST_INFO = utils.getInstalledPkgList();
    }

    // 특정 파일 지우기.
    private void RmFile(String fileFullName){

        LogManager.DEBUG("rm file : " + fileFullName);

        try{
            File fi = new File(fileFullName);
            if(fi.exists()){
                fi.delete();
            }
        }
        catch (Exception ex){
            LogManager.ERROR(ex.toString());
        }
    }

    // install downloaded apk
    private void InstallApk(){

        LogManager.DEBUG("InstallApk()");

        try{
            String fileFullPath = _apkDownloadPath + "/" + _apkFileName;
            File localFile = new File(fileFullPath);

            if(!localFile.exists()){
                LogManager.ERROR("File not exist : " + fileFullPath);
                new Exception("File not found(apk)");
            }
            else {
                Intent intent;

                LogManager.DEBUG("File path : " + fileFullPath);

                if(Build.VERSION.SDK_INT >= 24) {
                    Uri apkUri = FileProvider.getUriForFile(MainActivity.this,
                            BuildConfig.APPLICATION_ID + ".fileprovider", localFile);

                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                else{
                    intent  = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(localFile), "application/vnd.android.package-archive");
                }
                /* OLD Version
                Uri apkUri = Uri.fromFile(localFile);
                Intent apkIntent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(apkUri, "application/vnd.android.package-archive")
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(apkIntent);
                */

                this.startActivity(intent);

                finish();
            }
        }
        catch (Exception ex){
            LogManager.ERROR("설치 실패 : " + ex.toString());
        }
    }


    //region ==== AsyncTask AREA ====
    //
    private class MainAsyncTask extends AsyncTask<String, Void, Void> {

        // Backgroud 이전 작업
        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            LogManager.DEBUG("onPreExecute()");
            progressDialogShow("초기 접속 준비중입니다. 잠시만 기다려주세요.");
        }

        @Override
        protected Void doInBackground(String... params) {

            LogManager.DEBUG("doInBackground()");

            // Microsoft Authenticator installed??
            checkIntuneAuthApp();;

            // AhnLab installed??
            checkAhnLabApp();

            if(_isAhnlabInstalled){

                // 키값 받아오기
                OI_VMC_03();

                LogManager.DEBUG("New MDM is skip : " + _isSkipMdmUpgrade);

                if(!_isSkipMdmUpgrade)
                    startTmwProcess();

                LogManager.DEBUG("MDM 설치 여부 : " + _isMdmInstalled);


                ThreadSleep(1);

                if(_isVMC03) {

                    // 루팅 체크
                    CheckRooting();

                    ThreadSleep(1);

                    // Android 설치 버전 정보 가져오기
                    OI_VMC_05();
                }
            }

            LogManager.DEBUG("END doInBackground()");
            return null;
        }


        // Background 작업 이후
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            LogManager.DEBUG("onPostExecute() " + _TMWResult);

            if(!_isINTUNEInstalled) {
                mHandler.sendEmptyMessage(MSG_NEED_INSTALL_INTUNE);
            } else if (!_isIntuneAuth){
                mHandler.sendEmptyMessage(MSG_NEED_INTUNE_AUTH);
            } else {
                if (_isAhnlabInstalled) {
                    // MDM이 최신 버전이거나 MDM 설치 여부를 취소 눌러 스킵을 했거나..
                    if (_TMWResult == TMW_EXECUTE_SUCCESS || _isSkipMdmUpgrade) {

                        LogManager.DEBUG("MDM 1");
                        if (_isMdmInstalled) {
                            LogManager.DEBUG("MDM is installed. Start MDM Activity..");

                            // START MDM
                            tmwStartScreenCaptureProtect();

                            // 업데이트 정보가 존재하는지..
                            if (_isUpdateInfo) {
                                mHandler.sendEmptyMessage(MSG_UPDATE_LAUNCHER);
                                mDialog.dismiss();
                            } else {

                                if (_isVMC03) {
                                    mHandler.sendEmptyMessage(GOTO_WEBVIEW);
                                    mDialog.dismiss();
                                } else {
                                    mHandler.sendEmptyMessage(MSG_IF_ERROR);
                                    mDialog.dismiss();
                                }
                            }
                        } else {
                            mHandler.sendEmptyMessage(MSG_NEED_INSTALL_MDM);
                            mDialog.dismiss();
                        }

                    } else if (_TMWResult == TMW_NEED_UPGRADE) {
                        // MDM Upgrade 가 존재할 경우...
                        mHandler.sendEmptyMessage(MSG_NEED_UPGRADE_MDM);
                        mDialog.dismiss();

                    } else {
                        if (_TMWResult == TMW_NEED_INSTALL) {
                            mHandler.sendEmptyMessage(MSG_NEED_INSTALL_MDM);
                            mDialog.dismiss();
                        } else {
                            LogManager.ERROR("No Action");
                            finish();
                        }

                    }
                } else {
                    mHandler.sendEmptyMessage(MSG_NEED_INSTALL_AHNLAB);
                }
            }
          /*
            if(_isRooting) {  // 루팅된 폰이면....
                //LogManager.DEBUG("END : rooting");
                mHandler.sendEmptyMessageDelayed(ROOTING_Y, 1000);
                mDialog.dismiss();

                return;
            }*/
            /*else if(!_isInstallPkg) {
                Log.i("", "END : package");
                mHandler.sendEmptyMessage(RECOMMEND_PKG_INSTALL_NO);
                mDialog.dismiss();
                return;
            }
            */
            /*
            else {
                //LogManager.DEBUG("END : complete");
                mHandler.sendEmptyMessage(GOTO_WEBVIEW);
                mDialog.dismiss();
            }
            */

            if(mDialog.isShowing())
                mDialog.dismiss();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    //endregion -- end AsyncTask

    //region ==== AsyncTask Download ===

    class DownloadFileAsync extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            LogManager.DEBUG("Start Download Progress");

            mDownloadDialog = new ProgressDialog(MainActivity.this);
            mDownloadDialog.setMessage(getResources().getString(R.string.title_msg_update));
            mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDownloadDialog.setCancelable(false);
            mDownloadDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            int count = 0;
            byte data[] = new byte[1024];
            long total = 0;
            int progress = 0;

            LogManager.DEBUG("Start Download doInBackground()");

            try{
                URL url = new URL(RET_OI_VMC_05.APP_URL);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghOfFile = connection.getContentLength();
                LogManager.DEBUG("File Size : " + lenghOfFile + " / local download path : " + _apkDownloadPath);

                InputStream is = url.openStream();
                File downDir = new File(_apkDownloadPath);
                if(!downDir.exists()){
                    downDir.mkdir();
                }
                FileOutputStream fos = new FileOutputStream(downDir + "/" + _apkFileName);
                while((count = is.read(data)) != -1){
                    total += count;
                    int progress_temp = (int) total * 100 / lenghOfFile;
                    publishProgress("" + progress_temp);
                    if(progress_temp % 10 == 0 && progress != progress_temp){
                        progress = progress_temp;
                    }
                    fos.write(data,0,count);

                }
                is.close();
                fos.close();
            }
            catch(Exception ex){
                LogManager.ERROR(ex.toString());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //LogManager.DEBUG("Start Download onProgressUpdate() : " + values[0]);

            mDownloadDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LogManager.DEBUG("Start Download onPostExecute() ");

            mDownloadDialog.dismiss();

            // 다운로드 된 파일의 버전코드를 확인해서 현재 버전코드보다 높을 때만 인스톨..
            if(utils.getAppVersionCode(MainActivity.this, SystemProperty.PACAKGE_NAME) < utils.getAppVersionCodeFromFile(MainActivity.this, _apkDownloadPath + "/" + _apkFileName)) {
                InstallApk();
            }
            else {
                mHandler.sendEmptyMessage(GOTO_WEBVIEW);
            }
        }
    }

    //endregion  -- end Download AsyncTask


    private void checkAhnLabApp(){
        _isAhnlabInstalled = utils.isPakcakgeInstalled(MainActivity.this, SystemProperty.AHNLAB_NAME);
        LogManager.DEBUG(">>>>>> AhnLab 설치 여부 : " + _isAhnlabInstalled);
    }

    private void checkIntuneAuthApp(){
        _isINTUNEInstalled = utils.isPakcakgeInstalled(MainActivity.this, SystemProperty.INTUNE_NAME);
        LogManager.DEBUG(">>>>>> Microsoft Authenticator 설치 여부 : " + _isINTUNEInstalled);
    }

    private void processMSAL(){
        // 1. Multiple Account Client 생성하기
        PublicClientApplication.createMultipleAccountPublicClientApplication(this,
                R.raw.auth_config_multiple_account,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        mMultipleAccountApp = application;
                        // Entra ID login
                        onMultipleAccountLogin();
                    }
                    @Override
                    public void onError(MsalException exception) {
                        LogManager.ERROR(exception.getMessage());
                    }
                });
    }

    /**
     * Mutiple Account Login
     * 한 장비에서 여러 사용자가 사용하는 경우
     */

    private void onMultipleAccountLogin(){
        loadAccounts(true);
    }

    /**
     * AcquireToken (Multiple Accounts)
     */
    private void acquireMultipleAccountsToken(){
        if(mMultipleAccountApp == null) {
            return;
        }

        mMultipleAccountApp.acquireToken(this,
                mScopes,
                getAuthInteractiveCallback(true));
    }
    /**
     * 로그아웃
     */
    private void onLogout(){
        if(mAccount == null){
            return;
        }
        // Multiple Account의 경우에는 계정 캐쉬에서 해당 계정을 삭제
        try{
            if(mMultipleAccountApp != null){
                mMultipleAccountApp.removeAccount(mAccount, new IMultipleAccountPublicClientApplication.RemoveAccountCallback() {
                    @Override
                    public void onRemoved() {
                        mAccount = null;
                    }

                    @Override
                    public void onError(@NonNull MsalException exception) {
                        LogManager.ERROR(exception.getMessage());
                    }
                });
            }
        }catch(Exception ex){
            LogManager.ERROR(ex.getMessage());
        }
    }

    /**
     * 인증 (사용자 Action으로 인한 인증)
     * @return
     */
    private AuthenticationCallback getAuthInteractiveCallback(final boolean isMultipleAccounts){
        return new AuthenticationCallback() {
            @Override
            public void onCancel() {
                LogManager.DEBUG("MSAL User cancelled login.");
            }

            /**
             * 성공시
             * @param authenticationResult
             */
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                mAccount = authenticationResult.getAccount();
                LogManager.DEBUG(mAccount.getUsername());
            }

            @Override
            public void onError(MsalException exception) {
                LogManager.ERROR(exception.getMessage());
            }
        };
    }

    /**
     * Multiple Account 로드
     */
    private void loadAccounts(boolean isMultipleAccounts) {
        try {
            if (isMultipleAccounts) {
                if (mMultipleAccountApp == null) {
                    return;
                }
                // 계정 요청
                mMultipleAccountApp.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
                    @Override
                    public void onTaskCompleted(List<IAccount> result) {

                        // 계정 정보가 없는 경우에는 acquireToken 호출
                        if (result == null || result.isEmpty()) {
                            acquireMultipleAccountsToken();
                        } else {
                            mAccountList = result;
                            IAccount account = mAccountList.get(0);

                            mMultipleAccountApp.acquireTokenSilentAsync(
                                    mScopes,
                                    account,
                                    account.getAuthority(),
                                    new SilentAuthenticationCallback() {
                                        @Override
                                        public void onSuccess(IAuthenticationResult authenticationResult) {
                                            if(authenticationResult != null){
                                                mAccount = authenticationResult.getAccount();
                                                LogManager.DEBUG("LOGIN USER" + mAccount.getUsername());
//                                                _isIntuneAuth = Boolean.TRUE;
                                            }
                                        }

                                        @Override
                                        public void onError(MsalException exception) {
                                            LogManager.ERROR(exception.getMessage());
                                            if(exception instanceof MsalUiRequiredException) {
                                                acquireMultipleAccountsToken();
                                            }
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onError(MsalException exception) {
                        LogManager.ERROR(exception.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            LogManager.ERROR(ex.getMessage());
        }
    }
    /*
    Teruten MDM 사용 안함 by 2021-11-27
     */
    //region --- MDM Teruten ----

    private int _TMWResult = 0;
    private final int TMW_EXECUTE_SUCCESS   = 3000;     // MDM 실행 준비 완료...
    private final int TMW_NEED_INSTALL      = 3001;     // MDM 설치가 필요...
    private final int TMW_ACEESBILITY_NO    = 3002;     // 접근성 권한이 없는 경우..
    private final int TMW_NEED_UPGRADE      = 3003;     // MDM 업그레이드가 필요..

    // Terten MDM 알고리즘 적용 관련...
    private void startTmwProcess(){

        // (1) MDM 버전 정보 받아오기.
        RET_OI_VMC_05 mdmInfo = getMdmInfo();

        if(mdmInfo == null){
            LogManager.DEBUG("TMW - (1) Installed package..");
        }

        // (2) package 설치 여부 체크...
        _isMdmInstalled = utils.isPakcakgeInstalled(MainActivity.this, SystemProperty.MDM_NAME);

        if(_isMdmInstalled){
            try {
                _TMWResult = TMW_EXECUTE_SUCCESS;
                return;
            }
            catch (Exception ex){
                LogManager.ERROR(ex.toString());
            }

        }else{
            LogManager.DEBUG("TMW - (2) Not installed package..");
            _TMWResult = TMW_NEED_INSTALL;
            return;
        }
    }

    // 접근성 권한 여부 확인 후 다이얼로그 / 모듈시작...
    private void tmwAccessbilityCheck(Context context){

        try {

            if (!isTmwContainedInAccessbility(context)) {
                mHandler.sendEmptyMessage(MSG_TMW_ACCESSBILITY_NEED);
            } else {
                tmwStartScreenCaptureProtect();
                _TMWResult = TMW_EXECUTE_SUCCESS;
            }
        }catch (Exception ex){
            LogManager.ERROR("tmwAccessbilityCheck() : " + ex.toString());
        }
    }

    // TMW App 실행.
    private void tmwStartScreenCaptureProtect(){

        LogManager.DEBUG("startScreenCaptureProtect()");

        try{
            ComponentName compName = new ComponentName("com.teruten.mw.screencheck","com.teruten.mw.screencheck.activity.MainActivity");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(compName);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
        catch (Exception ex){
            LogManager.ERROR(ex.toString());
        }
    }

    // 화면 캡춰 방지 서비스 실행중 확인...
    private static boolean isTmwServiceRunning(String serviceName, Context context){
        LogManager.DEBUG("isServiceRunning()");
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
            LogManager.DEBUG("service.getClassName : " + runningServiceInfo.service.getClassName());
            if(serviceName.equals(runningServiceInfo.service.getClassName())){
                LogManager.DEBUG("화면 캡춰 방지 서비스 실행중입니다....");
                return true;
            }
        }

        return false;
    }

    // 접근성 권한 확인 - 접근성 권한을 획득한 패키지 리스트를 가져와서 검사...
    private boolean isTmwContainedInAccessbility(Context context){

        AccessibilityManager accessibilityManager = (AccessibilityManager)context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(Build.VERSION.SDK_INT > 14) {
            List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

            LogManager.DEBUG("isContainedInAccessbility() : "+  serviceList.size() + " / " + serviceList.toString()); //+ serviceList.toString().contains("com.teruten.mw.screencheck"));

            if(serviceList.size() < 1)
                return false;
            else
                return serviceList.toString().contains("com.teruten.mw.screencheck");

        }
        else{
            return true;
        }
    }



    private String _mdmMessage;

    //endregion

}
