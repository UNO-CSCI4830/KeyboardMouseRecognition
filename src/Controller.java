public class Controller {

    private PatternDetector patternDetector;
    private ReplayEngine replayEngine;
    private NotifPopup notifPopup;

    public Controller(PatternDetector patternDetector, ReplayEngine replayEngine, NotifPopup notifPopup) {
        this.patternDetector = patternDetector;
        this.replayEngine = replayEngine;
        this.notifPopup = notifPopup;
    }
    // This should be called by InputTracker and pass the Action Object created from user input. 
    public void handleAction(Action action) {
        // Take in the action object made from the user input, compare with SequenceStore,
        // And then return the matched Sequence. 
        Sequence match = patternDetector.processAction(action);
        // Don't do anything if there wasn't a match. 
        if (match == null) {
            return;
        }
        // Make the Notification for approval show up, and return true or false to whether user wants automation
        boolean approved = notifPopup.show(match);
        /** If true, ReplayEngine will replay the remaining actions in the matched sequence. 
        * .getMatchLength() or an equivalent method in PatternDetector will get the length of the prefix of the sequence.
        * Which is then used to determine what section of the sequence is the remainder and should be executed.
        * See Method on Line 69 of Sequence. 
        * This means PatternDetector should have a buffer (current list) of actions,
        * The buffer should also be cleared on approval (but not on dismissal)
        */
        if (approved) {
            replayEngine.replay(match.getRemainingActions(patternDetector.getMatchLength()));
            patternDetector.clearBuffer();
        }
    }
}
