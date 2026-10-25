import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        record(new Action(ActionType.KEY_PRESS, keyName(code)));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        int code = e.getKeyCode();
        keysHeld.remove(code);
        record(new Action(ActionType.KEY_RELEASE, keyName(code)));
    }

    // turns a key code into a name like "CTRL" or "C"
    private String keyName(int code) {
        return NativeKeyEvent.getKeyText(code).toUpperCase();
    }

    // ---------- mouse ----------

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
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

    // everything recorded so far
    public List<Action> getRecordedActions() {
        return recorded;
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
        tracker.setListener(action -> System.out.println("Recorded: " + action));
        tracker.start();
        System.out.println("Tracking for 10 seconds, type and click around...");
        Thread.sleep(10000);
        tracker.stop();
        System.out.println("Done. Recorded " + tracker.getRecordedActions().size() + " actions.");
        System.exit(0);
    }
}
