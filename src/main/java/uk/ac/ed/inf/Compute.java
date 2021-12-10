package uk.ac.ed.inf;
import org.jgrapht.Graph;
import org.jgrapht.alg.tour.GreedyHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The class that does all the computation that is needed to calculate the path of the drone <br>
 * Steps taken in this module/algorithm : <br>
 * <ul>
 *     <li><b>Greedy TSP </b> :Computes the Permutation/the order in which to do the deliveries (JGraphT library is used for TSP)</li>
 *     <li><b>Set Source and Compute Internal Path</b> : The Path is Computed Internally after the Source is set
 *     meaning A* greedy is computed at each time the source is step and the path from that is used for the next Source.</li>
 *     <li><b>Check if bounded </b> : Drone has to have path below 1500 moves, if not bounded, Apply <b>Greedy Knapsack</b> and go back to Step 2 </li>
 *     <li><b>End </b> : if it reached this step, store all the results and commit.</li>
 * </ul>
 */
public class Compute {
    // Hashmap which stores the cost of each item in the menu
    private static HashMap<String,Integer> cost=new HashMap<>();
    // Hashmap which stores the location of each restaurant
    private static HashMap<String,String> location = new HashMap<>();
    // Arraylist which stores the destination of each order
    private static ArrayList<String> delivery_destination = new ArrayList<>();
    // Arraylist which stores the order_no of each order
    private static ArrayList<String> order_no = new ArrayList<>();
    /* ArrayList which stores each customer and their info, delivery destination, order no etc.
    The goal of this array list is to keep everything stored in one place.
     */
    private static ArrayList<Internal_path> customers = new ArrayList<>();
    // Stores the external permutation i.e. the order in which the drone goes about with the deliveries
    private List<Integer> External_Permutation;
    // The monetary value earned by the drone
    private static int monetary_value;
    private   static  final LongLat AT = new LongLat(-3.186874,55.944494);

    /**
     * Getter function which returns all the customers.
     * @return the permutation
     */
    public  ArrayList<Internal_path> getCustomers() {
        return customers;
    }

    /**
     * Getter function which returns the permutation (the order in which the deliveries are done).
     * @return the ArrayList of customers i.e. arraylist of internal path object which stores info about each customer/order
     */
    public List<Integer> getExternal_Permutation() {
        return External_Permutation;
    }

    /**
     * The Final Path that is used for the geojson file
     */
    public static Vector<LongLat> Path;

    /**
     * Setter function used by Initialise module to set Computation cost data structure.
     * @param cost1  the cost data structure calculated beforehand
     */
    public static void setCost(HashMap<String, Integer> cost1) {
        cost = cost1;
    }

    /**
     * Sets the location of the Location data structure which is queried and set by Initialise module.
     * @param location1 the location data structure calculated beforehand
     */
    public static void setLocation(HashMap<String, String> location1) {
        location = location1;
    }

    /**
     * Sets the delivery destination data structure which is queried and set by Initialise module.
     * @param delivery_destination1 the delivery destination data structure calculated beforehand
     */
    public static void setDelivery_destination(ArrayList<String> delivery_destination1) {
        delivery_destination = delivery_destination1;
    }

    /**
     * Sets the order number ArrayList which is queried and set by Initialise module.
     * @param order_no1 order number Arraylist calculated beforehand
     */
    public static void setOrder_no(ArrayList<String> order_no1) {
       order_no = order_no1;
    }

    /**
     * Getter function which returns the location from the hash table.
     * @param s the string to look up
     * @return the value of the look-up
     */
    public static String getLocation(String s) {
        return location.get(s);
    }

    /**
     *  Calculates the total delivery cost from the order given
     *  sends a GET request to the site to get the menu,
     *  a JSON file is received which is parsed and iterated to find the total cost.
     * @param items : all the items that have been ordered
     * @return : an integer which is the total cost of all the things that have been ordered
     */
    public int getDeliveryCost(ArrayList<String> items)  {
        // default delivery cost
        int cost1 =50;
        for (String item : items) {
            cost1 += cost.get(item);
        }
        return cost1;
    }

