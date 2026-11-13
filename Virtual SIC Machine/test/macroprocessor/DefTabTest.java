package macroprocessor;

/**
 * Dependency-free tests for DefTab and for its cooperation with NamTab.
 * Run with: java -cp build/classes macroprocessor.DefTabTest
 */
public class DefTabTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("starts empty", DefTabTest::startsEmpty);
        run("addLine then getLine", DefTabTest::addThenGet);
        run("size grows with each line", DefTabTest::sizeGrows);
        run("lines keep insertion order", DefTabTest::keepsOrder);
        run("lines are stored verbatim", DefTabTest::storedVerbatim);
        run("empty line accepted", DefTabTest::emptyLine);
        run("null line rejected", DefTabTest::nullLine);
        run("getLine negative index throws", DefTabTest::negativeIndex);
        run("getLine past the end throws", DefTabTest::pastEnd);
        run("getLine on empty table throws", DefTabTest::emptyTableGet);
        run("clear empties the table", DefTabTest::clearWorks);
        run("many lines", DefTabTest::manyLines);
        run("toString lists indexed lines", DefTabTest::toStringLists);
        run("two macros get contiguous, non-overlapping ranges", DefTabTest::twoMacrosRanges);
        run("nested definition keeps inner MACRO/MEND in the body", DefTabTest::nestedRange);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void startsEmpty() {
        DefTab d = new DefTab();
        check(d.getCurrentSize() == 0 && d.isEmpty(), "empty");
    }

    static void addThenGet() {
        DefTab d = new DefTab();
        d.addLine("LDA ?1");
        check(d.getLine(0).equals("LDA ?1"), "line 0");
    }

    static void sizeGrows() {
        DefTab d = new DefTab();
        for (int i = 1; i <= 5; i++) {
            d.addLine("L" + i);
            check(d.getCurrentSize() == i, "size after " + i);
        }
        check(!d.isEmpty(), "not empty");
    }

    static void keepsOrder() {
        DefTab d = new DefTab();
        d.addLine("A");
        d.addLine("B");
        d.addLine("C");
        check(d.getLine(0).equals("A") && d.getLine(1).equals("B") && d.getLine(2).equals("C"), "order");
    }

    static void storedVerbatim() {
        DefTab d = new DefTab();
        String s = "LOOP\tSTA  ?1,X   . comment";
        d.addLine(s);
        check(d.getLine(0).equals(s), "line must not be trimmed or altered");
    }

    static void emptyLine() {
        DefTab d = new DefTab();
        d.addLine("");
        check(d.getCurrentSize() == 1 && d.getLine(0).isEmpty(), "empty line is a valid body line");
    }

    static void nullLine() {
        DefTab d = new DefTab();
        expect(IllegalArgumentException.class, () -> d.addLine(null));
        check(d.getCurrentSize() == 0, "size unchanged");
    }

    static void negativeIndex() {
        DefTab d = new DefTab();
        d.addLine("A");
        expect(IndexOutOfBoundsException.class, () -> d.getLine(-1));
    }

    static void pastEnd() {
        DefTab d = new DefTab();
        d.addLine("A");
        expect(IndexOutOfBoundsException.class, () -> d.getLine(1));
    }

    static void emptyTableGet() {
        expect(IndexOutOfBoundsException.class, () -> new DefTab().getLine(0));
    }

    static void clearWorks() {
        DefTab d = new DefTab();
        d.addLine("A");
        d.clear();
        check(d.isEmpty() && d.getCurrentSize() == 0, "cleared");
        expect(IndexOutOfBoundsException.class, () -> d.getLine(0));
    }

    static void manyLines() {
        DefTab d = new DefTab();
        for (int i = 0; i < 100_000; i++) {
            d.addLine("LINE" + i);
        }
        check(d.getCurrentSize() == 100_000, "size");
        check(d.getLine(99_999).equals("LINE99999"), "last line");
    }

    static void toStringLists() {
        DefTab d = new DefTab();
        d.addLine("LDA ?1");
        d.addLine("MEND");
        String s = d.toString();
        check(s.contains("0") && s.contains("LDA ?1") && s.contains("MEND"), "toString: " + s);
    }

    /** Same sequence MacroProcessor.define() performs, to check both tables agree. */
    private static void define(NamTab n, DefTab d, String name, String... body) {
        n.addMacro(name, d.getCurrentSize());
        for (String line : body) {
            d.addLine(line);
        }
        n.setEndIndex(name, d.getCurrentSize() - 1);
    }

    static void twoMacrosRanges() {
        NamTab n = new NamTab();
        DefTab d = new DefTab();
        define(n, d, "A", "LDA ?1", "STA ?2", "MEND");
        define(n, d, "B", "LDX ?1", "MEND");
        NamTab.MacroInfo a = n.getMacro("A");
        NamTab.MacroInfo b = n.getMacro("B");
        check(a.startIndex == 0 && a.endIndex == 2, "A range");
        check(b.startIndex == 3 && b.endIndex == 4, "B range");
        check(d.getLine(a.endIndex).equals("MEND") && d.getLine(b.endIndex).equals("MEND"), "ranges end at MEND");
        check(d.getLine(b.startIndex).equals("LDX ?1"), "B starts right after A");
    }

    static void nestedRange() {
        NamTab n = new NamTab();
        DefTab d = new DefTab();
        define(n, d, "OUTER", "INNER MACRO", "LDA ?1", "MEND", "INNER ?1", "MEND");
        NamTab.MacroInfo o = n.getMacro("OUTER");
        check(o.startIndex == 0 && o.endIndex == 4, "outer spans inner MACRO..MEND");
        check(d.getLine(o.endIndex).equals("MEND"), "last line is the outer MEND");
        check(!n.contains("INNER"), "inner macro is not registered until the outer one is expanded");
    }

    // ---- mini harness ----

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Throwable e) {
            failed++;
            System.out.println("FAIL  " + name + " -> " + e);
        }
    }

    private static void check(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + e);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
