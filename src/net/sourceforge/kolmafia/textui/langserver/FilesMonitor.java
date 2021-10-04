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
			Script script = this.parent.scripts.get( file );
			if ( script == null )
			{
				script = new Script( this.parent, file );
				this.parent.scripts.put( file, script );
			}

			Script.Handler handler = this.findHandler( file );

			if ( handler != null &&
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

			if ( handler == null )
			{
				// make a new handler
				handler = script.makeHandler();
				handler.start();
			}

			//TODO if a handler exists, look at its imports.
			// We need to check if that script *loses* any import
			// after the new parsing, and make new individual handlers
			// for those (if there isn't another script using them).
			// This'll be a pain...

			handler.instructions.offer( new Script.Instruction.ParseFile() );
		}
	}

	Script.Handler findHandler( final File file )
	{
		synchronized ( this.parent.scripts )
		{
			for ( final Script script : this.parent.scripts.values() )
			{
				if ( script.handler != null &&
				     script.handler.imports.containsKey( file ) )
				{
					return script.handler;
				}
			}
		}

		return null;
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
