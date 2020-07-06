package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticatorStatusEntity {
    private boolean hasBlockingPages;
    private boolean isBurnt;
    private boolean isDeactivated;
    private boolean isFeedbackAllowed;
    private boolean isOpenBanKingRedirect;
    private boolean strongCookieNeeded;
    private boolean strongCustomerAuthenticationNeeded;
}
