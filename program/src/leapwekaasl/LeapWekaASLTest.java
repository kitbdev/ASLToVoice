

package leapwekaasl;

import com.leapmotion.leap.*;
import java.util.Scanner;
import java.io.IOException;

public class LeapWekaASLTest {
    public static Scanner scanner = new Scanner(System.in);
    public static Controller controller = new Controller();
    public static LeapSensor leapSensor = new LeapSensor();
    
    public static void main(String[] args) 
                throws InterruptedException, IOException {
        if(controller.isConnected()) {
            System.out.println("Controller connected!");
        } else {
            System.out.println("No controller connected!");
        }
        Menu();
        System.out.println("Exiting...");
//        System.out.println("Press Enter to quit...");
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    public static void Menu() throws IOException {
        boolean running = true;
        //boolean valid = false;
        while (running) {
            boolean connected = controller.isConnected();
            boolean hasData = leapSensor.HasData();
            //while (!valid) {
                // prompt
                System.out.print("\nPress a key to make a selection:\n");
                if (connected) {
                    System.out.print("[R]ecord, ");
                }
                if (hasData) {
                    System.out.print("[S]ave, ");
                }
                System.out.print("[L]oad, ");
                System.out.print("or [E]xit:\n");
                
                // get input
                String sIn = scanner.next();
                char s = sIn.toLowerCase().charAt(0);
                //valid = true;
                if (s == 'r') {
                    if (connected) {                     
                        System.out.println(s+" is not implemented yet");   
                        try {
                            Record();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("controller not connected!");   
                    }
                } else if (s == 's') {
                    // do something
                    if (hasData) {
                        System.out.println(s+" is not implemented yet");
                    } else {
                        System.out.println("No Data!");
                    }
                } else if (s == 'l') {
                    // do something
                    System.out.print(s+" is not implemented yet");
                } else if (s == 'e') {
                    running = false;
                    break;
                } else {
                    //valid = false;
                    System.out.println(s+" is not a valid selection.");
                }
            //}
        }
    }
    public static void Record() throws IOException, InterruptedException {
        // have loop and stuff
        System.out.println("Press Enter to stop recording");
        leapSensor.ClearRecording();
        leapSensor.StartRecording("Sign"); // replace with some sign
        
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
    }
    public static void Update() {
        Frame frame = controller.frame();
        leapSensor.ProcessFrame(frame);
    }
}
