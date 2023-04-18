package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1; //<-- 版本
    private final static String _DBName = "marker.db";  //<-- db name
    String TableName = "lat_lng "; //<-- table name

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version ,String TableName) {
        super(context, name, factory, version);
        this.TableName = TableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQLTable = "CREATE TABLE IF NOT EXISTS " + TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Latlng TEXT, " +
                "Taste_good TEXT," +
                "Tittle TEXT," +
                "Snippet TEXT" +
                ");";
        db.execSQL(SQLTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + TableName;
        db.execSQL(SQL);
    }

    public ArrayList<String> getTables(){
        Cursor cursor = getWritableDatabase().rawQuery(
                "select DISTINCT tbl_name from sqlite_master", null);
        ArrayList<String> tables = new ArrayList<>();
        while (cursor.moveToNext()){
            String getTab = new String (cursor.getBlob(0));
            if (getTab.contains("android_metadata")){}
            else if (getTab.contains("sqlite_sequence")){}
            else tables.add(getTab.substring(0,getTab.length()-1));
        }
        return tables;
    }

    public void addData(String Latlng, String taste_good,String tittle,String snippet) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Latlng", Latlng);
        values.put("Taste_good", taste_good);
        values.put("Tittle", tittle);
        values.put("Snippet", snippet);
        db.insert(TableName, null, values);
    }

    public ArrayList<HashMap<String, String>> showAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + TableName, null);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        while (c.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();

            String id = c.getString(0);
            String Latlng = c.getString(1);
            String taste_good = c.getString(2);
            String tittle = c.getString(3);
            String snippet = c.getString(4);

            hashMap.put("id", id);
            hashMap.put("Latlng", Latlng);
            hashMap.put("Taste_good", taste_good);
            hashMap.put("Tittle", tittle);
            hashMap.put("Snippet", snippet);
            arrayList.add(hashMap);
        }
        return arrayList;
    }
    //以id刪除資料(簡單)
    public void deleteByIdEZ(String Latlng){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TableName, "Latlng=" + "'" + Latlng + "'",null);
    }

}
