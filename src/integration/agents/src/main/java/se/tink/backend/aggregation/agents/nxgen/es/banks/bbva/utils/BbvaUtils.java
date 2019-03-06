package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;

public class BbvaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaUtils.class);

    private static final int RANDOM_HEX_LENGTH = 64;
    private static final Pattern NIE_PATTERN = Pattern.compile("(?i)^[XY].+[A-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$");
    private static final Pattern ES_PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{4}[0-9]{6}$");

    public static Optional<String> splitUtlGetKey(String toSplit) {
        try {
            return new URIBuilder(toSplit)
                    .getQueryParams().stream()
                            .filter(p -> BbvaConstants.QueryKeys.PAGINATION_OFFSET.equals(p.getName()))
                            .map(p -> Optional.of(p.getValue()))
                            .findAny()
                            .orElseThrow(
                                    () -> {
                                        throw new IllegalStateException(
                                                "Trying to get next pagination key when none exists");
                                    });
        } catch (URISyntaxException e) {
            // TODO: Seems we never should hit this one
            throw new IllegalArgumentException("Could not parse next page key in: " + toSplit);
        }
    }

    public static String generateRandomHex() {
        Random random = new Random();
        byte[] randBytes = new byte[RANDOM_HEX_LENGTH];
        random.nextBytes(randBytes);

        return Hex.encodeHexString(randBytes).toUpperCase();
    }

    // Non NIE/PASSPORT usernames must be prepended with '0' (based on ambassador credentials) while
    // NIE/PASSPORT
    // usernames are passed along as-is.
    public static String formatUsername(String username) {
        if (NIE_PATTERN.matcher(username).matches()
                || PASSPORT_PATTERN.matcher(username).matches()
                || ES_PASSPORT_PATTERN.matcher(username).matches()) {
            return username;
        }

        return String.format("0%s", username);
    }
}
