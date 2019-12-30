package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.util.UUID;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class BnpParibasIdentityDataFetcher implements IdentityDataFetcher {

    private final BnpParibasApiBaseClient apiClient;
    private final SessionStorage sessionStorage;
    private final BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;

    public BnpParibasIdentityDataFetcher(
            BnpParibasApiBaseClient apiClient,
            SessionStorage sessionStorage,
            BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.bnpParibasSignatureHeaderProvider = bnpParibasSignatureHeaderProvider;
    }

    public void setEidasProxyConfiguration(
            EidasProxyConfiguration eidasProxyConfiguration, EidasIdentity eidasIdentity) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String reqId = UUID.randomUUID().toString();
        String signature =
                bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        eidasProxyConfiguration,
                        eidasIdentity,
                        sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN),
                        reqId,
                        apiClient.getBnpParibasConfiguration());
        final EndUserIdentityResponse endUserIdentityResponse =
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
