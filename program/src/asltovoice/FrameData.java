/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asltovoice;

// holds all of the data for a sign
// hands, arm, fingers pos, rot, and vel over the full length of the recording
// time taken, number of frames, and other needed info
public class FrameData {
    //TODO: two hands
    public float time; // time since start of recording
    public float curframe;
    public Vector3 armPos;
    public Vector3 armRot;
    public Vector3 handPos;
    public Vector3 handRot;
    public Vector3 handVel;
    public Vector3[] fingerPos = new Vector3[5];
    public Vector3[] fingerRot = new Vector3[5];
    public Vector3[] fingerVel = new Vector3[5];
    public String sign;
    public class Vector3 {
        float x;
        float y;
        float z;
        public Vector3(){
            x = 0; y = 0; z = 0;
        };
    }
        Vector3 zero = new Vector3();
    // Loads comma seperated values into the correct variables
    public void LoadData(String textValues) {
        // TODO
    }
    // Saves variables into a comma seperated string of values
    public String GetData() {
        StringBuilder sb = new StringBuilder();
        // TODO
        sb.append(time);
        sb.append(',');
        sb.append(curframe);
        sb.append(',');
//        sb.append(numFrames);// total frames of this sign
//        sb.append(',');
////        for (int j = 0; j < dataPerFrame; j++) {
////            sb.append(records.get(i * dataPerFrame + j).toString());
////            sb.append(',');
//        }
        if (sign != "") {
            sb.append(sign);
        }
        return "";
    }
    public String GetHeaderLine() {
        String header = "";
        header += "id,";
        // other things
        header += "sign";
        return "";
    }
    void AddPosRot(StringBuilder sb, String name) {
        sb.append(name);
        sb.append("_pos_x,");
        sb.append(name);
        sb.append("_pos_y,");
        sb.append(name);
        sb.append("_pos_z,");
        sb.append(name);
        sb.append("_yaw,");
        sb.append(name);
        sb.append("_roll,");
        sb.append(name);
        sb.append("_pitch,");
    }

    void AddPosRotVel(StringBuilder sb, String name) {
        AddPosRot(sb, name);
        sb.append(name);
        sb.append("_vel_x,");
        sb.append(name);
        sb.append("_vel_y,");
        sb.append(name);
        sb.append("_vel_z,");
    }
    public void ClearData() {
        time = 0;
        armPos = zero;
        armRot = zero;
        handPos = zero;
        handRot = zero;
        handVel = zero;
        fingerPos = new Vector3[5];
        fingerRot = new Vector3[5];
        fingerVel = new Vector3[5];
        sign = "";
    }
}
