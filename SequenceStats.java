public class SequenceStats {

    private int sequenceId;
    private int detectionCount;
    private int approvalCount;
    private int dismissalCount;
    private double replaySpeed = 1.5;
    private long lastSuggested;
    private long lastApproved;
    private int priority;

    public SequenceStats() {
        this(-1);
    }

    public SequenceStats(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    public int getTimesSeen() {
        return detectionCount;
    }

    public void setDetectionCount(int detectionCount) {
        if (detectionCount < 0) {
            throw new IllegalArgumentException("Detection count cannot be negative");
        }
        this.detectionCount = detectionCount;
    }

    public int getApprovalCount() {
        return approvalCount;
    }

    public int getTimesApproved() {
        return approvalCount;
    }

    public void setApprovalCount(int approvalCount) {
        if (approvalCount < 0) {
            throw new IllegalArgumentException("Approval count cannot be negative");
        }
        this.approvalCount = approvalCount;
    }

    public int getDismissalCount() {
        return dismissalCount;
    }

    public int getTimesDismissed() {
        return dismissalCount;
    }

    public void setDismissalCount(int dismissalCount) {
        if (dismissalCount < 0) {
            throw new IllegalArgumentException("Dismissal count cannot be negative");
        }
        this.dismissalCount = dismissalCount;
    }

    public double getReplaySpeed() {
        return replaySpeed;
    }

    public void setReplaySpeed(double replaySpeed) {
        if (replaySpeed <= 0) {
            throw new IllegalArgumentException("Replay speed must be positive");
        }
        this.replaySpeed = replaySpeed;
    }

    public long getLastSuggested() {
        return lastSuggested;
    }

    public void setLastSuggested(long lastSuggested) {
        this.lastSuggested = lastSuggested;
    }

    public long getLastApproved() {
        return lastApproved;
    }

    public void setLastApproved(long lastApproved) {
        this.lastApproved = lastApproved;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void recordDetection() {
        detectionCount++;
    }

    public void recordApproval() {
        approvalCount++;
        lastApproved = System.currentTimeMillis();
    }

    public void recordDismissal() {
        dismissalCount++;
    }

    public void recordSuggestion() {
        lastSuggested = System.currentTimeMillis();
    }
}
