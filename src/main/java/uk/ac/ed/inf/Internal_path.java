package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.mapbox.geojson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Class that implements information about one singular order and the midpoints between the source and
 * destination. <br>
 * It stores all the useful information about a singular order, this class is used a lot of other modules. <br>
 * One of the core classes
 */
public class Internal_path implements Comparable<Internal_path>{
    // website object used to send requests internally
     static Website web;
    /** source of the delivery of the order
     *
     */
    public LongLat Source;
    /**
     *  delivery destination of the order
     */
    public LongLat Destination;
    // the restaurants between the source and destination
    private final ArrayList<LongLat> midpoints = new ArrayList<>();
    // the internation permutation
    private ArrayList<Integer> internal_Permutation;
    /**
     *  The path between the source, the midpoints and destination all connected
     */
    public Vector<LongLat> Path;
    /**
     * The total number of moves this takes
     */
    public int moves;
   // the weight used if needed for greedy knapsack
   private double weight;

    // Other information
    /**
     * The order number of the customer
     */
    public String order_no;
    /**
     * The restaurants to visit
     */
    public ArrayList<String> restaurants = new ArrayList<>();
    /**
     * The orders by the customer
     */
    public  ArrayList<String> orders;
    /**
     * W3W of the destination location
     */
    public String destination_location;
    /**
     * The delivery cost of the entire journey
     */
    public int cost;

    /**
     * Setter function which is used for greedy knapsack if it needed
     * @param weight the weight of the class
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * This is a static object that should be the same for all the objects hence this setter is used
     * @param web the web object whose reference is stored
     */
    public static void setWeb(Website web) {
        Internal_path.web = web;
    }

    /**
     * One of the constructors, it defines a normal customer information.
     * @param order_no the order number string
     * @param destination_location delivery w3w string
     * @param orders the orders made by the customer
     * @param cost t
     */
    public Internal_path(String order_no, String destination_location, ArrayList<String> orders, int cost){
        // standard constructor
        this.order_no = order_no;
        this.destination_location = destination_location;
        this.orders = orders;
        this.cost = cost;
        // internally calculates other data structures like getting the coordinates
        set_coordinates();
    }

    /**
     * A special constructor whose only purpose is for special objects which is not a customer but rather
     * used for the computation, uses custom order number requires a Path pre-computed.
     * @param order_no the custom order number
     * @param Path the path that has been pre-computed
     */
    public Internal_path(String order_no,Vector<LongLat> Path){
        this.order_no = order_no;
        this.Path = Path;
    }

    /**
     * Sends a web request to get the coordinates of a specific location.
     * @param web the object this is used to send the request
     * @param s the string of that specifies the W3W location
     * @return LongLat coordinates from gson object
     */
    public LongLat get_coordinates(Website web,String s){
        // standard web request
        ArrayList<String> data = new ArrayList<>();
        data.add(s);
        // send request to get the info
        String response = web.request("Location",data);
        /* Initialises new GSON for parsing
         */
        Gson gson = new Gson();
        Details places =
                gson.fromJson(response, Details.class);
        return new LongLat(places.coordinates.lng,places.coordinates.lat);

    }

    /**
     * Sets the source of the Class object as the precomputed LongLat object.
     * @param source the precomputed LongLat object
     */
    public void setSource(LongLat source) {

        Source = source;
        // calculate the internal permutation
        heuristic_shortest_internal_distance(Source,true);
        // Internally calculate the path after calculating the internal permutation
        calculate_path();

    }

    /**
     * Calculates the actual path from the source -> midpoints -> destination
     * stores it in Path Datastructures
     * also computes the number of moves this takes.
     */
    private void calculate_path(){
        // data structure to store the total path
        Vector<LongLat> current_path = new Vector<>();
        // the loop that goes through the midpoints
        for (int i = 0; i < internal_Permutation.size(); i++) {
            int current_perm = internal_Permutation.get(i);
            if(i==0){
              // Source -> First Midpoint path
              current_path.addAll(Source.path(midpoints.get(current_perm)));
              // if 2 midpoints i.e. two Shops, calculate the next perm and midpoint -> midpoint path
              if(internal_Permutation.size()==2){
                  int next_perm = internal_Permutation.get(i+1);
                  current_path.addAll(current_path.get(current_path.size()-1).path(midpoints.get(next_perm)));
              }
            }
            // if at the end of the loop Last Permutation Midpoint -> Destination
            if(i==internal_Permutation.size()-1){
                current_path.addAll(current_path.get(current_path.size()-1).path(Destination));
            }
        }
        // Finally, stores the computed paths reference in the global data
        Path = current_path;
        moves = Path.size();
    }

