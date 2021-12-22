package net.sourceforge.kolmafia.textui.langserver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.java.dev.spellcast.utilities.DataUtilities;
import net.sourceforge.kolmafia.KoLConstants;

public class FilesMonitor {
  final AshLanguageServer parent;

  FilesMonitor(final AshLanguageServer parent) {
    this.parent = parent;
  }

  public void updateFile(final File file, final String text, final int version) {
    if (file == null) {
      return;
    }

    synchronized (this.parent.scripts) {
      final Script script = this.getScript(file);

      final List<Script.Handler> handlers = this.findHandlers(file);

      if (handlers.size() > 0
          && ((script.text != null && script.version >= version)
              || (script.text == null && text == null))) {
        // We already have a working handler using an
        // up-to-date version of that file.
        return;
      }

      if (script.text == null || script.version < version || script.text != null && text == null) {
        script.text = text;
        script.version = version;
      }

      if (handlers.size() == 0) {
        // make a new handler
        handlers.add(script.makeHandler());
      } else {
        for (final Script.Handler handler : handlers) {
          this.parent.executor.execute(
              () -> {
                handler.refreshParsing();
              });
        }
      }
    }
  }

  /** Fetches or makes a Script for the given file. */
  private Script getScript(final File file) {
    synchronized (this.parent.scripts) {
      Script script = this.parent.scripts.get(file);
      if (script == null) {
        script = new Script(this.parent, file);
        this.parent.scripts.put(file, script);
      }

      return script;
    }
  }

  List<Script.Handler> findHandlers(final File file) {
    final List<Script.Handler> handlers = new LinkedList<>();

    synchronized (this.parent.scripts) {
      for (final Script script : this.parent.scripts.values()) {
        if (script.handler != null
            && script.handler.parser != null
            && script.handler.parser.getImports().containsKey(file)) {
          handlers.add(script.handler);
        }
      }
    }

    return handlers;
  }

  public List<Script.Handler> findOrMakeHandler(final File file) {
    final List<Script.Handler> handlers;

    synchronized (this.parent.scripts) {
      final Script script = this.getScript(file);

      handlers = this.findHandlers(file);

      if (handlers.size() == 0) {
        // make a new handler
        handlers.add(script.makeHandler());
      }
    }

    return handlers;
  }

  void scan() {
    for (final File directory :
        Arrays.asList(
            KoLConstants.SCRIPT_LOCATION,
            KoLConstants.PLOTS_LOCATION,
            KoLConstants.RELAY_LOCATION)) {
      this.scan(directory);
    }
  }

  void scan(final File directory) {
    for (final File file : Arrays.asList(DataUtilities.listFiles(directory))) {
      if (Thread.interrupted()) {
        break;
      }

      if (file.isDirectory()) {
        this.scan(file);
      } else if (file.isFile() && file.getName().endsWith(".ash")) {
        this.findOrMakeHandler(file);
      }
    }
  }

  public static File URIToFile(String uri) {
    if (uri == null) {
      return null;
    }

    try {
      return new File(new URI(uri));
    } catch (URISyntaxException e) {
      // A bad URI... oh boy...

      // The most likely guess is that the
      // client just didn't encode the URI.
      // Try feeding its path directly to File(String)

      // First, remove the scheme
      String scheme = "";
      if (uri.length() >= 5) {
        scheme = uri.substring(0, 5);
      }

      if (scheme.equalsIgnoreCase("file:")) {
        uri = uri.substring(5);
      } else {
        // That's not even a file.
        return null;
      }

      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        if (uri.startsWith("///") || uri.startsWith("\\\\\\")) {
          uri = uri.substring(3);
        } else if (uri.startsWith("/") || uri.startsWith("\\")) {
          uri = uri.substring(1);
        }
      } else {
        if (uri.startsWith("//") || uri.startsWith("\\\\")) {
          uri = uri.substring(2);
        }
      }

      return new File(uri);
    } catch (IllegalArgumentException e) {
      // We got a correct URI, but it doesn't point
      // to a file. That's the caller's problem.
      return null;
    }
  }
}
