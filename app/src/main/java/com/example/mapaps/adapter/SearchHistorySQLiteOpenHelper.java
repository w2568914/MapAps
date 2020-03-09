package com.example.mapaps.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchHistorySQLiteOpenHelper extends SQLiteOpenHelper {
    //数据库基础信息
    private static String name = "search.db";
    private static Integer version = 1;

    SearchHistorySQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table SearchHistroy(id integer primary key autoincrement," +
                "name text  not null unique," +
                "detail text not null," +
                "lon real not null," +
                "lat real not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
