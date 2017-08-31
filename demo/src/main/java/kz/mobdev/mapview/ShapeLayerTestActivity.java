package kz.mobdev.mapview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.onlylemi.mapview.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.ShapeLayer;
import kz.mobdev.mapview.library.models.shapes.Polygon;
import kz.mobdev.mapview.library.models.shapes.Shape;
import kz.mobdev.mapview.library.models.shapes.Rect;

public class ShapeLayerTestActivity extends AppCompatActivity {

    private static final String TAG = "ShapeLayerTestActivity";

    private MapView mapView;
    private ShapeLayer shapeLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layer_test);

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
                Log.i(TAG, "onMapLoadSuccess");
                List<PointF> pointsFirst = new ArrayList<PointF>() {
                    {
                        add(new PointF(740, 290));
                    }

                    {
                        add(new PointF(940, 290));
                    }

                    {
                        add(new PointF(940, 450));
                    }

                    {
                        add(new PointF(740, 450));
                    }

                };
                List<PointF> pointsSecond = new ArrayList<PointF>() {
                    {
                        add(new PointF(864, 42));
                    }

                    {
                        add(new PointF(881, 76));
                    }

                    {
                        add(new PointF(918, 81));
                    }

                    {
                        add(new PointF(892, 107));
                    }

                    {
                        add(new PointF(898, 141));
                    }

                    {
                        add(new PointF(864, 125));
                    }

                    {
                        add(new PointF(831, 141));
                    }

                    {
                        add(new PointF(837, 107));
                    }

                    {
                        add(new PointF(812, 81));
                    }

                    {
                        add(new PointF(847, 76));
                    }

                };
                shapeLayer = new ShapeLayer(mapView, new ShapeLayer.OnPolygonClickListener() {
                    @Override
                    public void onPolygonClick(Shape polygon) {
                        Toast.makeText(ShapeLayerTestActivity.this, polygon.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
                shapeLayer.addShape(new Polygon(pointsFirst));

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.YELLOW);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                shapeLayer.addShape(new Polygon("", pointsSecond));
                shapeLayer.addShape(new Rect(new RectF(10f, 100f, 200f, 200f), ""));

                mapView.addLayer(shapeLayer);
                mapView.refresh();
            }

            @Override
            public void onMapLoadFail() {
                Log.i(TAG, "onMapLoadFail");
            }

        });
        mapView.loadMap(bitmap);
    }
}
