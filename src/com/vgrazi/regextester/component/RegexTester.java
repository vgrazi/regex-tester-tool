package com.vgrazi.regextester.component;

import com.vgrazi.regextester.action.Colorizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RegexTester {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Regex Test Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JLabel patternJlabel = new JLabel("Pattern  ");
        patternJlabel.setBackground(Color.LIGHT_GRAY);
        topPanel.add(patternJlabel, BorderLayout.WEST);

        splitPane.add(topPanel);

        PatternPane patternPane = new PatternPane();
        topPanel.add(patternPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton matchButton = new JRadioButton("Match");
        JRadioButton lookingAtButton = new JRadioButton("Looking at");
        JRadioButton splitButton = new JRadioButton("Split");
        JRadioButton replaceButton = new JRadioButton("Replace");
        JRadioButton findButton = new JRadioButton("Find");
        findButton.setSelected(true);

        findButton.setActionCommand("Find");
        matchButton.setActionCommand("Match");
        lookingAtButton.setActionCommand("Looking at");
        splitButton.setActionCommand("Split");
        replaceButton.setActionCommand("Replace");

        buttonGroup.add(findButton);
        buttonGroup.add(matchButton);
        buttonGroup.add(lookingAtButton);
        buttonGroup.add(splitButton);
        buttonGroup.add(replaceButton);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(findButton);
        buttonPanel.add(matchButton);
        buttonPanel.add(lookingAtButton);
        buttonPanel.add(splitButton);
        buttonPanel.add(replaceButton);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        JTextPane characterPane = new JTextPane();
        characterPane.setFont(new Font("Courier New", Font.PLAIN, 20));
        characterPane.getStyledDocument().addStyle("fontSize", null);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        characterPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Colorizer.renderFindCharacterPane(characterPane, patternPane.getText());
            }
        });
        patternPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Colorizer.renderFindCharacterPane(characterPane, patternPane.getText());
            }
        });

        splitPane.add(bottomPanel);

        frame.getContentPane().add(splitPane);


        frame.setBounds(100, 100, 1000, 180);
        frame.setVisible(true);
    }

}
