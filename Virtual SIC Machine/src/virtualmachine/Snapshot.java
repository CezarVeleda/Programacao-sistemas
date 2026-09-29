package virtualmachine;

import utils.DataUtils;

public class Snapshot {
    public final int A, X, L, B, S, T, F, PC, SW;
    public final Condicional cc;
    public final boolean executionEnded;
    public final int[] memory;

    public Snapshot(int a, int x, int l, int b, int s, int t, int f, int pc, int sw, Condicional cc, boolean ended, byte[] memory) {
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
        
        // Converte os bytes físicos em palavras lógicas de 24 bits
        this.memory = new int[4095];
        for (int i = 0; i < 4095; i++) {
            this.memory[i] = DataUtils.bytes24ToInt(memory, i * 3);
        }
    }
}