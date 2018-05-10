package com.mediatek.wwtv.mediaplayer.util;

import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.util.Log;
import android.content.Context;
import java.lang.Thread;
import android.view.accessibility.AccessibilityManager;
import android.media.AudioAttributes;
import android.os.Bundle;

public class TextToSpeechUtil {
    private static final String TAG = "TextToSpeechUtil";
    private static TextToSpeech mTts = null;
    private Context myContext = null;
    private boolean mTalkBackEnabled;
    private AccessibilityManager mAccm;
    private Bundle mTtsBundle = null;

   /**
    * Queue mode where all entries in the playback queue (media to be played
    * and text to be synthesized) are dropped and replaced by the new entry.
    * Queues are flushed with respect to a given calling app. Entries in the queue
    * from other callees are not discarded.
    */
    public static final int QUEUE_FLUSH = 0;

   /**
    * Queue mode where the new entry is added at the end of the playback queue.
    */
    public static final int QUEUE_ADD = 1;

   /**
    * The constructor for the TextToSpeechUtil class, using the default TTS engine.
    * @param context The context this instance is running in.
    */
    public TextToSpeechUtil(Context context) {
        if(context!=null){
            myContext = context;
            mAccm = AccessibilityManager.getInstance(context);
            Log.d(TAG, "new TextToSpeech created!!!");
        }else{
            Log.d(TAG, "context is null!!!");
        }
    }

    private TextToSpeech getTextToSpeech() {
        if(mTts == null) {
            mTts = new TextToSpeech(myContext, mInitListener);
            mTts.setAudioAttributes(new AudioAttributes.Builder().setContentType(1).setUsage(11).build());
            mTtsBundle = new Bundle();
            mTtsBundle.putInt("streamType", 10);
        }

        return mTts;
    }

   /**
    * The initialization listener used when we are initalizing the settings
    * screen for the first time (as opposed to when a user changes his choice
    * of engine).
    */
    private final TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
        onInitEngine(status);
        }
    };
   
   /**
    * Called when the TTS engine is initialized.
    */
    private void onInitEngine(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS engine for settings screen initialized.");
            Log.d(TAG, "Updating engine: Successfully bound to the engine: " +
                getTextToSpeech().getCurrentEngine());
        } else {
            Log.d(TAG, "TTS engine for settings screen failed to initialize successfully.");
        }
    }

   /**
    * Check current is TTS Enable or disable status.
    * @return true when current TTS is enable from Launcher Settings.
    * @return false when disable TTS.
    */
    public boolean isTTSEnable() {
        boolean accessibilityEnabled = Settings.Secure.getInt(myContext.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
        Log.d(TAG, "accessibilityEnabled="+accessibilityEnabled);
        mTalkBackEnabled = mAccm.isEnabled() && mAccm.isTouchExplorationEnabled();
        Log.d(TAG, "mTalkBackEnabled="+mTalkBackEnabled);
        return mTalkBackEnabled;//accessibilityEnabled;
    }

   /**
    * Speaks the string using the specified queuing strategy and speech parameters.
    * @param text The string of text to be spoken. No longer than
    *{@link #getMaxSpeechInputLength()} characters.
    */
    public int speak(final String text) {
        if(isTTSEnable()){
            if(getTextToSpeech() == null){
                Log.d(TAG, "mTts is NULL in speak!!!");
                return -1;
            }
            Log.d(TAG, "mTts.getLanguage()="+mTts.getLanguage());
            Log.d(TAG, "mTts.isLanguageAvailable(mTts.getLanguage())="+mTts.isLanguageAvailable(mTts.getLanguage()));
            if(mTts.isLanguageAvailable(mTts.getLanguage()) >= TextToSpeech.LANG_AVAILABLE){
                Log.d(TAG, "mTts.speak "+ text);
                return mTts.speak(text, TextToSpeech.QUEUE_FLUSH, mTtsBundle, "MediaPlayer");
            }else{
                Log.d(TAG, "mTts.isLanguageAvailable false");
                return -1;
            }
        }else{
            Log.d(TAG, "isTTSEnable is false!!!");
        }
        return -1;
    }

   /**
    * Speaks the string using the specified queuing strategy and speech parameters.
    * @param text The string of text to be spoken. No longer than
    * {@link #getMaxSpeechInputLength()} characters.	
    * @param queueMode The queuing strategy to use, {@link #QUEUE_ADD} or {@link #QUEUE_FLUSH}.
    */
    public int speak(final String text, int queueMode) {
        if(isTTSEnable()){
            if(getTextToSpeech() == null){
                Log.d(TAG, "mTts is NULL in speak!!!");
                return -1;
            }
            Log.d(TAG, "mTts.getLanguage()="+mTts.getLanguage());
            Log.d(TAG, "mTts.isLanguageAvailable(mTts.getLanguage())="+mTts.isLanguageAvailable(mTts.getLanguage()));
            if(mTts.isLanguageAvailable(mTts.getLanguage()) >= TextToSpeech.LANG_AVAILABLE){
                Log.d(TAG, "mTts.speak "+ text);
                return mTts.speak(text, queueMode, mTtsBundle, "MediaPlayer");
            }else{
                Log.d(TAG, "mTts.isLanguageAvailable false");
                return -1;
            }
        }else{
            Log.d(TAG, "isTTSEnable is false!!!");
        }
        return -1;
    }

   /**
    * Releases the resources used by the TextToSpeech engine.
    * It is good practice for instance to call this method in the onDestroy() method of an Activity
    * so the TextToSpeech engine can be cleanly stopped.
    */
    public void shutdown() {
        if(getTextToSpeech() == null){
            Log.d(TAG, "mTts is NULL in shutdown!!!");
            return;
        }
        try {
            mTts.shutdown();
            mTts = null;
            Log.d(TAG, "TextToSpeech shutdown now!!!");
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down TTS engine" + e);
        }
    }
}
