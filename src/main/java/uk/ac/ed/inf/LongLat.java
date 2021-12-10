package uk.ac.ed.inf;
import java.lang.Math;
import java.util.Vector;

/**
 * A helper class that is used by other modules for storing the latitude and longitude of a Point <br>
 * also has methods relevant including Points.
 */
public class LongLat implements Comparable<LongLat> {
    // double precision for float values
    /**
     * Latitude of the Point
     */
    public double  latitude;
    /**
     * Longitude of the Point
     */
    public double  longitude;


    /** distance (for priority queue purposes)
     * This field is optional, and is only used for comparison purpose by A* class but can be expanded for other sorting purposes
     */
    public double fS;
    /**
     * Constructor of the class LongLat
     * @param longitude : Longitude of the drone
     * @param latitude : Latitude of the drone
     */
    public LongLat(double  longitude,double  latitude) {
        // this.something to not confuse parameters and class variables
        this.latitude = latitude;
        this.longitude = longitude; }

    /**
     * Checks whether the drone is within the confinement zone with the given constraint values.
     * @return : Boolean whether if it is or not within the zone
     */
    public boolean isConfined(){
        // constraints are four corners of the map
        return (latitude > 55.942617 && latitude < 55.946233) && (longitude > -3.192473 && longitude < -3.184319);
    }

    /**
     * Calculates the distance between two points/places with a common mathematical formula. Calculates with class variables and another object of the same class.
     * @param point : Another object of the same class to calculate distance from
     * @return : Double precision number which is the distance
     */
    public double distanceTo(LongLat point){
        double distance;
        // sqrt( (x1-x2)^2 + (y1-y2)^2 )
        distance = Math.sqrt( Math.pow( point.longitude - longitude , 2 ) + Math.pow( point.latitude - latitude , 2 ) );
        return distance;
    }

    /**
     *distance (for priority queue purposes)
     * This field is optional, and is only used for comparison purpose by A* class but can be expanded for other sorting purposes
     * A setter function for that.
     * @param fS the F Score value that has been  precomputed
     */
    public void setfS(double fS){
        this.fS = fS;
    }
    /**
     * Uses another method in the class to find if 2 places are close to eachother by calculating their distances from one another.
     * @param place : Another object of the same class we are comparing to
     * @return : Boolean of whether if it is close or not, by very small significant digits
     */
    public boolean closeTo(LongLat place){
        // just need precision upto 7 decimal digits
        return distanceTo(place)<0.00015;
    }

    /**
     * Calculates the next position of the drone, in a certain angle,
     * uses some basic trigonometry to calculate the new latitude and longitude
     * it stays in the same position, if angle -999
     * Hence, could be used for the movement of the drone
     * @param angle : The angle at which drone moves from its current position
     * @return : returns a class object of the new coordinates, or hovering
     */
    public LongLat nextPosition(int angle){
        // Condition to check if the drone needs to hover on the same position
        if(angle==-999){
            // returns the same coordinate if condition true
            return new LongLat(longitude,latitude);
        }
        // angle has to be radians for trigonometry java functions, drone moves 0.00015 each move
        //  calculates adjacent distance of the equilateral triangle and adds it to the coordinate longitude
        double new_longitude = longitude + ( 0.00015 * ( Math.cos(Math.toRadians(angle) ) ));
        // calculates the opposite distance of the equilateral triangle and adds it to the coordinates latitude
        double new_latitude = latitude + ( 0.00015 * ( Math.sin( Math.toRadians(angle) ) ));
        return new LongLat(new_longitude,new_latitude); }


    /**
     * Calculates the angle between two LongLat Points,
     * @param L the point to which the angle is calculated
     * @return the angle between the class current object and the parameter object given
     */
    public int angle(LongLat L){
        int a=-99;
        // if hover return -999
        if(this.longitude==L.longitude && this.latitude==L.latitude){
            return -999;
        }
        // else try to find the angle in all positions from the drone
        for (int i = 0; i < 360; i=i+10) {
            LongLat nL = L.nextPosition(i);
            if(nL.longitude==this.longitude && this.latitude==nL.latitude){
              a = i;
            }
        }
        // this condition is assurance that there is always an angle between the 2 points when used for final Path
        if(a==-99){
            System.err.println("Could not find angle between the points! Please debug the code.");
            System.exit(-1);
        }
        return a;
    }

    /**
     * Computes the path between the current object point and another object of the class
     * @param Q another object of the same class
     * @return Vector of objects of the same class which contains the path between the two points
     */
    public Vector<LongLat> path(LongLat Q){
        /*
        If there is going to be landmarks as safety measure it can be done here, as because of my lack of time there is no implementation
        of this feature, but it can be added in the future if needed, it is made very easy as there is already path_found variable
        and
         */
        // constructor
        A_star_greedy A = new A_star_greedy(new LongLat(longitude,latitude),Q);
        // return
        return A.get_path();
    }

    /**
     * This is comparable overwrite which is used by java internally for sorting, and priority queues
     * By default class objects cannot be sorted until specified which field is going to be used
     * for the comparison
     * @param o the LongLat object it is going to be compared to
     * @return the value of the comparison
     */
    @Override
    public int compareTo(LongLat o) {
        return Double.compare(this.fS, o.fS);
    }


}
