package net.sourceforge.kolmafia.session;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.QuestLogRequest;

public class ShenCopperheadManager {
  private static final Pattern SHEN_PATTERN =
      Pattern.compile(
          "(?:Bring me|artifact known only as) <b>(.*?)</b>, hidden away for centuries");

  public static final void parseInitialShen(final GenericRequest request) {
    Matcher matcher = SHEN_PATTERN.matcher(request.responseText);
    if (matcher.find()) {
      Preferences.setString("shenQuestItem", matcher.group(1));
    }
  }

  public static final void postInitialShen(final GenericRequest request) {
    QuestDatabase.setQuestProgress(Quest.SHEN, "step1");
    Preferences.setInteger("shenInitiationDay", KoLCharacter.getCurrentDays());
    if (Preferences.getString("shenQuestItem") == "") {
      // We didn't recognize quest request.responseText before accepting quest, so get it from quest
      // log
      RequestThread.postRequest(new QuestLogRequest());
    }
  }

  public static final void parseShenDemand(final GenericRequest request) {
    Matcher matcher = SHEN_PATTERN.matcher(request.responseText);
    if (matcher.find()) {
      Preferences.setString("shenQuestItem", matcher.group(1));
    }

    parseShenExchange();
  }

  public static final void parseShenFinal(final GenericRequest request) {
    Preferences.setString("shenQuestItem", "");

    parseShenExchange();
  }

  private static final void parseShenExchange() {
    QuestDatabase.advanceQuest(Quest.SHEN);

    // You will have exactly one of these items to ger rid of
    ResultProcessor.removeItem(ItemPool.FIRST_PIZZA);
    ResultProcessor.removeItem(ItemPool.LACROSSE_STICK);
    ResultProcessor.removeItem(ItemPool.EYE_OF_THE_STARS);
    ResultProcessor.removeItem(ItemPool.STANKARA_STONE);
    ResultProcessor.removeItem(ItemPool.MURPHYS_FLAG);
    ResultProcessor.removeItem(ItemPool.SHIELD_OF_BROOK);
  }
}
