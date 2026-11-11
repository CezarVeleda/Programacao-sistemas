package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
    private String currentString = "";
    private String executionResult = "";
    private String[] RegStrings = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};
    
    // Tipagem explícita dos modelos de tabela
    private DefaultTableModel registerTableModel;
    private DefaultTableModel memoryTableModel;
    
    // Referências às tabelas necessárias para customizar o fundo e pintura das células
    private JTable registerTable;
    private JTable memoryTable;

    private JLabel runningStatus, fileLoadedLabel, codeCompiledLabel, machineReadyLabel;

    JPanel execStatusPanel, memoryPanel, registerPanel;

    public ExecutionPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

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
        memoryPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MEMÓRIA", SwingConstants.CENTER);
        memoryPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Endereço", "Valor"};
        
        memoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        for (int i = 0; i < 4095; i++) {
            String hexAddress = String.format("#" + "%04X", i);
            String initialValue = "000000";

            memoryTableModel.addRow(new Object[]{hexAddress, initialValue});
        }

        memoryTable = new JTable(memoryTableModel);
        memoryTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(memoryTable);
        memoryPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void initRegisterPanel() {
        registerPanel = new JPanel();
        registerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("REGISTRADORES");
        registerPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Registradores", "Valor"};

        registerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        for (String s : RegStrings) {
            registerTableModel.addRow(new Object[]{s, "000000"});
        }

        registerTable = new JTable(registerTableModel);
        registerTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(registerTable);
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
            String hexValue = String.format("%06X", valoresReg[i] & 0xFFFFFF);
            registerTableModel.setValueAt("#" + hexValue, i, 1);
        }

        if (snapshot.memory != null) {
            for (int i = 0; i < snapshot.memory.length; i++) {
                String hexWordValue = String.format("%06X", snapshot.memory[i] & 0xFFFFFF);
                memoryTableModel.setValueAt("#" + hexWordValue, i, 1);
            }
        }
    }

    private void readFile() {
        fileContent = "";
        try {
            scanner = new Scanner(currentFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    fileContent += line + "\n";
                }
            }

        } catch (FileNotFoundException ex) {
            System.getLogger(ExecutionPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        currentString = fileContent;
    }

    public String getCurrentString() {
        return currentString;
    }

    public void setCurrentString(String currentString) {
        this.currentString = currentString;
    }

    protected void setPrimaryPanelsBackground(Color color) {
        setBackground(color);
        execStatusPanel.setBackground(color);
        memoryPanel.setBackground(color);
        registerPanel.setBackground(color);
    }
    
    protected void setSecondaryPanelsBackground(Color color) {
        memoryPanel.setBackground(color);
        registerPanel.setBackground(color);
    }

    public void setTablesBackground(Color color) {
        if (registerTable != null) {
            registerTable.setBackground(color);
            registerTable.setOpaque(true);
            if (registerTable.getParent() instanceof JViewport) {
                registerTable.getParent().setBackground(color);
            }
        }

        if (memoryTable != null) {
            memoryTable.setBackground(color);
            memoryTable.setOpaque(true);
            if (memoryTable.getParent() instanceof JViewport) {
                memoryTable.getParent().setBackground(color);
            }
        }
    }
    
    public void setDarkLabelsColor(boolean isDark) {
        Color textColor = isDark ? new Color(220, 220, 220) : new Color(30, 30, 30);
        Color titleColor = isDark ? Color.WHITE : Color.DARK_GRAY;
        
        if (runningStatus != null) runningStatus.setForeground(textColor);
        if (fileLoadedLabel != null) fileLoadedLabel.setForeground(textColor);
        if (codeCompiledLabel != null) codeCompiledLabel.setForeground(textColor);
        if (machineReadyLabel != null) machineReadyLabel.setForeground(textColor);

        updateComponentLabelsRecursive(this, titleColor, textColor);

        if (registerTable != null) {
            registerTable.setForeground(textColor);
        }
        if (memoryTable != null) {
            memoryTable.setForeground(textColor);
        }
    }

    private void updateComponentLabelsRecursive(java.awt.Component comp, Color titleColor, Color textColor) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            if (label.getFont().isBold() || label.getText().equals("ESTADO DE EXECUÇÃO") || label.getText().equals("MEMÓRIA") || label.getText().equals("REGISTRADORES")) {
                label.setForeground(titleColor);
            } else {
                label.setForeground(textColor);
            }
        } else if (comp instanceof Container) {
            for (java.awt.Component child : ((Container) comp).getComponents()) {
                updateComponentLabelsRecursive(child, titleColor, textColor);
            }
        }
    }
}