/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view.common;

import android.view.KeyEvent;

/**
 * @author MTK40707
 *
 */
public interface NavBasic {
    public static final int NAV_COMP_ID_MASK        = 0xF000000;
    //For Android Component ID
    public static final int NAV_COMP_ID_BASIC       = 0x1000000;
    public static final int NAV_COMP_ID_EAS         = NAV_COMP_ID_BASIC + 1;
    public static final int NAV_COMP_ID_BANNER      = NAV_COMP_ID_BASIC + 2;
    public static final int NAV_COMP_ID_MUTE        = NAV_COMP_ID_BASIC + 3;
    public static final int NAV_COMP_ID_CEC         = NAV_COMP_ID_BASIC + 4;
    public static final int NAV_COMP_ID_CH_LIST     = NAV_COMP_ID_BASIC + 5;
    public static final int NAV_COMP_ID_SUNDRY      = NAV_COMP_ID_BASIC + 6;
    public static final int NAV_COMP_ID_ZOOM_PAN    = NAV_COMP_ID_BASIC + 7;
    public static final int NAV_COMP_ID_DIALOG_MSG  = NAV_COMP_ID_BASIC + 8;
    public static final int NAV_COMP_ID_EWS         = NAV_COMP_ID_BASIC + 9;
    public static final int NAV_COMP_ID_FAV_LIST    = NAV_COMP_ID_BASIC + 10;
    public static final int NAV_COMP_ID_VOL_CTRL    = NAV_COMP_ID_BASIC + 11;
    public static final int NAV_COMP_ID_GINGA_TV    = NAV_COMP_ID_BASIC + 12;
    public static final int NAV_COMP_ID_INFO_BAR    = NAV_COMP_ID_BASIC + 13;
    public static final int NAV_COMP_ID_INPUT_SRC   = NAV_COMP_ID_BASIC + 14;
    public static final int NAV_COMP_ID_UPDATER     = NAV_COMP_ID_BASIC + 15;
    public static final int NAV_COMP_ID_TWINKLE_MSG = NAV_COMP_ID_BASIC + 16;
    public static final int NAV_COMP_ID_TELETEXT    = NAV_COMP_ID_BASIC + 17;
    public static final int NAV_COMP_ID_PWD_DLG     = NAV_COMP_ID_BASIC + 18;
    public static final int NAV_COMP_ID_POP         = NAV_COMP_ID_BASIC + 19;
    public static final int NAV_COMP_ID_CI          = NAV_COMP_ID_BASIC + 20;
    public static final int NAV_COMP_ID_OAD         = NAV_COMP_ID_BASIC + 21;
    public static final int NAV_COMP_ID_SAT_SEL     = NAV_COMP_ID_BASIC + 22;
    public static final int NAV_COMP_ID_SCART_MONITOR= NAV_COMP_ID_BASIC + 23;
    public static final int NAV_COMP_ID_MISC        = NAV_COMP_ID_BASIC + 24;
    public static final int NAV_COMP_ID_PVR_TIMESHIFT = NAV_COMP_ID_BASIC + 25;
    public static final int NAV_COMP_ID_POWER_OFF   = NAV_COMP_ID_BASIC + 26;
    public static final int NAV_COMP_ID_SUNDRY_DIALOG   = NAV_COMP_ID_BASIC + 27;
    public static final int NAV_COMP_ID_MENU_OPTION_DIALOG = NAV_COMP_ID_BASIC + 28;
    public static final int NAV_COMP_ID_CI_DIALOG   = NAV_COMP_ID_BASIC + 29;
    public static final int NAV_COMP_ID_EPOP_VIEW  = NAV_COMP_ID_BASIC + 30;

