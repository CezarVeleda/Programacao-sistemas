package virtualmachine;

import utils.DataUtils;

public class Registrador {
    private byte[] val = new byte[3];

    public void setVal(byte[] val) {
        this.val = val;
    }

    public byte[] getVal() {
        return val;
    }

    public void setIntVal(int val) {
        this.val = DataUtils.intToBytes24(val);
    }

    public int getIntVal() {
        return DataUtils.bytes24ToInt(this.val);
    }
}
