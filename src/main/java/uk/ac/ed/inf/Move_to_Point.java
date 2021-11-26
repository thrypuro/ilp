package uk.ac.ed.inf;
import com.mapbox.geojson.*;

import java.util.*;
public class Move_to_Point {
    public static LongLat Landmark_1 = new LongLat(-3.1862,55.9457);
    public  static LongLat Landmark_2 = new LongLat(-3.1916,55.9437);

    static public boolean in_Polygon(LongLat P){
        boolean inside = false;
        Winding_number.Pointloc P1 = new Winding_number.Pointloc(P.longitude,P.latitude);

        for(int i=0;i<Winding_number.Polygons.size();i++){
            inside=inside||Winding_number.isInside(Winding_number.Polygons.get(i),P1);
        }
        return inside;
    }
    static class  Point_PQ implements Comparable<Point_PQ>
    {
        double x;
        double y;
        double fS;
        public Point_PQ(double x,double y,double fS){
            this.x = x;
            this.y = y;
            this.fS = fS;
        }

        @Override
        public int compareTo(Point_PQ o) {
            if(this.fS>o.fS){
                return 1;
            }
            else if (this.fS<o.fS) {
                return -1;
            }
            else{
            return 0; }
        }
    }

    public static Vector<LongLat> reconstruct_path(HashMap<Point_PQ, Point_PQ> cameFrom, Point_PQ current){
        Vector<LongLat> total_path = new Vector<>();
        LongLat current_L = new LongLat(current.x,current.y);
        total_path.add(current_L);
        while(!cameFrom.isEmpty()){
            current = cameFrom.get(current);
            if(current==null){

                break;
            }
            LongLat current_L1 = new LongLat(current.x,current.y);
            total_path.add(0,current_L1);
        }
        return total_path;

    }
    public static double H(LongLat source,LongLat end){
        double dx,dy,D,D2;
        dx = Math.abs(source.longitude-end.longitude);
        dy = Math.abs(source.latitude-end.latitude);
        D = 0.00015;
        D2 = Math.sqrt(2)*D;
        double dist = D * (dx + dy) + (D2 - 2 * D) * Math.min(dx, dy);
        return dist;
    }

