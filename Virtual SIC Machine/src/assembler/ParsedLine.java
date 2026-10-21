package assembler;

/**
 * Represents a parsed and cleaned assembly line with its corresponding SIC/XE flags.
 * This immutable record is passed to the First and Second Passes of the Assembler.
 * 
 * @param label       the cleaned label (or null if none exists)
 * @param operation   the cleaned operation mnemonic or directive
 * @param operand     the cleaned operand string
 * @param isDirective true if the operation is an assembler directive
 * @param n           Indirect addressing flag
 * @param i           Immediate addressing flag
 * @param x           Indexed addressing flag
 * @param e           Extended format (Format 4) flag
 */
public record ParsedLine(
    String label, 
    String operation, 
    String operand, 
    boolean isDirective, 
    boolean n, 
    boolean i, 
    boolean x, 
    boolean e
) {}