package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Vector;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;

public class Winding_number {
    private static final HttpClient client = HttpClient.newHttpClient();
    public  static Vector<Vector<Pointloc>> Polygons = new Vector<Vector<Pointloc>>();
    public static boolean make_Polygons() {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:9898/buildings/" + "no-fly-zones.geojson"))
                .build();

        try {
            // Sends the GET request to the site
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            String source = response.body();
            FeatureCollection fc = FeatureCollection.fromJson(source);


            for (int i = 0; i < fc.features().size(); i++) {
                Vector <Pointloc> Poly = new Vector<>();

                Polygon pol = (Polygon) fc.features().get(i).geometry();
                for(int j=0;j<pol.coordinates().get(0).size();j++){
                    Pointloc P2 = new Pointloc(pol.coordinates().get(0).get(j).coordinates().get(0),pol.coordinates().get(0).get(j).coordinates().get(1));

                    Poly.add(P2);
                }
                Polygons.add(Poly);
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
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
        Vector q_i = new Vector();
        int omega = 0;
        for (int i = 0; i < P.size() - 1; i++) {
            int currrent = classify(i, P, R);
            if (currrent == -99) {
                System.out.println("An Error Occured!");
                return false;
            }
            q_i.add(currrent);
        }
        q_i.add(q_i.get(0));
        for (int i = 0; i < P.size() - 1; i++) {
            int q1 = (int) q_i.get(i + 1);
            int q2 = (int) q_i.get(i);
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


        return omega != 0 ? true : false;
    }

    public static void main(String[] args) {

    }
}
