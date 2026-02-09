package com.vgrazi.regextester.component;

import java.awt.*;

/**
 * This is the fundamental class used for containing ranges and colors in the patternPane.
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
        return String.format("Range: [%d, %d] (%d) %s inclusive=%s", start, getEnd(), getSize(), color, inclusive);
    }
}
