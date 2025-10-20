import bagel.Image;
import bagel.Window;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Projectile fired by the player that damages enemies and destroys certain objects
 */
public class Bullet {
    private Point position;
    private final double dx;
    private final double dy;
    private final double speed;
    private final int damage;
    private boolean active = true;
    private final Image image;

    public Bullet(Point startPos, Point targetPos, int damage) {
        this.position = new Point(startPos.x, startPos.y);
        this.damage = damage;
        this.image = new Image("res/bullet.png");
        this.speed = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("bulletSpeed"));

        // Calculate direction vector
        double dirX = targetPos.x - startPos.x;
        double dirY = targetPos.y - startPos.y;
        double length = Math.sqrt(dirX * dirX + dirY * dirY);

        // Normalize and apply speed
        if (length > 0) {
            this.dx = (dirX / length) * speed;
            this.dy = (dirY / length) * speed;
        } else {
            this.dx = 0;
            this.dy = 0;
        }
    }

    public void update() {
        if (!active) return;

        // Move bullet
        position = new Point(position.x + dx, position.y + dy);

        // Deactivate if out of bounds
        if (position.x < 0 || position.x > Window.getWidth() ||
                position.y < 0 || position.y > Window.getHeight()) {
            active = false;
        }
    }

    public void draw() {
        if (active) {
            image.draw(position.x, position.y);
        }
    }

    public boolean hasCollidedWith(Rectangle boundingBox) {
        return active && image.getBoundingBoxAt(position).intersects(boundingBox);
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public int getDamage() {
        return damage;
    }

    public Rectangle getBoundingBox() {
        return image.getBoundingBoxAt(position);
    }
}