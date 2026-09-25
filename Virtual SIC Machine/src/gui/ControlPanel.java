package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

public class ControlPanel extends JPanel {

    private JPanel configPanel, machinePanel;
    private Font font = new Font("Dialog", Font.BOLD, 16);
    private java.io.File currentSelectedFile = null;

    private JLabel newArqLabel;
    private JLabel currentLoadedFileLabel;
    private JButton newArqButton;
    
    private JLabel compileLabel;
    private JButton compileButton;
    private JLabel assembleLabel;
    private JButton assembleButton;

    public ControlPanel() {
        init();
    }

    private void init() {
        setLayout(new GridLayout());
        
        initConfigPanel();
        initMachinePanel();

        add(configPanel);
        add(machinePanel);
    }

    private void initMachinePanel() {
        JPanel dummyPanel = new JPanel();
        machinePanel = new JPanel();
        machinePanel.setLayout(new GridBagLayout());
        machinePanel.setBackground(Palette.getSECONDARYColor());
        dummyPanel.setBackground(Palette.getSECONDARYColor());

        //######################################################
        compileLabel = new JLabel("COMPILAR CODIGO DE MAQUINA:");
        compileButton = new JButton("compilar");
        
        assembleLabel = new JLabel("MONTAR CODIGO:");
        assembleButton = new JButton("montar");

        compileLabel.setFont(font);
        compileButton.setBackground(Palette.getPRIMARYColor());
        assembleLabel.setFont(font);
        assembleButton.setBackground(Palette.getPRIMARYColor());

        //######################################################
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        machinePanel.add(compileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        machinePanel.add(compileButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        machinePanel.add(assembleLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        machinePanel.add(assembleButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        //dummyPanel.setBackground(Color.red);
        machinePanel.add(dummyPanel, gbc);
    }

    private void initConfigPanel() {
        JPanel dummyPanel = new JPanel();
        configPanel = new JPanel();
        configPanel.setLayout(new GridBagLayout());
        configPanel.setBackground(Palette.getSECONDARYColor());
        dummyPanel.setBackground(Palette.getSECONDARYColor());

        //######################################################
        newArqLabel = new JLabel("ABRIR NOVO ARQUIVO:");
        currentLoadedFileLabel = new JLabel("NONE");
        newArqButton = new JButton("abrir");
        newArqButton.setBackground(Palette.getPRIMARYColor());

        newArqLabel.setFont(font);

        //######################################################
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        configPanel.add(newArqLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        configPanel.add(newArqButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        configPanel.add(currentLoadedFileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        //dummyPanel.setBackground(Color.red);
        configPanel.add(dummyPanel, gbc);

        //#####################################
        newArqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filePopup();
            }
        });
    }

    public void update() {
        final String loadedPrefixFile = "Arquivo atual: ";
        if (currentSelectedFile == null) {
            currentLoadedFileLabel.setText(loadedPrefixFile + "NENHUM");
        } else {
            currentLoadedFileLabel.setText(loadedPrefixFile + currentSelectedFile.getAbsolutePath());
        }
    }

    private void filePopup() {
        JFileChooser fileChooser = new JFileChooser();
        int res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            currentSelectedFile = file;
        }
    }
}