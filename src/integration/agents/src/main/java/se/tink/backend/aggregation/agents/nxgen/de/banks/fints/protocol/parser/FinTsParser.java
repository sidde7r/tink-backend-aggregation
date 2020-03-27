package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.exception.FinTsParseException;

public class FinTsParser {

    private static final Pattern ELEMENT_REGEX = Pattern.compile("(.*?(?=(?<!\\?)[:+']))");
    private static final Pattern BINARY_LENGTH_REGEX = Pattern.compile("@(\\d+)@");

    public static final String ESCAPE_CHARACTER = "?";
    public static final String ELEMENT_DELIMITER = ":";
    public static final String ELEMENT_GROUP_DELIMITER = "+";
    public static final String SEGMENT_DELIMITER = "'";
    public static final String BINARY_DELIMITER = "@";

    private static List<RawSegment> rawSegments;

    private static RawSegment currentSegment;
    private static RawGroup currentGroup;

    private static String message;
    private static int position;
    private static Matcher elementMatcher;
    private static Matcher binaryLengthMatcher;

    public static String escape(String string) {
        return string.replace(ESCAPE_CHARACTER, ESCAPE_CHARACTER + ESCAPE_CHARACTER)
                .replace(ELEMENT_DELIMITER, ESCAPE_CHARACTER + ELEMENT_DELIMITER)
                .replace(ELEMENT_GROUP_DELIMITER, ESCAPE_CHARACTER + ELEMENT_GROUP_DELIMITER)
                .replace(SEGMENT_DELIMITER, ESCAPE_CHARACTER + SEGMENT_DELIMITER)
                .replace(BINARY_DELIMITER, ESCAPE_CHARACTER + BINARY_DELIMITER);
    }

    private static String unescape(String string) {
        return string.replace(ESCAPE_CHARACTER + ESCAPE_CHARACTER, ESCAPE_CHARACTER)
                .replace(ESCAPE_CHARACTER + ELEMENT_DELIMITER, ELEMENT_DELIMITER)
                .replace(ESCAPE_CHARACTER + ELEMENT_GROUP_DELIMITER, ELEMENT_GROUP_DELIMITER)
                .replace(ESCAPE_CHARACTER + SEGMENT_DELIMITER, SEGMENT_DELIMITER)
                .replace(ESCAPE_CHARACTER + BINARY_DELIMITER, BINARY_DELIMITER);
    }

    public static List<RawSegment> parse(String rawMessage) {

        rawSegments = new ArrayList<>();
        currentSegment = new RawSegment();
        currentGroup = new RawGroup();

        message = rawMessage;
        position = 0;
        elementMatcher = ELEMENT_REGEX.matcher(message);
        binaryLengthMatcher = BINARY_LENGTH_REGEX.matcher(message);

        parseElement();
        while (position < message.length() - 1) {
            parseDelimiter();
            parseElement();
        }
        parseDelimiter();

        return rawSegments;
    }

    private static void parseElement() {
        if (isBinaryElement()) {
            parseBinaryElement();
        } else {
            parseRegularElement();
        }
    }

    private static boolean isBinaryElement() {
        boolean found = binaryLengthMatcher.find(position);
        return found && binaryLengthMatcher.start() == position;
    }

    private static void parseBinaryElement() {
        int length = Integer.parseInt(binaryLengthMatcher.group(1));
        position = binaryLengthMatcher.end();
        int binaryElementEndPosition = position + length;
        if (binaryElementEndPosition > message.length()) {
            throw new FinTsParseException(
                    "Binary content at position: "
                            + position
                            + " reported length exceeds remaining message length. Binary element would end at: "
                            + binaryElementEndPosition
                            + " , message length: "
                            + message.length());
        }
        currentGroup.add(message.substring(position, position + length));
        position += length;
    }

    private static void parseRegularElement() {
        boolean found = elementMatcher.find(position);
        if (found) {
            String s = elementMatcher.group();
            currentGroup.add(unescape(s));
            position = elementMatcher.end();
        } else {
            throw new FinTsParseException(
                    "Could not find regular element starting at position: " + position);
        }
    }

    private static void parseDelimiter() {
        String delimiter = "" + message.charAt(position);
        switch (delimiter) {
            case ELEMENT_DELIMITER:
                break;
            case ELEMENT_GROUP_DELIMITER:
                currentSegment.addGroup(currentGroup);
                currentGroup = new RawGroup();
                break;
            case SEGMENT_DELIMITER:
                currentSegment.addGroup(currentGroup);
                currentGroup = new RawGroup();
                rawSegments.add(currentSegment);
                currentSegment = new RawSegment();
                break;
            default:
                throw new FinTsParseException(
                        "Unexpected delimiter: --> " + delimiter + " <-- at position: " + position);
        }
        position++;
    }
}
