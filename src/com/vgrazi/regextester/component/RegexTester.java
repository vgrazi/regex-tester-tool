package com.vgrazi.regextester.component;

import com.vgrazi.regextester.action.Colorizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RegexTester {
    private static final Font DEFAULT_FONT = new Font("Courier New", Font.PLAIN, 20);
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
        patternPane.setFont(DEFAULT_FONT);

        topPanel.add(patternPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton matchButton = new JRadioButton("Matches");
        JRadioButton lookingAtButton = new JRadioButton("Looking at");
        JRadioButton splitButton = new JRadioButton("Split");
        JRadioButton replaceButton = new JRadioButton("Replace");
        JRadioButton findButton = new JRadioButton("Find");
        findButton.setSelected(true);

        findButton.setActionCommand("find");
        matchButton.setActionCommand("matches");
        lookingAtButton.setActionCommand("looking-at");
        splitButton.setActionCommand("split");
        replaceButton.setActionCommand("replace");

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
        characterPane.setFont(DEFAULT_FONT);
        characterPane.getStyledDocument().addStyle("fontSize", null);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                renderCharacterPane(characterPane, patternPane, buttonGroup);
            }
        };
        characterPane.addKeyListener(keyListener);
        patternPane.addKeyListener(keyListener);
        ActionListener actionListener = e -> renderCharacterPane(characterPane, patternPane, buttonGroup);
        findButton.addActionListener(actionListener);
        lookingAtButton.addActionListener(actionListener);
        matchButton.addActionListener(actionListener);
        replaceButton.addActionListener(actionListener);
        splitButton.addActionListener(actionListener);



        splitPane.add(bottomPanel);

        frame.getContentPane().add(splitPane);


        frame.setBounds(100, 100, 1000, 180);
        frame.setVisible(true);
    }

    private static void renderCharacterPane(JTextPane characterPane, PatternPane patternPane, ButtonGroup buttonGroup) {
        Colorizer.renderFindCharacterPane(characterPane, patternPane.getText(), buttonGroup.getSelection().getActionCommand());
    }

}
