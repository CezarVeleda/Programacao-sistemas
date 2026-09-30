package gui;

import virtualmachine.MaquinaSic;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AppPanel extends JPanel {

    EUserState currentState = EUserState.CLEAN_STATE;
    private Thread thread = null;
    private volatile boolean isRunning = false;
    private volatile MaquinaSic sicMachine = null;
    private JMenuBar menuBar;

    private ExecutionPanel executionPanel;
    JMenu fileMenu;
    JMenu compileMenu;
    JMenu assemblyMenu;
    JMenu machineMenu;
    JMenu exitMenu;

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
        if (sicMachine == null || executionPanel == null) {
            return;
        }
        executionPanel.update(sicMachine.getMachineStateSnapshot(), currentState);
        updateGuards();
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

        fileMenu = new JMenu("Arquivo");
        JMenuItem loadFileItem = new JMenuItem("Carregar arquivo");
        JMenuItem exportFileResultItem = new JMenuItem("Exportar resultado");
        fileMenu.add(loadFileItem);
        fileMenu.add(exportFileResultItem);

        compileMenu = new JMenu("Compilar");
        JMenuItem compileItem = new JMenuItem("Compilar código de máquina");

        compileMenu.add(compileItem);

        assemblyMenu = new JMenu("Montar");
        JMenuItem AssemblyItem = new JMenuItem("Montar código");

        assemblyMenu.add(AssemblyItem);

        machineMenu = new JMenu("Maquina");
        JMenuItem runItem = new JMenuItem("Rodar");
        JMenuItem stepItem = new JMenuItem("Passo");
        JMenuItem resetItem = new JMenuItem("Reiniciar");

        machineMenu.add(runItem);
        machineMenu.add(stepItem);
        machineMenu.add(resetItem);

        exitMenu = new JMenu("Sair");
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
        loadFileItem.addActionListener(e -> {
            openFile();
            if (!executionPanel.getFileContent().equals("")) {
                currentState = EUserState.FILE_LOADED_STATE;
            }
        });
        exportFileResultItem.addActionListener(e -> {
            //System.out.println("EXPORTAR");
        });

        compileItem.addActionListener(e -> {
            //System.out.println("COMPILAR");
            currentState = EUserState.CODE_COMPILED_STATE;
            String code = executionPanel.getFileContent();
            sicMachine.compilar(code, true);
        });

        AssemblyItem.addActionListener(e -> {
            //System.out.println("MONTAR");
        });

        runItem.addActionListener(e -> {
            //System.out.println("RODAR");
            currentState = EUserState.MACHINE_RUNNING_STATE;
            sicMachine.run();
        });
        stepItem.addActionListener(e -> {
            //System.out.println("PASSO");
            currentState = EUserState.MACHINE_RUNNING_STATE;
            sicMachine.step();
        });
        resetItem.addActionListener(e -> {
            //System.out.println("REINICIAR");
            currentState = EUserState.FILE_LOADED_STATE;
            sicMachine.resetar();
        });

        exitItem.addActionListener(e -> System.exit(0));
    }

    private void openFile() {
        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");

        JFileChooser fileChooser = new JFileChooser(desktop);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            executionPanel.setCurrentFile(fileChooser.getSelectedFile());
        }
    }

    private void updateGuards() {
        switch (currentState) {
            case CLEAN_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(false);
                assemblyMenu.setEnabled(false);
                machineMenu.setEnabled(false);
                break;
            case FILE_LOADED_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(true);
                assemblyMenu.setEnabled(true);
                machineMenu.setEnabled(false);
                break;
            case CODE_COMPILED_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(true);
                assemblyMenu.setEnabled(true);
                machineMenu.setEnabled(true);
                break;
            case MACHINE_RUNNING_STATE:
                fileMenu.setEnabled(false);
                compileMenu.setEnabled(false);
                assemblyMenu.setEnabled(false);
                machineMenu.setEnabled(true);
                break;
            default:
                throw new AssertionError();
        }
    }

    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
    }

}
