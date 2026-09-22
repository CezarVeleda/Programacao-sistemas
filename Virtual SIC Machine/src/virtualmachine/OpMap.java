package virtualmachine;

//FORA DE USO

import VirtualMachine.OP;
import java.util.HashMap;
import utils.DataUtils;

public class OpMap extends HashMap<Byte, OP>{

    public OpMap() {
        put(OpCode.ADD.getCode(), args -> {
            Registrador reg = (Registrador) args[0];
            byte[] memory = (byte[]) args[1];
            int adress = (int) args[2];
            int val1 = DataUtils.bytes24ToInt(reg.getVal());
            int val2 = DataUtils.bytes24ToInt(memory, adress);
            byte[] res = DataUtils.intToBytes24(val1 + val2);
            reg.setVal(res);
        });
    }
    
}
