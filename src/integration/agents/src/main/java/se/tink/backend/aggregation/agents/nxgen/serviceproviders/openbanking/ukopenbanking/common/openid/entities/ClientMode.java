package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;

@Getter
@RequiredArgsConstructor
public enum ClientMode {
    ACCOUNTS(OpenIdConstants.Scopes.ACCOUNTS),
    PAYMENTS(OpenIdConstants.Scopes.PAYMENTS);

    private final String value;
}
