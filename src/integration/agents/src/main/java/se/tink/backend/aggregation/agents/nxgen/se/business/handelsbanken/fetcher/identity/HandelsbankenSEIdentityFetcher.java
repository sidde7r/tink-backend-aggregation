package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.identity;

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
                .map(HandelsbankenSEIdentityFetcher::toTinkIdentityData)
                .reduce(IdentityData::throwingMerger);
    }

    // This is just for the POC. We would want to create a business identity model with
    // business name and business number. Cannot temporarily set business number as DateOfBirth
    // for the POC since it is not a valid birth date.
    private static IdentityData toTinkIdentityData(Mandate mandate) {
        return IdentityData.builder()
                .setFullName(mandate.getCustomerName())
                .setDateOfBirth(null)
                .build();
    }
}
