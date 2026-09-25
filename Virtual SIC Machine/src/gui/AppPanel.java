package gui;

import VirtualMachine.MaquinaSic;
import java.awt.GridLayout;
import javax.swing.*;

public class AppPanel extends JPanel {

    private Thread thread = null;
    private boolean isRunning = false;
    MaquinaSic sicMachine = null;
    JTabbedPane tabbedPane;
    ControlPanel controlPanel;
    ExecutionPanel executionPanel;

    private void start() {
        thread = new Thread(() -> run());
        thread.start();
        isRunning = true;
    }

    private void stop() {
        isRunning = false;
    }

    private void run() {
        while (isRunning) {
            update();
        }
    }

    private void update() {
        
        controlPanel.update();
        
        if (sicMachine == null) {
            //System.out.println("ERROR: No SIC Machine in AppPanel");
            return;
        }
    }

    public AppPanel() {
        init();
        start();
    }

    private void init() {
        setBackground(Palette.getBACKGROUNDColor());
        setLayout(new GridLayout());

        controlPanel = new ControlPanel();
        executionPanel = new ExecutionPanel();
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("CONTROLE", controlPanel);
        tabbedPane.addTab("EXECUÇÃO", executionPanel);

        add(tabbedPane);
    }
}
