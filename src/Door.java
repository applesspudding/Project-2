import bagel.Image;
import bagel.util.Point;

/**
 * Door which can be locked or unlocked, allows the player to move to the room it's connected to
 */
public class Door {
    private final Point position;
    private Image image;
    public final String toRoomName;
    public BattleRoom battleRoom; // only set if this door is inside a Battle Room
    private boolean unlocked = false;
    private boolean justEntered = false; // when the player only just entered this door's room
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
        return image.getBoundingBoxAt(position).intersects(player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }

    private void onCollideWith(Player player) {
        // when the player only just entered this door's room, overlapping with the unlocked door shouldn't trigger room transition
        if (unlocked && !justEntered) {
            ShadowDungeon.changeRoom(toRoomName);
        }
        if (!unlocked) {
            player.move(player.getPrevPosition().x, player.getPrevPosition().y);
        }
    }

    private void onNoLongerCollide() {
        // when the player only just moved away from the unlocked door after walking through it
        if (unlocked && justEntered) {
            justEntered = false;

            // Battle Room activation conditions
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
}
