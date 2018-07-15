package com.vgrazi.regextester.action;

public class UnmatchedLeftParenException extends IllegalArgumentException {
    private int position;

    public UnmatchedLeftParenException(int position) {
        super();
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
