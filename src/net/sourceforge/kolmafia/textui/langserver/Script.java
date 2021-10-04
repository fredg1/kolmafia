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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import net.sourceforge.kolmafia.textui.Parser;
import net.sourceforge.kolmafia.textui.Parser.AshDiagnostic;
import net.sourceforge.kolmafia.textui.parsetree.Scope;

class Script
{
	final AshLanguageServer parent;
	final File file;

	Handler handler;
	int version = -1;
	String text;

	Script( final AshLanguageServer parent, final File file )
	{
		this.parent = parent;
		this.file = file;
	}

	Handler makeHandler()
	{
		return this.handler = new Handler();
	}

	InputStream getStream()
	{
		if ( this.text == null )
		{
			return null;
		}

		return new ByteArrayInputStream( this.text.getBytes() );
	}

	/**
	 * A thread tasked with taking care of a Script.
	 * <p>
	 * All files imported by this script should also be handled by this thread
	 */
	class Handler
		extends Thread
	{
		Parser parser;
		Scope scope;
		Map<File, Long> imports;

		Thread parserThread;

		final BlockingQueue<Script.Instruction> instructions = new LinkedBlockingQueue<>();

		Handler()
		{
			this.setName( Script.this.file.getName() + " - Main" );
			this.setDaemon( true );
		}

		//TODO scan the user's scripts to see if there's an unopened script that imports this one

		@Override
		public void run()
		{
			while ( true )
			{
				if ( Script.this.handler != this )
				{
					// We've been kicked out
					return;
				}

				try
				{
					final Instruction instruction = this.instructions.take();

					if ( instruction instanceof Script.Instruction.ParseFile )
					{
						if ( this.parserThread != null )
						{
							this.parserThread.interrupt();
						}

						this.imports = Collections.synchronizedMap( new HashMap<>() );

						this.parser = new LSParser( Script.this.file, Script.this.getStream(), this.imports );

						this.parserThread = new Thread( () ->
							{
								try
								{
									this.scope = this.parser.parse();

									this.instructions.offer( new Script.Instruction.SendDiagnostics() );
								}
								catch ( InterruptedException e )
								{
								}

								this.parserThread = null;
							},
							Script.this.file.getName() + " - Parser" );
						this.parserThread.setDaemon( true );
						this.parserThread.start();
						continue;
					}
					if ( instruction instanceof Script.Instruction.SendDiagnostics )
					{
						if ( this.parserThread != null )
						{
							// We're not done parsing
							this.instructions.offer( instruction );
							continue;
						}

						final Map<String, List<Diagnostic>> diagnosticsByUri = new HashMap<>();

						for ( final AshDiagnostic diagnostic : this.parser.diagnostics )
						{
							final String uri = diagnostic.sourceUri;

							List<Diagnostic> diagnostics = diagnosticsByUri.get( uri );
							if ( diagnostics == null )
							{
								diagnostics = new ArrayList<>();
								diagnosticsByUri.put( uri, diagnostics );
							}

							diagnostics.add( diagnostic.toLspDiagnostic() );
						}

						for ( final Map.Entry<String, List<Diagnostic>> entry : diagnosticsByUri.entrySet() )
						{
							Script.this.parent.client.publishDiagnostics(
								new PublishDiagnosticsParams(
									entry.getKey(),
									entry.getValue() ) );
						}

						continue;
					}
				}
				catch ( InterruptedException e )
				{
				}
			}
		}

		void close()
		{
			if ( this.parserThread != null )
			{
				this.parserThread.interrupt();
			}

			Script.this.handler = null;
		}

		private class LSParser
			extends Parser
		{
			private LSParser( final File scriptFile, final InputStream stream, final Map<File, Long> imports )
			{
				super( scriptFile, stream, imports );
			}

			@Override
			protected Parser getParser( final File scriptFile )
			{
				InputStream stream = null;

				synchronized ( Script.this.parent.scripts )
				{
					final Script script = Script.this.parent.scripts.get( scriptFile );

					if ( script != null )
					{
						if ( script.handler != null )
						{
							// The Handler that made the Parser that called this method
							// is now in charge of this file
							script.handler.close();
						}

						stream = script.getStream();
					}
				}

				return new LSParser( scriptFile, stream, this.getImports() );
			}
		}
	}

	static interface Instruction
		extends AshLanguageServer.Instruction
	{
		static class ParseFile
			implements Script.Instruction
		{
		}

		static class SendDiagnostics
			implements Script.Instruction
		{
		}
	}
}
