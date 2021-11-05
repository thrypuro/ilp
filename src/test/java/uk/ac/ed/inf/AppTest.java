package uk.ac.ed.inf;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void testIsConfinedTrueA(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        assertTrue(appletonTower.isConfined());
    }

    @Test
    public void testIsConfinedTrueB(){
        LongLat businessSchool = new LongLat(-3.1873,55.9430);
        assertTrue(businessSchool.isConfined());
    }

    @Test
    public void testIsConfinedFalse(){
        LongLat greyfriarsKirkyard = new LongLat(-3.1928,55.9469);
        assertFalse(greyfriarsKirkyard.isConfined());
    }

    private boolean approxEq(double d1, double d2) {
        return Math.abs(d1 - d2) < 1e-12;
    }

    @Test
    public void testDistanceTo(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat businessSchool = new LongLat(-3.1873,55.9430);
        double calculatedDistance = 0.004344122466045367;
        assertTrue(approxEq(appletonTower.distanceTo(businessSchool), calculatedDistance));
    }

    @Test
    public void testCloseToTrue(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat alsoAppletonTower = new LongLat(-3.19156134686606,55.94364815975075);
        assertTrue(appletonTower.closeTo(alsoAppletonTower));
    }


    @Test
    public void testCloseToFalse(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat businessSchool = new LongLat(-3.1873,55.9430);
        assertFalse(appletonTower.closeTo(businessSchool));
    }


    private boolean approxEq(LongLat l1, LongLat l2) {
        return approxEq(l1.longitude, l2.longitude) &&
                approxEq(l1.latitude, l2.latitude);
    }

    @Test
    public void testAngle0(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(0);
        LongLat calculatedPosition = new LongLat(-3.1914439999999997, 55.943658);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle30(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(30);
        LongLat calculatedPosition = new LongLat(-3.1914640961894323, 55.943733);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle60(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(60);
        LongLat calculatedPosition = new LongLat(-3.191519, 55.943787903810566);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle90(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(90);
        LongLat calculatedPosition = new LongLat(-3.191594, 55.943808);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle150(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(150);
        LongLat calculatedPosition = new LongLat(-3.1917239038105674, 55.943733);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle210(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(210);
        LongLat calculatedPosition = new LongLat(-3.1917239038105674, 55.943583);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle270(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(270);
        LongLat calculatedPosition = new LongLat(-3.191594, 55.943508);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle310(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(310);
        LongLat calculatedPosition = new LongLat(-3.1914975818585467, 55.94354309333353);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle350(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        LongLat nextPosition = appletonTower.nextPosition(350);
        LongLat calculatedPosition = new LongLat(-3.191446278837048, 55.94363195277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle999(){
        LongLat appletonTower = new LongLat(-3.191594,55.943658);
        // The special junk value -999 means "hover and do not change position"
        LongLat nextPosition = appletonTower.nextPosition(-999);
        assertTrue(approxEq(nextPosition, appletonTower));
    }

    @Test
    public void testMenusOne() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(totalCost, 230 + 50);
    }

    @Test
    public void testMenusTwo() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(totalCost, 230 + 230 + 50);
    }

    @Test
    public void testMenusThree() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(totalCost, 230 + 230 + 460 + 50);
    }

    @Test
    public void testMenusFourA() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte",
                "Dirty matcha latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(totalCost, 230 + 230 + 460 + 460 + 50);
    }

    @Test
    public void testMenusFourB() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Flaming tiger latte",
                "Dirty matcha latte",
                "Strawberry matcha latte",
                "Fresh taro latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(totalCost, 4 * 460 + 50);
    }


}