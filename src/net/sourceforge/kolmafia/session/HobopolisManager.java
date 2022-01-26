package net.sourceforge.kolmafia.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.AscensionClass;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.EquipmentRequest;
import net.sourceforge.kolmafia.request.GenericRequest;

public class HobopolisManager {
  private static final HashMap<AscensionClass, AdventureResult> classInstruments = new HashMap<>();

  static {
    classInstruments.put(AscensionClass.SEAL_CLUBBER, ItemPool.get(ItemPool.SEALSKIN_DRUM));
    classInstruments.put(AscensionClass.TURTLE_TAMER, ItemPool.get(ItemPool.WASHBOARD_SHIELD));
    classInstruments.put(AscensionClass.PASTAMANCER, ItemPool.get(ItemPool.SPAGHETTI_BOX_BANJO));
    classInstruments.put(AscensionClass.SAUCEROR, ItemPool.get(ItemPool.MARINARA_JUG));
    classInstruments.put(AscensionClass.DISCO_BANDIT, ItemPool.get(ItemPool.MAKESHIFT_CASTANETS));
    classInstruments.put(
        AscensionClass.ACCORDION_THIEF, ItemPool.get(ItemPool.LEFT_HANDED_MELODICA));
  }

  private static final HashMap<AscensionClass, String> performanceMessages = new HashMap<>();

  static {
    performanceMessages.put(AscensionClass.SEAL_CLUBBER, "vigorously beating a sealskin drum");
    performanceMessages.put(AscensionClass.TURTLE_TAMER, "playing a washboard");
    performanceMessages.put(AscensionClass.PASTAMANCER, "plinking away at a spaghetti-box banjo");
    performanceMessages.put(AscensionClass.SAUCEROR, "huffing and puffing on a marinara jug");
    performanceMessages.put(AscensionClass.DISCO_BANDIT, "playing a pair of castanets");
    performanceMessages.put(AscensionClass.ACCORDION_THIEF, "toodling out a melody on a melodica");
  }

  private static LinkedList<AscensionClass> missingPerformers;
  private static int performerCount;

  private HobopolisManager() {}

  public static final void checkHoboBoss(final int decision, final String bossName) {
    // Stop for Hobopolis bosses
    if (decision == 2 && KoLmafia.isAdventuring()) {
      KoLmafia.updateDisplay(MafiaState.PENDING, bossName + " waits for you.");
    }
  }

