package macroprocessor;

/**
 * Dependency-free tests for NamTab (the project has no JUnit on the classpath).
 * Run with: java -cp build/classes macroprocessor.NamTabTest
 */
public class NamTabTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("starts empty", NamTabTest::startsEmpty);
        run("addMacro then getMacro", NamTabTest::addThenGet);
        run("endIndex is -1 until set", NamTabTest::endIndexOpen);
        run("setEndIndex updates the macro", NamTabTest::setEnd);
        run("setEndIndex on unknown macro throws", NamTabTest::setEndUnknown);
        run("setEndIndex before start throws", NamTabTest::setEndBeforeStart);
        run("contains detects macro calls", NamTabTest::containsWorks);
        run("contains(null) is false", NamTabTest::containsNull);
        run("getMacro of unknown name is null", NamTabTest::getUnknown);
        run("duplicate macro throws and keeps first", NamTabTest::duplicateThrows);
        run("names are case-sensitive", NamTabTest::caseSensitive);
        run("null name rejected", NamTabTest::nullName);
        run("blank name rejected", NamTabTest::blankName);
        run("negative start rejected", NamTabTest::negativeStart);
        run("start 0 and one-line macro accepted", NamTabTest::boundaries);
        run("entries keeps insertion order", NamTabTest::insertionOrder);
        run("entries is read-only", NamTabTest::entriesReadOnly);
        run("clear empties the table", NamTabTest::clearWorks);
        run("many macros", NamTabTest::manyMacros);
        run("toString lists macros", NamTabTest::toStringLists);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void startsEmpty() {
        NamTab t = new NamTab();
        check(t.size() == 0, "size");
        check(t.entries().isEmpty(), "entries");
    }

    static void addThenGet() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 5);
        NamTab.MacroInfo i = t.getMacro("COPY");
        check(i != null && i.startIndex == 5, "startIndex");
        check(t.size() == 1, "size");
    }

    static void endIndexOpen() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 0);
        check(t.getMacro("COPY").endIndex == -1, "endIndex should be -1");
    }

    static void setEnd() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 2);
        t.setEndIndex("COPY", 6);
        check(t.getMacro("COPY").endIndex == 6, "endIndex");
        check(t.getMacro("COPY").startIndex == 2, "startIndex untouched");
    }

    static void setEndUnknown() {
        expect(IllegalArgumentException.class, () -> new NamTab().setEndIndex("NOPE", 3));
    }

    static void setEndBeforeStart() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 4);
        expect(IllegalArgumentException.class, () -> t.setEndIndex("COPY", 3));
        check(t.getMacro("COPY").endIndex == -1, "endIndex must stay open");
    }

    static void containsWorks() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 0);
        check(t.contains("COPY"), "registered");
        check(!t.contains("LDA"), "plain instruction is not a macro");
        check(!t.contains("MACRO"), "directive is not a macro");
    }

    static void containsNull() {
        check(!new NamTab().contains(null), "null opcode");
    }

    static void getUnknown() {
        check(new NamTab().getMacro("NOPE") == null, "should be null");
    }

    static void duplicateThrows() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 1);
        expect(IllegalArgumentException.class, () -> t.addMacro("COPY", 9));
        check(t.getMacro("COPY").startIndex == 1, "first definition must be kept");
        check(t.size() == 1, "size");
    }

    static void caseSensitive() {
        NamTab t = new NamTab();
        t.addMacro("copy", 0);
        t.addMacro("COPY", 3);
        check(t.size() == 2, "two distinct macros");
        check(t.getMacro("copy").startIndex == 0 && t.getMacro("COPY").startIndex == 3, "indices");
    }

    static void nullName() {
        expect(IllegalArgumentException.class, () -> new NamTab().addMacro(null, 0));
    }

    static void blankName() {
        expect(IllegalArgumentException.class, () -> new NamTab().addMacro("  ", 0));
        expect(IllegalArgumentException.class, () -> new NamTab().addMacro("", 0));
    }

    static void negativeStart() {
        expect(IllegalArgumentException.class, () -> new NamTab().addMacro("COPY", -1));
    }

    static void boundaries() {
        NamTab t = new NamTab();
        t.addMacro("ONE", 0);
        t.setEndIndex("ONE", 0); // end == start: macro made of a single line
        check(t.getMacro("ONE").endIndex == 0, "single-line macro");
    }

    static void insertionOrder() {
        NamTab t = new NamTab();
        String[] names = {"ZED", "ALPHA", "MID", "BETA"};
        for (int i = 0; i < names.length; i++) {
            t.addMacro(names[i], i);
        }
        check(java.util.List.copyOf(t.entries().keySet()).equals(java.util.List.of(names)), "order");
    }

    static void entriesReadOnly() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 0);
        expect(UnsupportedOperationException.class, () -> t.entries().put("X", null));
        expect(UnsupportedOperationException.class, () -> t.entries().clear());
    }

    static void clearWorks() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 0);
        t.clear();
        check(t.size() == 0 && !t.contains("COPY"), "cleared");
        t.addMacro("COPY", 7); // can be registered again
        check(t.getMacro("COPY").startIndex == 7, "re-add after clear");
    }

    static void manyMacros() {
        NamTab t = new NamTab();
        for (int i = 0; i < 10_000; i++) {
            t.addMacro("M" + i, i * 3);
            t.setEndIndex("M" + i, i * 3 + 2);
        }
        check(t.size() == 10_000, "size");
        check(t.getMacro("M9999").startIndex == 29997 && t.getMacro("M9999").endIndex == 29999, "last macro");
    }

    static void toStringLists() {
        NamTab t = new NamTab();
        t.addMacro("COPY", 2);
        t.setEndIndex("COPY", 5);
        String s = t.toString();
        check(s.contains("COPY") && s.contains("2") && s.contains("5"), "toString: " + s);
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
