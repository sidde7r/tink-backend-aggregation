package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;

public class StrongAuthenticationState {
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private static final Logger log = LoggerFactory.getLogger(StrongAuthenticationState.class);

    private final String state;
    private final RandomValueGenerator randomValueGenerator;

    @Deprecated
    public StrongAuthenticationState(String appUriId) {
        this(appUriId, new RandomValueGeneratorImpl());
    }

    public StrongAuthenticationState(String appUriId, RandomValueGenerator randomValueGenerator) {
        // Use `appUriId` as state or, if not set, generate a random one.
        //
        // The strong authentication state carries information to main
        // regarding where to redirect the user.
        // That is why we must prefer using the appUriId (which is random)
        // as the state.
        // Last resort is to randomize it ourselves.
        this.randomValueGenerator = randomValueGenerator;
        this.state = generateState(appUriId);
    }

    private String generateState(String appUriId) {
        if (Strings.isNullOrEmpty(appUriId)) {
            log.warn("The appUriId should not be null or empty");
            // Beware! Some financial institutes have limitations on
            // the state parameter. Known limitations:
            // - SDC only allow UUID.
            // - Barclays only allow ^(?!\s)(a-zA-Z0-9-_){1,255})$
            return this.randomValueGenerator.generateUuidWithTinkTag();
        }

        return appUriId;
    }

    public String getState() {
        return this.state;
    }

    public String getSupplementalKey() {
        return String.format(UNIQUE_PREFIX_TPCB, this.state);
    }

    public static String formatSupplementalKey(String key) {
        return String.format(UNIQUE_PREFIX_TPCB, key);
    }
}
