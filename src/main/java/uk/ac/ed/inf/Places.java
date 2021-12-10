package uk.ac.ed.inf;

/**
 * Class necessary for GSON parsing
 */
public class Places {
    private String name;
    String location;
    menu[] menu;

    /**
     * Class that is used for gson parsing
     */
    public  static class menu{
        String item;
        int pence;
    }
}