  public static final void checkDungeonSewers(final GenericRequest request, final int decision) {
    if (decision != 1) {
      return;
    }

    // Somewhat Higher and Mostly Dry
    // Disgustin' Junction
    // The Former or the Ladder

    String text = request.responseText;
    int explorations = 0;

    int dumplings = InventoryManager.getAccessibleCount(ItemPool.DUMPLINGS);
    int wads = InventoryManager.getAccessibleCount(ItemPool.SEWER_WAD);
    int oozeo = InventoryManager.getAccessibleCount(ItemPool.OOZE_O);
    int oil = InventoryManager.getAccessibleCount(ItemPool.OIL_OF_OILINESS);
    int umbrella = InventoryManager.getAccessibleCount(ItemPool.GATORSKIN_UMBRELLA);

    // You steel your nerves and descend into the darkened tunnel.
    if (!text.contains("You steel your nerves and descend into the darkened tunnel.")) {
      return;
    }

    // *** CODE TESTS ***

    // You flip through your code binder, and figure out that one
    // of the glyphs is code for 'shortcut', while the others are
    // the glyphs for 'longcut' and 'crewcut', respectively. You
    // head down the 'shortcut' tunnel.

    if (text.contains("'crewcut'")) {
      explorations += 1;
    }

    // You flip through your code binder, and gain a basic
    // understanding of the sign: "This ladder just goes in a big
    // circle. If you climb it you'll end up back where you
    // started." You continue down the tunnel, instead.

    if (text.contains("in a big circle")) {
      explorations += 3;
    }

    // You consult your binder and translate the glyphs -- one of
    // them says "This way to the Great Egress" and the other two
    // are just advertisements for Amalgamated Ladderage, Inc. You
    // head toward the Egress.

    if (text.contains("Amalgamated Ladderage")) {
      explorations += 5;
    }

    // *** ITEM TESTS ***

    // "How about these?" you ask, offering the fish some of your
    // unfortunate dumplings.
    if (text.contains("some of your unfortunate dumplings")) {
      // Remove unfortunate dumplings from inventory
      ResultProcessor.processItem(ItemPool.DUMPLINGS, -1);
      ++explorations;
      dumplings = InventoryManager.getAccessibleCount(ItemPool.DUMPLINGS);
      if (dumplings <= 0) {
        RequestLogger.printLine("That was your last unfortunate dumplings.");
      }
    }

    // Before you can ask him what kind of tribute he wants, you
    // see his eyes light up at the sight of your sewer wad.
    if (text.contains("the sight of your sewer wad")) {
      // Remove sewer wad from inventory
      ResultProcessor.processItem(ItemPool.SEWER_WAD, -1);
      ++explorations;
      wads = InventoryManager.getAccessibleCount(ItemPool.SEWER_WAD);
      if (wads <= 0) {
        RequestLogger.printLine("That was your last sewer wad.");
      }
    }

    // He finds a bottle of Ooze-O, and begins giggling madly. He
    // uncorks the bottle, takes a drink, and passes out in a heap.
    if (text.contains("He finds a bottle of Ooze-O")) {
      // Remove bottle of Ooze-O from inventory
      ResultProcessor.processItem(ItemPool.OOZE_O, -1);
      ++explorations;
      oozeo = InventoryManager.getAccessibleCount(ItemPool.OOZE_O);
      if (oozeo <= 0) {
        RequestLogger.printLine("That was your last bottle of Ooze-O.");
      }
    }

    // You grunt and strain, but you can't manage to get between
    // the bars. In a flash of insight, you douse yourself with oil
    // of oiliness (it takes three whole bottles to cover your
    // entire body) and squeak through like a champagne cork. Only
    // without the bang, and you're not made out of cork, and
    // champagne doesn't usually smell like sewage. Anyway. You
    // continue down the tunnel.
    if (text.contains("it takes three whole bottles")) {
      // Remove 3 bottles of oil of oiliness from inventory
      ResultProcessor.processItem(ItemPool.OIL_OF_OILINESS, -3);
      ++explorations;
      oil = InventoryManager.getAccessibleCount(ItemPool.OIL_OF_OILINESS);
      if (oil < 3) {
        RequestLogger.printLine("You have less than 3 bottles of oil of oiliness left.");
      }
    }

    // Fortunately, your gatorskin umbrella allows you to pass
    // beneath the sewage fall without incident. There's not much
    // left of the umbrella, though, and you discard it before
    // moving deeper into the tunnel.
    if (text.contains("your gatorskin umbrella allows you to pass")) {
      // Unequip gatorskin umbrella and discard it.

      ++explorations;
      AdventureResult item = ItemPool.get(ItemPool.GATORSKIN_UMBRELLA, 1);
      int slot = EquipmentManager.WEAPON;
      if (KoLCharacter.hasEquipped(item, EquipmentManager.WEAPON)) {
        slot = EquipmentManager.WEAPON;
      } else if (KoLCharacter.hasEquipped(item, EquipmentManager.OFFHAND)) {
        slot = EquipmentManager.OFFHAND;
      }

      EquipmentManager.setEquipment(slot, EquipmentRequest.UNEQUIP);

      AdventureResult.addResultToList(KoLConstants.inventory, item);
      ResultProcessor.processItem(ItemPool.GATORSKIN_UMBRELLA, -1);
      umbrella = InventoryManager.getAccessibleCount(item);
      if (umbrella > 0) {
        RequestThread.postRequest(new EquipmentRequest(item, slot));
      } else {
        RequestLogger.printLine("That was your last gatorskin umbrella.");
      }
    }

    // *** GRATE ***

    // Further into the sewer, you encounter a halfway-open grate
    // with a crank on the opposite side. What luck -- looks like
    // somebody else opened this grate from the other side!

    if (text.contains("somebody else opened this grate")) {
      explorations += 5;
    }

    // Now figure out how to say what happened. If the player wants
    // to stop if runs out of test items, generate an ERROR and
    // list the missing items in the status message. Otherwise,
    // simply tell how many explorations were accomplished.

    AdventureResult result =
        AdventureResult.tallyItem("sewer tunnel explorations", explorations, false);
    AdventureResult.addResultToList(KoLConstants.tally, result);

    MafiaState state = MafiaState.CONTINUE;
    String message = "+" + explorations + " Explorations";

    if (Preferences.getBoolean("requireSewerTestItems")) {
      ArrayList<String> missing = new ArrayList<>();

      if (dumplings < 1) {
        missing.add("unfortunate dumplings");
      }
      if (wads < 1) {
        missing.add("sewer wad");
      }
      if (oozeo < 1) {
        missing.add("bottle of Ooze-O");
      }
      if (oil < 3) {
        missing.add("oil of oiliness");
      }
      if (umbrella < 1) {
        missing.add("gatorskin umbrella");
      }
      if (!missing.isEmpty()) {
        state = MafiaState.ERROR;
        message += ", NEED: " + String.join(", ", missing);
      }
    }

    KoLmafia.updateDisplay(state, message);
  }

  public static final AdventureResult getClassInstrument(final AscensionClass KoLClass) {
    return classInstruments.get(KoLClass);
  }

  public static final void resetPerformers() {
    missingPerformers = null;
  }

  public static final LinkedList<AscensionClass> getMissingPerformers() {
    return missingPerformers;
  }

  public static final int getPerformerCount() {
    return performerCount;
  }

  public static final void parseTentSurvey(final String responseText) {
    missingPerformers = new LinkedList<>(performanceMessages.keySet());
    performerCount = 0;

    for (Entry<AscensionClass, String> entry : performanceMessages.entrySet()) {
      if (responseText.contains(entry.getValue())) {
        missingPerformers.remove(entry.getKey());
        performerCount++;
      }
    }
  }
}
