package com.example.mapaps.adapter;

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
}
