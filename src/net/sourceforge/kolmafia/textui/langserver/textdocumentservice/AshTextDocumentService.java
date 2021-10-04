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

import java.util.List;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.TextDocumentService;

import net.sourceforge.kolmafia.textui.langserver.AshLanguageServer;
import net.sourceforge.kolmafia.textui.langserver.FilesMonitor;

public abstract class AshTextDocumentService
	implements TextDocumentService
{
	protected final AshLanguageServer parent;

	public AshTextDocumentService( final AshLanguageServer parent )
	{
		this.parent = parent;
	}

	public final void setCapabilities( final ServerCapabilities capabilities )
	{
		TextDocumentSyncOptions textDocumentSyncOptions = new TextDocumentSyncOptions();
		textDocumentSyncOptions.setOpenClose( true );
		textDocumentSyncOptions.setChange( TextDocumentSyncKind.Full );
		textDocumentSyncOptions.setWillSave( false );
		textDocumentSyncOptions.setWillSaveWaitUntil( false );
		textDocumentSyncOptions.setSave( new SaveOptions( false ) );

		capabilities.setTextDocumentSync( textDocumentSyncOptions );

		// completionProvider

		// hoverProvider

		// signatureHelpProvider

		// declarationProvider
		// Only for functions
		// TODO

		// definitionProvider
		// TODO

		// typeDefinitionProvider
		// TODO

		// implementationProvider
		// Doesn't exist in ASH

		// referencesProvider
		//capabilities.setReferencesProvider( Boolean.TRUE );

		// documentHighlightProvider

		// documentSymbolProvider

		// codeActionProvider
		// for fixing misspelled literals/typed constants?

		// codeLensProvider

		// documentLinkProvider
		// for imports statement? To point to the imported file?
		// We may just settle with the file being the "definition" target...

		// colorProvider

		// documentFormattingProvider
		// maybe trim trailing whitespaces?

		// documentRangeFormattingProvider

		// documentOnTypeFormattingProvider

		// renameProvider

		// foldingRangeProvider

		// selectionRangeProvider

		// linkedEditingRangeProvider

		// callHierarchyProvider

		// semanticTokensProvider

		// monikerProvider
		// not even sure what that it? Looking into the docs,
		// it seems like it's something that "should" be a normal feature,
		// but then why was it added so recently? TODO look into this
		// https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#textDocument_moniker

		// typeHierarchyProvider
		// Neither part of LSP yet, nor a thing in ASH
	}

	@Override
	public void didOpen( DidOpenTextDocumentParams params )
	{
		this.parent.executor.execute( () -> {
			TextDocumentItem document = params.getTextDocument();

			File file = new File( FilesMonitor.sanitizeURI( document.getUri() ) );

			this.parent.monitor.updateFile( file, document.getText(), document.getVersion() );
		} );
	}

	@Override
	public void didChange( DidChangeTextDocumentParams params )
	{
		this.parent.executor.execute( () -> {
			VersionedTextDocumentIdentifier document = params.getTextDocument();
			List<TextDocumentContentChangeEvent> changes = params.getContentChanges();

			if ( changes.size() == 0 )
			{
				// Nothing to see here
				return;
			}

			File file = new File( FilesMonitor.sanitizeURI( document.getUri() ) );

			// We don't support incremental changes, so we expect the client
			// to put the whole file's content in a single TextDocumentContentChangeEvent
			this.parent.monitor.updateFile( file, changes.get( 0 ).getText(), document.getVersion() );
		} );
	}

	@Override
	public void didClose( DidCloseTextDocumentParams params )
	{
		this.parent.executor.execute( () -> {
			TextDocumentIdentifier document = params.getTextDocument();

			File file = new File( FilesMonitor.sanitizeURI( document.getUri() ) );

			this.parent.monitor.updateFile( file, null, -1 );
		} );
	}

	@Override
	public void didSave( DidSaveTextDocumentParams params )
	{
		// TODO Auto-generated method stub
		
	}
}
