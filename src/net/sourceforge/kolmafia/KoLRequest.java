/**
 * Copyright (c) 2005-2007, KoLmafia development team
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

package net.sourceforge.kolmafia;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import net.sourceforge.foxtrot.Job;
import com.velocityreviews.forums.HttpTimeoutClient;
import com.velocityreviews.forums.HttpTimeoutHandler;

public class KoLRequest extends Job implements KoLConstants
{
	private static int totalDelay = 0;
	private static long lastAdjustTime = Long.MAX_VALUE;

	private static int INITIAL_CACHE_COUNT = 3;
	private static int ADJUSTMENT_REFRESH = 60000;
	private static int MINIMUM_TOLERANCE = 2000;
	private static int MAXIMUM_TOLERANCE = 60000;

	private static int currentDelay = 800;
	private static int lagTolerance = MINIMUM_TOLERANCE;

	private static final Object WAIT_OBJECT = new Object();

	private static final ArrayList BYTEFLAGS = new ArrayList();
	private static final ArrayList BYTEARRAYS = new ArrayList();
	private static final ArrayList BYTESTREAMS = new ArrayList();

	static
	{
		for ( int i = 0; i < INITIAL_CACHE_COUNT; ++i )
			addAdditionalCache();
	}

	private static final AdventureResult MAIDEN_EFFECT = new AdventureResult( "Dreams and Lights", 1, true );
	private static final AdventureResult BALLROOM_KEY = new AdventureResult( 1766, 1 );

	private static final Pattern CHOICE_PATTERN = Pattern.compile( "whichchoice value=(\\d+)" );
	private static final Pattern CHOICE_DECISION_PATTERN = Pattern.compile( "whichchoice=(\\d+).*?option=(\\d+)" );
	private static final Pattern EVENT_PATTERN = Pattern.compile( "<table width=.*?<table><tr><td>(.*?)</td></tr></table>.*?<td height=4></td></tr></table>" );

	public static final Pattern REDIRECT_PATTERN = Pattern.compile( "([^\\/]+)\\/login\\.php", Pattern.DOTALL );

	public static String sessionId = null;
	public static String passwordHash = null;
	public static boolean isRatQuest = false;
	public static boolean handlingChoices = false;

	public static int lastChoice = 0;
	public static int lastDecision = 0;

	public String encounter = "";
	private boolean shouldIgnoreResult;

	public static boolean isCompactMode = false;

	public static final String [][] SERVERS =
	{
		{ "dev.kingdomofloathing.com", "69.16.150.202" },
		{ "www.kingdomofloathing.com", "69.16.150.196" },
		{ "www2.kingdomofloathing.com", "69.16.150.197" },
		{ "www3.kingdomofloathing.com", "69.16.150.198" },
		{ "www4.kingdomofloathing.com", "69.16.150.199" },
		{ "www5.kingdomofloathing.com", "69.16.150.200" },
		{ "www6.kingdomofloathing.com", "69.16.150.205" },
		{ "www7.kingdomofloathing.com", "69.16.150.206" },
		{ "www8.kingdomofloathing.com", "69.16.150.207" }
	};

	public static final int SERVER_COUNT = 8;

	public static String KOL_HOST = SERVERS[1][0];
	public static String KOL_ROOT = "http://" + SERVERS[1][1] + "/";

	public URL formURL;
	public boolean followRedirects;
	public String formURLString;

	public boolean isChatRequest = false;

	protected List data;
	private boolean dataChanged = true;
	private byte [] dataString = null;

	public boolean needsRefresh;
	public boolean statusChanged;

	private boolean isDelayExempt;
	public int responseCode;
	public String responseText;
	public HttpURLConnection formConnection;
	public String redirectLocation;

	/**
	 * Static method called when <code>KoLRequest</code> is first
	 * instantiated or whenever the settings have changed.  This
	 * initializes the login server to the one stored in the user's
	 * settings, as well as initializes the user's proxy settings.
	 */

	public static void applySettings()
	{
		applyProxySettings();

		int defaultLoginServer = StaticEntity.getIntegerProperty( "defaultLoginServer" );
		setLoginServer( SERVERS[ defaultLoginServer == 0 ? 0 : 1 ][0] );
	}

	private static void applyProxySettings()
	{
		if ( System.getProperty( "os.name" ).startsWith( "Mac" ) )
			return;

		try
		{
			String proxySet = StaticEntity.getProperty( "proxySet" );
			String proxyHost = StaticEntity.getProperty( "http.proxyHost" );
			String proxyUser = StaticEntity.getProperty( "http.proxyUser" );

			System.setProperty( "proxySet", proxySet );

			// Remove the proxy host from the system properties
			// if one isn't specified, or proxy setting is off.

			if ( proxySet.equals( "false" ) || proxyHost.equals( "" ) )
			{
				System.getProperties().remove( "http.proxyHost" );
				System.getProperties().remove( "http.proxyPort" );
			}
			else
			{
				try
				{
					System.setProperty( "http.proxyHost", InetAddress.getByName( proxyHost ).getHostAddress() );
				}
				catch ( UnknownHostException e )
				{
					// This should not happen.  Therefore, print
					// a stack trace for debug purposes.

					StaticEntity.printStackTrace( e, "Error in proxy setup" );
					System.setProperty( "http.proxyHost", proxyHost );
				}

				System.setProperty( "http.proxyPort", StaticEntity.getProperty( "http.proxyPort" ) );
			}

			// Remove the proxy user from the system properties
			// if one isn't specified, or proxy setting is off.

			if ( proxySet.equals( "false" ) || proxyHost.equals( "" ) || proxyUser.equals( "" ) )
			{
				System.getProperties().remove( "http.proxyUser" );
				System.getProperties().remove( "http.proxyPassword" );
			}
			else
			{
				System.setProperty( "http.proxyUser", StaticEntity.getProperty( "http.proxyUser" ) );
				System.setProperty( "http.proxyPassword", StaticEntity.getProperty( "http.proxyPassword" ) );
			}
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			e.printStackTrace();
		}
	}

	private static boolean substringMatches( String a, String b )
	{	return a.indexOf( b ) != -1 || b.indexOf( a ) != -1;
	}

	/**
	 * Static method used to manually set the server to be used as
	 * the root for all requests by all KoLmafia clients running
	 * on the current JVM instance.
	 *
	 * @param	server	The hostname of the server to be used.
	 */

	public static void setLoginServer( String server )
	{
		if ( server == null )
			return;

		for ( int i = 0; i < SERVERS.length; ++i )
		{
			if ( !substringMatches( server, SERVERS[i][0] ) && !substringMatches( server, SERVERS[i][1] ) )
				continue;

			KOL_HOST = SERVERS[i][0];
			KOL_ROOT = "http://" + SERVERS[i][1] + "/";

			StaticEntity.setProperty( "loginServerName", KOL_HOST );

			RequestLogger.printLine( "Redirected to " + KOL_HOST + "..." );
			System.setProperty( "http.referer", "http://" + KOL_HOST + "/main.php" );
		}
	}

	private static void chooseNewLoginServer()
	{
		KoLmafia.updateDisplay( "Choosing new login server..." );
		for ( int i = 0; i < SERVER_COUNT; ++i )
			if ( SERVERS[i][0].equals( KOL_HOST ) )
			{
				int next = ( i + 1 ) % SERVERS.length;
				KOL_HOST = SERVERS[next][0];
				KOL_ROOT = "http://" + SERVERS[next][1] + "/";
				return;
			}
	}

	/**
	 * Static method used to return the server currently used by
	 * this KoLmafia session.
	 *
	 * @return	The host name for the current server
	 */

	public static String getRootHostName()
	{	return KOL_HOST;
	}

	/**
	 * Constructs a new KoLRequest which will notify the given client
	 * of any changes and will use the given URL for data submission.
	 *
	 * @param	formURLString	The form to be used in posting data
	 */

	public KoLRequest( String formURLString )
	{	this( formURLString, false );
	}

	/**
	 * Constructs a new KoLRequest which will notify the given client
	 * of any changes and will use the given URL for data submission,
	 * possibly following redirects if the parameter so specifies.
	 *
	 * @param	formURLString	The form to be used in posting data
	 * @param	followRedirects	<code>true</code> if redirects are to be followed
	 */

	public KoLRequest( String formURLString, boolean followRedirects )
	{
		this.data = new ArrayList();
		this.followRedirects = followRedirects;

		this.constructURLString( formURLString );

		this.isDelayExempt = this.getClass() == KoLRequest.class || this instanceof LoginRequest || this instanceof LogoutRequest ||
			this instanceof ChatRequest || this instanceof CharpaneRequest || this instanceof LocalRelayRequest;
	}

	public boolean isDelayExempt()
	{	return this.isDelayExempt;
	}

	public void setDelayExempt( boolean isDelayExempt )
	{	this.isDelayExempt = isDelayExempt;
	}

	public KoLRequest constructURLString( String newURLString )
	{	return this.constructURLString( newURLString, true );
	}

	public KoLRequest constructURLString( String newURLString, boolean usePostMethod )
	{
		this.dataChanged = true;
		this.data.clear();

		if ( newURLString.startsWith( "/" ) )
			newURLString = newURLString.substring(1);

		int formSplitIndex = newURLString.indexOf( "?" );

		if ( formSplitIndex == -1 || !usePostMethod )
		{
			this.formURLString = newURLString;
		}
		else
		{
			this.formURLString = newURLString.substring( 0, formSplitIndex );
			this.addEncodedFormFields( newURLString.substring( formSplitIndex + 1 ) );
		}

		this.isChatRequest = this.formURLString.indexOf( "chat" ) != -1 && !this.formURLString.startsWith( "chatlaunch.php" ) &&
			!this.formURLString.startsWith( "lchat.php" ) && !this.formURLString.startsWith( "devchat.php" );

		this.shouldIgnoreResult = this.isChatRequest || this.formURLString.startsWith( "message" ) || this.formURLString.startsWith( "ascension" ) ||
			this.formURLString.startsWith( "search" ) || this.formURLString.startsWith( "static" ) || this.formURLString.startsWith( "desc" ) ||
			this.formURLString.startsWith( "show" ) || this.formURLString.startsWith( "doc" ) ||
			(this.formURLString.startsWith( "clan" ) && !this.formURLString.startsWith( "clan_stash" ) && !this.formURLString.startsWith( "clan_rumpus" ));

		return this;
	}

	public boolean ignoreResult()
	{	return this.shouldIgnoreResult;
	}

	/**
	 * Returns the location of the form being used for this URL, in case
	 * it's ever needed/forgotten.
	 */

	public String getURLString()
	{	return this.data.isEmpty() ? this.formURLString : this.formURLString + "?" + this.getDataString( false );
	}

	/**
	 * Clears the data fields so that the descending class
	 * can have a fresh set of data fields.  This allows
	 * requests with variable numbers of parameters to be
	 * reused.
	 */

	public void clearDataFields()
	{	this.data.clear();
	}

	/**
	 * Adds the given form field to the KoLRequest.  Descendant classes
	 * should use this method if they plan on submitting forms to Kingdom
	 * of Loathing before a call to the <code>super.run()</code> method.
	 * Ideally, these fields can be added at construction time.
	 *
	 * @param	name	The name of the field to be added
	 * @param	value	The value of the field to be added
	 * @param	allowDuplicates	true if duplicate names are OK
	 */

	public void addFormField( String name, String value, boolean allowDuplicates )
	{
		this.dataChanged = true;

		if ( name.equals( "pwd" ) || name.equals( "phash" ) )
		{
			this.data.add( name );
			return;
		}

		if ( name.equals( "playerid" ) && value.equals( "" ) )
		{
			this.data.add( "playerid=" + KoLCharacter.getUserId() );
			return;
		}

		String encodedName = name == null ? "" : name;
		String encodedValue = value == null ? "" : value;

		try
		{
			encodedName = URLEncoder.encode( encodedName, this instanceof LoginRequest ? "UTF-8" : "ISO-8859-1" ) + "=";
			encodedValue = URLEncoder.encode( encodedValue, this instanceof LoginRequest ? "UTF-8" : "ISO-8859-1" );
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
			return;
		}

		// Make sure that when you're adding data fields, you don't
		// submit duplicate fields.

		if ( !allowDuplicates )
			for ( int i = 0; i < this.data.size(); ++i )
				if ( ((String)this.data.get(i)).startsWith( encodedName ) )
					this.data.remove( i );

		// If the data did not already exist, then
		// add it to the end of the array.

		this.data.add( encodedName + encodedValue );
	}

	public void addFormField( String name, String value )
	{	this.addFormField( name, value, false );
	}

	/**
	 * Adds the given form field to the KoLRequest.
	 * @param	element	The field to be added
	 */

	public void addFormField( String element )
	{
		int equalIndex = element.indexOf( "=" );
		if ( equalIndex == -1 )
		{
			this.addFormField( element, "", false );
			return;
		}

		String name = element.substring( 0, equalIndex ).trim();
		String value = element.substring( equalIndex + 1 ).trim();

		if ( name.equals( "pwd" ) || name.equals( "phash" ) )
		{
			// If you were in Valhalla on login, then
			// make sure you discover the password hash
			// in some other way.

			if ( (passwordHash == null || passwordHash.equals( "" )) && value.length() != 0 )
				passwordHash = value;

			this.addFormField( name, "", false );
		}
		else
		{
			// Otherwise, add the name-value pair as was
			// specified in the original method.

			this.addFormField( name, value, true );
		}
	}

	/**
	 * Adds an already encoded form field to the KoLRequest.
	 * @param	element	The field to be added
	 */

	public void addEncodedFormField( String element )
	{
		if ( element == null )
			return;

		if ( element.startsWith( "pwd=" ) )
			element = "pwd";
		else if ( element.startsWith( "phash=" ) )
			element = "phash";

		this.data.add( element );
	}

	public void addEncodedFormFields( String fields )
	{
		if ( fields.indexOf( "&" ) == -1 )
		{
			this.addEncodedFormField( fields );
			return;
		}

		String [] tokens = fields.split( "&" );
		for ( int i = 0; i < tokens.length; ++i )
		{
			if ( tokens[i].indexOf( " " ) != -1 )
				this.addFormField( tokens[i] );
			else
				this.addEncodedFormField( tokens[i] );
		}
	}

	public String getFormField( String key )
	{
		if ( this.data.isEmpty() )
			return null;

		for ( int i = 0; i < this.data.size(); ++i )
		{
			int splitIndex = ((String)this.data.get(i)).indexOf( "=" );
			if ( splitIndex == -1 )
				continue;

			String name = ((String)this.data.get(i)).substring( 0, splitIndex );
			if ( !name.equalsIgnoreCase( key ) )
				continue;

			String value = ((String)this.data.get(i)).substring( splitIndex + 1 ) ;

			try
			{
				// Everything was encoded as ISO-8859-1, so go
				// ahead and decode it that way.

				return URLDecoder.decode( value, "ISO-8859-1" );
			}
			catch ( Exception e )
			{
				// This shouldn't happen, but since you did
				// manage to find the key, return the value.

				return value;
			}
		}

		return null;
	}

	private String getDataString( boolean includeHash )
	{
		StringBuffer dataBuffer = new StringBuffer();
		String [] elements = new String[ this.data.size() ];
		this.data.toArray( elements );

		for ( int i = 0; i < elements.length; ++i )
		{
			if ( i > 0 )
				dataBuffer.append( '&' );

			if ( elements[i].equals( "pwd" ) || elements[i].equals( "phash" ) )
			{
				dataBuffer.append( elements[i] );

				if ( includeHash )
				{
					dataBuffer.append( "=" );
					dataBuffer.append( passwordHash );
				}
			}
			else
				dataBuffer.append( elements[i] );
		}

		return dataBuffer.toString();
	}

	private boolean shouldUpdateDebugLog()
	{	return RequestLogger.isDebugging() && !this.isChatRequest;
	}

	/**
	 * Runs the thread, which prepares the connection for output, posts the data
	 * to the Kingdom of Loathing, and prepares the input for reading.  Because
	 * the Kingdom of Loathing has identical page layouts, all page reading and
	 * handling will occur through these method calls.
	 */

	public void run()
	{
		if ( sessionId == null && !(this instanceof LoginRequest || this instanceof LogoutRequest || this instanceof LocalRelayRequest) )
			return;

		String location = this.getURLString();

		if ( this.shouldUpdateDebugLog() )
			RequestLogger.updateDebugLog( this.getClass() );

		if ( location.startsWith( "sewer.php" ) )
		{
			if ( StaticEntity.getBooleanProperty( "relayAlwaysBuysGum" ) )
				DEFAULT_SHELL.executeLine( "acquire chewing gum on a string" );
		}
		else if ( location.startsWith( "hermit.php?autopermit=on" ) )
		{
			DEFAULT_SHELL.executeLine( "acquire hermit permit" );
		}

		else if ( location.startsWith( "casino.php" ) )
		{
			DEFAULT_SHELL.executeLine( "acquire casino pass" );
		}

		// To avoid wasting turns, buy a can of hair spray before
		// climbing the tower.  Also, if the person has an NG,
		// make sure to construct it first.  If there are any
		// tower items sitting in the closet or that have not
		// been constructed, pull them out.

		if ( location.startsWith( "lair4.php" ) || location.startsWith( "lair5.php" ) )
			SorceressLair.makeGuardianItems();

		this.needsRefresh = false;
		this.execute();

		if ( this.responseCode != 200 )
			return;

		// When following redirects, you will get different URL
		// strings, so make sure you update.

		if ( this.followRedirects )
			location = this.getURLString();

		boolean isQuestLocation = location.startsWith( "council" ) || location.startsWith( "guild" ) ||
			location.startsWith( "friars" ) || location.startsWith( "trapper" ) || location.startsWith( "bhh" ) ||
			(location.startsWith( "adventure" ) && location.indexOf( "=84" ) != -1);

		if ( isQuestLocation )
			CouncilFrame.handleQuestChange( location, responseText );

		// If this is an ascension, make sure to refresh the
		// session, be it relay or mini-browser.

		if ( location.equals( "main.php?refreshtop=true&noobmessage=true" ) )
			StaticEntity.getClient().handleAscension();

		// Once everything is complete, decide whether or not
		// you should refresh your status.

		if ( this.needsRefresh || this.statusChanged )
		{
			if ( RequestFrame.instanceExists() )
				RequestFrame.refreshStatus();
			else
				CharpaneRequest.getInstance().run();
		}
		else if ( this.formURLString.startsWith( "charpane.php" ) )
		{
			KoLCharacter.recalculateAdjustments();
			KoLCharacter.updateStatus();
		}
		else if ( !this.shouldIgnoreResult )
		{
			KoLCharacter.updateStatus();
		}

		if ( !(LoginRequest.isInstanceRunning() || this instanceof LogoutRequest) )
			ConcoctionsDatabase.refreshConcoctions();
	}

	public void execute()
	{
		String urlString = this.getURLString();

		// If this is the rat quest, then go ahead and pre-set the data
		// to reflect a fight sequence (mini-browser compatibility).

		isRatQuest |= urlString.startsWith( "rats.php" );
		if ( !this.isChatRequest && !urlString.startsWith( "charpane.php" ) && !urlString.startsWith( "rats.php" ) )
			isRatQuest &= urlString.startsWith( "fight.php" );

		if ( isRatQuest )
			KoLmafia.addTavernLocation( this );

		if ( !this.shouldIgnoreResult )
			RequestLogger.registerRequest( this, urlString );

		if ( urlString.startsWith( "choice.php" ) )
			this.saveLastChoice( urlString );

		if ( !this.isChatRequest && !urlString.startsWith( "charpane.php" ) )
			StaticEntity.getClient().setCurrentRequest( this );

		// If you're about to fight the Naughty Sorceress,
		// clear your list of effects.

		if ( urlString.startsWith( "lair6.php" ) && urlString.indexOf( "place=5" ) != -1 )
		{
			activeEffects.clear();
			this.needsRefresh = true;
		}

		if ( urlString.startsWith( "lair6.php" ) && urlString.indexOf( "place=6" ) != -1 )
		{
			KoLCharacter.setHardcore( false );
			KoLCharacter.setConsumptionRestriction( AscensionSnapshotTable.NOPATH );
			this.needsRefresh = true;
		}

		if ( urlString.startsWith( "ascend.php" ) )
		{
			if ( KoLCharacter.hasItem( KoLAdventure.MEATCAR ) )
				(new UntinkerRequest( KoLAdventure.MEATCAR.getItemId() )).run();

			ItemCreationRequest belt = ItemCreationRequest.getInstance( 677 );
			if ( belt != null && belt.getQuantityPossible() > 0 )
			{
				belt.setQuantityNeeded( belt.getQuantityPossible() );
				belt.run();
			}
		}

		this.statusChanged = false;

		do
		{
			if ( !this.prepareConnection() && KoLmafia.refusesContinue() )
				break;
		}
		while ( !this.postClientData() || !this.retrieveServerReply() );

		if ( System.currentTimeMillis() - ADJUSTMENT_REFRESH > lastAdjustTime )
		{
			lagTolerance -= MINIMUM_TOLERANCE;
			HttpTimeoutClient.setTimeout( lagTolerance );
			lastAdjustTime = lagTolerance == MINIMUM_TOLERANCE ? Long.MAX_VALUE : System.currentTimeMillis();
		}

		if ( this.isDelayExempt || this.responseCode != 200 )
			return;
	}

	private void saveLastChoice( String url )
	{
		Matcher choiceMatcher = CHOICE_DECISION_PATTERN.matcher( url );
		if ( choiceMatcher.find() )
		{
			lastChoice = StaticEntity.parseInt( choiceMatcher.group(1) );
			lastDecision = StaticEntity.parseInt( choiceMatcher.group(2) );

			switch ( lastChoice )
			{
			// Strung-Up Quartet
			case 106:

				if ( lastDecision != 4 )
				{
					StaticEntity.setProperty( "lastQuartetAscension", String.valueOf( KoLCharacter.getAscensions() ) );
					StaticEntity.setProperty( "lastQuartetRequest", String.valueOf( lastDecision ) );

					if ( KoLCharacter.recalculateAdjustments() )
						KoLCharacter.updateStatus();
				}

				break;

			// Wheel In the Sky Keep on Turning: Muscle Position
			case 9:
				StaticEntity.setProperty( "currentWheelPosition",
					String.valueOf( lastDecision == 1 ? "mysticality" : lastDecision == 2 ? "moxie" : "muscle" ) );
				break;

			// Wheel In the Sky Keep on Turning: Mysticality Position
			case 10:
				StaticEntity.setProperty( "currentWheelPosition",
					String.valueOf( lastDecision == 1 ? "map quest" : lastDecision == 2 ? "muscle" : "mysticality" ) );
				break;

			// Wheel In the Sky Keep on Turning: Map Quest Position
			case 11:
				StaticEntity.setProperty( "currentWheelPosition",
					String.valueOf( lastDecision == 1 ? "moxie" : lastDecision == 2 ? "mysticality" : "map quest" ) );
				break;

			// Wheel In the Sky Keep on Turning: Moxie Position
			case 12:
				StaticEntity.setProperty( "currentWheelPosition",
					String.valueOf( lastDecision == 1 ? "muscle" : lastDecision == 2 ? "map quest" : "moxie" ) );
				break;
			}
		}
	}

	private void mapCurrentChoice( String text )
	{
		// Let the Violet Fog handle this
		if ( VioletFog.mapChoice( text ) )
			return;

		// Let the Louvre handle this
		if ( Louvre.mapChoice( text ) )
			return;
	}

	public static int getLastChoice()
	{	return lastChoice;
	}

	public static int getLastDecision()
	{	return lastDecision;
	}

	public static boolean shouldIgnore( String formURLString )
	{
		return formURLString.indexOf( "mall" ) != -1 || formURLString.indexOf( "chat" ) != -1 ||
			formURLString.indexOf( "send" ) != -1 || formURLString.startsWith( "bhh" );
	}

	public static boolean delay()
	{
		totalDelay += currentDelay;
		return delay( currentDelay );
	}

	public static void printTotalDelay()
	{
		int seconds = totalDelay / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;

		RequestLogger.printLine();
		RequestLogger.printLine( "Delay between requests: " + (currentDelay / 1000.0f) + " seconds" );
		RequestLogger.printLine( "Delay added this session: " + minutes + " minutes, " + seconds + " seconds" );
		RequestLogger.printLine();
	}

	/**
	 * Utility method which waits for the given duration without
	 * using Thread.sleep() - this means CPU usage can be greatly
	 * reduced.
	 */

	public static boolean delay( long milliseconds )
	{
		if ( milliseconds <= 0 )
			return true;

		try
		{
			synchronized ( WAIT_OBJECT )
			{
				WAIT_OBJECT.wait( milliseconds );
				WAIT_OBJECT.notifyAll();
			}
		}
		catch ( InterruptedException e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
		}

		return true;
	}

	/**
	 * Utility method used to prepare the connection for input and output
	 * (if output is necessary).  The method attempts to open the connection,
	 * and then apply the needed settings.
	 *
	 * @return	<code>true</code> if the connection was successfully prepared
	 */

	private boolean prepareConnection()
	{
		if ( this.shouldUpdateDebugLog() )
			RequestLogger.updateDebugLog( "Connecting to " + this.formURLString + "..." );

		// Make sure that all variables are reset before you reopen
		// the connection.

		this.responseCode = 0;
		this.responseText = null;
		this.redirectLocation = null;
		this.formConnection = null;

		try
		{
			// For now, because there isn't HTTPS support, just open the
			// connection and directly cast it into an HttpURLConnection

			this.formURL = null;

			if ( StaticEntity.getBooleanProperty( "testSocketTimeout" ) )
			{
				if ( this.formURLString.startsWith( "http:" ) )
					this.formURL = new URL( null, this.formURLString, HttpTimeoutHandler.getInstance() );
				else
					this.formURL = new URL( null, KOL_ROOT + this.formURLString, HttpTimeoutHandler.getInstance() );
			}
			else
			{
				if ( this.formURLString.startsWith( "http:" ) )
					this.formURL = new URL( this.formURLString );
				else
					this.formURL = new URL( KOL_ROOT + this.formURLString );
			}

			this.formConnection = (HttpURLConnection) this.formURL.openConnection();
		}
		catch ( Exception e )
		{
			// In the event that an Exception is thrown, one can assume
			// that there was a timeout; return false and let the loop
			// attempt to connect again

			if ( this.shouldUpdateDebugLog() )
				RequestLogger.updateDebugLog( "Error opening connection.  Retrying..." );

			if ( this instanceof LoginRequest )
				chooseNewLoginServer();

			delay( lagTolerance );
			return false;
		}

		this.formConnection.setDoInput( true );
		this.formConnection.setDoOutput( !this.data.isEmpty() );
		this.formConnection.setUseCaches( false );
		this.formConnection.setInstanceFollowRedirects( false );

		if ( sessionId != null )
		{
			if ( this.formURLString.startsWith( "inventory.php" ) && !StaticEntity.getProperty( "visibleBrowserInventory" ).equals( "" ) )
				this.formConnection.addRequestProperty( "Cookie", StaticEntity.getProperty( "visibleBrowserInventory" ) + "; " + sessionId );
			else
				this.formConnection.addRequestProperty( "Cookie", sessionId );
		}

		this.formConnection.setRequestProperty( "User-Agent", VERSION_NAME );

		if ( this.dataChanged )
		{
			this.dataChanged = false;
			this.dataString = this.getDataString( true ).getBytes();
		}

		this.formConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );

		if ( !this.data.isEmpty() )
			this.formConnection.setRequestProperty( "Content-Length", String.valueOf( this.dataString.length ) );
		else
			this.formConnection.setRequestProperty( "Content-Length", "0" );


		return true;
	}

	/**
	 * Utility method used to post the client's data to the Kingdom of
	 * Loathing server.  The method grabs all form fields added so far
	 * and posts them using the traditional ampersand style of HTTP
	 * requests.
	 *
	 * @return	<code>true</code> if all data was successfully posted
	 */

	private boolean postClientData()
	{
		if ( this.shouldUpdateDebugLog() )
			this.printRequestProperties();

		// Only attempt to post something if there's actually
		// data to post - otherwise, opening an input stream
		// should be enough

		if ( this.data.isEmpty() )
			return true;

		try
		{
			this.formConnection.setRequestMethod( "POST" );

			if ( this.shouldUpdateDebugLog() )
				this.printRequestProperties();

			OutputStream ostream = this.formConnection.getOutputStream();
			ostream.write( this.dataString );

			ostream.flush();
			ostream.close();

			ostream = null;
			return true;
		}
		catch ( Exception e )
		{
			RequestLogger.printLine( "Time out during data post (" + this.formURLString + ").  This could be bad..." );

			if ( this.shouldUpdateDebugLog() )
				RequestLogger.updateDebugLog( "Connection timed out during post.  Retrying..." );

			if ( this instanceof LoginRequest )
				chooseNewLoginServer();

			delay( lagTolerance );
			return false;
		}
	}

	/**
	 * Utility method used to retrieve the server's reply.  This method
	 * detects the nature of the reply via the response code provided
	 * by the server, and also detects the unusual states of server
	 * maintenance and session timeout.  All data retrieved by this
	 * method is stored in the instance variables for this class.
	 *
	 * @return	<code>true</code> if the data was successfully retrieved
	 */

	private boolean retrieveServerReply()
	{
		InputStream istream = null;

		// In the event of redirects, the appropriate flags should be set
		// indicating whether or not the direct is a normal redirect (ie:
		// one that results in something happening), or an error-type one
		// (ie: maintenance).

		if ( this.shouldUpdateDebugLog() )
			RequestLogger.updateDebugLog( "Retrieving server reply..." );

		this.responseText = "";
		this.redirectLocation = "";

		try
		{
			istream = this.formConnection.getInputStream();
			this.responseCode = this.formConnection.getResponseCode();
			this.redirectLocation = this.formConnection.getHeaderField( "Location" );
		}
		catch ( Exception e1 )
		{
			boolean shouldRetry = retryOnTimeout();

			if ( shouldRetry )
				RequestLogger.printLine( "Time out during response (" + this.formURLString + ").  This could be bad..." );
			else
				RequestLogger.printLine( "Time out during response (" + this.formURLString + ").  Redirecting..." );

			if ( !isChatRequest && this.shouldUpdateDebugLog() )
				RequestLogger.updateDebugLog( "Connection timed out during response." );

			lagTolerance = Math.min( MAXIMUM_TOLERANCE, lagTolerance + MINIMUM_TOLERANCE );
			HttpTimeoutClient.setTimeout( lagTolerance );
			lastAdjustTime = System.currentTimeMillis();

			try
			{
				if ( istream != null )
					istream.close();
			}
			catch ( Exception e2 )
			{
				// The input stream was already closed.  Ignore this
				// error and continue.
			}

			if ( this instanceof LoginRequest )
				chooseNewLoginServer();

			if ( shouldRetry )
			{
				delay( lagTolerance );
				return false;
			}

			this.responseCode = 302;
			this.redirectLocation = "main.php";
		}

		if ( this.shouldUpdateDebugLog() )
			this.printHeaderFields();

		boolean shouldStop = false;

		try
		{
			if ( this.responseCode == 200 )
			{
				shouldStop = this.retrieveServerReply( istream );
				istream.close();
			}
			else
			{
				// If the response code is not 200, then you've read all
				// the information you need.  Close the input stream.

				istream.close();
				shouldStop = this.responseCode == 302 ? this.handleServerRedirect() : true;
			}
		}
		catch ( Exception e )
		{
			// Do nothing, you're going to close the input stream
			// and nullify it in the next section.

			return true;
		}

		istream = null;
		return shouldStop;
	}

	protected boolean retryOnTimeout()
	{	return data.isEmpty() || this.getClass() == KoLRequest.class;
	}

	private boolean handleServerRedirect()
	{
		if ( this.redirectLocation == null )
			return true;

		// Check to see if this is a login page redirect.  If it is, then
		// construct the URL string and notify the browser that it should
		// change everything.

		Matcher matcher = REDIRECT_PATTERN.matcher( this.redirectLocation );

		int lastSlashIndex = this.redirectLocation.lastIndexOf( "/" );

		if ( lastSlashIndex != -1 )
			this.redirectLocation = this.redirectLocation.substring( lastSlashIndex + 1 );

		if ( matcher.find() )
		{
			setLoginServer( matcher.group(1) );
			this.constructURLString( this.redirectLocation, false );
			return false;
		}

		if ( sessionId == null && this.redirectLocation.startsWith( "login.php" ) )
		{
			this.constructURLString( this.redirectLocation, false );
			return false;
		}

		if ( this instanceof LocalRelayRequest )
		{
			if ( this.formURLString.startsWith( "login.php" ) )
				LoginRequest.processLoginRequest( this );

			return true;
		}

		if ( this.formURLString.startsWith( "fight.php" ) )
		{
			FightRequest.updateCombatData( this.encounter, "" );
			return true;
		}

		if ( this.followRedirects )
		{
			// Re-setup this request to follow the redirect
			// desired and rerun the request.

			this.constructURLString( this.redirectLocation, false );
			return false;
		}

		if ( this.redirectLocation.startsWith( "fight.php" ) )
		{
			if ( this instanceof UseSkillRequest || this instanceof ConsumeItemRequest )
				return true;

			// You have been redirected to a fight!  Here, you need
			// to complete the fight before you can continue.

			FightRequest.INSTANCE.run();
			return this instanceof AdventureRequest;
		}

		if ( this.redirectLocation.startsWith( "login.php" ) && !(this instanceof ChatRequest) )
		{
			LoginRequest.executeTimeInRequest();
			return sessionId == null;
		}

		if ( this.redirectLocation.startsWith( "maint.php" ) )
		{
			// If the system is down for maintenance, the user must be
			// notified that they should try again later.

			KoLmafia.updateDisplay( ABORT_STATE, "Nightly maintenance." );
			return true;
		}

		if ( this.redirectLocation.startsWith( "choice.php" ) )
		{
			handlingChoices = true;
			this.processChoiceAdventure();
			handlingChoices = false;

			return true;
		}

		if ( this.redirectLocation.startsWith( "valhalla.php" ) )
		{
			passwordHash = "";
			return true;
		}

		if ( this.shouldUpdateDebugLog() )
			RequestLogger.updateDebugLog( "Redirected: " + this.redirectLocation );

		return true;
	}

	private static void addAdditionalCache()
	{
		synchronized ( BYTEFLAGS )
		{
			BYTEFLAGS.add( Boolean.TRUE );
			BYTEARRAYS.add( new byte[ 8096 ] );
			BYTESTREAMS.add( new ByteArrayOutputStream( 8096 ) );
		}
	}

	private boolean retrieveServerReply( InputStream istream ) throws Exception
	{
		// Find an available byte array in order to buffer the data.  Allow
		// this to scale based on the number of incoming requests in order
		// to reduce the probability that the program hangs.

		int desiredIndex = -1;

		synchronized ( BYTEFLAGS )
		{
			for ( int i = 0; desiredIndex == -1 && i < BYTEFLAGS.size(); ++i )
				if ( BYTEFLAGS.get(i) == Boolean.FALSE )
					desiredIndex = i;
		}

		if ( desiredIndex == -1 )
		{
			desiredIndex = BYTEFLAGS.size();
			addAdditionalCache();
		}
		else
		{
			BYTEFLAGS.set( desiredIndex, Boolean.TRUE );
		}

		// Read all the data into the static byte array output stream and then
		// convert that string to UTF-8.

		byte [] array = (byte []) BYTEARRAYS.get( desiredIndex );
		ByteArrayOutputStream stream = (ByteArrayOutputStream) BYTESTREAMS.get( desiredIndex );

		int availableBytes = 0;
		while ( (availableBytes = istream.read( array )) != -1 )
			stream.write( array, 0, availableBytes );

		this.responseText = stream.toString( "UTF-8" );
		stream.reset();

		// You are now done with the array.  Go ahead and reset the value
		// to false to let the program know the objects are available to
		// be reused.

		BYTEFLAGS.set( desiredIndex, Boolean.FALSE );
		this.processResponse();
		return true;
	}

	/**
	 * This method allows classes to process a raw, unfiltered
	 * server response.
	 */

	public void processResponse()
	{
		if ( this.responseText == null )
			return;

		if ( this.shouldUpdateDebugLog() )
			RequestLogger.updateDebugLog( LINE_BREAK_PATTERN.matcher( this.responseText ).replaceAll( "" ) );

		this.statusChanged = this.responseText.indexOf( "charpane.php" ) != -1;
		if ( this.statusChanged && !(this instanceof LocalRelayRequest) )
			LocalRelayServer.addStatusMessage( "<!-- REFRESH -->" );

		if ( !isChatRequest )
			this.checkForNewEvents();

		if ( isRatQuest )
			KoLmafia.addTavernLocation( this );

		this.encounter = AdventureRequest.registerEncounter( this );

		if ( this.formURLString.equals( "fight.php" ) )
			FightRequest.updateCombatData( this.encounter, this.responseText );

		if ( !this.shouldIgnoreResult )
			this.parseResults();

		if ( !LoginRequest.isInstanceRunning() && !(this instanceof LocalRelayRequest) && !(this instanceof CharpaneRequest) && !this.isChatRequest && this.formURLString.indexOf( "search" ) == -1 )
			this.showInBrowser( false );

		// Now let the main method of result processing for
		// each request type happen.

		this.processResults();

		// Let the mappers do their work
		this.mapCurrentChoice( this.responseText );

		if ( AdventureRequest.useMarmotClover( this.formURLString, this.responseText ) || HermitRequest.useHermitClover( this.formURLString ) )
			DEFAULT_SHELL.executeLine( "use * ten-leaf clover" );

		this.needsRefresh &= !(this.getClass() == KoLRequest.class || this instanceof LocalRelayRequest || this instanceof FightRequest);
		this.needsRefresh &= !this.formURLString.startsWith( "charpane.php" );

		this.statusChanged &= !this.formURLString.startsWith( "charpane.php" );
		KoLmafia.applyEffects();
	}

	/**
	 * Utility method used to skip the given number of tokens within
	 * the provided <code>StringTokenizer</code>.  This method is used
	 * in order to clarify what's being done, rather than calling
	 * <code>st.nextToken()</code> repeatedly.
	 *
	 * @param	st	The <code>StringTokenizer</code> whose tokens are to be skipped
	 * @param	tokenCount	The number of tokens to skip
	 */

	public static final void skipTokens( StringTokenizer st, int tokenCount )
	{
		for ( int i = 0; i < tokenCount; ++i )
			st.nextToken();
	}

	/**
	 * Utility method used to transform the next token on the given
	 * <code>StringTokenizer</code> into an integer.  Because this
	 * is used repeatedly in parsing, its functionality is provided
	 * globally to all instances of <code>KoLRequest</code>.
	 *
	 * @param	st	The <code>StringTokenizer</code> whose next token is to be retrieved
	 * @return	The integer token, if it exists, or 0, if the token was not a number
	 */

	public static final int intToken( StringTokenizer st )
	{	return intToken( st, 0 );
	}

	/**
	 * Utility method used to transform the next token on the given
	 * <code>StringTokenizer</code> into an integer; however, this
	 * differs in the single-argument version in that only a part
	 * of the next token is needed.  Because this is also used
	 * repeatedly in parsing, its functionality is provided globally
	 * to all instances of <code>KoLRequest</code>.
	 *
	 * @param	st	The <code>StringTokenizer</code> whose next token is to be retrieved
	 * @param	fromStart	The index at which the integer to parse begins
	 * @return	The integer token, if it exists, or 0, if the token was not a number
	 */

	public static final int intToken( StringTokenizer st, int fromStart )
	{
		String token = st.nextToken().substring( fromStart );
		return StaticEntity.parseInt( token );
	}

	/**
	 * Utility method used to transform part of the next token on the
	 * given <code>StringTokenizer</code> into an integer.  This differs
	 * from the two-argument in that part of the end of the string is
	 * expected to contain non-numeric values as well.  Because this is
	 * also repeatedly in parsing, its functionality is provided globally
	 * to all instances of <code>KoLRequest</code>.
	 *
	 * @param	st	The <code>StringTokenizer</code> whose next token is to be retrieved
	 * @param	fromStart	The index at which the integer to parse begins
	 * @param	fromEnd	The distance from the end at which the first non-numeric character is found
	 * @return	The integer token, if it exists, or 0, if the token was not a number
	 */

	public static final int intToken( StringTokenizer st, int fromStart, int fromEnd )
	{
		String token = st.nextToken();
		token = token.substring( fromStart, token.length() - fromEnd );
		return StaticEntity.parseInt( token );
	}

	/**
	 * An alternative method to doing adventure calculation is determining
	 * how many adventures are used by the given request, and subtract
	 * them after the request is done.  This number defaults to <code>zero</code>;
	 * overriding classes should change this value to the appropriate
	 * amount.
	 *
	 * @return	The number of adventures used by this request.
	 */

	public int getAdventuresUsed()
	{	return this.formURLString.startsWith( "choice.php" ) ? 1 : 0;
	}

	public void parseResults()
	{
		// If this is a lucky adventure, then remove a clover
		// from the player's inventory -- this will occur when
		// you see either "Your ten-leaf clover" or "your
		// ten-leaf clover" (shorten to "our ten-leaf clover"
		// for substring matching)

		if ( this.responseText.indexOf( "our ten-leaf clover" ) != -1 && this.responseText.indexOf( "puff of smoke" ) != -1 )
			StaticEntity.getClient().processResult( SewerRequest.CLOVER );

		if ( this.formURLString.startsWith( "sewer.php" ) && this.responseText.indexOf( "You acquire" ) != -1 )
			StaticEntity.getClient().processResult( SewerRequest.GUM );

		int previousHP = KoLCharacter.getCurrentHP();
		this.needsRefresh |= StaticEntity.getClient().processResults( this.responseText );
		this.needsRefresh |= this.getAdventuresUsed() > 0;

		// If the character's health drops below zero, make sure
		// that beaten up is added to the effects.

		if ( previousHP != 0 && KoLCharacter.getCurrentHP() == 0 )
		{
			// Wild hare is exempt from beaten up status if you
			// are beaten up in the middle of a battle.

			if ( !this.formURLString.equals( "fight.php" ) || this.responseText.indexOf( "lair6.php" ) != -1 )
				this.needsRefresh |= StaticEntity.getClient().processResult( KoLAdventure.BEATEN_UP.getInstance( 4 - KoLAdventure.BEATEN_UP.getCount( activeEffects ) ) );
			else if ( KoLCharacter.getFamiliar().getId() != 50 )
				this.needsRefresh |= StaticEntity.getClient().processResult( KoLAdventure.BEATEN_UP.getInstance( 3 - KoLAdventure.BEATEN_UP.getCount( activeEffects ) ) );
		}
	}

	public void processResults()
	{
	}

	/**
	 * Utility method which notifies thethat it needs to process
	 * the given choice adventure.
	 */

	public void processChoiceAdventure()
	{
		// You can no longer simply ignore a choice adventure.	One of
		// the options may have that effect, but we must at least run
		// choice.php to find out which choice it is.

		StaticEntity.getClient().processResult( new AdventureResult( AdventureResult.CHOICE, 1 ) );

		KoLRequest request = new KoLRequest( this.redirectLocation );
		request.run();

		String choice = null;
		String option = null;
		String decision = null;

		while ( request.responseText.indexOf( "choice.php" ) != -1 )
		{
			// Slight delay before each choice is made

			KoLRequest.delay();
			Matcher choiceMatcher = CHOICE_PATTERN.matcher( request.responseText );

			if ( !choiceMatcher.find() )
			{
				// choice.php did not offer us any choices. This would
				// be a bug in KoL itself. Bail now and let the user
				// finish by hand.

				KoLmafia.updateDisplay( ABORT_STATE, "Encountered choice adventure with no choices." );
				request.showInBrowser( true );
				return;
			}

			choice = choiceMatcher.group(1);
			option = "choiceAdventure" + choice;
			decision = StaticEntity.getProperty( option );

			// If this happens to be adventure 26 or 27,
			// check against the player's conditions.

			if ( (choice.equals( "26" ) || choice.equals( "27" )) && !conditions.isEmpty() )
			{
				for ( int i = 0; i < 12; ++i )
					if ( AdventureDatabase.WOODS_ITEMS[i].getCount( conditions ) > 0 )
						decision = choice.equals( "26" ) ? String.valueOf( (i / 4) + 1 ) : String.valueOf( ((i % 4) / 2) + 1 );
			}

			// If the player is looking for the ballroom key,
			// then update their preferences so that KoLmafia
			// automatically switches things for them.

			if ( choice.equals( "85" ) && conditions.contains( BALLROOM_KEY ) )
				StaticEntity.setProperty( option, decision.equals( "1" ) ? "2" : "1" );

			// Certain choices should always be taken.  These
			// choices are handled here.

			else if ( choice.equals( "7" ) )
			{
				decision = "1";
			}

			// Sometimes, the choice adventure for the louvre
			// loses track of whether to ignore the louvre or not.

			else if ( choice.equals( "91" ) )
			{
				decision = StaticEntity.getIntegerProperty( "louvreDesiredGoal" ) != 0 ? "1" : "2";
			}

			// If there is no setting which determines the
			// decision, see if it's in the violet fog

			if ( decision.equals( "" ) )
				decision = VioletFog.handleChoice( choice );

			// If there is no setting which determines the
			// decision, see if it's in the Louvre

			if ( decision.equals( "" ) )
				decision = Louvre.handleChoice( choice );

			// If there is currently no setting which determines the
			// decision, give an error and bail.

			if ( decision.equals( "" ) )
			{
				KoLmafia.updateDisplay( ABORT_STATE, "Unsupported choice adventure #" + choice );
				request.showInBrowser( true );

				StaticEntity.printRequestData( request );
				return;
			}

			// If the user wants to ignore this specific choice or all
			// choices, see if this choice is ignorable.

			boolean willIgnore = false;

			// But first, handle the maidens adventure in a less random
			// fashion that's actually useful.

			if ( choice.equals( "89" ) )
			{
				willIgnore = true;

				switch ( StaticEntity.parseInt( decision ) )
				{
				case 0:
					decision = String.valueOf( RNG.nextInt(2) + 1 );
					break;
				case 1:
				case 2:
					break;
				case 3:
					decision = activeEffects.contains( MAIDEN_EFFECT ) ? String.valueOf( RNG.nextInt(2) + 1 ) : "3";
					break;
				case 4:
					decision = activeEffects.contains( MAIDEN_EFFECT ) ? "1" : "3";
					break;
				case 5:
					decision = activeEffects.contains( MAIDEN_EFFECT ) ? "2" : "3";
					break;
				}
			}
			else if ( decision.equals( "0" ) )
			{
				String ignoreChoice = AdventureDatabase.ignoreChoiceOption( option );
				if ( ignoreChoice != null )
				{
					willIgnore = true;
					decision = ignoreChoice;
				}
			}

			// Always change the option whenever it's not an ignore option
			// and remember to store the result.

			if ( !willIgnore )
				decision = this.pickOutfitChoice( option, decision );

			request.clearDataFields();
			request.addFormField( "pwd" );
			request.addFormField( "whichchoice", choice );
			request.addFormField( "option", decision );
			request.run();
		}

		// Manually process any adventure usage for choice adventures,
		// since they necessarily consume an adventure.

		if ( AdventureDatabase.consumesAdventure( option, decision ) )
			this.needsRefresh = !request.needsRefresh;
	}

	private String pickOutfitChoice( String option, String decision )
	{
		// Find the options for the choice we've encountered

		String [] possibleDecisions = null;
		for ( int i = 0; i < AdventureDatabase.CHOICE_ADVS.length; ++i )
		{
			if ( AdventureDatabase.CHOICE_ADVS[i].getSetting().equals( option ) )
			{
				possibleDecisions = AdventureDatabase.CHOICE_ADVS[i].getItems();
				break;
			}
		}

		// If it's not in the table (the castle wheel, for example) or
		// isn't an outfit completion choice, return the player's
		// chosen decision.

		if ( possibleDecisions == null )
			return decision.equals( "0" ) ? "1" : decision;

		// Choose an item in the conditions first, if it's available.
		// This allows conditions to override existing choices.

		for ( int i = 0; i < possibleDecisions.length; ++i )
		{
			if ( possibleDecisions[i] != null )
			{
				AdventureResult item = new AdventureResult( StaticEntity.parseInt( possibleDecisions[i] ), 1 );
				if ( conditions.contains( item ) )
					return String.valueOf( i + 1 );

				if ( decision.equals( "4" ) && !KoLCharacter.hasItem( item ) )
					return String.valueOf( i + 1 );
			}
		}

		// If no item is found in the conditions list, and the player
		// has a non-ignore decision, go ahead and use it.

		if ( !decision.equals( "0" ) && StaticEntity.parseInt( decision ) - 1 < possibleDecisions.length )
			return decision;

		// Choose a null choice if no conditions match what you're
		// trying to look for.

		for ( int i = 0; i < possibleDecisions.length; ++i )
			if ( possibleDecisions[i] == null )
				return String.valueOf( i + 1 );

		// If they have everything and it's an ignore choice, then use
		// the first choice no matter what.

		return "1";
	}

	/*
	 * Method to display the current request in the Fight Frame.
	 *
	 * If we are synchronizing, show all requests
	 * If we are finishing, show only exceptional requests
	 */

	public void showInBrowser( boolean exceptional )
	{
		// Check to see if this request should be showed
		// in a browser.  If you're using a command-line
		// interface, then you should not display the request.

		if ( existingFrames.isEmpty() )
			return;

		if ( !exceptional && !StaticEntity.getBooleanProperty( "showAllRequests" ) )
			return;

		// Only show the request if the response code is
		// 200 (not a redirect or error).


		FightFrame.showRequest( this );
	}

	private void checkForNewEvents()
	{
		if ( this.responseText.indexOf( "bgcolor=orange><b>New Events:</b>") == -1 )
			return;

		// Capture the entire new events table in order to display the
		// appropriate message.

		Matcher eventMatcher = EVENT_PATTERN.matcher( this.responseText );
		if ( !eventMatcher.find() )
			return;

		// Make an array of events
		String [] events = eventMatcher.group(1).replaceAll( "<br>", "\n" ).split( "\n" );

		for ( int i = 0; i < events.length; ++i )
			if ( events[i].indexOf( "/" ) == -1 )
				events[i] = null;

		// Remove the events from the response text
		this.responseText = eventMatcher.replaceFirst( "" );
		boolean shouldLoadEventFrame = false;

		for ( int i = 0; i < events.length; ++i )
		{
			if ( events[i] == null )
				continue;

			if ( events[i].indexOf( "logged" ) != -1 )
				continue;

			shouldLoadEventFrame = true;
			String event = events[i];

			// The event may be marked up with color and links to
			// user profiles. For example:

			// 04/25/06 12:53:54 PM - New message received from <a target=mainpane href='showplayer.php?who=115875'><font color=green>Brianna</font></a>.
			// 04/25/06 01:06:43 PM - <a class=nounder target=mainpane href='showplayer.php?who=115875'><b><font color=green>Brianna</font></b></a> has played a song (The Polka of Plenty) for you.

			// Add in a player Id so that the events can be handled
			// using a ShowDescriptionList.

			event = event.replaceAll( "</a>", "<a>" ).replaceAll( "<[^a].*?>", " " ).replaceAll( "\\s+", " " );
			event = event.replaceAll( "<a[^>]*showplayer\\.php\\?who=(\\d+)[^>]*>(.*?)<a>", "$2 (#$1)" );

			if ( event.indexOf( "/" ) == -1 )
				continue;

			// If it's a song or a buff, must update status

			// <name> has played a song (The Ode to Booze) for you
			// An Elemental Saucesphere has been conjured around you by <name>
			// <name> has imbued you with Reptilian Fortitude
			// <name> has given you the Tenacity of the Snapper
			// <name> has fortified you with Empathy of the Newt

			if ( event.indexOf( " has " ) != -1 )
				this.needsRefresh = true;

			// Add the event to the event list
			eventHistory.add( event );

			// Print everything to the default shell; this way, the
			// graphical CLI is also notified of events.

			RequestLogger.printLine( event );

			// Balloon messages for whenever the person does not have
			// focus on KoLmafia.

			if ( StaticEntity.usesSystemTray() )
				SystemTrayFrame.showBalloon( event );

			if ( KoLMessenger.isRunning() )
			{
				int dash = event.indexOf( "-" );
				KoLMessenger.updateChat( "<font color=green>" + event.substring( dash + 2 ) + "</font>" );
			}
		}

		shouldLoadEventFrame &= StaticEntity.getGlobalProperty( "initialFrames" ).indexOf( "EventsFrame" ) != -1;

		// If we're not a GUI and there are no GUI windows open
		// (ie: the GUI loader command wasn't used), quit now.

		if ( existingFrames.isEmpty() )
			return;

		// If we are not running chat, pop up an EventsFrame to show
		// the events.  Use the standard run method so that you wait
		// for it to finish before calling it again on another event.

		shouldLoadEventFrame |= StaticEntity.getGlobalProperty( "initialDesktop" ).indexOf( "EventsFrame" ) != -1 &&
			KoLDesktop.getInstance().isVisible();

		if ( shouldLoadEventFrame )
			SwingUtilities.invokeLater( new CreateFrameRunnable( EventsFrame.class ) );
	}

	public final void loadResponseFromFile( String filename )
	{
		try
		{
			BufferedReader buf = KoLDatabase.getReader( filename );
			String line;  StringBuffer response = new StringBuffer();

			while ( (line = buf.readLine()) != null )
				response.append( line );

			this.responseCode = 200;
			this.responseText = response.toString();
		}
		catch ( Exception e )
		{
			// This means simply that there was no file from which
			// to load the data.  Given that this is run during debug
			// tests, only, we can ignore the error.
		}
	}

	public String toString()
	{	return this.getURLString();
	}

	public void printRequestProperties()
	{
		RequestLogger.updateDebugLog();
		RequestLogger.updateDebugLog( "Requesting: http://" + KOL_HOST + "/" + this.getURLString() );

		Map requestProperties = this.formConnection.getRequestProperties();
		RequestLogger.updateDebugLog( requestProperties.size() + " request properties" );
		RequestLogger.updateDebugLog();

		Iterator iterator = requestProperties.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			Entry entry = (Entry)iterator.next();
			RequestLogger.updateDebugLog( "Field: " + entry.getKey() + " = " + entry.getValue() );
		}

		RequestLogger.updateDebugLog();
	}

	public void printHeaderFields()
	{
		RequestLogger.updateDebugLog();
		RequestLogger.updateDebugLog( "Retrieved: http://" + KOL_HOST + "/" + this.getURLString() );
		RequestLogger.updateDebugLog();

		Map headerFields = this.formConnection.getHeaderFields();
		RequestLogger.updateDebugLog( headerFields.size() + " header fields" );

		Iterator iterator = headerFields.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			Entry entry = (Entry)iterator.next();
			RequestLogger.updateDebugLog( "Field: " + entry.getKey() + " = " + entry.getValue() );
		}

		RequestLogger.updateDebugLog();
	}
}
