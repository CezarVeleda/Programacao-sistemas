package virtualmachine;

import java.util.function.IntBinaryOperator;
import utils.DataUtils;
import virtualmachine.Condicional;
import virtualmachine.Maquina;
import virtualmachine.Registrador;
import virtualmachine.Snapshot;

public class MaquinaSic implements  Maquina {
    private Registrador[] registradores;
    byte[] memory;
    private boolean executionEnded = true;
    private static final int ENDERECO_SENTINELA = -1;

    public MaquinaSic() {
        memory = new byte[4095 * 3];
        
        registradores = new Registrador[10];
        for (int i = 0; i < registradores.length; ++i) {
            this.registradores[i] = new Registrador();
        }
        
        resetar();
    }
    
    @Override
    public void run() {
        while (!executionEnded) {
            step();
        }
    }
    
    @Override
    public void step() {
        if (executionEnded) return;
    
        int pcValue = registradores[8].getIntVal(); 
    
        // CHECAGEM DE FIM DE PROGRAMA E LIMITES
        if (pcValue == ENDERECO_SENTINELA) {
            executionEnded = true;
            return;
        }
        if (pcValue < 0 || pcValue >= 4095) {
            System.err.println("Erro: PC fora dos limites da memória (" + pcValue + ").");
            executionEnded = true;
            return;
        }
    
        int byteOffset = pcValue * 3; 
        byte[] instruction = DataUtils.readBytes(memory, byteOffset, 3);
        
        // Como o endereçamento é por palavra, o PC avança apenas 1 unidade por padrão
        registradores[8].setIntVal(pcValue + 1); 
    
        compute(instruction);
    }
    
