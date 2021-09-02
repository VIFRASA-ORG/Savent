package Helper;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe Helper che fornisce metodi a supporto dei componenti MapsView.
 * Permette di calcolare le coordinate eografiche di aree quadrate intorno
 * ad un singolo punto di riferimento.
 */
public class MapsHelper {


    /**
     * Metodo che calcola i confini, in coordinate globali, di un area quadrata
     * intorno ad un punto di riferimento centrale dato come parametro.
     *
     * @param refPoint punto al centro dell'area quadrata da calcolare.
     * @param radius raggio del quadrato da calcolare.
     * @return un oggetto di classe LocationBoundaries con tutti i confini calcolati.
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
     * Metodo che trova la coordinata pìu grande nella serie
     * di coordinata date come paramentro.
     *
     * @param coordinates array di coordinate.
     * @return il massimo di tutto l'array.
     */
    public static final double maxCoordinate(double... coordinates){
        List<Double> list = new ArrayList<Double>();
        for (double c : coordinates) list.add(c);
        return Collections.max(list,null);
    }

    /**
     * Metodo che trova la coordinata pìu piccola nella serie
     * di coordinata date come paramentro.
     *
     * @param coordinates array di coordinate.
     * @return il minimo di tutto l'array.
     */
    public static final double minCoordinate(double... coordinates){
        List<Double> list = new ArrayList<Double>();
        for (double c : coordinates) list.add(c);
        return Collections.min(list,null);
    }



    /**\
     * CLASSE INTERNA CHE PERMETTE DI DEFINIRE UN AREA QUADRATA USANDO LE COORDINATE
     * DENTRO IL SISTEMA DI COORDINATE DELLE MAPPE.
     */
    public static class LocationBoundaries{

        //Latitudie più alta, la più lontana a nord
        public double upperLatitude = 0;

        //Latitudine più bassa, la meno a nord.
        public double lowerLatitude = 0;

        //Londitudine più a ovest
        public double leftLongitude = 0;

        //Longitudine più a est
        public double rightLongitude = 0;

        public LocationBoundaries(){ }
    }

}
