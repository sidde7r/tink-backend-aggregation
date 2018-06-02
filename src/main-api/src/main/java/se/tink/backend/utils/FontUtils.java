package se.tink.backend.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class FontUtils {

    public static class Fonts {

        // Note for reference:
        // This note is to inform the developer that the fonts that are marked with a (x) has had their metadata altered to
        // fix font rendering in SVGs, since the library we use to render SVGs does not support custom font family names.
        // More specifically, the font family and preferred font names have been altered for compatibility with the Android and iOS apps.
        // This change is not intended to exist perpetually, so please remove this when we rewrite the feed so we keep ourselves to CSS and SVG standards.

        public static final Font PROXIMA_NOVA_REGULAR;
        public static final Font PROXIMA_NOVA_SEMIBOLD;
        public static final Font PROXIMA_NOVA_LIGHT;
        public static final Font BROWN_REGULAR;
        public static final Font BROWN_BOLD;
        public static final Font BROWN_LIGHT;
        public static final Font LOTA_REGULAR; // (x)
        public static final Font LOTA_SEMIBOLD; // (x)
        public static final Font LOTA_BOLD; // (x)
        public static final Font TINK_ICONS;

        public static final Font IOS_SYSTEM_SEMIBOLD = new Font("-apple-system", Font.PLAIN, 12).deriveFont(
                Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD));
        public static final Font IOS_SYSTEM_LIGHT = new Font("-apple-system", Font.PLAIN, 12).deriveFont(
                Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_LIGHT));
        public static final Font IOS_SYSTEM_REGULAR = new Font("-apple-system", Font.PLAIN, 12).deriveFont(
                Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR));

        static {
            String userDir = System.getProperty("user.dir");

            try {
                PROXIMA_NOVA_REGULAR = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/ProximaNovaA-Regular.ttf"));
                PROXIMA_NOVA_SEMIBOLD = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/ProximaNovaA-Semibold.ttf"));
                PROXIMA_NOVA_LIGHT = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/ProximaNovaA-Light.ttf"));
                BROWN_REGULAR = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lineto-brown-pro-regular.ttf"));
                BROWN_BOLD = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lineto-brown-pro-bold.ttf"));
                BROWN_LIGHT = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lineto-brown-pro-light.ttf"));
                LOTA_REGULAR = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lota-regular.ttf"));
                LOTA_SEMIBOLD= Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lota-semi-bold.ttf"));
                LOTA_BOLD = Font
                        .createFont(Font.TRUETYPE_FONT, new File(userDir + "/data/fonts/lota-bold.ttf"));
                TINK_ICONS = Font.createFont(Font.TRUETYPE_FONT,
                        new File(userDir + "/data/fonts/tinksymbols-regular-webfont.ttf"));

            } catch (FontFormatException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