    /**
     * Queries the database for details about the specific order number, <br>
     * i.e. the orders of a particular order number queried from the order-details table.
     * @param orderno The order number that is needed for the querying
     * @param web  This object helps us send web requests and sql queries
     * @return The orders made by the order number
     */
    private ArrayList<String> get_order_details(String orderno, Website web){
        // our sql statement
        String statement = "select * from orderdetails where orderno=(?)";
        // once again we pack our additional data in the compact form
        ArrayList<String> data = new ArrayList<>();
        data.add("orderdetails");
        data.add(statement);
        data.add(orderno);
        // data structure that will be storing our results
        ArrayList<String> orders = new ArrayList<>();
        // sending the request from our web object
        String s = web.request("Database-select-table",data);
        if(s.equals("Successfully selected!")){
            // results will always be written to set, (if it is a prepared statement)
            ResultSet rs = web.getSet();
            // standard sql quering
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

    /**
     * Empty graph builder class for jgrapht.
     * @return an empty graph
     */
    private static Graph<Integer, DefaultWeightedEdge> buildEmptySimpleGraph()
    {return GraphTypeBuilder
                .<Integer, DefaultWeightedEdge> undirected().allowingMultipleEdges(true)
                .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();
    }

    /**
     * Calculates the TSP and returns the permutation in
     * which the orders have to go through, in order for it
     * to delivery, uses greedy Heuristic to calculate the permutation.
     * @return Permutation of the order in which delivery must take place
     */
    private static List<Integer> TSP_external_path(){
        // a safety measure to make sure we have stuff calculated beforehand
        if(customers==null){
            System.err.println("Cannot resolve paths, if the Arraylist is empty");
            System.exit(-1);
        }
        // we need a complete graph in order to compute TSP, so we shall do just that
        Graph<Integer, DefaultWeightedEdge> completeGraph = buildEmptySimpleGraph();
        completeGraph.addVertex(0);
        // adds all the vertices
        for (int i = 0; i < customers.size(); i++) {
            completeGraph.addVertex((i+1));
        }
        // adds the edge between each and every vertex
        for (int i = 0; i < customers.size(); i++) {
            // the starting vertex is always AT, so we compute that with every other vertex
            Internal_path I = customers.get(i);
            // an approximate heuristic distance
            double dist = I.heuristic_shortest_internal_distance(AT,false);
            // adds an edge and edge weight (the heuristic distance)
            completeGraph.addEdge(0,(i+1));
            completeGraph.setEdgeWeight(0,(i+1), dist);
            for (int j = i+1; j < customers.size(); j++) {
                // does the same with current vertex with all the other vertex
                Internal_path J = customers.get(j);
                double dist_2 = J.heuristic_shortest_internal_distance(I.Destination,false);
                completeGraph.addEdge((i+1),(j+1));
                completeGraph.setEdgeWeight((i+1), (j+1),dist_2);
            }
        }
        // The greedyTSP module by JgraphT
        GreedyHeuristicTSP<Integer,DefaultWeightedEdge> E = new GreedyHeuristicTSP<>();

        return E.getTour(completeGraph).getVertexList();
    }

    /**
     * Set the sources of each Customer, which internally computes the path within the class whose result is used for the next iteration.
     * @param T1  the permutation that tells us what delivery sources are
     */
    public void setSources(List<Integer> T1){
        // remove first and Last vertex since, AT does not have a source, makes coding it easier (more of a convenience thing)
        T1.remove(0);
        T1.remove(T1.size()-1);
        for (int i = 0; i < T1.size()-1; i++) {
            // the permutation was calculated i+1, as AT was vertex 0,we just subtract 1 to get the actual index
            int perm =T1.get(i)-1;
            int perm_2 = T1.get(i+1)-1;
            // get the current customer
            Internal_path I = customers.get(perm);
            if(i==0){
                // first order's source is always AT
                I.setSource(AT);
            }
            // sets its source which results in the path being calculated
            customers.get(perm_2).setSource(I.Path.get(I.Path.size()-1));
        }
    }

    /**
     * Greedy Knapsack algorithm which sorts the customers by value and removes the ones that
     * whose ratio is lower.
     */
    private void remove_lowest(){
        // calculate the ratio for each customer and set them
        for(Internal_path customer: customers){
            double d= customer.cost/(double)(customer.moves);
            customer.setWeight(d);
        }
        // sort them in reverse order by their ratio
        customers.sort(Collections.reverseOrder());
        // index for the while loop
        int i =0;
        // the upper bound we have been given
        int M = 1500;
        // the result/new filtered customer list that will be considered for the calculation
        ArrayList<Internal_path> new_customer = new ArrayList<>();
        // main loop of the greedy knapsack
        while (i<customers.size()){
            if(customers.get(i).moves<=M){
                M = M-customers.get(i).moves;
                new_customer.add(customers.get(i));
            }
            i = i+1;
        }

        customers = new_customer;
    }

    /**
     * The final path is computed the whole idea of this part is to connect between the orders and check whether
     * we can proceed to the final stage or not.
     * @param size the size of the customers
     * @return returns the final path if it exists and obeys all the bounds, otherwise null which means asks program to recalculate again
     */
    private Vector<LongLat> get_final_path(int size){
        // the final total Path
        Vector<LongLat> V = new Vector<>();
        // variables keeping track of total moves and cost so, it does not exceed the bound (total moves)
        int total_moves = 0;
        int total_cost = 0;
        for (int i=0;i<size;i++) {
            // get customer by their permutation
            int perm = External_Permutation.get(i)-1;
            Internal_path customer = customers.get(perm);
            // number of moves by a customer and cost
            total_moves += customer.moves;

            total_cost+=customer.cost;
            // checks if total moves below 1500
            if(total_moves<=1500) {
                V.addAll(customer.Path);
            }
            else{
                return null;
            }
            if(i==size-1){
                // if we reached the end, success but one last verification to see if it can actually return to AT
                Vector<LongLat> U = customers.get(perm).Path.get(customers.get(perm).Path.size()-1).path(AT);
                if((total_moves+ U.size()-1>1500)){
                    return null;
                }
                // if reached here successfully computed the final path
                V.addAll(U);
                // let's add a customer AT, whose only purpose is to specify to return (makes it easy for computation)
                Internal_path AT = new Internal_path("AT",U);
                customers.add(AT);
                External_Permutation.add(customers.size());
                break;
            }
        }
        // monetary value computed
         monetary_value=total_cost;
         return V;
    }

    /**
     * Total monetary value of all the customers.
     * @return the total monetary value of all the orders
     */
    public int get_total_monetary_value(){
        // total
        int total = 0;
        // very basic loop iterating through the customers and computing the cost
        for(Internal_path customer : customers){
            total+=customer.cost;
        }
        return total;
    }

    /**
     * The constructor that does all the work, mentioned, it iterates through the orderno and creates
     * a Internal path object and stores all the objects in a ArrayList
     * @param I  Initialise object we used earlier in the program
     */
    public Compute(Initialise I){
        // a static field that should be the same for all objects
        Internal_path.setWeb(I.getWeb());
        // iterates through the order and creates an object of Internal Path which is an important class
        for (int i = 0; i < order_no.size() ; i++) {
            // get the order number and delivery destination
            String current_order_no = order_no.get(i);
            String current_destination = delivery_destination.get(i);
            // get the list of orders
            ArrayList<String> orders= get_order_details(current_order_no,I.getWeb());
            int cost = getDeliveryCost(orders);
            // Create the new object and add it to the arraylist
            Internal_path single_customer = new Internal_path(current_order_no,current_destination,orders,cost);
            customers.add(single_customer);
        }
        // let's compute the total cost,moves before we remove stuff from the computer
        int total_monetary_value = get_total_monetary_value();
        int total_orders = customers.size();
        // Let's try computing once before we go into the loop
        External_Permutation= TSP_external_path();
        setSources(External_Permutation);
        Vector<LongLat> final_path=get_final_path(External_Permutation.size());
        // this loop tries to find until it finds a final path, potential to get stuck in an infinite loop if conditions are never met
          while (final_path==null){
              remove_lowest();
              External_Permutation= TSP_external_path();
              setSources(External_Permutation);
             final_path = get_final_path(External_Permutation.size());

          }
        // store the results
        Path = final_path;
        // Print all the results neatly
        System.out.println("-------------------RESULTS---------------------");
        System.out.println("Moves used :" + (Path.size()-1) + "/" + "1500");
        System.out.println("Money gained " +monetary_value + "/" + total_monetary_value + "  ("+ ((double) monetary_value*100/ total_monetary_value)+" %)");
        System.out.println("Orders Delivered: " + (customers.size()-1) + "/" + total_orders);
        System.out.println("----------------------------------------------");

    }
}
