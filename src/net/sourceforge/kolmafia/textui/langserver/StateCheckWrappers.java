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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

/**
 * Wrapper for {@link net.sourceforge.kolmafia.textui.langserver.AshLanguageServer AshLanguageServer}.
 * <p>
 * Takes care of checking the server's state before every request and notification.
 */
public class StateCheckWrappers
{
	static class AshLanguageServer
		extends net.sourceforge.kolmafia.textui.langserver.AshLanguageServer
	{
		private boolean notInitialized()
		{
			return this.state == ServerState.STARTED;
		}

		private boolean wasShutdown()
		{
			return this.state == ServerState.SHUTDOWN;
		}

		/** To use with @JsonNotifications. They are simply ignored if we are not initialized. */
		private boolean isActive()
		{
			return this.state == ServerState.INITIALIZED;
		}

		private void initializeCheck()
		{
			if ( this.notInitialized() )
			{
				ResponseError error = new ResponseError( ResponseErrorCode.serverNotInitialized, "Server was not initialized", null );
				throw new ResponseErrorException( error );
			}
		}

		private void shutdownCheck()
		{
			if ( this.wasShutdown() )
			{
				ResponseError error = new ResponseError( ResponseErrorCode.InvalidRequest, "Server was shut down", null );
				throw new ResponseErrorException( error );
			}
		}

		/** To use with @JsonRequests. They must throw an error if we are not initialized. */
		private void stateCheck()
		{
			this.initializeCheck();
			this.shutdownCheck();
		}


		// LanguageServer

		@Override
		public CompletableFuture<InitializeResult> initialize( InitializeParams params )
		{
			this.shutdownCheck();
			return super.initialize( params );
		}

		@Override
		public void initialized( InitializedParams params )
		{
			if ( this.isActive() )
			{
				super.initialized( params );
			}
		}

		@Override
		public CompletableFuture<Object> shutdown()
		{
			this.initializeCheck();
			return super.shutdown();
		}

		//exit() gets processed regardless

		@Override
		public void cancelProgress( WorkDoneProgressCancelParams params )
		{
			if ( this.isActive() )
			{
				super.cancelProgress( params );
			}
		}


		// TextDocumentService

		@Override
		public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion( CompletionParams position )
		{
			this.stateCheck();
			return super.completion( position );
		}

		@Override
		public CompletableFuture<CompletionItem> resolveCompletionItem( CompletionItem unresolved )
		{
			this.stateCheck();
			return super.resolveCompletionItem( unresolved );
		}

		@Override
		public CompletableFuture<Hover> hover( HoverParams params )
		{
			this.stateCheck();
			return super.hover( params );
		}

		@Override
		public CompletableFuture<SignatureHelp> signatureHelp( SignatureHelpParams params )
		{
			this.stateCheck();
			return super.signatureHelp( params );
		}

		@Override
		public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration( DeclarationParams params )
		{
			this.stateCheck();
			return super.declaration( params );
		}

		@Override
		public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition( DefinitionParams params )
		{
			this.stateCheck();
			return super.definition( params );
		}

		@Override
		public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition( TypeDefinitionParams params )
		{
			this.stateCheck();
			return super.typeDefinition( params );
		}

		@Override
		public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation( ImplementationParams params )
		{
			this.stateCheck();
			return super.implementation( params );
		}

		@Override
		public CompletableFuture<List<? extends Location>> references( ReferenceParams params )
		{
			this.stateCheck();
			return super.references( params );
		}

		@Override
		public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight( DocumentHighlightParams params )
		{
			this.stateCheck();
			return super.documentHighlight( params );
		}

		@Override
		public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol( DocumentSymbolParams params )
		{
			this.stateCheck();
			return super.documentSymbol( params );
		}

		@Override
		public CompletableFuture<List<Either<Command, CodeAction>>> codeAction( CodeActionParams params )
		{
			this.stateCheck();
			return super.codeAction( params );
		}

		@Override
		public CompletableFuture<CodeAction> resolveCodeAction( CodeAction unresolved )
		{
			this.stateCheck();
			return super.resolveCodeAction( unresolved );
		}

		@Override
		public CompletableFuture<List<? extends CodeLens>> codeLens( CodeLensParams params )
		{
			this.stateCheck();
			return super.codeLens( params );
		}

		@Override
		public CompletableFuture<CodeLens> resolveCodeLens( CodeLens unresolved )
		{
			this.stateCheck();
			return super.resolveCodeLens( unresolved );
		}

		@Override
		public CompletableFuture<List<? extends TextEdit>> formatting( DocumentFormattingParams params )
		{
			this.stateCheck();
			return super.formatting( params );
		}

		@Override
		public CompletableFuture<List<? extends TextEdit>> rangeFormatting( DocumentRangeFormattingParams params )
		{
			this.stateCheck();
			return super.rangeFormatting( params );
		}

		@Override
		public CompletableFuture<List<? extends TextEdit>> onTypeFormatting( DocumentOnTypeFormattingParams params )
		{
			this.stateCheck();
			return super.onTypeFormatting( params );
		}

		@Override
		public CompletableFuture<WorkspaceEdit> rename( RenameParams params )
		{
			this.stateCheck();
			return super.rename( params );
		}

		@Override
		public CompletableFuture<LinkedEditingRanges> linkedEditingRange( LinkedEditingRangeParams params )
		{
			this.stateCheck();
			return super.linkedEditingRange( params );
		}

		@Override
		public void didOpen( DidOpenTextDocumentParams params )
		{
			if ( this.isActive() )
			{
				super.didOpen( params );
			}
		}

		@Override
		public void didChange( DidChangeTextDocumentParams params )
		{
			if ( this.isActive() )
			{
				super.didChange( params );
			}
		}

		@Override
		public void didClose( DidCloseTextDocumentParams params )
		{
			if ( this.isActive() )
			{
				super.didClose( params );
			}
		}

		@Override
		public void didSave( DidSaveTextDocumentParams params )
		{
			if ( this.isActive() )
			{
				super.didSave( params );
			}
		}

		@Override
		public void willSave( WillSaveTextDocumentParams params )
		{
			if ( this.isActive() )
			{
				super.willSave( params );
			}
		}

		@Override
		public CompletableFuture<List<TextEdit>> willSaveWaitUntil( WillSaveTextDocumentParams params )
		{
			this.stateCheck();
			return super.willSaveWaitUntil( params );
		}

		@Override
		public CompletableFuture<List<DocumentLink>> documentLink( DocumentLinkParams params )
		{
			this.stateCheck();
			return super.documentLink( params );
		}

		@Override
		public CompletableFuture<DocumentLink> documentLinkResolve( DocumentLink params )
		{
			this.stateCheck();
			return super.documentLinkResolve( params );
		}

		@Override
		public CompletableFuture<List<ColorInformation>> documentColor( DocumentColorParams params )
		{
			this.stateCheck();
			return super.documentColor( params );
		}

		@Override
		public CompletableFuture<List<ColorPresentation>> colorPresentation( ColorPresentationParams params )
		{
			this.stateCheck();
			return super.colorPresentation( params );
		}

		@Override
		public CompletableFuture<List<FoldingRange>> foldingRange( FoldingRangeRequestParams params )
		{
			this.stateCheck();
			return super.foldingRange( params );
		}

		@Override
		public CompletableFuture<Either<Range, PrepareRenameResult>> prepareRename( PrepareRenameParams params )
		{
			this.stateCheck();
			return super.prepareRename( params );
		}

		@Override
		public CompletableFuture<TypeHierarchyItem> typeHierarchy( TypeHierarchyParams params )
		{
			this.stateCheck();
			return super.typeHierarchy( params );
		}

		@Override
		public CompletableFuture<TypeHierarchyItem> resolveTypeHierarchy( ResolveTypeHierarchyItemParams params )
		{
			this.stateCheck();
			return super.resolveTypeHierarchy( params );
		}

		@Override
		public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy( CallHierarchyPrepareParams params )
		{
			this.stateCheck();
			return super.prepareCallHierarchy( params );
		}

		@Override
		public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls( CallHierarchyIncomingCallsParams params )
		{
			this.stateCheck();
			return super.callHierarchyIncomingCalls( params );
		}

		@Override
		public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls( CallHierarchyOutgoingCallsParams params )
		{
			this.stateCheck();
			return super.callHierarchyOutgoingCalls( params );
		}

		@Override
		public CompletableFuture<List<SelectionRange>> selectionRange( SelectionRangeParams params )
		{
			this.stateCheck();
			return super.selectionRange( params );
		}

		@Override
		public CompletableFuture<SemanticTokens> semanticTokensFull( SemanticTokensParams params )
		{
			this.stateCheck();
			return super.semanticTokensFull( params );
		}

		@Override
		public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta( SemanticTokensDeltaParams params )
		{
			this.stateCheck();
			return super.semanticTokensFullDelta( params );
		}

		@Override
		public CompletableFuture<SemanticTokens> semanticTokensRange( SemanticTokensRangeParams params )
		{
			this.stateCheck();
			return super.semanticTokensRange( params );
		}

		@Override
		public CompletableFuture<List<Moniker>> moniker( MonikerParams params )
		{
			this.stateCheck();
			return super.moniker( params );
		}


		// WorkspaceService

		@Override
		public CompletableFuture<Object> executeCommand( ExecuteCommandParams params )
		{
			this.stateCheck();
			return super.executeCommand( params );
		}

		@Override
		public CompletableFuture<List<? extends SymbolInformation>> symbol( WorkspaceSymbolParams params )
		{
			this.stateCheck();
			return super.symbol( params );
		}

		@Override
		public void didChangeConfiguration( DidChangeConfigurationParams params )
		{
			if ( this.isActive() )
			{
				super.didChangeConfiguration( params );
			}
		}

		@Override
		public void didChangeWatchedFiles( DidChangeWatchedFilesParams params )
		{
			if ( this.isActive() )
			{
				super.didChangeWatchedFiles( params );
			}
		}

		@Override
		public void didChangeWorkspaceFolders( DidChangeWorkspaceFoldersParams params )
		{
			if ( this.isActive() )
			{
				super.didChangeWorkspaceFolders( params );
			}
		}

		@Override
		public CompletableFuture<WorkspaceEdit> willCreateFiles( CreateFilesParams params )
		{
			this.stateCheck();
			return super.willCreateFiles( params );
		}

		@Override
		public void didCreateFiles( CreateFilesParams params )
		{
			if ( this.isActive() )
			{
				super.didCreateFiles( params );
			}
		}

		@Override
		public CompletableFuture<WorkspaceEdit> willRenameFiles( RenameFilesParams params )
		{
			this.stateCheck();
			return super.willRenameFiles( params );
		}

		@Override
		public void didRenameFiles( RenameFilesParams params )
		{
			if ( this.isActive() )
			{
				super.didRenameFiles( params );
			}
		}

		@Override
		public CompletableFuture<WorkspaceEdit> willDeleteFiles( DeleteFilesParams params )
		{
			this.stateCheck();
			return super.willDeleteFiles( params );
		}

		@Override
		public void didDeleteFiles( DeleteFilesParams params )
		{
			if ( this.isActive() )
			{
				super.didDeleteFiles( params );
			}
		}
	}
}
