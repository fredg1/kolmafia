package net.sourceforge.kolmafia.persistence.choiceadventures;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.RequestLogger;

import net.sourceforge.kolmafia.objectpool.EffectPool;
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

import net.sourceforge.kolmafia.request.AdventureRequest;
import net.sourceforge.kolmafia.request.ArcadeRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.TavernRequest;

import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.HaciendaManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.RabbitHoleManager;

class CADatabase400to499 extends ChoiceAdventureDatabase
{
	final void add400to499()
	{
		new ChoiceAdventure( 400, "No Rest for the Room", "Mer-kin Elementary School" )
		{
			void setup()
			{
				new Option( 1, "fight Mer-kin teacher", true );
				new Option( 2, "Mer-kin cancerstick", true )
					.turnCost( 1 )
					.attachItem( "Mer-kin cancerstick", 1, AUTO );
			}
		};

		new ChoiceAdventure( 401, "Raising Cane", "Mer-kin Elementary School" )
		{
			final AdventureResult MERKIN_BUNWIG = new AdventureResult( "Mer-kin bunwig", 0, false );
			final AdventureResult MERKIN_FACECOWL = new AdventureResult( "Mer-kin facecowl", 1, false );
			final AdventureResult MERKIN_WAISTROPE = new AdventureResult( "Mer-kin waistrope", 1, false );
			final AdventureResult MERKIN_WORDQUIZ = ItemPool.get( ItemPool.MERKIN_WORDQUIZ, 1 );

			void setup()
			{
				new Option( 1, "fight a Mer-kin punisher", true );
				new Option( 2, "Mer-kin wordquiz (+2 w/ Mer-kin bunwig)", true )
					.turnCost( 1 )
					// does it really not work if it's your current hat??
					.attachItem( MERKIN_BUNWIG, new DisplayAll( "bunwig", WANT, INV_ONLY_AT_LEAST, 1 ) );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Option option = getOption( 2 );
				if ( MERKIN_BUNWIG.getCount( KoLConstants.inventory ) > 0 )
				{
					option.attachItem( MERKIN_WORDQUIZ.getInstance( 3 ), AUTO, new DisplayAll( "wordquiz" ) );
				}
				else
				{
					option.attachItem( MERKIN_WORDQUIZ, AUTO, new DisplayAll( "wordquiz" ) );
				}

				if ( MERKIN_FACECOWL.getCount( KoLConstants.inventory ) == 0 )
				{
					option.attachItem( MERKIN_FACECOWL, AUTO, new ImageOnly() );
				}
				else if ( MERKIN_WAISTROPE.getCount( KoLConstants.inventory ) == 0 )
				{
					option.attachItem( MERKIN_WAISTROPE, AUTO, new ImageOnly() );
				}
			}
		};

		new RetiredChoiceAdventure( 402, "Don't Hold a Grudge", "The Haunted Bathroom" )
		{
			void setup()
			{
				new Option( 1, "muscle substats", true )
					.turnCost( 1 );
				new Option( 2, "mysticality substats", true )
					.turnCost( 1 );
				new Option( 3, "moxie substats", true )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 403, "Picking Sides", "The Skate Park" )
		{
			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( "skate blade", 1, AUTO );
				new Option( 2, null, true )
					.turnCost( 1 )
					.attachItem( "brand new key", 1, AUTO );
			}
		};

		new UnknownChoiceAdventure( 404 );

		new UnknownChoiceAdventure( 405 );

		new UnknownChoiceAdventure( 406 );

		new UnknownChoiceAdventure( 407 );

		new UnknownChoiceAdventure( 408 );

		new ChoiceAdventure( 409, "The Island Barracks", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 410 );
			}
		};

		new ChoiceAdventure( 410, "A Short Hallway", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 411 );
				new Option( 2 )
					.leadsTo( 412 );
				new Option( 3, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				getOption( 1 ).text( HaciendaManager.getWingSpoilers( 0 ) );
				getOption( 2 ).text( HaciendaManager.getWingSpoilers( 1 ) );
			}
		};

		new ChoiceAdventure( 411, "Hallway Left", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 413, true, ( Option o ) -> o.index != 4 );
				new Option( 2 )
					.leadsTo( 414, true, ( Option o ) -> o.index != 4 );
				new Option( 3 )
					.leadsTo( 415, true, ( Option o ) -> o.index != 4 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 412, "Hallway Right", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 416, true, ( Option o ) -> o.index != 4 );
				new Option( 2 )
					.leadsTo( 417, true, ( Option o ) -> o.index != 4 );
				new Option( 3 )
					.leadsTo( 418, true, ( Option o ) -> o.index != 4 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 413, "Kitchen", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 0 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 1 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 2 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new ChoiceAdventure( 414, "Dining room", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 3 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 4 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 5 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new ChoiceAdventure( 415, "Storeroom", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 6 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 7 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 8 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new ChoiceAdventure( 416, "Bedroom", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 9 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 10 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 11 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new ChoiceAdventure( 417, "Library", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 12 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 13 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 14 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new ChoiceAdventure( 418, "Parlour", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4, "leave barrack (1)" )
					.turnCost( 1 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				getOption( 1 ).text( HaciendaManager.getSpoiler( 15 ) );
				getOption( 2 ).text( HaciendaManager.getSpoiler( 16 ) );
				getOption( 3 ).text( HaciendaManager.getSpoiler( 17 ) );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				HaciendaManager.parseRoom( this.choice, decision, request.responseText );

				if ( decision < 4 && !request.responseText.contains( "Fight!" ) &&
				     !( decision == 3 && QuestDatabase.isQuestFinished( Quest.NEMESIS ) ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}
		};

		new UnknownChoiceAdventure( 419 );

		new UnknownChoiceAdventure( 420 );

		new UnknownChoiceAdventure( 421 );

		new UnknownChoiceAdventure( 422 );

		new RetiredChoiceAdventure( 423, "A Wrenching Encounter", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 424, "Get Your Bolt On, Michael", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 425, "Taking a Proper Gander", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 426, "It's Electric, Boogie-oogie-oogie", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 427, "A Voice Crying in the Crimbo Factory", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 428, "Disguise the Limit", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 429, "Diagnosis: Hypnosis", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 430, "Secret Agent Penguin", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 431, "Zapatos Con Crete", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 432, "Don We Now Our Bright Apparel", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new RetiredChoiceAdventure( 433, "Everything is Illuminated", "Crimbo Town Toy Factory (2009)" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 434 );

		new RetiredChoiceAdventure( 435, "Season's Beatings", "The Don's Crimbo Compound" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 436 );

		new ChoiceAdventure( 437, "Flying In Circles", "The Nemesis' Lair" )
		{
			void setup()
			{
				new Option( 1 );
			}

			@Override
			void registerDeferredChoice()
			{
				RequestLogger.registerLastLocation();
			}
		};

		new ChoiceAdventure( 438, "From Little Acorns...", "The Arrrboretum" )
		{
			void setup()
			{
				new Option( 1 )
					.attachItem( ItemPool.UNDERWORLD_ACORN, -1, MANUAL, new ImageOnly() );
			}
		};

		new UnknownChoiceAdventure( 439 );

		new ChoiceAdventure( 440, "Puttin' on the Wax", "The Island Barracks" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				HaciendaManager.preRecording( request.responseText );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					HaciendaManager.parseRecording( request.getURLString(), request.responseText );
				}
			}
		};

		new ChoiceAdventure( 441, "The Mad Tea Party", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// I'm sorry, but there's a very strict dress code for
				// this party

				if ( decision == 1 &&
				     !request.responseText.contains( "very strict dress code" ) )
				{
					Preferences.setBoolean( "_madTeaParty", true );
				}
			}
		};

		new ChoiceAdventure( 442, "A Moment of Reflection", "Item-Driven" )
		{
			void setup()
			{
				this.customName = this.name;
				// TODO should be/stay Item-Driven, but that's a lot of NC...
				// regroup them all into this one first?
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1 )
					.attachItem( ItemPool.REFLECTION_OF_MAP, -1, MANUAL, new NoDisplay() );
				new Option( 2 )
					.attachItem( ItemPool.REFLECTION_OF_MAP, -1, MANUAL, new NoDisplay() );
				new Option( 3 )
					.attachItem( ItemPool.REFLECTION_OF_MAP, -1, MANUAL, new NoDisplay() );
				new Option( 4, "Ittah bittah hookah", true )
					.attachItem( ItemPool.REFLECTION_OF_MAP, -1, MANUAL, new NoDisplay() )
					.attachItem( "ittah bittah hookah" )
					.leadsTo( 450, false, ( Option o ) -> o.index == 1 );
				new Option( 5, "get a chess cookie" )
					.attachItem( ItemPool.REFLECTION_OF_MAP, -1, MANUAL, new NoDisplay() )
					.leadsTo( 443 )
					.attachItem( "knight cookie", new ImageOnly() )
					.attachItem( "bishop cookie", new ImageOnly() )
					.attachItem( "rook cookie", new ImageOnly() )
					.attachItem( "king cookie", new ImageOnly() )
					.attachItem( "queen cookie", new ImageOnly() );
				new Option( 6, "skip adventure", true );

				new CustomOption( 1, "Seal Clubber/Pastamancer item, or yellow matter custard" );
				new CustomOption( 2, "Sauceror/Accordion Thief item, or delicious comfit?" );
				new CustomOption( 3, "Disco Bandit/Turtle Tamer item, or fight croqueteer" );
				new CustomOption( 5, "Chessboard" );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				String myClass = KoLCharacter.getClassType();

				Option option = getOption( 1 );
				if ( myClass == KoLCharacter.SEAL_CLUBBER )
				{
					option.leadsTo( 444, false, ( Option o ) -> true, " or " );
				}
				else if ( myClass == KoLCharacter.PASTAMANCER )
				{
					option.leadsTo( 445, false, ( Option o ) -> true, " or " );
				}
				else
				{
					option.turnCost( 1 )
						.text( "yellow matter custard" )
						.attachItem( ItemPool.YELLOW_CUSTARD, 1, AUTO );
				}

				option = getOption( 2 );
				if ( myClass == KoLCharacter.ACCORDION_THIEF )
				{
					option.leadsTo( 446, false, ( Option o ) -> true, " or " );
				}
				else if ( myClass == KoLCharacter.SAUCEROR )
				{
					option.leadsTo( 447, false, ( Option o ) -> true, " or " );
				}
				else
				{
					option.turnCost( 1 )
						.text( "delicious comfit?" )
						.attachItem( ItemPool.DELICIOUS_COMFIT, 1, AUTO );
				}

				option = getOption( 3 );
				if ( myClass == KoLCharacter.TURTLE_TAMER )
				{
					option.leadsTo( 448, false, ( Option o ) -> true, " or " );
				}
				else if ( myClass == KoLCharacter.DISCO_BANDIT )
				{
					option.leadsTo( 449, false, ( Option o ) -> true, " or " );
				}
				else
				{
					option.text( "fight croqueteer" );
				}

				int count = 0;
				for ( int item : new int[] { ItemPool.BEAUTIFUL_SOUP, ItemPool.LOBSTER_QUA_GRILL, ItemPool.MISSING_WINE, ItemPool.WALRUS_ICE_CREAM, ItemPool.HUMPTY_DUMPLINGS } )
				{
					if ( InventoryManager.getCount( item ) > 0 )
					{
						++count;
					}
				}

				getOption( 4 ).text( "you have " + count + "/5 of the items needed for an ittah bittah hookah" );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 5 )
				{
					// Option 5 is Chess Puzzle
					RabbitHoleManager.parseChessPuzzle( request.responseText );
				}
			}
		};

		new ChoiceAdventure( 443, "The Great Big Chessboard", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return RabbitHoleManager.registerChessboardRequest( urlString );
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				// No "encounter" when moving on the chessboard
				if ( urlString.contains( "xy" ) )
				{
					return null;
				}
				return AdventureRequest.parseEncounter( responseText );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				RabbitHoleManager.decorateChessPuzzle( buffer );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					// Option 1 is "Play"
					RabbitHoleManager.parseChessMove( request.getURLString(), request.responseText );
				}
			}

			@Override
			void decorateChoiceResponse( StringBuffer buffer, int option )
			{
				RabbitHoleManager.decorateChessPuzzleResponse( buffer );
			}
		};

		new ChoiceAdventure( 444, "The Field of Strawberries", "Item-Driven" ) // Seal Clubber
		{
			void setup()
			{
				this.customName = "Reflection of Map (Seal Clubber)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "walrus ice cream", true )
					.turnCost( 1 )
					.attachItem( ItemPool.WALRUS_ICE_CREAM, 1, AUTO );
				new Option( 2, "yellow matter custard", true )
					.turnCost( 1 )
					.attachItem( "yellow matter custard", 1, AUTO );
			}
		};

		new ChoiceAdventure( 445, "The Field of Strawberries", "Item-Driven" ) // Pastamancer
		{
			void setup()
			{
				this.customName = "Reflection of Map (Pastamancer)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "eggman noodles", true )
					.turnCost( 1 )
					.attachItem( "eggman noodles", 1, AUTO );
				new Option( 2, "yellow matter custard", true )
					.turnCost( 1 )
					.attachItem( "yellow matter custard", 1, AUTO );
			}
		};

		new ChoiceAdventure( 446, "The Caucus Racetrack", "Item-Driven" ) // Accordion Thief
		{
			void setup()
			{
				this.customName = "Reflection of Map (Accordion Thief)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "missing wine", true )
					.turnCost( 1 )
					.attachItem( ItemPool.MISSING_WINE, 1, AUTO );
				new Option( 2, "delicious comfit?", true )
					.turnCost( 1 )
					.attachItem( "delicious comfit?", 1, AUTO );
			}
		};

		new ChoiceAdventure( 447, "The Caucus Racetrack", "Item-Driven" ) // Sauceror
		{
			void setup()
			{
				this.customName = "Reflection of Map (Sauceror)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "Vial of <i>jus de larmes</i>", true )
					.turnCost( 1 )
					.attachItem( "Vial of jus de larmes", 1, AUTO );
				new Option( 2, "delicious comfit?", true )
					.turnCost( 1 )
					.attachItem( "delicious comfit?", 1, AUTO );
			}
		};

		new ChoiceAdventure( 448, "The Croquet Grounds", "Item-Driven" ) // Turtle Tamer
		{
			void setup()
			{
				this.customName = "Reflection of Map (Turtle Tamer)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "beautiful soup", true )
					.turnCost( 1 )
					.attachItem( ItemPool.BEAUTIFUL_SOUP, 1, AUTO );
				new Option( 2, "fight croqueteer", true );
			}
		};

		new ChoiceAdventure( 449, "The Croquet Grounds", "Item-Driven" ) // Disco Bandit
		{
			void setup()
			{
				this.customName = "Reflection of Map (Disco Bandit)";
				this.customZones.clear();
				this.customZones.add( "RabbitHole" );

				new Option( 1, "Lobster <i>qua</i> Grill", true )
					.turnCost( 1 )
					.attachItem( ItemPool.LOBSTER_QUA_GRILL, 1, AUTO );
				new Option( 2, "fight croqueteer", true );
			}
		};

		new ChoiceAdventure( 450, "The Duchess' Cottage", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 )
					.attachItem( ItemPool.BEAUTIFUL_SOUP, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( ItemPool.LOBSTER_QUA_GRILL, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( ItemPool.MISSING_WINE, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( ItemPool.WALRUS_ICE_CREAM, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) )
					.attachItem( ItemPool.HUMPTY_DUMPLINGS, -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 2 );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 1 && !request.responseText.contains( "Delectable and pulchritudinous!" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 451, "Typographical Clutter", "The Enormous Greater-Than Sign" )
		{
			final AdventureResult PLUS_SIGN = ItemPool.get( ItemPool.PLUS_SIGN, 1 );

			void setup()
			{
				new Option( 1, null, true )
					.turnCost( 1 )
					.attachItem( "left parenthesis", 1, AUTO );
				new Option( 2, "moxie, alternately lose then gain meat", true )
					.turnCost( 1 );
				new Option( 3 )
					.turnCost( 1 );
				new Option( 4, "mysticality substats + MP", true )
					.turnCost( 1 );
				new Option( 5, "5 adv of teleportitis", true )
					.turnCost( 1 )
					.attachEffect( EffectPool.TELEPORTITIS );

				new CustomOption( 3, "plus sign, then muscle" );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Option option = getOption( 3 );
				if ( PLUS_SIGN.getCount( KoLConstants.inventory ) > 0 )
				{
					option.text( "muscle substats" );
				}
				else
				{
					option.attachItem( PLUS_SIGN, AUTO );
				}
			}
		};

		new ChoiceAdventure( 452, "Leave a Message and I'll Call You Back", "Professor Jacking's Small-O-Fier" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "tiny fly glasses", true )
					.turnCost( 1 )
					.attachItem( ItemPool.TINY_FLY_GLASSES, 1, AUTO );
				new Option( 3, "fruit", true )
					.turnCost( 1 );

				new CustomOption( 1, "look for the spider" );
			}
		};

		new ChoiceAdventure( 453, "Getting a Leg Up", "Professor Jacking's Small-O-Fier" )
		{
			void setup()
			{
				new Option( 1, "fight jungle scabie", true );
				new Option( 2, "stats", true )
					.turnCost( 1 );
				new Option( 3, "hair of the calf", true )
					.turnCost( 1 )
					.attachItem( "hair of the calf", 1, AUTO );
			}
		};

		new ChoiceAdventure( 454, "Just Like the Ocean Under the Moon", "Professor Jacking's Small-O-Fier" )
		{
			void setup()
			{
				new Option( 1, "fight smooth jazz scabie", true );
				new Option( 2, "HP and MP", true )
					.turnCost( 1 )
					.attachMP( 95 );
			}
		};

		new ChoiceAdventure( 455, "Double Trouble in the Stubble", "Professor Jacking's Small-O-Fier" )
		{
			void setup()
			{
				new Option( 1, "stats", true )
					.turnCost( 1 );
				new Option( 2, "The Legendary Beat", true )
					.turnCost( 1 )
					.attachItem( ItemPool.LEGENDARY_BEAT, 1, AUTO )
					.attachItem( "can-you-dig-it?", new DisplayAll( NEED, EQUIPPED_AT_LEAST, 1 ) );
			}
		};

		new ChoiceAdventure( 456, "Made it, Ma!	 Top of the World!", "Professor Jacking's Huge-A-Ma-tron" )
		{
			void setup()
			{
				new Option( 1, "fight The Whole Kingdom", true );
				new Option( 2, "10 adv of Hurricane Force", true )
					.turnCost( 1 )
					.attachEffect( "Hurricane Force" );
				new Option( 3, "a dance upon the palate, or skip adventure", true )
					.turnCost( 1 )
					.attachItem( "a dance upon the palate", 1, AUTO, new DisplayAll( "palate", WANT, EXACTLY, 0 ) );
				new Option( 4, "stats", true )
					.turnCost( 1 );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 3 && request.responseText.contains( "You're already tasting too many things" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 457, "Oh, No! Five-Oh!", "Kegger in the Woods" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2, "skip adventure" );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				int count = InventoryManager.getCount( ItemPool.ORQUETTES_PHONE_NUMBER );
				if ( decision == 1 && count > 0 )
				{
					getOption( decision ).attachItem( ItemPool.ORQUETTES_PHONE_NUMBER, -count, MANUAL )
						.attachItem( ItemPool.KEGGER_MAP, -1, MANUAL );
				}
			}
		};

		new ChoiceAdventure( 458, "... Grow Unspeakable Horrors", "The Arrrboretum" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 459 );

		new ChoiceAdventure( 460, "Bridge", null ) // Space Trip
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				ArcadeRequest.visitSpaceTripChoice( request.responseText );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 461, "Navigation", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 462, "Diagnostics", null )
		{
			void setup()
			{
				new Option( 1 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 463, "Alpha Quadrant", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 464, "Beta Quadrant", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 465, "Explore an uncharted system", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new UnknownChoiceAdventure( 466 );

		new ChoiceAdventure( 467, "Combat", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
				new Option( 7 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 468, "Starbase Hub", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 469, "General Store", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
				new Option( 7 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 470, "Military Surplus Store", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 471, "DemonStar", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				ArcadeRequest.visitDemonStarChoice( request.responseText );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceDemonStar( request, decision );
			}
		};

		new ChoiceAdventure( 472, "Astrozorian Trade Vessel", null ) // Alpha Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 473, "Murderbots", null ) // Alpha Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 474, "Slavers", null ) // Alpha Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 475, "Astrozorian Trade Vessel", null ) // Beta Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 476, "Astrozorian Trade Vessel", null ) // Gamma Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 477, "Gamma Quadrant", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 478, "The Source", null ) // Gamma Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 479, "Slavers", null ) // Beta Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 480, "Scadians", null ) // Beta Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 481, "Hipsterians", null ) // Gamma Quadrant
		{
			void setup()
			{
				// <nothing>
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 482, "Slavers", null ) // Gamma Quadrant
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 483, "Scadian Homeworld", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 484, "End", null )
		{
			void setup()
			{
				new Option( 1 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceSpaceTrip( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 485, "Fighters Of Fighting", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
				new Option( 6 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				ArcadeRequest.visitFightersOfFightingChoice( request.responseText );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				ArcadeRequest.decorateFightersOfFighting( buffer );
			}

			@Override
			String specialChoiceHandling( GenericRequest request )
			{
				return ArcadeRequest.autoChoiceFightersOfFighting( request );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceFightersOfFighting( request, decision );
			}
		};

		new ChoiceAdventure( 486, "Dungeon Fist!", null )
		{
			void setup()
			{
				this.option0IsManualControl = false;

				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
				new Option( 5 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				ArcadeRequest.visitDungeonFistChoice( request.responseText );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				ArcadeRequest.decorateDungeonFist( buffer );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				if ( ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE )
				{	// Don't automate this if we logged in in the middle of the game -
					// the auto script isn't robust enough to handle arbitrary starting points.
					return ArcadeRequest.autoDungeonFist( stepCount, responseText );
				}
				return "0";
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceDungeonFist( request, decision );
			}
		};

		new UnknownChoiceAdventure( 487 );

		new ChoiceAdventure( 488, "Bridge", null ) // Meteoid
		{
			void setup()
			{
				new Option( 1 ) // teleport
					.leadsTo( 490 );
				new Option( 2 ) // spacemall
					.leadsTo( 489 );
				new Option( 6 ) // self-destruct
					.leadsTo( 491 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				ArcadeRequest.visitMeteoidChoice( request.responseText );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceMeteoid( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 489, "SpaceMall", null )
		{
			void setup()
			{
				new Option( 1 ); // bomb
				new Option( 2 ); // missile
				new Option( 3 ); // energy
				new Option( 4 ); // ice beam
				new Option( 6 ) // close spacemall
					.leadsTo( 488 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceMeteoid( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 490, "Underground", null )
		{
			void setup()
			{
				new Option( 1 ); // up
				new Option( 2 ); // down
				new Option( 3 ); // left
				new Option( 4 ); // right
				new Option( 5 ); // shoot
				new Option( 6 ); // activate
				new Option( 7 ); // bomb
				new Option( 8 ); // missile
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceMeteoid( request, this.choice, decision );
			}
		};

		new ChoiceAdventure( 491, "Game Over", null )
		{
			void setup()
			{
				new Option( 1 );
			}

			@Override
			boolean registerRequest( String urlString, int decision )
			{
				return true;
			}

			@Override
			String encounterName( String urlString, String responseText )
			{
				return null;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				ArcadeRequest.postChoiceMeteoid( request, this.choice, decision );
			}
		};

		new UnknownChoiceAdventure( 492 );

		new UnknownChoiceAdventure( 493 );

		new UnknownChoiceAdventure( 494 );

		new UnknownChoiceAdventure( 495 );

		new ChoiceAdventure( 496, "Crate Expectations", "The Typical Tavern Cellar" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				TavernRequest.postTavernVisit( request );
			}
		};

		new ChoiceAdventure( 497, "SHAFT!", "Neckback Crick" )
		{
			void setup()
			{
				new Option( 1, "fight unearthed monstrosity" );
				new Option( 2, "skip adventure" );
			}
		};

		new UnknownChoiceAdventure( 498 );

		new UnknownChoiceAdventure( 499 );
	}
}
