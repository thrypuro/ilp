package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.mapbox.geojson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Internal_path implements Comparable<Internal_path>{
    Website web;
    //
    LongLat Source;
    LongLat Destination;
    ArrayList<LongLat> midpoints = new ArrayList<>();
    ArrayList<Integer> internal_Permutation;
    Vector<LongLat> Path;
    ArrayList<Integer> angles = new ArrayList<>();
    int moves;
   //
    // public  static  final LongLat AT = new LongLat(-3.186874,55.944494);
    double weight;

    // Other information
    public String order_no;
    ArrayList<String> restaurants = new ArrayList<>();
    ArrayList<String> orders;
    String destination_location;
    int cost;

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Internal_path(String order_no, String destination_location, ArrayList<String> orders, Website web
            , int cost){
        this.order_no = order_no;
        this.destination_location = destination_location;
        this.orders = orders;
        this.web = web;
        this.cost = cost;
        set_coordinates();
    }
    public Internal_path(String order_no,Vector<LongLat> Path){
        this.order_no = order_no;
        this.Path = Path;
    }
    public LongLat get_coordinates(Website web,String s){
        ArrayList<String> data = new ArrayList<>();
        data.add(s);
        String response = web.request("Location",data);
        /* Initialises new GSON for parsing
         */
        Gson gson = new Gson();
        Details places =
                gson.fromJson(response, Details.class);
        return new LongLat(places.coordinates.lng,places.coordinates.lat);

    }
    public static double H(LongLat source,LongLat end){
        double dx,dy,D,D2;
        dx = Math.abs(source.longitude-end.longitude);
        dy = Math.abs(source.latitude-end.latitude);
        D = 0.00015;
        D2 = Math.sqrt(2)*D;
        double dist = D * (dx + dy) + (D2 - 2 * D) * Math.min(dx, dy);
        dist = dist*1.1;
        return dist;
    }
    public void setSource(LongLat source) {
        Source = source;
        shortest_internal_distance(Source,true);
        calculate_path();

    }

    public void calculate_path(){
        Vector<LongLat> current_path = new Vector<>();
        for (int i = 0; i < internal_Permutation.size(); i++) {
            int current_perm = internal_Permutation.get(i);
            if(i==0){
              current_path.addAll(Source.path(midpoints.get(current_perm)));
              if(internal_Permutation.size()==2){
                  int next_perm = internal_Permutation.get(i+1);
                  current_path.addAll(current_path.get(current_path.size()-1).path(midpoints.get(next_perm)));
              }
            }
            if(i==internal_Permutation.size()-1){
                current_path.addAll(current_path.get(current_path.size()-1).path(Destination));
            }
        }
        Path = current_path;
        moves = Path.size();
    }
    public double shortest_internal_distance(LongLat source,boolean setPerm){
        double dist= Double.POSITIVE_INFINITY;
        int k=0;
        for (int i = 0; i < midpoints.size(); i++) {
          double temp_dist=0;
            ArrayList<Integer> temp_internal_perm= new ArrayList<>();

            temp_dist+=H(source, midpoints.get(i));
            temp_internal_perm.add(i);
            for (int j = 0; j < midpoints.size(); j++) {
             if(i==j){
                 continue;
             }
                temp_dist+=H(midpoints.get(i), midpoints.get(j));
                temp_internal_perm.add(j);
              k = j;
            }
            temp_dist+=H(midpoints.get(k),Destination);
            if(temp_dist<dist){
                dist=temp_dist;
                if(setPerm){
                internal_Permutation = temp_internal_perm; }
            }
        }

        return dist;
    }
    public void set_coordinates(){
        this.Destination = get_coordinates(web,this.destination_location);

        for (String s : orders) {
            String order_location = Compute.getLocation(s);

            if(restaurants.contains(order_location)){
                continue;
            }
            restaurants.add(order_location);
            LongLat L = get_coordinates(web,order_location);
            assert  L!=null;
            midpoints.add(L);

        }
    }
    public static String make_geo_json(Vector<LongLat> V){

        List<Point> Line = new ArrayList<>();
        for (LongLat longLat : V) {
            Point Poi = Point.fromLngLat(longLat.longitude, longLat.latitude);
            Line.add(Poi);

        }
        LineString L = LineString.fromLngLats(Line);
        Feature Fea = (Feature) Feature.fromGeometry((Geometry) L);
        FeatureCollection Cole = FeatureCollection.fromFeature(Fea);
        return Cole.toJson();
    }
    @Override
    public int compareTo(Internal_path o) {
        return Double.compare(this.weight, o.weight);
    }
}
