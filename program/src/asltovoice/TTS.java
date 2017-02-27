/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asltovoice;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
/**
 *
 * @author calebshirley0610
 */
public class TTS {
    VoiceManager voiceManager = VoiceManager.getInstance();
    Voice helloVoice = voiceManager.getVoice("kevin16");
    
    public boolean mute = false;
    
    public void allo()
    {
        if (helloVoice == null) {
            System.err.println(
                "Cannot find a voice named kevin16.  Please specify a different voice.");
            System.exit(1);
        }
        helloVoice.allocate();
    }
    public void deallo()
    {
        helloVoice.deallocate();
    }
    
    public void speak(String word){
        if(!mute)
        {
            helloVoice.speak(word);
        }
    }
}
