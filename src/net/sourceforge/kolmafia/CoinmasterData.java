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

package net.sourceforge.kolmafia;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.java.dev.spellcast.utilities.LockableListModel;

import net.sourceforge.kolmafia.objectpool.IntegerPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;

import net.sourceforge.kolmafia.persistence.CoinmastersDatabase;
import net.sourceforge.kolmafia.persistence.ItemDatabase;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.request.CoinMasterRequest;
import net.sourceforge.kolmafia.request.HermitRequest;

import net.sourceforge.kolmafia.utilities.StringUtilities;

public class CoinmasterData
	implements Comparable<CoinmasterData>
{
	private final String master;
	private final String nickname;
	private final Class requestClass;
	// The token(s) that you exchange for items.
	// One, for now
	private String token;
	public String plural;
	private final String tokenTest;
	private final boolean positiveTest;
	private final Pattern tokenPattern;
	private AdventureResult item;
	private final String property;
	// For Coinmasters that deal with "rows", a map from item name to row number
	private final Map<String, Integer> itemRows;
	// The base URL used to buy things from this Coinmaster
	private final String buyURL;
	private final String buyAction;
	private final LockableListModel<AdventureResult> buyItems;
	private final Map<String, Integer> buyPrices;
	// The base URL used to sell things to this Coinmaster
	private final String sellURL;
	private final String sellAction;
	private final LockableListModel<AdventureResult> sellItems;
	private final Map<String, Integer> sellPrices;
	// Fields assumed to be common to buying & selling
	private final String itemField;
	private final Pattern itemPattern;
	private final String countField;
	private final Pattern countPattern;
	private final String storageAction;
	private final String tradeAllAction;
	private final boolean canPurchase;

	public CoinmasterData( 
		final String master,
		final String nickname,
		final Class requestClass,
		final String token,
		final String tokenTest,
		final boolean positiveTest,
		final Pattern tokenPattern,
		final AdventureResult item,
		final String property,
		final Map<String, Integer> itemRows,
		final String buyURL,
		final String buyAction,
		final LockableListModel<AdventureResult> buyItems,
		final Map<String, Integer> buyPrices,
		final String sellURL,
		final String sellAction,
		final LockableListModel<AdventureResult> sellItems,
		final Map<String, Integer> sellPrices,
		final String itemField,
		final Pattern itemPattern,
		final String countField,
		final Pattern countPattern,
		final String storageAction,
		final String tradeAllAction,
		final boolean canPurchase )
	{
		this.master = master;
		this.nickname = nickname;
		this.requestClass = requestClass;
		this.token = token;
		this.tokenTest = tokenTest;
		this.positiveTest = positiveTest;
		this.tokenPattern = tokenPattern;
		this.item = item;
		this.property = property;
		this.itemRows = itemRows;
		this.buyURL = buyURL;
		this.buyAction = buyAction;
		this.buyItems = buyItems;
		this.buyPrices = buyPrices;
		this.sellURL = sellURL;
		this.sellAction = sellAction;
		this.sellItems = sellItems;
		this.sellPrices = sellPrices;
		this.itemField = itemField;
		this.itemPattern = itemPattern;
		this.countField = countField;
		this.countPattern = countPattern;
		this.storageAction = storageAction;
		this.tradeAllAction = tradeAllAction;
		this.canPurchase = canPurchase;

		// Derived fields
		this.plural = item != null ? ItemDatabase.getPluralName( token ) : token + "s";
	}

	public final String getMaster()
	{
		return this.master;
	}

	public final String getNickname()
	{
		return this.nickname;
	}

	public final Class getRequestClass()
	{
		return this.requestClass;
	}

	public final String getBuyURL()
	{
		return this.buyURL;
	}

	public final String getSellURL()
	{
		return this.sellURL;
	}

	public final String getToken()
	{
		return this.token;
	}

	public final String getPluralToken()
	{
		return this.plural;
	}

	public final void setToken( final String token )
	{
		this.token = token;
	}

	public final int availableTokens()
	{
		AdventureResult item = this.item;
		if ( item != null )
		{
			return  item.getItemId() == ItemPool.WORTHLESS_ITEM ?
				HermitRequest.getWorthlessItemCount() :
				item.getCount( KoLConstants.inventory );
		}
		String property = this.property;
		if ( property != null )
		{
			return Preferences.getInteger( property );
		}
		return 0;
	}

	public final int availableStorageTokens()
	{
		return this.storageAction != null ? this.item.getCount( KoLConstants.storage ) : 0;
	}

	public final int affordableTokens()
	{
		// Special handling for acquiring worthless items
		if ( this.item != null && this.item.getItemId() == ItemPool.WORTHLESS_ITEM )
		{
			return HermitRequest.getAcquirableWorthlessItemCount();
		}
		return this.availableTokens();
	}

	public final String getTokenTest()
	{
		return this.tokenTest;
	}

	public final boolean getPositiveTest()
	{
		return this.positiveTest;
	}

	public final Pattern getTokenPattern()
	{
		return this.tokenPattern;
	}

	public final AdventureResult getItem()
	{
		return this.item;
	}

	public final void setItem( final AdventureResult item )
	{
		this.item = item;
	}

	public final String getProperty()
	{
		return this.property;
	}

	public final String getItemField()
	{
		return this.itemField;
	}

	public final Pattern getItemPattern()
	{
		return this.itemPattern;
	}

	public final Matcher getItemMatcher( final String string )
	{
		return this.itemPattern == null ? null : this.itemPattern.matcher( string );
	}

	public final String getCountField()
	{
		return this.countField;
	}

	public final Pattern getCountPattern()
	{
		return this.countPattern;
	}

	public final Matcher getCountMatcher( final String string )
	{
		return this.countPattern == null ? null : this.countPattern.matcher( string );
	}

	public final String getBuyAction()
	{
		return this.buyAction;
	}

	public final LockableListModel<AdventureResult> getBuyItems()
	{
		return this.buyItems;
	}

	public final Map<String, Integer> getBuyPrices()
	{
		return this.buyPrices;
	}

	public final boolean canBuyItem( final String itemName )
	{
		if ( this.buyItems == null )
		{
			return false;
		}

		AdventureResult item = new AdventureResult( itemName, 1, false );
		return item.getCount( this.buyItems ) > 0;
	}

	public final int getBuyPrice( final String itemName )
	{
		if ( this.buyPrices == null )
		{
			return 0;
		}

		String name = StringUtilities.getCanonicalName( itemName );
		Integer price = (Integer) this.buyPrices.get( name );
		return price != null ? price.intValue() : 0;
	}

	public AdventureResult itemBuyPrice( final String itemName )
	{
		int price = this.getBuyPrice( itemName );
		return  this.item == null ?
			AdventureResult.tallyItem( this.token, price, false ) :
			this.item.getInstance( price );
	}

	public final AdventureResult itemBuyPrice( final int itemId )
	{
		return this.itemBuyPrice( ItemDatabase.getItemName( itemId ) );
	}

	public final String getSellAction()
	{
		return this.sellAction;
	}

	public final LockableListModel<AdventureResult> getSellItems()
	{
		return this.sellItems;
	}

	public final Map<String, Integer> getSellPrices()
	{
		return this.sellPrices;
	}

	public final boolean canSellItem( final String itemName )
	{
		if ( this.sellPrices != null )
		{
			String name = StringUtilities.getCanonicalName( itemName );
			return this.sellPrices.containsKey( name );
		}
		return false;
	}

	public final int getSellPrice( final String itemName )
	{
		if ( this.sellPrices != null )
		{
			String name = StringUtilities.getCanonicalName( itemName );
			Integer price = (Integer) this.sellPrices.get( name );
			return price != null ? price.intValue() : 0;
		}
		return 0;
	}

	public AdventureResult itemSellPrice( final String itemName )
	{
		int price = this.getSellPrice( itemName );
		return  this.item == null ?
			AdventureResult.tallyItem( this.token, price, false ) :
			this.item.getInstance( price );
	}

	public final AdventureResult itemSellPrice( final int itemId )
	{
		return this.itemSellPrice( ItemDatabase.getItemName( itemId ) );
	}

	public final String getStorageAction()
	{
		return this.storageAction;
	}

	public final String getTradeAllAction()
	{
		return this.tradeAllAction;
	}

	public Map<String, Integer> getRows()
	{
		return this.itemRows;
	}

	public final Integer getRow( int itemId )
	{
		if ( this.itemRows == null )
		{
			return IntegerPool.get( itemId );
		}
		String name = ItemDatabase.getCanonicalName( itemId );
		Integer row = this.itemRows.get( name );
		return row;
	}

	public void registerPurchaseRequests()
	{
		// If this Coin Master doesn't sell anything that goes into
		// your inventory, nothing to register
		if ( !this.canPurchase )
		{
			return;
		}

		// Clear existing purchase requests
		CoinmastersDatabase.clearPurchaseRequests( this );

		// For each item you can buy from this Coin Master, create a purchase request
		for ( AdventureResult item : this.buyItems )
		{
			int itemId = item.getItemId();
			String itemName = item.getName();
			int price = CoinmastersDatabase.getPrice( itemName, this.buyPrices );
			int quantity = item.getCount();
			CoinmastersDatabase.registerPurchaseRequest( this, itemId, price, quantity );
		}
	}

	@Override
	public String toString()
	{
		return this.master;
	}

	@Override
	public boolean equals( final Object o )
	{
		return o != null && o instanceof CoinmasterData && this.master == ( (CoinmasterData) o ).master;
	}

	@Override
	public int hashCode()
	{
		return this.master != null ? this.master.hashCode() : 0;
	}

	public int compareTo( final CoinmasterData cd )
	{
		return this.master.compareToIgnoreCase( cd.master );
	}

	public CoinMasterRequest getRequest()
	{
		Class requestClass = this.getRequestClass();
		Class [] parameters = new Class[ 0 ] ;

		try
		{
			Constructor constructor = requestClass.getConstructor( parameters );
			Object [] initargs = new Object[ 0 ];
			return (CoinMasterRequest) constructor.newInstance( initargs );
		}
		catch ( Exception e )
		{
			return null;
		}
	}

	public CoinMasterRequest getRequest( final boolean buying, final AdventureResult [] items )
	{
		Class requestClass = this.getRequestClass();
		Class [] parameters = new Class[ 2 ] ;
		parameters[ 0 ] = boolean.class;
		parameters[ 1 ] = AdventureResult[].class;

		try
		{
			Constructor constructor = requestClass.getConstructor( parameters );
			Object [] initargs = new Object[ 2 ];
			initargs[ 0 ] = buying;
			initargs[ 1 ] = items;
			return (CoinMasterRequest) constructor.newInstance( initargs );
		}
		catch ( Exception e )
		{
			return null;
		}
	}

	public boolean isAccessible()
	{
		return this.accessible() == null;
	}

	public String accessible()
	{
		// Returns an error reason or null

		Class requestClass = this.getRequestClass();
		Class [] parameters = new Class[ 0 ] ;

		try
		{
			Method method = requestClass.getMethod( "accessible", parameters );
			Object [] args = new Object[ 0 ];
			return (String) method.invoke( null, args );
		}
		catch ( Exception e )
		{
			return null;
		}
	}

	public String canSell()
	{
		// Returns an error reason or null

		Class requestClass = this.getRequestClass();
		Class [] parameters = new Class[ 0 ] ;

		try
		{
			Method method = requestClass.getMethod( "canSell", parameters );
			Object [] args = new Object[ 0 ];
			return (String) method.invoke( null, args );
		}
		catch ( Exception e )
		{
			return null;
		}
	}

	public String canBuy()
	{
		// Returns an error reason or null

		Class requestClass = this.getRequestClass();
		Class [] parameters = new Class[ 0 ] ;

		try
		{
			Method method = requestClass.getMethod( "canBuy", parameters );
			Object [] args = new Object[ 0 ];
			return (String) method.invoke( null, args );
		}
		catch ( Exception e )
		{
			return null;
		}
	}
}
