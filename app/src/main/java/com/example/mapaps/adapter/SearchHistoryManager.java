package com.example.mapaps.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;

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
        cursor.moveToFirst();
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

    //添加记录（Tip）
    public void addRecord(String name,String detail,String dis,Double lat,Double lon){
        if(!isExist("name",name)){
            liteDatabase=searchHistorySQLiteOpenHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            values.put("name",name);
            values.put("detail",detail);
            values.put("dis",dis);
            values.put("lat",lat);
            values.put("lon",lon);
            Log.e("test2","name；"+name+"\nlon:"+lon+" lat:"+lat);
            liteDatabase.replace(tableName,null,values);
            liteDatabase.close();
        }
    }

    //删除全部记录
    public void deleteAllRecords(){
        liteDatabase=searchHistorySQLiteOpenHelper.getWritableDatabase();
        liteDatabase.execSQL("delete from "+tableName);
        liteDatabase.close();
    }

    //获取指定元素全部记录
    public List<String> getAllStringRecorsByKey(@NonNull String key){
        List<String> stringList=new ArrayList<>();

        liteDatabase=searchHistorySQLiteOpenHelper.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);
        cursor.moveToFirst();
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
        @SuppressLint("Recycle") Cursor cursor=liteDatabase.query(tableName,null,null,null,null,null,null);
        cursor.moveToFirst();
        while (cursor.moveToNext()){
            Double name = cursor.getDouble(cursor.getColumnIndexOrThrow(key));
            stringList.add(name);
        }

        liteDatabase.close();
        return stringList;
    }

    //获取指定元素全部记录
    public List<Tip> getAllTipsRecords(){
        //todo 怀疑返回list错误
        List<Tip> tipList= new ArrayList<>();
        List<String> detail_list=getAllStringRecorsByKey("detail");
        List<String> name_list=getAllStringRecorsByKey("name");
        List<String> dis_list=getAllStringRecorsByKey("dis");
        List<Double> lon_list=getAllDoubleRecorsByKey("lon");
        List<Double> lat_list=getAllDoubleRecorsByKey("lat");

        for(int i=0;i<name_list.size();i++){
            Tip tip=new Tip();
            tip.setName(name_list.get(i));
            tip.setAddress(detail_list.get(i));
            tip.setDistrict(dis_list.get(i));
            tip.setPostion(new LatLonPoint(lat_list.get(i),lon_list.get(i)));
            //Log.e("test2", "地点：" + tip.getName() + "\n地名：" + tip.getAddress() + "\n坐标：（" + tip.getPoint().getLongitude() + "," + tip.getPoint().getLatitude() + "）");
            tipList.add(tip);
        }

        return tipList;
    }

}
