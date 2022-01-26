package net.sourceforge.kolmafia.session;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.utilities.StringUtilities;

public abstract class SpacegateManager {
  private static final Pattern FLUENCY_PATTERN = Pattern.compile("Fluency is now (\\d+)%");

  private SpacegateManager() {}

  public static void parseLanguageFluency(final String text, final String setting) {
    Matcher m = FLUENCY_PATTERN.matcher(text);
    if (m.find()) {
      Preferences.setInteger(setting, StringUtilities.parseInt(m.group(1)));
    }
  }
}
