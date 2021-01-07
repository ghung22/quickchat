import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Runnable;
import java.net.Socket;

@SuppressWarnings("serial")
public class Client extends GUI {
    Socket s;

    Boolean failLogin = false;
    Integer failsafeFlag = 5; // End client after 5 failed attemps

    Client() {
        super("client");
    }

    public void launch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isShowing()) {
                    if (screenID == 76) {
                        connect();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "Handle login").start();
    }

    private void connect() {
        // Conect to specified server (if logged in successfully)
        String ip = server;
        Integer p = Integer.parseInt(port);
        sendLoading("Connecting server at " + ip + ":" + p);
        try {
            s = new Socket(ip, p);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
            sendNotice("Connection made successfully");
            loginToServer();

            // Check if connection up and window showing
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkConnection();
                }
            }, "Check connection").start();

            // Start Main Menu
            mainStart();
            msgStr = "<b>" + user + "</b> entered the chat. Please be civilized.";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (connectStarted) {
                        listen();
                    }
                }
            }, "Listen").start();
        } catch (Exception e) {
            sendAlert("Error while connecting to server: " + e.getMessage());
            endConnection();
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
        endConnection();
    }

    private void register() {
        try {
            dos.writeUTF("706»regUser»" + user);
            dos.writeUTF("706»regPass»" + pass);
        } catch (Exception e) {
            sendAlert("Error while registering client: " + e.getMessage());
        }
    }

    private void authenticate() {
        try {
            dos.writeUTF("706»authUser»" + user);
            dos.writeUTF("706»authPass»" + pass);
        } catch (Exception e) {
            sendAlert("Error while authenticating client: " + e.getMessage());
        }
    }

    private void loginToServer() throws Exception {
        while (!connectStarted) {
            if (registerStarted) {
                register();
                registerStarted = false;
            }
            if (!authStarted) {
                return;
            }
            authStarted = false;
            authenticate();
            while (!failLogin) {
            }
            if (failLogin)
                user = defaultUser;
                sendAlert("Error while authenticating client: Wrong username or password");
                if (--failsafeFlag <= 0) {
                    sendAlert("Critical: Too many failed attempts, closing client");
                    endConnection();
                }
            failLogin = false;
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

            case 706:
                subsubmsg = submsg[1].split("»", 2);
                switch (subsubmsg[0]) {
                    case "authStatus":
                        if (subsubmsg[1] == "success") {
                            connectStarted = true;
                        } else {
                            failLogin = true;
                        }
                        break;

                    default:
                        sendAlert("Unresolvable message: code " + msgCode + " » " + msg);
                        break;
                }
                break;

            case 427:
                switch (submsg[0]) {
                    case "Close server":
                        sendNotice("Server closed");
                        close();
                        break;

                    case "Close client":
                        sendNotice("Received close request");
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
            if (s != null && !s.isClosed()) {
                s.close();
            }
            sendNotice("Connection ended");
        } catch (Exception e) {
        }
        loginStart();
    }

    public void close() {
        sendLoading("Ending connection");
        try {
            dos.writeUTF("427»Close client");
            if (s != null && !s.isClosed()) {
                s.close();
            }
            sendNotice("Connection ended");
            dispose();
        } catch (Exception e) {
            sendAlert("Error while ending connection: " + e.getMessage());
        }
    }
}
