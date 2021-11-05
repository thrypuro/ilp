
package uk.ac.ed.inf;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class Menus {
    String machine;
    String port;
    String urlString;
    //  One HttpClient shared between all HttpRequests, to avoid repeated thread starts
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Constructor of the class Menus, also constructs the general url from the parameters
     * @param machine : ip address of the machine to request data
     * @param port : the port number in the ip where the site is situated
     */
    public Menus(String machine,String port){
        this.machine = machine;
        this.port = port;
        this.urlString = "http://"+this.machine+":"+this.port;
    }

    /**
     *  Calculates the total delivery cost from the order given
     *  sends a GET request to the site to get the menu,
     *  a JSON file is received which is parsed and iterated to find the total cost
     * @param items : all the items that have been ordered
     * @return : an integer which is the total cost of all the things that have been ordered
     */
    public int getDeliveryCost(String... items)  {
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString+"/menus/menus.json"))
                .build();
        try {
            // Sends the GET request to the site
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            // Sets/HashSets have good look-up, making it ideal for contains operator
            Set<String> itemSet = new HashSet<>(Arrays.asList(items));
            /* Initialises new GSON for parsing
               ArrayList is used, as it is an array of JSONs
               Uses TypeToken and Type for getting the type
             */
            Gson gson = new Gson();

            Type listType =
                    new TypeToken<ArrayList<Places>>() {}.getType();
            ArrayList<Places> places =
                    gson.fromJson(response.body(), listType);

            // 50p Delivery charge by default
            int cost =50;
            // To-do: maybe remove redundant searches after 4 orders break the loop
            // Class specific iterator, cleaner to iterate
            for(Places place : places){
                // Takes the important field of the class
                Places.menu[] current_place = place.menu;
                // functional programming logic for iterating through classes, sums the costs of all the items it finds
                int place_cost = Arrays.stream(current_place)
                        // filters the class array and pass only the objects that contains the item
                        .filter(pl -> itemSet.contains(pl.item))
                        // takes only the pence field of the class object
                        .map(x -> x.pence)
                        // Sums up all the pence field
                        .mapToInt(Integer::intValue).sum();
                cost+=place_cost;

            }
            return cost;
        }
        // necessary catch statement if the request to the site failed
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        // returns 1 if failed
        return 1;
    }

}
