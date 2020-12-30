public class Quickchat {
    public static void main(String[] args) {
        Client c = new Client();

        // Handle login screen
        while (c.isShowing()) {
            if (c.screenID == 76) {
                c.connect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        c.close();
    }
}