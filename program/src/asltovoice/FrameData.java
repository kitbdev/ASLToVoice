package asltovoice;

// holds the raw data taken from the leap motion sensor
public class FrameData {
    //TODO: two hands
    public int id;
    public long time; // time since start of recording in ms
    public int curframe;
    public int totalframes;// TODO: move this to sign data only
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
        public float magnitude() {
            return (float) Math.sqrt(x*x+y*y+z*z);
        }
    }
    public Vector3 zero = new Vector3();

    // Loads comma seperated values into the correct variables
    public void LoadData(String textValues) {
        String[] dataValues = textValues.split(", ");
        id = Integer.parseInt(dataValues[0]);
        time = Integer.parseInt(dataValues[1]);
        curframe = Integer.parseInt(dataValues[2]);
        totalframes = Integer.parseInt(dataValues[3]);
        armPos.x = Float.parseFloat(dataValues[4]);
        armPos.y = Float.parseFloat(dataValues[5]);
        armPos.z = Float.parseFloat(dataValues[6]);
        armRot.x = Float.parseFloat(dataValues[7]);
        armRot.y = Float.parseFloat(dataValues[8]);
        armRot.z = Float.parseFloat(dataValues[9]);
        handPos.x = Float.parseFloat(dataValues[10]);
        handPos.y = Float.parseFloat(dataValues[11]);
        handPos.z = Float.parseFloat(dataValues[12]);
        handRot.x = Float.parseFloat(dataValues[13]);
        handRot.y = Float.parseFloat(dataValues[14]);
        handRot.z = Float.parseFloat(dataValues[15]);
        handVel.x = Float.parseFloat(dataValues[16]);
        handVel.y = Float.parseFloat(dataValues[17]);
        handVel.z = Float.parseFloat(dataValues[18]);
        for (int i=0;i<5;i++)
        {
            fingerPos[i].x = Float.parseFloat(dataValues[19+i*9]);
            fingerPos[i].y = Float.parseFloat(dataValues[20+i*9]);
            fingerPos[i].z = Float.parseFloat(dataValues[21+i*9]);
            fingerRot[i].x = Float.parseFloat(dataValues[22+i*9]);
            fingerRot[i].y = Float.parseFloat(dataValues[23+i*9]);
            fingerRot[i].z = Float.parseFloat(dataValues[24+i*9]);
            fingerVel[i].x = Float.parseFloat(dataValues[25+i*9]);
            fingerVel[i].y = Float.parseFloat(dataValues[26+i*9]);
            fingerVel[i].z = Float.parseFloat(dataValues[27+i*9]);
        }
        sign = dataValues[dataValues.length-1];
    }
    // Saves variables into a comma seperated string of values
    public String GetData() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(',');
        sb.append(time);
        sb.append(',');
        sb.append(curframe);
        sb.append(',');
        sb.append(totalframes);
        sb.append(',');
        sb.append(armPos.x);
        sb.append(',');
        sb.append(armPos.y);
        sb.append(',');
        sb.append(armPos.z);
        sb.append(',');
        sb.append(armRot.x);
        sb.append(',');
        sb.append(armRot.y);
        sb.append(',');
        sb.append(armRot.z);
        sb.append(',');
        sb.append(handPos.x);
        sb.append(',');
        sb.append(handPos.y);
        sb.append(',');
        sb.append(handPos.z);
        sb.append(',');
        sb.append(handRot.x);
        sb.append(',');
        sb.append(handRot.y);
        sb.append(',');
        sb.append(handRot.z);
        sb.append(',');
        sb.append(handVel.x);
        sb.append(',');
        sb.append(handVel.y);
        sb.append(',');
        sb.append(handVel.z);
        sb.append(',');
        for (int i=0;i<5;i++)
        {
            sb.append(fingerPos[i].x);
            sb.append(',');
            sb.append(fingerPos[i].y);
            sb.append(',');
            sb.append(fingerPos[i].z);
            sb.append(',');
            sb.append(fingerRot[i].x);
            sb.append(',');
            sb.append(fingerRot[i].y);
            sb.append(',');
            sb.append(fingerRot[i].z);
            sb.append(',');
            sb.append(fingerVel[i].x);
            sb.append(',');
            sb.append(fingerVel[i].y);
            sb.append(',');
            sb.append(fingerVel[i].z);
            sb.append(',');
        }
        sb.append(sign);
        return sb.toString();
    }
    // Saves variables into a double array
    public double[] GetDoubleData() {
        double[] data = new double[64];
        int n = 0;
        data[n++] = (id);
        data[n++] = (time);
        data[n++] = (curframe);
        data[n++] = (totalframes);
        data[n++] = (armPos.x);
        data[n++] = (armPos.y);
        data[n++] = (armPos.z);
        data[n++] = (armRot.x);
        data[n++] = (armRot.y);
        data[n++] = (armRot.z);
        data[n++] = (handPos.x);
        data[n++] = (handPos.y);
        data[n++] = (handPos.z);
        data[n++] = (handRot.x);
        data[n++] = (handRot.y);
        data[n++] = (handRot.z);
        data[n++] = (handVel.x);
        data[n++] = (handVel.y);
        data[n++] = (handVel.z);
        for (int i=0;i<5;i++)
        {
            data[n++] = (fingerPos[i].x);
            data[n++] = (fingerPos[i].y);
            data[n++] = (fingerPos[i].z);
            data[n++] = (fingerRot[i].x);
            data[n++] = (fingerRot[i].y);
            data[n++] = (fingerRot[i].z);
            data[n++] = (fingerVel[i].x);
            data[n++] = (fingerVel[i].y);
            data[n++] = (fingerVel[i].z);
        }
        return data;
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
