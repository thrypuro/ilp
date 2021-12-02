package uk.ac.ed.inf;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Website {
    public  static  String machine;
    public  static String site_port;
    public  static String database_port;
    public  static String site_urlString;
    public  static String database_urlString;
    public static boolean flight_table_created =false;
    public static boolean deliveries_table_created =false;
    private static final String flight_path_create = "create table flightpath(orderNo char(8)," +
            "fromLongitude double," +
            "fromLatitude double," +
            "angle integer," +
            "toLongitude double," +
            "toLatitude double)";
    private static final String deliveries_table_create = "create table deliveries(orderNo char(8)," + "deliveredTo varchar(19)," +
            "costInPence int)";
    private static final String[] table_create = {flight_path_create, deliveries_table_create,"Already created!", "Already created!"};
    public    ResultSet set;


    public ResultSet getSet() {
        return set;
    }

    //  One HttpClient shared between all HttpRequests, to avoid repeated thread starts
    private static final HttpClient client = HttpClient.newHttpClient();

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
    public Website(String machine1,String port1,String port2){
        machine = machine1;
        site_port = port1;
        database_port = port2;
        site_urlString = "http://"+machine+":"+site_port;
        database_urlString = "jdbc:derby://"+machine+":"+database_port+"/derbyDB";
    }
    private ResultSet execute_query_prepared(String statement_str, ArrayList<String> values,String query){
        Connection conn = connect_to_database();
        try {
            assert Arrays.stream(statement_str.split("[?]+")).count() == values.size();
            PreparedStatement prepared_statement =
                    conn.prepareStatement(statement_str);
            for (int i = 0; i < values.size(); i++) {
                prepared_statement.setString(i+1, values.get(i));
            }
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

    private void execute_query_Unprepared(String statement_str) {
        Connection conn= connect_to_database();

        try {
            Statement statement = conn.createStatement();
           statement.execute(statement_str);
        }catch ( SQLException e) {
            System.err.println("Error with the Unprepared statement!");
            e.printStackTrace();

        }
    }
    public void query(int index, String query,ArrayList<String> data){
        String statement = "";
        if(!(index != 2 && index != 3 || query.equals("select"))){
            query = "invalid";
        }
        switch (query) {
            case "create" -> {
                statement = table_create[index];
                execute_query_Unprepared(statement);

            }
            case "drop" -> {

                statement = "drop table "+data.get(0);
                execute_query_Unprepared(statement);

            }
            case "write", "select" -> {
                assert data.size()>2;
                statement = data.get(1);
                ArrayList<String> V= new ArrayList<>();
                for (int i = 2; i <data.size(); i++) {
                    V.add(data.get(i));
                }
                set = execute_query_prepared(statement,V,query);
            }
            default -> System.err.println("Please enter a valid sql query");
        }
    }
    private boolean create_query(ArrayList<String> data,String query){ // can easily add new sql command
        boolean query_success=  false;
        String statement="";
        if(data==null){
            System.err.println("Cannot query!");
            System.exit(1);
        }
        switch (data.get(0)) {
            case "flightpath"-> {
                boolean can_query = (query.equals("create") && !flight_table_created) || ((query.equals("drop")|| query.equals("write"))  && flight_table_created);
                if(can_query){
                query(0, query, data);
                    flight_table_created= !query.equals("drop");
                }

            }
            case "deliveries"-> {
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
        query_success=true;
        return query_success;
    }

    public String request(String request, ArrayList<String> Additional_data){ /// easily able to add new options and features if website changes
        switch (request){
            case "Menus" ->{
                return get_request("/menus/menus.json").body();
            }
            case "Location"->{
                if(Additional_data==null){
                    break;
                }
                String  site_index = Additional_data.get(0).replace(".","/");
                return get_request("/words/"+site_index+"/details.json").body(); }
            case "Polygon" ->  {
                return get_request("/buildings/" + "no-fly-zones.geojson").body();
            }
            case "Database-create-table"-> {

                return create_query(Additional_data, "create") ? "Successfully created!" : "Failed to create table!";
            }
            case "Database-drop-table"->{
                assert !Additional_data.isEmpty();
                return create_query(Additional_data,"drop")?"Successfully dropped!":"Failed to drop table!";
            }
            case "Database-select-table"->{
                assert !Additional_data.isEmpty();
                return create_query(Additional_data,"select")?"Successfully selected!":"Couldn't find anything!";
            }
            case "Database-write-table" ->{
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



