package com.seeun.devsign;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by USER on 2017-08-24.
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE DEVICES (btnnum INTEGER PRIMARY KEY," +
                " address TEXT, name TEXT, type TEXT, image INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(int btnnum, String address, String name, String type, int image ){
        SQLiteDatabase db = getWritableDatabase(); //읽고 쓰기 가능하게 DB 오픈
        db.execSQL("INSERT INTO DEVICES VALUES("+btnnum+", '"+address+"', '"+name+"', '"+type+"', "+image+");");
        db.close();
    }

    public void update(int btnnum, String address, String name, String type, int image ){
        SQLiteDatabase db = getWritableDatabase(); //읽고 쓰기 가능하게 DB 오픈
        db.execSQL("UPDATE DEVICES SET address='"+address+"', name='"+name+"', " +
                "type='"+type+"', image="+image+" WHERE btnnum="+btnnum+";");
        db.close();
    }

    public void delete(int btnnum){
        SQLiteDatabase db = getWritableDatabase(); //읽고 쓰기 가능하게 DB 오픈
        db.execSQL("DELETE FROM DEVICES WHERE btnnum="+btnnum+";");
        db.close();
    }

    public String getResult(int btnnum) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM DEVICES WHERE btnnum="+btnnum+";", null);
        while (cursor.moveToNext()) {
            result = cursor.getString(2);
        }

        return result;
    }

    public String getAddress(int btnnum) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT address FROM DEVICES WHERE btnnum="+btnnum+";", null);
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
        }

        return result;
    }

    public int getImage(int btnnum){
        SQLiteDatabase db = getReadableDatabase();
        int result = -1;

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM DEVICES WHERE btnnum="+btnnum+";", null);
        while (cursor.moveToNext()) {
            result = cursor.getInt(4);
        }

        return result;
    }

}
