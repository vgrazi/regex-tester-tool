package com.vgrazi.regextester.action;

import com.vgrazi.regextester.component.ColorRange;
import com.vgrazi.regextester.component.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.vgrazi.regextester.component.Constants.GROUP_COLOR;
import static com.vgrazi.regextester.component.Constants.HIGHLIGHT_COLOR;

/**
 * Calculates all of the groups in a Regex pattern, calculates all of the matches in a string, etc
 */
public class Calculator {

    /**
     * Returns a list of ColorRanges of all capture groups in the supplied regex. These are in order,
     * beginning with capture group 1 in position 0 of the array
     *
     * @param regex the regex pattern
     * @param color the color to render
     * @return the color range for rendering the error
     * @throws UnmatchedLeftParenException if no matching right paren
     */
    public static ColorRange[] parseGroupRanges(String regex, Color color) throws UnmatchedLeftParenException {
        List<ColorRange> list = new ArrayList<>();
        Stack<ColorRange> stack = new Stack<>();
        for (int index = 0; index < regex.length(); index++) {
            char ch = regex.charAt(index);
            if (ch == '(') {
                if (isNotEscaped(regex, index)) {
                    // for now, set the size == 0. We will adjust it as required when a right paren turns up
                    ColorRange colorRange = new ColorRange(color, index, 0, false);
                    stack.push(colorRange);
                    list.add(colorRange);
                }
            } else if (ch == ')') {
                if (isNotEscaped(regex, index)) {
                    if (!stack.isEmpty()) {
                        ColorRange colorRange = stack.pop();
                        colorRange.setEnd(index);
                    } else {
                        throw new UnmatchedLeftParenException(index);
                    }
                }
            }
        }
        // find all left and right parentheses and return as 1 character ranges
        ColorRange[] ranges = new ColorRange[list.size()];
        list.toArray(ranges);
        return ranges;
    }

    /**
     * Give that the user selected the Find radio, calculates the highlight ranges for all matches
     *
     * @param matcher the matcher matching the text and regex
     * @param text    the target string
     * @return the color ranges containing the matching segments
     */
    static List<ColorRange> processFindCommand(Matcher matcher, String text) {
        List<ColorRange> list = new ArrayList<>();
        while (matcher.find()) {
            processCommand(matcher, list, text);
        }
        return list;
    }

    /**
     * Like {@link #processFindCommand}, except displays match groups in auxiliary pabe
     * @param matcher
     * @param text
     * @param auxiliaryPane
     * @return
     */
    static List<ColorRange> processFinderCommand(Matcher matcher, String text, JEditorPane auxiliaryPane) {
        List<ColorRange> list = new ArrayList<>();
        int count = matcher.groupCount();
        StringBuilder groupString = new StringBuilder();
//        while (matcher.find()) {
//            processCommand(matcher, list, text);
//        }
        while (matcher.find()) {
            processCommand(matcher, list, text);
        }
        matcher.reset();
        while (matcher.find()) {
            for (int i = 0; i <= count; i++) {
                String group = matcher.group(i);
                if (group != null) {
//                    System.out.println(group);
                    groupString.append(i).append(". ").append(group).append("\n");
                    if (i == count) {
                        groupString.append("\n");
                    }
                }

            }
        }

        auxiliaryPane.setText(groupString.toString());

        return list;
    }

    /**
     * Give that the user selected the Looking at radio, calculates the highlight range, if any
     *
     * @param matcher the matcher matching the text and regex
     * @param text    the target string
     * @return the color ranges containing the matching segments
     */
    static List<ColorRange> processLookingAtCommand(Matcher matcher, String text) {
        List<ColorRange> list = new ArrayList<>();
        if (matcher.lookingAt()) {
            processCommand(matcher, list, text);
        }
        return list;
    }

    /**
     * Give that the user selected the Matches radio, calculates the highlight ranges for the match, if any
     *
     * @param matcher the matcher matching the text and regex
     * @param text    the target string
     * @return the color ranges containing the matching segments
     */
    static List<ColorRange> processMatchesCommand(Matcher matcher, String text) {
        List<ColorRange> list = new ArrayList<>();
        if (matcher.matches()) {
            processCommand(matcher, list, text);
        }
        return list;
    }

