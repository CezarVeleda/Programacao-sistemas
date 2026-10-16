package assembler;

/** Thrown when a label is defined more than once ("Símbolo Duplicado"). */
public class DuplicateSymbolException extends RuntimeException {

    private final String symbol;

    public DuplicateSymbolException(String symbol) {
        super("Símbolo Duplicado: " + symbol);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
