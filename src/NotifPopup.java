//I do not miss writing Java code...
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JDialog;
import javax.swing.Timer;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
public class NotifPopup {

    // Ignoring after 10(default) seconds == cancel.
    public static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final int MARGIN = 16; //play with this value to adjust screen safe are
    private volatile int timeoutMs  = DEFAULT_TIMEOUT_MS;; // changeable timeout setting

    public NotifPopup() {
        String LAF;
        if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            // GTK set explicitly on Linux [i love you archwiki]
            LAF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        } else {
            // native LAF on [everywhere else (win32)]
            LAF = UIManager.getSystemLookAndFeelClassName();
        }
        try {
            UIManager.setLookAndFeel(LAF);
        } catch (Exception ignored) {}
    } //UI styling

    // Console log ms
    private static void log(long start, String message) {
        System.out.println("[NotifPopup +" + (System.currentTimeMillis() - start) + "ms] " + message);
    }


    // Approval notification
    // Return T/F based on user choice
    public boolean show(Sequence match) {
        //initialize logging variables
        final long start = System.currentTimeMillis();
        final boolean[] timedOut = { false }; //if we hit timeoutMs
        log(start, "show() called, sequence has " + match.size() + " actions");

        Object[] choices = {"Automate!", "Nope!"};

        // One action per line; scrolls if sequence is longer than 10 actions.
        StringBuilder lines = new StringBuilder();
        for (Action action : match.getActions()) {
            lines.append(action).append('\n');
        }
        JTextArea actionList = new JTextArea(lines.toString(), Math.min(match.size(), 10), 30);
        actionList.setEditable(false);

        Object message = new Object[] {
                "Automate these " +match.size()+ " actions? \uD83E\uDD7A",
                new JScrollPane(actionList)
        };
        JOptionPane pane = new JOptionPane(
                message, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, choices, choices[0]);

        try {
            SwingUtilities.invokeAndWait(() -> {
                log(start, "on Swing thread, creating dialog");
                JDialog dialog = pane.createDialog("Automation Assistant");
                log(start, "dialog created");
                dialog.setAlwaysOnTop(true);
                dialog.setFocusableWindowState(false); // never steal keyboard focus

                // Bottom-right corner, above the taskbar.
                Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                dialog.setLocation(area.x + area.width - dialog.getWidth() - MARGIN,
                        area.y + area.height - dialog.getHeight() - MARGIN);

                Timer timeout = new Timer(timeoutMs, e -> {timedOut[0] = true; dialog.dispose();});
                timeout.setRepeats(false);
                timeout.start();
                log(start, "showing dialog (timeout " + timeoutMs + "ms)");
                dialog.setVisible(true); // modal: blocks until a button, the X, or the timeout
                timeout.stop();
                log(start, "dialog closed");
            });
        } catch (Exception e) {
            log(start, "error showing popup: " + e);
            e.printStackTrace();
        }
        //return code with logging
        Object value = pane.getValue();
        boolean approved = choices[0].equals(value);
        String outcome = approved ? "approved"
                : timedOut[0] ? "timed out (counted as dismissed)"
                : choices[1].equals(value) ? "dismissed"
                : "closed without choosing (counted as dismissed)";
        log(start, "result: " + outcome);
        return approved;
    }
}
