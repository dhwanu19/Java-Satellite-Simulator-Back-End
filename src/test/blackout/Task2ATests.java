package blackout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import unsw.blackout.BlackoutController;
import unsw.response.models.EntityInfoResponse;
import unsw.utils.Angle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

@TestInstance(value = Lifecycle.PER_CLASS)
public class Task2ATests {
    @Test
    public void testBasicStandardSatelliteMovement() {
        BlackoutController controller = new BlackoutController();

        // Create a standard satellite
        controller.createSatellite("S1", "StandardSatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(360));
        assertEquals(
                new EntityInfoResponse("S1", Angle.fromDegrees(360), 10000 + RADIUS_OF_JUPITER, "StandardSatellite"),
                controller.getInfo("S1"));

        // Ensure position aftert 1 tick is decreasing.
        controller.simulate();
        assertEquals(-1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(360)));
        controller.simulate(60);

        // Ensure position after 60 more ticks (61 total ticks) is correct
        assertEquals(new EntityInfoResponse("S1", Angle.fromDegrees(250.6583), 10000 + RADIUS_OF_JUPITER,
                "StandardSatellite"), controller.getInfo("S1"));
    }

    @Test
    public void testBasicTeleportatingSatelliteMovement() {
        BlackoutController controller = new BlackoutController();
        // Create teleporting Satellite
        controller.createSatellite("S1", "TeleportingSatellite", 80000, Angle.fromDegrees(177));

        controller.simulate();

        // Check that direction is antiClockwise (angle is increasing)
        assertEquals(1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(177)));

        controller.simulate(5);
        double currentPosition = controller.getInfo("S1").getPosition().toDegrees();

        // Check that satellite teleported
        assertTrue(currentPosition < 360 && currentPosition > 270);
        controller.simulate(1);

