package se.tink.backend.aggregation.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isValidJson(String input) {
        try {
            mapper.readTree(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String prettyJson(Object json) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException je) {
            throw new IllegalStateException(je);
        }
    }

    /**
     * Escape all single backslashes which are not before JSON special chars (\b, \f, \n, \r, \t,
     * \\, \")
     *
     * @param input json string
     * @return escaped string
     */
    public static String escapeNotSpecialSingleBackslashes(String input) {
        final Pattern pattern = Pattern.compile("[^\\\\](\\\\)[^bfnrt\"\\\\]");
        Matcher matcher = pattern.matcher(input);
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            output.append(input, lastIndex, matcher.start(1)).append("\\\\");

            lastIndex = matcher.end(1);
        }
        if (lastIndex < input.length()) {
            output.append(input, lastIndex, input.length());
        }
        return output.toString();
    }
}
