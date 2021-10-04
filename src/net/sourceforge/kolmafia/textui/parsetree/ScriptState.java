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
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION ) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE ) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia.textui.parsetree;

import java.io.PrintStream;

import org.eclipse.lsp4j.Location;

import net.sourceforge.kolmafia.textui.DataTypes;
import net.sourceforge.kolmafia.textui.AshRuntime;
import net.sourceforge.kolmafia.textui.ScriptRuntime;

public abstract class ScriptState
	extends Command
{
	private final ScriptRuntime.State state;

	public ScriptState( final Location location, final ScriptRuntime.State state )
	{
		super( location );
		this.state = state;
	}

	@Override
	public String toString()
	{
		return this.state.toString();
	}

	@Override
	public Value execute( final AshRuntime interpreter )
	{
		if ( ScriptRuntime.isTracing() )
		{
			interpreter.traceIndent();
			interpreter.trace( this.toString() );
			interpreter.traceUnindent();
		}
		interpreter.setState( this.state );
		return DataTypes.VOID_VALUE;
	}

	@Override
	public void print( final PrintStream stream, final int indent )
	{
		AshRuntime.indentLine( stream, indent );
		stream.println( "<COMMAND " + this.state + ">" );
	}

	@Override
	public boolean assertBarrier()
	{
		return true;
	}

	public static final class BadScriptState
		extends ScriptState
	{
		public BadScriptState( final Location location )
		{
			super( location, ScriptRuntime.State.NORMAL );
		}

		@Override
		public Value execute( final AshRuntime interpreter )
		{
			return null;
		}

		@Override
		public boolean assertBarrier()
		{
			return false;
		}
	}
}
