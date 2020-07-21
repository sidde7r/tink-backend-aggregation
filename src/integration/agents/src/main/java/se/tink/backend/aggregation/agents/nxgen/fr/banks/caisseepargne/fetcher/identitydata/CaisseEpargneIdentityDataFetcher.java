package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils.SoapHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.identitydata.IdentityData;

public class CaisseEpargneIdentityDataFetcher implements IdentityDataFetcher {

    private final Storage instanceStorage;

    public CaisseEpargneIdentityDataFetcher(Storage instanceStorage) {
        this.instanceStorage = instanceStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String finalAuthResponse = instanceStorage.get(StorageKeys.FINAL_AUTH_RESPONSE);
        return SoapHelper.getIdentityData(finalAuthResponse);
    }
}