    @Override
    public void compilar(String codigoTxt, boolean formatoPalavra) {
        resetar(); // Limpa a máquina antes de carregar o programa novo
        
        // Quebra o texto por espaços em branco, quebras de linha ou tabs
        String[] tokens = codigoTxt.trim().split("\\s+");
        int memIndex = 0; // Índice físico no vetor byte[] memory
        
        if (formatoPalavra) {
            // Exemplo esperado: "1820A0 4C0000" (blocos de 6 caracteres hex = 1 palavra)
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                
                // Garante que tenha 6 caracteres (preenche com zeros à esquerda se faltar)
                token = String.format("%6s", token).replace(' ', '0');
            
                // Quebra a string em 3 pedaços de 2 caracteres e salva como byte
                memory[memIndex] = (byte) Integer.parseInt(token.substring(0, 2), 16);
                memory[memIndex + 1] = (byte) Integer.parseInt(token.substring(2, 4), 16);
                memory[memIndex + 2] = (byte) Integer.parseInt(token.substring(4, 6), 16);
                memIndex += 3;
            }
        } else {
            // Exemplo esperado: "18 20 A0 4C 00 00" (blocos de 2 caracteres hex = 1 byte)
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                
                memory[memIndex] = (byte) Integer.parseInt(token, 16);
                memIndex++;
            }
        }
    }
    
    @Override
    public Snapshot getMachineStateSnapshot() {
        return new Snapshot(
            registradores[0].getIntVal(), // A
            registradores[1].getIntVal(), // X
            registradores[2].getIntVal(), // L
            registradores[3].getIntVal(), // B
            registradores[4].getIntVal(), // S
            registradores[5].getIntVal(), // T
            registradores[6].getIntVal(), // F
            registradores[8].getIntVal(), // PC
            registradores[9].getIntVal(), // SW
            getConditionCode(),           // Condicional atual extraído do SW
            executionEnded,               // Estado da simulação
            memory
        );
    }
    
    @Override
    public void resetar() {
        for (Registrador r : registradores) {
            r.setIntVal(0);
        }
        
        java.util.Arrays.fill(memory, (byte) 0);
        
        // PREPARAÇÃO PARA O FIM DO PROGRAMA
        registradores[2].setIntVal(ENDERECO_SENTINELA);
        executionEnded = false;
    }

    private void compute(byte[] ins) {
        switch(ins[0]){
            case (byte)0x18, (byte)0x19, (byte)0x1A, (byte)0x1B -> add(ins);
            case (byte)0x90 -> addr(ins);
            case (byte)0x40, (byte)0x41, (byte)0x42, (byte)0x43 -> and(ins);
            case (byte)0xB4 -> clear(ins);
            case (byte)0x28, (byte)0x29, (byte)0x2A, (byte)0x2B -> comp(ins);
            case (byte)0xA0 -> compr(ins);
            case (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27 -> div(ins);
            case (byte)0x9C -> divr(ins);
            case (byte)0x3C, (byte)0x3D, (byte)0x3E, (byte)0x3F -> j(ins);
            case (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33 -> jeq(ins);
            case (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37 -> jgt(ins);
            case (byte)0x38, (byte)0x39, (byte)0x3A, (byte)0x3B -> jlt(ins);
            case (byte)0x48, (byte)0x49, (byte)0x4A, (byte)0x4B -> jsub(ins);
            case (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03 -> lda(ins);
            case (byte)0x68, (byte)0x69, (byte)0x6A, (byte)0x6B -> ldb(ins);
            case (byte)0x50, (byte)0x51, (byte)0x52, (byte)0x53 -> ldch(ins);
            case (byte)0x08, (byte)0x09, (byte)0x0A, (byte)0x0B -> ldl(ins);
            case (byte)0x6C, (byte)0x6D, (byte)0x6E, (byte)0x6F -> lds(ins);
            case (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77 -> ldt(ins);
            case (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07 -> ldx(ins);
            case (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23 -> mul(ins);
            case (byte)0x98 -> mulr(ins);
            case (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47 -> or(ins);
            case (byte)0xAC-> rmo(ins);
            case (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x4F -> rsub(ins);
            case (byte)0xA4 -> shiftl(ins);
            case (byte)0xA8 -> shiftr(ins);
            case (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F -> sta(ins);
            case (byte)0x78, (byte)0x79, (byte)0x7A, (byte)0x7B -> stb(ins);
            case (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57 -> stch(ins);
            case (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17 -> stl(ins);
            case (byte)0x7C, (byte)0x7D, (byte)0x7E, (byte)0x7F -> sts(ins);
            case (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87 -> stt(ins);
            case (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13 -> stx(ins);
            case (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F -> sub(ins);
            case (byte)0x94 -> subr(ins);
            case (byte)0x2C, (byte)0x2D, (byte)0x2E, (byte)0x2F -> tix(ins);
            case (byte)0xB8 -> tixr(ins);
        }
    }
    
    // --- MÉTODOS AUXILIARES ---

    private boolean isImmediate(byte[] ins) {
        return (ins[0] & 0x01) != 0 && (ins[0] & 0x02) == 0;
    }

    private void writeMemoryWord(int targetAddress, int value) {
        // Agora delega completamente para o DataUtils gravar direto na array original
        DataUtils.writeInt24ToMemory(value, memory, targetAddress * 3);
    }
    
    private int readMemoryWord(int targetAddress) {
        // Utiliza versão Signed (numérica) para matemática padrão
        return DataUtils.bytes24ToIntSigned(memory, targetAddress * 3);
    }
    
    private void setConditionCode(Condicional cond) {
        int swValue = registradores[9].getIntVal();
        swValue = swValue & (~0x03); 
        swValue = swValue | cond.ordinal(); 
        registradores[9].setIntVal(swValue);
    }
    
    private Condicional getConditionCode() {
        int swValue = registradores[9].getIntVal();
        int ccValor = swValue & 0x03;
        return Condicional.values()[ccValor];
    }
    
    private int decodeFlags(byte[] ins) {
        boolean n = (ins[0] & 0x02) != 0;
        boolean i = (ins[0] & 0x01) != 0;
        
        boolean x, b, p, e;
        
        // Se for SIC Padrão (n=0 e i=0), NÃO há flags b, p, e. 
        // Os bits pertencem ao endereço de 15 bits, então forçamos falso e pulamos o switch.
        if (!n && !i) {
            x = (ins[1] & 0x80) != 0;
            b = false;
            p = false;
            e = false;
        } else {
            int flags = ((ins[0] & 0x03) << 4) | ((ins[1] >> 4) & 0x0F);
            
            boolean isValid = switch (flags) {
                case 0b000000, 0b001000 -> true;
                case 0b010000, 0b010001, 0b010010, 0b010100 -> true;
                case 0b100000, 0b100001, 0b100010, 0b100100 -> true;
                case 0b110000, 0b110001, 0b110010, 0b110100, 
                     0b111000, 0b111001, 0b111010, 0b111100 -> true;
                default -> false;
            };
            
            if (!isValid) {
                throw new IllegalArgumentException(
                    String.format("Combinação de flags inválida: %5s", Integer.toBinaryString(flags)).replace(' ', '0')
                );
            }
            
            x = (flags & 0b001000) != 0;
            b = (flags & 0b000100) != 0;
            p = (flags & 0b000010) != 0;
            e = (flags & 0b000001) != 0;
        }
    
        int disp = ((ins[1] & 0x0F) << 8) | (ins[2] & 0xFF);

        if (e) {
            byte format4Byte = memory[registradores[8].getIntVal() * 3];
            disp = (disp << 8) | (format4Byte & 0xFF);
            registradores[8].setIntVal(registradores[8].getIntVal() + 1);
        }

        int targetAddress = 0;
        
        if (!n && !i) {
            targetAddress = ((ins[1] & 0x7F) << 8) | (ins[2] & 0xFF);
        } else {
            // Verifica se é imediato puro (i=1, n=0) sem base e sem PC
            boolean isNegativeImmediate = (i && !n && !p && !b); 
            
            // Extensão de sinal para PC Relativo ou Imediato Negativo (somente se não for Formato 4)
            if (!e && (p || isNegativeImmediate) && (disp & 0x800) != 0) {
                disp |= 0xFFFFF000;
            }
            targetAddress = disp;
        }

        if (p) {
            targetAddress += registradores[8].getIntVal(); 
        } else if (b) {
            targetAddress += registradores[3].getIntVal(); 
        }
        
        if (x) {
            targetAddress += registradores[1].getIntVal(); 
        }
        
        // 7. Resolução de Ponteiro Indireto
        if (n && !i) {
            // REFATORADO: Usa conversão Crua (Unsigned) porque endereços não recebem extensão de sinal
            targetAddress = DataUtils.bytes24ToIntUnsigned(memory, targetAddress * 3);
        }
        
        return targetAddress;
    }
    
    // --- GRUPO 1: Loads (Cargas de Memória F3/4) ---
    private void lda(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[0].setIntVal(operand);
    }
    
    private void ldb(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[3].setIntVal(operand);
    }

    private void ldch(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand;
        
        if (isImmediate(ins)) {
            operand = targetAddress & 0xFF;
        } else {
            operand = memory[targetAddress * 3 + 2] & 0xFF;
        }
        
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal((valorA & 0xFFFF00) | operand);
    }

    private void ldl(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[2].setIntVal(operand);
    }

    private void lds(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[4].setIntVal(operand);
    }

    private void ldt(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[5].setIntVal(operand);
    }

    private void ldx(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        int operand = isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
        registradores[1].setIntVal(operand);
    }
    
    // --- GRUPO 2: Stores (Armazenamentos F3/4) ---
    private void sta(byte[] ins) {
        int targetAddress = decodeFlags(ins); 
        int valorA = registradores[0].getIntVal();
        writeMemoryWord(targetAddress, valorA); 
    }
    
    private void stb(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        writeMemoryWord(targetAddress, registradores[3].getIntVal());
    }

    private void stch(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        memory[targetAddress * 3 + 2] = (byte) (registradores[0].getIntVal() & 0xFF);
    }

    private void stl(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        writeMemoryWord(targetAddress, registradores[2].getIntVal());
    }

    private void sts(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        writeMemoryWord(targetAddress, registradores[4].getIntVal());
    }

    private void stt(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        writeMemoryWord(targetAddress, registradores[5].getIntVal());
    }

    private void stx(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        writeMemoryWord(targetAddress, registradores[1].getIntVal());
    }
    
    // --- GRUPO 3: Aritmética, Lógica e Testes de Memória ---
    private int getOperandValue(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        return isImmediate(ins) ? targetAddress : readMemoryWord(targetAddress);
    }

    private void add(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal(valorA + operand);
    }

    private void sub(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal(valorA - operand);
    }

    private void mul(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal(valorA * operand);
    }

    private void div(byte[] ins) {
        int operand = getOperandValue(ins);
        if (operand != 0) {
            int valorA = registradores[0].getIntVal();
            registradores[0].setIntVal(valorA / operand);
        } else {
            System.err.println("Erro: Divisão por zero na instrução DIV.");
        }
    }

    private void and(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal(valorA & operand);
    }

    private void or(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();
        registradores[0].setIntVal(valorA | operand);
    }

    private void comp(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorA = registradores[0].getIntVal();

        if (valorA == operand) {
            setConditionCode(Condicional.Igual);
        } else if (valorA > operand) {
            setConditionCode(Condicional.Maior);
        } else {
            setConditionCode(Condicional.Menor);
        }
    }

    private void tix(byte[] ins) {
        int operand = getOperandValue(ins);
        int valorX = registradores[1].getIntVal() + 1;
        registradores[1].setIntVal(valorX);

        if (valorX == operand) {
            setConditionCode(Condicional.Igual);
        } else if (valorX > operand) {
            setConditionCode(Condicional.Maior);
        } else {
            setConditionCode(Condicional.Menor);
        }
    }
    
    // --- GRUPO 4: Operações Puras de Registrador (F2) ---
    private Registrador reg1(byte[] ins) {
        return registradores[(ins[1] >> 4) & 0x0F];
    }

    private Registrador reg2(byte[] ins) {
        return registradores[ins[1] & 0x0F];
    }

    private void operaRegistradores(byte[] ins, IntBinaryOperator op) {
        reg2(ins).setIntVal(op.applyAsInt(reg2(ins).getIntVal(), reg1(ins).getIntVal()));
    }

    private void comparaEAtualizaCC(int a, int b) {
        setConditionCode(Condicional.values()[Integer.signum(Integer.compare(a, b)) + 1]);
    }

    private void addr(byte[] ins) {
        operaRegistradores(ins, (v2, v1) -> v2 + v1);
    }

    private void subr(byte[] ins) {
        operaRegistradores(ins, (v2, v1) -> v2 - v1);
    }

    private void mulr(byte[] ins) {
        operaRegistradores(ins, (v2, v1) -> v2 * v1);
    }

    private void divr(byte[] ins) {
        if (reg1(ins).getIntVal() == 0) {
            System.err.println("Erro: Divisão por zero na instrução DIVR.");
            return;
        }
        operaRegistradores(ins, (v2, v1) -> v2 / v1);
    }

    private void compr(byte[] ins) {
        comparaEAtualizaCC(reg1(ins).getIntVal(), reg2(ins).getIntVal());
    }

    private void tixr(byte[] ins) {
        registradores[1].setIntVal(registradores[1].getIntVal() + 1);
        comparaEAtualizaCC(registradores[1].getIntVal(), reg1(ins).getIntVal());
    }

    private void rmo(byte[] ins) {
        reg2(ins).setIntVal(reg1(ins).getIntVal());
    }

    private void clear(byte[] ins) {
        reg1(ins).setIntVal(0);
    }

    private void shiftl(byte[] ins) {
        int n = (ins[1] & 0x0F) + 1; 
        int val = reg1(ins).getIntVal();
        reg1(ins).setIntVal((val << n) & 0xFFFFFF); // Shift lógico com máscara de 24 bits
    }

    private void shiftr(byte[] ins) {
        int n = (ins[1] & 0x0F) + 1;
        reg1(ins).setIntVal(reg1(ins).getIntVal() >> n);
    }
    
    // --- GRUPO 5: Controle de Fluxo e Subrotinas (F3/4) ---
    private void jeq(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        if (getConditionCode() == Condicional.Igual) {
            registradores[8].setIntVal(targetAddress);
        }
    }
    
    private void j(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        registradores[8].setIntVal(targetAddress);
    }

    private void jgt(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        if (getConditionCode() == Condicional.Maior) {
            registradores[8].setIntVal(targetAddress);
        }
    }

    private void jlt(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        if (getConditionCode() == Condicional.Menor) {
            registradores[8].setIntVal(targetAddress);
        }
    }

    private void jsub(byte[] ins) {
        int targetAddress = decodeFlags(ins);
        registradores[2].setIntVal(registradores[8].getIntVal());
        registradores[8].setIntVal(targetAddress);
    }

    private void rsub(byte[] ins) {
        registradores[8].setIntVal(registradores[2].getIntVal());
    }
}