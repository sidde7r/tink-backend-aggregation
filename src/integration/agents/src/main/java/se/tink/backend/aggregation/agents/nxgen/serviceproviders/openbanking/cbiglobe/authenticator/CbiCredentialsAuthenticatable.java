package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsDefinition;

public interface CbiCredentialsAuthenticatable {

    String getUpdatePsuAuthenticationLink();

    PsuCredentialsDefinition getPsuCredentials();
}
