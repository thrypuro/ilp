
package uk.ac.ed.inf;

import java.awt.*;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;

import java.util.*;
import java.util.List;


public class Menus {
    public static String machine;
    public static String port;
    public static String urlString;
    public static HashMap<String,Integer> cost=new HashMap();
    public static HashMap<String,String> location = new HashMap<>();



    //  One HttpClient shared between all HttpRequests, to avoid repeated thread starts
    private static final HttpClient client = HttpClient.newHttpClient();
    public static void prepare_menus(){
        // HttpRequest assumes that it is a GET request by default.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString+"/menus/menus.json"))
                .build();
        try {
            // Sends the GET request to the site
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

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

            // To-do: maybe remove redundant searches after 4 orders break the loop
            // Class specific iterator, cleaner to iterate
            for(Places place : places){
                // Takes the important field of the class
                Places.menu[] current_place = place.menu;
                for(Places.menu menu : place.menu){
                    cost.put(menu.item, menu.pence);
                    location.put(menu.item,place.location);
                }


            }
    } // necessary catch statement if the request to the site failed
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }


    /**
     * Constructor of the class Menus, also constructs the general url from the parameters
     * @param machine : ip address of the machine to request data
     * @param port : the port number in the ip where the site is situated
     */
    public Menus(String machine,String port){
        this.machine = machine;
        this.port = port;
        this.urlString = "http://"+this.machine+":"+this.port;
        prepare_menus();

    }

    /**
     *  Calculates the total delivery cost from the order given
     *  sends a GET request to the site to get the menu,
     *  a JSON file is received which is parsed and iterated to find the total cost
     * @param items : all the items that have been ordered
     * @return : an integer which is the total cost of all the things that have been ordered
     */
    public int getDeliveryCost(ArrayList<String> items)  {
        int cost1 =50;
        for (int i = 0; i < items.size(); i++) {
            cost1+=cost.get(items.get(i));
        }
            return cost1;
        }


}
