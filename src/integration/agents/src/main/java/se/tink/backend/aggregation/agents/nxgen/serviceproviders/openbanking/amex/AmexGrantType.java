package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AmexGrantType {
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    REVOKE("revoke");

    private final String type;
}
