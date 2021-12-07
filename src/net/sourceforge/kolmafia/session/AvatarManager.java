package net.sourceforge.kolmafia.session;

import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;

public class AvatarManager {
  public static final void handleAfterAvatar(final int decision) {
    String newClass = "Unknown";
    switch (decision) {
      case 1:
        newClass = "Seal Clubber";
        break;
      case 2:
        newClass = "Turtle Tamer";
        break;
      case 3:
        newClass = "Pastamancer";
        break;
      case 4:
        newClass = "Sauceror";
        break;
      case 5:
        newClass = "Disco Bandit";
        break;
      case 6:
        newClass = "Accordion Thief";
        break;
    }

    String message = "Now walking on the " + newClass + " road.";

    KoLmafia.updateDisplay(message);
    RequestLogger.updateSessionLog(message);

    KoLmafia.resetAfterAvatar();
  }
}
