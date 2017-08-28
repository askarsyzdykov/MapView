package com.onlylemi.mapview.library.models;


import android.graphics.PointF;
import android.text.TextUtils;

public class Marker extends PointF {

    private Object object;
    private String title;

    public Marker(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Marker(float x, float y, String title) {
        this.x = x;
        this.y = y;
        this.title = title;
    }

    public Marker(float x, float y, Object object) {
        this.x = x;
        this.y = y;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getTitle() {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        if (object != null) {
            return object.toString();
        }
        return this.toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
