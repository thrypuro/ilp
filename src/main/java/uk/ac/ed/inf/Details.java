package uk.ac.ed.inf;

public class Details {
    String country;
    square square;
    String nearestPlace;
    cordinates coordinates;
    public  static class square{
        southwest southwest;
        northwest northwest;
    }
    public  static class southwest{
        double lng;
        double lat;
    }
    public  static class northwest{
        double lng;
        double lat;
    }
    public  static class cordinates{
        double lng;
        double lat;
    }
    String words;
    String language;
    String map;
}
