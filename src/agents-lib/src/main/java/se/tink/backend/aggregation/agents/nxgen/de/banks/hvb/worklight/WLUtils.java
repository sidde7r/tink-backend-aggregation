package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class WLUtils {
    private WLUtils() {
        throw new AssertionError();
    }

    public static <T> T encasedJsonToEntity(final HttpResponse response, final Class<T> entityClass) {
        final String responseString = response.getBody(String.class);
        return SerializationUtils.deserializeFromString(extractJson(responseString), entityClass);
    }

    /**
     * The received JSON is enclosed within a C-style comment to make our lives harder.
     */
    private static String extractJson(final String responseBody) {
        final Matcher matcher = WLConstants.Regex.ENCLOSED_JSON.matcher(responseBody.trim());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException(
                    String.format("Could not extract JSON from response body: %s", responseBody));
        }
    }
}
