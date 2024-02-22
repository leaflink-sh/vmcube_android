package common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import kr.co.lguplus.mucloud.CPCProperty;

/**
 * Created by njoy on 2015-04-02.
 */
public class Utils {

    Context context;

    public Utils(Context context){
        this.context = context;
    }

    // 검사할 패키지 리스트르 받아와서 검사.
    public String getInstalledPkgList(){
        String retValue = null;

        String[] pkgList = new String[] { "com.citrix.Receiver" };

        for(int i=0;i<pkgList.length;i++){
            LogManager.DEBUG("필수 패키지명 확인 중 : " + pkgList[i]);
            if(isPakcakgeInstalled(context, pkgList[i])) {
                retValue += "TRUE,";
            }else{
                retValue += "FALSE,";
            }
        }

        retValue = retValue.substring(0,retValue.length()-1);

        return retValue;
    }

    public boolean isPakcakgeInstalled(Context context, String pkgName){
        PackageManager pm = context.getPackageManager();
        try{
            pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
        }
        catch(Exception ex){
            return false;
        }

        return true;
    }

    // 루팅 여부 체크 //
    public boolean isRootingCheck()
    {
        boolean found = false;

        if(!found)
        {
            String[] place = {
                    "/sbin/","/system/bin/","/system/xbin","/data/local/xbin/","/data/local/bin/",
                    "/system/sd/xbin/","/system/bin/failsafe/","/data/local/"
            };

            for(String where : place){
                if(new File( where + "su").exists()){
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    // Hash Encrypt
    public String encryptHash(String str){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(md != null){
            md.update(str.getBytes());
            byte byteData[] = md.digest();
            String base64 = Base64.encodeToString(byteData, Base64.DEFAULT);

            return base64;
        }

        return str;
    }

    // 앱 캐시 삭제
    public void clearApplicationCache(Context context, File file){
        File dir= null;

        if(file == null){
            dir = context.getCacheDir();
        }else{
            dir = file;
        }

        if(dir == null)
            return;

        File[] children = dir.listFiles();
        try{
            for(int i=0;i<children.length;i++){
                if(children[i].isDirectory())
                    clearApplicationCache(context, children[i]);
                else
                    children[i].delete();
            }
        }
        catch(Exception ex){}

    }

    // 설치 패키지의 버전코드를 리턴..
    public int getAppVersionCode(Context context, String packageName){
        try{
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            return pi.versionCode;

        }catch(Exception ex){
            return 0;
        }
    }

    // 로컬 경로의 apk 의 버전코드 정보를 가져온다
    public int getAppVersionCodeFromFile(Context context, String fullFilePath){
        try{
            final PackageManager pm = context.getPackageManager();

            File fl = new File(fullFilePath);
            if(!fl.exists())
                return 0;
            else{
                PackageInfo info = pm.getPackageArchiveInfo(fullFilePath, 0);
                LogManager.DEBUG("Downloaded file version code : " + info.versionCode);

                return info.versionCode;
            }
        }
        catch (Exception ex){
            return 0;
        }
    }

    // App 의 Hash Value
    public String getAppHash(Context pContext){
        Context context = pContext; //getApplicationContext();
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String cert = null;

        try {
            PackageInfo packageInfo = packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature certSignature =  packageInfo.signatures[0];
            MessageDigest msgDigest = MessageDigest.getInstance("SHA1");
            msgDigest.update(certSignature.toByteArray());
            cert = Base64.encodeToString(msgDigest.digest(), Base64.DEFAULT);

            LogManager.DEBUG("Hash Code : " + cert);

        } catch (PackageManager.NameNotFoundException e) {
            LogManager.ERROR(e.toString());
        } catch (NoSuchAlgorithmException e) {
            LogManager.ERROR(e.toString());
        }

        return cert;
    }

    // Package Name 의 App 실행.
    public void openOtherApp(Context context, String packageName){

        LogManager.DEBUG("OpenOtherApp() : " + packageName);
        PackageManager pm = context.getPackageManager();

        try{
            Intent itt = pm.getLaunchIntentForPackage(packageName);
            itt.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(itt);
        }
        catch (Exception ex){
            throw ex;
        }
    }

    // 실행중인 프로세스 리스트
    public void showProcessList(Context context){
        LogManager.DEBUG("showProcessList()");

        ActivityManager am = (ActivityManager)context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for(int i=0;i<appList.size();i++){
            ActivityManager.RunningAppProcessInfo rap = appList.get(i);
            LogManager.DEBUG("PROCESS : " + rap.processName);
        }
    }

    // 실행중인 서비스 리스트
    public void showServiceList(Context context){
        LogManager.DEBUG("showServiceList()");

        ActivityManager am = (ActivityManager)context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);

        for(int i=0;i<rs.size();i++){
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            LogManager.DEBUG("SERVICE : " + rsi.service.getPackageName());
        }
    }


}
