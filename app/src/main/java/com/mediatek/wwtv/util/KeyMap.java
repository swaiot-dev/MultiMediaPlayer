package com.mediatek.wwtv.util;

import android.view.KeyEvent;

public class KeyMap {
	/**
	 * The following keys correspond to the 58 keys on the remote control, remote control button on the order from top to bottom, left to right, for example,
       in onKeyDown() Function to determine if(keyCode = =
	 * KEYCODE_POWER) or if(keyCode = = KEYCODE_MTKIR_SOURCE) POWER Usage
     * KEYCODE_POWER SOURCE  Usage KEYCODE_MTKIR_SOURCE
	 */

	// key 1 POWER
	public static final int KEYCODE_POWER = KeyEvent.KEYCODE_POWER;//26
	// key 2 HOME
	public static final int KEYCODE_HOME = KeyEvent.KEYCODE_HOME;///3
	// key 3 MTKIR_SOURCE
	public static final int KEYCODE_MTKIR_SOURCE = KeyEvent.KEYCODE_TV_INPUT;//178
	// key 4 MTKIR_TIMER
	public static final int KEYCODE_MTKIR_TIMER = 221;//KeyEvent.KEYCODE_MTKIR_TIMER;
	// key 5 MTKIR_SLEEP
	public static final int KEYCODE_MTKIR_SLEEP = 222;//KeyEvent.KEYCODE_MTKIR_SLEEP;
	// key 6 MTKIR_ZOOM
	public static final int KEYCODE_MTKIR_ZOOM = KeyEvent.KEYCODE_ZOOM_IN;//168
	// key 7 MTKIR_PEFFECT
	public static final int KEYCODE_MTKIR_PEFFECT = 224;//KeyEvent.KEYCODE_MTKIR_PEFFECT;
	// key 8 MTKIR_SEFFECT
	public static final int KEYCODE_MTKIR_SEFFECT = 215;//KeyEvent.KEYCODE_MTKIR_SEFFECT;
	// key 9 MTKIR_ASPECT
	public static final int KEYCODE_MTKIR_ASPECT = 225;//KeyEvent.KEYCODE_MTKIR_ASPECT;
	// key 10 MTKIR_PIPPOP
	public static final int KEYCODE_MTKIR_PIPPOP = KeyEvent.KEYCODE_WINDOW;//171
	// key 11 MTKIR_POPPOS
	public static final int KEYCODE_MTKIR_PIPPOS = 227;//KeyEvent.KEYCODE_MTKIR_POPPOS;
	// key 12 MTKIR_PIPSIZE
	public static final int KEYCODE_MTKIR_PIPSIZE = 228;//KeyEvent.KEYCODE_MTKIR_PIPSIZE;
	// key 13 MTKIR_MTSAUDIO
	public static final int KEYCODE_MTKIR_MTSAUDIO = 216;//KeyEvent.KEYCODE_MTKIR_MTSAUDIO;
	// key 14 MTKIR_CC
	public static final int KEYCODE_MTKIR_MTKIR_CC = KeyEvent.KEYCODE_CAPTIONS;//175
	// key 15 MTKIR_SWAP
	public static final int KEYCODE_MTKIR_MTKIR_SWAP = 230;//KeyEvent.KEYCODE_MTKIR_SWAP;
	// key 16 1
	public static final int KEYCODE_1 = KeyEvent.KEYCODE_1;//8
	// key 17 2
	public static final int KEYCODE_2 = KeyEvent.KEYCODE_2;//9
	// key 18 3
	public static final int KEYCODE_3 = KeyEvent.KEYCODE_3;//10
	// key 19 4
	public static final int KEYCODE_4 = KeyEvent.KEYCODE_4;//11
	// key 20 5
	public static final int KEYCODE_5 = KeyEvent.KEYCODE_5;//12
	// key 21 6
	public static final int KEYCODE_6 = KeyEvent.KEYCODE_6;//13
	// key 22 7
	public static final int KEYCODE_7 = KeyEvent.KEYCODE_7;//14
	// key 23 8
	public static final int KEYCODE_8 = KeyEvent.KEYCODE_8;//15
	// key 24 9
	public static final int KEYCODE_9 = KeyEvent.KEYCODE_9;//16
	// key 25 MTKIR_FREEZE
	public static final int KEYCODE_MTKIR_FREEZE = 231;//KeyEvent.KEYCODE_MTKIR_FREEZE;
	// key 26 0
	public static final int KEYCODE_0 = KeyEvent.KEYCODE_0;//7
	// key 27 PERIOD
	public static final int KEYCODE_PERIOD = KeyEvent.KEYCODE_PERIOD;//56
	// key 28 MTKIR_CHUP
	public static final int KEYCODE_MTKIR_CHUP = KeyEvent.KEYCODE_CHANNEL_UP;//166
	// key 29 MTKIR_PRECH
	public static final int KEYCODE_MTKIR_PRECH = 218;//KeyEvent.KEYCODE_MTKIR_PRECH;
	// key 30 VOLUME_UP
	public static final int KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;//24
	// key 31 MTKIR_CHDN
	public static final int KEYCODE_MTKIR_CHDN = KeyEvent.KEYCODE_CHANNEL_DOWN;//167
	// key 32 MTKIR_MUTE
	public static final int KEYCODE_MTKIR_MUTE = KeyEvent.KEYCODE_VOLUME_MUTE;//164
	// key 33 VOLUME_DOWN
	public static final int KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;//25
	// key 34 MENU
	public static final int KEYCODE_MENU = KeyEvent.KEYCODE_MENU;//82
	// key 35 DPAD_UP
	public static final int KEYCODE_DPAD_UP = KeyEvent.KEYCODE_DPAD_UP;//19
	// key 36 MTKIR_INFO
	public static final int KEYCODE_MTKIR_INFO = KeyEvent.KEYCODE_INFO;//165
	// key 37 DPAD_LEFT
	public static final int KEYCODE_DPAD_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;//21
	// key 38 DPAD_CENTER
	public static final int KEYCODE_DPAD_CENTER = KeyEvent.KEYCODE_DPAD_CENTER;//23
	// key 39 DPAD_RIGHT
	public static final int KEYCODE_DPAD_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;//22
	// key 40 MTKIR_GUIDE
	public static final int KEYCODE_MTKIR_GUIDE = KeyEvent.KEYCODE_GUIDE;//172
	// key 41 DPAD_DOWN
	public static final int KEYCODE_DPAD_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;//20
	// key 42 BACK
	public static final int KEYCODE_BACK = KeyEvent.KEYCODE_BACK;///4
	// key 43 MTKIR_EJECT
	public static final int KEYCODE_MTKIR_EJECT = KeyEvent.KEYCODE_MEDIA_EJECT;//129
	// key 44 MTKIR_PLAYPAUSE
	public static final int KEYCODE_MTKIR_PLAYPAUSE = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;//85
	// key 45 MTKIR_STOP
	public static final int KEYCODE_MTKIR_STOP = KeyEvent.KEYCODE_MEDIA_STOP;//86
	// key 46 MTKIR_RECORD
	public static final int KEYCODE_MTKIR_RECORD = KeyEvent.KEYCODE_MEDIA_RECORD;//130
	// key 47 MTKIR_REWIND
	public static final int KEYCODE_MTKIR_REWIND = KeyEvent.KEYCODE_MEDIA_REWIND;//89
	// key 48 MTKIR_FASTFORWARD
	public static final int KEYCODE_MTKIR_FASTFORWARD = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;//90
	// key 49 MTKIR_PREVIOUS
	public static final int KEYCODE_MTKIR_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;//88
	// key 50 MTKIR_NEXT
	public static final int KEYCODE_MTKIR_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;//87
	// key 51 MTKIR_TITLEPBC
	public static final int KEYCODE_MTKIR_TITLEPBC = 205;//KeyEvent.KEYCODE_MTKIR_TITLEPBC;
	// key 52 MTKIR_SUBTITLE
	public static final int KEYCODE_MTKIR_SUBTITLE = 206;//KeyEvent.KEYCODE_MTKIR_SUBTITLE;
	// key 53 MTKIR_REPEAT
	public static final int KEYCODE_MTKIR_REPEAT = 207;//KeyEvent.KEYCODE_MTKIR_REPEAT;
	// key 54 MTKIR_ANGLE
	public static final int KEYCODE_MTKIR_ANGLE = 214;//KeyEvent.KEYCODE_MTKIR_ANGLE;
	// key 55 MTKIR_RED
	public static final int KEYCODE_MTKIR_RED = KeyEvent.KEYCODE_PROG_RED;//183
	// key 56 MTKIR_GREEN
	public static final int KEYCODE_MTKIR_GREEN = KeyEvent.KEYCODE_PROG_GREEN;//184
	// key 57 MTKIR_YELLOW
	public static final int KEYCODE_MTKIR_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW;//185
	// key 58 MTKIR_BLUE
	public static final int KEYCODE_MTKIR_BLUE = KeyEvent.KEYCODE_PROG_BLUE;//186
	public static final int KEYCODE_MTKIR_BLUETOOTH_PRE 	= KeyEvent.KEYCODE_MEDIA_PREVIOUS;//88
	public static final int KEYCODE_MTKIR_BLUETOOTH_NEXT  	= KeyEvent.KEYCODE_MEDIA_NEXT;//87
	public static final int KEYCODE_MTKIR_BLUETOOTH_ENTER  	= 126;
    public static final int KEYCODE_MTKIR_BLUETOOTH_ENTER_2 = 127;
	//Begin==>Modified by duzhihong for solving "pause not work in 7701 "
	public static final int KEYCODE_MTKIR_TIMESHIFT_PAUSE  	= KeyEvent.KEYCODE_MEDIA_PAUSE;
	// key 59 SKYWORTH_text
	public static final int KEYCODE_SKYWORTH_TEXT = 500;
}
