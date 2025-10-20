import bagel.*;
import bagel.util.Point;

import java.util.Properties;

/**
 * Main game class that manages initialising the rooms and moving the player between rooms
 */
public class ShadowDungeon extends AbstractGame {
    public static Properties gameProps;
    public static Properties messageProps;
    public static double screenWidth;
    public static double screenHeight;

    private static String currRoomName;
    private static PrepRoom prepRoom;
    private static BattleRoom battleRoomA;
    private static BattleRoom battleRoomB;
    private static EndRoom endRoom;
    private static Player player;
    private final Image background;
    
    public static final String PREP_ROOM_NAME = "prep";
    public static final String BATTLE_ROOM_A_NAME = "A";
    public static final String BATTLE_ROOM_B_NAME = "B";
    public static final String END_ROOM_NAME = "end";

    public ShadowDungeon(Properties gameProps, Properties messageProps) {
        super(Integer.parseInt(gameProps.getProperty("window.width")),
                Integer.parseInt(gameProps.getProperty("window.height")),
                "Shadow Dungeon");

        ShadowDungeon.gameProps = gameProps;
        ShadowDungeon.messageProps = messageProps;
        screenWidth = Integer.parseInt(gameProps.getProperty("window.width"));
        screenHeight = Integer.parseInt(gameProps.getProperty("window.height"));
        this.background = new Image("res/background.png");

        resetGameState(gameProps);
    }

    public static void resetGameState(Properties gameProps) {
        prepRoom = new PrepRoom();
        battleRoomA = new BattleRoom(BATTLE_ROOM_A_NAME, BATTLE_ROOM_B_NAME);
        battleRoomB = new BattleRoom(BATTLE_ROOM_B_NAME, END_ROOM_NAME);
        endRoom = new EndRoom();

        prepRoom.initEntities(gameProps);
        battleRoomA.initEntities(gameProps);
        battleRoomB.initEntities(gameProps);
        endRoom.initEntities(gameProps);

        currRoomName = PREP_ROOM_NAME;

        ShadowDungeon.player = new Player(IOUtils.parseCoords(gameProps.getProperty("player.start")));
        prepRoom.setPlayer(player);
    }

    /**
     * Render the relevant screen based on the keyboard input given by the user and the status of the gameplay.
     * @param input The current mouse/keyboard input.
     */
    @Override
    protected void update(Input input) {
        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        }

        background.draw((double) Window.getWidth() / 2, (double) Window.getHeight() / 2);

        switch (currRoomName) {
            case PREP_ROOM_NAME:
                prepRoom.update(input);
                return;
            case BATTLE_ROOM_A_NAME:
                battleRoomA.update(input);
                return;
            case BATTLE_ROOM_B_NAME:
                battleRoomB.update(input);
                return;
            default:
                endRoom.update(input);
        }
    }

    public static void changeRoom(String roomName) {
        Door nextDoor;
        switch (roomName) {
            case PREP_ROOM_NAME:
                nextDoor = prepRoom.findDoorByDestination();

                // assume that prep room can only be entered through Battle Room A
                if (currRoomName.equals(BATTLE_ROOM_A_NAME)) {
                    battleRoomA.stopCurrentUpdateCall();
                }
                currRoomName = PREP_ROOM_NAME;

                // move the player to the center of the next room's door
                nextDoor.unlock(true);
                player.move(nextDoor.getPosition().x, nextDoor.getPosition().y);
                prepRoom.setPlayer(player);

                return;
            case BATTLE_ROOM_A_NAME:
                nextDoor = battleRoomA.findDoorByDestination(currRoomName);

                // assume that Battle Room A can only be entered through Prep Room or Battle Room B
                if (currRoomName.equals(BATTLE_ROOM_B_NAME)) {
                    battleRoomB.stopCurrentUpdateCall();
                } else if (currRoomName.equals(PREP_ROOM_NAME)) {
                    prepRoom.stopCurrentUpdateCall();
                }
                currRoomName = BATTLE_ROOM_A_NAME;

                // prepare the door to be able to activate the Battle Room
                if (!battleRoomA.isComplete()) {
                    nextDoor.setShouldLockAgain();
                }

                // move the player to the center of the next room's door
                nextDoor.unlock(true);
                player.move(nextDoor.getPosition().x, nextDoor.getPosition().y);
                battleRoomA.setPlayer(player);

                return;
            case BATTLE_ROOM_B_NAME:
                nextDoor = battleRoomB.findDoorByDestination(currRoomName);

                // assume that Battle Room B can only be entered through Battle Room A or End Room
                if (currRoomName.equals(BATTLE_ROOM_A_NAME)) {
                    battleRoomA.stopCurrentUpdateCall();
                } else if (currRoomName.equals(END_ROOM_NAME)) {
                    endRoom.stopCurrentUpdateCall();
                }
                currRoomName = BATTLE_ROOM_B_NAME;

                // prepare the door to be able to activate the Battle Room
                if (!battleRoomB.isComplete()) {
                    nextDoor.setShouldLockAgain();
                }

                // move the player to the center of the next room's door
                nextDoor.unlock(true);
                player.move(nextDoor.getPosition().x, nextDoor.getPosition().y);
                battleRoomB.setPlayer(player);

                return;
            default:
                nextDoor = endRoom.findDoorByDestination();

                // assume that end room can only be entered through Battle Room B
                if (currRoomName.equals(BATTLE_ROOM_B_NAME)) {
                    battleRoomB.stopCurrentUpdateCall();
                }
                currRoomName = END_ROOM_NAME;

                // move the player to the center of the next room's door
                nextDoor.unlock(true);
                player.move(nextDoor.getPosition().x, nextDoor.getPosition().y);
                endRoom.setPlayer(player);
        }
    }

    public static void changeToGameOverRoom() {
        switch (currRoomName) {
            case PREP_ROOM_NAME:
                prepRoom.stopCurrentUpdateCall();
            case BATTLE_ROOM_A_NAME:
                battleRoomA.stopCurrentUpdateCall();
            case BATTLE_ROOM_B_NAME:
                battleRoomB.stopCurrentUpdateCall();
            default:
        }

        endRoom.isGameOver();
        currRoomName = END_ROOM_NAME;

        Point startPos = IOUtils.parseCoords(ShadowDungeon.getGameProps().getProperty("player.start"));
        player.move(startPos.x, startPos.y);
        endRoom.setPlayer(player);
    }


    public static Properties getGameProps() {
        return gameProps;
    }
    public static Properties getMessageProps() {
        return messageProps;
    }

    public static void main(String[] args) {
        Properties gameProps = IOUtils.readPropertiesFile("res/app.properties");
        Properties messageProps = IOUtils.readPropertiesFile("res/message.properties");
        ShadowDungeon game = new ShadowDungeon(gameProps, messageProps);
        game.run();
    }
}
