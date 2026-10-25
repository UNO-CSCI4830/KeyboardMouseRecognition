// One keyboard or mouse action done by the user.
// value examples: "CTRL" for a key, "LEFT@500,300" for a click, "500,300" for a move
public class Action {

    public static final int NO_KEY_CODE = -1;

    private ActionType type;
    private String value;
    private int keyCode;    // java.awt.event.KeyEvent code for Robot, or NO_KEY_CODE for mouse actions
    private long timestamp; // not really used yet, might need it in sprint 2

    public Action(ActionType type, String value) {
        this(type, value, NO_KEY_CODE, System.currentTimeMillis());
    }

    public Action(ActionType type, String value, long timestamp) {
        this(type, value, NO_KEY_CODE, timestamp);
    }

    // used by InputTracker for key presses/releases so ReplayEngine can do robot.keyPress(keyCode)
    public Action(ActionType type, String value, int keyCode, long timestamp) {
        // type and value can't be null since equals() and hashCode() use them
        if (type == null) {
            throw new IllegalArgumentException("Action type can't be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Action value can't be null");
        }
        this.type = type;
        this.value = value;
        this.keyCode = keyCode;
        this.timestamp = timestamp;
    }

    public ActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    // the key code Robot uses (java.awt.event.KeyEvent.VK_...), or NO_KEY_CODE
    public int getKeyCode() {
        return keyCode;
    }

    public boolean hasKeyCode() {
        return keyCode != NO_KEY_CODE;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // two actions are the same if type and value match
    // we ignore timestamp so doing the steps slower or faster still counts
    // (keyCode comes from the same key as value, so we don't need to compare it)
    // (type and value are never null because the constructor checks them)
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) {
            return false;
        }
        Action other = (Action) obj;
        return this.type == other.type && this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + value.hashCode();
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}
