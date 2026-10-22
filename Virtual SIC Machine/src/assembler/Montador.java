package assembler;

import java.util.Optional;

public class Montador {

    private final Optab optab;
    private final Dirtab dirtab;
    private final AddressingResolver resolver;
    private final Symtab symtab;
    private final Dirtab.EstadoMontador estado;

    private String codigoObjeto = "";

    public Montador() {
        this.optab = new Optab();
        this.dirtab = new Dirtab();
        this.resolver = new AddressingResolver();
        this.symtab = new Symtab();
        this.estado = new Dirtab.EstadoMontador();
    }

    public void montar(String linhasFonte) {
        reset();
        symtab.reset();
        primeiraPassagem(linhasFonte);
        segundaPassagem(linhasFonte);
    }

    private void reset() {
        codigoObjeto = "";
    }

    private void primeiraPassagem(String linhasFonte) {
        estado.locctr = 0;
        estado.baseRegisterValue = null;

        String[] linhas = linhasFonte.trim().split("\n");
        for (String linha : linhas) {
            ParsedLine token = LexicalAnalyzer.parseLine(linha);
            if (token == null) {
                continue;
            }

            if (token.label() != null && !token.label().isEmpty()) {
                symtab.define(token.label(), estado.locctr, SymbolType.RELATIVE);
            }

            if (token.isDirective()) {
                dirtab.executar(token.operation(), estado, token.label(), token.operand());
            } else {
                Optional<InstructionData> instOpt = optab.get(token.operation());
                if (instOpt.isPresent()) {
                    InstructionData inst = instOpt.get();
                    int avanco = inst.tamanhoPalavras() + (token.e() ? 1 : 0);
                    estado.locctr += avanco;
                } else {
                    throw new RuntimeException("Erro: Instrução não reconhecida - " + token.operation());
                }
            }
        }
    }

    private void segundaPassagem(String linhasFonte) {
        int startAddress = estado.startAddress;
        estado.locctr = startAddress;
        estado.baseRegisterValue = null;
        estado.isFinished = false;

        String[] linhas = linhasFonte.trim().split("\n");
        for (String linha : linhas) {
            ParsedLine token = LexicalAnalyzer.parseLine(linha);
            if (token == null) {
                continue;
            }

            if (token.isDirective()) {
                dirtab.executar(token.operation(), estado, token.label(), token.operand());
                continue;
            }

            Optional<InstructionData> instOpt = optab.get(token.operation());
            if (instOpt.isEmpty()) {
                continue;
            }

            InstructionData inst = instOpt.get();
            int targetAddress = 0;
            AddressingResult result = null;

            if (inst.formato() != 2) {
                if (token.operand() == null || token.operand().isEmpty()) {
                    // Correção do ERRO 2: Instruções sem operando recebem deslocamento 0 puro
                    result = new AddressingResult(0, false, false, token.e());
                } else {
                    boolean isNumericImmediate = false;

                    if (token.operand() != null && !token.operand().isEmpty()) {
                        if (token.i() && !token.n() && ehNumero(token.operand())) {
                            targetAddress = Integer.parseInt(token.operand());
                            isNumericImmediate = true;
                        } else if (ehNumero(token.operand())) {
                            targetAddress = Integer.parseInt(token.operand());
                        } else {
                            Optional<SymbolData> symOpt = symtab.lookup(token.operand());
                            if (symOpt.isPresent()) {
                                targetAddress = symOpt.get().endereco();
                            } else {
                                throw new RuntimeException("Símbolo indefinido: " + token.operand());
                            }
                        }
                    }

                    if (isNumericImmediate) {
                        // Força p=0 e b=0 para operandos imediatos numéricos puros para gerar objeto limpo
                        result = new AddressingResult(targetAddress, false, false, token.e());
                    } else {
                        result = resolver.resolveAddressing(
                                targetAddress,
                                estado.locctr,
                                estado.baseRegisterValue,
                                token.e()
                        );
                    }
                }
            }

            codigoObjeto += gerarCodigoObjeto(inst, token, result) + "\n";
            
            int avanco = inst.tamanhoPalavras() + (token.e() ? 1 : 0);
            estado.locctr += avanco;
        }
    }

    private String gerarCodigoObjeto(InstructionData inst, ParsedLine token, AddressingResult result) {
        if (inst.formato() == 2) {
            int r1 = 0;
            int r2 = 0;
            if (token.operand() != null && !token.operand().isEmpty()) {
                String[] regs = token.operand().split(",");
                r1 = getRegisterNumber(regs[0]);
                if (regs.length > 1) {
                    r2 = getRegisterNumber(regs[1]);
                }
            }
            // Adiciona o preenchimento "00" no final para ocupar a palavra inteira (6 hexadecimais)
            return String.format("%02X%X%X00", inst.opcode(), r1, r2);
        }

        int opcodeBase = inst.opcode() & 0xFC;
        int nFlag = token.n() ? 2 : 0;
        int iFlag = token.i() ? 1 : 0;
        int primeiroByte = opcodeBase | nFlag | iFlag;

        int xFlag = token.x() ? 0x80 : 0;
        int bFlag = result.isB() ? 0x40 : 0;
        int pFlag = result.isP() ? 0x20 : 0;
        int eFlag = result.isE() ? 0x10 : 0;

        int disp = result.getDisp();

        if (token.e()) {
            int segundoByte = xFlag | bFlag | pFlag | eFlag | ((disp >> 16) & 0x0F);
            int terceiroByte = (disp >> 8) & 0xFF;
            int quartoByte = disp & 0xFF;
            return String.format("%02X%02X%02X %02X0000", primeiroByte, segundoByte, terceiroByte, quartoByte);
        } else {
            int segundoByte = xFlag | bFlag | pFlag | eFlag | ((disp >> 8) & 0x0F);
            int terceiroByte = disp & 0xFF;
            return String.format("%02X%02X%02X", primeiroByte, segundoByte, terceiroByte);
        }
    }

    private int getRegisterNumber(String reg) {
        if (reg == null || reg.trim().isEmpty()) {
            return 0;
        }
        return switch (reg.trim().toUpperCase()) {
            case "A" ->
                0;
            case "X" ->
                1;
            case "L" ->
                2;
            case "B" ->
                3;
            case "S" ->
                4;
            case "T" ->
                5;
            case "F" ->
                6;
            case "PC" ->
                8;
            case "SW" ->
                9;
            default ->
                throw new RuntimeException("Registrador inválido: " + reg.trim());
        };
    }

    private boolean ehNumero(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getCodigoObjeto() {
        return codigoObjeto;
    }
    
    
}
