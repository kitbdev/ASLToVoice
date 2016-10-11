import com.leapmotion.leap.*;

class Sample {
	public static void main(String[] args) {
	// Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
}