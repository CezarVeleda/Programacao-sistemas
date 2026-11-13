package macroprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition table (DEFTAB) of the macro processor.
 *
 * Holds the body lines of every macro in one contiguous list; the NAMTAB
 * keeps the start/end index of each macro inside it. Formal parameters
 * (&ARG) must already be converted to positional markers (?1, ?2) before
 * a line is added.
 */
public class DefTab {

    private final List<String> lines = new ArrayList<>();

    /** Appends a line at the end of the table. */
    public void addLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("DEFTAB line must not be null");
        }
        lines.add(line);
    }

    /**
     * Returns the line stored at the given index.
     *
     * @throws IndexOutOfBoundsException if the index is outside the table
     */
    public String getLine(int index) {
        if (index < 0 || index >= lines.size()) {
            throw new IndexOutOfBoundsException("DEFTAB index " + index + " out of range (size " + lines.size() + ")");
        }
        return lines.get(index);
    }

    /** Current size; also the index the next added line will receive (a macro's startIndex). */
    public int getCurrentSize() {
        return lines.size();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DEFTAB\n");
        for (int i = 0; i < lines.size(); i++) {
            sb.append(String.format("%4d  %s%n", i, lines.get(i)));
        }
        return sb.toString();
    }
}
