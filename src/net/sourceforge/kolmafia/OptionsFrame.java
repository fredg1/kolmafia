/**
 * Copyright (c) 2005, KoLmafia development team
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
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
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

package net.sourceforge.kolmafia;

// layout
import java.awt.Color;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;

// events
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.SwingUtilities;

// containers
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

// utilities
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import java.io.File;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.DataUtilities;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * <p>Handles all of the customizable user options in <code>KoLmafia</code>.
 * This class presents all of the options that the user can customize
 * in their adventuring and uses the appropriate <code>KoLSettings</code>
 * in order to display them.  This class also uses <code>KoLSettings</code>
 * to record the user's preferences for upcoming sessions.</p>
 *
 * <p>If this class is accessed before login, it will modify global settings
 * ONLY, and if the character already has settings, any modification of
 * global settings will not modify their own.  Accessing this class after
 * login will result in modification of the character's own settings ONLY,
 * and will not modify any global settings.</p>
 *
 * <p>Proxy settings are a special exception to this rule - because the
 * Java Virtual Machine requires the proxy settings to be specified at
 * a global level, though the settings are changed appropriately on disk,
 * only the most recently loaded settings will be active on the current
 * instance of the JVM.  If separate characters need separate proxies,
 * they cannot be run in the same JVM instance.</p>
 */

public class OptionsFrame extends KoLFrame
{
	private JTabbedPane tabs;
	private JTabbedPane customTabs;

	private JTree displayTree;
	private DefaultTreeModel displayModel;

	/**
	 * Constructs a new <code>OptionsFrame</code> that will be
	 * associated with the given client.  When this frame is
	 * closed, it will attempt to return focus to the currently
	 * active frame; note that if this is done while the client
	 * is shuffling active frames, closing the window will not
	 * properly transfer focus.
	 *
	 * @param	client	The client to be associated with this <code>OptionsFrame</code>
	 */

