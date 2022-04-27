package com.sj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.sj.activity.BikeNavigateActivity;
import com.sj.activity.WalkNavigateActivity;
import com.sj.overlayutil.BikingRouteOverlay;
import com.sj.overlayutil.WalkingRouteOverlay;
import com.sj.util.DialogUtil;

public class MainActivity extends Activity {

    private static final String TAG = "Bmap";
    //意图参数
    static final int NAVIGATEINTENTCODE = 1;//搜索意图
    static final int WALKNAVIGATECODE = 2;//步行导航返回意图
    static final int BIKENAVIGATECODE = 3;//骑行导航返回意图
    static final int NAVIGATEWAYWALKCODE = 1;//步行导航参数
    static final int NAVIGATEWAYBIKECODE = 2;//骑车导航参数


    private MapView mMapView = null;//地图view
    private BaiduMap mBaiduMap = null;//地图对象
    private LocationClient mLocationClient = null;//定位浏览器
    private LatLng mLatLng = null;//我的位置
    private boolean isFirstLocate = true;//是否第一次定位
    private SuggestionSearch mSuggestionSearch = null;//sug检索实例
    private RoutePlanSearch mSearch = null;//路径规划
    private LatLng navigate_start = null;//导航的起点
    private LatLng navigate_end = null;//导航的终点
    private int way_code = -1;//出行方式


