package uk.ac.ed.inf;


import java.awt.geom.Line2D;
import java.util.Vector;


/**
 * Class that is an implementation of an algorithm which checks
 * if a particular <b>Point in the Polygon</b> or not.<br> These kinds of problems are known as
 * <b>Point in Polygon problem </b>.<br> This Algorithm implemented here is called Winding Number
 * the algorithm is implemented from the paper: <a href="https://www.sciencedirect.com/science/article/pii/S0925772101000128">Winding-number paper</a><br>
 * Also checks if 2 line segments intersects, uses default java.awt class
 * these problems known as <b>Line-Line intersection problem </b>
 */
public class Polygon_inside_and_intersection {

    //All the Polygons are stored in this DataStructure
    private static Vector<Vector<Point_local>> Polygons = new Vector<>();

    /**
     * <b>Point in Polygon Algorithm.</b> <br>
     * Checks if the Point is in any of the given list of Polygons.
     * @param P the point to check if it is in the polygon
     * @return boolean whether of not the point is in any of the given list of polygon
     */
    public static boolean in_Polygon(LongLat P){
        // initialise convert Point from Global LongLat Class to Local Class
        boolean inside = false;
        Point_local P1 = new Point_local(P.longitude,P.latitude);
        // loop to check if it is in every single polygon
        for(int i = 0; i< Polygon_inside_and_intersection.Polygons.size(); i++){
            inside=inside|| isInside(Polygon_inside_and_intersection.Polygons.get(i),P1);
        }
        return inside;
    }

    /**
     * Sets the Polygon datastructures that was precomputed in Initialise class
     * @param Polygons1 the precomputed data from Initialise class
     */
    public static void setPolygons(Vector<Vector<Point_local>> Polygons1){
        Polygons = Polygons1;
    }

    /**
     * This local class is used instead of LongLat mostly as a preference
     * LongLat can also be used here but the reason for this choice is that
     * x and y is much better way to describe in this algorithm than latitude and
     * longitude
     */
    static class Point_local {
        double x;
        double y;

        public Point_local(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }

    /**
     * <b>Point in Polygon Algorithm.</b>
     * <br>
     * Calculates the determinant, a helper function implemented as per paper
     * @param P1 the i th Point of the Polygon
     * @param P2 the i+1 th Point of the Polygon
     * @param R the second point (usually the point to check if it is inside)
     * @return the determinant between the 2 points
     */
    private static double det( Point_local P1,Point_local P2, Point_local R) {
        double D1 = (P1.x - R.x) * (P2.y - R.y);
        double D2 = (P2.x - R.x) * (P1.y - R.y);
        return D1 - D2;
    }

    /**
     * <b>Point in Polygon Algorithm. </b>
     * <br>
     * Classify which quadrant the Point R belongs, helper function implemented as per the paper
     * refer to the paper for further explanation
     * @param P the first point of the polygon
     * @param R the point we are checking if it is in the polygon
     * @return integer specifying which quadrant R belongs
     */
    private static int classify(Point_local P, Point_local R) {
        if (P.y > R.y) {
            return (P.x <= R.x) ? 1 : 0;
        } else {
            if (P.y < R.y) {
                return 2 + ((P.x >= R.x) ? 1 : 0);
            } else {
                if (P.x > R.x) {
                    return 0;
                } else {
                    if (P.x < R.x) {
                        return 2;
                    } else {
                        return -99;
                    }
                }
            }
        }
    }

    /**
     *  <b>Point in Polygon Algorithm.</b>
     * <br>
     * Actual Logic to check if Point is inside the Polygon or not, this Function defines the
     * Winding number Algorithm.
     * @param P  vector of Points i.e. the Polygon
     * @param R the Point we are checking whether it is inside the Polygon
     * @return boolean of whether it is inside the polygon or not
     */
    public static boolean isInside(Vector<Point_local> P, Point_local R) {
        // stores the list of integers which specifies the quadrant
        Vector<Integer> q_i = new Vector<>();
        // this integer is factor which says if it is in inside the polygon or not
        int omega = 0;
        // loop to classify each Point of the Polygon with the Point to check if it is inside
        for (int i = 0; i < P.size() - 1; i++) {
            int current = classify(P.get(i), R);
            // a safety check, even-though this happening means something went very wrong
            if (current == -99) {
                System.err.println("An Error Occurred!");
                return false;
            }
            q_i.add(current);
        }
        q_i.add(q_i.get(0));
        // loop to calculate omega which is the factor which establishes whether if it is inside or not, heavily adapted from the paper
        for (int i = 0; i < P.size() - 1; i++) {
            int q1 = q_i.get(i + 1);
            int q2 = q_i.get(i);
            switch (q1 - q2) {
                case -3:
                    omega = omega + 1;
                    break;
                case 3:
                    omega = omega - 1;
                    break;
                case -2:
                    if (det(P.get(i),P.get(i+1), R) > 0) {
                        omega = omega + 1;
                    }
                    break;
                case 2:
                    if (det(P.get(i),P.get(i+1), R) < 0) {
                        omega = omega - 1;
                    }
                    break;
            }
        }

        // if it is 0 then it is not inside otherwise, it is inside the Polygon
        return omega != 0;
    }

    /**
     * <b>Line-Line intersection Problem algorithm.</b>
     * <br>
     * Checks if there is any of the Polygon intersects the line segment formed by the two Points
     * @param P the first Point
     * @param Q the second Point
     * @return boolean of whether the any of the Polygon intersects the Line segments
     */
    public static boolean intersect_polygon(LongLat P, LongLat Q){
        boolean intersect = false;
        // convert the Points to the Local Class
        Point_local P1 = new Point_local(P.longitude,P.latitude);
        Point_local Q1 = new Point_local(Q.longitude,Q.latitude);
        // loop to check if it is in every single polygon
        for(int i = 0; i< Polygon_inside_and_intersection.Polygons.size(); i++){
            intersect=intersect|| intersect(Polygon_inside_and_intersection.Polygons.get(i),P1,Q1);
        }
        return intersect;
    }

    /**
     * Checks if one Polygon intersects the Line Segment
     * @param P The Polygon
     * @param Q First Point
     * @param R Second Point
     * @return boolean if the line segment intersects any of the lines in Polygon
     */
    private static boolean intersect(Vector<Point_local> P, Point_local Q,Point_local R){
        boolean does_intersect;
        // loop to check if it is in Pairs of Point of the Poly (i+1)mod size because, the last point and 0th point is also a line segment
        for (int i = 0; i <P.size() ; i++) {
            does_intersect = Line2D.linesIntersect(P.get(i).x,P.get(i).y,P.get((i+1)%P.size()).x,P.get((i+1)%P.size()).y,Q.x,Q.y,R.x,R.y);
            if(does_intersect){
                return true;
            }
        }
        return false;
    }

}
