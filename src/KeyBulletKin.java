import bagel.Image;
import bagel.util.Point;

import java.util.ArrayList;

/**
 * Enemy that moves along a path and drops a key when defeated
 */
public class KeyBulletKin {
    private Point position;
    private final Image image;
    private double health;
    private boolean active = false;
    private boolean dead = false;
    private final ArrayList<Point> path;
    private int currentPathIndex = 0;
    private final double speed;
    private static final double CONTACT_DAMAGE_PER_FRAME = 0.2;

    public KeyBulletKin(Point startPos) {
        this.position = startPos;
        this.image = new Image("res/key_bullet_kin.png");
        this.health = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("keyBulletKinHealth"));
        this.speed = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("keyBulletKinSpeed"));
        this.path = new ArrayList<>();
    }

    public KeyBulletKin(String pathString) {
        this.image = new Image("res/key_bullet_kin.png");
        this.health = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("keyBulletKinHealth"));
        this.speed = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("keyBulletKinSpeed"));
        this.path = new ArrayList<>();

        // Parse path coordinates
        String[] coords = pathString.split(";");
        for (String coord : coords) {
            path.add(IOUtils.parseCoords(coord));
        }

        if (!path.isEmpty()) {
            this.position = path.get(0);
            this.currentPathIndex = 1 % path.size();
        } else {
            this.position = new Point(0, 0);
        }
    }

    public void update(Player player) {
        if (!active || dead) return;

        // Check collision with player for contact damage
        if (hasCollidedWith(player)) {
            player.receiveDamage(CONTACT_DAMAGE_PER_FRAME);
        }

        // Move along path
        if (!path.isEmpty()) {
            Point target = path.get(currentPathIndex);
            double dx = target.x - position.x;
            double dy = target.y - position.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < speed) {
                // Reached target, move to next point
                position = target;
                currentPathIndex = (currentPathIndex + 1) % path.size();
            } else {
                // Move towards target
                double moveX = (dx / distance) * speed;
                double moveY = (dy / distance) * speed;
                position = new Point(position.x + moveX, position.y + moveY);
            }
        }
    }

    public void draw() {
        if (active && !dead) {
            image.draw(position.x, position.y);
        }
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(
                player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }

    public boolean checkBulletCollision(Bullet bullet) {
        if (active && !dead && bullet.hasCollidedWith(image.getBoundingBoxAt(position))) {
            health -= bullet.getDamage();
            bullet.deactivate();

            if (health <= 0) {
                dead = true;
            }
            return true;
        }
        return false;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Point getPosition() {
        return position;
    }

    public void addPathPoint(Point point) {
        path.add(point);
        if (path.size() == 1) {
            position = point;
            currentPathIndex = 0;
        }
    }
}