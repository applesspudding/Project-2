import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Destructible obstacle that blocks the player and can be destroyed by bullets
 */
public class Table {
    private final Point position;
    private final Image image;
    private boolean active = true;

    public Table(Point position) {
        this.position = position;
        this.image = new Image("res/table.png");
    }

    public void update(Player player) {
        if (!active) return;

        // Block player movement
        if (hasCollidedWith(player)) {
            player.move(player.getPrevPosition().x, player.getPrevPosition().y);
        }
    }

    public void draw() {
        if (active) {
            image.draw(position.x, position.y);
        }
    }

    public boolean checkBulletCollision(Bullet bullet) {
        if (active && bullet.hasCollidedWith(getBoundingBox())) {
            bullet.deactivate();
            active = false;
            return true;
        }
        return false;
    }

    public boolean checkFireballCollision(Rectangle fireballBox) {
        if (active && getBoundingBox().intersects(fireballBox)) {
            return true; // Fireball is destroyed but table remains
        }
        return false;
    }

    public boolean hasCollidedWith(Player player) {
        return active && image.getBoundingBoxAt(position).intersects(
                player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }

    public Rectangle getBoundingBox() {
        return image.getBoundingBoxAt(position);
    }

    public boolean isActive() {
        return active;
    }
}