package unsw.blackout;

import unsw.utils.Angle;
import static unsw.utils.MathsHelper.CLOCKWISE;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public abstract class Device extends Entity {
    public Device(String id, String type, double height, Angle position, boolean isMoving) {
        super(id, type, height, position);
        this.setMovingDevice(isMoving);
        this.setDirection(CLOCKWISE);
        this.setRestrictions(new FileTransferRestrictions(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE));
    }

    // Moves a device on its input slope
    public void move(Slope slope) {
        this.setPosition(Angle.fromDegrees(this.getPosition().toDegrees() % 360));
        this.setHeight(this.getNewHeight(slope));
        this.setPosition(this.getPosition().add(this.getAngularVelocity()));
    }

    // Calculates the new height of a device on an input slope
    public double getNewHeight(Slope slope) {
        double newHeight;

        if (slope != null) {
            double deltaH = slope.getGradient() * this.getAngularVelocity().toDegrees();
            newHeight = this.getHeight() + deltaH;
        } else {
            newHeight = RADIUS_OF_JUPITER;
        }

        return newHeight;
    }
}
