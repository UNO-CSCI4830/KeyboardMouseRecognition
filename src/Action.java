// One keyboard or mouse action done by the user.
// value examples: "CTRL" for a key, "LEFT@500,300" for a click
public class Action {

    public static final int NO_KEY_CODE = -1;

    private final ActionType type;
    private final String value;
    private final int keyCode;
    private final long timestamp;
    private final int delayMs;       // time since the previous action
    private final String windowInfo; // title of the active window

    public Action(ActionType type, String value) {
        this(type, value, NO_KEY_CODE, System.currentTimeMillis(), 0, "");
    }

    public Action(ActionType type, String value, long timestamp) {
        this(type, value, NO_KEY_CODE, timestamp, 0, "");
    }

    public Action(ActionType type, String value, int keyCode, long timestamp) {
        this(type, value, keyCode, timestamp, 0, "");
    }

    // used by SequenceStore when loading from the database
    public Action(ActionType type, String value, int keyCode, int delayMs, String windowInfo) {
        this(type, value, keyCode, 0, delayMs, windowInfo);
    }

    public Action(ActionType type, String value, int keyCode, long timestamp, int delayMs, String windowInfo) {
        if (type == null) {
            throw new IllegalArgumentException("Action type can't be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Action value can't be null");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("Delay can't be negative: " + delayMs);
        }
        this.type = type;
        this.value = value;
        // SQLite returns 0 for a NULL KeyCode, and no real key code is 0 or less
        this.keyCode = (keyCode > 0) ? keyCode : NO_KEY_CODE;
        this.timestamp = timestamp;
        this.delayMs = delayMs;
        this.windowInfo = (windowInfo == null) ? "" : windowInfo;
    }

    public Action withDelayMs(int newDelayMs) {
        return new Action(type, value, keyCode, timestamp, newDelayMs, windowInfo);
    }

    public ActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean hasKeyCode() {
        return keyCode != NO_KEY_CODE;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public String getWindowInfo() {
        return windowInfo;
    }

    // Only type and value count, so timing doesn't matter. Window is left out
    // because titles change often (e.g. "Book1 - Excel" vs "Book2 - Excel").
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
