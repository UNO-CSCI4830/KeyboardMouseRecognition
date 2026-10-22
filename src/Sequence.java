import java.util.ArrayList;
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
        this.actions = new ArrayList<>(actions);
        this.windowInfo = windowInfo;
        id = -1;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public List<Action> getActions() {
        return actions;
    }

    public int size() {
        return actions.size();
    }

    // checks if the user's recent actions match the start of this sequence
    // PatternDetector uses this
    public boolean startsWith(List<Action> prefix) {
        // need at least one action, and there has to be something left to replay
        if (prefix.size() == 0 || prefix.size() >= actions.size()) {
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

    public void addTimesSeen() {
        timesSeen++;
    }

    public void addApproval() {
        approvals++;
    }

    public void addDismissal() {
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
        this.windowInfo = windowInfo;
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
