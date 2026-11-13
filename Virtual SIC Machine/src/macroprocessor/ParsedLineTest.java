package macroprocessor;

import java.util.Arrays;

/**
 * Testes sem dependências para ParsedLine (o projeto não tem JUnit no classpath).
 * Execução: java -cp build/classes macroprocessor.ParsedLineTest
 */
public class ParsedLineTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, Object expected, Object actual) {
        if (java.util.Objects.deepEquals(expected, actual)) {
            passed++;
        } else {
            failed++;
            System.out.println("FALHOU: " + name + "\n  esperado: " + show(expected) + "\n  obtido:   " + show(actual));
        }
    }

    private static String show(Object o) {
        return (o instanceof Object[] a) ? Arrays.toString(a) : String.valueOf(o);
    }

    public static void main(String[] args) {
        // label, opcode e operando separados por tabulação
        ParsedLine p = new ParsedLine("LOOP\tLDA\tALPHA");
        check("tab label", "LOOP", p.getLabel());
        check("tab opcode", "LDA", p.getOpcode());
        check("tab operands", new String[]{"ALPHA"}, p.getOperands());
        check("tab hasLabel", true, p.hasLabel());

        // sem label, espaços múltiplos, espaço após vírgula e comentário final
        p = new ParsedLine("   LDA     ALPHA, X     . carrega indexado");
        check("nolabel label", null, p.getLabel());
        check("nolabel hasLabel", false, p.hasLabel());
        check("nolabel opcode", "LDA", p.getOpcode());
        check("nolabel operands", new String[]{"ALPHA", "X"}, p.getOperands());
        check("nolabel comment", ". carrega indexado", p.getComment());

        // protótipo de macro
        p = new ParsedLine("ADD3     MACRO   &A,&B,&C");
        check("macro label", "ADD3", p.getLabel());
        check("macro opcode", "MACRO", p.getOpcode());
        check("macro operands", new String[]{"&A", "&B", "&C"}, p.getOperands());

        // comentários de linha inteira
        p = new ParsedLine(". 1. TESTE DE MODO IMEDIATO");
        check("comment line", true, p.isComment());
        check("comment line opcode", null, p.getOpcode());
        check("comment line reconstruct", ". 1. TESTE DE MODO IMEDIATO", p.reconstructLine());
        p = new ParsedLine("     . comentario indentado");
        check("indented comment", true, p.isComment());
        check("indented comment label", null, p.getLabel());

        // linhas vazias e null
        p = new ParsedLine("   \t ");
        check("blank", true, p.isBlank());
        check("blank opcode", null, p.getOpcode());
        check("blank operands", 0, p.getOperands().length);
        p = new ParsedLine(null);
        check("null blank", true, p.isBlank());
        check("null raw", "", p.getRawLine());
        check("null reconstruct", "", p.reconstructLine());

        // MEND com e sem indentação
        p = new ParsedLine("MEND");
        check("MEND col1 opcode", "MEND", p.getOpcode());
        check("MEND col1 label", null, p.getLabel());
        p = new ParsedLine("         MEND");
        check("MEND indented opcode", "MEND", p.getOpcode());

        // label sozinho
        p = new ParsedLine("LOOP");
        check("lone label", "LOOP", p.getLabel());
        check("lone label opcode", null, p.getOpcode());

        // operandos com aspas, vírgulas e pontos dentro
        p = new ParsedLine("         BYTE    C'A, B.'");
        check("quoted operand", new String[]{"C'A, B.'"}, p.getOperands());
        check("quoted comment", null, p.getComment());
        p = new ParsedLine("         WORD    1.5");
        check("decimal operand", new String[]{"1.5"}, p.getOperands());
        check("decimal comment", null, p.getComment());

        // operandos vazios mantêm a posição
        p = new ParsedLine("         CALL    A,,B");
        check("empty operand", new String[]{"A", "", "B"}, p.getOperands());

        // sem operandos, com comentário
        p = new ParsedLine("FIM      RSUB                . Volta ao SO");
        check("rsub label", "FIM", p.getLabel());
        check("rsub opcode", "RSUB", p.getOpcode());
        check("rsub operands", 0, p.getOperands().length);
        check("rsub comment", ". Volta ao SO", p.getComment());

        // prefixos são opacos para o processador de macros
        p = new ParsedLine("         +LDB    #FARDATA");
        check("plus opcode", "+LDB", p.getOpcode());
        check("hash operand", new String[]{"#FARDATA"}, p.getOperands());

        // reconstructLine
        check("reconstruct macro", "ADD3     MACRO    &A,&B",
                new ParsedLine("ADD3 MACRO &A, &B").reconstructLine());
        check("reconstruct nolabel", "         LDA      ALPHA,X  . c",
                new ParsedLine("   LDA ALPHA, X . c").reconstructLine());
        check("reconstruct no operand", "         RSUB",
                new ParsedLine("\tRSUB").reconstructLine());
        check("reconstruct long label", "LONGLABELX LDA      #1",
                new ParsedLine("LONGLABELX LDA #1").reconstructLine());

        // reconstruir e reparsear mantém os campos (idempotência)
        String[] samples = {
            "TESTE    START  0",
            "FIRST    LDA    #10          . Imediato",
            "         ADDR   S, A",
            "         BYTE   C'A, B.'",
            "MEND"
        };
        for (String s : samples) {
            ParsedLine a = new ParsedLine(s);
            ParsedLine b = new ParsedLine(a.reconstructLine());
            check("roundtrip label " + s, a.getLabel(), b.getLabel());
            check("roundtrip opcode " + s, a.getOpcode(), b.getOpcode());
            check("roundtrip operands " + s, a.getOperands(), b.getOperands());
            check("roundtrip comment " + s, a.getComment(), b.getComment());
        }

        // getOperands devolve cópia
        p = new ParsedLine("         LDA     X");
        p.getOperands()[0] = "Y";
        check("defensive copy", new String[]{"X"}, p.getOperands());

        System.out.println(passed + " passaram, " + failed + " falharam");
        if (failed > 0) {
            System.exit(1);
        }
    }
}