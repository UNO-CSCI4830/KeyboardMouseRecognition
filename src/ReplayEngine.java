import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
                robot.keyPress(keyCode(action.getValue()));
                break;

            case KEY_RELEASE:
                robot.keyRelease(keyCode(action.getValue()));
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

    private int keyCode(String value) {

        switch (value.toUpperCase()) {

            case "CTRL":
                return KeyEvent.VK_CONTROL;

            case "SHIFT":
                return KeyEvent.VK_SHIFT;

            case "A":
                return KeyEvent.VK_A;

            case "B":
                return KeyEvent.VK_B;

            case "C":
                return KeyEvent.VK_C;

            case "V":
                return KeyEvent.VK_V;

            case "1":
                return KeyEvent.VK_1;

            case "5":
                return KeyEvent.VK_5;

            case "6":
                return KeyEvent.VK_6;

            case "=":
                return KeyEvent.VK_EQUALS;

            default:
                throw new IllegalArgumentException("Unsupported key: " + value);
        }
    }

    private void clickMouse(String button) {

        switch (button.toUpperCase()) {

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
                throw new IllegalArgumentException("Unsupported mouse button: " + button);
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
