package net.sourceforge.kolmafia.textui.parsetree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.lsp4j.Location;

public class TypeList extends SymbolList<Type> {
  private final Map<Type, List<Location>> list = new TreeMap<>();

  public boolean add(final Type n) {
    if (this.find(n.getName()) != null) {
      return false;
    }

    list.put(n, new ArrayList<>());
    return true;
  }

  public Type find(final String name) {
    for (Type currentType : this.list.keySet()) {
      if (name != null && name.equalsIgnoreCase(currentType.getName())) {
        return currentType;
      }
    }

    return null;
  }

  public Iterator<Type> iterator() {
    return list.keySet().iterator();
  }

  public boolean contains(final Type type) {
    return list.containsKey(type);
  }

  public TypeList clone() {
    TypeList result = new TypeList();

    for (Type type : this.list.keySet()) {
      result.add(type);
    }

    return result;
  }

  public void addReference(final Type type, final Location location) {
    List<Location> references = this.list.get(type);

    if (references != null) {
      references.add(location);
    }
  }

  public List<Location> getReferences(final Type type) {
    return this.list.get(type);
  }
}
