package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class SocieteGeneraleIdentityDataFetcher implements IdentityDataFetcher {

    private final SocieteGeneraleApiClient apiClient;
    private final SocieteGeneraleConfiguration configuration;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;
    private final EidasIdentity eidasIdentity;
    private final EidasProxyConfiguration eidasProxyConfiguration;

    public SocieteGeneraleIdentityDataFetcher(
            SocieteGeneraleApiClient apiClient,
            SocieteGeneraleConfiguration configuration,
            SessionStorage sessionStorage,
            SignatureHeaderProvider signatureHeaderProvider,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.sessionStorage = sessionStorage;
        this.signatureHeaderProvider = signatureHeaderProvider;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                signatureHeaderProvider.buildSignatureHeader(
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