    /**
     * Calculates the shortest Heuristic distance between the source and the destination
     * gives the distance but only sets the value if and only if setPerm is True.
     * @param source The source from which we calculate
     * @param setPerm The condition on whether to we are committing to this source
     * @return distance that was calculated
     */
    public double heuristic_shortest_internal_distance(LongLat source, boolean setPerm){
        // useful so that we can get the minimum distance
        double dist= Double.POSITIVE_INFINITY;
        // stores the index which is the last index
        int k=0;
        // iterates through the midpoints
        for (int i = 0; i < midpoints.size(); i++) {
            // temp variables
          double temp_dist=0;
            ArrayList<Integer> temp_internal_perm= new ArrayList<>();
            // get Heuristic distance from source to midpoint
            temp_dist+=A_star_greedy.H(source, midpoints.get(i));
            // add to temp permutation
            temp_internal_perm.add(i);
            // another loop to compare other midpoints from current midpoint to other midpoints
            for (int j = 0; j < midpoints.size(); j++) {
             // we don't want midpoint(i) to be compared to itself
             if(i==j){
                 continue;
             }  // distance between the 2 midpoints
                temp_dist+=A_star_greedy.H(midpoints.get(i), midpoints.get(j));
                temp_internal_perm.add(j);
              k = j;
            }
            // finally, the final midpoint to the destination distance
            temp_dist+=A_star_greedy.H(midpoints.get(k),Destination);
            // comparison to find the shortest internal path
            if(temp_dist<dist){
                dist=temp_dist;
                if(setPerm){
                internal_Permutation = temp_internal_perm; }
            }
        }

        return dist;
    }

    /**
     * Sets coordinates i.e. sends request to get the gson of the coordinates which gets converted to LongLat by another function
     * which is used to set the midpoints and destination.
     */
    private void set_coordinates(){
        // get destination coordinates
        this.Destination = get_coordinates(web,this.destination_location);
        // loop to get coordinates for each midpoint
        for (String s : orders) {
            String order_location = Compute.getLocation(s);
            // only want unique restaurants
            if(restaurants.contains(order_location)){
                continue;
            }
            restaurants.add(order_location);
            LongLat L = get_coordinates(web,order_location);
            assert  L!=null;
            midpoints.add(L);

        }
    }

    /**
     * Makes the geojson string that written to the file and that can be viewed to see the drone permutation.
     * @param V the  Path vector of points which is converted to Geojson
     * @return A String which is converted from path to geojson format
     */
    public static String make_geo_json(Vector<LongLat> V){
       // List of Point type used by geojson class
        List<Point> Line = new ArrayList<>();
        for (LongLat longLat : V) {
            // convert LongLat class object to Point
            Point Poi = Point.fromLngLat(longLat.longitude, longLat.latitude);
            // add to the line
            Line.add(Poi);

        }
        // Create LineString -> Feature-> Feature Collection -> finally convert that to json
        LineString L = LineString.fromLngLats(Line);
        Feature Fea = Feature.fromGeometry(L);
        FeatureCollection Cole = FeatureCollection.fromFeature(Fea);
        return Cole.toJson();
    }

    /**
     * This is comparable overwrite which is used by java internally for sorting, and other data structures. <br>
     * By default, class objects cannot be sorted/compared until specified which field is going to be used
     * for the comparison.
     * @param o an object of this class
     * @return the value of the comparison
     */
    @Override
    public int compareTo(Internal_path o) {
        return Double.compare(this.weight, o.weight);
    }
}
