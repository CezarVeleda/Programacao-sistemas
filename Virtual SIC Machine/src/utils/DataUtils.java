package utils;

public class DataUtils {

    /**
     * Converte 3 bytes da memória para Int COM extensão de sinal.
     * Usado para leitura de dados numéricos (aritmética).
     */
    public static int bytes24ToIntSigned(byte[] memory, int offset) {
        int val = ((memory[offset] & 0xFF) << 16) | 
                  ((memory[offset + 1] & 0xFF) << 8) | 
                   (memory[offset + 2] & 0xFF);

        // Se o 24º bit for 1, estende o sinal para os 32 bits do int Java
        if ((val & 0x800000) != 0) {
            val |= 0xFF000000;
        }

        return val;
    }

    /**
     * Converte 3 bytes da memória para Int SEM extensão de sinal (Cru).
     * Usado para leitura de endereços e ponteiros lógicos.
     */
    public static int bytes24ToIntUnsigned(byte[] memory, int offset) {
        return ((memory[offset] & 0xFF) << 16) | 
               ((memory[offset + 1] & 0xFF) << 8) | 
                (memory[offset + 2] & 0xFF);
    }

    /**
     * Grava um inteiro diretamente em 3 bytes do vetor de memória (centraliza a atribuição física).
     */
    public static void writeInt24ToMemory(int val, byte[] memory, int offset) {
        memory[offset] = (byte) ((val >> 16) & 0xFF);
        memory[offset + 1] = (byte) ((val >> 8) & 0xFF);
        memory[offset + 2] = (byte) (val & 0xFF);
    }
    
    /**
     * Copia um trecho da memória instanciando um novo array (usado para buscar instruções F3/4).
     */
    public static byte[] readBytes(byte[] memory, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(memory, offset, result, 0, length);
        return result;
    }
    
    // --- Mantidos por compatibilidade para uso em outras partes do sistema ---
    
    public static int bytes24ToInt(byte[] bytes) {
        return bytes24ToIntSigned(bytes, 0);
    }
    
    public static byte[] intToBytes24(int val) {
        byte[] bytes = new byte[3];
        writeInt24ToMemory(val, bytes, 0);
        return bytes;
    }
}