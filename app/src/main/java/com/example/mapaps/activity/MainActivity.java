package com.example.mapaps.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.example.mapaps.Dialog.APS_Fragment;
import com.example.mapaps.R;
import com.example.mapaps.adapter.Common_Data;
import com.tencent.bugly.Bugly;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteSearch.OnRouteSearchListener , Common_Data {

    //地图样式参数
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 0;
    MapView mMapView=null;
    AMap aMap=null;
    MyLocationStyle myLocationStyle=null;

    //定位需要的声明
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private LocationSource.OnLocationChangedListener mListener = null;
    private boolean isFirstLoc=true;

    //路线规划参数
    private com.amap.api.services.core.LatLonPoint user_loc=null;
    private com.amap.api.services.core.LatLonPoint goal_loc=null;
    private RouteSearch.FromAndTo fromAndTo=null;
    private RouteSearch routeSearch=null;
    private String city_code=null;
    private int aps_code=-1;

    //用户界面元素
    private Button Select_btn_bom=null;
    private APS_Fragment aps_fragment=null;
    private LinearLayout top_bar=null;
    private Button Drive_btn=null;
    private Button Bike_btn=null;
    private Button Walk_btn=null;
    private Button Bus_btn=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //buggly初始化
        Bugly.init(getApplicationContext(), "3bfc25f272", true);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图组件
        init();
        aps_fragment=new APS_Fragment();
        top_bar=findViewById(R.id.top_choose_bar);
        top_bar.setVisibility(View.GONE);
        Drive_btn=findViewById(R.id.Drive_btn);
        Drive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aps_code=Common_Data.Drive_code;
                if(!startRouteSreach(user_loc,goal_loc,aps_code)){
                    Toast.makeText(MainActivity.this,"进行驾车路线规划失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bike_btn=findViewById(R.id.Bike_btn);
        Bike_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aps_code=Common_Data.Bike_code;
                if(!startRouteSreach(user_loc,goal_loc,aps_code)){
                    Toast.makeText(MainActivity.this,"进行骑行路线规划失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bus_btn=findViewById(R.id.Bus_btn);
        Bus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aps_code=Common_Data.Bus_code;
                if(!startRouteSreach(user_loc,goal_loc,aps_code)){
                    Toast.makeText(MainActivity.this,"进行公交路线规划失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Walk_btn=findViewById(R.id.Walk_btn);
        Walk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aps_code=Common_Data.Walk_code;
                if(!startRouteSreach(user_loc,goal_loc,aps_code)){
                    Toast.makeText(MainActivity.this,"进行步行路线规划失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 初始化AMap对象
    void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            //设置定位点样式
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            aMap.setMyLocationStyle(myLocationStyle);
            //设置显示定位按钮 并且可以点击
            UiSettings settings = aMap.getUiSettings();
            //设置了定位的监听
            aMap.setLocationSource(this);
            // 是否显示定位按钮
            settings.setMyLocationButtonEnabled(true);
            //显示定位层并且可以触发定位,默认是flase
            aMap.setMyLocationEnabled(true);
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(14);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
            //设置点击事件
            aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });
        }
        // 开启定位
        initLoc();

        Select_btn_bom=findViewById(R.id.select_button_bottom);
        Select_btn_bom.setText("设置目的地");
        Select_btn_bom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 打开aps界面
                switch (aps_code) {
                    //驾车
                    case Common_Data.Drive_code:
                        //骑行
                    case Common_Data.Bike_code:
                        //步行
                    case Common_Data.Walk_code:
                        //公交
                    case Common_Data.Bus_code:
                        aps_fragment.show(getSupportFragmentManager(),"aps");
                        break;
                    //默认进入搜索界面
                    default:
                        final Intent aps_intent=new Intent(MainActivity.this,SelectActivity.class);
                        startActivityForResult(aps_intent,Common_Data.POI_Res);
                        break;
                }
            }
        });
    }

    //进行路线规划
    private boolean startRouteSreach(com.amap.api.services.core.LatLonPoint start, com.amap.api.services.core.LatLonPoint end, int Code){
        if (routeSearch == null) {
            routeSearch = new RouteSearch(this);
        }
        if (start != null && end != null) {
            fromAndTo = new RouteSearch.FromAndTo(start, end);
        }
        routeSearch.setRouteSearchListener(this);
        //todo 测试数据
        Log.e("test","起点坐标为：（"+start.getLongitude()+","+start.getLatitude()+"）\n终点坐标为：（"+end.getLongitude()+","+end.getLatitude()+"）");
        //todo 分类规划
        switch(Code){
            case Common_Data.Drive_code:
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(
                        fromAndTo, RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST_AVOID_CONGESTION, null, null, "");
                routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
                break;
            case Common_Data.Bike_code:
                RouteSearch.RideRouteQuery rideRouteQuery=new RouteSearch.RideRouteQuery(fromAndTo,RouteSearch.RidingDefault);
                routeSearch.calculateRideRouteAsyn(rideRouteQuery);
                break;
            case Common_Data.Walk_code:
                RouteSearch.WalkRouteQuery walkRouteQuery=new RouteSearch.WalkRouteQuery(fromAndTo,RouteSearch.WALK_DEFAULT);
                routeSearch.calculateWalkRouteAsyn(walkRouteQuery);
                break;
            case Common_Data.Bus_code:
                RouteSearch.BusRouteQuery busRouteQuery=new RouteSearch.BusRouteQuery(fromAndTo,RouteSearch.BUS_COMFORTABLE,city_code,0);
            default:
                return false;
        }

        return true;
    }

    //路线绘制
    private void drawRouteToMap(RouteResult result, int code){
        //记录起点终点
        LatLng routeStart=new LatLng(result.getStartPos().getLatitude(),result.getStartPos().getLongitude());
        LatLng routeEnd=new LatLng(result.getTargetPos().getLatitude(),result.getTargetPos().getLongitude());
        //创建存储坐标点的集合
        List<LatLng> latLngs = new ArrayList<>();

        //分类记录路径
        switch (code) {
            case Common_Data.Drive_code:
                DriveRouteResult driveRouteResult=(DriveRouteResult)result;
                /*多路径模式
                List<DrivePath> paths=driveRouteResult.getPaths();
                for(DrivePath mDrivePath:paths) {
                    for(DriveStep mDriveStep:mDrivePath.getSteps()){
                        for(LatLonPoint mLatLonPoint:mDriveStep.getPolyline()){
                            latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                        }
                    }
                }*/
                DrivePath drivePath=driveRouteResult.getPaths().get(0);
                for(DriveStep mDriveStep:drivePath.getSteps()){
                    for(LatLonPoint mLatLonPoint:mDriveStep.getPolyline()){
                        latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                    }
                }
                break;
            //todo 设计其他路线模式
            case Common_Data.Bike_code:
                RideRouteResult rideRouteResult=(RideRouteResult)result;
                RidePath ridePath=rideRouteResult.getPaths().get(0);
                for(RideStep mRideStep:ridePath.getSteps()){
                    for(LatLonPoint mLatLonPoint:mRideStep.getPolyline()){
                        latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                    }
                }
                break;
            case Common_Data.Walk_code:
                WalkRouteResult walkRouteResult=(WalkRouteResult)result;
                WalkPath walkPath=walkRouteResult.getPaths().get(0);
                for(WalkStep mWalkStep:walkPath.getSteps()){
                    for(LatLonPoint mLatLonPoint:mWalkStep.getPolyline()){
                        latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                    }
                }
                break;
            case Common_Data.Bus_code:
                BusRouteResult busRouteResult=(BusRouteResult)result;
                BusPath busPath=busRouteResult.getPaths().get(0);
                for(BusStep mBusStep:busPath.getSteps()){
                    for(RouteBusLineItem busLineItem:mBusStep.getBusLines()){
                        for(LatLonPoint mLatLonPoint:busLineItem.getPolyline()){
                            latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                        }
                    }
                }
                break;
            default:
                return;
        }

        //先清除一下,避免重复显示
        aMap.clear();
        //top_bar.setVisibility(View.VISIBLE);

        //绘制起始位置和目的地marker
        aMap.addMarker(new MarkerOptions()
                .icon(null)
                .position(routeStart));
        aMap.addMarker(new MarkerOptions()
                .icon(null)
                .position(routeEnd));

        //绘制规划路径路线
        aMap.addPolyline(new PolylineOptions()
                //路线坐标点的集合
                .addAll(latLngs)
                //线的宽度
                .width(30)
                .color(getResources().getColor(R.color.design_default_color_primary_dark)));//设置画线的颜色

        //显示完整包含所有marker地图路线
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < latLngs.size(); i++) {
            builder.include(latLngs.get(i));
        }
        //显示全部marker,第二个参数是四周留空宽度
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));

        //启动导航模块
        aps_fragment.setmEndLatlng(new LatLonPoint(result.getTargetPos().getLatitude(),result.getTargetPos().getLongitude()));
        aps_fragment.setAps_code(code);
        Select_btn_bom.setText("开始导航");
        this.aps_code=code;
    }

    //接收目的地信息
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Common_Data.POI_Res&&resultCode==Common_Data.POI_Return){
            this.goal_loc=new LatLonPoint(data.getDoubleExtra("lat",0),data.getDoubleExtra("lon",0));
            Log.e("test1","地点坐标为："+goal_loc.getLongitude()+","+goal_loc.getLatitude());
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //测试绘制路线，默认驾车模式
            if(!startRouteSreach(this.user_loc,this.goal_loc,Common_Data.Drive_code)){
                Toast.makeText(MainActivity.this,"进行路线规划失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //解析错误原因
    private void Err_check(int i){
        if(i==Common_Data.Err_No_Concertion||i==Common_Data.Err_No_Internet){
            Toast.makeText(this,"请检查网络连接",Toast.LENGTH_LONG).show();
        }
        else if(i==Common_Data.Err_No_Way||i==Common_Data.Err_Not_Concert||i==Common_Data.Err_Not_China){
            Toast.makeText(this,"目的地无法到达，请更换目的地",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"未知错误",Toast.LENGTH_LONG).show();
        }
    }

    //初始化定位
    private void initLoc() {
        //  SDK在Android 6.0以上的版本需要进行运行检测的动态权限如下：
        //    Manifest.permission.ACCESS_COARSE_LOCATION,
        //    Manifest.permission.ACCESS_FINE_LOCATION,
        //    Manifest.permission.WRITE_EXTERNAL_STORAGE,
        //    Manifest.permission.READ_EXTERNAL_STORAGE,
        //    Manifest.permission.READ_PHONE_STATE

        //动态检查定位及内存权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    //启动定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }
    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }
    //定位监听回调
    //todo 标志位可能导致定位不准
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();  // 地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();  // 国家信息
                aMapLocation.getProvince();  // 省信息
                aMapLocation.getCity();  // 城市信息
                aMapLocation.getDistrict();  // 城区信息
                aMapLocation.getStreet();  // 街道信息
                aMapLocation.getStreetNum();  // 街道门牌号信息
                aMapLocation.getCityCode();  // 城市编码
                aMapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                    //记录用户当前位置
                    this.user_loc= new com.amap.api.services.core.LatLonPoint(aMapLocation.getLatitude(),
                            aMapLocation.getLongitude());
                    aps_fragment.setmStartLatlng(this.user_loc);
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(),
                            aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    //记录城市编码，用于公交规划
                    city_code=aMapLocation.getCityCode();
                    isFirstLoc = false;
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    //公交路线规划
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        // 清理地图上的所有覆盖物
        aMap.clear();
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //获取路径结果
            BusPath busPath=busRouteResult.getPaths().get(0);
            //获取花费
            float bus_cost=busPath.getCost();
            //距离
            float distance=busPath.getDistance()/1000;
            //时间
            long duration=busPath.getDuration()/60;
            //todo 显示信息
            Log.e("test","花费："+bus_cost+
                    "\n距离/公里"+distance+
                    "\n时间/分"+duration);
            drawRouteToMap(busRouteResult,Common_Data.Bus_code);
        }
        else {
            Log.e("test","公交路线规划失败");
            Err_check(i);
        }
    }
    //驾车路线规划
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        // 清理地图上的所有覆盖物
        aMap.clear();
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //获取路径结果
            DrivePath drivePath=driveRouteResult.getPaths().get(0);
            //策略
            String strategy=drivePath.getStrategy();
            //信号灯数量
            int clights=drivePath.getTotalTrafficlights();
            //距离
            float distance=drivePath.getDistance()/1000;
            //时间
            long duration=drivePath.getDuration()/60;
            //todo 显示信息
            Log.e("test","策略："+strategy+
                    "\n交通信号灯数量/个"+clights+
                    "\n距离/公里"+distance+
                    "\n时间/分"+duration);
            //todo 调用函数进行路线绘制
            drawRouteToMap(driveRouteResult,Common_Data.Drive_code);
        }
        else {
            Log.e("test","驾车路线规划失败");
            Err_check(i);
        }
    }
    //步行路线规划
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //获取路径结果
            WalkPath walkPath=walkRouteResult.getPaths().get(0);
            //距离
            float distance=walkPath.getDistance()/1000;
            //时间
            long duration=walkPath.getDuration()/60;
            //todo 显示信息
            Log.e("test", "距离/公里"+distance+ "\n时间/分"+duration);

        }
        else {
            Log.e("test","步行路线规划失败："+i);
            if(i==Common_Data.Err_Walk_TooLong){
                Toast.makeText(this,"距离过长，步行无法到达，请更换其他方式",Toast.LENGTH_LONG).show();
            }
            else {
                Err_check(i);
            }
        }

    }
    //骑行路线规划
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i == AMapException.CODE_AMAP_SUCCESS) {
            //获取路径结果
            RidePath ridePath=rideRouteResult.getPaths().get(0);
            //距离
            float distance=ridePath.getDistance()/1000;
            //时间
            long duration=ridePath.getDuration()/60;
            //todo 显示信息
            Log.e("test", "距离/公里"+distance+ "\n时间/分"+duration);
        }
        else {
            Log.e("test","骑行路线规划失败");
            Err_check(i);
        }
    }
}
