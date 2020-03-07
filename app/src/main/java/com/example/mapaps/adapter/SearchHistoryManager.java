package com.example.mapaps.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class SearchHistoryManager {
    private SearchHistorySQLiteOpenHelper searchHistorySQLiteOpenHelper;
    private SQLiteDatabase liteDatabase;
    private String tableName;

    public SearchHistoryManager(Context context,@NonNull String name){
        searchHistorySQLiteOpenHelper=new SearchHistorySQLiteOpenHelper(context);
        this.tableName=name;
    }

    //判断记录是否存在(String)
    public boolean isExist(@NonNull String key,String record){
        boolean isExist_flag=false;

        liteDatabase=searchHistorySQLiteOpenHelper.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);

        while(cursor.moveToNext()){
            if(record.equals(cursor.getColumnIndexOrThrow(key))){
                isExist_flag=true;
                break;
            }
        }
        liteDatabase.close();

        return isExist_flag;
    }

    //判断记录是否存在(Double)
    public boolean isExist(@NonNull String key,Double record){
        boolean isExist_flag=false;

        liteDatabase=searchHistorySQLiteOpenHelper.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);

        while(cursor.moveToNext()){
            if(record==cursor.getColumnIndexOrThrow(key)){
                isExist_flag=true;
                break;
            }
        }
        liteDatabase.close();

        return isExist_flag;
    }
    //添加记录(String)
    public void addStringRecord(@NonNull String key,String record){
        liteDatabase=searchHistorySQLiteOpenHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(key,record);
        liteDatabase.insert(tableName,null,values);
        liteDatabase.close();
    }

    //添加记录(Double)
    public void addDoubleRecord(@NonNull String key,Double record){
        liteDatabase=searchHistorySQLiteOpenHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(key,record);
        liteDatabase.insert(tableName,null,values);
        liteDatabase.close();
    }

    //删除全部记录
    public void deleteAllRecords(){
        liteDatabase=searchHistorySQLiteOpenHelper.getWritableDatabase();
        liteDatabase.execSQL("delete from"+tableName);
        liteDatabase.close();
    }

    //获取指定元素全部记录
    public List<String> getAllStringRecorsByKey(@NonNull String key){
        List<String> stringList=new ArrayList<>();

        liteDatabase=searchHistorySQLiteOpenHelper.getReadableDatabase();
        Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(key));
            stringList.add(name);
        }

        liteDatabase.close();
        return stringList;
    }

    //获取指定元素全部记录
    public List<Double> getAllDoubleRecorsByKey(@NonNull String key){
        List<Double> stringList=new ArrayList<>();

        liteDatabase=searchHistorySQLiteOpenHelper.getReadableDatabase();
        Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);

        while (cursor.moveToNext()){
            Double name = cursor.getDouble(cursor.getColumnIndexOrThrow(key));
            stringList.add(name);
        }

        liteDatabase.close();
        return stringList;
    }
}
