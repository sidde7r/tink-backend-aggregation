package se.tink.backend.export.helper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportStringFormatter {

    public static String format(String string) {
        List<String> splitString = splitByUnderscore(string)
                .flatMap(ExportStringFormatter::splitBySpace)
                .collect(Collectors.toList());
        return splitString.stream().map(s -> capFirstLetter(s.toLowerCase()))
                .collect(Collectors.joining(" "));
    }

    private static Stream<String> splitByUnderscore(String string){
        return Arrays.stream(string.split("_"));
    }

    private static Stream<String> splitBySpace(String string){
        return Arrays.stream(string.split(" "));
    }

    private static String capFirstLetter(String string){
        if (string.length() == 0) {
            return string;
        }

        if (string.length() == 1) {
            return String.valueOf(Character.toUpperCase(string.charAt(0)));
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
