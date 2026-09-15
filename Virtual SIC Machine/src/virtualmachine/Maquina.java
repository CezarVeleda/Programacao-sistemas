package virtualmachine;

public interface Maquina {
    void compute(byte opcode, Object... params);
}
