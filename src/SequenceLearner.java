// Decides which action lists are worth saving as sequences
public class SequenceLearner {

    private final SequenceStore sequenceStore;

    public SequenceLearner(SequenceStore sequenceStore) {
        if (sequenceStore == null) {
            throw new IllegalArgumentException("Sequence store cannot be null");
        }
        this.sequenceStore = sequenceStore;
    }
}