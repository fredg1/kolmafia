package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.GenieRequest;
import net.sourceforge.kolmafia.request.MummeryRequest;
import net.sourceforge.kolmafia.request.PantogramRequest;
import net.sourceforge.kolmafia.request.SweetSynthesisRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.LTAManager;
import net.sourceforge.kolmafia.session.QuestManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.SpacegateManager;
import net.sourceforge.kolmafia.utilities.StringUtilities;
import net.sourceforge.kolmafia.webui.ClanFortuneDecorator;
import net.sourceforge.kolmafia.webui.VillainLairDecorator;

class CADatabase1200to1299 extends ChoiceAdventureDatabase {
  final void add1200to1299() {
    new ChoiceAdventure(1200, "The Bookmobile", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1201, "Dr. Gordon Stuart, a Scientist", null) {
      void setup() {
        // options are assigned dynamically?
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }
    };

    new ChoiceAdventure(1202, "Noon in the Civic Center", "Gingerbread Civic Center") {
      void setup() {
        this.customName = this.name;

        new Option(1).attachItem("fancy marzipan briefcase", 1, AUTO, new NoDisplay());
        new Option(2, "acquire 50 sprinkles and unlock judge fudge", true)
            .attachItem(ItemPool.SPRINKLES, 50, AUTO, new NoDisplay());
        new Option(3, "enter Civic Planning Office")
            .leadsTo(1210, true)
            .attachItem(ItemPool.SPRINKLES, -1000, MANUAL);
        new Option(4)
            .attachItem("briefcase full of sprinkles", 1, AUTO)
            .attachItem(ItemPool.GINGERBREAD_BLACKMAIL_PHOTOS, -1, MANUAL, new NoDisplay());

        new CustomOption(1, "fancy marzipan briefcase");
        new CustomOption(3, "enter Civic Planning Office (costs 1000 sprinkles)");
        new CustomOption(
            4, "acquire briefcase full of sprinkles (with gingerbread blackmail photos)");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // You bribe the clerk and he lets you into the office with a sneer. And a key. The key was
        // probably the important part.
        if (decision == 3 && !request.responseText.contains("bribe the clerk")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");

        // You knock the column over and collect the sprinkles from its shattered remains.
        // A booming voice from behind you startles you. "YOU ARE IN CONTEMPT OF COURT!"
        if (request.responseText.contains("CONTEMPT OF COURT!")) {
          Preferences.setBoolean("_gingerbreadColumnDestroyed", true);
        } else if (request.responseText.contains("briefcase full of sprinkles")) {
          // He squints at you and pushes a briefcase across the table.
          Preferences.setBoolean("gingerBlackmailAccomplished", true);
        }
      }
    };

    new ChoiceAdventure(1203, "Midnight in Civic Center", "Gingerbread Civic Center") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 500 mysticality, learn about digging", true);
        new Option(2)
            .attachItem(ItemPool.COUNTERFEIT_CITY, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -300, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem("gingerbread moneybag", 1, AUTO)
            .attachItem(ItemPool.CREME_BRULEE_TORCH, -1, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.GINGERBREAD_CIGARETTE, 5, AUTO)
            .attachItem(ItemPool.SPRINKLES, -5, MANUAL, new NoDisplay());
        new Option(5)
            .attachItem("chocolate puppy", 1, AUTO)
            .attachItem(
                ItemPool.GINGERBREAD_DOG_TREAT, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(2, "acquire counterfeit city (costs 300 sprinkles)");
        new CustomOption(3, "acquire gingerbread moneybag (with creme brulee torch)");
        new CustomOption(4, "acquire 5 gingerbread cigarettes (costs 5 sprinkles)");
        new CustomOption(5, "acquire chocolate puppy (with gingerbread dog treat)");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 2:
            // You pay the counterfeiter to make you a fake version of Gingerbread City to pawn off
            // on some rube as the real thing
            if (!request.responseText.contains("fake version of Gingerbread City")) {
              this.choiceFailed();
            }
            break;
          case 3:
            // You quickly melt the lock on the cell and the criminal inside thanks you as he runs
            // off into the night.
            // "Hey," you shout after him, "you forgot your..." but he's already gone.
            // Oh well. He almost certainly stole this thing, anyway.
            if (!request.responseText.contains("melt the lock on the cell")) {
              this.choiceFailed();
            }
            break;
          case 4:
            // You insert your sprinkles and buy your cigarettes.
            if (!request.responseText.contains("buy your cigarettes")) {
              this.choiceFailed();
            }
            break;
          case 5:
            // You feed the treat to the puppy and he immediately becomes a loyal friend to you.
            // Dogs are so easy!
            if (!request.responseText.contains("Dogs are so easy")) {
              this.choiceFailed();
            }
            break;
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");

        // You step into the library and spend a few hours studying law. It's surprisingly
        // difficult! You gain a new respect for lawyers.
        // Haha no you don't.
        if (request.responseText.contains("few hours studying law")) {
          Preferences.increment("gingerLawChoice");
        }
      }
    };

    new ChoiceAdventure(1204, "Noon at the Train Station", "Gingerbread Train Station") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 8-11 candies", true);
        new Option(2, "increase ML and sprinkles drop of sewer gators (with sewer unlocked)", true);
        new Option(3, "gain 250 mysticality", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");

        // You pull the lever and hear a rumbling from underneath you as the sewer gets much larger.
        // Now there's more room for the alligators!
        if (request.responseText.contains("more room for the alligators")) {
          Preferences.setBoolean("_gingerBiggerAlligators", true);
          return;
        }

        // You can't make heads or tails of it
        if (request.responseText.contains("can't make heads or tails of it")) {
          Preferences.increment("gingerTrainScheduleStudies");
        } else if (request.responseText.contains("starting to get a feel for it")) {
          // You're starting to get a feel for it, but it's still really confusing
          Preferences.increment("gingerTrainScheduleStudies");
          if (Preferences.getInteger("gingerTrainScheduleStudies") < 4) {
            Preferences.setInteger("gingerTrainScheduleStudies", 4);
          }
        } else if (request.responseText.contains("starting to get a handle")) {
          // You're starting to get a handle on how it all works.
          Preferences.increment("gingerTrainScheduleStudies");
          if (Preferences.getInteger("gingerTrainScheduleStudies") < 7) {
            Preferences.setInteger("gingerTrainScheduleStudies", 7);
          }
        } else if (request.responseText.contains("pretty good understanding")) {
          // You think you've got a pretty good understanding of it at this point.
          Preferences.increment("gingerTrainScheduleStudies");
          if (Preferences.getInteger("gingerTrainScheduleStudies") < 10) {
            Preferences.setInteger("gingerTrainScheduleStudies", 10);
          }
        }
        // What next?
      }
    };

    new ChoiceAdventure(1205, "Midnight at the Train Station", "Gingerbread Train Station") {
      void setup() {
        this.customName = this.name;

        new Option(1);
        new Option(2, "acquire broken chocolate pocketwatch")
            .attachItem(ItemPool.BROKEN_CHOCOLATE_POCKETWATCH, 1, AUTO);
        new Option(3).leadsTo(1211);
        new Option(4, "acquire fruit-leather negatives")
            .attachItem(ItemPool.FRUIT_LEATHER_NEGATIVE, 1, AUTO);
        new Option(5, "acquire various items");

        new CustomOption(1, "gain 500 muscle and add track");
        new CustomOption(2, "acquire broken chocolate pocketwatch (with pumpkin spice candle)");
        new CustomOption(3, "enter The Currency Exchange (with candy crowbar)");
        new CustomOption(4, "acquire fruit-leather negatives (with track added)");
        new CustomOption(5, "acquire various items (with teethpick)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(Preferences.getInteger("gingerMuscleChoice") + " tracks added this ascension");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("sugar raygun")) {
          getOption(decision).attachItem(ItemPool.TEETHPICK, -1, MANUAL);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");

        if (request.responseText.contains("provide a great workout")) {
          Preferences.increment("gingerMuscleChoice");
        }

        if (request.responseText.contains("new line to the subway system")) {
          Preferences.setBoolean("gingerSubwayLineUnlocked", true);
        }

        if (request.responseText.contains("what looks like a sweet roll")) {
          Preferences.increment("gingerDigCount");
        } else if (request.responseText.contains("piece of rock candy")) {
          Preferences.increment("gingerDigCount");
          if (Preferences.getInteger("gingerDigCount") < 4) {
            Preferences.setInteger("gingerDigCount", 4);
          }
        } else if (request.responseText.contains("sugar raygun")) {
          Preferences.setInteger("gingerDigCount", 7);
        }
      }
    };

    new ChoiceAdventure(1206, "Noon in the Industrial Zone", "Gingerbread Industrial Zone") {
      void setup() {
        this.customName = this.name;

        new Option(1)
            .attachItem(ItemPool.CREME_BRULEE_TORCH, 1, AUTO, new NoDisplay())
            .attachItem(ItemPool.SPRINKLES, -25, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem("candy crowbar", 1, AUTO, new NoDisplay())
            .attachItem(ItemPool.SPRINKLES, -50, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem("candy screwdriver", 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -100, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.TEETHPICK, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -1000, MANUAL, new NoDisplay());
        new Option(5, "acquire 400-600 sprinkles")
            .attachItem(ItemPool.SPRINKLES, 500, AUTO, new NoDisplay());

        new CustomOption(1, "acquire creme brulee torch (costs 25 sprinkles)");
        new CustomOption(2, "acquire candy crowbar (costs 50 sprinkles)");
        new CustomOption(3, "acquire candy screwdriver (costs 100 sprinkles)");
        new CustomOption(4, "acquire teethpick (costs 1000 sprinkles after studying law)");
        new CustomOption(
            5, "acquire 400-600 sprinkles (with gingerbread mask, pistol and moneybag)");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1: // creme brulee torch
          case 2: // candy crowbar
          case 3: // candy screwdriver
          case 4: // teethpick
            // You buy the tool.
            if (!request.responseText.contains("buy the tool")) {
              this.choiceFailed();
            }
            break;
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");
      }
    };

    new ChoiceAdventure(1207, "Midnight in the Industrial Zone", "Gingerbread Industrial Zone") {
      void setup() {
        this.customName = this.name;

        new Option(1).leadsTo(1212, true, o -> true);
        new Option(2).leadsTo(1213, false, o -> true, " or ");
        new Option(3).attachItem(ItemPool.SPRINKLES, -100000, MANUAL);

        new CustomOption(1, "enter Seedy Seedy Seedy");
        new CustomOption(2, "enter The Factory Factor");
        new CustomOption(3, "acquire tattoo (costs 100000 sprinkles)");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // You can't afford a tattoo.
        if (decision == 3 && request.responseText.contains("can't afford a tattoo")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");
      }
    };

    new ChoiceAdventure(1208, "Upscale Noon", "Gingerbread Upscale Retail District") {
      void setup() {
        this.customName = this.name;

        new Option(1)
            .attachItem(ItemPool.GINGERBREAD_DOG_TREAT, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -200, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem("pumpkin spice candle", 1, AUTO, new NoDisplay())
            .attachItem(ItemPool.SPRINKLES, -150, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem("gingerbread spice latte", 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -50, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem("gingerbread trousers", 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -500, MANUAL, new NoDisplay());
        new Option(5)
            .attachItem("gingerbread waistcoat", 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -500, MANUAL, new NoDisplay());
        new Option(6)
            .attachItem("gingerbread tophat", 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -500, MANUAL, new NoDisplay());
        new Option(7, "acquire 400-600 sprinkles")
            .attachItem(ItemPool.SPRINKLES, 500, AUTO, new NoDisplay());
        new Option(
                8,
                "acquire gingerbread blackmail photos (drop off fruit-leather negatives and pick up next visit)",
                true)
            .attachItem(ItemPool.GINGERBREAD_BLACKMAIL_PHOTOS, 1, AUTO);
        new Option(9);

        new CustomOption(1, "acquire gingerbread dog treat (costs 200 sprinkles)");
        new CustomOption(2, "acquire pumpkin spice candle (costs 150 sprinkles)");
        new CustomOption(3, "acquire gingerbread spice latte (costs 50 sprinkles)");
        new CustomOption(4, "acquire gingerbread trousers (costs 500 sprinkles)");
        new CustomOption(5, "acquire gingerbread waistcoat (costs 500 sprinkles)");
        new CustomOption(6, "acquire gingerbread tophat (costs 500 sprinkles)");
        new CustomOption(
            7, "acquire 400-600 sprinkles (with gingerbread mask, pistol and moneybag)");
        new CustomOption(9, "leave");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1: // gingerbread dog treat
          case 2: // pumpkin spice candle
          case 3: // gingerbread spice latte
          case 4: // gingerbread trousers
          case 5: // gingerbread waistcoat
          case 6: // gingerbread tophat
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
          case 8:
            if (request.responseText.contains("drop off the negatives")) {
              Preferences.setBoolean("gingerNegativesDropped", true);
              getOption(decision).attachItem(ItemPool.FRUIT_LEATHER_NEGATIVE, -1, MANUAL);
            }
            break;
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");

        if (decision == 8 && request.responseText.contains("You acquire an item")) {
          // gingerbread blackmail photos
          Preferences.setBoolean("gingerNegativesDropped", false);
        }
      }
    };

    new ChoiceAdventure(1209, "Upscale Midnight", "Gingerbread Upscale Retail District") {
      void setup() {
        this.customName = this.name;

        new Option(1).attachItem("fake cocktail", 1, AUTO);
        new Option(2).leadsTo(1214);

        new CustomOption(1, "acquire fake cocktail");
        new CustomOption(2, "enter The Gingerbread Gallery (wearing Gingerbread Best)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_gingerbreadCityTurns");
      }
    };

    new ChoiceAdventure(1210, "Civic Planning Office", "Gingerbread Civic Center") {
      void setup() {
        this.customName = this.name;

        new Option(1, "unlock Gingerbread Upscale Retail District", true);
        new Option(2, "unlock Gingerbread Sewers", true);
        new Option(3, "unlock 10 extra City adventures", true);
        new Option(4, "unlock City Clock", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (Preferences.getBoolean("gingerRetailUnlocked")) {
          getOption(1).text("already unlocked");
        }

        if (Preferences.getBoolean("gingerSewersUnlocked")) {
          getOption(2).text("already unlocked");
        }

        if (Preferences.getBoolean("gingerExtraAdventures")) {
          getOption(3).text("already unlocked");
        }

        if (Preferences.getBoolean("gingerAdvanceClockUnlocked")) {
          getOption(4).text("already unlocked");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // You move the policy to the front of the drawer, and by the time you leave the office
        // they've already enacted it!
        if (!request.responseText.contains("they've already enacted it")) {
          return;
        }

        switch (decision) {
          case 1:
            Preferences.setBoolean("gingerRetailUnlocked", true);
            break;
          case 2:
            Preferences.setBoolean("gingerSewersUnlocked", true);
            break;
          case 3:
            Preferences.setBoolean("gingerExtraAdventures", true);
            break;
          case 4:
            Preferences.setBoolean("gingerAdvanceClockUnlocked", true);
            break;
        }
      }
    };

    new ChoiceAdventure(1211, "The Currency Exchange", "Gingerbread Train Station") {
      void setup() {
        this.customName = this.name;

        new Option(1).attachMeat(5000, AUTO);
        new Option(2).attachItem(ItemPool.FAT_LOOT_TOKEN, 1, AUTO);
        new Option(3).attachItem(ItemPool.SPRINKLES, 250, AUTO);
        new Option(4).attachItem(ItemPool.PRICELESS_DIAMOND, 1, AUTO);
        new Option(5).attachItem(ItemPool.PRISTINE_FISH_SCALE, 5, AUTO);

        new CustomOption(1, "acquire 5000 meat");
        new CustomOption(2, "acquire fat loot token");
        new CustomOption(3, "acquire 250 sprinkles");
        new CustomOption(4, "acquire priceless diamond");
        new CustomOption(5, "acquire 5 pristine fish scales");
      }
    };

    new ChoiceAdventure(1212, "Seedy Seedy Seedy", "Gingerbread Industrial Zone") {
      void setup() {
        this.customName = this.name;

        new Option(1)
            .attachItem("gingerbread pistol", 1, AUTO, new DisplayAll("pistol"))
            .attachItem(ItemPool.SPRINKLES, -300, MANUAL, new NoDisplay());
        new Option(2);
        new Option(3)
            .attachItem("ginger beer", 1, AUTO, new DisplayAll("beer"))
            .attachItem(ItemPool.GINGERBREAD_MUG, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(1, "acquire gingerbread pistol (costs 300 sprinkles)");
        new CustomOption(2, "gain 500 moxie");
        new CustomOption(3, "ginger beer (with gingerbread mug)");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          for (Option option : this.options.values()) {
            option.text(this.customOptions.get(option.index).displayText);
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1: // gingerbread pistol
          case 3: // gingerbread beer
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(1213, "The Factory Factor", "Gingerbread Industrial Zone") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire spare chocolate parts", true)
            .attachItem(ItemPool.SPARE_CHOCOLATE_PARTS, 1, AUTO);
        new Option(2, "fight GNG-3-R (with gingerservo)", true)
            .attachItem(ItemPool.GINGERSERVO, -1, MANUAL);
      }
    };

    new ChoiceAdventure(1214, "The Gingerbread Gallery", "Gingerbread Upscale Retail District") {
      void setup() {
        this.customName = this.name;

        new Option(1).attachItem(ItemPool.HIGH_END_GINGER_WINE, 1, AUTO);
        new Option(2)
            .attachItem(ItemPool.CHOCOLATE_SCULPTURE, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -300, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem(ItemPool.POP_ART_BOOK, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -1000, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.NO_HATS_BOOK, 1, AUTO)
            .attachItem(ItemPool.SPRINKLES, -1000, MANUAL, new NoDisplay());

        new CustomOption(1, "acquire high-end ginger win");
        new CustomOption(2, "acquire fancy chocolate sculpture (costs 300 sprinkles)");
        new CustomOption(3, "acquire Pop Art: a Guide (costs 1000 sprinkles)");
        new CustomOption(4, "acquire No Hats as Art (costs 1000 sprinkles)");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 2: // chocolate sculpture
          case 3: // Pop Art: a Guide
          case 4: // No Hats as Art
            if (!request.responseText.contains("You acquire an item")) {
              this.choiceFailed();
            }
            break;
        }
      }
    };

    new ChoiceAdventure(1215, "Setting the Clock", "Gingerbread Civic Center") {
      void setup() {
        this.customName = this.name;

        new Option(1, "advance time by 6 (yes, 6) hours", true);
        new Option(2, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setBoolean("_gingerbreadClockVisited", true);

        if (decision == 1) {
          Preferences.setBoolean("_gingerbreadClockAdvanced", true);
          Preferences.increment("_gingerbreadCityTurns");
        }
      }
    };

    new RetiredChoiceAdventure(1216, "Krampus Defeated", "Crimbo's Hat") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1217, "Sweet Synthesis", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        SweetSynthesisRequest.postChoice1(request.getURLString(), request.responseText);
      }
    };

    new ChoiceAdventure(1218, "Wax On", "Item-Driven") {
      void setup() {
        new Option(1)
            .attachItem(ItemPool.MINIATURE_CANDLE, 1, AUTO)
            .attachItem(ItemPool.WAX_GLOB, -1, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem(ItemPool.WAX_HAND, 1, AUTO)
            .attachItem(ItemPool.WAX_GLOB, -1, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem(ItemPool.WAX_FACE, 1, AUTO)
            .attachItem(ItemPool.WAX_GLOB, -1, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.WAX_PANCAKE, 1, AUTO)
            .attachItem(ItemPool.WAX_GLOB, -1, MANUAL, new NoDisplay());
        new Option(5)
            .attachItem(ItemPool.WAX_BOOZE, 1, AUTO)
            .attachItem(ItemPool.WAX_GLOB, -1, MANUAL, new NoDisplay());
        new Option(6);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision > 0 && decision < 6 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1219, "Approach the Jellyfish", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // You acquire an item: sea jelly
        // You think it'd be best to leave them alone for the rest of the day.
        if (decision == 1) {
          Preferences.setBoolean("_seaJellyHarvested", true);
        }
      }
    };

    new ChoiceAdventure(1220, "Into the Abyss", "The Marinara Trench") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1221, "Space Directions", "The Hole in the Sky") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }
    };

    new ChoiceAdventure(1222, "The Tunnel of L.O.V.E.", "The Tunnel of L.O.V.E.") {
      void setup() {
        new Option(1).leadsTo(1223);
        new Option(2);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Walk away from The Tunnel of L.O.V.E. if you've already had a trip
        if (responseText.contains("You've already gone through the Tunnel once today")) {
          return "2";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains(
                "You don't think you are feeling up to a trip through the Tunnel")) {
          Preferences.setBoolean("_loveTunnelUsed", true);
        }
      }
    };

    new ChoiceAdventure(1223, "L.O.V. Entrance", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Fight 1";

        new Option(1);
        new Option(2);

        new CustomOption(1, "(free) fight LOV Enforcer");
        new CustomOption(2, "avoid fight");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1224, "L.O.V. Equipment Room", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Choice 1";

        new Option(1).attachItem("LOV Eardigan", 1, AUTO, new NoDisplay());
        new Option(2).attachItem("LOV Epaulettes", 1, AUTO, new NoDisplay());
        new Option(3).attachItem("LOV Earrings", 1, AUTO, new NoDisplay());
        new Option(4);

        new CustomOption(1, "acquire LOV Eardigan");
        new CustomOption(2, "acquire LOV Epaulettes");
        new CustomOption(3, "acquire LOV Earrings");
        new CustomOption(4, "take nothing");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1225, "L.O.V. Engine Room", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Fight 2";

        new Option(1);
        new Option(2);

        new CustomOption(1, "(free) fight LOV Engineer");
        new CustomOption(2, "avoid fight");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1226, "L.O.V. Emergency Room", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Choice 2";

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);

        new CustomOption(1, "50 adv of Lovebotamy (+10 stats/fight)");
        new CustomOption(2, "50 adv of Open Heart Surgery (+10 fam weight)");
        new CustomOption(3, "50 adv of Wandering Eye Surgery (+50 item drop)");
        new CustomOption(4, "get no buff");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1227, "L.O.V. Elbow Room", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Fight 3";

        new Option(1);
        new Option(2);

        new CustomOption(1, "(free) fight LOV Equivocator");
        new CustomOption(2, "avoid fight");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1228, "L.O.V. Emporium", "The Tunnel of L.O.V.E.") {
      void setup() {
        this.customName = "L.O.V.E Choice 3";

        new Option(1).attachItem("LOV Enamorang", 1, AUTO, new NoDisplay());
        new Option(2).attachItem("LOV Emotionizer", 1, AUTO, new NoDisplay());
        new Option(3).attachItem("LOV Extraterrestrial Chocolate", 1, AUTO, new NoDisplay());
        new Option(4).attachItem("LOV Echinacea Bouquet", 1, AUTO, new NoDisplay());
        new Option(5).attachItem("LOV Elephant", 1, AUTO, new NoDisplay());
        new Option(6).attachItem("toast", 2, AUTO, new NoDisplay());
        new Option(7);

        new CustomOption(1, "acquire LOV Enamorang");
        new CustomOption(2, "acquire LOV Emotionizer");
        new CustomOption(3, "acquire LOV Extraterrestrial Chocolate");
        new CustomOption(4, "acquire LOV Echinacea Bouquet");
        new CustomOption(5, "acquire LOV Elephant");
        new CustomOption(6, "acquire 2 pieces of toast (if have Space Jellyfish)");
        new CustomOption(7, "take nothing");
      }

      @Override
      void registerDeferredChoice() {
        RequestLogger.registerLastLocation();
      }
    };

    new ChoiceAdventure(1229, "L.O.V. Exit", "The Tunnel of L.O.V.E.") {
      final Pattern LOV_EXIT_PATTERN = Pattern.compile("a sign above it that says <b>(.*?)</b>");
      final Pattern LOV_LOGENTRY_PATTERN = Pattern.compile("you scrawl <b>(.*?)</b>");

      void setup() {}

      @Override
      void visitChoice(GenericRequest request) {
        // As you are about to leave the station, you notice a
        // data entry pad and a sign above it that says <WORD>.
        // Huh, that's odd.

        Matcher matcher = LOV_EXIT_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          String message = "L.O.V. Exit word: " + matcher.group(1);
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Matcher matcher = LOV_LOGENTRY_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          String message = "Your log entry: " + matcher.group(1);
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
        }
      }
    };

    new ChoiceAdventure(1230, "Welcome to the Kingdom, Gelatinous Noob", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1231, "Gummi-Memories, In the Corner of Your Mind", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.GUMMY_MEMORY, -1, MANUAL, new NoDisplay());
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.increment("noobDeferredPoints", 5);
        }
      }
    };

    new ChoiceAdventure(1232, "Finally Human", null) {
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

    new ChoiceAdventure(1233, "Equipment Requisition", null) {
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
      }
    };

    new ChoiceAdventure(1234, "Spacegate Vaccination Machine", null) {
      final Pattern VACCINE_PATTERN =
          Pattern.compile("option value=(\\d+).*?class=button type=submit value=\"([^\"]*)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = VACCINE_PATTERN.matcher(request.responseText);

        while (matcher.find()) {
          String setting = "spacegateVaccine" + matcher.group(1);
          String button = matcher.group(2);
          if (button.startsWith("Select Vaccine")) {
            Preferences.setBoolean(setting, true);
          } else if (button.startsWith("Unlock Vaccine")) {
            Preferences.setBoolean(setting, false);
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // option 1 = Rainbow Vaccine
        // option 2 = Broad-Spectrum Vaccine
        // option 3 = Emotional Vaccine

        // You can unlock it (by turning in enough research) or
        // Select it (if previously unlocked).

        if (request.responseText.contains("New vaccine unlocked!")) {
          Preferences.setBoolean("spacegateVaccine" + decision, true);
        } else if (request.responseText.contains("You acquire an effect")) {
          Preferences.setBoolean("_spacegateVaccine", true);
        }
      }
    };

    new ChoiceAdventure(1235, "Spacegate Terminal", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // dials
        new Option(2); // input
        new Option(3); // random
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestManager.parseSpacegateTerminal(request.responseText, true);
      }
    };

    new ChoiceAdventure(1236, "Space Cave", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get Research").turnCost(1).attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO);
        new Option(2, "get more Research, sometimes a gemstone")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO)
            .attachItem(ItemPool.ALIEN_GEMSTONE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(1, "get Research (more with geology kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "1" || decision == "2") {
          if (responseText.contains("Drill out a core sample")) {
            return "2";
          }
          return "1";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1237, "A Simple Plant", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true).turnCost(1).attachItem("edible alien plant bit", 1, AUTO);
        new Option(2, "get 1 Research")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_PLANT_FIBERS, 1, AUTO);
        new Option(3, "get 3 Research")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_PLANT_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 1 Research (3 with botany kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Carefully extract a sample")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1238, "A Complicated Plant", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "3 edible alien plant bits", true)
            .turnCost(1)
            .attachItem("edible alien plant bit", 3, AUTO);
        new Option(2, "get 2-3 Research")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_PLANT_FIBERS, 2, AUTO);
        new Option(3, "get 10 Research")
            .turnCost(1)
            .attachItem(ItemPool.COMPLEX_ALIEN_PLANT_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 2-3 Research (10 with botany kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Extract a sample")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1239, "What a Plant!", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "4 edible alien plant bits", true)
            .turnCost(1)
            .attachItem("edible alien plant bit", 4, AUTO);
        new Option(2, "get 3-4 Research")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_PLANT_FIBERS, 3, AUTO);
        new Option(3, "get 20 Research")
            .turnCost(1)
            .attachItem(ItemPool.FASCINATING_ALIEN_PLANT_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 3-4 Research (20 with botany kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Extract a sample")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1240, "The Animals, The Animals", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true).turnCost(1).attachItem("alien meat", 1, AUTO);
        new Option(2, "get 1 Research").turnCost(1).attachItem(ItemPool.ALIEN_TOENAILS, 1, AUTO);
        new Option(3, "get 3 Research")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_ZOOLOGICAL_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 1 Research (3 with zoology kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Extract some DNA")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(
        1241, "Buffalo-Like Animal, Won't You Come Out Tonight", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "3 pieces of alien meat", true).turnCost(1).attachItem("alien meat", 3, AUTO);
        new Option(2, "get 2-3 Research").turnCost(1).attachItem(ItemPool.ALIEN_TOENAILS, 2, AUTO);
        new Option(3, "get 10 Research")
            .turnCost(1)
            .attachItem(ItemPool.COMPLEX_ALIEN_ZOOLOGICAL_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 2-3 Research (10 with zoology kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Extract some DNA")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1242, "House-Sized Animal", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "4 pieces of alien meat", true).turnCost(1).attachItem("alien meat", 4, AUTO);
        new Option(2, "get 3-4 Research").turnCost(1).attachItem(ItemPool.ALIEN_TOENAILS, 3, AUTO);
        new Option(3, "get 20 Research")
            .turnCost(1)
            .attachItem(ItemPool.FASCINATING_ALIEN_ZOOLOGICAL_SAMPLE, 1, AUTO);
        new Option(6, "skip adventure", true);

        new CustomOption(2, "get 3-4 Research (20 with zoology kit)");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision == "2" || decision == "3") {
          if (responseText.contains("Extract some DNA")) {
            return "3";
          }
          return "2";
        }
        return decision;
      }
    };

    new ChoiceAdventure(1243, "Interstellar Trade", "Through the Spacegate") {
      final Pattern COST_PATTERN = Pattern.compile("then holds up (\\d+) fingers");

      void setup() {
        this.customName = this.name;

        new Option(1);
        new Option(6);

        new CustomOption(1, "purchase item");
        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = COST_PATTERN.matcher(request.responseText);

        if (matcher.find()) {
          getOption(1).attachMeat(-StringUtilities.parseInt(matcher.group(1)), MANUAL);
        }
      }
    };

    new ChoiceAdventure(1244, "Here There Be No Spants", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1).turnCost(1).attachItem(ItemPool.MURDERBOT_DATA_CORE, 1, AUTO);
      }
    };

    new ChoiceAdventure(1245, "Recovering the Satellite", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1).turnCost(1).attachItem(ItemPool.SPANT_EGG_CASING, 1, AUTO);
      }
    };

    new ChoiceAdventure(1246, "Land Ho", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 10% Space Pirate language", true).turnCost(1);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(
                "gain 10% Space Pirate language (currently "
                    + Preferences.getInteger("spacePirateLanguageFluency")
                    + "%)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          SpacegateManager.parseLanguageFluency(request.responseText, "spacePirateLanguageFluency");
        }
      }
    };

    new ChoiceAdventure(1247, "Half The Ship it Used to Be", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(
                1,
                "with enough Space Pirate language, get space pirate treasure map, otherwise get +5% Space Pirate language",
                true)
            .turnCost(1)
            .attachItem(ItemPool.SPACE_PIRATE_TREASURE_MAP, 1, AUTO);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(
                "with enough Space Pirate language, get space pirate treasure map, otherwise get +5% Space Pirate language (currently "
                    + Preferences.getInteger("spacePirateLanguageFluency")
                    + "%)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("You acquire an item")) {
            Preferences.setInteger("spacePirateLanguageFluency", 0);
          } else {
            SpacegateManager.parseLanguageFluency(
                request.responseText, "spacePirateLanguageFluency");
          }
        }
      }
    };