	public OptionsFrame( KoLmafia client )
	{
		super( client, "Preferences" );
		tabs = new JTabbedPane();

		addTab( "General", new GeneralOptionsPanel() );
		addTab( "Area List", new AreaOptionsPanel() );
		addTab( "Choice Handling", new ChoiceOptionsPanel() );
		addTab( "Chat Options", new ChatOptionsPanel() );

		if ( !client.inLoginState() )
		{
			JPanel customContainer = new JPanel( new BorderLayout() );
			customTabs = new JTabbedPane();

			displayTree = new JTree();
			displayModel = (DefaultTreeModel) displayTree.getModel();

			JScrollPane treeScroller = new JScrollPane( displayTree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

			customTabs.add( "View", treeScroller );
			customTabs.add( "Modify", new CustomCombatPanel() );

			customContainer.add( customTabs, BorderLayout.CENTER );
			tabs.add( "Custom Combat", customContainer );
		}

		framePanel.setLayout( new CardLayout( 10, 10 ) );
		framePanel.add( tabs, "" );
	}

	public void dispose()
	{
		tabs = null;
		customTabs = null;
		displayTree = null;
		displayModel = null;

		super.dispose();
	}

	private void addTab( String name, JComponent panel )
	{
		JScrollPane scroller = new JScrollPane( panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		JComponentUtilities.setComponentSize( scroller, 560, 400 );
		tabs.add( name, scroller );
	}

	private class AreaOptionsPanel extends OptionsPanel
	{
		private String [] zones;
		private JCheckBox [] options;

		public AreaOptionsPanel()
		{
			super( "Adventure List", new Dimension( 370, 16 ), new Dimension( 20, 16 ) );

			zones = new String[ AdventureDatabase.ZONE_NAMES.size() ];
			options = new JCheckBox[ AdventureDatabase.ZONE_NAMES.size() + 2 ];

			for ( int i = 0; i < options.length; ++i )
				options[i] = new JCheckBox();

			VerifiableElement [] elements = new VerifiableElement[ AdventureDatabase.ZONE_NAMES.size() + 3 ];

			elements[0] = new VerifiableElement( "Sort adventure list", JLabel.LEFT, options[0] );
			elements[1] = new VerifiableElement( "Show associated zone", JLabel.LEFT, options[1] );
			elements[2] = new VerifiableElement( " ", new JLabel( "" ) );

			String [] names = new String[ AdventureDatabase.ZONE_NAMES.keySet().size() ];
			AdventureDatabase.ZONE_NAMES.keySet().toArray( names );

			for ( int i = 0; i < names.length; ++i )
			{
				zones[i] = (String) AdventureDatabase.ZONE_NAMES.get( names[i] );
				elements[i+3] = new VerifiableElement( "Hide " + AdventureDatabase.ZONE_DESCRIPTIONS.get( names[i] ), JLabel.LEFT, options[i+2] );
			}

			setContent( elements, false );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			setProperty( "sortAdventures", String.valueOf( options[0].isSelected() ) );
			setProperty( "showAdventureZone", String.valueOf( options[1].isSelected() ) );

			StringBuffer areas = new StringBuffer();

			for ( int i = 2; i < options.length; ++i )
			{
				if ( options[i].isSelected() )
				{
					if ( areas.length() != 0 )
						areas.append( ',' );

					areas.append( zones[i-2] );
				}
			}

			setProperty( "zoneExcludeList", areas.toString() );
			super.actionConfirmed();

			LockableListModel adventureList = AdventureDatabase.getAsLockableListModel();

			if ( client != null && options[0].isSelected() )
				Collections.sort( adventureList );
		}

		protected void actionCancelled()
		{
			options[0].setSelected( getProperty( "sortAdventures" ).equals( "true" ) );
			options[1].setSelected( getProperty( "showAdventureZone" ).equals( "true" ) );

			String excluded = getProperty( "zoneExcludeList" );
			for ( int i = 0; i < zones.length; ++i )
				options[i+2].setSelected( excluded.indexOf( zones[i] ) != -1 );
		}
	}

	/**
	 * This panel handles all of the things related to login
	 * options, including which server to use for login and
	 * all other requests, as well as the user's proxy settings
	 * (if applicable).
	 */

	private class GeneralOptionsPanel extends OptionsPanel
	{
		private JCheckBox [] optionBoxes;

		private final String [][] options =
		{
			{ "synchronizeFightFrame", "Show requests in mini-browser" },
			{ "serverFriendly", "Use server-friendlier request speed" },
			{ "forceReconnect", "Automatically time-in on time-out" },

			{ "cloverProtectActive", "Guard against accidental clover usage" },
			{ "autoSatisfyChecks", "Automatically satisfy conditions on check" },
		};

		/**
		 * Constructs a new <code>StartupOptionsPanel</code>, containing a
		 * place for the users to select their desired server and for them
		 * to modify any applicable proxy settings.
		 */

		public GeneralOptionsPanel()
		{
			super( "General Options", new Dimension( 370, 16 ), new Dimension( 20, 16 ) );
			VerifiableElement [] elements = new VerifiableElement[ options.length ];

			optionBoxes = new JCheckBox[ options.length ];
			for ( int i = 0; i < options.length; ++i )
				optionBoxes[i] = new JCheckBox();

			for ( int i = 0; i < options.length; ++i )
				elements[i] = new VerifiableElement( options[i][1], JLabel.LEFT, optionBoxes[i] );

			setContent( elements, false );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			for ( int i = 0; i < options.length; ++i )
				setProperty( options[i][0], String.valueOf( optionBoxes[i].isSelected() ) );

			super.actionConfirmed();
		}

		protected void actionCancelled()
		{
			for ( int i = 0; i < options.length; ++i )
				optionBoxes[i].setSelected( getProperty( options[i][0] ).equals( "true" ) );
		}
	}

	/**
	 * Panel used for handling chat-related options and preferences,
	 * including font size, window management and maybe, eventually,
	 * coloring options for contacts.
	 */

	private class ChatOptionsPanel extends OptionsPanel
	{
		private JComboBox autoLogSelect;
		private JComboBox fontSizeSelect;
		private JComboBox chatStyleSelect;
		private JComboBox useTabSelect;
		private JComboBox popupSelect;
		private JComboBox eSoluSelect;

		public ChatOptionsPanel()
		{
			super( "Chat Preferences" );

			autoLogSelect = new JComboBox();
			autoLogSelect.addItem( "Do not log chat" );
			autoLogSelect.addItem( "Automatically log chat" );

			fontSizeSelect = new JComboBox();
			for ( int i = 1; i <= 7; ++i )
				fontSizeSelect.addItem( String.valueOf( i ) );

			chatStyleSelect = new JComboBox();
			chatStyleSelect.addItem( "All conversations separate" );
			chatStyleSelect.addItem( "Channels separate, blues combined" );
			chatStyleSelect.addItem( "Channels combined, blues separate" );

			useTabSelect = new JComboBox();
			useTabSelect.addItem( "Use windowed chat interface (popup contacts)" );
			useTabSelect.addItem( "Use windowed chat interface (inline contacts)" );
			useTabSelect.addItem( "Use tabbed chat interface (popup contacts)" );
			useTabSelect.addItem( "Use tabbed chat interface (inline contacts)" );

			popupSelect = new JComboBox();
			popupSelect.addItem( "Display contact lists inline" );
			popupSelect.addItem( "Popup contact lists in new window" );

			eSoluSelect = new JComboBox();
			eSoluSelect.addItem( "Nameclick select bar only" );
			eSoluSelect.addItem( "eSolu scriptlet chat links (color)" );
			eSoluSelect.addItem( "eSolu scriptlet chat links (gray)" );

			VerifiableElement [] elements = new VerifiableElement[6];
			elements[0] = new VerifiableElement( "Chat Logs: ", autoLogSelect );
			elements[1] = new VerifiableElement( "Font Size: ", fontSizeSelect );
			elements[2] = new VerifiableElement( "Chat Style: ", chatStyleSelect );
			elements[3] = new VerifiableElement( "Tabbed Chat: ", useTabSelect );
			elements[4] = new VerifiableElement( "Contact List: ", popupSelect );
			elements[5] = new VerifiableElement( "eSolu Script: ", eSoluSelect );

			setContent( elements );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			if ( autoLogSelect.getSelectedIndex() == 1 )
				KoLMessenger.initializeChatLogs();

			setProperty( "autoLogChat", String.valueOf( autoLogSelect.getSelectedIndex() == 1 ) );
			setProperty( "fontSize", (String) fontSizeSelect.getSelectedItem() );
			LimitedSizeChatBuffer.setFontSize( Integer.parseInt( (String) fontSizeSelect.getSelectedItem() ) );

			setProperty( "chatStyle", String.valueOf( chatStyleSelect.getSelectedIndex() ) );
			setProperty( "useTabbedChat", String.valueOf( useTabSelect.getSelectedIndex() ) );
			setProperty( "usePopupContacts", String.valueOf( useTabSelect.getSelectedIndex() ) );
			setProperty( "eSoluScriptType", String.valueOf( eSoluSelect.getSelectedIndex() ) );

			super.actionConfirmed();
		}

		protected void actionCancelled()
		{
			autoLogSelect.setSelectedIndex( getProperty( "autoLogChat" ).equals( "true" ) ? 1 : 0 );
			fontSizeSelect.setSelectedItem( getProperty( "fontSize" ) );
			LimitedSizeChatBuffer.setFontSize( Integer.parseInt( getProperty( "fontSize" ) ) );

			chatStyleSelect.setSelectedIndex( Integer.parseInt( getProperty( "chatStyle" ) ) );
			useTabSelect.setSelectedIndex( Integer.parseInt( getProperty( "useTabbedChat" ) ) );
			popupSelect.setSelectedIndex( Integer.parseInt( getProperty( "usePopupContacts" ) ) );
			eSoluSelect.setSelectedIndex( Integer.parseInt( getProperty( "eSoluScriptType" ) ) );
		}
	}

	/**
	 * This panel allows the user to select which item they would like
	 * to do for each of the different choice adventures.
	 */

	private class ChoiceOptionsPanel extends KoLPanel
	{
		private JComboBox [] optionSelects;
		private JComboBox castleWheelSelect;

		/**
		 * Constructs a new <code>ChoiceOptionsPanel</code>.
		 */

		public ChoiceOptionsPanel()
		{
			super( new Dimension( 130, 20 ), new Dimension( 260, 20 ) );

			optionSelects = new JComboBox[ AdventureDatabase.CHOICE_ADVS.length ];
			for ( int i = 0; i < AdventureDatabase.CHOICE_ADVS.length; ++i )
			{
				optionSelects[i] = new JComboBox();

				boolean ignorable = AdventureDatabase.ignoreChoiceOption( AdventureDatabase.CHOICE_ADVS[i][0][0] ) != null;
				optionSelects[i].addItem( ignorable ?
										  "Ignore this adventure" :
										  "Can't ignore this adventure" );

				for ( int j = 0; j < AdventureDatabase.CHOICE_ADVS[i][2].length; ++j )
					optionSelects[i].addItem( AdventureDatabase.CHOICE_ADVS[i][2][j] );
			}

			castleWheelSelect = new JComboBox();
			castleWheelSelect.addItem( "Turn to map quest position" );
			castleWheelSelect.addItem( "Turn to muscle position" );
			castleWheelSelect.addItem( "Turn to mysticality position" );
			castleWheelSelect.addItem( "Turn to moxie position" );
			castleWheelSelect.addItem( "Ignore this adventure" );

			VerifiableElement [] elements = new VerifiableElement[ optionSelects.length + 1 ];
			elements[0] = new VerifiableElement( "Castle Wheel", castleWheelSelect );

			for ( int i = 1; i < elements.length; ++i )
				elements[i] = new VerifiableElement( AdventureDatabase.CHOICE_ADVS[i-1][1][0], optionSelects[i-1] );

			setContent( elements );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			setProperty( "luckySewerAdventure", (String) optionSelects[0].getSelectedItem() );
			for ( int i = 1; i < optionSelects.length; ++i )
			{
				int index = optionSelects[i].getSelectedIndex();
				String choice = AdventureDatabase.CHOICE_ADVS[i][0][0];
				boolean ignorable = AdventureDatabase.ignoreChoiceOption( choice ) != null;

				if ( ignorable || index != 0 )
					setProperty( choice, String.valueOf( index ) );
				else
					optionSelects[i].setSelectedIndex( Integer.parseInt( getProperty( choice ) ) );
			}

			//              The Wheel:

			//              Muscle
			// Moxie          +         Mysticality
			//            Map Quest

			// Option 1: Turn the wheel counterclockwise
			// Option 2: Turn the wheel clockwise
			// Option 3: Leave the wheel alone

			switch ( castleWheelSelect.getSelectedIndex() )
			{
				case 0: // Map quest position (choice adventure 11)
					setProperty( "choiceAdventure9", "2" );	  // Turn the muscle position counterclockwise
					setProperty( "choiceAdventure10", "1" );  // Turn the mysticality position clockwise
					setProperty( "choiceAdventure11", "3" );  // Leave the map quest position alone
					setProperty( "choiceAdventure12", "2" );  // Turn the moxie position counterclockwise
					break;

				case 1: // Muscle position (choice adventure 9)
					setProperty( "choiceAdventure9", "3" );	  // Leave the muscle position alone
					setProperty( "choiceAdventure10", "2" );  // Turn the mysticality position counterclockwise
					setProperty( "choiceAdventure11", "1" );  // Turn the map quest position clockwise
					setProperty( "choiceAdventure12", "1" );  // Turn the moxie position clockwise
					break;

				case 2: // Mysticality position (choice adventure 10)
					setProperty( "choiceAdventure9", "1" );	  // Turn the muscle position clockwise
					setProperty( "choiceAdventure10", "3" );  // Leave the mysticality position alone
					setProperty( "choiceAdventure11", "2" );  // Turn the map quest position counterclockwise
					setProperty( "choiceAdventure12", "1" );  // Turn the moxie position clockwise
					break;

				case 3: // Moxie position (choice adventure 12)
					setProperty( "choiceAdventure9", "2" );	  // Turn the muscle position counterclockwise
					setProperty( "choiceAdventure10", "2" );  // Turn the mysticality position counterclockwise
					setProperty( "choiceAdventure11", "1" );  // Turn the map quest position clockwise
					setProperty( "choiceAdventure12", "3" );  // Leave the moxie position alone
					break;

				case 4: // Ignore this adventure
					setProperty( "choiceAdventure9", "3" );	  // Leave the muscle position alone
					setProperty( "choiceAdventure10", "3" );  // Leave the mysticality position alone
					setProperty( "choiceAdventure11", "3" );  // Leave the map quest position alone
					setProperty( "choiceAdventure12", "3" );  // Leave the moxie position alone
					break;
			}
		}

		protected void actionCancelled()
		{
			optionSelects[0].setSelectedItem( getProperty( "luckySewerAdventure" ) );
			for ( int i = 1; i < optionSelects.length; ++i )
				optionSelects[i].setSelectedIndex( Integer.parseInt( getProperty( AdventureDatabase.CHOICE_ADVS[i][0][0] ) ) );

			// Determine the desired wheel position by examining
			// which choice adventure has the "3" value.  If none
			// exists, assume the user wishes to turn it to the map
			// quest.

			// If they are all "3", user wants the wheel left alone

			int option = 11;
			int count = 0;
			for ( int i = 9; i < 13; ++i )
				if ( getProperty( "choiceAdventure" + i ).equals( "3" ) )
				{
					option = i;
					count++;
				}

			switch ( count )
			{
				default:	// Bogus saved options
				case 0:		// Map quest position
					castleWheelSelect.setSelectedIndex(0);
					break;

				case 1:		// One chosen target
					switch ( option )
					{
					case 9: // Muscle position
						castleWheelSelect.setSelectedIndex(1);
						break;

					case 10: // Mysticality position
						castleWheelSelect.setSelectedIndex(2);
						break;

					case 11: // Map quest position
						castleWheelSelect.setSelectedIndex(0);
						break;

					case 12: // Moxie position
						castleWheelSelect.setSelectedIndex(3);
						break;
					}
					break;

				case 4:		// Ignore this adventure
					castleWheelSelect.setSelectedIndex(4);
					break;
			}
		}
	}

	/**
	 * A generic panel which adds a label to the bottom of the KoLPanel
	 * to update the panel's status.  It also provides a thread which is
	 * guaranteed to be a daemon thread for updating the frame which
	 * also retrieves a reference to the client's current settings.
	 */

	private abstract class OptionsPanel extends LabeledKoLPanel
	{
		public OptionsPanel()
		{	this( new Dimension( 130, 20 ), new Dimension( 260, 20 ) );
		}

		public OptionsPanel( String panelTitle )
		{	this( panelTitle, new Dimension( 130, 20 ), new Dimension( 260, 20 ) );
		}

		public OptionsPanel( Dimension left, Dimension right )
		{	this( null, left, right );
		}

		public OptionsPanel( String panelTitle, Dimension left, Dimension right )
		{	super( panelTitle, left, right );
		}

		public void setStatusMessage( int displayState, String message )
		{
		}

		protected void actionConfirmed()
		{
		}
	}

	/**
	 * Internal class used to handle everything related to
	 * displaying custom combat.
	 */

	private void refreshCombatTree()
	{
		CombatSettings.reset();
		displayModel.setRoot( CombatSettings.getRoot() );
		displayTree.setRootVisible( false );
	}

	private class CustomCombatPanel extends LabeledScrollPanel
	{
		public CustomCombatPanel()
		{
			super( "Custom Combat", "save", "help", new JTextArea( 12, 40 ) );

			try
			{
				BufferedReader reader = KoLDatabase.getReader( CombatSettings.settingsFileName() );
				StringBuffer buffer = new StringBuffer();

				String line;

				while ( (line = reader.readLine()) != null )
				{
					buffer.append( line );
					buffer.append( System.getProperty( "line.separator" ) );
				}

				reader.close();
				reader = null;
				((JTextArea)scrollComponent).setText( buffer.toString() );
			}
			catch ( Exception e )
			{
				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}

			refreshCombatTree();
		}

		protected void actionConfirmed()
		{
			try
			{
				PrintStream writer = new PrintStream( new FileOutputStream( DATA_DIRECTORY + CombatSettings.settingsFileName() ) );
				writer.println( ((JTextArea)scrollComponent).getText() );
				writer.close();
				writer = null;

				int customIndex = KoLCharacter.getBattleSkillIDs().indexOf( "custom" );
				KoLCharacter.getBattleSkillIDs().setSelectedIndex( customIndex );
				KoLCharacter.getBattleSkillNames().setSelectedIndex( customIndex );
				setProperty( "battleAction", "custom" );
			}
			catch ( Exception e )
			{
				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}

			// After storing all the data on disk, go ahead
			// and reload the data inside of the tree.

			refreshCombatTree();
			customTabs.setSelectedIndex(0);
		}

		protected void actionCancelled()
		{	openSystemBrowser( "http://kolmafia.sourceforge.net/combat.html" );
		}
	}

	/**
	 * The main method used in the event of testing the way the
	 * user interface looks.  This allows the UI to be tested
	 * without having to constantly log in and out of KoL.
	 */

	public static void main( String [] args )
	{
		HPRestoreItemList.reset();
		MPRestoreItemList.reset();
		(new CreateFrameRunnable( OptionsFrame.class )).run();
	}
}
