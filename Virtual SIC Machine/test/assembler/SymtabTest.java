package assembler;

import java.util.List;
import java.util.Optional;

/**
 * Dependency-free tests for Symtab (the project has no JUnit on the classpath).
 * Run with: java -cp build/classes assembler.SymtabTest
 */
public class SymtabTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("starts empty", SymtabTest::startsEmpty);
        run("define then lookup", SymtabTest::defineThenLookup);
        run("lookup of unknown symbol is empty", SymtabTest::lookupUnknown);
        run("contains", SymtabTest::containsWorks);
        run("duplicate throws and keeps first value", SymtabTest::duplicateThrows);
        run("duplicate message and symbol", SymtabTest::duplicateMessage);
        run("duplicate with different type/address still throws", SymtabTest::duplicateDifferentData);
        run("names are case-sensitive", SymtabTest::caseSensitive);
        run("null name rejected", SymtabTest::nullName);
        run("blank name rejected", SymtabTest::blankName);
        run("negative address rejected", SymtabTest::negativeAddress);
        run("null type rejected", SymtabTest::nullType);
        run("address 0 and 0xFFFFF accepted", SymtabTest::boundaryAddresses);
        run("entries keeps insertion order", SymtabTest::insertionOrder);
        run("entries is read-only", SymtabTest::entriesReadOnly);
        run("clear empties the table", SymtabTest::clearWorks);
        run("many symbols", SymtabTest::manySymbols);
        run("toString lists symbols", SymtabTest::toStringLists);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---- tests ----

    static void startsEmpty() {
        Symtab t = new Symtab();
        check(t.isEmpty() && t.size() == 0, "new table should be empty");
    }

    static void defineThenLookup() {
        Symtab t = new Symtab();
        t.define("LOOP", 0x10, SymbolType.RELATIVE);
        SymbolData d = t.lookup("LOOP").orElseThrow();
        check(d.endereco() == 0x10, "address");
        check(d.tipo() == SymbolType.RELATIVE, "type");
        check(t.size() == 1, "size");
    }

    static void lookupUnknown() {
        check(new Symtab().lookup("NOPE").isEmpty(), "unknown should be empty");
    }

    static void containsWorks() {
        Symtab t = new Symtab();
        t.define("A", 1, SymbolType.ABSOLUTE);
        check(t.contains("A"), "contains A");
        check(!t.contains("B"), "not contains B");
    }

    static void duplicateThrows() {
        Symtab t = new Symtab();
        t.define("X", 5, SymbolType.RELATIVE);
        expect(DuplicateSymbolException.class, () -> t.define("X", 9, SymbolType.RELATIVE));
        check(t.lookup("X").orElseThrow().endereco() == 5, "first value must be preserved");
        check(t.size() == 1, "size unchanged");
    }

    static void duplicateMessage() {
        Symtab t = new Symtab();
        t.define("DUP", 1, SymbolType.RELATIVE);
        try {
            t.define("DUP", 2, SymbolType.RELATIVE);
            check(false, "should have thrown");
        } catch (DuplicateSymbolException e) {
            check(e.getSymbol().equals("DUP"), "getSymbol");
            check(e.getMessage().contains("Símbolo Duplicado"), "message: " + e.getMessage());
            check(e.getMessage().contains("DUP"), "message has name");
        }
    }

    static void duplicateDifferentData() {
        Symtab t = new Symtab();
        t.define("Z", 1, SymbolType.RELATIVE);
        expect(DuplicateSymbolException.class, () -> t.define("Z", 1, SymbolType.RELATIVE));
        expect(DuplicateSymbolException.class, () -> t.define("Z", 2, SymbolType.ABSOLUTE));
    }

    static void caseSensitive() {
        Symtab t = new Symtab();
        t.define("loop", 1, SymbolType.RELATIVE);
        t.define("LOOP", 2, SymbolType.RELATIVE);
        check(t.size() == 2, "two distinct symbols");
        check(t.lookup("loop").orElseThrow().endereco() == 1, "lowercase");
        check(t.lookup("LOOP").orElseThrow().endereco() == 2, "uppercase");
    }

    static void nullName() {
        expect(IllegalArgumentException.class, () -> new Symtab().define(null, 0, SymbolType.RELATIVE));
    }

    static void blankName() {
        expect(IllegalArgumentException.class, () -> new Symtab().define("", 0, SymbolType.RELATIVE));
        expect(IllegalArgumentException.class, () -> new Symtab().define("   ", 0, SymbolType.RELATIVE));
    }

    static void negativeAddress() {
        expect(IllegalArgumentException.class, () -> new Symtab().define("N", -1, SymbolType.RELATIVE));
    }

    static void nullType() {
        expect(IllegalArgumentException.class, () -> new Symtab().define("N", 0, null));
    }

    static void boundaryAddresses() {
        Symtab t = new Symtab();
        t.define("LOW", 0, SymbolType.ABSOLUTE);
        t.define("HIGH", 0xFFFFF, SymbolType.RELATIVE);
        check(t.lookup("LOW").orElseThrow().endereco() == 0, "low");
        check(t.lookup("HIGH").orElseThrow().endereco() == 0xFFFFF, "high");
    }

    static void insertionOrder() {
        Symtab t = new Symtab();
        t.define("C", 3, SymbolType.RELATIVE);
        t.define("A", 1, SymbolType.RELATIVE);
        t.define("B", 2, SymbolType.RELATIVE);
        check(List.copyOf(t.entries().keySet()).equals(List.of("C", "A", "B")), "order: " + t.entries().keySet());
    }

    static void entriesReadOnly() {
        Symtab t = new Symtab();
        t.define("A", 1, SymbolType.RELATIVE);
        expect(UnsupportedOperationException.class, () -> t.entries().put("B", new SymbolData(2, SymbolType.RELATIVE)));
        expect(UnsupportedOperationException.class, () -> t.entries().remove("A"));
    }

    static void clearWorks() {
        Symtab t = new Symtab();
        t.define("A", 1, SymbolType.RELATIVE);
        t.clear();
        check(t.isEmpty(), "empty after clear");
        t.define("A", 2, SymbolType.RELATIVE); // no duplicate error after clear
        check(t.lookup("A").orElseThrow().endereco() == 2, "redefined");
    }

    static void manySymbols() {
        Symtab t = new Symtab();
        for (int i = 0; i < 10_000; i++) {
            t.define("S" + i, i, SymbolType.RELATIVE);
        }
        check(t.size() == 10_000, "size");
        Optional<SymbolData> d = t.lookup("S9999");
        check(d.isPresent() && d.get().endereco() == 9999, "last symbol");
    }

    static void toStringLists() {
        Symtab t = new Symtab();
        t.define("FIRST", 0x1A, SymbolType.RELATIVE);
        String s = t.toString();
        check(s.contains("FIRST") && s.contains("00001A") && s.contains("RELATIVE"), "toString: " + s);
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
