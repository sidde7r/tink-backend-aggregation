package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SocieteGeneraleSignatureUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class SocieteGeneraleIdentityDataFetcher implements IdentityDataFetcher {

    private final SocieteGeneraleApiClient apiClient;
    private final SocieteGeneraleConfiguration configuration;
    private final SessionStorage sessionStorage;
    private EidasIdentity eidasIdentity;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public SocieteGeneraleIdentityDataFetcher(
            SocieteGeneraleApiClient apiClient,
            SocieteGeneraleConfiguration configuration,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(
            EidasProxyConfiguration eidasProxyConfiguration, EidasIdentity eidasIdentity) {
        this.eidasIdentity = eidasIdentity;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SocieteGeneraleSignatureUtils.buildSignatureHeader(
                        eidasProxyConfiguration,
                        eidasIdentity,
                        sessionStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN),
                        reqId,
                        configuration);

        EndUserIdentityResponse endUserIdentityResponse =
                apiClient.getEndUserIdentity(signature, reqId);
        return IdentityData.builder()
                .setFullName(endUserIdentityResponse.getConnectedPsu())
                .setDateOfBirth(null)
                .build();
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
