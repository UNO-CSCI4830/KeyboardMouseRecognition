// Temporary import to provide key codes that usually would come from InputTracker to showcase demo sequences.

public class Main {

    public static void main(String[] args) {

        // Initialize Swing UI components to not look terrible
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Core data/storage components
        SequenceStore sequenceStore = new SequenceStore("automation.db");

        // Business logic
        PatternDetector patternDetector = new PatternDetector(sequenceStore);

        ReplayEngine replayEngine = new ReplayEngine();

        // User interface
        NotifPopup notifPopup = new NotifPopup();

        // Input capture
        InputTracker inputTracker = new InputTracker();

        // Application coordinator
        Controller controller = new Controller(patternDetector, replayEngine, notifPopup, inputTracker);

        inputTracker.setListener(controller::handleAction);

        System.out.println("Automation Assistant Started");
        
        inputTracker.start();
    }
}
