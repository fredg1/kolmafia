package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.AscensionClass;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ConcoctionDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.HobopolisManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.textui.command.GongCommand;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase200to299 extends ChoiceAdventureDatabase {
  final void add200to299() {
    new ChoiceAdventure(200, "Enter The Hoboverlord", "Hobopolis Town Square") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Hodgman", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Hodgman");
      }
    };

    new ChoiceAdventure(201, "Home, Home in the Range", "Burnbarrel Blvd.") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Ol' Scratch", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Ol' Scratch");
      }
    };

    new ChoiceAdventure(202, "Bumpity Bump Bump", "Exposure Esplanade") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Frosty", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Frosty");
      }
    };

    new ChoiceAdventure(203, "Deep Enough to Dive", "The Heap") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Oscus", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Oscus");
      }
    };

    new ChoiceAdventure(204, "Welcome To You!", "The Ancient Hobo Burial Ground") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Zombo", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Zombo");
      }
    };

    new ChoiceAdventure(205, "Van, Damn", "The Purple Light District") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Chester", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Chester");
      }
    };

    new ChoiceAdventure(206, "Getting Tired", "Burnbarrel Blvd.") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "start tirevalanche", true).turnCost(1);
        new Option(2, "add tire to stack", true).turnCost(1);
        new Option(3, "skip adventure", true);
      }
    };

    new ChoiceAdventure(207, "Hot Dog! I Mean... Door!", "Burnbarrel Blvd.") {
      void setup() {
        new Option(1, "add hot hobos & get clan meat", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(
        208, "Ah, So That's Where They've All Gone", "The Ancient Hobo Burial Ground") {
      void setup() {
        new Option(1, "increase spooky & decrease stench", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(209, "Timbarrrr!", "The Arrrboretum") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1)
            .turnCost(1)
            .attachItem("bag of Crotchety Pine saplings", 1, AUTO, new NoDisplay())
            .attachItem("handful of Crotchety Pine needles")
            .attachItem("crotchety pants");
        new Option(2)
            .turnCost(1)
            .attachItem("bag of Saccharine Maple saplings", 1, AUTO, new NoDisplay())
            .attachItem("lump of Saccharine Maple sap")
            .attachItem("Saccharine Maple pendant");
        new Option(3)
            .turnCost(1)
            .attachItem("bag of Laughing Willow saplings", 1, AUTO, new NoDisplay())
            .attachItem("handful of Laughing Willow bark")
            .attachItem("willowy bonnet");
      }
    };

    new ChoiceAdventure(210, "Stumped", "The Arrrboretum") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(211, "Despite All Your Rage", "A Maze of Sewer Tunnels") { // initial trap
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(10);
        new Option(2);

        new CustomOption(1, "gnaw through the bars");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You gnaw through the bars on the cage")) {
          // freed by someone/something else
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(
        212, "Despite All Your Rage", "A Maze of Sewer Tunnels") { // waiting for rescue -- nothing
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(10);
        new Option(2);

        new CustomOption(1, "gnaw through the bars");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You gnaw through the bars on the cage")) {
          // freed by someone/something else
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(213, "Piping Hot", "Burnbarrel Blvd.") {
      void setup() {
        new Option(1, "decrease heat & increase sleaze popularity", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(214, "You vs. The Volcano", "The Heap") {
      void setup() {
        new Option(1, "kill stench hobos & increase stench", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(215, "Piping Cold", "Exposure Esplanade") {
      void setup() {
        new Option(1, "decrease heat", true).turnCost(1);
        new Option(2, "decrease sleaze popularity", true).turnCost(1);
        new Option(3, "increase number of icicles & increase heat, or skip adventure", true)
            .turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3 && request.responseText.contains("Stupid boring empty pipes")) {
          this.choiceFailed();
          getOption(3).entersQueue(false);
        }
      }
    };

    new ChoiceAdventure(216, "The Compostal Service", "The Heap") {
      void setup() {
        new Option(1, "decrease stench & spooky", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(217, "There Goes Fritz!", "Exposure Esplanade") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "use a few icicles to kill cold hobos", true).turnCost(1);
        new Option(2, "use icicles to kill cold hobos, may vow to stop yodeling", true).turnCost(1);
        new Option(3, "use a LOT of icicles to kill cold hobos & vow to stop yodeling", true)
            .turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You vow to stop yodeling forever")) {
          String message = "You vowed to stop yodeling";
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
        }
      }
    };

    new ChoiceAdventure(218, "I Refuse!", "The Heap") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "explore the junkpile & reset stench", true).turnCost(1);
        new Option(2, "skip adventure & reset stench", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(219, "The Furtivity of My City", "The Purple Light District") {
      void setup() {
        new Option(1, "fight sleaze hobo", true);
        new Option(2, "increase stench", true).turnCost(1);
        new Option(3, "add sleaze hobos & get clan meat", true).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3 && request.responseText.contains("already been pretty thorougly dived")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(220, "Returning to the Tomb", "The Ancient Hobo Burial Ground") {
      void setup() {
        new Option(1, "add spooky hobos, increase zone ML & get clan meat", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // You pry open the door of the tomb, and find it empty.
        // It turns out the tomb had been defiled, it had just
        // been defiled by some very tidy, very courteous grave-robbers.
        if (decision == 1 && request.responseText.contains("very courteous grave-robbers")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(221, "A Chiller Night", "The Ancient Hobo Burial Ground") {
      void setup() {
        this.customName = this.name;

        new Option(1, "study the dance moves", true).turnCost(1);
        new Option(2, "dance (poorly) with hobo zombies", true).turnCost(1);
        new Option(3, "skip adventure (still consumes this instance)", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(
        222, "A Chiller Night", "The Ancient Hobo Burial Ground") { // after watching x3
      void setup() {
        this.customName = this.name;

        new Option(1, "kill spooky hobos", true).turnCost(1);
        new Option(2, "skip adventure (still consumes this instance)", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(223, "Getting Clubbed", "The Purple Light District") {
      void setup() {
        new Option(1, "get inside (if popularity is low enough)", true).leadsTo(224);
        new Option(2, "decrease sleaze popularity", true).turnCost(1);
        new Option(
                3, "decrease sleaze popularity & add 5 \"A Chiller Night\" instances in AHBG", true)
            .turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("you just can't get through")) {
          this.choiceFailed().entersQueue(false);
        }
      }
    };

    new ChoiceAdventure(224, "Exclusive!", "The Purple Light District") {
      void setup() {
        new Option(1, "fight sleaze hobo", true);
        new Option(2, "kill 10% of remaining sleaze hobos", true).turnCost(1);
        new Option(3, "get lots of stats & increase sleaze popularity", true).turnCost(1);
      }
    };

    new ChoiceAdventure(225, "Attention -- A Tent!", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "perform on stage", true).leadsTo(226);
        new Option(2, "join the crowd", true).leadsTo(227);
        new Option(3, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        AdventureResult instrument =
            HobopolisManager.getClassInstrument(KoLCharacter.getAscensionClass());
        Option option = getOption(1);

        if (instrument == null) {
          option.text("Get rejected");
        } else {
          option.attachItem(
              instrument.getInstance(0), AUTO, new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1));
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("Hey, you can't come in here")) {
          this.choiceFailed().turnCost(1).entersQueue(true);
        } else if (decision == 2) {
          HobopolisManager.resetPerformers();
        }
      }
    };

    new ChoiceAdventure(226, "Here You Are, Up On Stage", "Hobopolis Town Square") {
      void setup() {
        new Option(1);
        new Option(2).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("You shrug and leave the stage")) {
          getOption(1).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(227, "Working the Crowd", "Hobopolis Town Square") {
      final String[] moshResults =
          new String[] {
            "waste an adventure",
            "kill a handful of hobos",
            "kill a few hobos",
            "kill ~24",
            "kill ~40 hobos",
            "kill ~64 hobos",
            "kill ~100 hobos. Do it! Do it! Do it!!"
          };
      final String[] buskResults =
          new String[] {
            "waste an adventure",
            "get ~4 nickels",
            "get ~6 nickels",
            "get ~8 nickels",
            "get ~11 nickels",
            "get ~15 nickels",
            "get ~20 nickels"
          };

      void setup() {
        new Option(1, "click me").entersQueue(false);
        new Option(2, "survey the crowd, first").turnCost(1);
        new Option(3, "survey the crowd, first").turnCost(1);
        new Option(4, "skip adventure").entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          return;
        }

        LinkedList<AscensionClass> missingPerformers = HobopolisManager.getMissingPerformers();
        int performerCount = HobopolisManager.getPerformerCount();

        if (missingPerformers == null) {
          // user hasn't surveyed since they went in.
          return;
        }

        // currently nobody on stage. Missing seal clubber, turtle tamer, pastamancer, sauceror,
        // disco bandit, accordion thief.
        StringBuffer surveyText = new StringBuffer(125);

        surveyText.append("Currently ");
        if (performerCount == 0) {
          surveyText.append("nobody");
        } else {
          surveyText.append(String.valueOf(performerCount) + "/6");
        }
        surveyText.append(" on stage. ");

        if (performerCount == 6) {
          surveyText.append("Go get'em!");
        } else {
          List<String> classNames =
              missingPerformers.stream()
                  .map(performer -> performer.toString())
                  .collect(Collectors.toList());
          surveyText.append("Missing " + String.join(", ", classNames));

          for (AscensionClass KoLClass : missingPerformers) {
            getOption(1)
                .attachItem(
                    HobopolisManager.getClassInstrument(KoLClass).getInstance(0),
                    AUTO,
                    new ImageOnly(KoLClass.toString()));
          }
        }

        getOption(1).text(surveyText.toString());
        getOption(2).text(moshResults[performerCount]);
        getOption(3).text(buskResults[performerCount]);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          HobopolisManager.parseTentSurvey(request.responseText);
        }
      }
    };

    new UnknownChoiceAdventure(228);

    new UnknownChoiceAdventure(229);

    new ChoiceAdventure(230, "Mind Yer Binder", "Hobopolis Town Square") {
      void setup() {
        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -30, MANUAL, new ImageOnly())
            .attachItem(ItemPool.HOBO_CODE_BINDER, 1, AUTO, new ImageOnly());
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You don't have enough nickels to buy that")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(231, "The Hobo Marketplace", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1, "auto-used consumables").leadsTo(233);
        new Option(2, "buy hobo items").leadsTo(245);
        new Option(3, "buffs/tattoo").leadsTo(262);
      }
    };

    new ChoiceAdventure(232, "The Hobo Marketplace", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(233, "Food Went A-Courtin'", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1).leadsTo(235);
        new Option(2).leadsTo(240);
        new Option(3, "fight gang of hobo muggers");
      }
    };

    new ChoiceAdventure(234, "Food Went A-Courtin'", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(235, "Food, Glorious Food", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1, "muscle food").leadsTo(237, true);
        new Option(2, "mysticality food").leadsTo(238, true);
        new Option(3, "moxie food").leadsTo(239, true);
      }
    };

    new ChoiceAdventure(236, "Food, Glorious Food", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(237, "Big Merv's Protein Shakes", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "+5 fullness, +60-80 advs, +200-400 muscle stats")
            .turnCost(1)
            .attachAdv(60)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (KoLCharacter.getFullness() + 5 > KoLCharacter.getFullnessLimit()) {
          getOption(1).text("You're too full for that");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("into a Quickie-Mart slurpee cup")) {
            // You gain 5 fullness
            KoLCharacter.setFullness(KoLCharacter.getFullness() + 5);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(238, "Suddenly Salad!", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "+5 fullness, +60-80 advs, +200-400 myst stats")
            .turnCost(1)
            .attachAdv(60)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (KoLCharacter.getFullness() + 5 > KoLCharacter.getFullnessLimit()) {
          getOption(1).text("You're too full for that");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("The salad is actually pretty decent")) {
            // You gain 5 fullness
            KoLCharacter.setFullness(KoLCharacter.getFullness() + 5);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(239, "Sizzling Weasel On a Stick", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "+5 fullness, +60-80 advs, +200-400 moxie stats")
            .turnCost(1)
            .attachAdv(60)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (KoLCharacter.getFullness() + 5 > KoLCharacter.getFullnessLimit()) {
          getOption(1).text("You're too full for that");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("slathers the weasel with some kind of brownish")) {
            // You gain 5 fullness
            KoLCharacter.setFullness(KoLCharacter.getFullness() + 5);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(240, "Booze, Glorious Booze", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1, "muscle booze").leadsTo(242);
        new Option(2, "mysticality booze").leadsTo(243);
        new Option(3, "moxie booze").leadsTo(244);
      }
    };

    new ChoiceAdventure(241, "Booze, Glorious Booze", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(242, "Arthur Finn's World-Record Homebrew Stout", "Hobopolis Town Square") {
      void setup() {
        // Available even overdrunk!
        new Option(1, "+5 drunk, +40-60 advs, +500-1000 muscle stats")
            .turnCost(1)
            .attachAdv(40)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("like drinking a loaf of bread")) {
            // You gain 5 drunkenness.  This will be set
            // when we refresh the charpane.
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(243, "Mad Jack's Corn Squeezery", "Hobopolis Town Square") {
      void setup() {
        // Available even overdrunk!
        new Option(1, "+5 drunk, +40-60 advs, +500-1000 myst stats")
            .turnCost(1)
            .attachAdv(40)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("beats the hell out of your brains")) {
            // You gain 5 drunkenness.  This will be set
            // when we refresh the charpane.
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(244, "Bathtub Jimmy's Gin Mill", "Hobopolis Town Square") {
      void setup() {
        // Available even overdrunk!
        new Option(1, "+5 drunk, +40-60 advs, +500-1000 moxie stats")
            .turnCost(1)
            .attachAdv(40)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("flushes out your entire nervous system")) {
            // You gain 5 drunkenness.  This will be set
            // when we refresh the charpane.
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(245, "Math Is Hard", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1, "hats/pants/accessories").leadsTo(248);
        new Option(2, "combat items/instruments/hobo monkey/free stats").leadsTo(253);
        new Option(3).leadsTo(247, false, (Option o) -> o.index == 1);
      }
    };

    new ChoiceAdventure(246, "Math Is Hard", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(247, "The Guy Who Carves Driftwood Animals", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10))
            .attachItem(ItemPool.VALUABLE_TRINKET, 3, AUTO);
        new Option(2).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You don't have enough nickels")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(248, "Garment District", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1).leadsTo(250, false, (Option o) -> o.index != 4);
        new Option(2).leadsTo(251, false, (Option o) -> o.index != 4);
        new Option(3).leadsTo(252, false, (Option o) -> o.index != 4);
      }
    };

    new ChoiceAdventure(249, "Garment District", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(250, "A Hattery", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -250, MANUAL, new DisplayAll(NEED, AT_LEAST, 250))
            .attachItem(ItemPool.CRUMPLED_FELT_FEDORA, 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -150, MANUAL, new DisplayAll(NEED, AT_LEAST, 150))
            .attachItem(ItemPool.BATTERED_OLD_TOPHAT, 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -200, MANUAL, new DisplayAll(NEED, AT_LEAST, 200))
            .attachItem(ItemPool.SHAPELESS_WIDE_BRIMMED_HAT, 1, AUTO);
        new Option(4).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford that hat")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(251, "A Pantry", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -200, MANUAL, new DisplayAll(NEED, AT_LEAST, 200))
            .attachItem(ItemPool.MOSTLY_RAT_HIDE_LEGGINGS, 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -150, MANUAL, new DisplayAll(NEED, AT_LEAST, 150))
            .attachItem(ItemPool.HOBO_DUNGAREES, 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -250, MANUAL, new DisplayAll(NEED, AT_LEAST, 250))
            .attachItem(ItemPool.OLD_PATCHED_SUIT_PANTS, 1, AUTO);
        new Option(4).entersQueue(false);
      }
    };

    new ChoiceAdventure(252, "Hobo Blanket Bingo", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -250, MANUAL, new DisplayAll(NEED, AT_LEAST, 250))
            .attachItem(ItemPool.OLD_SOFT_SHOES, 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -200, MANUAL, new DisplayAll(NEED, AT_LEAST, 200))
            .attachItem(ItemPool.HOBO_STOGIE, 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -150, MANUAL, new DisplayAll(NEED, AT_LEAST, 150))
            .attachItem(ItemPool.ROPE_WITH_SOAP, 1, AUTO);
        new Option(4).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford that particular piece of")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(253, "Housewares", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1).leadsTo(255, false, (Option o) -> o.index != 4);
        new Option(2, "fight gang of hobo muggers");
        new Option(3, "instruments/hobo monkey/free stats").leadsTo(256, true);
      }
    };

    new ChoiceAdventure(254, "Housewares", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(255, "Black-and-Blue-and-Decker", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10))
            .attachItem("sharpened hubcap", 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10))
            .attachItem("very large caltrop", 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10))
            .attachItem("The Six-Pack of Pain", 1, AUTO);
        new Option(4).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford any self-defense")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(256, "Entertainment", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1);
        new Option(2).leadsTo(259, true, (Option o) -> o.index != 3);
        new Option(3, "fight gang of hobo muggers");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(1);
        AdventureResult classInstrument =
            HobopolisManager.getClassInstrument(KoLCharacter.getAscensionClass());
        if (classInstrument.getCount(KoLConstants.inventory)
                + classInstrument.getCount(KoLConstants.closet)
                + classInstrument.getCount(KoLConstants.storage)
                + classInstrument.getCount(KoLConstants.collection)
                + InventoryManager.getEquippedCount(classInstrument)
            > 0) {
          option.leadsTo(275, false, (Option o) -> o.index == 1);
        } else {
          option.leadsTo(258, true, (Option o) -> o.index == 1);
        }
      }
    };

    new ChoiceAdventure(257, "Entertainment", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(258, "Instru-mental", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -99, MANUAL, new DisplayAll(NEED, AT_LEAST, 99));
        new Option(2).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        AdventureResult instrument =
            HobopolisManager.getClassInstrument(KoLCharacter.getAscensionClass());

        if (instrument != null) {
          getOption(1).attachItem(instrument.getInstance(1), AUTO, new ImageOnly());
        }
      }
    };

    new ChoiceAdventure(259, "We'll Make Great...", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1).leadsTo(261, false, (Option o) -> o.index == 1);
        new Option(2).turnCost(1);
        new Option(3, "fight gang of hobo muggers");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        getOption(2)
            .text("gain " + Math.min(KoLCharacter.getBaseMainstat(), 200) + " of each substats");
      }
    };

    new ChoiceAdventure(260, "We'll Make Great...", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(261, "Everybody's Got Something To Hide", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -1000, MANUAL, new DisplayAll(NEED, AT_LEAST, 1000))
            .attachItem("hobo monkey", 1, AUTO);
        new Option(2).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford a monkey")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(262, "Salud", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1, "20 adv of Trepandation").turnCost(1).attachEffect("Trepandation");
        new Option(2).leadsTo(264, false, (Option o) -> o.index != 3, " or ");
        new Option(3).leadsTo(265, false, (Option o) -> true, "<br>");
      }
    };

    new ChoiceAdventure(263, "Salud", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(264, "Tanning Salon", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "20 adv of Healthy Bronze Glow")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("Healthy Bronze Glow");
        new Option(2, "20 adv of Chalky White Pallor")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("Chalky White Pallor");
        new Option(3).entersQueue(false);
      }
    };

    new ChoiceAdventure(265, "Another Part of the Market", "Hobopolis Town Square") { // decoded
      void setup() {
        new Option(1).leadsTo(267, false, (Option o) -> o.index != 3, " or ");
        new Option(2).leadsTo(268, false, (Option o) -> o.index != 3, " or ");
        new Option(3).leadsTo(269, false, (Option o) -> o.index != 2, " or ");
      }
    };

    new ChoiceAdventure(266, "Another Part of the Market", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(267, "Let's All Go To The Movies", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "20 adv of Educated (Kinda)")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("Educated (Kinda)");
        new Option(2, "20 adv of Educated (Sorta)")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("Educated (Sorta)");
        new Option(3).entersQueue(false);
      }
    };

    new ChoiceAdventure(268, "It's Fun To Stay There", "Hobopolis Town Square") {
      void setup() {
        new Option(1, "20 adv of The Sweats")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("The Sweats");
        new Option(2, "20 adv of It Didn't Kill You")
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new DisplayAll(NEED, AT_LEAST, 5))
            .attachEffect("It Didn't Kill You");
        new Option(3).entersQueue(false);
      }
    };

    new ChoiceAdventure(269, "Body Modifications", "Hobopolis Town Square") { // decoded
      void setup() {
        // leads to 271 or 274, but we
        // don't track the tattoo's progression
        new Option(1, "upgrade your hobo tattoo for 20 nickels * (its progress + 1)")
            .entersQueue(false)
            .attachItem(ItemPool.HOBO_NICKEL);
        new Option(2, "fight gang of hobo muggers");
        new Option(3, "20 adv of Hooked Up and restore all MP").attachEffect("Hooked Up");
      }
    };

    new ChoiceAdventure(270, "Body Modifications", "Hobopolis Town Square") { // coded
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(271, "Tattoo Shop", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -20, MANUAL, new DisplayAll(NEED, AT_LEAST, 20));
        new Option(2, "skip adventure").entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // "Great," you say. "With my luck it's probably a take-out menu."
        if (!request.responseText.contains("probably a take-out menu")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(272, "Marketplace Entrance", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .attachItem(ItemPool.HOBO_NICKEL)
            .attachItem(ItemPool.HOBO_CODE_BINDER, new DisplayAll(WANT, EQUIPPED_AT_LEAST, 1));
        new Option(2, "skip adventure", true).entersQueue(false);

        new CustomOption(1, "enter marketplace");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);
        if (KoLCharacter.hasEquipped(ItemPool.HOBO_CODE_BINDER)) { // works even in left-hand man
          option.leadsTo(231);
        } else {
          option.leadsTo(232);
        }
      }
    };

    new ChoiceAdventure(273, "The Frigid Air", "Exposure Esplanade") {
      void setup() {
        new Option(1, "frozen banquet", true).turnCost(1).attachItem("frozen banquet", 1, AUTO);
        new Option(2, "get clan meat", true).turnCost(1);
        new Option(3, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(274, "Tattoo Redux", "Hobopolis Town Square") {
      final Pattern TATTOO_PATTERN = Pattern.compile("otherimages/sigils/hobotat(\\d+).gif");

      void setup() {
        new Option(1).turnCost(1).attachItem(ItemPool.HOBO_NICKEL);
        new Option(2, "skip adventure").entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          Matcher matcher = TATTOO_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            int tattoo = StringUtilities.parseInt(matcher.group(1));
            int cost = 20 * tattoo;
            getOption(decision)
                .attachItem(
                    ItemPool.HOBO_NICKEL, -cost, MANUAL, new DisplayAll(NEED, AT_LEAST, cost));
          }
        }
      }
    };

    new ChoiceAdventure(275, "Triangle, Man", "Hobopolis Town Square") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -10, MANUAL, new DisplayAll(NEED, AT_LEAST, 10))
            .attachItem("dinged-up triangle", 1, AUTO);
        new Option(2).entersQueue(false);
      }
    };

    new ChoiceAdventure(276, "The Gong Has Been Bung", "Item-Driven") {
      void setup() {
        this.customName = "Llama Gong";

        new Option(1, "Choice adventure chain. Takes 3 turns")
            .attachItem(ItemPool.GONG, -1, MANUAL, new NoDisplay())
            .attachEffect(EffectPool.FORM_OF_ROACH)
            .leadsTo(278, true);
        new Option(2, "12 forced adventures at Mt. Molehill")
            .attachItem(ItemPool.GONG, -1, MANUAL, new NoDisplay())
            .attachEffect(EffectPool.SHAPE_OF_MOLE);
        new Option(3, "turn into a bird for 15 adventures")
            .attachItem(ItemPool.GONG, -1, MANUAL, new NoDisplay())
            .attachEffect(EffectPool.FORM_OF_BIRD);

        for (int i = 0; i < GongCommand.GONG_PATHS.length; ++i) {
          int index = i; // needed, otherwise the lambda function will cry
          new CustomOption(
              index,
              GongCommand.GONG_PATHS[index],
              () -> {
                Preferences.setInteger("gongPath", index);
                GongCommand.setPath(index);
              });
        }

        this.customLoad =
            () -> {
              int index = Preferences.getInteger("gongPath");
              if (index < 0 || index > GongCommand.GONG_PATHS.length) {
                System.out.println("Invalid setting " + index + " for gongPath");
                return 0;
              }
              return index;
            };

        this.customPreferencesToListen.add("gongPath");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setInteger("moleTunnelLevel", 0);
        Preferences.setInteger("birdformCold", 0);
        Preferences.setInteger("birdformHot", 0);
        Preferences.setInteger("birdformRoc", 0);
        Preferences.setInteger("birdformSleaze", 0);
        Preferences.setInteger("birdformSpooky", 0);
        Preferences.setInteger("birdformStench", 0);
      }
    };

    new ChoiceAdventure(277, "Welcome Back!", "Item-Driven") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "finish journey");
        new Option(2, "also finish journey");
      }
    };

    new ChoiceAdventure(278, "Enter the Roach", "Item-Driven") {
      void setup() {
        new Option(1).turnCost(1).leadsTo(279, true);
        new Option(2).turnCost(1).leadsTo(280, true);
        new Option(3).turnCost(1).leadsTo(281, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int statGain = Math.min(KoLCharacter.getBaseMainstat(), 200);

        getOption(1).text("gain " + statGain + " muscle substats");
        getOption(2).text("gain " + statGain + " mysticality substats");
        getOption(3).text("gain " + statGain + " moxie substats");
      }
    };

    new ChoiceAdventure(279, "It's Nukyuhlur - the 'S' is Silent.", "Item-Driven") {
      void setup() {
        new Option(1).turnCost(1).leadsTo(282);
        new Option(2).turnCost(1).leadsTo(283);
        new Option(3, "gain 175 MP").turnCost(1).leadsTo(284);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int statGain = Math.min(KoLCharacter.getBaseMainstat(), 200);

        getOption(1).text("gain " + statGain + " moxie substats");
        getOption(2).text("gain " + statGain + " muscle substats");
      }
    };

    new ChoiceAdventure(280, "Eek! Eek!", "Item-Driven") {
      void setup() {
        new Option(1).turnCost(1).leadsTo(285);
        new Option(2).turnCost(1).leadsTo(286);
        new Option(3, "gain 175 MP").turnCost(1).leadsTo(287);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int statGain = Math.min(KoLCharacter.getBaseMainstat(), 200);

        getOption(1).text("gain " + statGain + " mysticality substats");
        getOption(2).text("gain " + statGain + " muscle substats");
      }
    };

    new ChoiceAdventure(281, "A Meta-Metamorphosis", "Item-Driven") {
      void setup() {
        new Option(1).turnCost(1).leadsTo(288);
        new Option(2).turnCost(1).leadsTo(289);
        new Option(3, "gain 175 MP").turnCost(1).leadsTo(290);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int statGain = Math.min(KoLCharacter.getBaseMainstat(), 200);

        getOption(1).text("gain " + statGain + " moxie substats");
        getOption(2).text("gain " + statGain + " mysticality substats");
      }
    };

    new ChoiceAdventure(282, "You've Got Wings, But No Wingman", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Ack! Barred!")
            .turnCost(1)
            .attachEffect(EffectPool.ACK_BARRED)
            .leadsTo(277);
        new Option(2, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
        new Option(3, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(283, "Time Enough at Last!", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Ack! Barred!")
            .turnCost(1)
            .attachEffect(EffectPool.ACK_BARRED)
            .leadsTo(277);
        new Option(2, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
        new Option(3, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(284, "Scavenger Is Your Middle Name", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Ack! Barred!")
            .turnCost(1)
            .attachEffect(EffectPool.ACK_BARRED)
            .leadsTo(277);
        new Option(2, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
        new Option(3, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(285, "Bugging Out", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Alchemical, Brother")
            .turnCost(1)
            .attachEffect(EffectPool.ALCHEMICAL_BROTHER)
            .leadsTo(277);
        new Option(2, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
        new Option(3, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(286, "A Sweeping Generalization", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
        new Option(2, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
        new Option(3, "20 adv of Alchemical, Brother")
            .turnCost(1)
            .attachEffect(EffectPool.ALCHEMICAL_BROTHER)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(287, "In the Frigid Aire", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
        new Option(2, "20 adv of Alchemical, Brother")
            .turnCost(1)
            .attachEffect(EffectPool.ALCHEMICAL_BROTHER)
            .leadsTo(277);
        new Option(3, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(288, "Our House", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
        new Option(2, "20 adv of Radiant Personality")
            .turnCost(1)
            .attachEffect(EffectPool.RADIANT_PERSONALITY)
            .leadsTo(277);
        new Option(3, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(289, "Workin' For The Man", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Unpopular")
            .turnCost(1)
            .attachEffect(EffectPool.UNPOPULAR)
            .leadsTo(277);
        new Option(2, "20 adv of Radiant Personality")
            .turnCost(1)
            .attachEffect(EffectPool.RADIANT_PERSONALITY)
            .leadsTo(277);
        new Option(3, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(290, "The World's Not Fair", "Item-Driven") {
      void setup() {
        new Option(1, "20 adv of Radiant Personality")
            .turnCost(1)
            .attachEffect(EffectPool.RADIANT_PERSONALITY)
            .leadsTo(277);
        new Option(2, "20 adv of New, Improved")
            .turnCost(1)
            .attachEffect(EffectPool.NEW_IMPROVED)
            .leadsTo(277);
        new Option(3, "20 adv of Extra Sensory Perception")
            .turnCost(1)
            .attachEffect(EffectPool.EXTRA_SENSORY_PERCEPTION)
            .leadsTo(277);
      }
    };

    new ChoiceAdventure(291, "A Tight Squeeze", "Burnbarrel Blvd.") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new ImageOnly())
            .attachItem(ItemPool.SQUEEZE, 1, AUTO);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford any squeeze")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(292, "Cold Comfort", "Exposure Esplanade") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new ImageOnly())
            .attachItem(ItemPool.FISHYSOISSE, 1, AUTO);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford a bowl of soup")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(293, "Flowers for You", "The Ancient Hobo Burial Ground") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new ImageOnly())
            .attachItem(ItemPool.LAMP_SHADE, 1, AUTO);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford to help the little girl")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(294, "Maybe It's a Sexy Snake!", "The Purple Light District") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new ImageOnly())
            .attachItem(ItemPool.LEWD_CARD, 1, AUTO);
        new Option(2)
            // apparently still costs an adventure??
            .turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford to see anything sexy")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(295, "Juicy!", "The Heap") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem(ItemPool.HOBO_NICKEL, -5, MANUAL, new ImageOnly())
            .attachItem(ItemPool.GARBAGE_JUICE, 1, AUTO);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You can't afford any garbage juice")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(296, "Pop!", "A Maze of Sewer Tunnels") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(297, "Gravy Fairy Ring", "The Haiku Dungeon") {
      void setup() {
        new Option(1, "2-3 mushrooms", true)
            .turnCost(1)
            .attachItem("Knob mushroom", 1, AUTO)
            .attachItem("Knoll mushroom", 1, AUTO)
            .attachItem("spooky mushroom", 1, AUTO);
        new Option(2, null, true).turnCost(1).attachItem("fairy gravy boat", 1, AUTO);
        new Option(3, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(298, "In the Shade", "An Octopus's Garden") {
      void setup() {
        new Option(1, "get 2-3 sea vegetables", true)
            .turnCost(1)
            .attachItem(ItemPool.SEED_PACKET, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem(ItemPool.GREEN_SLIME, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1))
            .attachItem(ItemPool.SEA_AVOCADO, 1, AUTO, new ImageOnly())
            .attachItem(ItemPool.SEA_CARROT, 1, AUTO, new ImageOnly())
            .attachItem(ItemPool.SEA_CUCUMBER, 1, AUTO, new ImageOnly())
            .attachItem("sea honeydew", 1, AUTO, new ImageOnly())
            .attachItem("sea lychee", 1, AUTO, new ImageOnly())
            .attachItem("sea tangelo", 1, AUTO, new ImageOnly());
        new Option(2, "skip adventure", true).entersQueue(false);
        new Option(3).turnCost(1).attachItem("sea truffle", 1, AUTO);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("1")) {
          int seeds = InventoryManager.getCount(ItemPool.SEED_PACKET);
          int slime = InventoryManager.getCount(ItemPool.GREEN_SLIME);
          if (seeds < 1 || slime < 1) {
            return "2";
          }
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // You carefully plant the packet of seeds, sprinkle it
        // with gooey green algae, wait a few days, and then
        // you reap what you sow. Sowed. Sew?

        if (decision == 1 && !request.responseText.contains("you reap what you sow")) {
          this.choiceFailed().turnCost(1);
        }
      }
    };

    new ChoiceAdventure(299, "Down at the Hatch", "The Wreck of the Edgar Fitzsimmons") {
      void setup() {
        new Option(1, "release creatures", true).turnCost(1);
        new Option(2, "skip adventure and banish for 20 adventures", true).entersQueue(false);
        new Option(3, "unlock tarnished luggage key adventure");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          // The first time you take option 1, you
          // release Big Brother. Subsequent times, you
          // release other creatures.
          Preferences.setBoolean("bigBrotherRescued", true);
          QuestDatabase.setQuestProgress(Quest.SEA_MONKEES, "step2");
          ConcoctionDatabase.setRefreshNeeded(false);
        }
      }
    };
  }
}
