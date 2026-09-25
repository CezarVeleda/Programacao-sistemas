package gui;

import java.awt.HeadlessException;
import javax.swing.*;
import VirtualMachine.MaquinaSic;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.plaf.ColorUIResource;

public class Window extends JFrame {

    private static final String WINDOW_TITLE = "Maquina virtual: SIC/XE";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private JPanel upperPanel, lowerPanel, leftPanel, rightPanel;
    private AppPanel appPanel;
    protected MaquinaSic sicMachine;

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
        //######################################################
        setLayout(new GridBagLayout());

        //######################################################
        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initPanels() {
        appPanel = new AppPanel();
        //################################
        upperPanel = new JPanel();
        upperPanel.setBackground(Palette.getBACKGROUNDColor());
        lowerPanel = new JPanel();
        lowerPanel.setBackground(Palette.getBACKGROUNDColor());
        leftPanel = new JPanel();
        leftPanel.setBackground(Palette.getBACKGROUNDColor());
        rightPanel = new JPanel();
        rightPanel.setBackground(Palette.getBACKGROUNDColor());
        //#################################
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = .9;
        gbc.weighty = .1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(upperPanel, gbc);

        gbc.gridy = 2;
        add(lowerPanel, gbc);

        gbc.gridy = 1;
        gbc.weightx = .1;
        gbc.weighty = .9;
        gbc.gridwidth = 1;
        add(leftPanel, gbc);

        gbc.gridx = 2;
        add(rightPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = .8;
        gbc.weighty = .8;
        add(appPanel, gbc);
    }

    public void setSicMachine(MaquinaSic sicMachine) {
        this.sicMachine = sicMachine;
    }

}
