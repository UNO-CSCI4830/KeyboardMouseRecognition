import java.sql.Timestamp;

public class SequenceStats {

    private final int sequenceId;
    private final int detectionCount;
    private final int approvalCount;
    private final int dismissalCount;
    private final double replaySpeed;
    private final Timestamp lastSuggested;
    private final Timestamp lastApproved;

    public SequenceStats(
            int sequenceId,
            int detectionCount,
            int approvalCount,
            int dismissalCount,
            double replaySpeed,
            Timestamp lastSuggested,
            Timestamp lastApproved) {

        this.sequenceId = sequenceId;
        this.detectionCount = detectionCount;
        this.approvalCount = approvalCount;
        this.dismissalCount = dismissalCount;
        this.replaySpeed = replaySpeed;
        this.lastSuggested = lastSuggested;
        this.lastApproved = lastApproved;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    public int getApprovalCount() {
        return approvalCount;
    }

    public int getDismissalCount() {
        return dismissalCount;
    }

    public double getReplaySpeed() {
        return replaySpeed;
    }

    public Timestamp getLastSuggested() {
        return lastSuggested;
    }

    public Timestamp getLastApproved() {
        return lastApproved;
    }
}