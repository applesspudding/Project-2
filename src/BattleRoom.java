import bagel.Input;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Room with doors that are locked until the player defeats all enemies
 */
public class BattleRoom {
    private Player player;
    private Door primaryDoor;
    private Door secondaryDoor;
    private KeyBulletKin keyBulletKin;
    private ArrayList<BulletKin> bulletKins;
    private ArrayList<AshenBulletKin> ashenBulletKins;
    private ArrayList<TreasureBox> treasureBoxes;
    private ArrayList<Wall> walls;
    private ArrayList<Table> tables;
    private ArrayList<Basket> baskets;
    private ArrayList<River> rivers;
    private ArrayList<Key> keys;
    private boolean stopCurrentUpdateCall = false;
    private boolean isComplete = false;
    private final String nextRoomName;
    private final String roomName;

    public BattleRoom(String roomName, String nextRoomName) {
        walls = new ArrayList<>();
        tables = new ArrayList<>();
        baskets = new ArrayList<>();
        rivers = new ArrayList<>();
        treasureBoxes = new ArrayList<>();
        bulletKins = new ArrayList<>();
        ashenBulletKins = new ArrayList<>();
        keys = new ArrayList<>();
        this.roomName = roomName;
        this.nextRoomName = nextRoomName;
    }

    public void initEntities(Properties gameProperties) {
        for (Map.Entry<Object, Object> entry: gameProperties.entrySet()) {
            String roomSuffix = String.format(".%s", roomName);

            if (entry.getKey().toString().contains(roomSuffix)) {
                String objectType = entry.getKey().toString()
                        .substring(0, entry.getKey().toString().length() - roomSuffix.length());
                String propertyValue = entry.getValue().toString();

                if (propertyValue.equals("0")) {
                    continue;
                }

                String[] coordinates;
                for (String coords: propertyValue.split(";")) {
                    switch (objectType) {
                        case "primarydoor":
                            coordinates = propertyValue.split(",");
                            primaryDoor = new Door(IOUtils.parseCoords(propertyValue), coordinates[2], this);
                            break;
                        case "secondarydoor":
                            coordinates = propertyValue.split(",");
                            secondaryDoor = new Door(IOUtils.parseCoords(propertyValue), coordinates[2], this);
                            break;
                        case "keyBulletKin":
                            keyBulletKin = new KeyBulletKin(propertyValue);
                            break;
                        case "bulletKin":
                            bulletKins.add(new BulletKin(IOUtils.parseCoords(coords)));
                            break;
                        case "ashenBulletKin":
                            ashenBulletKins.add(new AshenBulletKin(IOUtils.parseCoords(coords)));
                            break;
                        case "wall":
                            walls.add(new Wall(IOUtils.parseCoords(coords)));
                            break;
                        case "table":
                            tables.add(new Table(IOUtils.parseCoords(coords)));
                            break;
                        case "basket":
                            baskets.add(new Basket(IOUtils.parseCoords(coords)));
                            break;
                        case "treasurebox":
                            TreasureBox treasureBox = new TreasureBox(IOUtils.parseCoords(coords),
                                    Double.parseDouble(coords.split(",")[2]));
                            treasureBoxes.add(treasureBox);
                            break;
                        case "river":
                            rivers.add(new River(IOUtils.parseCoords(coords)));
                            break;
                        default:
                    }
                }
            }
        }
    }

    public void update(Input input) {
        // Update and draw doors
        primaryDoor.update(player);
        primaryDoor.draw();
        if (stopUpdatingEarlyIfNeeded()) {
            return;
        }

        secondaryDoor.update(player);
        secondaryDoor.draw();
        if (stopUpdatingEarlyIfNeeded()) {
            return;
        }

        // Update and draw obstacles
        for (Wall wall: walls) {
            wall.update(player);
            wall.draw();
        }

        for (Table table: tables) {
            if (table.isActive()) {
                table.update(player);
                table.draw();
            }
        }

        for (Basket basket: baskets) {
            if (basket.isActive()) {
                basket.update(player);
                basket.draw();
            }
        }

        for (River river: rivers) {
            river.update(player);
            river.draw();
        }

        // Update and draw enemies
        if (keyBulletKin != null && keyBulletKin.isActive()) {
            keyBulletKin.update(player);
            keyBulletKin.draw();
        }

        for (BulletKin bulletKin : bulletKins) {
            if (bulletKin.isActive()) {
                bulletKin.update(player);
                bulletKin.draw();
            }
        }

        for (AshenBulletKin ashenBulletKin : ashenBulletKins) {
            if (ashenBulletKin.isActive()) {
                ashenBulletKin.update(player);
                ashenBulletKin.draw();
            }
        }

        // Update treasure boxes
        for (TreasureBox treasureBox: treasureBoxes) {
            if (treasureBox.isActive()) {
                treasureBox.update(input, player);
                treasureBox.draw();
            }
        }

        // Update keys
        keys.removeIf(key -> {
            if (key.isActive()) {
                key.update(player);
                key.draw();
                return false;
            }
            return true;
        });

        // Handle bullet collisions
        if (player != null) {
            handleBulletCollisions();
        }

        // Update and draw player
        if (player != null) {
            player.update(input);
            player.draw();
        }

        // Check if all enemies defeated
        if (noMoreEnemies() && !isComplete()) {
            setComplete(true);
            unlockAllDoors();
        }
    }

