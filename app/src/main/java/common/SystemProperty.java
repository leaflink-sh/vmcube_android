package common;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import java.util.HashMap;

/**
 * Created by njoy on 2015-04-02.
 */
public class SystemProperty {

    public final static boolean IS_DEBUG = true;
    public final static String DEBUG = "UCLOUD";
    public final static String PACAKGE_NAME = "kr.co.lguplus.mucloud";
    public final static String APP_NAME = "kr.co.lguplus.mucloud";  // OI_VMC_05 - APP_NAME
    public final static String MDM_NAME = "com.teruten.mw.screencheck";
    public final static String AHNLAB_NAME = "com.ahnlab.v3mobileplus";
    public final static String INTUNE_NAME = "com.microsoft.windowsintune.companyportal";

    // Cloud Disk 잠금 기능 사용..
    public static String USER_ID = null;    // 로그인 사용자 아이디
    public static boolean IS_DISK_LOCK_OWNER = false;   // Cloud Disk 잠금 디스크 사용자인지... LGU..
    public static boolean IS_DISK_LOCK_STATE = false;   // Cloud Disk 잠금 상태인지..

    // Handler / Dialog Messages
    public final static int ALERT_DIALOG_OS_NOTSUPPORT = 0;   // OS 진저 이하는 지원 안함.
    public final static int MSG_GET_PKG_INFO        = 1;   // 설치 패키지 정보 확인
    public final static int MSG_GET_PKG_INFO_EXIST  = 2;    // 필수 패키지가 설치된 상태
    public final static int MSG_GET_PKG_INFO_NOT_EXIST  = 3;    // 필수 패키지가 설치 안된 상태

    public static String GET_PACKAGE_LIST_INFO = null;

    public static String AR_INFO = null;    // ARIA

    public static String PORTAL_LOGIN_ID = "";

    // 웹 로그인 완료 여부
    public static  boolean IS_LOGIN = false;

    /// 단말기 정보 ///
    public static HashMap<String, String> getDeviceInfo(Context context){

        HashMap<String, String> mHash = new HashMap<String, String>();

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        mHash.put("DEVICE_ID", Build.SERIAL);
        mHash.put("SIM_OPERATION", tm.getSimOperatorName());
        mHash.put("MODEL_NAME", Build.MODEL);
        mHash.put("PHONE_NUMBER", tm.getLine1Number());

        return mHash;
    }

    // Display 해상도 정보 //
    public static HashMap<String, Integer> getDisplay(Context context){

        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        HashMap<String, Integer> mHash = new HashMap<String, Integer>();

        mHash.put("Width", display.getWidth());
        mHash.put("Height", display.getHeight());

        return mHash;
    }
}
