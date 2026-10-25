import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SequenceStore {

    private List<Sequence> sequences = new ArrayList<>();
    public void save(Sequence sequence) {
        if (sequence == null) {
            throw new IllegalArgumentException("Can't save a null sequence");
        }
        sequences.add(sequence);
    }

    public List<Sequence> getSequences() {
        return Collections.unmodifiableList(sequences);
    }
    
}
