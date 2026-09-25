package main;

import VirtualMachine.MaquinaSic;
import gui.Window;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MaquinaSic sicMachine = new MaquinaSic();
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window window = new Window();
            }
        });
    }
}
