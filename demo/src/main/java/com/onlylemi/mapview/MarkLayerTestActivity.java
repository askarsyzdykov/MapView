package com.onlylemi.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.MapViewListener;
import com.onlylemi.mapview.library.layers.MarkerLayer;
import com.onlylemi.mapview.library.models.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarkLayerTestActivity extends AppCompatActivity {

    private MapView mapView;
    private MarkerLayer markLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_layer_test);

        mapView = (MapView) findViewById(R.id.mapview);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapView.loadMap(bitmap);
        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onMapLoadSuccess() {
                List<Marker> markers = new ArrayList<>();

                markers.add(new Marker(300, 185, "First"));
                markers.add(new Marker(172.5f, 65, "Second"));
                markers.add(new Marker(45, 185, "Third"));

                markLayer = new MarkerLayer(mapView, markers);
                markLayer.setOnMarkerClickListener(new MarkerLayer.OnMarkerClickListener() {
                    @Override
                    public void onMarkerClick(Marker marker) {
                        Toast.makeText(getApplicationContext(), marker.getTitle() + " is " +
                                "selected", Toast.LENGTH_SHORT).show();
                    }
                });
                mapView.addLayer(markLayer);
                mapView.refresh();
            }

            @Override
            public void onMapLoadFail() {

            }

        });
    }
}
