// QMenuManager.aidl
package skyworth.skyworthlivetv.menu;

import java.util.Map;
// Declare any non-default types here with import statements

interface QMenuManager {
    boolean IsQMenuShown();
    void ShowQMenu(boolean isShown);
    void ShowQMenuByItem(in Map menuItems);
    void DismissPictureModeUserDialog();
}
