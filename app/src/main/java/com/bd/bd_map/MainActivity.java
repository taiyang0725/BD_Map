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


    private LocationService locationService;


    private Context context;

    private double lat;//纬度
    private double lon;//经度
    private String cityName;//
    private String address;

    private BDLocation bdLocation;

    private boolean useDefaultIcon;

    @Override
    protected void onStart() {
        super.onStart();
        // -----------location config ------------
        locationService = ((BDMapApplication) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = 0;
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        txt_type = (TextView) this.findViewById(R.id.txt_type);
        txt_type.setOnClickListener(this);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService = ((BDMapApplication) getApplication()).locationService;
        //注册监听
        locationService.registerListener(mListener);

        locationService.setLocationOption(locationService.getDefaultLocationClientOption());

        if (locationService != null) {
            locationService.start();
        } else {
            Log.i("error-----null", "+locationService is null");
        }


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
                                if (locationService != null)
                                    locationService.start();
                                break;
                            case 3:
                                if (locationService != null)
                                    locationService.stop();
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
        edt_here.setText(address);
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
        PlanNode stNode = PlanNode.withCityNameAndPlaceName(cityName, here);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(cityName, there);

        mSearch.transitSearch((new TransitRoutePlanOption())
                .from(stNodes)
                .city(cityName)
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


    /*
     * @see copy funtion to you project
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                lat = location.getLatitude();
                lon = location.getLongitude();
                cityName = location.getCity();
                address = location.getAddrStr();

                setLocationResult(location);


                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");
                sb.append(location.getCityCode());
                sb.append("\ncity : ");
                sb.append(location.getCity());
                sb.append("\nDistrict : ");
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");
                sb.append(location.getStreet());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\nDescribe: ");
                sb.append(location.getLocationDescribe());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());
                sb.append("\nPoi: ");
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
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

                Log.i("result", sb.toString());

            }
        }

    };


    /***
     * Stop location service
     */
    @Override
    protected void onStop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

}
