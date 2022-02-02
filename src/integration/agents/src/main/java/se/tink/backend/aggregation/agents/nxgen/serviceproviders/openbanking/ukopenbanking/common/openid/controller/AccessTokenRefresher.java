package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

interface AccessTokenRefresher {
    OAuth2Token refresh(OAuth2Token token) throws SessionException;
}
