package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public interface ClientTokenRequester {
    OAuth2Token requestClientToken();
}
