package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    public static ArrayList<String> delivery_destination = new ArrayList<>();
    public static ArrayList<String> order_no = new ArrayList<>();
    public static ArrayList<ArrayList<String>> orders = new ArrayList<>();
    public static void empty_lists(){
        delivery_destination.clear();
        order_no.clear();
        orders.clear();
    }
    public static void orders_query_date(String date){

        try {
            // Sends the GET request to the site
            Connection conn = DriverManager.getConnection("jdbc:derby://127.0.0.1:9899/derbyDB");
// Create a statement object that we can use for running various
// SQL statement commands against the database.
            Statement statement = conn.createStatement();
            final String orderdate =
                    "select * from orders where deliveryDate=(?)";
            PreparedStatement psCourseQuery =
                    conn.prepareStatement(orderdate);
            psCourseQuery.setString(1, date);

            ResultSet rs = psCourseQuery.executeQuery();

            while (rs.next()) {
                String course = rs.getString("deliverto");
                String order = rs.getString("orderno");
                delivery_destination.add(course);
                order_no.add(order);
            }

        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }
    public static void orders_detail_query_orders(){
       if(!order_no.isEmpty()&&!delivery_destination.isEmpty()){
           try {

               // Sends the GET request to the site
               Connection conn = DriverManager.getConnection("jdbc:derby://127.0.0.1:9899/derbyDB");
// Create a statement object that we can use for running various
// SQL statement commands against the database.
               Statement statement = conn.createStatement();
               for (int i = 0; i < order_no.size() ; i++) {
                   ArrayList<String> single_customer_order = new ArrayList<>();
                   final String orderdate =
                           "select * from orderdetails where orderno=(?)";
                   PreparedStatement psCourseQuery =
                           conn.prepareStatement(orderdate);
                   psCourseQuery.setString(1, order_no.get(i));

                   ResultSet rs = psCourseQuery.executeQuery();

                   while (rs.next()) {
                       String item = rs.getString("item");
                       single_customer_order.add(item);
                   }
                   orders.add(single_customer_order);
               }

           } catch ( SQLException e) {
               e.printStackTrace();
           }
       }
       else{
           System.err.println("Cannot query an empty Array!");
       }
    }
    public static void main(String[] args) {
       orders_query_date("2022-01-01");
       orders_detail_query_orders();
        System.out.println(orders);
    }
}
