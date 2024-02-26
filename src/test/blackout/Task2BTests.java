package blackout;

import org.junit.jupiter.api.Test;
import unsw.blackout.BlackoutController;
import unsw.utils.Angle;

import static blackout.TestHelpers.assertListAreEqualIgnoringOrder;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;
import java.util.Arrays;

public class Task2BTests {
    @Test
    public void testCommunicationThroughSingleRelays() {
        BlackoutController controller = new BlackoutController();
        // Create entities that cannot communicate due to range/ visibility
        controller.createDevice("desktop", "DesktopDevice", Angle.fromDegrees(12.5));
        controller.createSatellite("tele1", "TeleportingSatellite", 75000, Angle.fromDegrees(70));
        controller.createSatellite("tele2", "TeleportingSatellite", 75000, Angle.fromDegrees(145));
        controller.createDevice("laptop", "LaptopDevice", Angle.fromDegrees(193));

        // Check that they cannot communicate
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("desktop"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("tele1"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("tele2"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("laptop"));

        // Add relays so that they should be able to communicate now
        controller.createSatellite("relay1", "RelaySatellite", 78000, Angle.fromDegrees(30));
        controller.createSatellite("relay2", "RelaySatellite", 77000, Angle.fromDegrees(105));
        controller.createSatellite("relay3", "RelaySatellite", 77000, Angle.fromDegrees(177));

        // Check that they can communicate with nearby entities
        assertListAreEqualIgnoringOrder(Arrays.asList("tele1", "relay1"),
                controller.communicableEntitiesInRange("desktop"));
        assertListAreEqualIgnoringOrder(Arrays.asList("desktop", "tele2", "relay1", "relay2"),
                controller.communicableEntitiesInRange("tele1"));
        assertListAreEqualIgnoringOrder(Arrays.asList("tele2", "relay3"),
                controller.communicableEntitiesInRange("laptop"));

        // Check that adding non-compatible entities doesnt affect the list for desktop,
        // even though there are relay satellites
        controller.createSatellite("standard", "StandardSatellite", 80000, Angle.fromDegrees(12.5));
        assertListAreEqualIgnoringOrder(Arrays.asList("tele1", "relay1"),
                controller.communicableEntitiesInRange("desktop"));
    }

    @Test
    public void testCommunicationThroughMultipleRelays() {
        BlackoutController controller = new BlackoutController();
        // Create entities that cannot communicate due to range/ visibility
        controller.createDevice("desktop", "DesktopDevice", Angle.fromDegrees(12.5));
        controller.createSatellite("tele1", "TeleportingSatellite", 75000, Angle.fromDegrees(180));

        // Check that they cannot communicate
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("desktop"));

        // Add relays so that they should be able to communicate now
        controller.createSatellite("r1", "RelaySatellite", 77500, Angle.fromDegrees(0));
        controller.createSatellite("r2", "RelaySatellite", 77500, Angle.fromDegrees(40));
        controller.createSatellite("r3", "RelaySatellite", 77500, Angle.fromDegrees(80));
        controller.createSatellite("r4", "RelaySatellite", 77500, Angle.fromDegrees(120));
        controller.createSatellite("r5", "RelaySatellite", 77500, Angle.fromDegrees(160));
        controller.createSatellite("r6", "RelaySatellite", 77500, Angle.fromDegrees(180));

        // Check that they can communicate now
        assertListAreEqualIgnoringOrder(Arrays.asList("tele1", "r1", "r2", "r3", "r4", "r5", "r6"),
                controller.communicableEntitiesInRange("desktop"));

    }

    @Test
    public void testCommunicationWithMovement() {
        BlackoutController controller = new BlackoutController();
        // Create entities that cannot communicate initially
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(180));
        controller.createSatellite("tele", "TeleportingSatellite", 75000, Angle.fromDegrees(70));
        controller.createSatellite("relay", "RelaySatellite", 75000, Angle.fromDegrees(150));
        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(340));

