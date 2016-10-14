

package leapwekaasl;

import com.leapmotion.leap.*;

import java.io.IOException;

public class LeapWekaASLTest {
    public static Controller controller = new Controller();
    //public static LeapSensor leapSensor = new LeapSensor();
    public static void main(String[] args) 
                throws InterruptedException, IOException {
        LeapSensor.SaveData();
        if(controller.isConnected()) {
            System.out.println("Controller connected!");
            System.out.println("Press Enter to quit...");
            long timeSinceStart = System.currentTimeMillis();
            //long dt = 0, frameStart = 0;
            long pollRate = 1/100;// 1 poll every .1 seconds
            // TODO: only loop when looking for data
            boolean exit = false;
            while(!exit) {
                long frameStart = System.currentTimeMillis();
                
                if (System.in.available() > 0) {
                    exit = true;
                }
                Update();
                long dt = System.currentTimeMillis() - frameStart; // time that this update took
                long timeLeftThisFrame = pollRate - dt;
                Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
            }
            // TODO: add cmd-based menu
        } else {
            System.out.println("No controller connected!");
        }
        
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void Update() {
        Frame frame = controller.frame();
        LeapSensor.ProcessFrame(frame);
        
    }
}
