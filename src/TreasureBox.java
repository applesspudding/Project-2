import bagel.Image;
import bagel.Input;
import bagel.Keys;
import bagel.util.Point;

/**
 * Chest that requires a key to unlock and earn coins
 */
public class TreasureBox {
    private final Point position;
    private final Image image;
    private final double coinValue;
    private boolean active = true;

    public TreasureBox(Point position, double coinValue) {
        this.position = position;
        this.coinValue = coinValue;
        this.image = new Image("res/treasure_box.png");
    }

    public void update(Input input, Player player) {
        if (!active) return;

        if (hasCollidedWith(player) && input.wasPressed(Keys.K)) {
            // Check if player has a key
            if (player.useKey()) {
                player.earnCoins(coinValue);
                active = false;
            }
        }
    }

    public void draw() {
        if (active) {
            image.draw(position.x, position.y);
        }
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(
                player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }

    public boolean isActive() {
        return active;
    }
}