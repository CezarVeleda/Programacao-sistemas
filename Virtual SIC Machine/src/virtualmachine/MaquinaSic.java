package VirtualMachine;

import utils.DataUtils;
import virtualmachine.OpMap;
import virtualmachine.Maquina;
//import virtualmachine.OpCode;
import virtualmachine.Registrador;

public class MaquinaSic implements  Maquina {
    private Registrador[] registradores;
    byte[] memory;
    //OpMap opMap;

    public MaquinaSic() {
        memory = new byte[4095];
        
        //opMap = new OpMap();
        
        registradores = new Registrador[10];
        for (int i = 0; i < registradores.length; ++i) {
            this.registradores[i] = new Registrador();
        }
        
        //test();
    }
    
    
    /*
    private void test() {
        int adress = 0;
        byte[] val = new byte[3];
        val = DataUtils.intToBytes24(7000);
        for (int i = 0; i < 3; ++i) {
            memory[adress + i] = val[i];
        }
        
        compute(OpCode.ADD.getCode(), registradores[0], memory, adress);
        compute(OpCode.ADD.getCode(), registradores[0], memory, adress);
        System.out.println(DataUtils.bytes24ToInt((registradores[0].getVal())));
    }
    */
    
    @Override
    public void run(){
        
    }
    
    @Override
    public void step(){
        int currentInstruction = registradores[8].getIntVal();
        byte[] instruction = new byte[3];
        
        instruction[0] = memory[currentInstruction];
        instruction[1] = memory[currentInstruction + 1];
        instruction[2] = memory[currentInstruction + 2];
        
        compute(instruction);
    }

