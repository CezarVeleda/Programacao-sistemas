package gui;

import java.awt.Color;

public enum Palette {
    PRIMARY(222, 222, 222),
    SECONDARY(125, 150, 180),
    BACKGROUND(125, 150, 150),;

    private final int red;
    private final int green;
    private final int blue;

    private Palette(int red, int green, int blue) {
        final int limit = 255;
        final int zero = 0;

        red = (red > limit) ? limit : red;
        red = (red < zero) ? zero : red;
        green = (green > limit) ? limit : green;
        green = (green < zero) ? zero : green;
        blue = (blue > limit) ? limit : blue;
        blue = (blue < zero) ? zero : blue;

        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Color getPRIMARYColor() {
        return new Color(PRIMARY.red, PRIMARY.green, PRIMARY.blue);
    }

    public static Color getSECONDARYColor() {
        return new Color(SECONDARY.red, SECONDARY.green, SECONDARY.blue);
    }

    public static Color getBACKGROUNDColor() {
        return new Color(BACKGROUND.red, BACKGROUND.green, BACKGROUND.blue);
    }

}
