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

/**
 * Given a set of ColorRange instances, applies the supplied color to those ranges
 */
public class Colorizer {

    public static void colorize(StyledDocument doc, ColorRange... colorRanges) {
        Style font = doc.getStyle("fontSize");
        StyleConstants.setBackground(font, Color.white);
        doc.setCharacterAttributes(0, doc.getLength(), font, true);

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

    public static void renderFindCharacterPane(JTextPane characterPane, String regex, String actionCommand) {
        List<ColorRange> list = new ArrayList<>();
        String text = characterPane.getText();
        try {
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
            colorize(characterPane.getStyledDocument(), list.toArray(ranges));

        } catch (Exception e) {
            System.out.println("parse exception");
            //disregard, the pattern is not valid
            // todo:  add a visual indicating that the pattern is invalid
        }
    }

    /**
     * Resets the coloring to none
     */
    public static void resetColor(StyledDocument doc) {
        colorize(doc, new ColorRange(Color.white, 0, doc.getLength()));
    }
}
