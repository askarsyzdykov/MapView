package kz.mobdev.mapview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.leantegra.wibeat.sdk.monitoring.ScanServiceManager;
import com.leantegra.wibeat.sdk.monitoring.info.IBeaconFrame;
import com.leantegra.wibeat.sdk.monitoring.listeners.ScanServiceConsumer;
import com.leantegra.wibeat.sdk.monitoring.service.ScanError;
import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import kz.mobdev.mapview.data.DataForBeacons;
import kz.mobdev.mapview.data.Point;
import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.LocationLayer;
import kz.mobdev.mapview.library.layers.MarkerLayer;
import kz.mobdev.mapview.library.layers.RouteLayer;
import kz.mobdev.mapview.library.models.Marker;
import kz.mobdev.mapview.library.utils.MapUtils;
import kz.mobdev.mapview.utils.SensorEventObservableFactory;
import kz.mobdev.mapview.utils.TupleSensorEvent;

import static com.leantegra.wibeat.sdk.monitoring.config.ScanConfig.SCAN_MODE_LOW_LATENCY;
import static com.leantegra.wibeat.sdk.monitoring.distance.ProximityZone.IMMEDIATE;
import static com.leantegra.wibeat.sdk.monitoring.distance.ProximityZone.NEAR;
import static com.leantegra.wibeat.sdk.monitoring.info.FrameType.I_BEACON;

public class BeaconRouteActivity extends AppCompatActivity implements ScanServiceConsumer {

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

    float[] mGravity = new float[3];
    float[] mGeomagnetic = new float[3];

    Observable<Integer> sensorEventObservable;
    Disposable sensorEventDisposable;

    float currentRoute;

    enum Direction {
        NORTH,
        SOUTH,
        WEST,
        EAST
    }

    Integer clockToSecondMark;

    private Direction mDirectionToWatch;
    private Direction mDirectionOfSecondMarker;

    MediaPlayer mPlayer;
    AudioManager mAudioManager;

