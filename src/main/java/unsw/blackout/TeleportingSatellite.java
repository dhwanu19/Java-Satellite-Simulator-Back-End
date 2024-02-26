package unsw.blackout;

import unsw.utils.Angle;

import static unsw.utils.MathsHelper.CLOCKWISE;

import static unsw.utils.MathsHelper.ANTI_CLOCKWISE;

public class TeleportingSatellite extends Satellite {
    private boolean justTeleported;

    public TeleportingSatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, height, position);
        this.setJustTeleported(false);
        this.setLinearV(1000);
        this.setDirection(ANTI_CLOCKWISE);
        this.setRange(200000);
        this.setRestrictions(new FileTransferRestrictions(Integer.MAX_VALUE, 200, 15, 10));

    }

    @Override
    public void move() {
        this.justTeleported = false;

        double startPos = this.getPosition().toDegrees();
        double posChange = this.getDirection() * Math.toDegrees(this.getLinearV() / this.getHeight());
        double newPos = startPos + posChange;

        boolean crossed180 = ((startPos <= 180 && newPos > 180) || (startPos >= 180 && newPos < 180));

        newPos = ((newPos + 360) % 360);

        if ((getDirection() == ANTI_CLOCKWISE) && crossed180) {
            newPos = 360;
            this.setDirection(CLOCKWISE);
            this.justTeleported = true;
        } else if ((getDirection() == CLOCKWISE) && crossed180) {
            newPos = 0;
            this.setDirection(ANTI_CLOCKWISE);
            this.justTeleported = true;
        }

        this.setPosition(Angle.fromDegrees(newPos));
    }

    public boolean isJustTeleported() {
        return justTeleported;
    }

    public void setJustTeleported(boolean justTeleported) {
        this.justTeleported = justTeleported;
    }

}
