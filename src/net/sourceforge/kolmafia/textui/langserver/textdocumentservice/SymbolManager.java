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

package net.sourceforge.kolmafia.textui.langserver.textdocumentservice;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;

import net.sourceforge.kolmafia.textui.langserver.AshLanguageServer;
import net.sourceforge.kolmafia.textui.langserver.Script.Handler;

import net.sourceforge.kolmafia.textui.parsetree.BasicScope;
import net.sourceforge.kolmafia.textui.parsetree.Function;
import net.sourceforge.kolmafia.textui.parsetree.FunctionList;
import net.sourceforge.kolmafia.textui.parsetree.Symbol;
import net.sourceforge.kolmafia.textui.parsetree.SymbolList;
import net.sourceforge.kolmafia.textui.parsetree.Type;
import net.sourceforge.kolmafia.textui.parsetree.TypeList;
import net.sourceforge.kolmafia.textui.parsetree.Variable;
import net.sourceforge.kolmafia.textui.parsetree.VariableList;

// Could really use CancelProgress for that one...

/**
 * This class would be way simpler if Ash only allowed
 * files to access the files that they, themselves import.
 * 
 * Sadly, that is not the case.
 * 
 * If a file is imported by another file, that imported file has,
 * right from the get-go, access to everything that was imported
 * so far, in the currently parsed script.
 * 
 * This TREMENDOUSLY increases how much checking we need to do
 * if the user imports the same files across multiple scripts,
 * as the symbol we are looking for could have been defined in
 * any of the scripts' files, and may be different in any project/script.
 */
class SymbolManager
{
	final AshLanguageServer parent;

	SymbolManager( final AshLanguageServer parent )
	{
		this.parent = parent;
	}

	final void setCapabilities( final ServerCapabilities capabilities )
	{
		// declarationProvider
		// Only for functions
		// TODO

		capabilities.setDefinitionProvider( true );
		capabilities.setTypeDefinitionProvider( true );
		// implementationProvider -- Doesn't exist in ASH
		capabilities.setReferencesProvider( true );
	}

	List<Location> getDefinition( final File file, final Position position )
	{
		return this.gatherAcrossProjects( file, position,
			( SymbolRequest request ) -> {
				return Arrays.asList( request.symbol.getDefinitionLocation() );
			} );
	}

	List<Location> getTypeDefinition( final File file, final Position position )
	{
		return this.gatherAcrossProjects( file, position,
			( SymbolRequest request ) -> {
				if ( request.symbol instanceof Type )
				{
					return Arrays.asList( request.symbol.getDefinitionLocation() );
				}
				if ( request.symbol instanceof Variable )
				{
					return Arrays.asList( ((Variable) request.symbol).getType().getDefinitionLocation() );
				}
				if ( request.symbol instanceof Function )
				{
					return Arrays.asList( ((Function) request.symbol).getType().getDefinitionLocation() );
				}
				return null;
			} );
	}

	List<Location> getReferences( final File file, final Position position )
	{
		return this.gatherAcrossProjects( file, position,
			( SymbolRequest request ) -> {
				if ( request.symbol instanceof Type )
				{
					return request.sourceScope.getTypes().getReferences( (Type) request.symbol );
				}
				if ( request.symbol instanceof Variable )
				{
					return request.sourceScope.getVariables().getReferences( (Variable) request.symbol );
				}
				if ( request.symbol instanceof Function )
				{
					//TODO if/when we support logging functions'
					// declaration, check the submitted parameters
					// for ReferenceParams.getContext().isIncludeDeclaration()
					return request.sourceScope.getFunctions().getReferences( (Function) request.symbol );
				}
				return null;
			} );
	}

	private <T> List<T> gatherAcrossProjects( final File file, final Position position,
		final java.util.function.Function<SymbolRequest, List<T>> itemProducer )
	{
		final List<T> result = new LinkedList<>();

		for ( final Handler handler :
			this.parent.monitor.findOrMakeHandler( file ) )
		{
			final SymbolRequest symbolRequest = new SymbolRequest( handler, file, position );

			final List<T> items = itemProducer.apply( symbolRequest );

			if ( items == null )
			{
				continue;
			}

			for ( final T item : items )
			{
				if ( item != null && !result.contains( item ) )
				{
					result.add( item );
				}
			}
		}

		return result;
	}

	private static class SymbolRequest
	{
		final Symbol symbol;
		final BasicScope sourceScope;

		private final String targetUri;
		private final Position targetPosition;

