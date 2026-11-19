// Temporary import to provide key codes that usually would come from InputTracker to showcase demo sequences.

public class Main {

    public static void main(String[] args) {

        // Initialize Swing UI components to not look terrible
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Core data/storage component
        SequenceStore sequenceStore = new SequenceStore("automation.db");

        // User interface
        NotifPopup notifPopup = new NotifPopup();

        // Input capture
        InputTracker inputTracker = new InputTracker();

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
