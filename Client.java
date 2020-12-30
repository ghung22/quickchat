import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.Runnable;
import java.net.Socket;
import java.util.Scanner;

@SuppressWarnings("serial")
public class Client extends GUI {
    Socket s;

    Integer failsafeFlag = 5; // End client after 5 failed attemps

    Client() {
        super("client");
    }

    public void connect() {
        // Conect to specified server (if logged in successfully)
        if (registerStarted) {
            register();
            registerStarted = false;
        }
        if (!authStarted) {
            return;
        }
        authStarted = false;
        if (!authenticate()) {
            sendAlert("Error while authenticating client: Wrong username or password.");
            if (--failsafeFlag <= 0) {
                sendAlert("Critical: Too many failed attempts, closing client.");
                dispose();
            }
            return;
        }
        connectStarted = true;
        String ip = server;
        Integer p = Integer.parseInt(port);
        sendLoading("Connecting server at " + ip + ":" + p);
        try {
            s = new Socket(ip, p);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            sendNotice("Connection made successfully");
            sendInfoToServer();

            // Start Main Menu
            mainStart();
            msgStr = "<b>" + user + "</b> entered the chat. Please be civilized.";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (!connectStarted) {
                            return;
                        }
                        listen();
                    }
                }
            }, "Listen").start();
        } catch (Exception e) {
            sendAlert("Error while connecting to server: " + e.getMessage());
            endConnection();
        }
    }

    private void register() {
        if (!authenticate(true)) {
            File file = new File("Data/user-accounts.csv");
            FileWriter fw = null;
            try {
                if (file.createNewFile()) {
                    fw = new FileWriter(file);
                    fw.write("Username,Password,Profile\n");
                    if (fw != null) {
                        fw.close();
                    }
                }
                fw = new FileWriter(file, true);
                fw.write(user + "," + pass + "," + "Data/default-profile.png" + "\n");
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception e) {
                sendAlert("Error while registering client: " + e.getMessage());
            }
        } else {
            sendAlert("Error while registering client: Username exists.");
        }
    }

    private Boolean authenticate(Boolean checkUserOnly) {
        Boolean foundUser = false, correctPassword = false;
        File file = new File("Data/user-accounts.csv");
        if (!file.exists()) {
            sendAlert("Error while authenticating client: Database not availible.");
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner s = new Scanner(fis, "UTF-8");
            if (s.hasNextLine()) {
                // Skip columns name
                s.nextLine();
            }
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] sec = line.split(",", 3);
                if (sec[0].equals(user)) {
                    foundUser = true;
                    if (sec[1].equals(pass)) {
                        correctPassword = true;
                    } else {
                        break;
                    }
                    break;
                }
            }
            if (fis != null) {
                fis.close();
            }
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
            sendAlert("Error while authenticating client: " + e.getMessage());
        }
        return (foundUser && correctPassword) || (checkUserOnly && foundUser);
    }

    private Boolean authenticate() {
        return authenticate(false);
    }

    private void sendInfoToServer() throws Exception {
        dos.writeUTF("103»username»" + user);
        dos.writeUTF("103»password»" + pass);
    }

    public void speak() {
        if (msgStr != "") {
            try {
                dos.writeUTF("356»" + msgStr);
                msgStr = "";
                dos.flush();
                dos.close();
            } catch (Exception e) {
                sendAlert("Error while sending message: " + e.getMessage());
            }
        }
    }

    public void listen() {
        try {
            String[] msg = dis.readUTF().split("»", 2);
            try {
                respond(Integer.parseInt(msg[0]), msg[1]);
            } catch (Exception e) {
                sendAlert("Error while resolving message: " + e.getMessage() + "\n" + "Received message: " + msg);
            }
        } catch (Exception e) {
            if (connectStarted) {
                sendAlert("Error while listening to server: " + e.getMessage());
                endConnection();
            }
        }
    }

    private void respond(Integer msgCode, String msg) {
        String[] submsg = msg.split("»", 2);
        String[] subsubmsg;
        switch (msgCode) {
            case 356:
                updateChatbox(submsg[0], submsg[1]);
                break;

            case 103:
                break;

            case 427:
                switch (submsg[0]) {
                    case "Close server":
                        sendNotice("Server closed");
                        close();
                        break;

                    case "Close client":
                        sendNotice("Received close request.");
                        close();
                        break;

                    case "Update chatbox":
                        subsubmsg = submsg[1].split("»", 2);
                        updateChatbox(subsubmsg[0], subsubmsg[1]);
                        break;

                    default:
                        sendAlert("Unresolvable message: code " + msgCode + " » " + msg);
                        break;
                }
                break;

            default:
                sendAlert("Unresolvable message: code " + msgCode + " » " + msg);
                break;
        }
    }

    public void endConnection() {
        connectStarted = false;
        try {
            dos.writeUTF("427»Close client");
            s.close();
            sendNotice("Connection ended");
        } catch (Exception e) {
        }
        loginStart();
    }

    public void close() {
        sendLoading("Ending connection");
        try {
            dos.writeUTF("427»Close client");
            s.close();
            sendNotice("Connection ended");
            dispose();
        } catch (Exception e) {
            sendAlert("Error while ending connection: " + e.getMessage());
        }
    }
}
