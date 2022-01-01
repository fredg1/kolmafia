package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.FamiliarData;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.objectpool.FamiliarPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.objectpool.OutfitPool;
import net.sourceforge.kolmafia.persistence.AdventureDatabase;
import net.sourceforge.kolmafia.persistence.EquipmentDatabase;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.persistence.MonsterDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.CampgroundRequest;
import net.sourceforge.kolmafia.request.CampgroundRequest.Mushroom;
import net.sourceforge.kolmafia.request.CargoCultistShortsRequest;
import net.sourceforge.kolmafia.request.EquipmentRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.QuestLogRequest;
import net.sourceforge.kolmafia.request.ScrapheapRequest;
import net.sourceforge.kolmafia.request.WildfireCampRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase1400to1499 extends ChoiceAdventureDatabase {
  final void add1400to1499() {
    new UnknownChoiceAdventure(1400);

    new UnknownChoiceAdventure(1401);

    new UnknownChoiceAdventure(1402);

    new UnknownChoiceAdventure(1403);

    new UnknownChoiceAdventure(1404);

    new ChoiceAdventure(1405, "Let's, uh, go!", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1406, "Drippy House on the Prairie", "The Dripping Trees") {
      void setup() {
        new Option(1, "discover more options");
        new Option(2, "add 7 drippy bats to this zone").turnCost(1);
        new Option(3, "2-4 driplets").turnCost(1).attachItem(ItemPool.DRIPLET, 1, AUTO);
        new Option(4, "drippy plum(?)").turnCost(1).attachItem(ItemPool.DRIPPY_PLUM, 1, AUTO);
        new Option(5, "drippy truncheon -> drippy stake")
            .turnCost(1)
            .attachItem(
                ItemPool.DRIPPY_TRUNCHEON, new DisplayAll("truncheon", NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem("drippy stake", 1, AUTO);
        new Option(6, "fight The Thing in the Basement");
        new Option(9).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 5) {
          // You use the tools to carve your truncheon into a sharp stake.
          if (request.responseText.contains("sharp stake")) {
            EquipmentManager.discardEquipment(ItemPool.DRIPPY_TRUNCHEON);
          } else {
            this.choiceFailed();
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        // 1 = Explore the House
        // Even though the house doesn't have a door, you check under the mat for a key anyway.  You
        // don't find one, but you <i>do</i> find a little puddle of those Driplet things Jeremy
        // told you about.
        // 1 = Keep Exploring
        // Just inside the door of the house, you discover a colony of nasty bat-looking things
        // nesting in the rafters.
        // In one of the side rooms of the house, you find a giant spiral shell stuck to the wall.
        // You pry it loose -- Jeremy will probably want to see this.
        // In the house's kitchen, you find a bucket underneath a dripping sink pipe. The
        // oddly-solid drops make a -thunk-... -thunk-... -thunk-... sound as they fall.
        // In the backyard of the house, you notice a strange fruit tree you hadn't seen before.
        // It's maybe... plums? Kinda hard to tell, what with everything being made out of the same
        // gross stuff.
        // In one of the back rooms, you find a workbench covered in drippy woodworking supplies.
        // Underneath a shattered bed frame in one of the back rooms, you find a trap door in the
        // floor. You press your ear to it and hear some... Thing... slithering around underneath
        // it.
        // 2 = Dislodge some bats
        // You flush some of the vile bat-things out of the rafters and into the nearby forest.  No
        // way that'll come back to bite you in the ass!
        // 3 = Check the bucket under the sink
        // You look in the bucket and find a few Driplets.  You collect them and put the bucket back
        // under the sink.
        // 4 - Pick a nasty fruit
        // You pluck the least nasty-looking plum(?) from the tree and pocket it.
        // 5 - Check out the woodworking bench
        // You use the tools to carve your truncheon into a sharp stake.
        // 6 - Go down to the basement
        // 9 = Leave
        if (request.responseText.contains("vile bat-things")) {
          Preferences.increment("drippyBatsUnlocked", 7);
        }

        // Since this choice appears on a schedule - the 16th
        // adventure in The Dripping Trees and then every 15
        // turns thereafter - "fix" the adventure count as needed.
        int advs = Preferences.getInteger("drippingTreesAdventuresSinceAscension");
        if (advs < 16) {
          Preferences.setInteger("drippingTreesAdventuresSinceAscension", 16);
          advs = 16;
        }
        int mod = (advs - 1) % 15;
        if (mod != 0) {
          Preferences.increment("drippingTreesAdventuresSinceAscension", 15 - mod);
        }
      }
    };

    new ChoiceAdventure(1407, "Mushroom District Costume Shop", null) {
      final Pattern MUSHROOM_COSTUME_PATTERN =
          Pattern.compile(
              "<form.*?name=option value=(\\d).*?type=submit value=\"(.*?) Costume\".*?>(\\d+) coins<.*?</form>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = MUSHROOM_COSTUME_PATTERN.matcher(request.responseText);
        int cost = 0;
        boolean carpenter = false;
        boolean gardener = false;
        boolean ballerina = false;
        while (matcher.find()) {
          int optionIndex = StringUtilities.parseInt(matcher.group(1));
          cost = StringUtilities.parseInt(matcher.group(3));

          getOption(optionIndex).attachItem(ItemPool.COIN, -cost, MANUAL, new NoDisplay());

          String costume = matcher.group(2);
          if (costume.equals("Carpenter")) {
            carpenter = true;
          } else if (costume.equals("Gardener")) {
            gardener = true;
          } else if (costume.equals("Ballerina")) {
            ballerina = true;
          }
        }
        String wearing =
            !carpenter ? "muscle" : !gardener ? "mysticality" : !ballerina ? "moxie" : "none";
        Preferences.setInteger("plumberCostumeCost", cost);
        Preferences.setString("plumberCostumeWorn", wearing);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        String costume;
        if (decision == 1
            && request.responseText.contains("You slip into something a little more carpentable")) {
          costume = "muscle";
        } else if (decision == 2
            && request.responseText.contains(
                "You let down your guard and put on the gardener costume")) {
          costume = "mysticality";
        } else if (decision == 3
            && request.responseText.contains("Todge holds out a tutu and you jump into it")) {
          costume = "moxie";
        } else {
          if (decision != 6) {
            this.choiceFailed();
          }
          return;
        }

        Preferences.increment("plumberCostumeCost", 50);
        Preferences.setString("plumberCostumeWorn", costume);
      }
    };

    new ChoiceAdventure(1408, "Mushroom District Badge Shop", null) {
      final Pattern MUSHROOM_BADGE_PATTERN = Pattern.compile("Current cost: (\\d+) coins.");

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
        new Option(12);
        new Option(13);
        new Option(14);
        new Option(25);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = MUSHROOM_BADGE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          int cost = StringUtilities.parseInt(matcher.group(1));
          Preferences.setInteger("plumberBadgeCost", cost);

          for (int i = 1; i < 15; i++) {
            getOption(i).attachItem(ItemPool.COIN, -cost, MANUAL, new NoDisplay());
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision > 0 && decision < 15) {
          if (request.responseText.contains("You acquire a skill")) {
            Preferences.increment("plumberBadgeCost", 25);
          } else {
            this.choiceFailed();
          }
        }
      }
    };

    new ChoiceAdventure(1409, "Your Quest is Over", null) {
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

    new ChoiceAdventure(1410, "The Mushy Center", "Your Mushroom Garden") {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int mushroomLevel = 0;
        int mushroomYieldId = 0;

        if (request.responseText.contains("decent-sized mushroom")
            || request.responseText.contains("mushgrow1.gif")) {
          mushroomLevel = 1;
          mushroomYieldId = ItemPool.FREE_RANGE_MUSHROOM;
        } else if (request.responseText.contains("plump mushroom")
            || request.responseText.contains("mushgrow2.gif")) {
          mushroomLevel = 2;
          mushroomYieldId = ItemPool.PLUMP_FREE_RANGE_MUSHROOM;
        } else if (request.responseText.contains("bulky mushroom")
            || request.responseText.contains("mushgrow3.gif")) {
          mushroomLevel = 3;
          mushroomYieldId = ItemPool.BULKY_FREE_RANGE_MUSHROOM;
        } else if (request.responseText.contains("giant mushroom")
            || request.responseText.contains("mushgrow4.gif")) {
          mushroomLevel = 4;
          mushroomYieldId = ItemPool.GIANT_FREE_RANGE_MUSHROOM;
        } else if (request.responseText.contains("walk around inside")) {
          // The mushroom in your garden is now large enough to walk around inside.  Also there's a
          // door on it, which is convenient for that purpose.
          // mushgrow5.gif is used for both the immense and colossal mushroom
          mushroomLevel = 11;
          mushroomYieldId = ItemPool.COLOSSAL_FREE_RANGE_MUSHROOM;
        } else if (request.responseText.contains("immense mushroom")
            || request.responseText.contains("mushgrow5.gif")) {
          mushroomLevel = 5;
          mushroomYieldId = ItemPool.IMMENSE_FREE_RANGE_MUSHROOM;
        }

        if (mushroomYieldId != 0) {
          getOption(2).attachItem(mushroomYieldId, 1, AUTO);
        }

        // After level 5, you can continue fertilizing for an unknown number
        // of days before you get the ultimate mushroom.

        // If we have fertilized the garden through KoLmafia,
        // this is the number of days we have fertilized.
        int currentLevel = Preferences.getInteger("mushroomGardenCropLevel");

        // If we have not fertilized consistently through KoLmafia, correct it here
        int newLevel = Math.max(mushroomLevel, currentLevel);

        Preferences.setInteger("mushroomGardenCropLevel", newLevel);
        CampgroundRequest.clearCrop();
        CampgroundRequest.setCampgroundItem(new Mushroom(newLevel));
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        int mushroomLevel = 1; // Tomorrow's mushroom
        switch (decision) {
          case 1:
            // Fertilize the mushroom
            mushroomLevel = Preferences.increment("mushroomGardenCropLevel", 1, 11, false);
            break;
          case 2:
            // Pick the mushroom
            Preferences.setInteger("mushroomGardenCropLevel", 1);
            break;
        }
        CampgroundRequest.clearCrop();
        CampgroundRequest.setCampgroundItem(new Mushroom(mushroomLevel));
        Preferences.setBoolean("_mushroomGardenVisited", true);
      }
    };

    new ChoiceAdventure(1411, "The Hall in the Hall", "The Dripping Hall") {
      final AdventureResult DRIPPY_GRUB = new AdventureResult("drippy grub", 1, false);
      final AdventureResult DRIPPY_SEED = new AdventureResult("drippy seed", 1, false);
      final AdventureResult DRIPPY_BEZOAR = new AdventureResult("drippy bezoar", 1, false);
      final AdventureResult DRIPPY_STEIN = ItemPool.get(ItemPool.DRIPPY_STEIN, -1);

      void setup() {
        this.customName = this.name;

        new Option(1, "drippy pool table", true)
            .turnCost(1)
            .attachItem(
                ItemPool.DRIPPY_STAFF, 1, AUTO, new DisplayAll("don't have", WANT, EXACTLY, 0))
            .attachItem("drippy orb", 1, AUTO, new DisplayAll("drippy orb"));
        new Option(2).leadsTo(1415, false, o -> true, " or ");
        new Option(3, "drippy humanoid", true).turnCost(1);
        new Option(4, "Trade a drippy stein for a drippy pilsner", true)
            .turnCost(1)
            .attachItem(ItemPool.DRIPPY_PILSNER, 1, AUTO)
            .attachItem(DRIPPY_STEIN, MANUAL, new DisplayAll("stein", NEED, AT_LEAST, 1));
        new Option(5, "Get some Driplets", true).turnCost(1).attachItem(ItemPool.DRIPLET, 3, AUTO);

        new CustomOption(2, "drippy vending machine");
      }

      @Override
      void visitChoice(GenericRequest request) {
        int inebriety = KoLCharacter.getInebriety();
        int totalPoolSkill = KoLCharacter.estimatedPoolSkill();

        getOption(1)
            .text(
                "acquire drippy staff if don't have, also maybe a drippy orb (Pool Skill at "
                    + Integer.valueOf(inebriety)
                    + " inebriety = "
                    + Integer.valueOf(totalPoolSkill)
                    + ")");

        Option option = getOption(3);
        if (KoLCharacter.hasSkill("Drippy Eye-Sprout")) {
          option.attachItem(DRIPPY_SEED, AUTO);
        } else if (KoLCharacter.hasSkill("Drippy Eye-Stone")) {
          option.attachItem(DRIPPY_BEZOAR, AUTO);
        } else if (KoLCharacter.hasSkill("Drippy Eye-Beetle")) {
          option.attachItem(DRIPPY_GRUB, AUTO);
        } else {
          option.text("nothing");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 4 && !request.responseText.contains("drippy pilsner")) {
          // still costs a turn
          this.choiceFailed().turnCost(1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
            // If you acquire a drippy org, count it
            if (request.responseText.contains("drippy orb")) {
              Preferences.increment("drippyOrbsClaimed");
            } else {
              int known = Preferences.getInteger("drippyOrbsClaimed");
              int min = KoLCharacter.estimatedPoolSkill() / 20;
              Preferences.setInteger("drippyOrbsClaimed", Math.max(known, min));
            }
            // deliberate fall-through
          case 2:
          case 3:
          case 4:
            Preferences.setBoolean("_drippingHallDoor" + decision, true);
            break;
        }

        // This only advances upon defeating a dripping reveler
        // or encountering this choice
        int advs = Preferences.increment("drippingHallAdventuresSinceAscension");

        // Since this choice appears on a schedule - the 12th
        // adventure in The Dripping Hall and then every 12
        // turns thereafter - "fix" the adventure count as needed.
        if (advs < 12) {
          Preferences.setInteger("drippingHallAdventuresSinceAscension", 12);
          advs = 12;
        }
        int mod = advs % 12;
        if (mod != 0) {
          Preferences.increment("drippingHallAdventuresSinceAscension", 12 - mod);
        }
      }
    };

    new ChoiceAdventure(1412, "Guzzlr Client Selection", "Item-Driven") {
      final Pattern DESCID_PATTERN = Pattern.compile("descitem\\((.*?)\\)");
      final Pattern GUZZLR_TIER_PATTERN =
          Pattern.compile("You have completed ([0-9,]+) (Bronze|Gold|Platinum) Tier deliveries");
      final Pattern GUZZLR_QUEST_PATTERN =
          Pattern.compile(
              "<p>You are currently tasked with taking a (.*?) to (.*?) in (.*?)\\.<p>");
      // You select a Gold Tier client, Norma "Smelly" Jackson, a hobo from The Oasis.
      final Pattern GUZZLR_LOCATION_PATTERN =
          Pattern.compile(
              "You select a (Bronze|Gold|Platinum) Tier client, (.*?), +an? (.*?) from (.*?)\\.<p>");

      void setup() {
        new Option(1); // abandon
        new Option(2); // bronze
        new Option(3); // gold
        new Option(4); // platinum
        new Option(5); // leave
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher tierMatcher = GUZZLR_TIER_PATTERN.matcher(request.responseText);
        while (tierMatcher.find()) {
          Preferences.setInteger(
              "guzzlr" + tierMatcher.group(2) + "Deliveries",
              StringUtilities.parseInt(tierMatcher.group(1)));
        }

        if (request.responseText.contains("You are currently tasked with")) {
          Matcher alreadyMatcher = GUZZLR_QUEST_PATTERN.matcher(request.responseText);
          if (alreadyMatcher.find()) {
            String booze = alreadyMatcher.group(1);
            Preferences.setString(
                "guzzlrQuestBooze",
                booze.equals("special personalized cocktail") ? "Guzzlr cocktail set" : booze);
            Preferences.setString("guzzlrQuestClient", alreadyMatcher.group(2));
            Preferences.setString("guzzlrQuestLocation", alreadyMatcher.group(3));
          }

          int itemId = ItemDatabase.getItemId(Preferences.getString("guzzlrQuestBooze"));

          if (itemId > 0) {
            if (InventoryManager.getCount(itemId) > 0) {
              QuestDatabase.setQuestProgress(Quest.GUZZLR, "step1");
            } else {
              QuestDatabase.setQuestProgress(Quest.GUZZLR, QuestDatabase.STARTED);
            }
          } else {
            QuestDatabase.setQuestProgress(Quest.GUZZLR, QuestDatabase.STARTED);
          }

          Preferences.setBoolean(
              "_guzzlrQuestAbandoned",
              ChoiceManager.findChoiceDecisionIndex("Abandon Client", request.responseText).equals("0"));

          return;
        }

        // If we have unlocked Gold Tier but cannot accept one, we must have already accepted three.
        boolean unlockedGoldTier = Preferences.getInteger("guzzlrBronzeDeliveries") >= 5;
        if (unlockedGoldTier
            && ChoiceManager.findChoiceDecisionIndex("Gold Tier", request.responseText).equals("0")) {
          Preferences.setInteger("_guzzlrGoldDeliveries", 3);
        }

        // If we have unlocked Platinum Tier but cannot accept one, we must have already accepted
        // one.
        boolean unlockedPlatinumTier = Preferences.getInteger("guzzlrGoldDeliveries") >= 5;
        if (unlockedPlatinumTier
            && ChoiceManager.findChoiceDecisionIndex("Platinum Tier", request.responseText)
                .equals("0")) {
          Preferences.setInteger("_guzzlrPlatinumDeliveries", 1);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
            // Abandon
            Preferences.setBoolean("_guzzlrQuestAbandoned", true);

            Preferences.setString("guzzlrQuestBooze", "");
            Preferences.setString("guzzlrQuestClient", "");
            Preferences.setString("guzzlrQuestLocation", "");
            Preferences.setString("guzzlrQuestTier", "");
            QuestDatabase.setQuestProgress(Quest.GUZZLR, QuestDatabase.UNSTARTED);
            break;
          case 2:
          case 3:
          case 4:
            Matcher locationMatcher = GUZZLR_LOCATION_PATTERN.matcher(request.responseText);
            Matcher boozeMatcher = DESCID_PATTERN.matcher(request.responseText);

            String tier = "";

            if (locationMatcher.find()) {
              tier = locationMatcher.group(1).toLowerCase();
              Preferences.setString("guzzlrQuestClient", locationMatcher.group(2));
              Preferences.setString("guzzlrQuestLocation", locationMatcher.group(4));
            }

            // If we didn't capture from the output we can determine from the choice
            if (tier.equals("")) {
              tier = decision == 2 ? "bronze" : decision == 3 ? "gold" : "platinum";
            }

            // Remember the tier of the current quest
            Preferences.setString("guzzlrQuestTier", tier);

            // Increment the number of gold or platinum deliveries STARTED today
            if (!tier.equals("bronze")) {
              Preferences.increment(
                  "_guzzlr" + StringUtilities.toTitleCase(tier) + "Deliveries", 1);
            }

            if (boozeMatcher.find()) {
              int itemId = ItemDatabase.getItemIdFromDescription(boozeMatcher.group(1));
              Preferences.setString("guzzlrQuestBooze", ItemDatabase.getItemName(itemId));
            }

            if (Preferences.getString("guzzlrQuestBooze").isEmpty()
                || Preferences.getString("guzzlrQuestLocation").isEmpty()) {
              RequestThread.postRequest(new QuestLogRequest());
            }

            int itemId = ItemDatabase.getItemId(Preferences.getString("guzzlrQuestBooze"));

            if (itemId > 0 && itemId != ItemPool.GUZZLR_COCKTAIL_SET) {
              if (InventoryManager.getCount(itemId) > 0) {
                QuestDatabase.setQuestProgress(Quest.GUZZLR, "step1");
              } else {
                QuestDatabase.setQuestProgress(Quest.GUZZLR, QuestDatabase.STARTED);
              }
            } else {
              QuestDatabase.setQuestProgress(Quest.GUZZLR, QuestDatabase.STARTED);
            }
            break;
        }
      }
    };

    new ChoiceAdventure(1413, "Low-Key Summer", null) {
      void setup() {
        new Option(1, "LIES!!!");
      }
    };

    new ChoiceAdventure(1414, "Lock Picking", null) {
      void setup() {
        new Option(1).attachItem(ItemPool.BORIS_KEY, 1, AUTO);
        new Option(2).attachItem(ItemPool.JARLSBERG_KEY, 1, AUTO);
        new Option(3).attachItem(ItemPool.SNEAKY_PETE_KEY, 1, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setBoolean("lockPicked", true);
      }
    };

    new ChoiceAdventure(1415, "Revolting Vending", "The Dripping Hall") {
      void setup() {
        this.customName = this.name;

        new Option(1, "drippy candy bar (costs 10,000 meat)", true)
            .turnCost(1)
            .attachMeat(-10000, MANUAL)
            .attachItem("drippy candy bar", 1, AUTO);
        new Option(2, "Driplets", true).turnCost(1).attachItem(ItemPool.DRIPLET, 3, AUTO);
      }
    };

    new UnknownChoiceAdventure(1416);

    new UnknownChoiceAdventure(1417);

    new ChoiceAdventure(1418, "So Cold", "Item-Driven") {
      void setup() {
        new Option(
                1,
                "10 adv of Entauntauned (+1 cold resist per Melodramedary weight), resets Melodramedary's xp to 0")
            .attachEffect("Entauntauned", new ImageOnly("Entauntauned"));
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          KoLCharacter.findFamiliar(FamiliarPool.MELODRAMEDARY).loseExperience();
          Preferences.setBoolean("_entauntaunedToday", true);
        }
      }
    };

    new ChoiceAdventure(1419, "Grey Sky Morning", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1420, "What has it got in its pocketses?", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        CargoCultistShortsRequest.parseAvailablePockets(request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          CargoCultistShortsRequest.parsePocketPick(request.getURLString(), request.responseText);
        }
      }
    };

    new UnknownChoiceAdventure(1421);

    new UnknownChoiceAdventure(1422);

    new UnknownChoiceAdventure(1423);

    new UnknownChoiceAdventure(1424);

    new ChoiceAdventure(1425, "Oh Yeah!", "Frat House") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Frat House Cartography";

        new Option(1)
            .turnCost(1)
            .attachItem("Orcish baseball cap", 1, AUTO)
            .attachItem("Orcish cargo shorts", 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem("Orcish frat-paddle", 1, AUTO)
            .attachItem("Orcish cargo shorts", 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem("Orcish frat-paddle", 1, AUTO)
            .attachItem("Orcish baseball cap", 1, AUTO);
        new Option(4, "fight eXtreme Sports Orcs", true);

        new CustomOption(1, "hat + shorts");
        new CustomOption(2, "shorts + paddle");
        new CustomOption(3, "hat + paddle");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new UnknownChoiceAdventure(1426);

    new ChoiceAdventure(1427, "The Hidden Junction", "Guano Junction") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Guano Junction Cartography";

        new Option(1, "fight screambat", true);
        new Option(2, "gain ~360 meat", true).turnCost(1).attachMeat(300, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1428, "Your Neck of the Woods", "The Dark Neck of the Woods") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Neck of the Woods Cartography";

        new Option(1, "advance quest 1 step and gain 1000 meat", true)
            .turnCost(1)
            .attachMeat(1000, AUTO);
        new Option(2, "advance quest 2 steps", true).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1429, "No Nook Unknown", "The Defiled Nook") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Defiled Nook Cartography";

        new Option(1, "acquire 2 evil eyes (0)", true).attachItem(ItemPool.EVIL_EYE, 2, AUTO);
        new Option(2, "fight party skeleton (1)", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1430, "Ghostly Memories", "A-Boo Peak") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "A-Boo Peak Cartography";

        new Option(1, "the Horror, spooky/cold res recommended", true).leadsTo(611);
        new Option(2, "fight oil baron", true);
        new Option(3, "lost overlook lodge", true).leadsTo(606, true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(
        1431, "Here There Be Giants", "The Castle in the Clouds in the Sky (Top Floor)") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Top Floor Cartography";

        new Option(1).leadsTo(677, true, o -> true);
        new Option(2).leadsTo(675, true, o -> true);
        new Option(3).leadsTo(676, true, o -> true);
        new Option(4).leadsTo(678, true, o -> true);

        new CustomOption(1, "Top Steampunk");
        new CustomOption(2, "Top Goth");
        new CustomOption(3, "Top Raver");
        new CustomOption(4, "Top Punk Rock");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1432, "Mob Maptality", "A Mob of Zeppelin Protesters") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Protesters Cartography";

        new Option(1).leadsTo(857, true, o -> o.index == 1);
        new Option(2).leadsTo(856, true, o -> o.index == 1);
        new Option(3).leadsTo(858, false, o -> o.index == 1);

        new CustomOption(1, "creep protestors (more with sleaze damage/sleaze spell damage)");
        new CustomOption(2, "scare protestors (more with lynyrd gear)");
        new CustomOption(3, "set fire to protestors (more with Flamin' Whatshisname)");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1433, "Sneaky, Sneaky", "Wartime Hippy Camp (Frat Disguise)") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "War Hippy Camp Cartography";

        new Option(1).leadsTo(139, false, o -> true);
        new Option(2).leadsTo(140, false, o -> true);
        new Option(3);

        new CustomOption(1, "get muscle/get ferret bait/fight a war hippy (space) cadet");
        new CustomOption(2, "get water pipe bombs/get moxie/fight a war hippy drill sergeant");
        new CustomOption(3, "get myst/get items/start the war");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(3);
        if (EquipmentDatabase.getOutfit(OutfitPool.WAR_FRAT_OUTFIT).isWearing()) {
          option.leadsTo(142, false, o -> true);
        } else {
          option.leadsTo(141, false, o -> true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1434, "Sneaky, Sneaky", "Wartime Frat House (Hippy Disguise)") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "War Frat House Cartography";

        new Option(1).leadsTo(143, false, o -> true);
        new Option(2);
        new Option(3).leadsTo(144, false, o -> true);

        new CustomOption(1, "get muscle/get sake bombs/fight a war pledge");
        new CustomOption(2, "get muscle/get items/start the war");
        new CustomOption(3, "get moxie/get beer bombs/fight a frat warrior drill sergeant");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(2);
        if (EquipmentDatabase.getOutfit(OutfitPool.WAR_HIPPY_OUTFIT).isWearing()) {
          option.leadsTo(146, false, o -> true);
        } else {
          option.leadsTo(145, false, o -> true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(1435, "Leading Yourself Right to Them", "undefined") {
      final Pattern MAPPED_MONSTER_PATTERN =
          Pattern.compile(
              "(<input type=\"hidden\" name=\"heyscriptswhatsupwinkwink\" value=\"(\\d+)\" />\\s+<input type=\"submit\" class=\"button\" value=\").*?(\" />\\s+</form>)");

      void setup() {
        this.isSuperlikely = true;

        new Option(1);
        new Option(2).turnCost(1);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        Matcher matcher = MAPPED_MONSTER_PATTERN.matcher(buffer.toString());
        while (matcher.find()) {
          String find = matcher.group(0);
          Integer monsterId = Integer.parseInt(matcher.group(2));
          String monsterName = MonsterDatabase.getMonsterName(monsterId);

          String replace = matcher.group(1) + monsterName + matcher.group(3);
          StringUtilities.singleStringReplace(buffer, find, replace);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("mappingMonsters", false);
        }
      }
    };

    new ChoiceAdventure(1436, "Billiards Room Options", "The Haunted Billiards Room") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Billiards Room Cartography";

        new Option(1, "get the pool cue", true).turnCost(1).attachItem(ItemPool.POOL_CUE, 1, AUTO);
        new Option(2, "go straight to the ghost", true).leadsTo(875);
        new Option(3, "fight a chalkdust wraith", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase1400to1499.cartographyAdventureEncountered(this.source);
      }
    };

    new ChoiceAdventure(
        1437, "Setup Your knock-off retro superhero cape", "Item-Driven") { // washing instructions
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
      void postChoice2(GenericRequest request, int decision) {
        String instructions =
            decision == 2
                ? "hold"
                : decision == 3 ? "thrill" : decision == 4 ? "kiss" : decision == 5 ? "kill" : null;
        if (instructions != null) {
          Preferences.setString("retroCapeWashingInstructions", instructions);
          ItemDatabase.setCapeSkills();
        }
      }
    };

    new ChoiceAdventure(
        1438, "Setup Your knock-off retro superhero cape", "Item-Driven") { // superhero
      void setup() {
        this.canWalkFromChoice = true;

        // leadsTo doesn't help since it won't know
        // the superhero the cape would have
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String hero =
            decision == 1 ? "vampire" : decision == 2 ? "heck" : decision == 3 ? "robot" : null;
        if (hero != null) {
          Preferences.setString("retroCapeSuperhero", hero);
          ItemDatabase.setCapeSkills();
        }
      }
    };

    new RetiredChoiceAdventure(1439, "Spread Crimbo Spirit", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new RetiredChoiceAdventure(1440, "A House", null) {
      void setup() {
        new Option(1).leadsTo(1441);
        new Option(2).leadsTo(1441);
        new Option(3).leadsTo(1441);
        new Option(4).leadsTo(1441);
      }
    };

    new RetiredChoiceAdventure(1441, "An Ulterior Motive", null) {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
      }
    };

    new ChoiceAdventure(1442, "Shipping Government Food", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).attachItem(ItemPool.GOVERNMENT_FOOD_SHIPMENT, -1, MANUAL, new NoDisplay());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You fill out all the appropriate forms")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1443, "Shipping Government Booze", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).attachItem(ItemPool.GOVERNMENT_BOOZE_SHIPMENT, -1, MANUAL, new NoDisplay());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You fill out all the appropriate forms")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1444, "Shipping Government Candy", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).attachItem(ItemPool.GOVERNMENT_CANDY_SHIPMENT, -1, MANUAL, new NoDisplay());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("You fill out all the appropriate forms")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1445, "Reassembly Station", null) {
      final Pattern UPDATED_PART_PATTERN = Pattern.compile("part=([^&]*)");
      final Pattern CHOSEN_PART_PATTERN = Pattern.compile("p=([^&]*)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // change parts
        new Option(2); // get new cpus
      }

      @Override
      void visitChoice(GenericRequest request) {
        ScrapheapRequest.parseConfiguration(request.responseText);

        if (request.getURLString().contains("show=cpus")) {
          ScrapheapRequest.parseCPUUpgrades(request.responseText);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // KoL may have unequipped some items based on our selection
        Matcher partMatcher = UPDATED_PART_PATTERN.matcher(request.getURLString());
        Matcher chosenPartMatcher = CHOSEN_PART_PATTERN.matcher(request.getURLString());
        String part = partMatcher.find() ? partMatcher.group(1) : null;
        int chosenPart =
            chosenPartMatcher.find() ? StringUtilities.parseInt(chosenPartMatcher.group(1)) : 0;

        if (part != null && !part.equals("cpus") && chosenPart != 0) {
          // If we have set our "top" to anything other than 2, we now have no familiar
          if (part.equals("top") && chosenPart != 2) {
            KoLCharacter.setFamiliar(FamiliarData.NO_FAMILIAR);
          }

          // If we've set any part of the main body to anything other than 4, we are now missing an
          // equip
          if (chosenPart != 4) {
            int slot = -1;

            switch (part) {
              case "top":
                slot = EquipmentManager.HAT;
                break;
              case "right":
                slot = EquipmentManager.OFFHAND;
                break;
              case "bottom":
                slot = EquipmentManager.PANTS;
                break;
              case "left":
                slot = EquipmentManager.WEAPON;
                break;
            }

            if (slot != -1) {
              EquipmentManager.setEquipment(slot, EquipmentRequest.UNEQUIP);
            }
          }
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        this.visitChoice(request);

        KoLCharacter.updateStatus();
      }
    };

    new ChoiceAdventure(1446, "You, Robot", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1447, "Statbot 5000", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        ScrapheapRequest.parseStatbotCost(request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        KoLCharacter.updateStatus();
      }
    };

    new ChoiceAdventure(1448, "Potted Power Plant", "Item-Driven") {
      // <button type="submit" name="pp" value="6" (...)> <img (...)
      // src="https://s3.amazonaws.com/images.kingdomofloathing.com/otherimages/powerplant/2.png"
      // alt="battery (AA)" title="battery (AA)"> </button>
      final Pattern STALK_PATTERN =
          Pattern.compile(
              "<button.*?name=\"pp\" value=\"(\\d+)\".*?>\\s+<img.*?src=\".*?/otherimages/powerplant/(\\d+)\\.png\"");
      final Pattern CHOSEN_STALK_PATTERN = Pattern.compile("pp=(\\d+)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String[] stalkStatus = new String[7];

        Matcher stalks = STALK_PATTERN.matcher(request.responseText);
        while (stalks.find()) {
          int pp = Integer.parseInt(stalks.group(1));
          stalkStatus[pp - 1] = stalks.group(2);
        }

        Preferences.setString("_pottedPowerPlant", String.join(",", stalkStatus));
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("You acquire an item:")) {
          Matcher stalkMatcher = CHOSEN_STALK_PATTERN.matcher(request.getURLString());
          if (stalkMatcher.find()) {
            String[] stalkStatus = Preferences.getString("_pottedPowerPlant").split(",");
            int pp = Integer.parseInt(stalkMatcher.group(1));

            stalkStatus[pp - 1] = "0";
            Preferences.setString("_pottedPowerPlant", String.join(",", stalkStatus));
          }
        }
      }
    };

    new ChoiceAdventure(1449, "Set Backup Camera Mode", "Item-Driven") {
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
        String setting =
            !request.responseText.contains("Warning Beep")
                ? "ml"
                : !request.responseText.contains("Infrared Spectrum")
                    ? "meat"
                    : !request.responseText.contains("Maximum Framerate") ? "init" : "";

        Preferences.setString("backupCameraMode", setting);
        Preferences.setBoolean(
            "backupCameraReverserEnabled", request.responseText.contains("Disable Reverser"));
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
            Preferences.setString("backupCameraMode", "ml");
            reequip();
            break;
          case 2:
            Preferences.setString("backupCameraMode", "meat");
            reequip();
            break;
          case 3:
            Preferences.setString("backupCameraMode", "init");
            reequip();
            break;
          case 4:
            Preferences.setBoolean("backupCameraReverserEnabled", true);
            break;
          case 5:
            Preferences.setBoolean("backupCameraReverserEnabled", false);
            break;
        }
      }

      void reequip() {
        // If you change the mode with the item equipped, you need to un-equip and re-equip it to
        // get the modifiers
        for (int i = EquipmentManager.ACCESSORY1; i <= EquipmentManager.ACCESSORY3; ++i) {
          AdventureResult item = EquipmentManager.getEquipment(i);
          if (item != null && item.getItemId() == ItemPool.BACKUP_CAMERA) {
            RequestThread.postRequest(new EquipmentRequest(EquipmentRequest.UNEQUIP, i));
            RequestThread.postRequest(new EquipmentRequest(item, i));
          }
        }
      }
    };

    new ChoiceAdventure(1450, "Wildfire!", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1451, "Fire Captain Hagnk", null) {
      final Pattern LOCATION_ID_PATTERN = Pattern.compile("zid=([^&]*)");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        WildfireCampRequest.parseCaptain(request.responseText);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          Matcher locationIdMatcher = LOCATION_ID_PATTERN.matcher(request.getURLString());
          if (locationIdMatcher.find()) {
            int zid = StringUtilities.parseInt(locationIdMatcher.group(1));
            KoLAdventure location =
                AdventureDatabase.getAdventureByURL("adventure.php?snarfblat=" + zid);
            WildfireCampRequest.reduceFireLevel(location);
          } else if (ChoiceManager.lastDecision == 3) {
            if (request.responseText.contains("Hagnk takes your fire extinguisher")) {
              Preferences.setInteger("_fireExtinguisherCharge", 100);
              Preferences.setBoolean("_fireExtinguisherRefilled", true);
            }
          }
        }
        WildfireCampRequest.parseCaptain(request.responseText);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3 && request.responseText.contains("Hagnk takes your fire extinguisher")) {
          Preferences.setInteger("_fireExtinguisherCharge", 100);
          Preferences.setBoolean("_fireExtinguisherRefilled", true);
        }
      }
    };

    new ChoiceAdventure(1452, "Sprinkler Joe", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("Thanks again for your help!")) {
          Preferences.setBoolean("wildfireSprinkled", true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("raindrop.gif")) {
          Preferences.setBoolean("wildfireSprinkled", true);
          WildfireCampRequest.refresh();
        }
      }
    };

    new ChoiceAdventure(1453, "Fracker Dan", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("Thanks again for your help!")) {
          Preferences.setBoolean("wildfireFracked", true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("raindrop.gif")) {
          Preferences.setBoolean("wildfireFracked", true);
          WildfireCampRequest.refresh();
        }
      }
    };

    new ChoiceAdventure(1454, "Cropduster Dusty", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("Thanks again for your help!")) {
          Preferences.setBoolean("wildfireDusted", true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("raindrop.gif")) {
          Preferences.setBoolean("wildfireDusted", true);
          WildfireCampRequest.refresh();
        }
      }
    };

    new ChoiceAdventure(1455, "Cold Medicine Cabinet", "Item-Driven") {
      final Pattern CONSULTATIONS_PATTERN = Pattern.compile("You have <b>(\\d)</b> consul");

      void setup() {
        this.canWalkFromChoice = true;

        // TODO complete
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        CampgroundRequest.setCurrentWorkshedItem(ItemPool.COLD_MEDICINE_CABINET);

        Matcher consultations = CONSULTATIONS_PATTERN.matcher(request.responseText);
        if (consultations.find()) {
          int remaining = Integer.parseInt(consultations.group(1));
          Preferences.setInteger("_coldMedicineConsults", 5 - remaining);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision != 6) {
          Preferences.increment("_coldMedicineConsults", 1, 5, false);
          Preferences.setInteger("_nextColdMedicineConsult", KoLCharacter.getTurnsPlayed() + 20);
        }
      }
    };

    new ChoiceAdventure(1456, "Experimental Sauna", null) {
      void setup() {
        new Option(1, "50 advs of Sauna-Fresh").turnCost(1).attachEffect("Sauna-Fresh");
        new Option(2);
      }
    };

    new ChoiceAdventure(1457, "Food Lab", null) {
      void setup() {
        new Option(1)
            .attachItem("[experimental crimbo food]", 1, AUTO)
            .attachItem(ItemPool.GOOIFIED_ANIMAL_MATTER, -5, MANUAL);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1458, "Nog Lab", null) {
      void setup() {
        new Option(1)
            .attachItem("[experimental crimbo booze]", 1, AUTO)
            .attachItem(ItemPool.GOOIFIED_VEGETABLE_MATTER, -5, MANUAL);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1459, "Chem Lab", null) {
      void setup() {
        new Option(1)
            .attachItem("[experimental crimbo spleen]", 1, AUTO)
            .attachItem(ItemPool.GOOIFIED_MINERAL_MATTER, -5, MANUAL);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1460, "Gift Fabrication Lab", null) {
      void setup() {
        this.canWalkFromChoice = true;

        this.customName = "Site Alpha Toy Lab";
        this.customZones.add("Crimbo21");

        new Option(1, "fleshy putty, third ear or festive egg sac", true)
            .attachItem("fleshy putty", 1, AUTO, new DisplayAll("putty"))
            .attachItem("third ear", 1, AUTO, new DisplayAll("third ear"))
            .attachItem("festive egg sac", 1, AUTO, new DisplayAll("egg sac"))
            .attachItem(ItemPool.GOOIFIED_ANIMAL_MATTER, -30, MANUAL, new NoDisplay());
        new Option(2, "poisonsettia, peppermint-scented socks or the Crymbich Manuscript", true)
            .attachItem("poisonsettia", 1, AUTO, new DisplayAll("poisonsettia"))
            .attachItem("peppermint-scented socks", 1, AUTO, new DisplayAll("socks"))
            .attachItem("the Crymbich Manuscript", 1, AUTO, new DisplayAll("Manuscript"))
            .attachItem(ItemPool.GOOIFIED_VEGETABLE_MATTER, -30, MANUAL, new NoDisplay());
        new Option(3, "projectile chemistry set, depleted Crimbonium football helmet or synthetic rock", true)
            .attachItem("projectile chemistry set", 1, AUTO, new DisplayAll("chemistry set"))
            .attachItem("depleted Crimbonium football helmet", 1, AUTO, new DisplayAll("helmet"))
            .attachItem("synthetic rock", 1, AUTO, new DisplayAll("synthetic rock"))
            .attachItem(ItemPool.GOOIFIED_MINERAL_MATTER, -30, MANUAL, new NoDisplay());
        new Option(4, "&quot;caramel&quot; orange, self-repairing earmuffs or carnivorous potted plant", true)
            .attachItem("&quot;caramel&quot; orange", 1, AUTO, new DisplayAll("orange"))
            .attachItem("self-repairing earmuffs", 1, AUTO, new DisplayAll("earmuffs"))
            .attachItem("carnivorous potted plant", 1, AUTO, new DisplayAll("potted plant"))
            .attachItem(ItemPool.GOOIFIED_ANIMAL_MATTER, -15, MANUAL, new NoDisplay())
            .attachItem(ItemPool.GOOIFIED_VEGETABLE_MATTER, -15, MANUAL, new NoDisplay());
        new Option(5, "universal biscuit, yule hatchet or potato alarm clock", true)
            .attachItem("universal biscuit", 1, AUTO, new DisplayAll("biscuit"))
            .attachItem("yule hatchet", 1, AUTO, new DisplayAll("hatchet"))
            .attachItem("potato alarm clock", 1, AUTO, new DisplayAll("clock"))
            .attachItem(ItemPool.GOOIFIED_VEGETABLE_MATTER, -15, MANUAL, new NoDisplay())
            .attachItem(ItemPool.GOOIFIED_MINERAL_MATTER, -15, MANUAL, new NoDisplay());
        new Option(6, "lab-grown meat, golden fleece or boxed gumball machine", true)
            .attachItem("lab-grown meat", 1, AUTO, new DisplayAll("meat"))
            .attachItem("golden fleece", 1, AUTO, new DisplayAll("fleece"))
            .attachItem("boxed gumball machine", 1, AUTO, new DisplayAll("machine"))
            .attachItem(ItemPool.GOOIFIED_MINERAL_MATTER, -15, MANUAL, new NoDisplay())
            .attachItem(ItemPool.GOOIFIED_ANIMAL_MATTER, -15, MANUAL, new NoDisplay());
        new Option(7, "cloning kit, electric pants or can of mixed everything", true)
            .attachItem("cloning kit", 1, AUTO, new DisplayAll("cloning kit"))
            .attachItem("electric pants", 1, AUTO, new DisplayAll("pants"))
            .attachItem("can of mixed everything", 1, AUTO, new DisplayAll("everything"))
            .attachItem(ItemPool.GOOIFIED_ANIMAL_MATTER, -10, MANUAL, new NoDisplay())
            .attachItem(ItemPool.GOOIFIED_VEGETABLE_MATTER, -10, MANUAL, new NoDisplay())
            .attachItem(ItemPool.GOOIFIED_MINERAL_MATTER, -10, MANUAL, new NoDisplay());
        new Option(8, "return to Site Alpha", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision != 8 && !request.responseText.contains("You acquire an item")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(1461, "Hello Knob My Old Friend", "Site Alpha Primary Lab") {
      void setup() {
        this.customZones.add("Crimbo21");

        new Option(1, "Increase goo intensity", true);
        new Option(2, "Decrease goo intensity", true);
        new Option(3, "Trade grey goo ring for gooified matter", true)
            .attachItem(ItemPool.GREY_GOO_RING, -1, MANUAL, new DisplayAll("goo ring", NEED, INV_ONLY_AT_LEAST, 1));
        new Option(4, "Do nothing", true);
        new Option(5, "Grab the cheer core. Just do it!", true);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If you can "Grab the Cheer Core!", do it.
        if (responseText.contains("Grab the Cheer Core!")) {
          return "5";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.increment("primaryLabGooIntensity", 1);
        } else if (decision == 2) {
          Preferences.decrement("primaryLabGooIntensity", 1);
        } else if (decision == 5) {
          Preferences.setBoolean("primaryLabCheerCoreGrabbed", true);
        }
      }
    };
  }

  static final void cartographyAdventureEncountered(KoLAdventure location) {
    String encounters = Preferences.getString("cartographyAdventuresEncountered");

    if (!encounters.equals("")) {
      encounters += ",";
    }

    encounters += location.getAdventureName();

    Preferences.setString("cartographyAdventuresEncountered", encounters);
  }
}
