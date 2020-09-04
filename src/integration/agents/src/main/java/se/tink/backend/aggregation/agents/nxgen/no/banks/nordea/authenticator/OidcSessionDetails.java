package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OidcSessionDetails {
    private String action;
    private String error;
    private String errorCode;
    private String errorMessage;
    private String merchantReference;
    private String sessionId;

    // At this point in authentication flow, we expect AUTHENTICATE state, and no errors.
    // Deployment of agent shown that there is also another good state - CHOOSE, but we do not know
    // how to handle it correctly.
    // Anything but other of these two states would suggest that we did something wrong already.
    // https://tinkab.atlassian.net/browse/ITE-1315
    public boolean isInProperState() {
        return Arrays.asList("AUTHENTICATE", "CHOOSE").contains(action)
                && "false".equalsIgnoreCase(error);
    }
}
