package net.sourceforge.kolmafia.persistence.choiceadventures;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;

import net.sourceforge.kolmafia.objectpool.ItemPool;

import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;

import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.RetiredChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.UnknownChoiceAdventure;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.request.GenericRequest;

import net.sourceforge.kolmafia.session.EquipmentManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.SeaMerkinOutpostManager;
import net.sourceforge.kolmafia.session.WumpusManager;

import net.sourceforge.kolmafia.webui.MemoriesDecorator;

class CADatabase300to399 extends ChoiceAdventureDatabase
{
	final void add300to399()
	{
		new RetiredChoiceAdventure( 300, "Merry Crimbo!", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new RetiredChoiceAdventure( 301, "And to All a Good Night", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 302, "You've Hit Bottom", "The Marinara Trench" ) // Sauceror
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 303, "You've Hit Bottom", "The Marinara Trench" ) // Pastamancer
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 304, "A Vent Horizon", "The Marinara Trench" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachMP( -200 )
					.attachItem( "bubbling tempura batter", 1, AUTO );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				int summons = 3 - Preferences.getInteger( "tempuraSummons" );

				getOption( 1 ).text( summons + " summons left today" );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// If we've already summoned three batters today or we
				// don't have enough MP, ignore this choice adventure.

				if ( decision.equals( "1" ) && ( Preferences.getInteger( "tempuraSummons" ) >= 3 || KoLCharacter.getCurrentMP() < 200 ) )
				{
					return "2";
				}
				return decision;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// "You conjure some delicious batter from the core of
				// the thermal vent. It pops and sizzles as you stick
				// it in your sack."

				if ( decision == 1 && !request.responseText.contains( "pops and sizzles" ) )
				{
					this.choiceFailed();
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "pops and sizzles" ) )
				{
					Preferences.increment( "tempuraSummons", 1 );
				}
			}
		};

		new ChoiceAdventure( 305, "There is Sauce at the Bottom of the Ocean", "The Marinara Trench" )
		{
			void setup()
			{
				new Option( 1, "Mer-kin pressureglobe -> Deep Sauce", true )
					.turnCost( 1 )
					.attachItem( ItemPool.MERKIN_PRESSUREGLOBE, -1, MANUAL, new DisplayAll( "pressureglobe", NEED, AT_LEAST, 1 ) )
					.attachItem( "globe of Deep Sauce", 1, AUTO );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
				new Option( 3 )
					.attachItem( "globe of Deep Sauce", 1, AUTO );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// If we don't have a Mer-kin pressureglobe, ignore
				// this choice adventure.

				if ( decision.equals( "1" ) && InventoryManager.getCount( ItemPool.MERKIN_PRESSUREGLOBE ) < 1 )
				{
					return "2";
				}
				return decision;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 1 && request.responseText.contains( "no safe way of transporting the Deep Sauc" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 306, "Not a Micro Fish", "Anemone Mine" ) // Seal Clubber or Turtle Tamer
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 307, "Ode to the Sea", "The Dive Bar" ) // Disco Bandit
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 308, "Boxing the Juke", "The Dive Bar" ) // Accordion Thief
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 309, "Barback", "The Dive Bar" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( "seaode", 1, AUTO );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				int seaodes = 3 - Preferences.getInteger( "seaodesFound" );

				// does it still cost a turn if you got all seodes?
				getOption( 1 ).text( seaodes + " more seodes available today" );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// If we've already found three seaodes today,
				// ignore this choice adventure.

				if ( decision.equals( "1" ) && Preferences.getInteger( "seaodesFound" ) >= 3 )
				{
					return "2";
				}
				return decision;
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// "You head down the tunnel into the cave, and manage
				// to find another seaode. Sweet! I mean... salty!"

				if ( request.responseText.contains( "salty!" ) )
				{
					Preferences.increment( "seaodesFound", 1 );
				}
			}
		};

		new ChoiceAdventure( 310, "The Economist of Scales", "Madness Reef" )
		{
			final AdventureResult DULL_FISH_SCALE = ItemPool.get( ItemPool.DULL_FISH_SCALE );
			final AdventureResult ROUGH_FISH_SCALE = ItemPool.get( ItemPool.ROUGH_FISH_SCALE );
			final AdventureResult PRISTINE_FISH_SCALE = ItemPool.get( ItemPool.PRISTINE_FISH_SCALE );

			void setup()
			{
				new Option( 1, "10 dull fish scales -> 1 rough fish scale", true )
					.attachItem( DULL_FISH_SCALE.getInstance( -10 ), MANUAL, new ImageOnly( "dull fish scales" ) )
					.attachItem( ROUGH_FISH_SCALE.getInstance( 1 ), AUTO, new ImageOnly() );
				new Option( 2, "10 rough fish scales -> 1 pristine fish scale", true )
					.attachItem( ROUGH_FISH_SCALE.getInstance( -10 ), MANUAL, new ImageOnly( "rough fish scales" ) )
					.attachItem( PRISTINE_FISH_SCALE.getInstance( 1 ), AUTO, new ImageOnly() );
				new Option( 4, "get multiple rough fish scales", true );
				new Option( 5, "get multiple pristine fish scales", true );
				new Option( 6, "skip adventure", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				int dullTrades = DULL_FISH_SCALE.getCount( KoLConstants.inventory ) / 10;
				int roughTrades = ROUGH_FISH_SCALE.getCount( KoLConstants.inventory ) / 10;

				Option option = getOption( 4 );
				option.attachItem( DULL_FISH_SCALE.getInstance( -10 * dullTrades ), MANUAL, new NoDisplay() );
				option.attachItem( ROUGH_FISH_SCALE.getInstance( dullTrades ), AUTO, new NoDisplay() );

				option = getOption( 5 );
				option.attachItem( ROUGH_FISH_SCALE.getInstance( -10 * roughTrades ), MANUAL, new NoDisplay() );
				option.attachItem( PRISTINE_FISH_SCALE.getInstance( roughTrades ), AUTO, new NoDisplay() );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "You don't have that many" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 311, "Heavily Invested in Pun Futures", "Madness Reef" )
		{
			void setup()
			{
				new Option( 1, "The Economist of Scales", true )
					.leadsTo( 310, true )
					.attachItem( ItemPool.DULL_FISH_SCALE )
					.attachItem( ItemPool.ROUGH_FISH_SCALE )
					.attachItem( ItemPool.PRISTINE_FISH_SCALE );
				new Option( 2, "skip adventure", true );
			}
		};

		new ChoiceAdventure( 312, "Into the Outpost", "The Mer-Kin Outpost" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 313, true, ( Option o ) -> o.index != 4 );
				new Option( 2 )
					.leadsTo( 314, true, ( Option o ) -> o.index != 4 );
				new Option( 3 )
					.leadsTo( 315, true, ( Option o ) -> o.index != 4 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 313, "Sneaky Intent", "The Mer-Kin Outpost" )
		{
			final AdventureResult MERKIN_SNEAKMASK = new AdventureResult( "Mer-kin sneakmask", 1, false );
			final AdventureResult MERKIN_FASTJUICE = new AdventureResult( "Mer-kin fastjuice", 1, false );
			final AdventureResult MERKIN_TAKEBAG = new AdventureResult( "Mer-kin takebag", 1, false );

			void setup()
			{
				new Option( 1, "an error happened" )
					.turnCost( 1 );
				new Option( 2, "an error happened" )
					.turnCost( 1 );
				new Option( 3, "an error happened" )
					.turnCost( 1 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				SeaMerkinOutpostManager.visitOutpostTent(
					this.choice,
					this.options,
					new AdventureResult[]
					{
						MERKIN_SNEAKMASK,
						MERKIN_FASTJUICE,
						MERKIN_TAKEBAG
					},
					new DisplayAll[]
					{
						new DisplayAll( MERKIN_SNEAKMASK.getName() ),
						new DisplayAll( MERKIN_FASTJUICE.getName() ),
						new DisplayAll( MERKIN_TAKEBAG.getName() )
					},
					new ImageOnly( "stashbox" )
				);
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision < 4 )
				{
					SeaMerkinOutpostManager.optionTried( this.choice, decision );
				}
			}
		};

		new ChoiceAdventure( 314, "Aggressive Intent", "The Mer-Kin Outpost" )
		{
			final AdventureResult MERKIN_ROUNDSHIELD = new AdventureResult( "Mer-kin roundshield", 1, false );
			final AdventureResult MERKIN_BREASTPLATE = new AdventureResult( "Mer-kin breastplate", 1, false );
			final AdventureResult MERKIN_HOOKSPEAR = new AdventureResult( "Mer-kin hookspear", 1, false );

			void setup()
			{
				new Option( 1, "an error happened" )
					.turnCost( 1 );
				new Option( 2, "an error happened" )
					.turnCost( 1 );
				new Option( 3, "an error happened" )
					.turnCost( 1 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				SeaMerkinOutpostManager.visitOutpostTent(
					this.choice,
					this.options,
					new AdventureResult[]
					{
						MERKIN_ROUNDSHIELD,
						MERKIN_BREASTPLATE,
						MERKIN_HOOKSPEAR
					},
					new DisplayAll[]
					{
						new DisplayAll( MERKIN_ROUNDSHIELD.getName() ),
						new DisplayAll( MERKIN_BREASTPLATE.getName() ),
						new DisplayAll( MERKIN_HOOKSPEAR.getName() )
					},
					new ImageOnly( "stashbox" )
				);
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision < 4 )
				{
					SeaMerkinOutpostManager.optionTried( this.choice, decision );
				}
			}
		};

		new ChoiceAdventure( 315, "Mysterious Intent", "The Mer-Kin Outpost" )
		{
			final AdventureResult MERKIN_KILLSCROLL = new AdventureResult( "Mer-kin killscroll", 1, false );
			final AdventureResult MERKIN_HEALSCROLL = new AdventureResult( "Mer-kin healscroll", 1, false );
			final AdventureResult MERKIN_PRAYERBEADS = new AdventureResult( "Mer-kin prayerbeads", 1, false );

			void setup()
			{
				new Option( 1, "an error happened" )
					.turnCost( 1 );
				new Option( 2, "an error happened" )
					.turnCost( 1 );
				new Option( 3, "an error happened" )
					.turnCost( 1 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				SeaMerkinOutpostManager.visitOutpostTent(
					this.choice,
					this.options,
					new AdventureResult[]
					{
						MERKIN_KILLSCROLL,
						MERKIN_HEALSCROLL,
						MERKIN_PRAYERBEADS
					},
					new DisplayAll[]
					{
						new DisplayAll( MERKIN_KILLSCROLL.getName() ),
						new DisplayAll( MERKIN_HEALSCROLL.getName() ),
						new DisplayAll( MERKIN_PRAYERBEADS.getName() )
					},
					new ImageOnly( "stashbox" )
				);
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision < 4 )
				{
					SeaMerkinOutpostManager.optionTried( this.choice, decision );
				}
			}
		};

		new UnknownChoiceAdventure( 316 );

		new ChoiceAdventure( 317, "No Man, No Hole", "The Sleazy Back Alley" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 318, "C'mere, Little Fella", "The Outskirts of Cobb's Knob" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 319, "Turtles of the Universe", "The Haunted Conservatory" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 320, "A Rolling Turtle Gathers No Moss", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 321, "Boxed In", "The \"Fun\" House" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 322, "Capital!", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 323 );

		new UnknownChoiceAdventure( 324 );

		new UnknownChoiceAdventure( 325 );

		new ChoiceAdventure( 326, "Showdown", "The Slime Tube" )
		{
			void setup()
			{
				new Option( 1, "enter combat with Mother Slime", true );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && KoLmafia.isAdventuring() )
				{
					KoLmafia.updateDisplay( MafiaState.ABORT,
						"Mother Slime waits for you." );
				}
			}
		};

		new ChoiceAdventure( 327, "Puttin' it on Wax", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 328, "Never Break the Chain", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 329, "Don't Be Alarmed, Now", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 330, "A Shark's Chum", "The Haunted Billiards Room" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1, "stats and pool skill", true )
					.turnCost( 1 );
				new Option( 2, "fight hustled spectre", true )
					.attachItem( "cube of billiard chalk", 1, AUTO );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					Preferences.increment( "poolSharkCount", 1 );
				}
			}
		};

		new ChoiceAdventure( 331, "Like That Time in Tortuga", "Barrrney's Barrr" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 332, "More eXtreme Than Usual", "The eXtreme Slope" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 333, "Cleansing your Palette", "The Haunted Gallery" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 334, "O Turtle Were Art Thou", "The Spooky Forest" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 335, "Blue Monday", "8-Bit Realm" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 336, "Jewel in the Rough", "South of the Border" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 337, "Engulfed!", "The Slime Tube" )
		{
			void setup()
			{
				new Option( 1, "+1 rusty -> slime-covered item conversion", true )
					.turnCost( 1 );
				new Option( 2, "raise area ML", true )
					.turnCost( 1 );
				new Option( 3, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// You reach for the uvula, but before you can tickle it, it withdraws into the Slime and out of reach. Ah well.
				// You reach for the gall bladder, but before you can squeeze it, it withdraws into the Slime and out of reach. Ah well.
				// You reach for the uvula, but before you can tickle it, it recoils from you and disappears into the mass of Slime. Looks like the Slime still remembers you from last time.
				// You reach for the gall bladder, but before you can get hold of it, it recoils from you and disappears into the mass of Slime. Looks like the Slime still remembers you from last time.
				if ( request.responseText.contains( "still remembers you" ) ||
				     request.responseText.contains( "into the Slime and out of reach" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 338, "Duel Nature", "Whitey's Grove" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 339, "Kick the Can", "The Haunted Pantry" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 340, "Turtle in peril", "The Haiku Dungeon" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 341, "Nantucket Snapper", "The Limerick Dungeon" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 342, "The Horror...", "Frat House" ) // Different from A-boo peak
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 343, "Turtles All The Way Around", "Hippy Camp" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 344, "Silent Strolling", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 345, "Training Day", "undefined" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 346, "Soup For You", "The Primordial Soup" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				QuestDatabase.setQuestIfBetter( Quest.PRIMORDIAL, QuestDatabase.STARTED );
			}
		};

		new ChoiceAdventure( 347, "Yes, Soup For You...", "The Primordial Soup" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				QuestDatabase.setQuestIfBetter( Quest.PRIMORDIAL, QuestDatabase.STARTED );
			}
		};

		new ChoiceAdventure( 348, "Souped Up", "The Primordial Soup" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				QuestDatabase.setQuestIfBetter( Quest.PRIMORDIAL, QuestDatabase.STARTED );
			}
		};

		new ChoiceAdventure( 349, "The Primordial Directive", "The Primordial Soup" )
		{
			void setup()
			{
				new Option( 1, "unlock the real zone (after using amino acids)", true )
					.turnCost( 1 );
				new Option( 2, "few substats", true )
					.turnCost( 1 );
				new Option( 3, "get amino acids", true )
					.turnCost( 1 )
					.attachItem( ItemPool.AMINO_ACIDS, 1, AUTO, new DisplayAll( NEED, EXACTLY, 0 ) );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// You swam upward, into a brighter and warmer part of the soup
				if ( decision == 1 && request.responseText.contains( "a brighter and warmer part of the soup" ) )
				{
					QuestDatabase.setQuestIfBetter( Quest.PRIMORDIAL, "step1" );
				}
			}
		};

		new ChoiceAdventure( 350, "Soupercharged", "The Primordial Soup" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1, "fight Cyrus", true );
				new Option( 2, "skip adventure and banish for 10 adventures", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( QuestDatabase.isQuestFinished( Quest.PRIMORDIAL ) )
				{
					getOption( 1 ).text( "stats" )
						.turnCost( 1 );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 && request.responseText.contains( "You've fixed me all up" ) )
				{
					Preferences.setInteger( "aminoAcidsUsed", 0 );
					QuestDatabase.setQuestProgress( Quest.PRIMORDIAL, QuestDatabase.FINISHED );
				}
			}
		};

		new ChoiceAdventure( 351, "Beginner's Luck", "The Primordial Soup" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				QuestDatabase.setQuestIfBetter( Quest.PRIMORDIAL, QuestDatabase.STARTED );
			}
		};

		new ChoiceAdventure( 352, "Savior Faire", "Seaside Megalopolis" ) // also called "Savorier Savior"
		{
			void setup()
			{
				new Option( 1, "moxie substats -> Bad Reception Down Here", true )
					.turnCost( 1 );
				new Option( 2, "muscle substats -> A Diseased Procurer", true )
					.turnCost( 1 );
				new Option( 3, "myst substats -> Give it a Shot", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 353, "Bad Reception Down Here", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.INDIGO_PARTY_INVITATION, 1, AUTO );
				new Option( 2, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.VIOLET_HUNT_INVITATION, 1, AUTO );
			}
		};

		new ChoiceAdventure( 354, "You Can Never Be Too Rich or Too in the Future", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "moxie substats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.INDIGO_PARTY_INVITATION, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of Serenity", true )
					.turnCost( 1 )
					.attachItem( ItemPool.INDIGO_PARTY_INVITATION, -1, MANUAL, new NoDisplay() )
					.attachEffect( "Serenity" );
			}
		};

		new ChoiceAdventure( 355, "I'm on the Hunt, I'm After You", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "stats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.VIOLET_HUNT_INVITATION, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of Phairly Pheromonal", true )
					.turnCost( 1 )
					.attachItem( ItemPool.VIOLET_HUNT_INVITATION, -1, MANUAL, new NoDisplay() )
					.attachEffect( "Phairly Pheromonal" );
			}
		};

		new ChoiceAdventure( 356, "A Diseased Procurer", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.BLUE_MILK_CLUB_CARD, 1, AUTO );
				new Option( 2, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.MECHA_MAYHEM_CLUB_CARD, 1, AUTO );
			}
		};

		new ChoiceAdventure( 357, "Painful, Circuitous Logic", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "muscle substats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.MECHA_MAYHEM_CLUB_CARD, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of Nano-juiced", true )
					.turnCost( 1 )
					.attachItem( ItemPool.MECHA_MAYHEM_CLUB_CARD, -1, MANUAL, new NoDisplay() )
					.attachEffect( "Nano-juiced" );
			}
		};

		new ChoiceAdventure( 358, "Brings All the Boys to the Blue Yard", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "stats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.BLUE_MILK_CLUB_CARD, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of Dance Interpreter", true )
					.turnCost( 1 )
					.attachItem( ItemPool.BLUE_MILK_CLUB_CARD, -1, MANUAL, new NoDisplay() )
					.attachEffect( "Dance Interpreter" );
			}
		};

		new UnknownChoiceAdventure( 359 );

		new ChoiceAdventure( 360, "Cavern Entrance", "The Jungles of Ancient Loathing" ) // Variable name; Wumpus caves
		{
			void setup()
			{
				this.customName = "Jungles: Wumpus Cave";

				new Option( 1 );
				new Option( 2, "skip adventure", true );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
			}

			@Override
			void postChoice0( GenericRequest request, int decision )
			{
				WumpusManager.preWumpus( decision );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				WumpusManager.visitChoice( request.responseText );
				WumpusManager.dynamicChoiceOptions( request.responseText, this.options );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				WumpusManager.decorate( buffer );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 &&
				     request.responseText.contains( "Krakrox wasn't up for that challenge" ) )
				{
					getOption( 2 ).entersQueue( false );
				}

				WumpusManager.takeChoice( decision, request.responseText );
			}
		};

		new ChoiceAdventure( 361, "Give it a Shot", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.SMUGGLER_SHOT_FIRST_BUTTON, 1, AUTO );
				new Option( 2, null, true )
					.turnCost( 1 )
					.attachItem( ItemPool.SPACEFLEET_COMMUNICATOR_BADGE, 1, AUTO );
			}
		};

		new ChoiceAdventure( 362, "A Bridge Too Far", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "stats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.SPACEFLEET_COMMUNICATOR_BADGE, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of Meatwise", true )
					.turnCost( 1 )
					.attachItem( ItemPool.SPACEFLEET_COMMUNICATOR_BADGE, -1, MANUAL, new NoDisplay() )
					.attachEffect( "Meatwise" );
			}
		};

		new ChoiceAdventure( 363, "Does This Bug You? Does This Bug You?", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "moxie substats", true )
					.turnCost( 1 )
					.attachItem( ItemPool.SMUGGLER_SHOT_FIRST_BUTTON, -1, MANUAL, new NoDisplay() );
				new Option( 2, "20 adv of In the Saucestream", true )
					.turnCost( 1 )
					.attachItem( ItemPool.SMUGGLER_SHOT_FIRST_BUTTON, -1, MANUAL, new NoDisplay() )
					.attachEffect( "In the Saucestream" );
			}
		};

		new ChoiceAdventure( 364, "451 Degrees! Burning Down the House!", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "moxie substats", true )
					.turnCost( 1 );
				new Option( 2, "Supreme Being Glossary", true )
					.turnCost( 1 )
					.attachItem( ItemPool.SUPREME_BEING_GLOSSARY, 1, AUTO );
				new Option( 3, "muscle substats", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 365, "None Shall Pass", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1, "muscle substats", true )
					.turnCost( 1 )
					.attachMeat( -30, AUTO );
				new Option( 2, null, true )
					.turnCost( 1 )
					.attachMeat( -60, AUTO )
					.attachItem( "multi-pass", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 366, "Entrance to the Forgotten City", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				this.customName = "Jungles: Forgotten City";

				new Option( 1 )
					.leadsTo( 368 );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 367, "Ancient Temple", "The Jungles of Ancient Loathing" ) // unlocked
		{
			void setup()
			{
				this.customName = "Jungles: Ancient Temple";

				new Option( 1, "Enter the Temple", true )
					.attachItem( "memory of a glowing crystal", new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( "memory of a cultist's robe", new DisplayAll( WANT, EQUIPPED_AT_LEAST, 1 ) );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 368, "City Center", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 369 );
				new Option( 2 )
					.leadsTo( 370 );
				new Option( 3 )
					.leadsTo( 371 );
				new Option( 4 )
					.leadsTo( 372, true );
				new Option( 5, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 369, "North Side of the City", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 373, true );
				new Option( 2 )
					.leadsTo( 374, true );
				new Option( 3 )
					.leadsTo( 368 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 370, "East Side of the City", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 375, true );
				new Option( 2 )
					.leadsTo( 368 );
				new Option( 3 )
					.leadsTo( 377 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 371, "West Side of the City", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 374, true );
				new Option( 2 )
					.leadsTo( 368 );
				new Option( 3 )
					.leadsTo( 378, true );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 372, "An Ancient Well", "The Jungles of Ancient Loathing" )
		{
			final AdventureResult GRAPPLING_HOOK = ItemPool.get( ItemPool.GRAPPLING_HOOK, 1 );

			void setup()
			{
				new Option( 1 )
					.leadsTo( 368 );
				new Option( 2 )
					.turnCost( 1 )
					.attachItem( GRAPPLING_HOOK, AUTO );
				new Option( 3 );
				new Option( 4, "nothing" )
					.entersQueue( false );
				new Option( 5, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 2 );
				if ( request == null ) // looking ahead
				{
					option.text( "retrieve grappling hook (after killing giant octopus)" );
				}

				option = getOption( 3 );
				if ( GRAPPLING_HOOK.getCount( KoLConstants.inventory ) > 0 )
				{
					option.text( "fight giant octopus (if not already killed)" );
				}
				else
				{
					option.text( "nothing" );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 3 && request.responseText.contains( "Krakrox peered down into the well" ) )
				{
					getOption( decision ).entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 373, "Northern Gate", "The Jungles of Ancient Loathing" )
		{
			final AdventureResult SMALL_STONE_BLOCK = ItemPool.get( ItemPool.SMALL_STONE_BLOCK, -1 );
			final AdventureResult LITTLE_STONE_BLOCK = ItemPool.get( ItemPool.LITTLE_STONE_BLOCK, -1 );

			void setup()
			{
				new Option( 1 )
					.leadsTo( 369 );
				new Option( 2 )
					.entersQueue( false )
					.attachItem( SMALL_STONE_BLOCK, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 3 )
					.entersQueue( false )
					.attachItem( LITTLE_STONE_BLOCK, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 4 )
					.entersQueue( false );
				new Option( 5 );
				new Option( 6 )
					.leadsTo( 376 );
				new Option( 7, "nothing" )
					.entersQueue( false );
				new Option( 8, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 4 );

				if ( request == null )
				{
					option.text( "once both blocks are in place, allows destroying the weights in the catacombs" );
				}
				else if ( request.responseText.contains( "The left one has been filled" ) &&
				          request.responseText.contains( "the one on the right has been filled" ) &&
				          request.responseText.contains( "(in the \"up\" position)" ) )
				{
					// technically, only the left block is needed to pull the lever
					// but then we lose a way to tell the player that they still
					// need the other stone block

					option.text( "allows destroying the weights in the catacombs" );
				}
				else
				{
					option.text( "nothing (yet)" );
				}


				option = getOption( 5 );

				if ( request == null )
				{
					option.text( "once you destroyed the weights in the catacombs, allows going north" );
				}
				else if ( request.responseText.contains( "when the chains holding the counterweights broke" ) )
				{
					option.text( "allows going north" )
						.turnCost( 1 );
				}
				else
				{
					option.text( "nothing (yet)" )
						.entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 374, "An Ancient Tower", "The Jungles of Ancient Loathing" )
		{
			final AdventureResult GRAPPLING_HOOK = ItemPool.get( ItemPool.GRAPPLING_HOOK );
			final AdventureResult HALF_STONE_CIRCLE = ItemPool.get( ItemPool.HALF_STONE_CIRCLE, 1 );

			void setup()
			{
				new Option( 1 )
					.leadsTo( 369 );
				new Option( 2 )
					.leadsTo( 371 );
				new Option( 3 )
					.attachItem( GRAPPLING_HOOK, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( HALF_STONE_CIRCLE, AUTO, new DisplayAll( NEED, EXACTLY, 0 ) );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 3 );
				if ( GRAPPLING_HOOK.getCount( KoLConstants.inventory ) == 0 )
				{
					option.text( "waste an adventure" )
						.turnCost( 1 );
				}
				else
				{
					option.text( "fight giant bird-creature, then get half stone circle, then nothing" );
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 3 )
				{
					if ( request.responseText.contains( "Krakrox considered briefly climbing" ) )
					{
						// after getting the half stone circle -- nothing happens
						getOption( decision ).entersQueue( false );
					}
					else if ( request.responseText.contains( "Once again you climb the tower" ) )
					{
						// getting the half stone circle
						getOption( decision ).turnCost( 1 );
					}
				}
			}
		};

		new ChoiceAdventure( 375, "Northern Abandoned Building", "The Jungles of Ancient Loathing" )
		{
			final AdventureResult IRON_KEY = ItemPool.get( ItemPool.IRON_KEY, 1 );
			final AdventureResult SMALL_STONE_BLOCK = ItemPool.get( ItemPool.SMALL_STONE_BLOCK );

			void setup()
			{
				new Option( 1 )
					.leadsTo( 370 );
				new Option( 2 );
				new Option( 3 )
					.leadsTo( 379, true );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 2 );
				if ( IRON_KEY.getCount( KoLConstants.inventory ) == 0 &&
				     SMALL_STONE_BLOCK.getCount( KoLConstants.inventory ) == 0 )
				{
					option.text( "get memory of an iron key" )
						.turnCost( 1 )
						.attachItem( IRON_KEY, AUTO );
				}
				else
				{
					option.text( "nothing" )
						.entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 376, "Ancient Temple", "The Jungles of Ancient Loathing" ) // locked
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 373, true );
				new Option( 2, "nothing" )
					.entersQueue( false );
				new Option( 3, "unlock the Ancient Temple" )
					.turnCost( 1 )
					.attachItem( ItemPool.HALF_STONE_CIRCLE, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( ItemPool.STONE_HALF_CIRCLE, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 377, "Southern Abandoned Building", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 370 );
				new Option( 2 )
					.leadsTo( 380, true );
				new Option( 3 )
					.leadsTo( 381 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 378, "Storehouse", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 371 );
				new Option( 2 )
					.turnCost( 1 )
					.attachItem( ItemPool.GRAPPLING_HOOK, 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 2 );
				if ( request == null )
				{
					option.text( "get grappling hook (first time only)" )
						.attachItem( ItemPool.GRAPPLING_HOOK, 1, AUTO );
				}
				else if ( request.responseText.contains( "wouldn't set your hopes on finding anything else" ) )
				{
					option.text( "waste an adventure" );
				}
				else
				{
					option.text( "get grappling hook" )
						.attachItem( ItemPool.GRAPPLING_HOOK, 1, AUTO );
				}
			}
		};

		new ChoiceAdventure( 379, "Northern Building (Basement)", "The Jungles of Ancient Loathing" )
		{
			final AdventureResult IRON_KEY = ItemPool.get( ItemPool.IRON_KEY, -1 );
			final AdventureResult SMALL_STONE_BLOCK = ItemPool.get( ItemPool.SMALL_STONE_BLOCK, 1 );

			void setup()
			{
				new Option( 1 )
					.leadsTo( 375 );
				new Option( 2 );
				new Option( 3 )
					.turnCost( 1 )
					.attachItem( SMALL_STONE_BLOCK, AUTO )
					.attachItem( IRON_KEY, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 2 );

				if ( request == null )
				{
					option.text( "fight giant spider, then get small stone block with iron key, then nothing" )
						.attachItem( SMALL_STONE_BLOCK, AUTO, new DisplayAll( WANT, EXACTLY, 0 ) )
						.attachItem( IRON_KEY, AUTO, new DisplayAll( WANT, AT_LEAST, 1 ) );
				}
				else if ( !request.responseText.contains( "you spot the gigantic corpse of the spider" ) )
				{
					option.text( "fight giant spider" );
				}
				else if ( request.responseText.contains( "the stone chest you discovered" ) )
				{
					option.text( "nothing" )
						.entersQueue( false );
				}
				else
				{
					option.text( "get small stone block" )
						.turnCost( 1 )
						.attachItem( SMALL_STONE_BLOCK, AUTO )
						.attachItem( IRON_KEY, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// Krakrox remembers finding all there was to find in the spiderwebs.

				if ( decision == 2 && request.responseText.contains( "all there was to find" ) )
				{
					getOption( decision ).entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 380, "Southern Building (Upstairs)", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 377 );
				new Option( 2, "fight giant jungle python, then nothing" );
				new Option( 3 )
					.turnCost( 1 )
					.attachItem( ItemPool.LITTLE_STONE_BLOCK, 1, AUTO );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				if ( request == null )
				{
					getOption( 3 ).text( "(only if giant jungle python was defeated)" );
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// Krakrox searched the vines and branches, but found nothing of note.

				if ( decision == 2 && request.responseText.contains( "but found nothing of note" ) )
				{
					getOption( decision ).entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 381, "Southern Building (Basement)", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 377 );
				new Option( 2 )
					.leadsTo( 382 );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 382, "Catacombs Entrance", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 381 );
				new Option( 2 )
					.leadsTo( 383 );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 383, "Catacombs Junction", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 385, true );
				new Option( 2 )
					.leadsTo( 382 );
				new Option( 3 )
					.leadsTo( 384 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 384, "Catacombs Dead-End", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 383 );
				new Option( 2, "smash the chest open" )
					.entersQueue( false );
				new Option( 3, "I said smash... the.. CHEST" )
					.turnCost( 1 )
					.attachItem( ItemPool.STONE_HALF_CIRCLE, 1, AUTO );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 385, "Shore of an Underground Lake", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 383 );
				new Option( 2, "nothing" )
					.entersQueue( false );
				new Option( 3 )
					.leadsTo( 386 );
				new Option( 4, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				if ( request == null )
				{
					// looking ahead from choice 383
					getOption( 3 ).text( "(only after killing the giant octopus in the ancient well)" );
				}
			}
		};

		new ChoiceAdventure( 386, "Catacombs Machinery", "The Jungles of Ancient Loathing" )
		{
			void setup()
			{
				new Option( 1 )
					// no need to look ahead, since we know the octopus is defeated
					.leadsTo( 385 );
				new Option( 2, "if lever pulled at Northern gate, unlock temple door. Otherwise nothing" )
					.attachItem( ItemPool.GRAPPLING_HOOK, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 2 )
				{
					Option option = getOption( decision );

					if ( request.responseText.contains( "Your head spinning and ears ringing" ) )
					{
						option.turnCost( 1 );
					}
					else
					{
						option.entersQueue( false );
					}
				}
			}
		};

		new ChoiceAdventure( 387, "Time Isn't Holding Up; Time is a Doughnut", "Seaside Megalopolis" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 388, "Extra Savoir Faire", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 389, "The Unbearable Supremeness of Being", "Seaside Megalopolis" )
		{
			final AdventureResult SUPREME_BEING_GLOSSARY = ItemPool.get( ItemPool.SUPREME_BEING_GLOSSARY, -1 );

			void setup()
			{
				new Option( 1 )
					.turnCost( 1 )
					.attachItem( SUPREME_BEING_GLOSSARY, new DisplayAll( WANT, AT_LEAST, 1 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// "Of course I understand," Jill says, in fluent
				// English. "I learned your language in the past five
				// minutes. I know where the element is, but we'll have
				// to go offworld to get it. Meet me at the Desert
				// Beach Spaceport." And with that, she gives you a
				// kiss and scampers off. Homina-homina.

				if ( request.responseText.contains( "Homina-homina" ) )
				{
					// was attached as decorative, now attach as cost
					getOption( decision ).attachItem( SUPREME_BEING_GLOSSARY, MANUAL );
				}
			}
		};

		new ChoiceAdventure( 390, "A Winning Pass", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 391, "OMG KAWAIII", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 392, "The Elements of Surprise . . .", "Seaside Megalopolis" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 )
					.attachItem( ItemPool.ESSENCE_OF_HEAT, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.ESSENCE_OF_KINK, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.ESSENCE_OF_COLD, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.ESSENCE_OF_STENCH, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.ESSENCE_OF_FRIGHT, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.ESSENCE_OF_CUTE, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.SECRET_FROM_THE_FUTURE, 1, MANUAL, new NoDisplay() );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				MemoriesDecorator.decorateElements( this.choice, buffer );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// And as the two of you walk toward the bed, you sense
				// your ancestral memories pulling you elsewhere, ever
				// elsewhere, because your ancestral memories are
				// total, absolute jerks.

				if ( request.responseText.contains( "total, absolute jerks" ) )
				{
					EquipmentManager.discardEquipment( ItemPool.RUBY_ROD );
				}
				else
				{
					this.choiceFailed().entersQueue( false );
				}
			}

			@Override
			void decorateChoiceResponse( StringBuffer buffer, int option )
			{
				MemoriesDecorator.decorateElementsResponse( buffer );
			}
		};

		new ChoiceAdventure( 393, "The Collector", "Item-Driven" )
		{
			void setup()
			{
				this.customName = "big bumboozer marble";

				new Option( 1, "1 of each marble -> 32768 Meat", true )
					.attachMeat( 32768, AUTO );
				new Option( 2, "skip adventure", true );

				Option option = getOption( 1 );
				for ( int i = ItemPool.GREEN_PEAWEE_MARBLE; i <= ItemPool.BIG_BUMBOOZER_MARBLE; ++i )
				{
					option.attachItem( i, -1, MANUAL, new ImageOnly() );
				}
			}
		};

		new ChoiceAdventure( 394, "Hellevator Music", "Heartbreaker's Hotel" )
		{
			final Pattern HELLEVATOR_PATTERN = Pattern.compile( "the (lobby|first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh) (button|floor)" );

			final String[] FLOORS = new String[]
			{
				"lobby",
				"first",
				"second",
				"third",
				"fourth",
				"fifth",
				"sixth",
				"seventh",
				"eighth",
				"ninth",
				"tenth",
				"eleventh",
			};

			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2 )
					.turnCost( 1 );
				new Option( 3 )
					.turnCost( 1 );
				new Option( 4 )
					.turnCost( 1 );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// Parse response
				Matcher matcher = HELLEVATOR_PATTERN.matcher( request.responseText );
				if ( !matcher.find() )
				{
					return;
				}
				String floor = matcher.group( 1 );
				for ( int mcd = 0; mcd < FLOORS.length; ++mcd )
				{
					if ( floor.equals( FLOORS[ mcd ] ) )
					{
						String message = "Setting monster level to " + mcd;
						RequestLogger.printLine( message );
						RequestLogger.updateSessionLog( message );
						break;
					}
				}
			}
		};

		new RetiredChoiceAdventure( 395, "Rumble On", null )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new ChoiceAdventure( 396, "Woolly Scaly Bully", "Mer-kin Elementary School" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1, "lose HP", true )
					.turnCost( 1 );
				new Option( 2, "lose HP", true )
					.turnCost( 1 );
				new Option( 3, "unlock janitor's closet", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 397, "Bored of Education", "Mer-kin Elementary School" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1, "lose HP", true )
					.turnCost( 1 );
				new Option( 2, "unlock the bathrooms", true )
					.turnCost( 1 );
				new Option( 3, "lose HP", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 398, "A Mer-kin Graffiti", "Mer-kin Elementary School" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1, "unlock teacher's lounge", true )
					.turnCost( 1 );
				new Option( 2, "lose HP", true )
					.turnCost( 1 );
				new Option( 3, "lose HP", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 399, "The Case of the Closet", "Mer-kin Elementary School" )
		{
			void setup()
			{
				new Option( 1, "fight Mer-kin monitor", true );
				new Option( 2, "Mer-kin sawdust", true )
					.turnCost( 1 )
					.attachItem( "Mer-kin sawdust", 1, AUTO );
			}
		};
	}
}
