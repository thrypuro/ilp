package uk.ac.ed.inf;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.tour.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.GraphTypeBuilder;

/**
 * Converts the database items into a set of cordinates that is useful by our algorithm
 */
public class Resolve_drone_path {
    private static final HttpClient client = HttpClient.newHttpClient();
    public  static  final LongLat AT = new LongLat(-3.186874,55.944494);
    static class Path_Info{
        LongLat Source;
        LongLat Destination;
        ArrayList<Integer> Permutation;
        double Distance;
        public  Path_Info(LongLat Source,LongLat Destination,ArrayList<Integer>Permutation, double Distance) {
            this.Source = Source;
            this.Destination = Destination;
            this.Permutation = Permutation;
            this.Distance = Distance;
        }
    }
    /**
     * Queries the W3W place to cordinates from the site
     */
    public static LongLat getCordinates(String Location){
     String  site_index = Location.replace(".","/");
        LongLat L=null;
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:9898/words/"+site_index+"/details.json"))
                .build();
        try {
            // Sends the GET request to the site
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            /* Initialises new GSON for parsing
             */
            Gson gson = new Gson();


            Details places =
                    gson.fromJson(response.body(), Details.class);

             L = new LongLat(places.coordinates.lng,places.coordinates.lat);



        } // necessary catch statement if the request to the site failed
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        return L;
    }
    public static double calculate_distance(Vector<LongLat> P){
        return (P.size()-1)*0.00015;
    }