    public static Vector<LongLat> A_star_modified(LongLat start,LongLat goal, boolean Euc){
        Vector S = null;
        Vector<Double> Lala = new Vector<Double>();
        double D = 0.00015;
        Lala.add(start.longitude);
        Lala.add(start.latitude);
        double score = H(start,goal);
        if(Euc){
            score = D*start.distanceTo(goal);

        }
        score = score*1.1;
        boolean bruh=false;


        Point_PQ start1 =  new Point_PQ(start.longitude, start.latitude,score);
        //camefrom
        HashMap<Point_PQ, Point_PQ> cameFrom = new HashMap<>();
        //openSet
        PriorityQueue<Point_PQ> openSet = new PriorityQueue<>();
        Set<Vector<Double>> openset_set = new HashSet<>();
        openSet.add(start1);
        openset_set.add(Lala);
        //closedSet
        Set<Vector<Double>> closetSet = new HashSet<>();
        // gscore
        HashMap<Vector<Double>, Double> gScore = new HashMap<>();
        gScore.put(Lala,0.0);
        // fscore
        HashMap<Point_PQ, Double> fScore = new HashMap<>();
        fScore.put(start1,score);
        int Heu = 0;
        int sols = 0;
        Point_PQ Smallest=null;
        while (!openSet.isEmpty()){
            Point_PQ current = openSet.remove();

            LongLat L = new LongLat(current.x,current.y);
            Vector<Double> cuLala = new Vector<>();
            cuLala.add(current.x);
            cuLala.add(current.y);
            closetSet.add(cuLala);
            openset_set.remove(cuLala);
            if(L.closeTo(goal)){

                return reconstruct_path(cameFrom,current);
            }
            for(int i=0;i<360;i=i+10){
                LongLat current_neghbor = L.nextPosition(i);
                Vector<Double> Lala1 = new Vector<Double>();
                Lala1.add(current_neghbor.longitude);
                Lala1.add(current_neghbor.latitude);
                double tenative_gScore = gScore.get(cuLala)+0.00015*Math.tan(Math.toRadians(i));
                boolean away_from_no_fly_zone=false;

                double dist = H(current_neghbor,goal);
                if(Euc){
                    dist = D*current_neghbor.distanceTo(goal);
                }
                dist = dist*1.1;
                double temp_fscore=dist+tenative_gScore;
                for (int j = 0; j <360 ; j=j+60) {
                    away_from_no_fly_zone= away_from_no_fly_zone||(in_Polygon(current_neghbor.shorter(j)));
                }

                if(away_from_no_fly_zone){
                    Heu+=1;
                    if(Heu==5000){
                        return S;
                    }
                    continue;
                }
                if(!current_neghbor.isConfined() ){
                    continue;
                }

                if( in_Polygon(current_neghbor) ){
                    continue;
                }
                if(closetSet.contains(Lala1) && current.fS>=temp_fscore){

                    continue;
                }
                if( gScore.get(Lala1) == null){
                    Point_PQ P = new Point_PQ(current_neghbor.longitude,current_neghbor.latitude,dist);
                    cameFrom.put(P,current);
                    gScore.put(Lala1,tenative_gScore);
                    fScore.put(P,dist+tenative_gScore);
                    if(!openset_set.contains(Lala1)){
                        openSet.add(P);
                        openset_set.add(Lala1);
                    }
                }
                else if(tenative_gScore<gScore.get(Lala1) ){
                    Point_PQ P = new Point_PQ(current_neghbor.longitude,current_neghbor.latitude,dist);
                    cameFrom.put(P,current);
                    gScore.put(Lala1,tenative_gScore);
                    fScore.put(P,dist+tenative_gScore);
                    if(!openset_set.contains(Lala1)){
                        openSet.add(P);
                        openset_set.add(Lala1);

                    }

                }
            }
        }
        return S;
    }
    public static String make_geo_json(Vector<LongLat> V){
        List<Point> Line = new ArrayList();
        for(int i =0;i<V.size();i++){
            Point Poi = Point.fromLngLat(V.get(i).longitude, V.get(i).latitude);
            Line.add(Poi);
        }
        LineString L = LineString.fromLngLats(Line);
        Geometry G = (Geometry) L;
        Feature Fea = (Feature) Feature.fromGeometry(G);
        FeatureCollection Cole = FeatureCollection.fromFeature(Fea);
        return Cole.toJson();
    }
    public static Vector<LongLat> extend_line_String(Vector<LongLat> V,Vector<LongLat> U ){
        if(U==null){
            return V;
        }
        for (int i = 0; i <U.size(); i++) {
            V.add(U.get(i));

        }
        return V;
    }
    public static Vector<LongLat> best_of_2(LongLat P,LongLat Q){
        Vector<LongLat> V = A_star_modified(P,Q,false);
        Vector<LongLat> U = A_star_modified(P,Q,true);
        if(U!=null){
            if(V==null){
                V=U;
            }
            else if(U.size()<V.size()){
                V=U;
            }
        }
        return V;
    }
    public static Vector<LongLat> get_path(LongLat P,LongLat Q){

        Vector<LongLat> V = best_of_2(P,Q);
        double dist1,dist2,ldist1,ldist2;
        dist1 = P.distanceTo(Landmark_1);
        ldist1 = Landmark_1.distanceTo(Q);
        dist2 = P.distanceTo(Landmark_2);
        ldist2 = Landmark_2.distanceTo(Q);

        boolean foundpath = true;

        if(V==null ){
            foundpath=false;


            Vector<LongLat> V1;

            if(dist1+ldist1<dist2+ldist2){
            V = best_of_2(P,Landmark_1);

            if(V!=null){
            V1 = best_of_2(V.get(V.size()-1),Q);
            V=extend_line_String(V,V1); }
                foundpath= V != null;
            }

             else {
                V = best_of_2(P,Landmark_2);
                if(V!=null){
                Vector<LongLat> V2;
                V2 =  best_of_2(V.get(V.size()-1),Q);
                V=extend_line_String(V,V2);
                foundpath= V != null && V2 != null;}
            }
             if(!foundpath){
               System.err.println("NO PATH FOUND!!");
           }


            return  V;

        }
        else {
            return  V;
        }
    }
    public static void main( String[] args ){
        LongLat P = new LongLat(  	-3.191065, 55.945626);
        LongLat Q = new LongLat(  -3.18933, 55.943389);
        LongLat Q1 = new LongLat(  -3.1861, 55.9447);
        LongLat Q2 = new LongLat(-3.1893,55.9434);
        LongLat Q3 = new LongLat(-3.1887,55.9459);
        Winding_number.make_Polygons();
        Vector<LongLat> V=get_path(P,Q);
        Vector<LongLat> V1 = get_path(V.get(V.size()-1),Q1);
        Vector<LongLat> V2 = get_path(V1.get(V1.size()-1),Q2);
        Vector<LongLat> V3 = get_path(V2.get(V2.size()-1),Q3);
        Vector<LongLat> V4 = get_path(V3.get(V3.size()-1),Landmark_1);
        //extend_line_String(extend_line_String(extend_line_String(V,V1),V2),V3)


        System.out.println(V);
        System.out.println(V1);
        System.out.println();
        System.out.println(make_geo_json(V));


    }
}
