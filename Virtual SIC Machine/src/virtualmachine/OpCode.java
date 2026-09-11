package virtualmachine;

// Essas Operações aqui são um exemplo do Enum
public enum OpCode {
    ADD("000", "ADD"),
    SUB("001", "SUB"),
    MULT("010", "MULT"),
    DIV("011", "DIV");

    private final String code;
    private final String mnemonic;

    OpCode(String code, String mnemonic) {
        this.code = code;
        this.mnemonic = mnemonic;
    }

    public String getCode() {
        return code;
    }

    public String getMnemonic() {
        return mnemonic;
    }
}