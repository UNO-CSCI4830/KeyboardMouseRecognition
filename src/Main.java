

public class Main {

    public static void main(String[] args) {

        // Initialize Swing UI components to not look terrible
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Core data/storage component
        SequenceStore sequenceStore = new SequenceStore("automation.db");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> sequenceStore.close()));

        // Input capture
        InputTracker inputTracker = new InputTracker();

        // User interface
        NotifPopup notifPopup = new NotifPopup(inputTracker);

        // Business logic
        PatternDetector patternDetector = new PatternDetector(sequenceStore, inputTracker);
        ReplayEngine replayEngine = new ReplayEngine();

        // Application coordinator
        Controller controller = new Controller(patternDetector, replayEngine, notifPopup, inputTracker);

        inputTracker.setListener(controller::handleAction);
        System.out.println("Automation Assistant Started");
        inputTracker.start();
    }
}
