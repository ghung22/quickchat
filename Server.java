import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    ServerSocket ss;
    Socket s;
    String ip = "localhost";
    Integer port = 7044;
    GUI gui = new GUI();

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
            String msg = dis.readUTF();
            System.out.println("> " + msg);
        } catch (Exception e) {
            gui.sendError("Error while listening to client: " + e.getMessage());
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
