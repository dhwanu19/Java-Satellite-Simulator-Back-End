package blackout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import unsw.blackout.BlackoutController;
import unsw.blackout.FileTransferException;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public class Task2CTests {
    @Test
    public void testBasicFileTransfer() {
        BlackoutController controller = new BlackoutController();
        // Create entites
        // Create message on handheld
        // Send from handheld to standard and check its progress (1 byte/tick)
        // Send the message from standard to laptop and chec its progress (1 byte/tick)

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90));
        controller.createDevice("laptop", "LaptopDevice", Angle.fromDegrees(90));

        String message = "abcde";
        controller.addFileToDevice("handheld", "file", message);
        assertEquals(new FileInfoResponse("file", "abcde", 5, true),
                controller.getInfo("handheld").getFiles().get("file"));
        assertDoesNotThrow(() -> controller.sendFile("file", "handheld", "standard"));
        controller.simulate(2);
        assertEquals(new FileInfoResponse("file", "ab", 5, false),
                controller.getInfo("standard").getFiles().get("file"));
        controller.simulate(3);
        assertEquals(new FileInfoResponse("file", "abcde", 5, true),
                controller.getInfo("standard").getFiles().get("file"));
        assertDoesNotThrow(() -> controller.sendFile("file", "standard", "laptop"));
        controller.simulate(2);
        assertEquals(new FileInfoResponse("file", "ab", 5, false), controller.getInfo("laptop").getFiles().get("file"));
        controller.simulate(3);
        assertEquals(new FileInfoResponse("file", "abcde", 5, true),
                controller.getInfo("laptop").getFiles().get("file"));
    }

    @Test
    public void testStandardOutOfRangeDuringTransfer() {
        BlackoutController controller = new BlackoutController();

        // Create entities
        // Send message from handheld to standard satellite
        // Move standard satellite out of range before filetransfer is complete
        // Check that message is deleted from standard satellite
        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(155));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(175));

        String message = "dsacijsakjbkjb";
        controller.addFileToDevice("handheld", "file", message);
        assertEquals(new FileInfoResponse("file", message, message.length(), true),
                controller.getInfo("handheld").getFiles().get("file"));
        assertDoesNotThrow(() -> controller.sendFile("file", "handheld", "standard"));
        controller.simulate(10);

        assertEquals(null, controller.getInfo("standard").getFiles().get("file"));

    }

    @Test
    public void testSatelliteToSatelliteOutOfRangeDuringTransfer() {
        BlackoutController controller = new BlackoutController();
        // Create entities
        // Send message from handheld to teleporting satellite
        // After message is fully downloaded by teleporting satellite, send from
        // teleporting satellite to standard satellite
        // Teleport out after 2 ticks. The standard satellite's version of the message
        // should have all "t"s removed after the 2nd byte
        controller.createSatellite("teleporting", "TeleportingSatellite", 75000, Angle.fromDegrees(177));
        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(179));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(179));

        String message = "tAttttt";
        String message2 = "tA";

        controller.addFileToDevice("handheld", "file", message);
        assertEquals(new FileInfoResponse("file", message, message.length(), true),
                controller.getInfo("handheld").getFiles().get("file"));
        assertDoesNotThrow(() -> controller.sendFile("file", "handheld", "teleporting"));
        controller.simulate(1);
        assertDoesNotThrow(() -> controller.sendFile("file", "teleporting", "standard"));
        controller.simulate(3);
        assertEquals(new FileInfoResponse("file", message2, message2.length(), true),
                controller.getInfo("standard").getFiles().get("file"));

    }

    @Test
    public void testDeviceToTeleportingSatelliteMidTransfer() {
        BlackoutController controller = new BlackoutController();
        // Create entities
        // Send a message from handheld to teleporting satellite
        // before teleporting satellite can download message, it teleports
        // file should be deleted from teleporting satellite, and should contain no "t"
        // bytes in handheld device
        controller.createSatellite("teleporting", "TeleportingSatellite", 75000, Angle.fromDegrees(179));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(179));

        String message = "tXYZtXYZtttXYZtXYZtabcdefgtttthijklmttttijklttttttmnopqtttttrstuvttttt";
        controller.addFileToDevice("handheld", "file", message);
        assertEquals(new FileInfoResponse("file", message, message.length(), true),
                controller.getInfo("handheld").getFiles().get("file"));
        assertDoesNotThrow(() -> controller.sendFile("file", "handheld", "teleporting"));
        controller.simulate(2);
        String message2 = message.replace("t", "");
        assertEquals(null, controller.getInfo("teleporting").getFiles().get("file"));
        assertEquals(new FileInfoResponse("file", message2, message2.length(), true),
                controller.getInfo("handheld").getFiles().get("file"));

    }

    @Test
    public void testExample() {
        // Task 2
        // Example from the specification
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to
        // download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "StandardSatellite", 10000 + RADIUS_OF_JUPITER,
                Angle.fromDegrees(320));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(310));
        controller.createDevice("DeviceC", "HandheldDevice", Angle.fromDegrees(320));

        String msg = "Hey";
        controller.addFileToDevice("DeviceC", "FileAlpha", msg);
        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "DeviceC", "Satellite1"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false),
                controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        controller.simulate(msg.length() * 2);
        assertEquals(new FileInfoResponse("FileAlpha", msg, msg.length(), true),
                controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "Satellite1", "DeviceB"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false),
                controller.getInfo("DeviceB").getFiles().get("FileAlpha"));

        controller.simulate(msg.length());
        assertEquals(new FileInfoResponse("FileAlpha", msg, msg.length(), true),
                controller.getInfo("DeviceB").getFiles().get("FileAlpha"));

        // Hints for further testing:
        // - What about checking about the progress of the message half way through?
        // - Device/s get out of range of satellite
        // ... and so on.
    }

    // EXCEPTION TESTS
    @Test
    public void testMaxBytesException() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90));

        String message = "rvohgdgmpsvyqbxigdodpinxrxlgttdhgrfyfduwwdkfhrxovppyhuikmbftfnokwttxmalzzniayizrzvavnmmxe";
        String messageContinued = "srtdjmcqgswrosvielwzeovmewzvhzawsrxysfgoxund";

        controller.addFileToDevice("handheld", "file1", message + messageContinued);
        assertThrows(FileTransferException.VirtualFileNoStorageSpaceException.class,
                () -> controller.sendFile("file1", "handheld", "standard"));
    }

    @Test
    public void testBandwidthException() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90));

        String message = "abc";
        controller.addFileToDevice("handheld", "file1", message);
        controller.addFileToDevice("handheld", "file2", message);
        assertDoesNotThrow(() -> controller.sendFile("file1", "handheld", "standard"));
        assertEquals(new FileInfoResponse("file1", "", message.length(), false),
                controller.getInfo("standard").getFiles().get("file1"));
        assertThrows(FileTransferException.VirtualFileNoBandwidthException.class,
                () -> controller.sendFile("file2", "handheld", "standard"));
    }

    @Test
    public void testFileAlreadyExistsException() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("laptop", "LaptopDevice", Angle.fromDegrees(90));

        String message = "abc";
        controller.addFileToDevice("laptop", "file", message);
        assertDoesNotThrow(() -> controller.sendFile("file", "laptop", "standard"));
        assertEquals(new FileInfoResponse("file", "", 3, false), controller.getInfo("standard").getFiles().get("file"));

        controller.simulate(5);
        assertThrows(FileTransferException.VirtualFileAlreadyExistsException.class,
                () -> controller.sendFile("file", "laptop", "standard"));

    }

    @Test
    public void testFileNotFoundException() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90));

        assertThrows(FileTransferException.VirtualFileNotFoundException.class,
                () -> controller.sendFile("fileNotFound", "handheld", "standard"));
    }

    @Test
    public void testMaxFilesException() {
        BlackoutController controller = new BlackoutController();

        controller.createSatellite("standard", "StandardSatellite", 75000, Angle.fromDegrees(90));
        controller.createDevice("handheld", "HandheldDevice", Angle.fromDegrees(90));

        String message = "a";
        controller.addFileToDevice("handheld", "file1", message);
        controller.addFileToDevice("handheld", "file2", message);
        controller.addFileToDevice("handheld", "file3", message);
        controller.addFileToDevice("handheld", "file4", message);
        assertDoesNotThrow(() -> controller.sendFile("file1", "handheld", "standard"));
        controller.simulate();
        assertDoesNotThrow(() -> controller.sendFile("file2", "handheld", "standard"));
        controller.simulate();
        assertDoesNotThrow(() -> controller.sendFile("file3", "handheld", "standard"));
        controller.simulate();

        assertThrows(FileTransferException.VirtualFileNoStorageSpaceException.class,
                () -> controller.sendFile("file4", "handheld", "standard"));
    }

    @Test
    public void testSomeExceptionsForSend() {
        // just some of them... you'll have to test the rest
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to
        // download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "StandardSatellite", 5000 + RADIUS_OF_JUPITER, Angle.fromDegrees(320));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(310));
        controller.createDevice("DeviceC", "HandheldDevice", Angle.fromDegrees(320));

        String msg = "Hey";
        controller.addFileToDevice("DeviceC", "FileAlpha", msg);
        assertThrows(FileTransferException.VirtualFileNotFoundException.class,
                () -> controller.sendFile("NonExistentFile", "DeviceC", "Satellite1"));

        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "DeviceC", "Satellite1"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false),
                controller.getInfo("Satellite1").getFiles().get("FileAlpha"));
        controller.simulate(msg.length() * 2);

        assertThrows(FileTransferException.VirtualFileAlreadyExistsException.class,
                () -> controller.sendFile("FileAlpha", "DeviceC", "Satellite1"));
    }

}
