package net.sourceforge.kolmafia.persistence.choiceadventures;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.EdServantData;
import net.sourceforge.kolmafia.FamiliarData;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.Modifiers;
import net.sourceforge.kolmafia.RequestLogger;

import net.sourceforge.kolmafia.objectpool.EffectPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;

import net.sourceforge.kolmafia.persistence.MonsterDatabase.Element;
import net.sourceforge.kolmafia.persistence.QuestDatabase;
import net.sourceforge.kolmafia.persistence.QuestDatabase.Quest;

import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ProcessType.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalImportance.*;
import static net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.GoalOperator.*;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.RetiredChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.UnknownChoiceAdventure;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.request.CharPaneRequest;
import net.sourceforge.kolmafia.request.CharPaneRequest.Companion;
import net.sourceforge.kolmafia.request.FloristRequest;
import net.sourceforge.kolmafia.request.FloristRequest.Florist;
import net.sourceforge.kolmafia.request.GenericRequest;

import net.sourceforge.kolmafia.session.AvatarManager;
import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.GameproManager;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.TurnCounter;

import net.sourceforge.kolmafia.textui.command.SnowsuitCommand;

import net.sourceforge.kolmafia.utilities.StringUtilities;

class CADatabase600to699 extends ChoiceAdventureDatabase
{
	final void add600to699()
	{
		new ChoiceAdventure( 600, "Summon Minion", null )
		{
			final AdventureResult WAKING_THE_DEAD = new AdventureResult( "Waking the Dead", 0, true );

			void setup()
			{
				this.option0IsManualControl = false;

				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( KoLCharacter.hasSkill( "Summon Horde" ) )
				{
					getOption( 1 ).text( "also get quantity * 10 adv of Waking the Dead" )
						.attachEffect( WAKING_THE_DEAD );
				}
			}

			@Override
			String specialChoiceHandling( GenericRequest request )
			{
				if ( ChoiceManager.getSkillUses() > 0 )
				{
					// Add the quantity field here and let the decision get added later
					request.addFormField( "quantity", String.valueOf( ChoiceManager.getSkillUses() ) );
				}
				return null;
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// Summon Minion is a skill
				if ( ChoiceManager.getSkillUses() > 0 )
				{
					ChoiceManager.setSkillUses( 0 );
					return "1";
				}
				return "2";
			}
		};

		new ChoiceAdventure( 601, "Summon Horde", null )
		{
			void setup()
			{
				this.option0IsManualControl = false;

				new Option( 1 )
					.attachMeat( -1000, MANUAL );
				new Option( 2 );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// Summon Horde is a skill, and unlike Summon Minion,can only be done 1 cast at a time
				int askedSkillUses = ChoiceManager.getSkillUses();

				if ( askedSkillUses > 0 )
				{
					// This skill has to be done 1 cast at a time
					ChoiceManager.setSkillUses( --askedSkillUses );
					return "1";
				}
				return "2";
			}
		};

		new ChoiceAdventure( 602, "Behind the Gash", null )
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
			void postChoice2( GenericRequest request, int decision )
			{
				// This is a multi-part choice adventure, and we only want to handle the last choice
				if ( request.responseText.contains( "you shout into the blackness" ) )
				{
					AvatarManager.handleAfterAvatar( decision );
				}
			}
		};

		new ChoiceAdventure( 603, "Skeletons and The Closet", "Item-Driven" )
		{
			final AdventureResult SKELETON = ItemPool.get( ItemPool.SKELETON, -1 );

			void setup()
			{
				this.canWalkFromChoice = true;

				this.customName = "Skeleton";

				new Option( 1, "warrior (dmg, delevel)", true )
					.attachEffect( "Skeletal Warrior" )
					.attachItem( SKELETON, MANUAL, new NoDisplay() );
				new Option( 2, "cleric (hot dmg, hp)", true )
					.attachEffect( "Skeletal Cleric" )
					.attachItem( SKELETON, MANUAL, new NoDisplay() );
				new Option( 3, "wizard (cold dmg, mp)", true )
					.attachEffect( "Skeletal Wizard" )
					.attachItem( SKELETON, MANUAL, new NoDisplay() );
				new Option( 4, "rogue (dmg, meat)", true )
					.attachEffect( "Skeletal Rogue" )
					.attachItem( SKELETON, MANUAL, new NoDisplay() );
				new Option( 5, "buddy (delevel, exp)", true )
					.attachEffect( "Skeletal Buddy" )
					.attachItem( SKELETON, MANUAL, new NoDisplay() );
				new Option( 6 );

				new CustomOption( 6, "ignore this adventure" );
			}
		};

		new ChoiceAdventure( 604, "Welcome to the Great Overlook Lodge", "Twin Peak" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 605 );

