import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Runnable;
import java.net.Socket;

@SuppressWarnings("serial")
public class Client extends GUI {
    Socket s;

    Client() {
        super("client");
    }

    public void connect() {
        // Conect to specified server (if logged in successfully)
        if (!connectStarted) {
            return;
        }
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

    public void sendInfoToServer() throws Exception {
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
                // Hightlisht admin message
                if (submsg[0] == "admin") {
                    submsg[0] = "<span style='font-color: red;'>admin</span>";
                }
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