    //控件实例
    private ImageView icon_daohang = null;//导航图标
    private LinearLayout start_liner = null;//开始导航
    private ImageButton start_image = null;//开始导航图标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //控件初始化
        widgetIni();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);

        //定位控件
        ImageButton locate = findViewById(R.id.locate);
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateCen();
            }
        });

        //通过mapView获取map对象
        mBaiduMap = mMapView.getMap();
        //开启定位服务
        {
            //开启地图的定位图层
            mBaiduMap.setMyLocationEnabled(true);

            //定位初始化
            mLocationClient = new LocationClient(this);

            //通过LocationClientOption设置LocationClient相关参数
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true); // 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setScanSpan(1000);

            //设置locationClientOption
            mLocationClient.setLocOption(option);

            //注册LocationListener监听器
            MyLocationListener myLocationListener = new MyLocationListener();
            mLocationClient.registerLocationListener(myLocationListener);

            //开启地图定位图层
            mLocationClient.start();
        }

        routePlanIni();//路径规划功能初始化

        navigateIni();//导航功能初始化
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
//        mLocationClient.start();
    }

    @Override
    protected void onDestroy() {
//        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
    }

    /**
     * 定位内部监听类
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
//            Log.d(TAG, "/" + location.getLatitude());

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            //以自己的坐标为地图中心点
            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            //判断是否为第一次定位，即进入app第一次自动定位
            if (isFirstLocate) {
                locateCen();
                isFirstLocate = false;
            }

            mBaiduMap.setMyLocationData(locData);
        }
    }

    /**
     * 定位中心事件
     */
    public void locateCen() {
        MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(mLatLng);
        mBaiduMap.animateMapStatus(status, 500);
    }

    /**
     * 控件初始化
     */
    public void widgetIni() {
//
//        Typeface typeface=Typeface.createFromAsset(getAssets(),"iconfont.ttf");
//        icon_daohang=findViewById(R.id.icon_daohang);
//
//        icon_daohang.setTypeface(typeface);

        icon_daohang = findViewById(R.id.icon_daohang);
        //意图跳转
        icon_daohang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SearchActivity.class);
                startActivityForResult(intent, NAVIGATEINTENTCODE);
                Log.e(TAG, "意图跳转");
            }
        });

        start_liner = findViewById(R.id.start_linner);
        start_liner.setVisibility(View.INVISIBLE);//默认不显示


        start_image = findViewById(R.id.start_image);
        start_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNavigate(navigate_start, navigate_end,way_code);
            }
        });

    }

    /**
     * 处理搜索活动返回的数据
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NAVIGATEINTENTCODE://导航请求码
//                Log.e(TAG,"接收到返回值"+data.getDoubleExtra("start_lat",0));
                if (data != null) {
                    LatLng start = new LatLng(data.getDoubleExtra("start_lat", 0), data.getDoubleExtra("start_lng", 0));
                    LatLng end = new LatLng(data.getDoubleExtra("end_lat", 0), data.getDoubleExtra("end_lng", 0));
                    way_code = data.getIntExtra("way_code", -1);
                    if (start.latitude == 0 || start.longitude == 0 || end.latitude == 0 || end.longitude == 0 || way_code == -1) {//数据错误
                        DialogUtil.errot(getApplicationContext(), "参数错误");
                        return;
                    }
                    navigate_start = start;
                    navigate_end = end;
                    start_liner.setVisibility(View.VISIBLE);
                    routePlan(start, end, way_code);
                }

                break;
            case WALKNAVIGATECODE://步行导航返回
                navigateIni();
                break;
        }
    }

    /**
     * 路径规划
     *
     * @param start 起点坐标
     * @param end   终点坐标
     * @param way   出行方式
     */
    public void routePlan(LatLng start, LatLng end, int way) {


        switch (way) {
            case NAVIGATEWAYWALKCODE://步行
                walkRoutePlan(start, end);
                break;
            case NAVIGATEWAYBIKECODE://骑车
                bikeRoutePlan(start, end);
                break;
        }

    }

    /**
     * 步行路径规划
     *
     * @param start 起点坐标
     * @param end   终点坐标
     */
    public void walkRoutePlan(LatLng start, LatLng end) {
        PlanNode stNode = PlanNode.withLocation(start);
        PlanNode enNode = PlanNode.withLocation(end);
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(enNode));
    }

    /**
     * 骑行路径规划
     *
     * @param start 起点坐标
     * @param end   终点坐标
     */
    public void bikeRoutePlan(LatLng start, LatLng end) {

        PlanNode stNode = PlanNode.withLocation(start);
        PlanNode enNode = PlanNode.withLocation(end);

        mSearch.bikingSearch((new BikingRoutePlanOption())
                .from(stNode)
                .to(enNode)
                // ridingType  0 普通骑行，1 电动车骑行
                // 默认普通骑行
                .ridingType(0));
    }

    /**
     * 路径规划功能初始化，包括步行和骑行
     */
    public void routePlanIni() {

        if (mSearch == null) {//为空则创建
            mSearch = RoutePlanSearch.newInstance();//创建实例
        }


        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            //步行路径规划初始化
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                Log.e(TAG, "步行路线规划");
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                if (walkingRouteResult.getRouteLines() != null && walkingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条数据为例)
                    //为WalkingRouteOverlay实例设置路径数据
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    //在地图上绘制WalkingRouteOverlay
                    overlay.addToMap();
                } else {
                    Log.e(TAG, "步行路线规划时出现了未知错误，可能是起点和终点离得太远了");
                }
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            //骑行路径规划初始化
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
                Log.e(TAG, "骑行路线规划");
                //创建BikingRouteOverlay实例
                BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
                if (bikingRouteResult.getRouteLines() != null &&bikingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条路线为例）
                    //为BikingRouteOverlay实例设置数据
                    overlay.setData(bikingRouteResult.getRouteLines().get(0));
                    //在地图上绘制BikingRouteOverlay
                    overlay.addToMap();
                }else {
                    Log.e(TAG, "骑行路线规划时出现了未知错误，可能是起点和终点离得太远了");
                }
            }
        };

        mSearch.setOnGetRoutePlanResultListener(listener);

    }

    /**
     * 导航功能初始化，包括步行和骑行
     */
    public void navigateIni() {
        // 获取导航控制类
        // 步行导航引擎初始化
        WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {

            @Override
            public void engineInitSuccess() {
                //引擎初始化成功的回调
                //routeWalkPlanWithParam();
                Log.e(TAG, "步行导航引擎初始化成功");
            }

            @Override
            public void engineInitFail() {
                //引擎初始化失败的回调
                Log.e(TAG, "步行导航引擎初始化失败");
            }
        });

        // 骑行导航引擎初始化
        BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
            @Override
            public void engineInitSuccess() {
                //骑行导航初始化成功之后的回调
                //routePlanWithParam();
                Log.e(TAG, "骑行导航引擎初始化成功");
            }

            @Override
            public void engineInitFail() {
                //骑行导航初始化失败之后的回调
                Log.e(TAG, "骑行导航引擎初始化失败");
            }
        });

    }

    /**
     * 开始导航
     *
     * @param start 起点
     * @param end   终点
     */
    public void startNavigate(LatLng start, LatLng end,int way) {
        if (start == null || end == null||way==-1) {
            Log.e(TAG, "发起导航时起点或终点为null，或者可能时出行方式不对，结束导航");
            return;
        }

        //导航功能初始化
        navigateIni();

        switch (way){
            case NAVIGATEWAYWALKCODE://步行导航
                startWalkNavigate(start,end);
                break;
            case NAVIGATEWAYBIKECODE://骑行导航
                startBikeNavigate(start,end);
                break;
        }

    }

    /**
     * 开始骑行导航
     * @param start 起点坐标
     * @param end 终点坐标
     */
    private void startBikeNavigate(LatLng start, LatLng end) {

        //构造BikeNaviLaunchParam
        BikeRouteNodeInfo startNode = new BikeRouteNodeInfo();
        startNode.setLocation(start);
        BikeRouteNodeInfo endNode = new BikeRouteNodeInfo();
        endNode.setLocation(end);
        BikeNaviLaunchParam mParam = new BikeNaviLaunchParam();
        mParam.startNodeInfo(startNode);
        mParam.endNodeInfo(endNode);
        mParam.stPt(start);//这两行不知道算不算bug
        mParam.endPt(end);//这两行不知道算不算bug

        //发起算路
        BikeNavigateHelper.getInstance().routePlanWithParams(mParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //开始算路的回调
                Log.e(TAG, "开始骑行导航算路");
            }

            @Override
            public void onRoutePlanSuccess() {
                //算路成功
                //跳转至诱导页面
                Intent intent = new Intent(MainActivity.this, BikeNavigateActivity.class);
                startActivityForResult(intent, BIKENAVIGATECODE);
                Log.e(TAG, "骑行导航算路成功");
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError bikeRoutePlanError) {
                //算路失败的回调
                Log.e(TAG, "骑行导航算路失败");
            }

        });
    }

    /**
     * 开始步行导航
     * @param start 起点坐标
     * @param end 终点坐标
     */
    public void startWalkNavigate(LatLng start,LatLng end){
        //构造WalkNaviLaunchParam
        //WalkNaviLaunchParam mParam = new WalkNaviLaunchParam().stPt(start).endPt(end);//已经被弃用了
        WalkRouteNodeInfo startNode = new WalkRouteNodeInfo();
        startNode.setLocation(start);
        WalkRouteNodeInfo endNode = new WalkRouteNodeInfo();
        endNode.setLocation(end);
        WalkNaviLaunchParam mParam = new WalkNaviLaunchParam();
        mParam.startNodeInfo(startNode);
        mParam.endNodeInfo(endNode);
        mParam.stPt(start);//这两行不知道算不算bug
        mParam.endPt(end);//这两行不知道算不算bug


        //发起算路
        WalkNavigateHelper.getInstance().routePlanWithParams(mParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //开始算路的回调
                Log.e(TAG, "开始步行导航算路");
            }

            @Override
            public void onRoutePlanSuccess() {
                //算路成功
                //跳转至诱导页面
                Intent intent = new Intent(MainActivity.this, WalkNavigateActivity.class);
                startActivityForResult(intent, WALKNAVIGATECODE);
                Log.e(TAG, "步行导航算路成功");
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError walkRoutePlanError) {
                //算路失败的回调
                Log.e(TAG, "步行导航算路失败");
            }
        });
    }

}
