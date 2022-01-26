package net.sourceforge.kolmafia.session;

import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.persistence.AdventureDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;

public class LightsOutManager {
  private LightsOutManager() {}

  public static void checkCounter() {
    if (!Preferences.getBoolean("trackLightsOut")) {
      return;
    }

    if (TurnCounter.isCounting("Spookyraven Lights Out")) {
      return;
    }

    if (Preferences.getString("nextSpookyravenElizabethRoom").equals("none")
        && Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
      return;
    }

    int turns = 37 - (KoLCharacter.getTurnsPlayed() % 37);
    TurnCounter.startCounting(turns, "Spookyraven Lights Out", "bulb.gif");
  }

  public static boolean lightsOutNow() {
    int totalTurns = KoLCharacter.getTurnsPlayed();
    return totalTurns % 37 == 0 && Preferences.getInteger("lastLightsOutTurn") != totalTurns;
  }

  public static void report() {
    String elizabethRoom = Preferences.getString("nextSpookyravenElizabethRoom");
    if (elizabethRoom.equals("none")) {
      RequestLogger.printLine("You have defeated Elizabeth Spookyraven");
    } else {
      RequestLogger.printLine("Elizabeth will next show up in " + elizabethRoom);
    }

    String stephenRoom = Preferences.getString("nextSpookyravenStephenRoom");
    if (stephenRoom.equals("none")) {
      RequestLogger.printLine("You have defeated Stephen Spookyraven");
    } else {
      RequestLogger.printLine("Stephen will next show up in " + stephenRoom);
    }
  }

  public static String message() {
    return LightsOutManager.message(false);
  }

  public static String message(boolean link) {
    String msg = "";
    String elizabethRoom = Preferences.getString("nextSpookyravenElizabethRoom");
    String stephenRoom = Preferences.getString("nextSpookyravenStephenRoom");
    if (!elizabethRoom.equals("none")) {
      if (link) {
        String url = AdventureDatabase.getAdventure(elizabethRoom).getRequest().getURLString();
        elizabethRoom = "<a href=\"" + url + "\">" + elizabethRoom + "</a>";
      }
      msg += "Elizabeth can be found in " + elizabethRoom + ".  ";
    }
    if (!stephenRoom.equals("none")) {
      if (link) {
        String url = AdventureDatabase.getAdventure(stephenRoom).getRequest().getURLString();
        stephenRoom = "<a href=\"" + url + "\">" + stephenRoom + "</a>";
      }
      msg += "Stephen can be found in " + stephenRoom + ".  ";
    }

    return msg;
  }

  public static String lightsOutAutomation(final int choice, final String responseText) {
    int automation = Preferences.getInteger("lightsOutAutomation");
    if (automation == 0) {
      return "0";
    }
    switch (choice) {
      case 890:
        if (automation == 1 && responseText.contains("Look Out the Window")) {
          return "3";
        }
        return "1";
      case 891:
        if (automation == 1 && responseText.contains("Check a Pile of Stained Sheets")) {
          return "3";
        }
        return "1";
      case 892:
        if (automation == 1 && responseText.contains("Inspect the Bathtub")) {
          return "3";
        }
        return "1";
      case 893:
        if (automation == 1 && responseText.contains("Make a Snack")) {
          return "4";
        }
        return "1";
      case 894:
        if (automation == 1 && responseText.contains("Go to the Children's Section")) {
          return "2";
        }
        return "1";
      case 895:
        if (automation == 1 && responseText.contains("Dance with Yourself")) {
          return "2";
        }
        return "1";
      case 896:
        if (automation == 1
            && responseText.contains("Check out the Tormented Damned Souls Painting")) {
          return "4";
        }
        return "1";
      case 897:
        if (responseText.contains("Search for a light")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Search a nearby nightstand")) {
          return "3";
        }
        if (responseText.contains("Check a nightstand on your left")) {
          return "1";
        }
        return "2";
      case 898:
        if (responseText.contains("Search for a lamp")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Search over by the (gaaah) stuffed animals")) {
          return "2";
        }
        if (responseText.contains("Examine the Dresser")) {
          return "2";
        }
        if (responseText.contains("Open the bear and put your hand inside")) {
          return "1";
        }
        if (responseText.contains("Unlock the box")) {
          return "1";
        }
        return "2";
      case 899:
        if (responseText.contains("Make a torch")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Examine the Graves")) {
          return "2";
        }
        if (responseText.contains("Examine the grave marked \"Crumbles\"")) {
          return "2";
        }
        return "2";
      case 900:
        if (responseText.contains("Search for a light")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("What the heck, let's explore a bit")) {
          return "2";
        }
        if (responseText.contains("Examine the taxidermy heads")) {
          return "2";
        }
        return "2";
      case 901:
        if (responseText.contains("Try to find a light")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Keep your cool")) {
          return "2";
        }
        if (responseText.contains("Investigate the wine racks")) {
          return "2";
        }
        if (responseText.contains("Examine the Pinot Noir rack")) {
          return "3";
        }
        return "2";
      case 902:
        if (responseText.contains("Look for a light")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Search the barrel")) {
          return "2";
        }
        if (responseText.contains("No, but I will anyway")) {
          return "2";
        }
        return "2";
      case 903:
        if (responseText.contains("Search for a light")) {
          return automation == 1 ? "1" : "2";
        }
        if (responseText.contains("Check it out")) {
          return "1";
        }
        if (responseText.contains("Examine the weird machines")) {
          return "3";
        }
        if (responseText.contains("Enter 23-47-99 and turn on the machine")) {
          return "1";
        }
        if (responseText.contains("Oh god")) {
          return "1";
        }
        return "2";
    }
    return "2";
  }
}
