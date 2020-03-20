package com.example.mapaps.Dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.example.mapaps.R;
import com.example.mapaps.adapter.Common_Data;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class APS_Fragment extends DialogFragment implements AMapNaviListener, AMapNaviViewListener, Common_Data {

    private AMapNaviView aps_view=null;
    private AMapNavi mapNavi=null;
    //算路终点坐标
    protected NaviLatLng mEndLatlng = null;
    //算路起点坐标
    protected NaviLatLng mStartLatlng = null;
    //存储算路起点的列表
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    //存储算路终点的列表
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    //导航类型
    protected int aps_code=-1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_aps,container,false);
        //初始化导航组件
        mapNavi=AMapNavi.getInstance(view.getContext());
        mapNavi.setUseInnerVoice(true);
        mapNavi.addAMapNaviListener(this);
        //设置模拟导航的行车速度
        mapNavi.setEmulatorNaviSpeed(75);
        //初始化导航界面
        aps_view=view.findViewById(R.id.aps_view);
        aps_view.onCreate(savedInstanceState);
        aps_view.setAMapNaviViewListener(this);

        return view;
    }

    public NaviLatLng getmStartLatlng(){
        return this.mStartLatlng;
    }

    public NaviLatLng getmEndLatlng(){
        return this.mEndLatlng;
    }

    public int getAps_code(){
        return this.aps_code;
    }

    public void setmStartLatlng(LatLonPoint startLatlng){
        this.mStartLatlng=new NaviLatLng(startLatlng.getLatitude(),startLatlng.getLongitude());
    }

    public void setmEndLatlng(LatLonPoint endLatlng){
        this.mEndLatlng=new NaviLatLng(endLatlng.getLatitude(),endLatlng.getLongitude());
    }

    public void setAps_code(int code){
        this.aps_code=code;
    }

    @Override
    public void onResume() {
        super.onResume();
        aps_view.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        aps_view.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aps_view.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mapNavi.stopNavi();
        mapNavi.destroy();

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {
        /**
         * 方法:
         *   int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute);
         * 参数:
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         * 说明:
         *      以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         * 注意:
         *      不走高速与高速优先不能同时为true
         *      高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            strategy = mapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 算路
        switch (this.aps_code)
        {
            //驾车
            case Common_Data.Drive_code:
                //构建起点终点列表
                sList.add(mStartLatlng);
                eList.add(mEndLatlng);
                Log.e("test1","开始算路");
                if(!mapNavi.calculateDriveRoute(sList,eList,null,strategy)){
                    Log.e("test3","非法请求");
                }
                break;
            //骑行
            case Common_Data.Bike_code:
                if(!mapNavi.calculateRideRoute(mStartLatlng,mEndLatlng)){
                    Log.e("test5","非法请求");
                }
                break;
            //步行
            case Common_Data.Walk_code:
                if(!mapNavi.calculateWalkRoute(mStartLatlng,mEndLatlng)){
                    Log.e("test4","非法请求");
                }
                break;
            //公交
            case Common_Data.Bus_code:
            default:
                Log.e("test10","非法请求");
                break;
        }

    }

    @Override
    public void onStartNavi(int i) {
        Log.e("test","开始算路");
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {
        Log.e("test1","退出");
    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.e("test2","非法请求");
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //算路成功
        if(ints.length!=0){
            //todo 模拟导航模式，实际使用使需切换
            mapNavi.startNavi(NaviType.EMULATOR);
            Log.e("test","算路");
        }
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        //算路成功
        if(aMapCalcRouteResult.getErrorCode()==0){
            //todo 模拟导航模式，实际使用使需切换
            mapNavi.startNavi(NaviType.EMULATOR);
            Log.e("test1","算路");
        }

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {
        Log.e("test","非法请求:"+aMapCalcRouteResult.getErrorCode()+","+aMapCalcRouteResult.getErrorDetail());
    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {
        dismiss();
    }

    //使用默认对话框
    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }
}