    AssetManager mAssetManager;
    AssetFileDescriptor fileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_layer_test);

        Observable<SensorEvent> o1 = createAccelerationObservable(this, Sensor.TYPE_ACCELEROMETER);
        Observable<SensorEvent> o2 = createAccelerationObservable(this, Sensor.TYPE_MAGNETIC_FIELD);

        sensorEventObservable = Observable.zip(o1, o2, TupleSensorEvent::new)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::getDegree)
                .filter(degree -> degree != 0f)
                .map(Float::intValue)
                .distinctUntilChanged();

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

                locationLayer = new LocationLayer(mapView, null);
                locationLayer.setOpenCompass(true);
                mapView.addLayer(locationLayer);

                markLayer.setOnMarkerClickListener(marker -> {
                    mMarker = marker;
                    if (position != null) {
                        routeList = MapUtils.getShortestDistanceBetweenTwoPoints
                                (position, mMarker, nodes, nodesContract);
                        routeLayer.setRouteList(routeList);
                        routeLayer.setNodeList(nodes);

                    }

                });


                mapView.refresh();

                sensorEventDisposable = sensorEventObservable.subscribe(
                        sensorEvent -> updateCompass(sensorEvent), throwable -> onFail(throwable)
                );
            }

            @Override
            public void onMapLoadFail() {
                Log.d(TAG, "onMapLoadFail");
            }

        });
        //mapView.loadMap(AssetsHelper.getContent(this, "sample2.svg"));
        mapView.loadMap(bitmap);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mAssetManager = this.getAssets();

        mPlayer = new MediaPlayer();
    }

    private void onFail(Throwable t) {
        Log.d("RXError", t.getClass().getName());
    }

    private void releaseMP() {
        if (mPlayer != null) {
            try {
                mPlayer.release();
                //mPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCompass(int degree) {
        // +250 degrees for changing direction to real
        currentRoute = mapView.getCurrentRotateDegrees() + degree + 250;

        Integer directionClock = degreeToClock();

        if (mMarker != null) {
            if ((mMarker.x == locationLayer.getCurrentPosition().x) && (mMarker.y < locationLayer.getCurrentPosition().y))
                //mDirectionOfSecondMarker = Direction.EAST;
                clockToSecondMark = 3;
            else if ((mMarker.x == locationLayer.getCurrentPosition().x) && (mMarker.y > locationLayer.getCurrentPosition().y))
                //mDirectionOfSecondMarker = Direction.WEST;
                clockToSecondMark = 10;
            else if ((mMarker.x > locationLayer.getCurrentPosition().x) && (mMarker.y == locationLayer.getCurrentPosition().y))
                //mDirectionOfSecondMarker = Direction.SOUTH;
                clockToSecondMark = 7;
            else if ((mMarker.x < locationLayer.getCurrentPosition().x) && (mMarker.y == locationLayer.getCurrentPosition().y))
                //mDirectionOfSecondMarker = Direction.NORTH;
                clockToSecondMark = 0;
        }

        if (clockToSecondMark != null) {
            int a = clockToSecondMark - directionClock;
            if (a < 0) {
                a = 12 - Math.abs(a);
            }
            playAudio(a + ".wav");
        }

        Log.d("degree2", String.valueOf(currentRoute));

        locationLayer.setCompassIndicatorArrowRotateDegree(currentRoute);
        mapView.refresh();
    }


    private Integer degreeToClock() {
        Integer directionClock;
        float realDegree = currentRoute - 250;
        if (realDegree == 360) {
            directionClock = 0;
        } else {
            directionClock = (int) realDegree / 30;
        }
        return directionClock;
    }

    private void playAudio(String audioName) {
        try {
            if (!mPlayer.isPlaying()) {
                fileDescriptor = mAssetManager.openFd(audioName);
                mPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(MediaPlayer::reset);
                mPlayer.setLooping(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        if (sensorEventDisposable != null && sensorEventDisposable.isDisposed()) {
            sensorEventDisposable = sensorEventObservable.subscribe(this::updateCompass);
        }
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
        }

        if (sensorEventDisposable != null) {
            sensorEventDisposable.dispose();
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
        put(104, new PointF(582, 698));
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
        mScanServiceManager.setScanListener(baseFrame -> {
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
        });

        //Connect to scan service
        mScanServiceManager.bind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorEventDisposable != null) {
            sensorEventDisposable.dispose();
        }
        releaseMP();
    }

    private float getDegree(TupleSensorEvent tuple) {
        SensorEvent accelerometerEvent = tuple.getFirst();
        SensorEvent magneticEvent = tuple.getSecond();

//        Log.d("LocationLayerTestActivity", accelerometerEvent.sensor.getName() + " " + Arrays.toString(accelerometerEvent.values));
//        Log.d("LocationLayerTestActivity", magneticEvent.sensor.getName() + " " + Arrays.toString(magneticEvent.values));

        final float[] degree = {0f};
        float alpha = 0.97f;

        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * accelerometerEvent.values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * accelerometerEvent.values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * accelerometerEvent.values[2];

        mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * magneticEvent.values[0];
        mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * magneticEvent.values[1];
        mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * magneticEvent.values[2];

        float R[] = new float[9];
        float I[] = new float[9];

        boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);

            degree[0] = ((float) Math.toDegrees(orientation[0]));
            Log.d("rawDegreeBefore", String.valueOf((float) Math.toDegrees(orientation[0])));
            degree[0] = (degree[0] + 360) % 360;
            Log.d("rawDegree360", String.valueOf((degree[0] + 360) % 360));
        }

        Log.d("rawDegreeAfter", String.valueOf(degree[0]));
        return degree[0];
    }

    @NonNull
    private static Observable<SensorEvent> createAccelerationObservable(@NonNull Context context, int sensorType) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(sensorType);
        if (sensors.isEmpty()) {
            throw new IllegalStateException("Device has no linear acceleration sensor");
        }

        return SensorEventObservableFactory.createSensorEventObservable(sensors, sensorManager);
    }
}
