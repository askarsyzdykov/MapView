package kz.mobdev.mapview.library.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.models.shapes.Shape;

/**
 * ShapeLayer
 *
 * @author: Askar Syzdykov
 */
public class ShapeLayer extends MapBaseLayer {

    private final String TAG = "PolygonLayer";

    private Paint defaultPaint;

    private List<Shape> shapes;

    private OnPolygonClickListener listener;

    public ShapeLayer(MapView mapView) {
        this(mapView, null);
    }

    public ShapeLayer(MapView mapView, OnPolygonClickListener listener) {
        super(mapView);
        this.listener = listener;

        initLayer();
    }

    private void initLayer() {
        shapes = new ArrayList<>();

        defaultPaint = new Paint();
        defaultPaint.setAntiAlias(true);
        defaultPaint.setColor(Color.BLUE);
        defaultPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setDefaultPaint(Paint paint) {
        if (paint == null) {
            throw new NullPointerException("Paint can't be null");
        }
        this.defaultPaint = paint;
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
    }

    public void setOnPolygonClickListener(OnPolygonClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onTouch(MotionEvent event) {
        for (Shape polygon : shapes) {
            float[] goal = mapView.convertMapXYToScreenXY(event.getX(), event.getY());
            if (polygon.contains(new PointF(goal[0], goal[1]))) {
                Log.d(TAG, "Click in polygon");
                if (listener != null) {
                    listener.onPolygonClick(polygon);
                }
                break;
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix currentMatrix, float currentZoom, float
            currentRotateDegrees) {
        if (isVisible && shapes != null) {
            canvas.save();
            for (Shape shape : shapes) {
                shape.draw(canvas, currentMatrix);
            }

            canvas.restore();
        }
    }

    public interface OnPolygonClickListener {
        void onPolygonClick(Shape polygon);
    }
}
