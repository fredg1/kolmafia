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

package net.sourceforge.kolmafia.swingui.menu;

import net.java.dev.spellcast.utilities.SortedListModel;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLConstants.CraftingType;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestThread;

import net.sourceforge.kolmafia.objectpool.ItemPool;

import net.sourceforge.kolmafia.persistence.ConcoctionDatabase;

import net.sourceforge.kolmafia.request.UntinkerRequest;

import net.sourceforge.kolmafia.swingui.listener.ThreadedListener;

import net.sourceforge.kolmafia.utilities.InputFieldUtilities;

public class UntinkerMenuItem
	extends ThreadedMenuItem
{
	public UntinkerMenuItem()
	{
		super( "Untinker Item", new UntinkerListener() );
	}

	private static class UntinkerListener
		extends ThreadedListener
	{
		@Override
		protected void execute()
		{
			SortedListModel untinkerItems = new SortedListModel();

			for ( int i = 0; i < KoLConstants.inventory.size(); ++i )
			{
				AdventureResult currentItem = (AdventureResult) KoLConstants.inventory.get( i );
				int itemId = currentItem.getItemId();

				// Ignore silly fairy gravy + meat from yesterday recipe
				if ( itemId == ItemPool.MEAT_STACK )
				{
					continue;
				}

				// Otherwise, accept any COMBINE recipe
				if ( ConcoctionDatabase.getMixingMethod( currentItem ) == CraftingType.COMBINE )
				{
					untinkerItems.add( currentItem );
				}
			}

			if ( untinkerItems.isEmpty() )
			{
				KoLmafia.updateDisplay( MafiaState.ERROR, "You don't have any untinkerable items." );
				return;
			}

			AdventureResult selectedValue =
				(AdventureResult) InputFieldUtilities.input( "You can unscrew meat paste?", untinkerItems );
			if ( selectedValue == null )
			{
				return;
			}

			RequestThread.postRequest( new UntinkerRequest( selectedValue.getItemId() ) );
		}
	}
}
