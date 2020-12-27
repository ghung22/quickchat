import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class Server extends GUI {
    ServerSocket ss;
    Socket s;
    String ip = "localhost";
    Integer port = 7044;

    TreeMap<String, String> client_info = new TreeMap<String, String>();
    DataInputStream dis;
    DataOutputStream dos;

    public Server() {
        super("server");
    }

    public void launch() {
        // Start server
        sendNotice("Starting server at " + ip + ":" + port);
        try {
            ss = new ServerSocket(port);
        } catch (Exception e) {
            sendAlert("Error while starting server: " + e.getMessage());
        }

        // Accept connections (if GUI is still showing)
        sendNotice("Server started successfully");
        while (isShowing()) {
            try {
                s = ss.accept();
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
            } catch (Exception e) {
                sendAlert("Error while running server: " + e.getMessage());
            }

            // Connect and listen to a new client (If exist client info)
            client_info = new TreeMap<String, String>();
            sendNotice("Connected to a client");
            while (client_info != null) {
                if (!connectStarted) {
                    break;
                }
                listen();
            }
        }

        // End server
        close();
    }

    public void listen() {
        try {
            String[] msg = dis.readUTF().split("»", 2);
            try {
                respond(Integer.parseInt(msg[0]), msg[1]);
            } catch (Exception e) {
                sendAlert("Error while resolving message: " + e.getMessage());
            }
        } catch (Exception e) {
            if (client_info != null) {
                sendAlert("Error while listening to client: " + e.getMessage());
            }
        }
    }

    private void respond(Integer msgCode, String msg) {
        String[] submsg = msg.split("»", 2);
        switch (msgCode) {
            case 356:
                String str = "[" + getTimestamp() + "]\n";
                str += " (" + client_info.get("username") + ") > " + msg;
                System.out.println(str);
                updateChatbox(str);
                try {
                    dos.writeUTF("427»Update chatbox»" + str);
                } catch (Exception e) {
                    sendAlert("Error while send update request to client: " + e.getMessage());
                }
                break;

            case 103:
                if (!client_info.containsKey(submsg[0])) {
                    client_info.put(submsg[0], submsg[1]);
                } else {
                    client_info.replace(submsg[0], submsg[1]);
                }
                break;

            case 427:
                switch (submsg[0]) {
                    case "Close client":
                        client_info.clear();
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

    public void close() {
        // Send closing notice to clients
        sendLoading("Closing server");
        try {
            dos.writeUTF("427»Close server");
        } catch (Exception e) {
            sendAlert("Error while send close request to client: " + e.getMessage());
        }
        
        // Close server
        try {
            client_info.clear();
            ss.close();
            sendNotice("Server closed");
        } catch (Exception e) {
            sendAlert("Error while closing server: " + e.getMessage());
        }
    }
}
