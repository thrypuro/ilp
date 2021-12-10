package uk.ac.ed.inf;

/**
 * Class that is necessary for the gson parsing
 */
public class Details {
    private String country;
    private square square;
    private String nearestPlace;
    /**
     * Class which stores the values we need from our gson query
     */
    public Details.coordinates coordinates;
    private  static class square{
        southwest southwest;
        northwest northwest;
    }
    private  static class southwest{
        double lng;
        double lat;
    }
    private  static class northwest{
        double lng;
        double lat;
    }

    /**
     * Coordinates class which stores the values we need from gson
     */
    public  static class coordinates {
        double lng;
        double lat;
    }
    String words;
    String language;
    String map;
}
