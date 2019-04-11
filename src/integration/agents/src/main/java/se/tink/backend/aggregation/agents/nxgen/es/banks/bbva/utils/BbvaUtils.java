package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags.UTILS_SPLIT_GET_PAGINATION_KEY;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;

public class BbvaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaUtils.class);

    /**
     * Splits a URI string and gets the pagination key
     *
     * @param uriToSplit
     * @return
     */
    public static Option<String> splitGetPaginationKey(String uriToSplit) {
        return Try.of(() -> new URIBuilder(uriToSplit))
                .onFailure(
                        URISyntaxException.class,
                        e ->
                                LOGGER.error(
                                        "{}: Could not parse next page key in: {}",
                                        UTILS_SPLIT_GET_PAGINATION_KEY,
                                        uriToSplit))
                .map(URIBuilder::getQueryParams)
                .map(List::ofAll)
                .getOrElse(List::empty)
                .filter(p -> QueryKeys.PAGINATION_OFFSET.equals(p.getName()))
                .flatMap(p -> Option.of(p.getValue()))
                .headOption()
                .onEmpty(
                        () ->
                                LOGGER.warn(
                                        "{}: Trying to get next pagination key when none exists",
                                        UTILS_SPLIT_GET_PAGINATION_KEY));
    }

    /**
     * Returns a randomly generated hex string
     *
     * @return
     */
    public static String generateRandomHex() {
        final int RANDOM_HEX_LENGTH = 64;
        final Random random = new Random();
        final byte[] randBytes = new byte[RANDOM_HEX_LENGTH];
        random.nextBytes(randBytes);

        return Hex.encodeHexString(randBytes).toUpperCase();
    }

    /**
     * Returns a BBVA formatted username
     *
     * <p>Non NIE/PASSPORT usernames must be prepended with '0' (based on ambassador credentials)
     * while NIE/PASSPORT usernames are passed along as-is.
     *
     * @param username The username to be formatted
     * @return
     */
    public static String formatUsername(String username) {
        final Pattern NIE_PATTERN = Pattern.compile("(?i)^[XY].+[A-Z]$");
        final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$");
        final Pattern ES_PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{4}[0-9]{6}$");

        return Match(username)
                .of(
                        Case($(NIE_PATTERN.asPredicate()), username),
                        Case($(PASSPORT_PATTERN.asPredicate()), username),
                        Case($(ES_PASSPORT_PATTERN.asPredicate()), username),
                        Case($(), String.format("0%s", username)));
    }
}
