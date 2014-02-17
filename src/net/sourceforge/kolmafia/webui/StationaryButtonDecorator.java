/**
 * Copyright (c) 2005-2014, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia" nor the names of its contributors may
 *      be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia.webui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;

import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.objectpool.SkillPool;

import net.sourceforge.kolmafia.persistence.SkillDatabase;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.request.FightRequest;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.request.UseSkillRequest;

import net.sourceforge.kolmafia.session.ChoiceManager;
import net.sourceforge.kolmafia.session.EquipmentManager;

import net.sourceforge.kolmafia.textui.command.ChoiceCommand;

import net.sourceforge.kolmafia.utilities.StringUtilities;

public class StationaryButtonDecorator
{
	private static final ArrayList<String> combatHotkeys = new ArrayList<String>();

	private static final boolean builtInSkill( final String skillId )
	{
		if ( skillId.equals( String.valueOf( SkillDatabase.getSkillId( KoLCharacter.getClassStun() ) ) ) )
		{
			return true;
		}

		if ( skillId.equals( String.valueOf( SkillPool.OLFACTION ) ) )
		{
			return true;
		}

		return false;
	}

	public static final void addSkillButton( final String skillId )
	{
		if ( skillId == null || skillId.equals( "none" ) )
		{
			return;
		}

		// Don't add a button for using a built-in skill
		if ( StationaryButtonDecorator.builtInSkill( skillId ) )
		{
			return;
		}

		int buttons = Preferences.getInteger( "relaySkillButtonCount" );
		int maximumIndex = buttons + 1;
		int insertIndex = 0;

		// Examine all buttons and find a place for this skill.
		for ( int i = 1; i < maximumIndex; )
		{
			String old = Preferences.getString( "stationaryButton" + i );
			// Remove built-in skills.
			if ( StationaryButtonDecorator.builtInSkill( old ) )
			{
				StationaryButtonDecorator.removeSkill( i, buttons );
				continue;
			}

			// If the button is already there, use it.
			if ( old.equals( skillId ) )
			{
				insertIndex = i;
			}

			// If we already have an insertion point, keep it
			else if ( insertIndex != 0 )
			{
			}

			// Choose first unused button.
			else if ( old.equals( "" ) || old.equals( "none" ) )
			{
				insertIndex = i;
			}
			++i;
		}

		// If all buttons are in use, remove oldest and insert at end.
		if ( insertIndex == 0 )
		{
			StationaryButtonDecorator.removeSkill( 1, buttons );
			insertIndex = buttons;
		}

		Preferences.setString( "stationaryButton" + insertIndex, skillId );
	}

	private static final void removeSkill( final int index, int buttons )
	{
		for ( int i = index ; i <= buttons; ++i )
		{
			String next = Preferences.getString( "stationaryButton" + ( i+1 ) );
			Preferences.setString( "stationaryButton" + i, next );
		}
	}

	public static final void removeBuiltInSkills()
	{
		int buttons = Preferences.getInteger( "relaySkillButtonCount" );
		int maximumIndex = buttons + 1;
		
		// Examine all buttons and find a place for this skill.
		for ( int i = 1; i < maximumIndex; )
		{
			String old = Preferences.getString( "stationaryButton" + i );

			// Remove built-in skills.
			if ( StationaryButtonDecorator.builtInSkill( old ) )
			{
				StationaryButtonDecorator.removeSkill( i, buttons );
				continue;
			}
			i++;
		}
	}
	
	public static final void decorate( final String urlString, final StringBuffer buffer )
	{
		if ( Preferences.getBoolean( "hideServerDebugText" ) )
		{
			int beginDebug = buffer.indexOf( "<div style='max-height" );
			if ( beginDebug != -1 )
			{
				int endDebug = buffer.indexOf( "</div>", beginDebug ) + 6;
				buffer.delete( beginDebug, endDebug );
			}
		}

		if ( Preferences.getBoolean( "serverAddsCustomCombat" ) )
		{
			int imageIndex = buffer.indexOf( "<td><img src='http://images.kingdomofloathing.com/itemimages/book3.gif' id='skills'>" );
			if ( imageIndex != -1 )
			{
				boolean again = FightRequest.getCurrentRound() == 0;
				String location = again ? getAdventureAgainLocation( buffer ) : "fight.php?action=custom";

				// Add a "script" button to the left

				buffer.insert( imageIndex, "<td><a href='" + location + "'><img src='http://images.kingdomofloathing.com/itemimages/plexpock.gif'></td><td class=spacer></td>" );

				// Give it either the "script" or "again" label
				int labelIndex = buffer.indexOf( "<tr class=label>", imageIndex ) + 16;

				buffer.insert( labelIndex, again ? "<td>again</td><td></td>" : "<td>script</td><td></td>" );

				// Also add spacers to the header
				labelIndex = buffer.indexOf( "<tbody><tr class=label><td></td><td></td><td>1</td><td>2</td>" ) + 23;
				buffer.insert( labelIndex, "<td></td><td></td>" );
			}
			else
			{
				// We are going to craft our own CAB. Pull in
				// the necessary Javascript.

				int insertIndex = buffer.indexOf( "</head>" );
				buffer.insert( insertIndex, "<link rel='stylesheet' type='text/css' href='http://images.kingdomofloathing.com/actionbar.6.css'><!--[if IE]><link rel='stylesheet' type='text/css' href='http://images.kingdomofloathing.com/actionbar.ie.4.css'><![endif]-->" );

				// Build the CAB in a new StringBuilder

				StringBuilder CAB = new StringBuilder();
				boolean choice = buffer.indexOf( "<input" ) != -1;

				CAB.append( "<img src='http://images.kingdomofloathing.com/itemimages/blank.gif' id='dragged'>" );
				CAB.append( "<div id='debug'></div>" );
				CAB.append( "<div class=contextmenu id='skillmenu'></div>" );
				CAB.append( "<div class=contextmenu id='itemsmenu'></div>" );

				// *** Start of 'topbar' div
				CAB.append( "<div id=topbar>" );
				CAB.append( "<center><table class=actionbar cellpadding=0 cellspacing=1><tbody>" );

				// *** Row 1 of table: class=label cols=19
				CAB.append( "<tr class=label>" );
				//     Column 1
				CAB.append( "<td>&nbsp;</td>" );
				//     Column 2-19
				for ( int i = 2; i <= 19; ++i )
				{
					CAB.append( "<td></td>" );
				}
				CAB.append( "</tr>" );

				// *** Row 2 of table: class=blueback cols=19
				CAB.append( "<tr class=blueback>" );
				//     Column 1
				CAB.append( "<td><a href='" );
				CAB.append( choice ? "choice.php?action=auto" : getAdventureAgainLocation( buffer ) );
				CAB.append( "'><img src='http://images.kingdomofloathing.com/itemimages/plexpock.gif'></td>" );
				//     Column 2
				CAB.append( "<td class=spacer></td>" );
				//     Column 3
				CAB.append( "<td><img src='http://images.kingdomofloathing.com/itemimages/blank.gif' id='skills'></td>" );
				//     Column 4
				CAB.append( "<td class=spacer></td>" );
				//     Column 5-16
				for ( int i = 5; i <= 16; ++i )
				{
					CAB.append( "<td><img src='http://images.kingdomofloathing.com/itemimages/blank.gif'></td>" );
				}
				//     Column 17
				CAB.append( "<td class=spacer></td>" );
				//     Column 18
				CAB.append( "<td class=spacer></td>" );
				//     Column 19
				CAB.append( "<td><img src='http://images.kingdomofloathing.com/itemimages/blank.gif'></td>" );
				CAB.append( "</tr>" );

				// *** Row 3 of table: class=label cols=19
				CAB.append( "<tr class=label>" );
				//	Column 1
				CAB.append( "<td>" );
				CAB.append( choice ? "auto" : "again" );
				CAB.append( "</td>" );
				//	Column 2-19
				for ( int i = 2; i < 19; ++i )
				{
					CAB.append( "<td></td>" );
				}
				CAB.append( "</tr>" );
				CAB.append( "</tbody></table></center>" );

				CAB.append( "</div>" );
				// *** End of 'topbar' div

				// *** Start of 'content' div
				CAB.append( "<div class='content' id='content_'>" );
				CAB.append( "<div id='effdiv' style='display: none;'></div>" );

				// *** Start of 'overflow' div
				CAB.append( "<div style='overflow: auto;'>" );

				insertIndex = buffer.indexOf( "<body>" ) + 6;
				buffer.insert( insertIndex, CAB.toString() );

				
				insertIndex = buffer.indexOf( "</body>" );
				if ( insertIndex > -1 )
				{
					buffer.insert( insertIndex, "</div></div>" );
				}
				else
				{
					buffer.append( "</div></div>" );
				}
				// *** End of 'overflow' div
				// *** End of 'content' div
			}

			return;
		}

		if ( !Preferences.getBoolean( "relayAddsCustomCombat" ) )
		{
			return;
		}

		StationaryButtonDecorator.removeBuiltInSkills();

		// Add stylesheet that controls header/page content when stationary buttons used
		int insertionPoint = buffer.indexOf( "</head>" );
		if ( insertionPoint == -1 )
		{
			return;
		}

		buffer.insert( insertionPoint, "<link rel=\"stylesheet\" type=\"text/css\" href=\"/" + KoLConstants.STATIONARYBUTTONS_CSS + "\">" );
		buffer.insert( insertionPoint, "<script src=\"http://code.jquery.com/jquery-1.9.1.js\"></script><script src=\"/" + KoLConstants.STATIONARYBUTTONS_JS + "\"></script>" );
		
		insertionPoint = buffer.indexOf( "<body" );
		if ( insertionPoint == -1 )
		{
			return;
		}
		insertionPoint = buffer.indexOf( ">", insertionPoint ) + 1;

		StringBuffer actionBuffer = new StringBuffer();

		// *** Start of 'page' div
		actionBuffer.append( "<div id=\"page\">" );
		
		// *** Start of 'mafiabuttons' div
		actionBuffer.append( "<div id=\"mafiabuttons\">" );
		actionBuffer.append( "<center><table width=\"95%\"><tr><td align=left>" );

		// *** Start of 'btnwrap' div
		actionBuffer.append( "<div id=\"btnwrap\">" );

		boolean inCombat = urlString.startsWith( "fight.php" ) && FightRequest.getCurrentRound() > 0;
		boolean inChoice = urlString.startsWith( "choice.php" ) && buffer.indexOf( "choice.php", buffer.indexOf( "<body>" ) + 1 ) != -1;

		// You can have either hot keys or the macro editor, since the
		// former make it impossible to type numbers in the macro field
		boolean useHotKeys = !Preferences.getBoolean( "macroLens" );

		if ( inCombat )
		{
			StationaryButtonDecorator.addCombatButtons( urlString, actionBuffer );
		}
		else if ( inChoice )
		{
			StationaryButtonDecorator.addChoiceButtons( actionBuffer );
		}
		else
		{
			StationaryButtonDecorator.addNonCombatButtons( buffer, actionBuffer );
		}

		actionBuffer.append( "</div>" );
		// *** End of 'btnwrap' div

		actionBuffer.append( "</td>" );

		// If you are either in combat or finished with one, give the
		// user the opportunity to update hot keys.
		if ( useHotKeys && !inChoice )
		{
			actionBuffer.append( "<td align=right valign=top>" );
			actionBuffer.append( "<select id=\"hotkeyViewer\" onchange=\"updateCombatHotkey();\">" );

			actionBuffer.append( "<option>- update hotkeys -</option>" );

			for ( int i = 0; i < StationaryButtonDecorator.combatHotkeys.size(); ++i )
			{
				actionBuffer.append( "<option>" );
				actionBuffer.append( i );
				actionBuffer.append( ": " );

				actionBuffer.append( StationaryButtonDecorator.combatHotkeys.get( i ) );
				actionBuffer.append( "</option>" );
			}

			actionBuffer.append( "</select>" );
			actionBuffer.append( "</td>" );
		}

		actionBuffer.append( "</tr></table></center>" );

		actionBuffer.append( "</div>" );
		// *** End of 'mafiabuttons' div

		// *** Start of 'content_' div
		actionBuffer.append( "<div class='content' id='content_'>" );
		actionBuffer.append( "<div id='effdiv' style='display: none;'></div>" );

		// *** Start of 'extra' div
		actionBuffer.append( "<div>" );

		buffer.insert( insertionPoint, actionBuffer.toString() );

		StringUtilities.insertBefore( buffer, "</body>", "</div>" );
		// *** End of 'extra' div

		StringUtilities.insertBefore( buffer, "</body>", "</div>" );
		// *** End of 'content_' div
		
		StringUtilities.insertBefore( buffer, "</body>", "</div>" );
		// *** End of 'page' div

		if ( useHotKeys )
		{
			StringUtilities.insertBefore( buffer, "</head>", "<script src=\"/" + KoLConstants.HOTKEYS_JS + "\"></script>" );
			StringUtilities.insertAfter( buffer, "<body", " onkeyup=\"handleCombatHotkey(event,false);\" onkeydown=\"handleCombatHotkey(event,true);\" " );
		}

	}

	public static final void addCombatButtons( final String urlString, final StringBuffer actionBuffer )
	{
		if ( Preferences.getBoolean( "relayScriptButtonFirst" ) )
		{
			StationaryButtonDecorator.addScriptButton( urlString, actionBuffer, true );
			StationaryButtonDecorator.addFightButton( actionBuffer, "attack", FightRequest.getCurrentRound() > 0 );
		}
		else
		{
			StationaryButtonDecorator.addFightButton( actionBuffer, "attack", FightRequest.getCurrentRound() > 0 );
			StationaryButtonDecorator.addScriptButton( urlString, actionBuffer, false );
		}

		boolean inBirdForm = KoLConstants.activeEffects.contains( FightRequest.BIRDFORM );
		if ( KoLCharacter.isSneakyPete() )
		{
			// If you are Sneaky Pete and can steal, you can also mug
			StationaryButtonDecorator.addFightButton( actionBuffer, "7201", FightRequest.canStillSteal() );
		}

		if ( KoLCharacter.canPickpocket() )
		{
			StationaryButtonDecorator.addFightButton( actionBuffer, "steal", FightRequest.canStillSteal() );
		}

		if ( EquipmentManager.usingChefstaff() )
		{
			boolean enabled = FightRequest.getCurrentRound() > 0;
			StationaryButtonDecorator.addFightButton( actionBuffer, "jiggle", enabled );
		}

		String classStun = KoLCharacter.getClassStun();
		// Some skills can be available in combat but aren't always stuns. Disable if so or change to Shadow Noodles if appropriate.
		if ( classStun.equals( "Shell Up" ) && KoLCharacter.getBlessingType() != KoLCharacter.STORM_BLESSING )
		{
			classStun = Preferences.getBoolean( "considerShadowNoodles" ) ? "Shadow Noodles" : "none";
		}

		int classStunId = SkillDatabase.getSkillId( classStun );
		if ( !inBirdForm && KoLCharacter.hasSkill( classStun ) )
		{
			UseSkillRequest stunRequest = UseSkillRequest.getInstance( classStun );
			boolean enabled = FightRequest.getCurrentRound() > 0 &&
				KoLConstants.availableCombatSkills.contains( stunRequest );
			// Only enable Club Foot when character has Fury, as it's only a stun then.
			enabled &= !( classStun.equals( "Club Foot" ) && KoLCharacter.getFury() == 0 );
			// Only enable Soul Bubble when character has 5 Soulsauce, as it's only a stun then.
			enabled &= !( classStun.equals( "Soul Bubble" ) && KoLCharacter.getSoulsauce() < 5 );
			// In Class Act 2, Stuns don't work at +76 and higher monster level
			enabled &= !( KoLCharacter.inClasscore2() && KoLCharacter.getMonsterLevelAdjustment() > 75 );
			StationaryButtonDecorator.addFightButton(actionBuffer, String.valueOf( classStunId ), enabled );
		}

		if ( !inBirdForm && KoLCharacter.hasSkill( "Transcendent Olfaction" ) )
		{
			boolean enabled = FightRequest.getCurrentRound() > 0 &&
				FightRequest.canOlfact();
			StationaryButtonDecorator.addFightButton( actionBuffer, "19", enabled );
		}

		if ( !inBirdForm && FightRequest.canPirateInsult() )
		{
			boolean enabled = FightRequest.getCurrentRound() > 0;
			StationaryButtonDecorator.addFightButton( actionBuffer, "insult", enabled );
		}

		if ( !inBirdForm && FightRequest.canJamFlyer() )
		{
			boolean enabled = FightRequest.getCurrentRound() > 0;
			StationaryButtonDecorator.addFightButton( actionBuffer, "jam flyer", enabled );
		}

		if ( !inBirdForm && FightRequest.canRockFlyer() )
		{
			boolean enabled = FightRequest.getCurrentRound() > 0;
			StationaryButtonDecorator.addFightButton( actionBuffer, "rock flyer", enabled );
		}

		int buttons = Preferences.getInteger( "relaySkillButtonCount" );
		for ( int i = 1; i <= buttons; ++i )
		{
			String action = Preferences.getString( "stationaryButton" + i );
			if ( action.equals( "" ) || action.equals( "none" ) )
			{
				continue;
			}

			// We use Skill IDs for button actions, but users can screw them up.
			if ( !StringUtilities.isNumeric( action ) )
			{
				action = String.valueOf( SkillDatabase.getSkillId( action ) );
			}

			String name = SkillDatabase.getSkillName( Integer.parseInt( action ) );
			boolean hasSkill = name != null && KoLCharacter.hasSkill( name );

			boolean remove = false;

			// If it's a completely bogus skill id, flush it
			if ( name == null )
			{
				remove = true;
			}
			// If we are in bird form, we can only use birdform skills.
			else if ( inBirdForm )
			{
				// Birdform skills do not appear on our list of
				// known skills. Display only unknown skills
				// but keep known skills in the preferences
				if ( hasSkill )
				{
					continue;
				}
			}
			// Otherwise, remove unknown skills from preferences
			else if ( !hasSkill )
			{
				remove = true;
			}

			if ( remove )
			{
				for ( int j = i; j < buttons; ++j )
				{
					Preferences.setString(
						"stationaryButton" + j, Preferences.getString( "stationaryButton" + ( j + 1 ) ) );
				}

				Preferences.setString( "stationaryButton" + buttons, "" );
				--i;	// retry with the skill that's now in this position
				continue;
			}

			// Show this skill.
			StationaryButtonDecorator.addFightButton( actionBuffer, action, FightRequest.getCurrentRound() > 0 );
		}

		// Add conditionally available combat skills
		// parsed from the fight page

		for ( int i = 0; i < KoLConstants.availableCombatSkills.size(); ++i )
		{
			UseSkillRequest current = (UseSkillRequest) KoLConstants.availableCombatSkills.get( i );
			int actionId = current.getSkillId();
			if ( actionId >= 7000 && actionId < 8000 && actionId != classStunId )
			{
				String action = String.valueOf( actionId );
				StationaryButtonDecorator.addFightButton( actionBuffer, action, FightRequest.getCurrentRound() > 0 );
			}
		}

		if ( StationaryButtonDecorator.combatHotkeys.isEmpty() )
		{
			StationaryButtonDecorator.reloadCombatHotkeyMap();
		}
	}

	public static final void addChoiceButtons( final StringBuffer buffer )
	{
		String name = "auto";
		String action = "choice.php?action=auto";
		boolean isEnabled = true;
		boolean forceFocus = true;

		StationaryButtonDecorator.addButton( buffer, name, action, isEnabled, forceFocus );

		int choice = ChoiceManager.currentChoice();
		if ( choice != 0 )
		{
			TreeMap choices = ChoiceCommand.parseChoices( false );
			Iterator i = choices.entrySet().iterator();
			StringBuilder actionBuffer = new StringBuilder();
			while ( i.hasNext() )
			{
				Map.Entry e = (Map.Entry) i.next();
				name = (String) e.getValue();
				actionBuffer.setLength( 0 );
				actionBuffer.append( "choice.php?whichchoice=" );
				actionBuffer.append( choice );
				actionBuffer.append( "&option=" );
				actionBuffer.append( ((Integer) e.getKey()).intValue() );
				actionBuffer.append( "&pwd=" );
				actionBuffer.append( GenericRequest.passwordHash );
				StationaryButtonDecorator.addButton( buffer, name, actionBuffer.toString(), true, false );
			}
		}
	}

	public static final void addNonCombatButtons( final StringBuffer response, final StringBuffer buffer )
	{
		String name = "again";
		String action = getAdventureAgainLocation( response );;
		boolean isEnabled = !action.equals( "main.php" );
		boolean forceFocus = true;

		StationaryButtonDecorator.addButton( buffer, name, action, isEnabled, forceFocus );
	}

	private static final void addScriptButton( final String urlString, final StringBuffer buffer, final boolean forceFocus )
	{
		String name;
		String action;
		boolean isEnabled = true;

		if ( urlString.endsWith( "action=script" ) )
		{
			name = "abort";
			action = "fight.php?action=abort";
		}
		else
		{
			name = "script";
			action = "fight.php?action=custom";
		}

		StationaryButtonDecorator.addButton( buffer, name, action, isEnabled, forceFocus );
	}

	private static final void addFightButton( final StringBuffer buffer, final String action, boolean isEnabled )
	{
		boolean forceFocus = action.equals( "attack" );
		String name = StationaryButtonDecorator.getActionName( action );

		StringBuilder actionBuffer = new StringBuilder( "fight.php?action=" );

		if ( action.equals( "attack" ) || action.equals( "steal" ) )
		{
			actionBuffer.append( action );
		}
		else if ( action.equals( "jiggle" ) )
		{
			actionBuffer.append( "chefstaff" );
			isEnabled &= !FightRequest.alreadyJiggled();
		}
		else if ( action.equals( "insult" ) )
		{
			int itemId =
				KoLCharacter.inBeecore() ?
				ItemPool.MARAUDER_MOCKERY_MANUAL :
				ItemPool.PIRATE_INSULT_BOOK;

			if ( KoLConstants.inventory.contains( ItemPool.get( itemId, 1 ) ) )
			{
				actionBuffer.append( "useitem&whichitem=" );
				actionBuffer.append( String.valueOf( itemId ) );
			}
			else
			{
				isEnabled = false;
			}
		}
		else if ( action.equals( "jam flyer" ) )
		{
			actionBuffer.append( "useitem&whichitem=2404" );
		}
		else if ( action.equals( "rock flyer" ) )
		{
			actionBuffer.append( "useitem&whichitem=2405" );
		}
		else
		{
			actionBuffer.append( "skill&whichskill=" );
			actionBuffer.append( action );
			int skillID = StringUtilities.parseInt( action );
			UseSkillRequest actionRequest = UseSkillRequest.getInstance( skillID );
			isEnabled &= KoLConstants.availableCombatSkills.contains( actionRequest );
		}

		StationaryButtonDecorator.addButton( buffer, name, actionBuffer.toString(), isEnabled, forceFocus );
	}

	private static final void addButton( final StringBuffer buffer, final String name, final String action,
					     final boolean isEnabled, final boolean forceFocus )
	{
		buffer.append( "<input type=\"button\" onClick=\"document.location.href='" );
		buffer.append( action );
		buffer.append( "';void(0);\" value=\"" );
		buffer.append( name );
		buffer.append( "\"" );

		if ( forceFocus )
		{
			buffer.append( " id=\"defaultButton\"" );
		}

		if ( !isEnabled )
		{
			buffer.append( " disabled" );
		}

		buffer.append( ">&nbsp;" );
	}

	private static final Pattern BODY_PATTERN = Pattern.compile( "<body>.*</body>", Pattern.DOTALL );
	private static final Pattern LOCATION_PATTERN = Pattern.compile( "<[aA] href=[\"']?([^\"'>]*)", Pattern.DOTALL );

	private static final String getAdventureAgainLocation( StringBuffer response )
	{
		// Get the "adventure again" link from the page.
		// Search only in the body of the page

		Matcher m = BODY_PATTERN.matcher( response );
		if ( !m.find() )
		{
			// This will not happen
			return "main.php";
		}

		m = LOCATION_PATTERN.matcher( m.group(0) );
		while ( m.find() )
		{
			// Skip Monster Manuel's link to a new factoid
			// questlog.php?which=6&vl=p#mon1429

			String link = m.group( 1 );
			if ( !link.contains( "questlog.php" ) && !link.contains( "desc_item.php" ) )
			{
				return link;
			}
		}

		// If there is none, perhaps we fought a monster as a result of
		// using an item.

		String monster = FightRequest.getLastMonsterName();

		if ( monster.equals( "giant sandworm" ) )
		{
			AdventureResult drumMachine = ItemPool.get( ItemPool.DRUM_MACHINE, 1 );
			if ( KoLConstants.inventory.contains( drumMachine ) )
			{
				return "inv_use.php?pwd=" + GenericRequest.passwordHash + "&which=3&whichitem=" + ItemPool.DRUM_MACHINE;
			}

			// Look for more drum machines in the Oasis
			return "adventure.php?snarfblat=122";
		}

		if ( monster.equals( "scary pirate" ) )
		{
			return "inv_use.php?pwd=" + GenericRequest.passwordHash +"&which=3&whichitem=" + ItemPool.CURSED_PIECE_OF_THIRTEEN;
		}

		return "main.php";
	}

	private static final String getActionName( final String action )
	{
		if ( action.equals( "attack" ) )
		{
			return FightRequest.getCurrentRound() == 0 ? "again" : "attack";
		}

		if ( action.equals( "insult" ) )
		{
			return "pirate insult";
		}
		
		if ( action.equals( "steal" ) || action.equals( "jiggle" ) ||
		     action.equals( "script" ) ||
		     action.equals( "jam flyer" ) || action.equals( "rock flyer" ) )
		{
			return action;
		}
		
		int skillId = StringUtilities.parseInt( action );
		String name = SkillDatabase.getSkillName( skillId ).toLowerCase();

		switch ( skillId )
		{
		case 15:	// CLEESH
		case 7002:	// Shake Hands
		case 7003:	// Hot Breath
		case 7004:	// Cold Breath
		case 7005:	// Spooky Breath
		case 7006:	// Stinky Breath
		case 7007:	// Sleazy Breath
			name = StringUtilities.globalStringDelete( name, " " );
			break;

		case 7001:	// Give In To Your Vampiric Urges
			name = "bakula";
			break;

		case 7010:	// red bottle-rocket
		case 7011:	// blue bottle-rocket
		case 7012:	// orange bottle-rocket
		case 7013:	// purple bottle-rocket
		case 7014:	// black bottle-rocket
			name = StringUtilities.globalStringDelete( StringUtilities.globalStringDelete( name, "fire " ), "bottle-" );
			break;

		case 1003:	// thrust-smack
			name = "thrust";
			break;

		case 1004: // lunge-smack
			name = "lunge";
			break;
			
		case 1005:	// lunging thrust-smack
			name = "lunging";
			break;
			
		case 2:		// Chronic Indigestion
		case 7009:	// Magic Missile
		case 3004:	// Entangling Noodles
		case 3009:	// Lasagna Bandages
		case 3019:	// Fearful Fettucini
		case 19:	// Transcendent Olfaction
		case 7063:	// Falling Leaf Whirlwind
			name = name.substring( name.lastIndexOf( " " ) + 1 );
			break;

		case 50:	// Break It On Down
		case 51:	// Pop and Lock It
		case 52:	// Run Like the WInd
		case 3003:	// Minor Ray of Something
		case 3005:	// eXtreme Ray of Something
		case 3007:	// Cone of Whatever
		case 3008:	// Weapon of the Pastalord
		case 3020:	// Spaghetti Spear
		case 4003:	// Stream of Sauce
		case 4009:	// Wave of Sauce
		case 5019:	// Tango of Terror
		case 7061:	// Spring Raindrop Attack
		case 7062:	// Summer Siesta
		case 7064:	// Winter's Bite Technique
		case 7201:	// Mug for the Audience
			name = name.substring( 0, name.indexOf( " " ) );
			break;

		case 5003:	// Disco Eye-Poke
			name = "eyepoke";
			break;

		case 5005:	// Disco Dance of Doom
			name = "dance1";
			break;

		case 5008:	// Disco Dance II: Electric Boogaloo
			name = "dance2";
			break;

		case 5036:	// Disco Dance 3: Back in the Habit
			name = "dance3";
			break;

		case 5012:	// Disco Face Stab
			name = "facestab";
			break;
		}

		return name;
	}

	public static final void reloadCombatHotkeyMap()
	{
		StationaryButtonDecorator.combatHotkeys.clear();

		for ( int i = 0; i <= 9; ++i )
		{
			StationaryButtonDecorator.combatHotkeys.add( Preferences.getString( "combatHotkey" + i ) );
		}
	}
}
