// Temporary import to provide key codes that usually would come from InputTracker to showcase demo sequences.
import java.awt.event.KeyEvent;

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

        // Demo Sequences for Sprint 1 to show off matching stored sequences.
        Sequence sequence_1 = new Sequence();

        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "CTRL",KeyEvent.VK_CONTROL, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "C",KeyEvent.VK_C, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "C",KeyEvent.VK_C, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "CTRL",KeyEvent.VK_CONTROL, System.currentTimeMillis()));

        // Will have to fine tune click coords for demo showcase
        sequence_1.addAction(new Action(
                ActionType.MOUSE_CLICK,
                "LEFT@500,300"));

        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "CTRL",KeyEvent.VK_CONTROL, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_PRESS, "V",KeyEvent.VK_V, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "V",KeyEvent.VK_V, System.currentTimeMillis()));

        sequence_1.addAction(new Action(ActionType.KEY_RELEASE, "CTRL",KeyEvent.VK_CONTROL, System.currentTimeMillis()));

        Sequence sequence_2 = new Sequence();

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "5",KeyEvent.VK_5, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "5",KeyEvent.VK_5, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "SHIFT",KeyEvent.VK_SHIFT, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "=",KeyEvent.VK_EQUALS, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "=",KeyEvent.VK_EQUALS, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "SHIFT",KeyEvent.VK_SHIFT, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "6",KeyEvent.VK_6, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "6",KeyEvent.VK_6, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "=",KeyEvent.VK_EQUALS, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "=",KeyEvent.VK_EQUALS, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "1",KeyEvent.VK_1, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "1",KeyEvent.VK_1, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_PRESS, "1",KeyEvent.VK_1, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.KEY_RELEASE, "1",KeyEvent.VK_1, System.currentTimeMillis()));

        sequence_2.addAction(new Action(ActionType.MOUSE_SCROLL,"-3"));

        sequenceStore.save(sequence_1);
        sequenceStore.save(sequence_2);
        inputTracker.start();

        // Uncomment below to test replayEngine. 
        // replayEngine.replay(sequence_1.getActions());
    }
}
