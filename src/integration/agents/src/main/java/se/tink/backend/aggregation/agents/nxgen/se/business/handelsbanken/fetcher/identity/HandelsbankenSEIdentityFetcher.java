package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.identity;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.EmbeddedEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class HandelsbankenSEIdentityFetcher {

    public static FetchIdentityDataResponse fetchIdentityData(SessionStorage sessionStorage) {
        return sessionStorage
                .get(
                        HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT,
                        ApplicationEntryPointResponse.class)
                .map(BaseResponse::getEmbedded)
                .map(HandelsbankenSEIdentityFetcher::embeddedToIdentityData)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }

    public static IdentityData embeddedToIdentityData(EmbeddedEntity embedded) {
        return IdentityData.builder()
                .setFullName(embedded.getUserInfo().getUserName())
                .setDateOfBirth(null)
                .build();
    }
}
