package uk.ac.ed.inf;
import org.jgrapht.Graph;
import org.jgrapht.alg.tour.GreedyHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Compute {
    public static HashMap<String,Integer> cost=new HashMap<>();
    public static HashMap<String,String> location = new HashMap<>();
    public static ArrayList<String> delivery_destination = new ArrayList<>();
    public static ArrayList<String> order_no = new ArrayList<>();
    public static ArrayList<Internal_path> customers = new ArrayList<>();
    public List<Integer> External_Permutation;
    public static int total_monetary_value;
    public static int monetary_value;
    public  static  final LongLat AT = new LongLat(-3.186874,55.944494);
    public static Vector<LongLat> return_to_AT = new Vector<>();
    public  ArrayList<Internal_path> getCustomers() {
        return customers;
    }

    public List<Integer> getExternal_Permutation() {
        return External_Permutation;
    }

    public static Vector<LongLat> Path;
    public static void setCost(HashMap<String, Integer> cost1) {
        cost = cost1;
    }

    public static void setLocation(HashMap<String, String> location1) {
        location = location1;
    }

    public static void setDelivery_destination(ArrayList<String> delivery_destination1) {
        delivery_destination = delivery_destination1;
    }

    public static void setOrder_no(ArrayList<String> order_no1) {
       order_no = order_no1;
    }

    public static String getLocation(String s) {
        return location.get(s);
    }

    /**
     *  Calculates the total delivery cost from the order given
     *  sends a GET request to the site to get the menu,
     *  a JSON file is received which is parsed and iterated to find the total cost
     * @param items : all the items that have been ordered
     * @return : an integer which is the total cost of all the things that have been ordered
     */
    public int getDeliveryCost(ArrayList<String> items)  {
        int cost1 =50;
        for (String item : items) {
            cost1 += cost.get(item);
        }
        return cost1;
    }
    private ArrayList<String> get_order_details(String orderno, Website web){
        String statement = "select * from orderdetails where orderno=(?)";
        ArrayList<String> data = new ArrayList<>();
        data.add("orderdetails");
        data.add(statement);
        data.add(orderno);
        ArrayList<String> orders = new ArrayList<>();
        String s = web.request("Database-select-table",data);
        if(s.equals("Successfully selected!")){
            // results will always be written to set
            ResultSet rs = web.getSet();
            try {
                while (rs.next()){
                    String item = rs.getString("item");
                    orders.add(item);
                }
            }
            catch ( SQLException e) {
                e.printStackTrace();
            }
        }
        return orders;
    }
    private static Graph<Integer, DefaultWeightedEdge> buildEmptySimpleGraph()
    {return GraphTypeBuilder
                .<Integer, DefaultWeightedEdge> undirected().allowingMultipleEdges(true)
                .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();
    }

    /**
     * Calculates the TSP and returns the permutation in
     * which the orders have to go through, in order for it
     * to delivery
     * @return Permutation of the order in which delivery must take place
     */
    private static List<Integer> TSP_external_path(){
        if(customers==null){
            System.err.println("Cannot resolve paths, if the Arraylist is empty");
            System.exit(-1);
        }
        Graph<Integer, DefaultWeightedEdge> completeGraph = buildEmptySimpleGraph();
        completeGraph.addVertex(0);
        for (int i = 0; i < customers.size(); i++) {
            completeGraph.addVertex((i+1));
        }
        for (int i = 0; i < customers.size(); i++) {
            Internal_path I = customers.get(i);
            double dist = I.shortest_internal_distance(AT,false);
            completeGraph.addEdge(0,(i+1));
            completeGraph.setEdgeWeight(0,(i+1), dist);
            for (int j = i+1; j < customers.size(); j++) {
                Internal_path J = customers.get(j);
                double dist_2 = J.shortest_internal_distance(I.Destination,false);
                completeGraph.addEdge((i+1),(j+1));
                completeGraph.setEdgeWeight((i+1), (j+1),dist_2);
            }
        }
        GreedyHeuristicTSP<Integer,DefaultWeightedEdge> E = new GreedyHeuristicTSP<>();

        return E.getTour(completeGraph).getVertexList();
    }
    public void setSources(List<Integer> T1){
        T1.remove(0);
        T1.remove(T1.size()-1);
        for (int i = 0; i < T1.size()-1; i++) {
            int perm =T1.get(i)-1;
            int perm_2 = T1.get(i+1)-1;
            Internal_path I = customers.get(perm);
            if(i==0){
                I.setSource(AT);
            }
            customers.get(perm_2).setSource(I.Path.get(I.Path.size()-1));
        }
    }
    public void remove_lowest(){
        for(Internal_path customer: customers){
            double d= customer.cost/(double)(customer.moves);
            customer.setWeight(d);
        }
        customers.sort(Collections.reverseOrder());
        int i =0;
        int M = 1500;
        ArrayList<Internal_path> bruh = new ArrayList<>();
        while (i<customers.size()){
            if(customers.get(i).moves<=M){
                M = M-customers.get(i).moves;
                bruh.add(customers.get(i));
            }
            i = i+1;
        }

        customers = bruh;
    }
    public Vector<LongLat> get_final_path(int size){
        Vector<LongLat> V = new Vector<>();
        int total_moves = 0;
        int total_cost = 0;
        for (int i=0;i<size;i++) {
            int perm = External_Permutation.get(i)-1;
            Internal_path customer = customers.get(perm);
            total_moves += customer.moves;
            Vector<LongLat> U = customers.get(perm).Path.get(customers.get(perm).Path.size()-1).path(AT);
            total_cost+=customer.cost;
            if(total_moves+U.size()-1<=1500) {
                V.addAll(customer.Path);
            }
            else{
                return null;
            }
            if(i==size-1){
                V.addAll(U);
                Internal_path AT = new Internal_path("AT",U);
                customers.add(AT);
                External_Permutation.add(customers.size());
                break;
            }
        }
         monetary_value=total_cost;
         return V;
    }

    public int total_monetary_value(){
        int total = 0;
        for(Internal_path customer : customers){
            total+=customer.cost;
        }
        return total;
    }

    public Compute(Initialise I){
        for (int i = 0; i < order_no.size() ; i++) {
            String current_order_no = order_no.get(i);
            String current_destination = delivery_destination.get(i);
            ArrayList<String> orders= get_order_details(current_order_no,I.getWeb());
            int cost = getDeliveryCost(orders);
            Internal_path single_customer = new Internal_path(current_order_no,current_destination,orders,I.getWeb(),cost);
            customers.add(single_customer);
        }
       total_monetary_value=total_monetary_value();
        int total_orders = customers.size();
        Vector<LongLat> final_path=null;
          while (final_path==null){
           remove_lowest();
           External_Permutation= TSP_external_path();
              setSources(External_Permutation);

             final_path = get_final_path(External_Permutation.size());
          }

        Path = final_path;
        System.out.println("-------------------RESULTS---------------------");
        System.out.println("Moves used :" + (Path.size()-1) + "/" + "1500");
        System.out.println("Money gained " +monetary_value + "/" + total_monetary_value + "  ("+ (monetary_value*100/total_monetary_value)+"%)");
        System.out.println("Orders Delivered: " + External_Permutation.size() + "/" + total_orders);
        System.out.println("----------------------------------------------");

    }
}
