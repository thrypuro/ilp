package uk.ac.ed.inf;

import java.net.http.HttpClient;

/**
 * Hello world!
 *
 */
public class App 
{     private static final HttpClient client = HttpClient.newHttpClient();

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
