package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.Storage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class OpBankIdentityFetcher {

    public static FetchIdentityDataResponse fetchIdentity(SessionStorage storage) {
        return storage.get(Storage.FULL_NAME, String.class)
                .map(name -> IdentityData.builder().setFullName(name).setDateOfBirth(null).build())
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
