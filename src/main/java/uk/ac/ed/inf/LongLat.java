package uk.ac.ed.inf;
import java.lang.Math;
import java.util.Vector;


public class LongLat implements Comparable<LongLat> {
    // double precision for float values
    public double  latitude;
    public double  longitude;


    // distance (for priority queue purposes)
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
     * @return : returns a class object of the new cordinates, or hovering
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
        LongLat new_cordinates =   new LongLat(new_longitude,new_latitude);
        return new_cordinates; }
    public LongLat shorter(int angle){
        // angle has to be radians for trigonometry java functions, drone moves 0.00015 each move
        //  calculates adjacent distance of the equilateral triangle and adds it to the coordinate longitude
        double new_longitude = longitude + ( 0.00009 * ( Math.cos(Math.toRadians(angle) ) ));
        // calculates the opposite distance of the equilateral triangle and adds it to the coordinates latitude
        double new_latitude = latitude + ( 0.00009 * ( Math.sin( Math.toRadians(angle) ) ));
        return new LongLat(new_longitude,new_latitude); }
    public int angle(LongLat L){
        int a=-99;
        if(this.longitude==L.longitude && this.latitude==L.latitude){
            return -999;
        }
        for (int i = 0; i < 360; i=i+10) {
            LongLat nL = L.nextPosition(i);
            if(nL.longitude==this.longitude && this.latitude==nL.latitude){
              a = i;
            }
        }
        return a;
    }


    public Vector<LongLat> path(LongLat Q){
        A_star_greedy A = new A_star_greedy(new LongLat(longitude,latitude),Q,false);
        A.get_path();
        A_star_greedy B = new A_star_greedy(new LongLat(longitude,latitude),Q,true);
        B.get_path();
        if(B.path_found){
            if(!A.path_found){
                return B.Path;
            }
            else if(B.size()<A.size()){
                return B.Path;
            }
        }
        return A.Path;
    }
    @Override
    public int compareTo(LongLat o) {
        return Double.compare(this.fS, o.fS);
    }



    public static void main(String[] args) {
        LongLat A = new LongLat(-3.185376953893118,
                55.9446576969785);
        LongLat B = new LongLat(-3.185332 ,55.944656 );
        System.out.println(A.angle(B));
    }
}
