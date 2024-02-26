package unsw.blackout;

import unsw.utils.Angle;

public class DesktopDevice extends Device {
    public DesktopDevice(String deviceId, String type, double height, Angle position, boolean isMoving) {
        super(deviceId, type, height, position, isMoving);

        this.setRange(200000);
        this.setLinearV(20);

    }
}
