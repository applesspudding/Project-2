import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Door which can be locked or unlocked, allows the player to move to the room it's connected to
 */
public class Door {
    private final Point position;
    private Image image;
    public final String toRoomName;
    public BattleRoom battleRoom;
    private boolean unlocked = false;
    private boolean justEntered = false;
    private boolean shouldLockAgain = false;

    private static final Image LOCKED = new Image("res/locked_door.png");
    private static final Image UNLOCKED = new Image("res/unlocked_door.png");

    public Door(Point position, String toRoomName) {
        this.position = position;
        this.image = LOCKED;
        this.toRoomName = toRoomName;
    }

    public Door(Point position, String toRoomName, BattleRoom battleRoom) {
        this.position = position;
        this.image = LOCKED;
        this.toRoomName = toRoomName;
        this.battleRoom = battleRoom;
    }

    public void update(Player player) {
        if (hasCollidedWith(player)) {
            onCollideWith(player);
        } else {
            onNoLongerCollide();
        }
    }

    public void draw() {
        image.draw(position.x, position.y);
    }

    public void unlock(boolean justEntered) {
        unlocked = true;
        image = UNLOCKED;
        this.justEntered = justEntered;
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(
                player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }

    private void onCollideWith(Player player) {
        if (unlocked && !justEntered) {
            ShadowDungeon.changeRoom(toRoomName);
        }
        if (!unlocked) {
            player.move(player.getPrevPosition().x, player.getPrevPosition().y);
        }
    }

    private void onNoLongerCollide() {
        if (unlocked && justEntered) {
            justEntered = false;

            if (shouldLockAgain && battleRoom != null && !battleRoom.isComplete()) {
                unlocked = false;
                image = LOCKED;
                battleRoom.activateEnemies();
            }
        }
    }

    public void lock() {
        unlocked = false;
        image = LOCKED;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setShouldLockAgain() {
        this.shouldLockAgain = true;
    }

    public Point getPosition() {
        return position;
    }

    public Rectangle getBoundingBox() {
        return image.getBoundingBoxAt(position);
    }
}