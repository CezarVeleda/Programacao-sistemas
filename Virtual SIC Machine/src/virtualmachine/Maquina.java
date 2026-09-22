package virtualmachine;

public interface Maquina {
    //void compute(byte opcode, Object... params);
    void step();
    void run();
    //Snapshot getMachineStateSnapshot();
    //void buildAndReset();
}
