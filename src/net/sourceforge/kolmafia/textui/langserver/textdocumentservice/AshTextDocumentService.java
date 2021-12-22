package net.sourceforge.kolmafia.textui.langserver.textdocumentservice;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.sourceforge.kolmafia.textui.langserver.AshLanguageServer;
import net.sourceforge.kolmafia.textui.langserver.FilesMonitor;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public abstract class AshTextDocumentService implements TextDocumentService {
  protected final AshLanguageServer parent;

  private final SymbolManager symbolManager;
  private final SemanticTokensHandler semanticHandler;

  public AshTextDocumentService(final AshLanguageServer parent) {
    this.parent = parent;
    this.symbolManager = new SymbolManager(parent);
    this.semanticHandler = new SemanticTokensHandler(parent);
  }

  public final void setCapabilities(final ServerCapabilities capabilities) {
    TextDocumentSyncOptions textDocumentSyncOptions = new TextDocumentSyncOptions();
    textDocumentSyncOptions.setOpenClose(true);
    textDocumentSyncOptions.setChange(TextDocumentSyncKind.Full);
    textDocumentSyncOptions.setWillSave(false);
    textDocumentSyncOptions.setWillSaveWaitUntil(false);
    textDocumentSyncOptions.setSave(new SaveOptions(false));

    capabilities.setTextDocumentSync(textDocumentSyncOptions);

    // completionProvider

    // hoverProvider

    // signatureHelpProvider

    this.symbolManager.setCapabilities(capabilities);

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

    this.semanticHandler.setCapabilities(capabilities);

    // monikerProvider
    // not even sure what that it? Looking into the docs,
    // it seems like it's something that "should" be a normal feature,
    // but then why was it added so recently? TODO look into this
    // https://microsoft.github.io/language-server-protocol/specifications/specification-3-17/#textDocument_moniker

    // typeHierarchyProvider
    // Neither part of LSP yet, nor a thing in ASH
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    this.parent.executor.execute(
        () -> {
          TextDocumentItem document = params.getTextDocument();

          File file = FilesMonitor.URIToFile(document.getUri());

          this.parent.monitor.updateFile(file, document.getText(), document.getVersion());
        });
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    this.parent.executor.execute(
        () -> {
          VersionedTextDocumentIdentifier document = params.getTextDocument();
          List<TextDocumentContentChangeEvent> changes = params.getContentChanges();

          if (changes.size() == 0) {
            // Nothing to see here
            return;
          }

          File file = FilesMonitor.URIToFile(document.getUri());

          // We don't support incremental changes, so we expect the client
          // to put the whole file's content in a single TextDocumentContentChangeEvent
          this.parent.monitor.updateFile(file, changes.get(0).getText(), document.getVersion());
        });
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    this.parent.executor.execute(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();

          File file = FilesMonitor.URIToFile(document.getUri());

          this.parent.monitor.updateFile(file, null, -1);
        });
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
      definition(DefinitionParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();
          Position position = params.getPosition();

          File file = FilesMonitor.URIToFile(document.getUri());

          return Either.forLeft(this.symbolManager.getDefinition(file, position));
        },
        this.parent.executor);
  }

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
      typeDefinition(TypeDefinitionParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();
          Position position = params.getPosition();

          File file = FilesMonitor.URIToFile(document.getUri());

          return Either.forLeft(this.symbolManager.getTypeDefinition(file, position));
        },
        this.parent.executor);
  }

  @Override
  public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();
          Position position = params.getPosition();
          // params.getContext().isIncludeDeclaration(); //for when we support declaration

          File file = FilesMonitor.URIToFile(document.getUri());

          return this.symbolManager.getReferences(file, position);
        },
        this.parent.executor);
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();

          File file = FilesMonitor.URIToFile(document.getUri());

          return this.semanticHandler.getSemanticTokens(file, null);
        },
        this.parent.executor);
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
    return CompletableFuture.supplyAsync(
        () -> {
          TextDocumentIdentifier document = params.getTextDocument();
          Range range = params.getRange();

          File file = FilesMonitor.URIToFile(document.getUri());

          return this.semanticHandler.getSemanticTokens(file, range);
        },
        this.parent.executor);
  }
}
