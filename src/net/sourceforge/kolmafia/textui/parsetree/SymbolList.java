package net.sourceforge.kolmafia.textui.parsetree;

import java.util.Iterator;
import java.util.List;
import org.eclipse.lsp4j.Location;

public abstract class SymbolList<S extends Symbol> implements Iterable<S> {
  public abstract boolean add(final S s);

  public abstract Iterator<S> iterator();

  public abstract boolean contains(final S s);

  public abstract SymbolList<S> clone();

  public abstract void addReference(final S s, final Location location);

  public abstract List<Location> getReferences(final S s);
}
