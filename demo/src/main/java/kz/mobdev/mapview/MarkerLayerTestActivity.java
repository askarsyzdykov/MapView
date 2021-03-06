package kz.mobdev.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.MarkerLayer;
import kz.mobdev.mapview.library.models.Marker;

public class MarkerLayerTestActivity extends AppCompatActivity {

    private static final String TAG = "MarkLayerTestActivity";

    private MapView mapView;
    private MarkerLayer markerLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_layer_test);

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerLayer.clear();
            }
        });

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

                List<Marker> markers = new ArrayList<>();
                markers.add(Marker.newBuilder()
                        .setTitle("First")
                        .setPosition(new PointF(300, 185))
                        .build());
                markers.add(Marker.newBuilder()
                        .setTitle("Second. Red")
                        .setPosition(new PointF(172.5f, 65))
                        .setSelectedIcon(BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.end_point))
                        .build());
                markers.add(Marker.newBuilder()
                        .setTitle("Third. Green")
                        .setPosition(new PointF(45, 185))
                        .setSelectedIcon(BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.start_point))
                        .build());

                markerLayer = new MarkerLayer(mapView, markers);
                markerLayer.setDefaultIcon(BitmapFactory.decodeResource(mapView.getResources(), kz.mobdev.mapview.library.R.mipmap.mark));
                markerLayer.setDefaultSelectedIcon(BitmapFactory.decodeResource(mapView.getResources(), kz.mobdev.mapview.library.R.mipmap.mark_touch));
                markerLayer.setOnMarkerClickListener(new MarkerLayer.OnMarkerClickListener() {
                    @Override
                    public void onMarkerClick(Marker marker) {
                        Toast.makeText(getApplicationContext(), marker.getTitle() + " is " +
                                "selected", Toast.LENGTH_SHORT).show();
                    }
                });
                markerLayer.addMarker(Marker.newBuilder()
                        .setTitle("Fourth")
                        .setPosition(new PointF(505, 185))
                        .build());
                mapView.addLayer(markerLayer);
            }

            @Override
            public void onMapLoadFail() {
                Log.d(TAG, "onMapLoadFail");
            }
        });
        mapView.loadMap(bitmap);
    }
}
