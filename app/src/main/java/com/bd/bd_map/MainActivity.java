package com.bd.bd_map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiAddrInfo;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.List;

import overlayutil.PoiOverlay;
import overlayutil.TransitRouteOverlay;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private TextView txt_type;

    private LocationClient mLocationClient;
    private BDLocationListener myListener = new MyLocationListener();

    private Context context;

    private double lat;//纬度
    private double lon;//经度
    private String cityName;//

    private boolean useDefaultIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        txt_type = (TextView) this.findViewById(R.id.txt_type);
        txt_type.setOnClickListener(this);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();


    }

    /**
     *
     * */
    private void setMapType(int type) {
        mBaiduMap.setMapType(type);
    }

    private void setLocationResult(BDLocation location) {

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.locationicon);
        MyLocationConfiguration config = new
                MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
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
    }

    @Override
    public void onClick(View v) {
        String[] items = new String[]{
                getResources().getString(R.string.map_type_normal),
                getResources().getString(R.string.map_type_satellite),
                getResources().getString(R.string.start_location),
                getResources().getString(R.string.stop_location),
                getResources().getString(R.string.search_around),
                getResources().getString(R.string.go_to_where)
        };
        new AlertDialog.Builder(this).setItems(items,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                setMapType(BaiduMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                initLocation();
                                mLocationClient.start();
                                break;
                            case 3:
                                mLocationClient.stop();
                                break;
                            case 4:
                                searchWhat();
                                break;
                            case 5:
                                goToWhere();

                                //Intent intent = new Intent(context, GoToActivity.class);
                                //startActivity(intent);
                        }

                    }
                }).show();

    }

    private void goToWhere() {
        View view = View.inflate(this, R.layout.go_to_where, null);
        final Dialog dialog = new AlertDialog.Builder(this).setView(view).show();
        final EditText edt_here = (EditText) view.findViewById(R.id.edt_here);
        final EditText edt_there = (EditText) view.findViewById(R.id.edt_there);

        view.findViewById(R.id.txt_walk).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        String here = edt_here.getText().toString();
                        String there = edt_there.getText().toString();

                    }
                });
        view.findViewById(R.id.txt_bus).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        String here = edt_here.getText().toString();
                        String there = edt_there.getText().toString();
                        set_public_transport_line(here, there);

                    }
                });


    }

    private void set_public_transport_line(String here, String there) {

        RoutePlanSearch mSearch = RoutePlanSearch.newInstance();

        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {

                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(context, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()东门
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                }

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        };

        mSearch.setOnGetRoutePlanResultListener(listener);
        PlanNode stNodes = PlanNode.withLocation(new LatLng(lat, lon));
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("西安", here);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("西安", there);

        mSearch.transitSearch((new TransitRoutePlanOption())
                .from(stNodes)
                .city("西安")
                .to(enNode));


    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {


        /**
         * 构造函数
         *
         * @param baiduMap 该TransitRouteOverlay引用的 BaiduMap 对象
         */
        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }


        @Override
        public BitmapDescriptor getStartMarker() {

            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);

        }

        @Override
        public BitmapDescriptor getTerminalMarker() {

            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);

        }
    }

    private void searchWhat() {

        View view = View.inflate(this, R.layout.search_ui, null);
        final Dialog dialog = new AlertDialog.Builder(this).setView(view).show();

        final EditText edt = (EditText) view.findViewById(R.id.edt_what);
        view.findViewById(R.id.txt_search).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        String va = edt.getText().toString();
                        if (!TextUtils.isEmpty(va))
                            setPoiSearch(va);

                    }
                });

    }


    private void setPoiSearch(String va) {

        PoiSearch poiSearch = PoiSearch.newInstance();

        OnGetPoiSearchResultListener onGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult result) {

                if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    mBaiduMap.clear();
                    //创建PoiOverlay
                    PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                    //设置overlay可以处理标注点击事件
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    //设置PoiOverlay数据
                    overlay.setData(result);
                    //添加PoiOverlay到地图中
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    return;
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };


        poiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener);


        PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();
        poiNearbySearchOption.keyword(va);
        poiNearbySearchOption.location(new LatLng(lat, lon));
        poiNearbySearchOption.radius(5000);

        //poiSearch.searchNearby(poiNearbySearchOption);

        boolean isSuccee = poiSearch.searchNearby(poiNearbySearchOption);

        if (!isSuccee) {
            Toast.makeText(context, "搜索失败!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "搜索成功!", Toast.LENGTH_SHORT).show();

        }

        //poiSearch.destroy();


    }

    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            return true;
        }
    }


    /**
     * 配置定位参数
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    /**
     * 定位监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            setLocationResult(location);
            lat = location.getLatitude();
            lon = location.getLongitude();
            cityName = location.getCity();

            //Log.i("-----result-----", cityName+location.getCountry()+location.getAddrStr());

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("-----result-----", sb.toString());
        }
    }
}
