
package leapwekaasltest;

import com.leapmotion.leap.*;
import java.io.IOException;

public class LeapWekaASLTest {

    public static void main(String[] args) {
        SampleListener listener = new SampleListener();
        Controller controller = new Controller();
        
        controller.addListener(listener);
//        controller.frame();
        
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller.removeListener(listener);
    }
    
}

class SampleListener extends Listener {

    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }

    public void onFrame(Controller controller) {
        System.out.println("Frame available");
    }
}