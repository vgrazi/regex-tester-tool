package com.vgrazi.regextester.component;

import com.vgrazi.regextester.action.Colorizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import static com.vgrazi.regextester.component.Constants.DEFAULT_PANEL_FONT;

public class RegexTester {

    private static int flags;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Regex Test Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(50);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JLabel patternJlabel = new JLabel("Pattern  ");
        patternJlabel.setBackground(Color.LIGHT_GRAY);
        patternJlabel.setFont(DEFAULT_PANEL_FONT);
        topPanel.add(patternJlabel, BorderLayout.WEST);

        splitPane.add(topPanel);

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

        findButton.setFont(DEFAULT_PANEL_FONT);
        matchButton.setFont(DEFAULT_PANEL_FONT);
        lookingAtButton.setFont(DEFAULT_PANEL_FONT);
        splitButton.setFont(DEFAULT_PANEL_FONT);
        replaceButton.setFont(DEFAULT_PANEL_FONT);

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(findButton);
        buttonPanel.add(matchButton);
        buttonPanel.add(lookingAtButton);
        buttonPanel.add(splitButton);
        buttonPanel.add(replaceButton);
        buttonPanel.add(Box.createHorizontalGlue());
        JCheckBox caseButton = new JCheckBox("Case Insensitive");
        JCheckBox commentsButton = new JCheckBox("Comments");
        JCheckBox dotallButton = new JCheckBox("Dot All");
        JCheckBox literalButton = new JCheckBox("Literal");
        JCheckBox multilineButton = new JCheckBox("Multiline");
        buttonPanel.add(caseButton);
        buttonPanel.add(commentsButton);
        buttonPanel.add(dotallButton);
        buttonPanel.add(literalButton);
        buttonPanel.add(multilineButton);

        caseButton.setFont(DEFAULT_PANEL_FONT);
        commentsButton.setFont(DEFAULT_PANEL_FONT);
        dotallButton.setFont(DEFAULT_PANEL_FONT);
        literalButton.setFont(DEFAULT_PANEL_FONT);
        multilineButton.setFont(DEFAULT_PANEL_FONT);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        JTextPane characterPane = new JTextPane();
        characterPane.setForeground(Constants.FONT_COLOR);
        characterPane.setBackground(Constants.BACKGROUND_COLOR);
        characterPane.setFont(Constants.DEFAULT_PANE_FONT);

        characterPane.getStyledDocument().addStyle("highlights", null);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        PatternPane patternPane = new PatternPane(characterPane);
        patternPane.setFont(Constants.DEFAULT_PANE_FONT);
        patternPane.setForeground(Constants.FONT_COLOR);
        patternPane.setBackground(Constants.BACKGROUND_COLOR);

        ActionListener recalcFlagListener = e -> {
            flags = recalculateFlags(caseButton, commentsButton, dotallButton, literalButton, multilineButton);
            renderCharacterPane(characterPane, patternPane, buttonGroup);
            patternPane.setFlags(flags);
        };
        caseButton.addActionListener(recalcFlagListener);
        commentsButton.addActionListener(recalcFlagListener);
        dotallButton.addActionListener(recalcFlagListener);
        literalButton.addActionListener(recalcFlagListener);
        multilineButton.addActionListener(recalcFlagListener);

        topPanel.add(patternPane, BorderLayout.CENTER);

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

        frame.setBounds(100, 100, 1000, 600);
        frame.setVisible(true);
    }

    private static int recalculateFlags(JCheckBox caseButton, JCheckBox commentsButton, JCheckBox dotallButton, JCheckBox literalButton, JCheckBox multilineButton) {
        int flags = 0;
        flags |= caseButton.isSelected()? Pattern.CASE_INSENSITIVE:0;
        flags |= commentsButton.isSelected()? Pattern.COMMENTS:0;
        flags |= dotallButton.isSelected()? Pattern.DOTALL:0;
        flags |= literalButton.isSelected()? Pattern.LITERAL:0;
        flags |= multilineButton.isSelected()? Pattern.MULTILINE:0;
        return flags;
    }

    private static void renderCharacterPane(JTextPane characterPane, PatternPane patternPane, ButtonGroup buttonGroup) {
        try {
            int caret = characterPane.getCaretPosition();
            String text = characterPane.getText().replaceAll("\\n\\r", "\\r");
            characterPane.setText(text);
            characterPane.setCaretPosition(caret);
            Colorizer.renderCharacterPane(characterPane, patternPane.getText(), buttonGroup.getSelection().getActionCommand(), flags);
            patternPane.setBorder(Constants.WHITE_BORDER);
        } catch (Exception e) {
            patternPane.setBorder(Constants.RED_BORDER);

        }
    }

}
