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
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.SpringLayout;

// event listeners
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// containers
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

// other imports
import java.util.Date;
import com.sun.java.forums.SpringUtilities;
import net.java.dev.spellcast.utilities.SortedListModel;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * An extended <code>KoLFrame</code> which presents the user with the ability to
 * login to the Kingdom of Loathing.  Essentially, this class is a modification
 * of the <code>LoginDialog</code> class from the Spellcast project.
 */

public class LoginFrame extends KoLFrame
{
	private JComponent usernameField;
	private SortedListModel saveStateNames;

	public LoginFrame()
	{	this( new SortedListModel() );
	}

	/**
	 * Constructs a new <code>LoginFrame</code> which allows the user to
	 * log into the Kingdom of Loathing.  The <code>LoginFrame</code>
	 * assigns its <code>LoginPanel</code> as the content panel used by
	 * <code>KoLPanel</code> and other classes for updating its display,
	 * and derived classes may access the <code>LoginPanel</code> indirectly
	 * in this fashion.
	 */

	public LoginFrame( SortedListModel saveStateNames )
	{
		super( VERSION_NAME + ": Login" );
		tabs = new JTabbedPane();

		this.saveStateNames = new SortedListModel();
		this.saveStateNames.addAll( saveStateNames );

		tabs.addTab( "KoL Login", constructLoginPanel() );

		JPanel breakfastPanel = new JPanel();
		breakfastPanel.setLayout( new BoxLayout( breakfastPanel, BoxLayout.Y_AXIS ) );
		
		breakfastPanel.add( new BreakfastPanel( "Softcore Characters", "softcore" ) );
		breakfastPanel.add( new BreakfastPanel( "Hardcore Characters", "hardcore" ) );
		tabs.addTab( "Breakfast", breakfastPanel );
		
		JScrollPane scroller = new JScrollPane( new StartupFramesPanel(),
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		JComponentUtilities.setComponentSize( scroller, 300, 300 );
		tabs.addTab( "Displays", scroller );

		JPanel connectPanel = new JPanel();

		connectPanel.setLayout( new BoxLayout( connectPanel, BoxLayout.Y_AXIS ) );
		connectPanel.add( new UserInterfacePanel() );
		connectPanel.add( new ProxyOptionsPanel() );

		tabs.addTab( "Settings", connectPanel );

		framePanel.setLayout( new CardLayout( 10, 10 ) );
		framePanel.add( tabs, "" );
	}

	public void requestFocus()
	{
		super.requestFocus();
		usernameField.requestFocus();
	}

	public void dispose()
	{
		super.dispose();
		if ( StaticEntity.getClient().getPasswordHash() == null )
			System.exit( 0 );
	}

	public JPanel constructLoginPanel()
	{
		JPanel imagePanel = new JPanel( new BorderLayout( 0, 0 ) );
		imagePanel.add( new JLabel( " " ), BorderLayout.NORTH );
		imagePanel.add( new JLabel( JComponentUtilities.getImage( "limeglass.jpg" ), JLabel.CENTER ), BorderLayout.SOUTH );

		JPanel containerPanel = new JPanel( new BorderLayout() );
		containerPanel.add( imagePanel, BorderLayout.NORTH );
		containerPanel.add( new LoginPanel(), BorderLayout.CENTER );
		return containerPanel;
	}

	/**
	 * An internal class which represents the panel which is nested
	 * inside of the <code>LoginFrame</code>.
	 */

	private class LoginPanel extends KoLPanel implements ActionListener
	{
		private boolean isBreakfastEnabled = true;

		private JPasswordField passwordField;
		private ScriptSelectPanel scriptField;
		private JCheckBox savePasswordCheckBox;
		private JCheckBox autoLoginCheckBox;
		private JCheckBox getBreakfastCheckBox;
		
		/**
		 * Constructs a new <code>LoginPanel</code>, containing a place
		 * for the users to input their login name and password.  This
		 * panel, because it is intended to be the content panel for
		 * status message updates, also has a status label.
		 */

		public LoginPanel()
		{
			super( "login", "cancel" );

			usernameField = GLOBAL_SETTINGS.getProperty( "saveState" ).equals( "" ) ? (JComponent)(new JTextField()) : (JComponent)(new LoginNameComboBox());
			passwordField = new JPasswordField();
			scriptField = new ScriptSelectPanel( new JTextField() );
			
			savePasswordCheckBox = new JCheckBox();
			savePasswordCheckBox.addActionListener( this );

			autoLoginCheckBox = new JCheckBox();
			getBreakfastCheckBox = new JCheckBox();

			JPanel checkBoxPanels = new JPanel();
			checkBoxPanels.add( Box.createHorizontalStrut( 16 ) );
			checkBoxPanels.add( new JLabel( "Save Password: " ), "" );
			checkBoxPanels.add( savePasswordCheckBox );
			checkBoxPanels.add( Box.createHorizontalStrut( 16 ) );
			checkBoxPanels.add( new JLabel( "Auto-Login: " ), "" );
			checkBoxPanels.add( autoLoginCheckBox );
			checkBoxPanels.add( Box.createHorizontalStrut( 16 ) );
			checkBoxPanels.add( new JLabel( "Breakfast: " ), "" );
			checkBoxPanels.add( getBreakfastCheckBox );
			checkBoxPanels.add( Box.createHorizontalStrut( 16 ) );

			VerifiableElement [] elements = new VerifiableElement[3];
			elements[0] = new VerifiableElement( "Login: ", usernameField );
			elements[1] = new VerifiableElement( "Password: ", passwordField );
			elements[2] = new VerifiableElement( "On Login: ", scriptField );

			setContent( elements );

			actionStatusPanel.add( new JLabel( " ", JLabel.CENTER ), BorderLayout.CENTER );
			actionStatusPanel.add( checkBoxPanels, BorderLayout.NORTH );

			String autoLoginSetting = GLOBAL_SETTINGS.getProperty( "autoLogin" );
			if ( autoLoginSetting.equals( "" ) )
				autoLoginSetting = GLOBAL_SETTINGS.getProperty( "lastUsername" );
			else
				autoLoginCheckBox.setSelected( true );

			if ( usernameField instanceof JComboBox )
				((JComboBox)usernameField).setSelectedItem( autoLoginSetting );

			String passwordSetting = StaticEntity.getClient().getSaveState( autoLoginSetting );

			if ( passwordSetting != null )
			{
				passwordField.setText( passwordSetting );
				savePasswordCheckBox.setSelected( true );
			}

			getBreakfastCheckBox.setSelected( GLOBAL_SETTINGS.getProperty( "alwaysGetBreakfast" ).equals( "true" ) );
			setDefaultButton( confirmedButton );
		}

		public void setEnabled( boolean isEnabled )
		{
			super.setEnabled( isEnabled );
			usernameField.setEnabled( isEnabled );
			passwordField.setEnabled( isEnabled );

			scriptField.setEnabled( isEnabled && isBreakfastEnabled );
			savePasswordCheckBox.setEnabled( isEnabled );
			autoLoginCheckBox.setEnabled( isEnabled );
			getBreakfastCheckBox.setEnabled( isEnabled && isBreakfastEnabled );
		}

		protected void actionConfirmed()
		{
			GLOBAL_SETTINGS.setProperty( "alwaysGetBreakfast", String.valueOf( getBreakfastCheckBox.isSelected() ) );
			String username = ((String)(usernameField instanceof JComboBox ?
				((JComboBox)usernameField).getSelectedItem() : ((JTextField)usernameField).getText() ));

			String password = new String( passwordField.getPassword() );

			if ( username == null || password == null || username.equals("") || password.equals("") )
			{
				DEFAULT_SHELL.updateDisplay( ERROR_STATE, "Invalid login." );
				return;
			}

			if ( autoLoginCheckBox.isSelected() )
				GLOBAL_SETTINGS.setProperty( "autoLogin", username );
			else
				GLOBAL_SETTINGS.setProperty( "autoLogin", "" );

			GLOBAL_SETTINGS.setProperty( "loginScript." + username.toLowerCase(), scriptField.getText() );
			GLOBAL_SETTINGS.setProperty( "getBreakfast." + username.toLowerCase(), String.valueOf( getBreakfastCheckBox.isSelected() ) );

			(new LoginRequest( StaticEntity.getClient(), username, password, savePasswordCheckBox.isSelected(), getBreakfastCheckBox.isSelected() )).run();
			StaticEntity.getClient().enableDisplay();
		}

		protected void actionCancelled()
		{
			StaticEntity.getClient().declareWorldPeace();
			usernameField.requestFocus();
		}

		public void actionPerformed( ActionEvent e )
		{
			if ( !savePasswordCheckBox.isSelected() && usernameField instanceof JComboBox )
				StaticEntity.getClient().removeSaveState( (String) ((JComboBox)usernameField).getSelectedItem() );
		}

		/**
		 * Special instance of a JComboBox which overrides the default
		 * key events of a JComboBox to allow you to catch key events.
		 */

		/**
		 * Special instance of a JComboBox which overrides the default
		 * key events of a JComboBox to allow you to catch key events.
		 */

		private class LoginNameComboBox extends MutableComboBox
		{
			public LoginNameComboBox()
			{
				super( saveStateNames );
				this.getEditor().getEditorComponent().addKeyListener( new PasswordFocusListener() );
			}

			public void setSelectedItem( Object anObject )
			{
				super.setSelectedItem( anObject );
				setPassword();
			}

			public void focusLost( FocusEvent e )
			{
				super.focusLost( e );
				setPassword();
			}

			private void setPassword()
			{
				if ( currentMatch == null )
				{
					passwordField.setText( "" );
					setStatusMessage( " " );
					
					isBreakfastEnabled = true;
					LoginPanel.this.setEnabled( true );
					return;
				}

				String password = StaticEntity.getClient().getSaveState( currentMatch );
				if ( password == null )
				{
					passwordField.setText( "" );
					setStatusMessage( " " );

					isBreakfastEnabled = true;
					LoginPanel.this.setEnabled( true );
					return;
				}

				passwordField.setText( password );

				String todayString = sdf.format( new Date() );

				String loginScript = GLOBAL_SETTINGS.getProperty( "loginScript." + currentMatch.toLowerCase() );
				String breakfastSetting = GLOBAL_SETTINGS.getProperty( "getBreakfast." + currentMatch.toLowerCase() );
				
				scriptField.setText( loginScript == null ? "" : loginScript );
				getBreakfastCheckBox.setSelected( breakfastSetting != null && breakfastSetting.equals( "true" ) );

				String lastBreakfast = GLOBAL_SETTINGS.getProperty( "lastBreakfast." + currentMatch.toLowerCase() );
				isBreakfastEnabled = lastBreakfast == null || !lastBreakfast.equals( todayString );
				LoginPanel.this.setEnabled( true );

				setStatusMessage( isBreakfastEnabled ? " " : "Once-per-day options disabled (already executed today)" );
			}

			private class PasswordFocusListener extends KeyAdapter
			{
				public void keyReleased( KeyEvent e )
				{
					if ( e.getKeyCode() == KeyEvent.VK_ENTER )
						passwordField.requestFocus();
				}
			}
		}
	}
	
	private class BreakfastPanel extends LabeledKoLPanel
	{
		private String breakfastType;
		private JCheckBox [] skillOptions;
		
		public BreakfastPanel( String title, String breakfastType )
		{
			super( title, new Dimension( 380, 20 ), new Dimension( 20, 20 ) );
			
			this.breakfastType = breakfastType;
			skillOptions = new JCheckBox[ KoLmafia.BREAKFAST_SKILLS.length ];
			for ( int i = 0; i < KoLmafia.BREAKFAST_SKILLS.length; ++i )
				skillOptions[i] = new JCheckBox();
			
			VerifiableElement [] elements = new VerifiableElement[ skillOptions.length ];
			
			for ( int i = 0; i < elements.length; ++i )
				elements[i] = new VerifiableElement( KoLmafia.BREAKFAST_SKILLS[i][0], JLabel.LEFT, skillOptions[i] );
			
			setContent( elements, false );
			actionCancelled();
		}
		
		public void actionConfirmed()
		{
			StringBuffer skillString = new StringBuffer();

			for ( int i = 0; i < KoLmafia.BREAKFAST_SKILLS.length; ++i )
			{
				if ( skillOptions[i].isSelected() )
				{
					if ( skillString.length() != 0 )
						skillString.append( "," );

					skillString.append( KoLmafia.BREAKFAST_SKILLS[i][0] );
				}
			}

			GLOBAL_SETTINGS.setProperty( "breakfast." + breakfastType, skillString.toString() );
		}
		
		public void actionCancelled()
		{
			String skillString = GLOBAL_SETTINGS.getProperty( "breakfast." + breakfastType );
			for ( int i = 0; i < KoLmafia.BREAKFAST_SKILLS.length; ++i )
				skillOptions[i].setSelected( skillString != null && skillString.indexOf( KoLmafia.BREAKFAST_SKILLS[i][0] ) != -1 );
		}
	}

	private class StartupFramesPanel extends KoLPanel
	{
		private final String [][] FRAME_OPTIONS =
		{
			{ "Mini-Browser", "RequestFrame" },
			{ "Request Synch", "FightFrame" },
			{ "Relay Server", "LocalRelayServer" },

			{ "Adventure", "AdventureFrame" },
			{ "Purchases", "MallSearchFrame" },
			{ "Graphical CLI", "CommandDisplayFrame" },

			{ "Player Status", "CharsheetFrame" },
			{ "Item Manager", "ItemManageFrame" },
			{ "Gear Changer", "GearChangeFrame" },

			{ "Store Manager", "StoreManageFrame" },
			{ "Display Case", "MuseumFrame" },
			{ "Hagnk Storage", "HagnkStorageFrame" },

			{ "Meat Manager", "MeatManageFrame" },
			{ "Skill Casting", "SkillBuffPanel" },
			{ "Auto Restore", "RestoreOptionsFrame" },

			{ "Buffbot Manager", "BuffBotFrame" },
			{ "Purchase Buffs", "BuffRequestFrame" },

			{ "Flower Hunter", "FlowerHunterFrame" },
			{ "Mushroom Plot", "MushroomFrame" },
			{ "Familiar Trainer", "FamiliarTrainingFrame" },

			{ "IcePenguin Express", "MailboxFrame" },
			{ "KoLmafia Chat", "KoLMessenger" },
			{ "Recent Events", "EventsFrame" },

			{ "Clan Manager", "ClanManageFrame" },
			{ "Farmer's Almanac", "CalendarFrame" },
			{ "Encyclopedia", "ExamineItemsFrame" },

			{ "The Meatsink", "MoneyMakingGameFrame" },
		};

		private InterfaceRadioButton [] nullOptions;
		private InterfaceRadioButton [] startupOptions;
		private InterfaceRadioButton [] interfaceOptions;

		public StartupFramesPanel()
		{
			super( new Dimension( 380, 20 ), new Dimension( 20, 20 ) );

			nullOptions = new InterfaceRadioButton[ FRAME_OPTIONS.length ];
			startupOptions = new InterfaceRadioButton[ FRAME_OPTIONS.length ];
			interfaceOptions = new InterfaceRadioButton[ FRAME_OPTIONS.length ];

			JPanel contentPanel = new JPanel( new SpringLayout() );

			for ( int i = 0; i < FRAME_OPTIONS.length; ++i )
			{
				nullOptions[i] = new InterfaceRadioButton( "manual" );
				startupOptions[i] = new InterfaceRadioButton( "startup" );
				interfaceOptions[i] = new InterfaceRadioButton( "as tab" );

				ButtonGroup holder = new ButtonGroup();
				holder.add( nullOptions[i] );
				holder.add( startupOptions[i] );
				holder.add( interfaceOptions[i] );

				contentPanel.add( new JLabel( FRAME_OPTIONS[i][0] + ": ", JLabel.RIGHT ) );
				contentPanel.add( nullOptions[i] );
				contentPanel.add( startupOptions[i] );
				contentPanel.add( interfaceOptions[i] );
			}

			setContent( new VerifiableElement[0], false );

			SpringUtilities.makeCompactGrid( contentPanel, FRAME_OPTIONS.length, 4, 5, 5, 5, 5 );
			container.add( contentPanel, BorderLayout.CENTER );
			actionCancelled();
		}

		public void actionConfirmed()
		{
			StringBuffer frameString = new StringBuffer();
			StringBuffer desktopString = new StringBuffer();

			for ( int i = 0; i < FRAME_OPTIONS.length; ++i )
			{
				if ( startupOptions[i].isSelected() )
				{
					if ( frameString.length() != 0 )
						frameString.append( "," );
					frameString.append( FRAME_OPTIONS[i][1] );
				}

				if ( interfaceOptions[i].isSelected() )
				{
					if ( desktopString.length() != 0 )
						desktopString.append( "," );
					desktopString.append( FRAME_OPTIONS[i][1] );
				}
			}

			GLOBAL_SETTINGS.setProperty( "initialFrames", frameString.toString() );
			GLOBAL_SETTINGS.setProperty( "initialDesktopTabs", desktopString.toString() );
		}

		public void actionCancelled()
		{
			String frameString = GLOBAL_SETTINGS.getProperty( "initialFrames" );
			String desktopString = GLOBAL_SETTINGS.getProperty( "initialDesktopTabs" );

			for ( int i = 0; i < FRAME_OPTIONS.length; ++i )
			{
				startupOptions[i].setSelected( frameString.indexOf( FRAME_OPTIONS[i][1] ) != -1 );
				interfaceOptions[i].setSelected( desktopString.indexOf( FRAME_OPTIONS[i][1] ) != -1 );

				if ( !startupOptions[i].isSelected() && !interfaceOptions[i].isSelected() )
					nullOptions[i].setSelected( true );
			}
		}

		private class InterfaceRadioButton extends JRadioButton implements ActionListener
		{
			public InterfaceRadioButton( String text )
			{
				super( text );
				addActionListener( this );
			}
			
			public void actionPerformed( ActionEvent e )
			{	actionConfirmed();
			}
		}
	}

	/**
	 * Allows the user to select a server to use when
	 * using KoLmafia to login.  Also allows the user
	 * to select the framing mode to use.
	 */

	private class UserInterfacePanel extends LabeledKoLPanel
	{
		private JComboBox servers, textheavy, passwords;
		private JComboBox toolbars, windowing, trayicon;

		public UserInterfacePanel()
		{
			super( "User Interface", new Dimension( 80, 20 ), new Dimension( 280, 20 ) );

			servers = new JComboBox();
			servers.addItem( "Auto-select login server" );
			for ( int i = 1; i <= KoLRequest.SERVER_COUNT; ++i )
				servers.addItem( "Login using login server " + i );

			textheavy = new JComboBox();
			textheavy.addItem( "Use graphical side pane" );
			textheavy.addItem( "Use text-heavy side pane" );

			toolbars = new JComboBox();
			toolbars.addItem( "Show global menus only" );
			toolbars.addItem( "Put toolbar along top of panel" );
			toolbars.addItem( "Put toolbar along bottom of panel" );
			toolbars.addItem( "Put toolbar left of panel" );
			toolbars.addItem( "Put toolbar right of panel" );

			windowing = new JComboBox();
			windowing.addItem( "Allow one taskbar icon (requires restart)" );
			windowing.addItem( "Allow multiple taskbar icons (requires restart)" );

			trayicon = new JComboBox();
			trayicon.addItem( "Minimize KoLmafia to taskbar (requires restart)" );
			trayicon.addItem( "Minimize KoLmafia to system tray (requires restart)" );

			int elementCount = System.getProperty( "os.name" ).startsWith( "Windows" ) ? 4 : 3;

			VerifiableElement [] elements = new VerifiableElement[ elementCount ];
			elements[0] = new VerifiableElement( "Server: ", servers );
			elements[1] = new VerifiableElement( "Sidebar: ", textheavy );
			elements[2] = new VerifiableElement( "Toolbars: ", toolbars );

			if ( System.getProperty( "os.name" ).startsWith( "Windows" ) )
				elements[3] = new VerifiableElement( "SysTray: ", trayicon );

			setContent( elements );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			GLOBAL_SETTINGS.setProperty( "loginServer", String.valueOf( servers.getSelectedIndex() ) );
			GLOBAL_SETTINGS.setProperty( "useTextHeavySidepane", String.valueOf( textheavy.getSelectedIndex() == 1 ) );
			GLOBAL_SETTINGS.setProperty( "useToolbars", String.valueOf( toolbars.getSelectedIndex() != 0 ) );
			GLOBAL_SETTINGS.setProperty( "toolbarPosition", String.valueOf( toolbars.getSelectedIndex() ) );
			GLOBAL_SETTINGS.setProperty( "useSystemTrayIcon", String.valueOf( trayicon.getSelectedIndex() == 1 ) );
		}

		protected void actionCancelled()
		{
			textheavy.setSelectedIndex( GLOBAL_SETTINGS.getProperty( "useTextHeavySidepane" ).equals( "true" ) ? 1 : 0 );
			toolbars.setSelectedIndex( Integer.parseInt( GLOBAL_SETTINGS.getProperty( "toolbarPosition" ) ) );
			trayicon.setSelectedIndex( GLOBAL_SETTINGS.getProperty( "useSystemTrayIcon" ).equals( "true" ) ? 1 : 0 );
		}
	}

	/**
	 * This panel handles all of the things related to proxy
	 * options (if applicable).
	 */

	private class ProxyOptionsPanel extends LabeledKoLPanel
	{
		private ProxySettingsCheckBox proxySet;

		private JTextField proxyHost;
		private JTextField proxyPort;
		private JTextField proxyLogin;
		private JTextField proxyPassword;

		/**
		 * Constructs a new <code>ProxyOptionsPanel</code>, containing a
		 * place for the users to select their desired server and for them
		 * to modify any applicable proxy settings.
		 */

		public ProxyOptionsPanel()
		{
			super( "Proxy Setup", new Dimension( 80, 20 ), new Dimension( 240, 20 ) );

			proxySet = new ProxySettingsCheckBox();

			proxyHost = new JTextField();
			proxyPort = new JTextField();
			proxyLogin = new JTextField();
			proxyPassword = new JPasswordField();

			VerifiableElement [] elements = new VerifiableElement[5];
			elements[0] = new VerifiableElement( "Use Proxy: ", proxySet );
			elements[1] = new VerifiableElement( "Host: ", proxyHost );
			elements[2] = new VerifiableElement( "Port: ", proxyPort );
			elements[3] = new VerifiableElement( "Login: ", proxyLogin );
			elements[4] = new VerifiableElement( "Password: ", proxyPassword );

			setContent( elements, true );
			actionCancelled();
		}

		protected void actionConfirmed()
		{
			GLOBAL_SETTINGS.setProperty( "proxySet", String.valueOf( proxySet.isSelected() && proxyHost.getText().trim().length() > 0 ) );
			GLOBAL_SETTINGS.setProperty( "http.proxyHost", proxyHost.getText() );
			GLOBAL_SETTINGS.setProperty( "http.proxyPort", proxyPort.getText() );
			GLOBAL_SETTINGS.setProperty( "http.proxyUser", proxyLogin.getText() );
			GLOBAL_SETTINGS.setProperty( "http.proxyPassword", proxyPassword.getText() );
		}

		protected void actionCancelled()
		{
			proxySet.setSelected( GLOBAL_SETTINGS.getProperty( "proxySet" ).equals( "true" ) );
			proxyHost.setText( GLOBAL_SETTINGS.getProperty( "http.proxyHost" ) );
			proxyPort.setText( GLOBAL_SETTINGS.getProperty( "http.proxyPort" ) );
			proxyLogin.setText( GLOBAL_SETTINGS.getProperty( "http.proxyUser" ) );
			proxyPassword.setText( GLOBAL_SETTINGS.getProperty( "http.proxyPassword" ) );

			proxySet.actionPerformed( null );
		}

		private class ProxySettingsCheckBox extends JCheckBox implements ActionListener
		{
			public ProxySettingsCheckBox()
			{	addActionListener( this );
			}

			public void actionPerformed( ActionEvent e )
			{
				proxyHost.setEnabled( isSelected() );
				proxyPort.setEnabled( isSelected() );
				proxyLogin.setEnabled( isSelected() );
				proxyPassword.setEnabled( isSelected() );
			}
		}
	}
}
