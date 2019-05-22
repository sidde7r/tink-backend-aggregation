package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.identity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Mandate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class HandelsbankenSEIdentityFetcher {

    public static FetchIdentityDataResponse fetchIdentityData(PersistentStorage persistentStorage) {
        return persistentStorage
                .get(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, AuthorizeResponse.class)
                .map(BaseResponse::getMandates)
                .map(HandelsbankenSEIdentityFetcher::mandatesToIdentity)
                .map(Optional::get)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }

    private static Optional<IdentityData> mandatesToIdentity(List<Mandate> mandates) {
        return mandates.stream()
                .distinct()
                .map(
                        mandate ->
                                SeIdentityData.of(
                                        mandate.getCustomerName(), mandate.getCustomerNumber()))
                .reduce(IdentityData::throwingMerger);
    }
}
