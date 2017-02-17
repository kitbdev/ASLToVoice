/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TTSWrapper;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
/**
 *
 * @author calebshirley0610
 */
public class TTS {
    VoiceManager voiceManager = VoiceManager.getInstance();
    Voice helloVoice = voiceManager.getVoice("kevin16");
    public TTS(){
         helloVoice.allocate();
    }
    public void speak(String word){
        helloVoice.speak(word);
    }
}
