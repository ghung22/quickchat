import java.text.SimpleDateFormat;
import java.util.Date;

public class GUI {
    String timestamp;

    GUI() {

    }

    public void sendLoading(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(@) [" + timestamp + "] " + msg + "...");
    }

    public void sendNotice(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(i) [" + timestamp + "] " + msg + ".");
    }

    public void sendError(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(!) [" + timestamp + "] " + msg + ".");
    }

    public void sendQuery(String msg) {
        timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("(?) [" + timestamp + "] " + msg + ".");
    }
}
