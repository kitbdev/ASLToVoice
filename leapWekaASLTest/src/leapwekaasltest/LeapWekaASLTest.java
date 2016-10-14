
package leapwekaasltest;

import com.leapmotion.leap.*;
import java.io.IOException;

public class LeapWekaASLTest {

    public static void main(String[] args) {
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();
        
        controller.addListener(listener);
        
        //while(true){}
        
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Quiting");
        controller.removeListener(listener);
    }
    
}

class SampleListener extends Listener {

    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }

    public void onFrame(Controller controller) {
        System.out.println("Frame available");
        Frame frame = controller.frame();
        System.out.println("Frame id: " + frame.id()
                   + ", timestamp: " + frame.timestamp()
                   + ", hands: " + frame.hands().count()
                   + ", fingers: " + frame.fingers().count());
    }
}
