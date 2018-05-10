package com.mediatek.wwtv.mediaplayer.jni;
import android.graphics.Bitmap;
import android.os.SystemProperties;
import android.util.Log;

public class GLJniLib {

    static {
	    try {
				System.loadLibrary("vss");
				System.out.println("System.loadLibrary libvss: successfully");
			} catch(UnsatisfiedLinkError e) {
				System.out.println("System.loadLibrary" + e);
				e.printStackTrace(System.out);
			}
    }
    
   /**
    * @param width the current view width
    * @param height the current view height
    */
    
    public static native void nativeInit();
    public static native void nativeUnInit();
    public static native int nativeGetHeight();
    public static native int nativeGetWidth();
    public static native void nativeResize(int w, int h);
    public static native void nativeRender();
    public static native void nativeTimingChange();
    public static native void nativeDone();
    public static native int add(int a,int b);
    public static native void nativeUpdateTexture(int texid);
}
