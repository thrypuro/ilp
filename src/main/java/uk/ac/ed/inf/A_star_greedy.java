package uk.ac.ed.inf;

import java.util.*;
public class A_star_greedy {


    public Vector<LongLat> Path = new Vector<>();
    LongLat start;
    LongLat goal;
    static Double D = 0.00015;

    boolean Euclidean;
    boolean path_found=false;

    static int stuck_limit = 50000; // adjust this limit with the scale of the programme, a safety measure
    int size;
    boolean is_stuck=false;
    //came From
    HashMap<LongLat, LongLat> cameFrom = new HashMap<>();


    //openSet
    PriorityQueue<LongLat> openSet = new PriorityQueue<>();

    //closedSet
    HashSet<LongLat> closetSet = new HashSet<>();

    // gscore
    HashMap<LongLat, Double> gScore = new HashMap<>();

    public A_star_greedy(LongLat start, LongLat goal,boolean Euclidean){
        this.start = start;
        this.goal = goal;
        this.Euclidean = Euclidean;
        double score;
        if(Euclidean){
            score = D*start.distanceTo(goal);

        }
        else {
            score=H(start,goal);
        }
        start.setfS(score);
        openSet.add(start);
        gScore.put(start,score);

    }
    public double H(LongLat source,LongLat end){
        double dx,dy,D,D2;
        dx = Math.abs(source.longitude-end.longitude);
        dy = Math.abs(source.latitude-end.latitude);
        D = 0.00015;
        D2 = Math.sqrt(2)*D;
        double dist = D * (dx + dy) + (D2 - 2 * D) * Math.min(dx, dy);
        if(this.Euclidean){
            dist = D*source.distanceTo(goal);
        }
        dist =dist*1.1;
        return dist;
    }
    public boolean drone_can_move(LongLat current_neghbor,LongLat current){
        boolean away_from_no_fly_zone=false;
        for (int j = 0; j <360 ; j=j+120) {
            away_from_no_fly_zone= away_from_no_fly_zone||(Polygon_Winding_number.in_Polygon(current.shorter(j)));
        }
        if(away_from_no_fly_zone){

            return false;
        }
        if(!current_neghbor.isConfined() ){
            return false;
        }

        return !Polygon_Winding_number.in_Polygon(current_neghbor);
    }


    public  void reconstruct_path(HashMap<LongLat, LongLat> cameFrom, LongLat current){
        this.Path.add(current);
        while(!cameFrom.isEmpty()){

            current = cameFrom.get(current);

            if(current==null){
                break;
            }

            this.Path.add(0,current);
        }
        this.size = Path.size();



    }


    public void get_path(){
        int stuck = 1;
        double inf = Double.POSITIVE_INFINITY;
        while (!openSet.isEmpty()){

            LongLat current = openSet.remove();

            closetSet.add(current);
            if(current.closeTo(goal)){
                reconstruct_path(cameFrom,current);
                path_found = true;
                break;
            }

            for(int i=0;i<360;i=i+10){
                LongLat current_neghbor = current.nextPosition(i);
                gScore.putIfAbsent(current_neghbor,inf);
                double tenative_gScore = gScore.get(current)+0.00015;


                double dist = H(current_neghbor,goal);


                if(!drone_can_move(current_neghbor,current)){
                    stuck+=1;
                    if(stuck>stuck_limit){
                        path_found = false;
                        is_stuck = true;
                        openSet.clear();
                        break;
                    }
                    continue;

                }
                if(closetSet.stream().noneMatch(L -> L.closeTo(current_neghbor)) && !(current.fS>= tenative_gScore)){
                    if(tenative_gScore<gScore.get(current_neghbor) ){
                        cameFrom.put(current_neghbor,current);
                        gScore.put(current_neghbor,tenative_gScore);
                        current_neghbor.setfS(dist);
                        openSet.add(current_neghbor);

                    }
                }

            }
        }
    }
    public int size(){
        return this.size;
    }

}