    new ChoiceAdventure(1248, "Paradise Under a Strange Sun", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(
                1,
                "acquire Space Pirate Astrogation Handbook (with space pirate treasure map)",
                true)
            .turnCost(1)
            .attachItem(ItemPool.SPACE_PIRATE_ASTROGATION_HANDBOOK, 1, AUTO)
            .attachItem(ItemPool.SPACE_PIRATE_TREASURE_MAP, -1, MANUAL, new NoDisplay());
        new Option(2, "gain 1000 moxie stats", true).turnCost(1);
        new Option(6);

        new CustomOption(6, "leave");
      }
    };

    new ChoiceAdventure(1249, "That's No Moonlith, it's a Monolith", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "gain 20% procrastinator language (with murderbot data core)", true)
            .turnCost(1)
            .attachItem(ItemPool.MURDERBOT_DATA_CORE, -1, MANUAL);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(
                "gain 20% procrastinator language (with murderbot data core) (currently "
                    + Preferences.getInteger("procrastinatorLanguageFluency")
                    + "%)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          SpacegateManager.parseLanguageFluency(
              request.responseText, "procrastinatorLanguageFluency");
        }
      }
    };

    new ChoiceAdventure(1250, "I'm Afraid It's Terminal", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(
                1, "acquire procrastinator locker key (with enough procrastinator language)", true)
            .turnCost(1)
            .attachItem(ItemPool.PROCRASTINATOR_LOCKER_KEY, 1, AUTO);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(
                "acquire procrastinator locker key (with enough procrastinator language) (currently "
                    + Preferences.getInteger("procrastinatorLanguageFluency")
                    + "%)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("You acquire an item")) {
            Preferences.setInteger("procrastinatorLanguageFluency", 0);
          }
        }
      }
    };

    new ChoiceAdventure(1251, "Curses, a Hex", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire Non-Euclidean Finance (with procrastinator locker key)", true)
            .turnCost(1)
            .attachItem(ItemPool.NON_EUCLIDEAN_FINANCE, 1, AUTO)
            .attachItem(ItemPool.PROCRASTINATOR_LOCKER_KEY, -1, MANUAL, new NoDisplay());
        new Option(6);

        new CustomOption(6, "leave");
      }
    };

    new ChoiceAdventure(1252, "Time Enough at Last", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire Space Baby childrens' book", true)
            .turnCost(1)
            .attachItem(ItemPool.SPACE_BABY_CHILDRENS_BOOK, 1, AUTO);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          // You get a Space Baby children's book which
          // will grants spaceBabyLanguageFluency +10
          // when read
        }
      }
    };

    new ChoiceAdventure(1253, "Mother May I", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire Space Baby bawbaw (with enough Space Baby language)", true)
            .turnCost(1)
            .attachItem(ItemPool.SPACE_BABY_BAWBAW, 1, AUTO);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(1)
            .text(
                "acquire Space Baby bawbaw (with enough Space Baby language) (currently "
                    + Preferences.getInteger("spaceBabyLanguageFluency")
                    + "%)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("You acquire an item")) {
            Preferences.setInteger("spaceBabyLanguageFluency", 0);
          }
        }
      }
    };

    new ChoiceAdventure(1254, "Please Baby Baby Please", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire Peek-a-Boo! (with Space Baby bawbaw)", true)
            .turnCost(1)
            .attachItem(ItemPool.PEEK_A_BOO, 1, AUTO)
            .attachItem(ItemPool.SPACE_BABY_BAWBAW, -1, MANUAL, new NoDisplay());
        new Option(6);

        new CustomOption(6, "leave");
      }
    };

    new ChoiceAdventure(1255, "Cool Space Rocks", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get Research").turnCost(1).attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO);
        new Option(2, "get more Research, sometimes a gemstone")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO)
            .attachItem(ItemPool.ALIEN_GEMSTONE, 1, AUTO);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (responseText.contains("Take a core sample")) {
          return "2";
        }
        return "1";
      }
    };

    new ChoiceAdventure(1256, "Wide Open Spaces", "Through the Spacegate") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get Research").turnCost(1).attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO);
        new Option(2, "get more Research, sometimes a gemstone")
            .turnCost(1)
            .attachItem(ItemPool.ALIEN_ROCK_SAMPLE, 1, AUTO)
            .attachItem(ItemPool.ALIEN_GEMSTONE, 1, AUTO);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (responseText.contains("Take a core sample")) {
          return "2";
        }
        return "1";
      }
    };

    new ChoiceAdventure(1257, "License to Adventure", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1258, "Daily Briefing", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1259, "LI-11 HQ", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        LTAManager.parseLI11HQ(request);
      }
    };

    new ChoiceAdventure(1260, "A Strange Panel", "Super Villain's Lair") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return VillainLairDecorator.spoilColorChoice();
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision < 1 || decision > 3) {
          return;
        }
        if (request.responseText.contains("10 casualties")
            || request.responseText.contains("10 crew")
            || request.responseText.contains("10 minions")
            || request.responseText.contains("10 ski")
            || request.responseText.contains("10 members")
            || request.responseText.contains("ten techs")
            || request.responseText.contains("10 soldiers")) {
          Preferences.increment("_villainLairProgress", 10);
        } else if (request.responseText.contains("5 casualties")
            || request.responseText.contains("5 souls")
            || request.responseText.contains("5 minions")
            || request.responseText.contains("five minions")
            || request.responseText.contains("group of ski")
            || request.responseText.contains("5 members")
            || request.responseText.contains("five people")
            || request.responseText.contains("five of us")) {
          Preferences.increment("_villainLairProgress", 5);
        } else {
          Preferences.decrement("_villainLairProgress", 7);
        }
        Preferences.setBoolean("_villainLairColorChoiceUsed", true);
      }
    };

    new ChoiceAdventure(1261, "Which Door?", "Super Villain's Lair") {
      void setup() {
        new Option(1).attachMeat(-1000, AUTO);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(2);

        if (request.responseText.contains("Boris")) {
          option.attachItem(ItemPool.BORIS_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
          Preferences.setString("_villainLairKey", "boris");
        } else if (request.responseText.contains("Jarlsberg")) {
          option.attachItem(ItemPool.JARLSBERG_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
          Preferences.setString("_villainLairKey", "jarlsberg");
        } else if (request.responseText.contains("Sneaky Pete")) {
          option.attachItem(
              ItemPool.SNEAKY_PETE_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
          Preferences.setString("_villainLairKey", "pete");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("insert the key")) {
            this.choiceFailed();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (request.responseText.contains("drop 1000")) {
            Preferences.increment("_villainLairProgress", 5);
            Preferences.setBoolean("_villainLairDoorChoiceUsed", true);
          }
        } else if (decision == 2) {
          if (request.responseText.contains("insert the key")) {
            Preferences.increment("_villainLairProgress", 15);
            Preferences.setBoolean("_villainLairDoorChoiceUsed", true);
          }
        } else if (decision == 3) {
          Preferences.decrement("_villainLairProgress", 13);
          Preferences.setBoolean("_villainLairDoorChoiceUsed", true);
        }
      }
    };

    new ChoiceAdventure(1262, "What Setting?", "Super Villain's Lair") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return VillainLairDecorator.Symbology(responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision < 1 || decision > 3) {
          return;
        }
        if (request.responseText.contains("20 of the")
            || request.responseText.contains("20 minions")
            || request.responseText.contains("20 or so")
            || request.responseText.contains("20 soldiers")) {
          Preferences.increment("_villainLairProgress", 20);
        } else if (request.responseText.contains("10 or so")
            || request.responseText.contains("10 injured")
            || request.responseText.contains("10 patrol-sicles")
            || request.responseText.contains("10 soldiers")) {
          Preferences.increment("_villainLairProgress", 10);
        } else if (request.responseText.contains("15 aquanats")
            || request.responseText.contains("15 reserve")
            || request.responseText.contains("15 previously")
            || request.responseText.contains("15 Soldiers")) {
          Preferences.decrement("_villainLairProgress", 15);
        }
        Preferences.setBoolean("_villainLairSymbologyChoiceUsed", true);
      }
    };

    new ChoiceAdventure(1263, "Lyle, Traveling Infrastructure Specialist", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
      }
    };

    new ChoiceAdventure(1264, "Meteor Metal Machinations", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1)
            .attachItem(ItemPool.METEORTARBOARD, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem(ItemPool.METEORITE_GUARD, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem(ItemPool.METEORB, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.ASTEROID_BELT, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(5)
            .attachItem(ItemPool.METEORTHOPEDIC_SHOES, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(6)
            .attachItem(ItemPool.SHOOTING_MORNING_STAR, 1, AUTO)
            .attachItem(ItemPool.METAL_METEOROID, -1, MANUAL, new NoDisplay());
        new Option(7);
      }
    };

    new ChoiceAdventure(1265, "Live. Ascend. Repeat.", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1266, "The Hostler", null) {
      final Pattern HORSE_NAME_PATTERN =
          Pattern.compile("<td valign=top class=small><b>([^<]+)</b> the ([^ ]+) Horse<P>");
      final Pattern CRAZY_HORSE_MODIFIERS_PATTERN =
          Pattern.compile(
              "Gives you\\s+([+-]\\d+)% Muscle, ([+-]\\d+)% Mysticality, and ([+-]\\d+)%");
      final Pattern HORSE_RENT_PATTERN = Pattern.compile("You rent(|ed) the (.*?)!");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Preferences.setBoolean("horseryAvailable", true);

        // <td valign=top class=small><b>Drab Teddy</b> the Normal Horse<P>
        // <td valign=top class=small><b>Surreptitious Mantilla</b> the Dark Horse<P>
        // <td valign=top class=small><b>Wacky Biggles</b> the Crazy Horse<P>
        // <td valign=top class=small><b>Frightful Twiggy</b> the Pale Horse<P>

        // Save the horse names so we can recognize them in combat
        Matcher matcher = HORSE_NAME_PATTERN.matcher(request.responseText);
        while (matcher.find()) {
          String name = matcher.group(1);
          String type = matcher.group(2);
          String setting =
              type.equals("Crazy")
                  ? "_horseryCrazyName"
                  : type.equals("Dark")
                      ? "_horseryDarkName"
                      : type.equals("Normal")
                          ? "_horseryNormalName"
                          : type.equals("Pale") ? "_horseryPaleName" : null;
          if (setting != null) {
            Preferences.setString(setting, name);
          }
        }

        matcher = CRAZY_HORSE_MODIFIERS_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("_horseryCrazyMus", matcher.group(1));
          Preferences.setString("_horseryCrazyMys", matcher.group(2));
          Preferences.setString("_horseryCrazyMox", matcher.group(3));
        }

        // The missing one is the one we have.
        Preferences.setString(
            "_horsery",
            !request.responseText.contains("name=option value=1")
                ? "normal horse"
                : !request.responseText.contains("name=option value=2")
                    ? "dark horse"
                    : !request.responseText.contains("name=option value=3")
                        ? "crazy horse"
                        : !request.responseText.contains("name=option value=4")
                            ? "pale horse"
                            : "");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision < 5) {
          Matcher matcher = HORSE_RENT_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            String horse = matcher.group(2);
            Preferences.setString("_horsery", horse);
            String message = "Chose the " + horse;
            RequestLogger.printLine(message);
            RequestLogger.updateSessionLog(message);
            String setting =
                horse.equals("crazy horse")
                    ? "_horseryCrazyName"
                    : horse.equals("dark horse")
                        ? "_horseryDarkName"
                        : horse.equals("normal horse")
                            ? "_horseryNormalName"
                            : horse.equals("pale horse") ? "_horseryPaleName" : null;
            if (setting != null) {
              String name = Preferences.getString(setting);
              Preferences.setString("_horseryCurrentName", name);
            }
          }
        } else if (decision == 5) {
          Preferences.setString("_horsery", "");
          Preferences.setString("_horseryCurrentName", "");
          String message = "Returned your horse";
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
        }
      }
    };

    new ChoiceAdventure(1267, "Rubbed it the Right Way", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        GenieRequest.visitChoice(request.responseText);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        String wish = request.getFormField("wish");
        GenieRequest.postChoice(request.responseText, wish);
      }
    };

    new ChoiceAdventure(1268, "Every Day I'm Chiselin'", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new UnknownChoiceAdventure(1269);

    new ChoiceAdventure(1270, "Pantagramming", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // The item that we get has a procedurally-generated name
        request.setHasResult(false);
        PantogramRequest.parseResponse(request.getURLString(), request.responseText);
      }
    };

    new ChoiceAdventure(1271, "Mummery", "Item-Driven") {
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
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        MummeryRequest.parseResponse(decision, request.responseText);
      }
    };

    new RetiredChoiceAdventure(1272, "Tammy R&D", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        int letterId = ChoiceManager.extractIidFromURL(request.getURLString());
        String letterName = ItemDatabase.getItemName(letterId);
        if (letterName != null) {
          // Turning in letter
          String message = "handed over " + letterName;
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
          ResultProcessor.removeItem(letterId);
        }
      }
    };

    new ChoiceAdventure(1273, "The Cursed Warehouse", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1)
            .attachItem(ItemPool.WAREHOUSE_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("slide the weird key into the weird lock")) {
          this.choiceFailed();
        }
      }
    };

    new UnknownChoiceAdventure(1274);

    new ChoiceAdventure(1275, "Rummaging through the Garbage", "Item-Driven") {
      final Pattern DECEASED_TREE_PATTERN = Pattern.compile("Looks like it has (.*?) needle");
      final Pattern BROKEN_CHAMPAGNE_PATTERN = Pattern.compile("Looks like it has (\\d+) ounce");
      final Pattern GARBAGE_SHIRT_PATTERN =
          Pattern.compile("Looks like you can read roughly (\\d+) scrap");

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
      void postChoice1(GenericRequest request, int decision) {
        if (decision >= 1 && decision <= 5) {
          // Remove all of the items before parsing the newly-received one
          EquipmentManager.removeEquipment(ItemPool.DECEASED_TREE);
          ResultProcessor.removeItem(ItemPool.DECEASED_TREE);
          EquipmentManager.removeEquipment(ItemPool.BROKEN_CHAMPAGNE);
          ResultProcessor.removeItem(ItemPool.BROKEN_CHAMPAGNE);
          EquipmentManager.removeEquipment(ItemPool.TINSEL_TIGHTS);
          ResultProcessor.removeItem(ItemPool.TINSEL_TIGHTS);
          EquipmentManager.removeEquipment(ItemPool.WAD_OF_TAPE);
          ResultProcessor.removeItem(ItemPool.WAD_OF_TAPE);
          EquipmentManager.removeEquipment(ItemPool.MAKESHIFT_GARBAGE_SHIRT);
          ResultProcessor.removeItem(ItemPool.MAKESHIFT_GARBAGE_SHIRT);
          if (!Preferences.getBoolean("_garbageItemChanged")) {
            Preferences.setInteger("garbageTreeCharge", 1000);
            Preferences.setInteger("garbageChampagneCharge", 11);
            Preferences.setInteger("garbageShirtCharge", 37);
          }
          Preferences.setBoolean("_garbageItemChanged", true);
        }
        // Do some parsing of needles/wine/scraps here
        Matcher matcher = DECEASED_TREE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger("garbageTreeCharge", StringUtilities.parseInt(matcher.group(1)));
        }
        matcher = BROKEN_CHAMPAGNE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger(
              "garbageChampagneCharge", StringUtilities.parseInt(matcher.group(1)));
        }
        matcher = GARBAGE_SHIRT_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger("garbageShirtCharge", StringUtilities.parseInt(matcher.group(1)));
        }
      }
    };

    new UnknownChoiceAdventure(1276);

    new ChoiceAdventure(1277, "Extra, Extra", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1)
            .attachItem(ItemPool.BURNING_HAT, 1, AUTO)
            .attachItem(ItemPool.BURNING_NEWSPAPER, -1, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem(ItemPool.BURNING_CAPE, 1, AUTO)
            .attachItem(ItemPool.BURNING_NEWSPAPER, -1, MANUAL, new NoDisplay());
        new Option(3)
            .attachItem(ItemPool.BURNING_SLIPPERS, 1, AUTO)
            .attachItem(ItemPool.BURNING_NEWSPAPER, -1, MANUAL, new NoDisplay());
        new Option(4)
            .attachItem(ItemPool.BURNING_JORTS, 1, AUTO)
            .attachItem(ItemPool.BURNING_NEWSPAPER, -1, MANUAL, new NoDisplay());
        new Option(5)
            .attachItem(ItemPool.BURNING_CRANE, 1, AUTO)
            .attachItem(ItemPool.BURNING_NEWSPAPER, -1, MANUAL, new NoDisplay());
        new Option(6);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision >= 1 && decision <= 5 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1278, "Madame Zatara&rsquo;s Relationship Fortune Teller", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        ClanFortuneDecorator.decorateQuestion(buffer);
      }
    };

    new ChoiceAdventure(1279, "Gotta Familiarize Yourself With 'Em All!", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1280, "Welcome to FantasyRealm", null) {
      void setup() {
        this.customName = this.name;
        this.customZones.add("FantasyRealm");

        new Option(1, "acquire FantasyRealm Warrior's Helm", true)
            .attachItem(ItemPool.FR_WARRIOR_HELM, 1, AUTO);
        new Option(2, "acquire FantasyRealm Mage's Hat", true)
            .attachItem(ItemPool.FR_MAGE_HAT, 1, AUTO);
        new Option(3, "acquire FantasyRealm Rogue's Mask", true)
            .attachItem(ItemPool.FR_ROGUE_MASK, 1, AUTO);
        new Option(6, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision != 6) {
          Preferences.setInteger("_frHoursLeft", 5);
          StringBuffer unlocks = new StringBuffer();
          unlocks.append("The Bandit Crossroads,");
          if (Preferences.getBoolean("frMountainsUnlocked")) {
            unlocks.append("The Towering Mountains,");
          }
          if (Preferences.getBoolean("frWoodUnlocked")) {
            unlocks.append("The Mystic Wood,");
          }
          if (Preferences.getBoolean("frSwampUnlocked")) {
            unlocks.append("The Putrid Swamp,");
          }
          if (Preferences.getBoolean("frVillageUnlocked")) {
            unlocks.append("The Cursed Village,");
          }
          if (Preferences.getBoolean("frCemetaryUnlocked")) {
            unlocks.append("The Sprawling Cemetery,");
          }
          Preferences.setString("_frAreasUnlocked", unlocks.toString());
        }
      }
    };

    new ChoiceAdventure(1281, "You'll See You at the Crossroads", "The Bandit Crossroads") {
      void setup() {
        new Option(1, "unlock The Towering Mountains", true).turnCost(1);
        new Option(2, "unlock The Mystic Wood", true).turnCost(1);
        new Option(3, "unlock The Putrid Swamp", true).turnCost(1);
        new Option(4, "unlock Cursed Village", true).turnCost(1);
        new Option(5, "unlock The Sprawling Cemetery", true).turnCost(1);
        new Option(8, "leave", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 8) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Bandit Crossroads,", "");
        }
        if (decision == 1) {
          unlocks.append("The Towering Mountains,");
        } else if (decision == 2) {
          unlocks.append("The Mystic Wood,");
        } else if (decision == 3) {
          unlocks.append("The Putrid Swamp,");
        } else if (decision == 4) {
          unlocks.append("The Cursed Village,");
        } else if (decision == 5) {
          unlocks.append("The Sprawling Cemetery,");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1282, "Out of Range", "The Towering Mountains") {
      void setup() {
        new Option(1, "unlock The Old Rubee Mine (using FantasyRealm key)", true)
            .turnCost(1)
            .attachItem(ItemPool.FR_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(2, "unlock The Foreboding Cave", true).turnCost(1);
        new Option(3, "unlock The Master Thief's Chalet").turnCost(1);
        new Option(4, "acquire charged druidic orb")
            .turnCost(1)
            .attachItem(ItemPool.FR_CHARGED_ORB, 1, AUTO)
            .attachItem(ItemPool.FR_DRUIDIC_ORB, -1, MANUAL, new NoDisplay());
        new Option(5, "unlock The Ogre Chieftain's Keep").turnCost(1);
        new Option(10);
        new Option(11);

        new CustomOption(3, "unlock The Master Thief's Chalet (with FantasyRealm Rogue's Mask)");
        new CustomOption(4, "charge druidic orb (need druidic orb)");
        new CustomOption(5, "unlock The Ogre Chieftain's Keep (with FantasyRealm Warrior's Helm)");
        new CustomOption(10, "1/5 to fight Skeleton Lord (with FantasyRealm outfit)");
        new CustomOption(11, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(10).text(CADatabase1200to1299.fantasyRealmBigRedButtonSpoiler());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 10 && !request.responseText.contains("Just send everybody home")) {
          getOption(10).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 11) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Towering Mountains,", "");
        }
        if (decision == 1) {
          unlocks.append("The Old Rubee Mine,");
        } else if (decision == 2) {
          unlocks.append("The Foreboding Cave,");
        } else if (decision == 3) {
          unlocks.append("The Master Thief's Chalet,");
        } else if (decision == 5) {
          unlocks.append("The Ogre Chieftain's Keep,");
        } else if (decision == 10) {
          CADatabase1200to1299.fantasyRealmParseBigRedButtonPress(request.responseText);
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1283, "Where Wood You Like to Go", "The Mystic Wood") {
      void setup() {
        new Option(1, "unlock The Faerie Cyrkle", true).turnCost(1);
        new Option(2, "unlock The Druidic Campsite").turnCost(1);
        new Option(3, "unlock The Ley Nexus").turnCost(1);
        new Option(5, "acquire plump purple mushroom", true)
            .turnCost(1)
            .attachItem(ItemPool.FR_PURPLE_MUSHROOM, 1, AUTO);
        new Option(10);
        new Option(11);

        new CustomOption(2, "unlock The Druidic Campsite (with LyleCo premium rope)");
        new CustomOption(3, "unlock The Ley Nexus (with Cheswick Copperbottom's compass)");
        new CustomOption(10, "1/5 to fight Skeleton Lord (with FantasyRealm outfit)");
        new CustomOption(11, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(10).text(CADatabase1200to1299.fantasyRealmBigRedButtonSpoiler());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 10 && !request.responseText.contains("Just send everybody home")) {
          getOption(10).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 11) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Mystic Wood,", "");
        }
        if (decision == 1) {
          unlocks.append("The Faerie Cyrkle,");
        } else if (decision == 2) {
          unlocks.append("The Druidic Campsite,");
        } else if (decision == 3) {
          unlocks.append("The Ley Nexus,");
        } else if (decision == 10) {
          CADatabase1200to1299.fantasyRealmParseBigRedButtonPress(request.responseText);
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1284, "Swamped with Leisure", "The Putrid Swamp") {
      void setup() {
        new Option(1, "unlock Near the Witch's House", true).turnCost(1);
        new Option(2, "unlock The Troll Fortress (using FantasyRealm key)", true)
            .turnCost(1)
            .attachItem(ItemPool.FR_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(3, "unlock The Dragon's Moor").turnCost(1);
        new Option(5, "acquire tainted marshmallow", true)
            .turnCost(1)
            .attachItem(ItemPool.FR_TAINTED_MARSHMALLOW, 1, AUTO);
        new Option(10);
        new Option(11);

        new CustomOption(3, "unlock The Dragon's Moor (with FantasyRealm Warrior's Helm)");
        new CustomOption(10, "1/5 to fight Skeleton Lord (with FantasyRealm outfit)");
        new CustomOption(11, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(10).text(CADatabase1200to1299.fantasyRealmBigRedButtonSpoiler());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 10 && !request.responseText.contains("Just send everybody home")) {
          getOption(10).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 11) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Putrid Swamp,", "");
        }
        if (decision == 1) {
          unlocks.append("Near the Witch's House,");
        } else if (decision == 2) {
          unlocks.append("The Troll Fortress,");
        } else if (decision == 3) {
          unlocks.append("The Dragon's Moor,");
        } else if (decision == 10) {
          CADatabase1200to1299.fantasyRealmParseBigRedButtonPress(request.responseText);
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1285, "It Takes a Cursed Village", "The Cursed Village") {
      void setup() {
        new Option(1, "unlock The Evil Cathedral", true).turnCost(1);
        new Option(2, "unlock The Cursed Village Thieves' Guild").turnCost(1);
        new Option(3, "unlock The Archwizard's Tower").turnCost(1);
        new Option(4, "20 adv of Fortunate, Son").turnCost(1).attachEffect("Fortunate, Son");
        new Option(5, "acquire 40-60 Rubees&trade;")
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(6, "acquire dragon slaying sword")
            .turnCost(1)
            .attachItem(ItemPool.FR_DRAGON_SLAYING_SWORD, 1, AUTO)
            .attachItem(ItemPool.FR_DRAGON_ORE, -1, MANUAL, new NoDisplay());
        new Option(7, "acquire notarized arrest warrant")
            .turnCost(1)
            .attachItem(ItemPool.FR_NOTARIZED_WARRANT, 1, AUTO)
            .attachItem(ItemPool.FR_ARREST_WARRANT, -1, MANUAL, new NoDisplay());
        new Option(10);
        new Option(11);

        new CustomOption(
            2, "unlock The Cursed Village Thieves' Guild (using FantasyRealm Rogue's Mask)");
        new CustomOption(3, "unlock The Archwizard's Tower (with FantasyRealm Mage's Hat)");
        new CustomOption(4, "20 adv of +2-3 Rubee&trade; drop");
        new CustomOption(5, "acquire 40-60 Rubees&trade; (with LyleCo premium rope)");
        new CustomOption(6, "acquire dragon slaying sword (with dragon aluminum ore)");
        new CustomOption(7, "acquire notarized arrest warrant (with arrest warrant)");
        new CustomOption(10, "1/5 to fight Skeleton Lord (with FantasyRealm outfit)");
        new CustomOption(11, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(10).text(CADatabase1200to1299.fantasyRealmBigRedButtonSpoiler());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 10 && !request.responseText.contains("Just send everybody home")) {
          getOption(10).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 11) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Cursed Village,", "");
        }
        if (decision == 1) {
          unlocks.append("The Evil Cathedral,");
        } else if (decision == 2) {
          unlocks.append("The Cursed Village Thieves' Guild,");
        } else if (decision == 3) {
          unlocks.append("The Archwizard's Tower,");
        } else if (decision == 10) {
          CADatabase1200to1299.fantasyRealmParseBigRedButtonPress(request.responseText);
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1286, "Resting in Peace", "The Sprawling Cemetery") {
      void setup() {
        new Option(1, "unlock The Labyrinthine Crypt", true).turnCost(1);
        new Option(2, "unlock The Barrow Mounds", true).turnCost(1);
        new Option(3, "unlock Duke Vampire's Chateau").turnCost(1);
        new Option(4, "acquire 40-60 Rubees&trade;")
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(5, "acquire Chewsick Copperbottom's notes")
            .turnCost(1)
            .attachItem(ItemPool.FR_CHESWICKS_NOTES, 1, AUTO);
        new Option(10);
        new Option(11);

        new CustomOption(3, "unlock Duke Vampire's Chateau (with FantasyRealm Rogue's Mask)");
        new CustomOption(4, "acquire 40-60 Rubees&trade; (need LyleCo premium pickaxe)");
        new CustomOption(5, "acquire Chewsick Copperbottom's notes (with FantasyRealm Mage's Hat)");
        new CustomOption(10, "1/5 to fight Skeleton Lord (with FantasyRealm outfit)");
        new CustomOption(11, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        getOption(10).text(CADatabase1200to1299.fantasyRealmBigRedButtonSpoiler());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 10 && !request.responseText.contains("Just send everybody home")) {
          getOption(10).turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 11) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Sprawling Cemetery,", "");
        }
        if (decision == 1) {
          unlocks.append("The Labyrinthine Crypt,");
        } else if (decision == 2) {
          unlocks.append("The Barrow Mounds,");
        } else if (decision == 3) {
          unlocks.append("Duke Vampire's Chateau,");
        } else if (decision == 10) {
          CADatabase1200to1299.fantasyRealmParseBigRedButtonPress(request.responseText);
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1287, "What's Going On?", "A Monorail Station") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1288, "What's Yours is Yours", "The Old Rubee Mine") {
      void setup() {
        new Option(1, "acquire 20-30 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 20, AUTO);
        new Option(2, "acquire dragon aluminum ore")
            .turnCost(1)
            .attachItem(ItemPool.FR_DRAGON_ORE, 1, AUTO);
        new Option(3, "acquire grolblin rum", true).turnCost(1).attachItem("grolblin rum", 1, AUTO);
        new Option(6);

        new CustomOption(2, "acquire dragon aluminum ore (need LyleCo premium pickaxe)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Old Rubee Mine,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1289, "A Warm Place", "The Foreboding Cave") {
      void setup() {
        new Option(1, "acquire 90-110 Rubees&trade;")
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 90, AUTO);
        new Option(2, "acquire sachet of strange powder", true)
            .turnCost(1)
            .attachItem("sachet of strange powder", 1, AUTO);
        new Option(3, "unlock The Lair of the Phoenix").turnCost(1);
        new Option(6);

        new CustomOption(1, "acquire 90-110 Rubees&trade; (with FantasyRealm key)");
        new CustomOption(3, "unlock The Lair of the Phoenix (with FantasyRealm Mage's Hat)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Foreboding Cave,", "");
        }
        if (decision == 3) {
          unlocks.append("The Lair of the Phoenix,");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1290, "The Cyrkle Is Compleat", "The Faerie Cyrkle") {
      void setup() {
        new Option(1, "100 adv of Fantasy Faerie Blessing", true)
            .turnCost(1)
            .attachEffect("Fantasy Faerie Blessing");
        new Option(2, "acquire faerie dust", true).turnCost(1).attachItem("faerie dust", 1, AUTO);
        new Option(3, "The Spider Queen's Lair").turnCost(1);
        new Option(6);

        new CustomOption(3, "unlock The Spider Queen's Lair (with FantasyRealm Rogue's Mask)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Faerie Cyrkle,", "");
        }
        if (decision == 3) {
          unlocks.append("The Spider Queen's Lair,");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1291, "Dudes, Where's My Druids?", "The Druidic Campsite") {
      void setup() {
        new Option(1, "acquire druidic s'more", true)
            .turnCost(1)
            .attachItem("druidic s'more", 1, AUTO);
        new Option(2, "acquire poisoned druidic s'more")
            .turnCost(1)
            .attachItem(ItemPool.FR_POISONED_SMORE, 1, AUTO)
            .attachItem(ItemPool.FR_TAINTED_MARSHMALLOW, -1, MANUAL, new NoDisplay());
        new Option(3, "acquire druidic orb")
            .turnCost(1)
            .attachItem(ItemPool.FR_DRUIDIC_ORB, 1, AUTO);
        new Option(6);

        new CustomOption(2, "acquire poisoned druidic s'more (with tainted marshmallow)");
        new CustomOption(3, "acquire druidic orb (with FantasyRealm Mage's Hat)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Druidic Campsite,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1292, "Witch One You Want?", "Near the Witch's House") {
      void setup() {
        new Option(1, "50 adv of Brewed up").turnCost(1).attachEffect("Brewed up");
        new Option(2, "10 adv of Poison for Blood")
            .turnCost(1)
            .attachEffect("Poison for Blood")
            .attachItem(ItemPool.FR_PURPLE_MUSHROOM, -1, MANUAL, new NoDisplay());
        new Option(3, "acquire to-go brew", true).turnCost(1).attachItem("to-go brew", 1, AUTO);
        new Option(4, "acquire 40-60 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(6);

        new CustomOption(1, "50 adv of +200% init");
        new CustomOption(2, "10 adv of Poison for Blood (with plump purple mushroom)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "Near the Witch's House,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1293, "Altared States", "The Evil Cathedral") {
      void setup() {
        new Option(1, "acquire 20-30 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 20, AUTO);
        new Option(2, "100 adv of Fantastical Health")
            .turnCost(1)
            .attachEffect("Fantastical Health");
        new Option(3, "acquire sanctified cola", true)
            .turnCost(1)
            .attachItem("sanctified cola", 1, AUTO);
        new Option(4, "acquire flask of holy water")
            .turnCost(1)
            .attachItem(ItemPool.FR_HOLY_WATER, 1, AUTO);
        new Option(6);

        new CustomOption(2, "100 adv of +200% HP");
        new CustomOption(4, "acquire flask of holy water (with FantasyRealm Mage's Hat)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Evil Cathedral,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1294, "Neither a Barrower Nor a Lender Be", "The Barrow Mounds") {
      void setup() {
        new Option(1, "acquire 20-30 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 20, AUTO);
        new Option(2, "acquire mourning wine", true)
            .turnCost(1)
            .attachItem("mourning wine", 1, AUTO);
        new Option(3, "unlock The Ghoul King's Catacomb").turnCost(1);
        new Option(6);

        new CustomOption(3, "unlock The Ghoul King's Catacomb (with FantasyRealm Warrior's Helm)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Barrow Mounds,", "");
        }
        if (decision == 3) {
          unlocks.append("The Ghoul King's Catacomb,");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1295, "Honor Among You", "The Cursed Village Thieves' Guild") {
      void setup() {
        new Option(1, "acquire 40-60 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(2, "acquire universal antivenin", true)
            .turnCost(1)
            .attachItem("universal antivenin", 1, AUTO);
        new Option(6);

        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Cursed Village Thieves' Guild,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1296, "For Whom the Bell Trolls", "The Troll Fortress") {
      void setup() {
        new Option(1, "nothing happens / joke option");
        new Option(2, "acquire nasty haunch", true).turnCost(1).attachItem("nasty haunch", 1, AUTO);
        new Option(3, "acquire Cheswick Copperbottom's compass")
            .turnCost(1)
            .attachItem(ItemPool.FR_CHESWICKS_COMPASS, 1, AUTO)
            .attachItem(ItemPool.FR_CHESWICKS_NOTES, -1, MANUAL, new NoDisplay());
        new Option(4, "acquire 40-60 Rubees&trade;")
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(6);

        new CustomOption(
            3, "acquire Cheswick Copperbottom's compass (with Chewsick Copperbottom's notes)");
        new CustomOption(4, "acquire 40-60 Rubees&trade; (with LyleCo premium pickaxe)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Troll Fortress,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1297, "Stick to the Crypt", "The Labyrinthine Crypt") {
      void setup() {
        new Option(1, "acquire hero's skull", true).turnCost(1).attachItem("hero's skull", 1, AUTO);
        new Option(2, "acquire 40-60 Rubees&trade;", true)
            .turnCost(1)
            .attachItem(ItemPool.RUBEE, 40, AUTO);
        new Option(3, "acquire arrest warrant")
            .turnCost(1)
            .attachItem(ItemPool.FR_ARREST_WARRANT, 1, AUTO);
        new Option(6);

        new CustomOption(3, "acquire arrest warrant (with FantasyRealm Rogue's Mask)");
        new CustomOption(6, "leave");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision != 6) {
          Preferences.decrement("_frHoursLeft");
          StringUtilities.singleStringReplace(unlocks, "The Labyrinthine Crypt,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1298, "The \"Phoenix\"", "The Lair of the Phoenix") {
      void setup() {
        new Option(1, "fight \"Phoenix\"")
            .attachItem(ItemPool.FR_HOLY_WATER, -1, MANUAL, new NoDisplay());
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(1, "fight \"Phoenix\" (with 5+ hot res and flask of holy water)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Lair of the Phoenix,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };

    new ChoiceAdventure(1299, "Stop Dragon Your Feet", "The Dragon's Moor") {
      void setup() {
        new Option(1, "fight Sewage Treatment Dragon");
        new Option(2, "get beaten up", true);
        new Option(6, "leave", true);

        new CustomOption(
            1, "fight Sewage Treatment Dragon (with 5+ stench res and dragon slaying sword)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        StringBuffer unlocks = new StringBuffer(Preferences.getString("_frAreasUnlocked"));
        if (decision == 1) {
          StringUtilities.singleStringReplace(unlocks, "The Dragon's Moor,", "");
        }
        Preferences.setString("_frAreasUnlocked", unlocks.toString());
      }
    };
  }

  static final String fantasyRealmBigRedButtonSpoiler() {
    int presses = Preferences.getInteger("_frButtonsPressed");

    if (presses == 4) {
      return "fight Skeleton Lord";
    }

    // Have they gotten another non-combat already?
    int timeLeft = Preferences.getInteger("_frHoursLeft");
    if (presses + timeLeft < 5) {
      return "won't have time for this";
    }

    // Do they have all 5 areas unlocked?
    int possiblePresses = presses;

    String unlocks = Preferences.getString("_frAreasUnlocked");
    for (String area :
        new String[] {
          "The Towering Mountains",
          "The Mystic Wood",
          "The Putrid Swamp",
          "The Cursed Village",
          "The Sprawling Cemetery"
        }) {
      if (unlocks.contains(area)) {
        possiblePresses++;
      }
    }

    if (possiblePresses < 5) {
      return "won't have time for this";
    }

    return (1 + presses) + "/5 to fight Skeleton Lord";
  }

  static final void fantasyRealmParseBigRedButtonPress(String responseText) {
    if (responseText.contains("main quest progress dialog number one")) {
      Preferences.setInteger("_frButtonsPressed", 1);
    } else if (responseText.contains("It cuts off abruptly")) {
      Preferences.setInteger("_frButtonsPressed", 2);
    } else if (responseText.contains("You seem to be missing some context")) {
      Preferences.setInteger("_frButtonsPressed", 3);
    } else if (responseText.contains("another dumb damsel-in-distress story")) {
      Preferences.setInteger("_frButtonsPressed", 4);
    } else if (responseText.contains("Just send everybody home")) {
      Preferences.setInteger("_frButtonsPressed", 5);
    } else {
      Preferences.increment("_frButtonsPressed");
    }
  }
}
