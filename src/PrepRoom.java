import bagel.Image;
import bagel.Input;
import bagel.Keys;
import bagel.util.Point;

import java.util.Map;
import java.util.Properties;

/**
 * Room where the game starts and character selection happens
 */
public class PrepRoom {
    private Player player;
    private Door door;
    private RestartArea restartArea;
    private boolean stopCurrentUpdateCall = false;
    private Image robotSprite;
    private Image marineSprite;
    private Point robotPosition;
    private Point marinePosition;

    public void initEntities(Properties gameProperties) {
        // Find configuration for this room
        for (Map.Entry<Object, Object> entry: gameProperties.entrySet()) {
            String roomSuffix = String.format(".%s", ShadowDungeon.PREP_ROOM_NAME);
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

        // Load character sprites for display
        robotSprite = new Image("res/robot_sprite.png");
        marineSprite = new Image("res/marine_sprite.png");
        robotPosition = IOUtils.parseCoords(gameProperties.getProperty("Robot"));
        marinePosition = IOUtils.parseCoords(gameProperties.getProperty("Marine"));
    }

    public void update(Input input) {
        UserInterface.drawStartMessages();

        // Draw character sprites
        robotSprite.draw(robotPosition.x, robotPosition.y);
        marineSprite.draw(marinePosition.x, marinePosition.y);

        // Update and draw all game objects in this room
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

        // Character selection and door unlock mechanism
        if (input.wasPressed(Keys.R)) {
            if (player != null) {
                player.selectCharacter(CharacterType.ROBOT);
                if (!findDoor().isUnlocked()) {
                    findDoor().unlock(false);
                }
            }
        }

        if (input.wasPressed(Keys.M)) {
            if (player != null) {
                player.selectCharacter(CharacterType.MARINE);
                if (!findDoor().isUnlocked()) {
                    findDoor().unlock(false);
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
}