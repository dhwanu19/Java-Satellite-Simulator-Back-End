package unsw.blackout;

import unsw.utils.Angle;

public class LaptopDevice extends Device {
    public LaptopDevice(String deviceId, String type, double height, Angle position, boolean isMoving) {
        super(deviceId, type, height, position, isMoving);
        this.setRange(100000);
        this.setLinearV(30);
    }
}
