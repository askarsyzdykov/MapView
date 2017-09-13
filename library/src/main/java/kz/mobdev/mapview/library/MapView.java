package kz.mobdev.mapview.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import kz.mobdev.mapview.library.layers.MapBaseLayer;
import kz.mobdev.mapview.library.layers.MapLayer;
import kz.mobdev.mapview.library.utils.MapMath;
import kz.mobdev.mapview.library.utils.MapUtils;

/**
 * MapView
 *
 * @author: onlylemi, Askar Syzdykov
 */
public class MapView extends RelativeLayout implements SurfaceHolder.Callback {

    private static final String TAG = "MapView";

    SurfaceView surfaceView;
    LinearLayout viewZoomControls;
    ImageButton btnCompass;

    private SurfaceHolder holder;
    private MapViewListener mapViewListener;
    private boolean isMapLoadFinish = false;
    private List<MapBaseLayer> layers; // all layers
    private MapLayer mapLayer;

    private Canvas canvas;

    private float minZoom = 0.5f;
    private float maxZoom = 3.0f;

    private PointF startTouch = new PointF();
    private PointF mid = new PointF();

    private Matrix saveMatrix = new Matrix();
    private Matrix currentMatrix = new Matrix();
    private float currentZoom = 1.0f;
    private float saveZoom = 0f;
    private float currentRotateDegrees = 0.0f;
    private float saveRotateDegrees = 0.0f;

    private static final int TOUCH_STATE_NO = 0; // no touch
    private static final int TOUCH_STATE_SCROLL = 1; // scroll(one point)
    private static final int TOUCH_STATE_SCALE = 2; // scale(two points)
    private static final int TOUCH_STATE_ROTATE = 3; // rotate(two points)
    private static final int TOUCH_STATE_TWO_POINTED = 4; // two points touch
    private int currentTouchState = MapView.TOUCH_STATE_NO; // default touch state

    private float oldDist = 0, oldDegree = 0;
    private boolean isScaleAndRotateTogether = false;

    private boolean isCompassButtonVisible = true;

