package kr.co.lguplus.mucloud;

/**
 * Created by njoy on 2016. 8. 18..
 */
public class CPCProperty {


    static {
        System.loadLibrary("ucloudJNI");
    }

    static public native String getHost(int val);

    static public native String getUrl();

    static public native String getCustCd();

    static public native String getInterfaceUrl(int val);

    static public native String getKey();

}