		private Symbol currentBest = null;
		private Range currentBestRange = null;
		private BasicScope currentBestScope = null;

		SymbolRequest( final Handler handler, final File file, final Position position )
		{
			// It's too dangerous to trust file.toUri().toString()
			this.targetUri = handler.getParser().getImports().get( file ).getUri().toString();
			this.targetPosition = position;

			// getScope() returns "top level". We want the default library.
			final BasicScope topScope = handler.getScope().getParentScope();

			// Some definition/reference locations are more than
			// just 1 "word", so we need to check EVERYTHING,
			// not just the first match

			for ( final BasicScope scope : topScope.getScopes() )
			{
				for ( final SymbolList<? extends Symbol> symbols :
					Arrays.asList(
						scope.getTypes(),
						scope.getVariables(),
						scope.getFunctions() ) )
				{
					for ( final Symbol symbol : symbols )
					{
						final Location definitionLocation = symbol.getDefinitionLocation();

						if ( isInTarget( definitionLocation ) &&
						     isBetterThanCurrentBest( definitionLocation ) )
						{
							this.currentBest = symbol;
							this.currentBestRange = definitionLocation.getRange();
							this.currentBestScope = scope;
							continue;
						}

						for ( final Location referenceLocation :
							//symbols.getReferences( symbol )
							// Stupid capture groups that don't work right...
							symbols instanceof TypeList ?
								((TypeList) symbols).getReferences( (Type) symbol ) :
							symbols instanceof VariableList ?
								((VariableList) symbols).getReferences( (Variable) symbol ) :
							symbols instanceof FunctionList ?
								((FunctionList) symbols).getReferences( (Function) symbol ) :
							Collections.<Location>emptyList() )
						{
							if ( isInTarget( referenceLocation ) &&
							     isBetterThanCurrentBest( referenceLocation ) )
							{
								this.currentBest = symbol;
								this.currentBestRange = referenceLocation.getRange();
								this.currentBestScope = scope;
								break;
							}
						}
					}
				}
			}

			this.symbol = currentBest;
			this.sourceScope = currentBestScope;
		}

		private boolean isInTarget( final Location location )
		{
			return location != null &&
			       this.targetUri.equals( location.getUri() ) &&
			       ( this.targetPosition.getLine() > location.getRange().getStart().getLine() ||
			         this.targetPosition.getLine() == location.getRange().getStart().getLine() &&
			         this.targetPosition.getCharacter() >= location.getRange().getStart().getCharacter() ) &&
			       ( this.targetPosition.getLine() < location.getRange().getEnd().getLine() ||
			         this.targetPosition.getLine() == location.getRange().getEnd().getLine() &&
			         this.targetPosition.getCharacter() <= location.getRange().getEnd().getCharacter() );
		}

		private boolean isBetterThanCurrentBest( final Location location )
		{
			if ( this.currentBest == null )
			{
				return true;
			}

			final Range range = location.getRange();

			// Compare how many lines they both span
			int height = range.getEnd().getLine() - range.getStart().getLine();
			int currentBestHeight = currentBestRange.getEnd().getLine() - currentBestRange.getStart().getLine();
			if ( height != currentBestHeight )
			{
				return height < currentBestHeight;
			}

			// If they are on a single line, we can easily compare their width
			if ( height == 0 )
			{
				int width = range.getEnd().getCharacter() - range.getStart().getCharacter();
				int currentBestWidth = currentBestRange.getEnd().getCharacter() - currentBestRange.getStart().getCharacter();

				return width < currentBestWidth;
			}

			// Two multiline symbols... they are most likely nested inside each other.
			// Check which starts last / ends first, that should be the smaller one.
			if ( range.getStart().getLine() != currentBestRange.getStart().getLine() )
			{
				return range.getStart().getLine() > currentBestRange.getStart().getLine();
			}
			if ( range.getStart().getCharacter() != currentBestRange.getStart().getCharacter() )
			{
				return range.getStart().getCharacter() > currentBestRange.getStart().getCharacter();
			}
			if ( range.getEnd().getLine() != currentBestRange.getEnd().getLine() )
			{
				return range.getEnd().getLine() < currentBestRange.getEnd().getLine();
			}
			if ( range.getEnd().getCharacter() != currentBestRange.getEnd().getCharacter() )
			{
				return range.getEnd().getCharacter() < currentBestRange.getEnd().getCharacter();
			}

			// Only way to reach this is for the same symbol to be registered twice.
			return false;
		}
	}
}
