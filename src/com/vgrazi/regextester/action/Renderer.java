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

/**
 * Given a set of ColorRange instances, applies the supplied color to those ranges
 */
public class Renderer {

    /**
     * Styles the docs by coloring all of the color ranges. If resetColor is true, first sets the default color
     * @param doc
     * @param resetColor
     * @param colorRanges
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
     * @param characterPane
     * @param groupIndex
     * @param regex
     * @param flags
     */
    public static void renderMatchingGroupsHighlightsInCharacterPane(JTextPane characterPane, int groupIndex, String regex, int flags) {
        List<ColorRange> list = Calculator.calculateMatchingGroups(characterPane, groupIndex, regex, flags);
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), false, list.toArray(ranges));
    }

    /**
     * Renders the character pane, according to the selected radio button
     * @param characterPane
     * @param auxiliaryPane
     * @param replacementPane
     * @param regex
     * @param actionCommand
     * @param flags
     */
    public static void renderCharacterPane(JTextPane characterPane, final JTextPane auxiliaryPane, JTextPane replacementPane, String regex, String actionCommand, int flags) {
        replacementPane.setBorder(Constants.WHITE_BORDER);
        List<ColorRange> list = new ArrayList<>();
        String text = characterPane.getText();
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(text);
        if(!actionCommand.equals(previousCommand)) {
            replacementPane.setText("");
            auxiliaryPane.setText("");
            previousCommand = actionCommand;
        }
        switch(actionCommand) {
            case "find":
                list = Calculator.processFindCommand(matcher);
                break;
            case "looking-at":
                list = Calculator.processLookingAtCommand(matcher);
                break;
            case "matches":
                list = Calculator.processMatchesCommand(matcher);
                break;
            case "split":
                list = Calculator.processSplitCommand(auxiliaryPane, text, pattern, matcher);
                break;
            case "replace":
                list = Calculator.processReplaceAllCommand(auxiliaryPane, replacementPane, matcher);
                break;
        }
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), true, list.toArray(ranges));
    }

    /**
     * Highlights all matches in the character pane for the specified group name
     * @param flags
     * @param groupName
     * @param patternPane
     * @param characterPane
     * @param auxiliaryPane
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
