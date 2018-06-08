package kz.mobdev.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kz.mobdev.mapview.data.TestData;
import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.MarkerLayer;
import kz.mobdev.mapview.library.layers.RouteLayer;
import kz.mobdev.mapview.library.models.Marker;
import kz.mobdev.mapview.library.utils.MapUtils;

public class RouteLayerTestActivity extends AppCompatActivity {

    private static final String TAG = "RouteLayerTestActivity";
    private MapView mapView;

    private MarkerLayer markLayer;
    private RouteLayer routeLayer;

    private List<PointF> nodes;
    private List<PointF> nodesContract;
    private List<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_layer_test);

        nodes = TestData.getNodesList();
        nodesContract = TestData.getNodesContactList();
        markers = TestData.getMarks();
        MapUtils.init(nodes.size(), nodesContract.size());

        mapView = (MapView) findViewById(R.id.mapview);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("map.png"));
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
                        List<Integer> routeList = MapUtils.getShortestDistanceBetweenTwoPoints
                                (markers.get(markers.size() - 1), marker, nodes, nodesContract);
                        routeLayer.setNodeList(nodes);
                        routeLayer.setRouteList(routeList);
                        mapView.refresh();
                    }
                });
                mapView.refresh();
            }

            @Override
            public void onMapLoadFail() {
                Log.d(TAG, "onMapLoadFail");
            }

        });
        mapView.loadMap(bitmap);
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
}
