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

package net.sourceforge.kolmafia.textui.langserver;

import java.io.File;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.java.dev.spellcast.utilities.DataUtilities;

import net.sourceforge.kolmafia.KoLConstants;

class FilesMonitor
{
	final AshLanguageServer parent;

	FilesMonitor( final AshLanguageServer parent )
	{
		this.parent = parent;
	}

	void updateFile( final File file, final String text, final int version )
	{
		synchronized ( this.parent.scripts )
		{
			final Script script = this.getScript( file );

			final List<Script.Handler> handlers = this.findHandlers( file );

			if ( handlers.size() > 0 &&
			     ( ( script.text != null && script.version >= version ) ||
			       ( script.text == null && text == null ) ) )
			{
				// We already have a working handler using an
				// up-to-date version of that file.
				return;
			}

			if ( script.text == null || script.version < version ||
			     script.text != null && text == null )
			{
				script.text = text;
				script.version = version;
			}

			if ( handlers.size() == 0 )
			{
				// make a new handler
				handlers.add( script.makeHandler() );
				handlers.get( 0 ).start();
			}

			//TODO if a handler exists, look at its imports.
			// We need to check if that script *loses* any import
			// after the new parsing, and make new individual handlers
			// for those (if there isn't another script using them).
			// This'll be a pain...

			for ( final Script.Handler handler : handlers )
			{
				handler.instructions.offer( new Script.Instruction.ParseFile() );
			}
		}
	}

	/** Fetches or makes a Script for the given file. */
	Script getScript( final File file )
	{
		synchronized ( this.parent.scripts )
		{
			Script script = this.parent.scripts.get( file );
			if ( script == null )
			{
				script = new Script( this.parent, file );
				this.parent.scripts.put( file, script );
			}

			return script;
		}
	}

	List<Script.Handler> findHandlers( final File file )
	{
		final List<Script.Handler> handlers = new LinkedList<>();

		synchronized ( this.parent.scripts )
		{
			for ( final Script script : this.parent.scripts.values() )
			{
				if ( script.handler != null &&
				     script.handler.imports.containsKey( file ) )
				{
					handlers.add( script.handler );
				}
			}
		}

		return handlers;
	}

	List<Script.Handler> findOrMakeHandler( final File file )
	{
		final List<Script.Handler> handlers;

		synchronized ( this.parent.scripts )
		{
			final Script script = this.getScript( file );

			handlers = this.findHandlers( file );

			if ( handlers.size() == 0 )
			{
				// make a new handler
				handlers.add( script.makeHandler() );
				handlers.get( 0 ).start();
				handlers.get( 0 ).instructions.offer( new Script.Instruction.ParseFile() );
			}
		}

		return handlers;
	}

	void scan()
	{
		for ( final File directory :
			Arrays.asList(
				KoLConstants.SCRIPT_LOCATION,
				KoLConstants.PLOTS_LOCATION,
				KoLConstants.RELAY_LOCATION ) )
		{
			this.scan( directory );
		}
	}

	void scan( final File directory )
	{
		for ( final File file :
			Arrays.asList( DataUtilities.listFiles( directory ) ) )
		{
			if ( file.isDirectory() )
			{
				this.scan( file );
			}
			else if ( file.isFile() )
			{
				this.findOrMakeHandler( file );
			}
		}
	}

	static String sanitizeURI( final String uri )
	{
		if ( uri == null )
		{
			return null;
		}

		if ( System.getProperty( "os.name" ).toLowerCase().contains( "win" ) )
		{
			if ( uri.startsWith( "file:///" ) || uri.startsWith( "file:\\\\\\" ) )
			{
				return uri.substring( 8 );
			}
			else if ( uri.startsWith( "file:/" ) || uri.startsWith( "file:\\" ) )
			{
				return uri.substring( 6 );
			}
		}
		else
		{
			if ( uri.startsWith( "file://" ) || uri.startsWith( "file:\\\\" ) )
			{
				return uri.substring( 7 );
			}
		}

		return uri;
	}
}