		new ChoiceAdventure( 606, "Lost in the Great Overlook Lodge", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 607 );
				new Option( 2 )
					.leadsTo( 608 );
				new Option( 3 )
					.leadsTo( 609 )
					.attachItem( ItemPool.JAR_OF_OIL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 4 )
					.leadsTo( 610 );
				new Option( 6, "flee" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				getOption( 1 ).text( "need +4 stench resist, have " + KoLCharacter.getElementalResistanceLevels( Element.STENCH ) );

				// annoyingly, the item drop check does not take into account fairy (or other sidekick) bonus.
				// This is just a one-off implementation, but should be standardized somewhere in Modifiers
				// if kol adds more things like this.
				double bonus = 0;

				// Check for familiars
				if ( !KoLCharacter.getFamiliar().equals( FamiliarData.NO_FAMILIAR ) )
				{
					bonus = Modifiers.getNumericModifier( KoLCharacter.getFamiliar(), "Item Drop" );
				}
				// Check for Clancy
				else if ( KoLCharacter.getCurrentInstrument() != null &&
					KoLCharacter.getCurrentInstrument().equals( CharPaneRequest.LUTE ) )
				{
					int weight = 5 * KoLCharacter.getMinstrelLevel();
					bonus = Math.sqrt( 55 * weight ) + weight - 3;
				}
				// Check for Eggman
				else if ( KoLCharacter.getCompanion() == Companion.EGGMAN )
				{
					bonus = KoLCharacter.hasSkill( "Working Lunch" ) ? 75 : 50;
				}
				// Check for Cat Servant
				else if ( KoLCharacter.isEd() )
				{
					EdServantData servant = EdServantData.currentServant();
					if ( servant != null && servant.getId() == 1 )
					{
						int level = servant.getLevel();
						if ( level >= 7 )
						{
							bonus = Math.sqrt( 55 * level ) + level - 3;
						}
					}
				}
				// Check for Throne
				FamiliarData throned = KoLCharacter.getEnthroned();
				if ( !throned.equals( FamiliarData.NO_FAMILIAR ) )
				{
					bonus += Modifiers.getNumericModifier( "Throne", throned.getRace(), "Item Drop" );
				}
				// Check for Bjorn
				FamiliarData bjorned = KoLCharacter.getBjorned();
				if ( !bjorned.equals( FamiliarData.NO_FAMILIAR ) )
				{
					bonus += Modifiers.getNumericModifier( "Throne", bjorned.getRace(), "Item Drop" );
				}
				// Check for Florist
				if ( FloristRequest.haveFlorist() )
				{
					List<Florist> plants = FloristRequest.getPlants( "Twin Peak" );
					if ( plants != null )
					{
						for ( Florist plant : plants )
						{
							bonus += Modifiers.getNumericModifier( "Florist", plant.toString(), "Item Drop" );
						}
					}
				}

				getOption( 2 ).text( "need +50% item drop, have " + Math.round( KoLCharacter.getItemDropPercentAdjustment() +
					KoLCharacter.currentNumericModifier( Modifiers.FOODDROP ) - bonus ) + "%" );

				getOption( 4 ).text( "need +40% init, have " + KoLCharacter.getInitiativeAdjustment() + "%" );
			}
		};

		new ChoiceAdventure( 607, "Room 237", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2 )
					.leadsTo( 606 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// Twin Peak first choice
				if ( request.responseText.contains( "You take a moment to steel your nerves." ) )
				{
					int prefval = Preferences.getInteger( "twinPeakProgress" );
					prefval |= 1;
					Preferences.setInteger( "twinPeakProgress", prefval );
				}
			}
		};

		new ChoiceAdventure( 608, "Go Check It Out!", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2 )
					.leadsTo( 606 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// Twin Peak second choice
				if ( request.responseText.contains( "All work and no play" ) )
				{
					int prefval = Preferences.getInteger( "twinPeakProgress" );
					prefval |= 2;
					Preferences.setInteger( "twinPeakProgress", prefval );
				}
			}
		};

		new ChoiceAdventure( 609, "There's Always Music In the Air", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 616 )
					.attachItem( ItemPool.JAR_OF_OIL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 2 )
					.leadsTo( 606 );
			}
		};

		new ChoiceAdventure( 610, "To Catch a Killer", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 1056 );
				new Option( 2 )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 1 && request.responseText.contains( "You're starting to pant" ) )
				{
					getOption( decision ).turnCost( 1 )
						.entersQueue( true );
				}
			}
		};

		new ChoiceAdventure( 611, "The Horror...", "A-Boo Peak" )
		{
			int abooPeakLevel = 0;

			void setup()
			{
				this.neverEntersQueue = true;

				new Option( 1 );
				new Option( 2, "Flee" );
			}

			@Override
			void preChoice( String urlString )
			{
				// To find which step we're on, look at the responseText from the _previous_ request.  This should still be in lastResponseText.
				abooPeakLevel = findBooPeakLevel( ChoiceManager.findChoiceDecisionText( 1, ChoiceManager.lastResponseText ) );
				// Handle changing the progress level in postChoice2 where we know the result.
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				getOption( 1 ).text( booPeakDamage() );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "<p><a href=\"adventure.php?snarfblat=296\">Adventure Again (A-Boo Peak)</a>" ) )
				{
					getOption( decision ).turnCost( 1 );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// We need to detect if the choiceadv step was completed OR we got beaten up.
				// If we Flee, nothing changes
				if ( decision == 1 )
				{
					if ( request.responseText.contains( "That's all the horror you can take" ) ) // AKA beaten up
					{
						Preferences.decrement( "booPeakProgress", 2, 0 );
					}
					else
					{
						Preferences.decrement( "booPeakProgress", 2 * abooPeakLevel, 0 );
					}
					if ( Preferences.getInteger( "booPeakProgress" ) < 0 )
					{
						Preferences.setInteger( "booPeakProgress", 0 );
					}
				}
			}

			@Override
			void decorateChoiceResponse( StringBuffer buffer, int option )
			{
				int index = buffer.indexOf( "<p><a href=\"adventure.php?snarfblat=296\">Adventure Again (A-Boo Peak)</a>" );
				if ( index == -1 )
				{
					return;
				}

				boolean glover = KoLCharacter.inGLover();
				int itemId = glover ? ItemPool.GLUED_BOO_CLUE : ItemPool.BOO_CLUE;
				int count = ItemPool.get( itemId, 1 ).getCount( KoLConstants.inventory );
				if ( count == 0 )
				{
					return;
				}

				String name = glover ? "glued A-Boo Clue" : "A-Boo Clue";
				String link = "<a href=\"javascript:singleUse('inv_use.php','which=3&whichitem=" + itemId +
						"&pwd=" + GenericRequest.passwordHash +
						"&ajax=1');void(0);\">Use another " +
						name +
						"</a>";
				buffer.insert( index, link );
			}

			String booPeakDamage()
			{
				int booPeakLevel = findBooPeakLevel( ChoiceManager.findChoiceDecisionText( 1, ChoiceManager.lastResponseText ) );
				if ( booPeakLevel < 1 )
					return "";

				int damageTaken = 0;
				int diff = 0;

				switch ( booPeakLevel )
				{
				case 1:
					// actual base damage is 13
					damageTaken = 30;
					diff = 17;
					break;
				case 2:
					// actual base damage is 25
					damageTaken = 30;
					diff = 5;
					break;
				case 3:
					damageTaken = 50;
					break;
				case 4:
					damageTaken = 125;
					break;
				case 5:
					damageTaken = 250;
					break;
				}

				double spookyDamage = KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.SPOOKYFORM ) ) ? 1.0 :
					Math.max( damageTaken * ( 100.0 - KoLCharacter.elementalResistanceByLevel( KoLCharacter.getElementalResistanceLevels( Element.SPOOKY ) ) ) / 100.0 - diff, 1 );
				if ( KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.COLDFORM ) ) || KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.SLEAZEFORM ) ) )
				{
					spookyDamage *= 2;
				}

				double coldDamage = KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.COLDFORM ) ) ? 1.0 :
					Math.max( damageTaken * ( 100.0 - KoLCharacter.elementalResistanceByLevel( KoLCharacter.getElementalResistanceLevels( Element.COLD ) ) ) / 100.0 - diff, 1 );
				if ( KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.SLEAZEFORM ) ) || KoLConstants.activeEffects.contains( EffectPool.get( EffectPool.STENCHFORM ) ) )
				{
					coldDamage *= 2;
				}
				return ( (int) Math.ceil( spookyDamage ) ) + " spooky damage, " + ( (int) Math.ceil( coldDamage ) ) + " cold damage";
			}

			int findBooPeakLevel( String decisionText )
			{
				if ( decisionText == null )
				{
					return 0;
				}
				if ( decisionText.equals( "Ask the Question" ) || decisionText.equals( "Talk to the Ghosts" ) ||
					decisionText.equals( "I Wanna Know What Love Is" ) || decisionText.equals( "Tap Him on the Back" ) ||
					decisionText.equals( "Avert Your Eyes" ) || decisionText.equals( "Approach a Raider" ) ||
					decisionText.equals( "Approach the Argument" ) || decisionText.equals( "Approach the Ghost" ) ||
					decisionText.equals( "Approach the Accountant Ghost" ) || decisionText.equals( "Ask if He's Lost" ) )
				{
					return 1;
				}
				else if ( decisionText.equals( "Enter the Crypt" ) ||
					decisionText.equals( "Try to Talk Some Sense into Them" ) ||
					decisionText.equals( "Put Your Two Cents In" ) || decisionText.equals( "Talk to the Ghost" ) ||
					decisionText.equals( "Tell Them What Werewolves Are" ) || decisionText.equals( "Scream in Terror" ) ||
					decisionText.equals( "Check out the Duel" ) || decisionText.equals( "Watch the Fight" ) ||
					decisionText.equals( "Approach and Reproach" ) || decisionText.equals( "Talk Back to the Robot" ) )
				{
					return 2;
				}
				else if ( decisionText.equals( "Go down the Steps" ) || decisionText.equals( "Make a Suggestion" ) ||
					decisionText.equals( "Tell Them About True Love" ) || decisionText.equals( "Scold the Ghost" ) ||
					decisionText.equals( "Examine the Pipe" ) || decisionText.equals( "Say What?" ) ||
					decisionText.equals( "Listen to the Lesson" ) || decisionText.equals( "Listen in on the Discussion" ) ||
					decisionText.equals( "Point out the Malefactors" ) || decisionText.equals( "Ask for Information" ) )
				{
					return 3;
				}
				else if ( decisionText.equals( "Hurl Some Spells of Your Own" ) || decisionText.equals( "Take Command" ) ||
					decisionText.equals( "Lose Your Patience" ) || decisionText.equals( "Fail to Stifle a Sneeze" ) ||
					decisionText.equals( "Ask for Help" ) ||
					decisionText.equals( "Ask How Duskwalker Basketball Is Played, Against Your Better Judgment" ) ||
					decisionText.equals( "Knights in White Armor, Never Reaching an End" ) ||
					decisionText.equals( "Own up to It" ) || decisionText.equals( "Approach the Poor Waifs" ) ||
					decisionText.equals( "Look Behind You" ) )
				{
					return 4;
				}
				else if ( decisionText.equals( "Read the Book" ) || decisionText.equals( "Join the Conversation" ) ||
					decisionText.equals( "Speak of the Pompatus of Love" ) || decisionText.equals( "Ask What's Going On" ) ||
					decisionText.equals( "Interrupt the Rally" ) || decisionText.equals( "Ask What She's Doing Up There" ) ||
					decisionText.equals( "Point Out an Unfortunate Fact" ) || decisionText.equals( "Try to Talk Sense" ) ||
					decisionText.equals( "Ask for Directional Guidance" ) || decisionText.equals( "What?" ) )
				{
					return 5;
				}

				return 0;
			}
		};

		new RetiredChoiceAdventure( 612, "Behind the world there is a door...", null )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 613, true );
				new Option( 2 );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				TurnCounter.stopCounting( "Silent Invasion window begin" );
				TurnCounter.stopCounting( "Silent Invasion window end" );
				TurnCounter.startCounting( 35, "Silent Invasion window begin loc=*", "lparen.gif" );
				TurnCounter.startCounting( 40, "Silent Invasion window end loc=*", "rparen.gif" );
			}
		};

		new ChoiceAdventure( 613, "Behind the door there is a fog", null )
		{
			final Pattern FOG_PATTERN = Pattern.compile( "<font.*?><b>(.*?)</b></font>" );

			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "fight four-shadowed mime" );
				new Option( 3 )
					.leadsTo( 614 );
				new Option( 4, "use soul coin to get a skillbook" )
					.turnCost( 1 )
					.attachItem( "soul coin", -1, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 4 );
				switch ( KoLCharacter.mainStat() )
				{
				case MUSCLE:
					option.attachItem( ItemPool.INFURIATING_SILENCE_RECORD, 1, AUTO );
				case MYSTICALITY:
					option.attachItem( ItemPool.TRANQUIL_SILENCE_RECORD, 1, AUTO );
				case MOXIE:
					option.attachItem( ItemPool.MENACING_SILENCE_RECORD, 1, AUTO );
				default:
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 4 && request.responseText.contains( "don't have any change" ) )
				{
					this.choiceFailed().turnCost( 1 );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					Matcher fogMatcher = FOG_PATTERN.matcher( request.responseText );
					if ( fogMatcher.find() )
					{
						String message = "Message: \"" + fogMatcher.group( 1 ) + "\"";
						RequestLogger.printLine( message );
						RequestLogger.updateSessionLog( message );
					}
				}
			}
		};

		new ChoiceAdventure( 614, "Near the fog there is an... anvil?", null )
		{
			void setup()
			{
				new Option( 1, "soul doorbell" )
					.turnCost( 1 )
					.attachItem( "soul doorbell", 1, AUTO )
					.attachItem( ItemPool.MIME_SOUL_FRAGMENT, -3, MANUAL, new NoDisplay() );
				new Option( 2, "soul mask" )
					.turnCost( 1 )
					.attachItem( "soul mask", 1, AUTO )
					.attachItem( ItemPool.MIME_SOUL_FRAGMENT, -11, MANUAL, new NoDisplay() );
				new Option( 3, "soul knife" )
					.turnCost( 1 )
					.attachItem( "soul knife", 1, AUTO )
					.attachItem( ItemPool.MIME_SOUL_FRAGMENT, -23, MANUAL, new NoDisplay() );
				new Option( 4, "soul coin" )
					.turnCost( 1 )
					.attachItem( "soul coin", 1, AUTO )
					.attachItem( ItemPool.MIME_SOUL_FRAGMENT, -37, MANUAL, new NoDisplay() );
				new Option( 5 );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision < 5 && !request.responseText.contains( "You acquire" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 615, "Ding Dong", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1 )
					.leadsTo( 613, true );
			}
		};

		new ChoiceAdventure( 616, "He Is the Arm, and He Sounds Like This", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 )
					.attachItem( ItemPool.JAR_OF_OIL, -1, MANUAL );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				int prefval = Preferences.getInteger( "twinPeakProgress" );
				prefval |= 4;
				Preferences.setInteger( "twinPeakProgress", prefval );
			}
		};

		new RetiredChoiceAdventure( 617, "Now It's Dark", "Twin Peak" ) // moved to 1056
		{
			void setup()
			{
				new Option( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// Twin Peak fourth choice
				if ( request.responseText.contains( "When the lights come back" ) )
				{
					// the other three must be completed at this point.
					Preferences.setInteger( "twinPeakProgress", 15 );
				}
			}
		};

		new ChoiceAdventure( 618, "Cabin Fever", "Twin Peak" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "mercifully, the hotel explodes" ) )
				{
					// the other three must be completed at this point.
					Preferences.setInteger( "twinPeakProgress", 15 );
				}
			}
		};

		new ChoiceAdventure( 619, "To Meet a Gourd", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 620, "A Blow Is Struck!", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void registerDeferredChoice()
			{
				RequestLogger.registerLastLocation();
			}
		};

		new ChoiceAdventure( 621, "Hold the Line!", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void registerDeferredChoice()
			{
				RequestLogger.registerLastLocation();
			}
		};

		new ChoiceAdventure( 622, "The Moment of Truth", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}

			@Override
			void registerDeferredChoice()
			{
				RequestLogger.registerLastLocation();
			}
		};

		new ChoiceAdventure( 623, "Return To the Fray!", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 624, "Returning to Action", "The Gourd!" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 625, "The Table", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 626, "Super Crimboman Crimbo Type is Go!", null )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 627, "ChibiBuddy&trade;", "Item-Driven" ) // on
		{
			void setup()
			{
				this.canWalkFromChoice = true;

				new Option( 1 )
					.leadsTo( 628 );
				new Option( 2 )
					.leadsTo( 629 );
				new Option( 3 )
					.leadsTo( 630 );
				new Option( 4 )
					.leadsTo( 631 );
				new Option( 5 )
					.attachEffect( "ChibiChanged" );
				new Option( 6 );
				new Option( 7 );
			}
		};

		new ChoiceAdventure( 628, "Feeding your ChibiBuddy&trade;", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1, "+Fitness" )
					.turnCost( 1 );
				new Option( 2, "-Intelligence" )
					.turnCost( 1 );
				new Option( 3, "+Intelligence, -Socialization" );
				new Option( 4, "+Alignment, -Intelligence" );
				new Option( 5, "+Socialization, -Alignment" );
				new Option( 6 )
					.leadsTo( 627 );
			}
		};

		new ChoiceAdventure( 629, "Entertaining your ChibiBuddy&trade;", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1, "+Intelligence" )
					.turnCost( 1 );
				new Option( 2, "-Socialization" )
					.turnCost( 1 );
				new Option( 3, "+Socialization, -Fitness" );
				new Option( 4, "+Fitness, -Alignment" );
				new Option( 5, "+Alignment, -Socialization" );
				new Option( 6 )
					.leadsTo( 627 );
			}
		};

		new ChoiceAdventure( 630, "Interacting with your ChibiBuddy&trade;", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1, "+Socialization" )
					.turnCost( 1 );
				new Option( 2, "-Alignment" )
					.turnCost( 1 );
				new Option( 3, "+Fitness, -Intelligence" );
				new Option( 4, "+Alignment, -Fitness" );
				new Option( 5, "+Intelligence, -Alignment" );
				new Option( 6 )
					.leadsTo( 627 );
			}
		};

		new ChoiceAdventure( 631, "Exploring with your ChibiBuddy&trade;", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1, "+Alignment" )
					.turnCost( 1 );
				new Option( 2, "-Fitness" )
					.turnCost( 1 );
				new Option( 3, "+Intelligence, -Fitness" );
				new Option( 4, "+Fitness, -Socialization" );
				new Option( 5, "+Socialization, -Intelligence" );
				new Option( 6 )
					.leadsTo( 627 );
			}
		};

		new ChoiceAdventure( 632, "Add an E-Mail Address", null )
		{
			void setup()
			{
				this.canWalkFromChoice = true;

				new Option( 1 );
			}
		};

		new ChoiceAdventure( 633, "ChibiBuddy&trade;", "Item-Driven" ) // off
		{
			void setup()
			{
				this.canWalkFromChoice = true;

				new Option( 1 )
					.attachItem( ItemPool.CHIBIBUDDY_ON, 1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.CHIBIBUDDY_OFF, -1, MANUAL, new NoDisplay() );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 634, "Goodbye Fnord", "The Gourd!" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void registerDeferredChoice()
			{
				RequestLogger.registerLastLocation();
			}
		};

		new UnknownChoiceAdventure( 635 );

		new ChoiceAdventure( 636, "First Mate's Log", "The Old Man's Bathtime Adventures" ) // set 1
		{
			void setup()
			{
				new Option( 1, "-1 Crayon, Add Cray-Kin" )
					.turnCost( 1 );
				new Option( 2, "+3 crew, -8-10 bubbles" )
					.turnCost( 1 );
				new Option( 3, "+2 crayons, +8-11 bubbles" )
					.turnCost( 1 );
				new Option( 4, "Block Ferocious roc, -2 crayons" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 637, "First Mate's Log", "The Old Man's Bathtime Adventures" ) // set 2
		{
			void setup()
			{
				new Option( 1, "Add Bristled Man-O-War" )
					.turnCost( 1 );
				new Option( 2, "Block Deadly Hydra, -3 crayons" )
					.turnCost( 1 );
				new Option( 3, "+20-23 bubbles, -1 crew" )
					.turnCost( 1 );
				new Option( 4, "Block giant man-eating shark, -16 bubbles" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 638, "First Mate's Log", "The Old Man's Bathtime Adventures" ) // set 3
		{
			void setup()
			{
				new Option( 1, "Add Deadly Hydra" )
					.turnCost( 1 );
				new Option( 2, "+13-19 bubbles" )
					.turnCost( 1 );
				new Option( 3, "+4 crayon, -1 crew" )
					.turnCost( 1 );
				new Option( 4, "Block fearsome giant squid, -13-20 bubbles" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 639, "First Mate's Log", "The Old Man's Bathtime Adventures" ) // set 4
		{
			void setup()
			{
				new Option( 1, "-8 crew, -3 crayons, -17 bubbles, increase NC rate" )
					.turnCost( 1 );
				new Option( 2, "+3 crayons" )
					.turnCost( 1 );
				new Option( 3, "+3 crayons, +16 bubbles, -2 crew" )
					.turnCost( 1 );
				new Option( 4, "+5 crew, -6-16 bubbles, -2 crayons" )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 640, "Tailor the Snow Suit", "Item-Driven" )
		{
			void setup()
			{
				this.customName = "Snow Suit";

				new Option( 1, "Familiar does physical damage", true );
				new Option( 2, "Familiar does cold damage", true );
				new Option( 3, "+10% item drops, can drop carrot nose", true )
					.attachItem( ItemPool.CARROT_NOSE );
				new Option( 4, "Heals 1-20 HP after combat", true );
				new Option( 5, "Restores 1-10 MP after combat", true );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				Preferences.setString( "snowsuit", SnowsuitCommand.DECORATION[ decision - 1 ][ 0 ] );
			}
		};

		new ChoiceAdventure( 641, "Stupid Pipes.", "Anger Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "Dive Down" ) &&
				     KoLCharacter.getElementalResistanceLevels( Element.HOT ) >= 25 )
				{
					Preferences.setBoolean( "flickeringPixel1", true );
				}

				int resistance = KoLCharacter.getElementalResistanceLevels( Element.HOT );
				int damage = (int)( 2.50 * ( 100.0 - KoLCharacter.elementalResistanceByLevel( resistance ) ) );
				long hp = KoLCharacter.getCurrentHP();

				getOption( 1 ).text( "take " + damage + " hot damage, current HP = " + hp + ", current hot resistance = " + resistance );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel1", true );
				}
			}
		};

		new ChoiceAdventure( 642, "You're Freaking Kidding Me", "Anger Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "Wait a minute..." ) &&
				     KoLCharacter.getAdjustedMuscle() >= 500 &&
				     KoLCharacter.getAdjustedMysticality() >= 500 &&
				     KoLCharacter.getAdjustedMoxie() >= 500 )
				{
					Preferences.setBoolean( "flickeringPixel2", true );
				}

				getOption( 1 ).text( "50 buffed Muscle/Mysticality/Moxie required, have " +
						KoLCharacter.getAdjustedMuscle() +
						"/" +
						KoLCharacter.getAdjustedMysticality() +
						"/" +
						KoLCharacter.getAdjustedMoxie() );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel2", true );
				}
			}
		};

		new ChoiceAdventure( 643, "Great. A Stupid Door. What Next?", "Anger Man's Level" )
		{
			void setup()
			{
				new Option( 1, "fight Anger Man" );
				new Option( 2, "skip adventure" );
			}
		};

		new ChoiceAdventure( 644, "Snakes.", "Fear Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "Tie the snakes in a knot." ) &&
				     KoLCharacter.getAdjustedMoxie() >= 300 )
				{
					Preferences.setBoolean( "flickeringPixel3", true );
				}

				getOption( 1 ).text( "50 buffed Moxie required, have " + KoLCharacter.getAdjustedMoxie() );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel3", true );
				}
			}
		};

		new ChoiceAdventure( 645, "So... Many... Skulls...", "Fear Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "You fear no evil" ) &&
				     KoLCharacter.getElementalResistanceLevels( Element.SPOOKY ) >= 25 )
				{
					Preferences.setBoolean( "flickeringPixel4", true );
				}

				int resistance = KoLCharacter.getElementalResistanceLevels( Element.SPOOKY );
				int damage = (int)( 2.50 * ( 100.0 - KoLCharacter.elementalResistanceByLevel( resistance ) ) );
				long hp = KoLCharacter.getCurrentHP();

				getOption( 1 ).text( "take " + damage + " spooky damage, current HP = " + hp + ", current spooky resistance = " + resistance );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel4", true );
				}
			}
		};

		new ChoiceAdventure( 646, "Oh No... A Door...", "Fear Man's Level" )
		{
			void setup()
			{
				new Option( 1, "fight Fear Man" );
				new Option( 2, "skip adventure" );
			}
		};

		new ChoiceAdventure( 647, "A Stupid Dummy. Also, a Straw Man.", "Doubt Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				// *** unspaded
				if ( !request.responseText.contains( "Graaaaaaaaargh!" ) &&
				     KoLCharacter.currentBonusDamage() >= 1000 )
				{
					Preferences.setBoolean( "flickeringPixel5", true );
				}

				getOption( 1 ).text( "100 weapon damage required, have " + String.valueOf( KoLCharacter.currentBonusDamage() ) );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel5", true );
				}
			}
		};

		new ChoiceAdventure( 648, "Slings and Arrows", "Doubt Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				// *** Yes, there's supposed to be two spaces there.
				if ( !request.responseText.contains( "Arrows?  Ha." ) &&
				     KoLCharacter.getCurrentHP() >= 1000 )
				{
					Preferences.setBoolean( "flickeringPixel6", true );
				}

				getOption( 1 ).text( "101 HP required, have " + KoLCharacter.getCurrentHP() );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel6", true );
				}
			}
		};

		new ChoiceAdventure( 649, "A Door. Figures.", "Doubt Man's Level" )
		{
			void setup()
			{
				new Option( 1, "fight Doubt Man" );
				new Option( 2, "skip adventure" );
			}
		};

		new ChoiceAdventure( 650, "This Is Your Life. Your Horrible, Horrible Life.", "Regret Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "Then watch it again with the commentary on!" ) &&
				     KoLCharacter.getCurrentMP() >= 1000 )
				{
					Preferences.setBoolean( "flickeringPixel7", true );
				}

				getOption( 1 ).text( "101 MP required, have " + KoLCharacter.getCurrentMP() );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel7", true );
				}
			}
		};

		new ChoiceAdventure( 651, "The Wall of Wailing", "Regret Man's Level" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
				new Option( 2, "flickering pixel" )
					.turnCost( 1 )
					.attachItem( "flickering pixel", 1, AUTO );
				new Option( 3, "skip adventure" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( !request.responseText.contains( "Make the tide resist you" ) &&
				     KoLCharacter.currentPrismaticDamage() >= 60 )
				{
					Preferences.setBoolean( "flickeringPixel8", true );
				}

				getOption( 1 ).text( "10 prismatic damage required, have " + KoLCharacter.currentPrismaticDamage() );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 && request.responseText.contains( "flickering pixel" ) )
				{
					Preferences.setBoolean( "flickeringPixel8", true );
				}
			}
		};

		new ChoiceAdventure( 652, "A Door. Too Soon...", "Regret Man's Level" )
		{
			void setup()
			{
				new Option( 1, "fight Regret Man" );
				new Option( 2, "skip adventure" );
			}
		};

		new UnknownChoiceAdventure( 653 );

		new ChoiceAdventure( 654, "Courier? I don't even...", "Chinatown Shops" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new ChoiceAdventure( 655, "They Have a Fight, Triangle Loses", "Triad Factory" )
		{
			void setup()
			{
				// right after the fight against The Sierpinski brothers.
				// apparently takes *another* turn??
				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 656, "Wheels Within Wheels", "Item-Driven" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new ChoiceAdventure( 657, "You Grind 16 Rats, and Whaddya Get?", "Chinatown Tenement" )
		{
			void setup()
			{
				new Option( 1, "fight The Server", true )
					.leadsTo( 658 )
					.attachItem( ItemPool.GOLD_PIECE, -30, MANUAL, new DisplayAll( WANT, AT_LEAST, 30 ) );
				new Option( 2, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 1 && request.responseText.contains( "Grumbling, I turned and stomped away" ) )
				{
					this.choiceFailed();
				}
			}
		};

		new ChoiceAdventure( 658, "Debasement", "Chinatown Tenement" )
		{
			void setup()
			{
				new Option( 1, "fight The Server, for real this time" );
				new Option( 2, "chicken out, wasting the 30 coins" );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				return "1";
			}
		};

		new ChoiceAdventure( 659, "How Does a Floating Platform Even Work?", "undefined" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 660, "It's a Place Where Books Are Free", "undefined" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 661, "Sphinx For the Memories", "undefined" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 662, "Think or Thwim", "undefined" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 663, "When You're a Stranger", "undefined" )
		{
			void setup()
			{
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}
		};

		new ChoiceAdventure( 664, "The Crackpot Mystic's Shed", null )
		{
			void setup()
			{
				this.canWalkFromChoice = true;

				new Option( 1 )
					.attachItem( ItemPool.TRANSFUNCTIONER, 1, AUTO );
				new Option( 2 );
			}
		};

		new ChoiceAdventure( 665, "A Gracious Maze", "undefined" )
		{
			void setup()
			{
				this.hasGoalButton = true;
				this.option0IsManualControl = false;

				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
			}

			@Override
			void decorateChoice( StringBuffer buffer )
			{
				GameproManager.addGoalButton( buffer );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				if ( ChoiceManager.action == ChoiceManager.PostChoiceAction.NONE )
				{	// Don't automate this if we logged in in the middle of the game -
					// the auto script isn't robust enough to handle arbitrary starting points.
					return GameproManager.autoSolve( stepCount );
				}
				return "0";
			}
		};

		new UnknownChoiceAdventure( 666 );

		new UnknownChoiceAdventure( 667 );

		new UnknownChoiceAdventure( 668 );

		new ChoiceAdventure( 669, "The Fast and the Furry-ous", "The Castle in the Clouds in the Sky (Basement)" )
		{
			final AdventureResult TITANIUM_UMBRELLA = ItemPool.get( ItemPool.TITANIUM_UMBRELLA, 0 );

			void setup()
			{
				this.customName = "Basement Furry";

				new Option( 1, "Open Ground Floor with titanium umbrella, otherwise Neckbeard Choice", true );
				new Option( 2, "200 Moxie substats", true )
					.turnCost( 1 );
				new Option( 4, "skip adventure and guarantees this adventure will reoccur" )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Option option = getOption( 1 );

				if ( QuestDatabase.isQuestBefore( Quest.GARBAGE, "step8" ) )
				{
					option.attachItem( TITANIUM_UMBRELLA, new DisplayAll( "umbrella", WANT, EQUIPPED_AT_LEAST, 1 ) );

					if ( KoLCharacter.hasEquipped( TITANIUM_UMBRELLA ) )
					{
						option.turnCost( 1 );
					}
					else
					{
						option.leadsTo( 671, true );
					}
				}
				else
				{
					option.text( "Neckbeard Choice" )
						.leadsTo( 671, true );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// All New Area Unlocked messages unlock the Ground Floor but check for it specifically in case future changes unlock areas with message.
				if ( request.responseText.contains( "New Area Unlocked" ) && request.responseText.contains( "The Ground Floor" ) )
				{
					Preferences.setInteger( "lastCastleGroundUnlock", KoLCharacter.getAscensions() );
					QuestDatabase.setQuestProgress( Quest.GARBAGE, "step8" );
				}
			}
		};

		new ChoiceAdventure( 670, "You Don't Mess Around with Gym", "The Castle in the Clouds in the Sky (Basement)" )
		{
			final AdventureResult EXTREME_AMULET = ItemPool.get( ItemPool.EXTREME_AMULET, 0 );

			void setup()
			{
				this.customName = "Basement Fitness";

				new Option( 1, "massive dumbbell, then skip adventure", true )
					.turnCost( 1 )
					.attachItem( "massive dumbbell", 1, AUTO );
				new Option( 2, "200 Muscle substats", true )
					.turnCost( 1 );
				new Option( 3, "Items", true )
					.turnCost( 1 )
					.attachItem( "pec oil", 1, AUTO )
					.attachItem( "giant jar of protein powder", 1, AUTO )
					.attachItem( "Squat-Thrust Magazine", 1, AUTO );
				new Option( 4, "Open Ground Floor with amulet, otherwise skip", true );
				new Option( 5, "skip adventure and guarantees this adventure will reoccur", true )
					.entersQueue( false );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 4 );

				if ( QuestDatabase.isQuestBefore( Quest.GARBAGE, "step8" ) )
				{
					option.attachItem( EXTREME_AMULET, new DisplayAll( "amulet", WANT, EQUIPPED_AT_LEAST, 1 ) );

					if ( KoLCharacter.hasEquipped( EXTREME_AMULET ) )
					{
						option.turnCost( 1 );
					}
					else
					{
						option.entersQueue( false );
					}
				}
				else
				{
					option.text( "skip adventure" )
						.entersQueue( false );
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// You take it, trying to shake off the feeling you've just stolen the giant's lost tongue piercing.
				if ( decision == 1 && !request.responseText.contains( "lost tongue piercing" ) )
				{
					this.choiceFailed().entersQueue( false );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// All New Area Unlocked messages unlock the Ground Floor but check for it specifically in case future changes unlock areas with message.
				if ( request.responseText.contains( "New Area Unlocked" ) && request.responseText.contains( "The Ground Floor" ) )
				{
					Preferences.setInteger( "lastCastleGroundUnlock", KoLCharacter.getAscensions() );
					QuestDatabase.setQuestProgress( Quest.GARBAGE, "step8" );
				}
			}
		};

		new ChoiceAdventure( 671, "Out in the Open Source", "The Castle in the Clouds in the Sky (Basement)" )
		{
			final AdventureResult MASSIVE_DUMBBELL = new AdventureResult( "massive dumbbell", 0, false );

			void setup()
			{
				this.customName = "Basement Neckbeard";

				new Option( 1, "Open Ground Floor with massive dumbbell, otherwise skip", true );
				new Option( 2, "200 Mysticality substats", true )
					.turnCost( 1 );
				new Option( 3, "O'RLY manual, open sauce", true )
					.turnCost( 1 )
					.attachItem( "O'RLY manual", 1, AUTO, new DisplayAll( "manual" ) )
					.attachItem( "open sauce", 1, AUTO );
				new Option( 4, "Fitness Choice", true )
					.leadsTo( 670, true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 1 );

				if ( QuestDatabase.isQuestBefore( Quest.GARBAGE, "step8" ) )
				{
					option.attachItem( MASSIVE_DUMBBELL, new DisplayAll( "dumbbell", WANT, AT_LEAST, 1 ) );

					if ( MASSIVE_DUMBBELL.getCount( KoLConstants.inventory ) > 0 )
					{
						option.turnCost( 1 );
					}
					else
					{
						option.entersQueue( false );
					}
				}
				else
				{
					option.text( "skip adventure" )
						.entersQueue( false );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				// All New Area Unlocked messages unlock the Ground Floor but check for it specifically in case future changes unlock areas with message.
				if ( request.responseText.contains( "New Area Unlocked" ) && request.responseText.contains( "The Ground Floor" ) )
				{
					Preferences.setInteger( "lastCastleGroundUnlock", KoLCharacter.getAscensions() );
					QuestDatabase.setQuestProgress( Quest.GARBAGE, "step8" );
				}
			}
		};

		new ChoiceAdventure( 672, "There's No Ability Like Possibility", "The Castle in the Clouds in the Sky (Ground Floor)" )
		{
			void setup()
			{
				this.customName = "Ground Possibility";

				new Option( 1, "3 random items", true )
					.turnCost( 1 );
				new Option( 2, "30 adv of Nothing Is Impossible", true )
					.turnCost( 1 )
					.attachEffect( "Nothing Is Impossible" );
				new Option( 3, "skip adventure", true )
					.entersQueue( false );
			}
		};

		new ChoiceAdventure( 673, "Putting Off Is Off-Putting", "The Castle in the Clouds in the Sky (Ground Floor)" )
		{
			void setup()
			{
				this.customName = "Ground Procrastination";

				new Option( 1, "very overdue library book, then skip adventure", true )
					.turnCost( 1 )
					.attachItem( "very overdue library book", 1, AUTO );
				new Option( 2, "30 adv of Trash-Wrapped", true )
					.turnCost( 1 )
					.attachEffect( "Trash-Wrapped" );
				new Option( 3, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// You crawl under the pile of junk, but after you almost
				// crawl into a giant pair of filthy boxer shorts, you decide to quit looking.
				if ( decision == 1 && request.responseText.contains( "you decide to quit looking" ) )
				{
					this.choiceFailed().entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 674, "Huzzah!", "The Castle in the Clouds in the Sky (Ground Floor)" )
		{
			void setup()
			{
				this.customName = "Ground Renaissance";

				new Option( 1, "pewter claymore, then skip adventure", true )
					.turnCost( 1 )
					.attachItem( "pewter claymore", 1, AUTO );
				new Option( 2, "30 adv of Pretending to Pretend", true )
					.turnCost( 1 )
					.attachEffect( "Pretending to Pretend" );
				new Option( 3, "skip adventure", true )
					.entersQueue( false );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				// You climb onto the writing desk. There are a few raven feathers
				// that have been sharpened to use as quills, as well as
				// an assortment of ballpoint pens. They're all too big to be
				// of any use to you, so you hop back down. Forsooth, that was a waste of time.
				if ( decision == 1 && request.responseText.contains( "Forsooth, that was a waste of time" ) )
				{
					this.choiceFailed().entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 675, "Melon Collie and the Infinite Lameness", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			final AdventureResult DRUM_N_BASS = new AdventureResult( "drum 'n' bass 'n' drum 'n' bass record", -1, false );

			void setup()
			{
				this.customName = "Top Goth";

				new Option( 1, "fight Goth Giant", true );
				new Option( 2, "with drum'n'bass record, complete quest" )
					.attachItem( DRUM_N_BASS, MANUAL, new DisplayAll( NEED, AT_LEAST, 1 ) );
				new Option( 3, "3 thin black candles", true )
					.turnCost( 1 )
					.attachItem( ItemPool.BLACK_CANDLE, 3, AUTO );
				new Option( 4, "Steampunk Choice", true )
					.leadsTo( 677, true );

				new CustomOption( 5, "with drum'n'bass record, complete quest, otherwise fight goth giant", 2 );
				new CustomOption( 6, "with drum'n'bass record, complete quest, otherwise candles", 5 );
				new CustomOption( 7, "with drum'n'bass record, complete quest, otherwise Steampunk Choice", 6 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 2 );
				if ( DRUM_N_BASS.getCount( KoLConstants.inventory ) > 0 &&
				     QuestDatabase.isQuestBefore( Quest.GARBAGE, "step10" ) )
				{
					option.leadsTo( 679 );
				}
				else
				{
					option.turnCost( 1 );
				}
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				boolean canComplete = DRUM_N_BASS.getCount( KoLConstants.inventory ) > 0 &&
					QuestDatabase.isQuestBefore( Quest.GARBAGE, "step10" );

				switch ( decision )
				{
				case "2":
					return canComplete ? "2" : "1";
				case "5":
					return canComplete ? "2" : "3";
				case "6":
					return canComplete ? "2" : "4";
				}
				return decision;
			}
		};

		new ChoiceAdventure( 676, "Flavor of a Raver", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			void setup()
			{
				this.customName = "Top Raver";

				new Option( 1, "fight Raver Giant", true );
				new Option( 2, "Restore 1000 hp & mp", true )
					.turnCost( 1 )
					.attachMP( 1000 );
				new Option( 3, "drum 'n' bass 'n' drum 'n' bass record, then skip adventure", true )
					.turnCost( 1 )
					.attachItem( "drum 'n' bass 'n' drum 'n' bass record", 1, AUTO );
				new Option( 4, "Punk Rock Choice", true )
					.leadsTo( 678, true );
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 3 && !request.responseText.contains( "You acquire" ) )
				{
					this.choiceFailed().entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 677, "Copper Feel", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			final AdventureResult MODEL_AIRSHIP = ItemPool.get( ItemPool.MODEL_AIRSHIP, -1 );

			void setup()
			{
				this.customName = "Top Steampunk";

				new Option( 1, "With model airship, complete quest, otherwise fight Steampunk Giant", true );
				new Option( 2, "steam-powered model rocketship, then skip adventure", true )
					.turnCost( 1 )
					.attachItem( "steam-powered model rocketship", 1, AUTO );
				new Option( 3, null, true )
					.turnCost( 1 )
					.attachItem( "brass gear", 1, AUTO );
				new Option( 4, "Goth Choice", true )
					.leadsTo( 675, true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 1 );
				boolean haveAirship = MODEL_AIRSHIP.getCount( KoLConstants.inventory ) > 0;
				boolean questNotFinished = QuestDatabase.isQuestBefore( Quest.GARBAGE, "step10" );

				if ( questNotFinished )
				{
					if ( haveAirship )
					{
						option.leadsTo( 679 )
							.attachItem( MODEL_AIRSHIP, MANUAL, new DisplayAll( "airship" ) );
					}
					else
					{
						option.attachItem( MODEL_AIRSHIP, new DisplayAll( "airship" ) );
					}
				}
				else
				{
					option.text( "fight Steampunk Giant" );

					if ( haveAirship )
					{
						// they waste a turn and lose the airship
						option.turnCost( 1 )
							.attachItem( MODEL_AIRSHIP, MANUAL, new DisplayAll( NEED, EXACTLY, 0 ) );
					}
					else
					{
						option.attachItem( MODEL_AIRSHIP, new DisplayAll( NEED, EXACTLY, 0 ) );
					}
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( decision == 2 && !request.responseText.contains( "You acquire" ) )
				{
					this.choiceFailed().entersQueue( false );
				}
			}
		};

		new ChoiceAdventure( 678, "Yeah, You're for Me, Punk Rock Giant", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			final AdventureResult MOHAWK_WIG = ItemPool.get( ItemPool.MOHAWK_WIG );

			void setup()
			{
				this.customName = "Top Punk Rock";

				new Option( 1, "Wearing mohawk wig, complete quest, otherwise fight Punk Rock Giant", true );
				new Option( 2, "500 meat", true )
					.turnCost( 1 )
					.attachMeat( 500, AUTO );
				new Option( 3, "Steampunk Choice", true )
					.leadsTo( 677, true );
				new Option( 4, "Raver Choice", true )
					.leadsTo( 676 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				//lookaheadsafe

				Option option = getOption( 1 );
				if ( QuestDatabase.isQuestBefore( Quest.GARBAGE, "step10" ) )
				{
					option.attachItem( MOHAWK_WIG, new DisplayAll( "hawk wig", WANT, EQUIPPED_AT_LEAST, 1 ) );

					if ( KoLCharacter.hasEquipped( MOHAWK_WIG ) )
					{
						option.leadsTo( 679 );
					}
				}
				else
				{
					option.text( "fight Punk Rock Giant" );
				}
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// Option 3 isn't always available, but decision to take isn't clear if it's selected, so show in browser
				if ( decision.equals( "3" ) && !responseText.contains( "Check behind the trash can" ) )
				{
					return "0";
				}
				return decision;
			}
		};

		new ChoiceAdventure( 679, "Keep On Turnin' the Wheel in the Sky", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				QuestDatabase.setQuestProgress( Quest.GARBAGE, "step10" );
			}
		};

		new ChoiceAdventure( 680, "Are you a Man or a Mouse?", "The Castle in the Clouds in the Sky (Top Floor)" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 )
					.leadsTo( 679 );
			}
		};

		new ChoiceAdventure( 681, "F-F-Fantastic!", "The Penultimate Fantasy Airship" )
		{
			void setup()
			{
				new Option( 1 )
					.turnCost( 1 )
					.attachItem( ItemPool.TISSUE_PAPER_IMMATERIA, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.TIN_FOIL_IMMATERIA, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.GAUZE_IMMATERIA, -1, MANUAL, new NoDisplay() )
					.attachItem( ItemPool.PLASTIC_WRAP_IMMATERIA, -1, MANUAL, new NoDisplay() );
			}
		};

		new ChoiceAdventure( 682, "Now Leaving Jarlsberg, Population You", null )
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
			void postChoice2( GenericRequest request, int decision )
			{
				AvatarManager.handleAfterAvatar( decision );
			}
		};

		new UnknownChoiceAdventure( 683 );

		new ChoiceAdventure( 684, "There's a Wizard in My Gizzard", null )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 685 );

		new ChoiceAdventure( 686, "Of Might and Magic", "The Naughty Sorceress' Chamber" )
		{
			void setup()
			{
				new Option( 1 );
			}
		};

		new UnknownChoiceAdventure( 687 );

		new UnknownChoiceAdventure( 688 );

		new ChoiceAdventure( 689, "The Final Reward", "The Daily Dungeon" )
		{
			void setup()
			{
				this.customName = "Daily Dungeon: Chest 3";

				new Option( 1, "Get fat loot token", true )
					.turnCost( 1 )
					.attachItem( ItemPool.FAT_LOOT_TOKEN, 1, AUTO );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Preferences.setInteger( "_lastDailyDungeonRoom", 14 );
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "claim your rightful reward" ) )
				{
					// Daily Dungeon Complete
					Preferences.setBoolean( "dailyDungeonDone", true );
					Preferences.setInteger( "_lastDailyDungeonRoom", 15 );
				}
			}
		};

		new ChoiceAdventure( 690, "The First Chest Isn't the Deepest.", "The Daily Dungeon" )
		{
			void setup()
			{
				this.customName = "Daily Dungeon: Chest 1";

				new Option( 1, "Get item", true )
					.turnCost( 1 );
				new Option( 2, "Skip to 8th chamber, no turn spent", true );
				new Option( 3, "Skip to 6th chamber, no turn spent", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Preferences.setInteger( "_lastDailyDungeonRoom", 4 );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// *** The first chest in the daily dungeon.

				if ( KoLCharacter.hasEquipped( ItemPool.RING_OF_DETECT_BORING_DOORS ) )
				{
					return "2";
				}
				else if ( decision == "2" )
				{
					// they wanted the boring door, but it's unavailable
					return "3";
				}

				return decision;
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 )
				{
					Preferences.increment( "_lastDailyDungeonRoom", 3 );
				}
				else
				{
					Preferences.increment( "_lastDailyDungeonRoom", 1 );
				}
			}
		};

		new ChoiceAdventure( 691, "Second Chest", "The Daily Dungeon" )
		{
			void setup()
			{
				this.customName = "Daily Dungeon: Chest 2";

				new Option( 1, "Get item", true )
					.turnCost( 1 );
				new Option( 2, "Skip to 13th chamber, no turn spent", true );
				new Option( 3, "Skip to 11th chamber, no turn spent", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Preferences.setInteger( "_lastDailyDungeonRoom", 9 );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// *** The second chest in the daily dungeon.

				if ( KoLCharacter.hasEquipped( ItemPool.RING_OF_DETECT_BORING_DOORS ) )
				{
					return "2";
				}
				else if ( decision == "2" )
				{
					// they wanted the boring door, but it's unavailable
					return "3";
				}

				return decision;
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 2 )
				{
					Preferences.increment( "_lastDailyDungeonRoom", 3 );
				}
				else
				{
					Preferences.increment( "_lastDailyDungeonRoom", 1 );
				}
			}
		};

		new ChoiceAdventure( 692, "I Wanna Be a Door", "The Daily Dungeon" )
		{
			final Pattern CHAMBER_PATTERN = Pattern.compile( "Chamber <b>#(\\d+)</b>" );

			void setup()
			{
				this.customName = "Daily Dungeon: Doors";

				new Option( 1, "suffer trap effects", true )
					.turnCost( 1 );
				new Option( 2, "unlock door with key (may break), no turn spent" )
					.attachItem( ItemPool.SKELETON_KEY, new DisplayAll( "with key", NEED, AT_LEAST, 1 ) );
				new Option( 3, "pick lock with lockpicks, no turn spent" );
				new Option( 4 )
					.turnCost( 1 );
				new Option( 5 )
					.turnCost( 1 );
				new Option( 6 )
					.turnCost( 1 );
				new Option( 7, "open door with card, no turn spent" );
				new Option( 8, "leave, no turn spent" )
					.entersQueue( false );

				new CustomOption( 11, "unlock door using PYEC, lockpicks, or skeleton key" );
				new CustomOption( 12, "try to avoid trap using highest buffed stat" );

				this.customLoad = () -> {
					int index = Preferences.getInteger( "choiceAdventure692" );
					switch ( index )
					{
					default:
						System.out.println( "Invalid setting " + index + " for choiceAdventure692" );
						// fall-through
					case 0:
						return 0;
					case 1:
						return 1;
					case 2:
					case 3:
					case 7:
					case 11:
						return 11;
					case 4:
					case 5:
					case 6:
					case 12:
						return 12;
					case 8:
						return Integer.MAX_VALUE; // "valid but hidden"
					}
				};
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Matcher chamberMatcher = CHAMBER_PATTERN.matcher( request.responseText );
				if ( chamberMatcher.find() )
				{
					int round = StringUtilities.parseInt( chamberMatcher.group( 1 ) );
					Preferences.setInteger( "_lastDailyDungeonRoom", round - 1 );
				}

				getOption( 4 ).text( KoLCharacter.getAdjustedMuscle() >= 30 ?
					"bypass trap with muscle" :
					"suffer trap effects" );
				getOption( 5 ).text( KoLCharacter.getAdjustedMysticality() >= 30 ?
					"bypass trap with mysticality" :
					"suffer trap effects" );
				getOption( 6 ).text( KoLCharacter.getAdjustedMoxie() >= 30 ?
					"bypass trap with moxie" :
					"suffer trap effects" );
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// *** This is the locked door in the daily dungeon.

				// If you have a Platinum Yendorian Express Card, use it.
				// Otherwise, if you have pick-o-matic lockpicks, use them
				// Otherwise, if you have a skeleton key, use it.

				if ( decision.equals( "11" ) )
				{
					if ( InventoryManager.getCount( ItemPool.EXPRESS_CARD ) > 0 )
					{
						return "7";
					}
					if ( InventoryManager.getCount( ItemPool.PICKOMATIC_LOCKPICKS ) > 0 )
					{
						return "3";
					}
					if ( InventoryManager.getCount( ItemPool.SKELETON_KEY ) > 0 )
					{
						return "2";
					}
					// Cannot unlock door
					return "0";
				}

				// Use highest stat to try to pass door
				if ( decision.equals( "12" ) )
				{
					int buffedMuscle = KoLCharacter.getAdjustedMuscle();
					int buffedMysticality = KoLCharacter.getAdjustedMysticality();
					int buffedMoxie = KoLCharacter.getAdjustedMoxie();

					if ( buffedMuscle >= buffedMysticality && buffedMuscle >= buffedMoxie )
					{
						return "4";
					}
					if ( buffedMysticality >= buffedMuscle && buffedMysticality >= buffedMoxie )
					{
						return "5";
					}
					return "6";
				}
				return decision;
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "key breaks off in the lock" ) )
				{
					// Unfortunately, the key breaks off in the lock.
					getOption( decision ).attachItem( ItemPool.SKELETON_KEY, -1, MANUAL );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision != 8 )
				{
					Preferences.increment( "_lastDailyDungeonRoom", 1 );
				}
			}
		};

		new ChoiceAdventure( 693, "It's Almost Certainly a Trap", "The Daily Dungeon" )
		{
			final Pattern CHAMBER_PATTERN = Pattern.compile( "Chamber <b>#(\\d+)</b>" );
			final AdventureResult ELEVEN_FOOT_POLE = ItemPool.get( ItemPool.ELEVEN_FOOT_POLE );

			void setup()
			{
				this.customName = "Daily Dungeon: Traps";

				new Option( 1, "Suffer elemental damage, get stats", true )
					.turnCost( 1 );
				new Option( 2, "Avoid trap with eleven-foot pole, no turn spent" );
				new Option( 3, "Leave, no turn spent", true );

				new CustomOption( 4, "with eleven-foot pole, avoid trap for free, otherwise brute force it" );
				new CustomOption( 5, "with eleven-foot pole, avoid trap for free, otherwise leave", 2 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				Matcher chamberMatcher = CHAMBER_PATTERN.matcher( request.responseText );
				if ( chamberMatcher.find() )
				{
					int round = StringUtilities.parseInt( chamberMatcher.group( 1 ) );
					Preferences.setInteger( "_lastDailyDungeonRoom", round - 1 );
				}
			}

			@Override
			String getDecision( String responseText, String decision, int stepCount )
			{
				// *** This is a trap in the daily dungeon.

				boolean hasPole = ELEVEN_FOOT_POLE.getCount( KoLConstants.inventory ) > 0;

				if ( decision == "2" )
				{
					return hasPole ? "2" : "3";
				}
				if ( decision == "4" )
				{
					return hasPole ? "2" : "1";
				}

				return decision;
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision != 3 )
				{
					Preferences.increment( "_lastDailyDungeonRoom", 1 );
				}
			}
		};

		new ChoiceAdventure( 694, "Deeps Impact", "The Briny Deeps" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 695, "A Drawer of Chests", "The Mer-Kin Outpost" )
		{
			void setup()
			{
				this.isSuperlikely = true;

				new Option( 1 )
					.turnCost( 1 );
			}
		};

		new ChoiceAdventure( 696, "Stick a Fork In It", "The Edge of the Swamp" )
		{
			void setup()
			{
				new Option( 1, "unlock Dark and Spooky Swamp", true );
				new Option( 2, "unlock The Wildlife Sanctuarrrrrgh", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( Preferences.getBoolean( "maraisDarkUnlock" ) )
				{
					getOption( 1 ).text( "Dark and Spooky Swamp already unlocked" );
				}

				if ( Preferences.getBoolean( "maraisWildlifeUnlock" ) )
				{
					getOption( 2 ).text( "The Wildlife Sanctuarrrrrgh already unlocked" );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					Preferences.setBoolean( "maraisDarkUnlock", true );
				}
				else if ( decision == 2 )
				{
					Preferences.setBoolean( "maraisWildlifeUnlock", true );
				}
			}
		};

		new ChoiceAdventure( 697, "Sophie's Choice", "The Dark and Spooky Swamp" )
		{
			void setup()
			{
				new Option( 1, "unlock The Corpse Bog", true );
				new Option( 2, "unlock The Ruined Wizard Tower", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( Preferences.getBoolean( "maraisCorpseUnlock" ) )
				{
					getOption( 1 ).text( "The Corpse Bog already unlocked" );
				}

				if ( Preferences.getBoolean( "maraisWizardUnlock" ) )
				{
					getOption( 2 ).text( "The Ruined Wizard Tower already unlocked" );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					Preferences.setBoolean( "maraisCorpseUnlock", true );
				}
				else if ( decision == 2 )
				{
					Preferences.setBoolean( "maraisWizardUnlock", true );
				}
			}
		};

		new ChoiceAdventure( 698, "From Bad to Worst", "The Wildlife Sanctuarrrrrgh" )
		{
			void setup()
			{
				new Option( 1, "unlock Swamp Beaver Territory", true );
				new Option( 2, "unlock The Weird Swamp Village", true );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( Preferences.getBoolean( "maraisBeaverUnlock" ) )
				{
					getOption( 1 ).text( "Swamp Beaver Territory already unlocked" );
				}

				if ( Preferences.getBoolean( "maraisVillageUnlock" ) )
				{
					getOption( 2 ).text( "The Weird Swamp Village already unlocked" );
				}
			}

			@Override
			void postChoice2( GenericRequest request, int decision )
			{
				if ( decision == 1 )
				{
					Preferences.setBoolean( "maraisBeaverUnlock", true );
				}
				else if ( decision == 2 )
				{
					Preferences.setBoolean( "maraisVillageUnlock", true );
				}
			}
		};

		new ChoiceAdventure( 699, "Lumber-related Pun", null )
		{
			void setup()
			{
				// choices here are weird.
				// if you have nothing to hand, your option is "um...", as option 1
				// otherwise, the branch, the axe, the roses and "Not yet, sorry."
				// are DYNAMICALLY assigned to option 1, 2, 3 and 4, in order.
				new Option( 1 );
				new Option( 2 );
				new Option( 3 );
				new Option( 4 );
			}

			@Override
			void visitChoice( GenericRequest request )
			{
				if ( request != null && request.responseText.contains( "Pick the sword" ) )
				{
					// he's offering us the rewards
					getOption( 1 ).attachItem( ItemPool.HAND_CARVED_BOKKEN, 1, AUTO );
					getOption( 2 ).attachItem( ItemPool.HAND_CARVED_BOW, 1, AUTO );
					getOption( 3 ).attachItem( ItemPool.HAND_CARVED_STAFF, 1, AUTO );
				}
			}

			@Override
			void postChoice1( GenericRequest request, int decision )
			{
				if ( request.responseText.contains( "hand him the branch" ) )
				{
					// Marty's eyes widen when you hand him the
					// branch from the Great Tree.
					getOption( decision ).attachItem( ItemPool.GREAT_TREE_BRANCH, -1, MANUAL );
				}
				else if ( request.responseText.contains( "hand him the rust" ) )
				{
					// At first Marty looks disappointed when you
					// hand him the rust-spotted, rotten-handled
					// axe, but after a closer inspection he gives
					// an impressed whistle.
					getOption( decision ).attachItem( ItemPool.PHIL_BUNION_AXE, -1, MANUAL );
				}
				else if ( request.responseText.contains( "hand him the bouquet" ) )
				{
					// Marty looks delighted when you hand him the
					// bouquet of swamp roses.
					getOption( decision ).attachItem( ItemPool.SWAMP_ROSE_BOUQUET, -1, MANUAL );
				}
			}
		};
	}
}
