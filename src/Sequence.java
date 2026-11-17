import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A list of actions in order that makes up a workflow
public class Sequence {

    private int id; // -1 until SequenceStore saves it
    private List<Action> actions;
    private int timesSeen;
    private int approvals;
    private int dismissals;

    public Sequence() {
        actions = new ArrayList<>();
        id = -1;
    }

    public Sequence(List<Action> actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions list can't be null");
        }
        this.actions = new ArrayList<>();
        id = -1;
        for (Action action : actions) {
            addAction(action);
        }
    }

    public void addAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Can't add a null action");
        }
        // the first action shouldn't wait on something from before the sequence
        if (actions.isEmpty() && action.getDelayMs() != 0) {
            action = action.withDelayMs(0);
        }
        actions.add(action);
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public int size() {
        return actions.size();
    }

    // true if the user's recent actions match the start of this sequence
    public boolean startsWith(List<Action> prefix) {
        if (prefix == null || prefix.size() == 0 || prefix.size() >= actions.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!actions.get(i).equals(prefix.get(i))) {
                return false;
            }
        }
        return true;
    }

    public List<Action> getRemainingActions(int start) {
        List<Action> remaining = new ArrayList<>();
        for (int i = start; i < actions.size(); i++) {
            remaining.add(actions.get(i));
        }
        return remaining;
    }

    public long getTotalDelayMs() {
        long total = 0;
        for (Action action : actions) {
            total += action.getDelayMs();
        }
        return total;
    }

    public String getStartWindow() {
        if (actions.isEmpty()) {
            return "";
        }
        return actions.get(0).getWindowInfo();
    }

    public void incrementTimesSeen() {
        timesSeen++;
    }

    public void incrementApprovals() {
        approvals++;
    }

    public void incrementDismissals() {
        dismissals++;
    }

    public int getTimesSeen() {
        return timesSeen;
    }

    public int getApprovals() {
        return approvals;
    }

    public int getDismissals() {
        return dismissals;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Sequence " + id + " (" + getStartWindow() + "): " + actions;
    }
}
