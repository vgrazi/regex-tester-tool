package com.vgrazi.regextester.action;

import com.vgrazi.regextester.component.ColorRange;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Calculates all of the groups in a Regex pattern, calculates all of the matches in a string, etc
 */
public class Calculator {
    public static ColorRange[] parseGroupRanges(String regex, Color color) throws UnmatchedLeftParenException
    {
        boolean newLine = false;
        List<ColorRange> list = new ArrayList<>();
        Stack<ColorRange> stack = new Stack<>();
        for(int index = 0; index < regex.length(); index++) {
            char ch = regex.charAt(index);
            // todo: make sure these are not escaped. Ignore
            if(ch == '(') {
                if(!isEscaped(regex, index)) {
                    // for now, set the size == 0. We will fix it when a right paren turns up
                    ColorRange range = new ColorRange(color, index, 0, false);
                    stack.push(range);
                }
            }
            else if(ch == ')') {
                if (!isEscaped(regex, index)) {
                    if(!stack.isEmpty()) {
                        ColorRange colorRange = stack.pop();
                        colorRange.setEnd(index);
                        list.add(colorRange);
                    }
                    else {
                        throw new UnmatchedLeftParenException(index);
                    }
                }
            }
        }
        // find all left and right parens and return as 1 character ranges
        ColorRange[] ranges = new ColorRange[list.size()];
        list.toArray(ranges);
        return ranges;
    }

    /**
     * checks that the previous character is not an escape
     * @param regex
     * @param i
     * @return
     */
    private static boolean isEscaped(String regex, int i) {
        return false;
    }

}
