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
import uk.ac.ed.inf.Polygon_Winding_number.Pointloc;
public class Initialise {
    static boolean status=false;
    public static Website Web;

    /**
     * Getter function to get the Website Object
     * created by Initialise, to use the same object
     * amongst all the classes
     * @return an object Web that can use methods in Website class
     */
    public  Website getWeb() {
        return Web;
    }

    /**
     * Initiates the menus hashmap to make it easier get Location
     * as well the costs when we need to calculate the cost and location
     * orders.
     */
    public void prepare_menus(){
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
     * Class that makes the polygons that are needed to
     * for the Winding-number Algorithm, to check if
     * a point is in the polygon or not.
     * Sends a get-request to the webserver to get the
     * data that is needed to run the algorithm.
     */
    public  void make_Polygons() {
            // Requests the Polygon Points
            String source = Web.request("Polygon",null);
            assert source!=null;
            // Parses it from String to GeoJson format
            FeatureCollection fc = FeatureCollection.fromJson(source);
            // Polygon Variable that stores the points in the polygon in the end
           Vector<Vector<Pointloc>> Polygons = new Vector<>();
           // assert the request went through
           assert fc.features()!=null;
           /*
            Iterates to the geoJson file and converts it from
            features to Points that is in the form of Point
            to my Local Classes that can be used for calculation
            */
            for (int i = 0; i < fc.features().size(); i++) {
                Vector<Pointloc> Poly = new Vector<>();
                Polygon pol = (Polygon) fc.features().get(i).geometry();
                assert pol != null;
                // One Polygon iteration
                for(int j=0;j<pol.coordinates().get(0).size();j++){
                    // get the x and y coordinates
                    double x = pol.coordinates().get(0).get(j).coordinates().get(0);
                    double y = pol.coordinates().get(0).get(j).coordinates().get(1);
                    Pointloc P2 = new Pointloc(x,y);
                    Poly.add(P2);
                }
                // Finally, add each Polygon to the collection
                Polygons.add(Poly);
            }
            assert !Polygons.isEmpty();
            /* Setter function that puts in the method that actually uses
            this data
             */


            Polygon_Winding_number.setPolygons(Polygons);
        System.out.println("Polygon prepared!");
    }

    /**
     * Makes sure that the Input given by the user is valid
     * check if it is a valid date, port and machine
     * This is a security purpose function to prevent from nasty
     * sql injection attacks.
     * @param arguments : All the user given arguments in main method in App
     * @return : Boolean if the arguments are safe/trusted or valid
     */
    private boolean Sanitise_inputs(ArrayList<String> arguments){
        // valid hostname or ipaddress?
        if(!arguments.get(0).matches("[a-zA-z0-9.]+")){
            System.err.println("Invalid Machine!");
            return false;
        }
        boolean valid_args;
        // valid port?
        valid_args = (Integer.parseInt(arguments.get(1))<=65535 && Integer.parseInt(arguments.get(1))>=0);
        valid_args = valid_args && (Integer.parseInt(arguments.get(2))<=65535 && Integer.parseInt(arguments.get(2))>=0);
        String dateStr = arguments.get(5)+"-"+arguments.get(4)+"-"+arguments.get(3);
        // To prevent some nasty sql injection
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
     *
     * @param date dae
     */
    public void get_orders(String date){
        String statement = "select * from orders where deliveryDate=(?)";
        ArrayList<String> data = new ArrayList<>();
        data.add("orders");
        data.add(statement);
        data.add(date);
        ArrayList<String> delivery_destination = new ArrayList<>();
        ArrayList<String> order_no = new ArrayList<>();
        Web.request("Database-select-table",data);
        ResultSet rs = Web.getSet();
        try {
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
        Compute.setDelivery_destination(delivery_destination);
        Compute.setOrder_no(order_no);
        System.out.println("Order prepared!");

    }
    public static void create_tables(){
        ArrayList<String> d = new ArrayList<>();
        d.add("deliveries");
      Web.request("Database-create-table",d);
        d = new ArrayList<>();
        d.add("flightpath");
        Web.request("Database-create-table",d);

    }
    public Initialise(ArrayList<String> arguments){
        if(!Sanitise_inputs(arguments)){
            System.err.println("One the values entered is not valid!");
            System.exit(1);
        }
        String day = arguments.get(3);
        String month = arguments.get(4);
        String year = arguments.get(5);
        String date = year+"-"+month+"-"+day;
        // Website(machine,port1,port2)
        Web = new Website(arguments.get(0),arguments.get(1),arguments.get(2));
        // prepare menu
        prepare_menus();
        // make the points in polygon
        make_Polygons();
        // get orders
        get_orders(date);
        create_tables();
        status=true;
    }
}
