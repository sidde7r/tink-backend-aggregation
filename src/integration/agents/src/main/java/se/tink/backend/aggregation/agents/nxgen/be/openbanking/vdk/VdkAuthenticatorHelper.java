package se.tink.backend.aggregation.agents.nxgen.be.openbanking.vdk;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VdkAuthenticatorHelper extends Xs2aDevelopersAuthenticatorHelper {
    private final Credentials credentials;

    public VdkAuthenticatorHelper(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials) {
        super(
                apiClient,
                persistentStorage,
                sessionStorage,
                configuration,
                localDateTimeSource,
                credentials);
        this.credentials = credentials;
    }

    @Override
    protected AccessEntity getAccessEntity() {
        AccountReferenceEntity accountReference =
                AccountReferenceEntity.builder()
                        .currency("EUR")
                        .iban(credentials.getField(Key.IBAN))
                        .build();
        List<AccountReferenceEntity> accountReferences =
                Collections.singletonList(accountReference);
        return AccessEntity.builder()
                .accounts(accountReferences)
                .balances(accountReferences)
                .transactions(accountReferences)
                .build();
    }
}
