import bagel.Input;
import bagel.Keys;

import java.util.Map;
import java.util.Properties;

/**
 * Room where the game ends when the player either completes all rooms or dies
 */
public class EndRoom {
    private Player player;
    private Door door;
    private RestartArea restartArea;
    private boolean isGameOver = false;
    private boolean stopCurrentUpdateCall = false; // this determines whether to prematurely stop the update execution

    public void initEntities(Properties gameProperties) {
        // find the configuration of game objects for this room
        for (Map.Entry<Object, Object> entry: gameProperties.entrySet()) {
            String roomSuffix = String.format(".%s", ShadowDungeon.END_ROOM_NAME);
            if (entry.getKey().toString().contains(roomSuffix)) {
                String objectType = entry.getKey().toString().substring(0, entry.getKey().toString().length() - roomSuffix.length());
                String propertyValue = entry.getValue().toString();

                switch (objectType) {
                    case "door":
                        String[] coordinates = propertyValue.split(",");
                        door = new Door(IOUtils.parseCoords(propertyValue), coordinates[2]);
                        break;
                    case "restartarea":
                        restartArea = new RestartArea(IOUtils.parseCoords(propertyValue));
                        break;
                    default:
                }
            }
        }
    }

    public void update(Input input) {
        UserInterface.drawEndMessage(!isGameOver);

        // door should be locked if player got to this room by dying
        if (isGameOver) {
            findDoor().lock();
        }

        // update and draw all game objects in this room
        door.update(player);
        door.draw();
        if (stopUpdatingEarlyIfNeeded()) {
            return;
        }

        restartArea.update(input, player);
        restartArea.draw();

        if (player != null) {
            player.update(input);
            player.draw();
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

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void stopCurrentUpdateCall() {
        stopCurrentUpdateCall = true;
    }

    public Door findDoor() {
        return door;
    }

    public Door findDoorByDestination() {
        return door;
    }

    public void isGameOver() {
        isGameOver = true;
    }
}
