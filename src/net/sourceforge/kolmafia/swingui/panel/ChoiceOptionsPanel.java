/*
 * Copyright (c) 2005-2021, KoLmafia development team
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

package net.sourceforge.kolmafia.swingui.panel;

import java.awt.CardLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.java.dev.spellcast.utilities.ActionPanel;

import net.sourceforge.kolmafia.KoLAdventure;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.RequestThread;

import net.sourceforge.kolmafia.listener.Listener;
import net.sourceforge.kolmafia.listener.PreferenceListenerRegistry;

import net.sourceforge.kolmafia.objectpool.IntegerPool;

import net.sourceforge.kolmafia.persistence.AdventureDatabase;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.RetiredChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.UnknownChoiceAdventure;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure.CustomOption;
import net.sourceforge.kolmafia.persistence.choiceadventures.ChoiceAdventureDatabase.ChoiceAdventure.Option;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.swingui.CommandDisplayFrame;

import net.sourceforge.kolmafia.swingui.widget.GenericScrollPane;

import net.sourceforge.kolmafia.utilities.CharacterEntities;
import net.sourceforge.kolmafia.utilities.InputFieldUtilities;
import net.sourceforge.kolmafia.utilities.StringUtilities;

/**
 * This panel allows the user to select which item they would like to do for each of the different choice
 * adventures.
 */

