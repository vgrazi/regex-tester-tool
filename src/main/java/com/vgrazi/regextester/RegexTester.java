package com.vgrazi.regextester;

import com.vgrazi.regextester.action.Renderer;
import com.vgrazi.regextester.component.Constants;
import com.vgrazi.regextester.component.PatternPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
        JFrame frame = new JFrame("Regex Tester Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
// Set a slower cursor blink rate (in milliseconds)
        int blinkRate = 1000; // Default is usually 500ms, we're making it slightly slower

// Apply to all text components
        UIManager.put("TextArea.caretBlinkRate", blinkRate);
        UIManager.put("TextField.caretBlinkRate", blinkRate);
        UIManager.put("TextPane.caretBlinkRate", blinkRate);
        UIManager.put("EditorPane.caretBlinkRate", blinkRate);
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
// F5 - Clear all input
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "clearAll");
        actionMap.put("clearAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                patternPane.setText("");
                characterPane.setText("");
                auxiliaryPane.setText("");
                replacementPane.setText("");
            }
        });;
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
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
        splitPane.setDividerLocation(50);
        splitPane.setResizeWeight(0.0); // Don't resize the top panel
        splitPane.setContinuousLayout(true); // Enable continuous layout for smoother resizing

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
        bottomPane.setResizeWeight(0.5); // Allow both sides to resize equally
        bottomPane.setContinuousLayout(true); // Enable continuous layout for smoother resizing

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setCursor(blankCursor);
        characterPane = new JTextPane();
        characterPane.setCursor(blankCursor);
        formatCharacterPane(characterPane);
        bottomPanel.add(characterPane, BorderLayout.CENTER);
        auxiliarySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // Install UI so we can modify the divider
        auxiliarySplit.setUI(new BasicSplitPaneUI() {
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
        auxiliarySplit.setCursor(blankCursor);
        auxiliarySplit.setDividerLocation(40);
        auxiliarySplit.setResizeWeight(0.0); // Keep the replacement panel fixed height
        auxiliarySplit.setContinuousLayout(true); // Enable continuous layout for smoother resizing
        auxiliaryPane = new JTextPane();
        auxiliaryPane.setCursor(blankCursor);
        auxiliaryPane.setEditable(false);
        auxiliaryPane.setFont(DEFAULT_PANE_FONT);

        // Create a panel to hold the replacement label and text field with dynamic height
        JPanel replacementContainer = new JPanel(new BorderLayout(5, 2)) {
            @Override
            public Dimension getPreferredSize() {
                if (!isVisible()) {
                    return new Dimension(0, 0);
                }
                Dimension size = super.getPreferredSize();
                size.height = 30; // Fixed height for the replacement panel when visible
                size.width = Integer.MAX_VALUE; // Allow it to expand
                return size;
            }
            
            @Override
            public Dimension getMaximumSize() {
                if (!isVisible()) {
                    return new Dimension(0, 0);
                }
                Dimension size = super.getMaximumSize();
                size.height = 30; // Fixed maximum height when visible
                size.width = Integer.MAX_VALUE; // Allow it to expand
                return size;
            }
        };
        replacementContainer.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Add some padding
        replacementContainer.setCursor(blankCursor);
        
        // Add label to the left of the replacement text field
        replacementLabel = new JLabel("Replacement ");
        replacementLabel.setFont(DEFAULT_LABEL_FONT);
        replacementLabel.setCursor(blankCursor);
        replacementLabel.setVisible(false); // Start hidden
        
        // Create a panel for the label and text field
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(replacementLabel, BorderLayout.WEST);
        
        // Add the replacement text field to the center of the panel
        replacementPane = new JTextPane() {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.height = 26; // Slightly smaller height to fit better
                size.width = 400; // Wider default width
                return size;
            }
            
            @Override
            public Dimension getMinimumSize() {
                Dimension size = super.getMinimumSize();
                size.width = 200; // Minimum width to show enough text
                size.height = 26;
                return size;
            }
            
            @Override
            public Dimension getMaximumSize() {
                Dimension size = super.getMaximumSize();
                size.height = 26; // Fixed height
                return size;
            }
        };
        
        // Add some padding and make it look better
        replacementPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        replacementPane.setCursor(blankCursor);
        replacementPane.setFont(DEFAULT_PANE_FONT);
        
        // Add the text field to the input panel
        inputPanel.add(replacementPane, BorderLayout.CENTER);
        
        // Add the input panel to the container
        replacementContainer.add(inputPanel, BorderLayout.CENTER);
        
        // Add some glue to push everything to the left
        replacementContainer.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        
        // Add the container to the split pane
        auxiliarySplit.add(replacementContainer);
        
        // Initially hide the replacement label
        replacementLabel.setVisible(false);
        auxiliarySplit.add(auxiliaryPane);
        patternPane = new PatternPane(characterPane, auxiliaryPane, replacementPane);
        patternPane.setCharacterPaneRenderer(() -> renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup));
        patternPane.setCursor(blankCursor);
        JPanel buttonPanel = createButtonPanel(patternPane, characterPane, auxiliaryPane, replacementPane, buttonGroup);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        formatPatternPane(patternPane);

        topPanel.add(patternPane, BorderLayout.CENTER);

        KeyAdapter keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.isActionKey() || e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                    return;
                }
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

        // Add title bar and content to frame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        frame.setBounds(10, 100, 1200, 600);
        frame.setVisible(true);

        setCursorRecursively(frame.getContentPane(), cursorVisible ? Cursor.getDefaultCursor() : blankCursor);
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
        // Create all radio buttons
        JRadioButton findButton = new JRadioButton("Find");
        JRadioButton matchButton = new JRadioButton("Matches");
        JRadioButton lookingAtButton = new JRadioButton("Looking at");
        JRadioButton splitButton = new JRadioButton("Split");
        JRadioButton splitWithLimitButton = new JRadioButton("Split with Limit");
        JRadioButton splitWithDelimitersButton = new JRadioButton("Split with Delimiters");
        JRadioButton replaceAllButton = new JRadioButton("Replace all");
        JRadioButton replaceFirstButton = new JRadioButton("Replace first");

        // Set cursor to default for radio buttons
        findButton.setCursor(Cursor.getDefaultCursor());
        matchButton.setCursor(Cursor.getDefaultCursor());
        lookingAtButton.setCursor(Cursor.getDefaultCursor());
        splitButton.setCursor(Cursor.getDefaultCursor());
        splitWithLimitButton.setCursor(Cursor.getDefaultCursor());
        splitWithDelimitersButton.setCursor(Cursor.getDefaultCursor());
        replaceAllButton.setCursor(Cursor.getDefaultCursor());
        replaceFirstButton.setCursor(Cursor.getDefaultCursor());
        findButton.setSelected(true);

        // Set action commands
        findButton.setActionCommand("find");
        matchButton.setActionCommand("matches");
        lookingAtButton.setActionCommand("looking-at");
        splitButton.setActionCommand("split");
        splitWithLimitButton.setActionCommand("split-with-limit");
        splitWithDelimitersButton.setActionCommand("split-with-delimiters");
        replaceAllButton.setActionCommand("replace-all");
        replaceFirstButton.setActionCommand("replace-first");

        // Add to button group
        buttonGroup.add(findButton);
        buttonGroup.add(matchButton);
        buttonGroup.add(lookingAtButton);
        buttonGroup.add(splitButton);
        buttonGroup.add(splitWithLimitButton);
        buttonGroup.add(splitWithDelimitersButton);
        buttonGroup.add(replaceAllButton);
        buttonGroup.add(replaceFirstButton);

        // Set fonts
        findButton.setFont(DEFAULT_BUTTON_FONT);
        matchButton.setFont(DEFAULT_BUTTON_FONT);
        lookingAtButton.setFont(DEFAULT_BUTTON_FONT);
        splitButton.setFont(DEFAULT_BUTTON_FONT);
        splitWithLimitButton.setFont(DEFAULT_BUTTON_FONT);
        splitWithDelimitersButton.setFont(DEFAULT_BUTTON_FONT);
        replaceAllButton.setFont(DEFAULT_BUTTON_FONT);
        replaceFirstButton.setFont(DEFAULT_BUTTON_FONT);

        // Create a panel with FlowLayout that wraps components
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setCursor(blankCursor);
        
        // Create a container panel for the radio buttons with wrapping FlowLayout
        JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        radioButtonPanel.setOpaque(false);
        
        // Add radio buttons to the panel
        radioButtonPanel.add(findButton);
        radioButtonPanel.add(matchButton);
        radioButtonPanel.add(lookingAtButton);
        radioButtonPanel.add(splitButton);
        radioButtonPanel.add(splitWithLimitButton);
        radioButtonPanel.add(splitWithDelimitersButton);
        radioButtonPanel.add(replaceAllButton);
        radioButtonPanel.add(replaceFirstButton);
        
        // Create checkboxes
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
        
        // Create a panel for checkboxes with wrapping FlowLayout
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        checkboxPanel.setOpaque(false);
        checkboxPanel.add(caseButton);
        checkboxPanel.add(commentsButton);
        checkboxPanel.add(dotallButton);
        checkboxPanel.add(literalButton);
        checkboxPanel.add(multilineButton);
        
        // Add both panels to the main button panel
        buttonPanel.add(radioButtonPanel, BorderLayout.NORTH);
        buttonPanel.add(checkboxPanel, BorderLayout.SOUTH);
        
        // Add a component listener to handle resizing
        buttonPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // This will force the FlowLayout to recalculate the layout
                radioButtonPanel.revalidate();
                checkboxPanel.revalidate();
            }
        });

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
        // Create a custom action listener that handles visibility of replacement label
        ActionListener actionListener = e -> {
            String command = buttonGroup.getSelection().getActionCommand();
            
            // Show replacement label for replace operations
            boolean isReplace = command.equals("replace-all") || command.equals("replace-first");
            // Show limit label for split operations
            boolean isSplit = command.startsWith("split-");
            
            // Get the replacement container (parent of the label and text field)
            Container replacementContainer = replacementPane.getParent();
            
            if (isReplace) {
                replacementLabel.setText("Replacement:");
                replacementLabel.setVisible(true);
                replacementPane.setVisible(true);
                replacementContainer.setVisible(true);
            } else if (isSplit) {
                replacementLabel.setText("Limit:");
                replacementLabel.setVisible(true);
                replacementPane.setVisible(true);
                replacementContainer.setVisible(true);
                replacementPane.setText(""); // Clear the replacement pane when switching to split
            } else {
                // Hide the entire container for other operations
                replacementContainer.setVisible(false);
            }

            // Force the split pane to update its layout
            auxiliarySplit.resetToPreferredSizes();
            auxiliarySplit.revalidate();
            auxiliarySplit.repaint();
            
            renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
        };
        // Add action listeners to all radio buttons
        findButton.addActionListener(actionListener);
        matchButton.addActionListener(actionListener);
        lookingAtButton.addActionListener(actionListener);
        splitButton.addActionListener(actionListener);
        splitWithLimitButton.addActionListener(actionListener);  // This line was missing
        splitWithDelimitersButton.addActionListener(actionListener);
        replaceAllButton.addActionListener(actionListener);
        replaceFirstButton.addActionListener(actionListener);
        
        // Add action listeners to radio buttons
        ActionListener radioButtonListener = e -> renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
        findButton.addActionListener(radioButtonListener);
        matchButton.addActionListener(radioButtonListener);
        lookingAtButton.addActionListener(radioButtonListener);
        splitButton.addActionListener(radioButtonListener);
        splitWithLimitButton.addActionListener(radioButtonListener);
        splitWithDelimitersButton.addActionListener(radioButtonListener);
        replaceAllButton.addActionListener(radioButtonListener);
        replaceFirstButton.addActionListener(radioButtonListener);
        
        // Add key listener to replacement pane to update when limit changes
        replacementPane.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            
            private void update() {
                String command = buttonGroup.getSelection().getActionCommand();
                if (command.equals("split-with-limit") || command.equals("split-with-delimiters")) {
                    renderCharacterPane(characterPane, patternPane, auxiliaryPane, replacementPane, buttonGroup);
                }
            }
        });
        
        splitWithDelimitersButton.addActionListener(actionListener);
        
        // Trigger the action listener to set initial visibility
        actionListener.actionPerformed(new ActionEvent(buttonGroup.getSelection().getActionCommand(), ActionEvent.ACTION_PERFORMED, ""));
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

    private static JTextPane characterPane;
    private static PatternPane patternPane;
    private static JTextPane replacementPane;
    private static JLabel replacementLabel; // Made this a class field
    private static JSplitPane auxiliarySplit;
    private static JTextPane auxiliaryPane;
    private static boolean showingHelp = false;

    private static void renderCharacterPane(JTextPane characterPane, PatternPane patternPane, JTextPane auxiliaryPane, JTextPane replacementPane, ButtonGroup buttonGroup) {
        if (showingHelp) {
            return; // Don't update the character pane when help is being shown
        }
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
