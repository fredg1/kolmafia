package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.AscensionClass;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.Modifiers;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.BanishManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.CinderellaManager;
import net.sourceforge.kolmafia.session.HaciendaManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.LightsOutManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.RumpleManager;
import net.sourceforge.kolmafia.session.ShenCopperheadManager;
import net.sourceforge.kolmafia.session.TurnCounter;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase800to899 extends ChoiceAdventureDatabase {
  final void add800to899() {
    new UnknownChoiceAdventure(800);

    new ChoiceAdventure(801, "A Reanimated Conversation", "Item-Driven") {
      final Pattern REANIMATOR_ARM_PATTERN = Pattern.compile("(\\d+) arms??<br>");
      final Pattern REANIMATOR_LEG_PATTERN = Pattern.compile("(\\d+) legs??<br>");
      final Pattern REANIMATOR_SKULL_PATTERN = Pattern.compile("(\\d+) skulls??<br>");
      final Pattern REANIMATOR_WEIRDPART_PATTERN =
          Pattern.compile("(\\d+) weird random parts??<br>");
      final Pattern REANIMATOR_WING_PATTERN = Pattern.compile("(\\d+) wings??<br>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1, "skulls increase meat drops");
        new Option(2, "arms deal extra damage");
        new Option(3, "legs increase item drops");
        new Option(4, "wings sometimes delevel at start of combat");
        new Option(5, "weird parts sometimes block enemy attacks");
        new Option(6, "get rid of all collected parts");
        new Option(7, "no changes");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = REANIMATOR_ARM_PATTERN.matcher(request.responseText);
        Preferences.setInteger(
            "reanimatorArms", matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0);

        matcher = REANIMATOR_LEG_PATTERN.matcher(request.responseText);
        Preferences.setInteger(
            "reanimatorLegs", matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0);

        matcher = REANIMATOR_SKULL_PATTERN.matcher(request.responseText);
        Preferences.setInteger(
            "reanimatorSkulls", matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0);

        matcher = REANIMATOR_WEIRDPART_PATTERN.matcher(request.responseText);
        Preferences.setInteger(
            "reanimatorWeirdParts",
            matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0);

        matcher = REANIMATOR_WING_PATTERN.matcher(request.responseText);
        Preferences.setInteger(
            "reanimatorWings", matcher.find() ? StringUtilities.parseInt(matcher.group(1)) : 0);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 6) {
          Preferences.setInteger("reanimatorArms", 0);
          Preferences.setInteger("reanimatorLegs", 0);
          Preferences.setInteger("reanimatorSkulls", 0);
          Preferences.setInteger("reanimatorWeirdParts", 0);
          Preferences.setInteger("reanimatorWings", 0);
        }
      }
    };

    new UnknownChoiceAdventure(802);

    new RetiredChoiceAdventure(
        803, "Behind the Music.  Literally.", "The Space Odyssey Discotheque") {
      void setup() {
        new Option(1, "gain 2-3 horoscopes", true).turnCost(1);
        new Option(3, "find interesting room", true).turnCost(1);
        new Option(4, "investigate interesting room", true).turnCost(1);
        new Option(5, "investigate trap door", true);
        new Option(6, "investigate elevator", true);
      }
    };

    new ChoiceAdventure(804, "Trick or Treat!", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // scope a new block
        new Option(2); // leave
        new Option(3); // houses
      }
    };

    new ChoiceAdventure(805, "A Sietch in Time", "The Arid, Extra-Dry Desert") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
        new Option(7);

        new CustomOption(1, "talk to Gnasir");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        int gnasirProgress = Preferences.getInteger("gnasirProgress");

        // Annoyingly, the option numbers change as you turn
        // things in. Therefore, we must look at response request.responseText

        if (request.responseText.contains("give the stone rose to Gnasir")) {
          getOption(decision).attachItem(ItemPool.STONE_ROSE, -1, MANUAL);
          gnasirProgress |= 1;
          Preferences.setInteger("gnasirProgress", gnasirProgress);
        } else if (request.responseText.contains("hold up the bucket of black paint")) {
          getOption(decision).attachItem(ItemPool.BLACK_PAINT, -1, MANUAL);
          gnasirProgress |= 2;
          Preferences.setInteger("gnasirProgress", gnasirProgress);
        } else if (request.responseText.contains("hand Gnasir the glass jar")) {
          getOption(decision).attachItem(ItemPool.KILLING_JAR, -1, MANUAL);
          gnasirProgress |= 4;
          Preferences.setInteger("gnasirProgress", gnasirProgress);
        } else if (request.responseText.contains("hand him the pages")) {
          // You hand him the pages, and he shuffles them into their correct order and inspects them
          // carefully.
          getOption(decision).attachItem(ItemPool.WORM_RIDING_MANUAL_PAGE, -15, MANUAL);
          gnasirProgress |= 8;
          Preferences.setInteger("gnasirProgress", gnasirProgress);
        }
      }
    };

    new ChoiceAdventure(806, "A Fun-Size Dilemma", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(807, "Breaker Breaker!", "Item-Driven") {
      void setup() {
        new Option(1);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }
    };

    new RetiredChoiceAdventure(808, "Silence at last", "The Spirit World") {
      void setup() {
        new Option(1, "gain spirit bed piece", true);
        new Option(2, "fight spirit alarm clock", true);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Abort if you want to fight spirit alarm clock but it isn't available.
        if (decision.equals("2") && !responseText.contains("nightstand wasn't here before")) {
          return "0";
        }
        return decision;
      }
    };

    new RetiredChoiceAdventure(809, "Uncle Crimbo's Trailer", null) {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(810, "K.R.A.M.P.U.S. facility", null) {
      // <td align="center" valign="middle"><a
      // href="choice.php?whichchoice=810&option=1&slot=7&pwd=xxx" style="text-decoration:none"><img
      // alt='Toybot (Level 3)' title='Toybot (Level 3)' border=0
      // src='http://images.kingdomofloathing.com/otherimages/crimbotown/krampus_toybot.gif'
      // /></a></td>
      final Pattern URL_SLOT_PATTERN = Pattern.compile("slot=(\\d+)");
      final Pattern BOT_PATTERN = Pattern.compile("<td.*?<img alt='([^']*)'.*?</td>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).attachItem(ItemPool.WARBEAR_WHOSIT, -100, MANUAL);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 4 && request.responseText.contains("You upgrade the robot!")) {
          String bot = findKRAMPUSBot(request.getURLString(), request.responseText);
          int cost =
              (bot == null) ? 0 : bot.contains("Level 2") ? 250 : bot.contains("Level 3") ? 500 : 0;
          if (cost != 0) {
            getOption(decision).attachItem(ItemPool.WARBEAR_WHOSIT, -cost, MANUAL);
          }
        }
      }

      String findKRAMPUSBot(String urlString, String responseText) {
        Matcher slotMatcher = URL_SLOT_PATTERN.matcher(urlString);
        if (!slotMatcher.find()) {
          return null;
        }
        String slotString = slotMatcher.group(0);
        Matcher botMatcher = BOT_PATTERN.matcher(responseText);
        while (botMatcher.find()) {
          if (botMatcher.group(0).contains(slotString)) {
            return botMatcher.group(1);
          }
        }
        return null;
      }
    };

    new UnknownChoiceAdventure(811);

    new RetiredChoiceAdventure(812, "The Unpermery", null) {
      final Pattern UNPERM_PATTERN =
          Pattern.compile("Turning (.+)(?: \\(HP\\)) into (\\d+) karma.");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          Matcher matcher = UNPERM_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            KoLCharacter.removeAvailableSkill(matcher.group(1));
            Preferences.increment("bankedKarma", Integer.parseInt(matcher.group(2)));
          }
        }
      }
    };

    new RetiredChoiceAdventure(
        813, "What Warbears Are Good For", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1, "Open K.R.A.M.P.U.S. facility", true);
      }
    };

    new RetiredChoiceAdventure(814, "The Spoils of Warbears", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(815, "Make Love, Not Warbears", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(816, "Let Slip the Bears of War", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(817, "The Hundred-Years Warbear", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(818, "The Unhurt Locker", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(819, "Warbear of the Roses", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
      }
    };

    new RetiredChoiceAdventure(820, "The Civil Warbear", "WarBear Fortress (First Level)") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(821, "Warbear LP-ROM burner", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          HaciendaManager.parseRecording(request.getURLString(), request.responseText);
        }
      }
    };

    new ChoiceAdventure(822, "The Prince's Ball (In the Restroom)", "The Prince's Restroom") {
      void setup() {
        // options are assigned dynamically, and contain sub-choices...
        for (int i = 1; i <= 5; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(823, "The Prince's Ball (On the Dance Floor)", "The Prince's Dance Floor") {
      void setup() {
        // options are assigned dynamically, and contain sub-choices...
        for (int i = 1; i <= 8; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(824, "The Prince's Ball (The Kitchen)", "The Prince's Kitchen") {
      void setup() {
        // options are assigned dynamically...
        for (int i = 1; i <= 12; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(825, "The Prince's Ball (On the Balcony)", "The Prince's Balcony") {
      void setup() {
        // options are assigned dynamically...
        for (int i = 1; i <= 8; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(826, "The Prince's Ball (The Lounge)", "The Prince's Lounge") {
      void setup() {
        // options are assigned dynamically, and contain sub-choices...
        for (int i = 1; i <= 9; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(
        827, "The Prince's Ball (At the Canapés Table)", "The Prince's Canapes Table") {
      void setup() {
        // options are assigned dynamically, and contain sub-choices...
        for (int i = 1; i <= 15; i++) {
          new Option(i);
        }
      }

      @Override
      void visitChoice(GenericRequest request) {
        CinderellaManager.parseCinderellaTime();
        Preferences.setString("grimstoneMaskPath", "stepmother");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CinderellaManager.postChoice2(request);
      }
    };

    new ChoiceAdventure(828, "Warbear Sequential Gaiety Distribution System", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(829, "We All Wear Masks", "Item-Driven") {
      void setup() {
        new Option(1).attachItem(ItemPool.GRIMSTONE_MASK, -1, MANUAL, new NoDisplay());
        new Option(2).attachItem(ItemPool.GRIMSTONE_MASK, -1, MANUAL, new NoDisplay());
        new Option(3).attachItem(ItemPool.GRIMSTONE_MASK, -1, MANUAL, new NoDisplay());
        new Option(4).attachItem(ItemPool.GRIMSTONE_MASK, -1, MANUAL, new NoDisplay());
        new Option(5)
            .attachEffect("Hare-Brained")
            .attachItem(ItemPool.GRIMSTONE_MASK, -1, MANUAL, new NoDisplay());
        new Option(6);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision < 6 && decision > 0) {
          Preferences.setInteger("cinderellaMinutesToMidnight", 0);
        }
        if (decision == 1) {
          Preferences.setInteger("cinderellaMinutesToMidnight", 30);
          Preferences.setInteger("cinderellaScore", 0);
          Preferences.setString("grimstoneMaskPath", "stepmother");
        } else if (decision == 2) {
          Preferences.setString("grimstoneMaskPath", "wolf");
        } else if (decision == 3) {
          Preferences.setString("grimstoneMaskPath", "witch");
        } else if (decision == 4) {
          Preferences.setString("grimstoneMaskPath", "gnome");
        } else if (decision == 5) {
          Preferences.setString("grimstoneMaskPath", "hare");
        }
        RumpleManager.reset(decision);
      }
    };

    new ChoiceAdventure(830, "Cooldown", "The Inner Wolf Gym") {
      void setup() {
        this.customName = this.name;

        new Option(1, "Shower Power", true).leadsTo(832, false, o -> true, " or ");
        new Option(2, "Vendie, Vidi, Vici", true).leadsTo(833, false, o -> true, " or ");
        new Option(3, "Back Room Dealings", true).leadsTo(834, false, o -> true, " or ");
        new Option(6, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(831, "Intrusion", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(832, "Shower Power", "The Inner Wolf Gym") {
      void setup() {
        this.customName = this.name;

        new Option(1, "+Wolf Offence", true).turnCost(1);
        new Option(2, "+Wolf Defence", true).turnCost(1);
      }
    };

    new ChoiceAdventure(833, "Vendie, Vidi, Vici", "The Inner Wolf Gym") {
      void setup() {
        this.customName = this.name;

        new Option(1, "+Wolf Elemental Attacks", true).turnCost(1);
        new Option(2, "+Rabbit", true).turnCost(1);
      }
    };

    new ChoiceAdventure(834, "Back Room Dealings", "The Inner Wolf Gym") {
      void setup() {
        this.customName = this.name;

        new Option(2, "Improved Howling!", true).turnCost(1);
        new Option(3, "+Wolf Lung Capacity", true).turnCost(1);
      }
    };

    new ChoiceAdventure(835, "Barely Tales", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        this.customName = "Grim Brother";

        new Option(1, "30 adv of Soles of Glass").attachEffect("Soles of Glass");
        new Option(2, "30 adv of Angry like the Wolf").attachEffect("Angry like the Wolf");
        new Option(3, "30 adv of Grumpy and Ornery").attachEffect("Grumpy and Ornery");

        new CustomOption(1, "30 adv of +20 initiative");
        new CustomOption(2, "30 adv of +20 max HP, +10 max MP");
        new CustomOption(3, "30 adv of +10 Weapon Damage, +20 Spell Damage");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision != 0) {
          Preferences.setBoolean("_grimBuff", true);
        }
      }
    };

    new ChoiceAdventure(836, "Adventures Who Live in Ice Houses...", null) {
      final Pattern ICEHOUSE_PATTERN = Pattern.compile("perfectly-preserved (.*?), right");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = ICEHOUSE_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          String icehouseMonster = matcher.group(1).toLowerCase();
          String knownBanishes = Preferences.getString("banishedMonsters");
          if (!knownBanishes.contains(icehouseMonster)) {
            // If not already known to be banished, add it
            BanishManager.banishMonster(icehouseMonster, "ice house");
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          BanishManager.removeBanishByBanisher("ice house");
        }
      }
    };

    new ChoiceAdventure(837, "On Purple Pond", "Sweet-Ade Lake") {
      void setup() {
        this.customName = this.name;

        new Option(1, "find out the two children not invading", true).turnCost(1);
        new Option(2, "+1 Moat", true).turnCost(1);
        new Option(3, "gain Candy", true).turnCost(1);
      }
    };

    new ChoiceAdventure(838, "General Mill", "Sweet-Ade Lake") {
      void setup() {
        this.customName = this.name;

        new Option(1, "+1 Moat", true).turnCost(1);
        new Option(2, "gain Candy", true).turnCost(1);
      }
    };

    new ChoiceAdventure(839, "The Sounds of the Undergrounds", "Eager Rice Burrows") {
      void setup() {
        this.customName = this.name;

        new Option(1, "learn what the first two waves will be", true).turnCost(1);
        new Option(2, "+1 Minefield Strength", true).turnCost(1);
        new Option(3, "gain Candy", true).turnCost(1);
      }
    };

    new ChoiceAdventure(840, "Hop on Rock Pops", "Eager Rice Burrows") {
      void setup() {
        this.customName = this.name;

        new Option(1, "+1 Minefield Strength", true).turnCost(1);
        new Option(2, "gain Candy", true).turnCost(1);
      }
    };

    new ChoiceAdventure(841, "Building, Structure, Edifice", "Gumdrop Forest") {
      void setup() {
        this.customName = this.name;

        new Option(1, "increase candy in another location", true).turnCost(1);
        new Option(2, "+2 Random Defense", true).turnCost(1);
        new Option(3, "gain Candy", true).turnCost(1);
      }
    };

    new ChoiceAdventure(842, "The Gingerbread Warehouse", "Gumdrop Forest") {
      void setup() {
        this.customName = this.name;

        new Option(1, "+1 Wall Strength", true).turnCost(1);
        new Option(2, "+1 Poison Jar", true).turnCost(1);
        new Option(3, "+1 Anti-Aircraft Turret", true).turnCost(1);
        new Option(4, "gain Candy", true).turnCost(1);
      }
    };

    new UnknownChoiceAdventure(843);

    new ChoiceAdventure(844, "The Portal to Horrible Parents", "Portal to Terrible Parents") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2).leadsTo(846);
        new Option(3);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          RumpleManager.spyOnParents(request.responseText);
        }
      }
    };

    new ChoiceAdventure(845, "Rumpelstiltskin's Workshop", "Rumpelstiltskin's Workshop") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).leadsTo(849);
        new Option(2).leadsTo(850);
        new Option(3);
        new Option(4);
      }
    };

    new ChoiceAdventure(
        846, "Bartering for the Future of Innocent Children", "Portal to Terrible Parents") {
      void setup() {
        new Option(1).leadsTo(847);
        new Option(2).leadsTo(847);
        new Option(3).leadsTo(847);
        new Option(4).leadsTo(844);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        RumpleManager.pickParent(decision);
      }
    };

    new ChoiceAdventure(847, "Pick Your Poison", "Portal to Terrible Parents") {
      void setup() {
        new Option(1).leadsTo(848);
        new Option(2).leadsTo(848);
        new Option(3).leadsTo(848);
        new Option(4).leadsTo(848);
        new Option(5).leadsTo(848);
        new Option(6).leadsTo(848);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        RumpleManager.pickSin(decision);
      }
    };

    new ChoiceAdventure(848, "Where the Magic Happens", "Portal to Terrible Parents") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(846);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Preferences.setString("grimstoneMaskPath", "gnome");
        RumpleManager.parseAvailableMaterials(request);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision != 4) {
          RumpleManager.recordTrade(request.responseText);
        }
      }
    };

    new ChoiceAdventure(849, "The Practice", "Rumpelstiltskin's Workshop") {
      void setup() {
        // adding the costs isn't important
        // since we immediately parse them
        new Option(1).attachItem(ItemPool.FILLING);
        new Option(2).attachItem(ItemPool.PARCHMENT);
        new Option(3).attachItem(ItemPool.GLASS);
        new Option(4).leadsTo(845);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Preferences.setString("grimstoneMaskPath", "gnome");
        RumpleManager.parseAvailableMaterials(request);
      }
    };

    new ChoiceAdventure(850, "World of Bartercraft", "Rumpelstiltskin's Workshop") {
      void setup() {
        // adding the costs isn't important
        // since we immediately parse them
        new Option(1).attachItem(ItemPool.FILLING, 1, AUTO);
        new Option(2).attachItem(ItemPool.PARCHMENT, 1, AUTO);
        new Option(3).attachItem(ItemPool.GLASS, 1, AUTO);
        new Option(4).leadsTo(845);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Preferences.setString("grimstoneMaskPath", "gnome");
        RumpleManager.parseAvailableMaterials(request);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        RumpleManager.decorateWorkshop(buffer);
      }
    };

    new ChoiceAdventure(851, "Shen Copperhead, Nightclub Owner", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        ShenCopperheadManager.parseInitialShen(request);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        ShenCopperheadManager.postInitialShen(request);
      }
    };

    new ChoiceAdventure(852, "Shen Copperhead, Jerk", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        ShenCopperheadManager.parseShenDemand(request);
      }
    };

    new ChoiceAdventure(853, "Shen Copperhead, Huge Jerk", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        ShenCopperheadManager.parseShenDemand(request);
      }
    };

    new ChoiceAdventure(854, "Shen Copperhead, World's Biggest Jerk", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        ShenCopperheadManager.parseShenFinal(request);
        if (InventoryManager.hasItem(ItemPool.COPPERHEAD_CHARM)
            && InventoryManager.hasItem(ItemPool.COPPERHEAD_CHARM_RAMPANT)) {
          ResultProcessor.autoCreate(ItemPool.TALISMAN);
        }
      }
    };

    new ChoiceAdventure(855, "Behind the 'Stache", "The Copperhead Club") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "don't take initial damage in fights", true);
        new Option(2, "may get priceless diamond after fights", true)
            .attachItem(ItemPool.PRICELESS_DIAMOND, new ImageOnly("diamond"));
        new Option(3, "turn unnamed cocktail into Flamin' Whatshisname after fights", true)
            .attachItem(ItemPool.UNNAMED_COCKTAIL, new DisplayAll("cocktail"))
            .attachItem(ItemPool.FLAMIN_WHATSHISNAME, new DisplayAll("Whatshisname"));
        new Option(4, "get unnamed cocktail + 3-4 random items", true)
            .attachItem(ItemPool.UNNAMED_COCKTAIL, 1, AUTO, new DisplayAll("cocktail"))
            .attachItem("blowdart", 1, AUTO)
            .attachItem(ItemPool.TOMMY_AMMO, 1, AUTO)
            .attachItem("shuriken salad", 1, AUTO)
            .attachItem("throwing fork", 1, AUTO)
            .attachItem("throwing knife", 1, AUTO)
            .attachItem("throwing spoon", 1, AUTO);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        String hazard = Preferences.getString("copperheadClubHazard");
        switch (decision) {
          case 1:
            hazard = "gong";
            break;
          case 2:
            hazard = "ice";
            break;
          case 3:
            hazard = "lantern";
            break;
        }
        Preferences.setString("copperheadClubHazard", hazard);
      }
    };

    new ChoiceAdventure(
        856, "This Looks Like a Good Bush for an Ambush", "A Mob of Zeppelin Protesters") {
      final Pattern LYNYRD_PATTERN =
          Pattern.compile(
              "(?:scare|group of|All) <b>(\\d+)</b> (?:of the protesters|protesters|of them)");
      final AdventureResult LYNYRD_CAP = new AdventureResult("lynyrdskin cap", 0, false);
      final AdventureResult LYNYRD_TUNIC = new AdventureResult("lynyrdskin tunic", 0, false);
      final AdventureResult LYNYRD_BREECHES = new AdventureResult("lynyrdskin breeches", 0, false);
      final AdventureResult MUSKY = new AdventureResult("Musky", 0, true);

      void setup() {
        this.customName = this.name;

        new Option(1, "scare protestors (more with lynyrd gear)", true)
            .turnCost(1)
            .attachItem(LYNYRD_CAP, new DisplayAll(WANT, EQUIPPED_AT_LEAST, 1))
            .attachItem(LYNYRD_TUNIC, new DisplayAll(WANT, EQUIPPED_AT_LEAST, 1))
            .attachItem(LYNYRD_BREECHES, new DisplayAll(WANT, EQUIPPED_AT_LEAST, 1))
            .attachEffect(MUSKY);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        int lynyrd = 3;

        for (AdventureResult ar :
            new AdventureResult[] {LYNYRD_CAP, LYNYRD_TUNIC, LYNYRD_BREECHES}) {
          if (KoLCharacter.hasEquipped(ar)) {
            lynyrd += 5;
          }
        }

        if (KoLConstants.activeEffects.contains(MUSKY)) {
          lynyrd += 3;
        }

        getOption(1)
            .text(
                "scare "
                    + lynyrd
                    + " protestors"
                    + (lynyrd == 21 ? " (max)" : " (more with lynyrd gear)"));
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Matcher lynyrdMatcher = LYNYRD_PATTERN.matcher(request.responseText);
        if (lynyrdMatcher.find()) {
          int protestersScared = StringUtilities.parseInt(lynyrdMatcher.group(1));
          Preferences.increment("zeppelinProtestors", protestersScared);
          RequestLogger.printLine("Scared off " + protestersScared + " protesters");
        }
      }
    };

    new ChoiceAdventure(857, "Bench Warrant", "A Mob of Zeppelin Protesters") {
      final Pattern BENCH_WARRANT_PATTERN =
          Pattern.compile("creep <font color=blueviolet><b>(\\d+)</b></font> of them");

      void setup() {
        this.customName = this.name;

        new Option(1, "creep protestors (more with sleaze damage/sleaze spell damage)", true)
            .turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        // TODO calculate dirty pears... sigh...
        double sleaze = KoLCharacter.currentNumericModifier(Modifiers.SLEAZE_DAMAGE);
        double sleazeSpell = KoLCharacter.currentNumericModifier(Modifiers.SLEAZE_SPELL_DAMAGE);
        double kills = Math.sqrt(sleaze + sleazeSpell);

        String text = "creep ";

        if (kills <= 3.0) {
          text += "3";
        } else {
          text += (int) Math.floor(kills) + "-" + (int) Math.ceil(kills);
        }

        text += " protestors (more with sleaze damage/sleaze spell damage)";

        getOption(1).text(text);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Matcher benchWarrantMatcher = BENCH_WARRANT_PATTERN.matcher(request.responseText);
        if (benchWarrantMatcher.find()) {
          int protestersCreeped = StringUtilities.parseInt(benchWarrantMatcher.group(1));
          Preferences.increment("zeppelinProtestors", protestersCreeped);
          RequestLogger.printLine("Creeped out " + protestersCreeped + " protesters");
        }
      }
    };

    new ChoiceAdventure(858, "Fire Up Above", "A Mob of Zeppelin Protesters") {
      void setup() {
        this.customName = this.name;

        new Option(1, "set fire to 3 protestors (10 with Flamin' Whatshisname)", true)
            .turnCost(1)
            // don't add as a cost since optional
            .attachItem(ItemPool.FLAMIN_WHATSHISNAME, new DisplayAll("Whatshisname"));
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("Flamin' Whatshisname")) {
          getOption(decision).attachItem(ItemPool.FLAMIN_WHATSHISNAME, -1, MANUAL);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("three nearest protesters")) {
          Preferences.increment("zeppelinProtestors", 3);
          RequestLogger.printLine("Soaked 3 protesters");
        } else if (request.responseText.contains("Flamin' Whatshisname")) {
          Preferences.increment("zeppelinProtestors", 10);
          RequestLogger.printLine("Set fire to 10 protesters");
        }
      }
    };

    new ChoiceAdventure(859, "Upping Your Grade", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).leadsTo(860);
        new Option(2).leadsTo(861);
        new Option(3).leadsTo(862);
        new Option(4).leadsTo(863);
        new Option(5).leadsTo(864);
        new Option(6).leadsTo(865);
      }
    };

    new ChoiceAdventure(860, "Another Tired Retread", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeTires", "Racing Slicks");
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeTires", "Spiky Tires");
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeTires", "Snow Tires");
        }
      }
    };

    new ChoiceAdventure(861, "Station of the Gas", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeGasTank", "Large Capacity Tank");
          KoLCharacter.setDesertBeachAvailable();
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeGasTank", "Extra-Buoyant Tank");
          Preferences.setInteger("lastIslandUnlock", KoLCharacter.getAscensions());
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeGasTank", "Nitro-Burnin' Funny Tank");
        }
      }
    };

    new ChoiceAdventure(862, "Me and Cinderella Put It All Together", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeHeadlight", "Ultrabright Yellow Bulb");
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeHeadlight", "Party Bulb");
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeHeadlight", "Blacklight Bulb");
        }
      }
    };

    new ChoiceAdventure(863, "Endowing the Cowling", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeCowling", "Ghost Vacuum");
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeCowling", "Rocket Launcher");
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeCowling", "Sweepy Red Light");
        }
      }
    };

    new ChoiceAdventure(864, "Diving into the Mufflers", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeMuffler", "Extra-Loud Muffler");
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeMuffler", "Extra-Quiet Muffler");
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeMuffler", "Extra-Smelly Muffler");
        }
      }
    };

    new ChoiceAdventure(865, "Ayy, Sit on It", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4).leadsTo(859);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setString("peteMotorbikeSeat", "Massage Seat");
        } else if (decision == 2) {
          Preferences.setString("peteMotorbikeSeat", "Deep Seat Cushions");
        } else if (decision == 3) {
          Preferences.setString("peteMotorbikeSeat", "Sissy Bar");
        }
      }
    };

    new ChoiceAdventure(
        866, "Methinks the Protesters Doth Protest Too Little", "A Mob of Zeppelin Protesters") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1).leadsTo(856, true, o -> o.index == 1);
        new Option(2).leadsTo(857, true, o -> o.index == 1);
        new Option(3).leadsTo(858, false, o -> o.index == 1);

        new CustomOption(1, "This Looks Like a Good Bush for an Ambush");
        new CustomOption(2, "Bench Warrant");
        new CustomOption(3, "Fire Up Above");
      }
    };

    new ChoiceAdventure(867, "Sneaky Peterskills", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(868, "Repeat As Pete", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(869, "End of Pete Road", null) {
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

    new ChoiceAdventure(870, "Hair Today", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(4).turnCost(1);
      }
    };

    new ChoiceAdventure(871, null, null) { // inspecting Motorbike
      final Pattern MOTORBIKE_TIRES_PATTERN = Pattern.compile("<b>Tires:</b> (.*?)?\\(");
      final Pattern MOTORBIKE_GASTANK_PATTERN = Pattern.compile("<b>Gas Tank:</b> (.*?)?\\(");
      final Pattern MOTORBIKE_HEADLIGHT_PATTERN = Pattern.compile("<b>Headlight:</b> (.*?)?\\(");
      final Pattern MOTORBIKE_COWLING_PATTERN = Pattern.compile("<b>Cowling:</b> (.*?)?\\(");
      final Pattern MOTORBIKE_MUFFLER_PATTERN = Pattern.compile("<b>Muffler:</b> (.*?)?\\(");
      final Pattern MOTORBIKE_SEAT_PATTERN = Pattern.compile("<b>Seat:</b> (.*?)?\\(");

      void setup() {
        this.canWalkFromChoice = true;

        // nothing?
      }

      @Override
      void visitChoice(GenericRequest request) {
        Matcher matcher = MOTORBIKE_TIRES_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeTires", matcher.group(1).trim());
        }
        matcher = MOTORBIKE_GASTANK_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeGasTank", matcher.group(1).trim());
          if (Preferences.getString("peteMotorbikeGasTank").equals("Large Capacity Tank")) {
            KoLCharacter.setDesertBeachAvailable();
          } else if (Preferences.getString("peteMotorbikeGasTank").equals("Extra-Buoyant Tank")) {
            Preferences.setInteger("lastIslandUnlock", KoLCharacter.getAscensions());
          }
        }
        matcher = MOTORBIKE_HEADLIGHT_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeHeadlight", matcher.group(1).trim());
        }
        matcher = MOTORBIKE_COWLING_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeCowling", matcher.group(1).trim());
        }
        matcher = MOTORBIKE_MUFFLER_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeMuffler", matcher.group(1).trim());
        }
        matcher = MOTORBIKE_SEAT_PATTERN.matcher(request.responseText);
        if (matcher.find()) {
          Preferences.setString("peteMotorbikeSeat", matcher.group(1).trim());
        }
      }
    };

    new ChoiceAdventure(872, "Drawn Onward", null) {
      final Pattern PHOTO_PATTERN = Pattern.compile("<select name=\"(.*?)\".*?</select>");

      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        Matcher matcher = PHOTO_PATTERN.matcher(buffer.toString());
        while (matcher.find()) {
          String photo = matcher.group(1);
          String find = matcher.group(0);
          String replace = null;
          if (photo.equals("photo1")) {
            if (find.contains("2259")) {
              replace =
                  StringUtilities.singleStringReplace(
                      find, "<option value=\"2259\">", "<option value=\"2259\" selected>");
            }
          } else if (photo.equals("photo2")) {
            if (find.contains("7264")) {
              replace =
                  StringUtilities.singleStringReplace(
                      find, "<option value=\"7264\">", "<option value=\"7264\" selected>");
            }
          } else if (photo.equals("photo3")) {
            if (find.contains("7263")) {
              replace =
                  StringUtilities.singleStringReplace(
                      find, "<option value=\"7263\">", "<option value=\"7263\" selected>");
            }
          } else if (photo.equals("photo4")) {
            if (find.contains("7265")) {
              replace =
                  StringUtilities.singleStringReplace(
                      find, "<option value=\"7265\">", "<option value=\"7265\" selected>");
            }
          }

          if (replace != null) {
            StringUtilities.singleStringReplace(buffer, find, replace);
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Handle quest in Ed
        if (request.responseText.contains("Rot in a jar of dog paws!")) {
          QuestDatabase.setQuestProgress(Quest.PALINDOME, QuestDatabase.FINISHED);
          getOption(decision).attachItem(ItemPool.ED_FATS_STAFF, -1, MANUAL);
          if (InventoryManager.getCount(ItemPool.ED_EYE) == 0
              && InventoryManager.getCount(ItemPool.ED_AMULET) == 0) {
            QuestDatabase.setQuestProgress(Quest.MACGUFFIN, QuestDatabase.FINISHED);
          }
        }
      }
    };

    new ChoiceAdventure(873, "Rod Nevada, Vendor", "Inside the Palindome") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "photograph of a red nugget")
            .turnCost(1)
            .attachMeat(-500, MANUAL)
            .attachItem(ItemPool.PHOTOGRAPH_OF_RED_NUGGET, 1, AUTO);
        new Option(2, "skip adventure");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1
            && !request.responseText.contains("had Rod not run off before you got the chance.")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(874, "Add a Password", null) {
      void setup() {
        new Option(1, "we're not looking!");
      }
    };

    new ChoiceAdventure(875, "Welcome To Our ool Table", "The Haunted Billiards Room") {
      final Pattern POOL_SKILL_PATTERN = Pattern.compile("(\\d+) Pool Skill</b>");

      void setup() {
        this.customName = "Pool Table";

        new Option(1, "try to beat ghost", true).turnCost(1);
        new Option(2, "increase pool skill by 1 (+1 with chalky hand) (+2 with staff of fats)")
            .turnCost(1)
            .attachEffect("chalky hand", new ImageOnly("hand"))
            .attachItem(
                ItemPool.STAFF_OF_FATS, new DisplayAll("of fats", WANT, EQUIPPED_AT_LEAST, 1));
        new Option(3, "skip adventure", true).entersQueue(false);

        new CustomOption(2, "improve pool skill");
      }

      @Override
      void visitChoice(GenericRequest request) {
        int poolSkill = KoLCharacter.estimatedPoolSkill();
        StringBuffer text = new StringBuffer("pool skill at " + poolSkill);

        // rolls a number between 14 and 18 (14, 15, 16, 17, 18)
        // need equal or greater
        int chance = Math.max(Math.min(poolSkill, 18), 13) - 13; // x/5
        int chancePercent = chance * 20; // x/100

        text.append(" (");
        text.append(chancePercent);
        text.append("% chance of beating him)");

        getOption(1).text(text.toString());
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Matcher poolSkillMatcher = POOL_SKILL_PATTERN.matcher(request.responseText);
        if (poolSkillMatcher.find()) {
          Preferences.increment("poolSkill", StringUtilities.parseInt(poolSkillMatcher.group(1)));
        }
      }
    };

    new ChoiceAdventure(876, "One Simple Nightstand", "The Haunted Bedroom") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "old leather wallet", true).attachItem("old leather wallet", 1, AUTO);
        new Option(2, "muscle substats", true);
        new Option(3, "200 muscle substats (with ghost key)", true)
            .attachItem(ItemPool.GHOST_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip", true);
      }
    };

    new ChoiceAdventure(877, "One Mahogany Nightstand", "The Haunted Bedroom") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "old coin purse or half a memo", true)
            .attachItem("old coin purse", 1, AUTO, new DisplayAll("purse"))
            .attachItem("half of a memo", 1, AUTO);
        new Option(2, "take damage", true);
        new Option(3, "quest item", true);
        new Option(4, "gain ~1000 meat (with ghost key)", true)
            .attachMeat(1000, AUTO)
            .attachItem(ItemPool.GHOST_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(3);
        AscensionClass classType = KoLCharacter.getAscensionClass();

        if (KoLCharacter.isZombieMaster()
            || classType == AscensionClass.GELATINOUS_NOOB
            || classType == AscensionClass.AVATAR_OF_JARLSBERG
            || classType == AscensionClass.AVATAR_OF_SNEAKY_PETE
            || classType == AscensionClass.ED
            || KoLCharacter.isVampyre()
            || classType == AscensionClass.PLUMBER) {
          option.text("get nothing");
        } else {
          option.attachItem(
              ItemPool.SPOOKYRAVEN_SPECTACLES,
              new DisplayAll("spectacles", NEED, EQUIPPED_AT_LEAST, 1));

          String text = "get ";

          switch (classType) {
            case SEAL_CLUBBER:
            case AVATAR_OF_BORIS:
              text += "tattered wolf standard";
              option.attachItem(
                  ItemPool.TATTERED_WOLF_STANDARD, 1, AUTO, new DisplayAll("standard"));
              break;
            case TURTLE_TAMER:
              text += "tattered snake standard";
              option.attachItem(
                  ItemPool.TATTERED_SNAKE_STANDARD, 1, AUTO, new DisplayAll("standard"));
              break;
            case PASTAMANCER:
            case SAUCEROR:
              text += "English to A. F. U. E. Dictionary";
              option.attachItem(
                  ItemPool.ENGLISH_TO_A_F_U_E_DICTIONARY, 1, AUTO, new DisplayAll("Dictionary"));
              break;
            case DISCO_BANDIT:
            case ACCORDION_THIEF:
              text += "bizarre illegible sheet music";
              option.attachItem(
                  ItemPool.BIZARRE_ILLEGIBLE_SHEET_MUSIC, 1, AUTO, new DisplayAll("sheet music"));
              break;
            default:
              text += "???";
              break;
          }

          text += " (first time only) (with Lord Spookyraven's spectacles)";

          option.text(text);
        }
      }
    };

    new ChoiceAdventure(878, "One Ornate Nightstand", "The Haunted Bedroom") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "gain ~500 meat", true).attachMeat(500, AUTO);
        new Option(2, "mysticality substats", true);
        new Option(3, "Lord Spookyraven's spectacles (first time only)", true)
            .attachItem(ItemPool.SPOOKYRAVEN_SPECTACLES, 1, AUTO);
        new Option(4, "disposable instant camera", true)
            .attachItem(ItemPool.DISPOSABLE_CAMERA, 1, AUTO);
        new Option(5, "200 mysticality substats (with ghost key)", true)
            .attachItem(ItemPool.GHOST_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip", true);
      }
    };

    new ChoiceAdventure(879, "One Rustic Nightstand", "The Haunted Bedroom") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "moxie substats", true);
        new Option(2, "grouchy restless spirit or empty drawer", true)
            .attachItem("grouchy restless spirit", 1, AUTO);
        new Option(3, "enter combat with mistress (1)", true)
            .attachItem(ItemPool.ANTIQUE_HAND_MIRROR, 1, AUTO);
        new Option(4, "(!!!RARE!!!) Engorged Sausages and You (!!!RARE!!!)")
            .attachItem(ItemPool.ENGORGED_SAUSAGES_AND_YOU, 1, AUTO);
        new Option(5, "200 moxie substats (with ghost key)", true)
            .attachItem(ItemPool.GHOST_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip", true);

        new CustomOption(4, "Engorged Sausages and You or moxie");
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        ChoiceManager.defaultRegister(this.choice, decision);
        RequestLogger.updateSessionLog(urlString);

        if (decision == 3) {
          // Option 3 redirects to a fight with the remains of a
          // jilted mistress. Unlike other such redirections,
          // this takes a turn.
          RequestLogger.registerLocation("The Haunted Bedroom");
        }

        return true;
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(3);

        switch (KoLCharacter.getAscensionClass()) {
          case SEAL_CLUBBER:
            option.attachItem(ItemPool.CHINTZY_SEAL_PENDANT, 1, AUTO);
            break;
          case TURTLE_TAMER:
            option.attachItem(ItemPool.CHINTZY_TURTLE_BROOCH, 1, AUTO);
            break;
          case PASTAMANCER:
            option.attachItem(ItemPool.CHINTZY_NOODLE_RING, 1, AUTO);
            break;
          case SAUCEROR:
            option.attachItem(ItemPool.CHINTZY_SAUCEPAN_EARRING, 1, AUTO);
            break;
          case DISCO_BANDIT:
            option.attachItem(ItemPool.CHINTZY_DISCO_BALL_PENDANT, 1, AUTO);
            break;
          case ACCORDION_THIEF:
            option.attachItem(ItemPool.CHINTZY_ACCORDION_PIN, 1, AUTO);
            break;
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        boolean sausagesAvailable = responseText.contains("Check under the nightstand");

        // If the player wants the sausage book and it is
        // available, take it.
        if (decision.equals("4")) {
          return sausagesAvailable ? "4" : "1";
        }

        return decision;
      }
    };

    new ChoiceAdventure(880, "One Elegant Nightstand", "The Haunted Bedroom") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = this.name;

        new Option(1, "Lady Spookyraven's finest gown (first time only)", true)
            .attachItem(ItemPool.FINEST_GOWN, 1, AUTO);
        new Option(2, "elegant nightstick", true).attachItem("elegant nightstick", 1, AUTO);
        new Option(3, "100 of each stats (with ghost key)", true)
            .attachItem(ItemPool.GHOST_KEY, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip", true);
      }
    };

    new ChoiceAdventure(881, "Never Gonna Make You Up", "The Haunted Bathroom") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(882, "Off the Rack", "The Haunted Bathroom") {
      void setup() {
        this.customName = "Bathroom Towel";

        new Option(1, null, true).turnCost(1).attachItem(ItemPool.TOWEL, 1, AUTO);
        new Option(2, "waste adventure", true).turnCost(1);
      }
    };

    new UnknownChoiceAdventure(883);

    new ChoiceAdventure(884, "Chasin' Babies", "The Haunted Laboratory") { // Laboratory
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(4);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("Close the jar!")) {
          getOption(4).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(885, "Chasin' Babies", "The Haunted Nursery") { // Nursery
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(4);
      }
    };

    new ChoiceAdventure(886, "Chasin' Babies", "The Haunted Storage Room") { // Storage Room
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(4);
      }
    };

    new UnknownChoiceAdventure(887);

    new ChoiceAdventure(888, "Take a Look, it's in a Book!", "The Haunted Library") { // Rise
      void setup() {
        this.customName = "Rise of Spookyraven";

        new Option(1, "background history").leadsTo(86);
        new Option(2, "cooking recipe").turnCost(1);
        new Option(3).leadsTo(88, true, o -> true);
        new Option(4, "skip adventure and banish both versions for 10 adventures")
            .entersQueue(false);

        new CustomOption(
            1,
            "boost mysticality substats",
            () -> {
              Preferences.setString("choiceAdventure888", "3");
              Preferences.setString("choiceAdventure88", "1");
            });
        new CustomOption(
            2,
            "boost moxie substats",
            () -> {
              Preferences.setString("choiceAdventure888", "3");
              Preferences.setString("choiceAdventure88", "2");
            });
        new CustomOption(
            3,
            "acquire mysticality skill",
            () -> {
              Preferences.setString("choiceAdventure888", "3");
              Preferences.setString("choiceAdventure88", "3");
            });
        new CustomOption(4, "ignore this adventure");

        this.customLoad =
            () -> {
              int index888 = Preferences.getInteger("choiceAdventure888");
              switch (index888) {
                default:
                  System.out.println("Invalid setting " + index888 + " for choiceAdventure888");
                case 0:
                  return 0;
                case 3:
                  int index88 = Preferences.getInteger("choiceAdventure88");
                  switch (index88) {
                    case 1:
                    case 2:
                    case 3:
                      return index88;
                  }
                  return 0;
                case 4:
                  return 4;
              }
            };
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 4) {
          TurnCounter.startCounting(10, "Take a Look Banished loc=*", "cookbook.gif");
        }
      }
    };

    new ChoiceAdventure(889, "Take a Look, it's in a Book!", "The Haunted Library") { // Fall
      void setup() {
        this.customName = "Fall of Spookyraven";

        new Option(1, "background history").leadsTo(87);
        new Option(2, "cocktailcrafting recipe").turnCost(1);
        new Option(3, "muscle substats", true).turnCost(1);
        new Option(4).attachItem(ItemPool.DICTIONARY, 1, AUTO, new ImageOnly()).turnCost(1);
        new Option(5, "skip adventure and banish both versions for 10 adventures")
            .entersQueue(false);

        new CustomOption(4, "take dictionary, then skip adventure");
        new CustomOption(5, "skip adventure");
      }

      @Override
      void visitChoice(GenericRequest request) {
        int statGain = Math.min(KoLCharacter.getBaseMainstat(), 75);

        getOption(3).text("gain " + statGain + " muscle substats, get ~12 spooky damage");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("4") && !responseText.contains("Read the dictionary")) {
          return "5";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 5) {
          TurnCounter.startCounting(10, "Take a Look Banished loc=*", "cookbook.gif");
        }
      }
    };

    new ChoiceAdventure(890, "Lights Out in the Storage Room", "The Haunted Storage Room") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        // the ChoiceSelectPanel for all the Lights Out in-- adventures
        this.customName = "Lights Out";
        this.customZones.clear();
        this.customZones.add("Manor1");

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);

        new CustomOption(
            0, "show in browser", () -> Preferences.setInteger("lightsOutAutomation", 0));
        new CustomOption(
            1,
            "take quest option if available",
            () -> Preferences.setInteger("lightsOutAutomation", 1));
        new CustomOption(
            2, "skip adventure", () -> Preferences.setInteger("lightsOutAutomation", 2));

        this.customLoad =
            () -> {
              int lightsOutIndex = Preferences.getInteger("lightsOutAutomation");
              switch (lightsOutIndex) {
                default:
                  System.out.println(
                      "Invalid setting " + lightsOutIndex + " for lightsOutAutomation");
                  // fall-through
                case 0:
                  return 0;
                case 1:
                  return 1;
                case 2:
                  return 2;
              }
            };

        this.customPreferencesToListen.add("lightsOutAutomation");
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
        if (request.responseText.contains("BUT AIN'T NO ONE CAN GET A STAIN OUT LIKE OLD AGNES!")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Laundry Room");
        }
      }
    };

    new ChoiceAdventure(891, "Lights Out in the Laundry Room", "The Haunted Laundry Room") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        if (request.responseText.contains("DO YOU SEE THE STAIN UPON MY TOWEL?")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Bathroom");
        }
      }
    };

    new ChoiceAdventure(892, "Lights Out in the Bathroom", "The Haunted Bathroom") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        if (request.responseText.contains("THE STAIN HAS BEEN LIFTED")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Kitchen");
        }
      }
    };

    new ChoiceAdventure(893, "Lights Out in the Kitchen", "The Haunted Kitchen") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        if (request.responseText.contains("If You Give a Demon a Brownie")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Library");
        }
      }
    };

    new ChoiceAdventure(894, "Lights Out in the Library", "The Haunted Library") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        if (request.responseText.contains("If You Give a Demon a Brownie")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Ballroom");
        }
      }
    };

    new ChoiceAdventure(895, "Lights Out in the Ballroom", "The Haunted Ballroom") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        if (request.responseText.contains("The Flowerbed of Unearthly Delights")
            && !Preferences.getString("nextSpookyravenElizabethRoom").equals("none")) {
          Preferences.setString("nextSpookyravenElizabethRoom", "The Haunted Gallery");
        }
      }
    };

    new ChoiceAdventure(896, "Lights Out in the Gallery", "The Haunted Gallery") {
      void setup() {
        this.isSuperlikely = true;
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
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
        // The correct option leads to a combat with Elizabeth.
        // If you win, we will set "nextSpookyravenElizabethRoom" to "none"
      }
    };

    new ChoiceAdventure(897, "Lights Out in the Bedroom", "The Haunted Bedroom") {
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
        if (request.responseText.contains("restock his medical kit in the nursery")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Nursery");
        }
      }
    };

    new ChoiceAdventure(898, "Lights Out in the Nursery", "The Haunted Nursery") {
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
        if (request.responseText.contains("This afternoon we're burying Crumbles")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Conservatory");
        }
      }
    };

    new ChoiceAdventure(899, "Lights Out in the Conservatory", "The Haunted Conservatory") {
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
        if (request.responseText.contains("Crumbles isn't buried very deep")
            && !Preferences.getString("nextSpookyravenStephenRoom").equals("none")) {
          Preferences.setString("nextSpookyravenStephenRoom", "The Haunted Billiards Room");
        }
      }
    };
  }
}