        // Check that satellite direction is now clockwise (angle is decreasing)
        assertEquals(-1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(currentPosition)));

        controller.simulate(480);
        currentPosition = controller.getInfo("S1").getPosition().toDegrees();
        controller.simulate(1);
        // Check that direction is anti-clocwise after another revolution around Jupiter
        assertEquals(1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(currentPosition)));
    }

    @Test
    public void testTeleportingSatelliteSpawnPast180() {
        BlackoutController controller = new BlackoutController();

        // Create teleporting Satellite
        controller.createSatellite("S1", "TeleportingSatellite", 80000, Angle.fromDegrees(181));

        controller.simulate(120);

        // Check that direction is antiClockwise (angle is increasing)
        assertEquals(1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(181)));

        // Check that direction does not change as you cross 0/ 360 degrees
        double currentPosition = controller.getInfo("S1").getPosition().toDegrees();
        controller.simulate(120);
        assertEquals(1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(currentPosition)));
        currentPosition = controller.getInfo("S1").getPosition().toDegrees();
        controller.simulate(360);
        currentPosition = controller.getInfo("S1").getPosition().toDegrees();

        // Check that direction is clockwise after a revoluition
        controller.simulate(1);
        assertEquals(-1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(currentPosition)));

    }

    @Test
    public void testBasicRelaySatelliteMovement() {
        BlackoutController controller = new BlackoutController();

        // Create relay Satellite in bounds
        controller.createSatellite("S1", "RelaySatellite", 80000, Angle.fromDegrees(135));

        controller.simulate(1);

        // Check that direction is antiClockwise (angle is increasing)
        assertEquals(1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(135)));

        // Confirm that direction has changed after hitting bounds
        controller.simulate(70);
        double currentPosition = controller.getInfo("S1").getPosition().toDegrees();
        controller.simulate();
        assertEquals(-1, controller.getInfo("S1").getPosition().compareTo(Angle.fromDegrees(currentPosition)));

    }

    @Test
    public void testRelaySatelliteMovementOutOfBounds() {
        BlackoutController controller = new BlackoutController();

        // Create relay Satellite out of bounds and above 1 degree
        controller.createSatellite("1", "RelaySatellite", 80000, Angle.fromDegrees(1));
        // Create relay Satellite out of bounds and above 345 degrees
        controller.createSatellite("346", "RelaySatellite", 80000, Angle.fromDegrees(346));
        // Create relay Satellite out of bounds at 345 degrees
        controller.createSatellite("345", "RelaySatellite", 80000, Angle.fromDegrees(345));
        // Create relay Satellite out of bounds and below 345 degrees
        controller.createSatellite("344", "RelaySatellite", 80000, Angle.fromDegrees(344));

        // Check that all satellites are moving in the correct direction
        controller.simulate(1);
        assertEquals(1, controller.getInfo("1").getPosition().compareTo(Angle.fromDegrees(1)));
        assertEquals(1, controller.getInfo("346").getPosition().compareTo(Angle.fromDegrees(346)));
        assertEquals(1, controller.getInfo("345").getPosition().compareTo(Angle.fromDegrees(345)));
        assertEquals(-1, controller.getInfo("344").getPosition().compareTo(Angle.fromDegrees(344)));

        // Check that after a revolution and hitting the bounds, the satellitesare
        // switching direcitons
        controller.simulate(200);
        double currentPosition1 = controller.getInfo("1").getPosition().toDegrees();
        double currentPosition346 = controller.getInfo("346").getPosition().toDegrees();
        double currentPosition345 = controller.getInfo("345").getPosition().toDegrees();
        double currentPosition344 = controller.getInfo("344").getPosition().toDegrees();

        controller.simulate();
        assertEquals(-1, controller.getInfo("1").getPosition().compareTo(Angle.fromDegrees(currentPosition1)));
        assertEquals(-1, controller.getInfo("346").getPosition().compareTo(Angle.fromDegrees(currentPosition346)));
        assertEquals(-1, controller.getInfo("345").getPosition().compareTo(Angle.fromDegrees(currentPosition345)));
        assertEquals(1, controller.getInfo("344").getPosition().compareTo(Angle.fromDegrees(currentPosition344)));

        // Check that after hitting the bounds again, the satellites are switching
        // directions
        currentPosition1 = controller.getInfo("1").getPosition().toDegrees();
        currentPosition346 = controller.getInfo("346").getPosition().toDegrees();
        currentPosition345 = controller.getInfo("345").getPosition().toDegrees();
        currentPosition344 = controller.getInfo("344").getPosition().toDegrees();

        controller.simulate(80);

        assertEquals(1, controller.getInfo("1").getPosition().compareTo(Angle.fromDegrees(currentPosition1)));
        assertEquals(1, controller.getInfo("346").getPosition().compareTo(Angle.fromDegrees(currentPosition346)));
        assertEquals(1, controller.getInfo("345").getPosition().compareTo(Angle.fromDegrees(currentPosition345)));
        assertEquals(-1, controller.getInfo("344").getPosition().compareTo(Angle.fromDegrees(currentPosition344)));

    }

    @Test
    public void testMovement() {
        // Task 2
        // Example from the specification
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to
        // download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "StandardSatellite", 100 + RADIUS_OF_JUPITER, Angle.fromDegrees(340));
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(340), 100 + RADIUS_OF_JUPITER,
                "StandardSatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(337.95), 100 + RADIUS_OF_JUPITER,
                "StandardSatellite"), controller.getInfo("Satellite1"));
    }

    @Test
    public void testRelayMovement() {
        // Task 2
        // Example from the specification
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to
        // download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "RelaySatellite", 100 + RADIUS_OF_JUPITER, Angle.fromDegrees(180));

        // moves in negative direction
        assertEquals(
                new EntityInfoResponse("Satellite1", Angle.fromDegrees(180), 100 + RADIUS_OF_JUPITER, "RelaySatellite"),
                controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(178.77), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(177.54), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(176.31), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));

        controller.simulate(5);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(170.18), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate(24);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(140.72), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        // edge case
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(139.49), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        // coming back
        controller.simulate(1);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(140.72), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate(5);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(146.85), 100 + RADIUS_OF_JUPITER,
                "RelaySatellite"), controller.getInfo("Satellite1"));
    }

    @Test
    public void testTeleportingMovement() {
        // Test for expected teleportation movement behaviour
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("Satellite1", "TeleportingSatellite", 10000 + RADIUS_OF_JUPITER,
                Angle.fromDegrees(0));

        controller.simulate();
        Angle clockwiseOnFirstMovement = controller.getInfo("Satellite1").getPosition();
        controller.simulate();
        Angle clockwiseOnSecondMovement = controller.getInfo("Satellite1").getPosition();
        assertTrue(clockwiseOnSecondMovement.compareTo(clockwiseOnFirstMovement) == 1);

        // It should take 250 simulations to reach theta = 180.
        // Simulate until Satellite1 reaches theta=180
        controller.simulate(250);

        // Verify that Satellite1 is now at theta=0
        assertTrue(controller.getInfo("Satellite1").getPosition().toDegrees() % 360 == 0);
    }
}
