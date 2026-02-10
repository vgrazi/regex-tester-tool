package com.vgrazi.regextester.component;

import com.vgrazi.regextester.action.Calculator;
import com.vgrazi.regextester.action.Renderer;
import com.vgrazi.regextester.action.UnmatchedLeftParenException;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.vgrazi.regextester.component.Constants.GROUP_COLOR;
import static com.vgrazi.regextester.component.Constants.RED_BORDER;
import static com.vgrazi.regextester.component.Constants.WHITE_BORDER;

/**
 * This is the JTextPane that renders the regex pattern. When user positions the cursor just after a parenthesis, highlights that paren and the paired opening or closing paren
 */
public class PatternPane extends JTextPane {
    /**
     * As we navigate the pattern pane, we update the character pane accordingly. Therefore we need a reference
     */
    private JTextPane characterPane;
    private JTextPane auxiliaryPane;
    private Runnable characterPaneRenderer;
    private int lastHighlightedGroupIndex = -1;
    private int flags;
    private final static Pattern CAPTURE_GROUP_PATTERN = Pattern.compile("\\((\\?([^<]+?))\\)");

    public PatternPane(final JTextPane characterPane, final JTextPane auxiliaryPane, JTextPane replacementPane) {
        this.characterPane = characterPane;
        this.auxiliaryPane = auxiliaryPane;
        getStyledDocument().addStyle("highlights", null);

        // Add a single key typed listener for immediate response
        addKeyListener(new KeyAdapter() {
            private long lastUpdateTime = 0;
            private final long UPDATE_THRESHOLD = 50; // ms between updates

            @Override
            public void keyTyped(KeyEvent e) {
                // Only update if enough time has passed since last update
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > UPDATE_THRESHOLD) {
                    lastUpdateTime = currentTime;
                    SwingUtilities.invokeLater(() -> {
                        renderMatchingGroupsInCharacterPane();
                        if (characterPane != null) {
                            characterPane.repaint();
                        }
                    });
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Handle any immediate key presses if needed
            }
        });

        addCaretListener(new CaretListener() {
            private boolean pending;

            @Override
            public void caretUpdate(CaretEvent e) {
                if (pending) {
                    return;
                }
                pending = true;
                SwingUtilities.invokeLater(() -> {
                    pending = false;
                    renderMatchingGroupsInCharacterPane();
                });
            }
        });

        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Renderer.resetColor(getStyledDocument());
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Update immediately when focus is gained
                renderMatchingGroupsInCharacterPane();
            }
        });

        // Add property change listener to catch text changes
        addPropertyChangeListener("document", evt -> {
            triggerRerender();
        });

        // Add document listener as backup
        getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                triggerRerender();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                triggerRerender();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                // Don't react to style changes
            }
        });
    }

    // Override text modification methods to catch all changes
    @Override
    public void cut() {
        super.cut();
        triggerRerender();
    }

    @Override
    public void paste() {
        super.paste();
        triggerRerender();
    }

    @Override
    public void replaceSelection(String content) {
        super.replaceSelection(content);
        triggerRerender();
    }

    private void triggerRerender() {
        SwingUtilities.invokeLater(() -> {
            renderMatchingGroupsInCharacterPane();
            if (characterPaneRenderer != null) {
                characterPaneRenderer.run();
            }
        });
    }

    /**
     * Renders the matching groups, if any, in the character pane with additional highlighting
     */
    public void renderMatchingGroupsInCharacterPane() {
        String text = getText();
        Renderer.resetColor(getStyledDocument());
        
        // Check for syntax errors first
        try {
            Pattern.compile(text, flags);
            setBorder(WHITE_BORDER);
        } catch (PatternSyntaxException e) {
            setBorder(RED_BORDER);
            return; // Exit early if there's a syntax error
        }
        
        try {
            auxiliaryPane.setLayout(new BoxLayout(auxiliaryPane, BoxLayout.Y_AXIS));
//            List<String> names = extractNamedGroups(text);
//            auxiliaryPane.removeAll();
//            ButtonGroup buttonGroup = new ButtonGroup();
//            names.forEach(name->addRadioButton(name, buttonGroup, auxiliaryPane));
//            auxiliaryPane.doLayout();

            Matcher matcher = CAPTURE_GROUP_PATTERN.matcher(text);
            String replaced = matcher.replaceAll("-$1-");

            // note: gotcha! Syntax like (?i:hot) is not a capture group, even though it captures "hot".
            // Therefore this is working correctly, don't treat that like a special case
            // also, a named capture group (?<name>regex) is  a capture group, so we exclude that from the above pattern
            ColorRange[] colorRanges = Calculator.parseGroupRanges(replaced, GROUP_COLOR);
            // if the cursor is at the start or end of any range, colorize that one
            int position = getCaretPosition();
            int newHighlightedGroupIndex = -1;
            for (int groupIndex = 0; groupIndex < colorRanges.length; groupIndex++) {
                ColorRange range = colorRanges[groupIndex];
                if (range.getStart() == position - 1 || range.getEnd() == position - 1) {
                    newHighlightedGroupIndex = groupIndex;
                    Renderer.colorize(getStyledDocument(), true, range);
                    // group index starts at 1, so add 1 to the list position
                    int finalGroupIndex = groupIndex;
                    Runnable runnable = () -> {
                        try {
                            Renderer.renderMatchingGroupsHighlightsInCharacterPane(characterPane, finalGroupIndex + 1, getText(), flags);
                        } catch (PatternSyntaxException e) {
                            Renderer.resetColor(getStyledDocument());
                            // todo: work on error index
//                            System.out.printf("Error index:%d%n", e.getIndex());
//                            ColorRange errorRange = new ColorRange(Color.red, e.getIndex() -1, e.getIndex() + 1);
//                            Renderer.colorize(getStyledDocument(), true, errorRange);

                            setBorder(RED_BORDER);
                            System.out.println("PatternPane.renderMatchingGroupsInCharacterPane " + e + " index:" + e.getIndex());
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                    break;
                }
            }

            if (newHighlightedGroupIndex == -1) {
                if (lastHighlightedGroupIndex != -1 && characterPaneRenderer != null) {
                    SwingUtilities.invokeLater(characterPaneRenderer);
                }
                lastHighlightedGroupIndex = -1;
            } else {
                lastHighlightedGroupIndex = newHighlightedGroupIndex;
            }
        } catch (UnmatchedLeftParenException e1) {
            ColorRange range = new ColorRange(Color.red, e1.getPosition(), e1.getPosition()+1);
            Renderer.colorize(getStyledDocument(), true, range);
        }
    }

    public void setCharacterPaneRenderer(Runnable characterPaneRenderer) {
        this.characterPaneRenderer = characterPaneRenderer;
    }

    private void addRadioButton(final String name, ButtonGroup buttonGroup, JComponent parent) {
        JRadioButton button = new JRadioButton(name);
        buttonGroup.add(button);
        parent.add(button);
        button.setBackground(Color.white);
        button.addActionListener(actionEvent ->
                {
                    SwingUtilities.invokeLater(() -> Renderer.renderNamedGroupInCharacterPane(flags, name, this, characterPane, auxiliaryPane));
                }
        );
        button.setActionCommand(name);
    }

    public static List<String> extractNamedGroups(String text) {
        List<String> list = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile("\\?<(.+?)>");
            Matcher matcher = pattern.matcher(text);
            while(matcher.find()) {
                list.add(matcher.group(1));
            }

        } catch (Exception e) {
            System.out.println("PatternPane.extractNamedGroups "+e);
            // swallow the error, this is not yet ready to compile
        }
        return list;
    }

    public void setFlags(int flags) {
        this.flags = flags;
        renderMatchingGroupsInCharacterPane();
    }
}
