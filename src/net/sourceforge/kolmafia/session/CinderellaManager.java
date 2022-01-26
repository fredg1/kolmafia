package net.sourceforge.kolmafia.session;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.utilities.StringUtilities;

public class CinderellaManager {
  private static final Pattern CINDERELLA_TIME_PATTERN =
      Pattern.compile("<i>It is (\\d+) minute(?:s) to midnight.</i>");
  private static final Pattern CINDERELLA_SCORE_PATTERN =
      Pattern.compile("score (?:is now|was) <b>(\\d+)</b>");

  private CinderellaManager() {}

  public static final void postChoice2(final GenericRequest request) {
    // The Prince's Ball
    if (parseCinderellaTime() == false) {
      Preferences.decrement("cinderellaMinutesToMidnight");
    }
    Matcher matcher = CINDERELLA_SCORE_PATTERN.matcher(request.responseText);
    if (matcher.find()) {
      int score = StringUtilities.parseInt(matcher.group(1));
      if (score != -1) {
        Preferences.setInteger("cinderellaScore", score);
      }
    }
    if (request.responseText.contains("Your final score was")) {
      Preferences.setInteger("cinderellaMinutesToMidnight", 0);
      Preferences.setString("grimstoneMaskPath", "");
    }
  }

  public static final boolean parseCinderellaTime() {
    Matcher matcher = CINDERELLA_TIME_PATTERN.matcher(ChoiceManager.lastResponseText);
    while (matcher.find()) {
      int time = StringUtilities.parseInt(matcher.group(1));
      if (time != -1) {
        Preferences.setInteger("cinderellaMinutesToMidnight", time);
        return true;
      }
    }
    return false;
  }
}
