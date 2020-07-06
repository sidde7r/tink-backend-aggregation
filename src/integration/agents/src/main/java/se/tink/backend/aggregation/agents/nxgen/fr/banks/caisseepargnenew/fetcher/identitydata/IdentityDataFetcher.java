package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils.SoapHelper;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataFetcher
        implements se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata
                .IdentityDataFetcher {

    private final Storage instanceStorage;

    public IdentityDataFetcher(Storage instanceStorage) {
        this.instanceStorage = instanceStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String finalAuthResponse = instanceStorage.get(StorageKeys.FINAL_AUTH_RESPONSE);
        return SoapHelper.getIdentityData(finalAuthResponse);
    }
}
