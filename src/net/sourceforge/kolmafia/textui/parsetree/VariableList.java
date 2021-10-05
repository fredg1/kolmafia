package net.sourceforge.kolmafia.textui.parsetree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.lsp4j.Location;

public class VariableList extends SymbolList<Variable> {
  private final Map<Variable, List<Location>> list = new TreeMap<>();

  public boolean add(final Variable n) {
    if (this.find(n.getName()) != null) {
      return false;
    }

    list.put(n, new ArrayList<>());
    return true;
  }

  public Variable find(final String name) {
    for (Variable variable : this.list.keySet()) {
      if (variable != null && name != null && name.equalsIgnoreCase(variable.getName())) {
        return variable;
      }
    }

    return null;
  }

  public Iterator<Variable> iterator() {
    return list.keySet().iterator();
  }

  public boolean contains(final Variable variable) {
    return list.containsKey(variable);
  }

  public VariableList clone() {
    VariableList result = new VariableList();

    for (Variable variable : this.list.keySet()) {
      result.add(variable);
    }

    return result;
  }

  public void addReference(final Variable variable, final Location location) {
    List<Location> references = this.list.get(variable);

    if (references != null) {
      references.add(location);
    }
  }

  public List<Location> getReferences(final Variable variable) {
    return this.list.get(variable);
  }
}
