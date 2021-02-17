package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.fetcher.identitydata;

import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.DkIdentityData;

@AllArgsConstructor
public class DanskeBankDKIdentityFetcher implements IdentityDataFetcher {
    private PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return persistentStorage
                .get(Storage.IDENTITY_INFO, FinalizeAuthenticationResponse.class)
                .map(
                        user ->
                                DkIdentityData.of(
                                        user.getUserInfo().getFirstName(),
                                        user.getUserInfo().getLastname(),
                                        user.getUserId()))
                .orElseThrow(NoSuchElementException::new);
    }
}
