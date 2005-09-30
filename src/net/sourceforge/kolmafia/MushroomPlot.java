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

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MushroomPlot implements KoLConstants
{
	// The player's mushroom plot
	//
	//  1  2  3  4
	//  5  6  7  8
	//  9 10 11 12
	// 13 14 15 16

	private static int [] plot = new int[16];
	private static boolean initialized = false;
	private static boolean ownsPlot = false;

	// Empty spot
	public static final int EMPTY = 0;

	// Sprout
	public static final int SPROUT = 1;

	// First generation mushrooms
	public static final int SPOOKY = 724;
	public static final int KNOB = 303;
	public static final int KNOLL = 723;

	// Second generation mushrooms
	public static final int WARM = 749;
	public static final int COOL = 751;
	public static final int POINTY = 753;

	// Third generation mushrooms
	public static final int FLAMING = 755;
	public static final int FROZEN = 756;
	public static final int STINKY = 757;

	// Assocations between the mushroom IDs
	// and the mushroom image.

	public static final Object [][] MUSHROOMS =
	{
		// Sprout and emptiness
		{ new Integer( EMPTY ), "dirt1.gif", "__" },
		{ new Integer( SPROUT ), "mushsprout.gif", ".." },

		// First generation mushrooms
		{ new Integer( SPOOKY ), "spooshroom.gif", "Sp" },
		{ new Integer( KNOB ), "bmushroom.gif", "Kb" },
		{ new Integer( KNOLL ), "mushroom.gif", "Kn" },

		// Second generation mushrooms
		{ new Integer( WARM ), "flatshroom.gif", "Wa" },
		{ new Integer( COOL ), "plaidroom.gif", "Co" },
		{ new Integer( POINTY ), "tallshroom.gif", "Po" },

		// Third generation mushrooms
		{ new Integer( FLAMING ), "fireshroom.gif", "Fl" },
		{ new Integer( FROZEN ), "iceshroom", "Fr" },
		{ new Integer( STINKY ), "stinkshroo", "St" }
	};

	// Spore data - includes price of the spore
	// and the item ID associated with the spore.

	private static final int [][] SPORE_DATA = { { SPOOKY, 30 }, { KNOB, 40 }, { KNOLL, 50 } };

	/**
	 * Static method which resets the state of the
	 * mushroom plot.  This should be used whenever
	 * the login process is restarted.
	 */

	public static void reset()
	{	initialized = false;
	}

	/**
	 * Utility method which returns a two-dimensional
	 * array showing the arrangement of the plot.
	 */

	public static String getMushroomPlot( KoLmafia client, boolean isHypertext )
	{
		// First, try to initialize the mushroom plot.  In
		// this way, you ensure that you're getting a real
		// look at the mushroom plot.

		initialize( client );

		// If for some reason, the plot was invalid, then
		// the flag would have been set on the client.  In
		// this case, return a null string.

		if ( !client.permitsContinue() )
			return "Your plot is unavailable.";

		// Otherwise, you need to construct the string form
		// of the mushroom plot.  Shorthand and hpertext are
		// the only two versions at the moment.

		StringBuffer buffer = new StringBuffer();

		if ( isHypertext )
			buffer.append( "<center><table cellspacing=4 cellpadding=4>" );

		for ( int row = 0; row < 4; ++row )
		{
			// In a hypertext document, you initialize the
			// row in the table before you start appending
			// the squares.

			if ( isHypertext )
				buffer.append( "<tr>" );

			for ( int col = 0; col < 4; ++col )
			{
				// Hypertext documents need to have their cells opened before
				// the cell can be printed.

				buffer.append( isHypertext ? "<td>" : " " );
				int square = plot [ row * 4 + col ];

				String description = MushroomPlot.mushroomDescription( square );

				// If a description is available, and you're creating a
				// hypertext document, print the hyperlink showing the
				// description.

				if ( isHypertext && description != null )
					buffer.append( description );

				// Mushroom images are used in hypertext documents, while
				// shorthand notation is used in non-hypertext documents.

				buffer.append( isHypertext ? mushroomImage( square ) : mushroomShorthand( square ) );

				// If a description is available, and you're creating a
				// hypertext document, close the hyperlink showing the
				// description.

				if ( isHypertext && description != null )
					buffer.append( "</a>" );

				// Hypertext documents need to have their cells closed before
				// another cell can be printed.

				if ( isHypertext )
					buffer.append( "</td>" );
			}

			// In a hypertext document, you need to close the row before
			// continuing.  Note that both documents can have a full
			// line break.

			if ( isHypertext )
				buffer.append( "</tr>" );

			buffer.append( System.getProperty( "line.separator" ) );
		}

		buffer.append( "</table></center>" );
		return buffer.toString();

	}

	/**
	 * Utility method which retrieves the image associated
	 * with the given mushroom type.
	 */

	private static String mushroomImage( int mushroomType )
	{
		for ( int i = 0; i < MUSHROOMS.length; ++i )
			if ( mushroomType == ((Integer) MUSHROOMS[i][0]).intValue() )
				return "<img src=\"http://images.kingdomofloathing.com/itemimages/" + MUSHROOMS[i][1] + "\" width=30 height=30 border=0>";

		return "<img src=\"http://images.kingdomofloathing.com/itemimages/dirt1.gif\" width=30 height=30 border=0>";
	}

	/**
	 * Utility method which retrieves the shorthand notation
	 * for the given mushroom type.
	 */

	private static String mushroomShorthand( int mushroomType )
	{
		for ( int i = 0; i < MUSHROOMS.length; ++i )
			if ( mushroomType == ((Integer) MUSHROOMS[i][0]).intValue() )
				return (String) MUSHROOMS[i][2];

		return "??";
	}

	/**
	 * Utility method which retrieves the hyperlink description
	 * for the given mushroom type.
	 */

	private static String mushroomDescription( int mushroomType )
	{
		return mushroomType == EMPTY || mushroomType == SPROUT ? null :
			"<a href=\"desc_item.php?whichitem=" + TradeableItemDatabase.getDescriptionID( mushroomType ) + "\">";
	}

	/**
	 * One of the major functions of the mushroom plot handler,
	 * this method plants the given spore into the given position
	 * (or square) of the mushroom plot.
	 */

	public static boolean plantMushroom( KoLmafia client, int square, int spore )
	{
		// Validate square parameter.  It's possible that
		// the user input the wrong spore number.

		if ( square < 1 || square > 16 )
		{
			client.updateDisplay( ERROR_STATE, "Squares are numbered from 1 to 16." );
			client.cancelRequest();
			return false;
		}

		// Determine the spore that the user wishes to
		// plant and the price for that spore.  Place
		// those into holder variables.

		int sporeIndex = -1, sporePrice = -1;
		for ( int i = 0; i < SPORE_DATA.length; ++i )
			if ( SPORE_DATA[i][0] == spore )
			{
				sporeIndex = i + 1;
				sporePrice = SPORE_DATA[i][1];
			}

		// If nothing was reset, then return from this
		// method after notifying the user that the spore
		// they provided is not plantable.

		if ( sporeIndex == -1 )
		{
			client.updateDisplay( ERROR_STATE, "You can't plant that." );
			client.cancelRequest();
			return false;
		}

		// Make sure we have enough meat to pay for the spore.
		// Rather than using requirements validation, check the
		// character data.

		if ( client.getCharacterData().getAvailableMeat() < sporePrice )
			return false;

		// Make sure we know current state of mushroom plot
		// before we plant the mushroom.  Bail if it fails.

		if ( !initialize( client ) )
			return false;

		// If the square isn't empty, pick what's there

		if ( plot[ square - 1 ] != EMPTY && !pickMushroom( client, square ) )
			return false;

		// Plant the requested spore.

		MushroomPlotRequest request = new MushroomPlotRequest( client, square, sporeIndex );
		request.run();

		// If it failed, bail.

		if ( !client.permitsContinue() )
			return false;

		// Pay for the spore.  At this point, it's guaranteed
		// that the client allows you to continue.

		client.processResult( new AdventureResult( AdventureResult.MEAT, 0 - sporePrice ) );
		return true;
	}

	/**
	 * One of the major functions of the mushroom plot handler,
	 * this method picks the mushroom located in the given square.
	 */

	public static boolean pickMushroom( KoLmafia client, int square )
	{
		// Validate square parameter.  It's possible that
		// the user input the wrong spore number.

		if ( square < 1 || square > 16 )
		{
			client.updateDisplay( ERROR_STATE, "Squares are numbered from 1 to 16." );
			client.cancelRequest();
			return false;
		}

		// Make sure we know current state of mushroom plot
		// before we plant the mushroom.  Bail if it fails.

		if ( !initialize( client ) )
			return false;

		// If the square is not empty, run a request to pick
		// the mushroom in the square.

		if ( plot[ square - 1 ] != EMPTY )
		{
			MushroomPlotRequest request = new MushroomPlotRequest( client, square );
			request.run();
		}

		return client.permitsContinue();
	}

	/**
	 * Utility method used to initialize the state of
	 * the plot into the one-dimensional array.
	 */

	private static boolean initialize( KoLmafia client )
	{
		// Clear error state so that the flags are
		// properly detected.

		client.resetContinueState();

		// If you're not in a Muscle sign, no go.

		if ( !client.getCharacterData().inMuscleSign() )
		{
			client.updateDisplay( ERROR_STATE, "You can't find the mushroom fields." );
			client.cancelRequest();
			return false;
		}

		// Do this only once.

		if ( initialized )
			return true;

		// Ask for the state of your plot.

		initialized = true;
		MushroomPlotRequest request = new MushroomPlotRequest( client );
		request.run();

		return client.permitsContinue();
	}

	private static class MushroomPlotRequest extends KoLRequest
	{
		public MushroomPlotRequest( KoLmafia client )
		{	super( client, "knoll_mushrooms.php", true );
		}

		public MushroomPlotRequest( KoLmafia client, int square )
		{
			this( client );
			addFormField( "action", "click" );
			addFormField( "pos", String.valueOf( square - 1 ) );
		}

		public MushroomPlotRequest( KoLmafia client, int square, int spore )
		{
			this( client );
			addFormField( "action", "plant" );
			addFormField( "pos", String.valueOf( square - 1 ) );
			addFormField( "whichspore", String.valueOf( spore ) );
		}

		public void run()
		{
			super.run();
			parsePlot( responseText );

			// If you don't own a mushroom plot, there is nothing
			// left to do.  Update the display indicating this and
			// cancel the request.

			if ( ownsPlot == false )
			{
				client.updateDisplay( ERROR_STATE, "You haven't bought a mushroom plot yet." );
				client.cancelRequest();
				return;
			}

			client.processResults( responseText );

		}

		private void parsePlot( String text )
		{
			initialized = true;

			// Pretend all of the sections on the plot are empty
			// before you begin parsing the plot.

			for ( int i = 0; i < plot.length; ++i )
				plot[i] = EMPTY;

			Matcher plotMatcher = Pattern.compile( "<b>Your Mushroom Plot:</b><p><table>(<tr>.*?</tr><tr>.*></tr><tr>.*?</tr><tr>.*</tr>)</table>" ).matcher( text );
			ownsPlot = plotMatcher.find();

			// If there is no plot data, then we can assume that
			// the person does not own a plot.  Return from the
			// method if this is the case.  Otherwise, try to find
			// all of the squares.

			if ( !ownsPlot )
				return;

			Matcher squareMatcher = Pattern.compile( "<td>(.*?)</td>" ).matcher( plotMatcher.group(1) );

			for ( int i = 0; i < 16 && squareMatcher.find(); ++i )
				plot[i] = parseSquare( squareMatcher.group(1) );
		}

		private int parseSquare( String text )
		{
			// We figure out what's there based on the image.  This
			// is done by checking the text in the square against
			// the table of square values.

			Matcher gifMatcher = Pattern.compile( ".*/((.*)\\.gif)" ).matcher( text );
			if ( gifMatcher.find() )
			{
				String gif = gifMatcher.group(1);
				for ( int i = 0; i < MUSHROOMS.length; ++i )
					if ( gif.equals( MUSHROOMS[i][1] ) )
						return ((Integer) MUSHROOMS[i][0]).intValue();
			}

			return EMPTY;
		}
	}
}
