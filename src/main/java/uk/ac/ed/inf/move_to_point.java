package uk.ac.ed.inf;
import com.mapbox.geojson.*;

import java.lang.Math;
import java.util.*;

public class move_to_point  {
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
            return 0;
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
            total_path.add(current_L1);
        }
        return total_path;

    }
    public static Vector<LongLat> A_star_modified(LongLat start,LongLat goal){
        Vector S = null;
        double score = start.distanceTo(goal);
        Point_PQ start1 =  new Point_PQ(start.longitude, start.latitude,score);
        //camefrom
        HashMap<Point_PQ, Point_PQ> cameFrom = new HashMap<>();
        //openSet
        PriorityQueue<Point_PQ> openSet = new PriorityQueue<>();
        openSet.add(start1);
        // gscore
        HashMap<Point_PQ, Double> gScore = new HashMap<>();
        gScore.put(start1,0.0);
        // fscore
        HashMap<Point_PQ, Double> fScore = new HashMap<>();
        fScore.put(start1,score);
        int Heu = 0;
        while (!openSet.isEmpty()){
            Point_PQ current = openSet.remove();
            LongLat L = new LongLat(current.x,current.y);

            if(L.closeTo(goal)){

                return reconstruct_path(cameFrom,current);
            }
            if( in_Polygon(L)){
                continue;
            }
            for(int i=0;i<360;i=i+10){
                LongLat current_neghbor = L.nextPosition(i);
                boolean away_from_no_fly_zone=false;
                if(!current_neghbor.isConfined()){
                    continue;
                }
                for (int j = 0; j < 360 ; j=j+60) {
                       away_from_no_fly_zone= away_from_no_fly_zone||(in_Polygon(current_neghbor.nextPosition(j)));
                }
                if( in_Polygon(current_neghbor) ||  away_from_no_fly_zone){
                   break;
                }

                double tenative_gScore = gScore.get(current)+0.00015;
                if(gScore.get(current_neghbor) == null){
                    double D = 0.00015;
                    double D2 = Math.sqrt(0.00015);
                    double dx,dy;
                    dx = Math.abs(current_neghbor.longitude-goal.longitude);
                    dy = Math.abs(current_neghbor.latitude-goal.latitude);
                    double dist;
                    dist=dx+dy;
                    switch (Heu){
                        case 1: dist = dx+dy; break;
                        case  0: dist = current_neghbor.distanceTo(goal); break;
                    }
                    //dist = D*(dx+dy)+(D2-2*D)*Math.min(dx,dy);
                    //dist = dx+dy;
                    Point_PQ P = new Point_PQ(current_neghbor.longitude,current_neghbor.latitude,dist);

                    cameFrom.put(P,current);
                    gScore.put(P,tenative_gScore);
                    fScore.put(P,dist);
                    if(!openSet.contains(current_neghbor)){
                       openSet.add(P);
                    }

                }
                else if(tenative_gScore<gScore.get(current_neghbor)){
                    double dist = current_neghbor.distanceTo(goal);

                    Point_PQ P = new Point_PQ(current_neghbor.longitude,current_neghbor.latitude,dist);

                    cameFrom.put(P,current);
                    gScore.put(P,tenative_gScore);
                    fScore.put(P,dist);
                    if(!openSet.contains(current_neghbor)){
                        openSet.add(P);
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
        System.out.println(L.toJson());
        Feature Fea = (Feature) Feature.fromGeometry(G);
        System.out.println(Fea.toJson());
        FeatureCollection Cole = FeatureCollection.fromFeature(Fea);

        return Cole.toJson();
    }
    public static Vector<LongLat> extend_line_String(Vector<LongLat> V,Vector<LongLat> U ){
        if(U==null){
            return V;
        }
        for (int i = U.size()-1; i >-1; i--) {
            V.add(0,U.get(i));

        }
        return V;
    }
    public static Vector<LongLat> get_path(LongLat P,LongLat Q){

        Vector<LongLat> V = A_star_modified(P,Q);
        System.out.println(V);
        if(V==null){
            V = A_star_modified(P,Landmark_1);
            Vector<LongLat> V1 = A_star_modified(Landmark_1,Q);
            System.out.println(V1);
            if(V==null || V1==null){
                V = A_star_modified(P,Landmark_2);
                Vector<LongLat> V2 = A_star_modified(Landmark_2,Q);
                System.out.println(V1);
                System.out.println(V2);
                V=extend_line_String(V,V2);


            }
            else {
                V=extend_line_String(V,V1);
            }

            return  V;

        }
        else {
            return  V;
        }
    }
    public static void main( String[] args ){
        LongLat P = new LongLat(  	-3.1911, 55.9456);
        LongLat Q = new LongLat(  -3.1869, 55.9445);
        LongLat Q1 = new LongLat(  -3.1861, 55.9447);
        LongLat Q2 = new LongLat(-3.1893,55.9434);
        LongLat Q3 = new LongLat(-3.1887,55.9459);
        Winding_number.make_Polygons();
        Vector<LongLat> V=get_path(P,Q);
        Vector<LongLat> V1 = get_path(V.get(0),Q1);
        Vector<LongLat> V2 = get_path(V1.get(0),Q2);
        Vector<LongLat> V3 = get_path(V2.get(0),Q3);
        Vector<LongLat> V4 = get_path(V3.get(0),Landmark_1);
        //extend_line_String(extend_line_String(extend_line_String(V,V1),V2),V3)

        System.out.println(make_geo_json(extend_line_String(extend_line_String(extend_line_String(extend_line_String(V,V1),V2),V3),V4)));


    }
}
