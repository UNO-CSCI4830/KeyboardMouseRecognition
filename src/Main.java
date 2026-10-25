public class Main {

    public static void main(String[] args) {

        // Core data/storage components
        SequenceStore sequenceStore = new SequenceStore();

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

        //Demo Sequences for Sprint 1 to show off matching stored sequences. 
        Sequence sequence_1 = new Sequence();

        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "CTRL"));
        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "C"));
        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "C"));
        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "CTRL"));
        // Will have to fine tune click coords for demo showcase
        sequence_1.addAction(new Action(ActionType.MOUSE_CLICK, "LEFT@500,300"));
        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "CTRL"));
        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "V"));
        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "V"));
        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "CTRL"));

        Sequence sequence_2 = new Sequence();

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "5"));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "5"));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "SHIFT"));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "="));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "="));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "SHIFT"));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "6"));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "6"));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "="));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "="));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "1"));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "1"));
        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "1"));
        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "1"));
        sequence_2.addAction(new Action(ActionType.MOUSE_SCROLL, "-3"));

        sequenceStore.save(sequence_1);
        sequenceStore.save(sequence_2);
        inputTracker.start();
    }
}
