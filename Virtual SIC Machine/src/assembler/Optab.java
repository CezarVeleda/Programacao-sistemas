package assembler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Instruction table (OPTAB), read-only after construction.
 *
 * Backed by a HashMap (O(1) lookup). Keys are upper-case mnemonics WITHOUT the
 * '+' prefix: the tokenizer strips it and sets e=1 (Format 4, 2 words). Opcodes
 * are the base values with the two low bits zeroed; n/i are set at assembly time.
 *
 * Floating-point and privileged instructions are intentionally left out.
 */
public class Optab {

    private final Map<String, InstructionData> table = new HashMap<>();

    public Optab() {
        // Format 2 (register operands), 1 word
        f2("ADDR", 0x90);
        f2("CLEAR", 0xB4);
        f2("COMPR", 0xA0);
        f2("DIVR", 0x9C);
        f2("MULR", 0x98);
        f2("RMO", 0xAC);
        f2("SHIFTL", 0xA4);
        f2("SHIFTR", 0xA8);
        f2("SUBR", 0x94);
        f2("SVC", 0xB0);
        f2("TIXR", 0xB8);

        // Format 3 (memory operand), 1 word; F4 = '+' prefix, 2 words
        f3("ADD", 0x18);
        f3("AND", 0x40);
        f3("COMP", 0x28);
        f3("DIV", 0x24);
        f3("J", 0x3C);
        f3("JEQ", 0x30);
        f3("JGT", 0x34);
        f3("JLT", 0x38);
        f3("JSUB", 0x48);
        f3("LDA", 0x00);
        f3("LDB", 0x68);
        f3("LDCH", 0x50);
        f3("LDL", 0x08);
        f3("LDS", 0x6C);
        f3("LDT", 0x74);
        f3("LDX", 0x04);
        f3("MUL", 0x20);
        f3("OR", 0x44);
        f3("RSUB", 0x4C);
        f3("STA", 0x0C);
        f3("STB", 0x78);
        f3("STCH", 0x54);
        f3("STL", 0x14);
        f3("STS", 0x7C);
        f3("STT", 0x84);
        f3("STX", 0x10);
        f3("SUB", 0x1C);
        f3("TIX", 0x2C);
    }

    private void f2(String mnemonic, int opcode) {
        table.put(mnemonic, new InstructionData(opcode, 1, 2));
    }

    private void f3(String mnemonic, int opcode) {
        table.put(mnemonic, new InstructionData(opcode, 1, 3));
    }

    /**
     * Looks a mnemonic up (case-insensitive). A '+' prefix is NOT accepted here:
     * the tokenizer must remove it first. Empty if the mnemonic is unknown.
     */
    public Optional<InstructionData> get(String mnemonic) {
        if (mnemonic == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(table.get(mnemonic.toUpperCase(Locale.ROOT)));
    }

    public boolean contains(String mnemonic) {
        return get(mnemonic).isPresent();
    }

    public int size() {
        return table.size();
    }

    /** Read-only view of the table. */
    public Map<String, InstructionData> entries() {
        return Collections.unmodifiableMap(table);
    }
}
