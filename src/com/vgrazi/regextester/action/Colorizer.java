package com.vgrazi.regextester.action;

import com.vgrazi.regextester.component.ColorRange;

import java.awt.*;
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
public class Colorizer {

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

    /**
     * When the caret has highlighted a group, we want to select the matching groups in the character pane
     * @param characterPane
     * @param groupIndex
     * @param regex
     */
    public static void highlightMatchingGroups(JTextPane characterPane, int groupIndex, String regex) {
        List<ColorRange> list = Calculator.calculateMatchingGroups(characterPane, groupIndex, regex);
        ColorRange[] ranges = new ColorRange[list.size()];
        colorize(characterPane.getStyledDocument(), false, list.toArray(ranges));
    }

    public static void renderFindCharacterPane(JTextPane characterPane, String regex, String actionCommand) {
        List<ColorRange> list = new ArrayList<>();
        String text = characterPane.getText();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        switch(actionCommand) {
            case "find":
                list = Calculator.processFindCommand(matcher);
                break;
            case "looking-at":
                list = Calculator.processLookingAtCommand(matcher);
                break;
            case "matches":
                list = Calculator.processMatchesCommand(matcher);
        }
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
