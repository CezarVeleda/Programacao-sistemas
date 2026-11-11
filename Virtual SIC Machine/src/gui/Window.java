package gui;

import assembler.Montador;
import java.awt.Color;
import javax.swing.*;
import virtualmachine.MaquinaSic;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class Window extends JFrame {

    private static final String WINDOW_TITLE = "Maquina virtual: SIC/XE";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private JPanel upperPanel, lowerPanel, leftPanel, rightPanel;
    private AppPanel appPanel;
    protected MaquinaSic sicMachine;
    protected Montador assembler;

    public Window() {
        initWindow();
        initPanels();
    }

    private void initWindow() {
        setTitle(WINDOW_TITLE);
        Dimension d = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // ######################################################
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        // ######################################################
        
        pack();
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initPanels() {
        appPanel = new AppPanel(this);
        
        upperPanel = new JPanel();
        lowerPanel = new JPanel();
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        
        
        final double spacing = 0.1;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = spacing*2;
        gbc.weighty = spacing;
        gbc.gridx = 0;
        gbc.gridy = 0;
        getContentPane().add(upperPanel, gbc);

        gbc.gridy = 2;
        getContentPane().add(lowerPanel, gbc);

        gbc.gridy = 1;
        gbc.weightx = spacing;
        gbc.weighty = spacing*2;
        gbc.gridwidth = 1;
        getContentPane().add(leftPanel, gbc);

        gbc.gridx = 2;
        getContentPane().add(rightPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0 - spacing*2;
        gbc.weighty = 1.0 - spacing*2;
        getContentPane().add(appPanel, gbc);
    }

    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
        appPanel.setSicMachine(sicMachine);
    }

    public void setAssembler(Montador assembler) {
        this.assembler = assembler;
        appPanel.setAssembler(assembler);
    }
    
    protected void setBackgroundColor(Color color) {
        getContentPane().setBackground(color);
        lowerPanel.setBackground(color);
        upperPanel.setBackground(color);
        leftPanel.setBackground(color);
        rightPanel.setBackground(color);
    }
    
}
