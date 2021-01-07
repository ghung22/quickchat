import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Runnable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class Server extends GUI {
    ServerSocket ss;
    String ip = "localhost";
    Integer port = 7044;

    Socket s;
    TreeMap<String, String> clientInfo = new TreeMap<String, String>();

    Connection con = null;

    // ArrayList<Socket> s = new ArrayList<Socket>();
    // ArrayList<TreeMap<String, String>> clientInfo = new ArrayList<TreeMap<String,
    // String>>();
    // TreeMap<String, Integer> clientList = new TreeMap<String, Integer>();

    public Server() {
        super("server");
    }

    public void launch() {
        // Start server
        sendNotice("Starting server at " + ip + ":" + port);
        try {
            ss = new ServerSocket(port);
            dbConnect();
        } catch (Exception e) {
            sendAlert("Error while starting server: " + e.getMessage());
        }

        // Check if connection up and window showing
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkConnection();
            }
        }, "Check connection").start();

        // Accept connections in a thread (if GUI is still showing)
        sendNotice("Server started successfully");
        updateChatbox(ip + ":" + port, "Server started successfully");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isShowing()) {
                    accept();
                }
            }
        }, "Accept").start();
    }

    private void dbConnect() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String conString = "jdbc:sqlserver://172.17.0.1:1433;databaseName=Quickchat;user=SA;password=";
        con = DriverManager.getConnection(conString);
        if (con != null) {
            sendNotice("Connected to MSSQL database");
        }
    }

    private void checkConnection() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (!connectStarted || !isShowing()) {
                break;
            }
        }
        close();
    }

    private void accept() {
        try {
            s = ss.accept();
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());

            // Connect and listen to a new client in a thread (If exist client info)           
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isShowing() && !s.isClosed()) {
                        listen();
                    }
                }
            }, "Listen").start();

            // Listen to login info
            new Thread(new Runnable() {
                @Override
                public void run() {
                    postConnectedStatus();
                }
            }, "Post Connected").start();
        } catch (Exception e) {
            if (!e.getMessage().contains("closed")) {
                sendAlert("Error while running server: " + e.getMessage());
            }
        }
    }

    private void postConnectedStatus() {
        clientInfo = new TreeMap<String, String>();
        sendNotice("Connected to a client");

        // Wait until username is received and send entering chat room message
        while (!clientInfo.containsKey("username")) {
            while (clientInfo.size() < 2) {
                continue;
            }
            if (clientInfo.containsKey("authUser")) {
                authenticate();
            } else if (clientInfo.containsKey("regUser")) {
                register();
            }
        }
        String str = clientInfo.get("username") + " entered the chat room.";
        updateChatbox(ip + ":" + port, str);
        try {
            dos.writeUTF("427»Update chatbox»" + ip + ":" + port + "»" + str);
        } catch (Exception e) {
            sendAlert("Error while send update request to client: " + e.getMessage());
            endConnection();
        }
    }

    private void authenticate() {
        try {
            String u = clientInfo.get("authUser"), p = clientInfo.get("authPass");
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("select * from UserData where UserName == " + u);
            if (rs.wasNull()) {
                sendAlert("Error while authenticating client: Incorrect username or password");
                clientInfo.clear();
                return;
            }
            if (rs.getString(1) != p) {
                sendAlert("Error while authenticating client: Incorrect username or password");
                clientInfo.clear();
                return;
            }
            clientInfo.clear();
            clientInfo.put("username", u);
            clientInfo.put("password", p);
        } catch (Exception e) {
            sendAlert("Error while authenticating client: " + e.getMessage());
        }
    }

    private void register() {
        if (user.equals("ADMIN") || user.equals("ANONYMOUS")) {
            sendAlert("Error while registering client: Username '" + user + "' is reserved");
            return;
        }
        try {
            String u = clientInfo.get("regUser"), p = clientInfo.get("regPass");
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("select * from UserData where UserName == " + u);
            if (!rs.wasNull()) {
                sendAlert("Error while registering client: Existing username");
                clientInfo.clear();
                return;
            }
            clientInfo.clear();
            clientInfo.put("username", u);
            clientInfo.put("password", p);
        } catch (Exception e) {
            sendAlert("Error while registering client: " + e.getMessage());
        }
    }

    private void listen() {
        try {
            String[] msg = dis.readUTF().split("»", 2);
            try {
                respond(Integer.parseInt(msg[0]), msg[1]);
            } catch (Exception e) {
                sendAlert("Error while resolving message: " + e.getMessage() + ". Received message: " + msg);
            }
        } catch (Exception e) {
            if (clientInfo != null) {
                sendAlert("Error while listening to client: " + e.getMessage());
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
                try {
                    dos.writeUTF("427»Update chatbox»" + submsg[0] + "»" + submsg[1]);
                } catch (Exception e) {
                    sendAlert("Error while send update request to client: " + e.getMessage());
                    endConnection();
                }
                break;

            case 706:
                if (!clientInfo.containsKey(submsg[0])) {
                    clientInfo.put(submsg[0], submsg[1]);
                } else {
                    clientInfo.replace(submsg[0], submsg[1]);
                }
                break;

            case 427:
                switch (submsg[0]) {
                    case "Close client":
                        endConnection();
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
        clientInfo.clear();
        try {
            dos.writeUTF("427»Close client");
            if (s != null && !s.isClosed()) {
                s.close();
            }
            sendNotice("Connection ended");
        } catch (Exception e) {
        }
    }

    public void close() {
        // Send closing notice to clients
        sendLoading("Closing server");
        try {
            dos.writeUTF("427»Close server");
        } catch (Exception e) {
            if (!clientInfo.isEmpty()) {
                sendAlert("Error while send close request to client: " + e.getMessage());
                clientInfo.clear();
            }
        }

        // Close server
        try {
            clientInfo.clear();
            if (con != null && !con.isClosed()) {
                con.close();
            }
            if (ss != null && !ss.isClosed()) {
                ss.close();
            }
            sendNotice("Server closed");
            dispose();
        } catch (Exception e) {
            sendAlert("Error while closing server: " + e.getMessage());
        }
    }
}
