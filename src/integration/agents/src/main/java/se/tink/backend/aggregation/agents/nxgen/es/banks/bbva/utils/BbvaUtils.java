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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;

@Slf4j
public class BbvaUtils {
    private static final Random RANDOM = new Random();
    private static final Pattern NIE_PATTERN = Pattern.compile("(?i)^[XY].+[A-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$");
    private static final Pattern ES_PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{4}[0-9]{6}$");

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
                                log.error(
                                        "{}: Could not parse next page key in: {}",
                                        UTILS_SPLIT_GET_PAGINATION_KEY,
                                        uriToSplit,
                                        e))
                .map(URIBuilder::getQueryParams)
                .map(List::ofAll)
                .getOrElse(List::empty)
                .filter(p -> QueryKeys.PAGINATION_OFFSET.equals(p.getName()))
                .flatMap(p -> Option.of(p.getValue()))
                .headOption()
                .onEmpty(
                        () ->
                                log.warn(
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
        final byte[] randBytes = new byte[RANDOM_HEX_LENGTH];
        RANDOM.nextBytes(randBytes);

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
        return Match(username)
                .of(
                        Case($(NIE_PATTERN.asPredicate()), username),
                        Case($(PASSPORT_PATTERN.asPredicate()), username),
                        Case($(ES_PASSPORT_PATTERN.asPredicate()), username),
                        Case($(), String.format("0%s", username)));
    }
}