    //For Native Component ID
    public static final int NAV_NATIVE_COMP_ID_BASIC        = (2 * NAV_COMP_ID_BASIC);
    public static final int NAV_NATIVE_COMP_ID_MHEG5        = NAV_NATIVE_COMP_ID_BASIC + 1;
    public static final int NAV_NATIVE_COMP_ID_HBBTV        = NAV_NATIVE_COMP_ID_BASIC + 2;
    public static final int NAV_NATIVE_COMP_ID_GINGA        = NAV_NATIVE_COMP_ID_BASIC + 3;
    public static final int NAV_NATIVE_COMP_ID_MHP          = NAV_NATIVE_COMP_ID_BASIC + 4;
    public static final int NAV_NATIVE_COMP_ID_SUBTITLE_INFO= NAV_NATIVE_COMP_ID_BASIC + 5;

    //code for communicate between Activities
    public static final int NAV_REQUEST_CODE                = (3 * NAV_COMP_ID_BASIC);
    public static final int NAV_RESULT_CODE_MENU            = NAV_REQUEST_CODE + 1;

    //For comp show/hide flag
    public static final String NAV_COMPONENT_HIDE_FLAG = "NavComponentHide";
    public static final String NAV_COMPONENT_SHOW_FLAG = "NavComponentShow";

    //Common time out
    public static final int NAV_TIMEOUT_1   = 1 * 1000;//1s
    public static final int NAV_TIMEOUT_2   = 2 * 1000;//2S
    public static final int NAV_TIMEOUT_3   = 3 * 1000;//3S
    public static final int NAV_TIMEOUT_5   = 5 * 1000;//5s
    public static final int NAV_TIMEOUT_10  = 2 * NAV_TIMEOUT_5;//10s
    public static final int NAV_TIMEOUT_120 = 12* NAV_TIMEOUT_10;//120s

    //Priority
    public static final int NAV_PRIORITY_DEFAULT    = 10;
    public static final int NAV_PRIORITY_HIGH_1     = 11;
    public static final int NAV_PRIORITY_HIGH_2     = 12;
    public static final int NAV_PRIORITY_HIGH_3     = 13;
    public static final int NAV_PRIORITY_LOW_1      = 9;
    public static final int NAV_PRIORITY_LOW_2      = 8;
    public static final int NAV_PRIORITY_LOW_3      = 7;

    //Common Methods

    /**
     * this method is used to check the status(visible or invisible) of this components
     *
     * @return true if the component is visible, false if invisible
     */
    public boolean isVisible();

    /**
     * this method is used to check whether the specific key is the hot key of this component
     * <pre>
     * For example, the hot key of input source component is KEYCODE_MTKIR_SOURCE, when keyCode
     * is KEYCODE_MTKIR_SOURCE, the returned value is true, otherwise, false will be returned.
     * <pre>
     * @param keyCode, key code
     *
     * @return true if it's hot key of this component, others is false.
     */
    public boolean isKeyHandler(int keyCode);

    /**
     * this method is used to get the component id
     *
     * @return the component id
     */
    public int getComponentID();

    /**
     * this method is used to get the priority of the component
     *
     * @return the priority
     */
    public int getPriority();

    /**
     * this method is used to judge whether the specific component id is conexist with this component
     *
     * @param componentID, the component id
     *
     * @return true if they are coexist, others is false.
     */
    public boolean isCoExist(int componentID);

    /**
     * this method is used to handler key when the conponent is visible
     *
     * @param keyCode, key code
     * @param event
     * @param fromNative, true if key is from native app, false if key is from android ap
     *
     * @return true if key is handled and other other component should not handle it again, others is false.
     */
    public boolean KeyHandler(int keyCode, KeyEvent event, boolean fromNative);

    /**
     * this method is used to handler key when the conponent is visible
     *
     * @param keyCode, key code
     * @param event
     *
     * @return true if key is handled and other other component should not handle it again, others is false.
     */
    public boolean KeyHandler(int keyCode, KeyEvent event);

    /**
     * this method is used to initial the view resource of the component
     *
     * @return true if the flow is success, others is false.
     */
    public boolean initView();

    /**
     * this method is used to start component, , should be called only when the TurnkeyUiMainActivity is resumed
     *
     * @return true if the flow is success, others is false.
     */
    public boolean startComponent();

    /**
     * this method is used to destroy the view resource of the component
     *
     * @return true if the flow is success, others is false.
     */
    public boolean deinitView();
}
