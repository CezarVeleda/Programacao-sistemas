package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import virtualmachine.Snapshot;

public class ExecutionPanel extends JPanel {

    private File currentFile = null;
    private Scanner scanner;
    private String fileContent = "";
    private String executionResult = "";
    private String[] RegStrings = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};
    private DefaultTableModel registerTableModel;
    private DefaultTableModel memoryTableModel;
    private JLabel runningStatus, fileLoadedLabel, codeCompiledLabel, machineReadyLabel;

    JPanel execStatusPanel, memoryPanel, registerPanel;

    public ExecutionPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(Palette.getSECONDARYColor());

        initExecStatusPanel();
        initMemoryPanel();
        initRegisterPanel();

        execStatusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        registerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        memoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        execStatusPanel.setPreferredSize(new Dimension(400, 300));
        registerPanel.setPreferredSize(new Dimension(400, 150));
        memoryPanel.setPreferredSize(new java.awt.Dimension(250, 450));

        execStatusPanel.setMinimumSize(new Dimension(100, 100));
        registerPanel.setMinimumSize(new Dimension(100, 100));
        memoryPanel.setMinimumSize(new Dimension(150, 100));

        JSplitPane divisorEsquerdo = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                execStatusPanel,
                registerPanel
        );
        divisorEsquerdo.setResizeWeight(0.66);
        divisorEsquerdo.setContinuousLayout(true);
        divisorEsquerdo.setOpaque(false);
        divisorEsquerdo.setBorder(null);

        JSplitPane divisorPrincipal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                divisorEsquerdo,
                memoryPanel
        );
        divisorPrincipal.setResizeWeight(0.66);
        divisorPrincipal.setContinuousLayout(true);
        divisorPrincipal.setOpaque(false);
        divisorPrincipal.setBorder(null);

        add(divisorPrincipal, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            divisorEsquerdo.setDividerLocation(0.66);
            divisorPrincipal.setDividerLocation(0.66);
        });
    }

    private void initExecStatusPanel() {
        execStatusPanel = new JPanel();
        execStatusPanel.setBackground(Palette.getPRIMARYColor());
        execStatusPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        gbc.weightx = 1.0;

        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("ESTADO DE EXECUÇÃO");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        execStatusPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        runningStatus = new JLabel(); 
        execStatusPanel.add(runningStatus, gbc);
        
        gbc.gridy = 2;
        fileLoadedLabel = new JLabel(); 
        execStatusPanel.add(fileLoadedLabel, gbc);
        
        gbc.gridy = 3;
        codeCompiledLabel = new JLabel();
        execStatusPanel.add(codeCompiledLabel, gbc);
        
        gbc.gridy = 4;
        machineReadyLabel = new JLabel();
        execStatusPanel.add(machineReadyLabel, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        execStatusPanel.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);
    }

    private void initMemoryPanel() {
        memoryPanel = new JPanel();
        memoryPanel.setBackground(Palette.getPRIMARYColor());
        memoryPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MEMÓRIA", SwingConstants.CENTER);
        memoryPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Endereço", "Valor (Hex)"};
        memoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int i = 0; i < 4095; i++) {
            String enderecoHex = String.format("%04X", i);
            String valorInicial = "000000";

            memoryTableModel.addRow(new Object[]{enderecoHex, valorInicial});
        }

        JTable table = new JTable(memoryTableModel);

        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        memoryPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void initRegisterPanel() {
        registerPanel = new JPanel();
        registerPanel.setBackground(Palette.getPRIMARYColor());
        registerPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("REGISTRADORES");
        registerPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Registradores", "Valor"};

        registerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String s : RegStrings) {
            registerTableModel.addRow(new Object[]{s, "000000"});
        }

        JTable table = new JTable(registerTableModel);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        registerPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void setCurrentFile(File newFile) {
        this.currentFile = newFile;
        readFile();

    }

    public String getFileContent() {
        return fileContent;
    }

    public void update(Snapshot snapshot, EUserState currentState) {

        boolean isRunning = !snapshot.executionEnded;
        String s;
        s = (currentState == EUserState.MACHINE_RUNNING_STATE) ? "sim" : "não";
        runningStatus.setText("Usuário executando a máquina: " + s);
        s = (currentState != EUserState.CLEAN_STATE) ? "sim" : "não";
        fileLoadedLabel.setText("Arquivo carregado no app: " + s);
        s = (currentState == EUserState.CODE_COMPILED_STATE || currentState == EUserState.MACHINE_RUNNING_STATE) ? "sim" : "não";
        codeCompiledLabel.setText("Código compilado na máquina: " + s);
        s = (currentState == EUserState.CODE_COMPILED_STATE || currentState == EUserState.MACHINE_RUNNING_STATE) ? "sim" : "não";
        machineReadyLabel.setText("Máquina pronta para execução: " + s);

        int[] valoresReg = {
            snapshot.A, snapshot.X, snapshot.L, snapshot.B,
            snapshot.S, snapshot.T, snapshot.F, snapshot.PC, snapshot.SW
        };

        for (int i = 0; i < RegStrings.length; i++) {
            String valorHex = String.format("%06X", valoresReg[i] & 0xFFFFFF);
            registerTableModel.setValueAt(valorHex, i, 1);
        }

        if (snapshot.memory != null) {
            for (int i = 0; i < snapshot.memory.length; i++) {
                String valorPalavraHex = String.format("%06X", snapshot.memory[i] & 0xFFFFFF);
                memoryTableModel.setValueAt(valorPalavraHex, i, 1);
            }
        }
    }

    private void readFile() {
        fileContent = ""; // 1. Zera o conteúdo anterior
        try {
            scanner = new Scanner(currentFile);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    // 2. Adiciona a quebra de linha ao concatenar!
                    fileContent += line + "\n"; 
                }
            }
            
        } catch (FileNotFoundException ex) {
            System.getLogger(ExecutionPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
