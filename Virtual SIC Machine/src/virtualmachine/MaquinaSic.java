package VirtualMachine;

import utils.DataUtils;
import virtualmachine.OpMap;
import virtualmachine.Maquina;
import virtualmachine.OpCode;
import virtualmachine.Registrador;

public class MaquinaSic implements  Maquina {
    private Registrador[] registradores;
    byte[] memory;
    OpMap opMap;

    public MaquinaSic() {
        memory = new byte[4095];
        
        opMap = new OpMap();
        
        registradores = new Registrador[9];
        for (int i = 0; i < registradores.length; ++i) {
            this.registradores[i] = new Registrador();
        }
        
        test();
    }
    
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

    @Override
    public void compute(byte opcode, Object... params) {
        opMap.get(opcode).solve(params);
    }
}