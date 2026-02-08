package com.vgrazi.regextester.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public interface Constants {
    Color HIGHLIGHT_COLOR = new Color(170, 232, 252);
    Color GROUP_COLOR = new Color(46, 198, 246);
    Color BACKGROUND_COLOR = Color.WHITE;
    // use a white border rather than null, so that no spacing shift when removing the red border
    Border WHITE_BORDER = BorderFactory.createLineBorder(BACKGROUND_COLOR);
    Border RED_BORDER = BorderFactory.createLineBorder(Color.red);
    Font DEFAULT_PANE_FONT = new Font("Courier New", Font.PLAIN, 24);
    Font DEFAULT_LABEL_FONT = new Font("Arial", Font.PLAIN, 16);
    Color FONT_COLOR = Color.BLACK;
}
