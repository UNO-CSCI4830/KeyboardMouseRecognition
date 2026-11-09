import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Controller {

    private InputTracker inputTracker;
    private PatternDetector patternDetector;
    private ReplayEngine replayEngine;
    private NotifPopup notifPopup;

    public Controller(PatternDetector patternDetector, ReplayEngine replayEngine,
                      NotifPopup notifPopup, InputTracker inputTracker) {
        this.patternDetector = patternDetector;
        this.replayEngine = replayEngine;
        this.notifPopup = notifPopup;
        this.inputTracker = inputTracker;
    }

    // This should be called by InputTracker and pass the Action Object created from user input.
    public void handleAction(Action action) {

        // Take in the action object made from the user input, compare with SequenceStore,
        // And then return the matched Sequence. Don't do anything if there wasn't a match.

        Sequence match = patternDetector.processAction(action);
        if (match == null) {
            return;
        }

        inputTracker.setPaused(true);

        // Make the Notification for approval show up, and return true or false to whether user wants automation
        try {
            boolean approved = notifPopup.show(match);

            /** If true, ReplayEngine will replay the remaining actions in the matched sequence.
             * .getMatchLength() or an equivalent method in PatternDetector will get the length of the prefix of the sequence.
             * Which is then used to determine what section of the sequence is the remainder and should be executed.
             * See Method on Line 69 of Sequence.
             * This means PatternDetector should have a buffer (current list) of actions,
             * The buffer should also be cleared on approval (but not on dismissal)
             * inputTracker.setPaused(true) makes it so user inputs are not recorded during replay.
             * Setting it to false resumes recording user inputs.
             */
            if (approved) {
                replayWithStopButton(
                        match.getRemainingActions(patternDetector.getMatchLength()));
                patternDetector.clearBuffer();
            }
        } finally {
            inputTracker.setPaused(false);
        }
    }

    private void replayWithStopButton(List<Action> actions) {
        JDialog[] stopDialog = new JDialog[1];

        try {
            SwingUtilities.invokeAndWait(() -> {
                JButton stopButton = new JButton("Stop");

                stopButton.addActionListener(e -> {
                    replayEngine.stop();
                    stopButton.setEnabled(false);
                });

                JDialog dialog = new JDialog();
                dialog.setTitle("Replay");
                dialog.setModal(false);
                dialog.setAlwaysOnTop(true);
                dialog.setFocusableWindowState(false);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.add(stopButton);
                dialog.pack();
                dialog.setLocationRelativeTo(null);

                stopDialog[0] = dialog;
                dialog.setVisible(true);
            });

            replayEngine.replay(actions);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while opening the Stop window", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Couldn't open the Stop window", e.getCause());
        } finally {
            SwingUtilities.invokeLater(() -> {
                if (stopDialog[0] != null) {
                    stopDialog[0].dispose();
                }
            });
        }
    }
}