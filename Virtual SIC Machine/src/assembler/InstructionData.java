package assembler;

/**
 * Value stored in the OPTAB for each mnemonic.
 *
 * @param opcode          base opcode with the two low bits (n/i) zeroed, e.g. LDA = 0x00, ADD = 0x18
 * @param tamanhoPalavras size in words: 1 for F1/F2/F3, 2 for F4 (the F4 variant comes from the '+' prefix)
 * @param formato         instruction format: 1, 2 or 3 (F4 is F3 with the 'e' flag set)
 */
public record InstructionData(int opcode, int tamanhoPalavras, int formato) {

    public InstructionData {
        if (opcode < 0 || opcode > 0xFF) {
            throw new IllegalArgumentException("Opcode must fit in 8 bits: " + opcode);
        }
        if (formato < 1 || formato > 3) {
            throw new IllegalArgumentException("Format must be 1, 2 or 3: " + formato);
        }
        if (tamanhoPalavras < 1 || tamanhoPalavras > 2) {
            throw new IllegalArgumentException("Size must be 1 or 2 words: " + tamanhoPalavras);
        }
    }
}
