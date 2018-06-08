package kz.mobdev.mapview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.leantegra.wibeat.sdk.monitoring.ScanServiceManager;
import com.leantegra.wibeat.sdk.monitoring.info.BaseFrame;
import com.leantegra.wibeat.sdk.monitoring.info.IBeaconFrame;
import com.leantegra.wibeat.sdk.monitoring.listeners.ScanListener;
import com.leantegra.wibeat.sdk.monitoring.listeners.ScanServiceConsumer;
import com.leantegra.wibeat.sdk.monitoring.service.ScanError;
import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import kz.mobdev.mapview.data.DataForBeacons;
import kz.mobdev.mapview.data.Point;
import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.LocationLayer;
import kz.mobdev.mapview.library.layers.MarkerLayer;
import kz.mobdev.mapview.library.layers.RouteLayer;
import kz.mobdev.mapview.library.models.Marker;
import kz.mobdev.mapview.library.utils.MapUtils;
import kz.mobdev.mapview.utils.AssetsHelper;

import static com.leantegra.wibeat.sdk.monitoring.config.ScanConfig.SCAN_MODE_LOW_LATENCY;
import static com.leantegra.wibeat.sdk.monitoring.distance.ProximityZone.IMMEDIATE;
import static com.leantegra.wibeat.sdk.monitoring.distance.ProximityZone.NEAR;
import static com.leantegra.wibeat.sdk.monitoring.info.FrameType.I_BEACON;

public class BeaconRouteActivity extends AppCompatActivity implements ScanServiceConsumer, SensorEventListener, MapView.OnRotationChanged {

    private static final String TAG = "BeaconRouteActivity";

    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private MapView mapView;

    private MarkerLayer markLayer;
    private RouteLayer routeLayer;
    private LocationLayer locationLayer;

    private List<PointF> nodes;
    private List<PointF> nodesContract;
    private List<Marker> markers;
    private Marker mMarker;
    List<Integer> routeList;

    private ScanServiceManager mScanServiceManager;
    private ArrayMap<String, BaseFrame> mFoundedDeviceMap = new ArrayMap<>();

    private SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] mGravity = new float[3];
    float[] mGeomagnetic = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_layer_test);

        nodes = DataForBeacons.getNodesList();
        nodesContract = DataForBeacons.getNodesContactList();
        markers = DataForBeacons.getMarks();


        MapUtils.init(nodes.size(), nodesContract.size());

        mapView = findViewById(R.id.mapview);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("office.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onMapLoadSuccess() {
                Log.d(TAG, "onMapLoadSuccess");

                routeLayer = new RouteLayer(mapView);
                mapView.addLayer(routeLayer);


                markLayer = new MarkerLayer(mapView, markers);
                mapView.addLayer(markLayer);

                markLayer.setOnMarkerClickListener(new MarkerLayer.OnMarkerClickListener() {
                    @Override
                    public void onMarkerClick(Marker marker) {
                        mMarker = marker;

                        if (position != null) {
                            routeList = MapUtils.getShortestDistanceBetweenTwoPoints
                                    (position, mMarker, nodes, nodesContract);
                            routeLayer.setRouteList(routeList);
                            routeLayer.setNodeList(nodes);
                        }

                    }
                });


                locationLayer = new LocationLayer(mapView, null);
                locationLayer.setOpenCompass(true);
                mapView.addLayer(locationLayer);
                mapView.refresh();

            }

            @Override
            public void onMapLoadFail() {
                Log.d(TAG, "onMapLoadFail");
            }

        });
        //mapView.loadMap(AssetsHelper.getContent(this, "sample2.svg"));
        mapView.loadMap(bitmap);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_layer_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mapView.isMapLoadFinish()) {
            switch (item.getItemId()) {
                case R.id.route_layer_tsp:
                    List<PointF> list = new ArrayList<>();
                    list.add(markers.get(markers.size() - 1));
                    list.add(markers.get(new Random().nextInt(10)));
                    list.add(markers.get(new Random().nextInt(10) + 10));
                    list.add(markers.get(new Random().nextInt(10) + 20));
                    list.add(markers.get(new Random().nextInt(10) + 9));
                    List<Integer> routeList = MapUtils.getBestPathBetweenPoints(list, nodes,
                            nodesContract);
                    routeLayer.setNodeList(nodes);
                    routeLayer.setRouteList(routeList);
                    mapView.refresh();
                    break;
                default:
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mapView.isMapLoadFinish()) {
            float mapDegree = 0; // the rotate between reality map to northern
            float degree = 0f;
            float alpha = 0.97f;

            synchronized (this) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
                    mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
                    mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
                }

                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];

                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);

                        degree = (float) Math.toDegrees(orientation[0]);
                        degree = (degree + 360) % 360;
                    }
                    //Log.d("suka", "onSensorChanged: " + String.valueOf(degree));
                    locationLayer.setCompassIndicatorArrowRotateDegree(mapDegree + mapView
                            .getCurrentRotateDegrees() + degree);
                    mapView.refresh();
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onBind() {

    }

    @Override
    public void onUnbind() {

    }

    @Override
    public void onError(ScanError scanError) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Check coarse location permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            startRanging();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRanging();
                } else {
                    showErrorDialog(getString(R.string.error_permission));
                }
            }
            break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanServiceManager != null) {
            //Stop scan
            mScanServiceManager.stopScan();
            //Stop scan service
            mScanServiceManager.unbind();
            mScanServiceManager = null;
            sensorManager.unregisterListener(this);
        }
    }

    private void showErrorDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BeaconRouteActivity.this);
        builder.setTitle(R.string.error);
        builder.setMessage(error);
        builder.show();
    }

    HashMap<Integer, PointF> pointsTest = Point.getPointsMaps();

    HashMap<Integer, PointF> points = new HashMap<Integer, PointF>() {{
        put(101, new PointF(582, 1035));
        put(102, new PointF(403, 388));
        put(104, new PointF(596, 698));
    }};

    PointF position;

    private void startRanging() {
        //Create scan service manager
        mScanServiceManager = new ScanServiceManager(this, this);
        //Set foreground scan period
        mScanServiceManager.setForegroundScanPeriod(5000, 0);
        //Set scan mode
        mScanServiceManager.setScanMode(SCAN_MODE_LOW_LATENCY);
        //Set scan listener or use setRangingListener()
        mScanServiceManager.setScanListener(new ScanListener() {
            @Override
            public void onScanResult(BaseFrame baseFrame) {
                String key = baseFrame.getBluetoothDevice().getAddress();
                //int index = mFoundedDeviceMap.indexOfKey(key);
                mFoundedDeviceMap.put(key, baseFrame);
                if (baseFrame.getType() == I_BEACON && (baseFrame.getProximityZone() == NEAR || baseFrame.getProximityZone() == IMMEDIATE)) {
                    PointF temp = points.get(((IBeaconFrame) baseFrame).getMinor());
                    if (temp != null) {
                        position = temp;
                        if (!position.equals(locationLayer.getCurrentPosition())) {
                            locationLayer.setCurrentPosition(position);

                            if (mMarker != null) {
                                routeList = MapUtils.getShortestDistanceBetweenTwoPoints
                                        (position, mMarker, nodes, nodesContract);
                                routeLayer.setRouteList(routeList);
                                routeLayer.setNodeList(nodes);
                            }
                            mapView.refresh();
                        }
                    }
                }
            }
        });

        //Connect to scan service
        mScanServiceManager.bind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRotateBegin() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onRotateEnd() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }
}
