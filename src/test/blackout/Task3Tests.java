package blackout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import unsw.blackout.BlackoutController;
import unsw.utils.Angle;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public class Task3Tests {
    @Test
    public void testDeviceMovementWithoutSlopes() {
        BlackoutController controller = new BlackoutController();

        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90), true);
        controller.simulate(10);
        // Check that handheld device is moving in correct direction
        assertEquals(1, controller.getInfo("handheld").getPosition().compareTo(Angle.fromDegrees(0)));
    }

    @Test
    public void testDeviceMovementWith1Slope() {
        BlackoutController controller = new BlackoutController();

        controller.createSlope(90, 180, 300);
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(85), true);
        // Check that the device is currently on jupiter
        assertTrue(controller.getInfo("handheld").getHeight() == RADIUS_OF_JUPITER);

        controller.simulate(240);
        // Check that the device is climbing up the slope after 240 ticks
        assertTrue(controller.getInfo("handheld").getHeight() > RADIUS_OF_JUPITER);

        controller.simulate(20 * 120);
        // Check that the device is still on teh slope after 21 * 120 more ticks
        assertTrue(controller.getInfo("handheld").getHeight() > RADIUS_OF_JUPITER);

        controller.simulate(120);
        // Check that the device is on the surface of jupiter after falling off the edge
        // of the slope
        assertTrue(controller.getInfo("handheld").getHeight() == RADIUS_OF_JUPITER);
    }

    @Test
    public void testDeviceMovementWithMultipleSlopes() {
        BlackoutController controller = new BlackoutController();

        controller.createSlope(0, 20, 700);
        controller.createSlope(20, 50, -520);

        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(350), true);

        // Check that the device is climbing up the first slope after 240 ticks
        controller.simulate(360);
        assertTrue(controller.getInfo("handheld").getHeight() > RADIUS_OF_JUPITER);

        controller.simulate(5 * 120);
        // Check that the device is still above the surface of jupiter, meaning it is
        // now on the second slope
        assertTrue(controller.getInfo("handheld").getHeight() > RADIUS_OF_JUPITER);

        controller.simulate(6 * 120);
        // Check that the device is on the surface of jupiter after falling off leaving
        // the second slope
        assertTrue(controller.getInfo("handheld").getHeight() == RADIUS_OF_JUPITER);
    }
}
