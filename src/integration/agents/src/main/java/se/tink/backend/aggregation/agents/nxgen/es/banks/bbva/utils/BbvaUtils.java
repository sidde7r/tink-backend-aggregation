package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils;

import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;

public class BbvaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaUtils.class);

    public static Optional<String> splitUtlGetKey(String toSplit) {
        try {
            return new URIBuilder(toSplit)
                    .getQueryParams()
                    .stream()
                    .filter(p -> BbvaConstants.Query.PAGINATION_OFFSET.equals(p.getName()))
                    .map(p -> Optional.of(p.getValue()))
                    .findAny()
                    .orElseThrow(
                            () -> {
                                throw new IllegalStateException(
                                        "Trying to get next pagination key when none exists");
                            });
        } catch (URISyntaxException e) {
            //TODO: Seems we never should hit this one
            throw new IllegalArgumentException("Could not parse next page key in: " + toSplit);
        }
    }
}
