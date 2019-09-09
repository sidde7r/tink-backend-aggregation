package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;

public class RangeRegexUtils {

    public static int fillByNines(int integer, int ninesCount) {
        String str = Integer.toString(integer);
        int length = str.length();
        String result;
        if (length <= ninesCount) {
            result = StringUtils.repeat("9", ninesCount);
        } else {
            result = str.substring(0, length - ninesCount) + StringUtils.repeat("9", ninesCount);
        }
        return Integer.valueOf(result);
    }

    public static int fillByZeros(int integer, int zerosCount) {
        return integer - integer % ((int) Math.pow(10, zerosCount));
    }

    public static List<Integer> splitToRanges(int min, int max) {
        List<Integer> stops = new ArrayList<>();
        stops.add(max);

        int nines_count = 1;
        int stop = fillByNines(min, nines_count);
        while (min <= stop && stop < max) {
            stops.add(stop);
            nines_count += 1;
            stop = fillByNines(min, nines_count);
        }

        int zerosCount = 1;
        stop = fillByZeros(max + 1, zerosCount) - 1;
        while (min < stop && stop <= max) {
            stops.add(stop);
            zerosCount += 1;
            stop = fillByZeros(max + 1, zerosCount) - 1;
        }

        Collections.sort(stops);
        return stops.stream().distinct().collect(Collectors.toList());
    }

    public static String rangeToPattern(int start, int stop) {
        String pattern = "";
        int anyDigitCount = 0;
        String startStr = Integer.toString(start);
        String stopStr = Integer.toString(stop);
        int length = Math.min(startStr.length(), stopStr.length());

        for (int i = 0; i < length; i++) {
            char startDigit = startStr.charAt(i);
            char stopDigit = stopStr.charAt(i);
            if (startDigit == stopDigit) {
                pattern += startDigit;
            } else if (startDigit != '0' || stopDigit != '9') {
                pattern += String.format("[%c-%c]", startDigit, stopDigit);
            } else {
                anyDigitCount += 1;
            }
        }

        if (anyDigitCount != 0) {
            pattern += "[0-9]";
        }
        if (anyDigitCount > 1) {
            pattern += String.format("{%d}", anyDigitCount);
        }
        return pattern;
    }

    public static List<String> splitToPatterns(int min, int max) {
        List<String> subpatterns = new ArrayList<>();
        int start = min;
        for (int stop : splitToRanges(min, max)) {
            subpatterns.add(rangeToPattern(start, stop));
            start = stop + 1;
        }
        return subpatterns;
    }

    public static String regexForRange(int min, int max) {

        final List<String> positiveSubpatterns;
        final List<String> negativeSubpatterns;

        if (min < 0) {
            int min2 = 1;
            if (max < 0) {
                min2 = Math.abs(max);
            }
            int max2 = Math.abs(min);
            negativeSubpatterns = splitToPatterns(min2, max2);
            min = 0;
        } else {
            negativeSubpatterns = new ArrayList<>();
        }

        if (max >= 0) {
            positiveSubpatterns = splitToPatterns(min, max);
        } else {
            positiveSubpatterns = new ArrayList<>();
        }

        List<String> negativeOnlySubpatterns =
                negativeSubpatterns.stream()
                        .filter(subpattern -> !positiveSubpatterns.contains(subpattern))
                        .map(subpattern -> "-" + subpattern)
                        .collect(Collectors.toList());
        List<String> positiveOnlySubpatterns =
                positiveSubpatterns.stream()
                        .filter(subpattern -> !negativeSubpatterns.contains(subpattern))
                        .collect(Collectors.toList());
        List<String> intersectedSubpatterns =
                negativeSubpatterns.stream()
                        .filter(subpattern -> positiveSubpatterns.contains(subpattern))
                        .map(subpattern -> "-?" + subpattern)
                        .collect(Collectors.toList());

        String pattern =
                Stream.of(negativeOnlySubpatterns, intersectedSubpatterns, positiveOnlySubpatterns)
                        .flatMap(list -> list.stream())
                        .collect(Collectors.joining("|"));

        return String.format("^(%s)$", pattern);
    }
}
