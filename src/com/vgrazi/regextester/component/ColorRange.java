package com.vgrazi.regextester.component;

import java.awt.*;

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

    private void setSize(int size) {
        this.end = size - start-1;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("Range: [%d, %d] (%d)", start, getEnd(), getSize());
    }
}
