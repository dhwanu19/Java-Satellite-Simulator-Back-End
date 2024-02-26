package unsw.blackout;

import unsw.utils.Angle;

public abstract class Satellite extends Entity {
    public Satellite(String id, String type, double height, Angle position) {
        super(id, type, height, position);
    }

    public abstract void move();
}
