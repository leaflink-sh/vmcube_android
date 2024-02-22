package common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by njoy on 2018. 1. 16..
 */

public class SQLiteManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "VMCUBE";
    private static final String LOGIN_TABLE_NAME = "TBL_LOGIN";
    private static final int DB_VERSION = 1;

    public SQLiteManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //super(context, name, factory, version);
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createLoginTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean createLoginTable(SQLiteDatabase db){
        try{
            StringBuffer sb = new StringBuffer();
            sb.append("CREATE TABLE " + LOGIN_TABLE_NAME + " (");
            sb.append("SEQ_NO INTEGER, ");
            sb.append("USER_ID TEXT");
            sb.append(")");

            db.execSQL(sb.toString());

            LogManager.DEBUG("Success to create table");

            return true;
        }
        catch (Exception ex){
            LogManager.ERROR(ex.toString());

            throw ex;
        }
    }

    public void getDB(){
        SQLiteDatabase db = getReadableDatabase();
    }

    // 로그인 사용자 계정 가져오기.
    public String getLoginData(){
        String loginId = "";

        SQLiteDatabase db = getReadableDatabase();

        try{
            StringBuffer sb = new StringBuffer();
            sb.append("select USER_ID from " + LOGIN_TABLE_NAME + " where SEQ_NO=1");

            Cursor cursor = db.rawQuery(sb.toString(), null);

            LogManager.DEBUG("Cursor count : " + cursor.getCount());

            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                loginId = cursor.getString(0);
            }else{
                LogManager.DEBUG("Table에 데이터가 없습니다.");
            }

            //cursor.close();
        }
        catch (Exception ex){
            LogManager.ERROR("getLoginData() : " + ex.toString());
        }
        finally {
            //if(db.isOpen())
            //    db.close();
        }

        LogManager.DEBUG("getLoginData() login id : " + loginId);

        return loginId;
    }

    public boolean isExistLoginInfo(){
        boolean isExist = false;

        SQLiteDatabase db = getReadableDatabase();

        try{
            StringBuffer sb = new StringBuffer();
            sb.append("select USER_ID from " + LOGIN_TABLE_NAME + " where SEQ_NO=1");

            Cursor cursor = db.rawQuery(sb.toString(), null);

            LogManager.DEBUG("Cursor count : " + cursor.getCount());

            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                isExist = true;
            }else{
                //LogManager.DEBUG("Table에 데이터가 없습니다.");
                isExist = false;
            }

            //cursor.close();
        }
        catch (Exception ex){
            LogManager.ERROR("isExistLoginInfo() : " + ex.toString());
        }
        finally {
            //if(db.isOpen())
            //    db.close();
        }

        LogManager.DEBUG("isExistLoginInfo() : " + isExist);

        return isExist;
    }

    // 로그인 아이디 저장
    public void setLoginData(String pUserId){

        LogManager.DEBUG("setLoginData()");


        SQLiteDatabase db = getWritableDatabase();

        try{
            //if(getLoginData() == ""){
            if(!isExistLoginInfo()){
                db.execSQL("INSERT INTO " + LOGIN_TABLE_NAME + " VALUES (1, '" + pUserId + "');");
                LogManager.DEBUG("INSERT --> USER ID.." + pUserId);
            }else if(getLoginData() != pUserId){
                db.execSQL("UPDATE " + LOGIN_TABLE_NAME + " SET USER_ID = '" + pUserId + "' where SEQ_NO=1");
                LogManager.DEBUG("Update --> USER ID.."+ pUserId);
            }else{
                LogManager.DEBUG("이미 저장되어 있는 USER ID..");
            }
        }
        catch (Exception ex){
            LogManager.ERROR("setLoginData() : " + ex.toString());
        }
        finally {
            if(db.isOpen())
                db.close();
        }
    }
}
