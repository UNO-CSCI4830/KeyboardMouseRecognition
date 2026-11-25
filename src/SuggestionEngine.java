import java.util.List;

/*
Chooses the sequence that is the strongest candidate for replay.
 */
public class SuggestionEngine {

    private final SequenceStore sequenceStore;

    public SuggestionEngine() {
        this(null);
    }

    public SuggestionEngine(SequenceStore sequenceStore) {
        this.sequenceStore = sequenceStore;
    }

    /*
     Selects the best sequence match.
     
     Prefer the longest match. If lengths are equal, use feedback and history
     to decide which one to suggest.
     */
    public Sequence chooseBestMatch(List<Sequence> matches) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }

        Sequence best = null;
        SequenceStats bestStats = null;

        for (Sequence candidate : matches) {
            if (candidate == null) {
                continue;
            }

            SequenceStats candidateStats = getStats(candidate);
            if (best == null || isBetter(candidate, candidateStats, best, bestStats)) {
                best = candidate;
                bestStats = candidateStats;
            }
        }

        return best;
    }

    private boolean isBetter(Sequence candidate, SequenceStats candidateStats,
                             Sequence current, SequenceStats currentStats) {
        if (candidate.size() != current.size()) {
            return candidate.size() > current.size();
        }

        double candidateApprovalRate = approvalRate(candidateStats);
        double currentApprovalRate = approvalRate(currentStats);
        if (candidateApprovalRate != currentApprovalRate) {
            return candidateApprovalRate > currentApprovalRate;
        }

        int candidateNetApprovals =
                candidateStats.getApprovalCount() - candidateStats.getDismissalCount();
        int currentNetApprovals =
                currentStats.getApprovalCount() - currentStats.getDismissalCount();
        if (candidateNetApprovals != currentNetApprovals) {
            return candidateNetApprovals > currentNetApprovals;
        }

        if (candidateStats.getDetectionCount() != currentStats.getDetectionCount()) {
            return candidateStats.getDetectionCount() > currentStats.getDetectionCount();
        }

        if (candidateStats.getDismissalCount() != currentStats.getDismissalCount()) {
            return candidateStats.getDismissalCount() < currentStats.getDismissalCount();
        }

        return candidate.getId() >= 0
                && (current.getId() < 0 || candidate.getId() < current.getId());
    }

    private double approvalRate(SequenceStats stats) {
        int decisions = stats.getApprovalCount() + stats.getDismissalCount();
        return decisions == 0 ? 0.0 : (double) stats.getApprovalCount() / decisions;
    }

    private SequenceStats getStats(Sequence sequence) {
        if (sequenceStore != null) {
            SequenceStats stored = sequenceStore.getSequenceStats(sequence.getId());
            if (stored != null) {
                return stored;
            }
        }

        return new SequenceStats(sequence.getId());
    }
}
