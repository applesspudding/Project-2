import bagel.Image;
import bagel.Input;
import bagel.Keys;
import bagel.util.Point;

/**
 * Store interface for purchasing weapon upgrades and health
 */
public class Store {
    private final Image image;
    private final Point position;
    private boolean visible = false;

    public Store() {
        this.image = new Image("res/store.png");
        this.position = IOUtils.parseCoords(ShadowDungeon.getGameProps().getProperty("store"));
    }

    public void update(Input input, Player player) {
        // Purchase weapon upgrade
        if (input.wasPressed(Keys.L)) {
            player.upgradeWeapon();
        }

        // Purchase health
        if (input.wasPressed(Keys.E)) {
            player.purchaseHealth();
        }

        // Restart game
        if (input.wasPressed(Keys.P)) {
            ShadowDungeon.resetGameState(ShadowDungeon.getGameProps());
        }
    }

    public void draw() {
        if (visible) {
            image.draw(position.x, position.y);
        }
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }
}