package kz.mobdev.mapview.library.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

import kz.mobdev.mapview.library.MapView;
import kz.mobdev.mapview.library.R;
import kz.mobdev.mapview.library.models.Marker;
import kz.mobdev.mapview.library.utils.MapMath;

import java.util.ArrayList;
import java.util.List;

/**
 * MarkLayer
 *
 * @author: onlylemi
 */
public class MarkerLayer extends MapBaseLayer {

    private List<Marker> markers;
    private OnMarkerClickListener onMarkerClickListener;

    private Bitmap defaultIcon;
    private Bitmap defaultSelectedIcon;

    private float radiusMark;

    private Marker selectedMarker;

    private Paint paint;

    public MarkerLayer(MapView mapView) {
        this(mapView, new ArrayList<Marker>());
    }

    public MarkerLayer(MapView mapView, List<Marker> markers) {
        super(mapView);
        this.markers = markers;

        initLayer();
    }

    private void initLayer() {
        radiusMark = setValue(10f);

        defaultIcon = BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.mark);
        defaultSelectedIcon = BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.mark_touch);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void onTouch(MotionEvent event) {
        if (markers != null) {
            if (!markers.isEmpty()) {
                float[] goal = mapView.convertMapXYToScreenXY(event.getX(), event.getY());
                selectedMarker = null;
                for (Marker marker : markers) {
                    Bitmap icon = marker.getIcon() == null ? defaultIcon : marker.getIcon();
                    if (MapMath.getDistanceBetweenTwoPoints(goal[0], goal[1],
                            marker.x - icon.getWidth() / 2, marker.y - icon.getHeight() / 2) <= 50) {
                        selectedMarker = marker;
                        break;
                    }
                }
            }

            if (onMarkerClickListener != null && selectedMarker != null) {
                onMarkerClickListener.onMarkerClick(selectedMarker);
            }
            mapView.refresh();
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix currentMatrix, float currentZoom, float currentRotateDegrees) {
        if (isVisible && markers != null) {
            canvas.save();
            if (!markers.isEmpty()) {
                for (Marker marker : markers) {
                    float[] goal = {marker.x, marker.y};
                    currentMatrix.mapPoints(goal);

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(radiusMark);
                    //marker title
                    if (mapView.getCurrentZoom() > 1.0) {
                        canvas.drawText(marker.getTitle(), goal[0] - radiusMark, goal[1] -
                                radiusMark / 2, paint);
                    }
                    //marker icon
                    Bitmap icon = marker.getIcon() == null ? defaultIcon : marker.getIcon();
                    float left = goal[0] - icon.getWidth() / 2;
                    float top = goal[1] - icon.getHeight() / 2;
                    canvas.drawBitmap(icon, left, top, paint);

                    if (selectedMarker != null && selectedMarker == marker) {
                        Bitmap selectedIcon = selectedMarker.getSelectedIcon() == null ? defaultSelectedIcon : selectedMarker.getSelectedIcon();
                        canvas.drawBitmap(selectedIcon, goal[0] - selectedIcon.getWidth() / 2,
                                goal[1] - selectedIcon.getHeight(), paint);
                    }
                }
            }
            canvas.restore();
        }
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public Marker getSelectedMarker() {
        return selectedMarker;
    }

    public void addMarker(Marker marker) {
        markers.add(marker);
    }

    public void deleteMarker(Marker marker) {
        markers.remove(marker);
    }

    public void clear() {
        markers.clear();
        selectedMarker = null;
        mapView.refresh();
    }

    public void setDefaultIcon(Bitmap defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    public void setDefaultSelectedIcon(Bitmap defaultSelectedIcon) {
        this.defaultSelectedIcon = defaultSelectedIcon;
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.onMarkerClickListener = listener;
    }

    public interface OnMarkerClickListener {
        void onMarkerClick(Marker marker);
    }
}
