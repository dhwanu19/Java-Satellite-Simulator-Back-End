package unsw.blackout;

import unsw.utils.Angle;

import static unsw.utils.MathsHelper.CLOCKWISE;
import static unsw.utils.MathsHelper.ANTI_CLOCKWISE;

public class RelaySatellite extends Satellite {
    private boolean spawnedOutOfBounds;

    public RelaySatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, height, position);
        this.setLinearV(1500);
        this.setSpawnedOutOfBounds(false);
        this.setDirection(CLOCKWISE);
        this.setRange(300000);
        this.setSpawnConditions();
    }

    @Override
    public void move() {
        Angle currPos = this.getPosition();
        // Condition for whether or not currently out of bounds
        boolean outOfBounds = (currPos.compareTo(Angle.fromDegrees(190)) == 1)
                || (currPos.compareTo(Angle.fromDegrees(140)) == -1);

        // Change direction if out of bounds
        if (outOfBounds && !spawnedOutOfBounds) {
            super.setDirection(this.getDirection() * -1);
        } else if (!outOfBounds && spawnedOutOfBounds) {
            spawnedOutOfBounds = false;
        }

        double posChange = this.getDirection() * Math.toDegrees(this.getLinearV() / this.getHeight());
        double newPos = ((this.getPosition().toDegrees() + posChange + 360) % 360);

        // newPos = currPos - posChange
        this.setPosition(Angle.fromDegrees(newPos));
    }

    // Corrects the spawn direction and spawnedOutOfBounds boolean if required;
    public void setSpawnConditions() {
        boolean outOfBounds = ((this.getPosition().compareTo(Angle.fromDegrees(190)) == 1)
                || (this.getPosition().compareTo(Angle.fromDegrees(140)) == -1));

        if (outOfBounds) {
            this.spawnedOutOfBounds = true;
            if ((this.getPosition().compareTo(Angle.fromDegrees(345)) >= 0)
                    || (this.getPosition().compareTo(Angle.fromDegrees(140)) <= 0)) {
                this.setDirection(ANTI_CLOCKWISE);
            }
        }
    }

    public boolean isSpawnedOutOfBounds() {
        return spawnedOutOfBounds;
    }

    public void setSpawnedOutOfBounds(boolean spawnedOutOfBounds) {
        this.spawnedOutOfBounds = spawnedOutOfBounds;
    }

}
