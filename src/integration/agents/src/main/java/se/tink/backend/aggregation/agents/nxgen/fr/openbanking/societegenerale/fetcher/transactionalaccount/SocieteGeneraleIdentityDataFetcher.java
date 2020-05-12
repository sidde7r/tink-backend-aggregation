package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class SocieteGeneraleIdentityDataFetcher implements IdentityDataFetcher {

    private final SocieteGeneraleApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;

    @Override
    public IdentityData fetchIdentityData() {
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                signatureHeaderProvider.buildSignatureHeader(
                        sessionStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN), reqId);

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
