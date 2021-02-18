package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.CompleteEnrollResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.DkIdentityData;

@RequiredArgsConstructor
public class BankdataIdentitydataFetcher implements IdentityDataFetcher {
    private final PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return persistentStorage
                .get(StorageKeys.IDENTITY_DATA, CompleteEnrollResponse.class)
                .map(user -> DkIdentityData.of(user.getCustomerName(), user.getCprNo()))
                .orElseThrow(NoSuchElementException::new);
    }
}
