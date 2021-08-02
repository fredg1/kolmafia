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
import java.io.IOException;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import net.sourceforge.kolmafia.StaticEntity;

/**
 * The thread in charge of listening for the client's messages.
 * <p>
 * Was made abstract, because the actual class to use is {@link StateCheckWrappers.AshLanguageServer}.
 */
public abstract class AshLanguageServer
	implements LanguageClientAware, LanguageServer, TextDocumentService, WorkspaceService
{
	/** The Launcher */
	public static void main( String[] args )
		throws IOException
	{
		AshLanguageServer server = new StateCheckWrappers.AshLanguageServer();

		final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher( server, System.in, System.out );
		server.connect( launcher.getRemoteProxy() );
		launcher.startListening();
	}

	/* The server */

	protected ServerState state = ServerState.STARTED;

	protected enum ServerState
	{
		STARTED,
		INITIALIZED,
		SHUTDOWN
	}

	LanguageClient client;
	ClientCapabilities clientCapabilities;

	final FilesMonitor monitor = new FilesMonitor( this );

	final Map<File, Script> scripts = Collections.synchronizedMap( new Hashtable<>( 20 ) );


	@Override
	public void connect( LanguageClient client )
	{
		this.client = client;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize( InitializeParams params )
	{
		this.state = ServerState.INITIALIZED;

		// TODO Auto-generated method stub
		this.clientCapabilities = params.getCapabilities();
		// params.getTrace(); for when we implement trace
		// params.getClientInfo(); do we need/care about that?
		// params.getWorkspaceFolders(); look into this later

		this.monitor.scan();

		ServerCapabilities capabilities = new ServerCapabilities();
		// soooo... what *can* we do, currently?

		// textDocumentSync
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

		// codeLensProvider

		// documentLinkProvider

		// colorProvider

		// documentFormattingProvider
		// maybe trim trailing whitespaces?

		// documentRangeFormattingProvider

		// documentOnTypeFormattingProvider

		// renameProvider

		// foldingRangeProvider

		// executeCommandProvider

		// selectionRangeProvider

		// linkedEditingRangeProvider

		// callHierarchyProvider

		// semanticTokensProvider

		// monikerProvider
		// not even sure what that it? Looking into the docs,
		// it seems like it's something that "should" be a normal feature,
		// but then why was it added so recently? TODO look into this
		// https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#textDocument_moniker

		// workspaceSymbolProvider

		// workspace

		// typeHierarchyProvider
		// Neither part of LSP yet, nor a thing in ASH

		// experimental
		// no..?

		ServerInfo info = new ServerInfo( StaticEntity.getVersion() );

		return CompletableFuture.completedFuture( new InitializeResult( capabilities, info ) );
	}

	@Override
	public CompletableFuture<Object> shutdown()
	{
		this.state = ServerState.SHUTDOWN;

		for ( final Script script : this.scripts.values() )
		{
			if ( script.handler != null )
			{
				script.handler.close();
			}
		}

		return CompletableFuture.completedFuture( null );
	}

	@Override
	public void exit()
	{
		System.exit( this.state == ServerState.SHUTDOWN ? 0 : 1 );
	}

	@Override
	public TextDocumentService getTextDocumentService()
	{
		return this;
	}

	@Override
	public WorkspaceService getWorkspaceService()
	{
		return this;
	}


	// TextDocumentService

	@Override
	public void didOpen( DidOpenTextDocumentParams params )
	{
		TextDocumentItem document = params.getTextDocument();

		File file = new File( FilesMonitor.sanitizeURI( document.getUri() ) );

		this.monitor.updateFile( file, document.getText(), document.getVersion() );
	}

	@Override
	public void didChange( DidChangeTextDocumentParams params )
	{
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
		this.monitor.updateFile( file, changes.get( 0 ).getText(), document.getVersion() );
	}

	@Override
	public void didClose( DidCloseTextDocumentParams params )
	{
		TextDocumentIdentifier document = params.getTextDocument();

		File file = new File( FilesMonitor.sanitizeURI( document.getUri() ) );

		this.monitor.updateFile( file, null, -1 );
	}

	@Override
	public void didSave( DidSaveTextDocumentParams params )
	{
		// TODO Auto-generated method stub
		
	}


	// WorkspaceService

	@Override
	public void didChangeConfiguration( DidChangeConfigurationParams params )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didChangeWatchedFiles( DidChangeWatchedFilesParams params )
	{
		// TODO Auto-generated method stub
		
	}

	/** Type of message sent between the threads of this Language Server. */
	static interface Instruction
	{
	}
}
