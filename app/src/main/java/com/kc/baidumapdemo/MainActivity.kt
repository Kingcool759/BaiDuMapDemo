package com.kc.baidumapdemo

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng


class MainActivity : AppCompatActivity() {

    val mMapView: MapView by lazy {
        findViewById<MapView>(R.id.mMapView)
    }
    val mBaiduMap: BaiduMap by lazy {
        mMapView.map
    }
    lateinit var mLocationClient: LocationClient
    lateinit var option: LocationClientOption
    lateinit var mLocationConfiguration: MyLocationConfiguration
    lateinit var myLocationListener: MyLocationListener
    lateinit var mLocationData: MyLocationData
    var mLatitude:Double = 0.0  //维度
    var mLongitude:Double = 0.0  //经度

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //需要初始化SDK
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(application)
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main)
//        mBaiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE; //显示卫星图层
        mBaiduMap.isMyLocationEnabled = true;  //开启地图的定位图层

        //在地图上获取我的位置
        getMyLocationAtMap()
        //在地图上画弧线
        drawLineAtMap()
        //在地图上画圆
        drawCircleAtMap()
        //在地图上画多边形
        drawMutliAtMap()

        //增加重定位我的位置
        val btnReGetLocation = findViewById<Button>(R.id.btnReGetLocation)
        btnReGetLocation.setOnClickListener {
           // mBaiduMap.setMyLocationData(mLocationData)
            val mLatLng = LatLng(mLatitude,mLongitude)
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng))
            //getMyLocationAtMap()
        }
    }

    fun getMyLocationAtMap() {
        //定位初始化
        mLocationClient = LocationClient(this)
        //通过LocationClientOption设置LocationClient相关参数
        option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(0)
        //设置locationClientOption
        mLocationClient.locOption = option
        //注册LocationListener监听器
        myLocationListener = MyLocationListener()
        mLocationClient.registerLocationListener(myLocationListener)
        //开启地图定位图层
        mLocationClient.start()
        //绘制我的个人定位点
        mLocationConfiguration = MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.NORMAL,
            true,
            BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background),
            Color.parseColor("#AAFFFF88"),
            Color.parseColor("#AA00FF00")
        )
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration)
    }

    fun drawLineAtMap() {
        // 添加弧线坐标数据
        val p1 = LatLng(39.97923, 116.357428) //起点
        val p2 = LatLng(39.94923, 116.397428) //中间点
        val p3 = LatLng(39.97923, 116.437428) //终点
        //构造ArcOptions对象
        val mArcOptions: OverlayOptions = ArcOptions()
            .color(Color.RED)
            .width(10)
            .points(p1, p2, p3)
        //在地图上显示弧线
        val mArc = mBaiduMap.addOverlay(mArcOptions)
    }

    fun drawCircleAtMap() {
        //圆心位置
        val center = LatLng(39.90923, 116.447428)
        //构造CircleOptions对象
        val mCircleOptions = CircleOptions().center(center)
            .radius(1400)
            .fillColor(-0x55ffff01) //填充颜色
            .stroke(Stroke(5, -0x55ff0100)) //边框宽和边框颜色
        //在地图上显示圆
        val mCircle: Overlay = mBaiduMap.addOverlay(mCircleOptions)
    }

    fun drawMutliAtMap() {
        //多边形顶点位置
        val points: MutableList<LatLng> = ArrayList()
        points.add(LatLng(39.93923, 116.357428))
        points.add(LatLng(39.91923, 116.327428))
        points.add(LatLng(39.89923, 116.347428))
        points.add(LatLng(39.89923, 116.367428))
        points.add(LatLng(39.91923, 116.387428))
        //构造PolygonOptions
        val mPolygonOptions = PolygonOptions()
            .points(points)
            .fillColor(-0x55000100) //填充颜色
            .stroke(Stroke(5, -0x55ff0100)) //边框宽度和颜色
        //在地图上显示多边形
        mBaiduMap.addOverlay(mPolygonOptions)
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause()
    }

    override fun onDestroy() {
        mLocationClient.stop();
        mBaiduMap.isMyLocationEnabled = false;
        super.onDestroy()
        //在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy()
    }

    inner class MyLocationListener() :
        BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return
            }
            Log.d("zjwlocTypeDescription", location.locTypeDescription)
            Log.d("zjwlocType", location.locType.toString())
            mLocationData = MyLocationData.Builder()
                .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.direction)
                .latitude(location.latitude)
                .longitude(location.longitude).build()
            mLatitude = location.latitude
            mLongitude = location.longitude
            mBaiduMap.setMyLocationData(mLocationData)
        }
    }
}

