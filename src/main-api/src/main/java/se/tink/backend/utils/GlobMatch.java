package se.tink.backend.utils;

import java.util.regex.Pattern;

public class GlobMatch {
    static private final Pattern multipleAsterisks = Pattern.compile("\\*{2,}");
    
    public boolean match(String text, String pattern) {
        // Done to minimize the worst case scenario of multiple consecutive asterisks. 
        pattern = multipleAsterisks.matcher(pattern).replaceAll("*");
        
        return matchCharacter(text, pattern, 0, 0);
    }

    private boolean matchCharacter(String text, String pattern, int patternIndex, int textIndex) {
        if (patternIndex >= pattern.length()) {
            return false;
        }
        
        switch (pattern.charAt(patternIndex)) {
        case '*':
            // * at the end of the pattern will match anything

            if (patternIndex + 1 >= pattern.length()) {
                return true;
            }

            // Probe forward to see if we can get a match

            while (textIndex < text.length()) {
                if (matchCharacter(text, pattern, patternIndex + 1, textIndex)) {
                    return true;
                }
                textIndex++;
            }

            return false;

        default:
            if (textIndex >= text.length()) {
                return false;
            }

            String textChar = text.substring(textIndex, textIndex + 1);
            String patternChar = pattern.substring(patternIndex, patternIndex + 1);

            // Note the match is case insensitive

            if (textChar.compareToIgnoreCase(patternChar) != 0) {
                return false;
            }
        }

        // End of pattern and text?

        if (patternIndex + 1 >= pattern.length() && textIndex + 1 >= text.length()) {
            return true;
        }

        // Go on to match the next character in the pattern

        return matchCharacter(text, pattern, patternIndex + 1, textIndex + 1);
    }
}
