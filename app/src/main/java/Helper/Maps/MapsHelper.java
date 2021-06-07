package Helper.Maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Feature support class for displaying events on maps
 */
public class MapsHelper {

    /**
     * Return the distance in meters between two coordinate
     *
     * @param l1 first coordinate
     * @param l2 second coordinate
     * @return distance in meters
     */
    public static final double calcDistance(LatLng l1, LatLng l2){
        Location loc1 = new Location("Location l1");
        Location loc2 = new Location("Location l2");

        loc1.setLatitude(l1.latitude);
        loc1.setLongitude(l1.longitude);

        loc2.setLatitude(l2.latitude);
        loc2.setLongitude(l2.longitude);

        return loc1.distanceTo(loc2);
    }

    /**
     *  Calculate the boundaries of a square area around a reference point
     *
     * @param refPoint point in the middle of the square area
     * @param radius radius of the square area
     * @return an object of class LocationBoundaries with all the boundaries.
     */
    public static final LocationBoundaries calcBoundaries(LatLng refPoint, int radius){
        LocationBoundaries boundaries = new LocationBoundaries();
        double upperLatitude = 0;

        //computing the upper latitude bound
        LatLng nord = SphericalUtil.computeOffset(refPoint,radius,0);
        boundaries.upperLatitude = nord.latitude;

        //computing the lower latitude bound
        LatLng south = SphericalUtil.computeOffset(refPoint,radius,180);
        boundaries.lowerLatitude = south.latitude;

        //computing the right longitude bound
        LatLng east = SphericalUtil.computeOffset(refPoint,radius,90);
        boundaries.rightLongitude = east.longitude;

        //computing the left longitude bound
        LatLng west = SphericalUtil.computeOffset(refPoint,radius,270);
        boundaries.leftLongitude = west.longitude;

        return boundaries;
    }

    /**
     * Compute the maximum of a series of coordinates
     *
     * @param coordinates array of coordinates
     * @return  tha maximum of the whole array
     */
    public static final double maxCoordinate(double... coordinates){
        List<Double> list = new ArrayList<Double>();
        for (double c : coordinates) list.add(c);
        return Collections.max(list,null);
    }

    /**
     * Compute the minimum of a series of coordinates
     *
     * @param coordinates array of coordinates
     * @return  tha minimum of the whole array
     */
    public static final double minCoordinate(double... coordinates){
        List<Double> list = new ArrayList<Double>();
        for (double c : coordinates) list.add(c);
        return Collections.min(list,null);
    }

}
