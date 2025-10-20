import bagel.*;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.util.ArrayList;

/**
 * Player character that can move around and between rooms, shoot, defeat enemies, collect coins and keys
 */
public class Player {
    private Point prevPosition;
    private Point position;
    private Image currImage;
    private double health;
    private double speed;
    private double coins = 0;
    private int keys = 0;
    private int weaponLevel = 0; // 0=Standard, 1=Advanced, 2=Elite
    private boolean faceLeft = false;
    private CharacterType characterType = CharacterType.NONE;
    private int shootCooldown = 0;
    private final int shootFrequency;
    private final ArrayList<Bullet> bullets;

    private static final Image RIGHT_IMAGE = new Image("res/player_right.png");
    private static final Image LEFT_IMAGE = new Image("res/player_left.png");
    private static final Image ROBOT_RIGHT = new Image("res/robot_right.png");
    private static final Image ROBOT_LEFT = new Image("res/robot_left.png");
    private static final Image MARINE_RIGHT = new Image("res/marine_right.png");
    private static final Image MARINE_LEFT = new Image("res/marine_left.png");

    public Player(Point position) {
        this.position = position;
        this.currImage = RIGHT_IMAGE;
        this.speed = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("movingSpeed"));
        this.health = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("initialHealth"));
        this.shootFrequency = Integer.parseInt(ShadowDungeon.getGameProps().getProperty("bulletfreq"));
        this.bullets = new ArrayList<>();
    }

    public void update(Input input) {
        // Check movement keys and mouse cursor
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

        // Update the player position accordingly and ensure it can't move past the game window
        Rectangle rect = currImage.getBoundingBoxAt(new Point(currX, currY));
        Point topLeft = rect.topLeft();
        Point bottomRight = rect.bottomRight();
        if (topLeft.x >= 0 && bottomRight.x <= Window.getWidth() && topLeft.y >= 0 && bottomRight.y <= Window.getHeight()) {
            move(currX, currY);
        }

        // Handle shooting (only if character is selected)
        if (characterType != CharacterType.NONE) {
            if (shootCooldown > 0) {
                shootCooldown--;
            }

            if (input.isDown(MouseButtons.LEFT) && shootCooldown == 0) {
                shoot(new Point(input.getMouseX(), input.getMouseY()));
                shootCooldown = shootFrequency;
            }
        }

        // Update bullets
        bullets.removeIf(bullet -> {
            bullet.update();
            return !bullet.isActive();
        });
    }

    public void move(double x, double y) {
        prevPosition = position;
        position = new Point(x, y);
    }

    public void draw() {
        // Select appropriate image based on character and direction
        switch (characterType) {
            case ROBOT:
                currImage = faceLeft ? ROBOT_LEFT : ROBOT_RIGHT;
                break;
            case MARINE:
                currImage = faceLeft ? MARINE_LEFT : MARINE_RIGHT;
                break;
            default:
                currImage = faceLeft ? LEFT_IMAGE : RIGHT_IMAGE;
        }

        currImage.draw(position.x, position.y);

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw();
        }

        // Draw UI stats
        UserInterface.drawStats(health, coins, keys, weaponLevel);
    }

    private void shoot(Point targetPos) {
        int damage = getWeaponDamage();
        bullets.add(new Bullet(position, targetPos, damage));
    }

    private int getWeaponDamage() {
        switch (weaponLevel) {
            case 0: return Integer.parseInt(ShadowDungeon.getGameProps().getProperty("weaponStandardDamage"));
            case 1: return Integer.parseInt(ShadowDungeon.getGameProps().getProperty("weaponAdvanceDamage"));
            case 2: return Integer.parseInt(ShadowDungeon.getGameProps().getProperty("weaponEliteDamage"));
            default: return 30;
        }
    }

    public void selectCharacter(CharacterType type) {
        this.characterType = type;
    }

    public void earnCoins(double coins) {
        // Robot gets bonus coins
        if (characterType == CharacterType.ROBOT) {
            coins += Double.parseDouble(ShadowDungeon.getGameProps().getProperty("robotExtraCoin"));
        }
        this.coins += coins;
    }

    public void addKey() {
        keys++;
    }

    public boolean useKey() {
        if (keys > 0) {
            keys--;
            return true;
        }
        return false;
    }

    public boolean upgradeWeapon() {
        int cost = Integer.parseInt(ShadowDungeon.getGameProps().getProperty("weaponPurchase"));
        if (weaponLevel < 2 && coins >= cost) {
            coins -= cost;
            weaponLevel++;
            return true;
        }
        return false;
    }

    public boolean purchaseHealth() {
        int cost = Integer.parseInt(ShadowDungeon.getGameProps().getProperty("healthPurchase"));
        double healthGain = Double.parseDouble(ShadowDungeon.getGameProps().getProperty("healthPurchase"));
        if (coins >= cost) {
            coins -= cost;
            health += healthGain;
            return true;
        }
        return false;
    }

    public void receiveDamage(double damage) {
        // Marine is immune to river damage
        if (characterType == CharacterType.MARINE &&
                damage == Double.parseDouble(ShadowDungeon.getGameProps().getProperty("riverDamagePerFrame"))) {
            return;
        }

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

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public int getKeys() {
        return keys;
    }

    public double getCoins() {
        return coins;
    }

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public double getHealth() {
        return health;
    }
}

/**
 * Enum for character types
 */
enum CharacterType {
    NONE,
    ROBOT,
    MARINE
}