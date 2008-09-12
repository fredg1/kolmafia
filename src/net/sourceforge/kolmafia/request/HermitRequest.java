/**
 * Copyright (c) 2005-2008, KoLmafia development team
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

package net.sourceforge.kolmafia.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.session.InventoryManager;
import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.utilities.StringUtilities;

import net.sourceforge.kolmafia.persistence.ItemDatabase;

public class HermitRequest
	extends GenericRequest
{
	private static final Pattern CLOVER_PATTERN = Pattern.compile( "(\\d+) left in stock for today" );
	private static final Pattern TRADE_PATTERN = Pattern.compile( "whichitem=([\\d,]+).*quantity=(\\d+)" );

	private static boolean checkedForClovers = false;
	private static int usedWorthless1 = 0;
	private static int usedWorthless2 = 0;
	private static int usedWorthless3 = 0;

	public static final AdventureResult WORTHLESS_ITEM = new AdventureResult( 13, 1 );

	public static final AdventureResult PERMIT = new AdventureResult( 42, 1 );

	public static final AdventureResult TRINKET = ItemPool.get( ItemPool.WORTHLESS_TRINKET, 1 );
	public static final AdventureResult GEWGAW = ItemPool.get( ItemPool.WORTHLESS_GEWGAW, 1 );
	public static final AdventureResult KNICK_KNACK = ItemPool.get( ItemPool.WORTHLESS_KNICK_KNACK, 1 );

	private static final AdventureResult HACK_SCROLL = ItemPool.get( 567, 1 );
	private static final AdventureResult SUMMON_SCROLL = ItemPool.get( 553, 1 );

	private final int itemId;

	private int quantity;

	/**
	 * Constructs a new <code>HermitRequest</code> that simply checks what items the hermit has available.
	 */

	public HermitRequest()
	{
		super( "hermit.php" );

		this.itemId = -1;
		this.quantity = 0;
	}

	/**
	 * Constructs a new <code>HermitRequest</code>. Note that in order for the hermit request to successfully run,
	 * there must be <code>KoLSettings</code> specifying the trade that takes place.
	 */

	public HermitRequest( final int itemId, final int quantity )
	{
		super( "hermit.php" );

		this.itemId = itemId;
		this.quantity = quantity;

		this.addFormField( "action", "trade" );
		this.addFormField( "whichitem", String.valueOf( itemId ) );
		this.addFormField( "quantity", String.valueOf( quantity ) );
		this.addFormField( "pwd" );
	}

	public static final void resetClovers()
	{
		checkedForClovers = false;
	}

	/**
	 * Executes the <code>HermitRequest</code>. This will trade the item specified in the character's
	 * <code>KoLSettings</code> for their worthless trinket; if the character has no worthless trinkets, this method
	 * will report an error to the StaticEntity.getClient().
	 */

	public void run()
	{
		if ( this.itemId > 0 && this.quantity <= 0 )
		{
			KoLmafia.updateDisplay( KoLConstants.ERROR_STATE, "Zero is not a valid quantity." );
			return;
		}

		if ( InventoryManager.hasItem( HermitRequest.HACK_SCROLL ) )
		{
			( new UseItemRequest( HermitRequest.HACK_SCROLL ) ).run();
		}

		if ( KoLCharacter.getLevel() >= 9 && InventoryManager.hasItem( HermitRequest.SUMMON_SCROLL ) )
		{
			int itemCount = HermitRequest.SUMMON_SCROLL.getCount( KoLConstants.inventory );
			( new UseItemRequest( HermitRequest.SUMMON_SCROLL.getInstance( itemCount ) ) ).run();

			if ( InventoryManager.hasItem( HermitRequest.HACK_SCROLL ) )
			{
				( new UseItemRequest( HermitRequest.HACK_SCROLL ) ).run();
				( new UseItemRequest( HermitRequest.SUMMON_SCROLL.getInstance( itemCount - 1 ) ) ).run();
			}
		}

		if ( HermitRequest.getWorthlessItemCount() < this.quantity )
		{
			InventoryManager.retrieveItem( HermitRequest.WORTHLESS_ITEM.getInstance( this.quantity ) );
		}
		else if ( HermitRequest.getWorthlessItemCount() == 0 )
		{
			InventoryManager.retrieveItem( HermitRequest.WORTHLESS_ITEM );
		}

		if ( HermitRequest.getWorthlessItemCount() == 0 )
		{
			return;
		}

		this.quantity = Math.min( this.quantity, HermitRequest.getWorthlessItemCount() );
		KoLmafia.updateDisplay( "Robbing the hermit..." );

		super.run();
	}

	public void processResults()
	{
		if ( !HermitRequest.parseHermitTrade( this.getURLString(), this.responseText ) )
		{
			if ( !InventoryManager.hasItem( HermitRequest.PERMIT ) )
			{
				if ( InventoryManager.retrieveItem( HermitRequest.PERMIT ) )
				{
					this.run();
				}

				return;
			}

			KoLmafia.updateDisplay( KoLConstants.ERROR_STATE, "You're not allowed to visit the Hermit." );
			return;
		}

		if ( this.itemId == -1 )
		{
			return;
		}

		// If you don't have enough Hermit Permits, then retrieve the
		// number of hermit permits requested.

		if ( this.responseText.indexOf( "You don't have enough Hermit Permits" ) != -1 )
		{
			if ( InventoryManager.retrieveItem( HermitRequest.PERMIT.getInstance( this.quantity ) ) )
			{
				this.run();
			}

			return;
		}

		// If the item is unavailable, assume he was asking for clover

		if ( this.responseText.indexOf( "doesn't have that item." ) != -1 )
		{
			KoLmafia.updateDisplay( KoLConstants.ERROR_STATE, "Today is not a clover day." );
			return;
		}

		// If you still didn't acquire items, what went wrong?

		if ( this.responseText.indexOf( "You acquire" ) == -1 )
		{
			KoLmafia.updateDisplay( KoLConstants.ERROR_STATE, "The hermit kept his stuff." );
			return;
		}

		KoLmafia.updateDisplay( "Hermit successfully looted!" );
	}

	public static final boolean parseHermitTrade( final String urlString, final String responseText )
	{
		// There should be a form, or an indication of item receipt,
		// for all valid hermit requests.

		if ( responseText.indexOf( "hermit.php" ) == -1 && responseText.indexOf( "You acquire" ) == -1 )
		{
			return false;
		}

		// If you don't have enough Hermit Permits, failure

		if ( responseText.indexOf( "You don't have enough Hermit Permits" ) != -1 )
		{
			HermitRequest.checkedForClovers = false;
                        HermitRequest.restoreInventory();
			return true;
		}

		// Only check for clovers.  All other items at the hermit
		// are assumed to be static final.

		AdventureResult clover = ItemPool.get( ItemPool.TEN_LEAF_CLOVER, 1 );
		KoLConstants.hermitItems.remove( clover );

		if ( responseText.indexOf( "he sends you packing" ) != -1 )
		{
			// No worthless items in inventory, so we can't tell if
			// clovers remain in stock
			HermitRequest.checkedForClovers = false;
		}
		else
		{
			Matcher cloverMatcher = CLOVER_PATTERN.matcher( responseText );
			if ( cloverMatcher.find() )
			{
				int count = Integer.parseInt( cloverMatcher.group( 1 ) );
				KoLConstants.hermitItems.add( ItemPool.get( ItemPool.TEN_LEAF_CLOVER, count ) );
			}

			HermitRequest.checkedForClovers = true;
		}

		Matcher matcher = HermitRequest.TRADE_PATTERN.matcher( urlString );
		if ( !matcher.find() )
		{
			// We simply visited the hermit
			return true;
		}

		int itemId = StringUtilities.parseInt( matcher.group( 1 ) );
		int quantity = StringUtilities.parseInt( matcher.group( 2 ) );

		if ( responseText.indexOf( "looks confused for a moment" ) != -1 )
		{
			// Put back Hermit Permits deducted by registerRequest
			ResultProcessor.processResult( HermitRequest.PERMIT.getInstance( quantity ) );
		}

		// If the item is unavailable, assume he was asking for clover
		// If asked for too many, you get no items

		if ( responseText.indexOf( "doesn't have that item." ) != -1 ||
		     responseText.indexOf( "You acquire" ) == -1 )
		{
                        HermitRequest.restoreInventory();
			return true;
		}

		return true;
	}

	public static final void restoreInventory()
	{
		// Restore hermit permits and worthless items deducted by
		// registerRequest

		int quantity = 0;

		if ( HermitRequest.usedWorthless1 > 0 )
		{
			ResultProcessor.processResult( HermitRequest.TRINKET.getInstance( HermitRequest.usedWorthless1 ) );
			quantity += HermitRequest.usedWorthless1;
		}

		if ( HermitRequest.usedWorthless2 > 0 )
		{
			ResultProcessor.processResult( HermitRequest.GEWGAW.getInstance( HermitRequest.usedWorthless2 ) );
			quantity += HermitRequest.usedWorthless2;
		}

		if ( HermitRequest.usedWorthless3 > 0 )
		{
			ResultProcessor.processResult( HermitRequest.KNICK_KNACK.getInstance( HermitRequest.usedWorthless3 ) );
			quantity += HermitRequest.usedWorthless3;
		}

		if ( quantity > 0 )
		{
			ResultProcessor.processResult( HermitRequest.PERMIT.getInstance( quantity ) );
		}
	}

	public static final boolean isWorthlessItem( final int itemId )
	{
		return itemId == ItemPool.WORTHLESS_TRINKET || itemId == ItemPool.WORTHLESS_GEWGAW || itemId == ItemPool.WORTHLESS_KNICK_KNACK;
	}

	private static final int subtractWorthlessItems( final AdventureResult item, final int total )
	{
		int count = 0 - Math.min( total, item.getCount( KoLConstants.inventory ) );
		ResultProcessor.processResult( item.getInstance( count ) );
		return 0 - count;
	}

	public static final int getWorthlessItemCount()
	{
		return HermitRequest.TRINKET.getCount( KoLConstants.inventory ) + HermitRequest.GEWGAW.getCount( KoLConstants.inventory ) + HermitRequest.KNICK_KNACK.getCount( KoLConstants.inventory );
	}

	public static final boolean isCloverDay()
	{
		if ( !HermitRequest.checkedForClovers )
		{
			new HermitRequest().run();
		}

		AdventureResult clover = ItemPool.get( ItemPool.TEN_LEAF_CLOVER, 1 );
		return KoLConstants.hermitItems.contains( clover );
	}

	public static final boolean registerRequest( final String urlString )
	{
		if ( !urlString.startsWith( "hermit.php" ) )
		{
			return false;
		}

		HermitRequest.usedWorthless1 = 0;
		HermitRequest.usedWorthless2 = 0;
		HermitRequest.usedWorthless3 = 0;

		RequestLogger.updateSessionLog();

		Matcher matcher = HermitRequest.TRADE_PATTERN.matcher( urlString );
		if ( !matcher.find() )
		{
			RequestLogger.updateSessionLog( "hermit" );
			return true;
		}

		int itemId = StringUtilities.parseInt( matcher.group( 1 ) );
		int quantity = StringUtilities.parseInt( matcher.group( 2 ) );

		RequestLogger.updateSessionLog( "hermit " + quantity + " " + ItemDatabase.getItemName( itemId ) );

		if ( quantity > HermitRequest.getWorthlessItemCount() )
		{
			// Asking for too many. Request will fail.
			return true;
		}

		// Assume hermit permits are consumed. Fix later, if not.
		ResultProcessor.processResult( HermitRequest.PERMIT.getInstance( 0 - quantity ) );

		// Subtract the worthless items in order of their priority;
		// as far as we know, the priority is the item Id.

		HermitRequest.usedWorthless1 = HermitRequest.subtractWorthlessItems( HermitRequest.TRINKET, quantity );
		quantity -= HermitRequest.usedWorthless1;
		HermitRequest.usedWorthless2 = HermitRequest.subtractWorthlessItems( HermitRequest.GEWGAW, quantity );
		quantity -= HermitRequest.usedWorthless2;
		HermitRequest.usedWorthless3 = HermitRequest.subtractWorthlessItems( HermitRequest.KNICK_KNACK, quantity );

		return true;
	}
}
