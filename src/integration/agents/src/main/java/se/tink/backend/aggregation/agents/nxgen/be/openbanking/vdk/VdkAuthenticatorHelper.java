package se.tink.backend.aggregation.agents.nxgen.be.openbanking.vdk;

import java.util.Collections;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VdkAuthenticatorHelper extends Xs2aDevelopersAuthenticatorHelper {
    private final Credentials credentials;

    public VdkAuthenticatorHelper(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials) {
        super(apiClient, persistentStorage, configuration, localDateTimeSource, credentials);
        this.credentials = credentials;
    }

    @Override
    protected AccessEntity getAccessEntity() {
        AccessInfoEntity accessInfoEntity =
                new AccessInfoEntity("EUR", credentials.getField(Key.IBAN));
        return new AccessEntity(Collections.singletonList(accessInfoEntity));
    }
}
