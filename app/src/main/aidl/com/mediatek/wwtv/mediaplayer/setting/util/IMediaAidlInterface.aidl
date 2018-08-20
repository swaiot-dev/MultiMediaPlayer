// IMediaAidlInterface.aidl
package com.mediatek.wwtv.mediaplayer.setting.util;

// Declare any non-default types here with import statements

interface IMediaAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void Previous();   //上一个
    void Next();       //下一个
    void Pause();      //暂停
    void Play();       //继续
    void Stop();       //停止
    void StartOver();  //重来
    void Rewind();     //倒带
    void FastForward();//快进
    /**
    * play media by type
    * @param type 0:video
    * 1:photo
    * 2:music
    */
    void playResource(int type);
}
