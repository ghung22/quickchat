import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket ss;
    Socket s;
    String ip = "localhost";
    Integer port = 7044;
    GUI gui = new GUI("Quickchat Server");

    DataInputStream dis;

    Server() {
        gui.sendLoading("Starting server at " + ip + ":" + port);
        try {
            ss = new ServerSocket(port);
            s = ss.accept();
            dis = new DataInputStream(s.getInputStream());
            gui.sendNotice("Server started successfully");
        } catch (Exception e) {
            gui.sendError("Error while starting server: " + e.getMessage());
        }
    }

    public void listen() {
        try {
            String[] msg = dis.readUTF().split("»", 2);
            try {
                respond(Integer.parseInt(msg[0]), msg[1]);
            } catch (Exception e) {
                gui.sendError("Error while resolving message: " + e.getMessage());
            }
        } catch (Exception e) {
            gui.sendError("Error while listening to client: " + e.getMessage());
        }
    }

    private void respond(Integer msgCode, String msg) {
        switch (msgCode) {
            case 356: // Chat msg
                System.out.println("> " + msg);
                break;

            default:
                gui.sendError("Unresolvable message: code " + msgCode + " » " + msg);
                break;
        }
    }

    public void close() {
        gui.sendLoading("Closing server");
        try {
            ss.close();
            gui.sendNotice("Server closed");
        } catch (Exception e) {
            gui.sendError("Error while closing server: " + e.getMessage());
        }
    }
}
