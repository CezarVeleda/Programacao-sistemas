package gui;

import virtualmachine.MaquinaSic;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AppPanel extends JPanel {

    //private Thread thread = null;
    //private volatile boolean isRunning = false;
    private volatile MaquinaSic sicMachine = null;
    private JMenuBar menuBar;

    private ExecutionPanel executionPanel;

    /*
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
    */

    private void update() {
        if (sicMachine == null || executionPanel == null) {
            return;
        } 
        executionPanel.update(sicMachine.getMachineStateSnapshot());
    }

    public AppPanel() {
        init();
        //start();
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
        JMenuItem exportFileResultItem = new JMenuItem("Exportar resultado");
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
        //  ACTION LISTENERS E SALVAGUARDAS
        //######################################################
        
        // 1. Inicia com os botões perigosos desativados
        compileItem.setEnabled(false);
        runItem.setEnabled(false);
        stepItem.setEnabled(false);
        resetItem.setEnabled(false);

        loadFileItem.addActionListener(e -> {
            openFile();
            // Só libera o botão de compilar se o arquivo realmente tiver conteúdo
            if (!executionPanel.getFileContent().trim().isEmpty()) {
                compileItem.setEnabled(true);
            }
        });

        compileItem.addActionListener(e -> {
            String code = executionPanel.getFileContent();
            sicMachine.compilar(code, true);
            update(); 
            
            // Só libera os botões de execução APÓS o código estar na memória
            runItem.setEnabled(true);
            stepItem.setEnabled(true);
            resetItem.setEnabled(true);
            
            JOptionPane.showMessageDialog(this, "Código compilado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        });

        runItem.addActionListener(e -> {
            // Trava os botões para o usuário não clicar em "Passo" enquanto está rodando
            runItem.setEnabled(false);
            stepItem.setEnabled(false);
            
            new Thread(() -> {
                sicMachine.run(); 
                SwingUtilities.invokeLater(() -> {
                    update();
                    JOptionPane.showMessageDialog(this, "Execução finalizada!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    // Como a execução acabou, ele só pode resetar agora
                });
            }).start();
        });
        
        stepItem.addActionListener(e -> {
            sicMachine.step();
            update(); 
            
            // Verifica pelo snapshot se a máquina chegou ao fim
            if (sicMachine.getMachineStateSnapshot().executionEnded) {
                runItem.setEnabled(false);
                stepItem.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Fim da execução atingido.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        resetItem.addActionListener(e -> {
            sicMachine.resetar();
            update(); 
            
            // Como a máquina foi resetada (memória zerada), 
            // obrigamos o usuário a compilar de novo antes de rodar
            runItem.setEnabled(false);
            stepItem.setEnabled(false);
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
    
    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
    }
}
