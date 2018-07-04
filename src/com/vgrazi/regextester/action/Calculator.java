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
            if(ch == '(') {
                if(!isEscaped(regex, index)) {
                    // for now, set the size == 0. We will adjust it as required when a right paren turns up
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
     * checks that the character at the supplied index is not escaped
     */
    private static boolean isEscaped(String regex, int index) {
        // count the number of consecutive \ characters preceding the supplied index
        // if this is odd return true
        int count = 0;
        for(int i = index -1; i>=0; i++){
            if(regex.charAt(i) == '\\') {
                count++;
            }
            else{
                break;
            }
        }
        return count %2 == 1;
    }

}
