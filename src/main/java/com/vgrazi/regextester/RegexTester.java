package com.vgrazi.regextester;

import com.vgrazi.regextester.action.Renderer;
import com.vgrazi.regextester.component.Constants;
import com.vgrazi.regextester.component.PatternPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

import static com.vgrazi.regextester.component.Constants.DEFAULT_LABEL_FONT;
import static com.vgrazi.regextester.component.Constants.DEFAULT_PANE_FONT;
import static com.vgrazi.regextester.component.Constants.DEFAULT_BUTTON_FONT;

public class RegexTester {

    private static int flags;
    // Create a 16×16 transparent image
    private static BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    // Create a new blank cursor
    private static Cursor blankCursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");


    public void launch() {
        JFrame frame = new JFrame("Regex Tester Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Apply it to the frame (or any component)
        frame.getContentPane().setCursor(blankCursor);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setCursor(blankCursor);
        splitPane.setDividerLocation(50);

        JPanel topPanel = new JPanel();
        topPanel.setCursor(blankCursor);

        topPanel.setLayout(new BorderLayout());

        JLabel patternJlabel = new JLabel("Pattern  ");
        patternJlabel.setCursor(blankCursor);
        patternJlabel.setVerticalAlignment(SwingConstants.TOP);
        patternJlabel.setBackground(Color.LIGHT_GRAY);
        patternJlabel.setFont(DEFAULT_LABEL_FONT);
        patternJlabel.setCursor(blankCursor);

        topPanel.add(patternJlabel, BorderLayout.WEST);

        splitPane.add(topPanel);

        JSplitPane bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(.8d);
        bottomPane.setCursor(blankCursor);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setCursor(blankCursor);
        JTextPane characterPane = new JTextPane();
        characterPane.setCursor(blankCursor);
        formatCharacterPane(characterPane);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        JSplitPane auxiliarySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        auxiliarySplit.setCursor(blankCursor);
        auxiliarySplit.setDividerLocation(40);
        JTextPane auxiliaryPane = new JTextPane();
        auxiliaryPane.setCursor(blankCursor);
        auxiliaryPane.setEditable(false);
        auxiliaryPane.setFont(DEFAULT_PANE_FONT);

        JTextPane replacementPane = new JTextPane();
        replacementPane.setCursor(blankCursor);
        replacementPane.setFont(DEFAULT_PANE_FONT);
        auxiliarySplit.add(replacementPane);
        auxiliarySplit.add(auxiliaryPane);
        PatternPane patternPane = new PatternPane(characterPane, auxiliaryPane, replacementPane);
        patternPane.setCursor(blankCursor);
        ButtonGroup buttonGroup = new ButtonGroup();
        JPanel buttonPanel = createButtonPanel(patternPane, characterPane, auxiliaryPane, replacementPane, buttonGroup);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        formatPatternPane(patternPane);

        topPanel.add(patternPane, BorderLayout.CENTER);

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
            }
        };
        characterPane.addKeyListener(keyListener);
        patternPane.addKeyListener(keyListener);
        replacementPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                Pattern pattern = Pattern.compile(patternPane.getText(), flags);
                Renderer.renderCharacterPane(characterPane, auxiliaryPane, pattern, replacementPane, patternPane.getText(), buttonGroup.getSelection().getActionCommand());
            }
        });

        // even though there is a focus listener, we still need a mouse listener, in case the pattern pane already has
        // focus, when user clicks the mouse
        patternPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                StyledDocument styledDocument = patternPane.getStyledDocument();
                try {
                    Renderer.renderCharacterPane(characterPane, auxiliaryPane,
                            Pattern.compile(styledDocument.getText(0, styledDocument.getLength())),
                            replacementPane, characterPane.getText(), buttonGroup.getSelection().getActionCommand());
                    Renderer.resetColor(patternPane.getStyledDocument());
                    patternPane.renderMatchingGroupsInCharacterPane();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });


        bottomPane.add(bottomPanel);
        bottomPane.add(auxiliarySplit);
        splitPane.add(bottomPane);

        frame.getContentPane().add(splitPane);

        frame.setBounds(10, 100, 1200, 600);
        frame.setVisible(true);
    }

    private static void formatPatternPane(PatternPane patternPane) {
        patternPane.setFont(Constants.DEFAULT_PANE_FONT);
        patternPane.setForeground(Constants.FONT_COLOR);
        patternPane.setBackground(Constants.BACKGROUND_COLOR);
    }

    private static void formatCharacterPane(JTextPane characterPane) {
        characterPane.setForeground(Constants.FONT_COLOR);
        characterPane.setBackground(Constants.BACKGROUND_COLOR);
        characterPane.setFont(Constants.DEFAULT_PANE_FONT);

        characterPane.getStyledDocument().addStyle("highlights", null);
    }

    private static JPanel createButtonPanel(PatternPane patternPane, JTextPane characterPane, JTextPane auxiliaryPane, JTextPane replacementPane, ButtonGroup buttonGroup) {
        JRadioButton matchButton = new JRadioButton("Matches");
        JRadioButton lookingAtButton = new JRadioButton("Looking at");
        JRadioButton splitButton = new JRadioButton("Split");
        JRadioButton replaceAllButton = new JRadioButton("Replace all");
        JRadioButton replaceFirstButton = new JRadioButton("Replace first");
        JRadioButton findButton = new JRadioButton("Find");
        findButton.setSelected(true);

        findButton.setActionCommand("find");
        matchButton.setActionCommand("matches");
        lookingAtButton.setActionCommand("looking-at");
        splitButton.setActionCommand("split");
        replaceAllButton.setActionCommand("replace-all");
        replaceFirstButton.setActionCommand("replace-first");

        buttonGroup.add(findButton);
        buttonGroup.add(matchButton);
        buttonGroup.add(lookingAtButton);
        buttonGroup.add(splitButton);
        buttonGroup.add(replaceAllButton);
        buttonGroup.add(replaceFirstButton);

        findButton.setFont(DEFAULT_BUTTON_FONT);
        matchButton.setFont(DEFAULT_BUTTON_FONT);
        lookingAtButton.setFont(DEFAULT_BUTTON_FONT);
        splitButton.setFont(DEFAULT_BUTTON_FONT);
        replaceAllButton.setFont(DEFAULT_BUTTON_FONT);
        replaceFirstButton.setFont(DEFAULT_BUTTON_FONT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setCursor(blankCursor);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(findButton);
        buttonPanel.add(matchButton);
        buttonPanel.add(lookingAtButton);
        buttonPanel.add(splitButton);
        buttonPanel.add(replaceAllButton);
        buttonPanel.add(replaceFirstButton);
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

        caseButton.setFont(DEFAULT_BUTTON_FONT);
        commentsButton.setFont(DEFAULT_BUTTON_FONT);
        dotallButton.setFont(DEFAULT_BUTTON_FONT);
        literalButton.setFont(DEFAULT_BUTTON_FONT);
        multilineButton.setFont(DEFAULT_BUTTON_FONT);

        ActionListener recalcFlagListener = e -> {
            flags = recalculateFlags(caseButton, commentsButton, dotallButton, literalButton, multilineButton);
            renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
            patternPane.setFlags(flags);
        };
        caseButton.addActionListener(recalcFlagListener);
        commentsButton.addActionListener(recalcFlagListener);
        dotallButton.addActionListener(recalcFlagListener);
        literalButton.addActionListener(recalcFlagListener);
        multilineButton.addActionListener(recalcFlagListener);
        ActionListener actionListener = e -> renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
        findButton.addActionListener(actionListener);
        lookingAtButton.addActionListener(actionListener);
        matchButton.addActionListener(actionListener);
        replaceAllButton.addActionListener(actionListener);
        replaceFirstButton.addActionListener(actionListener);
        splitButton.addActionListener(actionListener);
        return buttonPanel;
    }

    private static int recalculateFlags(JCheckBox caseButton, JCheckBox commentsButton, JCheckBox dotallButton, JCheckBox literalButton, JCheckBox multilineButton) {
        int flags = 0;
        flags |= caseButton.isSelected() ? Pattern.CASE_INSENSITIVE : 0;
        flags |= commentsButton.isSelected() ? Pattern.COMMENTS : 0;
        flags |= dotallButton.isSelected() ? Pattern.DOTALL : 0;
        flags |= literalButton.isSelected() ? Pattern.LITERAL : 0;
        flags |= multilineButton.isSelected() ? Pattern.MULTILINE : 0;
        return flags;
    }

    private static void renderCharacterPane(JTextPane characterPane, PatternPane patternPane, JTextPane auxiliaryPane, JTextPane replacementPane, ButtonGroup buttonGroup) {
        try {
            String regex = patternPane.getText();
            Pattern pattern = Pattern.compile(regex, flags);
            Renderer.renderCharacterPane(characterPane, auxiliaryPane, pattern, replacementPane, regex, buttonGroup.getSelection().getActionCommand());
            patternPane.setBorder(Constants.WHITE_BORDER);
        } catch (Exception e) {
            System.out.println("RegexTester.renderCharacterPane " + e);
            Renderer.resetColor(characterPane.getStyledDocument());

            patternPane.setBorder(Constants.RED_BORDER);

        }
    }
}
