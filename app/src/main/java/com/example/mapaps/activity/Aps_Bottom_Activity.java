package com.example.mapaps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

public class Aps_Bottom_Activity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener , Common_Data {

    //地图样式参数
    MapView mMapView=null;
    AMap aMap=null;
    //路线规划参数
    private com.amap.api.services.core.LatLonPoint user_loc=null;
    private com.amap.api.services.core.LatLonPoint goal_loc=null;
    private RouteSearch.FromAndTo fromAndTo=null;
    private RouteSearch routeSearch=null;
    private String city_code=null;
    private int aps_code=1;
    //用户界面元素
    private APS_Fragment aps_fragment=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aps__bottom);

        getLoc(getIntent());
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.aps_bot_view);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        init();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Log.e("test","log:"+aps_code);
                switch (menuItem.getItemId()){
                    //todo 添加按钮判断
                    case R.id.drive_item:
                        aps_code=Common_Data.Drive_code;
                        break;
                    case R.id.ride_item:
                        aps_code=Common_Data.Bike_code;
                        break;
                    case R.id.walk_item:
                        aps_code=Common_Data.Walk_code;
                        break;
                    case R.id.bus_item:
                        aps_code=Common_Data.Bus_code;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }
                aps_fragment.setAps_code(aps_code);
                if(!startRouteSreach(user_loc,goal_loc,aps_code)) {
                    Log.e("test","初始规划失败");
                }
                Log.e("test","log:"+aps_code);
                return true;
            }
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.drive_item,R.id.ride_item,R.id.walk_item,R.id.bus_item)
                .build();

        FloatingActionButton start_btn=findViewById(R.id.start_aps_Button);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aps_code!=Common_Data.Bus_code){
                    aps_fragment.show(getSupportFragmentManager(),"aps");
                }
                else {
                    Toast.makeText(Aps_Bottom_Activity.this,"本版本不支持公交导航",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 初始化AMap对象
    void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(14);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
            //将地图移动到定位点
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(this.user_loc.getLatitude(),this.user_loc.getLongitude())));
            //设置点击事件
            aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    return false;
                }
            });
            aps_fragment=new APS_Fragment();
            aps_fragment.setAps_code(aps_code);
            aps_fragment.setmStartLatlng(user_loc);
            aps_fragment.setmEndLatlng(goal_loc);
            if(!startRouteSreach(user_loc,goal_loc,aps_code)) {
                Log.e("test","初始规划失败");
            }
        }
    }

    //获取起点终点数据
    private void getLoc(@NonNull Intent data){
        this.city_code=data.getStringExtra("city_code");
        this.user_loc=new LatLonPoint(data.getDoubleExtra("slat",0),data.getDoubleExtra("slon",0));
        this.goal_loc=new LatLonPoint(data.getDoubleExtra("elat",0),data.getDoubleExtra("elon",0));
        Log.e("test1","地点坐标为："+goal_loc.getLongitude()+","+goal_loc.getLatitude());
        this.aps_code=Common_Data.Drive_code;
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
        Log.e("test","起点坐标为：（"+start.getLongitude()+","+start.getLatitude()
                +"）\n终点坐标为：（"+end.getLongitude()+","+end.getLatitude()+"）"
                +"\n code为："+aps_code);
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
                routeSearch.calculateBusRouteAsyn(busRouteQuery);
                break;
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
                Log.e("test","开始绘制骑行路线");
                for(RideStep mRideStep:ridePath.getSteps()){
                    for(LatLonPoint mLatLonPoint:mRideStep.getPolyline()){
                        latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                    }
                }
                break;
            case Common_Data.Walk_code:
                WalkRouteResult walkRouteResult=(WalkRouteResult)result;
                WalkPath walkPath=walkRouteResult.getPaths().get(0);
                Log.e("test","开始绘制步行路线");
                for(WalkStep mWalkStep:walkPath.getSteps()){
                    for(LatLonPoint mLatLonPoint:mWalkStep.getPolyline()){
                        latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                    }
                }
                break;
            case Common_Data.Bus_code:
                BusRouteResult busRouteResult=(BusRouteResult)result;
                BusPath busPath=busRouteResult.getPaths().get(0);
                Log.e("test","开始绘制公交路线");
                for(BusStep mBusStep:busPath.getSteps()){
                    for(RouteBusLineItem busLineItem:mBusStep.getBusLines()){
                        for(LatLonPoint mLatLonPoint:busLineItem.getPolyline()){
                            latLngs.add(new LatLng(mLatLonPoint.getLatitude(),mLatLonPoint.getLongitude()));
                        }
                    }
                    //todo 增加绘制步行路线
                }
                break;
            default:
                return;
        }

        //先清除一下,避免重复显示
        aMap.clear();

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

        this.aps_code=code;
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
        aps_code=Common_Data.Drive_code;
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
            drawRouteToMap(walkRouteResult,Common_Data.Walk_code);
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
            drawRouteToMap(rideRouteResult,Common_Data.Bike_code);
        }
        else {
            Log.e("test","骑行路线规划失败");
            Err_check(i);
        }
    }

}
