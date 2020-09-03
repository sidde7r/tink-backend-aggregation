package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.identitydata;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils.SoapHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class CaisseEpargneIdentityDataFetcher implements IdentityDataFetcher {

    private final CaisseEpargneStorage caisseEpargneStorage;

    @Override
    public IdentityData fetchIdentityData() {
        String finalAuthResponse = caisseEpargneStorage.getFinalAuthResponse();
        return SoapHelper.getIdentityData(finalAuthResponse);
    }
}