    static List<ColorRange> processMatchesCommand(Matcher matcher, String text, JEditorPane auxiliaryPane) {
        List<ColorRange> list = new ArrayList<>();
        if (matcher.matches()) {
            processCommand(matcher, list, text);
            int count = matcher.groupCount();
            StringBuilder groupString = new StringBuilder();
            for (int i = 0; i <= count; i++) {
                String group = matcher.group(i);
                if (group != null) {
                    groupString.append(i).append(". ").append(group).append("\n");
                }
            }
            auxiliaryPane.setText(groupString.toString());
        } else {
            auxiliaryPane.setText("");
        }
        return list;
    }

    static List<ColorRange> processSplitCommand(Matcher matcher, String text, JTextPane auxiliaryPane, Pattern pattern) {
        List<ColorRange> list = processFindCommand(matcher, text);
        String[] split = pattern.split(text);
        StringBuilder sb = new StringBuilder();
        
        if (!"".equals(text)) {
            System.out.println(Arrays.asList(split));
            for (int i = 0; i < split.length; i++) {
                sb.append(i).append(": ").append(split[i]).append("\n");
            }
        }
        
        auxiliaryPane.setText(sb.toString());
        return list;
    }

    static List<ColorRange> processSplitCommandWithLimit(Matcher matcher, String text, JTextPane auxiliaryPane, Pattern pattern, int limit) {
        List<ColorRange> list = processFindCommand(matcher, text);
        String[] split = pattern.split(text, limit);
        StringBuilder sb = new StringBuilder();
        
        if (!"".equals(text)) {
            System.out.println(Arrays.asList(split));
            for (int i = 0; i < split.length; i++) {
                sb.append(i).append(": ").append(split[i]).append("\n");
            }
        }
        
        auxiliaryPane.setText(sb.toString());
        return list;
    }

