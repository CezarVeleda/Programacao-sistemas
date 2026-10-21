package assembler;

import java.util.Set;

/**
 * Lexical Analyzer for the SIC/XE Assembler.
 * Cleans tokens and identifies addressing flags (n, i, x, e) based on prefixes and suffixes.
 */
public class LexicalAnalyzer {

    // Set of known directives to prevent flag parsing on their operands
    private static final Set<String> DIRECTIVES = Set.of(
        "START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE", 
        "EQU", "ORG", "LTORG", "USE", "CSECT", "EXTDEF", "EXTREF"
    );

    /**
     * Parses a raw line of assembly code into a clean ParsedLine record.
     * 
     * @param line the raw source code line
     * @return ParsedLine containing cleaned tokens and flags, or null if the line is empty/comment
     */
    public static ParsedLine parseLine(String line) {
        if (line == null || line.trim().isEmpty() || line.trim().startsWith(".")) {
            return null;
        }

        // 1. Verifica se a linha tem label ANTES de remover os espaços
        boolean hasLabel = !line.startsWith(" ") && !line.startsWith("\t");

        // 2. Remove os espaços das pontas (trim) para evitar posições vazias no split
        String[] parts = line.trim().split("\\s+");
        int index = 0;

        String label = null;
        String operation = null;
        String operand = null;

        if (hasLabel && index < parts.length) {
            label = parts[index++];
        }

        if (index < parts.length) {
            operation = parts[index++];
        }

        if (index < parts.length) {
            StringBuilder opBuilder = new StringBuilder();
            for (int k = index; k < parts.length; k++) {
                opBuilder.append(parts[k]).append(" ");
            }
            operand = opBuilder.toString().trim();
        }

        boolean n = true;
        boolean i = true;
        boolean x = false;
        boolean e = false;
        boolean isDirective = false;

        if (operation != null) {
            String cleanOp = operation.replace("+", "");
            isDirective = DIRECTIVES.contains(cleanOp.toUpperCase());

            if (!isDirective) {
                if (operation.startsWith("+")) {
                    e = true;
                    operation = operation.substring(1);
                }

                if (operand != null && !operand.isEmpty()) {
                    if (operand.startsWith("#")) {
                        i = true;
                        n = false;
                        operand = operand.substring(1);
                    } else if (operand.startsWith("@")) {
                        i = false;
                        n = true;
                        operand = operand.substring(1);
                    }

                    if (operand.toUpperCase().endsWith(",X") || operand.toUpperCase().endsWith(", X")) {
                        x = true;
                        int commaIndex = operand.lastIndexOf(',');
                        operand = operand.substring(0, commaIndex).trim();
                    }
                }
            }
        }

        return new ParsedLine(label, operation, operand, isDirective, n, i, x, e);
    }
}