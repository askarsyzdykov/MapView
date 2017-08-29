package kz.mobdev.mapview.models;


import android.graphics.Bitmap;
import android.graphics.PointF;
import android.text.TextUtils;

public class Marker extends PointF {

    private Object object;
    private String title;
    private Bitmap icon;
    private Bitmap selectedIcon;

    private Marker() {

    }

    public Object getObject() {
        return object;
    }

    public String getTitle() {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        if (object != null) {
            return object.toString();
        }
        return "";
    }

    public Bitmap getIcon() {
        return icon;
    }

    public Bitmap getSelectedIcon() {
        return selectedIcon;
    }

    public static Builder newBuilder() {
        return new Marker().new Builder();
    }

    public class Builder {

        private Builder() {
            // private constructor
        }

        public Builder setTitle(String title) {
            Marker.this.title = title;

            return this;
        }

        public Builder setIcon(Bitmap icon) {
            Marker.this.icon = icon;

            return this;
        }

        public Builder setSelectedIcon(Bitmap selectedIcon) {
            Marker.this.selectedIcon = selectedIcon;

            return this;
        }

        public Builder setPosition(float x, float y) {
            Marker.this.x = x;
            Marker.this.y = y;

            return this;
        }

        public Builder setPosition(PointF position) {
            return setPosition(position.x, position.y);
        }

        public Marker build() {
            return Marker.this;
        }

    }
}
