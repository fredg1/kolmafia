package net.sourceforge.kolmafia.textui.parsetree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.lsp4j.Location;

public class FunctionList extends SymbolList<Function> {
  private final SortedMap<String, Function> list = new TreeMap<String, Function>();
  private final Map<Function, List<Location>> references = new TreeMap<>(FUNCTION_COMPARATOR);

  // Assumes there will not be more than 65535 functions in any scope.
  // Assumes that \0 will never appear in a function name.
  private char sequence = '\0';

  public boolean add(final Function f) {
    this.list.put(f.getName().toLowerCase() + '\0' + this.sequence, f);
    this.references.put(f, new ArrayList<>());
    ++this.sequence;
    return true;
  }

  public boolean remove(final Function f) {
    return this.list.values().remove(f);
  }

  public Function[] findFunctions(String name) {
    name = name.toLowerCase();
    return this.list.subMap(name + '\0', name + '\1').values().toArray(new Function[0]);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public Iterator<Function> iterator() {
    return list.values().iterator();
  }

  public boolean contains(final Function f) {
    return list.values().contains(f);
  }

  public FunctionList clone() {
    FunctionList result = new FunctionList();

    for (Function type : this.list.values()) {
      result.add(type);
    }

    return result;
  }

  public void addReference(final Function function, final Location location) {
    List<Location> references = this.references.get(function);

    if (references != null) {
      references.add(location);
    }
  }

  public List<Location> getReferences(final Function function) {
    return this.references.get(function);
  }

  private static final Comparator<Function> FUNCTION_COMPARATOR =
      (Function f1, Function f2) -> {
        int comparison = f1.compareTo(f2);

        if (comparison != 0) {
          return comparison;
        }

        Iterator<VariableReference> f1Refs = f1.getVariableReferences().iterator();
        Iterator<VariableReference> f2Refs = f2.getVariableReferences().iterator();

        while (f1Refs.hasNext()) {
          if (!f2Refs.hasNext()) {
            return 1;
          }

          Type f1Type = f1Refs.next().getRawType();
          Type f2Type = f2Refs.next().getRawType();

          comparison = compareTypes(f1Type, f2Type);

          if (comparison != 0) {
            return comparison;
          }
        }

        if (f2Refs.hasNext()) {
          return -1;
        }

        return 0;
      };

  private static final int compareTypes(final Type t1, final Type t2) {
    int comparison = t1.getType() - t2.getType();

    if (comparison != 0) {
      return comparison;
    }

    if (t1 instanceof AggregateType) {
      comparison = compareTypes(((AggregateType) t1).dataType, ((AggregateType) t2).dataType);

      if (comparison == 0) {
        comparison = compareTypes(((AggregateType) t1).indexType, ((AggregateType) t2).indexType);
      }
    } else if (t1 instanceof RecordType) {
      comparison = t1.compareTo(t2);
    } else if (t1 instanceof TypeDef) {
      comparison = compareTypes(((TypeDef) t1).base, ((TypeDef) t2).base);
    }

    return comparison;
  }
}
