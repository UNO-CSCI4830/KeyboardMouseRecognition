// Decides which action lists are worth saving as sequences
public class SequenceLearner {

    // PatternDetector needs 3 matched actions plus at least 1 left to replay
    private static final int MIN_SEQUENCE_LENGTH = 4;

    private final SequenceStore sequenceStore;

    public SequenceLearner(SequenceStore sequenceStore) {
        if (sequenceStore == null) {
            throw new IllegalArgumentException("Sequence store cannot be null");
        }
        this.sequenceStore = sequenceStore;
    }

public boolean isLongEnough(Sequence sequence) {    
    return sequence.size() >= MIN_SEQUENCE_LENGTH;
    }
}