package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.objectpool.SkillPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.persistence.SkillDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.PyramidRequest;
import net.sourceforge.kolmafia.request.QuestLogRequest;
import net.sourceforge.kolmafia.session.ClanManager;
import net.sourceforge.kolmafia.session.EncounterManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.LightsOutManager;
import net.sourceforge.kolmafia.session.Limitmode;
import net.sourceforge.kolmafia.session.LouvreManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.TurnCounter;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase900to999 extends ChoiceAdventureDatabase {
  final void add900to999() {
    new ChoiceAdventure(900, "Lights Out in the Billiards Room", "The Haunted Billiards Room") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TurnCounter.stopCounting("Spookyraven Lights Out");
        Preferences.setInteger("lastLightsOutTurn", KoLCharacter.getTurnsPlayed());
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return LightsOutManager.lightsOutAutomation(this.choice, responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains(
                "The wolf head has a particularly nasty expression on its face")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Wine Cellar");
        }
      }
    };

    new ChoiceAdventure(901, "Lights Out in the Wine Cellar", "The Haunted Wine Cellar") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TurnCounter.stopCounting("Spookyraven Lights Out");
        Preferences.setInteger("lastLightsOutTurn", KoLCharacter.getTurnsPlayed());
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return LightsOutManager.lightsOutAutomation(this.choice, responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("Crumbles II (Wolf)")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Boiler Room");
        }
      }
    };

    new ChoiceAdventure(902, "Lights Out in the Boiler Room", "The Haunted Boiler Room") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TurnCounter.stopCounting("Spookyraven Lights Out");
        Preferences.setInteger("lastLightsOutTurn", KoLCharacter.getTurnsPlayed());
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return LightsOutManager.lightsOutAutomation(this.choice, responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("CRUMBLES II")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Laboratory");
        }
      }
    };

    new ChoiceAdventure(903, "Lights Out in the Laboratory", "The Haunted Laboratory") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TurnCounter.stopCounting("Spookyraven Lights Out");
        Preferences.setInteger("lastLightsOutTurn", KoLCharacter.getTurnsPlayed());
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return LightsOutManager.lightsOutAutomation(this.choice, responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // The correct option leads to a combat with Stephen.
        // If you win, we will set "nextSpookyravenStephenRoom" to "none"
      }
    };

    new ChoiceAdventure(904, "Louvre It or Leave It", "The Haunted Gallery") { // Relativity Start
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(
        905, "Louvre It or Leave It", "The Haunted Gallery") { // The Persistence of Memory
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(906, "Louvre It or Leave It", "The Haunted Gallery") { // Piet Mondrian
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(907, "Louvre It or Leave It", "The Haunted Gallery") { // The Scream
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(908, "Louvre It or Leave It", "The Haunted Gallery") { // The Birth of Venus
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(
        909, "Louvre It or Leave It", "The Haunted Gallery") { // The Creation of Adam
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(
        910, "Louvre It or Leave It", "The Haunted Gallery") { // The Death of Socrates
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(911, "Louvre It or Leave It", "The Haunted Gallery") { // Nighthawks
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(
        912,
        "Louvre It or Leave It",
        "The Haunted Gallery") { // Sunday Afternoon on the Island of La Grande Jatte
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(913, "Louvre It or Leave It", "The Haunted Gallery") { // The Last Supper
      void setup() {
        this.hasGoalButton = true;

        LouvreManager.choiceSpoilers(
            this.choice,
            i -> {
              return new Option(i);
            });
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return LouvreManager.encounterName(choice);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LouvreManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("")) {
          return LouvreManager.handleChoice(this.choice, stepCount);
        }

        return decision;
      }
    };

    new ChoiceAdventure(914, "Louvre It or Leave It", "The Haunted Gallery") {
      void setup() {
        this.customName = "Louvre Goal";

        new Option(1, "Enter the Drawing").leadsTo(904);
        new Option(2, "skip adventure").entersQueue(false);

        new CustomOption(
            0,
            "Ignore this adventure",
            () -> {
              Preferences.setInteger("louvreDesiredGoal", 0);
            });
        int i = 0;
        for (String goal : LouvreManager.LouvreGoals) {
          int index = ++i;
          new CustomOption(
              index,
              goal,
              () -> {
                Preferences.setInteger("louvreDesiredGoal", index);
              });
        }
        int index = i;
        new CustomOption(
            index + 1,
            "Boost Prime Stat",
            () -> {
              Preferences.setInteger("louvreDesiredGoal", index + 1);
            });
        new CustomOption(
            index + 2,
            "Boost Lowest Stat",
            () -> {
              Preferences.setInteger("louvreDesiredGoal", index + 2);
            });

        this.customLoad =
            () -> {
              int desiredGoal = Preferences.getInteger("louvreDesiredGoal");
              if (desiredGoal < 0 || desiredGoal > index + 2) { // you didn't see nuthin'
                System.out.println("Invalid setting " + desiredGoal + " for louvreDesiredGoal");
                return 0;
              }
              return desiredGoal;
            };

        this.customPreferencesToListen.add("louvreDesiredGoal");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Sometimes, the choice adventure for the louvre
        // loses track of whether to ignore the louvre or not.

        LouvreManager.resetDecisions();
        return Preferences.getInteger("louvreGoal") != 0 ? "1" : "2";
      }
    };

    new ChoiceAdventure(915, "Et Tu, Buff Jimmy?", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("skinny mushroom girls")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_MUSHROOM, QuestDatabase.STARTED);
        } else if (request.responseText.contains(
            "But here's a few Beach Bucks as a token of my changes in gratitude")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_MUSHROOM, QuestDatabase.FINISHED);
          getOption(decision).attachItem(ItemPool.PENCIL_THIN_MUSHROOM, -10, MANUAL);
        } else if (request.responseText.contains("not really into moving out of this hammock")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_CHEESEBURGER, QuestDatabase.STARTED);
          Preferences.setInteger("buffJimmyIngredients", 0);
        } else if (request.responseText.contains(
            "So I'll just give you some Beach Bucks instead")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_CHEESEBURGER, QuestDatabase.FINISHED);
          Preferences.setInteger("buffJimmyIngredients", 0);
          getOption(decision).attachItem(ItemPool.CHEESEBURGER_RECIPE, -1, MANUAL);
        } else if (request.responseText.contains("sons of sons of sailors are")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_SALT, QuestDatabase.STARTED);
        } else if (request.responseText.contains("So here's some Beach Bucks instead")) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_SALT, QuestDatabase.FINISHED);
          getOption(decision).attachItem(ItemPool.SAILOR_SALT, -50, MANUAL);
        }
      }
    };

    new ChoiceAdventure(916, "Taco Dan's Taco Stand's Taco Dan", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("find those receipts")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_AUDIT, QuestDatabase.STARTED);
        } else if (request.responseText.contains(
            "Here's a little Taco Dan's Taco Stand gratitude for ya")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_AUDIT, QuestDatabase.FINISHED);
          getOption(decision).attachItem(ItemPool.TACO_DAN_RECEIPT, -10, MANUAL);
        } else if (request.responseText.contains("fill it up with as many cocktail drippings")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_COCKTAIL, QuestDatabase.STARTED);
          Preferences.setInteger("tacoDanCocktailSauce", 0);
        } else if (request.responseText.contains(
            "sample of Taco Dan's Taco Stand's Tacoriffic Cocktail Sauce")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_COCKTAIL, QuestDatabase.FINISHED);
          Preferences.setInteger("tacoDanCocktailSauce", 0);
          getOption(decision).attachItem(ItemPool.TACO_DAN_SAUCE_BOTTLE, -1, MANUAL);
        } else if (request.responseText.contains("get enough taco fish")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_FISH, QuestDatabase.STARTED);
          Preferences.setInteger("tacoDanFishMeat", 0);
        } else if (request.responseText.contains(
            "batch of those Taco Dan's Taco Stand's Taco Fish Tacos")) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_FISH, QuestDatabase.FINISHED);
          Preferences.setInteger("tacoDanFishMeat", 0);
        }
      }
    };

    new ChoiceAdventure(917, "Do You Even Brogurt", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("need about ten shots of it")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_BACTERIA, QuestDatabase.STARTED);
          Preferences.setInteger("brodenBacteria", 0);
        } else if (request.responseText.contains("YOLO cup to spit the bacteria into")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_BACTERIA, QuestDatabase.FINISHED);
          Preferences.setInteger("brodenBacteria", 0);
        } else if (request.responseText.contains("loan you my sprinkle shaker to fill up")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_SPRINKLES, QuestDatabase.STARTED);
          Preferences.setInteger("brodenSprinkles", 0);
        } else if (request.responseText.contains("can sell some <i>deluxe</i> brogurts")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_SPRINKLES, QuestDatabase.FINISHED);
          Preferences.setInteger("brodenSprinkles", 0);
          getOption(decision).attachItem(ItemPool.SPRINKLE_SHAKER, -1, MANUAL);
        } else if (request.responseText.contains("There were like fifteen of these guys")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_DEBT, QuestDatabase.STARTED);
        } else if (request.responseText.contains("And they all had broupons, huh")) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_DEBT, QuestDatabase.FINISHED);
          getOption(decision).attachItem(ItemPool.BROUPON, -15, MANUAL);
        }
      }
    };

    new ChoiceAdventure(918, "Yachtzee!", "The Sunken Party Yacht") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get cocktail ingredients (sometimes Ultimate Mind Destroyer)", true)
            .turnCost(1);
        new Option(2, "get 5k meat and random item", true).turnCost(1).attachMeat(5000, AUTO);
        new Option(3, "trade moist beads for Beach Bucks", true)
            .turnCost(1)
            .attachItem(ItemPool.BEACH_BUCK, 1, AUTO)
            .attachItem(ItemPool.MOIST_BEADS, new DisplayAll("beads"));
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);

        // Is it 7 or more days since the last time you got the Ultimate Mind Destroyer?
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT-0700"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastUMDDateString = Preferences.getString("umdLastObtained");
        if (lastUMDDateString != null && lastUMDDateString != "") {
          try {
            Date lastUMDDate = sdf.parse(lastUMDDateString);
            Calendar compareDate = Calendar.getInstance(TimeZone.getTimeZone("GMT-0700"));
            compareDate.setTime(lastUMDDate);
            compareDate.add(Calendar.DAY_OF_MONTH, 7);
            if (date.compareTo(compareDate) >= 0) {
              option
                  .text("get Ultimate Mind Destroyer")
                  .attachItem(ItemPool.MIND_DESTROYER, 1, AUTO);
            } else {
              option.text("get cocktail ingredients");
            }
          } catch (ParseException ex) {
            KoLmafia.updateDisplay("Unable to parse " + lastUMDDateString);
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3 && request.responseText.contains("You open the captain's door")) {
          int beads = Math.min(InventoryManager.getCount(ItemPool.MOIST_BEADS), 100);
          getOption(decision).attachItem(ItemPool.MOIST_BEADS, -beads, MANUAL);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("Ultimate Mind Destroyer")) {
          Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT-0700"));
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          String today = sdf.format(date.getTime());
          Preferences.setString("umdLastObtained", today);
        }
      }
    };

    new ChoiceAdventure(919, "Break Time!", "Sloppy Seconds Diner") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get Beach Bucks", true).turnCost(1).attachItem(ItemPool.BEACH_BUCK, 1, AUTO);
        new Option(2, "+15ML on Sundaes", true).turnCost(1);
        new Option(3, "+15ML on Burgers", true).turnCost(1);
        new Option(4, "+15ML on Cocktails", true).turnCost(1);
        new Option(5, "reset ML on monsters", true).turnCost(1);
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Abort if you have plundered the register too many times today
        if (decision.equals("1") && responseText.contains("You've already thoroughly")) {
          return "6";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("You've already thoroughly")) {
          this.choiceFailed().entersQueue(false);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("You've already thoroughly")) {
            Preferences.setInteger("_sloppyDinerBeachBucks", 4);
          } else {
            Preferences.increment("_sloppyDinerBeachBucks", 1);
          }
        }
      }
    };

    new ChoiceAdventure(920, "Eraser", "Item-Driven") {
      void setup() {
        this.customName = "Ultimate Mind Destroyer";

        new Option(1, "reset Buff Jimmy quests", true)
            .attachItem(ItemPool.MIND_DESTROYER, -1, MANUAL, new NoDisplay());
        new Option(2, "reset Taco Dan quests", true)
            .attachItem(ItemPool.MIND_DESTROYER, -1, MANUAL, new NoDisplay());
        new Option(3, "reset Broden quests", true)
            .attachItem(ItemPool.MIND_DESTROYER, -1, MANUAL, new NoDisplay());
        new Option(4, "don't use it", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          QuestDatabase.setQuestProgress(Quest.JIMMY_MUSHROOM, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.JIMMY_CHEESEBURGER, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.JIMMY_SALT, QuestDatabase.UNSTARTED);
        } else if (decision == 2) {
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_AUDIT, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_COCKTAIL, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.TACO_DAN_FISH, QuestDatabase.UNSTARTED);
        } else if (decision == 3) {
          QuestDatabase.setQuestProgress(Quest.BRODEN_BACTERIA, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.BRODEN_SPRINKLES, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.BRODEN_DEBT, QuestDatabase.UNSTARTED);
        }
      }
    };

    new ChoiceAdventure(921, "We'll All Be Flat", "The Haunted Ballroom") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.MANOR, "step1");
      }
    };

    new ChoiceAdventure(922, "Summoning Chamber", "Summoning Chamber") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(923, "All Over the Map", "The Black Forest") {
      void setup() {
        new Option(1).leadsTo(924, true, (Option o) -> true);
        new Option(2).leadsTo(925, false, (Option o) -> o.index != 6);
        new Option(3).leadsTo(926, false, (Option o) -> o.index != 6);
        new Option(4).leadsTo(927, false, (Option o) -> o.index != 6);

        new CustomOption(1, "Blackberry");
        new CustomOption(2, "Blacksmith");
        new CustomOption(3, "Black Gold Mine");
        new CustomOption(4, "Black Church");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Manual control if the choice you want isn't available
        if ((decision.equals("2") && !responseText.contains("Visit the blacksmith's cottage"))
            || (decision.equals("3") && !responseText.contains("Go to the black gold mine"))
            || (decision.equals("4") && !responseText.contains("Check out the black church"))) {
          return "0";
        }
        return decision;
      }
    };

    new ChoiceAdventure(924, "You Found Your Thrill", "The Black Forest") {
      void setup() {
        this.customName = "Blackberry";

        new Option(1, "fight blackberry bush", true).attachItem(ItemPool.BLACKBERRY);
        new Option(2, "visit cobbler").leadsTo(928);
        new Option(3, "head towards beehive (1)", true).turnCost(1).leadsTo(1018);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          if (InventoryManager.hasItem(ItemPool.BEEHIVE)) {
            getOption(3).reset();
          }
        }
      }
    };

    new ChoiceAdventure(925, "The Blackest Smith", "The Black Forest") {
      void setup() {
        this.customName = "Blacksmith";

        new Option(1, null, true).turnCost(1).attachItem("black sword", 1, AUTO);
        new Option(2, null, true).turnCost(1).attachItem("black shield", 1, AUTO);
        new Option(3, null, true).turnCost(1).attachItem("black helmet", 1, AUTO);
        new Option(4, null, true).turnCost(1).attachItem("black greaves", 1, AUTO);
        new Option(6, "return to main choice").leadsTo(923);
      }
    };

    new ChoiceAdventure(926, "Be Mine", "The Black Forest") {
      void setup() {
        this.customName = "Black Gold Mine";

        new Option(1, "get black gold", true).turnCost(1).attachItem("black gold", 1, AUTO);
        new Option(2, "get Texas tea", true).turnCost(1).attachItem("Texas tea", 1, AUTO);
        new Option(3, "30 adv of Black Lung", true).turnCost(1).attachEffect("Black Lung");
        new Option(6, "return to main choice").leadsTo(923);
      }
    };

    new ChoiceAdventure(927, "Sunday Black Sunday", "The Black Forest") {
      void setup() {
        this.customName = "Black Church";

        new Option(1, "get 13 turns of Salsa Satanica or beaten up", true)
            .turnCost(1)
            .attachEffect("Salsa Satanica", new ImageOnly("Satanica"))
            .attachItem("black helmet", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("black sword", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("black shield", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("black greaves", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("black cloak", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("dark baconstone ring", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("dark hamethyst ring", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("dark porquoise ring", new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1));
        new Option(2, "get black kettle drum", true)
            .turnCost(1)
            .attachItem("black kettle drum", 1, AUTO);
        new Option(6, "return to main choice").leadsTo(923);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && request.responseText.contains("You're not sure your soul can handle")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(928, "The Blackberry Cobbler", "The Black Forest") {
      void setup() {
        this.customName = "Blackberry Cobbler";

        new Option(1, null, true)
            .turnCost(1)
            .attachItem("blackberry slippers", 1, AUTO)
            .attachItem(ItemPool.BLACKBERRY, -3, MANUAL, new NoDisplay());
        new Option(2, null, true)
            .turnCost(1)
            .attachItem("blackberry moccasins", 1, AUTO)
            .attachItem(ItemPool.BLACKBERRY, -3, MANUAL, new NoDisplay());
        new Option(3, null, true)
            .turnCost(1)
            .attachItem("blackberry combat boots", 1, AUTO)
            .attachItem(ItemPool.BLACKBERRY, -3, MANUAL, new NoDisplay());
        new Option(4, null, true)
            .turnCost(1)
            .attachItem("blackberry galoshes", 1, AUTO)
            .attachItem(ItemPool.BLACKBERRY, -3, MANUAL, new NoDisplay());
        new Option(6, "return to main choice").leadsTo(923);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision != 6 && request.responseText.contains("You don't have enough blackberries")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(929, "Control Freak", null) {
      void setup() {
        this.canWalkFromChoice = true;

        this.customName = "Control Room";
        this.customZones.add("Pyramid");

        new Option(1, "turn lower chamber, lose wheel", true)
            .attachItem(ItemPool.CRUMBLING_WHEEL, -1, MANUAL);
        new Option(2, "turn lower chamber, lose ratchet", true)
            .attachItem(ItemPool.TOMB_RATCHET, -1, MANUAL);
        new Option(5).turnCost(1);
        new Option(6);

        new CustomOption(5, "enter lower chamber");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("wooden wheel disintegrating")) {
            PyramidRequest.advancePyramidPosition();
          } else {
            this.choiceFailed();
          }
        } else if (decision == 2) {
          if (request.responseText.contains("snap the ratchet onto the peg")) {
            PyramidRequest.advancePyramidPosition();
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(930, "Another Errand I Mean Quest", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          QuestDatabase.setQuestIfBetter(Quest.CITADEL, QuestDatabase.STARTED);
        }
      }
    };

    new ChoiceAdventure(
        931, "Life Ain't Nothin But Witches and Mummies", "The Road to the White Citadel") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        QuestDatabase.setQuestIfBetter(Quest.CITADEL, "step6");
      }
    };

    new ChoiceAdventure(932, "No Whammies", "The Road to the White Citadel") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          QuestDatabase.setQuestIfBetter(Quest.CITADEL, "step8");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("sharp white teeth of a mimic")
            || request.responseText.contains("steel your nerves for what lies ahead")) {
          getOption(decision).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("steel your nerves for what lies ahead")) {
          QuestDatabase.setQuestProgress(Quest.CITADEL, "step9");
        }
      }
    };

    new UnknownChoiceAdventure(933);

    new UnknownChoiceAdventure(934);

    new RetiredChoiceAdventure(935, "Lost in Space... Ship", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(936, "The Nerve Center", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(937, "The Spacement", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(938, "The Ship's Kitchen", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(939, "What Outfit?", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(940, "Let Your Fists Do The Walking", "Whitey's Grove") {
      void setup() {
        this.customName = "white page";

        new Option(1).attachItem(ItemPool.WHITE_PAGE, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.WHITE_PAGE, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.WHITE_PAGE, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.WHITE_PAGE, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.WHITE_PAGE, -1, MANUAL, new NoDisplay());
        new Option(6);

        new CustomOption(1, "fight whitesnake");
        new CustomOption(2, "fight white lion");
        new CustomOption(3, "fight white chocolate golem");
        new CustomOption(4, "fight white knight");
        new CustomOption(5, "fight white elephant");
        new CustomOption(6, "skip");
      }
    };

    new ChoiceAdventure(941, "This Turtle Rocks!", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(942, "Even Tamer Than Usual", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(943, "Really Sticking Her Neck Out", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(944, "More Like... Hurtle", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(945, "Musk! Musk! Musk!", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(946, "Armchair Quarterback", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(947, "Two Birds", "Drunken Stupor") { // 1/2
      void setup() {
        this.isSuperlikely = true;

        new Option(1).leadsTo(948);
      }
    };

    new ChoiceAdventure(948, "Two Birds", "Drunken Stupor") { // 2/2
      void setup() {
        new Option(1).leadsTo(949);
      }
    };

    new ChoiceAdventure(949, "One Bucket", "Drunken Stupor") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(950, "Time-Twitching Tower Voting / Phone Booth", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(951, "They Did the Vote. They Did the Monster Vote.", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
      }
    };

    new RetiredChoiceAdventure(952, "Choose wisely. Or choose carelessly. Who cares.", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new RetiredChoiceAdventure(953, "Name That Team...", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(954, "Ook the Mook", "The Cave Before Time") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(955, "Time Cave.  Period.", "The Cave Before Time") {
      void setup() {
        this.customName = "Time Cave";

        new Option(1, "fight Adventurer echo", true);
        new Option(2, "twitching time capsule", true)
            .turnCost(1)
            .attachItem("twitching time capsule", 1, AUTO);
        new Option(3, "talk to caveman", true).leadsTo(954);
      }
    };

    new RetiredChoiceAdventure(956, "What does the future hold?", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }
    };

    new ChoiceAdventure(957, "It Came from Beneath the Sewer? Great!", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(958, "Close, but Yes Cigar", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(959, "Slow and Steady Wins the Brawl", "A Barroom Brawl") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(960, "Harem Scarum", "Cobb's Knob Harem") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(961, "The worst kind of drowning", "Guano Junction") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(962, "The Real Victims", "Oil Peak") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(963, "Slow Food", "The Castle in the Clouds in the Sky (Ground Floor)") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(964, "Stormy Weather", "The Castle in the Clouds in the Sky (Top Floor)") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(965, "Slow Road to Hell", "The Haunted Boiler Room") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(966, "Why Is the World In Love Again?", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(967, "The Thunder Rolls...", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(6).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(7).attachItem(ItemPool.THUNDER_THIGH, -1, MANUAL, new NoDisplay());
        new Option(8);
      }
    };

    new ChoiceAdventure(968, "The Rain Falls Down With Your Help...", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(6).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(7).attachItem(ItemPool.AQUA_BRAIN, -1, MANUAL, new NoDisplay());
        new Option(8);
      }
    };

    new ChoiceAdventure(969, "And The Lightning Strikes...", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(6).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(7).attachItem(ItemPool.LIGHTNING_MILK, -1, MANUAL, new NoDisplay());
        new Option(8);
      }
    };

    new ChoiceAdventure(970, "Rainy Fax Dreams on your Wedding Day", null) {
      void setup() {
        // can walk away, but do NOT want to (skill cost not refunded)

        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          EncounterManager.ignoreSpecialMonsters();
          KoLAdventure.lastLocationURL = request.getURLString();
          GenericRequest.itemMonster = "Rain Man";
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          KoLCharacter.incrementRain(SkillDatabase.getRainCost(SkillPool.RAIN_MAN));
        }
      }
    };

    new RetiredChoiceAdventure(971, "They Did the Vote. They Did the Monster Vote.", null) { // bis
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
      }
    };

    new RetiredChoiceAdventure(972, "They Did the Vote. They Did the Monster Vote.", null) { // ter
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
      }
    };

    new ChoiceAdventure(973, "Shoe Repair Store", null) {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;
        this.customZones.add("Twitch");

        new Option(1);
        new Option(2, "exchange hooch for Chroners", true)
            .turnCost(1)
            .attachItem(ItemPool.CHRONER, 1, AUTO);
        new Option(6);

        new CustomOption(1, "visit shop");
        new CustomOption(6, "leave");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Leave if you have no hooch but have chosen to exchange hooch for chroners
        if (decision.equals("2") && !responseText.contains("Turn in Hooch")) {
          return "6";
        }
        return decision;
      }
    };

    new ChoiceAdventure(974, "Around The World", "An Illicit Bohemian Party") {
      final Pattern PINK_WORD_PATTERN =
          Pattern.compile(
              "scrawled in lipstick on a cocktail napkin:  <b><font color=pink>(.*?)</font></b>");

      void setup() {
        new Option(1, "get up to 5 hooch", true).turnCost(1);
        new Option(2, "leave", true).entersQueue(false);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Matcher pinkWordMatcher = PINK_WORD_PATTERN.matcher(request.responseText);
        if (pinkWordMatcher.find()) {
          String pinkWord = pinkWordMatcher.group(1);
          String message =
              "Bohemian Party Pink Word found: "
                  + pinkWord
                  + " in clan "
                  + ClanManager.getClanName(false)
                  + ".";
          RequestLogger.printLine("<font color=\"blue\">" + message + "</font>");
          RequestLogger.updateSessionLog(message);
        }
      }
    };

    new ChoiceAdventure(975, "Crazy Still After All These Years", "Moonshiners' Woods") {
      void setup() {
        new Option(1, "swap 5 cocktail onions for 10 hooch", true)
            .turnCost(1)
            .attachItem(ItemPool.COCKTAIL_ONION, -5, MANUAL);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Leave if you have less than 5 cocktail onions, even if you haven't decided to
        if (!responseText.contains("Stick in the onions")) {
          return "2";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("toss 5 cocktail onions into the still")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(976, "Ed the Undrowning", "The Lower Chambers") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(977, "The Chariot-Racing Colosseum", null) {
      void setup() {
        new Option(1).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(6).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(7).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(8).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(9).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(10).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(11).attachItem(ItemPool.CHRONER, -10, MANUAL, new NoDisplay());
        new Option(12);
        new Option(13).leadsTo(981);
      }
    };

    new RetiredChoiceAdventure(
        978, "They Did the Vote. They Did the Monster Vote.", null) { // quater
      void setup() {
        // just a guess based on the previous ones
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
      }
    };

    new ChoiceAdventure(979, "The Agora", "The Roman Forum") {
      void setup() {
        this.customName = this.name;

        new Option(1).leadsTo(980);
        new Option(2).leadsTo(982);
        new Option(6).leadsTo(983);

        new CustomOption(1, "get blessing");
        new CustomOption(2, "visit store");
        new CustomOption(6, "play dice");
      }
    };

    new ChoiceAdventure(980, "Welcome to Blessings Hut", "The Roman Forum") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true).turnCost(1).attachItem("Bruno's blessing of Mars", 1, AUTO);
        new Option(2, null, true).turnCost(1).attachItem("Dennis's blessing of Minerva", 1, AUTO);
        new Option(3, null, true).turnCost(1).attachItem("Burt's blessing of Bacchus", 1, AUTO);
        new Option(4, null, true).turnCost(1).attachItem(ItemPool.MERCURY_BLESSING, 1, AUTO);
        new Option(6).leadsTo(979);

        new CustomOption(6, "return to Agora");
      }
    };

    new ChoiceAdventure(981, "The Back Room", null) {
      void setup() {
        new Option(1).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(6).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(7).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(8).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(9).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(10).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(11).attachItem(ItemPool.MERCURY_BLESSING, -1, MANUAL, new NoDisplay());
        new Option(12).leadsTo(977);
      }
    };

    new ChoiceAdventure(982, "The 99-Centurion Store", "The Roman Forum") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem("centurion helmet", 1, AUTO)
            .attachItem(ItemPool.CHRONER, -1, MANUAL);
        new Option(2, null, true)
            .turnCost(1)
            .attachItem("pteruges", 1, AUTO)
            .attachItem(ItemPool.CHRONER, -1, MANUAL);
        new Option(6).leadsTo(979);

        new CustomOption(6, "return to Agora");
      }
    };

    new ChoiceAdventure(983, "Playing Dice With Romans", "The Roman Forum") {
      void setup() {
        this.customName = this.name;

        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.CHRONER, 5, AUTO)
            .attachItem(ItemPool.CHRONER, -1, MANUAL);
        new Option(6).leadsTo(979);

        new CustomOption(1, "make a bet and throw dice");
        new CustomOption(6, "return to Agora");
      }
    };

    new ChoiceAdventure(984, "A Radio on a Beach", null) {
      final Pattern RADIO_STATIC_PATTERN =
          Pattern.compile("<p>(?!(?:<form|</center>))(.+?)(?=<[^i</])");
      final Map<Quest, String> conspiracyQuestMessages = new HashMap<>();

      void setup() {
        conspiracyQuestMessages.put(
            Quest.CLIPPER,
            "&quot;Attention any available operative. Attention any available operative. A reward has been posted for DNA evidence gathered from Lt. Weirdeaux's subjects inside Site 15. The DNA is to be gathered via keratin extraction. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.EVE,
            "&quot;Attention Operative 01-A-A. General Sitterson reports a... situation involving experiment E-V-E-6. Military intervention has been requested. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.FAKE_MEDIUM,
            "&quot;Attention Operative EC-T-1. An outside client has expressed interest in the acquisition of an ESP suppression collar from the laboratory. Operationally significant sums of money are involved. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.GORE,
            "&quot;Attention any available operative. Attention any available operative. Laboratory overseer General Sitterson reports unacceptable levels of environmental gore. Several elevator shafts are already fully clogged, limiting staff mobility, and several surveillance camera lenses have been rendered opaque, placing the validity of experimental data at risk. Immediate janitorial assistance is requested. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.JUNGLE_PUN,
            "&quot;Attention any available operative. Attention any available operative. The director of Project Buena Vista has posted a significant bounty for the collection of jungle-related puns. Repeat: Jungle-related puns. Non-jungle puns or jungle non-puns will not be accepted. Non-jungle non-puns, by order of the director, are to be rewarded with summary execution. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.OUT_OF_ORDER,
            "&quot;Attention Operative QZ-N-0. Colonel Kurzweil at Jungle Interior Camp 4 reports the theft of Project T. L. B. materials. Requests immediate assistance. Is confident that it has not yet been removed from the jungle. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.SERUM,
            "&quot;Attention Operative 21-B-M. Emergency deployment orders have been executed due to a shortage of experimental serum P-00. Repeat: P Zero Zero. Lt. Weirdeaux is known to have P-00 manufacturing facilities inside the Site 15 mansion. Message repeats.&quot;");
        conspiracyQuestMessages.put(
            Quest.SMOKES,
            "&quot;Attention Operative 00-A-6. Colonel Kurzweil at Jungle Interior Camp 4 reports that they have run out of smokes. Repeat: They have run out of smokes. Requests immediate assistance. Message repeats.&quot;");

        this.canWalkFromChoice = true;

        new Option(1);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("Awaiting mission")) {
          return;
        }

        Matcher staticMatcher = RADIO_STATIC_PATTERN.matcher(request.responseText);

        String snippet = ".*";

        while (staticMatcher.find()) {
          String section = staticMatcher.group(1);

          if (section.contains("You turn the biggest knob on the radio")) {
            continue;
          }

          for (String part : section.split("&lt;.*?&gt;")) {
            if (part.startsWith("&lt;") || part.length() < 3) {
              continue;
            }

            part = part.replaceAll("\\.* *$", "");
            part = part.replace("  ", " ");
            snippet += Pattern.quote(part) + ".*";
          }
        }

        Iterator<Entry<Quest, String>> iterator = conspiracyQuestMessages.entrySet().iterator();

        Quest todaysQuest = null;

        while (iterator.hasNext()) {
          Map.Entry<Quest, String> entry = iterator.next();

          if (Pattern.matches(snippet, entry.getValue())) {
            if (todaysQuest == null) {
              todaysQuest = entry.getKey();
            } else {
              // Multiple matches
              todaysQuest = null;
              break;
            }
          }
        }

        if (todaysQuest != null) {
          Preferences.setString("_questESp", todaysQuest.getPref());
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Clear quests when accepting a new one as you can only have one
        if (request.responseText.contains("your best paramilitary-sounding radio lingo")) {
          QuestDatabase.setQuestProgress(Quest.JUNGLE_PUN, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.GORE, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.CLIPPER, QuestDatabase.UNSTARTED);
        }
        // Also clear repeatable quests if there is no quest active at the radio
        if (request.responseText.contains("Maybe try again tomorrow")) {
          QuestDatabase.setQuestProgress(Quest.JUNGLE_PUN, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.GORE, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.CLIPPER, QuestDatabase.UNSTARTED);
        } else if (request.responseText.contains("navigation protocol")) {
          // EVE quest started
          QuestDatabase.setQuestProgress(Quest.EVE, QuestDatabase.STARTED);
          Preferences.setString("EVEDirections", "LLRLR0");
        } else if (request.responseText.contains("a tiny parachute")) {
          // EVE quest finished
          QuestDatabase.setQuestProgress(Quest.EVE, QuestDatabase.FINISHED);
          Preferences.resetToDefault("EVEDirections");
        } else if (request.responseText.contains(
            "tape recorder self-destructs with a shower of sparks and a puff of smoke")) {
          // Jungle Pun quest finished (start handled in ResultProcessor)
          EquipmentManager.discardEquipment(ItemPool.MINI_CASSETTE_RECORDER);
          ResultProcessor.removeItem(ItemPool.MINI_CASSETTE_RECORDER);
          QuestDatabase.setQuestProgress(Quest.JUNGLE_PUN, QuestDatabase.UNSTARTED);
          Preferences.resetToDefault("junglePuns");
        } else if (request.responseText.contains("bucket came from")) {
          // Gore quest finished (start handled in ResultProcessor)
          EquipmentManager.discardEquipment(ItemPool.GORE_BUCKET);
          ResultProcessor.removeItem(ItemPool.GORE_BUCKET);
          QuestDatabase.setQuestProgress(Quest.GORE, QuestDatabase.UNSTARTED);
          Preferences.resetToDefault("goreCollected");
        } else if (request.responseText.contains("return the fingernails and the clippers")) {
          // Clipper quest finished (start handled in ResultProcessor)
          ResultProcessor.removeItem(ItemPool.FINGERNAIL_CLIPPERS);
          QuestDatabase.setQuestProgress(Quest.CLIPPER, QuestDatabase.UNSTARTED);
          Preferences.resetToDefault("fingernailsClipped");
        } else if (request.responseText.contains("maximal discretion")) {
          // Fake Medium quest started
          QuestDatabase.setQuestProgress(Quest.FAKE_MEDIUM, QuestDatabase.STARTED);
        } else if (request.responseText.contains("toss the device into the ocean")) {
          // Fake Medium quest finished
          ResultProcessor.removeItem(ItemPool.ESP_COLLAR);
          QuestDatabase.setQuestProgress(Quest.FAKE_MEDIUM, QuestDatabase.FINISHED);
        } else if (request.responseText.contains("wonder how many vials they want")) {
          // Serum quest started
          if (InventoryManager.getCount(ItemPool.EXPERIMENTAL_SERUM_P00) >= 5) {
            QuestDatabase.setQuestProgress(Quest.SERUM, "step1");
          } else {
            QuestDatabase.setQuestProgress(Quest.SERUM, QuestDatabase.STARTED);
          }
        } else if (request.responseText.contains("drop the vials into it")) {
          // Serum quest finished
          QuestDatabase.setQuestProgress(Quest.SERUM, QuestDatabase.FINISHED);
        } else if (request.responseText.contains("acquire cigarettes")) {
          // Smokes quest started
          QuestDatabase.setQuestProgress(Quest.SMOKES, QuestDatabase.STARTED);
        } else if (request.responseText.contains("cigarettes with a grappling gun")) {
          // Smokes quest finished
          QuestDatabase.setQuestProgress(Quest.SMOKES, QuestDatabase.FINISHED);
        } else if (request.responseText.contains("takes your nifty new watch")) {
          // Out of Order quest finished
          EquipmentManager.discardEquipment(ItemPool.GPS_WATCH);
          ResultProcessor.removeItem(ItemPool.GPS_WATCH);
          ResultProcessor.removeItem(ItemPool.PROJECT_TLB);
          QuestDatabase.setQuestProgress(Quest.OUT_OF_ORDER, QuestDatabase.FINISHED);
        } else {
          // Can't parse quest due to static so visit quest log
          RequestThread.postRequest(new QuestLogRequest());
        }
      }
    };

    new ChoiceAdventure(985, "The Odd Jobs Board", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).turnCost(3);
        new Option(2).turnCost(10);
        new Option(3).turnCost(10);
        new Option(4).turnCost(10);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("don't have time")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(986, "Control Panel", null) {
      final Pattern OMEGA_PATTERN = Pattern.compile("<br>Current power level: (\\d+)%</td>");

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
      }

      @Override
      void visitChoice(GenericRequest request) {
        Preferences.setBoolean(
            "controlPanel1", !request.responseText.contains("All-Ranchero FM station: VOLUNTARY"));
        Preferences.setBoolean(
            "controlPanel2", !request.responseText.contains("&pi; sleep-hypnosis generators: OFF"));
        Preferences.setBoolean(
            "controlPanel3",
            !request.responseText.contains("Simian Ludovico Wednesdays: CANCELLED"));
        Preferences.setBoolean(
            "controlPanel4",
            !request.responseText.contains("Monkey food safety protocols: OBEYED"));
        Preferences.setBoolean(
            "controlPanel5", !request.responseText.contains("Shampoo Dispensers: CHILD-SAFE"));
        Preferences.setBoolean(
            "controlPanel6", !request.responseText.contains("Assemble-a-Bear kiosks: CLOSED"));
        Preferences.setBoolean(
            "controlPanel7", !request.responseText.contains("Training algorithm: ROUND ROBIN"));
        Preferences.setBoolean(
            "controlPanel8", !request.responseText.contains("Re-enactment supply closet: LOCKED"));
        Preferences.setBoolean(
            "controlPanel9", !request.responseText.contains("Thermostat setting: 76 DEGREES"));
        Matcher omegaMatcher = OMEGA_PATTERN.matcher(request.responseText);
        if (omegaMatcher.find()) {
          Preferences.setInteger(
              "controlPanelOmega", StringUtilities.parseInt(omegaMatcher.group(1)));
        }
        if (request.responseText.contains("Omega device activated")) {
          Preferences.setInteger("controlPanelOmega", 0);
          QuestDatabase.setQuestProgress(Quest.EVE, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.FAKE_MEDIUM, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.SERUM, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.SMOKES, QuestDatabase.UNSTARTED);
          QuestDatabase.setQuestProgress(Quest.OUT_OF_ORDER, QuestDatabase.UNSTARTED);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision >= 1 && decision <= 9) {
          Preferences.setBoolean("_controlPanelUsed", true);
          if (!request.responseText.contains("minimum of 24 hours")) {
            Preferences.increment("controlPanelOmega", 11, 100, false);
          }
        }
      }
    };

    new RetiredChoiceAdventure(987, "The Post-Apocalyptic Survivor Encampment", null) {
      final Pattern ENCAMPMENT_PATTERN = Pattern.compile("whichfood=(\\d+)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (!request.responseText.contains("accept your donation")) {
          return;
        }
        int qty = -1;
        if (request.getURLString().contains("giveten")) {
          qty = -10;
        }
        Matcher encampmentMatcher = ENCAMPMENT_PATTERN.matcher(request.getURLString());
        if (encampmentMatcher.find()) {
          int encampmentId = StringUtilities.parseInt(encampmentMatcher.group(1));
          getOption(decision).attachItem(encampmentId, qty, MANUAL);
        }
      }
    };

    new ChoiceAdventure(988, "The Containment Unit", "The Secret Government Laboratory") {
      char next = '?';

      void setup() {
        this.option0IsManualControl = false;

        this.customName = "Containment Unit";

        new Option(1);
        new Option(2);

        new CustomOption(1, "automate");
      }

      @Override
      void visitChoice(GenericRequest request) {
        String containment = Preferences.getString("EVEDirections");
        if (containment.length() != 6) {
          next = '?';
          return;
        }
        int progress = StringUtilities.parseInt(containment.substring(5, 6));
        if (progress < 0 && progress > 5) {
          next = '?';
          return;
        }

        getOption(1).text(next == 'L' ? "right way" : next != 'R' ? "unknown" : null);
        getOption(2).text(next == 'R' ? "right way" : next != 'L' ? "unknown" : null);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (next == 'L') {
          return "1";
        }
        if (next == 'R') {
          return "2";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        String containment = Preferences.getString("EVEDirections");
        if (containment.length() != 6) {
          return;
        }
        if (request.responseText.contains("another pair of doors")) {
          int progress = StringUtilities.parseInt(containment.substring(5, 6));
          if (progress < 0 && progress > 4) {
            return;
          }
          progress++;
          Preferences.setString("EVEDirections", containment.substring(0, 5) + progress);
        } else {
          Preferences.setString("EVEDirections", containment.substring(0, 5) + "0");
        }
      }
    };

    new ChoiceAdventure(989, "Paranormal Test Lab", "The Secret Government Laboratory") {
      String answer = "0";

      void setup() {
        this.option0IsManualControl = false;

        this.customName = this.name;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);

        new CustomOption(1, "automate");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("ever-changing constellation")) {
          answer = "1";
        } else if (request.responseText.contains("card in the circle of light")) {
          answer = "2";
        } else if (request.responseText.contains("waves a fly away")) {
          answer = "3";
        } else if (request.responseText.contains("back to square one")) {
          answer = "4";
        } else if (request.responseText.contains("adds to your anxiety")) {
          answer = "5";
        } else {
          answer = "0";
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        // Highlight valid card
        String find = "espcard" + answer + ".gif";
        String replace = "espcard" + answer + ".gif style=\"border: 2px solid blue;\"";
        if (buffer.indexOf(find) >= 0) {
          StringUtilities.singleStringReplace(buffer, find, replace);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return answer;
      }
    };

    new UnknownChoiceAdventure(990);

    new RetiredChoiceAdventure(991, "Build a Crimbot!", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1)
            .leadsTo(992)
            .attachItem(ItemPool.CRIMBONIUM_FUEL_ROD, -1, MANUAL, new NoDisplay());
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(992, "Inside the Fully Automated Crimbo Factory", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(993, "Tales of Spelunking", "Item-Driven") {
      void setup() {
        new Option(1)
            .turnCost(10)
            .attachItem(ItemPool.TALES_OF_SPELUNKING, -1, MANUAL, new NoDisplay());
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (!request.responseText.contains("You need 10 Adventures")) {
            KoLCharacter.enterLimitmode(Limitmode.SPELUNKY);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new RetiredChoiceAdventure(994, "Hide a Gift!", "Item-Driven") {
      final Pattern ITEMID_PATTERN = Pattern.compile("itemid(\\d+)=(\\d+)");
      final Pattern QTY_PATTERN = Pattern.compile("qty(\\d+)=(\\d+)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You hide")) {
          HashMap<Integer, Integer> idMap = new HashMap<>(3);
          HashMap<Integer, Integer> qtyMap = new HashMap<>(3);
          int index;
          int id;
          int giftQty;

          Matcher idMatcher = ITEMID_PATTERN.matcher(request.getURLString());
          while (idMatcher.find()) {
            index = StringUtilities.parseInt(idMatcher.group(1));
            if (index < 1) continue;
            id = StringUtilities.parseInt(idMatcher.group(2));
            if (id < 1) continue;
            idMap.put(index, id);
          }

          Matcher qtyMatcher = QTY_PATTERN.matcher(request.getURLString());
          while (qtyMatcher.find()) {
            index = StringUtilities.parseInt(qtyMatcher.group(1));
            if (index < 1) continue;
            giftQty = StringUtilities.parseInt(qtyMatcher.group(2));
            if (giftQty < 1) continue;
            qtyMap.put(index, giftQty);
          }

          for (int i = 1; i <= 3; i++) {
            Integer itemId = idMap.get(i);
            Integer giftQuantity = qtyMap.get(i);
            if (itemId == null || giftQuantity == null) continue;
            getOption(decision).attachItem(itemId, -giftQuantity, MANUAL);
          }
          getOption(decision).attachItem(ItemPool.SNEAKY_WRAPPING_PAPER, -1, MANUAL);
        }
      }
    };

    new ChoiceAdventure(995, "Being Picky", null) {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(996, "The Elf Resistance Camp", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(997, "Prize votin'", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(998, "Game of Cards", "The Rowdy Saloon") {
      void setup() {
        this.customName = this.name;

        new Option(1, "Gain 7 Chroner", true).turnCost(1).attachItem(ItemPool.CHRONER, 7, AUTO);
        new Option(2, "Gain 9 Chroner", true).turnCost(1).attachItem(ItemPool.CHRONER, 9, AUTO);
        new Option(3, "Gain 13 Chroner (80% chance)", true)
            .turnCost(1)
            .attachItem(ItemPool.CHRONER, 13, AUTO);
        new Option(4, "Gain 17 Chroner (60% chance)", true)
            .turnCost(1)
            .attachItem(ItemPool.CHRONER, 17, AUTO);
        new Option(5, "Gain 21 Chroner, lose pocket ace", true)
            .turnCost(1)
            .attachItem(ItemPool.CHRONER, 21, AUTO)
            .attachItem(ItemPool.POCKET_ACE, -1, MANUAL);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("confiscate your deuces")) {
          int removeDeuces = decision - 1;
          getOption(decision).attachItem(ItemPool.SLEEVE_DEUCE, 0 - removeDeuces, MANUAL);
        }
      }
    };

    new ChoiceAdventure(999, "Shrubberatin'", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("_shrubDecorated", true);
          Pattern topperPattern = Pattern.compile("topper=(\\d)");
          Pattern lightsPattern = Pattern.compile("lights=(\\d)");
          Pattern garlandPattern = Pattern.compile("garland=(\\d)");
          Pattern giftPattern = Pattern.compile("gift=(\\d)");
          int decoration;

          Matcher matcher = topperPattern.matcher(request.getURLString());
          if (matcher.find()) {
            decoration = StringUtilities.parseInt(matcher.group(1));
            switch (decoration) {
              case 1:
                Preferences.setString("shrubTopper", "Muscle");
                break;
              case 2:
                Preferences.setString("shrubTopper", "Mysticality");
                break;
              case 3:
                Preferences.setString("shrubTopper", "Moxie");
                break;
            }
          }

          matcher = lightsPattern.matcher(request.getURLString());
          if (matcher.find()) {
            decoration = StringUtilities.parseInt(matcher.group(1));
            switch (decoration) {
              case 1:
                Preferences.setString("shrubLights", "prismatic");
                break;
              case 2:
                Preferences.setString("shrubLights", "Hot");
                break;
              case 3:
                Preferences.setString("shrubLights", "Cold");
                break;
              case 4:
                Preferences.setString("shrubLights", "Stench");
                break;
              case 5:
                Preferences.setString("shrubLights", "Spooky");
                break;
              case 6:
                Preferences.setString("shrubLights", "Sleaze");
                break;
            }
          }

          matcher = garlandPattern.matcher(request.getURLString());
          if (matcher.find()) {
            decoration = StringUtilities.parseInt(matcher.group(1));
            switch (decoration) {
              case 1:
                Preferences.setString("shrubGarland", "HP");
                break;
              case 2:
                Preferences.setString("shrubGarland", "PvP");
                break;
              case 3:
                Preferences.setString("shrubGarland", "blocking");
                break;
            }
          }

          matcher = giftPattern.matcher(request.getURLString());
          if (matcher.find()) {
            decoration = StringUtilities.parseInt(matcher.group(1));
            switch (decoration) {
              case 1:
                Preferences.setString("shrubGifts", "yellow");
                break;
              case 2:
                Preferences.setString("shrubGifts", "meat");
                break;
              case 3:
                Preferences.setString("shrubGifts", "gifts");
                break;
            }
          }
        }
      }
    };
  }
}
