package assembler;

//Guarda os dados calculados (o deslocamento DISP e as flags b, p e e)

public class AddressingResult {
    private final int disp;     // Deslocamento/Endereço de 12 bits (ou 20 bits se e=1)
    private final boolean b;    // Flag Relativo à Base
    private final boolean p;    // Flag Relativo ao PC
    private final boolean e;    // Flag Formato 4 Estendido

    public AddressingResult(int disp, boolean b, boolean p, boolean e) {
        this.disp = disp;
        this.b = b;
        this.p = p;
        this.e = e;
    }

    public int getDisp() { return disp; }
    public boolean isB() { return b; }
    public boolean isP() { return p; }
    public boolean isE() { return e; }
}
