package se.tink.backend.aggregation.nxgen.controllers.authentication.utils;

import com.google.common.base.Strings;
import java.util.UUID;

public class StrongAuthenticationState {
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private static final String UUID_TINK_TAG = "feed";

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
            // Beware! Some financial institutes have limitations on
            // the state parameter. Known limitations:
            // - SDC only allow UUID.
            // - Barclays only allow ^(?!\s)(a-zA-Z0-9-_){1,255})$

            // Todo: Use se.tink.libraries.uuid.UUIDUtils once the repositories are merged.
            String uuid = UUID.randomUUID().toString();
            return uuid.substring(0, uuid.length() - UUID_TINK_TAG.length()) + UUID_TINK_TAG;
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
