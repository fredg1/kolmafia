package net.sourceforge.kolmafia.persistence.choiceadventures;

import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.request.EquipmentRequest;
import net.sourceforge.kolmafia.request.FloristRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.DreadScrollManager;
import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase700to799 extends ChoiceAdventureDatabase {
  final void add700to799() {
    new ChoiceAdventure(700, "Delirium in the Cafeterium", null) {
      final AdventureResult JOCK_EFFECT = EffectPool.get(EffectPool.JAMMING_WITH_THE_JOCKS);
      final AdventureResult NERD_EFFECT = EffectPool.get(EffectPool.NERD_IS_THE_WORD);
      final AdventureResult GREASER_EFFECT = EffectPool.get(EffectPool.GREASER_LIGHTNIN);

      boolean hasJockEffect = false;
      boolean hasNerdEffect = false;
      boolean hasGreaserEffect = false;

      void setup() {
        this.neverEntersQueue = true;

        this.customName = this.name;
        this.customZones.add("KOL High School");

        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);

        new CustomOption(1, "get stats if possible else lose hp");
      }

      @Override
      void visitChoice(GenericRequest request) {
        hasJockEffect = KoLConstants.activeEffects.contains(JOCK_EFFECT);
        hasNerdEffect = KoLConstants.activeEffects.contains(NERD_EFFECT);
        hasGreaserEffect = KoLConstants.activeEffects.contains(GREASER_EFFECT);

        getOption(1).text(hasJockEffect ? "Gain stats" : "Lose HP");
        getOption(2).text(hasNerdEffect ? "Gain stats" : "Lose HP");
        getOption(3).text(hasGreaserEffect ? "Gain stats" : "Lose HP");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (!decision.equals("0")) {
          return hasJockEffect ? "1" : hasNerdEffect ? "2" : "3";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.increment("_kolhsAdventures", 1);
      }
    };

    new ChoiceAdventure(701, "Ators Gonna Ate", "Mer-kin Gymnasium") {
      void setup() {
        new Option(1, "get an item", true)
            .turnCost(1)
            .attachItem("Mer-kin dodgeball", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("Mer-kin dragnet", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("Mer-kin headguard", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("Mer-kin switchblade", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("Mer-kin thighguard", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("Mer-kin fastjuice", 1, AUTO);
        new Option(2, "skip adventure", true).entersQueue(false);
      }
    };

    new ChoiceAdventure(702, "No Corn, Only Thorns", "Swamp Beaver Territory") {
      void setup() {
        this.option0IsManualControl = false;

        new Option(1);
        new Option(2);
        new Option(3);
        new Option(4);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        if (responseText.contains("facing north")
            || responseText.contains("face north")
            || responseText.contains("indicate north")) {
          return "1";
        }
        if (responseText.contains("facing east")
            || responseText.contains("face east")
            || responseText.contains("indicate east")) {
          return "2";
        }
        if (responseText.contains("facing south")
            || responseText.contains("face south")
            || responseText.contains("indicate south")) {
          return "3";
        }
        if (responseText.contains("facing west")
            || responseText.contains("face west")
            || responseText.contains("indicate west")) {
          return "4";
        }
        if (responseText.contains("And then...")) {
          return "1";
        }
        return "0";
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("Stupid jerk beavers!")) {
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(703, "Mer-kin dreadscroll", "Item-Driven") {
      final AdventureResult FISHY = EffectPool.get(EffectPool.FISHY);

      void setup() {
        new Option(1);
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);
        if (KoLAdventure.lastVisitedLocation().getEnvironment().equals("underwater")
            && !KoLConstants.activeEffects.contains(FISHY)) {
          option.turnCost(2);
        } else {
          option.turnCost(1);
        }
      }

      @Override
      void decorateChoice(StringBuffer buffer) {
        // Load the options of the dreadscroll with the correct responses
        DreadScrollManager.decorate(buffer);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (request.responseText.contains("I guess you're the Mer-kin High Priest now")) {
          Preferences.setString("merkinQuestPath", "scholar");
          getOption(decision).attachItem(ItemPool.DREADSCROLL, -1, MANUAL);
        }
      }
    };

    new ChoiceAdventure(704, "Playing the Catalog Card", "Mer-kin Library") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(4).turnCost(1);
        new Option(5).turnCost(1);
        new Option(6).turnCost(1);
        new Option(7).turnCost(1);
        new Option(8).turnCost(1);
        new Option(9).turnCost(1);
        new Option(10).turnCost(1);
        new Option(11).turnCost(1);
        new Option(12).turnCost(1);
        new Option(13).turnCost(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        DreadScrollManager.handleLibrary(request.responseText);
      }
    };

    new ChoiceAdventure(705, "Halls Passing in the Night", "Mer-kin Elementary School") {
      final AdventureResult MERKIN_HALLPASS = ItemPool.get(ItemPool.MERKIN_HALLPASS, -1);
      final AdventureResult MERKIN_BUNWIG = new AdventureResult("Mer-kin bunwig", 0, false);
      final AdventureResult MERKIN_FACECOWL = new AdventureResult("Mer-kin facecowl", 1, false);
      final AdventureResult MERKIN_WAISTROPE = new AdventureResult("Mer-kin waistrope", 1, false);
      final AdventureResult MERKIN_WORDQUIZ = ItemPool.get(ItemPool.MERKIN_WORDQUIZ, 1);

      void setup() {
        new Option(1, "fight Mer-kin spectre", true)
            .attachItem(MERKIN_HALLPASS, MANUAL, new NoDisplay());
        new Option(2, "Mer-kin sawdust", true)
            .turnCost(1)
            .attachItem("Mer-kin sawdust", 1, AUTO)
            .attachItem(MERKIN_HALLPASS, MANUAL, new NoDisplay());
        new Option(3, "Mer-kin cancerstick", true)
            .turnCost(1)
            .attachItem("Mer-kin cancerstick", 1, AUTO)
            .attachItem(MERKIN_HALLPASS, MANUAL, new NoDisplay());
        new Option(4, "Mer-kin wordquiz (+2 w/ Mer-kin bunwig)", true)
            .turnCost(1)
            .attachItem(MERKIN_BUNWIG, new DisplayAll("bunwig", WANT, INV_ONLY_AT_LEAST, 1))
            .attachItem(MERKIN_HALLPASS, MANUAL, new NoDisplay());
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(4);
        // really only works if in inventory? not equipped?
        if (MERKIN_BUNWIG.getCount(KoLConstants.inventory) > 0) {
          option.attachItem(MERKIN_WORDQUIZ.getInstance(3), AUTO, new DisplayAll("wordquiz"));
        } else {
          option.attachItem(MERKIN_WORDQUIZ, AUTO, new DisplayAll("wordquiz"));
        }

        if (MERKIN_FACECOWL.getCount(KoLConstants.inventory) == 0) {
          option.attachItem(MERKIN_FACECOWL, AUTO, new ImageOnly());
        } else if (MERKIN_WAISTROPE.getCount(KoLConstants.inventory) == 0) {
          option.attachItem(MERKIN_WAISTROPE, AUTO, new ImageOnly());
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Option 2-4 aren't always available, but decision to take isn't clear if it's selected, so
        // show in browser
        if (decision.equals("2") && !responseText.contains("Go to the janitor's closet")) {
          return "0";
        }
        if (decision.equals("3") && !responseText.contains("Head to the bathroom")) {
          return "0";
        }
        if (decision.equals("4") && !responseText.contains("Check out the teacher's lounge")) {
          return "0";
        }
        return decision;
      }
    };

    new ChoiceAdventure(706, "In The Temple of Violence, Shine Like Thunder", null) {
      void setup() {
        new Option(1).leadsTo(707);
      }
    };

    new ChoiceAdventure(707, "Flex Your Pecs in the Narthex", null) {
      void setup() {
        new Option(1).leadsTo(708).attachEffect("Jiggu... What?");
      }
    };

    new ChoiceAdventure(708, "Don't Falter at the Altar", null) {
      void setup() {
        new Option(1, "fight Shub-Jigguwatt, Elder God of Violence");
      }
    };

    new ChoiceAdventure(709, "You Beat Shub to a Stub, Bub", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("merkinQuestPath", "done");
      }
    };

    new ChoiceAdventure(710, "They've Got Fun and Games", null) {
      void setup() {
        new Option(1).leadsTo(711);
      }
    };

    new ChoiceAdventure(711, "They've Got Everything You Want", null) {
      void setup() {
        new Option(1).leadsTo(712).attachEffect("More Like a Suckrament");
      }
    };

    new ChoiceAdventure(712, "Honey, They Know the Names", null) {
      void setup() {
        new Option(1, "fight Yog-Urt, Elder Goddess of Hatred");
      }
    };

    new ChoiceAdventure(713, "You Brought Her To Her Kn-kn-kn-kn-knees, Knees.", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("merkinQuestPath", "done");
      }
    };

    new ChoiceAdventure(714, "An Unguarded Door", null) { // pre-Life in the Stillness
      void setup() {
        new Option(1).leadsTo(715);
      }
    };

    new ChoiceAdventure(715, "Life in the Stillness", null) {
      void setup() {
        new Option(1).leadsTo(716).attachEffect("A Hole in the World");
      }
    };

    new ChoiceAdventure(716, "An Unguarded Door", null) { // post-Life in the Stillness
      void setup() {
        new Option(1, "fight Dad Sea Monkee (<i><b>fight limited to 12 rounds</b></i>)");
      }
    };

    new ChoiceAdventure(717, "Over. Over Now.", null) {
      void setup() {
        new Option(1);
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        Preferences.setString("merkinQuestPath", "done");
      }
    };

    new UnknownChoiceAdventure(718);

    new UnknownChoiceAdventure(719);

    new ChoiceAdventure(720, "The Florist Friar's Cottage", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1); // planting
        new Option(2); // digging up
        new Option(4); // visiting
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        FloristRequest.parseResponse(request.getURLString(), request.responseText);
      }
    };

    new ChoiceAdventure(721, "The Cabin in the Dreadsylvanian Woods", "Dreadsylvanian Woods") {
      void setup() {
        this.customName = "Cabin";
        this.customOrder = 1;

        // The Kitchen
        new Option(1).leadsTo(722, true, o -> o.index != 6);
        // The Cellar
        new Option(2).leadsTo(723, false, o -> o.index != 6);
        // The Attic (locked)
        new Option(3, "possibly locked")
            .leadsTo(724, true, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil1");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil1");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          // Try the Attic

          // You use your skeleton key to unlock the padlock on the attic trap door.
          // Then you use your legs to climb the ladder into the attic.
          // Then you use your stupidity to lose the skeleton key.  Crap.

          if (request.responseText.contains("lose the skeleton key")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(722, "The Kitchen in the Woods", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "dread tarragon").turnCost(1).attachItem(ItemPool.DREAD_TARRAGON, 1, AUTO);
        new Option(2, "old dry bone -> bone flour")
            .turnCost(1)
            .attachItem(
                ItemPool.OLD_DRY_BONE,
                -1,
                MANUAL,
                new DisplayAll("dry bone", NEED, INV_ONLY_AT_LEAST, 1))
            .attachItem(ItemPool.BONE_FLOUR, 1, AUTO);
        new Option(3, "-stench").turnCost(1);
        new Option(6, "Return to The Cabin").leadsTo(721, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMuscleClass()) {
          getOption(2).reset(request != null ? "you're not a muscle class :c " : null);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          // Screw around with the flour mill
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(723, "What Lies Beneath (the Cabin)", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "5-7 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(2, "100 adv of Bored Stiff").turnCost(1).attachEffect("Bored Stiff");
        new Option(3, "replica key -> Dreadsylvanian auditor's badge")
            .turnCost(1)
            .attachItem(
                ItemPool.REPLICA_KEY, -1, MANUAL, new DisplayAll("replica key", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.AUDITORS_BADGE, 1, AUTO);
        new Option(4, "wax banana -> complicated lock impression")
            .turnCost(1)
            .attachItem(
                ItemPool.WAX_BANANA, -1, MANUAL, new DisplayAll("banana", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.WAX_LOCK_IMPRESSION, 1, AUTO);
        new Option(6, "Return to The Cabin").leadsTo(721, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 || decision == 3 || decision == 4) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(724, "Where it's Attic", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1).turnCost(1);
        new Option(2, "fewer werewolves").turnCost(1);
        new Option(3, "fewer vampires").turnCost(1);
        new Option(4, "moxie substats").turnCost(1);
        new Option(6, "Return to The Cabin").leadsTo(721, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(1);
        if (KoLCharacter.isAccordionThief()) {
          option
              .text("-spooky + intricate music box parts")
              .attachItem(ItemPool.INTRICATE_MUSIC_BOX_PARTS, 1, AUTO);
        } else {
          option.text(
              "-spooky" + (request == null ? "" : " (don't -- have an Accordion Thief do this)"));
        }
      }
    };

    new ChoiceAdventure(725, "The Tallest Tree in the Forest", "Dreadsylvanian Woods") {
      void setup() {
        this.customName = "Tallest Tree";
        this.customOrder = 2;

        // Climb tree (muscle only)
        new Option(1).leadsTo(726, false, o -> o.index != 6);
        // Fire Tower (locked)
        new Option(2, "possibly locked")
            .leadsTo(727, false, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        // Base of tree
        new Option(3).leadsTo(728, true, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMuscleClass()) {
          getOption(1).reset("you're not a muscle class :c ");
        }

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil2");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil2");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          // Check out the fire tower

          // You climb the rope ladder and use your skeleton key to
          // unlock the padlock on the door leading into the little room
          // at the top of the watchtower. Then you accidentally drop
          // your skeleton key and lose it in a pile of leaves. Rats.

          if (request.responseText.contains("you accidentally drop your skeleton key")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(726, "Top of the Tree, Ma!", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "drop blood kiwi")
            .turnCost(1)
            .attachItem(ItemPool.BLOOD_KIWI, new DisplayAll("kiwi"));
        new Option(2, "-sleaze").turnCost(1);
        new Option(3, "moon-amber").turnCost(1).attachItem(ItemPool.MOON_AMBER, 1, AUTO);
        new Option(6, "Return to The Tallest Tree").leadsTo(725, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request != null) {
          getOption(1).text("drop blood kiwi (make sure someone's waiting)");
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(727, "All Along the Watchtower", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "fewer ghosts").turnCost(1);
        new Option(2, "7-11 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 7, AUTO);
        new Option(3, "muscle substats").turnCost(1);
        new Option(6, "Return to The Tallest Tree").leadsTo(725, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(728, "Treebasing", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "blood kiwi (from above)").leadsTo(761);
        new Option(2, "Dreadsylvanian seed pod")
            .turnCost(1)
            .attachItem(ItemPool.DREAD_POD, 1, AUTO);
        new Option(3, "folder (owl)")
            .turnCost(1)
            .attachItem(ItemPool.FOLDER_HOLDER, new DisplayAll(NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem(ItemPool.FOLDER_22, 1, AUTO);
        new Option(6, "Return to The Tallest Tree").leadsTo(725, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          if (InventoryManager.getAccessibleCount(ItemPool.FOLDER_HOLDER) == 0) {
            getOption(3).reset();
          }
        }
      }
    };

    new ChoiceAdventure(729, "Below the Roots", "Dreadsylvanian Woods") {
      void setup() {
        this.customName = "Burrows";
        this.customOrder = 3;

        // Hot
        new Option(1).leadsTo(730, false, o -> o.index != 6);
        // Cold
        new Option(2).leadsTo(731, false, o -> o.index != 6);
        // Smelly
        new Option(3).leadsTo(732, false, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil3");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil3");
      }
    };

    new ChoiceAdventure(730, "Hot Coals", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "-hot").turnCost(1);
        new Option(2, "100 adv of Dragged Through the Coals")
            .turnCost(1)
            .attachEffect("Dragged Through the Coals");
        new Option(3, "old ball and chain -> cool iron ingot")
            .turnCost(1)
            .attachItem(
                ItemPool.OLD_BALL_AND_CHAIN,
                -1,
                MANUAL,
                new DisplayAll("ball and chain", NEED, INV_ONLY_AT_LEAST, 1))
            .attachItem(ItemPool.COOL_IRON_INGOT, 1, AUTO);
        new Option(6, "Return to The Burrows").leadsTo(729, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(731, "The Heart of the Matter", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "-cold").turnCost(1);
        new Option(2, "mysticality substats").turnCost(1);
        new Option(3, "100 adv of Nature's Bounty").turnCost(1).attachEffect("Nature's Bounty");
        new Option(6, "Return to The Burrows").leadsTo(729, true);
      }
    };

    new ChoiceAdventure(732, "Once Midden, Twice Shy", "Dreadsylvanian Woods") {
      void setup() {
        new Option(1, "fewer bugbears").turnCost(1);
        new Option(2, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(6, "Return to The Burrows").leadsTo(729, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(733, "Dreadsylvanian Village Square", "Dreadsylvanian Village") {
      void setup() {
        this.customName = "Village Square";
        this.customOrder = 4;

        // Schoolhouse (locked)
        new Option(1, "possibly locked")
            .leadsTo(734, false, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        // Blacksmith
        new Option(2).leadsTo(735, true, o -> o.index != 6);
        // Gallows
        new Option(3).leadsTo(736, true, o -> o.index != 6 && o.index != 3);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil4");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil4");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          // The schoolhouse

          // You try the door of the schoolhouse, but it's locked. You
          // try your skeleton key in the lock, but it works. I mean and
          // it works. But it breaks. That was the but.

          if (request.responseText.contains("But it breaks")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(734, "Fright School", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "fewer ghosts").turnCost(1);
        new Option(2, "ghost pencil").turnCost(1).attachItem(ItemPool.GHOST_PENCIL, 1, AUTO);
        new Option(3, "mysticality substats").turnCost(1);
        new Option(6, "Return to The Village Square").leadsTo(733, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(735, "Smith, Black as Night", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "-cold").turnCost(1);
        new Option(2, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(3, "cool iron ingot + warm fur -> cooling iron equipment")
            .leadsTo(762)
            // inv only?
            .attachItem(ItemPool.HOTHAMMER, new DisplayAll("^", NEED, INV_ONLY_AT_LEAST, 1))
            .attachItem(ItemPool.COOL_IRON_INGOT, new DisplayAll("ingot", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.WARM_FUR, new DisplayAll("warm fur", NEED, AT_LEAST, 1));
        new Option(6, "Return to The Village Square").leadsTo(733, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (request == null) {
          if (InventoryManager.getCount(ItemPool.HOTHAMMER) == 0) {
            getOption(3).reset();
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(736, "Good Noose, Everyone!", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "-spooky").turnCost(1);
        new Option(2).leadsTo(765, true, o -> o.index == 1);
        new Option(3, "refresh the trap door").entersQueue(false);
        new Option(4, "help clannie gain an item");
        new Option(6, "Return to The Village Square").leadsTo(733, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 4) {
          // You pull the lever, sending your clanmate <Clanmate> hurtling through the floor of the
          // gallows platform. Whee!
          Option option = getOption(decision);

          if (request.responseText.contains("hurtling through the floor of the gallows")) {
            option.turnCost(1);
          } else {
            option.entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(737, "The Even More Dreadful Part of Town", "Dreadsylvanian Village") {
      void setup() {
        this.customName = "Skid Row";
        this.customOrder = 5;

        // Sewers
        new Option(1).leadsTo(738, false, o -> o.index != 6);
        // Tenement
        new Option(2).leadsTo(740, false, o -> o.index != 6);
        // Ticking Shack (moxie only)
        new Option(3).leadsTo(739, false, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMoxieClass()) {
          getOption(3).reset("you're not a moxie class :c ");
        }

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil5");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil5");
      }
    };

    new ChoiceAdventure(738, "A Dreadful Smell", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "-stench").turnCost(1);
        new Option(2, "100 adv of Sewer-Drenched").turnCost(1).attachEffect("Sewer-Drenched");
        new Option(6, "Return to Skid Row").leadsTo(737, true);
      }
    };

    new ChoiceAdventure(739, "The Tinker's. Damn.", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(2, "lock impression + music box parts -> replica key")
            .turnCost(1)
            .attachItem(
                ItemPool.WAX_LOCK_IMPRESSION,
                -1,
                MANUAL,
                new DisplayAll("impression", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.INTRICATE_MUSIC_BOX_PARTS,
                -1,
                MANUAL,
                new DisplayAll("box parts", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.REPLICA_KEY, 1, AUTO);
        new Option(3, "moon-amber -> polished moon-amber")
            .turnCost(1)
            .attachItem(
                ItemPool.MOON_AMBER, -1, MANUAL, new DisplayAll("^moon-amber", NEED, AT_LEAST, 1))
            .attachItem("polished moon-amber", 1, AUTO);
        new Option(4, "3 music box parts + clockwork key -> mechanical songbird")
            .turnCost(1)
            .attachItem(
                ItemPool.INTRICATE_MUSIC_BOX_PARTS,
                -3,
                MANUAL,
                new DisplayAll("box parts", NEED, AT_LEAST, 3))
            .attachItem(
                ItemPool.DREADSYLVANIAN_CLOCKWORK_KEY,
                -1,
                MANUAL,
                new DisplayAll("clockwork key", NEED, AT_LEAST, 1))
            .attachItem("unwound mechanical songbird", 1, AUTO);
        new Option(5, "3 lengths of old fuse")
            .turnCost(1)
            .attachItem("length of old fuse", 3, AUTO);
        new Option(6, "Return to Skid Row").leadsTo(737, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 || decision == 2 || decision == 3 || decision == 4) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(740, "Eight, Nine, Tenement", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "fewer skeletons").turnCost(1);
        new Option(2, "-sleaze").turnCost(1);
        new Option(3, "muscle substats").turnCost(1);
        new Option(6, "Return to Skid Row").leadsTo(737, true);
      }
    };

    new ChoiceAdventure(741, "The Old Duke's Estate", "Dreadsylvanian Village") {
      void setup() {
        this.customName = "Old Duke's Estate";
        this.customOrder = 6;

        // Cemetery
        new Option(1).leadsTo(742, false, o -> o.index != 6);
        // Servants' Quarters
        new Option(2).leadsTo(743, true, o -> o.index != 6);
        // Master Suite (locked)
        new Option(3, "possibly locked")
            .leadsTo(744, false, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil6");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil6");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          // Make your way to the master suite

          // You find the door to the old Duke's master bedroom and
          // unlock it with your skeleton key.

          if (request.responseText.contains("unlock it with your skeleton key")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(742, "The Plot Thickens", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "fewer zombies").turnCost(1);
        new Option(2, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(3, "100 adv of Fifty Ways to Bereave Your Lover")
            .turnCost(1)
            .attachEffect("Fifty Ways to Bereave Your Lover");
        new Option(6, "Return to The Old Duke's Estate").leadsTo(741, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(743, "No Quarter", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "-hot").turnCost(1);
        new Option(
                2,
                "dread tarragon + dreadful roast + bone flour + stinking agaricus -> Dreadsylvanian shepherd's pie")
            .turnCost(1)
            .attachItem(
                ItemPool.DREAD_TARRAGON,
                -1,
                MANUAL,
                new DisplayAll("dread tarragon", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.DREADFUL_ROAST,
                -1,
                MANUAL,
                new DisplayAll("dreadful roast", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.BONE_FLOUR, -1, MANUAL, new DisplayAll("bone flour", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.STINKING_AGARICUS,
                -1,
                MANUAL,
                new DisplayAll("stinking agaricus", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.SHEPHERDS_PIE, 1, AUTO);
        new Option(3, "moxie substats").turnCost(1);
        new Option(6, "Return to The Old Duke's Estate").leadsTo(741, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMysticalityClass()) {
          getOption(2).reset(request != null ? "you're not a mysticality class :c " : null);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          // Make a shepherd's pie
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(744, "The Master Suite -- Sweet!", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "fewer werewolves").turnCost(1);
        new Option(2, "eau de mort").turnCost(1).attachItem(ItemPool.EAU_DE_MORT, 1, AUTO);
        new Option(3, "ghost threads -> ghost shawl")
            .turnCost(1)
            .attachItem(
                ItemPool.GHOST_THREAD, -10, MANUAL, new DisplayAll("threads", NEED, AT_LEAST, 10))
            .attachItem(ItemPool.GHOST_SHAWL, 1, AUTO);
        new Option(6, "Return to The Old Duke's Estate").leadsTo(741, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          // Mess with the loom
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(745, "This Hall is Really Great", "Dreadsylvanian Castle") {
      void setup() {
        this.customName = "Great Hall";
        this.customOrder = 8;

        // Ballroom (locked)
        new Option(1, "possibly locked")
            .leadsTo(746, false, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        // Kitchen
        new Option(2).leadsTo(747, false, o -> o.index != 6);
        // Dining Room
        new Option(3).leadsTo(748, true, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil7");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil7");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          // Head to the ballroom

          // You unlock the door to the ballroom with your skeleton
          // key. You open the door, and are so impressed by the site of
          // the elegant ballroom that you drop the key down a nearby
          // laundry chute.

          if (request.responseText.contains("you drop the key")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(746, "The Belle of the Ballroom", "Dreadsylvanian Castle") {
      final AdventureResult MUDDY_SKIRT = ItemPool.get(ItemPool.MUDDY_SKIRT, -1);
      final AdventureResult WEEDY_SKIRT = ItemPool.get(ItemPool.WEEDY_SKIRT, 1);

      void setup() {
        new Option(1, "fewer vampires").turnCost(1);
        new Option(2, "muddy skirt + seed pod -> weedy skirt. Also, moxie substats")
            .turnCost(1)
            // we make them manual in postChoice1
            .attachItem(
                MUDDY_SKIRT, AUTO, new DisplayAll("muddy skirt", NEED, EQUIPPED_AT_LEAST, 1))
            .attachItem(ItemPool.DREAD_POD, -1, AUTO, new DisplayAll("seed pod", NEED, AT_LEAST, 1))
            .attachItem(WEEDY_SKIRT, AUTO, new DisplayAll("weedy skirt"));
        new Option(6, "Return to The Great Hall").leadsTo(745, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          // Trip the light fantastic

          // You twirl around on the dance floor to music only you can
          // hear, your muddy skirt whirling around you filthily. You get
          // so caught up in the twirling that you drop your seed pod. It
          // breaks open, spreading weed seeds all over your skirt, which
          // immediately take root and grow.

          if (request.responseText.contains("spreading weed seeds all over your skirt")) {
            EquipmentManager.discardEquipment(ItemPool.MUDDY_SKIRT);
            EquipmentManager.setEquipment(EquipmentManager.PANTS, WEEDY_SKIRT);
            getOption(decision).attachItem(ItemPool.DREAD_POD, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(747, "Cold Storage", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "-cold").turnCost(1);
        new Option(2, "100 adv of Staying Frosty").turnCost(1).attachEffect("Staying Frosty");
        new Option(6, "Return to The Great Hall").leadsTo(745, true);
      }
    };

    new ChoiceAdventure(748, "Dining In (the Castle)", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "dreadful roast").turnCost(1).attachItem(ItemPool.DREADFUL_ROAST, 1, AUTO);
        new Option(2, "-stench").turnCost(1);
        new Option(3, "wax banana").turnCost(1).attachItem(ItemPool.WAX_BANANA, 1, AUTO);
        new Option(6, "Return to The Great Hall").leadsTo(745, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMysticalityClass()) {
          getOption(3).reset(request != null ? "you're not a mysticality class :c " : null);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 || decision == 3) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(749, "Tower Most Tall", "Dreadsylvanian Castle") {
      void setup() {
        this.customName = "Tower";
        this.customOrder = 7;

        // Laboratory (locked)
        new Option(1, "possibly locked")
            .leadsTo(750, true, o -> o.index != 6)
            .attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY);
        // Books (mysticality only)
        new Option(2).leadsTo(751, true, o -> o.index != 6);
        // Bedroom
        new Option(3).leadsTo(752, false, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!KoLCharacter.isMysticalityClass()) {
          getOption(2).reset("you're not a mysticality class :c ");
        }
        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil8");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil8");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          // Go to the laboratory

          // You use your skeleton key to unlock the door to the
          // laboratory. Unfortunately, the lock is electrified, and it
          // incinerates the key shortly afterwards.

          if (request.responseText.contains("it incinerates the key")) {
            getOption(decision).attachItem(ItemPool.DREADSYLVANIAN_SKELETON_KEY, -1, MANUAL);
          }
        }
      }
    };

    new ChoiceAdventure(750, "Working in the Lab, Late One Night", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "fewer bugbears").turnCost(1);
        new Option(2, "fewer zombies").turnCost(1);
        new Option(3, "visit The Machine").leadsTo(764).attachItem(ItemPool.SKULL_CAPACITOR);
        new Option(4, "blood kiwi + eau de mort -> bloody kiwitini")
            .turnCost(1)
            .attachItem(
                ItemPool.BLOOD_KIWI, -1, MANUAL, new DisplayAll("blood kiwi", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.EAU_DE_MORT, -1, MANUAL, new DisplayAll("de mort", NEED, AT_LEAST, 1))
            .attachItem(ItemPool.BLOODY_KIWITINI, 1, AUTO);
        new Option(6, "Return to The Tower").leadsTo(749, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!KoLCharacter.isMoxieClass()) {
          getOption(4).reset(request != null ? "you're not a moxie class :c " : null);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3) {
          // You approach The Machine, and notice that the
          // capacitor you're carrying fits perfectly into an
          // obviously empty socket on the base of it. You plug
          // it in, and The Machine whirs ominously to life.......

          if (request.responseText.contains("You plug it in")) {
            getOption(decision).attachItem(ItemPool.SKULL_CAPACITOR, -1, MANUAL);
          }
        } else if (decision == 4) {
          // Use the still
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(751, "Among the Quaint and Curious Tomes", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "fewer skeletons").turnCost(1);
        new Option(2, "mysticality substats").turnCost(1);
        new Option(3, "learn recipe for moon-amber necklace")
            .turnCost(1)
            .attachItem(ItemPool.MOON_AMBER_NECKLACE);
        new Option(6, "Return to The Tower").leadsTo(749, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        if (!Preferences.getBoolean("unknownRecipe" + ItemPool.MOON_AMBER_NECKLACE)) {
          if (request == null) {
            getOption(3).reset();
          } else {
            // don't reset, just in case we're wrong
            getOption(3).text("recipe already known");
          }
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3
            && request.responseText.contains("already learned everything from that book")) {
          this.choiceFailed().entersQueue(false);
        }
      }
    };

    new ChoiceAdventure(752, "In The Boudoir", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "-sleaze").turnCost(1);
        new Option(2, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(3, "100 adv of Magically Fingered")
            .turnCost(1)
            .attachEffect("Magically Fingered");
        new Option(6, "Return to The Tower").leadsTo(749, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(753, "The Dreadsylvanian Dungeon", "Dreadsylvanian Castle") {
      void setup() {
        this.customName = "Dungeons";
        this.customOrder = 9;

        // Prison
        new Option(1).leadsTo(754, false, o -> o.index != 6);
        // Boiler Room
        new Option(2).leadsTo(755, false, o -> o.index != 6);
        // Guard room
        new Option(3).leadsTo(756, false, o -> o.index != 6);
        new Option(5, "learn shortcut", true)
            .turnCost(1)
            .attachItem(ItemPool.GHOST_PENCIL, -1, MANUAL, new DisplayAll(NEED, AT_LEAST, 1));
        new Option(6, "skip adventure", true).entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        CADatabase700to799.shortcutSpoiler(getOption(5), "ghostPencil9");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.ghostPencilDecisionRedirect(
            responseText, decision, "ghostPencil9");
      }
    };

    new ChoiceAdventure(754, "Live from Dungeon Prison", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "-spooky").turnCost(1);
        new Option(2, "muscle substats").turnCost(1);
        new Option(3, "+MP").turnCost(1);
        new Option(6, "Return to The Dungeons").leadsTo(753, true);
      }
    };

    new ChoiceAdventure(755, "The Hot Bowels", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "-hot").turnCost(1);
        new Option(2, "5-6 Freddies").turnCost(1).attachItem(ItemPool.KRUEGERAND, 5, AUTO);
        new Option(3, "+each substats").turnCost(1);
        new Option(6, "Return to The Dungeons").leadsTo(753, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(756, "Among the Fungus", "Dreadsylvanian Castle") {
      void setup() {
        new Option(1, "stinking agaricus")
            .turnCost(1)
            .attachItem(ItemPool.STINKING_AGARICUS, 1, AUTO);
        new Option(2, "100 adv of Spore-wreathed").turnCost(1).attachEffect("Spore-wreathed");
        new Option(6, "Return to The Dungeons").leadsTo(753, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new UnknownChoiceAdventure(757);

    new ChoiceAdventure(758, "End of the Path", "Dreadsylvanian Woods") {
      final AdventureResult FIRST_BLOOD_KIWI = EffectPool.get(EffectPool.FIRST_BLOOD_KIWI);
      final AdventureResult MAKESHIFT_TURBAN = ItemPool.get(ItemPool.MAKESHIFT_TURBAN);
      final AdventureResult TEMPORARY_BLINDNESS = EffectPool.get(EffectPool.TEMPORARY_BLINDNESS);
      final AdventureResult HELPS_YOU_SLEEP = ItemPool.get(ItemPool.HELPS_YOU_SLEEP);
      final AdventureResult SLEEP_MASK = ItemPool.get(ItemPool.SLEEP_MASK);

      void setup() {
        this.isSuperlikely = true;

        new Option(1)
            .attachItem(
                ItemPool.MOON_AMBER_NECKLACE,
                new DisplayAll("necklace", WANT, EQUIPPED_AT_LEAST, 1))
            .attachEffect(FIRST_BLOOD_KIWI, new ImageOnly("First Blood Kiwi"));
        new Option(2, "Run away");
      }

      @Override
      void visitChoice(GenericRequest request) {
        boolean isBlind =
            KoLConstants.activeEffects.contains(TEMPORARY_BLINDNESS)
                || KoLCharacter.hasEquipped(MAKESHIFT_TURBAN)
                || KoLCharacter.hasEquipped(HELPS_YOU_SLEEP)
                || KoLCharacter.hasEquipped(SLEEP_MASK);

        Option option = getOption(1);

        StringBuilder buffer =
            new StringBuilder(
                "GWotA (moon-amber necklace for hard mode) / Falls-From-Sky (First Blood Kiwi (");

        if (KoLConstants.activeEffects.contains(FIRST_BLOOD_KIWI)) {
          buffer.append("Have ");

          if (isBlind) {
            buffer.append("and blind");
          } else {
            buffer.append("<font color=\"red\">but NOT BLIND</font>");
          }
        } else {
          buffer.append("from bloody kiwitini");
          option.attachItem(ItemPool.BLOODY_KIWITINI, new DisplayAll("bloody kiwitini"));
        }
        buffer.append(") for hard mode)");

        option.text(buffer.toString());
      }
    };

    new ChoiceAdventure(759, "You're About to Fight City Hall", "Dreadsylvanian Village") {
      void setup() {
        this.isSuperlikely = true;

        new Option(
                1, "Mayor Ghost (auditor's badge for hard mode) / ZHOA (weedy skirt for hard mode)")
            .attachItem(
                ItemPool.AUDITORS_BADGE,
                new DisplayAll("auditor's badge", WANT, EQUIPPED_AT_LEAST, 1))
            .attachItem(
                ItemPool.WEEDY_SKIRT, new DisplayAll("weedy skirt", WANT, EQUIPPED_AT_LEAST, 1));
        new Option(2, "Run away");
      }
    };

    new ChoiceAdventure(760, "Holding Court", "Dreadsylvanian Castle") {
      final AdventureResult SHEPHERDS_BREATH = EffectPool.get(EffectPool.SHEPHERDS_BREATH);

      void setup() {
        this.isSuperlikely = true;

        new Option(1)
            .attachItem(ItemPool.GHOST_SHAWL, new DisplayAll("shawl", WANT, EQUIPPED_AT_LEAST, 1))
            .attachEffect(SHEPHERDS_BREATH, new ImageOnly("Shepherd's Breath"));
        new Option(2, "Run away");
      }

      @Override
      void visitChoice(GenericRequest request) {
        Option option = getOption(1);

        StringBuilder buffer =
            new StringBuilder(
                "Drunkula (ghost shawl for hard mode) / Unkillable skeleton (Shepherd's Breath (");
        if (KoLConstants.activeEffects.contains(SHEPHERDS_BREATH)) {
          buffer.append("Have");
        } else {
          buffer.append("from Shepherd's pie");
          option.attachItem(ItemPool.SHEPHERDS_PIE, new DisplayAll("Shepherd's pie"));
        }
        buffer.append(") for hard mode)");

        option.text(buffer.toString());
      }
    };

    new ChoiceAdventure(761, "Staring Upwards...", "Dreadsylvanian Woods") {
      void setup() {
        new Option(
                1,
                "if a clanmate stomps at the top of the tree, get a blood kiwi. Otherwise refreshes")
            .turnCost(1)
            .attachItem(ItemPool.BLOOD_KIWI, 1, AUTO, new DisplayAll("kiwi"));
        new Option(2).leadsTo(728, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(762, "Try New Extra-Strength Anvil", "Dreadsylvanian Village") {
      void setup() {
        new Option(1, "cool iron ingot + warm fur -> cooling iron helmet")
            .turnCost(1)
            .attachItem(
                ItemPool.COOL_IRON_INGOT, -1, MANUAL, new DisplayAll("ingot", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.WARM_FUR, -1, MANUAL, new DisplayAll("warm fur", NEED, AT_LEAST, 1))
            .attachItem("cooling iron helmet", 1, AUTO);
        new Option(2, "cool iron ingot + warm fur -> cooling iron breastplate")
            .turnCost(1)
            .attachItem(
                ItemPool.COOL_IRON_INGOT, -1, MANUAL, new DisplayAll("ingot", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.WARM_FUR, -1, MANUAL, new DisplayAll("warm fur", NEED, AT_LEAST, 1))
            .attachItem("cooling iron breastplate", 1, AUTO);
        new Option(3, "cool iron ingot + warm fur -> cooling iron greaves")
            .turnCost(1)
            .attachItem(
                ItemPool.COOL_IRON_INGOT, -1, MANUAL, new DisplayAll("ingot", NEED, AT_LEAST, 1))
            .attachItem(
                ItemPool.WARM_FUR, -1, MANUAL, new DisplayAll("warm fur", NEED, AT_LEAST, 1))
            .attachItem("cooling iron greaves", 1, AUTO);
        new Option(4).leadsTo(735, true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 || decision == 2 || decision == 3) {
          if (!request.responseText.contains("You acquire")) {
            this.choiceFailed().entersQueue(false);
          }
        }
      }
    };

    new ChoiceAdventure(763, "It's Kind of a Big Deal", null) {
      void setup() {
        new Option(1);
      }
    };

    new ChoiceAdventure(764, "The Machine", "Dreadsylvanian Castle") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1);
        new Option(2);
        new Option(3);
        // TODO can we parse the choice adventure to
        // tell what skill they are about to get?
        new Option(4, "feel the POWER");
        new Option(5);
        new Option(6).leadsTo(750, true);
      }
    };

    new ChoiceAdventure(765, "Hello Gallows", "Dreadsylvanian Village") {
      final AdventureResult HANGMAN_HOOD = new AdventureResult("hangman's hood", 1, false);
      final AdventureResult CURSED_RING_FINGER_RING =
          new AdventureResult("cursed ring finger ring", 1, false);
      final AdventureResult DREADSYLVANIAN_CLOCKWORK_KEY =
          ItemPool.get(ItemPool.DREADSYLVANIAN_CLOCKWORK_KEY, 1);

      void setup() {
        new Option(1);
        new Option(2).leadsTo(736, true);
      }

      @Override
      void visitChoice(GenericRequest request) {
        // lookaheadsafe

        Option option = getOption(1);
        AdventureResult item = null;
        String name = "nothing";

        if (KoLCharacter.isMuscleClass()) {
          item = HANGMAN_HOOD;
        } else if (KoLCharacter.isMysticalityClass()) {
          item = CURSED_RING_FINGER_RING;
        } else if (KoLCharacter.isMoxieClass()) {
          item = DREADSYLVANIAN_CLOCKWORK_KEY;
        }

        if (item != null) {
          name = item.getName();
          option.attachItem(item, AUTO, new DisplayAll(name));
        }

        option.text("gain " + name + " with help of clannie");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1) {
          // Your clanmate <Clanmate> pulls the lever, opening the trapdoor beneath you. Whee!
          Option option = getOption(decision);

          if (request.responseText.contains("opening the trapdoor beneath you")) {
            option.turnCost(1);
          } else {
            option.entersQueue(false);
          }
        }
      }
    };

    new UnknownChoiceAdventure(766);

    new ChoiceAdventure(767, "Tales of Dread", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        // *nothing* -- this choice adventure uses the field
        // "whichstory" to select things
      }
    };

    new ChoiceAdventure(768, "The Littlest Identity Crisis", null) {
      final AdventureResult SAUCE_SC = new AdventureResult("Elbow Sauce", 1, true);
      final AdventureResult SAUCE_TT = new AdventureResult("Wet Rub", 1, true);
      final AdventureResult SAUCE_PM = new AdventureResult("Saucefingers", 1, true);
      final AdventureResult SAUCE_S = new AdventureResult("Saucegoggles", 1, true);
      final AdventureResult SAUCE_DB = new AdventureResult("Shoesauce", 1, true);
      final AdventureResult SAUCE_AT = new AdventureResult("Corona de la Salsa", 1, true);
      final AdventureResult SONG_SC = new AdventureResult("Frio Como Helado", 1, true);
      final AdventureResult SONG_TT = new AdventureResult("Concha de Tortuga", 1, true);
      final AdventureResult SONG_PM = new AdventureResult("Riqueza de Pasta", 1, true);
      final AdventureResult SONG_S =
          new AdventureResult("Cafeter&iacute;a Brujer&iacute;a", 1, true);
      final AdventureResult SONG_DB = new AdventureResult("Carne Obsesionada", 1, true);
      final AdventureResult SONG_AT = new AdventureResult("Bailando, Fernando", 1, true);

      void setup() {
        new Option(
            1,
            "1+: physical dmg<br>5+: volleyball(+stats)<br>10+: <font color=\"blue\">cold damage</font>+stun<br>15+: removes debuffs");
        new Option(
            2,
            "1+: volleyball(+stats)<br>5+: starfish(siphons enemy HP into MP)<br>10+: physical dmg + delevel<br>15+: <font color=\"grey\">spooky damage</font>");
        new Option(
            3,
            "1+: potato(blocks)<br>5+: regen HP<br>10+: leprechaun(+meat)<br>15+: prismatic dmg");
        new Option(4);
        new Option(
            5,
            "1+: delevels<br>5+: fairy(+items)<br>10+: mosquito(siphon enemy HP)<br>15+: physical dmg + delevel");
        new Option(6);
      }

      @Override
      void visitChoice(GenericRequest request) {
        final AdventureResult sauce;
        final AdventureResult song;

        switch (KoLCharacter.getAscensionClass()) {
          case SEAL_CLUBBER:
            sauce = SAUCE_SC;
            song = SONG_SC;
            break;
          case TURTLE_TAMER:
            sauce = SAUCE_TT;
            song = SONG_TT;
            break;
          case PASTAMANCER:
            sauce = SAUCE_PM;
            song = SONG_PM;
            break;
          case SAUCEROR:
            sauce = SAUCE_S;
            song = SONG_S;
            break;
          case DISCO_BANDIT:
            sauce = SAUCE_DB;
            song = SONG_DB;
            break;
          case ACCORDION_THIEF:
            sauce = SAUCE_AT;
            song = SONG_AT;
            break;
          default:
            sauce = null;
            song = null;
        }

        String sauceText = "nothing :(";
        String songText = "nothing :(";
        Option sauceOption = getOption(4);
        Option songOption = getOption(6);

        if (sauce != null) {
          sauceText = sauce.getName();
          sauceOption.attachEffect(sauce, new ImageOnly(sauceText));
        }
        if (song != null) {
          songText = song.getName();
          songOption.attachEffect(song, new ImageOnly(songText));
        }

        sauceOption.text(
            "1+: leprechaun(+meat)<br>"
                + "5+: <font color=\"red\">hot</font> or <font color=\"blue\">cold</font> damage<br>"
                + "10+: <font color=\"blue\">cold</font> starfish(uses cold dmg to siphon enemy HP into MP)<br>"
                + "15+: "
                + sauceText);
        songOption.text(
            "1+: "
                + songText
                + "<br>"
                + "5+: ghoul whelp(regen HP & MP)<br>"
                + "10+: fairy(+items)<br>"
                + "15+: sombrero(+stats, ML dependend)");
      }
    };

    new ChoiceAdventure(769, "The Super-Secret Canadian Mind-Control Device", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(770, "The Institute for Canadian Studies", "Pump Up Mysticality") {
      void setup() {
        this.neverAutomate = true;
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(771, "It Was All a Horrible, Horrible Dream", "undefined") {
      void setup() {
        this.neverEntersQueue = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(772, "Saved by the Bell", null) {
      final AdventureResult YEARBOOK_CAMERA = ItemPool.get(ItemPool.YEARBOOK_CAMERA, 1);
      final Pattern YEARBOOK_TARGET_PATTERN = Pattern.compile("<b>Results:</b>.*?<b>(.*?)</b>");

      void setup() {
        this.neverEntersQueue = true;

        new Option(1).attachEffect("School Spirited");
        new Option(2, "50 adv of Poetically Licensed").attachEffect("Poetically Licensed");
        new Option(3, "Get Yearbook Camera").attachItem(ItemPool.YEARBOOK_CAMERA, new ImageOnly());
        new Option(4, "50 adv of Cut But Not Dried").attachEffect("Cut But Not Dried");
        new Option(5, "50 turns of Isskay Like An Ashtray").attachEffect("Isskay like an Ashtray");
        new Option(6, "Make items");
        new Option(7, "Make items");
        new Option(8, "Make items");
        new Option(10, "Leave");
      }

      @Override
      void visitChoice(GenericRequest request) {
        // If you reach this encounter and Mafia things you've not spend 40 adventures in KOL High
        // school, correct this
        Preferences.setInteger("_kolhsAdventures", 40);

        Option option = getOption(1);
        if (Preferences.getBoolean("_kolhsSchoolSpirited")) {
          option.reset("Already got School Spirited today");
        } else {
          option.text(
              (Preferences.getInteger("kolhsTotalSchoolSpirited") + 1) * 10
                  + " adv of School Spirited");
        }

        if (Preferences.getBoolean("_kolhsPoeticallyLicenced")) {
          getOption(2).reset("Already got Poetically Licensed today");
        }

        if (InventoryManager.getCount(YEARBOOK_CAMERA) > 0
            || KoLCharacter.hasEquipped(YEARBOOK_CAMERA)) {
          getOption(3).text("Turn in yesterday's photo (if you have it)");
        }

        if (Preferences.getBoolean("_kolhsCutButNotDried")) {
          getOption(4).reset("Already got Cut But Not Dried today");
        }

        if (Preferences.getBoolean("_kolhsIsskayLikeAnAshtray")) {
          getOption(5).reset("Already got Isskay Like An Ashtray today");
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setBoolean("_kolhsSchoolSpirited", true);
          Preferences.increment("kolhsTotalSchoolSpirited", 1);
        } else if (decision == 2) {
          Preferences.setBoolean("_kolhsPoeticallyLicenced", true);
        } else if (decision == 3) {
          // You walk into the Yearbook Club and collar the kid with all
          // the camera equipment from yesterday. "Let me check your
          // memory card," he says, plugging the camera into a computer.
          // "Yup! You got it! Nice work. Here's your reward -- a nice
          // new accessory for that camera! If you're interested, now we
          // need a picture of a <b>monster</b>. You up for it?"
          //
          // You walk back into the Yearbook Club, a little tentatively.
          // "All right! Let's see what you've got!" the camera kid
          // says, and plugs your camera into a computer. "Aw, man, you
          // didn't get it? Well, I'll give you another chance.  If you
          // can still get us a picture of a <b>monster</b> and bring it
          // in tomorrow, you're still in the Club."
          //
          // You poke your head into the Yearbook Club room, but the
          // camera kid's packing up all the equipment and putting it
          // away. "Sorry, gotta go," he says, "but remember, you've
          // gotta get a picture of a <b>monster</b> for tomorrow, all
          // right? We're counting on you."

          if (request.responseText.contains("You got it!")) {
            Preferences.setString("yearbookCameraTarget", "");
            Preferences.setBoolean("yearbookCameraPending", false);
            Preferences.increment("yearbookCameraUpgrades", 1, 20, false);
            if (KoLCharacter.getAscensions()
                != Preferences.getInteger("lastYearbookCameraAscension")) {
              Preferences.setInteger("lastYearbookCameraAscension", KoLCharacter.getAscensions());
              Preferences.increment("yearbookCameraAscensions", 1, 20, false);
            }
          }

          Matcher matcher = YEARBOOK_TARGET_PATTERN.matcher(request.responseText);
          if (matcher.find()) {
            Preferences.setString("yearbookCameraTarget", matcher.group(1));
          }
        } else if (decision == 4) {
          Preferences.setBoolean("_kolhsCutButNotDried", true);
        } else if (decision == 5) {
          Preferences.setBoolean("_kolhsIsskayLikeAnAshtray", true);
        }
        if (decision < 10) {
          Preferences.increment("_kolhsSavedByTheBell", 1);
        }
      }
    };

    new UnknownChoiceAdventure(773);

    new ChoiceAdventure(774, "Opening up the Folder Holder", "Item-Driven") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
      }

      @Override
      void visitChoice(GenericRequest request) {
        String option = request.getFormField("forceoption");
        if (option != null) {
          ChoiceManager.lastDecision = StringUtilities.parseInt(option);
        }
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        // Choice 1 is adding a folder.
        if (decision == 1
            && request.responseText.contains("You carefully place your new folder in the holder")) {
          // Figure out which one it was from the URL
          String id = request.getFormField("folder");
          AdventureResult folder = EquipmentRequest.idToFolder(id);
          getOption(decision).attachItem(folder.getInstance(-1), MANUAL);
        }

        // Choice 2 is removing a folder. Since the folder is
        // destroyed, it does not go back to inventory.

        // Set all folder slots from the response request.responseText
        EquipmentRequest.parseFolders(request.responseText);
      }
    };

    new ChoiceAdventure(775, "Too School for Cool", null) {
      void setup() {
        new Option(1);
      }
    };

    new UnknownChoiceAdventure(776);

    new UnknownChoiceAdventure(777);

    new ChoiceAdventure(778, "If You Could Only See", "Item-Driven") {
      final AdventureResult TONIC_DJINN = ItemPool.get(ItemPool.TONIC_DJINN, -1);

      void setup() {
        this.customName = "Tonic Djinn";

        new Option(1, "gain 400-500 meat", true)
            .attachMeat(450, AUTO)
            .attachItem(TONIC_DJINN, MANUAL, new NoDisplay());
        new Option(2, "gain 50-60 muscle stats", true)
            .attachItem(TONIC_DJINN, MANUAL, new NoDisplay());
        new Option(3, "gain 50-60 mysticality stats", true)
            .attachItem(TONIC_DJINN, MANUAL, new NoDisplay());
        new Option(4, "gain 50-60 moxie stats", true)
            .attachItem(TONIC_DJINN, MANUAL, new NoDisplay());
        new Option(6, "don't use it", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 2:
          case 3:
          case 4:
            Preferences.setBoolean("_tonicDjinn", true);
            if (request.responseText.contains("already had a wish today")) {
              this.choiceFailed();
            }
        }
      }
    };

    new UnknownChoiceAdventure(779);

    new ChoiceAdventure(780, "Action Elevator", "The Hidden Apartment Building") {
      final AdventureResult CURSE1_EFFECT = EffectPool.get(EffectPool.ONCE_CURSED);
      final AdventureResult CURSE2_EFFECT = EffectPool.get(EffectPool.TWICE_CURSED);
      final AdventureResult CURSE3_EFFECT = EffectPool.get(EffectPool.THRICE_CURSED);

      int hiddenApartmentProgress = 0;
      boolean hasOnceCursed = false;
      boolean hasTwiceCursed = false;
      boolean hasThriceCursed = false;
      boolean pygmyLawyersRelocated = false;

      void setup() {
        this.customName = "Hidden Apartment";

        new Option(1);
        new Option(2).turnCost(1);
        new Option(3).turnCost(1);
        new Option(6, "skip adventure", true).entersQueue(false);

        new CustomOption(1, "fight spirit or get cursed");
        new CustomOption(3, "banish lawyers or skip adventure");
      }

      @Override
      void visitChoice(GenericRequest request) {
        hiddenApartmentProgress = Preferences.getInteger("hiddenApartmentProgress");
        hasOnceCursed = KoLConstants.activeEffects.contains(CURSE1_EFFECT);
        hasTwiceCursed = KoLConstants.activeEffects.contains(CURSE2_EFFECT);
        hasThriceCursed = KoLConstants.activeEffects.contains(CURSE3_EFFECT);
        pygmyLawyersRelocated =
            Preferences.getInteger("relocatePygmyLawyer") == KoLCharacter.getAscensions();

        Option option = getOption(1);
        if (hiddenApartmentProgress < 7) {
          if (hasThriceCursed) {
            option.text("Fight ancient protector spirit");
          } else {
            option
                .text("Need Thrice-Cursed to fight ancient protector spirit")
                .attachEffect(CURSE3_EFFECT, new ImageOnly("Cursed"));
          }
        } else {
          option.text("penthouse empty");
        }

        option = getOption(2);
        if (hasThriceCursed) {
          option.text("Increase Thrice-Cursed").attachEffect(CURSE3_EFFECT);
        } else if (hasTwiceCursed) {
          option.text("Get Thrice-Cursed").attachEffect(CURSE3_EFFECT);
        } else if (hasOnceCursed) {
          option.text("Get Twice-Cursed").attachEffect(CURSE2_EFFECT);
        } else {
          option.text("Get Once-Cursed").attachEffect(CURSE1_EFFECT);
        }

        getOption(3)
            .text(
                pygmyLawyersRelocated
                    ? "Waste adventure"
                    : "Relocate pygmy witch lawyers to Hidden Park");
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If Boss dead, skip, else if thrice-cursed, fight spirit, if not, get cursed.
        if (decision.equals("1")) {
          return hiddenApartmentProgress >= 7 ? "6" : hasThriceCursed ? "1" : "2";
        }
        // Only relocate pygmy lawyers once, then leave
        if (decision.equals("3")) {
          return pygmyLawyersRelocated ? "6" : "3";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (!request.responseText.contains("Fight!")) {
            getOption(decision).entersQueue(false);
          }

          if (hiddenApartmentProgress < 7
              && request.responseText.contains("penthouse is empty now")) {
            Preferences.setInteger("hiddenApartmentProgress", 7);
          }
        } else if (decision == 3) {
          Preferences.setInteger("relocatePygmyLawyer", KoLCharacter.getAscensions());
        }
      }
    };

    new ChoiceAdventure(781, "Earthbound and Down", "An Overgrown Shrine (Northwest)") {
      void setup() {
        this.customName = "Shrine NW";

        new Option(1, "Unlock Hidden Apartment Building").turnCost(1);
        new Option(2, "Get stone triangle")
            .attachItem(ItemPool.STONE_TRIANGLE, 1, AUTO)
            .attachItem(ItemPool.MOSS_COVERED_STONE_SPHERE, -1, MANUAL, new NoDisplay());
        new Option(3, "6-ball -> 20 adv of Blessing of Bulbazinalli", true)
            .turnCost(1)
            .attachItem(ItemPool.SIX_BALL, -1, MANUAL, new DisplayAll("6-ball"))
            .attachEffect("Blessing of Bulbazinalli");
        new Option(6, "skip adventure", true);

        new CustomOption(1, "unlock hidden apartment building or get stone triangle");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!request.responseText.contains("option value=1")
            && Preferences.getInteger("hiddenApartmentProgress") == 0) {
          Preferences.setInteger("hiddenApartmentProgress", 1);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.overgrownShrineDecision(decision, "hiddenApartmentProgress");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setInteger("hiddenApartmentProgress", 1);
          QuestDatabase.setQuestProgress(Quest.CURSES, QuestDatabase.STARTED);
        } else if (decision == 2) {
          Preferences.setInteger("hiddenApartmentProgress", 8);
        }
      }
    };

    new UnknownChoiceAdventure(782);

    new ChoiceAdventure(783, "Water You Dune", "An Overgrown Shrine (Southwest)") {
      void setup() {
        this.customName = "Shrine SW";

        new Option(1, "Unlock Hidden Hospital").turnCost(1);
        new Option(2, "Get stone triangle")
            .attachItem(ItemPool.STONE_TRIANGLE, 1, AUTO)
            .attachItem(ItemPool.DRIPPING_STONE_SPHERE, -1, MANUAL, new NoDisplay());
        new Option(3, "2-ball -> 20 adv of Blessing of Squirtlcthulli", true)
            .turnCost(1)
            .attachItem(ItemPool.TWO_BALL, -1, MANUAL, new DisplayAll("2-ball"))
            .attachEffect("Blessing of Squirtlcthulli");
        new Option(6, "skip adventure", true);

        new CustomOption(1, "unlock hidden hospital or get stone triangle");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!request.responseText.contains("option value=1")
            && Preferences.getInteger("hiddenHospitalProgress") == 0) {
          Preferences.setInteger("hiddenHospitalProgress", 1);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.overgrownShrineDecision(decision, "hiddenHospitalProgress");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setInteger("hiddenHospitalProgress", 1);
          QuestDatabase.setQuestProgress(Quest.DOCTOR, QuestDatabase.STARTED);
        } else if (decision == 2) {
          Preferences.setInteger("hiddenHospitalProgress", 8);
        }
      }
    };

    new ChoiceAdventure(784, "You, M. D.", "The Hidden Hospital") {
      void setup() {
        this.isSuperlikely = true;

        this.customName = "Hidden Hospital";

        new Option(1, "fight ancient protector spirit");
        new Option(6, "skip adventure");

        new CustomOption(1, "fight spirit");
      }
    };

    new ChoiceAdventure(785, "Air Apparent", "An Overgrown Shrine (Northeast)") {
      void setup() {
        this.customName = "Shrine NE";

        new Option(1, "Unlock Hidden Office Building").turnCost(1);
        new Option(2, "Get stone triangle")
            .attachItem(ItemPool.STONE_TRIANGLE, 1, AUTO)
            .attachItem(ItemPool.CRACKLING_STONE_SPHERE, -1, MANUAL, new NoDisplay());
        new Option(3, "1-ball -> 20 adv of Blessing of Pikachutlotal", true)
            .turnCost(1)
            .attachItem(ItemPool.ONE_BALL, -1, MANUAL, new DisplayAll("1-ball"))
            .attachEffect("Blessing of Pikachutlotal");
        new Option(6, "skip adventure", true);

        new CustomOption(1, "unlock hidden office building or get stone triangle");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!request.responseText.contains("option value=1")
            && Preferences.getInteger("hiddenOfficeProgress") == 0) {
          Preferences.setInteger("hiddenOfficeProgress", 1);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.overgrownShrineDecision(decision, "hiddenOfficeProgress");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setInteger("hiddenOfficeProgress", 1);
          QuestDatabase.setQuestProgress(Quest.BUSINESS, QuestDatabase.STARTED);
        } else if (decision == 2) {
          Preferences.setInteger("hiddenOfficeProgress", 8);
        }
      }
    };

    new ChoiceAdventure(786, "Working Holiday", "The Hidden Office Building") {
      final AdventureResult MCCLUSKY_FILE = ItemPool.get(ItemPool.MCCLUSKY_FILE, -1);
      final AdventureResult MCCLUSKY_FILE_PAGE5 = ItemPool.get(ItemPool.MCCLUSKY_FILE_PAGE5);
      final AdventureResult BINDER_CLIP = ItemPool.get(ItemPool.BINDER_CLIP, 1);

      int hiddenOfficeProgress = 0;
      boolean hasMcCluskyFile = false;
      boolean hasBinderClip = false;

      void setup() {
        this.customName = "Hidden Office";

        new Option(1, "with McClusky File (complete), fight ancient protector spirit");
        new Option(2, "Get boring binder clip").turnCost(1);
        new Option(3, "fight pygmy witch accountant");
        new Option(6, "skip adventure", true).entersQueue(false);

        new CustomOption(1, "fight spirit or get binder clip or fight accountant");
        new CustomOption(3, "fight accountant");
      }

      @Override
      void visitChoice(GenericRequest request) {
        hiddenOfficeProgress = Preferences.getInteger("hiddenOfficeProgress");
        hasMcCluskyFile = InventoryManager.getCount(MCCLUSKY_FILE) > 0;
        hasBinderClip = InventoryManager.getCount(BINDER_CLIP) > 0;

        boolean hasBossUnlock = hiddenOfficeProgress >= 6;

        Option option = getOption(1);
        if (hiddenOfficeProgress < 7) {
          option.attachItem(
              MCCLUSKY_FILE, AUTO, new DisplayAll("\\(complete\\)", NEED, AT_LEAST, 1));
        } else {
          option.text("office empty");
        }

        option = getOption(2);
        if (hasBinderClip || hasMcCluskyFile || hasBossUnlock) {
          option.text("Get random item");
        } else {
          option.attachItem(BINDER_CLIP, AUTO);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // If boss dead, fight accountant, fight boss if available, if not, get binder clip if you
        // lack it, if not, fight accountant if you still need file
        if (decision.equals("1")) {
          boolean hasMcCluskyFilePage5 = InventoryManager.getCount(MCCLUSKY_FILE_PAGE5) > 0;
          return hiddenOfficeProgress >= 7
              ? "3"
              : hasMcCluskyFile ? "1" : !hasBinderClip ? "2" : !hasMcCluskyFilePage5 ? "3" : "0";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          if (!request.responseText.contains("Fight!")) {
            getOption(decision).entersQueue(false);
          }

          if (hiddenOfficeProgress < 7 && request.responseText.contains("boss's office is empty")) {
            Preferences.setInteger("hiddenOfficeProgress", 7);
          }
        } else if (decision == 2) {
          ResultProcessor.autoCreate(ItemPool.MCCLUSKY_FILE);

          // if you don't get the expected binder clip, don't have one, and don't have a mcclusky
          // file, you must have unlocked the boss at least
          if (!request.responseText.contains("boring binder clip")
              && !hasMcCluskyFile
              && !hasBinderClip
              && hiddenOfficeProgress < 6) {
            Preferences.setInteger("hiddenOfficeProgress", 6);
          }
        }
      }
    };

    new ChoiceAdventure(787, "Fire When Ready", "An Overgrown Shrine (Southeast)") {
      void setup() {
        this.customName = "Shrine SE";

        new Option(1, "Unlock Hidden Bowling Alley").turnCost(1);
        new Option(2, "Get stone triangle")
            .attachItem(ItemPool.STONE_TRIANGLE, 1, AUTO)
            .attachItem(ItemPool.SCORCHED_STONE_SPHERE, -1, MANUAL, new NoDisplay());
        new Option(3, "5-ball -> 20 adv of Blessing of Charcoatl", true)
            .turnCost(1)
            .attachItem(ItemPool.FIVE_BALL, -1, MANUAL, new DisplayAll("5-ball"))
            .attachEffect("Blessing of Charcoatl");
        new Option(6, "skip adventure", true);

        new CustomOption(1, "unlock hidden bowling alley or get stone triangle");
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (!request.responseText.contains("option value=1")
            && Preferences.getInteger("hiddenBowlingAlleyProgress") == 0) {
          Preferences.setInteger("hiddenBowlingAlleyProgress", 1);
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        return CADatabase700to799.overgrownShrineDecision(decision, "hiddenBowlingAlleyProgress");
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          Preferences.setInteger("hiddenBowlingAlleyProgress", 1);
          QuestDatabase.setQuestProgress(Quest.SPARE, QuestDatabase.STARTED);
        } else if (decision == 2) {
          Preferences.setInteger("hiddenBowlingAlleyProgress", 8);
        }
      }
    };

    new ChoiceAdventure(788, "Life is Like a Cherry of Bowls", "The Hidden Bowling Alley") {
      void setup() {
        this.customName = "Hidden Bowling Alley";

        new Option(1, "bowl and may fight spirit", true)
            .turnCost(1)
            .attachItem(ItemPool.BOWLING_BALL, -1, MANUAL);
        new Option(6, "skip adventure").entersQueue(false);
      }

      @Override
      void visitChoice(GenericRequest request) {
        int hiddenBowlingAlleyProgress = Preferences.getInteger("hiddenBowlingAlleyProgress");

        StringBuilder text = new StringBuilder();
        text.append("Get stats, on 5th visit, fight ancient protector spirit (");
        text.append(6 - hiddenBowlingAlleyProgress);
        text.append(" visit" + (hiddenBowlingAlleyProgress < 5 ? "s" : ""));
        text.append(" left)");

        getOption(1)
            .text(
                hiddenBowlingAlleyProgress > 6
                    ? "Get stats"
                    : hiddenBowlingAlleyProgress == 6
                        ? "fight ancient protector spirit"
                        : text.toString());
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && request.responseText.contains("Fight!")) {
          // doesn't cost a turn (inherently), doesn't cost a bowling ball
          this.choiceFailed();
          Preferences.setInteger("hiddenBowlingAlleyProgress", 6);
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 1) {
          int bowlCount = Preferences.getInteger("hiddenBowlingAlleyProgress");

          if (request.responseText.contains("without a frustrated ghost to torment")
              && bowlCount < 7) {
            Preferences.setInteger("hiddenBowlingAlleyProgress", 7);
          } else if (bowlCount < 6) {
            Preferences.setInteger("hiddenBowlingAlleyProgress", Math.max(2, bowlCount + 1));
          }
        }
      }
    };

    new ChoiceAdventure(
        789, "Where Does The Lone Ranger Take His Garbagester?", "The Hidden Park") {
      boolean pygmyJanitorsRelocated = false;

      void setup() {
        this.customName = "Hidden Park";

        new Option(1, "Get random items", true)
            .turnCost(1)
            .attachItem("surgical apron", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("half-size scalpel", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("bloodied surgical dungarees", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("head mirror", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem("surgical mask", 1, AUTO, new DisplayAll(WANT, EXACTLY, 0))
            .attachItem(ItemPool.BOWLING_BALL, 1, AUTO, new DisplayAll(WANT, AT_BEST, 2));
        new Option(2, "Relocate pygmy janitors to Hidden Park").turnCost(1);
        new Option(6, "skip adventure", true).entersQueue(false);

        new CustomOption(2, "relocate pygmy janitors then get random items");
      }

      @Override
      void visitChoice(GenericRequest request) {
        pygmyJanitorsRelocated =
            Preferences.getInteger("relocatePygmyJanitor") == KoLCharacter.getAscensions();

        if (pygmyJanitorsRelocated) {
          getOption(2).text("Waste adventure");
        }
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Only relocate pygmy janitors once, then get random items
        if (decision.equals("2") && pygmyJanitorsRelocated) {
          return "1";
        }
        return decision;
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (decision == 2) {
          Preferences.setInteger("relocatePygmyJanitor", KoLCharacter.getAscensions());
        }
      }
    };

    new UnknownChoiceAdventure(790);

    new ChoiceAdventure(791, "Legend of the Temple in the Hidden City", "A Massive Ziggurat") {
      final AdventureResult STONE_TRIANGLE = ItemPool.get(ItemPool.STONE_TRIANGLE);

      void setup() {
        this.customName = "Massive Ziggurat";

        new Option(1, "fight Protector Spectre", true)
            .attachItem(STONE_TRIANGLE, new DisplayAll(NEED, EXACTLY, 4));
        new Option(6, "skip adventure", true);
      }

      @Override
      String getDecision(String responseText, String decision, int stepCount) {
        // Leave if not enough triangles to fight spectre
        if (decision.equals("1") && InventoryManager.getCount(STONE_TRIANGLE) < 4) {
          return "6";
        }
        return decision;
      }
    };

    new ChoiceAdventure(792, "The Degrassi Knoll Gym", "Pump Up Muscle") {
      void setup() {
        this.neverAutomate = true;
        this.canWalkFromChoice = true;

        new Option(1);
      }
    };

    new ChoiceAdventure(793, "Welcome to The Shore, Inc.", "The Shore, Inc. Travel Agency") {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1, "Muscle Vacation", true)
            .turnCost(3, AUTO)
            .attachMeat(-500, AUTO)
            .attachItem(ItemPool.SHIP_TRIP_SCRIP, 1, AUTO, new NoDisplay());
        new Option(2, "Mysticality Vacation", true)
            .turnCost(3, AUTO)
            .attachMeat(-500, AUTO)
            .attachItem(ItemPool.SHIP_TRIP_SCRIP, 1, AUTO, new NoDisplay());
        new Option(3, "Moxie Vacation", true)
            .turnCost(3, AUTO)
            .attachMeat(-500, AUTO)
            .attachItem(ItemPool.SHIP_TRIP_SCRIP, 1, AUTO, new NoDisplay());
        new Option(4, "gift shop");
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        switch (decision) {
          case 1:
          case 2:
          case 3:
            if (request.responseText.contains("You can't afford to go on a vacation")
                || request.responseText.contains("if you don't have time to truly enjoy it")
                || request.responseText.contains("What? Where? Huh?")) {
              this.choiceFailed();
            }
        }
      }
    };

    new ChoiceAdventure(794, "Once More Unto the Junk", "The Old Landfill") {
      void setup() {
        new Option(1, "The Bathroom of Ten Men", true)
            .leadsTo(795)
            .attachItem(ItemPool.FUNKY_JUNK_KEY, -1, MANUAL, new NoDisplay());
        new Option(2, "The Den of Iquity", true)
            .leadsTo(796)
            .attachItem(ItemPool.FUNKY_JUNK_KEY, -1, MANUAL, new NoDisplay());
        new Option(3, "Let's Workshop This a Little", true)
            .leadsTo(797)
            .attachItem(ItemPool.FUNKY_JUNK_KEY, -1, MANUAL, new NoDisplay());
      }
    };

    new ChoiceAdventure(795, "The Bathroom of Ten Men", "The Old Landfill") {
      void setup() {
        this.customName = this.name;

        new Option(1, null, true).turnCost(1).attachItem("old claw-foot bathtub", 1, AUTO);
        new Option(2, "fight random junksprite", true);
        new Option(3, "make lots of noise", true);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 3 && !request.responseText.contains("Fight!")) {
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(796, "The Den of Iquity", "The Old Landfill") {
      void setup() {
        this.customName = this.name;

        new Option(1, "make lots of noise", true);
        new Option(2, null, true).turnCost(1).attachItem("old clothesline pole", 1, AUTO);
        new Option(3, "tangle of copper wire", true)
            .turnCost(1)
            .attachItem("tangle of copper wire", 1, AUTO);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 1 && !request.responseText.contains("Fight!")) {
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(797, "Let's Workshop This a Little", "The Old Landfill") {
      void setup() {
        this.customName = this.name;

        new Option(1, "Junk-Bond", true).turnCost(1).attachItem("Junk-Bond", 1, AUTO);
        new Option(2, "make lots of noise", true);
        new Option(3, null, true).turnCost(1).attachItem("antique cigar sign", 1, AUTO);
      }

      @Override
      void postChoice1(GenericRequest request, int decision) {
        if (decision == 2 && !request.responseText.contains("Fight!")) {
          getOption(decision).turnCost(1);
        }
      }
    };

    new ChoiceAdventure(798, "Hippy Talkin'", null) {
      void setup() {
        this.canWalkFromChoice = true;

        new Option(1);
        new Option(2);
        new Option(3);
      }

      @Override
      void visitChoice(GenericRequest request) {
        if (request.responseText.contains("You should totally keep it!")) {
          QuestDatabase.setQuestProgress(Quest.HIPPY, QuestDatabase.FINISHED);
          Preferences.setInteger("lastIslandUnlock", KoLCharacter.getAscensions());
        }
      }

      @Override
      void postChoice2(GenericRequest request, int decision) {
        if (request.responseText.contains("Point me at the landfill")) {
          QuestDatabase.setQuestProgress(Quest.HIPPY, QuestDatabase.STARTED);
        }
      }
    };

    new UnknownChoiceAdventure(799);
  }

  static final void shortcutSpoiler(ChoiceAdventure.Option option, String setting) {
    if (Preferences.getBoolean(setting)) {
      option.text("shortcut KNOWN");
    }
  }

  static final String ghostPencilDecisionRedirect(
      String responseText, String decision, String setting) {
    // Option 5 - "Use a ghost pencil" - is not always available.
    // Even if it is, if you already have this shortcut, skip it
    if (decision.equals("5")
        && (!responseText.contains("Use a ghost pencil") || Preferences.getBoolean(setting))) {
      return "6";
    }
    return decision;
  }

  static final String overgrownShrineDecision(String decision, String setting) {
    // Option 1 and 2 are not always available. Take appropriate one if option to
    // take action is selected. If not,leave.
    if (decision.equals("1")) {
      int progress = Preferences.getInteger(setting);
      return progress == 7 ? "2" : progress < 1 ? "1" : "6";
    }
    return decision;
  }
}
