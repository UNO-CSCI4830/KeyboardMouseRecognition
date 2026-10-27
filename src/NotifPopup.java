//I do not miss writing Java code...
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
public class NotifPopup {

    // Ignoring after 10(default) seconds == cancel.
    public static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final int MARGIN = 16; //play with this value to adjust screen safe are
    private volatile int timeoutMs  = DEFAULT_TIMEOUT_MS;; // changeable timeout setting

    public NotifPopup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    } //UI styling

    // Approval notification
    // Return T/F based on user choice
    public boolean show(Sequence match) {
        final boolean[] approved = { false };   // array so the lambda can write to it
        try {
            SwingUtilities.invokeAndWait(() -> {
                int choice = JOptionPane.showConfirmDialog(
                        null,
                        "Repeat the rest of this workflow?\n" + match.getRemainingActions(0).size() + " actions",
                        "Automation Assistant",
                        JOptionPane.YES_NO_OPTION);
                approved[0] = (choice == JOptionPane.YES_OPTION);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return approved[0];
    }
}

