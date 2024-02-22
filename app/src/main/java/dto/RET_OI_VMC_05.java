package dto;

/**
 * Created by njoy on 2016. 9. 8..
 */

public class RET_OI_VMC_05 {
    public static String STATUS;
    public static String STATUS_DESC;
    public static String APP_NAME;
    public static String VERSION_CODE;
    public static String HASH_CODE;
    public static String APP_URL;
    public static String IS_SKIPPABLE;

    public static String getSTATUS() {
        return STATUS;
    }

    public static void setSTATUS(String STATUS) {
        RET_OI_VMC_05.STATUS = STATUS;
    }

    public static String getStatusDesc() {
        return STATUS_DESC;
    }

    public static void setStatusDesc(String statusDesc) {
        STATUS_DESC = statusDesc;
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    public static String getVersionCode() {
        return VERSION_CODE;
    }

    public static void setVersionCode(String versionCode) {
        VERSION_CODE = versionCode;
    }

    public static String getHashCode() {
        return HASH_CODE;
    }

    public static void setHashCode(String hashCode) {
        HASH_CODE = hashCode;
    }

    public static String getAppUrl() {
        return APP_URL;
    }

    public static void setAppUrl(String appUrl) {
        APP_URL = appUrl;
    }

    public static String getIsSkippable() {
        return IS_SKIPPABLE;
    }

    public static void setIsSkippable(String isSkippable) {
        IS_SKIPPABLE = isSkippable;
    }
}
