package main;

import assembler.Montador;
import gui.Window;
import virtualmachine.MaquinaSic;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MaquinaSic sicMachine = new MaquinaSic();
        Montador assembler  = new Montador();
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window window = new Window();
                window.setSicMachine(sicMachine);
                window.setAssembler(assembler);
            }
        });
        
    }
}
