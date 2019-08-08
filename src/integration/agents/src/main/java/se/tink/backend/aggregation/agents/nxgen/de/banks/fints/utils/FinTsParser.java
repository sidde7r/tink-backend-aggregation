package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.libraries.strings.StringUtils;

public final class FinTsParser {

    private static final Pattern RE_ENCRYPTED_SEGMENTS =
            Pattern.compile("HNVSD:\\d+:\\d+\\+@\\d+@(.+)(?<!\\?)''");

    private static final Pattern RE_SEGMENT_NAME = Pattern.compile("^(.+?(?<!\\?)):");
    private static final Pattern RE_SEGMENT_NUMBER =
            Pattern.compile("^.+?(?<!\\?):([0-9]+(?<!\\?)):");
    private static final Pattern RE_SEGMENT_VERSION =
            Pattern.compile("^.+?(?<!\\?):[0-9]+(?<!\\?):([0-9]+)");

    private static final Pattern RE_SEGMENT_DATA =
            Pattern.compile("^.+?(?<!\\?):[0-9]+?(?<!\\?):[0-9:]+?(?<!\\?)\\+([\\s\\S]*(?<!\\?))$");

    private static final Pattern RE_SPLIT_DATA_GROUP = Pattern.compile("(?<!\\?)\\+");
    private static final Pattern RE_SPLIT_DATA_ELEMENT = Pattern.compile("(?<!\\?):");

    private static final Pattern RE_UNWRAP = Pattern.compile("HNVSD:\\d+:\\d+\\+@\\d+@(.+)''");
    private static final Pattern RE_SEGMENTS = Pattern.compile("'(?=[A-Z]{4,}:\\d|')");
    private static final Pattern RE_SYSTEMID = Pattern.compile("HISYN:\\d+:\\d+:\\d+\\+(.+)");
    private static final Pattern RE_GLOBALFEEDBACK = Pattern.compile("(HIRMG.+?')");
    private static final Pattern RE_TANMECH = Pattern.compile("\\d{3}");

    private static final Pattern RE_SUPPORTED_SEGMENT = Pattern.compile("^([A-Z]{5}):1$");

    // MT 940 related
    private static final Pattern RE_MT940_DATA = Pattern.compile("@\\d*@\\s*([\\s\\S]*)");
    private static final Pattern RE_MT940_AMOUNT =
            Pattern.compile("(^\\d{6})(\\d{4})?(D|C|RC|RD)\\D?(\\d*,\\d*)N.*");

    // MT 535 related
    private static final Pattern RE_MT535_DATA = Pattern.compile("(:16R:*?[\\s\\S]*?:16S:.*)");

    public static String[] splitSegments(String data) {
        return RE_SEGMENTS.split(data);
    }

    public static String unwrapSegment(String data) {
        Matcher unwrappedData = RE_UNWRAP.matcher(data);
        if (unwrappedData.find()) {
            return unwrappedData.group(1);
        } else {
            return data;
        }
    }

    public static String getStatus(String response) {
        Matcher hirmg = RE_GLOBALFEEDBACK.matcher(response);
        if (hirmg.find()) {
            return hirmg.group(1);
        } else {
            return response;
        }
    }

    public static String getMT940Content(String hikaz) {
        Matcher m = RE_MT940_DATA.matcher(hikaz);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new IllegalStateException("Illegal format for hikaz segment");
        }
    }

    public static double getMT940Amount(String tag61) {
        Matcher m = RE_MT940_AMOUNT.matcher(tag61);
        int factor = -1;
        if (m.find()) {
            if (m.group(3).contains("C")) {
                factor = 1;
            }
            return factor * StringUtils.parseAmount(m.group(4));
        } else {
            throw new IllegalStateException("Illegal format for tag 61");
        }
    }

    public static List<String> getMT535Content(String hiwpd) {
        List<String> matches = new ArrayList<String>();
        Matcher m = RE_MT535_DATA.matcher(hiwpd);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    public static List<String> getSupportedSegments(List<String> hiupd) {
        List<String> supportedSegments = new ArrayList<>();
        for (String message : hiupd) {
            Matcher m = RE_SUPPORTED_SEGMENT.matcher(message);
            if (m.find()) {
                supportedSegments.add(m.group(1));
            }
        }
        return supportedSegments;
    }

    public static String getBankName(String hibpa) {
        List<String> segments = getSegmentDataGroups(hibpa);
        if (segments.size() > 3) {
            return segments.get(3);
        } else {
            return "";
        }
    }

    private static String extractEncryptedSegments(String message) {
        Matcher m = RE_ENCRYPTED_SEGMENTS.matcher(message);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new IllegalStateException("Could not find encrypted message");
        }
    }

    private static String getSegmentType(String segment) {
        Matcher m = RE_SEGMENT_NAME.matcher(segment);
        if (!m.find()) {
            throw new IllegalStateException();
        }
        return m.group(1);
    }

    public static String getSystemId(String segment) {
        Matcher m = RE_SYSTEMID.matcher(segment);
        if (!m.find()) {
            throw new IllegalStateException("Could not find system id");
        }
        return m.group(1);
    }

    public static List<String> getTanMech(String message) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = RE_TANMECH.matcher(message);
        if (m.find()) {
            String group = m.group();
            if (!"999".equals(group)) {
                allMatches.add(group);
            }
        }
        return allMatches;
    }

    private static int getSegmentNumber(String segment) {
        Matcher m = RE_SEGMENT_NUMBER.matcher(segment);
        if (!m.find()) {
            throw new IllegalStateException();
        }
        return Integer.valueOf(m.group(1));
    }

    private static int getSegmentVersion(String segment) {
        Matcher m = RE_SEGMENT_VERSION.matcher(segment);
        if (!m.find()) {
            throw new IllegalStateException();
        }
        return Integer.valueOf(m.group(1));
    }

    public static List<String> getSegmentDataGroups(String segment) {
        Matcher m = RE_SEGMENT_DATA.matcher(segment);
        if (!m.find()) {
            return new ArrayList<>();
        }
        String data = m.group(0);
        return Arrays.stream(RE_SPLIT_DATA_GROUP.split(data)).collect(Collectors.toList());
    }

    public static List<String> getDataGroupElements(String dataGroup) {
        return Arrays.stream(RE_SPLIT_DATA_ELEMENT.split(dataGroup)).collect(Collectors.toList());
    }
}
