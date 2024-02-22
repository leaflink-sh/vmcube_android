package dto;

/**
 * Created by njoy on 2015-07-03.
 */
public class UserInfo {
    public static String CUST_CD;
    public static String USER_PHONE;
    public static String USER_DEVICE_ID;
    public static String USER_CTN;

    public static String getCUST_CD() {
        return CUST_CD;
    }

    public static void setCUST_CD(String CUST_CD) {
        UserInfo.CUST_CD = CUST_CD;
    }

    public static String getUSER_PHONE() {
        return USER_PHONE;
    }

    public static void setUSER_PHONE(String USER_PHONE) {
        UserInfo.USER_PHONE = USER_PHONE;
    }

    public static String getUSER_DEVICE_ID() {
        return USER_DEVICE_ID;
    }

    public static void setUSER_DEVICE_ID(String USER_DEVICE_ID) {
        UserInfo.USER_DEVICE_ID = USER_DEVICE_ID;
    }

    public static String getUSER_CTN() {
        return USER_CTN;
    }

    public static void setUSER_CTN(String USER_CTN) {
        UserInfo.USER_CTN = USER_CTN;
    }
}
