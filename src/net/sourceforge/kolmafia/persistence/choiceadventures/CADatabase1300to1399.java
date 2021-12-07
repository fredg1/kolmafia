package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.Modifiers;
import net.sourceforge.kolmafia.Modifiers.Modifier;
import net.sourceforge.kolmafia.Modifiers.ModifierList;
import net.sourceforge.kolmafia.RequestEditorKit;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.combat.MonsterStatusTracker;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.persistence.MonsterDatabase.Phylum;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.persistence.SkillDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.AdventureRequest;
import net.sourceforge.kolmafia.request.ApiRequest;
import net.sourceforge.kolmafia.request.BeachCombRequest;
import net.sourceforge.kolmafia.request.FightRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.LatteRequest;
import net.sourceforge.kolmafia.request.QuestLogRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.BanishManager;
import net.sourceforge.kolmafia.session.BeachManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.ResponseTextParser;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.TurnCounter;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase1300to1399 extends ChoiceAdventureDatabase {
  final void add1300to1399() {
    new ChoiceAdventure(1300, "Just Vamping", "Duke Vampire's Chateau") {
      void setup() {
        new Option(1, "fight Duke Vampire");
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight Duke Vampire (with 250%+ init and Poison for Blood)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "Duke Vampire's Chateau,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1301, "Now You've Spied Her", "The Spider Queen's Lair") {
      void setup() {
        new Option(1, "fight Spider Queen");
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight Spider Queen (with 500+ mox and Fantastic Immunity)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Spider Queen's Lair,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1302, "Don't Be Arch", "The Archwizard's Tower") {
      void setup() {
        new Option(1, "fight Archwizard")
            .attachItem(ItemPool.FR_CHARGED_ORB, -1, MANUAL, new NoDisplay());
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight Archwizard (with 5+ cold res and charged druidic orb)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Archwizard's Tower,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1303, "Ley Lady Ley", "The Ley Nexus") {
      void setup() {
        new Option(1, "fight Ley Incursion")
            .attachItem(ItemPool.FR_CHESWICKS_COMPASS, -1, MANUAL, new NoDisplay());
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(
            1, "fight Ley Incursion (with 500+ mys and Cheswick Copperbottom's compass)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Ley Nexus,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(
        1304, "He Is the Ghoul King, He Can Do Anything", "The Ghoul King's Catacomb") {
      void setup() {
        new Option(1, "fight Ghoul King");
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight Ghoul King (with 5+ spooky res and Fantasy Faerie Blessing)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Ghoul King's Catacomb,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1305, "The Brogre's Progress", "The Ogre Chieftain's Keep") {
      void setup() {
        new Option(1, "fight Ogre Chieftain")
            .attachItem(ItemPool.FR_POISONED_SMORE, -1, MANUAL, new NoDisplay());
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight Ogre Chieftain (with 500+ mus and poisoned druidic s'more)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Ogre Chieftain's Keep,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new UnknownChoiceAdventure(1306);

    new ChoiceAdventure(1307, "It Takes a Thief", "The Master Thief's Chalet") {
      void setup() {
        new Option(1, "fight Ted Schwartz, Master Thief")
            .attachItem(ItemPool.FR_NOTARIZED_WARRANT, -1, MANUAL, new NoDisplay());
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(
            1,
            "fight Ted Schwartz, Master Thief (with 5+ sleaze res and notarized arrest warrant)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Master Thief's Chalet,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1308, "On a Downtown Train", null) {
      final Pattern MUFFIN_TYPE_PATTERN =
          Pattern.compile("Looks like your order for a (.*? muffin) is not yet ready");
      final Pattern CHOICEFORM_PATTERN =
          Pattern.compile("name=choiceform\\d+(.*?)</form>", Pattern.DOTALL);

      void setup() {
        this.neverEntersQueue = true;
        this.isSuperlikely = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
      }

      // place.php?whichplace=monorail&action=monorail_downtown
      //
      // Everything you do at this location uses this choice
      // adventure #. As you select options, you stay in the
      // same choice, but the available options change.
      //
      // We must deduce what is happening by looking at the
      // response text

      @Override
      void visitChoice(GenericRequest request) {
        // breakfast counter start

        // Looks like your order for a <muffin type> muffin is not yet ready.
        Matcher muffinMatcher = MUFFIN_TYPE_PATTERN.matcher(request.responseText);
        if (muffinMatcher.find()) {
          Preferences.setBoolean("_muffinOrderedToday", true);
          Preferences.setString("muffinOnOrder", muffinMatcher.group(1));
        } else if (request.responseText.contains("you placed your order a lifetime ago")
            // You spot your order from the other day, neatly labelled on the pickup shelf.
            || request.responseText.contains("You spot your order from the other day")
            // (If we have an earthenware muffin tin and the "order" buttons are present)
            || request.responseText.contains("Order a blueberry muffin")) {
          // "Sorry, you placed your order a lifetime ago, so we had to throw out the actual baked
          // good. Here's your earthenware cookware.
          Preferences.setString("muffinOnOrder", "none");
        }

        // breakfast counter end

        // Lazy! They're lazy, I tell you! (or evil)
        // Options in On A Downtown Train are dynamically assigned to values
        // rather than being bound to them, so it's literally impossible to
        // know what each choice will do without parsing them and reading their value.

        // We'll have to imitate RequestEditorKit.addChoiceSpoilers

        Matcher matcher = CHOICEFORM_PATTERN.matcher(request.responseText);

        while (matcher.find()) {
          String currentSection = matcher.group(1);
          Matcher optionMatcher = RequestEditorKit.OPTION_PATTERN.matcher(currentSection);
          if (!optionMatcher.find()) { // this wasn't actually a choice option - strange!
            continue;
          }

          Matcher buttonTextMatcher = RequestEditorKit.BUTTON_TEXT_PATTERN.matcher(currentSection);
          if (!buttonTextMatcher.find()) { // no... button? a blank one, maybe? weird!
            continue;
          }

          String buttonText = buttonTextMatcher.group(1);
          int choiceNumber = StringUtilities.parseInt(optionMatcher.group(1));

          Option option = getOption(choiceNumber);

          switch (buttonText) {

              // breakfast counter start

            case "Exchange 10 shovelfuls of dirt and 10 hunks of granite for an earthenware muffin tin!":
              option
                  .attachItem(ItemPool.SHOVELFUL_OF_DIRT, -10, MANUAL)
                  .attachItem(ItemPool.HUNK_OF_GRANITE, -10, MANUAL)
                  .attachItem(ItemPool.EARTHENWARE_MUFFIN_TIN, 1, AUTO);
              break;
            case "Order a blueberry muffin":
              option
                  .attachItem(ItemPool.BLUEBERRY_MUFFIN, 1, AUTO)
                  .attachItem(ItemPool.EARTHENWARE_MUFFIN_TIN, -1, MANUAL, new NoDisplay());
              break;
            case "Order a bran muffin":
              option
                  .attachItem(ItemPool.BRAN_MUFFIN, 1, AUTO)
                  .attachItem(ItemPool.EARTHENWARE_MUFFIN_TIN, -1, MANUAL, new NoDisplay());
              break;
            case "Order a chocolate chip muffin":
              option
                  .attachItem(ItemPool.CHOCOLATE_CHIP_MUFFIN, 1, AUTO)
                  .attachItem(ItemPool.EARTHENWARE_MUFFIN_TIN, -1, MANUAL, new NoDisplay());
              break;
            case "Back to the Platform!":
              // What do we do? "leadsTo( 1308 )" ? That's still us!
              break;

              // breakfast counter end

          }
        }
      }
    };

    new UnknownChoiceAdventure(1309);

    new ChoiceAdventure(1310, "Granted a Boon", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void registerDeferredChoice() {
        // Boon after fight, location is currently null, so don't log under that name
        RequestLogger.registerLocation("God Lobster");
      }
    };

    new ChoiceAdventure(1311, "You Love Gs", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1312, "Choose a Soundtrack", "Item-Driven") {
      final Pattern BOOMBOX_PATTERN = Pattern.compile("you can do <b>(\\d+)</b> more");
      final Pattern BOOMBOX_SONG_PATTERN =
          Pattern.compile("&quot;(.*?)&quot;( \\(Keep playing\\)|)");

      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = BOOMBOX_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("_boomBoxSongsLeft", matcher.group(1));
        }
        Preferences.setString("boomBoxSong", "");
        matcher = BOOMBOX_SONG_PATTERN.matcher(request.responseText);
        while (matcher.find()) {
          if (matcher.group(2) != null && matcher.group(2).contains("Keep playing")) {
            Preferences.setString("boomBoxSong", matcher.group(1));
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (!request.responseText.contains("decide not to change the station")) {
          String songChosen = "";
          switch (decision) {
            case 1:
              songChosen = "Eye of the Giger";
              break;
            case 2:
              songChosen = "Food Vibrations";
              break;
            case 3:
              songChosen = "Remainin' Alive";
              break;
            case 4:
              songChosen = "These Fists Were Made for Punchin'";
              break;
            case 5:
              songChosen = "Total Eclipse of Your Meat";
              break;
          }
          if (!songChosen.equals("")) {
            if (!KoLCharacter.hasSkill("Sing Along")) {
              KoLCharacter.addAvailableSkill("Sing Along");
            }
            if (!Preferences.getString("boomBoxSong").equals(songChosen)) {
              Preferences.setString("boomBoxSong", songChosen);
              Preferences.decrement("_boomBoxSongsLeft");
              String message = "Setting soundtrack to " + songChosen;
              RequestLogger.printLine(message);
              RequestLogger.updateSessionLog(message);
            }
          } else {
            if (KoLCharacter.hasSkill("Sing Along")) {
              KoLCharacter.removeAvailableSkill("Sing Along");
            }
            if (!Preferences.getString("boomBoxSong").equals("")) {
              Preferences.setString("boomBoxSong", "");
              String message = "Switching soundtrack off";
              RequestLogger.printLine(message);
              RequestLogger.updateSessionLog(message);
            }
          }
        }
      }
    };

    new ChoiceAdventure(1313, "Bastille Battalion", "Item-Driven") {
      void setup() {
        new Option(1); // top left, change barbican
        new Option(2); // top right, change drawbridge
        new Option(3); // bottom right, change murder hole
        new Option(4); // bottom left, change moat
        new Option(5); // start
        new Option(6); // high scores
        new Option(8); // exit
      }

      @Override
      void visitChoice(GenericRequest request) {
        // barb1=barbarian barbecue
        // barb2=babar
        // barb3=barbershop
        // bridge1=brutalist
        // bridge2=draftsman
        // bridge3=art nouveau
        // holes1=cannon
        // holes2=catapult
        // holes3=gesture
        // moat1=sharks
        // moat2=lava
        // moat3=truth serum
        if (!request.responseText.contains("option=5")) {
          Preferences.setInteger("_bastilleGames", 5);
        }
      }
    };

    new ChoiceAdventure(1314, "Bastille Battalion (Master of None)", "Item-Driven") {
      void setup() {
        new Option(1).leadsTo(1317);
        new Option(2).leadsTo(1318);
        new Option(3).leadsTo(1319);
      }
    };

    new ChoiceAdventure(1315, "Castle vs. Castle", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(1316, "GAME OVER", "Item-Driven") {
      final Pattern BASTILLE_PATTERN = Pattern.compile("You can play <b>(\\d+)</b>");

      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = BASTILLE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger("_bastilleGames", 5 - StringUtilities.parseInt(matcher.group(1)));
        }
      }
    };

    new ChoiceAdventure(1317, "A Hello to Arms (Battalion)", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(1318, "Defensive Posturing", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(1319, "Cheese Seeking Behavior", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(1320, "A Heist!", "Item-Driven") {
      final Pattern HEIST_PATTERN =
          Pattern.compile("He shows you a list of potential targets:<p><i>\\((\\d+) more");
      // <p>From a basaltamander:<br><input type="submit" name="st:1799:8465" value="basaltamander
      // tongue" class="button"></p>
      // "st:<monster ID>:<item ID>"
      // choice.php?whichchoice=1320&pwd&option=1&st:1799:8465=basaltamander+tongue
      // choice.php?whichchoice=1320&pwd&option=1&st:1799:8465=

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("You didn't click a real thing.")) {
          Preferences.increment("_catBurglarHeistsComplete");
        }
      }
    };

    new ChoiceAdventure(1321, "Disguises Delimit", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1322, "The Beginning of the Neverend", "The Neverending Party") {
      void setup() {
        this.canWalkFromChoice = true;
        this.neverEntersQueue = true;

        this.customName = "Neverending Party Intro";

        new Option(1, "accept quest", true);
        new Option(2, "reject quest", true);
        new Option(6, "leave", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("PARTY! PARTY! PARTY! PARTY!")) {
          getOption(1).text("accept HARD quest");
        }

        if (request.responseText.contains("talk to him and help him get more booze")) {
          Preferences.setString("_questPartyFairQuest", "booze");
        } else if (request.responseText.contains("Think you can help me clean the place up?")) {
          Preferences.setString("_questPartyFairQuest", "trash");
        } else if (request.responseText.contains(
            "helping her with whatever problem she's having with the snacks")) {
          Preferences.setString("_questPartyFairQuest", "food");
        } else if (request.responseText.contains("megawoots right now")) {
          Preferences.setString("_questPartyFairQuest", "woots");
        } else if (request.responseText.contains("taking up a collection from the guests")) {
          Preferences.setString("_questPartyFairQuest", "dj");
        } else if (request.responseText.contains("all of the people to leave")) {
          Preferences.setString("_questPartyFairQuest", "partiers");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        boolean hard = KoLCharacter.hasEquipped(ItemPool.PARTY_HARD_T_SHIRT);
        Preferences.setBoolean("_partyHard", hard);
        if (decision == 1) {
          // Decided to quest
          String quest = Preferences.getString("_questPartyFairQuest");
          if (quest.equals("booze") || quest.equals("food")) {
            QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, QuestDatabase.STARTED);
            Preferences.setString("_questPartyFairProgress", "");
          } else {
            QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step1");
            if (quest.equals("woots")) {
              Preferences.setInteger("_questPartyFairProgress", 10);
            } else if (quest.equals("partiers")) {
              Preferences.setInteger("_questPartyFairProgress", hard ? 100 : 50);
            } else if (quest.equals("dj")) {
              Preferences.setInteger("_questPartyFairProgress", hard ? 10000 : 5000);
            } else if (quest.equals("trash")) {
              // The amount isn't known, so check quest log
              (new GenericRequest("questlog.php?which=1")).run();
            }
          }
        } else if (decision == 2) {
          // Decided to party
          Preferences.setString("_questPartyFair", "");
          Preferences.setString("_questPartyFairQuest", "");
          Preferences.setString("_questPartyFairProgress", "");
        }
      }
    };

    new ChoiceAdventure(1323, "All Done!", "The Neverending Party") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, QuestDatabase.FINISHED);
        Preferences.setString("_questPartyFairQuest", "");
        Preferences.setString("_questPartyFairProgress", "");
      }
    };

    new ChoiceAdventure(1324, "It Hasn't Ended, It's Just Paused", "The Neverending Party") {
      void setup() {
        this.customName = "Neverending Party Pause";

        new Option(1).leadsTo(1325, true, (Option o) -> true);
        new Option(2).leadsTo(1326, true, (Option o) -> true);
        new Option(3).leadsTo(1327, true, (Option o) -> true);
        new Option(4).leadsTo(1328, true, (Option o) -> true);
        new Option(5, "fight random partier", true);

        new CustomOption(1, "Bedroom");
        new CustomOption(2, "Kitchen");
        new CustomOption(3, "Back Yard");
        new CustomOption(4, "Basement");
      }
    };

    new ChoiceAdventure(1325, "A Room With a View...  Of a Bed", "The Neverending Party") {
      final Pattern SAFE_PATTERN = Pattern.compile("find ([\\d,]+) Meat in the safe");

      void setup() {
        this.customName = "Neverending Party Bedroom";

        new Option(1);
        new Option(2, "20 adv of Tomes of Opportunity").attachEffect("Tomes of Opportunity");
        new Option(3, "remove 30% of partiers")
            .attachItem(ItemPool.JAM_BAND_BOOTLEG, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(4, "get ~1000 meat for dj (with 300 Moxie)", true);
        new Option(5, "+20 megawoots")
            .attachItem(
                ItemPool.VERY_SMALL_RED_DRESS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(1, "full HP/MP heal");
        new CustomOption(2, "20 adv of +20% mys exp");
        new CustomOption(3, "remove 30% of partiers (with jam band bootleg)");
        new CustomOption(5, "+20 megawoots (with very small red dress)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent >= 10) {
          for (Option option : this.options.values()) {
            option.turnCost(1);
          }
        }

        if (request == null) {
          getOption(1).text(this.customOptions.get(1).displayText);

          String quest = Preferences.getString("_questPartyFairQuest");

          if (!quest.equals("partiers")) {
            getOption(3).reset();
          }

          if (!quest.equals("dj")) {
            getOption(4).reset();
          }

          if (!quest.equals("woots")) {
            getOption(5).reset();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent < 10) {
          Preferences.setInteger("_neverendingPartyFreeTurns", turnsSpent + 1);
        }

        if (decision == 3) {
          // Removes 30% of current partiers
          int current = Preferences.getInteger("_questPartyFairProgress");
          Preferences.setInteger(
              "_questPartyFairProgress", current - (int) Math.floor(current * 0.3));
        } else if (decision == 4) {
          // On dj quest (choice number guessed)
          Matcher matcher = SAFE_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            Preferences.decrement(
                "_questPartyFairProgress", StringUtilities.parseInt(matcher.group(1)), 0);
            if (Preferences.getInteger("_questPartyFairProgress") < 1) {
              QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step2");
            }
          }
        } else if (decision == 5) {
          // On woots quest
          Preferences.increment("_questPartyFairProgress", 20, 100, false);
          if (Preferences.getInteger("_questPartyFairProgress") == 100) {
            QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step2");
          }
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        StringUtilities.singleStringReplace(
            buffer,
            "hurry through the door to take your place.",
            "hurry through the door to take your place. ("
                + Preferences.getString("_questPartyFairProgress")
                + "/100 megawoots)");
        StringUtilities.singleStringReplace(
            buffer,
            "start complaining and then leave.",
            "start complaining and then leave. ("
                + Preferences.getString("_questPartyFairProgress")
                + " Partiers remaining)");
        StringUtilities.singleStringReplace(
            buffer,
            "contribute to the DJ's bill.",
            "contribute to the DJ's bill. ("
                + Preferences.getString("_questPartyFairProgress")
                + " Meat remaining)");
      }
    };

    new ChoiceAdventure(1326, "Gone Kitchin'", "The Neverending Party") {
      final Pattern GERALDINE_PATTERN =
          Pattern.compile("Geraldine wants (\\d+)<table>.*?descitem\\((\\d+)\\)");
      final Pattern TRASH_PATTERN = Pattern.compile("must have been (\\d+) pieces of trash");

      void setup() {
        this.customName = "Neverending Party Kitchen";

        new Option(1, "gain myst stats", true);
        new Option(2, "20 adv of Spiced Up").attachEffect("Spiced Up");
        new Option(3, "find out food to collect", true);
        new Option(4, "give collected food", true);
        new Option(5, "reduce trash by ~25%", true)
            .attachItem("gas can", -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(2, "20 adv of +20% Mus exp");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent >= 10) {
          for (Option option : this.options.values()) {
            option.turnCost(1);
          }
        }

        String quest = Preferences.getString("_questPartyFairQuest");
        if (quest.equals("food")) {
          String pref = Preferences.getString("_questPartyFairProgress");
          String itemIdString = null;
          int position = pref.indexOf(" ");
          if (position > 0) {
            itemIdString = pref.substring(position);
            if (itemIdString != null) {
              getOption(4)
                  .attachItem(
                      StringUtilities.parseInt(itemIdString),
                      -10,
                      MANUAL,
                      new DisplayAll(NEED, AT_LEAST, 10));
            }
          }
        }

        if (request == null) {
          if (!quest.equals("food")) {
            getOption(3).reset();
            getOption(4).reset();
          } else if (QuestDatabase.isQuestLaterThan(Quest.PARTY_FAIR, QuestDatabase.STARTED)) {
            getOption(3).reset();
          } else {
            getOption(4).reset();
          }

          if (!quest.equals("trash")) {
            getOption(5).reset();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent < 10) {
          Preferences.setInteger("_neverendingPartyFreeTurns", turnsSpent + 1);
        }

        if (decision == 3) {
          Matcher matcher = GERALDINE_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            int itemCount = StringUtilities.parseInt(matcher.group(1));
            int itemId = ItemDatabase.getItemIdFromDescription(matcher.group(2));
            Preferences.setString("_questPartyFairProgress", itemCount + " " + itemId);
            if (InventoryManager.getCount(itemId) >= itemCount) {
              QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step2");
            }
          }
          QuestDatabase.setQuestIfBetter(Quest.PARTY_FAIR, "step1");
        } else if (decision == 4) {
          QuestDatabase.setQuestIfBetter(Quest.PARTY_FAIR, QuestDatabase.FINISHED);
          Preferences.setString("_questPartyFairQuest", "");
          Preferences.setString("_questPartyFairProgress", "");
        } else if (decision == 5) {
          Matcher matcher = TRASH_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            Preferences.decrement(
                "_questPartyFairProgress", StringUtilities.parseInt(matcher.group(1)), 0);
          }
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        StringUtilities.singleStringReplace(
            buffer,
            "pieces of trash in that can!",
            "pieces of trash in that can! (~"
                + Preferences.getString("_questPartyFairProgress")
                + " pieces of trash remaining)");
      }
    };

    new ChoiceAdventure(1327, "Forward to the Back", "The Neverending Party") {
      final Pattern GERALD_PATTERN =
          Pattern.compile("Gerald wants (\\d+)<table>.*?descitem\\((\\d+)\\)");

      void setup() {
        this.customName = "Neverending Party Back Yard";

        new Option(1, "gain moxie stats", true);
        new Option(2, "50 adv of Citronella Armpits").attachEffect("Citronella Armpits");
        new Option(3, "find out booze to collect", true);
        new Option(4, "give collected booze", true);
        new Option(5, "remove 20% of partiers")
            .attachItem(
                ItemPool.PURPLE_BEAST_ENERGY_DRINK, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(2, "50 adv of +30 ML");
        new CustomOption(5, "remove 20% of partiers (with Purple Beast energy drink)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent >= 10) {
          for (Option option : this.options.values()) {
            option.turnCost(1);
          }
        }

        String quest = Preferences.getString("_questPartyFairQuest");
        if (quest.equals("booze")) {
          String pref = Preferences.getString("_questPartyFairProgress");
          String itemIdString = null;
          int position = pref.indexOf(" ");
          if (position > 0) {
            itemIdString = pref.substring(position);
            if (itemIdString != null) {
              int needed = Preferences.getBoolean("_partyHard") ? 20 : 10;
              getOption(4)
                  .attachItem(
                      StringUtilities.parseInt(itemIdString),
                      -needed,
                      MANUAL,
                      new DisplayAll(NEED, AT_LEAST, needed));
            }
          }
        }

        if (request == null) {
          if (!quest.equals("booze")) {
            getOption(3).reset();
            getOption(4).reset();
          } else if (QuestDatabase.isQuestLaterThan(Quest.PARTY_FAIR, QuestDatabase.STARTED)) {
            getOption(3).reset();
          } else {
            getOption(4).reset();
          }

          if (!quest.equals("partiers")) {
            getOption(5).reset();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent < 10) {
          Preferences.setInteger("_neverendingPartyFreeTurns", turnsSpent + 1);
        }

        if (decision == 3) {
          Matcher matcher = GERALD_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            int itemCount = StringUtilities.parseInt(matcher.group(1));
            int itemId = ItemDatabase.getItemIdFromDescription(matcher.group(2));
            Preferences.setString("_questPartyFairProgress", itemCount + " " + itemId);
            if (InventoryManager.getCount(itemId) >= itemCount) {
              QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step2");
            }
          }
          QuestDatabase.setQuestIfBetter(Quest.PARTY_FAIR, "step1");
        } else if (decision == 4) {
          QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, QuestDatabase.FINISHED);
          Preferences.setString("_questPartyFairQuest", "");
          Preferences.setString("_questPartyFairProgress", "");
        } else if (decision == 5) {
          // Removes 20% of current partiers
          int current = Preferences.getInteger("_questPartyFairProgress");
          Preferences.setInteger(
              "_questPartyFairProgress", current - (int) Math.floor(current * 0.2));
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        StringUtilities.singleStringReplace(
            buffer,
            "flees over the back fence.",
            "flees over the back fence. ("
                + Preferences.getString("_questPartyFairProgress")
                + " Partiers remaining)");
      }
    };

    new ChoiceAdventure(1328, "Basement Urges", "The Neverending Party") {
      void setup() {
        this.customName = "Neverending Party Basement";

        new Option(1, "gain muscle stats", true);
        new Option(2, "20 adv of The Best Hair You've Ever Had")
            .attachEffect("The Best Hair You've Ever Had");
        new Option(3, "acquire intimidating chainsaw", true)
            .attachItem(ItemPool.INTIMIDATING_CHAINSAW, 1, AUTO);
        new Option(4, "+20 megawoots")
            .attachItem(ItemPool.ELECTRONICS_KIT, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(2, "20 adv of +20% Mox exp");
        new CustomOption(4, "+20 megawoots (with electronics kit)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent >= 10) {
          for (Option option : this.options.values()) {
            option.turnCost(1);
          }
        }

        if (request == null) {
          if (InventoryManager.getAccessibleCount(ItemPool.INTIMIDATING_CHAINSAW) > 0) {
            getOption(3).reset();
          }

          String quest = Preferences.getString("_questPartyFairQuest");

          if (!quest.equals("woots")) {
            getOption(4).reset();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        int turnsSpent = Preferences.getInteger("_neverendingPartyFreeTurns");
        if (turnsSpent < 10) {
          Preferences.setInteger("_neverendingPartyFreeTurns", turnsSpent + 1);
        }

        if (decision == 4) {
          // On woots quest
          Preferences.increment("_questPartyFairProgress", 20, 100, false);
          if (Preferences.getInteger("_questPartyFairProgress") == 100) {
            QuestDatabase.setQuestProgress(Quest.PARTY_FAIR, "step2");
          }
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        StringUtilities.singleStringReplace(
            buffer,
            "burns the house down.",
            "burns the house down. ("
                + Preferences.getString("_questPartyFairProgress")
                + "/100 megawoots)");
      }
    };

    new ChoiceAdventure(1329, "Latte Shop", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        LatteRequest.parseVisitChoice(request.responseText);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // The item that we get has a procedurally-generated name
        request.setHasResult(false);
        LatteRequest.parseResponse(request.getURLString(), request.responseText);
      }
    };

    new UnknownChoiceAdventure(1330);

    new ChoiceAdventure(1331, "Daily Loathing Ballot", null) {
      final Pattern VOTE_PATTERN =
          Pattern.compile(
              "<label><input .*? value=\\\"(\\d)\\\" class=\\\"locals\\\" /> (.*?)<br /><span .*? color: blue\\\">(.*?)</span><br /></label>");
      final Pattern VOTE_SPEECH_PATTERN =
          Pattern.compile(
              "<p><input type='radio' name='g' value='(\\d+)' /> <b>(.*?)</b>(.*?)<br><blockquote>(.*?)</blockquote>");
      final Pattern URL_VOTE_PATTERN = Pattern.compile("local\\[\\]=(\\d)");

      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher localMatcher = VOTE_PATTERN.matcher(request.responseText);
        while (localMatcher.find()) {
          int voteValue = StringUtilities.parseInt(localMatcher.group(1)) + 1;
          String voteMod = Modifiers.parseModifier(localMatcher.group(3));
          if (voteMod != null) {
            Preferences.setString("_voteLocal" + voteValue, voteMod);
          }
        }

        int count = 1;

        Matcher platformMatcher = VOTE_SPEECH_PATTERN.matcher(request.responseText);
        while (platformMatcher.find()) {
          String party = platformMatcher.group(3);
          String speech = platformMatcher.group(4);

          String monsterName = parseVoteSpeech(party, speech);

          if (monsterName != null) {
            Preferences.setString("_voteMonster" + count, monsterName);
          }

          count++;
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        int count = 1;

        Matcher matcher = VOTE_SPEECH_PATTERN.matcher(buffer.toString());
        while (matcher.find()) {
          String find = matcher.group(0);
          String monsterName = Preferences.getString("_voteMonster" + count++);

          if (monsterName == "") {
            continue;
          }

          String replace =
              StringUtilities.singleStringReplace(
                  find,
                  "</blockquote>",
                  "<br />(vote for " + monsterName + " tomorrow)</blockquote>");
          StringUtilities.singleStringReplace(buffer, find, replace);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("must vote for a candidate")) {
          ModifierList modList = new ModifierList();
          Matcher matcher = URL_VOTE_PATTERN.matcher(request.getURLString());
          while (matcher.find()) {
            int vote = StringUtilities.parseInt(matcher.group(1)) + 1;
            String pref = Preferences.getString("_voteLocal" + vote);
            ModifierList addModList = Modifiers.splitModifiers(pref);
            for (Modifier modifier : addModList) {
              modList.addToModifier(modifier);
            }
          }
          Preferences.setString("_voteModifier", modList.toString());
          String message = "You have cast your votes";
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
        }
      }

      final String parseVoteSpeech(String party, String speech) {
        if (party.contains("Pork Elf Historical Preservation Party")) {
          if (speech.contains("strict curtailing of unnatural modern technologies")) {
            return "government bureaucrat";
          } else if (speech.contains("reintroduce Pork Elf DNA")) {
            return "terrible mutant";
          } else if (speech.contains("kingdom-wide seance")) {
            return "angry ghost";
          } else if (speech.contains("very interested in snakes")) {
            return "annoyed snake";
          } else if (speech.contains("lots of magical lard")) {
            return "slime blob";
          }
        } else if (party.contains("Clan Ventrilo")) {
          if (speech.contains("bringing this blessing to the entire population")) {
            return "slime blob";
          } else if (speech.contains("see your deceased loved ones again")) {
            return "angry ghost";
          } else if (speech.contains("stronger and more vigorous")) {
            return "terrible mutant";
          } else if (speech.contains("implement healthcare reforms")) {
            return "government bureaucrat";
          } else if (speech.contains("flavored drink in a tube")) {
            return "annoyed snake";
          }
        } else if (party.contains("Bureau of Efficient Government")) {
          if (speech.contains("graveyards are a terribly inefficient use of space")) {
            return "angry ghost";
          } else if (speech.contains("strictly enforced efficiency laws")) {
            return "government bureaucrat";
          } else if (speech.contains("distribute all the medications for all known diseases")) {
            return "terrible mutant";
          } else if (speech.contains("introduce an influx of snakes")) {
            return "annoyed snake";
          } else if (speech.contains("releasing ambulatory garbage-eating slimes")) {
            return "slime blob";
          }
        } else if (party.contains("Scions of Ich'Xuul'kor")) {
          if (speech.contains("increase awareness of our really great god")) {
            return "terrible mutant";
          } else if (speech.contains("hunt these evil people down")) {
            return "government bureaucrat";
          } else if (speech.contains("sound of a great hissing")) {
            return "annoyed snake";
          } else if (speech.contains("make things a little bit more like he's used to")) {
            return "slime blob";
          } else if (speech.contains("kindness energy")) {
            return "angry ghost";
          }
        } else if (party.contains("Extra-Terrific Party")) {
          if (speech.contains("wondrous chemical")) {
            return "terrible mutant";
          } else if (speech.contains("comprehensive DNA harvesting program")) {
            return "government bureaucrat";
          } else if (speech.contains("mining and refining processes begin")) {
            return "slime blob";
          } else if (speech.contains("warp engines will not destabilize")) {
            return "angry ghost";
          } else if (speech.contains("breeding pair of these delightful creatures")) {
            return "annoyed snake";
          }
        }

        return null;
      }
    };

    new ChoiceAdventure(1332, "government requisition form", "Item-Driven") {
      void setup() {
        // the text fields are just a farce; they aren't even sent with the URL
        new Option(1).attachItem(ItemPool.GOVERNMENT_REQUISITION_FORM, -1, MANUAL, new NoDisplay());
      }
    };

    new RetiredChoiceAdventure(1333, "Canadian cabin", "The Canadian Wildlife Preserve") {
      void setup() {
        this.customName = this.name;

        new Option(1, "50 adv of Long Winter's Napped")
            .turnCost(1)
            .attachEffect("Long Winter's Napped");
        new Option(2, "acquire grilled mooseflank (with mooseflank)", true)
            .turnCost(1)
            .attachItem("grilled mooseflank", 1, AUTO)
            .attachItem(ItemPool.MOOSEFLANK, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(3, "acquire antique Canadian lantern (with 10 thick walrus blubber)", true)
            .turnCost(1)
            .attachItem("antique Canadian lantern", 1, AUTO)
            .attachItem(ItemPool.WALRUS_BLUBBER, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10));
        new Option(4, "acquire muskox-skin cap (with 10 tiny bombs)", true)
            .turnCost(1)
            .attachItem("muskox-skin cap", 1, AUTO)
            .attachItem(ItemPool.TINY_BOMB, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10));
        new Option(5, "acquire antique beer (with Yeast-Hungry)", true)
            .turnCost(1)
            .attachItem("antique beer", 1, AUTO)
            .attachEffect("Yeast-Hungry");
        new Option(10, "skip adventure", true).entersQueue(false);

        new CustomOption(1, "50 adv of +100% weapon and spell damage");
      }
    };

    new ChoiceAdventure(1334, "Boxing Daycare (Lobby)", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1, "get a daily item");
        new Option(2, "get a daily 100 turns buff").leadsTo(1335);
        new Option(3).leadsTo(1336);
        new Option(4);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        // Special logging done elsewhere
        return true;
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("Boxing Daycare");
      }

      @Override
      String encounterName(String urlString, String responseText) {
        int urlOption = ChoiceManager.extractOptionFromURL(urlString);
        if (urlOption == 1) {
          // Have a Boxing Daydream
          return "Have a Boxing Daydream";
        }
        return null;
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (Preferences.getBoolean("_daycareNap")) {
          getOption(1).text("Already got an item today");
        }

        if (Preferences.getBoolean("_daycareSpa")) {
          getOption(2).reset("Already got a buff today");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("_daycareNap", true);
        } else if (decision == 2
            && request.responseText.contains("only allowed one spa treatment")) {
          Preferences.setBoolean("_daycareSpa", true);
        }
      }
    };

    new ChoiceAdventure(1335, "Boxing Day Spa", null) {
      void setup() {
        this.canWalkFromChoice = true;

        this.customName = this.name;
        this.customZones.add("Town");

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5).leadsTo(1334, true);

        new CustomOption(1, "100 adv of +200% muscle and +15 ML");
        new CustomOption(2, "100 adv of +200% moxie and +50% init");
        new CustomOption(3, "100 adv of +200% myst and +25% items");
        new CustomOption(
            4, "100 adv of +100 max hp, +50 max mp, +25 dr, 5-10 mp regen, 10-20 hp regen");
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        // Special logging done elsewhere
        return true;
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("Boxing Daycare");
      }

      @Override
      String encounterName(String urlString, String responseText) {
        int urlOption = ChoiceManager.extractOptionFromURL(urlString);
        if (urlOption >= 1 && urlOption <= 4) {
          // (Get a buff)
          return "Visit the Boxing Day Spa";
        }
        return null;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision != 5) {
          Preferences.setBoolean("_daycareSpa", true);
        }
      }
    };

    new ChoiceAdventure(1336, "Boxing Daycare", null) {
      final Pattern EARLY_DAYCARE_PATTERN =
          Pattern.compile("mostly empty. (.*?) toddlers are training with (.*?) instructor");
      final Pattern DAYCARE_PATTERN =
          Pattern.compile(
              "(?:Looks like|Probably around) (.*?) pieces in all. (.*?) toddlers are training with (.*?) instructor");
      final Pattern DAYCARE_RECRUITS_PATTERN =
          Pattern.compile("<font color=blue><b>\\[(.*?) Meat\\]</b></font>");
      final Pattern DAYCARE_RECRUIT_PATTERN = Pattern.compile("attract (.*?) new children");
      final Pattern DAYCARE_EQUIPMENT_COST_PATTERN =
          Pattern.compile("<font color=blue><b>\\[(free|\\d adventures?)\\]</b></font>");
      final Pattern DAYCARE_EQUIPMENT_PATTERN = Pattern.compile("manage to find (.*?) used");
      final Pattern DAYCARE_INSTRUCTOR_COST_PATTERN =
          Pattern.compile("Hire an instructor.*?<font color=blue><b>\\[(\\d+) (.*?)\\]</b></font>");
      final Pattern DAYCARE_ITEM_PATTERN =
          Pattern.compile(
              "<td valign=center>You lose an item: </td>.*?<b>(.*?)</b> \\((.*?)\\)</td>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).turnCost(1);
        new Option(5).leadsTo(1334, true);
        new Option(7);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        // Special logging done elsewhere
        return true;
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("Boxing Daycare");
      }

      @Override
      String encounterName(String urlString, String responseText) {
        int urlOption = ChoiceManager.extractOptionFromURL(urlString);
        if (urlOption >= 1 && urlOption <= 4) {
          // (recruit, scavenge, hire, spar)
          return "Enter the Boxing Daycare";
        }
        return null;
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = DAYCARE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("daycareEquipment", matcher.group(1).replaceAll(",", ""));
          Preferences.setString("daycareToddlers", matcher.group(2).replaceAll(",", ""));
          String instructors = matcher.group(3);
          if (instructors.equals("an")) {
            instructors = "1";
          }
          Preferences.setString("daycareInstructors", instructors);
        } else {
          matcher = EARLY_DAYCARE_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            Preferences.setString("daycareToddlers", matcher.group(1).replaceAll(",", ""));
            String instructors = matcher.group(2);
            if (instructors.equals("an")) {
              instructors = "1";
            }
            Preferences.setString("daycareInstructors", instructors);
          }
        }

        Matcher recruitsToday = DAYCARE_RECRUITS_PATTERN.matcher(request.responseText);
        if (recruitsToday.find()) {
          int recruitCostPowerOf10 = recruitsToday.group(1).replaceAll(",", "").length() - 1;
          getOption(1).attachMeat((int) Math.pow(10, recruitCostPowerOf10), AUTO);
          Preferences.setInteger("_daycareRecruits", recruitCostPowerOf10 - 2);
        }

        Matcher scavengeCostMatcher = DAYCARE_EQUIPMENT_COST_PATTERN.matcher(request.responseText);
        if (scavengeCostMatcher.find()) {
          String costString = scavengeCostMatcher.group(1);
          int cost =
              costString.equals("free")
                  ? 0
                  : StringUtilities.parseInt(String.valueOf(costString.charAt(0)));

          getOption(2).turnCost(cost);

          int savedScavenges = Preferences.getInteger("_daycareGymScavenges");

          if (savedScavenges != cost && !(cost == 3 && savedScavenges > cost)) {
            Preferences.setInteger("_daycareGymScavenges", cost);
          }
        }

        Matcher instructorCostMatcher =
            DAYCARE_INSTRUCTOR_COST_PATTERN.matcher(request.responseText);
        if (instructorCostMatcher.find()) {
          int amount = StringUtilities.parseInt(instructorCostMatcher.group(1));
          int itemId = ItemDatabase.getItemId(instructorCostMatcher.group(2), amount);

          if (itemId != -1) {
            // we'll make it AUTO and parse it again in postChoice1, for good measure
            getOption(3).attachItem(itemId, -amount, AUTO, new DisplayAll(NEED, AT_LEAST, amount));

            Preferences.setInteger("daycareInstructorCost", amount);
            Preferences.setString("daycareInstructorItem", ItemDatabase.getCanonicalName(itemId));
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // decision 1
        // the meat cost is AUTO, so no need to parse for failure
        if (decision == 2) {
          if (request.responseText.contains("don't have enough adventures")) {
            this.choiceFailed();
          }
        } else if (decision == 3) {
          Matcher matcher = DAYCARE_ITEM_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            String itemName = matcher.group(1);
            int itemCount = StringUtilities.parseInt(matcher.group(2).replaceAll(",", ""));
            getOption(decision).attachItem(itemName, -itemCount, MANUAL);
          }
        } else if (decision == 4) {
          if (request.responseText.contains("don't have enough adventures")) {
            this.choiceFailed();
          }
        } else if (decision == 7) {
          // You cross out the classified ad for that instructor, and
          // look for another one who (ideally) wants something else as compensation.

          // once per day only
          // instructor cost will be parsed in visitChoice
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String message1 = null;
        String message2 = null;
        if (decision == 1) {
          Matcher matcher = DAYCARE_RECRUIT_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            message1 = "Activity: Recruit toddlers";
            message2 = "You have recruited " + matcher.group(1) + " toddlers";
            Preferences.increment("_daycareRecruits");
          }
        } else if (decision == 2) {
          Matcher matcher = DAYCARE_EQUIPMENT_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            AdventureResult effect = EffectPool.get(EffectPool.BOXING_DAY_BREAKFAST, 0);
            boolean haveBreakfast = effect.getCount(KoLConstants.activeEffects) > 0;
            String countString = matcher.group(1);
            int equipment = StringUtilities.parseInt(countString.replaceAll(",", ""));
            Preferences.setInteger("daycareLastScavenge", equipment / (haveBreakfast ? 2 : 1));

            message1 = "Activity: Scavenge for gym equipment";
            message2 = "You have found " + countString + " pieces of gym equipment";
            Preferences.increment("_daycareGymScavenges");
          }
        } else if (decision == 3) {
          if (request.responseText.contains("new teacher joins the staff")) {
            message1 = "Activity: Hire an instructor";
            message2 = "You have hired a new instructor";
          }
        } else if (decision == 4) {
          if (request.responseText.contains("step into the ring")) {
            message1 = "Activity: Spar";
            Preferences.setBoolean("_daycareFights", true);
          }
        }
        if (message1 != null) {
          RequestLogger.printLine(message1);
          RequestLogger.updateSessionLog(message1);
        }
        if (message2 != null) {
          RequestLogger.printLine(message2);
          RequestLogger.updateSessionLog(message2);
        }
      }
    };

    new UnknownChoiceAdventure(1337);

    new RetiredChoiceAdventure(1338, null, null) { // Tammy, Crimbo 2018
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1339, "A Little Pump and Grind", "Item-Driven") {
      final Pattern SAUSAGE_PATTERN =
          Pattern.compile(
              "grinder needs (.*?) of the (.*?) required units of filling to make a sausage.  Your grinder reads \\\"(\\d+)\\\" units.");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2)
            .attachItem(ItemPool.MAGICAL_SAUSAGE, 1, AUTO)
            .attachItem(ItemPool.MAGICAL_SAUSAGE_CASING, -1, MANUAL, new NoDisplay());
        new Option(3);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        if (decision == 1) {
          int itemId = ChoiceManager.extractIidFromURL(urlString);
          int qty = ChoiceManager.extractQtyFromURL(urlString);
          String name = ItemDatabase.getItemName(itemId);
          if (name != null) {
            RequestLogger.updateSessionLog("grinding " + qty + " " + name);
            return true;
          }
        }
        return true;
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = SAUSAGE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger(
              "_sausagesMade",
              StringUtilities.parseInt(matcher.group(2).replaceAll(",", "")) / 111 - 1);
          Preferences.setString("sausageGrinderUnits", matcher.group(3));
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("filling counter increments")) {
          int itemId = ChoiceManager.extractIidFromURL(request.getURLString());
          int qty = ChoiceManager.extractQtyFromURL(request.getURLString());
          getOption(decision).attachItem(itemId, -qty, MANUAL);
        } else if (decision == 2 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1340, "Is There A Doctor In The House?", "undefined") {
      final Pattern DOCTOR_BAG_PATTERN =
          Pattern.compile("We've received a report of a patient (.*?), in (.*?)\\.");

      void setup() {
        this.isSuperlikely = true;

        this.customName = "Lil' Doctor&trade; bag Quest";
        this.customZones.add("Item-Driven");

        new Option(1);
        new Option(2);
        new Option(3);

        new CustomOption(1, "get quest");
        new CustomOption(2, "refuse quest");
        new CustomOption(3, "stop offering quest");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = DOCTOR_BAG_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          String malady = matcher.group(1);
          String item = "";
          if (malady.contains("tropical heatstroke")) {
            item = "palm-frond fan";
          } else if (malady.contains("archaic cough")) {
            item = "antique bottle of cough syrup";
          } else if (malady.contains("broken limb")) {
            item = "cast";
          } else if (malady.contains("low vim and vigor")) {
            item = "Doc Galaktik's Vitality Serum";
          } else if (malady.contains("bad clams")) {
            item = "anti-anti-antidote";
          } else if (malady.contains("criss-cross laceration")) {
            item = "plaid bandage";
          } else if (malady.contains("knocked out by a random encounter")) {
            item = "phonics down";
          } else if (malady.contains("Thin Blood Syndrome")) {
            item = "red blood cells";
          } else if (malady.contains("a blood shortage")) {
            item = "bag of pygmy blood";
          }
          Preferences.setString("doctorBagQuestItem", item);
          Preferences.setString("doctorBagQuestLocation", matcher.group(2));
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (Preferences.getString("doctorBagQuestItem") == "") {
            // We didn't recognize quest request.responseText, so get it from quest log
            RequestThread.postRequest(new QuestLogRequest());
          }
          int itemId = ItemDatabase.getItemId(Preferences.getString("doctorBagQuestItem"));

          if (itemId > 0) {
            if (InventoryManager.getCount(itemId) > 0) {
              QuestDatabase.setQuestProgress(Quest.DOCTOR_BAG, "step1");
            } else {
              QuestDatabase.setQuestProgress(Quest.DOCTOR_BAG, QuestDatabase.STARTED);
            }
          } else {
            QuestDatabase.setQuestProgress(Quest.DOCTOR_BAG, QuestDatabase.STARTED);
          }
        } else {
          QuestDatabase.setQuestProgress(Quest.DOCTOR_BAG, QuestDatabase.UNSTARTED);
          Preferences.setString("doctorBagQuestItem", "");
          Preferences.setString("doctorBagQuestLocation", "");
        }
      }
    };

    new ChoiceAdventure(1341, "A Pound of Cure", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        String itemName = Preferences.getString("doctorBagQuestItem");
        if (!itemName.equals("")) {
          getOption(decision).attachItem(itemName, -1, MANUAL);
        } else {
          // Don't know item to remove so refresh inventory instead
          ApiRequest.updateInventory();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.DOCTOR_BAG, QuestDatabase.UNSTARTED);
        Preferences.setString("doctorBagQuestItem", "");
        Preferences.setString("doctorBagQuestLocation", "");
        if (request.responseText.contains("One of the five green lights")) {
          Preferences.setInteger("doctorBagQuestLights", 1);
        } else if (request.responseText.contains("second of the five green lights")) {
          Preferences.setInteger("doctorBagQuestLights", 2);
        } else if (request.responseText.contains("third of the five green lights")) {
          Preferences.setInteger("doctorBagQuestLights", 3);
        } else if (request.responseText.contains("fourth of the five green lights")) {
          Preferences.setInteger("doctorBagQuestLights", 4);
        } else if (request.responseText.contains("lights go dark again")) {
          Preferences.setInteger("doctorBagQuestLights", 0);
        }
        if (request.responseText.contains("bag has been permanently upgraded")) {
          Preferences.increment("doctorBagUpgrades");
        }
      }
    };

    new ChoiceAdventure(1342, "Torpor", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (KoLCharacter.getCurrentRun() > 0) {
          // the initial "torpor" you get right after choice 1343
          // doesn't cost a turn.
          // this check relies on the fact nobody would
          // go back to the coffin right after.
          // should probably get changed, though...
          getOption(1).turnCost(1);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          // You can learn or forget Vampyre skills
          for (int i = 10; i < 39; i++) {
            String skillName = SkillDatabase.getSkillName(24000 + i);

            if (request.getURLString().contains("sk[]=" + i) && !KoLCharacter.hasSkill(24000 + i)) {
              KoLCharacter.addAvailableSkill(skillName);
              String message = "You have learned " + skillName;
              RequestLogger.printLine(message);
              RequestLogger.updateSessionLog(message);
            } else if (!request.getURLString().contains("sk[]=" + i)
                && KoLCharacter.hasSkill(24000 + i)) {
              KoLCharacter.removeAvailableSkill(skillName);
              String message = "You have forgotten " + skillName;
              RequestLogger.printLine(message);
              RequestLogger.updateSessionLog(message);
            }
          }
        }
      }
    };

    new ChoiceAdventure(1343, "Intro: View of a Vampire", null) {
      void setup() {
        new Option(1).leadsTo(1342);
      }
    };

    new ChoiceAdventure(1344, "Thank You, Come Again", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        AvatarManager.handleAfterAvatar(decision);
      }
    };

    new ChoiceAdventure(1345, "Blech House", "The Smut Orc Logging Camp") {
      void setup() {
        new Option(1, "use muscle/weapon damage", true).turnCost(1);
        new Option(2, "use myst/spell damage", true).turnCost(1);
        new Option(3, "use mox/sleaze res", true).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setInteger("smutOrcNoncombatProgress", 0);
      }
    };

    new ChoiceAdventure(1346, "Welcome to PirateRealm", null) {
      void setup() {
        new Option(1).leadsTo(1347);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          Preferences.setString("_prCurrentProgress", "unstarted");
        }
      }
    };

    new ChoiceAdventure(1347, "Groggy's Tavern", null) {
      //		type=submit value="the Wide-Eyed
      // Coxswain
      // "></form>
      final Pattern CREW_PATTERN =
          Pattern.compile(
              "<form.*?action=choice\\.php.*?name=option value=(\\d+).*?type=submit.*?value=\"the ([\\w\\-]+).+?(\\w+).\".*?</form>",
              Pattern.DOTALL);

      void setup() {
        new Option(1).leadsTo(1348);
        new Option(2).leadsTo(1348);
        new Option(3).leadsTo(1348);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher crewMatcher = CREW_PATTERN.matcher(request.responseText);
        while (crewMatcher.find()) {
          int crewmateNumber = StringUtilities.parseInt(crewMatcher.group(1));

          if (crewmateNumber == 0) {
            // failed parsing?
            continue;
          }

          String adjective = crewMatcher.group(2);
          String role = crewMatcher.group(3);

          Preferences.setString("_prPirateCrewAdjective" + crewmateNumber, adjective);
          Preferences.setString("_prPirateCrewRole" + crewmateNumber, role);

          StringBuilder spoilerText = new StringBuilder();

          boolean jungleUnlocked = false;
          boolean skullUnlocked = false;

          // they'll fix that typo...
          // one day...
          if (adjective.equalsIgnoreCase("Beligerent")
              || adjective.equalsIgnoreCase("Belligerent")) {
            spoilerText.append("Jungle Island, Avast, a Mast! gives +1 gun");
            jungleUnlocked = true;
          } else if (adjective.equalsIgnoreCase("Dipsomaniacal")) {
            spoilerText.append("+0-1 grog per fight, Avast, a Mast! gives +1 grog");
          } else if (adjective.equalsIgnoreCase("Gluttonous")) {
            spoilerText.append(
                "+0-1 grub per fight, Avast, a Mast! gives +1 grub, helps when grub runs out");
          } else if (adjective.equalsIgnoreCase("Pinch-Fisted")) {
            spoilerText.append("+5-10 gold per fight, sinking pirates gives +30-40 gold");
          } else if (adjective.equalsIgnoreCase("Wide-Eyed")) {
            spoilerText.append("Skull Island, birdwatching gives +3 fun in Smooth Sailing");
            skullUnlocked = true;
          }

          spoilerText.append(",<br>");

          if (role.equalsIgnoreCase("Coxswain")) {
            spoilerText.append("improves odds of outrunning storms");
          } else if (role.equalsIgnoreCase("Cryptobotanist")) {
            if (!jungleUnlocked) {
              spoilerText.append("Jungle Island, ");
            }
            spoilerText.append("helps when grog runs out");
          } else if (role.equalsIgnoreCase("Cuisinier")) {
            spoilerText.append("Dessert Island, eating gives +2 fun in Smooth Sailing");
          } else if (role.equalsIgnoreCase("Harquebusier")) {
            if (!skullUnlocked) {
              spoilerText.append("Skull Island, ");
            }
            spoilerText.append("deals dmg, +1 fun per fight");
          } else if (role.equalsIgnoreCase("Mixologist")) {
            spoilerText.append("drinking gives +2 fun in Smooth Sailing");
          }

          getOption(crewmateNumber).text(spoilerText.toString());
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setInteger("_prPirateCrewTaken", decision);
      }
    };

    new ChoiceAdventure(1348, "Seaside Curios", null) {
      void setup() {
        new Option(
                1,
                "(2-handed) fighting in A Sea Monster! always gives 10 fun and +300-400 gold (if worn)")
            .leadsTo(1349)
            .attachItem("bloody harpoon", 1, AUTO, new ImageOnly());
        new Option(
                2,
                "can be sold for 500 gold. If sold, Avast, a Mast! \"gives it back\", unlocking Glass Jack's Hideout")
            .leadsTo(1349)
            .attachItem(ItemPool.CURSED_COMPASS, 1, AUTO, new ImageOnly())
            .attachItem("recursed compass", new ImageOnly("\"gives it back\""));
        new Option(3, "Prison Island, makes Avast, a Mast! give +100-200 gold")
            .leadsTo(1349)
            .attachItem("ancient skull key", 1, AUTO, new ImageOnly());
        new Option(4, "Trash Island, improves odds of outrunning storms")
            .leadsTo(1349)
            .attachItem("curious anemometer", 1, AUTO, new ImageOnly());
        new Option(5, "increases likelihood of getting Who Pirates the Pirates? while sailing")
            .leadsTo(1349)
            .attachItem("Red Roger's flag", 1, AUTO, new ImageOnly());
        new Option(6, "Key Key")
            .leadsTo(1349)
            .attachItem("Glass Jack's spyglass", 1, AUTO, new ImageOnly());
      }
    };

    new ChoiceAdventure(1349, "Dishonest Ed's Ships", null) {
      void setup() {
        new Option(1, "7 turns between islands, 3 guns, 10 grub, 10 grog, 1 glue").leadsTo(1350);
        new Option(2, "7 turns between islands, 4 guns, 8 grub, 8 grog, 0 glue").leadsTo(1350);
        new Option(3, "6 turns between islands, 1 gun, 5 grub, 5 grog, 1 glue").leadsTo(1350);
        new Option(4, "4 turns between islands, 0 gun, 5 grub, 5 grog, 1 glue").leadsTo(1350);
        new Option(5, "9 turns between islands, 8 guns, 5 grub, 30 grog, 0 glue").leadsTo(1350);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String ship;

        switch (decision) {
          case 1:
            ship = "Frigate";
            break;
          case 2:
            ship = "Galleon";
            break;
          case 3:
            ship = "Caravel";
            break;
          case 4:
            ship = "Clipper";
            break;
          case 5:
            ship = "Man o' War";
            break;
          default:
            ship = "unknown";
            break;
        }

        Preferences.setString("_prPirateShip", ship);
      }
    };

    new ChoiceAdventure(1350, "Time to Set Sail!", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new UnknownChoiceAdventure(1351);

    new ChoiceAdventure(1352, "Island #1, Who Are You?", "Sailing the PirateRealm Seas") {
      void setup() {
        this.isSuperlikely = true;

        new Option(
            1,
            "+3 fun & +1 grub per fight, foes drop 120-180 meat, boss drops 1600-2400 meat & gives 17 fun");
        new Option(2, "+3 fun & +1 grog per fight, gives base booze");
        new Option(3, "+3 fun per fight, foes drop melty plastic grenades, gives 3 gun")
            .attachItem("melty plastic grenade", new DisplayAll("grenades"));
        new Option(4, "+4 fun & +6 gold per fight, makes Cemetary Island give Red Roger's map")
            .attachItem("Red Roger's map");
        new Option(5, "+7 fun per fight, gives random/missing tower key");
        new Option(6, "+3 fun per fight, foes drop oversized ice molecules, gives cocoa of youth")
            .attachItem("oversized ice molecule", new DisplayAll("molecules"))
            .attachItem("cocoa of youth");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String island = null;

        switch (decision) {
          case 1:
            island = "Crab Island";
            break;
          case 2:
            island = "Glass Island";
            break;
          case 3:
            island = "Battle Island";
            break;
          case 4:
            island = "Skull Island";
            break;
          case 5:
            island = "Key Key";
            break;
          case 6:
            island = "Dessert Island";
            break;
        }

        if (island != null) {
          Preferences.setString("_LastPirateRealmIsland", island);
          Preferences.setString("_prCurrentProgress", "sail");
          CADatabase1300to1399.resetPRTravelLeft();
        } else {
          Preferences.setString("_prCurrentProgress", "select");
        }
      }
    };

    new ChoiceAdventure(1353, "What's Behind Island #2?", "Sailing the PirateRealm Seas") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "+3 fun per fight, gives 30 fun")
            .attachItem("pirate shaving cream", new DisplayAll("cream"));
        new Option(
                2,
                "+4 fun & +6-11 gold per fight. With tomb opener, gives 20 fun & unlocks Red Roger's Fortress, else gives 10 fun")
            .attachItem("tomb opener", new DisplayAll("opener", WANT, AT_LEAST, 1));
        new Option(3, "+3 fun & +1 grog per fight, boss gives 17 fun");
        new Option(4, "+7 fun per fight, gives 20 fun");
        new Option(
            5,
            "+3 fun & +1 grub & +1 grog per fight, foes drop 400-600 meat, gives 4 random tradeable items (2-3 of each)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String island = null;

        switch (decision) {
          case 1:
            island = "Isla Gublar";
            break;
          case 2:
            island = "Cemetery Island";
            break;
          case 3:
            island = "Jungle Island";
            break;
          case 4:
            island = "Prison Island";
            break;
          case 5:
            island = "Trash Island";
            break;
        }

        if (island != null) {
          Preferences.setString("_LastPirateRealmIsland", island);
          Preferences.setString("_prCurrentProgress", "sail");
          CADatabase1300to1399.resetPRTravelLeft();
        } else {
          Preferences.setString("_prCurrentProgress", "select");
        }
      }
    };

    new ChoiceAdventure(1354, "Third Island's the Charm", "Sailing the PirateRealm Seas") {
      final AdventureResult ROGER_LEFT_FOOT =
          new AdventureResult("Red Roger's red left foot", 0, false);
      final AdventureResult ROGER_RIGHT_FOOT =
          new AdventureResult("Red Roger's red right foot", 0, false);
      final AdventureResult ROGER_LEFT_HAND =
          new AdventureResult("Red Roger's red left hand", 0, false);
      final AdventureResult ROGER_RIGHT_HAND =
          new AdventureResult("Red Roger's red right hand", 0, false);

      void setup() {
        this.isSuperlikely = true;

        // didn't something change there once the signal puzzle got solved?
        new Option(1, "+3 fun per fight, foes drop signal fragment, gives 17 fun")
            .attachItem("signal fragment", new DisplayAll("fragment"));
        new Option(2, "+3 fun per fight, foes drop cocktail items, gives 10 fun")
            .attachItem("hibiscus petal", new ImageOnly("cocktail items"))
            .attachItem("huge mint leaf", new ImageOnly("cocktail items"))
            .attachItem("pineapple slab", new ImageOnly("cocktail items"));
        new Option(3, "+3 fun per fight, foes drop windicle, gives 10 fun")
            .attachItem("windicle", new DisplayAll("windicle"));
        new Option(4, "+7 fun per fight, boss gives 17 fun & drops Red Roger's reliquary")
            .attachItem("Red Roger's reliquary");
        new Option(5, "+3 fun per fight, boss gives 17 fun");
        new Option(6, "+3-4 fun & +0-6 gold per fight, foes pewter shavings, gives 10 000 meat")
            .attachItem("pewter shavings", new DisplayAll("shavings"));
        new Option(7);
      }

      @Override
      void visitChoice(GenericRequest request) {
        boolean allEquipped = true;
        boolean haveAll = true;

        for (AdventureResult piece :
            new AdventureResult[] {
              ROGER_LEFT_FOOT, ROGER_RIGHT_FOOT, ROGER_LEFT_HAND, ROGER_RIGHT_HAND
            }) {
          haveAll &= InventoryManager.hasItem(piece);
          allEquipped &= KoLCharacter.hasEquipped(piece);
        }

        if (haveAll && !allEquipped) {
          getOption(7)
              .text(
                  "exit and return with all 4 of Red Roger's parts to unlock Temple Island<br><br><br><br>")
              .attachItem(
                  ROGER_RIGHT_HAND, new DisplayAll("(?:<br>){1}", WANT, EQUIPPED_AT_LEAST, 1))
              .attachItem(
                  ROGER_LEFT_HAND, new DisplayAll("(?:<br>){2}", WANT, EQUIPPED_AT_LEAST, 1))
              .attachItem(
                  ROGER_RIGHT_FOOT, new DisplayAll("(?:<br>){3}", WANT, EQUIPPED_AT_LEAST, 1))
              .attachItem(
                  ROGER_LEFT_FOOT, new DisplayAll("(?:<br>){4}", WANT, EQUIPPED_AT_LEAST, 1));
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String island = null;

        switch (decision) {
          case 1:
            island = "Signal Island";
            break;
          case 2:
            island = "Tiki Island";
            break;
          case 3:
            island = "Storm Island";
            break;
          case 4:
            island = "Red Roger's Fortress";
            break;
          case 5:
            island = "Jack's Hideout";
            break;
          case 6:
            island = "The Temple";
            break;
        }

        if (island != null) {
          Preferences.setString("_LastPirateRealmIsland", island);
          Preferences.setString("_prCurrentProgress", "sail");
          CADatabase1300to1399.resetPRTravelLeft();
        } else {
          Preferences.setString("_prCurrentProgress", "select");
        }
      }
    };

    new ChoiceAdventure(1355, "Land Ho!", "Sailing the PirateRealm Seas") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "island");
        Preferences.setInteger("_prTravelLeft", 0); // inland progress currently not supported
      }
    };

    new ChoiceAdventure(1356, "Smooth Sailing", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1, "-1 grub, +5 fun", true).turnCost(1);
        new Option(2, "-1 grog, +5 fun", true).turnCost(1);
        new Option(3, "+3 fun", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int crewmate = Preferences.getInteger("_prPirateCrewTaken");
        String adjective = Preferences.getString("_prPirateCrewAdjective" + crewmate);
        String role = Preferences.getString("_prPirateCrewRole" + crewmate);

        if (role.equalsIgnoreCase("Cuisinier")) {
          getOption(1).text("-1 grub, +7 fun");
        } else if (role.equalsIgnoreCase("Mixologist")) {
          getOption(2).text("-1 grog, +7 fun");
        }
        if (adjective.equalsIgnoreCase("Wide-Eyed")) {
          getOption(3).text("+6 fun");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1357, "High Tide, Low Morale", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1, "-3 grub, +5 fun", true).turnCost(1);
        new Option(2, "-3 grog, +5 fun", true).turnCost(1);
        new Option(3, "-30 gold, +5 fun", true).turnCost(1);
        new Option(4, "+1 fun, +1 fun (yes, twice)", true).turnCost(1);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 1, 2 & 3 are not always available
        if (decision.equals("1") && !responseText.contains("Have a feast")
            || decision.equals("2") && !responseText.contains("Have a party")
            || decision.equals("3") && !responseText.contains("Give everybody bonus wages")) {
          return "0";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1358, "The Starboard is Bare", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1, "+5 grub, +1 fun", true).turnCost(1);
        new Option(2, "+1-8 grub, +3 fun", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int crewmate = Preferences.getInteger("_prPirateCrewTaken");
        String adjective = Preferences.getString("_prPirateCrewAdjective" + crewmate);

        if (adjective.equalsIgnoreCase("Gluttonous")) {
          getOption(1).text("+8 grub, +1 fun");
          getOption(2).text("+4-11 grub, +3 fun");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1359, "Grog for the Grogless", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1, "+5 grog, +1 fun", true).turnCost(1);
        new Option(2, "+1-8 grog, +3 fun", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int crewmate = Preferences.getInteger("_prPirateCrewTaken");
        String role = Preferences.getString("_prPirateCrewRole" + crewmate);

        if (role.equalsIgnoreCase("Cryptobotanist")) {
          getOption(1).text("+8 grog, +1 fun");
          getOption(2).text("+4-11 grog, +3 fun");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1360, "Like Shops in the Night", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5, "next Avast, a Mast! encounter will unlock Glass Jack's Hideout");
        new Option(6).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 5 && request.responseText.contains("You gain 500 gold")) {
          // Sell them the cursed compass
          // Remove from equipment (including checkpoints)
          if (EquipmentManager.discardEquipment(ItemPool.CURSED_COMPASS) == -1) {
            // Remove from inventory
            ResultProcessor.removeItem(ItemPool.CURSED_COMPASS);
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        if (decision == 6) {
          Preferences.decrement("_prTravelLeft", 1, 0);
        }
      }
    };

    new ChoiceAdventure(1361, "Avast, a Mast!", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1362, "Stormy Weather", "Sailing the PirateRealm Seas") {
      final AdventureResult CURIOUS_ANEMOMETER =
          new AdventureResult("curious anemometer", 0, false);

      void setup() {
        new Option(1, "+5 fun", true).turnCost(1);
        new Option(2, "+8 fun. Either skip a sea non-combat, or ship broken", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int stormOutrunBonus = 0;

        int crewmate = Preferences.getInteger("_prPirateCrewTaken");
        String role = Preferences.getString("_prPirateCrewRole" + crewmate);
        if (role.equalsIgnoreCase("Coxswain")) {
          stormOutrunBonus++;
        }

        if (CURIOUS_ANEMOMETER.getCount(KoLConstants.inventory) > 0) {
          stormOutrunBonus++;
        }

        String likelihood;
        switch (stormOutrunBonus) {
          case 0:
            likelihood = "low";
            break;
          case 1:
            likelihood = "decent";
            break;
          case 2:
            likelihood = "very high";
            break;
          default:
            likelihood = "unknown";
            break;
        }

        getOption(2)
            .text(
                "+8 fun. Either skip a sea non-combat, or ship broken. Chance of skipping: "
                    + likelihood);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        if (decision == 2 && request.responseText.contains("manage to outsail the storm")) {
          Preferences.decrement("_prTravelLeft", 2, 0);
        } else {
          Preferences.decrement("_prTravelLeft", 1, 0);
        }
      }
    };

    new ChoiceAdventure(1363, "Who Pirates the Pirates?", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(
                1,
                "+8 fun and +50-60 gold and may gain 1 gun, or +7 fun and (-3 grub or -3 grog or -25-30 gold or ship broken)",
                true)
            .turnCost(1);
        new Option(2, "+5 fun, may lose 1 grub or grog", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int crewmate = Preferences.getInteger("_prPirateCrewTaken");
        String adjective = Preferences.getString("_prPirateCrewAdjective" + crewmate);

        if (adjective.equalsIgnoreCase("Pinch-Fisted")) {
          getOption(1)
              .text(
                  "+8 fun and +80-100 gold and may gain 1 gun, or +7 fun and (-3 grub or -3 grog or -25-30 gold or ship broken)");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1364, "An Opportunity for Dastardly Do", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(
                1,
                "+6 fun and (+1 gun or +3-10 grub or +3-10 grog or +50-60 gold), or +4 fun and ship broken",
                true)
            .turnCost(1);
        new Option(2, "+3 fun", true).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new ChoiceAdventure(1365, "A Sea Monster!", "Sailing the PirateRealm Seas") {
      final AdventureResult BLOODY_HARPOON = new AdventureResult("bloody harpoon", 0, false);

      void setup() {
        new Option(1, "+8 fun and +10-15 grub, or +7 fun and nothing (no downside?)", true)
            .turnCost(1);
        new Option(2, "+5 fun, may lose 1 grub or grog", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (KoLCharacter.hasEquipped(BLOODY_HARPOON)) {
          getOption(1).text("+10 fun and +300-400 gold");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
        Preferences.decrement("_prTravelLeft", 1, 0);
      }
    };

    new UnknownChoiceAdventure(1366);

    new ChoiceAdventure(1367, "The Ship is Wrecked", "Sailing the PirateRealm Seas") {
      void setup() {
        new Option(1, "+5 fun, -1 glue", true).turnCost(1);
        new Option(2, "+1 fun", true).turnCost(1);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 1 is not always available
        if (decision.equals("1") && !responseText.contains("Fix it with glue")) {
          return "2";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "sail");
      }
    };

    new ChoiceAdventure(1368, "All-You-Can-Fight Crab", "Crab Island") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1369, "The Battle (Island) Is Won", "Battle Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1370, "Skull's Well That Ends Skull", "Skull Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1371, "The Key Takeaway", "Key Key") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1372, "You Can See Clearly Now", "Glass Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new UnknownChoiceAdventure(1373);

    new ChoiceAdventure(1374, "Prince of the Jungle", "Jungle Island") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1375, "A Close Shave", "Isla Gublar") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1376, "Your Empire of Dirt", "Trash Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1377, "A Dreaded Sunny Day", "Cemetery Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1378, "You Can Fight the Signal", "Signal Island") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1379, "The Tiki Craze Is Over", "Tiki Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "finished");
      }
    };

    new ChoiceAdventure(1380, "Temple's Grand End", "The Temple") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "finished");
      }
    };

    new ChoiceAdventure(1381, "Roger, Over and Out", "Red Roger's Fortress") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1382, "No More Hiding", "Jack's Hideout") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1383, "Parole", "Prison Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1384, "The Calm After the Storm", "Storm Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "finished");
      }
    };

    new ChoiceAdventure(1385, "Just Desserts", "Dessert Island") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("_prCurrentProgress", "select");
      }
    };

    new ChoiceAdventure(1386, "Upgrade Your May the Fourth Cosplay Saber", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1: // choice 1, 10-15 MP regen
            // You fit the Kaiburr crystal to the end of your saber and feel energy course through
            // you.
            if (request.responseText.contains("Kaiburr crystal")) {
              Preferences.setInteger("_saberMod", 1);
            }
            break;

          case 2: // choice 2, 20 ML
            // You pry out your boring blue crystal and put in a badass purple crystal.
            if (request.responseText.contains("blue crystal")) {
              Preferences.setInteger("_saberMod", 2);
            }
            break;

          case 3: // choice 3, 3 resist all
            // Afraid of falling into some lava, you opt fo[sic] the resistance multiplier. The
            // Force sure works in mysterious ways.
            if (request.responseText.contains("resistance multiplier")) {
              Preferences.setInteger("_saberMod", 3);
            }
            break;

          case 4: // choice 4, 10 familiar wt
            // You click the empathy chip in to place and really start to feel for your familiar
            // companions.
            if (request.responseText.contains("empathy chip")) {
              Preferences.setInteger("_saberMod", 4);
            }
            break;
        }
      }
    };

    new ChoiceAdventure(1387, "Using the Force", "undefined") {
      void setup() {
        this.neverEntersQueue = true;
        this.isSuperlikely = true;

        new Option(1, "banish the previous monster for 30 turns");
        new Option(2, "the next 3 times the previous monster can naturally appear, it WILL appear");
        new Option(3, "get all of the previous monster's non-conditional items");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          BanishManager.banishCurrentMonster("Saber Force");
          Preferences.increment("_saberForceUses");
        } else if (decision == 2) {
          Preferences.setString("_saberForceMonster", MonsterStatusTracker.getLastMonsterName());
          Preferences.setInteger("_saberForceMonsterCount", 3);
          Preferences.increment("_saberForceUses");
        } else if (decision == 3) {
          Preferences.increment("_saberForceUses");
        } else {
          return;
        }

        // Reset all combat state for the next fight.
        FightRequest.clearInstanceData();

        // Eventually try to reduce delay in the last adventured area, and remove the
        // last monster from the queue.  Not reducing delay when the fight didn't come
        // from a location will likely be non-trivial.
      }
    };

    new ChoiceAdventure(1388, "Comb the Beach", "Item-Driven") {
      void setup() {
        new Option(1); // wander
        new Option(2); // random
        new Option(3); // buffs (with the "buff" field, 1-11)
        new Option(4); // comb (when wandering, with the "coords" field)
        new Option(5); // leave
        new Option(6); // common items
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return BeachCombRequest.registerRequest(urlString);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        if (!BeachCombRequest.containsEncounter(urlString)) {
          return null;
        }
        return AdventureRequest.parseEncounter(responseText);
      }

      @Override
      void visitChoice(GenericRequest request) {
        BeachManager.parseCombUsage(request.responseText);
        BeachManager.parseBeachMap(request.responseText);

        if (Preferences.getInteger("_freeBeachWalksUsed") == 11) {
          getOption(3).turnCost(1);
          getOption(4).turnCost(1);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          if (request.responseText.contains(">Huh?<")
              || !BeachManager.parseBeachHeadCombing(request.responseText)) {
            this.choiceFailed();
          }
        } else if (decision == 4) {
          if (request.responseText.contains("That area is under the surf right now.")
              || !BeachManager.parseBeachHeadCombing(request.responseText)
                  && !BeachManager.parseCombUsage(request.getURLString(), request.responseText)) {
            this.choiceFailed();
          }
        }
        BeachManager.parseCombUsage(request.responseText);
      }
    };

    new ChoiceAdventure(1389, "The Council of Exploathing", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(25); // wtf
      }
    };

    new UnknownChoiceAdventure(1390);

    new ChoiceAdventure(1391, "Rationing out Destruction", "The Exploaded Battlefield") {
      final Pattern TOSSID_PATTERN = Pattern.compile("tossid=(\\d+)");
      // You toss the space wine off the edge of the floating battlefield and 21 frat boys jump off
      // after it.
      final Pattern FRAT_RATIONING_PATTERN =
          Pattern.compile(
              "You toss the (.*?) off the edge of the floating battlefield and (\\d+) frat boys? jumps? off after it.");
      // You toss the mana curds into the crowd.  10 hippies dive onto it, greedily consume it, and
      // pass out.
      final Pattern HIPPY_RATIONING_PATTERN =
          Pattern.compile(
              "You toss the (.*?) into the crowd.  (\\d+) hippies dive onto it, greedily consume it, and pass out.");

      void setup() {
        new Option(1).turnCost(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // choice.php?whichchoice=1391&option=1&pwd&tossid=10321

        Matcher tossMatcher = TOSSID_PATTERN.matcher(request.getURLString());
        if (!tossMatcher.find()) {
          this.choiceFailed();
          return;
        }
        int itemId = StringUtilities.parseInt(tossMatcher.group(1));
        getOption(decision).attachItem(itemId, -1, MANUAL);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String army = null;
        String property = null;
        String consumable = null;
        int casualties = 0;

        Matcher fratMatcher = FRAT_RATIONING_PATTERN.matcher(request.responseText);
        Matcher hippyMatcher = HIPPY_RATIONING_PATTERN.matcher(request.responseText);
        if (fratMatcher.find()) {
          army = "frat boys";
          property = "fratboysDefeated";
          consumable = fratMatcher.group(1);
          casualties = StringUtilities.parseInt(fratMatcher.group(2));
        } else if (hippyMatcher.find()) {
          army = "hippies";
          property = "hippiesDefeated";
          consumable = hippyMatcher.group(1);
          casualties = StringUtilities.parseInt(hippyMatcher.group(2));
        }

        Preferences.increment(property, casualties);

        String message = "You defeated " + casualties + " " + army + " with some " + consumable;
        RequestLogger.printLine(message);
        RequestLogger.updateSessionLog(message);
      }
    };

    new ChoiceAdventure(1392, "Decorate your Tent", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Muscular Intentions per rest")
            .attachEffect("Muscular Intentions", new ImageOnly("Intentions"))
            .attachItem(ItemPool.BURNT_STICK, -1, MANUAL, new NoDisplay());
        new Option(2, "20 adv of Mystical Intentions per rest")
            .attachEffect("Mystical Intentions", new ImageOnly("Intentions"))
            .attachItem(ItemPool.BURNT_STICK, -1, MANUAL, new NoDisplay());
        new Option(3, "20 adv of Moxious Intentions per rest")
            .attachEffect("Moxious Intentions", new ImageOnly("Intentions"))
            .attachItem(ItemPool.BURNT_STICK, -1, MANUAL, new NoDisplay());
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int currentDecoration = Preferences.getInteger("campAwayDecoration");

        if (currentDecoration > 0 && currentDecoration < 4) {
          getOption(currentDecoration).text("waste a stick (already your current decoration)");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // no need to check for choice failure; the stick
        // is consumed even if that was already your decoration
        switch (decision) {
          case 1: // Muscular Intentions
          case 2: // Mystical Intentions
          case 3: // Moxious Intentions
            Preferences.setInteger("campAwayDecoration", decision);
            break;
        }
      }
    };

    new UnknownChoiceAdventure(1393);

    new ChoiceAdventure(1394, "Send up a Smoke Signal", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.CAMPFIRE_SMOKE, -1, MANUAL, new NoDisplay());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You send a smoky message to the sky.")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1395, "Take your Pills", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
        new Option(9);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (!request.responseText.contains("day's worth of pills")) {
          // Something failed.  Not enough spleen space left, already have
          // Everything Looks Yellow active, or maybe some other failure condition
          return;
        }
        if (decision >= 1 && decision <= 8) {
          if (!Preferences.getBoolean("_freePillKeeperUsed")) {
            Preferences.setBoolean("_freePillKeeperUsed", true);
          } else {
            KoLCharacter.setSpleenUse(KoLCharacter.getSpleenUse() + 3);
          }
        }
        if (decision == 7) {
          TurnCounter.stopCounting("Fortune Cookie");
          TurnCounter.stopCounting("Semirare window begin");
          TurnCounter.stopCounting("Semirare window end");
          TurnCounter.startCounting(0, "Fortune Cookie", "fortune.gif");
          Preferences.setString("semirareLocation", "");
        }
      }
    };

    new ChoiceAdventure(1396, "Adjusting Your Fish", "Item-Driven") {
      final Pattern RED_SNAPPER_PATTERN =
          Pattern.compile("guiding you towards: <b>(.*?)</b>.  You've found <b>(\\d+)</b> of them");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = RED_SNAPPER_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Phylum phylum = Phylum.find(matcher.group(1));
          int progress = StringUtilities.parseInt(matcher.group(2));
          Preferences.setString("redSnapperPhylum", phylum.toString());
          Preferences.setInteger("redSnapperProgress", progress);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // choice.php?pwd&whichchoice=1396&option=1&cat=fish
        if (decision == 1
            // no "cat" field, or it has an incorrect value
            && !request.responseText.contains("Hmm.")) {
          String phylum = request.getFormField("cat");
          String fixed = phylum.equals("merkin") ? "mer-kin" : phylum;
          Preferences.setString("redSnapperPhylum", fixed);
          Preferences.setInteger("redSnapperProgress", 0);
        }
      }
    };

    new RetiredChoiceAdventure(1397, "Kringle workshop", "The Wreck of the H. M. S. Kringle") {
      void setup() {
        this.customName = this.name;

        new Option(1, "craft stuff", true).turnCost(1);
        new Option(2, "get waterlogged items", true).turnCost(1);
        new Option(3, "fail at life", true).turnCost(1);
      }
    };

    new RetiredChoiceAdventure(1398, "Crimborienteering", "The Impenetrable Kelp-Holly Forest") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1399, "New Favorite Bird?", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Auto correct, in case we cast this outside of KoLmafia
        Preferences.setInteger("_birdsSoughtToday", 6);
        if (decision == 1) {
          String bird = Preferences.getString("_birdOfTheDay");
          Preferences.setString("yourFavoriteBird", bird);
          ResponseTextParser.learnSkill("Visit your Favorite Bird");
          ResultProcessor.updateBirdModifiers(
              EffectPool.BLESSING_OF_YOUR_FAVORITE_BIRD, "yourFavoriteBird");
        }
      }
    };
  }

  static final void resetPRTravelLeft() {
    String ship = Preferences.getString("_prPirateShip");
    int travelTime;

    if (ship == "Frigate" || ship == "Galleon") {
      travelTime = 7;
    } else if (ship == "Caravel") {
      travelTime = 6;
    } else if (ship == "Clipper") {
      travelTime = 4;
    } else if (ship == "Man o' War") {
      travelTime = 9;
    } else {
      travelTime = 7; // assume default
    }

    Preferences.setInteger("_prTravelLeft", travelTime);
  }
}
