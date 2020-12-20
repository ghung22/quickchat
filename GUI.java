import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class GUI {
    JFrame frame;

    Boolean noGUI = false;
    String timestamp;

    Integer paddingDefault = 20, marginDefault = 12;

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

            // Put elements into frame
            frame.add(newTitle(), BorderLayout.NORTH);
            frame.add(newFiller(), BorderLayout.WEST);
            frame.add(newLoginForm(), BorderLayout.CENTER);
            frame.add(newFiller(), BorderLayout.EAST);
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

    private JPanel newTitle() {
        JLabel title = new JLabel("Quickchat Messenger");
        title.setFont(title.getFont().deriveFont(56.0f));
        JPanel titlePanel = new JPanel();
        titlePanel.add(title);
        return titlePanel;
    }

    private JPanel newLoginForm() {
        // Reusables
        Dimension d = new Dimension(256, 32);
        CompoundBorder cb = BorderFactory.createCompoundBorder(new LineBorder(Color.DARK_GRAY, 2),
                new EmptyBorder(0, 8, 0, 8));

        // Username
        JTextField userField = new JTextField();
        userField.setPreferredSize(d);
        userField.setBorder(cb);
        JPanel userPanel = new JPanel();
        userPanel.add(newFiller());
        userPanel.add(userField);
        userPanel.add(newFiller());
        userPanel.setBorder(BorderFactory.createTitledBorder("Username:"));

        // Password
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(d);
        passwordField.setBorder(cb);
        JPanel passwordPanel = new JPanel();
        passwordPanel.add(newFiller());
        passwordPanel.add(passwordField);
        passwordPanel.add(newFiller());
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Password:"));

        // Buttons

        // Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.add(newFiller());
        formPanel.add(userPanel);
        formPanel.add(newFiller());
        formPanel.add(passwordPanel);
        formPanel.add(newFiller());
        // TODO: buttons
        formPanel.add(newFiller());

        return formPanel;
    }

    private Box.Filler newFiller() {
        return new Box.Filler(new Dimension(0, 0), new Dimension(128, 64), new Dimension(Short.MAX_VALUE, 640));
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
