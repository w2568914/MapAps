package com.example.mapaps.adapter;

import android.Manifest;

public interface Common_Data {
    //导航分类标志
    public static final int Drive_code=1;
    public static final int Bike_code=2;
    public static final int Walk_code=3;
    public static final int Bus_code=4;

    //intent传递标志
    public static final int POI_Res=1;
    public static final int POI_Return=3;

    //错误码标识
    public static final int Err_Walk_TooLong=3003;
    public static final int Err_Not_China=3000;
    public static final int Err_No_Way=3001;
    public static final int Err_Not_Concert=3002;
    public static final int Err_No_Internet=1802;
    public static final int Err_No_Concertion=1806;

    //权限标识
    //  SDK在Android 6.0以上的版本需要进行运行检测的动态权限如下：
    //    Manifest.permission.ACCESS_COARSE_LOCATION,
    //    Manifest.permission.ACCESS_FINE_LOCATION,
    //    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    //    Manifest.permission.READ_EXTERNAL_STORAGE,
    //    Manifest.permission.READ_PHONE_STATE

    public  static  final String[] perms={Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    public  static  final String[] loc_perms={Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    public  static  final int request_code=10000;
}
