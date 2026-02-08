package com.vgrazi.regextester;

import com.vgrazi.regextester.action.Renderer;
import com.vgrazi.regextester.component.Constants;
import com.vgrazi.regextester.component.PatternPane;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
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
    private final static JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private static int flags;
    // Create a 16×16 transparent image
    private static BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    // Create a new blank cursor
    private static Cursor blankCursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

    // Flag to track cursor visibility
    private static boolean cursorVisible = true;

    // Method to toggle cursor visibility
    private static void toggleCursorVisibility(Component component) {
        cursorVisible = !cursorVisible;
        Cursor cursor = cursorVisible ? Cursor.getDefaultCursor() : blankCursor;
        setCursorRecursively(component, cursor);
    }

    // Helper method to set cursor recursively on a component and its children
    private static void setCursorRecursively(Component component, Cursor cursor) {
        component.setCursor(cursor);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setCursorRecursively(child, cursor);
            }
        }
    }


    public void launch() {
        JFrame frame = new JFrame("Regex Test Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Add F1 and F2 key bindings to switch focus
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // F1 - Focus on input pane
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "focusInput");
        actionMap.put("focusInput", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                characterPane.requestFocusInWindow();
            }
        });

        // F2 - Focus on pattern pane
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "focusPattern");
        actionMap.put("focusPattern", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patternPane.requestFocusInWindow();
            }
        });

        // F3 - Focus on replacement pane
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "focusReplacement");
        actionMap.put("focusReplacement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replacementPane.requestFocusInWindow();
            }
        });

        // F4 - Clear all panes
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "clearPanes");
        actionMap.put("clearPanes", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patternPane.setText("");
                characterPane.setText("");
                replacementPane.setText("");
                // Focus back to the pattern pane after clearing
                patternPane.requestFocusInWindow();
            }
        });

        // F6 - Toggle cursor
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "toggleCursor");
        actionMap.put("toggleCursor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleCursorVisibility(frame.getContentPane());
            }
        });

        // F10 - Toggle help
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "showHelp");
        ButtonGroup buttonGroup = new ButtonGroup();

        actionMap.put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!showingHelp) {
                    String helpText = """
                                    F10 - Show Help
                                    F1 - Enter Pattern
                                    F2 - Enter Text
                                    F3 - Enter replacement
                                    F4 - Clear
                                    F5 - Toggle cursor visibility
                            """;
                    characterPane.setText(helpText);
                    showingHelp = true;
                } else {
                    showingHelp = false;
                    characterPane.setText("");
                    // Trigger a re-render of the character pane
                    renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
                }
            }
        });

        // Apply it to the frame (or any component)
        frame.getContentPane().setCursor(cursorVisible ? Cursor.getDefaultCursor() : blankCursor);
        // Install UI so we can modify the divider
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void setCursor(Cursor cursor) {
                        // Force the divider to always use the blank cursor
                        Cursor cursorToSet = cursorVisible ? Cursor.getDefaultCursor() : blankCursor;
                        super.setCursor(cursorToSet);
                    }
                };
            }
        });
        splitPane.setCursor(blankCursor);
        splitPane.setResizeWeight(0.2); // Allow pattern pane to be resizable
        splitPane.setDividerLocation(0.2); // Give pattern pane 20% of height initially

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
        // Install UI so we can modify the divider
        bottomPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void setCursor(Cursor cursor) {
                        // Force the divider to always use the blank cursor
                        Cursor cursorToSet = cursorVisible ? Cursor.getDefaultCursor() : blankCursor;
                        super.setCursor(cursorToSet);
                    }
                };
            }
        });
        bottomPane.setCursor(blankCursor);
        splitPane.setDividerLocation(.8d);
        bottomPane.setCursor(blankCursor);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setCursor(blankCursor);
        characterPane = new JTextPane();
        characterPane.setCursor(blankCursor);
        formatCharacterPane(characterPane);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        auxiliarySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        auxiliarySplit.setCursor(blankCursor);
        auxiliarySplit.setDividerLocation(40);
        auxiliaryPane = new JTextPane();
        auxiliaryPane.setEditable(false);
        auxiliaryPane.setFont(DEFAULT_PANE_FONT);
        
        replacementPane = new JTextPane();
        replacementPane.setCursor(blankCursor);
        replacementPane.setFont(DEFAULT_PANE_FONT);
        
        // Create panel for replacement pane with label
        replacementPanel = new JPanel();
        replacementPanel.setLayout(new BorderLayout());
        replacementPanel.setCursor(blankCursor);
        replacementLabel = new JLabel("Replacement  ");
        replacementLabel.setCursor(blankCursor);
        replacementLabel.setVerticalAlignment(SwingConstants.TOP);
        replacementLabel.setBackground(Color.LIGHT_GRAY);
        replacementLabel.setFont(DEFAULT_LABEL_FONT);
        replacementPanel.add(replacementLabel, BorderLayout.WEST);
        replacementPanel.add(replacementPane, BorderLayout.CENTER);
        
        // Create panel for auxiliary pane with label
        JPanel auxiliaryPanel = new JPanel();
        auxiliaryPanel.setLayout(new BorderLayout());
        auxiliaryPanel.setCursor(blankCursor);
        JLabel auxiliaryLabel = new JLabel("Results  ");
        auxiliaryLabel.setCursor(blankCursor);
        auxiliaryLabel.setVerticalAlignment(SwingConstants.TOP);
        auxiliaryLabel.setBackground(Color.LIGHT_GRAY);
        auxiliaryLabel.setFont(DEFAULT_LABEL_FONT);
        auxiliaryPanel.add(auxiliaryLabel, BorderLayout.WEST);
        auxiliaryPanel.add(auxiliaryPane, BorderLayout.CENTER);
        
        auxiliarySplit.add(replacementPanel);
        auxiliarySplit.add(auxiliaryPanel);
        
        // Hide replacement panel initially since "find" is selected by default
        replacementPanel.setVisible(false);
        
        patternPane = new PatternPane(characterPane, auxiliaryPane, replacementPane);
        patternPane.setCharacterPaneRenderer(() -> renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup));
        patternPane.setCursor(blankCursor);
        JPanel buttonPanel = createButtonPanel(patternPane, characterPane, auxiliaryPane, replacementPane, buttonGroup);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        formatPatternPane(patternPane);

        // Add document listener to trigger height adjustment when text changes
        patternPane.getStyledDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(RegexTester::adjustPatternPaneHeight);
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(RegexTester::adjustPatternPaneHeight);
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(RegexTester::adjustPatternPaneHeight);
            }
        });

        // Add component listener to automatically adjust pattern pane height based on content
        patternPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustPatternPaneHeight();
            }
        });

        topPanel.add(patternPane, BorderLayout.CENTER);

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.isActionKey() || e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                    return;
                }
                renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
                Pattern pattern = Pattern.compile(patternPane.getText(), flags);
                Renderer.renderCharacterPane(characterPane, auxiliaryPane, pattern, replacementPane, patternPane.getText(), buttonGroup.getSelection().getActionCommand());
            }
        };

        characterPane.addKeyListener(keyListener);
        patternPane.addKeyListener(keyListener);
        replacementPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Only re-render for split-with-delimiters when limit changes
                String actionCommand = buttonGroup.getSelection().getActionCommand();
                if ("split-with-delimiters".equals(actionCommand)) {
                    Pattern pattern = Pattern.compile(patternPane.getText(), flags);
                    Renderer.renderCharacterPane(characterPane, auxiliaryPane, pattern, replacementPane, patternPane.getText(), actionCommand);
                }
            }
        });

        // even though there is a focus listener, we still need a mouse listener, in case the pattern pane already has
        // focus, when user clicks the mouse
        MouseAdapter mouseListener = new MouseAdapter() {
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
        };
        patternPane.addMouseListener(mouseListener);
