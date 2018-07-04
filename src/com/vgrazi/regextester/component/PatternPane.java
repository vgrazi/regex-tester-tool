package com.vgrazi.regextester.component;

import com.vgrazi.regextester.action.Calculator;
import com.vgrazi.regextester.action.Colorizer;
import com.vgrazi.regextester.action.UnmatchedLeftParenException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PatternPane extends JTextPane {
    public PatternPane() {
        setFont(new Font("Courier New", Font.PLAIN, 20));
        getStyledDocument().addStyle("fontSize", null);

        addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        int textLength = getText().length();
                        Colorizer.colorize(getStyledDocument(), new ColorRange(Color.white, 0, textLength));
                        ColorRange[] colorRanges = null;
                        try {
                            colorRanges = Calculator.parseGroupRanges(getText(), Color.cyan);
                            // if the cursor is at the start or end of any range, colorize that one
                            int position = getCaretPosition();
                            for(ColorRange range: colorRanges) {
                                if(range.getStart() == position -1|| range.getEnd() == position-1) {
                                    Colorizer.colorize(getStyledDocument(), range);
                                    break;
                                }
                            }
                        } catch (UnmatchedLeftParenException e1) {
                            ColorRange range = new ColorRange(Color.red, e1.getPosition(), textLength - 1);
                            Colorizer.colorize(getStyledDocument(), range);
                        }
                    }
                });
    }
}
