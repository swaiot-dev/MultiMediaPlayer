package com.mediatek.wwtv.mediaplayer.jni;

import android.graphics.Rect;


public class PhotoRender {

	static {
		try {
			System.loadLibrary("picapi");
			System.out.println("System.loadLibrary: successfully");
		} catch(UnsatisfiedLinkError e) {
			System.out.println("System.loadLibrary" + e);
			e.printStackTrace(System.out);
		}
	}
	private long m_photoHandle;
	private int m_videoPath;
	private boolean m_init;

	public static int INIT_OK		= 0;
	public static int INIT_ERROR 	= -1;

	public PhotoRender()
	{
		//do nothing
		m_videoPath = 0;
		m_init = false;
	}

	public PhotoRender(int videoPath)
	{
		m_videoPath = videoPath;
		m_init = false;
	}

	public int	initPhotoPlay()
	{
		m_photoHandle = this.nativeInitPhotoPlay(m_videoPath);
		if (m_photoHandle == 0)
		{
			return INIT_ERROR;
		}

		m_init = true;

		return INIT_OK;
	}
	public int deinitPhotoPlay()
	{
		int ret = 0;

		if (m_init)
		{
			ret = this.nativeDeinitPhotoPlay(m_photoHandle);

			if (ret == 0)
			{
				m_init = false;
			}
		}

		return ret;
	}

	public int renderPhotoOnVdp(Rect src,Rect dst,long dataBitmap,int rotation)
	{
		return this.nativeRenderPhotoOnVdp(src, dst, dataBitmap, rotation, m_photoHandle);
	}

	public int regCbHandler()
	{
		return this.nativeRegCbHandler(this);
	}

	public int preparePhotoOnVdp(Rect src, Rect dst, long dataBitmap, int rotation)
	{
		return this.nativePreparePhotoOnVdp(src, dst, dataBitmap, rotation, m_photoHandle);
	}

	public int startFade(int duration, int bgColor, int fadeType)
	{
		return this.nativeStartFade(duration, bgColor, fadeType, m_photoHandle);
	}

	public int pauseFade()
	{
		return this.nativePauseFade(m_photoHandle);
	}

	public int restartFade()
	{
		return this.nativeRestartFade(m_photoHandle);
	}

	public int cleanVdp()
	{
		return this.nativeCleanVdp();
	}
	
	public static boolean is4KPanel()
	{
		return nativeIs4KPanel();
	}
	public static int rotateBitmap(long srcBitmap, long dstBitmap)
	{
		return nativeRotate(srcBitmap, dstBitmap);
	}
	private  native long nativeInitPhotoPlay(int videoPath);
	private  native int nativeDeinitPhotoPlay(long photoHandle);
	private  native int nativeRenderPhotoOnVdp(Rect src,Rect dst ,long dataBitmap, int rotation, long photoHandle);
	private  native int nativeRegCbHandler(PhotoRender a);
	private  native int nativePreparePhotoOnVdp(Rect src, Rect dst, long dataBitmap, int rotation, long photoHandle);
	private  native int nativeStartFade(int duration, int bgColor, int fadeType, long photoHandle);
	private  native int nativePauseFade(long photoHandle);
	private  native int nativeRestartFade(long photoHandle);
	private  native int nativeCleanVdp();
	private  static native boolean nativeIs4KPanel();
	private static native int nativeRotate(long srcBitmap, long dstBitmap);
}

