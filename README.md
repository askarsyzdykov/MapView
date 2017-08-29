# MapView

Forked from [https://github.com/onlylemi/MapView](https://github.com/onlylemi/MapView)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-MapView-green.svg?style=true)](https://android-arsenal.com/details/1/3497)
[![jitpack](https://img.shields.io/badge/jitpack-v1.0-green.svg)](https://jitpack.io/#onlylemi/mapview)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://github.com/onlylemi/MapView/blob/master/LICENSE)

This a indoor map view named MapView for `Android`. It also offer some layers. If you are doing a indoor map application and try to do it.

## What's changed?

* Marker
   * Instead using PointF class in some places
* MapView
   * Double click for zooming in
* MarkLayer
   * Renamed to MarkerLayer
   * New method 'addMarker(Marker)'
   * New method 'deleteMarker(Marker)'
   * New method 'clear()'
   * Custom selected marker icon

## Layers

* MapLayer
    * rotate
    * scale
    * slide
* LocationLayer
    * Sensor
* BitmapLayer
* MarkerLayer
* RouteLayer
    * ShortestPath By [FloydAlgorithm](https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm)
    * BestPath By [GeneticAlgorithm](https://en.wikipedia.org/wiki/Genetic_algorithm)， and you also look [here](https://github.com/onlylemi/GeneticTSP).

## Demo

I offer every layer demo and you can look the [demo](https://github.com/askarsyzdykov/MapView/tree/master/demo) folder. And the following is a screenshot of demo.

![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_1.gif)
![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_2.gif)
![](https://raw.githubusercontent.com/onlylemi/notes/master/images/android_mapview_3.gif)

## Usage

Include this project as module in your project

## About me

Welcome to pull [requests](https://github.com/askarsyzdykov/MapView/pulls).  

If you have any new idea about this project, feel free to [contact me](mailto:askar.syzdykov@gmail.com).
