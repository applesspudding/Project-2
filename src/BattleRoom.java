import bagel.Input;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Room with doors that are locked until the plaer defeats all enemies
 */
public class BattleRoom {
    private Player player;
    private Door primaryDoor;
    private Door secondaryDoor;
    private KeyBulletKin keyBulletKin;
    private ArrayList<TreasureBox> treasureBoxes;
    private ArrayList<Wall> walls;
    private ArrayList<River> rivers;
    private boolean stopCurrentUpdateCall = false; // this determines whether to prematurely stop the update execution
    private boolean isComplete = false;
    private final String nextRoomName;
    private final String roomName;

    public BattleRoom(String roomName, String nextRoomName) {
        walls = new ArrayList<>();
        rivers = new ArrayList<>();
        treasureBoxes = new ArrayList<>();
        this.roomName = roomName;
        this.nextRoomName = nextRoomName;
    }

    public void initEntities(Properties gameProperties) {
        // find the configuration of game objects for this room
        for (Map.Entry<Object, Object> entry: gameProperties.entrySet()) {
            String roomSuffix = String.format(".%s", roomName);

            if (entry.getKey().toString().contains(roomSuffix)) {
                String objectType = entry.getKey().toString()
                        .substring(0, entry.getKey().toString().length() - roomSuffix.length());
                String propertyValue = entry.getValue().toString();

                // ignore if the value is 0
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
                            keyBulletKin = new KeyBulletKin(IOUtils.parseCoords(propertyValue));
                            break;
                        case "wall":
                            Wall wall = new Wall(IOUtils.parseCoords(coords));
                            walls.add(wall);
                            break;
                        case "treasurebox":
                            TreasureBox treasureBox = new TreasureBox(IOUtils.parseCoords(coords),
                                    Double.parseDouble(coords.split(",")[2]));
                            treasureBoxes.add(treasureBox);
                            break;
                        case "river":
                            River river = new River(IOUtils.parseCoords(coords));
                            rivers.add(river);
                            break;
                        default:
                    }
                }
            }
        }
    }

    public void update(Input input) {
        // update and draw all active game objects in this room
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

        if (keyBulletKin.isActive()) {
            keyBulletKin.update(player);
            keyBulletKin.draw();
        }

        for (Wall wall: walls) {
            wall.update(player);
            wall.draw();
        }

        for (River river: rivers) {
            river.update(player);
            river.draw();
        }

        for (TreasureBox treasureBox: treasureBoxes) {
            if (treasureBox.isActive()) {
                treasureBox.update(input, player);
                treasureBox.draw();
            }
        }

        if (player != null) {
            player.update(input);
            player.draw();
        }

        if (noMoreEnemies() && !isComplete()) {
            setComplete(true);
            unlockAllDoors();
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
        keyBulletKin.setActive(true);
    }

    public boolean noMoreEnemies() {
        return keyBulletKin.isDead();
    }
}
