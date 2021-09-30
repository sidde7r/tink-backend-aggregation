package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UniversoAuthenticator extends Xs2aDevelopersAuthenticatorHelper {

    public UniversoAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials) {
        super(apiClient, persistentStorage, configuration, localDateTimeSource, credentials);
    }

    @Override
    protected AccessEntity getAccessEntity() {
        return AccessEntity.builder().allPsd2(new AccessType("all-accounts")).build();
    }
}
