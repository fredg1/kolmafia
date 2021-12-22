package net.sourceforge.kolmafia.textui.langserver;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ServerInfo;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import net.sourceforge.kolmafia.StaticEntity;

import net.sourceforge.kolmafia.textui.langserver.textdocumentservice.AshTextDocumentService;
import net.sourceforge.kolmafia.textui.langserver.workspaceservice.AshWorkspaceService;

/**
 * The thread in charge of listening for the client's messages.
 * Its methods all quickly delegate to other threads, because we want to avoid
 * blocking the reading of new messages.
 * <p>
 * Was made abstract, because the actual class to use is {@link StateCheckWrappers.AshLanguageServer}.
 */
public abstract class AshLanguageServer
	implements LanguageClientAware, LanguageServer
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

	private ServerState state = ServerState.STARTED;

	enum ServerState
	{
		STARTED,
		INITIALIZED,
		SHUTDOWN
	}

	public final ServerState getState()
	{
		return this.state;
	}

	protected abstract boolean notInitialized();
	protected abstract boolean wasShutdown();
	protected abstract boolean isActive();
	protected abstract void initializeCheck();
	protected abstract void shutdownCheck();
	protected abstract void stateCheck();

	public LanguageClient client;
	public ClientCapabilities clientCapabilities;

	public final AshTextDocumentService textDocumentService = new StateCheckWrappers.AshTextDocumentService( this );
	public final AshWorkspaceService workspaceService = new StateCheckWrappers.AshWorkspaceService( this );

	public final ExecutorService executor = Executors.newCachedThreadPool();
	public final FilesMonitor monitor = new FilesMonitor( this );

	public final Map<File, Script> scripts = Collections.synchronizedMap( new Hashtable<>( 20 ) );


	@Override
	public void connect( LanguageClient client )
	{
		this.client = client;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize( InitializeParams params )
	{
		this.state = ServerState.INITIALIZED;

		this.clientCapabilities = params.getCapabilities();
		// params.getTrace(); for when we implement trace
		// params.getClientInfo(); do we need/care about that?
		// params.getWorkspaceFolders(); look into this later

		this.executor.execute( () -> {
			this.monitor.scan();
		} );

		return CompletableFuture.supplyAsync( () -> {
			final ServerCapabilities capabilities = new ServerCapabilities();

			this.textDocumentService.setCapabilities( capabilities );
			this.workspaceService.setCapabilities( capabilities );

			final ServerInfo info = new ServerInfo( StaticEntity.getVersion() );

			return new InitializeResult( capabilities, info );
		}, this.executor );
	}

	@Override
	public CompletableFuture<Object> shutdown()
	{
		this.state = ServerState.SHUTDOWN;

		return CompletableFuture.supplyAsync( () -> {
			for ( final Script script : this.scripts.values() )
			{
				if ( script.handler != null )
				{
					script.handler.close();
				}
			}

			return null;
		}, this.executor );
	}

	@Override
	public void exit()
	{
		System.exit( this.state == ServerState.SHUTDOWN ? 0 : 1 );
	}

	@Override
	public TextDocumentService getTextDocumentService()
	{
		return this.textDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService()
	{
		return this.workspaceService;
	}
}