    static List<ColorRange> processSplitCommandWithDelimiters(Matcher matcher, String text, JTextPane auxiliaryPane, Pattern pattern, JTextPane replacementPane, String actionCommand) {
        List<ColorRange> list = processFindCommand(matcher, text);

        // Get all the delimiters first
        List<String> delimiters = new ArrayList<>();
        matcher.reset();
        while (matcher.find()) {
            delimiters.add(matcher.group());
        }

        // Split the text, keeping empty trailing strings
        String[] parts = pattern.split(text, -1);

        // Build the output string with interleaved parts and delimiters
        StringBuilder sb = new StringBuilder();
        int index = 0;

        for (int i = 0; i < parts.length; i++) {
            // Add the current part
            sb.append(index++).append(": ").append(parts[i]).append("\n");

            // Add the delimiter that follows this part (if any)
            if (i < delimiters.size()) {
                sb.append(index++).append(": ").append(delimiters.get(i)).append("\n");
            }
        }

        auxiliaryPane.setText(sb.toString());
        return list;
    }
    // Helper method to escape special characters for display
    private static String escapeSpecialChars(String input) {
        if (input == null) return "";
        return input.replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    static List<ColorRange> processReplaceAllCommand(Matcher matcher, String text, JTextPane auxiliaryPanel, JTextPane replacementPane) {
        List<ColorRange> list = processFindCommand(matcher, text);
        SwingUtilities.invokeLater(() -> {
            try {
                String replacement = replacementPane.getText();
                String replaced = matcher.replaceAll(replacement);
                if (!replaced.equals("")) {
                    replaced = replaced.replaceAll("\n", "\n>");
                }
                auxiliaryPanel.setText(replaced);
            } catch (Exception e) {
                auxiliaryPanel.setText("");
                replacementPane.setBorder(Constants.RED_BORDER);
            }
        });
        return list;
    }

    static List<ColorRange> processReplaceFirstCommand(Matcher matcher, String text, JTextPane auxiliaryPanel, JTextPane replacementPane) {
        List<ColorRange> list = processFindCommand(matcher, text);
        SwingUtilities.invokeLater(() -> {
            try {
                String replacement = replacementPane.getText();
                String replaced = matcher.replaceFirst(replacement);
                if (!replaced.equals("")) {
                    replaced = replaced.replaceAll("\n", "\n>");
                }
                auxiliaryPanel.setText(replaced);
            } catch (Exception e) {
                auxiliaryPanel.setText("");
                replacementPane.setBorder(Constants.RED_BORDER);
            }
        });
        return list;
    }

    /**
     * Used by all of the find, match, etc radios, calculates the next range in the matcher, and adds it to the supplied list
     *
     * @param matcher the matcher matching the text and regex
     * @param list    the list of color ranges to render
     * @param text    the target string
     */
    private static void processCommand(Matcher matcher, List<ColorRange> list, String text) {
        int start = matcher.start();
        int end = matcher.end();
        // count the new lines between 0 and start, and subtract those from start
        int startLineCount = Utils.countLines(text.substring(0, start));
        // start and end could be on different lines, so we need a separate count for end
        int endLineCount = Utils.countLines(text.substring(0, end));
        ColorRange range = new ColorRange(HIGHLIGHT_COLOR, start - startLineCount, end - endLineCount - 1, true);
        list.add(range);
    }


    /**
     * Returns a list of all ranges from the character pane that match the group indexed.
     *
     * @param characterPane contains the target text
     * @param groupIndex    the 1-based index of the highlighted group
     * @param regex         the regex pattern
     * @param flags         the selected flags
     * @return the color ranges matching the group index
     * @throws PatternSyntaxException if a regex ccmpilation error
     */
    static List<ColorRange> calculateMatchingGroups(JTextPane characterPane, int groupIndex, String regex, int flags) throws PatternSyntaxException {
        List<ColorRange> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(characterPane.getText());
        extractRangeByGroupIndexed(groupIndex, list, matcher);
        return list;
    }

    /**
     * Returns a list of all ranges from the character pane that match the group indexed.
     *
     * @param characterPane contains the target text
     * @param groupName     the name of the named group
     * @param regex         the regex pattern
     * @param flags         the selected flags
     * @return the list of color ranges matching the named group
     * @throws PatternSyntaxException if a regex compilation error
     */
    static List<ColorRange> calculateMatchingGroup(JTextPane characterPane, String groupName, String regex, int flags) {

        Pattern pattern = Pattern.compile(regex, flags);
        Matcher matcher = pattern.matcher(characterPane.getText());

        // do the finds first, then the groups, so that the group highlighting will overlay the find highlights
        List<ColorRange> list = Calculator.processFindCommand(matcher, characterPane.getText());
        matcher = pattern.matcher(characterPane.getText());

        extractRangeByNamedGroup(groupName, matcher, list);
        return list;
    }

    private static void extractRangeByGroupIndexed(int groupIndex, List<ColorRange> list, Matcher matcher) {
        while (matcher.find()) {
            int start = matcher.start(groupIndex);
            int end = matcher.end(groupIndex) - 1;
            addInclusiveRangeToList(list, start, end);
        }
    }

    /**
     * Helper method, mostly because IntelliJ was confusingly reporting these
     * methods as code duplicates, even though only these two calls were
     *
     * @param list  the list of color ranges
     * @param start the start of the range
     * @param end   the end of the range
     */
    private static void addInclusiveRangeToList(List<ColorRange> list, int start, int end) {
        ColorRange range = new ColorRange(GROUP_COLOR, start, end, true);
        list.add(range);
    }

    private static void extractRangeByNamedGroup(String groupName, Matcher matcher, List<ColorRange> list) {
        try {
            while (matcher.find()) {
                int start = matcher.start(groupName);
                int end = matcher.end(groupName) - 1;
                addInclusiveRangeToList(list, start, end);
            }
        } catch (Exception e) {
            System.out.println("Calculator.calculateMatchingGroup " + e);
        }
    }

    /**
     * checks that the character at the supplied index is not escaped
     */
    private static boolean isNotEscaped(String regex, int index) {
        // count the number of consecutive \ characters preceding the supplied index
        // if this is odd return true
        int count = 0;
        for (int i = index - 1; i >= 0; i++) {
            if (regex.charAt(i) == '\\') {
                count++;
            } else {
                break;
            }
        }
        return count % 2 == 0;
    }
}
