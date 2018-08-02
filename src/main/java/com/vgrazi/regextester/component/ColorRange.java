package com.vgrazi.regextester.component;

import java.awt.*;

/**
 * This is the fundamental class used for containing ranges and colors.
 * start is the position of the first character in the range, end is the last character (inclusive) and size is calculated as end-start+1
 * If inclusive is true, that tells the renderer
 * to color the entire range, whereas if it is fault, it only colors the first and last characters
 */
public class ColorRange {
    private final Color color;
    private final int start;
    private int end;
    private boolean inclusive;

    public ColorRange(Color color, int start, int end) {
        this(color, start, end, true);
    }
    public ColorRange(Color color, int start, int end, boolean inclusive) {
        this.color = color;
        this.start = start;
        this.end = end;
        this.inclusive = inclusive;
    }

    public int getStart() {
        return start;
    }

    public int getSize() {
        return end-start + 1;
    }

    public Color getColor() {
        return color;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
//        Color color1 = new Color(46,198,246);
//        Color color2 = new Color(255,255,255);
//        Color color3 = new Color(170,232, 252);
//        String colorName;
//        if(color.equals(color1))
//        {
//            colorName = "Dark blue";
//        }
//        else if(color.equals(color2))
//        {
//            colorName = "white";
//        }
//        else if(color.equals(color3))
//        {
//            colorName= "Light blue";
//        }
//        else colorName = "unknown";
        return String.format("Range: [%d, %d] (%d) %s inclusive=%s", start, getEnd(), getSize(), color, inclusive);
    }
}
