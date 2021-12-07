package uk.ac.ed.inf;


import java.util.Vector;



public class Polygon_Winding_number {

    public  static Vector<Vector<Pointloc>> Polygons = new Vector<>();
    public static boolean in_Polygon(LongLat P){
        boolean inside = false;
        Polygon_Winding_number.Pointloc P1 = new Polygon_Winding_number.Pointloc(P.longitude,P.latitude);

        for(int i = 0; i< Polygon_Winding_number.Polygons.size(); i++){
            inside=inside|| isInside(Polygon_Winding_number.Polygons.get(i),P1);
        }
        return inside;
    }
    public static void setPolygons(Vector<Vector<Pointloc>> Polygons1){
        Polygons = Polygons1;
    }

    static class Pointloc {
        double x;
        double y;

        public Pointloc(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }

    private static double det(int i, Vector<Pointloc> P, Pointloc R) {
        double D1 = (P.get(i).x - R.x) * (P.get(i + 1).y - R.y);
        double D2 = (P.get(i + 1).x - R.x) * (P.get(i).y - R.y);

        return D1 - D2;
    }

    private static int classify(int i, Vector<Pointloc> P, Pointloc R) {
        if (P.get(i).y > R.y) {
            return (P.get(i).x <= R.x) ? 1 : 0;
        } else {
            if (P.get(i).y < R.y) {
                return 2 + ((P.get(i).x >= R.x) ? 1 : 0);
            } else {
                if (P.get(i).x > R.x) {
                    return 0;
                } else {
                    if (P.get(i).x < R.x) {
                        return 2;
                    } else {
                        return -99;
                    }
                }
            }
        }
    }

    public static boolean isInside(Vector<Pointloc> P, Pointloc R) {
        Vector<Integer> q_i = new Vector<>();
        int omega = 0;
        for (int i = 0; i < P.size() - 1; i++) {
            int currrent = classify(i, P, R);
            if (currrent == -99) {
                System.err.println("An Error Occured!");
                return false;
            }
            q_i.add(currrent);
        }
        q_i.add(q_i.get(0));
        for (int i = 0; i < P.size() - 1; i++) {
            int q1 = q_i.get(i + 1);
            int q2 = q_i.get(i);
            switch (q1 - q2) {
                case -3:
                    omega = omega + 1;
                    break;
                case 3:
                    omega = omega - 1;
                    break;
                case -2:
                    if (det(i, P, R) > 0) {
                        omega = omega + 1;
                    }
                    break;
                case 2:
                    if (det(i, P, R) < 0) {
                        omega = omega - 1;
                    }
                    break;
            }
        }


        return omega != 0;
    }


}