    private GestureDetector gestureDetector;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMapView();
    }

    /**
     * init mapview
     */
    private void initMapView() {
        surfaceView = new SurfaceView(getContext());
        addView(surfaceView);

        initZoomControls();
        initCompassButton();
        //

        surfaceView.getHolder().addCallback(this);

        layers = new ArrayList<MapBaseLayer>() {
            @Override
            public boolean add(MapBaseLayer layer) {
                if (layers.size() != 0) {
                    if (layer.level >= this.get(this.size() - 1).level) {
                        super.add(layer);
                    } else {
                        for (int i = 0; i < layers.size(); i++) {
                            if (layer.level < this.get(i).level) {
                                super.add(i, layer);
                                break;
                            }
                        }
                    }
                } else {
                    super.add(layer);
                }
                return true;
            }
        };

        gestureDetector = new GestureDetector(null, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                setCurrentZoom(getCurrentZoom() + 2);
                refresh();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                if (withFloorPlan(event.getX(), event.getY())) {
                    // layers on touch
                    for (MapBaseLayer layer : layers) {
                        layer.onTouch(event);
                    }
                }
                currentTouchState = MapView.TOUCH_STATE_NO;
                return super.onSingleTapConfirmed(event);
            }
        });
    }

    private void initZoomControls() {
        viewZoomControls = new LinearLayout(getContext());
        viewZoomControls.setOrientation(LinearLayout.VERTICAL);

        ImageButton btnZoomIn = new ImageButton(getContext());
        btnZoomIn.setImageDrawable(getResources().getDrawable(R.drawable.ic_zoom_in));
        btnZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentZoom(currentZoom + 0.5f);
                refresh();
            }
        });
        viewZoomControls.addView(btnZoomIn);

        ImageButton btnZoomOut = new ImageButton(getContext());
        btnZoomOut.setImageDrawable(getResources().getDrawable(R.drawable.ic_zoom_out));
        btnZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentZoom(currentZoom - 0.5f);
                refresh();
            }
        });
        viewZoomControls.addView(btnZoomOut);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.leftMargin = 16;

        addView(viewZoomControls, params);
    }

    private void initCompassButton() {
        btnCompass = new ImageButton(getContext());
        btnCompass.setBackgroundColor(Color.TRANSPARENT);
        btnCompass.setImageDrawable(getResources().getDrawable(R.mipmap.ic_compass_direction));
        btnCompass.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentRotateDegrees(0);
                refresh();
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = 16;
        params.topMargin = 16;

        addView(btnCompass, params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
        refresh();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * show/hide zoom controls
     *
     * @param isVisible
     */
    public void setZoomControlsVisible(boolean isVisible) {
        viewZoomControls.setVisibility(isVisible ? VISIBLE : GONE);
    }

    /**
     * Returns viewZoomControls is visible
     *
     * @return <code>true</code> if the viewZoomControls is visible;
     * <code>false</code> otherwise.
     */
    public boolean isZoomControlsVisible() {
        return viewZoomControls.getVisibility() == VISIBLE;
    }

    /**
     * show/hide compass button
     *
     * @param isVisible
     */
    public void setCompassButtonVisible(boolean isVisible) {
        this.isCompassButtonVisible = isVisible;
        btnCompass.setVisibility(isVisible ? VISIBLE : GONE);
        onRotationChanged();
    }

    /**
     * Returns btnCompass is visible
     *
     * @return <code>true</code> if the btnCompass is visible;
     * <code>false</code> otherwise.
     */
    public boolean isCompassButtonVisible() {
        return isCompassButtonVisible;
    }

    /**
     * reload mapview
     */
    public void refresh() {
        if (holder != null) {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(-1);
                if (isMapLoadFinish) {
                    for (MapBaseLayer layer : layers) {
                        if (layer.isVisible) {
                            layer.draw(canvas, currentMatrix, currentZoom, currentRotateDegrees);
                        }
                    }
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void loadMap(Bitmap bitmap) {
        loadMap(MapUtils.getPictureFromBitmap(bitmap));
    }

    /**
     * load map image
     *
     * @param picture
     */
    public void loadMap(final Picture picture) {
        isMapLoadFinish = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (picture != null) {
                    if (mapLayer == null) {
                        mapLayer = new MapLayer(MapView.this);
                        // add map image layer
                        layers.add(mapLayer);
                    }
                    mapLayer.setImage(picture);
                    if (mapViewListener != null) {
                        // load map success, and callback
                        mapViewListener.onMapLoadSuccess();
                    }
                    isMapLoadFinish = true;
                    refresh();
                } else {
                    if (mapViewListener != null) {
                        mapViewListener.onMapLoadFail();
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isMapLoadFinish) {
            return false;
        }

        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        float newDist;
        float newDegree;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                saveMatrix.set(currentMatrix);
                startTouch.set(event.getX(), event.getY());
                currentTouchState = MapView.TOUCH_STATE_SCROLL;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    saveMatrix.set(currentMatrix);
                    saveZoom = currentZoom;
                    saveRotateDegrees = currentRotateDegrees;
                    startTouch.set(event.getX(0), event.getY(0));
                    currentTouchState = MapView.TOUCH_STATE_TWO_POINTED;

                    mid = midPoint(event);
                    oldDist = distance(event, mid);
                    oldDegree = rotation(event, mid);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                currentTouchState = MapView.TOUCH_STATE_NO;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (currentTouchState) {
                    case MapView.TOUCH_STATE_SCROLL:
                        currentMatrix.set(saveMatrix);
                        currentMatrix.postTranslate(event.getX() - startTouch.x, event.getY() -
                                startTouch.y);
                        refresh();
                        break;
                    case MapView.TOUCH_STATE_TWO_POINTED:
                        if (!isScaleAndRotateTogether) {
                            float x = oldDist;
                            float y = MapMath.getDistanceBetweenTwoPoints(event.getX(0),
                                    event.getY(0), startTouch.x, startTouch.y);
                            float z = distance(event, mid);
                            float cos = (x * x + y * y - z * z) / (2 * x * y);
                            float degree = (float) Math.toDegrees(Math.acos(cos));

                            if (degree < 120 && degree > 45) {
                                oldDegree = rotation(event, mid);
                                currentTouchState = MapView.TOUCH_STATE_ROTATE;
                            } else {
                                oldDist = distance(event, mid);
                                currentTouchState = MapView.TOUCH_STATE_SCALE;
                            }
                        } else {
                            currentMatrix.set(saveMatrix);
                            newDist = distance(event, mid);
                            newDegree = rotation(event, mid);

                            float rotate = newDegree - oldDegree;
                            float scale = newDist / oldDist;
                            if (scale * saveZoom < minZoom) {
                                scale = minZoom / saveZoom;
                            } else if (scale * saveZoom > maxZoom) {
                                scale = maxZoom / saveZoom;
                            }
                            currentZoom = scale * saveZoom;
                            currentRotateDegrees = (newDegree - oldDegree + currentRotateDegrees)
                                    % 360;
                            currentMatrix.postScale(scale, scale, mid.x, mid.y);
                            currentMatrix.postRotate(rotate, mid.x, mid.y);
                            refresh();
                        }
                        break;
                    case MapView.TOUCH_STATE_SCALE:
                        currentMatrix.set(saveMatrix);
                        newDist = distance(event, mid);
                        float scale = newDist / oldDist;
                        if (scale * saveZoom < minZoom) {
                            scale = minZoom / saveZoom;
                        } else if (scale * saveZoom > maxZoom) {
                            scale = maxZoom / saveZoom;
                        }
                        currentZoom = scale * saveZoom;
                        currentMatrix.postScale(scale, scale, mid.x, mid.y);
                        refresh();
                        break;
                    case MapView.TOUCH_STATE_ROTATE:
                        currentMatrix.set(saveMatrix);
                        newDegree = rotation(event, mid);
                        float rotate = newDegree - oldDegree;
                        currentRotateDegrees = (rotate + saveRotateDegrees) % 360;
                        currentRotateDegrees = currentRotateDegrees > 0 ? currentRotateDegrees :
                                currentRotateDegrees + 360;
                        currentMatrix.postRotate(rotate, mid.x, mid.y);

                        btnCompass.animate().rotation(currentRotateDegrees).setInterpolator(new AccelerateDecelerateInterpolator());
                        onRotationChanged();
                        refresh();
//                        Log.i(TAG, "rotate:" + currentRotateDegrees);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * set mapview listener
     *
     * @param mapViewListener
     */
    public void setMapViewListener(MapViewListener mapViewListener) {
        this.mapViewListener = mapViewListener;
    }

    /**
     * convert coordinate of map to coordinate of screen
     *
     * @param x
     * @param y
     * @return
     */
    public float[] convertMapXYToScreenXY(float x, float y) {
        Matrix invertMatrix = new Matrix();
        float[] value = {x, y};
        currentMatrix.invert(invertMatrix);
        invertMatrix.mapPoints(value);
        return value;
    }

    /**
     * map is/not load finish
     *
     * @return
     */
    public boolean isMapLoadFinish() {
        return isMapLoadFinish;
    }

    /**
     * add layer
     *
     * @param layer
     */
    public void addLayer(MapBaseLayer layer) {
        if (layer != null) {
            layers.add(layer);
        }
    }

    /**
     * get all layers
     *
     * @return
     */
    public List<MapBaseLayer> getLayers() {
        return layers;
    }

    public void translate(float x, float y) {
        currentMatrix.postTranslate(x, y);
    }

    /**
     * set point to map center
     *
     * @param x
     * @param y
     */
    public void mapCenterWithPoint(float x, float y) {
        float[] goal = {x, y};
        currentMatrix.mapPoints(goal);

        float deltaX = getWidth() / 2 - goal[0];
        float deltaY = getHeight() / 2 - goal[1];
        currentMatrix.postTranslate(deltaX, deltaY);
    }

    public float getCurrentRotateDegrees() {
        return currentRotateDegrees;
    }

    /**
     * set rotate degrees
     *
     * @param degrees
     */
    public void setCurrentRotateDegrees(float degrees) {
        mapCenterWithPoint(getMapWidth() / 2, getMapHeight() / 2);
        setCurrentRotateDegrees(degrees, getWidth() / 2, getHeight() / 2);
    }

    /**
     * set rotate degrees
     *
     * @param degrees
     * @param x
     * @param y
     */
    public void setCurrentRotateDegrees(float degrees, float x, float y) {
        currentMatrix.postRotate(degrees - currentRotateDegrees, x, y);

        currentRotateDegrees = degrees % 360;
        currentRotateDegrees = currentRotateDegrees > 0 ? currentRotateDegrees :
                currentRotateDegrees + 360;

        onRotationChanged();
    }

    private void onRotationChanged() {
        if (isCompassButtonVisible) {
            btnCompass.setVisibility(currentRotateDegrees == 360 ? GONE : VISIBLE);
        }
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public boolean isScaleAndRotateTogether() {
        return isScaleAndRotateTogether;
    }

    /**
     * setting scale&rotate is/not together on touch
     *
     * @param scaleAndRotateTogether
     */
    public void setScaleAndRotateTogether(boolean scaleAndRotateTogether) {
        isScaleAndRotateTogether = scaleAndRotateTogether;
    }

    public void setMaxZoom(float maxZoom) {
        if (maxZoom <= minZoom) {
            throw new IllegalArgumentException("MaxZoom cannot be less than minZoom");
        }
        this.maxZoom = maxZoom;
    }

    public void setMinZoom(float minZoom) {
        if (minZoom >= maxZoom) {
            throw new IllegalArgumentException("MinZoom cannot be greater than maxZoom");
        }
        this.minZoom = minZoom;
    }

    public void setCurrentZoom(float zoom) {
        setCurrentZoom(zoom, getWidth() / 2, getHeight() / 2);
    }

    public void setCurrentZoom(float zoom, float x, float y) {
        if (zoom < minZoom) {
            zoom = minZoom;
        }
        if (zoom > maxZoom) {
            zoom = maxZoom;
        }
        currentMatrix.postScale(zoom / this.currentZoom, zoom / this.currentZoom, x, y);
        this.currentZoom = zoom;
    }

    private PointF midPoint(MotionEvent event) {
        return MapMath.getMidPointBetweenTwoPoints(event.getX(0), event.getY(0)
                , event.getX(1), event.getY(1));
    }

    private float distance(MotionEvent event, PointF mid) {
        return MapMath.getDistanceBetweenTwoPoints(event.getX(0), event.getY(0)
                , mid.x, mid.y);
    }

    private float rotation(MotionEvent event, PointF mid) {
        return MapMath.getDegreeBetweenTwoPoints(event.getX(0), event.getY(0)
                , mid.x, mid.y);
    }

    /**
     * point is/not in floor plan
     *
     * @param x
     * @param y
     * @return
     */
    public boolean withFloorPlan(float x, float y) {
        float[] goal = convertMapXYToScreenXY(x, y);
        return goal[0] > 0 && goal[0] < mapLayer.getImage().getWidth() && goal[1] > 0
                && goal[1] < mapLayer.getImage().getHeight();
    }

    public float getMapWidth() {
        return mapLayer.getImage().getWidth();
    }

    public float getMapHeight() {
        return mapLayer.getImage().getHeight();
    }

}