//        replacementPane.addMouseListener(mouseListener);
        replacementPane.addKeyListener(keyListener);


        bottomPane.add(bottomPanel);
        bottomPane.add(auxiliarySplit);
        splitPane.add(bottomPane);

        frame.getContentPane().add(splitPane);

        frame.setBounds(10, 100, 1200, 600);
        frame.setVisible(true);
        
        // Set divider location after frame is visible to get correct dimensions
        SwingUtilities.invokeLater(() -> {
            int totalWidth = bottomPane.getWidth();
            if (totalWidth > 0) {
                bottomPane.setDividerLocation((int)(totalWidth * 0.6));
            }
        });

        setCursorRecursively(frame.getContentPane(), cursorVisible ? Cursor.getDefaultCursor() : blankCursor);
    }

    private static void formatPatternPane(PatternPane patternPane) {
        patternPane.setFont(Constants.DEFAULT_PANE_FONT);
        patternPane.setForeground(Constants.FONT_COLOR);
        patternPane.setBackground(Constants.BACKGROUND_COLOR);
        
        // Add mouse wheel listener for font size adjustment
        patternPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                Font currentFont = patternPane.getFont();
                int newSize = currentFont.getSize() - e.getWheelRotation();
                if (newSize >= 8 && newSize <= 72) {  // Limit font size between 8 and 72
                    Font newFont = currentFont.deriveFont((float) newSize);
                    patternPane.setFont(newFont);
                    e.consume();
                }
            }
        });
    }

    private static void formatCharacterPane(JTextPane characterPane) {
        characterPane.setForeground(Constants.FONT_COLOR);
        characterPane.setBackground(Constants.BACKGROUND_COLOR);
        characterPane.setFont(Constants.DEFAULT_PANE_FONT);
        characterPane.getStyledDocument().addStyle("highlights", null);
        
        // Add mouse wheel listener for font size adjustment
        characterPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                Font currentFont = characterPane.getFont();
                int newSize = currentFont.getSize() - e.getWheelRotation();
                if (newSize >= 8 && newSize <= 72) {  // Limit font size between 8 and 72
                    Font newFont = currentFont.deriveFont((float) newSize);
                    characterPane.setFont(newFont);
                    e.consume();
                }
            }
        });
    }

    private static JPanel createButtonPanel(PatternPane patternPane, JTextPane characterPane, JTextPane auxiliaryPane, JTextPane replacementPane, ButtonGroup buttonGroup) {
        JRadioButton matchButton = new JRadioButton("Matches");
        JRadioButton lookingAtButton = new JRadioButton("Looking at");
        JRadioButton splitButton = new JRadioButton("Split");
        JRadioButton splitWithDelimitersButton = new JRadioButton("Split with delimiters");
        JRadioButton replaceAllButton = new JRadioButton("Replace all");
        JRadioButton replaceFirstButton = new JRadioButton("Replace first");
        JRadioButton findButton = new JRadioButton("Find");

        // Set cursor to default for radio buttons
        matchButton.setCursor(Cursor.getDefaultCursor());
        lookingAtButton.setCursor(Cursor.getDefaultCursor());
        splitButton.setCursor(Cursor.getDefaultCursor());
        splitWithDelimitersButton.setCursor(Cursor.getDefaultCursor());
        replaceAllButton.setCursor(Cursor.getDefaultCursor());
        replaceFirstButton.setCursor(Cursor.getDefaultCursor());
        findButton.setCursor(Cursor.getDefaultCursor());
        findButton.setSelected(true);

        findButton.setActionCommand("find");
        matchButton.setActionCommand("matches");
        lookingAtButton.setActionCommand("looking-at");
        splitButton.setActionCommand("split");
        splitWithDelimitersButton.setActionCommand("split-with-delimiters");
        replaceAllButton.setActionCommand("replace-all");
        replaceFirstButton.setActionCommand("replace-first");

        buttonGroup.add(findButton);
        buttonGroup.add(matchButton);
        buttonGroup.add(lookingAtButton);
        buttonGroup.add(splitButton);
        buttonGroup.add(splitWithDelimitersButton);
        buttonGroup.add(replaceAllButton);
        buttonGroup.add(replaceFirstButton);

        findButton.setFont(DEFAULT_BUTTON_FONT);
        matchButton.setFont(DEFAULT_BUTTON_FONT);
        lookingAtButton.setFont(DEFAULT_BUTTON_FONT);
        splitButton.setFont(DEFAULT_BUTTON_FONT);
        splitWithDelimitersButton.setFont(DEFAULT_BUTTON_FONT);
        replaceAllButton.setFont(DEFAULT_BUTTON_FONT);
        replaceFirstButton.setFont(DEFAULT_BUTTON_FONT);

        JCheckBox caseButton = new JCheckBox("Case Insensitive");
        JCheckBox commentsButton = new JCheckBox("Comments");
        JCheckBox dotallButton = new JCheckBox("Dot All");
        JCheckBox literalButton = new JCheckBox("Literal");
        JCheckBox multilineButton = new JCheckBox("Multiline");

        // Set cursor to default for checkboxes
        caseButton.setCursor(Cursor.getDefaultCursor());
        commentsButton.setCursor(Cursor.getDefaultCursor());
        dotallButton.setCursor(Cursor.getDefaultCursor());
        literalButton.setCursor(Cursor.getDefaultCursor());
        multilineButton.setCursor(Cursor.getDefaultCursor());

        caseButton.setFont(DEFAULT_BUTTON_FONT);
        commentsButton.setFont(DEFAULT_BUTTON_FONT);
        dotallButton.setFont(DEFAULT_BUTTON_FONT);
        literalButton.setFont(DEFAULT_BUTTON_FONT);
        multilineButton.setFont(DEFAULT_BUTTON_FONT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setCursor(blankCursor);
        buttonPanel.setMinimumSize(new Dimension(100, 0)); // Allow height to grow when buttons wrap

        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(findButton);
        buttonPanel.add(matchButton);
        buttonPanel.add(lookingAtButton);
        buttonPanel.add(splitButton);
        buttonPanel.add(splitWithDelimitersButton);
        buttonPanel.add(replaceAllButton);
        buttonPanel.add(replaceFirstButton);
        buttonPanel.add(caseButton);
        buttonPanel.add(commentsButton);
        buttonPanel.add(dotallButton);
        buttonPanel.add(literalButton);
        buttonPanel.add(multilineButton);

        // Add component listener to dynamically adjust height based on button layout
        final boolean[] adjustingHeight = {false}; // Guard flag to prevent recursion
        buttonPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (adjustingHeight[0]) return; // Prevent recursive calls
                adjustingHeight[0] = true;
                try {
                    SwingUtilities.invokeLater(() -> {
                        FlowLayout layout = (FlowLayout) buttonPanel.getLayout();
                        int width = buttonPanel.getWidth();
                        if (width <= 0) return; // Skip if not yet initialized
                        
                        // Get actual button height for more accurate calculation
                        int buttonHeight = findButton.getPreferredSize().height;
                        int lineHeight = buttonHeight + layout.getVgap();
                        
                        // Calculate required height based on actual button layout
                        int totalWidth = 0;
                        int maxWidth = width - layout.getHgap() * 2; // Account for margins
                        int linesNeeded = 1;
                        
                        for (Component component : buttonPanel.getComponents()) {
                            int componentWidth = component.getPreferredSize().width + layout.getHgap();
                            if (totalWidth + componentWidth > maxWidth && totalWidth > 0) {
                                linesNeeded++;
                                totalWidth = componentWidth;
                            } else {
                                totalWidth += componentWidth;
                            }
                        }
                        
                        int requiredHeight = linesNeeded * lineHeight + layout.getVgap();
                        Dimension currentSize = buttonPanel.getPreferredSize();
                        if (currentSize.height != requiredHeight) {
                            buttonPanel.setPreferredSize(new Dimension(width, requiredHeight));
                            buttonPanel.revalidate();
                        }
                    });
                } finally {
                    adjustingHeight[0] = false;
                }
            }
        });

        caseButton.setFont(DEFAULT_BUTTON_FONT);
        commentsButton.setFont(DEFAULT_BUTTON_FONT);
        dotallButton.setFont(DEFAULT_BUTTON_FONT);
        literalButton.setFont(DEFAULT_BUTTON_FONT);
        multilineButton.setFont(DEFAULT_BUTTON_FONT);

        ActionListener recalcFlagListener = _ -> {
            flags = recalculateFlags(caseButton, commentsButton, dotallButton, literalButton, multilineButton);
            renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
            patternPane.setFlags(flags);
        };
        caseButton.addActionListener(recalcFlagListener);
        commentsButton.addActionListener(recalcFlagListener);
        dotallButton.addActionListener(recalcFlagListener);
        literalButton.addActionListener(recalcFlagListener);
        multilineButton.addActionListener(recalcFlagListener);
        ActionListener actionListener = _ -> {
            if (updatingVisibility) return; // Prevent recursive calls
            updatingVisibility = true;
            try {
                // Update replacement label and visibility based on selected action
                String actionCommand = buttonGroup.getSelection().getActionCommand();
                if ("split-with-delimiters".equals(actionCommand)) {
                    replacementLabel.setText("Limit  ");
                    replacementPanel.setVisible(true);
                    // Set minimum sizes and divider location
                    replacementPanel.setMinimumSize(new Dimension(0, 40));
                    auxiliaryPane.setMinimumSize(new Dimension(0, 0));
                    auxiliarySplit.setDividerLocation(40);
                } else if ("replace-all".equals(actionCommand) || "replace-first".equals(actionCommand)) {
                    replacementLabel.setText("Replacement  ");
                    replacementPanel.setVisible(true);
                    // Set minimum sizes and divider location
                    replacementPanel.setMinimumSize(new Dimension(0, 40));
                    auxiliaryPane.setMinimumSize(new Dimension(0, 0));
                    auxiliarySplit.setDividerLocation(40);
                } else {
                    replacementLabel.setText("Replacement  ");
                    replacementPanel.setVisible(false);
                    // Set minimum sizes to allow full collapse
                    replacementPanel.setMinimumSize(new Dimension(0, 0));
                    auxiliaryPane.setMinimumSize(new Dimension(0, 0));
                    auxiliarySplit.setDividerLocation(0);
                }
                // Revalidate the split pane to apply visibility changes
                auxiliarySplit.revalidate();
                auxiliarySplit.repaint();
                renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
            } finally {
                updatingVisibility = false;
            }
        };
        findButton.addActionListener(actionListener);
        lookingAtButton.addActionListener(actionListener);
        matchButton.addActionListener(actionListener);
        replaceAllButton.addActionListener(actionListener);
        replaceFirstButton.addActionListener(actionListener);
        splitButton.addActionListener(actionListener);
        splitWithDelimitersButton.addActionListener(actionListener);
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

    private static void adjustPatternPaneHeight() {
        if (adjustingPatternHeight) return; // Prevent recursive calls
        adjustingPatternHeight = true;
        try {
            // Calculate preferred height based on content
            int preferredHeight = patternPane.getPreferredSize().height;
            int totalHeight = splitPane.getHeight();
            
            if (totalHeight > 0 && preferredHeight > 0) {
                // Calculate the divider location needed to show all content
                double neededRatio = (double) preferredHeight / totalHeight;
                // Clamp between 10% and 80% to prevent extreme sizes
                double clampedRatio = Math.max(0.1, Math.min(0.8, neededRatio));
                int neededLocation = (int) (clampedRatio * totalHeight);
                
                // Only update if the location is significantly different
                int currentLocation = (int) splitPane.getDividerLocation();
                if (Math.abs(currentLocation - neededLocation) > 5) {
                    splitPane.setDividerLocation(clampedRatio);
                }
            }
        } finally {
            adjustingPatternHeight = false;
        }
    }

    private static JTextPane characterPane;
    private static PatternPane patternPane;
    private static JTextPane replacementPane;
    private static JTextPane auxiliaryPane;
    private static JLabel replacementLabel;
    private static JPanel replacementPanel;
    private static JSplitPane auxiliarySplit;
    private static boolean showingHelp = false;
    private static boolean updatingVisibility = false;
    private static boolean adjustingPatternHeight = false;

    private static void renderCharacterPane(JTextPane characterPane, PatternPane patternPane, JTextPane auxiliaryPane, JTextPane replacementPane, ButtonGroup buttonGroup) {
        try {
            String regex = patternPane.getText();
            Pattern pattern = Pattern.compile(regex, flags);
            Renderer.renderCharacterPane(characterPane, auxiliaryPane, pattern, replacementPane, regex, buttonGroup.getSelection().getActionCommand());
//            patternPane.setBorder(Constants.WHITE_BORDER);
        } catch (Exception e) {
            System.out.println("RegexTester.renderCharacterPane " + e);
            Renderer.resetColor(characterPane.getStyledDocument());

            patternPane.setBorder(Constants.RED_BORDER);

        }
    }
}
