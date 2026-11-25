import java.util.ArrayList;
import java.util.List;

public class PatternDetector {
    private static final int MIN_MATCH_LENGTH = 3;

    private final SequenceStore sequenceStore;
    private final InputTracker inputTracker;
    private final List<Action> buffer = new ArrayList<>();
    private int matchLength;

    public PatternDetector(SequenceStore sequenceStore, InputTracker inputTracker) {
        if (sequenceStore == null) {
            throw new IllegalArgumentException("Sequence store cannot be null");
        }
        if (inputTracker == null) {
            throw new IllegalArgumentException("Input tracker cannot be null");
        }

        this.sequenceStore = sequenceStore;
        this.inputTracker = inputTracker;
    }

    public List<Sequence> processActionMatches(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        // Keeps the action in the buffer even when a held key blocks suggestions.

        buffer.add(action);

        if (!inputTracker.isKeyboardIdle()) {
            matchLength = 0;
            return new ArrayList<>();
        }

        List<Sequence> matches = findMatches();

        // Remove the oldest action until the buffer can start a saved sequence.

        while (matches.isEmpty() && !buffer.isEmpty()) {
            buffer.remove(0);
            matches = findMatches();
        }

        if (matches.isEmpty() || buffer.size() < MIN_MATCH_LENGTH) {
            matchLength = 0;
            return new ArrayList<>();
        }

        matchLength = buffer.size();
        return matches;
    }

    public int getMatchLength() {
        return matchLength;
    }

    public void clearBuffer() {
        buffer.clear();
        matchLength = 0;
    }

    private List<Sequence> findMatches() {
        List<Sequence> matches = new ArrayList<>();

        for (Sequence sequence : sequenceStore.getSequences()) {
            if (sequence.startsWith(buffer)) {
                matches.add(sequence);
            }
        }

        return matches;
    }
}