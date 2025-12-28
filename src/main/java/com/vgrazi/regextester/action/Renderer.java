package com.vgrazi.regextester.action;

import com.vgrazi.regextester.component.ColorRange;
import com.vgrazi.regextester.component.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import static com.vgrazi.regextester.component.Constants.BACKGROUND_COLOR;
import static com.vgrazi.regextester.component.PatternPane.extractNamedGroups;

/**
 * Given a set of ColorRange instances, applies the supplied color to those ranges
 */
public class Renderer {

    /**
     * Styles the docs by coloring all of the color ranges. If resetColor is true, first sets the default color
     * @param doc the pane
     * @param resetColor true to reset the colors before rendering
     * @param colorRanges the color ranges to apply
     */
    public static void colorize(StyledDocument doc, boolean resetColor, ColorRange... colorRanges) {
        Style font = doc.getStyle("highlights");
        if (resetColor) {
            StyleConstants.setBackground(font, BACKGROUND_COLOR);
            doc.setCharacterAttributes(0, doc.getLength(), font, true);
        }
        Arrays.stream(colorRanges).forEach(colorRange ->
        {
            StyleConstants.setBackground(font, colorRange.getColor());
            if(colorRange.isInclusive()) {
                doc.setCharacterAttributes(colorRange.getStart(), colorRange.getSize(), font, true);
            }
            else {
                doc.setCharacterAttributes(colorRange.getStart(), 1, font, true);
                doc.setCharacterAttributes(colorRange.getEnd(), 1, font, true);
            }
        });
    }
    private static String previousCommand;

    /**
     * When the caret has highlighted a group, we want to select the matching groups in the character pane
     * @param characterPane the JPanel containing the target text
     * @param groupIndex the index of the group
     * @param regex the regex pattern
     * @param flags the selected flags
     */
    public static void renderMatchingGroupsHighlightsInCharacterPane(JTextPane characterPane, int groupIndex, String regex, int flags) {
        List<ColorRange> list = Calculator.calculateMatchingGroups(characterPane, groupIndex, regex, flags);
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), false, list.toArray(ranges));
    }

    /**
     * Renders the character pane, according to the selected radio button
     * @param characterPane the JPanel containing the target text
     * @param auxiliaryPane the pane where we render splits
     * @param pattern the compiled Pattern instance
     * @param replacementPane the JPanel where we render placements
     * @param regex the pattern regex
     * @param actionCommand the action command representing the clicked button
     */
    public static void renderCharacterPane(JTextPane characterPane, final JTextPane auxiliaryPane, Pattern pattern, JTextPane replacementPane, String regex, String actionCommand) {
        replacementPane.setBorder(Constants.WHITE_BORDER);
        List<ColorRange> list = new ArrayList<>();
        
        // Clear auxiliary pane if pattern is empty
        if (regex == null || regex.trim().isEmpty()) {
            auxiliaryPane.setText("");
            return;
        }
        
        String text = characterPane.getText();
        if(text.indexOf("\r\n") > 0) {
            text = text.replaceAll("\r\n", "\r");
            characterPane.setText(text);
        }
        Matcher matcher = pattern.matcher(text);
        if(!actionCommand.equals(previousCommand)) {
            auxiliaryPane.setText("");
            previousCommand = actionCommand;
        }
        list = switch (actionCommand) {
            case "find" ->
//                extractNamedGroups(regex);
//                list = Calculator.processReplaceAllCommand(matcher, text, auxiliaryPane, replacementPane);
//                list = Calculator.processFindCommand(matcher, text);
                    Calculator.processFinderCommand(matcher, text, auxiliaryPane);
            case "looking-at" ->
//                extractNamedGroups(regex);
                    Calculator.processLookingAtCommand(matcher, text);
            case "matches" -> {
                extractNamedGroups(regex);
                yield Calculator.processMatchesCommand(matcher, text, auxiliaryPane);
            }
            case "split" ->
//                auxiliaryPane.removeAll();
//                auxiliaryPane.doLayout();
                    Calculator.processSplitCommand(matcher, text, auxiliaryPane, pattern);
            case "replace-all" ->
//                auxiliaryPane.removeAll();
//                auxiliaryPane.doLayout();
                    Calculator.processReplaceAllCommand(matcher, text, auxiliaryPane, replacementPane);
            case "replace-first" ->
//                auxiliaryPane.removeAll();
//                auxiliaryPane.doLayout();
                    Calculator.processReplaceFirstCommand(matcher, text, auxiliaryPane, replacementPane);
            default -> list;
        };
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), true, list.toArray(ranges));
    }

    /**
     * Highlights all matches in the character pane for the specified group name
     * @param flags the selected flags
     * @param groupName the name of the named group
     * @param patternPane  the JPanel containing the regex pattern
     * @param characterPane the JPanel containing the target text
     * @param auxiliaryPane the pane where we render replacements and splits
     */
    public static void renderNamedGroupInCharacterPane(int flags, String groupName, JTextPane patternPane, JTextPane characterPane, JTextPane auxiliaryPane) {
        String regex = patternPane.getText();
        List<ColorRange> list = Calculator.calculateMatchingGroup(characterPane, groupName, regex, flags);
        auxiliaryPane.setText("");
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), true, list.toArray(ranges));
    }

    /**
     * Resets the coloring to none
     */
    public static void resetColor(StyledDocument doc) {
        colorize(doc, true, new ColorRange(BACKGROUND_COLOR, 0, doc.getLength()));
    }
}
