import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

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

// Captures keyboard and mouse input and turns it into Action objects.
// Mouse movement isn't recorded, only where the mouse was when it clicked.
public class InputTracker implements NativeKeyListener, NativeMouseListener, NativeMouseWheelListener {

    // so a long pause in the middle of a task isn't replayed
    public static final int MAX_DELAY_MS = 3000;
    public static final int MAX_RECORDED = 1000;

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    private Consumer<Action> listener;
    private volatile boolean recording = true;
    private volatile boolean paused = false;
    private List<Action> recorded = new ArrayList<>();
    private Set<Integer> keysHeld = new HashSet<>();
    private long lastActionTime = -1;

    private static final Map<Integer, Integer> KEY_CODES = buildKeyCodeMap();

    // JNativeHook gives right-side modifiers their own codes with no name or Robot code
    // (right Shift showed up as "UNKNOWN KEYCODE: 0XE36"), so treat them as the left ones
    private static final Map<Integer, Integer> RIGHT_TO_LEFT = Map.of(
            0x0E36, NativeKeyEvent.VC_SHIFT,
            0x0E1D, NativeKeyEvent.VC_CONTROL,
            0x0E38, NativeKeyEvent.VC_ALT,
            0x0E5C, NativeKeyEvent.VC_META);

    public void setListener(Consumer<Action> listener) {
        this.listener = listener;
    }

    public void start() {
        // JNativeHook logs a lot, turn it off
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

    private void capture(ActionType type, String value, int keyCode) {
        if (!recording || paused) {
            return;
        }
        long now = System.currentTimeMillis();
        int delayMs = 0;
        if (lastActionTime >= 0) {
            delayMs = (int) Math.max(0, Math.min(now - lastActionTime, MAX_DELAY_MS));
        }
        lastActionTime = now;
        record(new Action(type, value, keyCode, now, delayMs, getActiveWindowTitle()));
    }

    public void record(Action action) {
        if (!recording || paused) {
            return;
        }
        recorded.add(action);
        if (recorded.size() > MAX_RECORDED) {
            recorded.remove(0);
        }
        if (listener != null) {
            listener.accept(action);
        }
    }

    // ---------- keyboard ----------

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int code = e.getKeyCode();
        if (keysHeld.contains(code)) {
            return; // key is being held down
        }
        keysHeld.add(code);
        capture(ActionType.KEY_PRESS, keyName(code), toRobotKeyCode(code));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        int code = e.getKeyCode();
        keysHeld.remove(code);
        capture(ActionType.KEY_RELEASE, keyName(code), toRobotKeyCode(code));
    }

    private static int toLeftSide(int nativeCode) {
        return RIGHT_TO_LEFT.getOrDefault(nativeCode, nativeCode);
    }

    private String keyName(int code) {
        return NativeKeyEvent.getKeyText(toLeftSide(code)).toUpperCase();
    }

    public static int toRobotKeyCode(int nativeCode) {
        Integer robotCode = KEY_CODES.get(toLeftSide(nativeCode));
        if (robotCode == null) {
            return Action.NO_KEY_CODE;
        }
        return robotCode;
    }

    // JNativeHook and Robot number keys differently, so match them by name:
    // NativeKeyEvent.VC_ENTER -> KeyEvent.VK_ENTER
    private static Map<Integer, Integer> buildKeyCodeMap() {
        Map<Integer, Integer> map = new HashMap<>();

        for (char c = 'A'; c <= 'Z'; c++) {
            addKey(map, "" + c, "" + c);
        }
        for (char c = '0'; c <= '9'; c++) {
            addKey(map, "" + c, "" + c);
        }
        for (int i = 1; i <= 12; i++) {
            addKey(map, "F" + i, "F" + i);
        }

        // {JNativeHook name, Robot name}
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

    private static void addKey(Map<Integer, Integer> map, String nativeName, String robotName) {
        try {
            int nativeCode = NativeKeyEvent.class.getField("VC_" + nativeName).getInt(null);
            int robotCode = KeyEvent.class.getField("VK_" + robotName).getInt(null);
            map.put(nativeCode, robotCode);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // one of the libraries doesn't have this key
        }
    }

    // ---------- mouse ----------

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        // In JNativeHook BUTTON2 is right and BUTTON3 is middle
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
        capture(ActionType.MOUSE_CLICK, button + "@" + e.getX() + "," + e.getY(), Action.NO_KEY_CODE);
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        capture(ActionType.MOUSE_SCROLL, String.valueOf(e.getWheelRotation()), Action.NO_KEY_CODE);
    }

    // ---------- active window ----------

    // Windows only for now, "" everywhere else or if it fails
    public static String getActiveWindowTitle() {
        if (!IS_WINDOWS) {
            return "";
        }
        try {
            return WindowsWindow.activeTitle();
        } catch (Throwable t) {
            return "";
        }
    }

    // separate class so the JNA Windows code only loads on Windows
    private static class WindowsWindow {
        static String activeTitle() {
            HWND window = User32.INSTANCE.GetForegroundWindow();
            if (window == null) {
                return "";
            }
            char[] buffer = new char[512];
            User32.INSTANCE.GetWindowText(window, buffer, buffer.length);
            return Native.toString(buffer);
        }
    }

    // ---------- recording controls ----------

    public void setRecording(boolean recording) {
        this.recording = recording;
        lastActionTime = -1; // time spent off isn't a delay
    }

    public boolean isRecording() {
        return recording;
    }

    // true when no keys are held down, so PatternDetector only suggests
    // automations from a clean starting state
    public boolean isKeyboardIdle() {
        return keysHeld.isEmpty();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        lastActionTime = -1; // time spent paused isn't a delay
    }

    public List<Action> getRecordedActions() {
        return Collections.unmodifiableList(recorded);
    }

    public Sequence makeSequence() {
        Sequence seq = new Sequence(recorded);
        recorded = new ArrayList<>();
        lastActionTime = -1;
        return seq;
    }

    // quick test: run this, type and click around for 10 seconds
    public static void main(String[] args) throws InterruptedException {
        InputTracker tracker = new InputTracker();
        tracker.setListener(action -> System.out.println(action
                + " delay=" + action.getDelayMs() + "ms"
                + " window=\"" + action.getWindowInfo() + "\""));
        tracker.start();
        System.out.println("Tracking for 10 seconds, type and click around...");
        Thread.sleep(10000);
        tracker.stop();
        System.out.println("Done. Recorded " + tracker.getRecordedActions().size() + " actions.");
        System.exit(0);
    }
}
