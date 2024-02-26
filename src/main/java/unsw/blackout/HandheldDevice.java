package unsw.blackout;

import unsw.utils.Angle;

public class HandheldDevice extends Device {
    public HandheldDevice(String deviceId, String type, double height, Angle position, boolean isMoving) {
        super(deviceId, type, height, position, isMoving);
        this.setRange(50000);
        this.setLinearV(50);
    }
}
