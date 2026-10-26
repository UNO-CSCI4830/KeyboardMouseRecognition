import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.List;

public class ReplayEngine {

    private Robot robot;

    public ReplayEngine() {
        try {
            robot = new Robot();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void replay(List<Action> actions) {
        for (Action action : actions) {
            execute(action);
            robot.delay(30);
        }
    }

    // Switch statement determines Action Type, methods within cases translate to readable robot instructions
    private void execute(Action action) {
        switch(action.getType()) {

            case KEY_PRESS:
                if (!action.hasKeyCode()) {
                    throw new IllegalArgumentException("Missing key code for: " + action);
                }

                robot.keyPress(action.getKeyCode());
                break;

            case KEY_RELEASE:
                if (!action.hasKeyCode()) {
                    throw new IllegalArgumentException("Missing key code for: " + action);
                }

                robot.keyRelease(action.getKeyCode());
                break;

            case MOUSE_MOVE:
                moveMouse(action.getValue());
                break;

            case MOUSE_CLICK:
                clickMouse(action.getValue());
                break;

            case MOUSE_SCROLL:
                scrollMouse(action.getValue());
                break;

            default:
                throw new IllegalArgumentException("Unsupported action type: " + action.getType());
        }
    }


    private void clickMouse(String value) {

        String[] parts = value.split("@");

        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid click format: " + value);
        }

        String click_button = parts[0];

        // Move to recorded location first if coordinates exist.
        if (parts.length == 2) {
            moveMouse(parts[1]);
        }

        switch (click_button.toUpperCase()) {

            case "LEFT":
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                break;

            case "RIGHT":
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                break;

            case "MIDDLE":
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                break;

            default:
                throw new IllegalArgumentException("Unsupported mouse button: " + value);
        }
    }

    private void moveMouse(String position) {

        String[] coords = position.split(",");

        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);

        robot.mouseMove(x, y);
    }

    private void scrollMouse(String value) {

        int amount = Integer.parseInt(value);

        robot.mouseWheel(amount);
    }
}
