package net.sourceforge.kolmafia.session;

import net.sourceforge.kolmafia.preferences.Preferences;

public class MonorailManager {
  private MonorailManager() {}

  public static void resetMuffinOrder() {
    String muffinOrder = Preferences.getString("muffinOnOrder");
    if (muffinOrder.equals("blueberry muffin")
        || muffinOrder.equals("bran muffin")
        || muffinOrder.equals("chocolate chip muffin")) {
      Preferences.setString("muffinOnOrder", "earthenware muffin tin");
    }
  }
}
