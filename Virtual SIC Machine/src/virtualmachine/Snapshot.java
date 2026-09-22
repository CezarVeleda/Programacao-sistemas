package virtualmachine;

public class Snapshot {
    public final int A, X, L, B, S, T, F, PC, SW;
    public final Condicional cc;
    public final boolean executionEnded;

    public Snapshot(int a, int x, int l, int b, int s, int t, int f, int pc, int sw, Condicional cc, boolean ended) {
        this.A = a;
        this.X = x;
        this.L = l;
        this.B = b;
        this.S = s;
        this.T = t;
        this.F = f;
        this.PC = pc;
        this.SW = sw;
        this.cc = cc;
        this.executionEnded = ended;
    }
}