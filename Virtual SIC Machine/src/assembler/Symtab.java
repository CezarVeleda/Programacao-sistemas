package assembler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Symbol table (SYMTAB) built during the first pass and read in the second.
 *
 * Backed by a LinkedHashMap: O(1) lookup like a HashMap, plus a deterministic
 * (insertion) order when the table is listed. Keys are clean label names
 * (no #, @ or ,X) and are case-sensitive.
 */
public class Symtab {

    private final Map<String, SymbolData> table = new LinkedHashMap<>();

    /**
     * Defines a new symbol.
     *
     * @throws DuplicateSymbolException if the label already exists
     * @throws IllegalArgumentException if the name is null/blank or the address is negative
     */
    public void define(String name, int endereco, SymbolType tipo) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Symbol name must not be null or blank");
        }
        if (endereco < 0) {
            throw new IllegalArgumentException("Symbol address must not be negative: " + endereco);
        }
        SymbolData data = new SymbolData(endereco, tipo);
        if (table.putIfAbsent(name, data) != null) {
            throw new DuplicateSymbolException(name);
        }
    }

    /** Looks a symbol up; empty if it is not defined. */
    public Optional<SymbolData> lookup(String name) {
        return Optional.ofNullable(table.get(name));
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }

    public int size() {
        return table.size();
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public void clear() {
        table.clear();
    }

    /** Read-only view of the table, in insertion order. */
    public Map<String, SymbolData> entries() {
        return Collections.unmodifiableMap(table);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SYMTAB\n");
        table.forEach((name, d) -> sb.append(String.format("%-10s %06X  %s%n", name, d.endereco(), d.tipo())));
        return sb.toString();
    }
}
