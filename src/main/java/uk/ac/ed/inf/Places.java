package uk.ac.ed.inf;

/**
 * Class necessary for GSON parsing
 */
public class Places {
    String name;
    String location;
    menu[] menu;
    public  static class menu{
        String item;
        int pence;
    }
}
