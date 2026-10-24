public class Main {

    public static void main(String[] args) {

        // Core data/storage components
        SequenceStore sequenceStore = new SequenceStore();

        // Business logic
        PatternDetector patternDetector = new PatternDetector(sequenceStore);

        ReplayEngine replayEngine = new ReplayEngine();

        // User interface
        NotifPopup notifPopup = new NotifPopup();

        // Application coordinator
        Controller controller = new Controller(patternDetector, replayEngine, notifPopup);

        // Input capture
        InputTracker inputTracker = new InputTracker(controller);

        System.out.println("Automation Assistant Started");

        inputTracker.start();
    }
}
