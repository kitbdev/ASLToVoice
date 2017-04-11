package asltovoice;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
/**
 *
 * @author calebshirley0610
 */
public class TTS {
    VoiceManager voiceManager = VoiceManager.getInstance();
    Voice voice = voiceManager.getVoice("kevin16");
    
    public boolean mute = false;
    
    public void allocate()
    {
        if (voice == null) {
            System.err.println(
                "Cannot find a voice named kevin16.  Please specify a different voice.");
            System.exit(1);
        }
        voice.allocate();
    }
    public void deallocate()
    {
        voice.deallocate();
    }
    
    public void speak(String word){
        if(!mute)
        {
            voice.speak(word);
        }
    }
}
