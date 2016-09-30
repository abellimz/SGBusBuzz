package com.abellimz.sgbusbuzz.utils;

import android.graphics.PointF;
import android.location.Location;

/**
 * Created by Abel on 9/16/2016.
 */

public class MapUtils {

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point   Point of origin
     * @param range   Range in meters
     * @param bearing Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static PointF calculateDerivedPosition(PointF point,
                                                  double range, double bearing) {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        PointF newPoint = new PointF((float) lat, (float) lon);

        return newPoint;
    }

    public static int calcDistance(double fromLat, double fromLon, double toLat, double toLon) {
//        double radius = 6378137;   // approximate Earth radius, *in meters*
//        double deltaLat = toLat - fromLat;
//        double deltaLon = toLon - fromLon;
//        double angle = 2 * Math.asin( Math.sqrt(
//                Math.pow(Math.sin(deltaLat/2), 2) +
//                        Math.cos(fromLat) * Math.cos(toLat) *
//                                Math.pow(Math.sin(deltaLon/2), 2) ) );
//        return (int)(radius * angle);
        Location loc1 = new Location("");
        loc1.setLatitude(fromLat);
        loc1.setLongitude(fromLon);

        Location loc2 = new Location("");
        loc2.setLatitude(toLat);
        loc2.setLongitude(toLon);

        return (int) loc1.distanceTo(loc2);
    }

}
