import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Runnable;
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

        // Accept connections in a thread (if GUI is still showing)
        sendNotice("Server started successfully");
        updateChatbox(ip + ":" + port, "Server started successfully");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isShowing()) {
                    accept();
                }

                // End server after stopping accepting connections
                close();
            }
        }, "Accept").start();
    }

    public void accept() {
        try {
            s = ss.accept();
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // Connect and listen to a new client in a thread (If exist client info)
            new Thread(new Runnable(){
                @Override
                public void run() {
                    postConnectedStatus();
                }
            }, "Post Connected").start();;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isShowing()) {
                        listen();
                    }
                }
            }, "Listen").start();
        } catch (Exception e) {
            sendAlert("Error while running server: " + e.getMessage());
        }
    }

    public void postConnectedStatus() {
        client_info = new TreeMap<String, String>();
        sendNotice("Connected to a client");

        // Wait until username is received and send entering chat room message
        while (!client_info.containsKey("username")) {
            continue;
        }
        String str = client_info.get("username") + " entered the chat room.";
        updateChatbox(ip + ":" + port, str);
        try {
            dos.writeUTF("427»Update chatbox»" + ip + ":" + port + "»" + str);
        } catch (Exception e) {
            sendAlert("Error while send update request to client: " + e.getMessage());
            client_info.clear();
        }
    }

    public void listen() {
        try {
            String[] msg = dis.readUTF().split("»", 2);
            try {
                respond(Integer.parseInt(msg[0]), msg[1]);
            } catch (Exception e) {
                sendAlert("Error while resolving message: " + e.getMessage() + ". Received message: " + msg);
            }
        } catch (Exception e) {
            if (client_info != null) {
                sendAlert("Error while listening to client: " + e.getMessage());
                client_info.clear();
            }
        }
    }

    private void respond(Integer msgCode, String msg) {
        String[] submsg = msg.split("»", 2);
        String[] subsubmsg;
        switch (msgCode) {
            case 356:
                updateChatbox(submsg[0], submsg[1]);
                try {
                    dos.writeUTF("427»Update chatbox»" + submsg[0] + "»" + submsg[1]);
                } catch (Exception e) {
                    sendAlert("Error while send update request to client: " + e.getMessage());
                    client_info.clear();
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

    public void close() {
        // Send closing notice to clients
        sendLoading("Closing server");
        try {
            dos.writeUTF("427»Close server");
        } catch (Exception e) {
            sendAlert("Error while send close request to client: " + e.getMessage());
            client_info.clear();
        }

        // Close server
        try {
            client_info.clear();
            ss.close();
            sendNotice("Server closed");
            dispose();
        } catch (Exception e) {
            sendAlert("Error while closing server: " + e.getMessage());
        }
    }
}
