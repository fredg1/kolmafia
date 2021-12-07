package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ConcoctionDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.AdventureRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.TurnCounter;
import net.sourceforge.kolmafia.session.VioletFogManager;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase1to99 extends ChoiceAdventureDatabase {
  final void add1to99() {
    new RetiredChoiceAdventure(1, "The Choice!", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }
    };

    new ChoiceAdventure(2, "Denim Axes Examined", "Inside the Palindome") {
      void setup() {
        new Option(1, "trade rubber axe for denim axe")
            .turnCost(1)
            .attachItem("denim axe", 1, AUTO)
            .attachItem(
                "rubber axe", -1, MANUAL, new DisplayAll("rubber axe", NEED, INV_ONLY_AT_LEAST, 1));
        new Option(2, "skip adventure").entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // You make the trade and go on your merry way, examining your new denim axe.
        if (decision == 1 && !request.responseText.contains("examining your new denim axe")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(3, "The Oracle Will See You Now", "The Enormous Greater-Than Sign") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "skip adventure");
        new Option(2, "waste a turn and 100 meat").turnCost(1).attachMeat(-100, AUTO);
        new Option(3, "make plus sign usable").turnCost(1).attachMeat(-1000, AUTO);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2 && !request.responseText.contains("Your Minor Consultation is this")
            || decision == 3 && !request.responseText.contains("Your Major Consultation is this")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("actually a book")) {
          Preferences.setInteger("lastPlusSignUnlock", KoLCharacter.getAscensions());
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        StringUtilities.singleStringReplace(
            buffer,
            "It's actually a book.  Read it.",
            "It's actually a book. <font size=1>[<a href=\"inv_use.php?pwd="
                + GenericRequest.passwordHash
                + "&which=3&whichitem=818\">read it</a>]</font>");
      }
    };

    new ChoiceAdventure(4, "Finger-Lickin'... Death.", "South of the Border") {
      void setup() {
        new Option(1, "gain or lose 500 meat")
            .turnCost(1)
            .attachMeat(-500, MANUAL)
            .attachMeat(500, AUTO);
        new Option(2, "lose 500 meat and maybe get a poultrygeist")
            .turnCost(1)
            .attachMeat(-500, MANUAL)
            .attachItem("poultrygeist", 1, AUTO);
        new Option(3, "skip adventure", true).entersQueue(false);

        new CustomOption(1, "small meat boost");
        new CustomOption(2, "try for poultrygeist");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("Here are you weenings")) {
          // the 500 meat loss didn't happen
          // re-attach the turn cost, cuz that one did happen
          this.choiceFailed().turnCost(1);
        }
      }
    };

    new ChoiceAdventure(5, "Heart of Very, Very Dark Darkness", "The Spooky Gravy Burrow") {
      final AdventureResult INEXPLICABLY_GLOWING_ROCK =
          ItemPool.get(ItemPool.INEXPLICABLY_GLOWING_ROCK);

      void setup() {
        new Option(1, "fight the fairy queen", true)
            .turnCost(1)
            .attachItem(INEXPLICABLY_GLOWING_ROCK, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);

        if (INEXPLICABLY_GLOWING_ROCK.getCount(KoLConstants.inventory) > 0) {
          option.leadsTo(7, true, o -> o.index == 1);
        } else {
          option.leadsTo(6);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (INEXPLICABLY_GLOWING_ROCK.getCount(KoLConstants.inventory) > 0) {
          return "1";
        }
        return "2";
      }
    };

    new ChoiceAdventure(6, "Darker Than Dark", "The Spooky Gravy Burrow") {
      void setup() {
        new Option(1, "get Beaten Up").turnCost(1).attachEffect(EffectPool.BEATEN_UP);
        new Option(2, "skip adventure").entersQueue(false);
      }
    };

    new ChoiceAdventure(7, "How Depressing", "The Spooky Gravy Burrow") {
      final AdventureResult SPOOKY_GLOVE = ItemPool.get(ItemPool.SPOOKY_GLOVE);

      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(SPOOKY_GLOVE, new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1));
        new Option(2, "skip adventure").entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (KoLCharacter.hasEquipped(SPOOKY_GLOVE)) {
          getOption(1).leadsTo(8);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (KoLCharacter.hasEquipped(SPOOKY_GLOVE)) {
          return "1";
        }
        return "2";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && KoLCharacter.hasEquipped(SPOOKY_GLOVE)) {
          EquipmentManager.discardEquipment(SPOOKY_GLOVE);
        }
      }
    };

    new ChoiceAdventure(8, "On the Verge of a Dirge", "The Spooky Gravy Burrow") {
      void setup() {
        new Option(1, "fight Felonia");
        new Option(2, "fight Felonia");
        new Option(3, "fight Felonia");
      }
    };

    new RetiredChoiceAdventure(9, "Wheel In the Sky Keep on Turning", null) { // Muscle Position
      void setup() {
        new Option(1, "Turn to mysticality").turnCost(1);
        new Option(2, "Turn to moxie").turnCost(1);
        new Option(3, "Leave at muscle");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString(
            "currentWheelPosition",
            decision == 1 ? "mysticality" : decision == 2 ? "moxie" : "muscle");
      }
    };

    new RetiredChoiceAdventure(
        10, "Wheel In the Sky Keep on Turning", null) { // Mysticality Position
      void setup() {
        new Option(1, "Turn to map quest").turnCost(1);
        new Option(2, "Turn to muscle").turnCost(1);
        new Option(3, "Leave at mysticality");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString(
            "currentWheelPosition",
            decision == 1 ? "map quest" : decision == 2 ? "muscle" : "mysticality");
      }
    };

    new RetiredChoiceAdventure(11, "Wheel In the Sky Keep on Turning", null) { // Map Quest Position
      void setup() {
        new Option(1, "Turn to moxie").turnCost(1);
        new Option(2, "Turn to mysticality").turnCost(1);
        new Option(3, "Leave at map quest");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString(
            "currentWheelPosition",
            decision == 1 ? "moxie" : decision == 2 ? "mysticality" : "map quest");
      }
    };

    new RetiredChoiceAdventure(12, "Wheel In the Sky Keep on Turning", null) { // Moxie Position
      void setup() {
        new Option(1, "Turn to muscle").turnCost(1);
        new Option(2, "Turn to map quest").turnCost(1);
        new Option(3, "Leave at moxie");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString(
            "currentWheelPosition",
            decision == 1 ? "muscle" : decision == 2 ? "map quest" : "moxie");
      }
    };

    new UnknownChoiceAdventure(13);

    new RetiredChoiceAdventure(14, "A Bard Day's Night", "Cobb's Knob Harem") {
      void setup() {
        new Option(1, "Knob goblin harem veil", true)
            .turnCost(1)
            .attachItem("Knob goblin harem veil", 1, AUTO);
        new Option(2, "Knob goblin harem pants", true)
            .turnCost(1)
            .attachItem("Knob goblin harem pants", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(15, "Yeti Nother Hippy", "The eXtreme Slope") {
      void setup() {
        new Option(1, "eXtreme mittens", true).turnCost(1).attachItem("eXtreme mittens", 1, AUTO);
        new Option(2, "eXtreme scarf", true).turnCost(1).attachItem("eXtreme scarf", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(200, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(16, "Saint Beernard", "The eXtreme Slope") {
      void setup() {
        new Option(1, "snowboarder pants", true)
            .turnCost(1)
            .attachItem("snowboarder pants", 1, AUTO);
        new Option(2, "eXtreme scarf", true).turnCost(1).attachItem("eXtreme scarf", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(200, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(17, "Generic Teen Comedy Snowboarding Adventure", "The eXtreme Slope") {
      void setup() {
        new Option(1, "eXtreme mittens", true).turnCost(1).attachItem("eXtreme mittens", 1, AUTO);
        new Option(2, "snowboarder pants", true)
            .turnCost(1)
            .attachItem("snowboarder pants", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(200, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(18, "A Flat Miner", "Itznotyerzitz Mine") {
      void setup() {
        new Option(1, "miner's pants", true).turnCost(1).attachItem("miner's pants", 1, AUTO);
        new Option(2, "7-Foot Dwarven mattock", true)
            .turnCost(1)
            .attachItem("7-Foot Dwarven mattock", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(19, "100% Legal", "Itznotyerzitz Mine") {
      void setup() {
        new Option(1, "miner's helmet", true).turnCost(1).attachItem("miner's helmet", 1, AUTO);
        new Option(2, "miner's pants", true).turnCost(1).attachItem("miner's pants", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(20, "See You Next Fall", "Itznotyerzitz Mine") {
      void setup() {
        new Option(1, "miner's helmet", true).turnCost(1).attachItem("miner's helmet", 1, AUTO);
        new Option(2, "7-Foot Dwarven mattock", true)
            .turnCost(1)
            .attachItem("7-Foot Dwarven mattock", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(21, "Under the Knife", "The Sleazy Back Alley") {
      boolean sexChanged = false;

      void setup() {
        new Option(1, "switch genders", true).turnCost(1).attachMeat(-500, MANUAL);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("anaesthetizes you")) {
            sexChanged = true;
          } else {
            this.choiceFailed();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (sexChanged) {
          Preferences.increment("sexChanges", 1);
          Preferences.setBoolean("_sexChanged", true);
          KoLCharacter.setGender(
              request.responseText.contains("in more ways than one")
                  ? KoLCharacter.FEMALE
                  : KoLCharacter.MALE);
          ConcoctionDatabase.setRefreshNeeded(false);
        }
      }
    };

    new ChoiceAdventure(22, "The Arrrbitrator", "The Obligatory Pirate's Cove") {
      void setup() {
        new Option(1, "eyepatch", true).turnCost(1).attachItem("eyepatch", 1, AUTO);
        new Option(2, "swashbuckling pants", true)
            .turnCost(1)
            .attachItem("swashbuckling pants", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(23, "Barrie Me at Sea", "The Obligatory Pirate's Cove") {
      void setup() {
        new Option(1, "stuffed shoulder parrot", true)
            .turnCost(1)
            .attachItem("stuffed shoulder parrot", 1, AUTO)
            .attachMeat(-5, AUTO);
        new Option(2, "swashbuckling pants", true)
            .turnCost(1)
            .attachItem("swashbuckling pants", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(24, "Amatearrr Night", "The Obligatory Pirate's Cove") {
      void setup() {
        new Option(1, "stuffed shoulder parrot", true)
            .turnCost(1)
            .attachItem("stuffed shoulder parrot", 1, AUTO);
        new Option(2, "small meat boost", true).turnCost(1).attachMeat(100, AUTO);
        new Option(3, "eyepatch", true).turnCost(1).attachItem("eyepatch", 1, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(25, "Ouch! You bump into a door!", "The Dungeons of Doom") {
      void setup() {
        new Option(1, "50 meat -> get magic lamp", true)
            .turnCost(1)
            .attachMeat(-50, MANUAL)
            .attachItem("magic lamp", 1, AUTO);
        new Option(2, "5000 meat -> fight mimic, get dead mimic", true)
            .attachMeat(-5000, MANUAL)
            .attachItem("dead mimic", 1, AUTO);
        new Option(3, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("and sheepishly leave the store")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(26, "A Three-Tined Fork", "The Spooky Forest") {
      void setup() {
        new Option(1, "muscle class starter items").leadsTo(27);
        new Option(2, "myst class starter items").leadsTo(28);
        new Option(3, "moxie class starter items").leadsTo(29);
      }
    };

    new ChoiceAdventure(27, "Footprints", "The Spooky Forest") {
      void setup() {
        new Option(1, "get seal clubber items")
            .turnCost(1)
            .attachItem(ItemPool.SEAL_CLUB, 1, AUTO)
            .attachItem(ItemPool.SEAL_HELMET, 1, AUTO);
        new Option(2, "get turtle tamer items")
            .turnCost(1)
            .attachItem(ItemPool.HELMET_TURTLE, 1, AUTO)
            .attachItem(ItemPool.TURTLE_TOTEM, 1, AUTO);
      }
    };

    new ChoiceAdventure(28, "A Pair of Craters", "The Spooky Forest") {
      void setup() {
        new Option(1, "get pastamancer items")
            .turnCost(1)
            .attachItem(ItemPool.PASTA_SPOON, 1, AUTO)
            .attachItem(ItemPool.RAVIOLI_HAT, 1, AUTO);
        new Option(2, "get sauceror items")
            .turnCost(1)
            .attachItem(ItemPool.SAUCEPAN, 1, AUTO)
            .attachItem(ItemPool.SPICES, 1, AUTO);
      }
    };

    new ChoiceAdventure(29, "The Road Less Visible", "The Spooky Forest") {
      void setup() {
        new Option(1, "get disco bandit items")
            .turnCost(1)
            .attachItem(ItemPool.DISCO_MASK, 1, AUTO)
            .attachItem(ItemPool.DISCO_BALL, 1, AUTO);
        new Option(2, "get accordion thief items")
            .turnCost(1)
            .attachItem(ItemPool.STOLEN_ACCORDION, 1, AUTO)
            .attachItem(ItemPool.MARIACHI_PANTS, 1, AUTO);
      }
    };

    new UnknownChoiceAdventure(30);

    new UnknownChoiceAdventure(31);

    new UnknownChoiceAdventure(32);

    new UnknownChoiceAdventure(33);

    new UnknownChoiceAdventure(34);

    new UnknownChoiceAdventure(35);

    new UnknownChoiceAdventure(36);

    new UnknownChoiceAdventure(37);

    new UnknownChoiceAdventure(38);

    new UnknownChoiceAdventure(39);

    new ChoiceAdventure(40, "The Effervescent Fray", "Battlefield (No Uniform)") {
      void setup() {
        new Option(1, "Cloaca-Cola fatigues", true)
            .turnCost(1)
            .attachItem("Cloaca-Cola fatigues", 1, AUTO);
        new Option(2, "Dyspepsi-Cola shield", true)
            .turnCost(1)
            .attachItem("Dyspepsi-Cola shield", 1, AUTO);
        new Option(3, "mysticality substats", true).turnCost(1);
      }
    };

    new ChoiceAdventure(41, "Smells Like Team Spirit", "Battlefield (No Uniform)") {
      void setup() {
        new Option(1, "Dyspepsi-Cola fatigues", true)
            .turnCost(1)
            .attachItem("Dyspepsi-Cola fatigues", 1, AUTO);
        new Option(2, "Cloaca-Cola helmet", true)
            .turnCost(1)
            .attachItem("Cloaca-Cola helmet", 1, AUTO);
        new Option(3, "muscle substats", true).turnCost(1);
      }
    };

    new ChoiceAdventure(42, "What is it Good For?", "Battlefield (No Uniform)") {
      void setup() {
        new Option(1, "Dyspepsi-Cola helmet", true)
            .turnCost(1)
            .attachItem("Dyspepsi-Cola helmet", 1, AUTO);
        new Option(2, "Cloaca-Cola shield", true)
            .turnCost(1)
            .attachItem("Cloaca-Cola shield", 1, AUTO);
        new Option(3, "moxie substats", true).turnCost(1);
      }
    };

    new UnknownChoiceAdventure(43);

    new UnknownChoiceAdventure(44);

    new RetiredChoiceAdventure(45, "Maps and Legends", "The Spooky Forest") {
      void setup() {
        new Option(1, "Spooky Temple map").turnCost(1).attachItem(ItemPool.SPOOKY_MAP, 1, AUTO);
        new Option(2, "skip adventure").entersQueue(false);
        new Option(3, "skip adventure").entersQueue(false);
      }
    };

    new ChoiceAdventure(46, "An Interesting Choice", "The Spooky Forest") {
      void setup() {
        this.customName = "Spooky Forest Vampire";

        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "muscle substats", true).turnCost(1);
        new Option(3, "fight spooky vampire", true).attachItem(ItemPool.VAMPIRE_HEART, 1, AUTO);
      }
    };

    new ChoiceAdventure(47, "Have a Heart", "The Spooky Forest") {
      final AdventureResult VAMPIRE_HEART = ItemPool.get(ItemPool.VAMPIRE_HEART);
      final Pattern HEARTS_PATTERN =
          Pattern.compile("some vampire hearts, there\\. (\\d+), to be exact\\.");

      void setup() {
        this.customName = "Spooky Forest Vampire Hunter";

        new Option(1, "trade all your vampire hearts for bottles of used blood", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(1);
        int hearts;

        if (request == null) {
          hearts = VAMPIRE_HEART.getCount(KoLConstants.inventory);
        } else {
          Matcher matcher = HEARTS_PATTERN.matcher(request.responseText);

          if (matcher.find()) {
            hearts = StringUtilities.parseInt(matcher.group(1));
          } else {
            hearts = VAMPIRE_HEART.getCount(KoLConstants.inventory);
          }
        }

        option
            .attachItem(ItemPool.USED_BLOOD, hearts, AUTO)
            .attachItem(
                VAMPIRE_HEART.getInstance(0 - hearts),
                MANUAL,
                new DisplayAll("your vampire hearts"));
      }
    };

    new ChoiceAdventure(48, "Violet Fog", "undefined") { // Start
      void setup() {
        this.hasGoalButton = true;

        this.customName = this.name;
        this.customZones.add("Astral");

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);

        for (int i = 0; i < VioletFogManager.FogGoals.length; ++i) {
          int index = i; // needed, otherwise the lambda function will cry
          new CustomOption(
              index,
              VioletFogManager.FogGoals[index],
              () -> {
                Preferences.setInteger("violetFogGoal", index);
              });
        }

        this.customLoad =
            () -> {
              int index = Preferences.getInteger("violetFogGoal");
              if (index < 0 || index > VioletFogManager.FogGoals.length) {
                System.out.println("Invalid setting " + index + " for violetFogGoal");
                return 0;
              }
              return index;
            };

        this.customPreferencesToListen.add("violetFogGoal");
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(49, "Violet Fog", "undefined") { // Man on Bicycle
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(50, "Violet Fog", "undefined") { // Pleasant-Faced Man
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(51, "Violet Fog", "undefined") { // Man on Cornflake
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(52, "Violet Fog", "undefined") { // Giant Chessboard
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(53, "Violet Fog", "undefined") { // Improbable Mustache
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(54, "Violet Fog", "undefined") { // Fog of Birds
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(55, "Violet Fog", "undefined") { // Intense-Looking Man
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(56, "Violet Fog", "undefined") { // Boat on River
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(57, "Violet Fog", "undefined") { // Man in Sunglasses
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(58, "Violet Fog", "undefined") { // Huge Caterpillar
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(59, "Violet Fog", "undefined") { // Man in Bowler
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(60, "Violet Fog", "undefined") { // Dance Number
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(61, "Violet Fog", "undefined") { // Huge Mountain
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(62, "The Big Scary Place", "undefined") { // Headgear
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(63, "The Big Scary Place", "undefined") { // Weapon
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(64, "The Big Scary Place", "undefined") { // Garment
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(65, "The Prince of Wishful Thinking", "undefined") { // Body
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(66, "The Prince of Wishful Thinking", "undefined") { // Wisdom
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(67, "The Prince of Wishful Thinking", "undefined") { // Charm
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(68, "She's So Unusual", "undefined") { // Alcohol
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(69, "She's So Unusual", "undefined") { // Food
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(70, "She's So Unusual", "undefined") { // Herbs or Medicines
      void setup() {
        this.hasGoalButton = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] violetFogSpoilers = VioletFogManager.choiceSpoilers(this.choice);

        for (int i = 0; i < 4; ++i) {
          getOption(i + 1).text(violetFogSpoilers[i]);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VioletFogManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return VioletFogManager.handleChoice(this.choice);
        }

        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VioletFogManager.mapChoice(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(71, "A Journey to the Center of Your Mind", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(72, "Lording Over The Flies", "Frat House") {
      final AdventureResult SPANISH_FLY = ItemPool.get(ItemPool.SPANISH_FLY);
      final AdventureResult AROUND_THE_WORLD = new AdventureResult("around the world", 1, false);

      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "every 5 spanish fly -> 1 around the world", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int trades = SPANISH_FLY.getCount(KoLConstants.inventory) / 5;

        // doesn't cost a turn either way, so NEED is not necessary
        getOption(1)
            .attachItem(
                SPANISH_FLY.getInstance(trades * -5),
                MANUAL,
                new DisplayAll("spanish fly", WANT, AT_LEAST, 5))
            .attachItem(AROUND_THE_WORLD.getInstance(trades), AUTO);
      }
    };

    new ChoiceAdventure(73, "Don't Fence Me In", "Whitey's Grove") {
      final AdventureResult RICE_BOWL = new AdventureResult("rice bowl", 0, false);

      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, null, true).turnCost(1).attachItem("white picket fence", 1, AUTO);
        new Option(3, "wedding cake, white rice 3x (+2x w/ rice bowl)", true)
            .turnCost(1)
            .attachItem("piece of wedding cake", 1, AUTO, new DisplayAll("cake"));
      }

      @Override
      void visitChoice(GenericRequest request) {
        int riceDrops = 5 - Preferences.getInteger("_whiteRiceDrops");

        Option option = getOption(3);
        if (riceDrops <= 0) {
          option.text("wedding cake");
        } else if (riceDrops < 3) {
          option
              .text("wedding cake, white rice (" + riceDrops + " more today) w/ rice bowl")
              .attachItem(ItemPool.WHITE_RICE, 1, AUTO, new DisplayAll("today\\)"))
              .attachItem(RICE_BOWL, new DisplayAll(WANT, EQUIPPED_AT_LEAST, 1));
        } else {
          option
              .text(
                  "wedding cake, white rice ("
                      + (riceDrops - 2)
                      + " more today), +2 max w/ rice bowl")
              .attachItem(ItemPool.WHITE_RICE, 1, AUTO, new DisplayAll("today\\)"))
              .attachItem(RICE_BOWL, AUTO);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          if (request.responseText.contains("you pick")
              || request.responseText.contains("you manage")) {
            Preferences.increment("_whiteRiceDrops", 1);
          }
        }
      }
    };

    new ChoiceAdventure(74, "The Only Thing About Him is the Way That He Walks", "Whitey's Grove") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "boxed wine x3", true).turnCost(1).attachItem("boxed wine", 3, AUTO);
        new Option(3, "mullet wig", true).turnCost(1).attachItem("mullet wig", 1, AUTO);
      }
    };

    new ChoiceAdventure(75, "Rapido!", "Whitey's Grove") {
      void setup() {
        new Option(1, "mysticality substats", true).turnCost(1);
        new Option(2, "white lightning x3", true)
            .turnCost(1)
            .attachItem("white lightning", 3, AUTO);
        new Option(3, "white collar", true).turnCost(1).attachItem("white collar", 1, AUTO);
      }
    };

    new ChoiceAdventure(76, "Junction in the Trunction", "The Knob Shaft") {
      void setup() {
        this.isSuperlikely = true;

        this.customZones.add("Knob");

        new Option(1, "3x cardboard ore", true).turnCost(1).attachItem("cardboard ore", 3, AUTO);
        new Option(2, "3x styrofoam ore", true).turnCost(1).attachItem("styrofoam ore", 3, AUTO);
        new Option(3, "3x bubblewrap ore", true).turnCost(1).attachItem("bubblewrap ore", 3, AUTO);
      }
    };

    new RetiredChoiceAdventure(77, "Minnesota Incorporeals", "The Haunted Billiards Room") {
      void setup() {
        new Option(1, "moxie substats").turnCost(1);
        new Option(2).leadsTo(78);
        new Option(3, "skip adventure").entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(78, "Broken", "The Haunted Billiards Room") {
      void setup() {
        new Option(1).leadsTo(79);
        new Option(2, "muscle substats").turnCost(1);
        new Option(3, "skip adventure").entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(79, "A Hustle Here, a Hustle There", "The Haunted Billiards Room") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2, "mysticality substats").turnCost(1);
        new Option(3, "skip adventure").entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(80, "Take a Look, it's in a Book!", "The Haunted Library") { // Rise
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new RetiredChoiceAdventure(81, "Take a Look, it's in a Book!", "The Haunted Library") { // Fall
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new RetiredChoiceAdventure(82, "One Nightstand", "The Haunted Bedroom") { // White
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(83, "One Nightstand", "The Haunted Bedroom") { // Mahogany
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(84, "One Nightstand", "The Haunted Bedroom") { // Ornate
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new RetiredChoiceAdventure(85, "One Nightstand", "The Haunted Bedroom") { // Wooden
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new ChoiceAdventure(86, "History is Fun!", "The Haunted Library") { // Rise
      void setup() {
        new Option(1, "Spookyraven Chapter 1").turnCost(1);
        new Option(2, "Spookyraven Chapter 2").turnCost(1);
        new Option(3, "Spookyraven Chapter 3").turnCost(1);
      }
    };

    new ChoiceAdventure(87, "History is Fun!", "The Haunted Library") { // Fall
      void setup() {
        new Option(1, "Spookyraven Chapter 4").turnCost(1);
        new Option(2, "Spookyraven Chapter 5").turnCost(1);
        new Option(3, "Spookyraven Chapter 6").turnCost(1);
      }
    };

    new ChoiceAdventure(88, "Naughty, Naughty", "The Haunted Library") {
      final AdventureResult ENGLISH_TO_A_F_U_E_DICTIONARY =
          ItemPool.get(ItemPool.ENGLISH_TO_A_F_U_E_DICTIONARY);

      void setup() {
        new Option(1, "mysticality substats").turnCost(1);
        new Option(2, "moxie substats").turnCost(1);
        new Option(3, "spooky damage").turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (ENGLISH_TO_A_F_U_E_DICTIONARY.getCount(KoLConstants.inventory) > 0) {
          final String text;

          switch (KoLCharacter.getAscensionClass()) {
            case PASTAMANCER:
              text = "learn Fearful Fettucini";
              break;
            case SAUCEROR:
              text = "learn Scarysauce";
              break;
            default:
              text = "?";
          }

          getOption(3)
              .text(text)
              .attachItem(ENGLISH_TO_A_F_U_E_DICTIONARY.getInstance(-1), MANUAL, new NoDisplay());
        }
      }
    };

    new ChoiceAdventure(89, "Out in the Garden", "The Haunted Gallery") {
      // ***DON'T USE THIS AS AN EXAMPLE***
      // ***AVERT YOUR EYES***
      // ***DON'T USE THIS AS AN EXAMPLE***
      final AdventureResult TATTERED_WOLF_STANDARD = ItemPool.get(ItemPool.TATTERED_WOLF_STANDARD);
      final AdventureResult TATTERED_SNAKE_STANDARD =
          ItemPool.get(ItemPool.TATTERED_SNAKE_STANDARD);
      final AdventureResult MAIDEN_EFFECT = EffectPool.get(EffectPool.DREAMS_AND_LIGHTS);

      void setup() {
        this.option0IsManualControl = false;

        this.customName = "The Maidens";

        new Option(1);
        new Option(2);
        new Option(3).turnCost(1);
        new Option(4, "skip adventure and banish for 10 adventures").entersQueue(false);

        new CustomOption(0, "Fight a random knight");
        new CustomOption(1, "Only fight the wolf knight");
        new CustomOption(2, "Only fight the snake knight");
        new CustomOption(3, "Maidens, then fight a random knight");
        new CustomOption(4, "Maidens, then fight the wolf knight");
        new CustomOption(5, "Maidens, then fight the snake knight");
        new CustomOption(6, "Ignore this adventure");
        new CustomOption(7, "Visit a knight if you have his banner, else fight a random knight");
        new CustomOption(
            8, "Visit a knight if you have his banner, else only fight the wolf knight");
        new CustomOption(
            9, "Visit a knight if you have his banner, else only fight the snake knight");
        new CustomOption(
            10, "Visit a knight if you have his banner, else Maidens, then fight a random knight");
        new CustomOption(
            11, "Visit a knight if you have his banner, else Maidens, then fight the wolf knight");
        new CustomOption(
            12, "Visit a knight if you have his banner, else Maidens, then fight the snake knight");
        new CustomOption(13, "Visit a knight if you have his banner, else ignore this adventure");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);
        String text = "fight Wolf Knight";

        if (TATTERED_WOLF_STANDARD.getCount(KoLConstants.inventory) > 0) {
          text = "learn Snarl of the Timberwolf";

          option
              .attachItem(TATTERED_WOLF_STANDARD.getInstance(-1), MANUAL, new NoDisplay())
              .turnCost(1);
        }
        option.text(text);

        option = getOption(2);
        text = "fight Snake Knight";

        if (TATTERED_SNAKE_STANDARD.getCount(KoLConstants.inventory) > 0) {
          text = "learn Spectral Snapper";

          option
              .attachItem(TATTERED_SNAKE_STANDARD.getInstance(-1), MANUAL, new NoDisplay())
              .turnCost(1);
        }
        option.text(text);

        option = getOption(3);

        if (KoLConstants.activeEffects.contains(MAIDEN_EFFECT)) {
          option.text("lose HP");
        } else {
          option.text("10 turns of Dreams and Lights").attachEffect(MAIDEN_EFFECT);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // must be kept for backwards compatibility ;-;
        int intDecision = StringUtilities.parseInt(decision);
        switch (intDecision) {
          case 0:
            return String.valueOf(KoLConstants.RNG.nextInt(2) + 1);
          case 1:
          case 2:
            return decision;
          case 3:
          case 4:
          case 5:
            if (!KoLConstants.activeEffects.contains(MAIDEN_EFFECT)) {
              return "3";
            }
            return this.getDecision(responseText, String.valueOf(intDecision - 3), stepCount);
          case 6:
            return "4";
          case 7:
          case 8:
          case 9:
          case 10:
          case 11:
          case 12:
          case 13:
            if (TATTERED_WOLF_STANDARD.getCount(KoLConstants.inventory) > 0) {
              return "1";
            }
            if (TATTERED_SNAKE_STANDARD.getCount(KoLConstants.inventory) > 0) {
              return "2";
            }
            return this.getDecision(responseText, String.valueOf(intDecision - 7), stepCount);
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        AdventureRequest.setNameOverride(
            "Knight", decision == 1 ? "Knight (Wolf)" : "Knight (Snake)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 4) {
          TurnCounter.startCounting(10, "Garden Banished loc=*", "wolfshield.gif");
        }
      }
    };

    new ChoiceAdventure(90, "Curtains", "The Haunted Ballroom") {
      final AdventureResult BIZARRE_ILLEGIBLE_SHEET_MUSIC =
          ItemPool.get(ItemPool.BIZARRE_ILLEGIBLE_SHEET_MUSIC);

      void setup() {
        new Option(1, "fight ghastly organist", true);
        new Option(2, "moxie substats", true).turnCost(1);
        new Option(3, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (BIZARRE_ILLEGIBLE_SHEET_MUSIC.getCount(KoLConstants.inventory) > 0) {
          final String text;

          switch (KoLCharacter.getAscensionClass()) {
            case DISCO_BANDIT:
              text = "learn Tango of Terror";
              break;
            case ACCORDION_THIEF:
              text = "learn Dirge of Dreadfulness";
              break;
            default:
              text = "?";
          }

          getOption(1)
              .text(text)
              .turnCost(1)
              .attachItem(BIZARRE_ILLEGIBLE_SHEET_MUSIC.getInstance(-1), MANUAL, new NoDisplay());
        }
      }
    };

    new RetiredChoiceAdventure(91, "Louvre It or Leave It", "The Haunted Gallery") {
      final String[] directions = new String[] {"up", "down", "side"};

      void setup() {
        this.customName = "Louvre Override";

        new Option(1);
        new Option(2);

        new CustomOption(
            0,
            "Use specified goal",
            () -> {
              Preferences.setString("choiceAdventure91", "2");
              Preferences.setString("louvreOverride", "");
            });

        int i = 0;
        ArrayList<String> paths = new ArrayList<>();
        for (String first : directions) {
          for (String second : directions) {
            for (String third : directions) {
              String path = first + ", " + second + ", " + third;
              paths.add(path);
              new CustomOption(
                  ++i,
                  path,
                  () -> {
                    Preferences.setString("choiceAdventure91", "1");
                    Preferences.setString("louvreOverride", path);
                  });
            }
          }
        }

        this.customLoad =
            () -> {
              String setting = Preferences.getString("louvreOverride");
              int index = paths.indexOf(setting);
              if (index > 0) {
                return index;
              }
              return 0;
            };

        this.customPreferencesToListen.add("louvreOverride");
      }
    };

    new RetiredChoiceAdventure(
        92, "Louvre It or Leave It", "The Haunted Gallery") { // Relativity Start
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(93, "Louvre It or Leave It", "The Haunted Gallery") { // Relativity 1
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(94, "Louvre It or Leave It", "The Haunted Gallery") { // Relativity 2
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(95, "Louvre It or Leave It", "The Haunted Gallery") { // Relativity 3
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        96, "Louvre It or Leave It", "The Haunted Gallery") { // Piet Mondrian
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(97, "Louvre It or Leave It", "The Haunted Gallery") { // The Scream
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        98, "Louvre It or Leave It", "The Haunted Gallery") { // The Birth of Venus
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        99, "Louvre It or Leave It", "The Haunted Gallery") { // The Creation of Adam
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };
  }
}
