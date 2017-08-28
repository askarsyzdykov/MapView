package com.onlylemi.mapview.library.layers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.onlylemi.mapview.library.MapView;
import com.onlylemi.mapview.library.models.Marker;
import com.onlylemi.mapview.library.utils.MapMath;
import com.onlylemi.mapview.library.R;

import java.util.List;

/**
 * MarkLayer
 *
 * @author: onlylemi
 */
public class MarkerLayer extends MapBaseLayer {

    private List<Marker> marks;
    private OnMarkerClickListener listener;

    private Bitmap bmpMark, bmpMarkTouch;

    private float radiusMark;
    private boolean isClickMark = false;
    private int num = -1;

    private Paint paint;

    public MarkerLayer(MapView mapView) {
        this(mapView, null);
    }

    public MarkerLayer(MapView mapView, List<Marker> marks) {
        super(mapView);
        this.marks = marks;

        initLayer();
    }

    private void initLayer() {
        radiusMark = setValue(10f);

        bmpMark = BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.mark);
        bmpMarkTouch = BitmapFactory.decodeResource(mapView.getResources(), R.mipmap.mark_touch);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void onTouch(MotionEvent event) {
        if (marks != null) {
            if (!marks.isEmpty()) {
                float[] goal = mapView.convertMapXYToScreenXY(event.getX(), event.getY());
                for (int i = 0; i < marks.size(); i++) {
                    if (MapMath.getDistanceBetweenTwoPoints(goal[0], goal[1],
                            marks.get(i).x - bmpMark.getWidth() / 2, marks.get(i).y - bmpMark
                                    .getHeight() / 2) <= 50) {
                        num = i;
                        isClickMark = true;
                        break;
                    }

                    if (i == marks.size() - 1) {
                        isClickMark = false;
                    }
                }
            }

            if (listener != null && isClickMark) {
                listener.onMarkerClick(marks.get(num));
                mapView.refresh();
            }
        }
    }

    @Override
    public void draw(Canvas canvas, Matrix currentMatrix, float currentZoom, float currentRotateDegrees) {
        if (isVisible && marks != null) {
            canvas.save();
            if (!marks.isEmpty()) {
                for (int i = 0; i < marks.size(); i++) {
                    PointF mark = marks.get(i);
                    float[] goal = {mark.x, mark.y};
                    currentMatrix.mapPoints(goal);

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(radiusMark);
                    //mark name
                    if (mapView.getCurrentZoom() > 1.0) {
                        canvas.drawText(marks.get(i).getTitle(), goal[0] - radiusMark, goal[1] -
                                radiusMark / 2, paint);
                    }
                    //mark ico
                    float left = goal[0] - bmpMark.getWidth() / 2;
                    float top = goal[1] - bmpMark.getHeight() / 2;
                    canvas.drawBitmap(bmpMark, left, top, paint);
                    if (i == num && isClickMark) {
                        canvas.drawBitmap(bmpMarkTouch, goal[0] - bmpMarkTouch.getWidth() / 2,
                                goal[1] - bmpMarkTouch.getHeight(), paint);
                    }
                }
            }
            canvas.restore();
        }
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<Marker> getMarks() {
        return marks;
    }

    public void setMarks(List<Marker> marks) {
        this.marks = marks;
    }

    public boolean isClickMark() {
        return isClickMark;
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.listener = listener;
    }

    public interface OnMarkerClickListener {
        void onMarkerClick(Marker marker);
    }
}
