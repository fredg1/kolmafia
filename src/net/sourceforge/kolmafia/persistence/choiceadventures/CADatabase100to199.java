package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.objectpool.OutfitPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.BeerPongRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.BanishManager;
import net.sourceforge.kolmafia.session.DvorakManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.GoalManager;
import net.sourceforge.kolmafia.session.HobopolisManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.QuestManager;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase100to199 extends ChoiceAdventureDatabase {
  final void add100to199() {
    new RetiredChoiceAdventure(
        100, "Louvre It or Leave It", "The Haunted Gallery") { // The Death of Socrates
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(101, "Louvre It or Leave It", "The Haunted Gallery") { // Nighthawks
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        102,
        "Louvre It or Leave It",
        "The Haunted Gallery") { // Sunday Afternoon on the Island of La Grande Jatte
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        103, "Louvre It or Leave It", "The Haunted Gallery") { // The Last Supper
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(
        104, "Louvre It or Leave It", "The Haunted Gallery") { // The Persistence of Memory
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(105, "Having a Medicine Ball", "The Haunted Bathroom") {
      void setup() {
        new Option(1, "mysticality substats (+ w/ antique hand mirror)", true)
            .turnCost(1)
            .attachItem(ItemPool.ANTIQUE_HAND_MIRROR);
        new Option(2).leadsTo(107, false, o -> true);
        new Option(3, "guy made of bees", true);

        new CustomOption(2, "other options");
      }

      @Override
      void visitChoice(GenericRequest request) {
        KoLCharacter.ensureUpdatedGuyMadeOfBees();
        boolean haveMirror = InventoryManager.getCount(ItemPool.ANTIQUE_HAND_MIRROR) > 0;
        int myst = KoLCharacter.getBaseMysticality();
        long mystGain = Math.min(Math.round(myst * (haveMirror ? 1.2 : 1.0)), 300);
        boolean defeated = Preferences.getBoolean("guyMadeOfBeesDefeated");

        getOption(1)
            .text(
                "get "
                    + mystGain
                    + " mysticality substats (antique hand mirror "
                    + (haveMirror ? "" : "NOT ")
                    + "in inventory)");

        getOption(3)
            .text(
                !defeated
                    ? "guy made of bees: called "
                        + Preferences.getString("guyMadeOfBeesCount")
                        + "/5 times"
                    : "guy made of bees: defeated");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          Option option = getOption(3);

          // You look into the mirror and say "Guy made of bees." Nothing happens.
          if (request.responseText.contains("say \"Guy made of bees.\" Nothing happens.")) {
            option.turnCost(1);
          } else if (request.responseText.contains("that ship is sailed")) {
            // You say "Guy made of bees" into the mirror, but nothing happens. Looks like that ship
            // is sailed. And the ship was made of bees, and it sailed in the air.
            option.entersQueue(false);
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          KoLCharacter.ensureUpdatedGuyMadeOfBees();
          Preferences.increment("guyMadeOfBeesCount", 1, 5, true);

          String text = request.responseText;
          String urlString = request.getPath();

          if (urlString.startsWith("fight.php")) {
            if (text.contains("guy made of bee pollen")) {
              // Record that we beat the guy made of bees.
              Preferences.setBoolean("guyMadeOfBeesDefeated", true);
            }
          } else if (urlString.startsWith("choice.php") && text.contains("that ship is sailed")) {
            // For some reason, we didn't notice when we
            // beat the guy made of bees. Record it now.
            Preferences.setBoolean("guyMadeOfBeesDefeated", true);
          }
        }
      }
    };

    new ChoiceAdventure(106, "Strung-Up Quartet", "The Haunted Ballroom") {
      void setup() {
        new Option(1, "increase monster level", true).turnCost(1);
        new Option(2, "decrease combat frequency", true).turnCost(1);
        new Option(3, "increase item drops", true).turnCost(1);
        new Option(4, "disable song", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        Preferences.setInteger("lastQuartetAscension", KoLCharacter.getAscensions());
        Preferences.setInteger("lastQuartetRequest", decision);

        if (KoLCharacter.recalculateAdjustments()) {
          KoLCharacter.updateStatus();
        }

        if (request.responseText.contains("You guys are doing fine")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(107, "Bad Medicine is What You Need", "The Haunted Bathroom") {
      void setup() {
        new Option(1, null, true).turnCost(1).attachItem("antique bottle of cough syrup", 1, AUTO);
        new Option(2, null, true).turnCost(1).attachItem("tube of hair oil", 1, AUTO);
        new Option(3, null, true).turnCost(1).attachItem("bottle of ultravitamins", 1, AUTO);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(108, "Aww, Craps", "The Sleazy Back Alley") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "meat and moxie", true).turnCost(1).attachMeat(35, AUTO);
        new Option(3, "random effect", true).turnCost(1);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(109, "Dumpster Diving", "The Sleazy Back Alley") {
      void setup() {
        new Option(1, "fight drunken half-orc hobo", true);
        new Option(2, "meat and moxie", true).turnCost(1).attachMeat(3, AUTO);
        new Option(3, "Mad Train wine", true).turnCost(1).attachItem("Mad Train wine", 1, AUTO);
      }
    };

    new ChoiceAdventure(110, "The Entertainer", "The Sleazy Back Alley") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "moxie and muscle", true).turnCost(1);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(15, AUTO);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(111, "Malice in Chains", "The Outskirts of Cobb's Knob") {
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "muscle substats, risk of missing", true).turnCost(1);
        new Option(3, "fight sleeping Knob Goblin Guard", true);
      }
    };

    new ChoiceAdventure(112, "Please, Hammer", "The Sleazy Back Alley") {
      void setup() {
        new Option(1, "accept hammer quest", true)
            .attachItem(ItemPool.HAROLDS_HAMMER_HEAD, 1, AUTO, new ImageOnly())
            .attachItem(ItemPool.HAROLDS_HAMMER_HANDLE, 1, AUTO, new ImageOnly())
            .attachItem(ItemPool.HAROLDS_BELL);
        new Option(2, "skip adventure", true).entersQueue(false);
        new Option(3, "muscle substats", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && KoLmafia.isAdventuring()) {
          InventoryManager.retrieveItem(ItemPool.get(ItemPool.HAROLDS_HAMMER, 1));
        }
      }
    };

    new ChoiceAdventure(113, "Knob Goblin BBQ", "The Outskirts of Cobb's Knob") {
      void setup() {
        new Option(1, "complete cake quest", true)
            .turnCost(1)
            .attachItem(ItemPool.UNLIT_BIRTHDAY_CAKE, new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem(ItemPool.LIT_BIRTHDAY_CAKE, 1, AUTO, new NoDisplay());
        new Option(2, "fight Knob Goblin Barbecue Team", true);
        new Option(3, "get a random item", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("scorch the novelty candles")) {
          getOption(decision).attachItem(ItemPool.UNLIT_BIRTHDAY_CAKE, -1, MANUAL);
        }
      }
    };

    new ChoiceAdventure(114, "The Baker's Dilemma", "The Haunted Pantry") {
      void setup() {
        new Option(1, "accept cake quest", true)
            .entersQueue(false)
            .attachItem(ItemPool.PAT_A_CAKE_PENDANT);
        new Option(2, "skip adventure", true).entersQueue(false);
        new Option(3, "moxie and meat (1)", true).turnCost(1).attachMeat(15, AUTO);
      }
    };

    new ChoiceAdventure(115, "Oh No, Hobo", "The Haunted Pantry") {
      void setup() {
        new Option(1, "fight drunken half-orc hobo", true);
        new Option(2, null, true).turnCost(1).attachMeat(-5, MANUAL).attachEffect("Good Karma");
        new Option(3, "mysticality, moxie, and meat", true).turnCost(1).attachMeat(7, AUTO);
      }
    };

    new ChoiceAdventure(116, "The Singing Tree", "The Haunted Pantry") {
      void setup() {
        new Option(1, "mysticality substats", true).turnCost(1).attachMeat(-1, MANUAL);
        new Option(2, "moxie substats", true).turnCost(1).attachMeat(-1, MANUAL);
        new Option(3, "random effect", true).turnCost(1).attachMeat(-1, MANUAL);
        new Option(4, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision != 4 && !request.responseText.contains("You flip a piece of Meat")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(117, "Trespasser", "The Haunted Pantry") {
      void setup() {
        new Option(1, "fight Knob Goblin Assistant Chef", true);
        new Option(2, "mysticality substats", true).turnCost(1);
        new Option(3, "get 1-5 random items", true)
            .turnCost(1)
            .attachItem(ItemPool.ASPARAGUS_KNIFE, 1, AUTO)
            .attachItem("chef's hat", 1, AUTO)
            .attachItem("magicalness-in-a-can", 1, AUTO)
            .attachItem(ItemPool.CAN_LID, 1, AUTO)
            .attachItem("stalk of asparagus", 1, AUTO);
      }
    };

    new ChoiceAdventure(118, "When Rocks Attack", "The Outskirts of Cobb's Knob") {
      void setup() {
        new Option(1, "accept unguent quest", true);
        new Option(2, "skip adventure", true);
      }
    };

    new RetiredChoiceAdventure(119, "Check It Out Now", "Simple Tool-Making Cave") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(
        120, "Ennui is Wasted on the Young", "The Outskirts of Cobb's Knob") {
      void setup() {
        new Option(1, "muscle and Pumped Up", true).turnCost(1);
        new Option(2, null, true).turnCost(1).attachItem("ice-cold Sir Schlitz", 1, AUTO);
        new Option(3, "moxie and lemon", true).turnCost(1).attachItem("lemon", 1, AUTO);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(121, "Next Sunday, A.D.", "Spooky Fright Factory") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new UnknownChoiceAdventure(122);

    new ChoiceAdventure(123, "At Least It's Not Full Of Trash", "The Hidden Temple") {
      void setup() {
        new Option(1, "lose HP");
        new Option(2, "Unlock Quest Puzzle").turnCost(1);
        new Option(3, "lose HP");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new UnknownChoiceAdventure(124);

    new ChoiceAdventure(125, "No Visible Means of Support", "The Hidden Temple") {
      void setup() {
        new Option(1, "lose HP");
        new Option(2, "lose HP");
        new Option(3, "Unlock Hidden City").turnCost(1);
      }

      @Override
      void postChoice0(GenericRequest request, int decision) {
        // If we are visiting for the first time,
        // finish the tiles
        DvorakManager.lastTile(request.responseText);
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          QuestDatabase.setQuestProgress(Quest.WORSHIP, "step3");
        }
      }
    };

    new ChoiceAdventure(126, "Sun at Noon, Tan Us", "Inside the Palindome") {
      void setup() {
        new Option(1, "moxie", true).turnCost(1);
        new Option(2, "chance of more moxie, or sunburned", true)
            .turnCost(1)
            .attachEffect("sunburned");
        new Option(3, "sunburned", true).turnCost(1).attachEffect("sunburned");
      }
    };

    new ChoiceAdventure(127, "No sir, away!  A papaya war is on!", "Inside the Palindome") {
      final AdventureResult PAPAYA = ItemPool.get(ItemPool.PAPAYA, 1);

      void setup() {
        this.customName = "Papaya War";

        new Option(1, "get 3 papayas")
            .turnCost(1)
            .attachItem(PAPAYA.getInstance(3), AUTO, new ImageOnly());
        new Option(2, "3 papayas -> stats")
            .turnCost(1)
            .attachItem(
                PAPAYA.getInstance(-3), MANUAL, new DisplayAll("papayas", NEED, AT_LEAST, 3));
        new Option(3, "stats").turnCost(1);

        new CustomOption(1, "3 papayas");
        new CustomOption(2, "Trade papayas for stats");
        new CustomOption(3, "Fewer stats");
        new CustomOption(4, "Stats until out of papayas then papayas");
        new CustomOption(5, "Stats until out of papayas then fewer stats");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        switch (decision) {
          case "4":
            return PAPAYA.getCount(KoLConstants.inventory) >= 3 ? "2" : "1";
          case "5":
            return PAPAYA.getCount(KoLConstants.inventory) >= 3 ? "2" : "3";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2 && request.responseText.contains("don't have nearly enough papayas")) {
          this.choiceFailed().turnCost(1);
        }
      }
    };

    new UnknownChoiceAdventure(128);

    new ChoiceAdventure(129, "Do Geese See God?", "Inside the Palindome") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "photograph of God")
            .turnCost(1)
            .attachMeat(-500, MANUAL)
            .attachItem(ItemPool.PHOTOGRAPH_OF_GOD, 1, AUTO);
        new Option(2, "skip adventure");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("your very own 8x10 glossy")) {
          this.choiceFailed();
        }
      }
    };

    new RetiredChoiceAdventure(130, "Rod Nevada, Vendor", "Inside the Palindome") { // now 873
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(131, "Dr. Awkward", null) {
      void setup() {
        new Option(1, "Illusion");
        new Option(2, "Of");
        new Option(3, "Choice");
      }
    };

    new RetiredChoiceAdventure(132, "Let's Make a Deal!", "The Arid, Extra-Dry Desert") {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          QuestDatabase.setQuestProgress(Quest.PYRAMID, "step1");
        }
      }
    };

    new UnknownChoiceAdventure(133);

    new RetiredChoiceAdventure(
        134, "Wheel in the Pyramid, Keep on Turning", "The Middle Chamber") { // initial visit
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(
        135, "Wheel in the Pyramid, Keep on Turning", "The Middle Chamber") { // subsequent visits
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(136, "Peace Wants Love", "Hippy Camp") {
      void setup() {
        new Option(1, "filthy corduroys", true).turnCost(1).attachItem("filthy corduroys", 1, AUTO);
        new Option(2, "filthy knitted dread sack", true)
            .turnCost(1)
            .attachItem("filthy knitted dread sack", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(137, "An Inconvenient Truth", "Hippy Camp") {
      void setup() {
        new Option(1, "filthy knitted dread sack", true)
            .turnCost(1)
            .attachItem("filthy knitted dread sack", 1, AUTO);
        new Option(2, "filthy corduroys", true).turnCost(1).attachItem("filthy corduroys", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(138, "Purple Hazers", "Frat House") {
      void setup() {
        new Option(1, "Orcish cargo shorts", true)
            .turnCost(1)
            .attachItem("Orcish cargo shorts", 1, AUTO);
        new Option(2, "Orcish baseball cap", true)
            .turnCost(1)
            .attachItem("Orcish baseball cap", 1, AUTO);
        new Option(3, "Orcish frat-paddle", true)
            .turnCost(1)
            .attachItem("Orcish frat-paddle", 1, AUTO);

        new CustomOption(4, "complete the outfit");
      }
    };

    new ChoiceAdventure(139, "Bait and Switch", "Wartime Hippy Camp (Frat Disguise)") {
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "2-5x ferret bait", true).turnCost(1).attachItem("ferret bait", 2, AUTO);
        new Option(3, "fight War Hippy (space) cadet", true);
      }
    };

    new ChoiceAdventure(140, "The Thin Tie-Dyed Line", "Wartime Hippy Camp (Frat Disguise)") {
      void setup() {
        new Option(1, "2-5x water pipe bomb", true)
            .turnCost(1)
            .attachItem("water pipe bomb", 2, AUTO);
        new Option(2, "moxie substats", true).turnCost(1);
        new Option(3, "fight War Hippy drill sergeant", true);
      }
    };

    new ChoiceAdventure(
        141,
        "Blockin' Out the Scenery",
        "Wartime Hippy Camp (Frat Disguise)") { // Frat Boy Ensemble
      void setup() {
        new Option(1, "mysticality substats", true).turnCost(1);
        new Option(2, "get some hippy food", true)
            .turnCost(1)
            .attachItem("cruelty-free wine", 1, AUTO, new ImageOnly())
            .attachItem("handful of walnuts", 1, AUTO, new ImageOnly())
            .attachItem("Genalen&trade; Bottle", 1, AUTO, new ImageOnly())
            .attachItem("mixed wildflower greens", 1, AUTO, new ImageOnly())
            .attachItem("thistle wine", 1, AUTO, new ImageOnly());
        new Option(3, "waste a turn", true).turnCost(1);
      }
    };

    new ChoiceAdventure(
        142,
        "Blockin' Out the Scenery",
        "Wartime Hippy Camp (Frat Disguise)") { // Frat Warrior Fatigues
      void setup() {
        new Option(1, "mysticality substats", true).turnCost(1);
        new Option(2, "get some hippy food", true)
            .turnCost(1)
            .attachItem("cruelty-free wine", 1, AUTO, new ImageOnly())
            .attachItem("handful of walnuts", 1, AUTO, new ImageOnly())
            .attachItem("Genalen&trade; Bottle", 1, AUTO, new ImageOnly())
            .attachItem("mixed wildflower greens", 1, AUTO, new ImageOnly())
            .attachItem("thistle wine", 1, AUTO, new ImageOnly());
        new Option(3, "start the war", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          CADatabase100to199.handleStartOfWar();
        }
      }
    };

    new ChoiceAdventure(143, "Catching Some Zetas", "Wartime Frat House (Hippy Disguise)") {
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "6-7x sake bomb", true).turnCost(1).attachItem("sake bomb", 6, AUTO);
        new Option(3, "fight War Pledge", true);
      }
    };

    new ChoiceAdventure(
        144, "One Less Room Than In That Movie", "Wartime Frat House (Hippy Disguise)") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "2-5x beer bomb", true).turnCost(1).attachItem("beer bomb", 2, AUTO);
        new Option(3, "fight Frat Warrior drill sergeant", true);
      }
    };

    new ChoiceAdventure(
        145, "Fratacombs", "Wartime Frat House (Hippy Disguise)") { // Filthy Hippy Disguise
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "get some frat food", true)
            .turnCost(1)
            .attachItem("brain-meltingly-hot chicken wings", 1, AUTO, new ImageOnly())
            .attachItem("frat brats", 1, AUTO, new ImageOnly())
            .attachItem("melted Jell-o shot", 1, AUTO, new ImageOnly())
            .attachItem("can of Swiller", 1, AUTO, new ImageOnly())
            .attachItem("knob ka-bobs", 1, AUTO, new ImageOnly());
        new Option(3, "waste a turn", true).turnCost(1);
      }
    };

    new ChoiceAdventure(
        146, "Fratacombs", "Wartime Frat House (Hippy Disguise)") { // War Hippy Fatigues
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "get some frat food", true)
            .turnCost(1)
            .attachItem("brain-meltingly-hot chicken wings", 1, AUTO, new ImageOnly())
            .attachItem("frat brats", 1, AUTO, new ImageOnly())
            .attachItem("melted Jell-o shot", 1, AUTO, new ImageOnly())
            .attachItem("can of Swiller", 1, AUTO, new ImageOnly())
            .attachItem("knob ka-bobs", 1, AUTO, new ImageOnly());
        new Option(3, "start the war", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          CADatabase100to199.handleStartOfWar();
        }
      }
    };

    new ChoiceAdventure(147, "Cornered!", "McMillicancuddy's Barn") {
      void setup() {
        new Option(1, "Open The Granary (meat)", true).turnCost(1);
        new Option(2, "Open The Bog (stench)", true).turnCost(1);
        new Option(3, "Open The Pond (cold, tornado path)", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(3);
        if (Preferences.getBoolean("chaosButterflyThrown")) {
          option.text("Open The Pond (cold, tornado path, butterfly thrown)");
        } else {
          option.attachItem(ItemPool.CHAOS_BUTTERFLY);
        }
      }
    };

    new ChoiceAdventure(148, "Cornered Again!", "McMillicancuddy's Barn") {
      void setup() {
        new Option(1, "Open The Back 40 (hot, tornado path)", true).turnCost(1);
        new Option(2, "Open The Family Plot (spooky)", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);
        if (Preferences.getBoolean("chaosButterflyThrown")) {
          option.text("Open The Back 40 (hot, tornado path, butterfly thrown)");
        } else {
          option.attachItem(ItemPool.CHAOS_BUTTERFLY);
        }
      }
    };

    new ChoiceAdventure(
        149, "How Many Corners Does this Stupid Barn Have!?", "McMillicancuddy's Barn") {
      void setup() {
        new Option(1, "Open The Shady Thicket (booze)", true).turnCost(1);
        new Option(2, "Open The Other Back 40 (sleaze, tornado path)", true).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(2);
        if (Preferences.getBoolean("chaosButterflyThrown")) {
          option.text("Open The Other Back 40 (sleaze, tornado path, butterfly thrown)");
        } else {
          option.attachItem(ItemPool.CHAOS_BUTTERFLY);
        }
      }
    };

    new ChoiceAdventure(150, "Another Adventure About BorderTown", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(151, "Adventurer, $1.99", "The \"Fun\" House") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight the clownlord (with 4+ clownosity)", true).leadsTo(152);
        new Option(2, "skip adventure, and banish for 10 advs (spent anywhere)");

        new CustomOption(2, "skip adventure");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("if you looked more like a clown")) {
          // beaten up
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(152, "Lurking at the Threshold", "The \"Fun\" House") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight the clownlord, for real this time");
        new Option(2, "skip adventure");
      }
    };

    new ChoiceAdventure(153, "Turn Your Head and Coffin", "The Defiled Alcove") {
      void setup() {
        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);
        new Option(3, "half-rotten brain", true)
            .turnCost(1)
            .attachItem("half-rotten brain", 1, AUTO);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(154, "Doublewide", "The Defiled Alcove") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(155, "Skull, Skull, Skull", "The Defiled Nook") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);
        new Option(3, "rusty bonesaw", true).turnCost(1).attachItem("rusty bonesaw", 1, AUTO);
        new Option(4, "(!!!RARE!!!) debonair deboner (!!!RARE!!!)")
            .turnCost(1)
            .attachItem("debonair deboner", 1, AUTO);
        new Option(5, "skip adventure", true).entersQueue(false);

        new CustomOption(6, "debonair deboner, or moxie");
        new CustomOption(7, "debonair deboner, or meat");
        new CustomOption(8, "debonair deboner, or bonesaw");
        new CustomOption(9, "debonair deboner, or skip", 4);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 4 - "Check the shiny object" - is not always available.
        int intDecision = StringUtilities.parseInt(decision);
        switch (intDecision) {
          case 4:
          case 6:
          case 7:
          case 8:
            if (responseText.contains("Check the shiny object")) {
              return "4";
            }
            if (intDecision == 4) {
              return String.valueOf(5);
            } else {
              return String.valueOf(intDecision - 5);
            }
        }
        return decision;
      }
    };

    new RetiredChoiceAdventure(156, "Pileup", "The Defiled Nook") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(157, "Urning Your Keep", "The Defiled Niche") {
      void setup() {
        new Option(1, "mysticality substats", true).turnCost(1);
        new Option(2, "plus-sized phylactery (the first time)", true)
            .turnCost(1)
            .attachItem("plus-sized phylactery", 1, AUTO);
        new Option(3, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);
        new Option(4, "skip adventure", true).entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(158, "Lich in the Niche", "The Defiled Niche") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(159, "Go Slow Past the Drawers", "The Defiled Cranny") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new RetiredChoiceAdventure(160, "Lunchtime", "The Defiled Cranny") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(161, "Bureaucracy of the Damned", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Check if we have all of Azazel's objects of evil
        for (int i = 2566; i <= 2568; ++i) {
          AdventureResult item = ItemPool.get(i);
          if (!KoLConstants.inventory.contains(item)) {
            return "4";
          }
        }
        return "1";
      }
    };

    new RetiredChoiceAdventure(162, "Between a Rock and Some Other Rocks", "The Goatlet") {
      final AdventureResult EARTHEN_FIST = EffectPool.get(EffectPool.EARTHEN_FIST);

      boolean wearsMiningOutfit = false;
      boolean hasEarthenFist = false;

      void setup() {
        new Option(1, "Open Goatlet").turnCost(1);
        new Option(2, "skip adventure");
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        wearsMiningOutfit = EquipmentManager.isWearingOutfit(OutfitPool.MINING_OUTFIT);
        hasEarthenFist = KoLConstants.activeEffects.contains(EARTHEN_FIST);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If you are wearing the outfit, have Worldpunch, or
        // are in Axecore, take the appropriate decision.
        // Otherwise, auto-skip the goatlet adventure so it can
        // be tried again later.
        return decision.equals("2")
            ? "2"
            : wearsMiningOutfit
                ? "1"
                : KoLCharacter.inFistcore() && hasEarthenFist
                    ? "1"
                    : KoLCharacter.inAxecore() ? "3" : "2";
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (KoLmafia.isAdventuring() && !wearsMiningOutfit && !hasEarthenFist) {
          QuestManager.unlockGoatlet();
        }
      }
    };

    new ChoiceAdventure(163, "Melvil Dewey Would Be Ashamed", "The Haunted Library") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "Necrotelicomnicon", true)
            .turnCost(1)
            .attachItem("Necrotelicomnicon", 1, AUTO);
        new Option(2, "Cookbook of the Damned", true)
            .turnCost(1)
            .attachItem("Cookbook of the Damned", 1, AUTO);
        new Option(3, "Sinful Desires", true).turnCost(1).attachItem("Sinful Desires", 1, AUTO);
        new Option(4, "skip adventure", true);
      }
    };

    new ChoiceAdventure(164, "Down by the Riverside", "The Stately Pleasure Dome") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "muscle substats", true).turnCost(1);
        new Option(2, "MP & Spirit of Alph", true)
            .turnCost(1)
            .attachMP(90)
            .attachEffect("Spirit of Alph");
        new Option(3, "fight roller-skating Muse", true);
      }
    };

    new ChoiceAdventure(165, "Beyond Any Measure", "The Stately Pleasure Dome") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "Rat-Faced -> Night Vision, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Rat-Faced", new ImageOnly("Rat-Faced"))
            .attachEffect("Night Vision", new ImageOnly("Night Vision"));
        new Option(2, "Bats in the Belfry -> Good with the Ladies, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Bats in the Belfry", new ImageOnly("Bats in the Belfry"))
            .attachEffect("Good with the Ladies", new ImageOnly("Good with the Ladies"));
        new Option(3, "mysticality substats", true).turnCost(1);
        new Option(4, "skip adventure", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 2:
            if (!request.responseText.contains("You acquire an effect")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(166, "Death is a Boat", "The Stately Pleasure Dome") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "No Vertigo -> S.T.L.T., otherwise skip", true)
            .turnCost(1)
            .attachEffect("No Vertigo", new ImageOnly("No Vertigo"))
            .attachItem("S.T.L.T.", 1, AUTO, new DisplayAll("S.T.L.T."));
        new Option(2, "moxie substats", true).turnCost(1);
        new Option(3, "Unusual Fashion Sense -> albatross necklace, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Unusual Fashion Sense", new ImageOnly("Unusual Fashion Sense"))
            .attachItem("albatross necklace", 1, AUTO, new DisplayAll("albatross necklace"));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 3:
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(167, "It's a Fixer-Upper", "The Mouldering Mansion") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight raven", true);
        new Option(2, "mysticality substats", true).turnCost(1);
        new Option(3, "HP & MP & Bats in the Belfry", true)
            .turnCost(1)
            .attachMP(45)
            .attachEffect("Bats in the Belfry");
      }
    };

    new ChoiceAdventure(168, "Midst the Pallor of the Parlor", "The Mouldering Mansion") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "Spirit of Alph -> Feelin' Philosophical, otherwise fight black cat", true)
            .turnCost(1)
            .attachEffect("Spirit of Alph", new ImageOnly("Spirit of Alph"))
            .attachEffect("Feelin' Philosophical", new ImageOnly("Feelin' Philosophical"));
        new Option(3, "Rat-Faced -> Unusual Fashion Sense, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Rat-Faced", new ImageOnly("Rat-Faced"))
            .attachEffect("Unusual Fashion Sense", new ImageOnly("Unusual Fashion Sense"));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 2:
          case 3:
            if (!request.responseText.contains("You acquire an effect")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(
        169, "A Few Chintz Curtains, Some Throw Pillows...", "The Mouldering Mansion") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "Night Vision -> flask of Amontillado, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Night Vision", new ImageOnly("Night Vision"))
            .attachItem("flask of Amontillado", 1, AUTO, new DisplayAll("flask of Amontillado"));
        new Option(2, "muscle substats", true).turnCost(1);
        new Option(3, "Dancing Prowess -> fancy ball mask, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Dancing Prowess", new ImageOnly("Dancing Prowess"))
            .attachItem("fancy ball mask", 1, AUTO, new DisplayAll("fancy ball mask"));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 3:
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(170, "La Vie Boheme", "The Rogue Windmill") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "HP & Rat-Faced", true).turnCost(1).attachEffect("Rat-Faced");
        new Option(2, "fight sensitive poet-type", true);
        new Option(3, "moxie substats", true).turnCost(1);
      }
    };

    new ChoiceAdventure(171, "Backstage at the Rogue Windmill", "The Rogue Windmill") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "Bats in the Belfry -> No Vertigo, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Bats in the Belfry", new ImageOnly("Bats in the Belfry"))
            .attachEffect("No Vertigo", new ImageOnly("No Vertigo"));
        new Option(2, "muscle substats", true).turnCost(1);
        new Option(3, "Spirit of Alph -> Dancing Prowess, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Spirit of Alph", new ImageOnly("Spirit of Alph"))
            .attachEffect("Dancing Prowess", new ImageOnly("Dancing Prowess"));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 3:
            if (!request.responseText.contains("You acquire an effect")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(172, "Up in the Hippo Room", "The Rogue Windmill") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "Good with the Ladies -> Can-Can skirt, otherwise fight can-can dancer", true)
            .turnCost(1)
            .attachEffect("Good with the Ladies", new ImageOnly("Good with the Ladies"))
            .attachItem("Can-Can skirt", 1, AUTO, new DisplayAll("Can-Can skirt"));
        new Option(2, "Feelin' Philosophical -> not-a-pipe, otherwise skip", true)
            .turnCost(1)
            .attachEffect("Feelin' Philosophical", new ImageOnly("Feelin' Philosophical"))
            .attachItem("not-a-pipe", 1, AUTO, new DisplayAll("not-a-pipe"));
        new Option(3, "mysticality substats", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 2:
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(173, "The Last Stand, Man", "undefined") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(174, "The Last Stand, Bra", "undefined") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new UnknownChoiceAdventure(175);

    new UnknownChoiceAdventure(176);

    new RetiredChoiceAdventure(177, "The Blackberry Cobbler", "The Black Forest") // now 928
    {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }
    };

    new ChoiceAdventure(178, "Hammering the Armory", "The Penultimate Fantasy Airship") {
      void setup() {
        new Option(1, "bronze breastplate", true)
            .turnCost(1)
            .attachItem("bronze breastplate", 1, AUTO);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new UnknownChoiceAdventure(179);

    new ChoiceAdventure(180, "A Pre-War Dresser Drawer, Pa!", "Inside the Palindome") {
      void setup() {
        new Option(1, "Ye Olde Navy Fleece or meat", true).turnCost(1).attachMeat(250, AUTO);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);
        if (KoLCharacter.isTorsoAware()) {
          option.attachItem(
              "Ye Olde Navy Fleece", 1, AUTO, new DisplayAll("Fleece", WANT, INV_ONLY, 0));
        } else {
          option.text("250 meat");
        }
      }
    };

    new ChoiceAdventure(
        181, "Chieftain of the Flies", "The Orcish Frat House (Bombed Back to the Stone Age)") {
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

    new ChoiceAdventure(182, "Random Lack of an Encounter", "The Penultimate Fantasy Airship") {
      final AdventureResult MODEL_AIRSHIP = ItemPool.get(ItemPool.MODEL_AIRSHIP, 1);

      void setup() {
        new Option(1, "enter combat", true);
        new Option(2, "Penultimate Fantasy chest", true)
            .turnCost(1)
            .attachItem("Penultimate Fantasy chest", 1, AUTO);
        new Option(3, "stats", true).turnCost(1);
        new Option(4, "model airship").turnCost(1).attachItem(MODEL_AIRSHIP, AUTO);

        new CustomOption(4, "model airship, then combat");
        new CustomOption(5, "model airship, then chest");
        new CustomOption(6, "model airship, then stats");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If the player is looking for the model airship,
        // then update their preferences so that KoLmafia
        // automatically switches things for them.
        int option4Mask = (responseText.contains("Gallivant down to the head") ? 1 : 0) << 2;

        if (option4Mask > 0 && GoalManager.hasGoal(MODEL_AIRSHIP)) {
          return "4";
        }
        if (Integer.parseInt(decision) < 4) {
          return decision;
        }

        return (option4Mask & Integer.parseInt(decision)) > 0
            ? "4"
            : String.valueOf(Integer.parseInt(decision) - 3);
      }
    };

    new UnknownChoiceAdventure(183);

    new ChoiceAdventure(184, "That Explains All The Eyepatches", "Barrrney's Barrr") {
      void setup() {
        new Option(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);

        new CustomOption(4, "3 drunk & stats");
        new CustomOption(5, "shot of rotgut");
        new CustomOption(6, "combat (or rotgut if Myst class)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // The choices are based on character class.
        // Mus: combat, shot of rotgut (2948), drunkenness
        // Mys: drunkenness, shot of rotgut (2948), shot of rotgut (2948)
        // Mox: combat, drunkenness, shot of rotgut (2948)

        Option option = getOption(1);
        if (KoLCharacter.isMysticalityClass()) {
          option.text("3 drunk and stats (varies by class)").turnCost(1);
        } else {
          option.text("enter combat (varies by class)");
        }

        option = getOption(2);
        if (KoLCharacter.isMoxieClass()) {
          option.text("3 drunk and stats (varies by class)");
        } else {
          option.text("shot of rotgut (varies by class)").attachItem(ItemPool.ROTGUT, 1, AUTO);
        }

        option = getOption(3);
        if (KoLCharacter.isMuscleClass()) {
          option.text("3 drunk and stats (varies by class)");
        } else {
          option.text("shot of rotgut (varies by class)").attachItem(ItemPool.ROTGUT, 1, AUTO);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        switch (KoLCharacter.getPrimeIndex() * 10 + StringUtilities.parseInt(decision)) {
            // Options 4-6 are mapped to the actual class-specific options:
            // 4=drunk & stats, 5=rotgut, 6=combat (not available to Myst)
            // Mus
          case 04:
            return "3";
          case 05:
            return "2";
          case 06:
            return "1";
            // Mys
          case 14:
            return "1";
          case 15:
            return "2";
          case 16:
            return "3";
            // Mox
          case 24:
            return "2";
          case 25:
            return "3";
          case 26:
            return "1";
        }
        return decision;
      }
    };

    new ChoiceAdventure(185, "Yes, You're a Rock Starrr", "Barrrney's Barrr") {
      void setup() {
        new Option(1, "base booze", true).turnCost(1);
        new Option(2, "mixed booze", true).turnCost(1);
        new Option(3, "if sober, fight, otherwise stats", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // 0 drunk: base booze, mixed booze, fight
        // More than 0 drunk: base booze, mixed booze, stats

        Option option = getOption(3);
        if (KoLCharacter.getInebriety() > 0) {
          option.text("stats").turnCost(1);
        } else {
          option.text("combat");
        }
      }
    };

    new ChoiceAdventure(186, "A Test of Testarrrsterone", "Barrrney's Barrr") {
      void setup() {
        new Option(1, "stats", true).turnCost(1);
        new Option(2, "3 drunkenness and stats", true).turnCost(1);
        new Option(3, "moxie", true).turnCost(1);
      }
    };

    new ChoiceAdventure(187, "Arrr You Man Enough?", "Barrrney's Barrr") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        float odds = BeerPongRequest.pirateInsultOdds() * 100.0f;

        getOption(1).text(KoLConstants.FLOAT_FORMAT.format(odds) + "% chance of winning");
        getOption(2).text(odds == 100.0f ? "Oh come on. Do it!" : "Try later");
      }
    };

    new ChoiceAdventure(188, "The Infiltrationist", "Frat House") {
      void setup() {
        this.isSuperlikely = true; // from Orcish Frat House blueprints

        this.customName = "Frathouse Blueprints";
        this.customZones.add("Item-Driven");

        new Option(1, "frat boy ensemble", true)
            .turnCost(1)
            .attachItem("Orcish baseball cap", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("Orcish frat-paddle", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("Orcish cargo shorts", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1));
        new Option(2, "mullet wig and briefcase", true)
            .turnCost(1)
            .attachItem(ItemPool.MULLET_WIG, new DisplayAll("wig", NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem(ItemPool.BRIEFCASE, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(3, "frilly skirt and 3 hot wings", true)
            .turnCost(1)
            .attachItem(ItemPool.FRILLY_SKIRT, new DisplayAll("skirt", NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem(ItemPool.HOT_WING, -3, MANUAL, new DisplayAll(NEED, AT_LEAST, 3));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Once you're inside the frat house, it's a simple
        // matter of making your way down to the basement and
        // retrieving Caronch's dentures from the frat boys'
        // ridiculous trophy case.

        if (decision == 3 && !request.responseText.contains("ridiculous trophy case")) {
          this.choiceFailed().turnCost(1);
        }
      }
    };

    new ChoiceAdventure(189, "O Cap'm, My Cap'm", "The Poop Deck") {
      void setup() {
        new Option(1).turnCost(1).attachMeat(-977, MANUAL);
        new Option(2, "skip adventure");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("Try again when you've got the Meat")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          QuestDatabase.setQuestIfBetter(Quest.NEMESIS, "step26");
        }
      }
    };

    new UnknownChoiceAdventure(190);

    new ChoiceAdventure(191, "Chatterboxing", "The F'c'le") {
      void setup() {
        new Option(1, "moxie substats", true).turnCost(1);
        new Option(2, "with valuable trinket, banish chatty pirate, otherwise lose hp", true)
            .turnCost(1)
            .attachItem(
                ItemPool.VALUABLE_TRINKET,
                -1,
                MANUAL,
                new DisplayAll("trinket", NEED, AT_LEAST, 1));
        new Option(3, "muscle substats", true).turnCost(1);
        new Option(4, "mysticality substats", true).turnCost(1);

        new CustomOption(5, "use valuable trinket to banish, or moxie");
        new CustomOption(6, "use valuable trinket to banish, or muscle");
        new CustomOption(7, "use valuable trinket to banish, or mysticality");
        new CustomOption(8, "use valuable trinket to banish, or mainstat");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        boolean trink = InventoryManager.getCount(ItemPool.VALUABLE_TRINKET) > 0;
        switch (StringUtilities.parseInt(decision)) {
          case 5: // banish or mox
            return trink ? "2" : "1";
          case 6: // banish or mus
            return trink ? "2" : "3";
          case 7: // banish or mys
            return trink ? "2" : "4";
          case 8: // banish or mainstat
            if (trink) return "2";
            switch (KoLCharacter.mainStat()) {
              case MUSCLE:
                return "3";
              case MYSTICALITY:
                return "4";
              case MOXIE:
                return "1";
              default:
                return "0";
            }
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (request.responseText.contains("find a valuable trinket that looks promising")) {
            BanishManager.banishMonster("chatty pirate", "chatterboxing");
          } else if (request.responseText.contains("can't find anything that fits the bill")) {
            this.choiceFailed().turnCost(1);
          }
        }
      }
    };

    new UnknownChoiceAdventure(192);

    new RetiredChoiceAdventure(193, "Modular, Dude", "Sinister Dodecahedron") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(194, "Modular, Dude", "Sinister Dodecahedron") { // bis
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new UnknownChoiceAdventure(195);

    new UnknownChoiceAdventure(196);

    new ChoiceAdventure(197, "Somewhat Higher and Mostly Dry", "A Maze of Sewer Tunnels") {
      void setup() {
        new Option(1, "take the tunnel", true).turnCost(1);
        new Option(2, "fight sewer gator", true);
        new Option(3, "turn the valve", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        HobopolisManager.checkDungeonSewers(request, decision);

        if (decision == 3 && request.responseText.contains("water doesn't change")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(198, "Disgustin' Junction", "A Maze of Sewer Tunnels") {
      void setup() {
        new Option(1, "take the tunnel", true).turnCost(1);
        new Option(2, "fight giant zombie goldfish", true);
        new Option(3, "open the grate", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        HobopolisManager.checkDungeonSewers(request, decision);

        if (decision == 3 && request.responseText.contains("can't get the crank to budge")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(199, "The Former or the Ladder", "A Maze of Sewer Tunnels") {
      void setup() {
        new Option(1, "take the tunnel", true).turnCost(1);
        new Option(2, "fight C. H. U. M.", true);
        new Option(3, "head down the ladder", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        HobopolisManager.checkDungeonSewers(request, decision);
      }
    };
  }

  static final void handleStartOfWar() {
    QuestDatabase.setQuestProgress(QuestDatabase.Quest.ISLAND_WAR, "step1");
    Preferences.setString("warProgress", "started");
    if (KoLCharacter.inPokefam()) {
      // The following is a guess. Since all
      // sidequests are open, it is at least
      // 458, and surely both sides are equal
      Preferences.setInteger("hippiesDefeated", 500);
      Preferences.setInteger("fratboysDefeated", 500);
    }
  }
}
