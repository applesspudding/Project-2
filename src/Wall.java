import bagel.Image;
import bagel.util.Point;

/**
 * Obstacle that blocks the player from moving through it
 */
public class Wall {
    private final Point position;
    private final Image image;

    public Wall(Point position) {
        this.position = position;
        this.image = new Image("res/wall.png");
    }

    public void update(Player player) {
        if (hasCollidedWith(player)) {
            // set the player to its position prior to attempting to move through this wall
            player.move(player.getPrevPosition().x, player.getPrevPosition().y);
        }
    }

    public void draw() {
        image.draw(position.x, position.y);
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }
}