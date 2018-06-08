package kz.mobdev.mapview.data;

import android.graphics.PointF;

import java.util.HashMap;
import java.util.List;

import kz.mobdev.mapview.library.models.Marker;

public class Point {

    private int mMinor;
    private PointF mCoordinate;

    public Point(int minor, PointF coordinate) {
        mMinor = minor;
        mCoordinate = coordinate;
    }

    public int getMinor() {
        return mMinor;
    }

    public PointF getCoordinate() {
        return mCoordinate;
    }
    
    public static HashMap<Integer, PointF> getPointsMaps() {
        List<Marker> markers = TestData.getMarks();
        
        HashMap<Integer, PointF> points = new HashMap<>();
        for (int i = 0, j = 100; i < markers.size(); i++, j++) {
            points.put(j, markers.get(i));
        }

        return points;
    }
}
