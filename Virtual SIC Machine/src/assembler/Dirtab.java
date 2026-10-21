package assembler;

import java.util.HashMap;
import java.util.Map;

public class Dirtab {

    @FunctionalInterface
    public interface DirectiveHandler {
        void process(EstadoMontador estado, String rotulo, String operando);
    }

    // Armazena o estado global do montador alterado pelas diretivas
    public static class EstadoMontador {
        public int locctr;
        public Integer baseRegisterValue;
        public String programName;
        public int startAddress;
        public boolean isFinished;

        public EstadoMontador() {
            this.locctr = 0;
            this.baseRegisterValue = null;
            this.programName = "";
            this.startAddress = 0;
            this.isFinished = false;
        }
    }

    private final Map<String, DirectiveHandler> tabela = new HashMap<>();

    public Dirtab() {
        inicializarTabela();
    }

    // Mapeia diretivas para referencias de metodos explicitos (sem lambdas)
    private void inicializarTabela() {
        tabela.put("START",  this::handle_START);
        tabela.put("END",    this::handle_END);
        tabela.put("WORD",   this::handle_WORD);
        tabela.put("RESW",   this::handle_RESW);
        tabela.put("RESB",   this::handle_RESB);
        tabela.put("BYTE",   this::handle_BYTE);
        tabela.put("BASE",   this::handle_BASE);
        tabela.put("NOBASE", this::handle_NOBASE);
    }

    // Handlers das diretivas
    public void handle_START(EstadoMontador estado, String rotulo, String operando) {
        estado.programName = (rotulo != null && !rotulo.isBlank()) ? rotulo : "NONAME";
        try {
            estado.startAddress = Integer.parseInt(operando.trim(), 16);
        } catch (NumberFormatException e) {
            estado.startAddress = 0;
        }
        estado.locctr = estado.startAddress;
    }

    public void handle_END(EstadoMontador estado, String rotulo, String operando) {
        estado.isFinished = true;
    }

    public void handle_WORD(EstadoMontador estado, String rotulo, String operando) {
        estado.locctr += 1; // Aloca 1 palavra (24 bits)
    }

    public void handle_RESW(EstadoMontador estado, String rotulo, String operando) {
        estado.locctr += Integer.parseInt(operando.trim()); // Aloca N palavras
    }

    public void handle_RESB(EstadoMontador estado, String rotulo, String operando) {
        int bytes = Integer.parseInt(operando.trim());
        estado.locctr += (int) Math.ceil(bytes / 3.0); // Converte bytes em palavras (1 palavra = 3 bytes)
    }

    public void handle_BYTE(EstadoMontador estado, String rotulo, String operando) {
        String op = operando.trim();
        int totalBytes = 1;

        if (op.startsWith("C'") && op.endsWith("'")) {
            totalBytes = op.substring(2, op.length() - 1).length();
        } else if (op.startsWith("X'") && op.endsWith("'")) {
            String hex = op.substring(2, op.length() - 1);
            totalBytes = (hex.length() + 1) / 2;
        }

        estado.locctr += (int) Math.ceil(totalBytes / 3.0);
    }

    public void handle_BASE(EstadoMontador estado, String rotulo, String operando) {
        try {
            estado.baseRegisterValue = Integer.valueOf(operando.trim());
        } catch (NumberFormatException e) {
            // Rotulos sao resolvidos na 2ª passagem
        }
    }

    public void handle_NOBASE(EstadoMontador estado, String rotulo, String operando) {
        estado.baseRegisterValue = null;
    }

    // Processamento e busca
    public boolean isDiretiva(String mnemonico) {
        return mnemonico != null && tabela.containsKey(mnemonico.toUpperCase().trim());
    }

    public boolean executar(String mnemonico, EstadoMontador estado, String rotulo, String operando) {
        if (mnemonico == null) return false;

        DirectiveHandler handler = tabela.get(mnemonico.toUpperCase().trim());
        if (handler != null) {
            handler.process(estado, rotulo, operando);
            return true;
        }
        return false;
    }
}