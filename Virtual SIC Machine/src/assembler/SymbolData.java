package assembler;

/**
 * Value stored in the SYMTAB for each label.
 *
 * @param endereco word address of the symbol (the machine is word-addressed)
 * @param tipo     whether the address is absolute or relative
 */
public record SymbolData(int endereco, SymbolType tipo) {

    public SymbolData {
        if (tipo == null) {
            throw new IllegalArgumentException("Symbol type must not be null");
        }
    }
}
