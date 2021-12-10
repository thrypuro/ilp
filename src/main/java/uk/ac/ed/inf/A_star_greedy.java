package uk.ac.ed.inf;

import java.util.*;

/**
 * <p>
 * A star greedy class which will compute the path between 2 points.
 *<br>
 * Uses the greedy heuristic to calculate fast but suboptimal path </p>
 */
public class A_star_greedy {


    // the start and destination
    LongLat start;
    LongLat goal;
    // says if path has been found or not
    boolean path_found=false;
    // a safety measure s.t program doesn't get stuck
    final static int stuck_limit = 50000; // adjust this limit with the scale of the programme, a safety measure
    // the size of the path found

    /*
    All the data structures that will be used for path algorithm
     */
    //Stores the parent of the Node
    HashMap<LongLat, LongLat> cameFrom = new HashMap<>();


    //Stores all the nodes that are going to be considered
    PriorityQueue<LongLat> openSet = new PriorityQueue<>();

    //Stores all the nodes that have already been considered
    HashSet<LongLat> closetSet = new HashSet<>();

    // G-score of the nodes that have been calculated
    HashMap<LongLat, Double> gScore = new HashMap<>();

    /**
     * Constructor which declares the source and destination needed for the
     * calculating the path.
     * @param start  starting position of the path
     * @param goal  goal that needs to be reached
     */
    public A_star_greedy(LongLat start, LongLat goal){
        this.start = start;
        this.goal = goal;
        double score;
        // the heuristic is used to calculate the initial score
        score=H(start,goal);
        // Setting fS as the priority needs something that is comparable to sort
        start.setfS(score);
        openSet.add(start);
        gScore.put(start,score);
    }

    /**
     * Heuristic Function, Diagonal Distance is used.
     * @param source  source/first point
     * @param end  end/second point
     * @return  approximate/heuristic distance between the points
     */
    public static double H(LongLat source,LongLat end){
        double dx,dy,D2;
        // "slope" of the two points
        dx = Math.abs(source.longitude-end.longitude);
        dy = Math.abs(source.latitude-end.latitude);
        // the diagonal if we think of it as a square
        D2 = Math.sqrt(2);
        // distance calculated
        double dist =  (dx + dy) + (D2 - 2 ) * Math.min(dx, dy);
        // Multiplied with 1.1 to eliminate ties
        dist =dist*1.1;
        return dist;
    }

    /**
     * <p>
     * Function which checks if the drone can move or not
     * <br>
     * Conditions :
     *<br>
     * Does the line segment intersect the Polygon? -> False
     *<br>
     * Is the neighbor not confined? -> False
     *<br>
     * Is the neighbor in the Polygon? -> False
     * </p>
     * @param current_neighbor  The current neighbor we are considering
     * @param current  The parent of the neighbor
     * @return  boolean which tells us if the drone can move or not
     */
    private boolean drone_can_move(LongLat current_neighbor,LongLat current){
        /* Checks if the neighboring point is in the No-fly zone
        and the line segment from Current to current_neighbor does not intersect any of the polygons
         */
        boolean does_it_intersect;
        does_it_intersect = Polygon_inside_and_intersection.intersect_polygon(current_neighbor,current);
        if(does_it_intersect){
            return false;
        }
        // makes sure the drone is confined
        if(!current_neighbor.isConfined() ){
            return false;
        }

        return !Polygon_inside_and_intersection.in_Polygon(current_neighbor);
    }

    /**
     * Final step in the Path computation which reconstructs the path by
     * iteratively going through the parent of the node until it
     * reaches null.
     * @param cameFrom Hash-map which stores the parent of the neighbor
     * @param current  the current node (the node that is close to the destination) that is
     *                the final node in consideration.
     * @return returns the final path that is computed
     */
    private   Vector<LongLat> reconstruct_path(HashMap<LongLat, LongLat> cameFrom, LongLat current){
        // Adds the final node as our starting path
        Vector<LongLat> final_path = new Vector<>();
        final_path.add(current);
        while(true){
            // gets the parent of the current node
            current = cameFrom.get(current);

            if(current==null){
                break;
            }
            // adds the node to the Path at the first index (as the parent comes first)
            final_path.add(0,current);
        }
        return final_path;
    }

    /**
     * The function that actually computes the path between the source and destination as declared by the constructor
     * after the constructor creates the object.
     * @return  The path between the two points
     */
    public Vector<LongLat> get_path(){
        /*
        Variables we will be needing
        stuck to make sure program doesn't get stuck
        infinity is useful as it is in pseudocode of A* on the wikipedia page
         */
        int stuck = 1;
        double inf = Double.POSITIVE_INFINITY;
        // while we have nodes to be considered
        while (!openSet.isEmpty()){
            // current node in consideration
            LongLat current = openSet.remove();
            // add the node as considered
            closetSet.add(current);
            // have we reached/close to the goal?
            if(current.closeTo(goal)){
                // true--> we are done, path found, reconstruct it and break the loop!
                path_found = true;
                return reconstruct_path(cameFrom,current);


            }
            // loop that considers each and every neighbor of the current neighbor
            for(int i=0;i<360;i=i+10){
                // calculate the next position
                LongLat current_neighbor = current.nextPosition(i);
                // useful function if the current neighbor is not present set as infinity as indicated in the A* pseudocode
                gScore.putIfAbsent(current_neighbor,inf);
                // calculate the gScore of the parent
                double tentative_gScore = gScore.get(current)+0.00015;


                double dist = H(current_neighbor,goal);
                double tentative_fScore = dist+tentative_gScore;
                // makes sure the drone can move to the node (as we are finding nodes as we go than using a pre-rendered map with nodes
                if(!drone_can_move(current_neighbor,current)){
                    stuck+=1;
                    if(stuck>stuck_limit){
                        /* If this happens the program stops, could  recompute with landmarks, this option was not implemented
                        because of the lack of time but a prototype of this was present in an earlier version of the code
                         */
                        System.err.println("Path couldn't be found, possibly an infinite loop!");
                        System.exit(404);
                    }
                    continue;
                }
                /* important condition, makes sure the current node isn't close to the set of nodes we have already considered and
                 the fScore is higher than the estimate fScore
                 */
                if(closetSet.stream().noneMatch(L -> L.closeTo(current_neighbor)) && !(current.fS>= tentative_fScore)){
                    // is the gScore smaller than neighbors gScore?
                    if(tentative_gScore<gScore.get(current_neighbor) ){
                        // Add the parent,gScore of the neighbor
                        cameFrom.put(current_neighbor,current);
                        gScore.put(current_neighbor,tentative_gScore);
                        // f(x)= 0*g(x)+h(x) - greedy
                        current_neighbor.setfS(dist);
                        openSet.add(current_neighbor);

                    }
                }

            }
        }
        return null;
    }


}
