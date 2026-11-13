package macroprocessor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Name table (NAMTAB) of the macro processor.
 *
 * Maps each macro name to the range of lines its body occupies in the DEFTAB.
 * Backed by a LinkedHashMap: O(1) lookup plus a deterministic (insertion)
 * order when the table is listed. Names are case-sensitive.
 */
public class NamTab {

    /** Pointers into the DEFTAB; endIndex is inclusive and points at the MEND line. */
    public static class MacroInfo {
        public final int startIndex;
        public int endIndex = -1; // -1 while the definition is still open

        MacroInfo(int startIndex) {
            this.startIndex = startIndex;
        }
    }

    private final Map<String, MacroInfo> table = new LinkedHashMap<>();

    /**
     * Registers a new macro.
     *
     * @throws IllegalArgumentException if the name is null/blank, the index is
     *                                  negative or the macro is already registered
     */
    public void addMacro(String name, int start) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Macro name must not be null or blank");
        }
        if (start < 0) {
            throw new IllegalArgumentException("Macro start index must not be negative: " + start);
        }
        if (table.putIfAbsent(name, new MacroInfo(start)) != null) {
            throw new IllegalArgumentException("Macro already defined: " + name);
        }
    }

    /**
     * Sets the last DEFTAB line (the MEND) of a registered macro.
     *
     * @throws IllegalArgumentException if the macro is unknown or end < start
     */
    public void setEndIndex(String name, int end) {
        MacroInfo info = table.get(name);
        if (info == null) {
            throw new IllegalArgumentException("Unknown macro: " + name);
        }
        if (end < info.startIndex) {
            throw new IllegalArgumentException(
                    "End index " + end + " is before start index " + info.startIndex + " of macro " + name);
        }
        info.endIndex = end;
    }

    /** True if the opcode is the name of a registered macro (i.e. a macro call). */
    public boolean contains(String opcode) {
        return opcode != null && table.containsKey(opcode);
    }

    /** Returns the pointers of a macro, or null if it is not registered. */
    public MacroInfo getMacro(String name) {
        return table.get(name);
    }

    public int size() {
        return table.size();
    }

    public void clear() {
        table.clear();
    }

    /** Read-only view of the table, in insertion order. */
    public Map<String, MacroInfo> entries() {
        return Collections.unmodifiableMap(table);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NAMTAB\n");
        table.forEach((name, i) -> sb.append(String.format("%-10s %4d %4d%n", name, i.startIndex, i.endIndex)));
        return sb.toString();
    }
}
