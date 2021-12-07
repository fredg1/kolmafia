package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestEditorKit;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.SpaaaceRequest;
import net.sourceforge.kolmafia.request.TavernRequest;
import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.BugbearManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.GameproManager;
import net.sourceforge.kolmafia.session.HobopolisManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.LostKeyManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.SafetyShelterManager;
import net.sourceforge.kolmafia.session.VampOutManager;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase500to599 extends ChoiceAdventureDatabase {
  final void add500to599() {
    new UnknownChoiceAdventure(500);

    new UnknownChoiceAdventure(501);

    new ChoiceAdventure(502, "Arboreal Respite", "The Spooky Forest") {
      void setup() {
        this.neverEntersQueue = true;

        this.customName = "Spooky Forest";

        new Option(1).leadsTo(503, true, o -> true);
        new Option(2).leadsTo(505, true, o -> true);
        new Option(3).leadsTo(506, true, o -> true);

        new CustomOption(
            1,
            "mosquito larva or spooky mushrooms",
            () -> {
              Preferences.setString("choiceAdventure502", "2");
              Preferences.setString("choiceAdventure505", "1");
            });
        new CustomOption(
            2,
            "Spooky-Gro fertilizer",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "2");
            });
        new CustomOption(
            3,
            "spooky sapling & sell bar skins",
            () -> {
              Preferences.setString("choiceAdventure502", "1");
              Preferences.setString("choiceAdventure503", "3");
              // If we have no Spooky Sapling
              // Preferences.setString("choiceAdventure504", "3");
              // If we have bear skins:
              // Preferences.setString("choiceAdventure504", "2");
              // Exit choice
              Preferences.setString("choiceAdventure504", "4");
            });
        new CustomOption(
            4,
            "Spooky Temple map then skip adventure",
            () -> {
              // Without tree-holed coin
              Preferences.setString("choiceAdventure502", "2");
              Preferences.setString("choiceAdventure505", "2");
              // With tree-holed coin
              // Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "3");
              Preferences.setString("choiceAdventure507", "1");
            });
        new CustomOption(
            5,
            "meet vampire hunter",
            () -> {
              Preferences.setString("choiceAdventure502", "1");
              Preferences.setString("choiceAdventure503", "2");
            });
        new CustomOption(
            6,
            "meet vampire",
            () -> {
              Preferences.setString("choiceAdventure502", "2");
              Preferences.setString("choiceAdventure505", "3");
            });
        new CustomOption(
            7,
            "gain meat",
            () -> {
              Preferences.setString("choiceAdventure502", "1");
              Preferences.setString("choiceAdventure503", "1");
            });
        new CustomOption(
            8,
            "loot Seal Clubber corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "1");
              Preferences.setString("choiceAdventure27", "1");
            });
        new CustomOption(
            9,
            "loot Turtle Tamer corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "1");
              Preferences.setString("choiceAdventure27", "2");
            });
        new CustomOption(
            10,
            "loot Pastamancer corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "2");
              Preferences.setString("choiceAdventure28", "1");
            });
        new CustomOption(
            11,
            "loot Sauceror corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "2");
              Preferences.setString("choiceAdventure28", "2");
            });
        new CustomOption(
            12,
            "loot Disco Bandit corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "3");
              Preferences.setString("choiceAdventure29", "1");
            });
        new CustomOption(
            13,
            "loot Accordion Thief corpse",
            () -> {
              Preferences.setString("choiceAdventure502", "3");
              Preferences.setString("choiceAdventure506", "1");
              Preferences.setString("choiceAdventure26", "3");
              Preferences.setString("choiceAdventure29", "2");
            });

        this.customLoad =
            () -> {
              int index502 = Preferences.getInteger("choiceAdventure502");
              switch (index502) {
                default:
                  System.out.println("Invalid setting " + index502 + " for choiceAdventure502");
                  // fall-through
                case 0: // Manual Control
                  return 0;
                case 1:
                  int index503 = Preferences.getInteger("choiceAdventure503");
                  switch (index503) {
                    default:
                      System.out.println("Invalid setting " + index503 + " for choiceAdventure503");
                    case 0:
                      return 0;
                    case 1: // Get Meat
                      return 7;
                    case 2: // Meet Vampire Hunter
                      return 5;
                    case 3: // Spooky Sapling & Sell Bar Skins
                      return 3;
                  }
                case 2:
                  int index505 = Preferences.getInteger("choiceAdventure505");
                  switch (index505) {
                    default:
                      System.out.println("Invalid setting " + index505 + " for choiceAdventure505");
                    case 0:
                      return 0;
                    case 1: // Mosquito Larva or Spooky Mushrooms
                      return 1;
                    case 2: // Tree-holed coin -> Spooky Temple Map
                      return 4;
                    case 3: // Meet Vampire
                      return 6;
                  }
                case 3:
                  int index506 = Preferences.getInteger("choiceAdventure506");
                  switch (index506) {
                    default:
                      System.out.println("Invalid setting " + index506 + " for choiceAdventure506");
                    case 0:
                      return 0;
                    case 1: // Forest Corpses
                      int index26 = Preferences.getInteger("choiceAdventure26");
                      switch (index26) {
                        default:
                          System.out.println(
                              "Invalid setting " + index26 + " for choiceAdventure26");
                        case 0:
                          return 0;
                        case 1: // muscle classes
                          int index27 = Preferences.getInteger("choiceAdventure27");
                          switch (index27) {
                            default:
                              System.out.println(
                                  "Invalid setting " + index27 + " for choiceAdventure27");
                            case 0:
                              return 0;
                            case 1:
                              return 8;
                            case 2:
                              return 9;
                          }
                        case 2: // mysticality classes
                          int index28 = Preferences.getInteger("choiceAdventure28");
                          switch (index28) {
                            default:
                              System.out.println(
                                  "Invalid setting " + index28 + " for choiceAdventure28");
                            case 0:
                              return 0;
                            case 1:
                              return 10;
                            case 2:
                              return 11;
                          }
                        case 3: // moxie classes
                          int index29 = Preferences.getInteger("choiceAdventure29");
                          switch (index29) {
                            default:
                              System.out.println(
                                  "Invalid setting " + index29 + " for choiceAdventure29");
                            case 0:
                              return 0;
                            case 1:
                              return 12;
                            case 2:
                              return 13;
                          }
                      }
                    case 2: // Spooky-Gro Fertilizer
                      return 2;
                    case 3: // Spooky Temple Map
                      return 4;
                  }
              }
            };
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (decision.equals("2")) {
          // mosquito larva, tree-holed coin, vampire
          if (!Preferences.getString("choiceAdventure505").equals("2")) {
            return decision;
          }

          // We want a tree-holed coin. If we already
          // have one, get Spooky Temple Map instead
          if (InventoryManager.getCount(ItemPool.TREE_HOLED_COIN) > 0) {
            return "3";
          }

          // We don't have a tree-holed coin. Either
          // obtain one or exit without consuming an
          // adventure
        }
        return decision;
      }
    };

    new ChoiceAdventure(503, "The Road Less Traveled", "The Spooky Forest") {
      final AdventureResult WOODEN_STAKES = ItemPool.get(ItemPool.WOODEN_STAKES, 1);
      final AdventureResult VAMPIRE_HEART = ItemPool.get(ItemPool.VAMPIRE_HEART);

      void setup() {
        new Option(1, "gain some meat").turnCost(1).attachMeat(45, AUTO);
        new Option(2);
        new Option(3, "buy spooky sapling and/or sell bar skins")
            .attachItem(ItemPool.SPOOKY_SAPLING, new DisplayAll("sapling"))
            .attachItem(ItemPool.BAR_SKIN)
            .leadsTo(504, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(2);

        if (VAMPIRE_HEART.getCount(KoLConstants.inventory) > 0) {
          option.text("trade vampire hearts").attachItem(VAMPIRE_HEART).leadsTo(47, true);
        } else if (WOODEN_STAKES.getCount(KoLConstants.inventory) == 0) {
          option
              .text("get wooden stakes")
              .turnCost(1)
              .attachItem(WOODEN_STAKES, AUTO, new ImageOnly());
        } else {
          option
              .text("skip adventure")
              .entersQueue(false)
              .attachItem(VAMPIRE_HEART, new DisplayAll(WANT, AT_LEAST, 1));
        }
      }
    };

    new ChoiceAdventure(504, "Tree's Last Stand", "The Spooky Forest") {
      final AdventureResult BAR_SKIN = ItemPool.get(ItemPool.BAR_SKIN);
      final Pattern BAR_SKIN_PATTERN =
          Pattern.compile("Sell him all your bar skins for (\\d+) Meat");

      void setup() {
        new Option(1).attachItem(BAR_SKIN.getInstance(1), MANUAL).attachMeat(75, AUTO);
        new Option(2);
        new Option(3, "buy spooky sapling")
            .attachItem(ItemPool.SPOOKY_SAPLING, 1, AUTO)
            .attachMeat(-100, MANUAL);
        new Option(4, "end this adventure").turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(2);
        int skins;

        if (request == null) {
          skins = BAR_SKIN.getCount(KoLConstants.inventory);
        } else {
          Matcher matcher = BAR_SKIN_PATTERN.matcher(request.responseText);

          if (matcher.find()) {
            int offer = StringUtilities.parseInt(matcher.group(1));
            skins = offer / 75;
          } else {
            skins = BAR_SKIN.getCount(KoLConstants.inventory);
          }
        }

        option.attachMeat(skins * 75, AUTO).attachItem(BAR_SKIN.getInstance(-skins), MANUAL);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If we have Bar Skins, sell them all
        if (InventoryManager.getCount(ItemPool.BAR_SKIN) > 1) {
          return "2";
        }
        if (InventoryManager.getCount(ItemPool.BAR_SKIN) > 0) {
          return "1";
        }

        // If we don't have a Spooky Sapling, buy one
        // unless we've already unlocked the Hidden Temple
        //
        // We should buy one if it is on our conditions - i.e.,
        // the player is intentionally collecting them - but we
        // have to make sure that each purchased sapling
        // decrements the condition so we don't loop and buy
        // too many.

        if (InventoryManager.getCount(ItemPool.SPOOKY_SAPLING) == 0
            && !KoLCharacter.getTempleUnlocked()
            && KoLCharacter.getAvailableMeat() >= 100) {
          return "3";
        }

        // Otherwise, exit this choice
        return "4";
      }
    };

    new ChoiceAdventure(505, "Consciousness of a Stream", "The Spooky Forest") {
      final AdventureResult SPOOKY_MUSHROOMS = new AdventureResult("spooky mushroom", 3, false);
      final AdventureResult TREE_HOLED_COIN = ItemPool.get(ItemPool.TREE_HOLED_COIN, 1);
      final AdventureResult SPOOKY_MAP = ItemPool.get(ItemPool.SPOOKY_MAP);

      void setup() {
        new Option(1).turnCost(1);
        new Option(2);
        new Option(3).leadsTo(46, false, o -> true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(1);

        if (QuestDatabase.isQuestBefore(Quest.LARVA, "step1")) {
          option.text("mosquito larva").attachItem(ItemPool.MOSQUITO_LARVA, 1, AUTO);
        } else {
          option.text("3 spooky mushrooms").attachItem(SPOOKY_MUSHROOMS, AUTO);
        }

        option = getOption(2);

        if (!KoLCharacter.getTempleUnlocked()
            && TREE_HOLED_COIN.getCount(KoLConstants.inventory) == 0
            && SPOOKY_MAP.getCount(KoLConstants.inventory) == 0) {
          option
              .text("tree-holed coin")
              .turnCost(1)
              .attachItem(TREE_HOLED_COIN, AUTO)
              .attachMeat(300, AUTO);
        } else {
          option.text("skip adventure").entersQueue(false);
        }

        if (request == null) {
          // remove the filter
          getOption(3).reset("get stats or fight a spooky vampire").leadsTo(46);
        }
      }
    };

    new ChoiceAdventure(506, "Through Thicket and Thinnet", "The Spooky Forest") {
      void setup() {
        new Option(1, "gain a class's starter items").leadsTo(26);
        new Option(2, "spooky-grow fertilizer")
            .turnCost(1)
            .attachItem(ItemPool.SPOOKY_FERTILIZER, 1, AUTO);
        new Option(3)
            .leadsTo(507, false, o -> o.index == 1)
            .attachItem(ItemPool.TREE_HOLED_COIN, new DisplayAll(NEED, AT_LEAST, 1));
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          if (InventoryManager.getCount(ItemPool.TREE_HOLED_COIN) == 0) {
            getOption(3).reset();
          }
        }
      }
    };

    new ChoiceAdventure(507, "O Lith, Mon", "The Spooky Forest") {
      void setup() {
        new Option(1, "gain Spooky Temple map")
            .turnCost(1)
            .attachItem(ItemPool.SPOOKY_MAP, 1, AUTO, new ImageOnly())
            .attachItem(ItemPool.TREE_HOLED_COIN, -1, MANUAL, new NoDisplay());
        new Option(2, "skip adventure").entersQueue(false);
        new Option(3, "skip adventure").entersQueue(false);
      }
    };

    new ChoiceAdventure(508, "Pants-Gazing", "Item-Driven") {
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
        if (request.responseText.contains("You acquire an effect")) {
          Preferences.increment("_gapBuffs", 1);
        }
      }
    };

    new ChoiceAdventure(509, "Of Course!", "The Typical Tavern Cellar") {
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

    new ChoiceAdventure(510, "Those Who Came Before You", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(511, "If it's Tiny, is it Still a Mansion?", "The Typical Tavern Cellar") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight Baron von Ratsworth", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(512, "Hot and Cold Running Rats", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1, "fight drunken rat", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(513, "Staring Down the Barrel", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(514, "1984 Had Nothing on This Cellar", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new ChoiceAdventure(515, "A Rat's Home...", "The Typical Tavern Cellar") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        TavernRequest.postTavernVisit(request);
      }
    };

    new UnknownChoiceAdventure(516);

    new RetiredChoiceAdventure(517, "Mr. Alarm, I presarm", "Cobb's Knob Laboratory") {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestIfBetter(Quest.PALINDOME, "step3");
      }
    };

    new ChoiceAdventure(518, "Clear and Present Danger", "Elf Alley") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "enter combat with Uncle Hobo", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        HobopolisManager.checkHoboBoss(decision, "Uncle Hobo");
      }
    };

    new ChoiceAdventure(519, "What a Tosser", "Elf Alley") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, null, true)
            .attachItem("gift-a-pult", 1, AUTO)
            .attachItem(ItemPool.HOBO_NICKEL, -50, MANUAL, new NoDisplay());
        new Option(2, "skip adventure", true);
      }
    };

    new RetiredChoiceAdventure(520, "A Show-ho-ho-down", null) {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
      }
    };

    new ChoiceAdventure(521, "A Wicked Buzz", null) {
      void setup() {
        new Option(1).turnCost(1);
      }
    };

    new ChoiceAdventure(522, "Welcome to the Footlocker", "Cobb's Knob Barracks") {
      void setup() {
        new Option(1, "outfit piece or donut", true).turnCost(1);
        new Option(2, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        boolean havePolearm =
            InventoryManager.getCount(ItemPool.KNOB_GOBLIN_POLEARM) > 0
                || InventoryManager.getEquippedCount(ItemPool.KNOB_GOBLIN_POLEARM) > 0;
        boolean havePants =
            InventoryManager.getCount(ItemPool.KNOB_GOBLIN_PANTS) > 0
                || InventoryManager.getEquippedCount(ItemPool.KNOB_GOBLIN_PANTS) > 0;
        boolean haveHelm =
            InventoryManager.getCount(ItemPool.KNOB_GOBLIN_HELM) > 0
                || InventoryManager.getEquippedCount(ItemPool.KNOB_GOBLIN_HELM) > 0;

        Option option = getOption(1);

        if (!havePolearm) {
          option
              .text("knob goblin elite polearm")
              .attachItem(ItemPool.KNOB_GOBLIN_POLEARM, 1, AUTO);
        } else if (!havePants) {
          option.text("knob goblin elite pants").attachItem(ItemPool.KNOB_GOBLIN_PANTS, 1, AUTO);
        } else if (!haveHelm) {
          option.text("knob goblin elite helm").attachItem(ItemPool.KNOB_GOBLIN_HELM, 1, AUTO);
        } else {
          option.text("knob jelly donut").attachItem(ItemPool.KNOB_DONUT, 1, AUTO);
        }
      }
    };

    new ChoiceAdventure(523, "Death Rattlin'", "The Defiled Cranny") {
      void setup() {
        new Option(1, "small meat boost", true).turnCost(1).attachMeat(250, AUTO);
        new Option(2, "stats & HP & MP", true).turnCost(1);
        new Option(3, "can of Ghuol-B-Gone&trade;", true)
            .turnCost(1)
            .attachItem("can of Ghuol-B-Gone&trade;", 1, AUTO);
        new Option(4, "fight swarm of ghuol whelps", true);
        new Option(5, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(524, "The Adventures of Lars the Cyberian", "Item-Driven") {
      void setup() {
        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("Skullhead's Screw")) {
          // You lose the book if you receive the reward.
          // I don't know if that's always the result of
          // the same choice option
          getOption(decision).attachItem(ItemPool.LARS_THE_CYBERIAN, -1, MANUAL);
        }
      }
    };

    new ChoiceAdventure(525, "Fiddling with a Puzzle", "Item-Driven") {
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

    new UnknownChoiceAdventure(526);

    new ChoiceAdventure(527, "The Haert of Darkness", "Haert of the Cyrpt") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1, "fight the Bonerdagon", true);
        new Option(2, "skip adventure", true);
      }
    };

    new RetiredChoiceAdventure(
        528, "It Was Then That a Hideous Monster Carried You", "The Icy Peak") {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new RetiredChoiceAdventure(
        529, "A Swarm of Yeti-Mounted Skeletons", "A Swarm of Yeti-Mounted Skeletons") {
      void setup() {
        new Option(1, "Weapon Damage", true);
        new Option(2, "Spell Damage", true);
        new Option(3, "Ranged Damage", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase500to599.parseSkeletonKills(request);
      }
    };

    new RetiredChoiceAdventure(530, "It Was Then That... Aaaaaaaah!", "The Icy Peak") {
      void setup() {
        this.customZones.clear();
        this.customZones.add("Events");

        new Option(1, null, true).turnCost(1).attachItem("hideous egg", 1, AUTO);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(531, "The Bonewall is In", "The Bonewall") {
      void setup() {
        new Option(1, "Item Drop", true);
        new Option(2, "HP Bonus", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase500to599.parseSkeletonKills(request);
      }
    };

    new RetiredChoiceAdventure(532, "You'll Sink His Battleship", "A Massive Flying Battleship") {
      void setup() {
        new Option(1, "Class Skills", true);
        new Option(2, "Accordion Thief Songs", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase500to599.parseSkeletonKills(request);
      }
    };

    new RetiredChoiceAdventure(533, "Train, Train, Choo-Choo Train", "A Supply Train") {
      void setup() {
        new Option(1, "Meat Drop", true);
        new Option(2, "Pressure Penalty Modifiers", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase500to599.parseSkeletonKills(request);
      }
    };

    new RetiredChoiceAdventure(534, "That's No Bone Moon...", "The Bone Star") {
      void setup() {
        new Option(1, "Torpedoes", true).attachItem("photoprotoneutron torpedo", -1, AUTO);
        new Option(2, "Initiative", true);
        new Option(3, "Monster Level", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        CADatabase500to599.parseSkeletonKills(request);
      }
    };

    new ChoiceAdventure(535, "Deep Inside Ronald, Baby", "Item-Driven") {
      void setup() {
        this.hasGoalButton = true;

        this.customName = "Map to Safety (Ronald)";
        this.customZones.add("Spaaace");

        new Option(1);
        new Option(2);
        new Option(3);

        String[] RonaldGoals = SafetyShelterManager.RonaldGoals;

        for (int i = 0; i < RonaldGoals.length; ++i) {
          new CustomOption(i + 1, RonaldGoals[i]);
        }
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return true;
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        SafetyShelterManager.addRonaldGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Don't automate this if we logged in in the middle of the game -
        // the auto script isn't robust enough to handle arbitrary starting points.
        if (ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE) {
          return SafetyShelterManager.autoRonald(decision, stepCount, responseText);
        }
        return "0";
      }
    };

    new ChoiceAdventure(536, "Deep Inside Grimace, Bow Chick-a Bow Bow", "Item-Driven") {
      void setup() {
        this.hasGoalButton = true;

        this.customName = "Map to Safety (Grimace)";
        this.customZones.add("Spaaace");

        new Option(1);
        new Option(2);
        new Option(3);

        String[] GrimaceGoals = SafetyShelterManager.GrimaceGoals;

        for (int i = 0; i < GrimaceGoals.length; ++i) {
          new CustomOption(i + 1, GrimaceGoals[i]);
        }
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return true;
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        SafetyShelterManager.addGrimaceGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Don't automate this if we logged in in the middle of the game -
        // the auto script isn't robust enough to handle arbitrary starting points.
        if (ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE) {
          return SafetyShelterManager.autoGrimace(decision, stepCount, responseText);
        }
        return "0";
      }
    };

    new ChoiceAdventure(537, "Play Porko!", null) {
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
      void visitChoice(GenericRequest request) {
        SpaaaceRequest.visitPorkoChoice(request.responseText);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        SpaaaceRequest.decoratePorko(buffer);
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        SpaaaceRequest.decoratePorko(buffer);
      }
    };

    new ChoiceAdventure(538, "Big-Time Generator", "Hamburglaris Shield Generator") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(539, "An E.M.U. for Y.O.U.", "undefined") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        EquipmentManager.discardEquipment(ItemPool.SPOOKY_LITTLE_GIRL);
      }
    };

    new ChoiceAdventure(540, "Big-Time Generator", "Hamburglaris Shield Generator") { // game board
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
      void visitChoice(GenericRequest request) {
        SpaaaceRequest.visitGeneratorChoice(request.responseText);
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        SpaaaceRequest.decoratePorko(buffer);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Win:
        //
        // The generator starts to hum and the well above you
        // begins to spin, slowly at first, then faster and
        // faster. The humming becomes a deep, sternum-rattling
        // thrum, a sub-audio *WHOOMP WHOOMP WHOOMPWHOOMPWHOOMP.*
        // Brilliant blue light begins to fill the well, and
        // you feel like your bones are turning to either
        // powder, jelly, or jelly with powder in it.<p>Then
        // you fall through one of those glowy-circle
        // transporter things and end up back on Grimace, and
        // boy, are they glad to see you! You're not sure where
        // one gets ticker-tape after an alien invasion, but
        // they seem to have found some.
        //
        // Lose 3 times:
        //
        // Your E.M.U.'s getting pretty beaten up from all the
        // pinballing between obstacles, and you don't like
        // your chances of getting back to the surface if you
        // try again. You manage to blast out of the generator
        // well and land safely on the surface. After that,
        // though, the E.M.U. gives an all-over shudder, a sad
        // little servo whine, and falls apart.

        if (request.responseText.contains("WHOOMP")
            || request.responseText.contains("a sad little servo whine")) {
          EquipmentManager.discardEquipment(ItemPool.EMU_UNIT);
          QuestDatabase.setQuestIfBetter(Quest.GENERATOR, QuestDatabase.FINISHED);
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        SpaaaceRequest.decoratePorko(buffer);
      }
    };

    new UnknownChoiceAdventure(541);

    new ChoiceAdventure(542, "Now's Your Pants!  I Mean... Your Chance!", "The Sleazy Back Alley") {
      void setup() {
        new Option(1).turnCost(1);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Then you make your way back out of the Alley,
        // clutching your pants triumphantly and trying really
        // hard not to think about how oddly chilly it has
        // suddenly become.

        // When you steal your pants, they are unequipped, you
        // gain a "you acquire" message", and they appear in
        // inventory.
        //
        // Treat this is simply discarding the pants you are
        // wearing
        if (request.responseText.contains("oddly chilly")) {
          EquipmentManager.discardEquipment(EquipmentManager.getEquipment(EquipmentManager.PANTS));
          QuestDatabase.setQuestProgress(Quest.MOXIE, "step1");
        }
      }
    };

    new ChoiceAdventure(543, "Up In Their Grill", "The Outskirts of Cobb's Knob") {
      void setup() {
        new Option(1).turnCost(1);
      }
    };

    new ChoiceAdventure(544, "A Sandwich Appears!", "The Haunted Pantry") {
      void setup() {
        new Option(1).turnCost(1);
      }
    };

    new UnknownChoiceAdventure(545);

    new ChoiceAdventure(546, "Interview With You", "Item-Driven") {
      void setup() {
        this.hasGoalButton = true;
        this.option0IsManualControl = false;

        this.customName = this.name;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);

        String[] VampOutGoals = VampOutManager.VampOutGoals;

        for (int i = 0; i < VampOutGoals.length; ++i) {
          new CustomOption(i + 1, VampOutGoals[i]);
        }
      }

      @Override
      boolean registerRequest(String urlString, int decision) {
        return true;
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        VampOutManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Don't automate this if we logged in in the middle of the game -
        // the auto script isn't robust enough to handle arbitrary starting points.
        if (ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE) {
          return VampOutManager.autoVampOut(
              StringUtilities.parseInt(decision), stepCount, responseText);
        }
        return "0";
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        VampOutManager.postChoiceVampOut(request.responseText);
      }
    };

    new UnknownChoiceAdventure(547);

    new ChoiceAdventure(548, "Behind Closed Doors", "The Haunted Sorority House") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Sorority House Necbromancer";

        new Option(1, "enter combat with The Necbromancer", true);
        new Option(2, "skip adventure", true);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2 && KoLmafia.isAdventuring()) {
          KoLmafia.updateDisplay(MafiaState.ABORT, "The Necbromancer waits for you.");
        }
      }
    };

    new ChoiceAdventure(549, "Dark in the Attic", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority House Attic";

        new Option(1).attachItem(ItemPool.STAFF_GUIDE, 3, AUTO);
        new Option(2).attachItem("ghost trap", 1, AUTO);
        new Option(3, "raise area ML");
        new Option(4, "lower area ML");
        // we deal with the shell in postChoice1
        new Option(5, "mass kill werewolves with silver shotgun shell")
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, -1, AUTO, new DisplayAll(NEED, AT_LEAST, 1));

        new CustomOption(1, "staff guides");
        new CustomOption(2, "ghost trap");
        new CustomOption(3, "mass kill werewolves with silver shotgun shell");
        new CustomOption(4, "raise area ML, then staff guides");
        new CustomOption(5, "raise area ML, then ghost trap");
        new CustomOption(6, "raise area ML, then mass kill werewolves");
        new CustomOption(7, "raise area ML, then mass kill werewolves or ghost trap");
        new CustomOption(8, "lower area ML, then staff guides");
        new CustomOption(9, "lower area ML, then ghost trap");
        new CustomOption(10, "lower area ML, then mass kill werewolves");
        new CustomOption(11, "lower area ML, then mass kill werewolves or ghost trap");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Some choices appear depending on whether
        // the boombox is on or off

        // 1 - acquire staff guides
        // 2 - acquire ghost trap
        // 3 - turn on boombox (raise area ML)
        // 4 - turn off boombox (lower area ML)
        // 5 - mass kill werewolves

        boolean boomboxOn = responseText.contains("sets your heart pounding and pulse racing");

        switch (StringUtilities.parseInt(decision)) {
          case 0: // show in browser
          case 1: // acquire staff guides
          case 2: // acquire ghost trap
            return decision;
          case 3: // mass kill werewolves with silver shotgun shell
            return "5";
          case 4: // raise area ML, then acquire staff guides
            return !boomboxOn ? "3" : "1";
          case 5: // raise area ML, then acquire ghost trap
            return !boomboxOn ? "3" : "2";
          case 6: // raise area ML, then mass kill werewolves
            return !boomboxOn ? "3" : "5";
          case 7: // raise area ML, then mass kill werewolves or ghost trap
            return !boomboxOn
                ? "3"
                : InventoryManager.getCount(ItemPool.SILVER_SHOTGUN_SHELL) > 0 ? "5" : "2";
          case 8: // lower area ML, then acquire staff guides
            return boomboxOn ? "4" : "1";
          case 9: // lower area ML, then acquire ghost trap
            return boomboxOn ? "4" : "2";
          case 10: // lower area ML, then mass kill werewolves
            return boomboxOn ? "4" : "5";
          case 11: // lower area ML, then mass kill werewolves or ghost trap
            return boomboxOn
                ? "4"
                : InventoryManager.getCount(ItemPool.SILVER_SHOTGUN_SHELL) > 0 ? "5" : "2";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains(
            "The silver pellets tear through the sorority werewolves")) {
          ResultProcessor.processItem(ItemPool.SILVER_SHOTGUN_SHELL, -1);
          RequestLogger.printLine("You took care of a bunch of werewolves.");
        } else if (request.responseText.contains("quietly sneak away")) {
          RequestLogger.printLine("You need a silver shotgun shell to kill werewolves.");
        } else if (request.responseText.contains("a loose shutter")) {
          RequestLogger.printLine("All the werewolves have been defeated.");
        } else if (request.responseText.contains("crank up the volume on the boombox")) {
          RequestLogger.printLine("You crank up the volume on the boombox.");
        } else if (request.responseText.contains("a firm counterclockwise twist")) {
          RequestLogger.printLine("You crank down the volume on the boombox.");
        }
      }
    };

    new ChoiceAdventure(550, "The Unliving Room", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority House Unliving Room";

        // we deal with the items in postChoice1
        new Option(1, "raise area ML");
        new Option(2, "lower area ML");
        new Option(3, "mass kill zombies with chainsaw chain")
            .attachItem(ItemPool.CHAINSAW_CHAIN, -1, AUTO, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(4, "mass kill skeletons with funhouse mirror")
            .attachItem(ItemPool.FUNHOUSE_MIRROR, -1, AUTO, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(5, "get random costume item")
            .attachItem(ItemPool.NECROTIZING_BODY_SPRAY, 1, AUTO)
            .attachItem(ItemPool.BITE_LIPSTICK, 1, AUTO)
            .attachItem(ItemPool.PRESS_ON_RIBS, 1, AUTO)
            .attachItem(ItemPool.GHOSTLY_BODY_PAINT, 1, AUTO)
            .attachItem(ItemPool.WHISKER_PENCIL, 1, AUTO);

        new CustomOption(1, "mass kill zombies with chainsaw chain");
        new CustomOption(2, "mass kill skeletons with funhouse mirror");
        new CustomOption(3, "get costume item");
        new CustomOption(4, "raise area ML, then mass kill zombies");
        new CustomOption(5, "raise area ML, then mass kill skeletons");
        new CustomOption(6, "raise area ML, then mass kill zombies/skeletons");
        new CustomOption(7, "raise area ML, then get costume item");
        new CustomOption(8, "lower area ML, then mass kill zombies");
        new CustomOption(9, "lower area ML, then mass kill skeletons");
        new CustomOption(10, "lower area ML, then mass kill zombies/skeletons");
        new CustomOption(11, "lower area ML, then get costume item");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Some choices appear depending on whether
        // the windows are opened or closed

        // 1 - close the windows (raise area ML)
        // 2 - open the windows (lower area ML)
        // 3 - mass kill zombies
        // 4 - mass kill skeletons
        // 5 - get costume item

        boolean windowsClosed = responseText.contains("covered all their windows");
        int chainsaw = InventoryManager.getCount(ItemPool.CHAINSAW_CHAIN);
        int mirror = InventoryManager.getCount(ItemPool.FUNHOUSE_MIRROR);

        switch (StringUtilities.parseInt(decision)) {
          case 0: // show in browser
            return decision;
          case 1: // mass kill zombies with chainsaw chain
            return "3";
          case 2: // mass kill skeletons with funhouse mirror
            return "4";
          case 3: // get costume item
            return "5";
          case 4: // raise area ML, then mass kill zombies
            return !windowsClosed ? "1" : "3";
          case 5: // raise area ML, then mass kill skeletons
            return !windowsClosed ? "1" : "4";
          case 6: // raise area ML, then mass kill zombies/skeletons
            return !windowsClosed ? "1" : chainsaw > mirror ? "3" : "4";
          case 7: // raise area ML, then get costume item
            return !windowsClosed ? "1" : "5";
          case 8: // lower area ML, then mass kill zombies
            return windowsClosed ? "2" : "3";
          case 9: // lower area ML, then mass kill skeletons
            return windowsClosed ? "2" : "4";
          case 10: // lower area ML, then mass kill zombies/skeletons
            return windowsClosed ? "2" : chainsaw > mirror ? "3" : "4";
          case 11: // lower area ML, then get costume item
            return windowsClosed ? "2" : "5";
        }
        return decision;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("you pull out the chainsaw blades")) {
          ResultProcessor.processItem(ItemPool.CHAINSAW_CHAIN, -1);
          RequestLogger.printLine("You took out a bunch of zombies.");
        } else if (request.responseText.contains("a wet tearing noise")) {
          RequestLogger.printLine("You need a chainsaw chain to kill zombies.");
        } else if (request.responseText.contains("a bloody tangle")) {
          RequestLogger.printLine("All the zombies have been defeated.");
        } else if (request.responseText.contains(
            "the skeletons collapse into piles of loose bones")) {
          ResultProcessor.processItem(ItemPool.FUNHOUSE_MIRROR, -1);
          RequestLogger.printLine("You made short work of some skeletons.");
        } else if (request.responseText.contains("couch in front of the door")) {
          RequestLogger.printLine("You need a funhouse mirror to kill skeletons.");
        } else if (request.responseText.contains("just coats")) {
          RequestLogger.printLine("All the skeletons have been defeated.");
        } else if (request.responseText.contains("close the windows")) {
          RequestLogger.printLine("You close the windows.");
        } else if (request.responseText.contains("open the windows")) {
          RequestLogger.printLine("You open the windows.");
        }
      }
    };

    new ChoiceAdventure(551, "Debasement", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority House Debasement";

        new Option(1, "Prop Deportment", true).leadsTo(552);
        new Option(2, "mass kill vampires with plastic vampire fangs", true)
            .attachItem(ItemPool.PLASTIC_VAMPIRE_FANGS, new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1));
        new Option(3, "raise area ML");
        new Option(4, "lower area ML");

        new CustomOption(3, "raise area ML, then Prop Deportment");
        new CustomOption(4, "raise area ML, then mass kill vampires");
        new CustomOption(5, "lower area ML, then Prop Deportment");
        new CustomOption(6, "lower area ML, then mass kill vampires");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Some choices appear depending on whether
        // the fog machine is on or off

        // 1 - Prop Deportment (choice adventure 552)
        // 2 - mass kill vampires
        // 3 - turn up the fog machine (raise area ML)
        // 4 - turn down the fog machine (lower area ML)

        boolean fogOn = responseText.contains("white clouds of artificial fog");

        switch (StringUtilities.parseInt(decision)) {
          case 0: // show in browser
          case 1: // Prop Deportment
          case 2: // mass kill vampires with plastic vampire fangs
            return decision;
          case 3: // raise area ML, then Prop Deportment
            return fogOn ? "1" : "3";
          case 4: // raise area ML, then mass kill vampires
            return fogOn ? "2" : "3";
          case 5: // lower area ML, then Prop Deportment
            return fogOn ? "4" : "1";
          case 6: // lower area ML, then mass kill vampires
            return fogOn ? "4" : "2";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("the vampire girls shriek")) {
          RequestLogger.printLine("You slew some vampires.");
        } else if (request.responseText.contains("gets back in her coffin")) {
          RequestLogger.printLine("You need to equip plastic vampire fangs to kill vampires.");
        } else if (request.responseText.contains("they recognize you")) {
          RequestLogger.printLine("You have already killed some vampires.");
        } else if (request.responseText.contains("crank up the fog machine")) {
          RequestLogger.printLine("You crank up the fog machine.");
        } else if (request.responseText.contains("turn the fog machine way down")) {
          RequestLogger.printLine("You crank down the fog machine.");
        }
      }
    };

    new ChoiceAdventure(552, "Prop Deportment", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority House Prop Deportment";

        new Option(1).attachItem(ItemPool.CHAINSAW_CHAIN, 1, AUTO);
        new Option(2, "create a silver shotgun shell")
            .leadsTo(553)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL);
        new Option(3).attachItem(ItemPool.FUNHOUSE_MIRROR, 1, AUTO);

        new CustomOption(1, "chainsaw chain");
        new CustomOption(2, "silver item");
        new CustomOption(3, "funhouse mirror");
        new CustomOption(4, "chainsaw/mirror");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Allow the user to let Mafia pick
        // which prop to get

        // 1 - chainsaw
        // 2 - Relocked and Reloaded
        // 3 - funhouse mirror
        // 4 - chainsaw chain OR funhouse mirror

        int chainsaw = InventoryManager.getCount(ItemPool.CHAINSAW_CHAIN);
        int mirror = InventoryManager.getCount(ItemPool.FUNHOUSE_MIRROR);

        switch (StringUtilities.parseInt(decision)) {
          case 0: // show in browser
          case 1: // chainsaw chain
          case 2: // Relocked and Reloaded
          case 3: // funhouse mirror
            return decision;
          case 4: // chainsaw chain OR funhouse mirror
            return chainsaw < mirror ? "1" : "3";
        }
        return decision;
      }
    };

    new ChoiceAdventure(553, "Relocked and Reloaded", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority House Relocked and Reloaded";

        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.MAXWELL_HAMMER, -1, MANUAL)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, 1, AUTO);
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.TONGUE_BRACELET, -1, MANUAL)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, 1, AUTO);
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.SILVER_CHEESE_SLICER, -1, MANUAL)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, 1, AUTO);
        new Option(4)
            .turnCost(1)
            .attachItem(ItemPool.SILVER_SHRIMP_FORK, -1, MANUAL)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, 1, AUTO);
        new Option(5)
            .turnCost(1)
            .attachItem(ItemPool.SILVER_PATE_KNIFE, -1, MANUAL)
            .attachItem(ItemPool.SILVER_SHOTGUN_SHELL, 1, AUTO);
        new Option(6, "exit adventure").entersQueue(false);

        new CustomOption(1, "melt Maxwell's Silver Hammer");
        new CustomOption(2, "melt silver tongue charrrm bracelet");
        new CustomOption(3, "melt silver cheese-slicer");
        new CustomOption(4, "melt silver shrimp fork");
        new CustomOption(5, "melt silver pat&eacute; knife");
        new CustomOption(6, "don't melt anything");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Choices appear depending on whether
        // you have the item to melt

        // 1 - Maxwell's Silver Hammer
        // 2 - silver tongue charrrm bracelet
        // 3 - silver cheese-slicer
        // 4 - silver shrimp fork
        // 5 - silver pat&eacute; knife
        // 6 - don't melt anything

        int item = 0;

        switch (StringUtilities.parseInt(decision)) {
          case 0: // show in browser
          case 6: // don't melt anything
            return decision;
          case 1: // melt Maxwell's Silver Hammer
            item = ItemPool.MAXWELL_HAMMER;
            break;
          case 2: // melt silver tongue charrrm bracelet
            item = ItemPool.TONGUE_BRACELET;
            break;
          case 3: // melt silver cheese-slicer
            item = ItemPool.SILVER_CHEESE_SLICER;
            break;
          case 4: // melt silver shrimp fork
            item = ItemPool.SILVER_SHRIMP_FORK;
            break;
          case 5: // melt silver pat&eacute; knife
            item = ItemPool.SILVER_PATE_KNIFE;
            break;
        }

        if (item == 0) {
          return "6";
        }
        return InventoryManager.getCount(item) > 0 ? decision : "6";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
            if (!request.responseText.contains("You melt")) {
              this.choiceFailed();
            }
        }
      }
    };

    new ChoiceAdventure(554, "Behind the Spooky Curtain", "The Haunted Sorority House") {
      void setup() {
        this.customName = "Sorority Staff Guide";
        this.customZones.add("Item-Driven");

        new Option(1, "staff guides, ghost trap, kill werewolves").leadsTo(549);
        new Option(2, "kill zombies, kill skeletons, costume item").leadsTo(550);
        new Option(3, "chainsaw chain, silver item, funhouse mirror, kill vampires").leadsTo(551);

        new CustomOption(1, "attic");
        new CustomOption(2, "main floor");
        new CustomOption(3, "basement");
      }
    };

    new UnknownChoiceAdventure(555);

    new ChoiceAdventure(556, "More Locker Than Morlock", "Itznotyerzitz Mine") {
      void setup() {
        new Option(1, "get an outfit piece", true)
            .turnCost(1)
            .attachItem("miner's helmet", 1, AUTO, new DisplayAll(WANT, INV_ONLY, 0))
            .attachItem("7-Foot Dwarven mattock", 1, AUTO, new DisplayAll(WANT, INV_ONLY, 0))
            .attachItem("miner's pants", 1, AUTO, new DisplayAll(WANT, INV_ONLY, 0))
            .attachItem("safety vest", 1, AUTO);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new RetiredChoiceAdventure(557, "Gingerbread Homestead", "Lollipop Forest") {
      void setup() {
        this.customName = this.name;

        new Option(1, "get 3-5 candies", true).turnCost(1);
        new Option(2, "licorice root", true).turnCost(1).attachItem("licorice root", 1, AUTO);
        new Option(3, "skip adventure or make a lollipop stick item", true).leadsTo(558);
      }
    };

    new RetiredChoiceAdventure(558, "Tool Time", "Lollipop Forest") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true)
            .turnCost(1)
            .attachItem("sucker bucket", 1, AUTO)
            .attachItem(ItemPool.LOLLIPOP_STICK, -4, MANUAL);
        new Option(2, null, true)
            .turnCost(1)
            .attachItem("sucker kabuto", 1, AUTO)
            .attachItem(ItemPool.LOLLIPOP_STICK, -5, MANUAL);
        new Option(3, null, true)
            .turnCost(1)
            .attachItem("sucker hakama", 1, AUTO)
            .attachItem(ItemPool.LOLLIPOP_STICK, -6, MANUAL);
        new Option(4, null, true)
            .turnCost(1)
            .attachItem("sucker tachi", 1, AUTO)
            .attachItem(ItemPool.LOLLIPOP_STICK, -7, MANUAL);
        new Option(5, null, true)
            .turnCost(1)
            .attachItem("sucker scaffold", 1, AUTO)
            .attachItem(ItemPool.LOLLIPOP_STICK, -8, MANUAL);
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Choices appear depending on whether
        // you have enough lollipop sticks

        // 1 - sucker bucket (4 lollipop sticks)
        // 2 - sucker kabuto (5 lollipop sticks)
        // 3 - sucker hakama (6 lollipop sticks)
        // 4 - sucker tachi (7 lollipop sticks)
        // 5 - sucker scaffold (8 lollipop sticks)
        // 6 - skip adventure

        if (decision.equals("0") || decision.equals("6")) {
          return decision;
        }

        int amount = 3 + StringUtilities.parseInt(decision);
        return InventoryManager.getCount(ItemPool.LOLLIPOP_STICK) >= amount ? decision : "6";
      }
    };

    new RetiredChoiceAdventure(559, "Fudge Mountain Breakdown", "Fudge Mountain") {
      void setup() {
        this.customName = this.name;

        new Option(1, "fudge lily", true).turnCost(1).attachItem("fudge lily", 1, AUTO);
        new Option(2, "fight a swarm of fudgewasps or skip adventure", true);
        new Option(3, "frigid fudgepuck or skip adventure", true)
            .turnCost(1)
            .attachItem("frigid fudgepuck", 1, AUTO);
        new Option(4, "superheated fudge or skip adventure", true)
            .turnCost(1)
            .attachItem("superheated fudge", 1, AUTO);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3 || decision == 4 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          if (request.responseText.contains("but nothing comes out")) {
            Preferences.setInteger("_fudgeWaspFights", 3);
          } else if (request.responseText.contains("trouble has taken a holiday")) {
            // The Advent Calendar hasn't been punched out enough to find fudgewasps yet
          } else {
            Preferences.increment("_fudgeWaspFights", 1);
          }
        }
      }
    };

    new ChoiceAdventure(560, "Foreshadowing Demon!", "The Clumsiness Grove") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(561, "You Must Choose Your Destruction!", "The Clumsiness Grove") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "The Thorax", true);
        new Option(2, "The Bat in the Spats", true);
      }
    };

    new ChoiceAdventure(562, "You're the Fudge Wizard Now, Dog", "Item-Driven") {
      void setup() {
        // fudgecule prices are handled in FudgeWandRequest
        new Option(1).attachItem(ItemPool.FUDGIE_ROLL, 1, AUTO);
        new Option(2).attachItem(ItemPool.FUDGE_SPORK, 1, AUTO);
        new Option(3).attachItem(ItemPool.FUDGE_CUBE, 1, AUTO);
        new Option(4).attachItem(ItemPool.FUDGE_BUNNY, 1, AUTO);
        new Option(5).attachItem(ItemPool.FUDGECYCLE, 1, AUTO);
        new Option(6);
      }
    };

    new ChoiceAdventure(563, "A Test of Your Mettle", "The Clumsiness Grove") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards other boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(564, "A Maelstrom of Trouble", "The Maelstrom of Lovers") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(565, "To Get Groped or Get Mugged?", "The Maelstrom of Lovers") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "The Terrible Pinch", true);
        new Option(2, "Thug 1 and Thug 2", true);
      }
    };

    new ChoiceAdventure(566, "A Choice to be Made", "The Maelstrom of Lovers") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards other boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(567, "You May Be on Thin Ice", "The Glacier of Jerks") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(568, "Some Sounds Most Unnerving", "The Glacier of Jerks") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "Mammon the Elephant", true);
        new Option(2, "The Large-Bellied Snitch", true);
      }
    };

    new ChoiceAdventure(569, "One More Demon to Slay", "The Glacier of Jerks") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1, "head towards other boss", true);
        new Option(2, "skip adventure", true);
      }
    };

    new ChoiceAdventure(570, "GameInformPowerDailyPro Walkthru", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        GameproManager.parseGameproMagazine(request.responseText);
      }
    };

    new ChoiceAdventure(571, "Your Minstrel Vamps", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.CLANCY, QuestDatabase.STARTED);
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        RequestEditorKit.addMinstrelNavigationLink(
            buffer, "Go to the Typical Tavern", "tavern.php");
      }
    };

    new ChoiceAdventure(572, "Your Minstrel Clamps", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.CLANCY, "step2");
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        RequestEditorKit.addMinstrelNavigationLink(
            buffer, "Go to the Knob Shaft", "adventure.php?snarfblat=101");
      }
    };

    new ChoiceAdventure(573, "Your Minstrel Stamps", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.CLANCY, "step4");
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        RequestEditorKit.addMinstrelNavigationLink(
            buffer, "Go to the Luter's Grave", "place.php?whichplace=plains&action=lutersgrave");
      }
    };

    new ChoiceAdventure(574, "The Minstrel Cycle Begins", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(575, "Duffel on the Double", "The eXtreme Slope") {
      void setup() {
        new Option(1, "get an outfit piece", true)
            .turnCost(1)
            .attachItem("eXtreme mittens", 1, AUTO)
            .attachItem("eXtreme scarf", 1, AUTO)
            .attachItem("snowboarder pants", 1, AUTO);
        new Option(2, "(!!!RARE!!!) jar of frostigkraut (!!!RARE!!!)")
            .turnCost(1)
            .attachItem("jar of frostigkraut", 1, AUTO);
        new Option(3, "skip adventure", true).entersQueue(false);
        new Option(4, "lucky pill", true).turnCost(1).attachItem("lucky pill", 1, AUTO);

        new CustomOption(5, "frostigkraut if available, otherwise outfit piece");
        new CustomOption(6, "frostigkraut if available, otherwise skip", 2);
        new CustomOption(7, "frostigkraut if available, otherwise lucky pill", 6);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 2 - "Dig deeper" - is not always available.
        if (responseText.contains("Dig deeper")) {
          switch (decision) {
            case "5":
            case "6":
              return "2";
            default:
              return decision;
          }
        } else {
          switch (decision) {
            case "2":
              return "3";
            case "5":
              return "1";
            case "6":
              return "4";
            default:
              return decision;
          }
        }
      }
    };

    new ChoiceAdventure(576, "Your Minstrel Camps", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.CLANCY, "step6");
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        RequestEditorKit.addMinstrelNavigationLink(
            buffer, "Go to the Icy Peak", "adventure.php?snarfblat=110");
      }
    };

    new ChoiceAdventure(577, "Your Minstrel Scamp", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        QuestDatabase.setQuestProgress(Quest.CLANCY, "step8");
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        RequestEditorKit.addMinstrelNavigationLink(
            buffer, "Go to the Ancient Buried Pyramid", "pyramid.php");
      }
    };

    new ChoiceAdventure(578, "End of the Boris Road", null) {
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

    new ChoiceAdventure(579, "Such Great Heights", "The Hidden Temple") {
      void setup() {
        this.customName = "Hidden Temple Heights";

        new Option(1, "mysticality substats", true).turnCost(1);
        new Option(2, "Nostril of the Serpent then skip adventure", true);
        new Option(3, "gain 3 adv then skip adventure", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(2);
        if (InventoryManager.getCount(ItemPool.NOSTRIL_OF_THE_SERPENT) == 0
            && Preferences.getInteger("lastTempleButtonsUnlock") != KoLCharacter.getAscensions()) {
          option
              .text("gain the Nostril of the Serpent")
              .turnCost(1)
              .attachItem(ItemPool.NOSTRIL_OF_THE_SERPENT, 1, AUTO);
        } else {
          option.text("skip adventure").entersQueue(false);
        }

        option = getOption(3);
        if (Preferences.getInteger("lastTempleAdventures") != KoLCharacter.getAscensions()) {
          option.text("gain 3 adventures and extend 10 of your buffs by 3 turns").attachAdv(3);
        } else {
          option.text("skip adventure").entersQueue(false);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2 && !request.responseText.contains("You acquire")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 3
            && Preferences.getInteger("lastTempleAdventures") != KoLCharacter.getAscensions()) {
          Preferences.setInteger("lastTempleAdventures", KoLCharacter.getAscensions());
        }
      }

      @Override
      void decorateChoiceResponse(StringBuffer buffer, int option) {
        if (option == 3) {
          // xyzzy
          int index =
              buffer.indexOf(
                  "<p><a href=\"adventure.php?snarfblat=280\">Adventure Again (The Hidden Temple)</a>");
          if (index == -1) {
            return;
          }

          int itemId = ItemPool.STONE_WOOL;
          int count = ItemPool.get(itemId, 1).getCount(KoLConstants.inventory);
          if (count == 0) {
            return;
          }

          String link =
              "<a href=\"javascript:singleUse('inv_use.php','which=3&whichitem="
                  + itemId
                  + "&pwd="
                  + GenericRequest.passwordHash
                  + "&ajax=1');void(0);\">Use another stone wool</a>";
          buffer.insert(index, link);
        }
      }
    };

    new ChoiceAdventure(580, "The Hidden Heart of the Hidden Temple", "The Hidden Temple") {
      final AdventureResult NOSTRIL_OF_THE_SERPENT =
          ItemPool.get(ItemPool.NOSTRIL_OF_THE_SERPENT, -1);
      final AdventureResult ANCIENT_CALENDAR_FRAGMENT =
          new AdventureResult("ancient calendar fragment", 1, false);

      void setup() {
        new Option(1, "???");
        new Option(2);
        new Option(3, "moxie substats and 5 turns of Somewhat poisoned")
            .turnCost(1)
            .attachEffect(EffectPool.SOMEWHAT_POISONED);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option;

        if (request != null) {
          option = getOption(1);
          if (request.responseText.contains("door_stone.gif")) {
            option.text("muscle substats").turnCost(1);
          } else if (request.responseText.contains("door_sun.gif")) {
            option
                .text("gain ancient calendar fragment")
                .turnCost(1)
                .attachItem(ANCIENT_CALENDAR_FRAGMENT, AUTO);
          } else if (request.responseText.contains("door_gargoyle.gif")) {
            option.text("gain mana").turnCost(1);
          } else if (request.responseText.contains("door_pikachu.gif")) {
            QuestDatabase.setQuestIfBetter(Quest.WORSHIP, "step2");

            option.text("unlock Hidden City (3)").leadsTo(123);
          }
        }

        option = getOption(2);
        if (NOSTRIL_OF_THE_SERPENT.getCount(KoLConstants.inventory) > 0) {
          option
              .text("choose Hidden Heart adventure (doesn't end this adventure)")
              .leadsTo(584)
              .attachItem(NOSTRIL_OF_THE_SERPENT, MANUAL);
        } else if (Preferences.getInteger("lastTempleButtonsUnlock")
            == KoLCharacter.getAscensions()) {
          option.text("choose Hidden Heart adventure (doesn't end this adventure)").leadsTo(584);
        } else {
          option
              .text(
                  "randomise Hidden Heart adventure (will be able to choose it for free if you get the Nostril of the Serpent)")
              .leadsTo(583)
              .attachItem(NOSTRIL_OF_THE_SERPENT, new ImageOnly());
        }
      }
    };

    new ChoiceAdventure(581, "Such Great Depths", "The Hidden Temple") {
      final AdventureResult HIDDEN_POWER = new AdventureResult("Hidden Power", 0, true);

      void setup() {
        this.customName = "Hidden Temple Depths";

        new Option(1, "gain a glowing fungus", true)
            .turnCost(1)
            .attachItem("glowing fungus", 1, AUTO);
        new Option(2, "+15 mus/mys/mox then skip adventure", true);
        new Option(3, "fight clan of cave bars", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(2);
        if (Preferences.getBoolean("_templeHiddenPower")) {
          option.text("skip adventure");
        } else {
          option.text("5 advs of Hidden Power").turnCost(1).attachEffect(HIDDEN_POWER);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          Preferences.setBoolean("_templeHiddenPower", true);
        }
      }
    };

    new ChoiceAdventure(582, "Fitting In", "The Hidden Temple") {
      void setup() {
        this.isSuperlikely = true;

        new Option(1).leadsTo(579, true, o -> !o.spoilerText.equals("skip adventure"));
        new Option(2, "Hidden Heart of the Hidden Temple", true).leadsTo(580, true);
        new Option(3).leadsTo(581, true, o -> !o.spoilerText.equals("skip adventure"));

        new CustomOption(1, "Such Great Heights");
        new CustomOption(3, "Such Great Depths");
      }
    };

    new ChoiceAdventure(583, "Confusing Buttons", "The Hidden Temple") {
      void setup() {
        new Option(1, "Press a random button").turnCost(1);
      }
    };

    new ChoiceAdventure(584, "Unconfusing Buttons", "The Hidden Temple") {
      void setup() {
        this.neverEntersQueue = true;

        // technically all lead to 580, but we would need
        // to have a system to have it update its options during
        // the visitChoice lookahead...
        new Option(1, "Hidden Temple (Stone) - muscle substats", true);
        new Option(2, "Hidden Temple (Sun) - gain ancient calendar fragment", true)
            .attachItem("ancient calendar fragment");
        new Option(3, "Hidden Temple (Gargoyle) - MP", true);
        new Option(4, "Hidden Temple (Pikachutlotal) - Hidden City unlock", true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe (but not necessary)

        if (request != null) {
          Preferences.setInteger("lastTempleButtonsUnlock", KoLCharacter.getAscensions());
        }
      }
    };

    new ChoiceAdventure(585, "Screwing Around!", null) {
      void setup() {
        new Option(1);
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }
    };

    new ChoiceAdventure(586, "All We Are Is Radio Huggler", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
        new Option(5);
        new Option(6);
      }
    };

    new RetiredChoiceAdventure(587, "Room, Interrupted", "Item-Driven") {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(588, "Machines!", "Sonar") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("The batbugbears around you start acting weird")) {
          BugbearManager.clearShipZone("Sonar");
        }
      }
    };

    new ChoiceAdventure(589, "Autopsy Auturvy", "Morgue") {
      void setup() {
        new Option(1)
            .turnCost(1)
            .attachItem(ItemPool.AUTOPSY_TWEEZERS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(2)
            .turnCost(1)
            .attachItem(ItemPool.AUTOPSY_TWEEZERS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(3)
            .turnCost(1)
            .attachItem(ItemPool.AUTOPSY_TWEEZERS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(4)
            .turnCost(1)
            .attachItem(ItemPool.AUTOPSY_TWEEZERS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(5)
            .turnCost(1)
            .attachItem(ItemPool.AUTOPSY_TWEEZERS, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure").entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // The tweezers you used dissolve in the caustic fluid. Rats.
        if (decision < 6 && !request.responseText.contains("dissolve in the caustic fluid")) {
          this.choiceFailed();
        }
      }
    };

    new ChoiceAdventure(590, "Not Alone In The Dark", "Special Ops") {
      void setup() {
        new Option(1, "chance of fighting Black Ops Bugbear (higher chance with light sources)");
        new Option(2, "use a flaregun to increase the chances of getting a fight")
            .turnCost(1)
            .attachItem("flaregun", -1, MANUAL, new DisplayAll("flaregun"));
        new Option(3, "skip adventure").entersQueue(false);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("don't find anything to fight")) {
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(
        591, "The Beginning of the Beginning of the End", null) { // "Bridge" is not actually a zone
      void setup() {
        new Option(1).leadsTo(592);
      }
    };

    new ChoiceAdventure(592, "The Middle of the Beginning of the End", null) {
      void setup() {
        new Option(1).leadsTo(593);
      }
    };

    new ChoiceAdventure(593, "The End of the Beginning of the End", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(594, "A Lost Room", "Item-Driven") {
      void setup() {
        this.hasGoalButton = true;

        this.customName = "Lost Key";

        new Option(1, null, true).attachItem(ItemPool.LOST_GLASSES, 1, AUTO, new NoDisplay());
        new Option(2, null, true).attachItem("lost comb", 1, AUTO, new NoDisplay());
        new Option(3, null, true).attachItem("lost pill bottle", 1, AUTO, new NoDisplay());
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        LostKeyManager.addGoalButton(buffer);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Don't automate this if we logged in in the middle of the game -
        // the auto script isn't robust enough to handle arbitrary starting points.
        if (ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE) {
          return LostKeyManager.autoKey(decision, stepCount, responseText);
        }
        return "0";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("You acquire")) {
          getOption(decision).attachItem(ItemPool.LOST_KEY, -1, MANUAL);
        }
      }
    };

    new ChoiceAdventure(595, "Fire! I... have made... fire!", "Item-Driven") {
      void setup() {
        this.customName = "CSA fire-starting kit";

        new Option(1, "pvp fights", true)
            .attachItem(ItemPool.CSA_FIRE_STARTING_KIT, -1, MANUAL, new NoDisplay());
        new Option(2, "hp/mp regen", true)
            .attachItem(ItemPool.CSA_FIRE_STARTING_KIT, -1, MANUAL, new NoDisplay());
      }

      @Override
      String encounterName(String urlString, String responseText) {
        return null;
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (!request.responseText.contains("rubbing the two stupid sticks together")
            && !request.responseText.contains("pile the sticks up on top of the briefcase")) {
          this.choiceFailed();
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setBoolean("_fireStartingKitUsed", true);
      }
    };

    new ChoiceAdventure(596, "Dawn of the D'oh", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(597, null, null) { // Reagnimated Gnome
      void setup() {
        this.customName = "Reagnimated Gnome (cake arena)";
        this.customZones.add("Item-Driven");

        new Option(1, "gnomish swimmer's ears (underwater)", true)
            .attachItem("gnomish swimmer's ears", 1, AUTO);
        new Option(2, "gnomish coal miner's lung (block)", true)
            .attachItem("gnomish coal miner's lung", 1, AUTO);
        new Option(3, "gnomish tennis elbow (damage)", true)
            .attachItem("gnomish tennis elbow", 1, AUTO);
        new Option(4, "gnomish housemaid's kgnee (gain advs)", true)
            .attachItem("gnomish housemaid's kgnee", 1, AUTO);
        new Option(5, "gnomish athlete's foot (delevel)", true)
            .attachItem("gnomish athlete's foot", 1, AUTO);
      }
    };

    new ChoiceAdventure(598, "Recruitment Jive", null) {
      void setup() {
        new Option(1);
        new Option(2);
      }
    };

    new ChoiceAdventure(599, "A Zombie Master's Bait", null) {
      final AdventureResult CRAPPY_BRAIN = ItemPool.get(ItemPool.CRAPPY_BRAIN);
      final AdventureResult DECENT_BRAIN = ItemPool.get(ItemPool.DECENT_BRAIN);
      final AdventureResult GOOD_BRAIN = ItemPool.get(ItemPool.GOOD_BRAIN);
      final AdventureResult BOSS_BRAIN = ItemPool.get(ItemPool.BOSS_BRAIN);

      void setup() {
        new Option(1, "1 zombie each").attachItem(CRAPPY_BRAIN);
        new Option(2, "2 zombies each").attachItem(DECENT_BRAIN);
        new Option(3, "3 zombies each").attachItem(GOOD_BRAIN);
        new Option(4, "6 zombies each").attachItem(BOSS_BRAIN);
        new Option(5);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.getFormField("quantity") == null) {
          return;
        }

        AdventureResult brain;
        switch (decision) {
          case 1:
            brain = CRAPPY_BRAIN;
            break;
          case 2:
            brain = DECENT_BRAIN;
            break;
          case 3:
            brain = GOOD_BRAIN;
            break;
          case 4:
            brain = BOSS_BRAIN;
            break;
          default:
            return;
        }

        int quantityField = Math.max(1, StringUtilities.parseInt(request.getFormField("quantity")));
        int inventoryCount = brain.getCount(KoLConstants.inventory);
        brain = brain.getInstance(-1 * Math.min(quantityField, inventoryCount));

        getOption(decision).attachItem(brain, MANUAL);
      }
    };
  }

  private static final Pattern SKELETON_PATTERN =
      Pattern.compile("You defeated <b>(\\d+)</b> skeletons");

  static final void parseSkeletonKills(GenericRequest request) {
    Matcher skeletonMatcher = SKELETON_PATTERN.matcher(request.responseText);
    if (skeletonMatcher.find()) {
      String message = "You defeated " + skeletonMatcher.group(1) + " skeletons";
      RequestLogger.printLine(message);
      RequestLogger.updateSessionLog(message);
    }
  }
}
