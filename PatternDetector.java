import java.util.List;

public class PatternDetector {
    //selects longest saved sequence & matches it with recent actions
    public static Sequence detectPattern(List<Sequence> storedSequences,
     List<Action> recentActions) {
        Sequence match = null;

        for (Sequence sequence : storedSequences) {
            if (sequence.startsWith(recentActions)) {
                if (match == null || sequence.size() > match.size()) {
                    match = sequence;
                }
            }
        }
        return match;
 }
}