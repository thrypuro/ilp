package uk.ac.ed.inf;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Website Class that is used by all the other methods
 * having one place helps with only using one thread for everything instead of
 * making a new http thread over and over again.
 */
public class Website {
    // the url string for website and database
    private   static String site_urlString;
    private  static String database_urlString;
    // specifies if the table has been created
    private static boolean flight_table_created =false;
    private static boolean deliveries_table_created =false;
    // all the create table commands, stored in an Array table_create
    private static final String flight_path_create = "create table flightpath(orderNo char(8)," +
            "fromLongitude double," +
            "fromLatitude double," +
            "angle integer," +
            "toLongitude double," +
            "toLatitude double)";
    private static final String deliveries_table_create = "create table deliveries(orderNo char(8)," + "deliveredTo varchar(19)," +
            "costInPence int)";
    private static final String[] table_create = {flight_path_create, deliveries_table_create,"Already created!", "Already created!"};
    // stores the result from the prepared statement
    private     ResultSet set;

    /**
     * Getter function which returns the results of the prepared statement query
     * @return resultset of prepared statement query
     */
    public ResultSet getSet() {
        return set;
    }

    //  One HttpClient shared between all HttpRequests, to avoid repeated thread starts
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Sends web  get request to the server, by specifying the path given, result is the HTTP response
     * @param path the postfix of the url, which is used to send
     * @return response  if the request was successful
     */
    private HttpResponse<String> get_request(String path){
         // HttpRequest assumes that it is a GET request by default.
         HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(site_urlString +path))
                .build();
        HttpResponse<String> response = null;
            try {
                // Sends the GET request to the site
                 response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                 if(response.statusCode()==404){
                     System.err.println("404 Request not found!");
                     System.exit(404);
                 }

                return response;
            } // necessary catch statement if the request to the site failed
            catch (IOException | InterruptedException e){
                e.printStackTrace();
                System.err.println("Request could not go through, check the webserver!");
                System.exit(-1);
            }
           return response;
        }

    /**
     * Constructor which constructs the website's url and database's url
      * @param machine1 machine or the url of the site
     * @param port1 the first port, port of the website
     * @param port2 the second port, port of the database
     */
    public Website(String machine1,String port1,String port2){
        site_urlString = "http://"+ machine1 +":"+ port1;
        database_urlString = "jdbc:derby://"+ machine1 +":"+ port2 +"/derbyDB";
    }

    /**
     * Executes a prepared statement, result is written to resultSet
     * @param statement_str the prepared statement to be executed
     * @param values the values to be inserted in the prepared statement
     * @param query the type of query it is
     * @return result set or null depending on the query
     */
    private ResultSet execute_query_prepared(String statement_str, ArrayList<String> values,String query){
        // establish connection to the database
        Connection conn = connect_to_database();
        try {
            assert Arrays.stream(statement_str.split("[?]+")).count() == values.size();
            // prepare the prepared statement
            PreparedStatement prepared_statement =
                    conn.prepareStatement(statement_str);
            // insert values wherever appropriate
            for (int i = 0; i < values.size(); i++) {
                prepared_statement.setString(i+1, values.get(i));
            }
            // only .execute if query is write else normal executeQuery, this is because sql statements can be finicky
            if(query.equals("write")){
                prepared_statement.execute();
            }
            else {
            return prepared_statement.executeQuery(); }
        } catch ( SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Establish connection to the database
     * @return connection from the database
     */
    private Connection  connect_to_database(){
        Connection conn = null;
        try {
            // Sends the GET request to the site
            conn = DriverManager.getConnection(database_urlString);

        }catch ( SQLException e) {
            System.err.println("Error with connecting to database!");
            e.printStackTrace();
            System.exit(-1);
        }
       return conn;
    }

    /**
     * Executes unprepared sql statement
     * @param statement_str the statement to be executed
     * @return String specifying whether the query was success or not
     */
    private String execute_query_Unprepared(String statement_str) {
        // establish connection
        Connection conn= connect_to_database();

        try {
            // standard sql statement execution
            Statement statement = conn.createStatement();
           statement.execute(statement_str);
        }catch ( SQLException e) {
            return "Error with the Unprepared statement!";
        }
        return "Success";
    }

    /**
     * Switch statement matches the query by its purpose and creates a statement according to it.
     * Point of this is to add or change easily if new statements arise without changing too much of the logic
     * @param index the table number 0 - flight path, 1 - deliveries, 2 - orders, 3 - orderdetails
     * @param query  query could be select, create, drop and write
     * @param data additional data that will be used for query if necessary
     */
    public void query(int index, String query,ArrayList<String> data){
        String statement;
        // makes sure that the 2nd and 3rd table can only query select
        if(!(index != 2 && index != 3 || query.equals("select"))){
            query = "invalid";
        }
        // switch statement which matches by the query
        switch (query) {
            case "create" -> {
                // additional data is not used
                // indexes by the table
                statement = table_create[index];
                // executes statement
                String result =execute_query_Unprepared(statement);
                /*
                assume a case when table has already been created and used beforehand, according to the specification
                we don't want to write on top of this, hence we will drop the table  before we create the table again
                 */
                if(!result.equals("Success")){
                    statement = "drop table "+data.get(0);
                    execute_query_Unprepared(statement);
                    statement = table_create[index];
                    execute_query_Unprepared(statement);
                }

            }
            case "drop" -> {
                // drop the table as requested
                statement = "drop table "+data.get(0);
                execute_query_Unprepared(statement);

            }
            case "write", "select" -> {
                // both of these cases use additional data hence, we will query the statement from it, also uses prepared statement
                // data has to be bigger than 2 on minimum since the values are stores afterwards
                assert data.size()>2;
                // the statement
                statement = data.get(1);
                // the values are stored in this ArrayList
                ArrayList<String> V= new ArrayList<>();
                for (int i = 2; i <data.size(); i++) {
                    V.add(data.get(i));
                }
                // store the results of the prepared statement
                set = execute_query_prepared(statement,V,query);
            }
            default -> System.err.println("Please enter a valid sql query");
        }
    }

    /**
     *  Switch statement checks by table name and checks if query is appropriate.
     * @param data additional data which contains table name and other data
     * @param query the query that will be performed
     * @return boolean of if the query is successful or not
     */
    private boolean create_query(ArrayList<String> data,String query){
        // if data is null then query is invalid
        if(data==null){
            System.err.println("Cannot query!");
            System.exit(1);
        }
        // matches by table name
        switch (data.get(0)) {
            case "flightpath"-> {
                // makes sure if we can create/drop flightpath or not
                boolean can_query = (query.equals("create") && !flight_table_created) || ((query.equals("drop")|| query.equals("write"))  && flight_table_created);
                if(can_query){
                query(0, query, data);
                    flight_table_created= !query.equals("drop");
                }

            }
            case "deliveries"-> {
                // makes sure if we can create/drop deliveries or not
                boolean can_query=(query.equals("create") && !deliveries_table_created) || ( (query.equals("drop") || query.equals("write")) && deliveries_table_created);

                if(can_query){
                    query(1, query, data);
                    deliveries_table_created= !query.equals("drop");
                }
                else {
                    System.err.println("Cannot do this operation!");
                    System.exit(101);
                }
            }
            case  "orders" -> query(2,query,data);
            case  "orderdetails" -> query(3,query,data);
            default->System.err.println("Could not find the table given!");
        }
        return true;
    }

    /**
     * The main bread and butter of this class that will be most of the time by other modules.
     *
     * @param request matches the string with a switch statement, depending on the query
     * @param Additional_data this is data is required for certain queries, they have certain format inspired from .pem formats, the format is <br>
     *                        data [0] : either table name or site index (location) <br>
     *                        data [1] : the statement to be executed <br>
     *                        data [2-n] : Additional values needed, example: writing to database table
     * @return either the request body or Success/failure string depending on the request
     */
    public String request(String request, ArrayList<String> Additional_data){ /// easily able to add new options and features if website changes
        switch (request){
            case "Menus" ->{
                return get_request("/menus/menus.json").body(); // menus path
            }
            case "Location"->{
                if(Additional_data==null){
                    break; // if additional data is null, break because we cannot index the site
                }
                // index the W3W and index the site
                String  site_index = Additional_data.get(0).replace(".","/");
                return get_request("/words/"+site_index+"/details.json").body(); }
            case "Polygon" ->  {
                // the list of points of the Polygon
                return get_request("/buildings/" + "no-fly-zones.geojson").body();
            }
            case "Database-create-table"-> {
                // create the table by table name
                return create_query(Additional_data, "create") ? "Successfully created!" : "Failed to create table!";
            }
            case "Database-drop-table"->{
                // drop the table by table name
                assert !Additional_data.isEmpty();
                return create_query(Additional_data,"drop")?"Successfully dropped!":"Failed to drop table!";
            }
            case "Database-select-table"->{
                // select the table by table name
                assert !Additional_data.isEmpty();
                return create_query(Additional_data,"select")?"Successfully selected!":"Couldn't find anything!";
            }
            case "Database-write-table" ->{
                // write to the table by table name
                if(Additional_data!=null){
                    return create_query(Additional_data,"write")?"Successfully written!":"Something went wrong";
                }
                else {
                    System.err.println("No information in the data block!");
                    System.exit(101);
                }
            }
            default->{
                System.err.println("Not a valid request!");
                System.exit(1); }
        }
     return "Failed to query";
    }

    /**
     * Write to the table flight path with arguments given after each movement of the drone
     * @param flightpath  table name
     * @param statement statement used to insert
     * @param orderno order number
     * @param longitude current longitude
     * @param latitude current latitude
     * @param angle angle between current and next point
     * @param longitude2 next longitude
     * @param latitude2 next latitude
     */
    public void write_flightpath_table(String flightpath, String statement, String orderno, String longitude
            , String latitude, String angle, String longitude2, String latitude2) {
        ArrayList<String> data = new ArrayList<>();
        data.add(flightpath);
        data.add(statement);
        data.add(orderno);
        data.add(longitude);
        data.add(latitude);
        data.add(angle);
        data.add(longitude2);
        data.add(latitude2);
        request("Database-write-table",data);
    }

    /**
     * Write to deliveries table after each order has been delivered
     * @param orderno order number
     * @param deliveredTo delivery destination
     * @param cost delivery cost of the order
     * @param statement the statement to be executed
     */
    public void write_deliveries_table( String orderno,String deliveredTo,String cost, String statement
    ){
        ArrayList<String> data = new ArrayList<>();
        data.add("deliveries");
        data.add(statement);
        data.add(orderno);
        data.add(deliveredTo);
        data.add(cost);
        request("Database-write-table", data);
    }
}



