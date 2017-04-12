package asltovoice;

import com.leapmotion.leap.*;

// Collects data from the leap Sensor
public class LeapSensor {
    
    public static Controller controller = new Controller();
    
    public long lastFrameID = 0;
    boolean recording = false;
    public FrameData curFrame = new FrameData();
    long recordStartTimeN = 0;
    int numFrames = 0;
    
    // records a frame, returns false if failed
    public boolean RecordFrame() {
        Frame frame = controller.frame();
        if (lastFrameID != frame.id()) {
            HandList hands = frame.hands();
            if (hands.count() > 0) {
                ClearFrameData();
                // add time since start frame
                long curTime = System.currentTimeMillis()- recordStartTimeN;
                curFrame.time = (int)curTime;
                // add right hand then left hand to records, 0s if not available
                Hand hand = hands.get(0);
                RecordHand(hand);
                // TODO: two hands
//                if (hand.isRight()) {
//                    // this is the right hand
//                    RecordHand(hand);
//                    if(hands.count() > 1) {
//                        Hand lhand = hands.get(1);
//                        RecordHand(lhand);
//                    } else {
//                        RecordHand(null);
//                    }
//                } else {
//                    // this is the left hand
//                    if(hands.count() > 1) {
//                        Hand rhand = hands.get(1);
//                        RecordHand(rhand);
//                    } else {
//                        RecordHand(null);
//                    }
//                    RecordHand(hand);
//                }
                numFrames++;
            } else {
                // no hands!
                return false;
            }
        } else {
            // polling too fast!
            return false;
        }
        lastFrameID = frame.id();
        return true;
    }
    
    void RecordHand(Hand hand) {
        if (hand==null) {
            return;
        }
        // currently arm, hand, and finger pos rot and vel
        // TODO: left vs right hands
        Arm arm = hand.arm();
        if (arm.isValid()) {
            Vector armPos = arm.elbowPosition();
            curFrame.armPos.x = armPos.getX();
            curFrame.armPos.y = armPos.getY();
            curFrame.armPos.z = armPos.getZ();
            Vector armDir = arm.direction();
            curFrame.armRot.x = armDir.getX();
            curFrame.armRot.y = armDir.getY();
            curFrame.armRot.z = armDir.getZ();
            // no arm velocity
        }
        // hand
        Vector handPos = hand.palmPosition();
        curFrame.handPos.x = handPos.getX();
        curFrame.handPos.y = handPos.getY();
        curFrame.handPos.z = handPos.getZ();
        Vector handDir = hand.direction();
        curFrame.handRot.x = handDir.getX();
        curFrame.handRot.y = handDir.getY();
        curFrame.handRot.z = handDir.getZ();
        Vector handVel = hand.palmVelocity();
        curFrame.handVel.x = handVel.getX();
        curFrame.handVel.y = handVel.getY();
        curFrame.handVel.z = handVel.getZ();

        // fingers
        FingerList fingers = hand.fingers();
        for (int i = 0; i < 5; i++) {
            Finger f = fingers.get(i);
            // finger pos relative to hand
            Vector fPos = f.tipPosition().minus(handPos);
            curFrame.fingerPos[i].x = fPos.getX();
            curFrame.fingerPos[i].y = fPos.getY();
            curFrame.fingerPos[i].z = fPos.getZ();
            Vector fDir = f.direction();
            curFrame.fingerRot[i].x = fDir.getX();
            curFrame.fingerRot[i].y = fDir.getY();
            curFrame.fingerRot[i].z = fDir.getZ();
            Vector fVel = f.tipVelocity();
            curFrame.fingerVel[i].x = fVel.getX();
            curFrame.fingerVel[i].y = fVel.getY();
            curFrame.fingerVel[i].z = fVel.getZ();
        }
    }
    public boolean HandAvailable() {
        Hand hand = controller.frame().hands().frontmost();
        return hand.isValid();
    }

    public boolean ControllerConnected() {
        return controller.isConnected();
    }
    
    // start recording with the leap
    public void StartRecording() {
        controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
        ClearFrameData();
        lastFrameID = 0;
        numFrames = 0;
        recordStartTimeN = System.currentTimeMillis();
    }

    // clear the data we recorded
    public void ClearFrameData() {
        curFrame.ClearData();
        curFrame.fingerPos[0].x = -1;
    }
}
