package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.AscensionClass;
import net.sourceforge.kolmafia.EdServantData;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.combat.MonsterStatusTracker;
import net.sourceforge.kolmafia.moods.HPRestoreItemList;
import net.sourceforge.kolmafia.moods.MPRestoreItemList;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.DeckOfEveryCardRequest;
import net.sourceforge.kolmafia.request.EdBaseRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.SpelunkyRequest;
import net.sourceforge.kolmafia.request.TavernRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.Limitmode;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.SorceressLairManager;
import net.sourceforge.kolmafia.session.TurnCounter;
import net.sourceforge.kolmafia.textui.command.EdPieceCommand;
import net.sourceforge.kolmafia.utilities.ChoiceUtilities;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase1000to1099 extends ChoiceAdventureDatabase {
  final void add1000to1099() {
    new ChoiceAdventure(1000, "Everything in Moderation", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        TavernRequest.decorateFaucetInteraction(buffer, option);
      }
    };

    new ChoiceAdventure(1001, "Hot and Cold Dripping Rats", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1, "fight drunken rat");
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(
        1002,
        "Temple of the Legend in the Hidden City",
        "A Massive Ziggurat") { // Handle quest in Ed
      void setup() {
        new Option(1).attachItem(ItemPool.STONE_TRIANGLE, new DisplayAll(NEED, EXACTLY, 4));
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("The spectre nods emphatically")) {
          getOption(decision)
              .turnCost(1)
              .attachItem(ItemPool.ED_AMULET, -1, MANUAL)
              .attachItem(ItemPool.STONE_TRIANGLE, -4, MANUAL);
          QuestDatabase.setQuestProgress(Quest.WORSHIP, QuestDatabase.FINISHED);
          if (InventoryManager.getCount(ItemPool.ED_EYE) == 0
              && InventoryManager.getCount(ItemPool.ED_FATS_STAFF) == 0) {
            QuestDatabase.setQuestProgress(Quest.MACGUFFIN, QuestDatabase.FINISHED);
          }
        }
      }
    };

    new ChoiceAdventure(1003, "Test Your Might And Also Test Other Things", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(6);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.parseContestBooth(0, request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SorceressLairManager.parseContestBooth(decision, request.responseText);
      }
    };

    new ChoiceAdventure(1004, "This Maze is... Mazelike...", "The Hedge Maze") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).leadsTo(1005);
        new Option(2);
      }
    };

    new ChoiceAdventure(1005, "'Allo", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 1";

        new Option(1, "topiary nugglet and advance to Room 2 (fight)", true)
            .turnCost(1)
            .leadsTo(1006)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Test #1 and advance to Room 4 (elemental test)", true)
            .turnCost(1)
            .leadsTo(1008);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 1)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SorceressLairManager.parseMazeTrap(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1006, "One Small Step For Adventurer", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 2";

        new Option(1, "topiary nugglet and advance to Room 3 (fight)", true)
            .turnCost(1)
            .leadsTo(1007)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Fight topiary gopher and advance to Room 4 (elemental test)", true)
            .leadsTo(1008);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 2)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1007, "Twisty Little Passages, All Hedge", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 3";

        new Option(1, "topiary nugglet and advance to Room 4 (elemental test)", true)
            .turnCost(1)
            .leadsTo(1008)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Fight topiary chihuahua herd and advance to Room 5 (fight)", true)
            .leadsTo(1009);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 3)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1008, "Pooling Your Resources", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 4";

        new Option(1, "topiary nugglet and advance to Room 5 (fight)", true)
            .turnCost(1)
            .leadsTo(1009)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Test #2 and advance to Room 7 (elemental test)", true)
            .turnCost(1)
            .leadsTo(1011);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 4)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SorceressLairManager.parseMazeTrap(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1009, "Good Ol' 44% Duck", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 5";

        new Option(1, "topiary nugglet and advance to Room 6 (fight)", true)
            .turnCost(1)
            .leadsTo(1010)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Fight topiary duck and advance to Room 7 (elemental test)", true)
            .leadsTo(1011);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 5)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1010, "Another Day, Another Fork", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 6";

        new Option(1, "topiary nugglet and advance to Room 7 (elemental test)", true)
            .turnCost(1)
            .leadsTo(1011)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Fight topiary kiwi and advance to Room 8 (last temptation)", true)
            .leadsTo(1012);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 6)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1011, "Of Mouseholes and Manholes", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 7";

        new Option(1, "topiary nugglet and advance to Room 8 (last temptation", true)
            .turnCost(1)
            .leadsTo(1012)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Test #3 and advance to Room 9 (end)", true).turnCost(1).leadsTo(1013);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 7)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SorceressLairManager.parseMazeTrap(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1012, "The Last Temptation", "The Hedge Maze") {
      void setup() {
        this.customName = "Hedge Maze 8";

        new Option(1, "topiary nugglet and advance to Room 9 (end)", true)
            .turnCost(1)
            .leadsTo(1013)
            .attachItem(ItemPool.TOPIARY_NUGGLET, 1, AUTO);
        new Option(2, "Lose HP for no benefit and advance to Room 9 (end)", true)
            .turnCost(1)
            .leadsTo(1013);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 8)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }
    };

    new ChoiceAdventure(1013, "Mazel Tov!", "The Hedge Maze") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLocation("The Hedge Maze (Room 9)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        SorceressLairManager.visitChoice(this.choice, request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // Then you both giggle and head through the exit at the same time.
        QuestDatabase.setQuestProgress(Quest.FINAL, "step5");
      }
    };

    new UnknownChoiceAdventure(1014);

    new ChoiceAdventure(
        1015, "The Mirror in the Tower has the View that is True", "Tower Level 4") {
      void setup() {
        this.customName = "Tower Mirror";

        new Option(1, "Gain Confidence! intrinsic until leave tower (1)", true)
            .turnCost(1)
            .attachEffect(EffectPool.CONFIDENCE);
        new Option(2, "Make Sorceress tougher (0 turns)", true);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.FINAL, "step10");
      }
    };

    new ChoiceAdventure(1016, "Frank Gets Earnest", "The Naughty Sorceress' Chamber") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1017, "Bear Verb Orgy", "The VERY Unquiet Garves") {
      void setup() {
        new Option(1).turnCost(1).attachItem(ItemPool.WAND_OF_NAGAMAR, 1, AUTO);
      }
    };

    new ChoiceAdventure(1018, "Bee Persistent", "The Black Forest") {
      void setup() {
        this.customName = "Bees 1";

        new Option(1, "lose HP, get closer to beehive (1)", true).turnCost(1).leadsTo(1019);
        new Option(2, "give up", true);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1019, "Bee Rewarded", "The Black Forest") {
      void setup() {
        this.customName = "Bees 2";

        new Option(1, "lose HP, get beehive (1)", true)
            .turnCost(1)
            .attachItem(ItemPool.BEEHIVE, 1, AUTO);
        new Option(2, "give up", true);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1020, "Closing Ceremony", null) {
      void setup() {
        new Option(1).leadsTo(1021);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }
    };

    new ChoiceAdventure(1021, "Meet Frank", null) { // 1/2
      void setup() {
        new Option(1).leadsTo(1022);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }
    };

    new ChoiceAdventure(1022, "Meet Frank", null) { // 2/2
      void setup() {
        new Option(1);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return SorceressLairManager.registerChoice(this.choice, urlString);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // Frank bobs his head toward the hedge maze in front of you.
        QuestDatabase.setQuestProgress(Quest.FINAL, "step4");
      }
    };

    new ChoiceAdventure(1023, "Like a Bat Into Hell", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        EdBaseRequest.parseReviveFees(request);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        StringUtilities.globalStringReplace(buffer, "Go right back to the fight!", "UNDYING!");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          KoLCharacter.setLimitmode(Limitmode.ED);
        } else if (decision == 2) {
          int edDefeats = Preferences.getInteger("_edDefeats");
          int kaCost = edDefeats > 2 ? (int) (Math.pow(2, Math.min(edDefeats - 3, 5))) : 0;
          getOption(decision).attachItem(ItemPool.KA_COIN, -kaCost, MANUAL);

          KoLCharacter.setLimitmode(null);
        }
      }
    };

    new ChoiceAdventure(1024, "Like a Bat out of Hell", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        EdBaseRequest.parseReviveFees(request);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        StringUtilities.globalStringReplace(buffer, "Return to the fight!", "UNDYING!");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          int edDefeats = Preferences.getInteger("_edDefeats");
          int kaCost = edDefeats > 2 ? (int) (Math.pow(2, Math.min(edDefeats - 3, 5))) : 0;
          AdventureResult cost = ItemPool.get(ItemPool.KA_COIN, -kaCost);
          ResultProcessor.processResult(cost);
          KoLCharacter.setLimitmode(null);
        } else if (decision == 2) {
          Preferences.setInteger("_edDefeats", 0);
          Preferences.setBoolean("edUsedLash", false);
          MonsterStatusTracker.reset();
          KoLCharacter.setLimitmode(null);
        }
      }
    };

    new ChoiceAdventure(1025, "Reconfigure your Mini-Crimbot", "Item-Driven") {
      final Pattern CRIMBOT_CHASSIS_PATTERN = Pattern.compile("base chassis is the (.*?),");
      final Pattern CRIMBOT_ARM_PATTERN =
          Pattern.compile("(?:My arm is the|</i> equipped with a) (.*?),");
      final Pattern CRIMBOT_PROPULSION_PATTERN =
          Pattern.compile(
              "(?:provided by a|am mobilized by an|equipped with a pair of|move via) (.*?),");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // chassis
        new Option(2); // arm
        new Option(3); // propulsion
        new Option(4); // randomize
        new Option(6); // leave
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = CRIMBOT_CHASSIS_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("crimbotChassis", matcher.group(1));
        }
        matcher = CRIMBOT_ARM_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("crimbotArm", matcher.group(1));
        }
        matcher = CRIMBOT_PROPULSION_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("crimbotPropulsion", matcher.group(1));
        }
      }
    };

    new ChoiceAdventure(
        1026, "Home on the Free Range", "The Castle in the Clouds in the Sky (Ground Floor)") {
      void setup() {
        this.customName = "Ground Foodie";

        new Option(1, "4 random pieces of candy", true) // no way we're adding them all
            .turnCost(1);
        new Option(2, "get electric boning knife")
            .turnCost(1)
            .attachItem(ItemPool.ELECTRIC_BONING_KNIFE, 1, AUTO);
        new Option(3, "skip adventure", true).entersQueue(false);

        new CustomOption(2, "electric boning knife, then skip adventure");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 2 is electric boning knife - until you get
        // it, at which point the option is not available.
        if (decision.equals("2") && !responseText.contains("Investigate the noisy drawer")) {
          return "3";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1027, "The End of the Tale of Spelunking", null) {
      void setup() {
        new Option(1).leadsTo(1042);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          // Remove all virtual items from inventory/tally
          SpelunkyRequest.resetItems();
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        SpelunkyRequest.decorateSpelunkyExit(buffer);
      }
    };

    new ChoiceAdventure(1028, "A Shop", "undefined") {
      String lastResponseText = "";

      void setup() {
        this.customName = this.name;
        this.customZones.add("Spelunky Area");

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);

        new CustomOption(5, "chance to fight shopkeeper");
        new CustomOption(6, "leave");
      }

      @Override
      void preChoice(String urlString) {
        this.lastResponseText = ChoiceManager.lastResponseText;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.logShop(this.lastResponseText, decision);

        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1029, "An Old Clay Pot", "The Mines") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 18-20 gold", true);
        new Option(5, "gain pot", true).attachItem("pot", 1, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1030, "It's a Trap!  A Dart Trap.", "The Mines") {
      void setup() {
        this.customName = this.name;

        new Option(1, "escape with whip", true);
        new Option(2, "unlock The Snake Pit using bomb", true);
        new Option(3, "unlock The Spider Hole using rope", true);
        new Option(4, "escape using offhand item", true);
        new Option(6, "take damage", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1031, "A Tombstone", "undefined") {
      void setup() {
        this.customName = this.name;
        this.customZones.add("Spelunky Area");

        new Option(1, "gain 20-25 gold or buddy", true);
        new Option(2, "gain shotgun with pickaxe", true).attachItem("shotgun", 1, AUTO);
        new Option(3, "gain Clown Crown with x-ray specs", true)
            .attachItem("The Clown Crown", 1, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1032, "It's a Trap!  A Tiki Trap.", "The Jungle") {
      void setup() {
        this.customName = this.name;

        new Option(1, "escape with spring boots", true);
        new Option(2, "unlock The Beehive using bomb, take damage without sticky bomb", true);
        new Option(
            3, "unlock The Ancient Burial Ground using rope, take damage without back item", true);
        new Option(6, "lose 30 hp", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1033, "A Big Block of Ice", "The Ice Caves") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 50-60 gold and restore health (with cursed coffee cup)", true);
        new Option(2, "gain buddy (or 60-70 gold) with torch", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1034, "A Landmine", "The Ice Caves") {
      void setup() {
        this.customName = this.name;

        new Option(2, "unlock An Ancient Altar and lose 10 HP", true);
        new Option(3, "unlock The Crashed UFO using 3 ropes", true);
        new Option(6, "lose 30 hp", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1035, "A Crate", "undefined") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1036, "Idolatry", "The Temple Ruins") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 250 gold with Resourceful Kid", true);
        new Option(2, "gain 250 gold with spring boots and yellow cloak", true);
        new Option(3, "gain 250 gold with jetpack", true);
        new Option(4, "gain 250 gold and lose 50 hp", true);
        new Option(6, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1037, "It's a Trap!  A Smashy Trap.", "The Temple Ruins") {
      void setup() {
        this.customName = this.name;

        new Option(2, "unlock The City of Goooold with key, or take damage", true);
        new Option(6, "lose 40 hp", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1038, "A Wicked Web", "The Spider Hole") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 15-20 gold", true);
        new Option(2, "gain buddy (or 20-30 gold) with machete", true);
        new Option(3, "gain 30-50 gold with torch", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1039, "A Golden Chest", "The City of Goooold") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 150 gold with key", true);
        new Option(2, "gain 80-100 gold with bomb", true);
        new Option(3, "gain 50-60 gold and lose 20 hp", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1040, "It's Lump. It's Lump.", "The Snake Pit") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain heavy pickaxe with bomb", true).attachItem("heavy pickaxe", 1, AUTO);
        new Option(6, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1041, "Spelunkrifice", null) {
      void setup() {
        this.customName = this.name;
        this.customZones.add("Spelunky Area");

        new Option(1, "sacrifice buddy", true);
        new Option(6, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1042, "Pick a Perk!", null) {
      void setup() {
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
        SpelunkyRequest.upgrade(decision);
        KoLmafia.resetAfterLimitmode();
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        SpelunkyRequest.decorateSpelunkyExit(buffer);
      }
    };

    new ChoiceAdventure(1043, "Sicking it (the carrot) to 'em", "Item-Driven") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1044, "The Gates of Hell", "LOLmec's Lair") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("unlock the padlock")) {
          SpelunkyRequest.unlock("Hell", "Hell");
        }
      }
    };

    new ChoiceAdventure(1045, "Hostile Work Environment", "undefined") {
      void setup() {
        this.customName = this.name;
        this.customZones.add("Spelunky Area");

        new Option(1, "fight shopkeeper", true);
        new Option(6, "take damage", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        SpelunkyRequest.parseChoice(this.choice, request.responseText, decision);
      }
    };

    new ChoiceAdventure(1046, "Actually Ed the Undying", null) {
      void setup() {
        new Option(1);
      }
    };

    new UnknownChoiceAdventure(1047);

    new RetiredChoiceAdventure(1048, "Twitch Event #8 Time Period", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }
    };

    new ChoiceAdventure(1049, "Tomb of the Unknown Your Class Here", "The Unquiet Garves") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // This adventure is both the tomb riddle, and the weapon switch

        if (request.responseText.contains("The Epic Weapon's yours")) {
          QuestDatabase.setQuestProgress(Quest.NEMESIS, "step3");
        }

        Map<Integer, String> choices = ChoiceUtilities.parseChoices(request.responseText);
        if (choices.size() != 3) {
          return;
        }

        String decision = this.getDecision(request.responseText, "0", Integer.MAX_VALUE);
        if (decision == "0") {
          return;
        }

        for (int i : new int[] {1, 2, 3}) {
          getOption(i).text(decision == String.valueOf(i) ? "right answer" : "wrong answer");
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // This handles every choice in the "The Unknown Tomb"
        // Many of them have a single option.
        Map<Integer, String> choices = ChoiceUtilities.parseChoices(responseText);
        if (choices.size() == 1) {
          return "1";
        }

        // The only one that has more than one option is the initial riddle.
        // The option numbers are randomized each time, although the correct
        // answer remains the same.
        final String answer;
        switch (KoLCharacter.getAscensionClass()) {
          case SEAL_CLUBBER:
            answer = "Boredom.";
            break;
          case TURTLE_TAMER:
            answer = "Friendship.";
            break;
          case PASTAMANCER:
            answer = "Binding pasta thralls.";
            break;
          case SAUCEROR:
            answer = "Power.";
            break;
          case DISCO_BANDIT:
            answer = "Me. Duh.";
            break;
          case ACCORDION_THIEF:
            answer = "Music.";
            break;
          default:
            answer = null;
        }

        // Only standard classes can join the guild, so we
        // should not fail. But, if we do, cope.
        if (answer == null) {
          return "0";
        }

        // Iterate over the option strings and find the one
        // that matches the correct answer.
        for (Map.Entry<Integer, String> entry : choices.entrySet()) {
          if (entry.getValue().contains(answer)) {
            return String.valueOf(entry.getKey());
          }
        }

        // Again, we should not fail, but cope.
        return "0";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        Option option = getOption(decision);

        // bad answer
        if (request.responseText.contains("...Nope")) {
          option.turnCost(1);
        } else if (request.responseText.contains("you dive out of the door of the tomb")) {
          option.turnCost(1);

          AscensionClass ascensionClass = KoLCharacter.getAscensionClass();
          if (ascensionClass != null) {
            option.attachItem(ascensionClass.getStarterWeapon(), -1, MANUAL);
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("Also in this room is a ghost")) {
          QuestDatabase.setQuestProgress(Quest.NEMESIS, "step1");
        } else if (request.responseText.contains("You acquire")) {
          QuestDatabase.setQuestProgress(Quest.NEMESIS, "step4");
        }
      }
    };

    new UnknownChoiceAdventure(1050);

    new ChoiceAdventure(1051, "The Book of the Undying", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        EdBaseRequest.inspectBook(request.responseText);
      }
    };

    new ChoiceAdventure(1052, "Underworld Body Shop", null) {
      final Pattern URL_SKILLID_PATTERN = Pattern.compile("skillid=(\\d+)");

      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        Matcher skillidMatcher = URL_SKILLID_PATTERN.matcher(request.getURLString());
        if (skillidMatcher.find()) {
          int cost = 0;
          switch (StringUtilities.parseInt(skillidMatcher.group(1))) {
            case 30:
              cost = 5;
              break;
            case 31:
            case 36:
            case 39:
            case 40:
            case 43:
            case 44:
              cost = 10;
              break;
            case 32:
              cost = 15;
              break;
            case 33:
            case 37:
            case 38:
            case 41:
            case 42:
            case 45:
            case 48:
              cost = 20;
              break;
            case 34:
              cost = 25;
              break;
            case 28:
            case 29:
            case 35:
            case 46:
              cost = 30;
              break;
          }
          getOption(decision).attachItem(ItemPool.KA_COIN, -cost, MANUAL);
        }
      }
    };

    new ChoiceAdventure(1053, "The Servants' Quarters", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // change
        new Option(3); // release
        new Option(5); // imbue
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return EdServantData.registerRequest(urlString);
      }

      @Override
      void visitChoice(GenericRequest request) {
        EdBaseRequest.inspectServants(request.responseText);
        EdServantData.inspectServants(request.responseText);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        EdServantData.manipulateServants(request, request.responseText);
      }
    };

    new ChoiceAdventure(1054, "Returning the MacGuffin", null) {
      void setup() {
        new Option(1)
            .leadsTo(1055)
            .attachItem(ItemPool.ED_HOLY_MACGUFFIN, -1, MANUAL, new NoDisplay());
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          QuestDatabase.setQuestProgress(Quest.WAREHOUSE, QuestDatabase.FINISHED);
        }
      }
    };

    new ChoiceAdventure(1055, "Returning the MacGuffin", null) { // class selection
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
        KoLCharacter.liberateKing();
        AvatarManager.handleAfterAvatar(decision);
      }
    };

    new ChoiceAdventure(1056, "Now It's Dark", "Twin Peak") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // Twin Peak fourth choice
        if (request.responseText.contains("When the lights come back")) {
          // the other three must be completed at this point.
          Preferences.setInteger("twinPeakProgress", 15);
        }
      }
    };

    new ChoiceAdventure(1057, "A Stone Shrine", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("shatter the")) {
          KoLCharacter.setHippyStoneBroken(true);
        }
      }
    };

    new RetiredChoiceAdventure(1058, "Abandon the Path of Undying", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }
    };

    new ChoiceAdventure(1059, "Helping Make Ends Meat", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // ok
        new Option(2) // here
            .attachItem(ItemPool.MEATSMITH_CHECK, -1, MANUAL, new NoDisplay());
        new Option(3); // will keep looking
        new Option(4); // no
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2 && request.responseText.contains("excitedly takes the check")) {
          QuestDatabase.setQuestProgress(Quest.MEATSMITH, QuestDatabase.FINISHED);
        } else if (decision == 1
                && request.responseText.contains("skeleton store is right next door")
            || decision == 3 && request.responseText.contains("I'll be here")) {
          QuestDatabase.setQuestProgress(Quest.MEATSMITH, QuestDatabase.STARTED);
        }
      }
    };

    new ChoiceAdventure(1060, "Temporarily Out of Skeletons", "The Skeleton Store") {
      void setup() {
        new Option(1, "gain office key, then ~35 meat", true)
            .turnCost(1)
            .attachItem("Skeleton Store office key", 1, AUTO)
            .attachMeat(35, AUTO);
        new Option(
                2,
                "with skeleton key, gain ring of telling skeletons what to do, then 300 meat. Otherwise, muscle substats",
                true)
            .turnCost(1)
            .attachItem(ItemPool.SKELETON_KEY, -1, MANUAL, new DisplayAll("key", WANT, AT_LEAST, 1))
            .attachItem(ItemPool.RING_OF_TELLING_SKELETONS_WHAT_TO_DO, 1, AUTO)
            .attachMeat(300, AUTO);
        new Option(3, "gain muscle stats", true).turnCost(1);
        new Option(4, "fight former owner of the Skeleton Store, with office key", true)
            .attachItem("Skeleton Store office key", -1, MANUAL);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("4")
            && QuestDatabase.isQuestLaterThan(Quest.MEATSMITH, QuestDatabase.STARTED)) {
          // Can only fight owner til defeated
          return "0";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2 && !request.responseText.contains("it snaps off")) {
          // still cost a turn
          this.choiceFailed().turnCost(1);
        }
      }
    };

    new ChoiceAdventure(1061, "Heart of Madness", "Madness Bakery") {
      void setup() {
        new Option(1, "try to enter office", true);
        new Option(2, "bagel machine", true).leadsTo(1080, false, o -> o.index == 1);
        new Option(3, "popular machine", true).leadsTo(1084, false, o -> o.index == 1);
        new Option(4, "learn recipe", true).turnCost(1);
        new Option(5, "~15 mysticality substats", true).turnCost(1);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("1") && QuestDatabase.isQuestLaterThan(Quest.ARMORER, "step4")) {
          // Can only enter office til Cake Lord is defeated
          return "0";
        } else if (decision.equals("3") && !QuestDatabase.isQuestFinished(Quest.ARMORER)) {
          // Can only access Popular machine after quest complete
          return "0";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("place the popular part")) {
          getOption(decision).attachItem(ItemPool.POPULAR_PART, -1, MANUAL);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          QuestDatabase.setQuestIfBetter(Quest.ARMORER, "step1");
        }
      }
    };

    new ChoiceAdventure(1062, "Lots of Options", "The Overgrown Lot") {
      void setup() {
        new Option(1, "acquire flowers", true)
            .turnCost(1)
            .attachItem("fraudwort", 1, AUTO, new NoDisplay())
            .attachItem("shysterweed", 1, AUTO, new NoDisplay())
            .attachItem("swindleblossom", 1, AUTO, new NoDisplay());
        new Option(2, "acquire food", true)
            .turnCost(1)
            .attachItem("carton of snake milk", 1, AUTO, new ImageOnly())
            .attachItem("pigeon egg", 1, AUTO, new ImageOnly())
            .attachItem("wad of dough", 1, AUTO, new ImageOnly());
        new Option(3, "acquire booze", true)
            .turnCost(1)
            .attachItem("premium malt liquor", 1, AUTO, new ImageOnly())
            .attachItem("bottle of vodka", 1, AUTO, new ImageOnly())
            .attachItem("bottle of whiskey", 1, AUTO, new ImageOnly())
            .attachItem("unflavored wine cooler", 1, AUTO, new ImageOnly());
        new Option(4, "moxie substats", true).turnCost(1);
        new Option(5, "acquire more booze with map", true)
            .turnCost(1)
            .attachItem(ItemPool.BOOZE_MAP, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem("premium malt liquor", 1, AUTO, new ImageOnly("booze"))
            .attachItem("bottle of vodka", 1, AUTO, new ImageOnly("booze"))
            .attachItem("bottle of whiskey", 1, AUTO, new ImageOnly("booze"))
            .attachItem("unflavored wine cooler", 1, AUTO, new ImageOnly("booze"));
      }
    };

    new ChoiceAdventure(1063, "Adjust your 'Edpiece", "Item-Driven") {
      final Pattern EDPIECE_PATTERN =
          Pattern.compile("<p>The crown is currently adorned with a golden (.*?).<center>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1, "Muscle +20, +2 Muscle Stats Per Fight");
        new Option(2, "Mysticality +20, +2 Mysticality Stats Per Fight");
        new Option(3, "Moxie +20, +2 Moxie Stats Per Fight");
        new Option(4, "+20 to Monster Level");
        new Option(5, "+10% Item Drops from Monsters, +20% Meat from Monsters");
        new Option(
            6, "The first attack against you will always miss, Regenerate 10-20 HP per Adventure");
        new Option(7, "Lets you breathe underwater");
        new Option(8);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        int index = decision - 1;
        if (index < 0 || index > EdPieceCommand.ANIMAL.length) {
          // Doing nothing
          return true;
        }
        String decoration = EdPieceCommand.ANIMAL[index][0];
        RequestLogger.updateSessionLog();
        RequestLogger.updateSessionLog("edpiece " + decoration);
        return true;
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = EDPIECE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("edPiece", matcher.group(1).trim());
        }
      }
    };

    new ChoiceAdventure(1064, "The Doctor is Out.  Of Herbs.", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // ok
        new Option(2) // here they are
            .attachItem(ItemPool.FRAUDWORT, -3, MANUAL, new NoDisplay())
            .attachItem(ItemPool.SHYSTERWEED, -3, MANUAL, new NoDisplay())
            .attachItem(ItemPool.SWINDLEBLOSSOM, -3, MANUAL, new NoDisplay());
        new Option(3); // I'll keep looking
        new Option(4); // don't have time
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          QuestDatabase.setQuestProgress(Quest.DOC, QuestDatabase.STARTED);
        } else if (decision == 2) {
          QuestDatabase.setQuestProgress(Quest.DOC, QuestDatabase.FINISHED);
          HPRestoreItemList.updateHealthRestored();
          MPRestoreItemList.updateManaRestored();
        }
      }
    };

    new ChoiceAdventure(1065, "Lending a Hand (and a Foot)", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // ok
        new Option(2); // here it is
        new Option(3); // brb
        new Option(4); // manipulative tactics
        new Option(6); // leave
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 || decision == 3) {
          QuestDatabase.setQuestProgress(Quest.ARMORER, QuestDatabase.STARTED);
        }
      }
    };

    new ChoiceAdventure(1066, "Employee Assignment Kiosk", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(6);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("Performance Review:  Sufficient")) {
          EquipmentManager.discardEquipment(ItemPool.TRASH_NET);
          ResultProcessor.removeItem(ItemPool.TRASH_NET);
          Preferences.setInteger("dinseyFilthLevel", 0);
          QuestDatabase.setQuestProgress(Quest.FISH_TRASH, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Unobjectionable")) {
          ResultProcessor.processItem(ItemPool.TOXIC_GLOBULE, -20);
          QuestDatabase.setQuestProgress(Quest.GIVE_ME_FUEL, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Bearable")) {
          Preferences.setInteger("dinseyNastyBearsDefeated", 0);
          QuestDatabase.setQuestProgress(Quest.NASTY_BEARS, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Acceptable")) {
          Preferences.setInteger("dinseySocialJusticeIProgress", 0);
          QuestDatabase.setQuestProgress(Quest.SOCIAL_JUSTICE_I, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Fair")) {
          Preferences.setInteger("dinseySocialJusticeIIProgress", 0);
          QuestDatabase.setQuestProgress(Quest.SOCIAL_JUSTICE_II, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Average")) {
          EquipmentManager.discardEquipment(ItemPool.LUBE_SHOES);
          ResultProcessor.removeItem(ItemPool.LUBE_SHOES);
          QuestDatabase.setQuestProgress(Quest.SUPER_LUBER, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Adequate")) {
          Preferences.setInteger("dinseyTouristsFed", 0);
          ResultProcessor.removeItem(ItemPool.DINSEY_REFRESHMENTS);
          QuestDatabase.setQuestProgress(Quest.WORK_WITH_FOOD, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("Performance Review:  Tolerable")) {
          EquipmentManager.discardEquipment(ItemPool.MASCOT_MASK);
          ResultProcessor.removeItem(ItemPool.MASCOT_MASK);
          Preferences.setInteger("dinseyFunProgress", 0);
          QuestDatabase.setQuestProgress(Quest.ZIPPITY_DOO_DAH, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("weren't kidding about the power")) {
          if (InventoryManager.getCount(ItemPool.TOXIC_GLOBULE) >= 20) {
            QuestDatabase.setQuestProgress(Quest.GIVE_ME_FUEL, "step1");
          } else {
            QuestDatabase.setQuestProgress(Quest.GIVE_ME_FUEL, QuestDatabase.STARTED);
          }
        } else if (request.responseText.contains("anatomical diagram of a nasty bear")) {
          QuestDatabase.setQuestProgress(Quest.NASTY_BEARS, QuestDatabase.STARTED);
        } else if (request.responseText.contains("lists all of the sexist aspects of the ride")) {
          QuestDatabase.setQuestProgress(Quest.SOCIAL_JUSTICE_I, QuestDatabase.STARTED);
        } else if (request.responseText.contains("ideas are all themselves so racist")) {
          QuestDatabase.setQuestProgress(Quest.SOCIAL_JUSTICE_II, QuestDatabase.STARTED);
        } else if (request.responseText.contains("box of snacks issues forth")) {
          Preferences.setInteger("dinseyTouristsFed", 0);
          QuestDatabase.setQuestProgress(Quest.WORK_WITH_FOOD, QuestDatabase.STARTED);
        }
      }
    };

    new ChoiceAdventure(1067, "Maint Misbehavin'", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).leadsTo(1068);
        new Option(2).leadsTo(1069);
        new Option(3).leadsTo(1070);
        new Option(4).leadsTo(1071);
        new Option(5, "fight Wart Dinsey")
            .attachItem("keycard &alpha;", new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem("keycard &beta;", new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem("keycard &gamma;", new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem("keycard &delta;", new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6)
            .attachItem(ItemPool.GARBAGE_BAG, -1, MANUAL, new DisplayAll(WANT, AT_LEAST, 1))
            .attachItem(ItemPool.FUNFUNDS, 3, AUTO, new NoDisplay());
        new Option(7);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 6) {
          if (request.responseText.contains("throw a bag of garbage into it")) {
            Preferences.setBoolean("_dinseyGarbageDisposed", true);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(1068, "Barf Mountain Breakdown", null) {
      final Pattern DINSEY_ROLLERCOASTER_PATTERN =
          Pattern.compile("rollercoaster is currently set to (.*?) Mode");

      void setup() {
        new Option(1, "switch which stat the statgains from rollercoasters are tuned to");
        new Option(2, "toggle faster occurences of rollercoasters");
        new Option(6).leadsTo(1067);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Matcher matcher = DINSEY_ROLLERCOASTER_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            String rollerCoasterMode = matcher.group(1).trim();
            getOption(1)
                .text(
                    "rollercoaster statgains will "
                        + (rollerCoasterMode.equals("Super-Fast")
                            ? " no longer be tuned"
                            : "be tuned towards "
                                + (rollerCoasterMode.equals("Standard")
                                    ? "muscle"
                                    : rollerCoasterMode.equals("Extra-Violent")
                                        ? "mysticality"
                                        : rollerCoasterMode.equals("Awe-Inspiring")
                                            ? "moxie"
                                            : "...something?")));
            Preferences.setString("dinseyRollercoasterStats", rollerCoasterMode);
          }

          boolean rapidPass = request.responseText.contains("Disable Rapid-Pass System");
          getOption(2)
              .text(
                  (rapidPass ? "30" : "20") + " tourists between each occurence of rollercoasters");
          Preferences.setBoolean("dinseyRapidPassEnabled", rapidPass);
        }
      }
    };

    new ChoiceAdventure(1069, "The Pirate Bay", null) {
      final Pattern DINSEY_PIRATE_PATTERN = Pattern.compile("'Updated Pirate' is (lit|dark)");

      void setup() {
        new Option(1, "toggle presence of flashy pirate");
        new Option(6).leadsTo(1067);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Matcher matcher = DINSEY_PIRATE_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            boolean garbageAnimatronicActivated = matcher.group(1).equals("lit");
            getOption(1)
                .text(
                    (garbageAnimatronicActivated ? "remove" : "add")
                        + " flashy pirate "
                        + (garbageAnimatronicActivated ? "from" : "to")
                        + " the zone");
            Preferences.setBoolean("dinseyGarbagePirate", garbageAnimatronicActivated);
          }
        }
      }
    };

    new ChoiceAdventure(1070, "In Your Cups", null) {
      final Pattern DINSEY_TEACUP_PATTERN =
          Pattern.compile("'Current Teacup Spin Rate' points to (\\d+),000 RPM");

      void setup() {
        new Option(1, "change how many turns of Toxic Vengeance you get per fight")
            .attachEffect("Toxic Vengeance", new ImageOnly("Vengeance"));
        new Option(2, "toggle ML boost");
        new Option(6).leadsTo(1067);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Matcher matcher = DINSEY_TEACUP_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            int toxicMultiplier = StringUtilities.parseInt(matcher.group(1).trim());
            getOption(1)
                .text(
                    "receive "
                        + (toxicMultiplier == 5
                            ? "only 1 turn"
                            : String.valueOf(toxicMultiplier + 1) + " turns")
                        + " of Toxic Vengeance per fight in the zone");
            Preferences.setInteger("dinseyToxicMultiplier", toxicMultiplier);
          }

          boolean protocolsRelaxed = request.responseText.contains("protocols seem pretty loose");
          getOption(2).text((protocolsRelaxed ? "decrease" : "increase") + " zone ML by 55");
          Preferences.setBoolean("dinseySafetyProtocolsLoose", protocolsRelaxed);
        }
      }
    };

    new ChoiceAdventure(1071, "Gator Gamer", null) {
      final Pattern DINSEY_SLUICE_PATTERN =
          Pattern.compile("'Sluice Swishers' is currently in the (.*?) position");

      void setup() {
        new Option(1, "toggle passive stench damage in Uncle Gator's");
        new Option(2, "toggle ML boost and effectiveness");
        new Option(6).leadsTo(1067);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Matcher matcher = DINSEY_SLUICE_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            String sluiceMode = matcher.group(1).trim();
            getOption(1)
                .text("turn zone passive damage " + (sluiceMode.equals("ON") ? "OFF" : "ON"));
            Preferences.setString("dinseyGatorStenchDamage", sluiceMode);
          }

          boolean highEngagement = request.responseText.contains("High Engagement Mode");
          getOption(2)
              .text((highEngagement ? "revert" : "increase") + " zone ML and its effectiveness");
          Preferences.setBoolean("dinseyAudienceEngagement", highEngagement);
        }
      }
    };

    new UnknownChoiceAdventure(1072);

    new ChoiceAdventure(1073, "This Ride Is Like... A Rollercoaster Baby Baby", "Barf Mountain") {
      final AdventureResult LUBE_SHOES = ItemPool.get(ItemPool.LUBE_SHOES);

      void setup() {
        this.isSuperlikely = true;

        new Option(1, "gain stats and meat", true).turnCost(1).attachMeat(1750, AUTO);
        new Option(6, "skip adventure and guarantees this adventure will reoccur", true)
            .entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (KoLCharacter.hasEquipped(LUBE_SHOES)) {
          getOption(1).text("lubricate the tracks");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("dinseyRollercoasterNext", false);
        }
        if (request.responseText.contains("lubricating every inch of the tracks")) {
          QuestDatabase.setQuestProgress(Quest.SUPER_LUBER, "step2");
        }
      }
    };

    new ChoiceAdventure(1074, "Welcome to the Copperhead Club", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).leadsTo(851);
        new Option(2);
      }

      // TODO show where he'll send you if you meet him today (and if you wait)
    };

    new ChoiceAdventure(1075, "Mmmmmmayonnaise", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TurnCounter.stopCounting("Mmmmmmayonnaise window begin");
        TurnCounter.stopCounting("Mmmmmmayonnaise window end");
      }
    };

    new ChoiceAdventure(1076, "Mayo Minder&trade;", "Item-Driven") {
      final Pattern MAYO_MINDER_PATTERN =
          Pattern.compile("currently loaded up with packets of (.*?)<p>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = MAYO_MINDER_PATTERN.matcher(request.responseText);
        Preferences.setString("mayoMinderSetting", matcher.find() ? matcher.group(1).trim() : "");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
            Preferences.setString("mayoMinderSetting", "Mayonex");
            break;
          case 2:
            Preferences.setString("mayoMinderSetting", "Mayodiol");
            break;
          case 3:
            Preferences.setString("mayoMinderSetting", "Mayostat");
            break;
          case 4:
            Preferences.setString("mayoMinderSetting", "Mayozapine");
            break;
          case 5:
            Preferences.setString("mayoMinderSetting", "Mayoflex");
            break;
          case 6:
            Preferences.setString("mayoMinderSetting", "");
            break;
        }
      }
    };

    new ChoiceAdventure(1077, "One Crazy Random Summer", null) {
      void setup() {
        new Option(1);
      }
    };

    new UnknownChoiceAdventure(1078);

    new UnknownChoiceAdventure(1079);

    new ChoiceAdventure(1080, "Bagelmat-5000", "Madness Bakery") {
      void setup() {
        this.customName = this.name;

        new Option(1, "wad of dough -> 3 plain bagels", true)
            .attachItem(ItemPool.DOUGH, -1, MANUAL, new DisplayAll("dough", NEED, AT_LEAST, 1))
            .attachItem("plain bagel", 3, AUTO);
        new Option(2, "return to Madness Bakery (1)", true).turnCost(1).leadsTo(1061);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("shove a wad of dough into the slot")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1081, "Assault and Baguettery", "Item-Driven") {
      void setup() {
        this.customName = "magical baguette";

        new Option(1, null, true)
            .attachItem("breadwand", 1, AUTO)
            .attachItem(ItemPool.MAGICAL_BAGUETTE, -1, MANUAL, new NoDisplay());
        new Option(2, null, true)
            .attachItem("loafers", 1, AUTO)
            .attachItem(ItemPool.MAGICAL_BAGUETTE, -1, MANUAL, new NoDisplay());
        new Option(3, null, true)
            .attachItem("bread basket", 1, AUTO)
            .attachItem(ItemPool.MAGICAL_BAGUETTE, -1, MANUAL, new NoDisplay());
        new Option(4, "make nothing", true);
      }
    };

    new ChoiceAdventure(1082, "The \"Rescue\"", "Madness Bakery") {
      void setup() {
        new Option(1).leadsTo(1083);
      }
    };

    new ChoiceAdventure(1083, "Cogito Ergot Sum", "Madness Bakery") {
      void setup() {
        new Option(1).turnCost(1);
      }
    };

    new ChoiceAdventure(1084, "The Popular Machine", "Madness Bakery") {
      void setup() {
        this.customName = this.name;

        new Option(1, "dough, berry, icing -> popular tart")
            .turnCost(1)
            .attachItem(ItemPool.DOUGH, -1, MANUAL, new DisplayAll("dough", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.STRAWBERRY, -1, MANUAL, new DisplayAll("berry", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.ENCHANTED_ICING, -1, MANUAL, new DisplayAll("icing", NEED, AT_LEAST, 1))
            .attachItem("popular tart", 1, AUTO);
        new Option(2, "return to Madness Bakery", true).leadsTo(1061);

        new CustomOption(1, "make popular tart");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Preferences.setBoolean("popularTartUnlocked", true);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("popular tart springs")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1085, "Deck of Every Card", "Item-Driven") {
      void setup() {
        new Option(1);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return DeckOfEveryCardRequest.parseCardEncounter(responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.increment("_deckCardsDrawn", 1, 15, false);
        }
        DeckOfEveryCardRequest.postChoice2(request.responseText);
      }
    };

    new ChoiceAdventure(1086, "Pick a Card", "Item-Driven") {
      void setup() {
        new Option(1).leadsTo(1085);
        new Option(2);
      }

      @Override
      void preChoice(String urlString) {
        // Selecting option 1 directly redirects us to choice 1085...
        // so we literally can't access any of our postChoice methods...
        //
        // sigh...
        //
        // we'll have to make up with handling our decision from preChoice...
        // this will get processed even if the user submits this URL from
        // outside choice 1086, but still... we just don't have a choice...

        if (ChoiceManager.getLastDecision() == 1) {
          // The extra 1 will be covered in choice 1085
          Preferences.increment("_deckCardsDrawn", 4, 15, false);
        }
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }
    };

    new ChoiceAdventure(1087, "The Dark and Dank and Sinister Cave Entrance", null) {
      void setup() {
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
        new Option(9);
        new Option(10);
        new Option(11);
      }

      @Override
      void visitChoice(GenericRequest request) {
        QuestDatabase.setQuestIfBetter(Quest.NEMESIS, "step11");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        Map<Integer, String> choices = ChoiceUtilities.parseChoices(responseText);
        if (choices.size() == 1) {
          return "1";
        }

        final String answer;
        switch (KoLCharacter.getAscensionClass()) {
          case SEAL_CLUBBER:
            answer = "Freak the hell out like a wrathful wolverine.";
            break;
          case TURTLE_TAMER:
            answer = "Sympathize with an amphibian.";
            break;
          case PASTAMANCER:
            answer = "Entangle the wall with noodles.";
            break;
          case SAUCEROR:
            answer = "Shoot a stream of sauce at the wall.";
            break;
          case DISCO_BANDIT:
            answer = "Focus on your disco state of mind.";
            break;
          case ACCORDION_THIEF:
            answer = "Bash the wall with your accordion.";
            break;
          default:
            answer = null;
        }

        // Only standard classes can join the guild, so we
        // should not fail. But, if we do, cope.
        if (answer == null) {
          return "0";
        }

        // Iterate over the option strings and find the one
        // that matches the correct answer.
        for (Map.Entry<Integer, String> entry : choices.entrySet()) {
          if (entry.getValue().contains(answer)) {
            return String.valueOf(entry.getKey());
          }
        }

        // Again, we should not fail, but cope.
        return "0";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (!request.responseText.contains("turn and head back out of the cave")
            && !request.responseText.contains("revealing the entrance to an underground cavern")
            && !request.responseText.contains("stumpy-legged mushroom creatures")) {
          // failed attempts cost a turn
          getOption(decision).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("stumpy-legged mushroom creatures")) {
          QuestDatabase.setQuestProgress(Quest.NEMESIS, "step12");
        }
      }
    };

    new ChoiceAdventure(1088, "Rubble, Rubble, Toil and Trouble", null) {
      void setup() {
        new Option(1)
            .attachItem(ItemPool.FIZZING_SPORE_POD, -6, MANUAL, new DisplayAll(NEED, AT_LEAST, 6));
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        QuestDatabase.setQuestIfBetter(Quest.NEMESIS, "step13");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("BOOOOOOM!")) {
            QuestDatabase.setQuestProgress(Quest.NEMESIS, "step15");
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(1089, "Community Service", null) {
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
        new Option(10);
        new Option(11);
        new Option(30);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("You acquire")) {
          String quest = null;
          switch (decision) {
            case 1:
              quest = "Donate Blood";
              break;
            case 2:
              quest = "Feed The Children";
              break;
            case 3:
              quest = "Build Playground Mazes";
              break;
            case 4:
              quest = "Feed Conspirators";
              break;
            case 5:
              quest = "Breed More Collies";
              break;
            case 6:
              quest = "Reduce Gazelle Population";
              break;
            case 7:
              quest = "Make Sausage";
              break;
            case 8:
              quest = "Be a Living Statue";
              break;
            case 9:
              quest = "Make Margaritas";
              break;
            case 10:
              quest = "Clean Steam Tunnels";
              break;
            case 11:
              quest = "Coil Wire";
              break;
          }
          if (quest != null) {
            String current = Preferences.getString("csServicesPerformed");
            if (current.equals("")) {
              Preferences.setString("csServicesPerformed", quest);
            } else {
              Preferences.setString("csServicesPerformed", current + "," + quest);
            }
          }
        }
      }
    };

    new ChoiceAdventure(1090, "The Towering Inferno Discotheque", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3).attachEffect("Doing The Hustle");
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7).attachItem(ItemPool.VOLCOINO, 1, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision > 1) {
          Preferences.setBoolean("_infernoDiscoVisited", true);
        }
      }
    };

    new ChoiceAdventure(1091, "The Floor Is Yours", "LavaCo&trade; Lamp Factory") {
      final AdventureResult GOLD_1970 = ItemPool.get(ItemPool.GOLD_1970, -1);
      final AdventureResult NEW_AGE_HEALING_CRYSTAL =
          ItemPool.get(ItemPool.NEW_AGE_HEALING_CRYSTAL, -1);
      final AdventureResult EMPTY_LAVA_BOTTLE = ItemPool.get(ItemPool.EMPTY_LAVA_BOTTLE, 1);
      final AdventureResult VISCOUS_LAVA_GLOBS = ItemPool.get(ItemPool.VISCOUS_LAVA_GLOBS);
      final AdventureResult GLOWING_NEW_AGE_CRYSTAL =
          ItemPool.get(ItemPool.GLOWING_NEW_AGE_CRYSTAL, -1);
      final AdventureResult CRYSTALLINE_LIGHT_BULB =
          ItemPool.get(ItemPool.CRYSTALLINE_LIGHT_BULB, 1);
      final AdventureResult HEAT_RESISTANT_SHEET_METAL =
          ItemPool.get(ItemPool.HEAT_RESISTANT_SHEET_METAL, -1);
      final AdventureResult INSULATED_GOLD_WIRE = ItemPool.get(ItemPool.INSULATED_GOLD_WIRE, -1);

      void setup() {
        new Option(1, "1,970 carat gold -> thin gold wire", true)
            .turnCost(1)
            .attachItem(GOLD_1970, MANUAL, new DisplayAll("carat gold", NEED, AT_LEAST, 1))
            .attachItem("thin gold wire", 1, AUTO);
        new Option(2, "New Age healing crystal -> empty lava bottle", true)
            .turnCost(1)
            .attachItem(
                NEW_AGE_HEALING_CRYSTAL, MANUAL, new DisplayAll("crystal", NEED, AT_LEAST, 1))
            .attachItem(EMPTY_LAVA_BOTTLE, AUTO);
        new Option(3, "empty lava bottle -> full lava bottle", true)
            .turnCost(1)
            .attachItem(
                EMPTY_LAVA_BOTTLE.getInstance(-1),
                MANUAL,
                new DisplayAll("empty lava bottle", NEED, AT_LEAST, 1))
            .attachItem("full lava bottle", 1, AUTO);
        new Option(4, "make colored lava globs", true)
            .leadsTo(1092)
            .attachItem(VISCOUS_LAVA_GLOBS, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(5, "glowing New Age crystal -> crystalline light bulb", true)
            .turnCost(1)
            .attachItem(
                GLOWING_NEW_AGE_CRYSTAL, MANUAL, new DisplayAll("Age crystal", NEED, AT_LEAST, 1))
            .attachItem(CRYSTALLINE_LIGHT_BULB, AUTO);
        new Option(
                6,
                "crystalline light bulb + insulated wire + heat-resistant sheet metal -> LavaCo&trade; Lamp housing",
                true)
            .turnCost(1)
            .attachItem(
                CRYSTALLINE_LIGHT_BULB.getInstance(-1),
                MANUAL,
                new DisplayAll("bulb", NEED, AT_LEAST, 1))
            .attachItem(
                HEAT_RESISTANT_SHEET_METAL, MANUAL, new DisplayAll("metal", NEED, AT_LEAST, 1))
            .attachItem(INSULATED_GOLD_WIRE, MANUAL, new DisplayAll("wire", NEED, AT_LEAST, 1))
            .attachItem("LavaCo&trade; Lamp housing", 1, AUTO);
        new Option(7, "fused fuse", true).turnCost(1).attachItem("fused fuse", 1, AUTO);
        new Option(9).entersQueue(false);

        new CustomOption(9, "leave");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("1") && GOLD_1970.getCount(KoLConstants.inventory) < 1) {
          // Manual Control if don't have 1,970 carat gold
          return "0";
        } else if (decision.equals("2")
            && NEW_AGE_HEALING_CRYSTAL.getCount(KoLConstants.inventory) < 1) {
          // Manual Control if don't have New Age healing crystal
          return "0";
        } else if (decision.equals("3") && EMPTY_LAVA_BOTTLE.getCount(KoLConstants.inventory) < 1) {
          // Manual Control if don't have empty lava bottle
          return "0";
        } else if (decision.equals("4")
            && VISCOUS_LAVA_GLOBS.getCount(KoLConstants.inventory) < 1) {
          // Manual Control if don't have viscous lava globs
          return "0";
        } else if (decision.equals("5")
            && GLOWING_NEW_AGE_CRYSTAL.getCount(KoLConstants.inventory) < 1) {
          // Manual Control if don't have glowing New Age crystal
          return "0";
        }

        // 6: "crystalline light bulb + insulated wire + heat-resistant sheet metal -> LavaCo&trade;
        // Lamp housing"
        // This exits choice if you don't have the ing1redients
        // 7: "fused fuse"
        // Doesn't require materials
        // 9: "leave"

        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision != 4 && decision != 9 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1092, "Dyer Maker", "LavaCo&trade; Lamp Factory") {
      final AdventureResult VISCOUS_LAVA_GLOBS = ItemPool.get(ItemPool.VISCOUS_LAVA_GLOBS, -1);

      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(VISCOUS_LAVA_GLOBS, MANUAL, new NoDisplay())
            .attachItem("red lava globs", 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(VISCOUS_LAVA_GLOBS, MANUAL, new NoDisplay())
            .attachItem("blue lava globs", 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(VISCOUS_LAVA_GLOBS, MANUAL, new NoDisplay())
            .attachItem("green lava globs", 1, AUTO);
        new Option(4).leadsTo(1091);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision != 4 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1093, "The WLF Bunker", null) {
      final Pattern WLF_PATTERN =
          Pattern.compile(
              "<form action=choice\\.php>.*?<b>(.*?)</b>.*?descitem\\((.*?)\\).*?>(.*?)<.*?name=option value=([\\d]*).*?</form>",
              Pattern.DOTALL);
      final Pattern WLF_COUNT_PATTERN = Pattern.compile(".*? \\(([\\d]+)\\)$");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // There is no choice if this happens, but we recognise title in visitChoice()
        // You enter the bunker, but the speaker is silent. You've already done your day's work,
        // soldier!
        if (request.responseText.contains("the speaker is silent")) {
          Preferences.setBoolean("_volcanoItemRedeemed", true);
          Preferences.setInteger("_volcanoItem1", 0);
          Preferences.setInteger("_volcanoItem2", 0);
          Preferences.setInteger("_volcanoItem3", 0);
          Preferences.setInteger("_volcanoItemCount1", 0);
          Preferences.setInteger("_volcanoItemCount2", 0);
          Preferences.setInteger("_volcanoItemCount3", 0);
          return;
        }

        // On the other hand, if there IS a choice on the page,
        // it will be whichchoice=1093 asking to redeem items

        Preferences.setBoolean("_volcanoItemRedeemed", false);

        Matcher matcher = WLF_PATTERN.matcher(request.responseText);
        while (matcher.find()) {
          // String challenge = matcher.group(1);
          String descid = matcher.group(2);
          int itemId = ItemDatabase.getItemIdFromDescription(descid);
          if (itemId != -1) {
            String itemName = matcher.group(3).trim();
            Matcher countMatcher = WLF_COUNT_PATTERN.matcher(itemName);
            int count = countMatcher.find() ? StringUtilities.parseInt(countMatcher.group(1)) : 1;
            String index = matcher.group(4);
            Preferences.setInteger("_volcanoItem" + index, itemId);
            Preferences.setInteger("_volcanoItemCount" + index, count);
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // A woman in an orange denim jumpsuit emerges from a
        // hidden trap door, smiles, and hands you a coin in
        // exchange for your efforts.

        if (request.responseText.contains("hands you a coin")) {
          String index = String.valueOf(decision);
          int itemId = Preferences.getInteger("_volcanoItem" + index);
          int count = Preferences.getInteger("_volcanoItemCount" + index);
          if (itemId > 0 && count > 0) {
            getOption(decision).attachItem(itemId, -count, MANUAL);
          }
          Preferences.setBoolean("_volcanoItemRedeemed", true);
          Preferences.setInteger("_volcanoItem1", 0);
          Preferences.setInteger("_volcanoItem2", 0);
          Preferences.setInteger("_volcanoItem3", 0);
          Preferences.setInteger("_volcanoItemCount1", 0);
          Preferences.setInteger("_volcanoItemCount2", 0);
          Preferences.setInteger("_volcanoItemCount3", 0);
        }
      }
    };

    new ChoiceAdventure(1094, "Back Room SMOOCHing", "The SMOOCH Army HQ") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5).turnCost(1).attachItem("SMOOCH coffee cup", 1, AUTO, new NoDisplay());

        new CustomOption(1, "fight Geve Smimmons");
        new CustomOption(2, "fight Raul Stamley");
        new CustomOption(3, "fight Pener Crisp");
        new CustomOption(4, "fight Deuce Freshly");
        new CustomOption(5, "acquire SMOOCH coffee cup");
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        // unique presentation

        // <table callspacing=0 callpadding=0><tbody><tr>
        // <td><a href="choice.php?whichchoice=1094&option=<x>&pwd=...">
        // <img src=choice_data/smoochdoor<x>.gif alt="Door #<x>" title="Door #<x>" width=100
        // height=100 border=0>
        // </a></td>
        // <td>(...)</td>
        // <td>(...)</td>
        // <td>(...)</td>
        // <td>(...)</td>
        // </tr></tbody></table>

        int choice = Preferences.getInteger("choiceAdventure1094");
        String find = "smoochdoor" + choice + ".gif";
        String replace = "smoochdoor" + choice + ".gif style=\"border: 2px solid blue;\"";
        if (buffer.toString().contains(find)) {
          StringUtilities.singleStringReplace(buffer, find, replace);
        }
        StringUtilities.globalStringReplace(buffer, "Door #1", "Geve Smimmons");
        StringUtilities.globalStringReplace(buffer, "Door #2", "Raul Stamley");
        StringUtilities.globalStringReplace(buffer, "Door #3", "Pener Crisp");
        StringUtilities.globalStringReplace(buffer, "Door #4", "Deuce Freshly");
      }
    };

    new ChoiceAdventure(1095, "Tin Roof -- Melted", "The Velvet / Gold Mine") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight Mr. Choch", true);
        new Option(2, "acquire half-melted hula girl", true)
            .turnCost(1)
            .attachItem("half-melted hula girl", 1, AUTO);
      }
    };

    new ChoiceAdventure(1096, "Re-Factory Period", "LavaCo&trade; Lamp Factory") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight Mr. Cheeng", true);
        new Option(2, "acquire glass ceiling fragments", true)
            .turnCost(1)
            .attachItem("glass ceiling fragments", 1, AUTO);
      }
    };

    new ChoiceAdventure(1097, "Who You Gonna Caldera?", "The Bubblin' Caldera") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "acquire The One Mood Ring", true)
            .turnCost(1)
            .attachItem("The One Mood Ring", 1, AUTO);
        new Option(2, "fight Lavalos", true);
      }
    };

    new UnknownChoiceAdventure(1098);

    new ChoiceAdventure(1099, "The Barrel Full of Barrels", "The Barrel Full of Barrels") {
      void setup() {
        this.canWalkFromChoice = true;

        this.customName = "Barrel full of Barrels";

        new Option(1); // barrels
        new Option(2); // turn crank
        new Option(3); // exit

        // put at 0 to prevent a "show in browser" from being added
        new CustomOption(
            0,
            "top rows (mixed drinks)",
            () -> {
              Preferences.setInteger("barrelGoal", 1);
            });
        new CustomOption(
            2,
            "middle rows (basic booze)",
            () -> {
              Preferences.setInteger("barrelGoal", 2);
            });
        new CustomOption(
            3,
            "top & middle rows",
            () -> {
              Preferences.setInteger("barrelGoal", 3);
            });
        new CustomOption(
            4,
            "bottom rows (schnapps, fine wine)",
            () -> {
              Preferences.setInteger("barrelGoal", 4);
            });
        new CustomOption(
            5,
            "top & bottom rows",
            () -> {
              Preferences.setInteger("barrelGoal", 5);
            });
        new CustomOption(
            6,
            "middle & bottom rows",
            () -> {
              Preferences.setInteger("barrelGoal", 6);
            });
        new CustomOption(
            7,
            "all available drinks",
            () -> {
              Preferences.setInteger("barrelGoal", 7);
            });

        this.customLoad =
            () -> {
              int index = Preferences.getInteger("barrelGoal");
              switch (index) {
                default:
                  System.out.println("Invalid setting " + index + " for barrelGoal");
                  // fall-through
                case 1:
                  return 0;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                  return index;
              }
            };

        this.customPreferencesToListen.add("barrelGoal");
      }
    };
  }
}
