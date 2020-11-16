package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class KbcAuthenticationData {

    private String consentId;
    private String codeVerifier;
}
