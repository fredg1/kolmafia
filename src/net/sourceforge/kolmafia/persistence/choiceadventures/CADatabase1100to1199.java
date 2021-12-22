package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.VYKEACompanionData;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.ConcoctionDatabase;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.ApiRequest;
import net.sourceforge.kolmafia.request.EatItemRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.BatManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EncounterManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.Limitmode;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase1100to1199 extends ChoiceAdventureDatabase {
  final void add1100to1199() {
    new ChoiceAdventure(1100, "Pray to the Barrel God", null) {
      void setup() {
        this.canWalkFromChoice = true;

        // no need to show anything, since KoL already puts
        // links to the items/effects' description in the choice
        new Option(1).attachItem(ItemPool.BARREL_LID, 1, AUTO, new NoDisplay());
        new Option(2).attachItem(ItemPool.BARREL_HOOP_EARRING, 1, AUTO, new NoDisplay());
        new Option(3).attachItem(ItemPool.BANKRUPTCY_BARREL, 1, AUTO, new NoDisplay());
        new Option(4);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("You already prayed to the Barrel god today")) {
          Preferences.setBoolean("_barrelPrayer", true);
          return;
        }
        if (!request.responseText.contains("barrel lid shield")) {
          Preferences.setBoolean("prayedForProtection", true);
        }
        if (!request.responseText.contains("barrel hoop earring")) {
          Preferences.setBoolean("prayedForGlamour", true);
        }
        if (!request.responseText.contains("bankruptcy barrel")) {
          Preferences.setBoolean("prayedForVigor", true);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision <= 4) {
          Preferences.setBoolean("_barrelPrayer", true);
          ConcoctionDatabase.refreshConcoctions();
        }
      }
    };

    new ChoiceAdventure(1101, "It's a Barrel Smashing Party!", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        if (decision == 1) {
          int itemId = ChoiceManager.extractIidFromURL(urlString);
          String name = ItemDatabase.getItemName(itemId);
          if (name != null) {
            RequestLogger.updateSessionLog("smash " + name);
            getOption(decision).attachItem(itemId, -1, MANUAL);
            return true;
          }
        } else if (decision == 2) {
          // We're smashing 100 barrels
          // The results don't say which barrels are being smashed, but it seems to happen in item
          // order
          int count = 100;
          int itemId = ItemPool.LITTLE_FIRKIN;
          RequestLogger.updateSessionLog("smashing 100 barrels");
          while (count > 0 && itemId <= ItemPool.BARNACLED_BARREL) {
            int smashNumber = Math.min(count, InventoryManager.getCount(itemId));
            String name = ItemDatabase.getItemName(itemId);
            if (smashNumber > 0 && name != null) {
              RequestLogger.updateSessionLog("smash " + smashNumber + " " + name);
              getOption(decision).attachItem(itemId, -smashNumber, MANUAL);
              count -= smashNumber;
            }
            itemId++;
          }
          return true;
        }
        return false;
      }
    };

    new ChoiceAdventure(1102, "The Biggest Barrel", "Item-Driven") {
      void setup() {
        // <nothing>
      }
    };

    new ChoiceAdventure(1103, "Doing the Maths", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (!request.responseText.contains("Try again")) {
          Preferences.increment("_universeCalculated");
        }
      }
    };

    new ChoiceAdventure(1104, "Tree Tea", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).leadsTo(1105);
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("_pottedTeaTreeUsed", true);
        }
      }
    };

    new ChoiceAdventure(1105, "Specifici Tea", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.getURLString().contains("itemid")) {
          Preferences.setBoolean("_pottedTeaTreeUsed", true);
        }
      }
    };

    new ChoiceAdventure(1106, "Wooof! Wooooooof!", "undefined") {
      void setup() {
        this.neverEntersQueue = true;

        this.customName = "Haunted Doghouse 1";
        this.customZones.add("Item-Driven");

        new Option(1, "gain stats", true);
        new Option(2, "30 adv of Adventurer's Best Friendship")
            .attachEffect("Adventurer's Best Friendship");
        new Option(3, "acquire familiar food", true).attachItem(ItemPool.GHOST_DOG_CHOW, 1, AUTO);

        new CustomOption(2, "+50% all stats for 30 turns");
      }
    };

    new ChoiceAdventure(1107, "Playing Fetch*", "undefined") {
      void setup() {
        this.neverEntersQueue = true;

        this.customName = "Haunted Doghouse 2";
        this.customZones.add("Item-Driven");

        new Option(1, "acquire tennis ball", true).attachItem(ItemPool.TENNIS_BALL, 1, AUTO);
        new Option(2, "30 adv of Tennis Elbow-wow").attachEffect("Tennis Elbow-wow");
        new Option(3, "acquire ~500 meat", true).attachMeat(500, AUTO);

        new CustomOption(2, "+50% init for 30 turns");
      }
    };

    new ChoiceAdventure(1108, "Your Dog Found Something Again", "undefined") {
      void setup() {
        this.neverEntersQueue = true;

        this.customName = "Haunted Doghouse 3";
        this.customZones.add("Item-Driven");

        new Option(1)
            .attachItem("bowl of eyeballs", 1, AUTO, new ImageOnly())
            .attachItem("bowl of maggots", 1, AUTO, new ImageOnly())
            .attachItem("bowl of mummy guts", 1, AUTO, new ImageOnly());
        new Option(2)
            .attachItem("blood and blood", 1, AUTO, new ImageOnly())
            .attachItem("Jack-O-Lantern beer", 1, AUTO, new ImageOnly())
            .attachItem("zombie", 1, AUTO, new ImageOnly());
        new Option(3)
            .attachItem("plastic nightmare troll", 1, AUTO, new ImageOnly())
            .attachItem("Telltale&trade; rubber heart", 1, AUTO, new ImageOnly())
            .attachItem("wind-up spider", 1, AUTO, new ImageOnly());

        new CustomOption(1, "acquire food");
        new CustomOption(2, "acquire booze");
        new CustomOption(3, "acquire cursed thing");
      }
    };

    new ChoiceAdventure(1109, "I shall call you...", "Item-Driven") {
      void setup() {
        new Option(1).leadsTo(1110);
      }
    };

    new ChoiceAdventure(1110, "Spoopy", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).leadsTo(1109);
        new Option(5);
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request != null) {
          Preferences.setBoolean(
              "doghouseBoarded", !request.responseText.contains("Board up the doghouse"));
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 5) {
          if (request.responseText.contains("You board up the doghouse")) {
            Preferences.setBoolean("doghouseBoarded", true);
          } else if (request.responseText.contains("You unboard-up the doghouse")) {
            Preferences.setBoolean("doghouseBoarded", false);
          }
        }
      }
    };

    new ChoiceAdventure(1111, "'Hello?", "Globe Theatre Backstage") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(1112, "Twitch Event #9 Time Period", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
      }
    };

    new ChoiceAdventure(1113, "Sled Dog Race Track", null) {
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
        new Option(10);
        new Option(11);
        new Option(12).leadsTo(1124);
        new Option(13);
        new Option(14);
        new Option(15);
      }
    };

    new ChoiceAdventure(1114, "Walford Rusley, Bucket Collector", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // turn in
        new Option(2); // quest
        new Option(3); // quest
        new Option(4); // quest
        new Option(5); // joke
        new Option(6); // dump
        new Option(7); // leave
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 || decision == 6) {
          QuestDatabase.setQuestProgress(Quest.BUCKET, QuestDatabase.UNSTARTED);
          Preferences.setInteger("walfordBucketProgress", 0);
          Preferences.setString("walfordBucketItem", "");
        } else if (decision < 5) {
          QuestDatabase.setQuestProgress(Quest.BUCKET, QuestDatabase.STARTED);
          Preferences.setInteger("walfordBucketProgress", 0);
          Preferences.setBoolean("_walfordQuestStartedToday", true);
          if (request.responseText.contains("Bucket of balls")) {
            Preferences.setString("walfordBucketItem", "balls");
          } else if (request.responseText.contains("bucket with blood")) {
            Preferences.setString("walfordBucketItem", "blood");
          } else if (request.responseText.contains("Bolts, mainly")) {
            Preferences.setString("walfordBucketItem", "bolts");
          } else if (request.responseText.contains("bucket of chicken")) {
            Preferences.setString("walfordBucketItem", "chicken");
          } else if (request.responseText.contains("Here y'go -- chum")) {
            Preferences.setString("walfordBucketItem", "chum");
          } else if (request.responseText.contains("fill that with ice")) {
            Preferences.setString("walfordBucketItem", "ice");
          } else if (request.responseText.contains("fill it up with milk")) {
            Preferences.setString("walfordBucketItem", "milk");
          } else if (request.responseText.contains("bucket of moonbeams")) {
            Preferences.setString("walfordBucketItem", "moonbeams");
          } else if (request.responseText.contains("bucket with rain")) {
            Preferences.setString("walfordBucketItem", "rain");
          }
        }
      }
    };

    new ChoiceAdventure(1115, "VYKEA!", "VYKEA") {
      void setup() {
        this.customName = this.name;

        new Option(1, "acquire VYKEA meatballs and mead (1/day)", true)
            .turnCost(1)
            .attachItem("VYKEA meatballs", 1, AUTO, new DisplayAll("balls"))
            .attachItem("VYKEA mead", 1, AUTO, new DisplayAll("and mead"));
        new Option(2, "acquire VYKEA hex key", true)
            .turnCost(1)
            .attachItem("VYKEA hex key", 1, AUTO);
        new Option(3, "fill bucket by 10-15%", true).turnCost(1);
        new Option(4, "acquire 3 Wal-Mart gift certificates (1/day)", true)
            .turnCost(1)
            .attachItem(ItemPool.WALMART_GIFT_CERTIFICATE, 3, AUTO, new DisplayAll("certificates"));
        new Option(5, "acquire VYKEA rune", true)
            .turnCost(1)
            .attachItem("VYKEA lightning rune", 1, AUTO, new ImageOnly())
            .attachItem("VYKEA blood rune", 1, AUTO, new ImageOnly())
            .attachItem("VYKEA frenzy rune", 1, AUTO, new ImageOnly());
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("_VYKEACafeteriaRaided", true);
        } else if (decision == 3) {
          Matcher WalfordMatcher =
              CADatabase1100to1199.WALFORD_PATTERN.matcher(request.responseText);
          if (WalfordMatcher.find()) {
            Preferences.increment(
                "walfordBucketProgress", StringUtilities.parseInt(WalfordMatcher.group(1)));
            if (Preferences.getInteger("walfordBucketProgress") >= 100) {
              QuestDatabase.setQuestProgress(Quest.BUCKET, "step2");
            }
          }
        } else if (decision == 4) {
          Preferences.setBoolean("_VYKEALoungeRaided", true);
        }
      }
    };

    new ChoiceAdventure(1116, "All They Got Inside is Vacancy (and Ice)", "The Ice Hotel") {
      void setup() {
        this.customName = this.name;

        new Option(3, "fill bucket by 10-15%", true).turnCost(1);
        new Option(4, "acquire cocktail ingredients", true).turnCost(1);
        new Option(5, "acquire 3 Wal-Mart gift certificates (1/day)", true)
            .turnCost(1)
            .attachItem(ItemPool.WALMART_GIFT_CERTIFICATE, 3, AUTO, new DisplayAll("certificates"));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          Matcher WalfordMatcher =
              CADatabase1100to1199.WALFORD_PATTERN.matcher(request.responseText);
          if (WalfordMatcher.find()) {
            Preferences.increment(
                "walfordBucketProgress", StringUtilities.parseInt(WalfordMatcher.group(1)));
            if (Preferences.getInteger("walfordBucketProgress") >= 100) {
              QuestDatabase.setQuestProgress(Quest.BUCKET, "step2");
            }
          }
        } else if (decision == 5) {
          Preferences.setBoolean("_iceHotelRoomsRaided", true);
        }
      }
    };

    new UnknownChoiceAdventure(1117);

    new ChoiceAdventure(1118, "X-32-F Combat Training Snowman Control Console", null) {
      final Pattern SNOJO_CONSOLE_PATTERN = Pattern.compile("<b>(.*?) MODE</b>");

      void setup() {
        this.canWalkFromChoice = true;

        this.customName = "Control Console";
        this.customZones.add("The Snojo");

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(6);

        new CustomOption(1, "muscle training");
        new CustomOption(2, "mysticality training");
        new CustomOption(3, "moxie training");
        new CustomOption(4, "tournament");
        new CustomOption(6, "leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = SNOJO_CONSOLE_PATTERN.matcher(request.responseText);
        Preferences.setString("snojoSetting", matcher.find() ? matcher.group(1).trim() : "");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
            Preferences.setString("snojoSetting", "MUSCLE");
            break;
          case 2:
            Preferences.setString("snojoSetting", "MYSTICALITY");
            break;
          case 3:
            Preferences.setString("snojoSetting", "MOXIE");
            break;
          case 4:
            Preferences.setString("snojoSetting", "TOURNAMENT");
            break;
        }
      }
    };

    new ChoiceAdventure(
        1119,
        "Shining Mauve Backwards In Time",
        "The Deep Machine Tunnels") { // actually has a random name
      void setup() {
        new Option(1, "acquire 4-5 abstractions", true);
        new Option(2, "acquire abstraction: comprehension", true)
            .attachItem("abstraction: comprehension", 1, AUTO);
        new Option(3, "acquire modern picture frame", true)
            .attachItem("modern picture frame", 1, AUTO);
        new Option(4, "duplicate one food, booze, spleen or potion", true).leadsTo(1125);
        new Option(6, "leave", true).entersQueue(false);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setInteger("encountersUntilDMTChoice", 49);
        if (decision == 4
            && Preferences.getInteger("lastDMTDuplication") != KoLCharacter.getAscensions()) {
          Preferences.setInteger("lastDMTDuplication", KoLCharacter.getAscensions());
        }
      }
    };

    new ChoiceAdventure(1120, "Some Assembly Required", "Item-Driven") {
      void setup() {
        new Option(1)
            .attachItem(ItemPool.VYKEA_PLANK, -5, MANUAL)
            .attachItem(ItemPool.VYKEA_INSTRUCTIONS, -1, MANUAL, new NoDisplay());
        new Option(2)
            .attachItem(ItemPool.VYKEA_RAIL, -5, MANUAL)
            .attachItem(ItemPool.VYKEA_INSTRUCTIONS, -1, MANUAL, new NoDisplay());
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VYKEACompanionData.assembleCompanion(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(1121, "Some Assembly Required", "Item-Driven") { // Runes
      void setup() {
        new Option(1).attachItem(ItemPool.VYKEA_FRENZY_RUNE, -1, MANUAL);
        new Option(2).attachItem(ItemPool.VYKEA_BLOOD_RUNE, -1, MANUAL);
        new Option(3).attachItem(ItemPool.VYKEA_LIGHTNING_RUNE, -1, MANUAL);
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VYKEACompanionData.assembleCompanion(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(1122, "Some Assembly Required", "Item-Driven") { // Dowels
      void setup() {
        new Option(1).attachItem(ItemPool.VYKEA_DOWEL, -1, MANUAL);
        new Option(2).attachItem(ItemPool.VYKEA_DOWEL, -11, MANUAL);
        new Option(3).attachItem(ItemPool.VYKEA_DOWEL, -23, MANUAL);
        new Option(4).attachItem(ItemPool.VYKEA_DOWEL, -37, MANUAL);
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VYKEACompanionData.assembleCompanion(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(1123, "Some Assembly Required", "Item-Driven") { // Finishing Touch
      void setup() {
        new Option(1).attachItem(ItemPool.VYKEA_PLANK, -5, MANUAL);
        new Option(2).attachItem(ItemPool.VYKEA_RAIL, -5, MANUAL);
        new Option(3).attachItem(ItemPool.VYKEA_BRACKET, -5, MANUAL);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VYKEACompanionData.assembleCompanion(this.choice, decision, request.responseText);
      }
    };

    new ChoiceAdventure(1124, "Visiting the Dogs", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3).leadsTo(1113);
      }
    };

    new ChoiceAdventure(1125, "A Vocalization of Two", "The Deep Machine Tunnels") {
      void setup() {
        // can walk away, but do NOT want to

        new Option(1).turnCost(1);
        new Option(2).leadsTo(1119);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }
    };

    new UnknownChoiceAdventure(1126);

    new RetiredChoiceAdventure(1127, "The Crimbo Elf Commune", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(1128, "Reindeer Commune", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new RetiredChoiceAdventure(1129, "The Crimbulmination", null) { // 1/4
      void setup() {
        new Option(1).leadsTo(1130);
      }
    };

    new RetiredChoiceAdventure(1130, "The Crimbulmination", null) { // 2/4
      void setup() {
        new Option(1).leadsTo(1131);
      }
    };

    new RetiredChoiceAdventure(1131, "The Crimbulmination", null) { // 3/4
      void setup() {
        new Option(1).leadsTo(1132);
      }
    };

    new RetiredChoiceAdventure(1132, "The Crimbulmination", null) { // 4/4
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1133, "Batfellow Begins", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          KoLCharacter.enterLimitmode(Limitmode.BATMAN);
        }
      }
    };

    new ChoiceAdventure(1134, "Batfellow Ends", null) { // choosing to exit
      void setup() {
        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          KoLCharacter.setLimitmode(null);
        }
      }
    };

    new ChoiceAdventure(1135, "The Bat-Sedan", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(9).leadsTo(1134);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return BatManager.parseBatSedan(responseText);
      }
    };

    new ChoiceAdventure(1136, "Bat-Research and Bat-Development", null) {
      void setup() {
        new Option(1).leadsTo(1137);
        new Option(2).leadsTo(1138);
        new Option(3).leadsTo(1139);
        new Option(4);
      }
    };

    new ChoiceAdventure(1137, "Bat-Suit Upgrades", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
        new Option(11).leadsTo(1136);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        BatManager.batSuitUpgrade(decision, request.responseText);
      }
    };

    new ChoiceAdventure(1138, "Bat-Sedan Upgrades", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);
        new Option(8);
        new Option(11).leadsTo(1136);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        BatManager.batSedanUpgrade(decision, request.responseText);
      }
    };

    new ChoiceAdventure(1139, "Bat-Cavern Upgrades", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(7);
        new Option(8);
        new Option(11).leadsTo(1136);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        BatManager.batCavernUpgrade(decision, request.responseText);
      }
    };

    new ChoiceAdventure(1140, "Casing the Conservatory", "Gotpork Conservatory of Flowers") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.GLOB_OF_BAT_GLUE, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.FINGERPRINT_DUSTING_KIT, -3, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1141, "Researching the Reservoir", "Gotpork Municipal Reservoir") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.BAT_AID_BANDAGE, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.ULTRACOAGULATOR, -3, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1142, "Combing the Cemetery", "Gotpork Gardens Cemetery") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.BAT_BEARING, -1, MANUAL, new NoDisplay());
        new Option(5).attachItem(ItemPool.EXPLODING_KICKBALL, -3, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1143, "Searching the Sewers", "Gotpork City Sewers") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.BAT_OOMERANG, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1144, "Assessing the Asylum", "Porkham Asylum") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.BAT_O_MITE, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1145, "Looking Over the Library", "The Old Gotpork Library") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.BAT_JUTE, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1146, "Considering the Clock Factory", "Gotpork Clock, Inc.") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.EXPLODING_KICKBALL, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1147, "Frisking the Foundry", "Gotpork Foundry") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.ULTRACOAGULATOR, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1148, "Taking Stock of the Trivia Company", "Trivial Pursuits, LLC") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).attachItem(ItemPool.FINGERPRINT_DUSTING_KIT, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(1149, "JokesterCo HQ", "JokesterCo") {
      void setup() {
        new Option(1).leadsTo(1169);
        new Option(2);
      }
    };

    new ChoiceAdventure(1150, "Knock Knock, Kudzu's There", "Gotpork Conservatory of Flowers") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(
        1151, "Half-man, Half-squid, Half-burr-- Wait.", "Gotpork Municipal Reservoir") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1152, "A Graves Situation", "Gotpork Gardens Cemetery") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1153, "Plumb Rude", "Gotpork City Sewers") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1154, "MEANWHILE...", "Porkham Asylum") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1155, "The Mad Librarian", "The Old Gotpork Library") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1156, "He's Totally Cuckoo", "Gotpork Clock, Inc.") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1157, "Out of the Frying Pan", "Gotpork Foundry") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1158, "This is Not a Test", "Trivial Pursuits, LLC") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1159, "The Loneliest Number", "Gotpork Conservatory of Flowers") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1160, "Last Tango in Gotpork", "Gotpork Municipal Reservoir") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1161, "And You're Out", "Gotpork Gardens Cemetery") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1162, "A Lousy Four-Flusher", "Gotpork City Sewers") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1163, "Self-Incrimination", "Porkham Asylum") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1164, "Half a Dozen of the Other", "The Old Gotpork Library") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1165, "Knocking on Heaven's Door", "Gotpork Clock, Inc.") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1166, "Black-Balled", "Gotpork Foundry") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1167, "A Stitch in Time", "Trivial Pursuits, LLC") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1168, "Batfellow Ends", null) { // running out of time
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        KoLCharacter.setLimitmode(null);
      }
    };

    new ChoiceAdventure(1169, "The Smokester, The Midnight Tokester", "JokesterCo") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1170, "The Punchline", "JokesterCo") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1171, "LT&T Office", "Item-Driven") {
      final Pattern TELEGRAM_PATTERN = Pattern.compile("value=\"RE: (.*?)\"");

      String lastResponseText = "";

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(8);
      }

      @Override
      void preChoice(String urlString) {
        this.lastResponseText = ChoiceManager.lastResponseText;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision < 4) {
          QuestDatabase.setQuestProgress(Quest.TELEGRAM, QuestDatabase.STARTED);
          Preferences.setInteger("lttQuestDifficulty", decision);
          Preferences.setInteger("lttQuestStageCount", 0);
          Matcher matcher = TELEGRAM_PATTERN.matcher(this.lastResponseText);
          for (int i = 0; i < decision; i++) {
            if (!matcher.find()) {
              Preferences.setString("lttQuestName", "");
              return;
            }
          }
          Preferences.setString("lttQuestName", matcher.group(1));
        } else if (decision == 5) {
          QuestDatabase.setQuestProgress(Quest.TELEGRAM, QuestDatabase.UNSTARTED);
          Preferences.setInteger("lttQuestDifficulty", 0);
          Preferences.setInteger("lttQuestStageCount", 0);
          Preferences.setString("lttQuestName", "");
        }
      }
    };

    new ChoiceAdventure(1172, "The Investigation Begins", "Investigating a Plaintive Telegram") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.TELEGRAM, "step1");
        Preferences.setInteger("lttQuestStageCount", 0);
      }
    };

    new ChoiceAdventure(1173, "The Investigation Continues", "Investigating a Plaintive Telegram") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.TELEGRAM, "step2");
        Preferences.setInteger("lttQuestStageCount", 0);
      }
    };

    new ChoiceAdventure(
        1174, "The Investigation Continues", "Investigating a Plaintive Telegram") { // bis
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.TELEGRAM, "step3");
        Preferences.setInteger("lttQuestStageCount", 0);
      }
    };

    new ChoiceAdventure(
        1175, "The Investigation Thrillingly Concludes!", "Investigating a Plaintive Telegram") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.TELEGRAM, "step4");
        Preferences.setInteger("lttQuestStageCount", 0);
      }
    };

    new ChoiceAdventure(1176, "Go West, Young Adventurer!", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          // Snake Oilers start with extra
          Preferences.setInteger("awolMedicine", 3);
          Preferences.setInteger("awolVenom", 3);
        }
      }
    };

    new ChoiceAdventure(1177, "Book of the West: Cow Punching", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(1178, "Book of the West: Beanslinging", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(1179, "Book of the West: Snake Oiling", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new UnknownChoiceAdventure(1180);

    new ChoiceAdventure(1181, "Your Witchess Set", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).leadsTo(1182);
        new Option(2).leadsTo(1184);
        new Option(3).leadsTo(1183);
        new Option(4);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        ChoiceManager.defaultRegister(this.choice, decision);
        return true;
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!request.responseText.contains("Examine the shrink ray")) {
          Preferences.setInteger("_witchessFights", 5);
        }
      }
    };

    new ChoiceAdventure(1182, "Play against the Witchess Pieces", "Item-Driven") {
      void setup() {
        // uses the "piece" field to select a piece
        // pawn:1935
        // knight:1936
        // bishop:1942
        // rook:1938
        // ox:1937
        // king:1940
        // witch:1941
        // queen:1939
        new Option(1);
        new Option(2).leadsTo(1181);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        // These will redirect to a fight. The encounter will suffice.
        if (decision == 1) {
          String desc = "Play against the Witchess pieces";
          RequestLogger.updateSessionLog(
              "Took choice " + this.choice + "/" + decision + ": " + desc);
        }
        return true;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          KoLAdventure.lastLocationURL = request.getURLString();
          GenericRequest.itemMonster = "Your Witchess Set";
        }
      }
    };

    new ChoiceAdventure(1183, "Witchess Puzzles", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3).leadsTo(1181);
      }
    };

    new ChoiceAdventure(1184, "Play Witchess", "Item-Driven") {
      void setup() {
        // yup, 1-2-5-4-3, that's their order...
        new Option(1);
        new Option(2);
        new Option(5);
        new Option(4);
        new Option(3).leadsTo(1181);
      }
    };

    new UnknownChoiceAdventure(1185);

    new UnknownChoiceAdventure(1186);

    new ChoiceAdventure(1187, "One Pill, Two Pill", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(1188, "The Call is Coming from Outside the Simulation", null) {
      final Pattern ENLIGHTENMENT_PATTERN = Pattern.compile("achieved <b>(\\d+)</b> enlightenment");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = ENLIGHTENMENT_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger("sourceEnlightenment", StringUtilities.parseInt(matcher.group(1)));
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          // Skill learned
          Preferences.decrement("sourceEnlightenment", 1, 0);
        }
      }
    };

    new UnknownChoiceAdventure(1189);

    new ChoiceAdventure(1190, "The Oracle", null) {
      final Pattern ORACLE_QUEST_PATTERN =
          Pattern.compile("don't remember leaving any spoons in (.*?)&quot;");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).attachItem(ItemPool.NO_SPOON, -1, MANUAL, new NoDisplay());
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          QuestDatabase.setQuestProgress(Quest.ORACLE, QuestDatabase.UNSTARTED);
          Preferences.increment("sourceEnlightenment");
          Preferences.setString("sourceOracleTarget", "");
        } else if (decision <= 3) {
          QuestDatabase.setQuestProgress(Quest.ORACLE, QuestDatabase.STARTED);
          Matcher matcher = ORACLE_QUEST_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            Preferences.setString("sourceOracleTarget", matcher.group(1));
          }
        }
      }
    };

    new ChoiceAdventure(1191, "Source Terminal", "Item-Driven") {
      final Pattern EDUCATE_PATTERN = Pattern.compile("rel=\"educate (.*?).edu\"");
      final Pattern ENHANCE_PATTERN = Pattern.compile("rel=\"enhance (.*?).enh\"");
      final Pattern ENHANCE_EFFECT_PATTERN =
          Pattern.compile("acquire an effect: (.*?) \\(duration: (\\d+) Adventures\\)</div>");
      final Pattern ENQUIRY_PATTERN = Pattern.compile("rel=\"enquiry (.*?).enq\"");
      final Pattern EXTRUDE_PATTERN = Pattern.compile("rel=\"extrude (.*?).ext\"");
      final Pattern CHIPS_PATTERN =
          Pattern.compile(
              "PRAM chips installed: (\\d+).*?GRAM chips installed: (\\d+).*?SPAM chips installed: (\\d+)");
      final Pattern CHIP_PATTERN = Pattern.compile("<div>(.*?) chip installed");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        request.setHasResult(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        request.setHasResult(false);
        if (decision != 1) {
          return;
        }
        String input = request.getFormField("input");
        if (input == null) {
          return;
        }

        if (input.startsWith("educate")) {
          int successIndex = request.responseText.lastIndexOf("active skills");
          int failIndex = request.responseText.lastIndexOf("missing educate");
          int listIndex = request.responseText.lastIndexOf("usage: educate [target file]");

          if (listIndex > successIndex && listIndex > failIndex) {
            int startIndex = request.responseText.lastIndexOf("available targets:");
            int endIndex = request.responseText.lastIndexOf("&gt;");
            if (startIndex == -1 || endIndex == -1) {
              // this shouldn't happen...
              return;
            }
            String text = request.responseText.substring(startIndex, endIndex);
            StringBuilder knownString = new StringBuilder();

            Matcher matcher = EDUCATE_PATTERN.matcher(text);
            while (matcher.find()) {
              if (knownString.length() > 0) {
                knownString.append(",");
              }
              knownString.append(matcher.group(1)).append(".edu");
            }
            Preferences.setString("sourceTerminalEducateKnown", knownString.toString());
            return;
          }

          if (failIndex > successIndex) return;

          String skill = input.substring(7).trim();

          if (Preferences.getString("sourceTerminalChips").contains("DRAM")) {
            Preferences.setString(
                "sourceTerminalEducate1", Preferences.getString("sourceTerminalEducate2"));
            Preferences.setString("sourceTerminalEducate2", skill);
          } else {
            Preferences.setString("sourceTerminalEducate1", skill);
          }
        } else if (input.startsWith("enhance")) {
          int successIndex = request.responseText.lastIndexOf("You acquire an effect");
          int badInputIndex = request.responseText.lastIndexOf("missing enhance");
          int limitIndex = request.responseText.lastIndexOf("enhance limit exceeded");
          int listIndex = request.responseText.lastIndexOf("usage: enhance [target file]");

          if (listIndex > limitIndex && listIndex > badInputIndex && listIndex > successIndex) {
            int startIndex = request.responseText.lastIndexOf("available targets:");
            int endIndex = request.responseText.lastIndexOf("&gt;");
            if (startIndex == -1 || endIndex == -1) {
              // this shouldn't happen...
              return;
            }
            String text = request.responseText.substring(startIndex, endIndex);
            StringBuilder knownString = new StringBuilder();

            Matcher matcher = ENHANCE_PATTERN.matcher(text);
            while (matcher.find()) {
              if (knownString.length() > 0) {
                knownString.append(",");
              }
              knownString.append(matcher.group(1) + ".enh");
            }
            Preferences.setString("sourceTerminalEnhanceKnown", knownString.toString());
            return;
          }

          if (limitIndex > badInputIndex && limitIndex > successIndex) {
            String chips = Preferences.getString("sourceTerminalChips");
            int limit = 1 + (chips.contains("CRAM") ? 1 : 0) + (chips.contains("SCRAM") ? 1 : 0);
            Preferences.setInteger("_sourceTerminalEnhanceUses", limit);
            return;
          }

          if (badInputIndex > successIndex) return;

          int startIndex = request.responseText.lastIndexOf("You acquire");
          int endIndex = request.responseText.lastIndexOf("&gt;");
          if (startIndex == -1 || endIndex == -1) {
            // this shouldn't happen...
            return;
          }
          String text = request.responseText.substring(startIndex, endIndex);

          Matcher matcher = ENHANCE_EFFECT_PATTERN.matcher(text);
          if (matcher.find()) {
            String message =
                "You acquire an effect: " + matcher.group(1) + " (" + matcher.group(2) + ")";
            RequestLogger.printLine(message);
            RequestLogger.updateSessionLog(message);
            // Refresh status manually since KoL doesn't trigger it
            ApiRequest.updateStatus(true);
          }

          Preferences.increment("_sourceTerminalEnhanceUses");
        } else if (input.startsWith("enquiry")) {
          int successIndex = request.responseText.lastIndexOf("enquiry mode set:");
          int failIndex = request.responseText.lastIndexOf("missing enquiry target");
          int listIndex = request.responseText.lastIndexOf("usage: enquiry [target file]");

          if (listIndex > failIndex && listIndex > successIndex) {
            int startIndex = request.responseText.lastIndexOf("available targets:");
            int endIndex = request.responseText.lastIndexOf("&gt;");
            if (startIndex == -1 || endIndex == -1) {
              // this shouldn't happen...
              return;
            }
            String text = request.responseText.substring(startIndex, endIndex);
            StringBuilder knownString = new StringBuilder();

            Matcher matcher = ENQUIRY_PATTERN.matcher(text);
            while (matcher.find()) {
              if (knownString.length() > 0) {
                knownString.append(",");
              }
              knownString.append(matcher.group(1) + ".enq");
            }
            Preferences.setString("sourceTerminalEnquiryKnown", knownString.toString());
            return;
          }

          if (failIndex > successIndex) return;

          int beginIndex = successIndex + 18;
          int endIndex = request.responseText.indexOf("</div>", beginIndex);
          Preferences.setString(
              "sourceTerminalEnquiry", request.responseText.substring(beginIndex, endIndex));
        } else if (input.startsWith("extrude")) {
          int acquire = request.responseText.lastIndexOf("You acquire");
          int invalid = request.responseText.lastIndexOf("Invalid");
          int insufficient = request.responseText.lastIndexOf("Insufficient");
          int confirm = request.responseText.lastIndexOf("to confirm");
          int exceeded = request.responseText.lastIndexOf("limits exceeded");
          int listIndex = request.responseText.lastIndexOf("usage: extrude [target file]");

          if (listIndex > acquire
              && listIndex > invalid
              && listIndex > insufficient
              && listIndex > confirm
              && listIndex > exceeded) {
            int startIndex = request.responseText.lastIndexOf("available targets:");
            int endIndex = request.responseText.lastIndexOf("&gt;");
            if (startIndex == -1 || endIndex == -1) {
              // this shouldn't happen...
              return;
            }
            String text = request.responseText.substring(startIndex, endIndex);
            StringBuilder knownString = new StringBuilder();

            Matcher matcher = EXTRUDE_PATTERN.matcher(text);
            while (matcher.find()) {
              if (knownString.length() > 0) {
                knownString.append(",");
              }
              knownString.append(matcher.group(1) + ".ext");
            }
            Preferences.setString("sourceTerminalExtrudeKnown", knownString.toString());
            return;
          }

          if (exceeded > acquire
              && exceeded > invalid
              && exceeded > insufficient
              && exceeded > confirm) {
            Preferences.setInteger("_sourceTerminalExtrudes", 3);
            return;
          }

          if (invalid > acquire || insufficient > acquire || confirm > acquire) return;

          // Creation must have succeeded
          Option option = getOption(decision);
          String message = "";
          if (input.contains("food")) {
            option.attachItem(ItemPool.BROWSER_COOKIE, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -10, MANUAL);
            message = "You acquire an item: browser cookie";
          } else if (input.contains("booze")) {
            option.attachItem(ItemPool.HACKED_GIBSON, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -10, MANUAL);
            message = "You acquire an item: hacked gibson";
          } else if (input.contains("goggles")) {
            option.attachItem(ItemPool.SOURCE_SHADES, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -100, MANUAL);
            message = "You acquire an item: Source shades";
          } else if (input.contains("gram")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_GRAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -100, MANUAL);
            message = "You acquire an item: Source terminal GRAM chip";
          } else if (input.contains("pram")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_PRAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -100, MANUAL);
            message = "You acquire an item: Source terminal PRAM chip";
          } else if (input.contains("spam")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_SPAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -100, MANUAL);
            message = "You acquire an item: Source terminal SPAM chip";
          } else if (input.contains("cram")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_CRAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -1000, MANUAL);
            message = "You acquire an item: Source terminal CRAM chip";
          } else if (input.contains("dram")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_DRAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -1000, MANUAL);
            message = "You acquire an item: Source terminal DRAM chip";
          } else if (input.contains("tram")) {
            option.attachItem(ItemPool.SOURCE_TERMINAL_TRAM_CHIP, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -1000, MANUAL);
            message = "You acquire an item: Source terminal TRAM chip";
          } else if (input.contains("familiar")) {
            option.attachItem(ItemPool.SOFTWARE_BUG, 1, MANUAL);
            option.attachItem(ItemPool.SOURCE_ESSENCE, -10000, MANUAL);
            message = "You acquire an item: software bug";
          }
          RequestLogger.printLine(message);
          RequestLogger.updateSessionLog(message);
          Preferences.increment("_sourceTerminalExtrudes");
        } else if (input.startsWith("status")) {
          int startIndex = request.responseText.lastIndexOf("Installed Hardware");
          int endIndex = request.responseText.lastIndexOf("&gt;");
          if (startIndex == -1 || endIndex == -1) {
            // this shouldn't happen...
            return;
          }
          String text = request.responseText.substring(startIndex, endIndex);
          StringBuilder chipString = new StringBuilder();

          Matcher matcher = CHIPS_PATTERN.matcher(text);
          if (matcher.find()) {
            Preferences.setInteger(
                "sourceTerminalPram", StringUtilities.parseInt(matcher.group(1)));
            Preferences.setInteger(
                "sourceTerminalGram", StringUtilities.parseInt(matcher.group(2)));
            Preferences.setInteger(
                "sourceTerminalSpam", StringUtilities.parseInt(matcher.group(3)));
          }

          text = text.substring(text.indexOf("reduced"));
          matcher = CHIP_PATTERN.matcher(text);
          while (matcher.find()) {
            if (chipString.length() > 0) {
              chipString.append(",");
            }
            chipString.append(matcher.group(1));
          }
          Preferences.setString("sourceTerminalChips", chipString.toString());
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        request.setHasResult(true);
      }
    };

    new UnknownChoiceAdventure(1192);

    new ChoiceAdventure(1193, "The Precinct", null) {
      final Pattern CASE_PATTERN = Pattern.compile("\\((\\d+) more case");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // take a case/investigate
        new Option(2); // quartermaster
        new Option(3); // leaderboards
        new Option(6); // leave
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = CASE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger(
              "_detectiveCasesCompleted", 3 - StringUtilities.parseInt(matcher.group(1)));
        }
      }
    };

    new ChoiceAdventure(1194, "War Apparently Changed", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(1195, "Spinning Your Time-Spinner", "Item-Driven") {
      final Pattern TIME_SPINNER_PATTERN = Pattern.compile("have (\\d+) minute");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).leadsTo(1196);
        new Option(2).leadsTo(1197);
        new Option(3);
        new Option(4).leadsTo(1199);
        new Option(5).leadsTo(1198);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        ChoiceManager.defaultRegister(this.choice, decision);
        RequestLogger.updateSessionLog(urlString);

        if (decision == 3) {
          KoLAdventure.lastLocationURL = urlString;
          GenericRequest.itemMonster = "Time-Spinner";

          String message = "[" + KoLAdventure.getAdventureCount() + "] Way Back in Time";
          RequestLogger.printLine();
          RequestLogger.printLine(message);

          RequestLogger.updateSessionLog();
          RequestLogger.updateSessionLog(message);
        }
        return true;
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = TIME_SPINNER_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setInteger(
              "_timeSpinnerMinutesUsed", 10 - StringUtilities.parseInt(matcher.group(1)));
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3) {
          Preferences.increment("_timeSpinnerMinutesUsed");
        } else if (decision == 4) {
          Preferences.increment("_timeSpinnerMinutesUsed", 2);
        }
      }
    };

    new ChoiceAdventure(1196, "Travel to a Recent Fight", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2).leadsTo(1195);
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        ChoiceManager.defaultRegister(this.choice, decision);
        RequestLogger.updateSessionLog(urlString);

        if (decision == 1 && !urlString.contains("monid=0")) {
          KoLAdventure.lastLocationURL = urlString;
          GenericRequest.itemMonster = "Time-Spinner";

          String message = "[" + KoLAdventure.getAdventureCount() + "] A Recent Fight";
          RequestLogger.printLine();
          RequestLogger.printLine(message);

          RequestLogger.updateSessionLog();
          RequestLogger.updateSessionLog(message);
        }
        return true;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && !request.getURLString().contains("monid=0")) {
          Preferences.increment("_timeSpinnerMinutesUsed", 3);
          EncounterManager.ignoreSpecialMonsters();
        }
      }
    };

    new ChoiceAdventure(1197, "Travel back to a Delicious Meal", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).leadsTo(1195);
      }

      @Override
      void postChoice0(GenericRequest request, int decision) {
        if (decision == 1 && !request.getURLString().contains("foodid=0")) {
          EatItemRequest.timeSpinnerUsed = true;
        }
      }
    };

    new ChoiceAdventure(1198, "Play a Time Prank", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2).leadsTo(1195);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("paradoxical time copy")) {
          Preferences.increment("_timeSpinnerMinutesUsed");
        }
      }
    };

    new ChoiceAdventure(1199, "The Far Future", "Item-Driven") {
      final Pattern TIME_SPINNER_MEDALS_PATTERN =
          Pattern.compile("memory of earning <b>(\\d+) medal");

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

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("item appears in the replicator")
            || request.responseText.contains("convoluted nature of time-travel")) {
          Preferences.setBoolean("_timeSpinnerReplicatorUsed", true);
          return;
        }
        Matcher medalMatcher = TIME_SPINNER_MEDALS_PATTERN.matcher(request.responseText);
        if (medalMatcher.find()) {
          Preferences.setInteger(
              "timeSpinnerMedals", StringUtilities.parseInt(medalMatcher.group(1)));
        }
      }
    };
  }

  private static final Pattern WALFORD_PATTERN =
      Pattern.compile("\\(Walford's bucket filled by (\\d+)%\\)");
}
