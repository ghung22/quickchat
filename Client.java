import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    Socket s;
    String ip = "localhost";
    Integer port = 7044;
    GUI gui = new GUI();

    DataOutputStream dos;

    Client() {
        gui.sendLoading("Connecting server at " + ip + ":" + port);
        try {
            s = new Socket(ip, port);
            dos = new DataOutputStream(s.getOutputStream());
            gui.sendNotice("Connection made successfully");
        } catch (Exception e) {
            gui.sendAlert("Error while connecting to server: " + e.getMessage());
        }
    }

    public void speak() {
        try {
            dos.writeUTF("356»Hello world!");
            dos.flush();
            dos.close();
        } catch (Exception e) {
            gui.sendAlert("Error while sending message: " + e.getMessage());
        }
    }
    
    public void close() {
        gui.sendLoading("Ending connection");
        try {
            s.close();
            gui.sendNotice("Connection ended");
        } catch (Exception e) {
            gui.sendAlert("Error while ending connection: " + e.getMessage());
        }
    }
}
