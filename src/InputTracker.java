import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

// Captures the user's keyboard and mouse input and turns it into Action objects.
// Uses JNativeHook so it works even when our program isn't the active window.
//
// Mouse movement isn't recorded for sprint 1 since it would make a huge
// number of actions. We just save where the mouse was when it clicked.
public class InputTracker implements NativeKeyListener, NativeMouseListener, NativeMouseWheelListener {

    private Consumer<Action> listener;   // who gets each new action (Main hooks this up)
    private boolean recording = true;    // FR2: recording is on by default
    private boolean paused = false;      // set to true while ReplayEngine is running
    private List<Action> recorded = new ArrayList<>();
    private Set<Integer> keysHeld = new HashSet<>(); // so holding a key down doesn't spam presses

    // JNativeHook key codes -> java.awt.event.KeyEvent codes (what Robot uses to replay keys)
    private static final Map<Integer, Integer> KEY_CODES = buildKeyCodeMap();

    public void setListener(Consumer<Action> listener) {
        this.listener = listener;
    }

    // starts listening to the keyboard and mouse
    public void start() {
        // JNativeHook prints a lot of log messages, turn them off
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.out.println("Couldn't start input tracking: " + e.getMessage());
            return;
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseWheelListener(this);
    }

    // stops listening
    public void stop() {
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseListener(this);
        GlobalScreen.removeNativeMouseWheelListener(this);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.out.println("Couldn't stop input tracking: " + e.getMessage());
        }
    }

    // every captured input goes through here
    public void record(Action action) {
        if (!recording || paused) {
            return;
        }
        recorded.add(action);
        if (listener != null) {
            listener.accept(action);
        }
    }

    // ---------- keyboard ----------

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int code = e.getKeyCode();
        if (keysHeld.contains(code)) {
            return; // key is being held down, already recorded it
        }
        keysHeld.add(code);
        record(new Action(ActionType.KEY_PRESS, keyName(code), toRobotKeyCode(code), System.currentTimeMillis()));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        int code = e.getKeyCode();
        keysHeld.remove(code);
        record(new Action(ActionType.KEY_RELEASE, keyName(code), toRobotKeyCode(code), System.currentTimeMillis()));
    }

    // turns a key code into a name like "CTRL" or "C"
    private String keyName(int code) {
        return NativeKeyEvent.getKeyText(code).toUpperCase();
    }

    // turns a JNativeHook key code into the code Robot uses, or -1 if we don't know it
    public static int toRobotKeyCode(int nativeCode) {
        Integer robotCode = KEY_CODES.get(nativeCode);
        if (robotCode == null) {
            return Action.NO_KEY_CODE;
        }
        return robotCode;
    }

    // JNativeHook and Robot use different numbers for the same key (for example ENTER),
    // so we match them up by name: NativeKeyEvent.VC_ENTER -> KeyEvent.VK_ENTER.
    // Looking them up by name means a key that doesn't exist just gets skipped.
    private static Map<Integer, Integer> buildKeyCodeMap() {
        Map<Integer, Integer> map = new HashMap<>();

        // letters, numbers, and F1-F12 have the same names in both
        for (char c = 'A'; c <= 'Z'; c++) {
            addKey(map, "" + c, "" + c);
        }
        for (char c = '0'; c <= '9'; c++) {
            addKey(map, "" + c, "" + c);
        }
        for (int i = 1; i <= 12; i++) {
            addKey(map, "F" + i, "F" + i);
        }

        // other keys: {JNativeHook name, Robot name}
        String[][] names = {
            {"ESCAPE", "ESCAPE"}, {"BACKSPACE", "BACK_SPACE"}, {"TAB", "TAB"},
            {"ENTER", "ENTER"}, {"SPACE", "SPACE"}, {"CAPS_LOCK", "CAPS_LOCK"},
            {"SHIFT", "SHIFT"}, {"CONTROL", "CONTROL"}, {"ALT", "ALT"},
            {"META", "WINDOWS"}, {"CONTEXT_MENU", "CONTEXT_MENU"},
            {"BACKQUOTE", "BACK_QUOTE"}, {"MINUS", "MINUS"}, {"EQUALS", "EQUALS"},
            {"OPEN_BRACKET", "OPEN_BRACKET"}, {"CLOSE_BRACKET", "CLOSE_BRACKET"},
            {"BACK_SLASH", "BACK_SLASH"}, {"SEMICOLON", "SEMICOLON"}, {"QUOTE", "QUOTE"},
            {"COMMA", "COMMA"}, {"PERIOD", "PERIOD"}, {"SLASH", "SLASH"},
            {"INSERT", "INSERT"}, {"DELETE", "DELETE"}, {"HOME", "HOME"}, {"END", "END"},
            {"PAGE_UP", "PAGE_UP"}, {"PAGE_DOWN", "PAGE_DOWN"},
            {"UP", "UP"}, {"DOWN", "DOWN"}, {"LEFT", "LEFT"}, {"RIGHT", "RIGHT"},
            {"PRINTSCREEN", "PRINTSCREEN"}, {"SCROLL_LOCK", "SCROLL_LOCK"},
            {"PAUSE", "PAUSE"}, {"NUM_LOCK", "NUM_LOCK"}
        };
        for (String[] pair : names) {
            addKey(map, pair[0], pair[1]);
        }
        return map;
    }

    // adds NativeKeyEvent.VC_<nativeName> -> KeyEvent.VK_<robotName> if both exist
    private static void addKey(Map<Integer, Integer> map, String nativeName, String robotName) {
        try {
            int nativeCode = NativeKeyEvent.class.getField("VC_" + nativeName).getInt(null);
            int robotCode = KeyEvent.class.getField("VK_" + robotName).getInt(null);
            map.put(nativeCode, robotCode);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // one of the libraries doesn't have this key, just skip it
        }
    }

    // ---------- mouse ----------

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        // NOTE: JNativeHook numbers buttons differently than java.awt.event.MouseEvent.
        // In JNativeHook BUTTON2 = right and BUTTON3 = middle (tested: right-click -> RIGHT).
        // ReplayEngine should map these strings to Robot's masks:
        //   LEFT -> InputEvent.BUTTON1_DOWN_MASK, MIDDLE -> BUTTON2_DOWN_MASK, RIGHT -> BUTTON3_DOWN_MASK
        String button;
        if (e.getButton() == NativeMouseEvent.BUTTON1) {
            button = "LEFT";
        } else if (e.getButton() == NativeMouseEvent.BUTTON2) {
            button = "RIGHT";
        } else if (e.getButton() == NativeMouseEvent.BUTTON3) {
            button = "MIDDLE";
        } else {
            button = "BUTTON" + e.getButton();
        }
        record(new Action(ActionType.MOUSE_CLICK, button + "@" + e.getX() + "," + e.getY()));
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        record(new Action(ActionType.MOUSE_SCROLL, String.valueOf(e.getWheelRotation())));
    }

    // ---------- recording controls ----------

    // FR2: user can turn recording on or off
    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isRecording() {
        return recording;
    }

    // Main should pause this during a replay so we don't record our own key presses
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // everything recorded so far (read-only so other classes can't change it)
    public List<Action> getRecordedActions() {
        return Collections.unmodifiableList(recorded);
    }

    // turns what was recorded into a Sequence and starts fresh
    public Sequence makeSequence(String windowInfo) {
        Sequence seq = new Sequence(recorded, windowInfo);
        recorded = new ArrayList<>();
        return seq;
    }

    // quick test: run this, type and click around for 10 seconds
    public static void main(String[] args) throws InterruptedException {
        InputTracker tracker = new InputTracker();
        tracker.setListener(action -> {
            if (action.hasKeyCode()) {
                System.out.println("Recorded: " + action + " keyCode=" + action.getKeyCode());
            } else {
                System.out.println("Recorded: " + action);
            }
        });
        tracker.start();
        System.out.println("Tracking for 10 seconds, type and click around...");
        Thread.sleep(10000);
        tracker.stop();
        System.out.println("Done. Recorded " + tracker.getRecordedActions().size() + " actions.");
        System.exit(0);
    }
}
