package com.vgrazi.regextester.action;

public class Utils {
    /**
     * This method counts all of the line feeds in the supplied string
     * For multi line strings, the regex tester is counting the line breaks
     * as a character, but the regex parser does not. So we need to adjust.
     * Therefore we must count the number of newlines between the start and
     * current character position.
     * To do so, we take the relevant substring and count all line feeds
     * @param s the target string to count
     * @return the number of line feeds in the string
     */
    static int countLines(String s) {
        int count = (int) s.chars().filter(c -> c == '\n').count();
        return count;
    }
}
