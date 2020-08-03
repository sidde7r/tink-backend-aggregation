package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

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

    // At this point in authentication flow, we expect this one state, and no errors. Anything but
    // that would suggest that we did something wrong already.
    public boolean isInProperState() {
        return "AUTHENTICATE".equals(action) && "false".equalsIgnoreCase(error);
    }
}
