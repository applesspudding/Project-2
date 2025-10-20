import bagel.Image;
import bagel.util.Point;

/**
 * Hazard that applies damage for as long as the player is on it
 */
public class River{
    private final Point position;
    private final Image image;
    private final double damagePerFrame;

    public River(Point position) {
        this.position = position;
        this.image = new Image("res/river.png");
        damagePerFrame = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("riverDamagePerFrame"));
    }

    public void update(Player player) {
        if (hasCollidedWith(player)) {
            player.receiveDamage(damagePerFrame);
        }
    }

    public void draw() {
        image.draw(position.x, position.y);
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }
}