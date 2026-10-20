package assembler;

import java.util.Map;

/**
 * Dependency-free tests for Optab (the project has no JUnit on the classpath).
 * Run with: java -cp build/classes assembler.OptabTest
 */
public class OptabTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final Map<String, Integer> F3_OPCODES = Map.ofEntries(
            Map.entry("ADD", 0x18), Map.entry("AND", 0x40), Map.entry("COMP", 0x28),
            Map.entry("DIV", 0x24), Map.entry("J", 0x3C), Map.entry("JEQ", 0x30),
            Map.entry("JGT", 0x34), Map.entry("JLT", 0x38), Map.entry("JSUB", 0x48),
            Map.entry("LDA", 0x00), Map.entry("LDB", 0x68), Map.entry("LDCH", 0x50),
            Map.entry("LDL", 0x08), Map.entry("LDS", 0x6C), Map.entry("LDT", 0x74),
            Map.entry("LDX", 0x04), Map.entry("MUL", 0x20), Map.entry("OR", 0x44),
            Map.entry("RSUB", 0x4C), Map.entry("STA", 0x0C), Map.entry("STB", 0x78),
            Map.entry("STCH", 0x54), Map.entry("STL", 0x14), Map.entry("STS", 0x7C),
            Map.entry("STT", 0x84), Map.entry("STX", 0x10), Map.entry("SUB", 0x1C),
            Map.entry("TIX", 0x2C));

    private static final Map<String, Integer> F2_OPCODES = Map.ofEntries(
            Map.entry("ADDR", 0x90), Map.entry("CLEAR", 0xB4), Map.entry("COMPR", 0xA0),
            Map.entry("DIVR", 0x9C), Map.entry("MULR", 0x98), Map.entry("RMO", 0xAC),
            Map.entry("SHIFTL", 0xA4), Map.entry("SHIFTR", 0xA8), Map.entry("SUBR", 0x94),
            Map.entry("SVC", 0xB0), Map.entry("TIXR", 0xB8));

    public static void main(String[] args) {
        run("known opcodes (examples)", OptabTest::knownOpcodes);
        run("all Format 3 entries: opcode, size 1, format 3", OptabTest::allF3);
        run("all Format 2 entries: opcode, size 1, format 2", OptabTest::allF2);
        run("no extra entries in the table", OptabTest::noExtras);
        run("base opcodes have the two low bits zeroed", OptabTest::lowBitsZero);
        run("unknown mnemonic is empty", OptabTest::unknown);
        run("'+' prefix is not accepted (tokenizer strips it)", OptabTest::plusRejected);
        run("null and blank are empty", OptabTest::nullAndBlank);
        run("lookup is case-insensitive", OptabTest::caseInsensitive);
        run("contains", OptabTest::containsWorks);
        run("entries is read-only", OptabTest::readOnly);
        run("InstructionData validates its fields", OptabTest::recordValidation);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---- tests ----

    static void knownOpcodes() {
        Optab t = new Optab();
        check(t.get("LDA").orElseThrow().opcode() == 0x00, "LDA");
        check(t.get("ADD").orElseThrow().opcode() == 0x18, "ADD");
        check(t.get("STA").orElseThrow().opcode() == 0x0C, "STA");
        check(t.get("JSUB").orElseThrow().opcode() == 0x48, "JSUB");
    }

    static void allF3() {
        Optab t = new Optab();
        F3_OPCODES.forEach((m, op) -> {
            InstructionData d = t.get(m).orElseThrow(() -> new AssertionError("missing " + m));
            check(d.opcode() == op, m + " opcode");
            check(d.tamanhoPalavras() == 1, m + " size");
            check(d.formato() == 3, m + " format");
        });
    }

    static void allF2() {
        Optab t = new Optab();
        F2_OPCODES.forEach((m, op) -> {
            InstructionData d = t.get(m).orElseThrow(() -> new AssertionError("missing " + m));
            check(d.opcode() == op, m + " opcode");
            check(d.tamanhoPalavras() == 1, m + " size");
            check(d.formato() == 2, m + " format");
        });
    }

    static void noExtras() {
        check(new Optab().size() == F3_OPCODES.size() + F2_OPCODES.size(),
                "size " + new Optab().size());
    }

    static void lowBitsZero() {
        new Optab().entries().forEach((m, d) ->
                check((d.opcode() & 0x03) == 0, m + " has n/i bits set"));
    }

    static void unknown() {
        check(new Optab().get("FOO").isEmpty(), "FOO");
        check(new Optab().get("FLOAT").isEmpty(), "floating point is excluded");
    }

    static void plusRejected() {
        check(new Optab().get("+LDA").isEmpty(), "+LDA must not match");
    }

    static void nullAndBlank() {
        Optab t = new Optab();
        check(t.get(null).isEmpty(), "null");
        check(t.get("").isEmpty(), "empty");
        check(t.get("  ").isEmpty(), "blank");
    }

    static void caseInsensitive() {
        check(new Optab().get("lda").orElseThrow().opcode() == 0x00, "lda");
    }

    static void containsWorks() {
        Optab t = new Optab();
        check(t.contains("RSUB"), "RSUB");
        check(!t.contains("NOPE"), "NOPE");
    }

    static void readOnly() {
        expect(UnsupportedOperationException.class,
                () -> new Optab().entries().put("X", new InstructionData(0, 1, 3)));
        expect(UnsupportedOperationException.class, () -> new Optab().entries().remove("LDA"));
    }

    static void recordValidation() {
        expect(IllegalArgumentException.class, () -> new InstructionData(-1, 1, 3));
        expect(IllegalArgumentException.class, () -> new InstructionData(0x100, 1, 3));
        expect(IllegalArgumentException.class, () -> new InstructionData(0, 0, 3));
        expect(IllegalArgumentException.class, () -> new InstructionData(0, 3, 3));
        expect(IllegalArgumentException.class, () -> new InstructionData(0, 1, 4));
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