public class ChoiceOptionsPanel
	extends JTabbedPane
	implements Listener
{
	/**
	 * Zone name ("manor1", "knob", "Item-Driven"...)<p>-><p>
	 * Drop-down name ("Haunted Pantry", "Llama Gong", "Breakable Equipment"...)<p>-><p>
	 * JComboBoxes (using ArrayLists, in case there are duplicate names,
	 * which is common when the location is used as the choice adventure's name)
	 */
	private final TreeMap<String, HashMap<String, ArrayList<JComponent>>> choiceMap = new TreeMap<>();
	private final CardLayout choiceCards = new CardLayout( 10, 10 );
	private final JPanel choicePanel;

	private final JComboBoxedChoice[] choiceOptionSelects;

	private final OceanDestinationComboBox oceanDestSelect = new OceanDestinationComboBox();
	private final JComboBox<String> oceanActionSelect = new JComboBox<>();
	private final JComboBox<String> basementMallSelect = new JComboBox<>();
	private final JComboBox<String> breakableSelect = new JComboBox<>();
	private final JComboBox<String> addingSelect = new JComboBox<>();

	/**
	 * Constructs a new <code>ChoiceOptionsPanel</code>.
	 */

	public ChoiceOptionsPanel()
	{
		super( JTabbedPane.LEFT );

		this.choicePanel = new JPanel( this.choiceCards );
		this.choicePanel.add( new JPanel(), "" );
		this.addTab( "Zone", new GenericScrollPane( this.choicePanel ) );
		this.setToolTipTextAt( 0, "Choices specific to the current adventure zone" );

		this.oceanActionSelect.addItem( "continue" );
		this.oceanActionSelect.addItem( "show" );
		this.oceanActionSelect.addItem( "stop" );
		this.oceanActionSelect.addItem( "save and continue" );
		this.oceanActionSelect.addItem( "save and show" );
		this.oceanActionSelect.addItem( "save and stop" );

		this.basementMallSelect.addItem( "do not show Mall prices" );
		this.basementMallSelect.addItem( "show Mall prices for items you don't have" );
		this.basementMallSelect.addItem( "show Mall prices for all items" );

		this.breakableSelect.addItem( "abort on breakage" );
		this.breakableSelect.addItem( "equip previous" );
		this.breakableSelect.addItem( "re-equip from inventory, or abort" );
		this.breakableSelect.addItem( "re-equip from inventory, or previous" );
		this.breakableSelect.addItem( "acquire & re-equip" );

		this.addingSelect.addItem( "show in browser" );
		this.addingSelect.addItem( "create goal scrolls only" );
		this.addingSelect.addItem( "create goal & 668 scrolls" );
		this.addingSelect.addItem( "create goal, 31337, 668 scrolls" );



		final ArrayList<JComboBoxedChoice> choiceOptionSelectList = new ArrayList<>();
		final Iterator<ChoiceAdventure> choiceAdventures = ChoiceAdventureDatabase.getDatabaseIterator();
		while ( choiceAdventures.hasNext() )
		{
			ChoiceAdventure choiceAdventure = choiceAdventures.next();

			//TODO add an option to see retired choice adventures anyway?
			if ( choiceAdventure instanceof RetiredChoiceAdventure ||
			     choiceAdventure instanceof UnknownChoiceAdventure ||
			     choiceAdventure.customOptions.size() == 0 )
			{
				continue;
			}

			choiceOptionSelectList.add( new JComboBoxedChoice( choiceAdventure ) );
		}
		this.choiceOptionSelects = choiceOptionSelectList.toArray( new JComboBoxedChoice[ 0 ] );

		Arrays.sort( this.choiceOptionSelects );

		for ( JComboBoxedChoice boxedChoice : this.choiceOptionSelects )
		{
			for ( String zone : boxedChoice.comboBoxes.keySet() )
			{
				addChoiceSelect(
					zone,
					CharacterEntities.unescape( boxedChoice.choiceAdventure.getCustomName() ),
					boxedChoice.comboBoxes.get( zone ) );
			}

			for ( String preference : boxedChoice.choiceAdventure.customPreferencesToListen )
			{
				PreferenceListenerRegistry.registerPreferenceListener( preference, this );
			}
		}


		this.addChoiceSelect( "Item-Driven", "Breakable Equipment", this.breakableSelect );
		this.addChoiceSelect( "Fernswarthy's Tower", "Fernswarthy's Basement", this.basementMallSelect );
		this.addChoiceSelect( "Island", "Ocean Destination", this.oceanDestSelect );
		this.addChoiceSelect( "Island", "Ocean Action", this.oceanActionSelect );
		this.addChoiceSelect( "Mountain", "The Valley of Rof L'm Fao", this.addingSelect );

		this.addChoiceSelect( "Item-Driven", "Item",
			new CommandButton( "use 1 llama lama gong" ) );
		this.addChoiceSelect( "Item-Driven", "Item",
			new CommandButton( "use 1 tiny bottle of absinthe" ) );
		this.addChoiceSelect( "Item-Driven", "Item",
			new CommandButton( "use 1 haunted sorority house staff guide" ) );
		this.addChoiceSelect( "Item-Driven", "Item",
			new CommandButton( "use 1 skeleton" ) );

		PreferenceListenerRegistry.registerPreferenceListener( "choiceAdventure*", this );
		PreferenceListenerRegistry.registerPreferenceListener( "oceanAction", this );
		PreferenceListenerRegistry.registerPreferenceListener( "oceanDestination", this );
		PreferenceListenerRegistry.registerPreferenceListener( "basementMallPrices", this );
		PreferenceListenerRegistry.registerPreferenceListener( "breakableHandling", this );
		PreferenceListenerRegistry.registerPreferenceListener( "addingScrolls", this );

		this.loadSettings();

		for ( String key : this.choiceMap.keySet() )
		{
			HashMap<String, ArrayList<JComponent>> optionsList = this.choiceMap.get( key );
			if ( key.equals( "Item-Driven" ) )
			{
				this.addTab( "Item",
					new GenericScrollPane( new ChoicePanel( optionsList ) ) );
				this.setToolTipTextAt( 1, "Choices related to the use of an item" );
			}
			else
			{
				this.choicePanel.add( new ChoicePanel( optionsList ), key );
			}
		}
	}

	public UpdateChoicesListener getUpdateListener()
	{
		return new UpdateChoicesListener();
	}

	private void addChoiceSelect( final String zone, final String name, final JComponent option )
	{
		if ( zone == null )
		{
			return;
		}

		this.choiceMap.putIfAbsent( zone, new LinkedHashMap<>() );

		HashMap<String, ArrayList<JComponent>> options = this.choiceMap.get( zone );

		options.putIfAbsent( name, new ArrayList<>() );

		ArrayList<JComponent> components = options.get( name );

		if ( !components.contains( option ) )
		{
			components.add( option );
		}
	}

	private class ChoicePanel
		extends GenericPanel
	{
		public ChoicePanel( final HashMap<String, ArrayList<JComponent>> options )
		{
			super( new Dimension( 150, 20 ), new Dimension( 300, 20 ) );

			ArrayList<VerifiableElement> elementList = new ArrayList<>();

			for ( String key : options.keySet() )
			{
				ArrayList<JComponent> value = options.get( key );

				if ( value.size() == 1 )
				{
					elementList.add( new VerifiableElement( key + ":  ", (JComponent) value.get( 0 ) ) );
				}
				else
				{
					for ( int j = 0; j < value.size(); ++j )
					{
						elementList.add( new VerifiableElement(
							key + " " + ( j + 1 ) + ":  ", (JComponent) value.get( j ) ) );
					}
				}
			}

			VerifiableElement[] elements = new VerifiableElement[ elementList.size() ];
			elementList.toArray( elements );

			this.setContent( elements );
		}

		@Override
		public void actionConfirmed()
		{
			ChoiceOptionsPanel.this.saveSettings();
		}

		@Override
		public void actionCancelled()
		{
		}

		@Override
		public void addStatusLabel()
		{
		}

		@Override
		public void setEnabled( final boolean isEnabled )
		{
		}
	}

	private class JComboBoxedChoice
		implements Comparable<JComboBoxedChoice>
	{
		private boolean isUpdating = false;
		private final ChoiceAdventure choiceAdventure;
		private final HashMap<String, JComboBox<CustomOption>> comboBoxes = new HashMap<>();

		JComboBoxedChoice( final ChoiceAdventure choiceAdventure )
		{
			this.choiceAdventure = choiceAdventure;

			CustomOption[] boxItems = choiceAdventure.customOptions.values().toArray( new CustomOption[] {} );
			for ( String zone : choiceAdventure.customZones )
			{
				if ( !zone.equals( "Item-Driven" ) &&
				     !AdventureDatabase.PARENT_ZONES.containsKey( zone ) )
				{
					// we don't "lose" or "risk" anything from having unrecognized zone names
					// but it also means that it's impossible for that JComboBox to appear
					// so assume it's a mistake
					RequestLogger.printLine( "<font color=red>\"" + zone + "\" is not a recognized zone name</font>" );
					RequestLogger.printLine( "@ ChoiceAdventure " + choiceAdventure.choice );
					continue;
				}

				this.comboBoxes.put( zone,
					new JComboBox<CustomOption>( boxItems )
					{
						@Override
						public void setSelectedItem( Object anObject )
						{
							super.setSelectedItem( anObject );

							if ( JComboBoxedChoice.this.isUpdating )
							{
								return;
							}

							JComboBoxedChoice.this.isUpdating = true;

							for ( JComboBox<CustomOption> comboBox : JComboBoxedChoice.this.comboBoxes.values() )
							{
								comboBox.setSelectedItem( anObject );
							}

							JComboBoxedChoice.this.isUpdating = false;
						}
					} );
			}
		}

		/**@see JComboBox#setSelectedItem(Object) */
		public void setSelectedItem( Object anObject )
		{
			for ( JComboBox<CustomOption> comboBox : this.comboBoxes.values() )
			{
				comboBox.setSelectedItem( anObject );
			}
		}

		/**@see JComboBox#getSelectedItem() */
		public Object getSelectedItem()
		{
			for ( JComboBox<CustomOption> comboBox : this.comboBoxes.values() )
			{
				return comboBox.getSelectedItem();
			}
			return null;
		}

		/**@see JComboBox#setSelectedIndex(int) */
		public void setSelectedIndex( int anIndex )
		{
			for ( JComboBox<CustomOption> comboBox : this.comboBoxes.values() )
			{
				comboBox.setSelectedIndex( anIndex );
			}
		}

		/**@see JComboBox#getSelectedIndex() */
		public int getSelectedIndex()
		{
			for ( JComboBox<CustomOption> comboBox : this.comboBoxes.values() )
			{
				return comboBox.getSelectedIndex();
			}
			return -1;
		}

		public int compareTo( final JComboBoxedChoice o )
		{
			ChoiceAdventure choiceUs = this.choiceAdventure;
			ChoiceAdventure choiceThem = o.choiceAdventure;
			KoLAdventure sourceUs = choiceUs.source;
			KoLAdventure sourceThem = choiceThem.source;

			// start by grouping them by zone
			String zoneUs = sourceUs == null ? null : sourceUs.getZone();
			String zoneThem = sourceThem == null ? null : sourceThem.getZone();
			if ( !( zoneUs == null && zoneThem == null ||
			        zoneUs != null && zoneUs.equals( zoneThem ) ) )
			{
				return zoneUs == null ? 1 :
					zoneThem == null ? -1 :
					zoneUs.compareToIgnoreCase( zoneThem );
			}

			// else, choices can have a specified relative ordering
			// within zone regardless of name or choice number
			int orderUs = choiceUs.getCustomOrder();
			int orderThem = choiceThem.getCustomOrder();
			if ( orderUs != orderThem )
			{
				return orderUs - orderThem;
			}

			// else, group them by location
			if ( !( sourceUs == null && sourceThem == null ||
			        sourceUs != null && sourceUs.equals( sourceThem ) ) )
			{
				// nulls at the bottom
				return sourceUs == null ? 1 :
					sourceThem == null ? -1 :
					// KoLAdventure.compareTo(KoLAdventure) marks adventures with
					// low required evasion (i.e. easy/early zones) as being greater.
					// We want those at the top (lesser), so compare THEM to US
					sourceThem.compareTo( sourceUs );
			}

			// else, sort by name
			int result = choiceUs.getCustomName().compareToIgnoreCase( choiceThem.getCustomName() );

			if ( result != 0 )
			{
				return result;
			}

			return choiceUs.choice - choiceThem.choice;
		}
	}

	private class OceanDestinationComboBox
		extends JComboBox<String>
		implements ActionListener
	{
		public OceanDestinationComboBox()
		{
			super();
			this.createMenu( Preferences.getString( "oceanDestination" ) );
			this.addActionListener( this );
		}

		private void createMenu( String dest )
		{
			this.addItem( "ignore adventure" );
			this.addItem( "manual control" );
			this.addItem( "muscle" );
			this.addItem( "mysticality" );
			this.addItem( "moxie" );
			this.addItem( "El Vibrato power sphere" );
			this.addItem( "the plinth" );
			this.addItem( "random choice" );
			if ( dest.indexOf( "," ) != -1 )
			{
				this.addItem( "go to " + dest );
			}
			this.addItem( "choose destination..." );
		}

		public void loadSettings()
		{
			String dest = Preferences.getString( "oceanDestination" );
			this.removeAllItems();
			this.createMenu( dest );
			this.loadSettings( dest );
		}

		private void loadSettings( String dest )
		{
			// Default is "Manual"
			int index = 1;

			if ( dest.equals( "ignore" ) )
			{
				index = 0;
			}
			else if ( dest.equals( "manual" ) )
			{
				index = 1;
			}
			else if ( dest.equals( "muscle" ) )
			{
				index = 2;
			}
			else if ( dest.equals( "mysticality" ) )
			{
				index = 3;
			}
			else if ( dest.equals( "moxie" ) )
			{
				index = 4;
			}
			else if ( dest.equals( "sphere" ) )
			{
				index = 5;
			}
			else if ( dest.equals( "plinth" ) )
			{
				index = 6;
			}
			else if ( dest.equals( "random" ) )
			{
				index = 7;
			}
			else if ( dest.indexOf( "," ) != -1 )
			{
				index = 8;
			}

			this.setSelectedIndex( index );
		}

		public void saveSettings()
		{
			String dest = (String) this.getSelectedItem();
			if ( dest == null )
			{
				return;
			}

			if ( dest.startsWith( "ignore" ) )
			{
				Preferences.setString( "choiceAdventure189", "2" );
				Preferences.setString( "oceanDestination", "ignore" );
				return;
			}

			String value = "";
			if ( dest.startsWith( "muscle" ) )
			{
				value = "muscle";
			}
			else if ( dest.startsWith( "mysticality" ) )
			{
				value = "mysticality";
			}
			else if ( dest.startsWith( "moxie" ) )
			{
				value = "moxie";
			}
			else if ( dest.startsWith( "El Vibrato power sphere" ) )
			{
				value = "sphere";
			}
			else if ( dest.startsWith( "the plinth" ) )
			{
				value = "plinth";
			}
			else if ( dest.startsWith( "random" ) )
			{
				value = "random";
			}
			else if ( dest.startsWith( "go to " ) )
			{
				value = dest.substring( 6 );
			}
			else if ( dest.startsWith( "choose " ) )
			{
				return;
			}
			else	// For anything else, assume Manual Control
			{
				// For manual control, do not take a choice first
				Preferences.setString( "choiceAdventure189", "0" );
				Preferences.setString( "oceanDestination", "manual" );
				return;
			}

			Preferences.setString( "choiceAdventure189", "1" );
			Preferences.setString( "oceanDestination", value );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			String dest = (String) this.getSelectedItem();
			if ( dest == null )
			{
				return;
			}

			// Are we choosing a custom destination?
			if ( !dest.startsWith( "choose" ) )
			{
				return;
			}

			// Prompt for a new destination
			String coords = getCoordinates();
			if ( coords == null )
			{
				// Restore previous selection
				this.loadSettings();
				return;
			}

			// Rebuild combo box
			this.removeAllItems();
			this.createMenu( coords );

			// Select the "go to" menu item
			this.setSelectedIndex( 8 );

			// Request that the settings be saved in a different thread.
			RequestThread.runInParallel( new SaveOceanDestinationSettingsRunnable( this ) );
		}

		private String getCoordinates()
		{
			String coords = InputFieldUtilities.input( "Longitude, Latitude" );
			if ( coords == null )
			{
				return null;
			}

			int index = coords.indexOf( "," );
			if ( index == -1 )
			{
				return null;
			}

			int longitude = StringUtilities.parseInt( coords.substring( 0, index ) );
			if ( longitude < 1 || longitude > 242 )
			{
				return null;
			}

			int latitude = StringUtilities.parseInt( coords.substring( index + 1 ) );
			if ( latitude < 1 || latitude > 100 )
			{
				return null;
			}

			return longitude + "," + latitude;
		}
	}

	private static class SaveOceanDestinationSettingsRunnable
		implements Runnable
	{
		private final OceanDestinationComboBox dest;

		public SaveOceanDestinationSettingsRunnable( OceanDestinationComboBox dest )
		{
			this.dest = dest;
		}

		public void run()
		{
			this.dest.saveSettings();
		}
	}

	private class UpdateChoicesListener
		implements ListSelectionListener
	{
		public void valueChanged( final ListSelectionEvent e )
		{
			JList source = (JList) e.getSource();
			KoLAdventure location = (KoLAdventure) source.getSelectedValue();
			if ( location == null )
			{
				return;
			}
			String zone = location.getZone();
			if ( zone.equals( "Item-Driven" ) )
			{
				ChoiceOptionsPanel.this.setSelectedIndex( 1 );
				ChoiceOptionsPanel.this.choiceCards.show(
					ChoiceOptionsPanel.this.choicePanel, "" );
			}
			else
			{
				ChoiceOptionsPanel.this.setSelectedIndex( 0 );
				ChoiceOptionsPanel.this.choiceCards.show(
					ChoiceOptionsPanel.this.choicePanel,
					ChoiceOptionsPanel.this.choiceMap.containsKey( zone ) ? zone : "" );
			}
			KoLCharacter.updateSelectedLocation( location );
		}
	}

	private boolean isAdjusting = false;

	public synchronized void update()
	{
		if ( !this.isAdjusting )
		{
			this.loadSettings();
		}
	}

	public synchronized void saveSettings()
	{
		if ( this.isAdjusting )
		{
			return;
		}
		this.isAdjusting = true;

		Preferences.setInteger( "basementMallPrices", this.basementMallSelect.getSelectedIndex() );
		Preferences.setInteger( "breakableHandling", this.breakableSelect.getSelectedIndex() + 1 );
		Preferences.setInteger( "addingScrolls", this.addingSelect.getSelectedIndex() );

		for ( JComboBoxedChoice boxedChoice : this.choiceOptionSelects )
		{
			Object selected = boxedChoice.getSelectedItem();

			if ( selected instanceof CustomOption )
			{
				( (CustomOption) selected ).selectionHandler.run();
			}
		}

		// OceanDestinationComboBox handles its own settings.
		this.oceanDestSelect.saveSettings();

		switch ( this.oceanActionSelect.getSelectedIndex() )
		{
		case 0:
			Preferences.setString( "oceanAction", "continue" );
			break;
		case 1:
			Preferences.setString( "oceanAction", "show" );
			break;
		case 2:
			Preferences.setString( "oceanAction", "stop" );
			break;
		case 3:
			Preferences.setString( "oceanAction", "savecontinue" );
			break;
		case 4:
			Preferences.setString( "oceanAction", "saveshow" );
			break;
		case 5:
			Preferences.setString( "oceanAction", "savestop" );
			break;
		}

		this.isAdjusting = false;
	}

	public synchronized void loadSettings()
	{
		this.isAdjusting = true;
		ActionPanel.enableActions( false );	// prevents recursive actions from being triggered

		this.basementMallSelect.setSelectedIndex( Preferences.getInteger( "basementMallPrices" ) );
		this.breakableSelect.setSelectedIndex( Math.max( 0, Preferences.getInteger( "breakableHandling" ) - 1 ) );

		int adding = Preferences.getInteger( "addingScrolls" );
		if ( adding == -1 )
		{
			adding = Preferences.getBoolean( "createHackerSummons" ) ? 3 : 2;
			Preferences.setInteger( "addingScrolls", adding );
		}
		this.addingSelect.setSelectedIndex( adding );

		for ( JComboBoxedChoice boxedChoice : this.choiceOptionSelects )
		{
			String setting;
			int index;

			Supplier<Integer> supplier = boxedChoice.choiceAdventure.customLoad;
			if ( supplier != null )
			{
				setting = null;
				index = supplier.get();
			}
			else
			{
				setting = "choiceAdventure" + boxedChoice.choiceAdventure.choice;
				index = Preferences.getInteger( setting );
			}

			CustomOption selection = null;
			for ( CustomOption customOption : boxedChoice.choiceAdventure.customOptions.values() )
			{
				if ( customOption.optionIndex == index )
				{
					selection = customOption;
					break;
				}
			}

			if ( selection != null )
			{
				boxedChoice.setSelectedItem( selection );
				continue;
			}


			// error handling: we didn't get pointed to a customIndex

			if ( setting == null && index != Integer.MAX_VALUE )
			{
				// we shouldn't be here; this means we went through
				// a custom-made supplier and still got directed
				// to an unexisting CustomOption
				System.out.println( "Error when loading choice adventure " + boxedChoice.choiceAdventure.choice );

				boxedChoice.setSelectedIndex( 0 );
				continue;
			}

			// handle if it's an option that's not shown as a CustomOption
			Option selec = boxedChoice.choiceAdventure.options.get( IntegerPool.get( index ) );
			if ( selec != null || selec == null && index == Integer.MAX_VALUE )
			{
				System.out.println( "Valid but hidden setting " + index + " for " + setting );
				boxedChoice.setSelectedItem( null );
				continue;
			}

			System.out.println( "Invalid setting " + index + " for " + setting );

			boxedChoice.setSelectedIndex( 0 );
		}

		// OceanDestinationComboBox handles its own settings.
		this.oceanDestSelect.loadSettings();

		String action = Preferences.getString( "oceanAction" );
		if ( action.equals( "continue" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 0 );
		}
		else if ( action.equals( "show" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 1 );
		}
		else if ( action.equals( "stop" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 2 );
		}
		else if ( action.equals( "savecontinue" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 3 );
		}
		else if ( action.equals( "saveshow" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 4 );
		}
		else if ( action.equals( "savestop" ) )
		{
			this.oceanActionSelect.setSelectedIndex( 5 );
		}

		this.isAdjusting = false;
		ActionPanel.enableActions( true );
	}

	public static class CommandButton
		extends JButton
		implements ActionListener
	{
		public CommandButton( String cmd )
		{
			super( cmd );

			this.setHorizontalAlignment( SwingConstants.LEFT );

			this.setActionCommand( cmd );
			this.addActionListener( this );
		}

		public void actionPerformed( ActionEvent e )
		{
			CommandDisplayFrame.executeCommand( e.getActionCommand() );
		}
	}
}
