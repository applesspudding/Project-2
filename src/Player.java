import bagel.*;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Player character that can move around and between rooms, defeat enemies, collect coins
 */
public class Player {
    private Point prevPosition;
    private Point position;
    private Image currImage;
    private double health;
    private double speed;
    private double coins = 0;
    private boolean faceLeft = false;

    private static final Image RIGHT_IMAGE = new Image("res/player_right.png");
    private static final Image LEFT_IMAGE = new Image("res/player_left.png");

    public Player(Point position) {
        this.position = position;
        this.currImage = RIGHT_IMAGE;
        this.speed = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("movingSpeed"));
        this.health = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("initialHealth"));
    }

    public void update(Input input) {
        // check movement keys and mouse cursor
        double currX = position.x;
        double currY = position.y;

        if (input.isDown(Keys.A)) {
            currX -= speed;
        }
        if (input.isDown(Keys.D)) {
            currX += speed;
        }
        if (input.isDown(Keys.W)) {
            currY -= speed;
        }
        if (input.isDown(Keys.S)) {
            currY += speed;
        }

        faceLeft = input.getMouseX() < currX;

        // update the player position accordingly and ensure it can't move past the game window
        Rectangle rect = currImage.getBoundingBoxAt(new Point(currX, currY));
        Point topLeft = rect.topLeft();
        Point bottomRight = rect.bottomRight();
        if (topLeft.x >= 0 && bottomRight.x <= Window.getWidth() && topLeft.y >= 0 && bottomRight.y <= Window.getHeight()) {
            move(currX, currY);
        }
    }
    
    public void move(double x, double y) {
        prevPosition = position;
        position = new Point(x, y);
    }

    public void draw() {
        currImage = faceLeft ? LEFT_IMAGE : RIGHT_IMAGE; // NOTE: this is an example of using the ternary operator
        currImage.draw(position.x, position.y);
        UserInterface.drawStats(health, coins);
    }

    public void earnCoins(double coins) {
        this.coins += coins;
    }

    public void receiveDamage(double damage) {
        health -= damage;
        if (health <= 0) {
            ShadowDungeon.changeToGameOverRoom();
        }
    }

    public Point getPosition() {
        return position;
    }

    public Image getCurrImage() {
        return currImage;
    }

    public Point getPrevPosition() {
        return prevPosition;
    }
}
