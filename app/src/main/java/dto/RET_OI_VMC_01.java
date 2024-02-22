package dto;

/**
 * Created by njoy on 2015-07-03.
 */
public class RET_OI_VMC_01 {
    public static String STATUS;
    public static String TRANSACTION_DT;
    public static String TRANSACTION_SEQ;
    public static String AUTH_TOKEN;

    public static String getAUTH_TOKEN() {
        return AUTH_TOKEN;
    }

    public static void setAUTH_TOKEN(String AUTH_TOKEN) {
        RET_OI_VMC_01.AUTH_TOKEN = AUTH_TOKEN;
    }

    public static String getSTATUS() {
        return STATUS;
    }

    public static void setSTATUS(String STATUS) {
        RET_OI_VMC_01.STATUS = STATUS;
    }

    public static String getTRANSACTION_DT() {
        return TRANSACTION_DT;
    }

    public static void setTRANSACTION_DT(String TRANSACTION_DT) {
        RET_OI_VMC_01.TRANSACTION_DT = TRANSACTION_DT;
    }

    public static String getTRANSACTION_SEQ() {
        return TRANSACTION_SEQ;
    }

    public static void setTRANSACTION_SEQ(String TRANSACTION_SEQ) {
        RET_OI_VMC_01.TRANSACTION_SEQ = TRANSACTION_SEQ;
    }



}
