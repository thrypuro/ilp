package uk.ac.ed.inf;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import uk.ac.ed.inf.Polygon_inside_and_intersection.Point_local;

/**
 * <p>
 * First step in the program where we get all the information that is necessary for the computation
 * this step sets up everything that is needed.
 * <br>
 * 1) Polygons
 * <br>
 * 2) Menus
 *<br>
 * 3) Databases </p>
 */
public class Initialise {
    static boolean status=false;
    /**
     * Website object which is used to send request to website/database
     */
    public static Website Web;

    /**
     * Getter function to get the Website Object
     * created by Initialise, to use the same object
     * amongst all the classes.
     * @return an object Web that can use methods in Website class
     */
    public  Website getWeb() {
        return Web;
    }

    /**
     * Initiates the menu's hashmap to make it easier get Location
     * as well the costs when we need to calculate the cost and location
     * orders.
     */
    private void prepare_menus(){
        // Sends a Request for the Menu, gets a response from the server
        // no additional data needed as it doesn't require any parameters
        String response=Web.request("Menus",null);
        // Instantiates the two Hashmaps that gets data added to
        HashMap<String,Integer> cost=new HashMap<>();
        HashMap<String,String> location = new HashMap<>();
        assert response!=null;
            /* Initialises new GSON for parsing
               ArrayList is used, as it is an array of JSONs
               Uses TypeToken and Type for getting the type
             */
        Gson gson = new Gson();
        Type listType =
                new TypeToken<ArrayList<Places>>() {}.getType();
        ArrayList<Places> places =
                gson.fromJson(response, listType);
        // 50p Delivery charge by default
        // Class specific iterator, cleaner to iterate
        for(Places place : places){
            // Takes the important field of the class
            for(Places.menu menu : place.menu){
                cost.put(menu.item, menu.pence);
                location.put(menu.item,place.location);
            }
                /* Setter function that sets the values that
                 will be needed for the next stage of the
                 program.
                 */
            Compute.setCost(cost);
            Compute.setLocation(location);

        }
        System.out.println("Menus prepared!");
    }

    /**
     * <p>
     * Class that makes the polygons that are needed to
     * for the Winding-number Algorithm, to check if
     * a point is in the polygon or not.
     * <br>
     * Sends a get-request to the webserver to get the
     * data that is needed to run the algorithm. </p>
     */
    private  void make_Polygons() {
        // Requests the Polygon Points
        String source = Web.request("Polygon",null);
        assert source!=null;
        // Parses it from String to GeoJson format
        FeatureCollection fc = FeatureCollection.fromJson(source);
        // Polygon Variable that stores the points in the polygon in the end
        Vector<Vector<Point_local>> Polygons = new Vector<>();
        // assert the request went through
        assert fc.features()!=null;
           /*
            Iterates to the geoJson file and converts it from
            features to Points that is in the form of Point
            to my Local Classes that can be used for calculation
            */
        for (int i = 0; i < fc.features().size(); i++) {

            Vector<Point_local> Poly = new Vector<>();
            Polygon pol = (Polygon) fc.features().get(i).geometry();
            assert pol != null;
            // One Polygon iteration
            for(int j=0;j<pol.coordinates().get(0).size();j++){
                // get the x and y coordinates
                double x = pol.coordinates().get(0).get(j).coordinates().get(0);
                double y = pol.coordinates().get(0).get(j).coordinates().get(1);
                Point_local P2 = new Point_local(x,y);
                Poly.add(P2);
            }
            // Finally, add each Polygon to the collection
            Polygons.add(Poly);
        }
        assert !Polygons.isEmpty();
            /* Setter function that puts in the method that actually uses
            this data
             */

        Polygon_inside_and_intersection.setPolygons(Polygons);
        System.out.println("Polygon prepared!");
    }

    /**
     * <p>
     * Makes sure that the Input given by the user is valid
     * check if it is a valid date, port and machine
     * <br>
     * This is a security purpose function to prevent from nasty
     * sql injection attacks. </p>
     * @param arguments : All the user given arguments in main method in App
     * @return : Boolean if the arguments are safe/trusted or valid
     */
    private boolean Sanitise_inputs(String[] arguments){
        // valid hostname or ipaddress?
        if(!arguments[0].matches("[a-zA-z0-9.]+")){
            System.err.println("Invalid Machine!");
            return false;
        }
        boolean valid_args;
        // valid port?
        valid_args = (Integer.parseInt(arguments[1])<=65535 && Integer.parseInt(arguments[1])>=0);
        valid_args = valid_args && (Integer.parseInt(arguments[2])<=65535 && Integer.parseInt(arguments[2])>=0);
        String dateStr = arguments[5]+"-"+arguments[4]+"-"+arguments[3];
        // To prevent some sql injection
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            formatter.setLenient(false);
            formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return valid_args;
    }

    /**
     * Creates an SQL statement which queries to get orders for the date that is asked for.
     * @param date The date string at which we want to query from the database.
     */
    public void get_orders(String date){
        // the prepared statement
        String statement = "select * from orders where deliveryDate=(?)";
        // the data List that is used to store the additional information that is need for the query
        ArrayList<String> data = new ArrayList<>();
        // Data ArrayList format in more detail in Website class!
        data.add("orders");
        data.add(statement);
        data.add(date);
        // Arraylist to store the results from the query
        ArrayList<String> delivery_destination = new ArrayList<>();
        ArrayList<String> order_no = new ArrayList<>();
        // Send the web request
        Web.request("Database-select-table",data);
        // get the response from the database
        ResultSet rs = Web.getSet();
        try {
            // query the database with the relevant column
            while (rs.next()){
                String course = rs.getString("deliverto");
                String order = rs.getString("orderno");
                delivery_destination.add(course);
                order_no.add(order);
            }
        }
        catch ( SQLException e) {
            e.printStackTrace();
        }
        // set the data that's important for the next stage!
        Compute.setDelivery_destination(delivery_destination);
        Compute.setOrder_no(order_no);
        System.out.println("Order prepared!");

    }

    /**
     * <p>
     * Query to create the database table if it is not yet created! <br>
     * If it is already created, attempts to drop the table in order to create it again. </p>
     */
    public static void create_tables(){
        // Additional data for the query
        ArrayList<String> data = new ArrayList<>();
        data.add("deliveries");
        // send the request to create table deliveries
        Web.request("Database-create-table",data);
        // empty data to create new request
        data.clear();
        data.add("flightpath");
        // send the request to create table flightpath
        Web.request("Database-create-table",data);

    }

    /**
     * The constructor that will be doing all the work that is needed by to Initialise module.
     * @param arguments : An arraylist which stores all the arguments that we use for initialising
     */
    public Initialise(String[] arguments){
        // if arguments aren't code-friendly
        if(!Sanitise_inputs(arguments)){
            System.err.println("One the values entered is not valid!");
            System.exit(1);
        }
        String day = arguments[3];
        String month = arguments[4];
        String year = arguments[5];
        String date = year+"-"+month+"-"+day;
        // Website(machine,port1,port2)
        Web = new Website(arguments[0],arguments[1],arguments[2]);
        // set up menus, polygons and orders
        prepare_menus();
        make_Polygons();
        get_orders(date);

        create_tables();
        status=true;
    }
}