        // Check that they cannot communicate initially

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("standard"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("tele"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("relay"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("handheld"));

        controller.simulate(75);

        // Check they can communicate after 75 ticks
        assertListAreEqualIgnoringOrder(Arrays.asList("handheld", "relay", "tele"),
                controller.communicableEntitiesInRange("standard"));
        assertListAreEqualIgnoringOrder(Arrays.asList("standard", "relay", "handheld"),
                controller.communicableEntitiesInRange("tele"));
        assertListAreEqualIgnoringOrder(Arrays.asList("standard", "handheld", "tele"),
                controller.communicableEntitiesInRange("relay"));
        assertListAreEqualIgnoringOrder(Arrays.asList("standard", "relay", "tele"),
                controller.communicableEntitiesInRange("handheld"));

        controller.simulate(180);

        // Check that the communicable have updated after 180 ticks
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("standard"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("tele"));
        assertListAreEqualIgnoringOrder(Arrays.asList("handheld"), controller.communicableEntitiesInRange("relay"));
        assertListAreEqualIgnoringOrder(Arrays.asList("relay"), controller.communicableEntitiesInRange("handheld"));
    }

    @Test
    public void testLaptopCommunicables() {
        BlackoutController controller = new BlackoutController();
        // Create a laptop and change entitiesInRange based on range
        // only atBounds and belowBound should show up in list
        controller.createDevice("laptop", "LaptopDevice", Angle.fromDegrees(0));
        controller.createSatellite("atBound", "StandardSatellite", 100000 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createSatellite("belowBound", "StandardSatellite", 99999 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createSatellite("pastBound", "StandardSatellite", 100001 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        assertListAreEqualIgnoringOrder(Arrays.asList("atBound", "belowBound"),
                controller.communicableEntitiesInRange("laptop"));
    }

    @Test
    public void testHandheldCommunicables() {
        BlackoutController controller = new BlackoutController();
        // Create a laptop and change entitiesInRange based on range
        // only atBounds and belowBound should show up in list
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(0));
        controller.createSatellite("atBound", "StandardSatellite", 50000 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createSatellite("belowBound", "StandardSatellite", 49999 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createSatellite("pastBound", "StandardSatellite", 50001 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        // Create a stndard satellite that is in range but not visible. Ensure that it
        // snot in the list (not visible)
        controller.createSatellite("standard", "StandardSatellite", 70000, Angle.fromDegrees(350));
        assertListAreEqualIgnoringOrder(Arrays.asList("atBound", "belowBound"),
                controller.communicableEntitiesInRange("handheld"));
    }

    @Test
    public void testDesktopCommunicables() {
        BlackoutController controller = new BlackoutController();
        // Create a laptop and change entitiesInRange based on range
        // only atBounds and belowBound should show up in list
        controller.createDevice("desktop", "DesktopDevice", Angle.fromDegrees(0));
        controller.createSatellite("atBound", "TeleportingSatellite", 200000 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createSatellite("belowBound", "TeleportingSatellite", 199999 + RADIUS_OF_JUPITER,
                Angle.fromDegrees(0));
        controller.createSatellite("pastBound", "TeleportingSatellite", 200001 + RADIUS_OF_JUPITER,
                Angle.fromDegrees(0));

        // Create a stndard satellite that is in range but not visible. Ensure that it
        // snot in the list (not visible)
        controller.createSatellite("standard", "StandardSatellite", 70000, Angle.fromDegrees(350));
        // Create a standard satellite and device that are visible and in range, and
        // ensure that they are not in the list (not compatible)
        controller.createSatellite("standard", "StandardSatellite", 12000 + RADIUS_OF_JUPITER, Angle.fromDegrees(0));
        controller.createDevice("laptop", "LaptopDevice", Angle.fromDegrees(0));
        assertListAreEqualIgnoringOrder(Arrays.asList("atBound", "belowBound"),
                controller.communicableEntitiesInRange("desktop"));
    }

    @Test
    public void testEntitiesInRange() {
        // Task 2
        // Example from the specification
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to
        // download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "StandardSatellite", 1000 + RADIUS_OF_JUPITER, Angle.fromDegrees(320));
        controller.createSatellite("Satellite2", "StandardSatellite", 1000 + RADIUS_OF_JUPITER, Angle.fromDegrees(315));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(310));
        controller.createDevice("DeviceC", "HandheldDevice", Angle.fromDegrees(320));
        controller.createDevice("DeviceD", "HandheldDevice", Angle.fromDegrees(180));
        controller.createSatellite("Satellite3", "StandardSatellite", 2000 + RADIUS_OF_JUPITER, Angle.fromDegrees(175));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceC", "Satellite2"),
                controller.communicableEntitiesInRange("Satellite1"));
        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceB", "DeviceC", "Satellite1"),
                controller.communicableEntitiesInRange("Satellite2"));
        assertListAreEqualIgnoringOrder(Arrays.asList("Satellite2"), controller.communicableEntitiesInRange("DeviceB"));

        assertListAreEqualIgnoringOrder(Arrays.asList("DeviceD"), controller.communicableEntitiesInRange("Satellite3"));
    }

}
