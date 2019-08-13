package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

public class StrongAuthenticationState {
    private static final Logger logger = LoggerFactory.getLogger(StrongAuthenticationState.class);
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";

    private final String state;

    public StrongAuthenticationState(String appUriId) {
        // Use `appUriId` as state or, if not set, generate a random one.
        //
        // The strong authentication state carries information to main
        // regarding where to redirect the user.
        // That is why we must prefer using the appUriId (which is random)
        // as the state.
        // Last resort is to randomize it ourselves.
        this.state = generateState(appUriId);
    }

    private String generateState(String appUriId) {
        if (Strings.isNullOrEmpty(appUriId)) {
            logger.warn("appUriId is empty!");
            return RandomUtils.generateRandomHexEncoded(8);
        }

        return appUriId;
    }

    public String getState() {
        return this.state;
    }

    public String getSupplementalKey() {
        return String.format(UNIQUE_PREFIX_TPCB, this.state);
    }
}