    private void handleBulletCollisions() {
        for (Bullet bullet : player.getBullets()) {
            if (!bullet.isActive()) continue;

            // Check enemy collisions
            if (keyBulletKin != null && keyBulletKin.isActive()) {
                if (keyBulletKin.checkBulletCollision(bullet)) {
                    if (keyBulletKin.isDead()) {
                        // Drop key at enemy position
                        keys.add(new Key(keyBulletKin.getPosition()));
                    }
                    continue;
                }
            }

            for (BulletKin bulletKin : bulletKins) {
                if (bulletKin.checkBulletCollision(bullet)) {
                    if (bulletKin.isDead()) {
                        player.earnCoins(bulletKin.getCoinReward());
                    }
                    break;
                }
            }

            for (AshenBulletKin ashenBulletKin : ashenBulletKins) {
                if (ashenBulletKin.checkBulletCollision(bullet)) {
                    if (ashenBulletKin.isDead()) {
                        player.earnCoins(ashenBulletKin.getCoinReward());
                    }
                    break;
                }
            }

            // Check obstacle collisions
            for (Table table : tables) {
                if (table.checkBulletCollision(bullet)) {
                    break;
                }
            }

            for (Basket basket : baskets) {
                if (basket.checkBulletCollision(bullet, player)) {
                    break;
                }
            }

            // Check wall collisions
            for (Wall wall : walls) {
                if (bullet.hasCollidedWith(wall.getBoundingBox())) {
                    bullet.deactivate();
                    break;
                }
            }

            // Check door collisions
            if (!primaryDoor.isUnlocked() && bullet.hasCollidedWith(primaryDoor.getBoundingBox())) {
                bullet.deactivate();
            }
            if (!secondaryDoor.isUnlocked() && bullet.hasCollidedWith(secondaryDoor.getBoundingBox())) {
                bullet.deactivate();
            }
        }

        // Check fireball collisions with obstacles
        for (BulletKin bulletKin : bulletKins) {
            for (Fireball fireball : bulletKin.getFireballs()) {
                if (!fireball.isActive()) continue;

                for (Table table : tables) {
                    if (table.checkFireballCollision(fireball.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                for (Basket basket : baskets) {
                    if (basket.checkFireballCollision(fireball.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                for (Wall wall : walls) {
                    if (fireball.hasCollidedWith(wall.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                if (!primaryDoor.isUnlocked() && fireball.hasCollidedWith(primaryDoor.getBoundingBox())) {
                    fireball.deactivate();
                }
                if (!secondaryDoor.isUnlocked() && fireball.hasCollidedWith(secondaryDoor.getBoundingBox())) {
                    fireball.deactivate();
                }
            }
        }

        for (AshenBulletKin ashenBulletKin : ashenBulletKins) {
            for (Fireball fireball : ashenBulletKin.getFireballs()) {
                if (!fireball.isActive()) continue;

                for (Table table : tables) {
                    if (table.checkFireballCollision(fireball.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                for (Basket basket : baskets) {
                    if (basket.checkFireballCollision(fireball.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                for (Wall wall : walls) {
                    if (fireball.hasCollidedWith(wall.getBoundingBox())) {
                        fireball.deactivate();
                        break;
                    }
                }

                if (!primaryDoor.isUnlocked() && fireball.hasCollidedWith(primaryDoor.getBoundingBox())) {
                    fireball.deactivate();
                }
                if (!secondaryDoor.isUnlocked() && fireball.hasCollidedWith(secondaryDoor.getBoundingBox())) {
                    fireball.deactivate();
                }
            }
        }
    }

    private boolean stopUpdatingEarlyIfNeeded() {
        if (stopCurrentUpdateCall) {
            player = null;
            stopCurrentUpdateCall = false;
            return true;
        }
        return false;
    }

    public void stopCurrentUpdateCall() {
        stopCurrentUpdateCall = true;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Door findDoorByDestination(String roomName) {
        if (primaryDoor.toRoomName.equals(roomName)) {
            return primaryDoor;
        } else {
            return secondaryDoor;
        }
    }

    private void unlockAllDoors() {
        primaryDoor.unlock(false);
        secondaryDoor.unlock(false);
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public void activateEnemies() {
        if (keyBulletKin != null) {
            keyBulletKin.setActive(true);
        }
        for (BulletKin bulletKin : bulletKins) {
            bulletKin.setActive(true);
        }
        for (AshenBulletKin ashenBulletKin : ashenBulletKins) {
            ashenBulletKin.setActive(true);
        }
    }

    public boolean noMoreEnemies() {
        boolean keyBulletKinDead = keyBulletKin == null || keyBulletKin.isDead();
        boolean allBulletKinsDead = bulletKins.stream().allMatch(BulletKin::isDead);
        boolean allAshenBulletKinsDead = ashenBulletKins.stream().allMatch(AshenBulletKin::isDead);

        return keyBulletKinDead && allBulletKinsDead && allAshenBulletKinsDead;
    }
}