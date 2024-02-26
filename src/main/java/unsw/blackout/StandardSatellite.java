package unsw.blackout;

import unsw.utils.Angle;

import static unsw.utils.MathsHelper.CLOCKWISE;

public class StandardSatellite extends Satellite {
    public StandardSatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, height, position);
        this.setLinearV(2500);
        this.setDirection(CLOCKWISE);
        this.setRange(150000);
        this.setMovingDevice(false);
        this.setRestrictions(new FileTransferRestrictions(3, 80, 1, 1));
    }

    @Override
    public void move() {
        double posChange = Math.toDegrees(this.getLinearV() / this.getHeight());
        // newPos = currPos - posChange, converted such that newPos is between 0 and 360
        double newPos = ((this.getPosition().toDegrees() - posChange + 360) % 360);
        this.setPosition(Angle.fromDegrees(newPos));
    }
}