    public static Path_Info resolve_internal_path(LongLat source, LongLat destination, Vector<LongLat> midpoints){
        Path_Info Pinf = null;
        ArrayList<Integer> permutation = new ArrayList<>();
        if(midpoints.size()==2){
            double dist1,dist2;

            dist1 = source.distanceTo(midpoints.get(0))+midpoints.get(0).distanceTo(midpoints.get(1))+midpoints.get(1).distanceTo(destination);

            dist2 = source.distanceTo(midpoints.get(1))+midpoints.get(1).distanceTo(midpoints.get(0))+midpoints.get(0).distanceTo(destination);

            if(dist1<dist2){
                permutation.add(0);
                permutation.add(1);
                 Pinf = new Path_Info(source,destination,permutation,dist1);
            }
            else {
                permutation.add(1);
                permutation.add(0);
                Pinf = new Path_Info(source,destination,permutation,dist2);
            }


        }
        else {

            permutation.add(0);
            double dist;
            dist = source.distanceTo(midpoints.get(0)) + midpoints.get(0).distanceTo(destination);
            Pinf = new Path_Info(source,destination,permutation,dist);

        }
        return Pinf;
    }
    private static Graph<String, DefaultWeightedEdge> buildEmptySimpleGraph()
    {
        return GraphTypeBuilder
                .<String, DefaultWeightedEdge> undirected().allowingMultipleEdges(true)
                .allowingSelfLoops(false).edgeClass(DefaultWeightedEdge.class).weighted(true).buildGraph();
    }
    public static Vector<Integer> resolve_external_paths(LongLat source, Vector<LongLat> dest, Vector<Vector<LongLat>> midpoints){

        Graph<String, DefaultWeightedEdge> completeGraph = buildEmptySimpleGraph();
        completeGraph.addVertex("v"+0);
        for (int i = 0; i < dest.size(); i++) {
            Path_Info Pinf = resolve_internal_path(source,dest.get(i),midpoints.get(i));
            completeGraph.addVertex("v"+(i+1));
            Vector<LongLat> U = resolve_path_int(Pinf,midpoints.get(i));

            completeGraph.addEdge("v"+0,"v"+(i+1));
           completeGraph.setEdgeWeight("v"+0,"v"+(i+1),calculate_distance(U));
        }

        for (int i = 0; i < dest.size(); i++) {
            for (int j = i+1; j < dest.size(); j++) {
                Path_Info Pinf = resolve_internal_path(dest.get(i),dest.get(j),midpoints.get(i));
                completeGraph.addEdge("v"+(i+1),"v"+(j+1));
                completeGraph.setEdgeWeight("v"+(i+1), "v"+(j+1),Pinf.Distance);
            }


         }

        NearestNeighborHeuristicTSP T = new NearestNeighborHeuristicTSP("v0");

        List<String> T1 = T.getTour(completeGraph).getVertexList();
        Vector<Integer> T2 = new Vector<>();
        for (String s : T1) {
            T2.add(Integer.valueOf(s.replace("v", "")));
        }
        // Print out the graph to be sure it's really complete

      return T2;
    }
    public static Vector<LongLat> resolve_path_int(Path_Info P,Vector<LongLat> midpoints){
        Vector<LongLat> La = new Vector<>();
        System.out.println("Hi");
        La=move_to_point.extend_line_String(La,move_to_point.get_path(P.Source,midpoints.get(P.Permutation.get(0))));
        if(P.Permutation.size()==2){
            System.out.println("aaa");
        La=move_to_point.extend_line_String(La,move_to_point.get_path(La.get(La.size()-1),midpoints.get(P.Permutation.get(1))));
            System.out.println(midpoints.get(P.Permutation.get(1)).longitude);
            System.out.println(midpoints.get(P.Permutation.get(1)).latitude);}
        System.out.println("Hi");

        System.out.println(P.Destination.longitude);
        System.out.println(P.Destination.latitude);

        La=move_to_point.extend_line_String(La,move_to_point.get_path(La.get(La.size()-1),P.Destination));


        System.out.println(La);
        return La;
    }
    public static Vector<LongLat> resolve_path(List<Integer> Perm,LongLat AT,Vector<LongLat> dest_lola,Vector<Vector<LongLat>> lola){
        int first = Perm.get(0)-1;
        int cur;
        Path_Info P = resolve_internal_path(AT,dest_lola.get(first),lola.get(first));
        Vector<LongLat> V = resolve_path_int(P,lola.get(first));
        int rest=0;
        for (int i = 0; i < Perm.size(); i++) {
            System.out.println(Perm.get(i));
            cur = Perm.get(i)-1;
            rest+=lola.get(cur).size();
            Path_Info P1 = resolve_internal_path(V.get(V.size()-1),dest_lola.get(cur),lola.get(cur));
            System.out.println("Ha");
            V = move_to_point.extend_line_String(V,resolve_path_int(P1,lola.get(cur)));
        }
        V = move_to_point.extend_line_String(V,move_to_point.get_path(V.get(V.size()-1),AT));
        System.out.println(V.size()+2+ dest_lola.size()+rest);
        return V;
    }
    public static void main(String[] args) {

        long d1=System.currentTimeMillis();
        Menus M = new Menus("localhost","9898");
        Menus.prepare_menus();
        Winding_number.make_Polygons();
        Database.orders_query_date("2023-12-30");
        Database.orders_detail_query_orders();

        Vector<Vector<LongLat>> rest_lola = new Vector<>();
        Vector<LongLat> dest_lola = new Vector<>();
        for (int i = 0; i < Database.orders.size(); i++) {
            Vector<String> rest_str = new Vector<>();
            Vector<LongLat> Current_rest = new Vector<>();
            for (int j = 0; j < Database.orders.get(i).size(); j++) {
                String r = Menus.location.get(Database.orders.get(i).get(j));
                LongLat L = getCordinates(r);
                if(!rest_str.contains(r)){
                  Current_rest.add(L);
                  rest_str.add(r);

                }
            }
            rest_lola.add(Current_rest);

        }
        for (int i = 0; i <Database.delivery_destination.size(); i++) {
            System.out.println(Database.delivery_destination.get(i));
             dest_lola.add(getCordinates(Database.delivery_destination.get(i)));
        }

            List<Integer> external_permutation= resolve_external_paths(AT,dest_lola,rest_lola);
        external_permutation.remove(0);
            external_permutation.remove(external_permutation.size()-1);
        System.out.println(external_permutation);
        System.out.println(move_to_point.make_geo_json(resolve_path(external_permutation,AT,dest_lola,rest_lola)));

           /* for (int i = 0; i <rest_lola.size(); i++) {
              int first=  external_permutation.get(i)-1;

Vector<LongLat> Q = resolve_internal_path(V.get(V.size()-1), dest_lola.get(first),rest_lola.get(first)).Path;
                V = move_to_point.extend_line_String(V,Q);
            }

            System.out.println(move_to_point.make_geo_json(V));*/
        Database.empty_lists();

        long d2=System.currentTimeMillis();
        System.out.println(d2-d1);
    }

}
