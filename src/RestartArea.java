import bagel.Image;
import bagel.Input;
import bagel.Keys;
import bagel.util.Point;

/**
 * Area in Prep or End Room where the player can trigger a game reset
 */
public class RestartArea {
    private final Point position;
    private final Image image;

    public RestartArea(Point position) {
        this.position = position;
        this.image = new Image("res/restart_area.png");
    }

    public void update(Input input, Player player) {
        if (hasCollidedWith(player) && input.wasPressed(Keys.ENTER)) {
            ShadowDungeon.resetGameState(ShadowDungeon.getGameProps());
        }
    }

    public void draw() {
        image.draw(position.x, position.y);
    }

    public boolean hasCollidedWith(Player player) {
        return image.getBoundingBoxAt(position).intersects(player.getCurrImage().getBoundingBoxAt(player.getPosition()));
    }
}
