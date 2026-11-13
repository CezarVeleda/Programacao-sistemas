package macroprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador léxico e DTO de uma linha de código-fonte para o Processador de Macros.
 *
 * <p>O processador de macros trata o texto como opaco: aqui não há validação de
 * mnemônicos nem de diretivas do SIC/XE. A divisão em campos usa apenas:
 * <ul>
 *   <li>a posição (primeira coluna ocupada => a primeira palavra é um label);</li>
 *   <li>espaços e tabulações como separadores de campos;</li>
 *   <li>a vírgula como separador de operandos;</li>
 *   <li>o ponto ('.') como início de comentário (linha inteira ou final de linha).</li>
 * </ul>
 *
 * <p>Regras de comentário: uma linha cuja primeira palavra começa com '.' é um comentário
 * de linha inteira. Um '.' no meio da linha só inicia comentário se vier depois de espaço ou
 * tabulação e fora de aspas simples, o que preserva operandos como C'A.B' ou 1.5.
 *
 * <p>Caso especial: uma única palavra na coluna 1, sem mais nada, normalmente seria um label
 * sozinho. Se essa palavra for MEND, ela é tratada como opcode, para que um MEND sem
 * indentação não escape da contagem de nível de {@code MacroProcessor.define()}.
 *
 * <p>A classe é imutável.
 */
public class ParsedLine {

    private static final int FIELD_WIDTH = 8;

    private final String rawLine;
    private final String label;      // null se não houver
    private final String opcode;     // null em linha vazia, comentário ou linha só com label
    private final String[] operands; // nunca null; vazio se não houver operandos
    private final String comment;    // null se não houver; inclui o '.' inicial
    private final boolean commentLine;
    private final boolean blank;

    /**
     * Executa o parsing espacial da linha.
     *
     * @param line linha bruta (pode ser null, tratada como linha vazia)
     */
    public ParsedLine(String line) {
        this.rawLine = (line == null) ? "" : line;

        if (rawLine.isBlank()) {
            this.label = null;
            this.opcode = null;
            this.operands = new String[0];
            this.comment = null;
            this.commentLine = false;
            this.blank = true;
            return;
        }

        if (rawLine.stripLeading().startsWith(".")) {
            this.label = null;
            this.opcode = null;
            this.operands = new String[0];
            this.comment = rawLine.strip();
            this.commentLine = true;
            this.blank = false;
            return;
        }

        this.commentLine = false;
        this.blank = false;

        int commentStart = findCommentStart(rawLine);
        String code = (commentStart >= 0) ? rawLine.substring(0, commentStart) : rawLine;
        this.comment = (commentStart >= 0) ? rawLine.substring(commentStart).strip() : null;

        // O label é decidido ANTES de qualquer trim: depende da coluna 1.
        boolean startsWithLabel = !Character.isWhitespace(code.charAt(0));

        int pos = 0;
        int len = code.length();

        String parsedLabel = null;
        if (startsWithLabel) {
            int end = nextWhitespace(code, pos);
            parsedLabel = code.substring(pos, end);
            pos = end;
        }

        pos = skipWhitespace(code, pos);
        String parsedOpcode = null;
        if (pos < len) {
            int end = nextWhitespace(code, pos);
            parsedOpcode = code.substring(pos, end);
            pos = end;
        }

        // Palavra única na coluna 1 igual a MEND: é o opcode, não um label.
        if (parsedOpcode == null && parsedLabel != null && "MEND".equalsIgnoreCase(parsedLabel)) {
            parsedOpcode = parsedLabel;
            parsedLabel = null;
        }

        String rest = (pos < len) ? code.substring(pos).strip() : "";

        this.label = parsedLabel;
        this.opcode = parsedOpcode;
        this.operands = splitOperands(rest);
    }

    /** Linha original, intacta. */
    public String getRawLine() {
        return rawLine;
    }

    /** Label da linha, ou null. */
    public String getLabel() {
        return label;
    }

    /** Operação ou diretiva (MACRO, MEND, nome de macro, ...), ou null. */
    public String getOpcode() {
        return opcode;
    }

    /** Operandos separados por vírgula, já sem espaços nas pontas. Nunca null. */
    public String[] getOperands() {
        return operands.clone();
    }

    /** Comentário (com o '.' inicial), ou null. */
    public String getComment() {
        return comment;
    }

    public boolean hasLabel() {
        return label != null;
    }

    /** true se a linha inteira for um comentário. */
    public boolean isComment() {
        return commentLine;
    }

    /** true se a linha for vazia ou só tiver espaços. */
    public boolean isBlank() {
        return blank;
    }

    /**
     * Remonta a linha com colunas padronizadas (label, opcode e operandos alinhados em
     * campos de 8 caracteres). Linhas vazias e comentários de linha inteira são devolvidos
     * como vieram. Operandos são reunidos com "," sem espaços, e o comentário final é mantido.
     * A linha reconstruída sempre começa com espaço quando não há label, para que o montador
     * continue enxergando a primeira coluna vazia.
     */
    public String reconstructLine() {
        if (blank || commentLine) {
            return rawLine;
        }

        StringBuilder sb = new StringBuilder();
        appendField(sb, label == null ? "" : label);

        if (opcode != null) {
            appendField(sb, opcode);
        }

        if (operands.length > 0) {
            sb.append(String.join(",", operands));
        }

        String result = sb.toString().stripTrailing();
        if (comment != null) {
            result = result + "  " + comment;
        }
        return result;
    }

    @Override
    public String toString() {
        return "ParsedLine[label=" + label + ", opcode=" + opcode
                + ", operands=" + String.join("|", operands)
                + ", comment=" + comment + "]";
    }

    // ---------------------------------------------------------------- helpers

    private static void appendField(StringBuilder sb, String text) {
        sb.append(text);
        int pad = Math.max(1, FIELD_WIDTH + 1 - text.length());
        sb.append(" ".repeat(pad));
    }

    private static int nextWhitespace(String s, int from) {
        int i = from;
        while (i < s.length() && !Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int skipWhitespace(String s, int from) {
        int i = from;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Procura o início de um comentário no fim da linha: um '.' precedido de espaço ou
     * tabulação e fora de aspas simples. Retorna -1 se não houver.
     */
    private static int findCommentStart(String s) {
        boolean inQuote = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote;
            } else if (c == '.' && !inQuote && i > 0 && Character.isWhitespace(s.charAt(i - 1))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Separa os operandos por vírgula, ignorando vírgulas dentro de aspas simples.
     * Operandos vazios são mantidos (ex.: "A,,B" => [A, "", B]) porque a posição de cada
     * argumento importa para a notação posicional ?1, ?2, ...
     */
    private static String[] splitOperands(String text) {
        if (text.isEmpty()) {
            return new String[0];
        }

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote;
                current.append(c);
            } else if (c == ',' && !inQuote) {
                parts.add(current.toString().strip());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString().strip());

        return parts.toArray(new String[0]);
    }
}