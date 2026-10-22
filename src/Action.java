// One keyboard or mouse action done by the user.
// value examples: "CTRL" for a key, "LEFT@500,300" for a click, "500,300" for a move
public class Action {

    private ActionType type;
    private String value;
    private long timestamp; // not really used yet, might need it in sprint 2

    public Action(ActionType type, String value) {
        this.type = type;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public Action(ActionType type, String value, long timestamp) {
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public ActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // two actions are the same if type and value match
    // we ignore timestamp so doing the steps slower or faster still counts
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
