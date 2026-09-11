package virtualmachine;

// ESSA CLASSE É UM EXEMPLO

public class OPAdd extends OP{

    @Override
    public int Solve(int a) {
        throw new UnsupportedOperationException("ADD need two operators");
    }

    @Override
    public int Solve(int a, int b) {
        return a + b;
    }
    
}
