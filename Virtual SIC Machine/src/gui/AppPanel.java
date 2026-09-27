package gui;

import VirtualMachine.MaquinaSic;
import java.awt.BorderLayout;
import javax.swing.*;

public class AppPanel extends JPanel {

    private Thread thread = null;
    private boolean isRunning = false;
    private MaquinaSic sicMachine = null;
    private JMenuBar menuBar;

    private ExecutionPanel executionPanel;

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
        if (sicMachine == null) {
            return;
        }
    }

    public AppPanel() {
        init();
        start();
    }

    private void init() {
        setBackground(Palette.getBACKGROUNDColor());
        setLayout(new BorderLayout());
        createMenuBar();
        add(menuBar, BorderLayout.NORTH);

        executionPanel = new ExecutionPanel();
        add(executionPanel, BorderLayout.CENTER);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem loadFileItem = new JMenuItem("Carregar arquivo");
        JMenuItem exportFileResultItem = new JMenuItem("Exportar Resultado");
        fileMenu.add(loadFileItem);
        fileMenu.add(exportFileResultItem);

        JMenu compileMenu = new JMenu("Compilar");
        JMenuItem compileItem = new JMenuItem("Compilar código de máquina");

        compileMenu.add(compileItem);

        JMenu assemblyMenu = new JMenu("Montar");
        JMenuItem AssemblyItem = new JMenuItem("Montar código");

        assemblyMenu.add(AssemblyItem);
        
        JMenu machineMenu = new JMenu("Maquina");
        JMenuItem runItem = new JMenuItem("Rodar");
        JMenuItem stepItem = new JMenuItem("Passo");
        JMenuItem resetItem = new JMenuItem("Reiniciar");
        
        machineMenu.add(runItem);
        machineMenu.add(stepItem);
        machineMenu.add(resetItem);

        JMenu exitMenu = new JMenu("Sair");
        JMenuItem exitItem = new JMenuItem("Sair do programa");
        exitMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(compileMenu);
        menuBar.add(assemblyMenu);
        menuBar.add(machineMenu);
        menuBar.add(exitMenu);

        //######################################################
        //  ACTION LISTENERS
        //######################################################
        loadFileItem.addActionListener(e -> openFile());
        exportFileResultItem.addActionListener(e -> {
            //System.out.println("EXPORTAR");
        });

        compileItem.addActionListener(e -> {
            //System.out.println("COMPILAR");
            String code = executionPanel.getFileContent();
            sicMachine.compilar(code, true);
        });

        AssemblyItem.addActionListener(e -> {
            //System.out.println("MONTAR");
        });
        
        runItem.addActionListener(e -> {
            //System.out.println("RODAR");
            while(sicMachine.getMachineStateSnapshot().executionEnded == false) {
                sicMachine.step();
            }
        });
        stepItem.addActionListener(e -> {
            //System.out.println("PASSO");
            sicMachine.step();
        });
        resetItem.addActionListener(e -> {
            //System.out.println("REINICIAR");
            sicMachine.resetar();
        });

        exitItem.addActionListener(e -> System.exit(0));
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            executionPanel.setCurrentFile(fileChooser.getSelectedFile());
        }
    }
    
    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
    }
}