    private void compute(byte[] ins) {
        registradores[8].setIntVal(registradores[8].getIntVal() + 3);
        
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
            case (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67 -> ldt(ins);
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
    
    private int decodeFlags(byte[] ins) {
        // 1. Isola os 6 bits (n, i, x, b, p, e) num único valor entre 0 e 63
        // O '& 0x03' pega os 2 últimos bits do opcode. O '& 0x0F' evita vazamento de sinal no byte.
        int flags = ((ins[0] & 0x03) << 4) | ((ins[1] >> 4) & 0x0F);
        
        // 2. Validação estrita baseada na tabela de modos de endereçamento
        boolean isValid = switch (flags) {
            // Instruções do SIC Padrão (n=0, i=0)
            case 0b000000, 0b001000 -> true;
                
            // Modo Imediato (n=0, i=1)
            case 0b010000, 0b010001, 0b010010, 0b010100 -> true;
                
            // Modo Indireto (n=1, i=0)
            case 0b100000, 0b100001, 0b100010, 0b100100 -> true;
                
            // Modo Direto/Simples SIC/XE (n=1, i=1)
            case 0b110000, 0b110001, 0b110010, 0b110100, 
                 0b111000, 0b111001, 0b111010, 0b111100 -> true;
             
            // Qualquer outra combinação é um erro arquitetural
            default -> false;
        };
        
        if (!isValid) {
            throw new IllegalArgumentException(
                String.format("Combinação de flags inválida: %5s", Integer.toBinaryString(flags)).replace(' ', '0')
            );
        }
        
        // 3. Extração das flags individuais para facilitar a lógica
        boolean n = (flags & 0b100000) != 0;
        boolean i = (flags & 0b010000) != 0;
        boolean x = (flags & 0b001000) != 0;
        boolean b = (flags & 0b000100) != 0;
        boolean p = (flags & 0b000010) != 0;
        boolean e = (flags & 0b000001) != 0;
        
        // 3.5. Montagem inicial do disp
        // Parênteses garantem a máscara correta antes de deslocar 8 bits
        int disp = ((ins[1] & 0x0F) << 8) | (ins[2] & 0xFF);

        // Se for formato 4 (e=1)
        if (e) {
            // Lê o 4º byte usando o PC que já aponta para ele
            byte format4Byte = memory[registradores[8].getIntVal()];
    
            // Desloca os 12 bits atuais para a esquerda em 8 posições e funde o último byte
            disp = (disp << 8) | (format4Byte & 0xFF);
    
            // INCREMENTO OBRIGATÓRIO: A instrução consumiu 4 bytes, o PC precisa refletir isso
            registradores[8].setIntVal(registradores[8].getIntVal() + 3);
        }

        int targetAddress = 0;
        
        // 4. Extração do Endereço / Deslocamento inicial
        if (!n && !i) {
            // SIC Padrão: Usa os últimos 15 bits como endereço absoluto
            targetAddress = ((ins[1] & 0x7F) << 8) | (ins[2] & 0xFF);
        } else {
            // Formato 3 SIC/XE: Deslocamento (disp) de 12 bits
            //int disp = ((ins[1] & 0x0F) << 8) | (ins[2] & 0xFF);
            
            // Extensão de sinal para endereçamento relativo ao PC (complemento de 2)
            if (p && (disp & 0x800) != 0) {
                disp |= 0xFFFFF000;
            }
            targetAddress = disp;
        }

        // 5. Adição de Base ou PC
        if (p) {
            targetAddress += registradores[8].getIntVal(); // PC
        } else if (b) {
            targetAddress += registradores[3].getIntVal(); // Base
        }
        
        // 6. Endereçamento Indexado
        if (x) {
            targetAddress += registradores[1].getIntVal(); // X
        }
        
        // 7. Resolução de Ponteiro Indireto
        if (n && !i) {
            // Lê 3 bytes (uma palavra) do endereço computado na memória e atualiza o destino
            byte[] addrBytes = {
                memory[targetAddress], 
                memory[targetAddress + 1], 
                memory[targetAddress + 2]
            };
            
            targetAddress = DataUtils.bytes24ToInt(addrBytes);
        }
        
        return targetAddress;
    }
    
    //Intruções entre registradores
    //Formato 2
    private void addr(byte[] ins){}
    private void subr(byte[] ins){}
    private void mulr(byte[] ins){}
    private void divr(byte[] ins){}
    private void shiftl(byte[] ins){}
    private void shiftr(byte[] ins){}
    private void rmo(byte[] ins){}
    private void clear(byte[] ins){}
    
    //Instruções de carga da memória para registradores
    //Formato 3/4
    private void lda(byte[] ins){}
    private void ldb(byte[] ins){}
    private void ldch(byte[] ins){}
    private void ldl(byte[] ins){}
    private void lds(byte[] ins){}
    private void ldt(byte[] ins){}
    private void ldx(byte[] ins){}
    
    //Instruções de armazenamento de registradores para a memória
    //Formato 3/4
    private void sta(byte[] ins){}
    private void stb(byte[] ins){}
    private void stch(byte[] ins){}
    private void stl(byte[] ins){}
    private void sts(byte[] ins){}
    private void stt(byte[] ins){}
    private void stx(byte[] ins){}
    
    //Intruções aritiméticas e lógicas com operandos na memória
    //Formato 3/4
    private void add(byte[] ins){}
    private void sub(byte[] ins){}
    private void mul(byte[] ins){}
    private void div(byte[] ins){}
    private void and(byte[] ins){}
    private void or(byte[] ins){}
    
    //Instruções de desvio e subrotinas
    //Formato 3/4
    private void j(byte[] ins){}
    private void jeq(byte[] ins){}
    private void jgt(byte[] ins){}
    private void jlt(byte[] ins){}
    private void jsub(byte[] ins){}
    private void rsub(byte[] ins){}
    
    //Instruções de comparação
    //Formato Variado
    private void comp(byte[] ins){}//Formato 3/4
    private void compr(byte[] ins){}//Formato 2
    private void tix(byte[] ins){}//Formato 3/4
    private void tixr(byte[] ins){}//Formato 2
}