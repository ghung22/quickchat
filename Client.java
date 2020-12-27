import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

@SuppressWarnings("serial")
public class Client extends GUI {
    Socket s;

    DataInputStream dis;
    DataOutputStream dos;

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
            dos.writeUTF("103»username»" + user);
            dos.writeUTF("103»password»" + pass);

            // Start Main Menu
            mainStart();
            while (connectStarted) {
                listen();
            }
        } catch (Exception e) {
            sendAlert("Error while connecting to server: " + e.getMessage());
            connectStarted = false;
        }
    }

    public void speak() {
        try {
            dos.writeUTF("356»Hello world!");
            dos.flush();
            dos.close();
        } catch (Exception e) {
            sendAlert("Error while sending message: " + e.getMessage());
        }
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
            if (connectStarted) {
                sendAlert("Error while listening to server: " + e.getMessage());
            }
        }
    }

    private void respond(Integer msgCode, String msg) {
        String[] submsg = msg.split("»", 2);
        switch (msgCode) {
            case 356:
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
                        updateChatbox(submsg[1]);
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
        sendLoading("Ending connection");
        try {
            dos.writeUTF("427»Close client");
            s.close();
            sendNotice("Connection ended");
        } catch (Exception e) {
            sendAlert("Error while ending connection: " + e.getMessage());
        }
    }
}
