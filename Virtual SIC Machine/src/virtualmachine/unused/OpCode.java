package virtualmachine;

//FORA DE USO
// Mnemonic | Format | Opcode
public enum OpCode {
    ADD("ADD", (byte) 4, (byte) 18);

    private final String mnemonic;
    private final Byte format;
    private final Byte code;

    OpCode(String mnemonic, Byte format, Byte code) {
        this.mnemonic = mnemonic;
        this.format = format;
        this.code = code;
    }
    
    public String getMnemonic() {
        return mnemonic;
    }
    
    public Byte getCode() {
        return code;
    }
    
    public Byte getFormat() {
        return format;
    }
}