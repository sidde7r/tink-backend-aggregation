package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher;

import java.util.NoSuchElementException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class SdcSeIdentityDataFetcher {

    private final SdcSessionStorage sdcSessionStorage;
    private final Credentials credentials;

    public SdcSeIdentityDataFetcher(SdcSessionStorage sdcSessionStorage, Credentials credentials) {
        this.sdcSessionStorage = sdcSessionStorage;
        this.credentials = credentials;
    }

    private static IdentityData reduce(IdentityData id1, IdentityData id2) {
        throw new IllegalStateException(
                String.format("Found multiple identities: %s %s", id1, id2));
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        return sdcSessionStorage.getAgreements().stream()
                .map(SessionStorageAgreement::getName)
                .distinct()
                .map(name -> SeIdentityData.of(name, credentials.getField(Key.USERNAME)))
                .reduce(SdcSeIdentityDataFetcher::reduce)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
