package gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public enum Palette {
    PRIMARY,
    SECONDARY,
    BACKGROUND;

    private final Map<String, Color> themeColors = new HashMap<>();
    private static String currentePalette = "originalPlus";

    static {
        PRIMARY.themeColors.put("original", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("original", new Color(255, 248, 231)); //ivory
        BACKGROUND.themeColors.put("original", new Color(70, 130, 180)); //steel blue
        
        PRIMARY.themeColors.put("originalPlus", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("originalPlus", new Color(253, 94, 83)); //sunset orange
        BACKGROUND.themeColors.put("originalPlus", new Color(70, 130, 180)); //steel blue
        
        PRIMARY.themeColors.put("hotdog", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("hotdog", new Color(218, 165, 32)); //goldenrod
        BACKGROUND.themeColors.put("hotdog", new Color(150, 0, 24)); //carmine red
        
        PRIMARY.themeColors.put("tan", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("tan", new Color(95, 158, 160)); //cadet blue
        BACKGROUND.themeColors.put("tan", new Color(183, 65, 14)); //rust
        
        PRIMARY.themeColors.put("constantinople", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("constantinople", new Color(115, 112, 0)); //bronze yellow
        BACKGROUND.themeColors.put("constantinople", new Color(128, 0, 32)); //burgundy
        
        PRIMARY.themeColors.put("asia", new Color(255, 248, 231)); //ivory
        SECONDARY.themeColors.put("asia", new Color(152, 251, 152)); //mint green
        BACKGROUND.themeColors.put("asia", new Color(0, 168, 107)); //jade green

        PRIMARY.themeColors.put("night", new Color(95, 95, 95)); //pebble
        SECONDARY.themeColors.put("night", new Color(89, 75, 91)); //graphite
        BACKGROUND.themeColors.put("night", new Color(35, 32, 37)); //charcoal
    }

    public Color getColor(String themeName) {
        return themeColors.getOrDefault(themeName, themeColors.get("original"));
    }

    public void setThemeColor(String themeName, int red, int green, int blue) {
        final int limit = 255;
        final int zero = 0;
        
        red = Math.max(zero, Math.min(limit, red));
        green = Math.max(zero, Math.min(limit, green));
        blue = Math.max(zero, Math.min(limit, blue));

        themeColors.put(themeName, new Color(red, green, blue));
    }

    public static void setCurrentePalette(String currentePalette) {
        Palette.currentePalette = currentePalette;
    }

    public static String getCurrentePalette() {
        return currentePalette;
    }
    
    
}