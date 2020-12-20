import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;

public class GUI {
    JFrame frame;

    Boolean noGUI = false;
    String timestamp;

    GUI() {
        this(false);
    }

    GUI(Boolean noGUI) {
        this.noGUI = noGUI;
        if (!noGUI) {
            sendLoading("Building GUI");
            // Create frame
            frame = new JFrame("Quickchat");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Create title
            JLabel title = new JLabel("Quickchat Messenger");
            title.setFont(title.getFont().deriveFont(56.0f));
            JPanel titPanel = new JPanel();
            titPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
            titPanel.add(title);
            // Put elements into frame
            frame.add(titPanel, BorderLayout.NORTH);
            frame.pack();
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    GUI(String title) {
        this(false);
        frame.setTitle(title);
    }

    public void sendLoading(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(@) [" + timestamp + "] " + msg + "...");
    }

    public void sendNotice(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(i) [" + timestamp + "] " + msg + ".");
    }

    public void sendError(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(!) [" + timestamp + "] " + msg + ".");
    }

    public void sendQuery(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(?) [" + timestamp + "] " + msg + ".");
    }
}
