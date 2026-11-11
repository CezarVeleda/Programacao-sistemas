package gui;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import virtualmachine.MaquinaSic;
import assembler.Montador;

public class AppPanel extends JPanel {

    EUserState currentState = EUserState.CLEAN_STATE;
    private Thread thread = null;
    private volatile boolean isRunning = false;
    private volatile MaquinaSic sicMachine = null;
    private volatile Montador assembler = null;
    private JMenuBar menuBar;

    private Window window;
    private ExecutionPanel executionPanel;
    JMenu fileMenu, compileMenu, assemblyMenu, machineMenu, optionsMenu, themesMenu, exitMenu;

    private void start() {
        thread = new Thread(() -> run());
        thread.start();
        isRunning = true;
    }

    private void stop() {
        isRunning = false;
    }

    private void run() {
        final double TARGET_FPS = 10;
        final long OPTIMAL_TIME = 1000000000L / (long) TARGET_FPS;

        while (isRunning) {
            long startTime = System.nanoTime();

            update();

            long updateTime = System.nanoTime() - startTime;
            long sleepTime = (OPTIMAL_TIME - updateTime) / 1000000L;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void update() {
        if (sicMachine == null || executionPanel == null || assembler == null) {
            return;
        }
        executionPanel.update(sicMachine.getMachineStateSnapshot(), currentState);
        updateGuards();
        updateThemeColors();
    }

    public AppPanel(Window window) {
        this.window = window;
        init();
        start();
    }

    private void init() {
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

        optionsMenu = new JMenu("Opções");
        themesMenu = new JMenu("Temas");
        JMenuItem themeOriginalItem = new JMenuItem("Original");
        JMenuItem themeOriginalPlusItem = new JMenuItem("Original Plus (Padrão)");
        JMenuItem themeAsiaItem = new JMenuItem("Ásia");
        JMenuItem themeHotdogItem = new JMenuItem("Cachorro Quente");
        JMenuItem themeTanItem = new JMenuItem("Bronzeado");
        JMenuItem themeConstantinopleItem = new JMenuItem("Contantinopla");
        JMenuItem themeNightItem = new JMenuItem("Noite");

        optionsMenu.add(themesMenu);
        themesMenu.add(themeOriginalItem);
        themesMenu.add(themeOriginalPlusItem);
        themesMenu.add(themeAsiaItem);
        themesMenu.add(themeHotdogItem);
        themesMenu.add(themeTanItem);
        themesMenu.add(themeConstantinopleItem);
        themesMenu.add(themeNightItem);

        exitMenu = new JMenu("Sair");
        JMenuItem exitItem = new JMenuItem("Sair do programa");
        exitMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(compileMenu);
        menuBar.add(assemblyMenu);
        menuBar.add(machineMenu);
        menuBar.add(optionsMenu);
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
            String code = executionPanel.getCurrentString();
            sicMachine.compilar(code, true);
        });

        AssemblyItem.addActionListener(e -> {
            //System.out.println("MONTAR");
            currentState = EUserState.CODE_ASSEMBLED_STATE;
            assembler.montar(executionPanel.getFileContent());
            executionPanel.setCurrentString(assembler.getCodigoObjeto());
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

        themeOriginalItem.addActionListener(e -> {
            Palette.setCurrentePalette("original");
            executionPanel.setDarkLabelsColor(false);
        });

        themeOriginalPlusItem.addActionListener(e -> {
            Palette.setCurrentePalette("originalPlus");
            executionPanel.setDarkLabelsColor(false);
        });

        themeAsiaItem.addActionListener(e -> {
            Palette.setCurrentePalette("asia");
            executionPanel.setDarkLabelsColor(false);
        });

        themeHotdogItem.addActionListener(e -> {
            Palette.setCurrentePalette("hotdog");
            executionPanel.setDarkLabelsColor(false);
        });

        themeTanItem.addActionListener(e -> {
            Palette.setCurrentePalette("tan");
            executionPanel.setDarkLabelsColor(false);
        });

        themeConstantinopleItem.addActionListener(e -> {
            Palette.setCurrentePalette("constantinople");
            executionPanel.setDarkLabelsColor(false);
        });

        themeNightItem.addActionListener(e -> {
            Palette.setCurrentePalette("night");
            executionPanel.setDarkLabelsColor(true);
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
                optionsMenu.setEnabled(true);
                break;
            case FILE_LOADED_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(false);
                assemblyMenu.setEnabled(true);
                machineMenu.setEnabled(false);
                optionsMenu.setEnabled(true);
                break;
            case CODE_ASSEMBLED_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(true);
                assemblyMenu.setEnabled(false);
                machineMenu.setEnabled(false);
                optionsMenu.setEnabled(true);
                break;
            case CODE_COMPILED_STATE:
                fileMenu.setEnabled(true);
                compileMenu.setEnabled(false);
                assemblyMenu.setEnabled(false);
                machineMenu.setEnabled(true);
                optionsMenu.setEnabled(true);
                break;
            case MACHINE_RUNNING_STATE:
                fileMenu.setEnabled(false);
                compileMenu.setEnabled(false);
                assemblyMenu.setEnabled(false);
                machineMenu.setEnabled(true);
                optionsMenu.setEnabled(false);
                break;
            default:
                throw new AssertionError();
        }
    }

    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
    }

    public void setAssembler(Montador assembler) {
        this.assembler = assembler;
    }

    private void updateThemeColors() {
        String currentPalette = Palette.getCurrentePalette();
        window.setBackgroundColor(Palette.BACKGROUND.getColor(currentPalette));
        this.setBackground(Palette.BACKGROUND.getColor(currentPalette));
        executionPanel.setPrimaryPanelsBackground(Palette.PRIMARY.getColor(currentPalette));
        executionPanel.setSecondaryPanelsBackground(Palette.SECONDARY.getColor(currentPalette));
        executionPanel.setTablesBackground(Palette.PRIMARY.getColor(currentPalette));
    }

}
