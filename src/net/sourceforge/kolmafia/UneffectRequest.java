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

public class UneffectRequest extends KoLRequest
{
	private int effectID;
	private AdventureResult effect;
	public static AdventureResult REMEDY = new AdventureResult( "soft green echo eyedrop antidote", -1 );

	/**
	 * Constructs a new <code>UneffectRequest</code>.
	 * @param	client	The client to be notified of completion
	 * @param	effect	The effect to be removed
	 */

	public UneffectRequest( KoLmafia client, AdventureResult effect )
	{
		super( client, "uneffect.php" );
		addFormField( "using", "Yep." );
		addFormField( "pwd" );

		this.effect = effect;
		this.effectID = StatusEffectDatabase.getEffectID( effect.getName() );
		addFormField( "whicheffect", String.valueOf( effectID ) );
	}

	public void run()
	{
		if ( !KoLCharacter.getInventory().contains( REMEDY ) )
		{
			DEFAULT_SHELL.updateDisplay( ERROR_STATE, "You don't have any soft green fluffy martians." );
			return;
		}

		DEFAULT_SHELL.updateDisplay( "Using soft green whatever..." );
		super.run();

		// If it notifies you that the effect was removed, delete it
		// from the list of effects.

		if ( responseText != null && responseText.indexOf( "Effect removed." ) != -1 )
		{
			KoLCharacter.getEffects().remove( effect );
			client.processResult( REMEDY );
			DEFAULT_SHELL.updateDisplay( "Effect removed." );
		}
		else
			DEFAULT_SHELL.updateDisplay( ERROR_STATE, "Effect removal failed." );

	}
}
