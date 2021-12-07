package net.sourceforge.kolmafia.session;

import java.util.Arrays;
import java.util.Map;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.MonsterDatabase;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure.DisplayType;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure.Option;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType;
import net.sourceforge.kolmafia.preferences.Preferences;

public class SeaMerkinOutpostManager {
  private static final int OPTIONS_PER_TENT = 3; // excluding "Nevermind"

  private static final char UNKNOWN = '0';
  private static final char NOT_HERE = 'X';
  private static final char MAYBE_HERE = '?';
  private static final char DEFINITELY_HERE = '!';

  private static final String MERKIN_BURGLAR = MonsterDatabase.findMonsterById(772).getName();
  private static final String MERKIN_RAIDER = MonsterDatabase.findMonsterById(771).getName();
  private static final String MERKIN_HEALER = MonsterDatabase.findMonsterById(773).getName();

  private static final Tent[] tents = {
    new Tent(313, MERKIN_BURGLAR), new Tent(314, MERKIN_RAIDER), new Tent(315, MERKIN_HEALER),
  };

  private static final class Tent {
    private final int choiceAdv;
    private final String ownerName;
    private final char[] options = new char[OPTIONS_PER_TENT];
    private boolean couldHaveStashBox;

    private Tent(int choiceAdv, String ownerName) {
      this.choiceAdv = choiceAdv;
      this.ownerName = ownerName;
    }

    private final boolean isFullySearched() {
      for (char c : this.options) {
        if (c != NOT_HERE) {
          return false;
        }
      }
      return true;
    }
  }

  // don't look, it's such a hack ;-;
  public static final void visitOutpostTent(
      final int choiceNumber,
      final Map<Integer, Option> options,
      final AdventureResult[] defaultItemPerOption,
      final DisplayType[] displayTypePerOption,
      final DisplayType stashboxDisplayType) {
    char[] layout = getOutpostLayout(choiceNumber);

    if (layout == null) {
      return;
    }

    for (int i = 0; i < OPTIONS_PER_TENT; i++) {
      Option option = options.get(i + 1);
      char currentChar = layout[i];
      AdventureResult item = defaultItemPerOption[i];
      DisplayType displayType = displayTypePerOption[i];

      if (currentChar == DEFINITELY_HERE) {
        option
            .text(">>>>> Mer-kin stashbox <<<<<")
            .attachItem(ItemPool.MERKIN_STASHBOX, 1, ProcessType.AUTO, stashboxDisplayType);
      } else if (currentChar == MAYBE_HERE) {
        option
            .text(item.getName() + ", maybe stashbox")
            .attachItem(item, ProcessType.AUTO, displayType)
            .attachItem(ItemPool.MERKIN_STASHBOX, 1, ProcessType.AUTO, stashboxDisplayType);
      } else if (currentChar == NOT_HERE) {
        option.text(item.getName()).attachItem(item, ProcessType.AUTO, displayType);
      }
      // there shouldn't be any "UNKNOWN" anymore, so treat those as errors.
    }
  }

  private static final char[] getOutpostLayout(final int choiceNumber) {
    if (InventoryManager.getCount(ItemPool.MERKIN_LOCKKEY) == 0
        || InventoryManager.getCount(ItemPool.MERKIN_STASHBOX) > 0) {
      // they must have already found the stashbox, so
      // it can't be anywhere anymore
      char[] result = new char[OPTIONS_PER_TENT];
      Arrays.fill(result, NOT_HERE);

      return result;
    }

    updateStashboxInfo();

    for (Tent tent : tents) {
      if (tent.choiceAdv == choiceNumber) {
        return tent.options;
      }
    }

    // bogus number?
    return null;
  }

  private static final void updateStashboxInfo() {
    updateSearches();

    // at this point, every Tent's options should be
    // either '0' or 'X' depending on if the option was
    // tried or not.  Now, update the '0's to be '?' or
    // '!' if they may have the stash, or is sure to have
    // it, respectively
    int possibleStashes = 0;

    for (Tent tent : tents) {
      for (int i = 0; i < tent.options.length; i++) {
        if (tent.couldHaveStashBox) {
          if (tent.options[i] == UNKNOWN) {
            tent.options[i] = MAYBE_HERE;
            possibleStashes++;
          }
        } else {
          tent.options[i] = NOT_HERE;
        }
      }
    }

    if (possibleStashes == 1) {
      // there was only 1, that must be it!
      for (Tent tent : tents) {
        for (int i = 0; i < tent.options.length; i++) {
          if (tent.options[i] == MAYBE_HERE) {
            tent.options[i] = DEFINITELY_HERE;
          } else {
            tent.options[i] = NOT_HERE;
          }
        }
      }
    }
  }

  private static final void updateSearches() {
    loadSearches();

    String lockkeyMonster = Preferences.getString("merkinLockkeyMonster");

    for (Tent tent : tents) {
      if (!tent.ownerName.equals(lockkeyMonster)) {
        continue;
      }

      tent.couldHaveStashBox = !tent.isFullySearched();

      if (tent.couldHaveStashBox) {
        for (Tent otherTent : tents) {
          if (tent == otherTent) {
            continue;
          }

          otherTent.couldHaveStashBox = false;
        }
      } else {
        // this isn't good; the user tried every
        // option in the tent we thought had the
        // stashbox, but found nothing
        for (Tent otherTent : tents) {
          if (tent == otherTent) {
            continue;
          }

          otherTent.couldHaveStashBox = !otherTent.isFullySearched();
        }
      }

      return;
    }

    // if we reach this, merkinLockkeyMonster is either
    // an empty string, or something we didn't recognize.
    for (Tent tent : tents) {
      tent.couldHaveStashBox = !tent.isFullySearched();
    }
  }

  private static final void loadSearches() {
    String searches = loadSearchesPreference();

    for (int tentNumber = 0; tentNumber < tents.length; tentNumber++) {
      int offset = tentNumber * OPTIONS_PER_TENT;

      for (int optionNumber = 0; optionNumber < OPTIONS_PER_TENT; optionNumber++) {
        tents[tentNumber].options[optionNumber] = searches.charAt(offset + optionNumber);
      }
    }
  }

  private static final String loadSearchesPreference() {
    String searches = Preferences.getString("merkinStashboxSearches");

    int expected_length = tents.length * OPTIONS_PER_TENT;
    if (searches.length() != expected_length) {
      // got messed with
      searches = SeaMerkinOutpostManager.resetSearches();
    }

    return searches;
  }

  public static final String resetSearches() {
    int expected_length = tents.length * OPTIONS_PER_TENT;

    char[] newValue = new char[expected_length];
    for (int i = 0; i < expected_length; i++) {
      newValue[i] = UNKNOWN;
    }

    String result = String.valueOf(newValue);

    Preferences.setString("merkinStashboxSearches", result);

    return result;
  }

  public static final void optionTried(final int choiceNumber, final int decision) {
    if (choiceNumber < 313 || choiceNumber > 315) {
      return;
    }

    StringBuffer searches = new StringBuffer(loadSearchesPreference());

    int offset = (choiceNumber - 313) * OPTIONS_PER_TENT;
    int index = offset + decision - 1;

    searches.setCharAt(index, NOT_HERE);

    Preferences.setString("merkinStashboxSearches", searches.toString());
  }
}
