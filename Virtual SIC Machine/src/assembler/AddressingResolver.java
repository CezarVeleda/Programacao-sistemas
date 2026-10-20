package assembler;

public class AddressingResolver {

    //Executa a lógica matemática das prioridades de endereçamento (Relativo ao PC, Relativo à Base, Direto e Formato 4 estendido).
    
    public AddressingResult resolveAddressing(int targetAddress, int locctr, Integer baseRegisterValue, boolean isExtendedF4) {
        
        if (isExtendedF4) {
            return new AddressingResult(targetAddress & 0xFFFFF, false, false, true);
        }

        // Tentativa 1: Relativo ao PC (p=1, b=0)
        int pc = locctr + 1; 
        int dispPC = targetAddress - pc;

        if (dispPC >= -2048 && dispPC <= 2047) {
            return new AddressingResult(dispPC & 0xFFF, false, true, false);
        }

        // Tentativa 2: Relativo à Base (b=1, p=0)
        if (baseRegisterValue != null) {
            int dispBase = targetAddress - baseRegisterValue;

            if (dispBase >= 0 && dispBase <= 4095) {
                return new AddressingResult(dispBase & 0xFFF, true, false, false);
            }
        }

        // Tentativa 3: Endereço Direto F3 (p=0, b=0)
        if (targetAddress >= 0 && targetAddress <= 4095) {
            return new AddressingResult(targetAddress & 0xFFF, false, false, false);
        }

        throw new RuntimeException(
            String.format("Erro de Montagem: Endereço de destino (%d) fora do alcance de 12 bits no LOCCTR %d. Use o formato estendido (+).", targetAddress, locctr)
        );
    }
}
