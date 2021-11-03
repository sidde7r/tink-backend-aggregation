package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@AllArgsConstructor
@Builder
public class IngAuthenticationInputData {

    @Getter private final IngUserAuthenticationData userAuthenticationData;
    @Getter private final StrongAuthenticationState strongAuthenticationState;
}
