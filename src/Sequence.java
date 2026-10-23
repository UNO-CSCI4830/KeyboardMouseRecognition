import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A list of actions in order that makes up a workflow
public class Sequence {

    private int id; // for SQLite later, not used in sprint 1
    private List<Action> actions;
    private int timesSeen;
    private int approvals;
    private int dismissals;
    private String windowInfo;

    public Sequence() {
        actions = new ArrayList<>();
        windowInfo = "";
        id = -1;
    }

    public Sequence(List<Action> actions, String windowInfo) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions list can't be null");
        }
        this.actions = new ArrayList<>();
        for (Action action : actions) {
            if (action == null) {
                throw new IllegalArgumentException("Can't add a null action");
            }
            this.actions.add(action);
        }
        this.windowInfo = (windowInfo == null) ? "" : windowInfo;
        id = -1;
    }

    public void addAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Can't add a null action");
        }
        actions.add(action);
    }

    // read-only so other classes have to use addAction() to change the list
    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public int size() {
        return actions.size();
    }

    // checks if the user's recent actions match the start of this sequence
    // PatternDetector uses this
    public boolean startsWith(List<Action> prefix) {
        // need at least one action, and there has to be something left to replay
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

    // gets the actions left after the part the user already did
    // ReplayEngine uses this
    public List<Action> getRemainingActions(int start) {
        List<Action> remaining = new ArrayList<>();
        for (int i = start; i < actions.size(); i++) {
            remaining.add(actions.get(i));
        }
        return remaining;
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

    public String getWindowInfo() {
        return windowInfo;
    }

    public void setWindowInfo(String windowInfo) {
        this.windowInfo = (windowInfo == null) ? "" : windowInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Sequence " + id + " (" + windowInfo + "): " + actions;
    }
}
