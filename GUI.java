import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI extends JFrame implements ActionListener {
    Boolean noGUI = false;
    String timestamp;
    Integer screenID = 0;
    ArrayList<JTextField> input = new ArrayList<JTextField>();

    // Integer paddingDefault = 20, marginDefault = 12;

    GUI() {
        this(false);
    }

    GUI(Boolean noGUI) {
        this.noGUI = noGUI;
        if (!noGUI) {
            sendLoading("Building GUI");

            // Create frame
            setTitle("Quickchat");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Show Login menu
            loginStart();

            // Show frame
            pack();
            setSize(800, 600);
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    GUI(String title) {
        this(false);
        setTitle(title);
    }

    private void cleanup() {
        input.clear();
    }

    private void loginStart() {
        cleanup();

        // Put elements into frame
        add(newTitle(), BorderLayout.NORTH);
        add(newFiller(), BorderLayout.WEST);
        add(newLoginForm(), BorderLayout.CENTER);
        add(newFiller(), BorderLayout.EAST);
        screenID = 76;
    }

    private JPanel newTitle() {
        JLabel title = new JLabel("Quickchat Messenger");
        title.setFont(title.getFont().deriveFont(56.0f));
        JPanel titlePanel = new JPanel();
        titlePanel.add(title);
        return titlePanel;
    }

    private JPanel newLoginForm() {// Create panel
        JPanel formPanel = new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                // Put elements into panel
                add(newFiller());
                add(newInput("Username"));
                add(newFiller());
                add(newInput("Password", true));
                add(newFiller());
                add(newButton("Login"));
                add(newFiller());
            }
        };

        return formPanel;
    }

    private JPanel newInput(String str, Boolean hideText) {
        // Cerate field
        JPasswordField field = new JPasswordField() {
            {
                if (!hideText) {
                    setEchoChar((char) 0);
                }
                setPreferredSize(new Dimension(256, 32));
                setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.DARK_GRAY, 2),
                        new EmptyBorder(0, 8, 0, 8)));
            }
        };

        // Add field to input array
        input.add(field);

        // Add field to titled panel
        JPanel panel = new JPanel() {
            {
                add(newFiller());
                add(field);
                add(newFiller());
                setBorder(BorderFactory.createTitledBorder(str + ":"));
            }
        };
        return panel;
    }

    private JPanel newInput(String str) {
        return newInput(str, false);
    }

    private JPanel newButton(String str) {
        // Create button
        JButton button = new JButton(str) {
            {
                setSize(128, 96);
                setMaximumSize(getSize());
            }
        };
        button.addActionListener(this);

        // Add button to panel
        JPanel panel = new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                add(newFiller());
                add(button);
                add(newFiller());
            }
        };
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<String> strs = new ArrayList<String>();
        for (JTextField i : input) {
            strs.add(i.getText());
        }
        switch (screenID) {
            case 76:
                sendNotice(strs.get(0) + ": " + strs.get(1));
                break;

            default:
                sendError("Unidentifiable screen");
                break;
        }
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
