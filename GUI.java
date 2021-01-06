import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener {
    final String defaultTitle = "Quickchat Messenger", defaultServer = "localhost", defaultPort = "7044",
            defaultUser = "ANONYMOUS";
    final Dimension defaultSize = new Dimension(800, 600);

    String mode, title = defaultTitle;
    Dimension winSize = defaultSize;
    Integer screenID = 0;
    ArrayList<JTextField> input = new ArrayList<JTextField>();
    JEditorPane chatbox;

    protected String server = defaultServer, port = defaultPort, user = defaultUser, pass, msgStr;
    protected Boolean connectStarted = false, authStarted = false, registerStarted = false;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    GUI(String mode) {
        this.mode = mode;
        sendLoading("Initializing GUI");

        // Create frame
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Show menu (Login for clients, skip to Main Menu for server admin)
        if (mode.equals("client")) {
            loginStart();
        } else if (mode.equals("server")) {
            connectStarted = true;
            user = "ADMIN";
            mainStart();
        }

        // Show frame
        pack();
        setSize(winSize);
        setMinimumSize(winSize);
        setLocationRelativeTo(null);
        setVisible(true);
        sendNotice("GUI started for " + mode);

    }

    private void cleanup() {
        input.clear();
        getContentPane().removeAll();
    }

    protected void loginStart() {
        cleanup();
        GridBagConstraints cTitle = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                anchor = NORTH;
                weightx = weighty = 1.0;
            }
        }, cForm = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                anchor = CENTER;
                weightx = 1.0;
            }
        }, cFooter = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 2;
                anchor = SOUTHEAST;
                weightx = weighty = 1.0;
            }
        };
        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            {
                add(newTitle(), cTitle);
                add(newLoginForm(), cForm);
                add(newLoginFooter(), cFooter);
            }
        };
        add(loginPanel);
        screenID = 76;
        validate();
    }

    protected void mainStart() {
        cleanup();
        sendNotice("Logged in as " + user);
        GridBagConstraints cHeader = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                anchor = NORTH;
                weightx = weighty = 1.0;
            }
        }, cChatbox = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                anchor = NORTH;
                weightx = weighty = 1.0;
            }
        };
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            {
                setPreferredSize(winSize);
                add(newHeader(user), cHeader);
                add(newChatbox(), cChatbox);
            }
        };
        add(mainPanel);
        screenID = 33;
        validate();
    }

    private JPanel newTitle() {
        JLabel title = new JLabel(this.title) {
            {
                setFont(getFont().deriveFont(56.0f));
            }
        };
        return new JPanel() {
            {
                add(title);
            }
        };
    }

    private JPanel newLoginForm() {
        GridBagConstraints cUser = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                gridwidth = 2;
                insets = new Insets(32, 0, 16, 0);
                weightx = weighty = 1.0;
            }
        }, cPass = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                gridwidth = 2;
                insets = new Insets(16, 0, 32, 0);
                weightx = weighty = 1.0;
            }
        }, cBtnLogin = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 2;
                gridwidth = 1;
                weightx = weighty = 1.0;
            }
        }, cBtnRegister = new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 2;
                gridwidth = 1;
                weightx = weighty = 1.0;
            }
        };
        return new JPanel(new GridBagLayout()) {
            {
                add(newInput("Username"), cUser);
                add(newInput("Password", true), cPass);
                add(newButton("Login"), cBtnLogin);
                add(newButton("Register"), cBtnRegister);
            }
        };
    }

    private JPanel newLoginFooter() {
        return new JPanel() {
            {
                add(newButton("Connection Settings"));
                add(newButton("Forgot your password?"));
            }
        };
    }

    private JPanel newInput(String str, Boolean hideText, String text) {
        // Cerate field
        JPasswordField field = new JPasswordField(text) {
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
        GridBagConstraints c = new GridBagConstraints() {
            {
                insets = new Insets(16, 64, 16, 64);
                weightx = weighty = 1.0;
            }
        };
        return new JPanel(new GridBagLayout()) {
            {
                add(field, c);
                setBorder(BorderFactory.createTitledBorder(str + ":"));
            }
        };
    }

    private JPanel newInput(String str) {
        return newInput(str, false, "");
    }

    private JPanel newInput(String str, Boolean hideText) {
        return newInput(str, hideText, "");
    }

    private JPanel newInput(String str, String text) {
        return newInput(str, false, text);
    }

    private JPanel newButton(String str, Dimension size) {
        // Create button
        JButton button = new JButton(str) {
            {
                setSize(size);
                setMaximumSize(getSize());
            }
        };
        button.addActionListener(this);

        // Add button to panel
        return new JPanel() {
            {
                add(button);
            }
        };
    }

    private JPanel newButton(String str) {
        return newButton(str, new Dimension(128, 96));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<String> strs = getInputStrings();
        Object o = e.getSource();
        if (o instanceof JButton) {
            switch (screenID) {
                case 76:
                    switch (((JButton) o).getText()) {
                        case "Login":
                            user = strs.get(0);
                            pass = strs.get(1);
                            authStarted = true;
                            return;

                        case "Register":
                            JPanel dialogReg = newRegister();
                            if (JOptionPane.showConfirmDialog(this, dialogReg, "Register Account",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                                user = input.get(2).getText();
                                pass = input.get(3).getText();
                                registerStarted = true;
                            }
                            input.remove(3);
                            input.remove(2);
                            return;

                        case "Connection Settings":
                            JPanel dialogConSet = newConnectSettings();
                            if (JOptionPane.showConfirmDialog(this, dialogConSet, "Connection Settings",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                                server = input.get(2).getText();
                                port = input.get(3).getText();
                            }
                            input.remove(3);
                            input.remove(2);
                            return;

                        case "Forgot your password?":
                            return;

                        default:
                            sendAlert("Unidentifiable button");
                            break;
                    }
                    return;

                case 33:
                    switch (((JButton) o).getText()) {
                        case "Log out":
                            connectStarted = false;
                            user = defaultUser;
                            pass = null;
                            return;

                        case "Close server":
                            connectStarted = false;
                            return;

                        case "Send":
                            try {
                                dos.writeUTF("356»" + user + "»" + strs.get(0));
                            } catch (Exception ex) {
                                if (dos != null) {
                                    sendAlert("Error while sending text: " + ex.getMessage());
                                }
                            } finally {
                                if (user == "ADMIN") {
                                    updateChatbox(user, strs.get(0));
                                }
                                input.get(0).setText("");
                                input.get(0).grabFocus();
                            }
                            return;

                        default:
                            sendAlert("Unidentifiable button");
                            break;
                    }
                    return;

                default:
                    sendAlert("Unidentifiable screen");
                    break;
            }
        }
    }

    private ArrayList<String> getInputStrings() {
        ArrayList<String> strs = new ArrayList<String>();
        for (JTextField i : input) {
            String temp = i.getText();
            if (temp == null) {
                temp = " ";
            }
            strs.add(temp);
        }
        return strs;
    }

    private JPanel newRegister() {
        GridBagConstraints cUser = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                weightx = 1.0;
            }
        }, cPass = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                weightx = 1.0;
            }
        };
        return new JPanel() {
            {
                setPreferredSize(new Dimension(420, 200));
                add(newInput("Username"), cUser);
                add(newInput("Password", true), cPass);
            }
        };
    }

    private JPanel newConnectSettings() {
        GridBagConstraints cServer = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                weightx = 1.0;
            }
        }, cPort = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                weightx = 1.0;
            }
        };
        return new JPanel() {
            {
                setPreferredSize(new Dimension(420, 200));
                add(newInput("Server", server), cServer);
                add(newInput("Port", port), cPort);
            }
        };
    }

    private JPanel newHeader(String user) {
        GridBagConstraints cLabel = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                insets = new Insets(0, 8, 0, 8);
                anchor = WEST;
                weightx = weighty = 1;
            }
        }, cInfo = new GridBagConstraints() {
            {
                gridx = 2;
                gridy = 0;
                anchor = EAST;
                insets = new Insets(0, 8, 0, 8);
                weightx = weighty = 1;
            }
        };
        return new JPanel(new GridBagLayout()) {
            {
                setPreferredSize(new Dimension((int) winSize.getWidth(), 120));
                add(newLabel("Welcome, " + user, 28.0f), cLabel);
                add(newLoginInfo(user), cInfo);
            }
        };
    }

    private JPanel newLoginInfo(String user) {
        GridBagConstraints cImg = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                gridwidth = 1;
                gridheight = 2;
                insets = new Insets(8, 8, 8, 8);
            }
        }, cUser = new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 0;
                anchor = LINE_START;
                insets = new Insets(8, 0, 0, 8);
            }
        }, cOut = new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 1;
                anchor = LINE_START;
                insets = new Insets(8, 0, 0, 8);
            }
        };

        return new JPanel(new GridBagLayout()) {
            {
                add(newImage(new Dimension(64, 64)), cImg);
                add(newLabel(user), cUser);
                add(newButton((user != "ADMIN") ? "Log out" : "Close server", new Dimension(108, 32)), cOut);
                setBorder(BorderFactory.createTitledBorder("Logged in as..."));
            }
        };
    }

    private JPanel newImage(Dimension size) {
        // REF (Print image on GUI):
        // https://stackoverflow.com/questions/8333802/displaying-an-image-in-java-swing
        try {
            BufferedImage bi = ImageIO.read(new File("Data/default-profile.png"));
            JLabel img = new JLabel(new ImageIcon(resize(bi, 64, 64)));
            JPanel panel = new JPanel() {
                {
                    add(img);
                    setPreferredSize(size);
                }
            };
            return panel;
        } catch (Exception e) {
            sendAlert("Error while creating login infobox: " + e.getMessage());
            return new JPanel() {
                {
                    setPreferredSize(size);
                    setBackground(Color.red);
                }
            };
        }
    }

    // REF:
    // https://stackoverflow.com/questions/9417356/bufferedimage-resize
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private JPanel newLabel(String str, Float font_size) {
        JLabel label = new JLabel(str) {
            {
                setFont(getFont().deriveFont(font_size));
            }
        };
        return new JPanel() {
            {
                add(label);
            }
        };
    }

    private JPanel newLabel(String str) {
        return newLabel(str, 16.0f);
    }

    private JPanel newChatbox() {
        GridBagConstraints cText = new GridBagConstraints() {
            {
                gridx = 1;
                gridy = 0;
                gridwidth = 7;
                anchor = WEST;
                weightx = 1.0;
            }
        }, cButton = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                anchor = WEST;
            }
        }, cChatbox = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 1;
                gridwidth = 8;
                weightx = weighty = 1.0;
            }
        };
        JPanel text = newInput("Your message");
        text.setBorder(null);
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet sh = editorKit.getStyleSheet();
        sh.addRule("p {line-height: 100%; margin: 100%;}");
        chatbox = new JEditorPane() {
            {
                setEditorKit(editorKit);
                setEditable(false);
                setContentType("text/html");
                setText("<html><body id='body'></body></html>");
                setPreferredSize(new Dimension((int) winSize.getWidth() - 64, (int) winSize.getHeight() - 256));
                setMinimumSize(new Dimension((int) winSize.getWidth() - 64, (int) winSize.getHeight() - 256));
            }
        };
        return new JPanel(new GridBagLayout()) {
            {
                add(text, cText);
                add(newButton("Send"), cButton);
                add(chatbox, cChatbox);
            }
        };
    }

    public String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public void sendLoading(String msg) {
        System.out.println("(@) [" + getTimestamp() + " - " + user + "] " + msg + "...");
    }

    public void sendNotice(String msg) {
        System.out.println("(i) [" + getTimestamp() + " - " + user + "] " + msg + ".");
    }

    public void sendAlert(String msg) {
        System.out.println("(!) [" + getTimestamp() + " - " + user + "] " + msg + ".");
        if (isShowing()) {
            JOptionPane.showMessageDialog(this, "(!) [" + getTimestamp() + "] " + msg + ".", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void sendQuery(String msg) {
        System.out.println("(?) [" + getTimestamp() + "] " + msg + ".");
    }

    public void updateChatbox(String u, String msg) {
        HTMLDocument content = (HTMLDocument) chatbox.getDocument();
        Element ele = content.getElement("body");
        try {
            content.insertBeforeEnd(ele, "<p><span style='font-size: 75%'>[" + getTimestamp() + "]</span> <b>" + u
                    + "</b>: " + msg + "</p>");
        } catch (Exception e) {
            sendAlert("Error while updating Chatbox: " + e.getMessage());
        }
        chatbox.setDocument(content);
        System.out.println("(>) [" + getTimestamp() + " - " + user + "] " + "(" + u + "): " + msg);
    }
}
