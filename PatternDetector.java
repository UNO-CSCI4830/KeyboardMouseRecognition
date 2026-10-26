import java.util.ArrayList;
import java.util.List;

public class PatternDetector {

    private final SequenceStore sequenceStore;
    private final List<Action> buffer = new ArrayList<>();
    private int matchLength;

    public PatternDetector(SequenceStore sequenceStore) {
        if (sequenceStore == null) {
            throw new IllegalArgumentException("Sequence store cannot be null");
        }
        this.sequenceStore = sequenceStore;
    }

    //Adds an action and checks if the buffer actions start a saved sequence.
    public Sequence processAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        buffer.add(action);
        Sequence match = findBestMatch();

        //Removes the oldest action until the buffer can start a saved sequence.
        while (match == null && !buffer.isEmpty()) {
            buffer.remove(0);
            match = findBestMatch();
        }

        matchLength = (match == null) ? 0 : buffer.size();
        return match;
    }
    //Gets the number of match actions so the remaining actions can replay.
    public int getMatchLength() {
        return matchLength;
    }
    //Clears the collected actions for the current match
    public void clearBuffer() {
        buffer.clear();
        matchLength = 0;
    }
    //Returns the longest saved sequence that starts with the buffer actions.
    private Sequence findBestMatch() {
        Sequence match = null;
        for (Sequence sequence : sequenceStore.getSequences()) {
            if (sequence.startsWith(buffer)
                    && (match == null || sequence.size() > match.size())) {
                match = sequence;
            }
        }
        return match;
    }
}
