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
    // Loads comma seperated values into the correct variables
    public void LoadData(String textValues){
        // TODO
    }
    // Saves variables into a comma seperated string of values
    public String SaveData(){
        // TODO
        return "";
    }
    
}
