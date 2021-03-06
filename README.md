# MapView

Forked from [https://github.com/onlylemi/MapView](https://github.com/onlylemi/MapView)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-MapView-green.svg?style=true)](https://android-arsenal.com/details/1/3497)
[![jitpack](https://img.shields.io/badge/jitpack-v1.0-green.svg)](https://jitpack.io/#onlylemi/mapview)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://github.com/onlylemi/MapView/blob/master/LICENSE)

This a indoor map view named MapView for `Android`. It also offer some layers. If you are doing a indoor map application and try to do it.

## What's new and changed?
* Marker
   * Instead using PointF class in some places
* MapView
   * Double click for zoom in
   * Added zooming controls
   * Added compass button for rotate the map to 0°
* MarkLayer
   * Renamed to MarkerLayer
   * New method 'addMarker(Marker)'
   * New method 'deleteMarker(Marker)'
   * New method 'clear()'
   * Custom selected marker icon
* ShapeLayer
   * Shape - abstract class
   * Rect
   * Polygon

## Layers

* MapLayer
    * rotate
    * scale
    * slide
* LocationLayer
    * Sensor
* BitmapLayer
* MarkerLayer
* ShapeLayer
* RouteLayer
    * ShortestPath By [FloydAlgorithm](https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm)
    * BestPath By [GeneticAlgorithm](https://en.wikipedia.org/wiki/Genetic_algorithm)， and you also look [here](https://github.com/onlylemi/GeneticTSP).

## Demo

I offer every layer demo and you can look the [demo](https://github.com/askarsyzdykov/MapView/tree/master/demo) folder. And the following is a screenshot of demo.

![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_1.gif)
![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_2.gif)
![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_3.gif)

## Usage

1) Include this project as module in your project
2) Put in your layout

```xml
<kz.mobdev.mapview.library.MapView
    android:id="@+id/mapView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    />
```
3) Add this code in Activity/Fragment
```java
MapView mapView = (MapView) findViewById(R.id.mapview);
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
        
    }

    @Override
    public void onMapLoadFail() {
        
    }

});
```
4) Enjoy!

## About me

Welcome to pull [requests](https://github.com/askarsyzdykov/MapView/pulls).  

If you have any new idea about this project, feel free to [contact me](mailto:askar.syzdykov@gmail.com).
