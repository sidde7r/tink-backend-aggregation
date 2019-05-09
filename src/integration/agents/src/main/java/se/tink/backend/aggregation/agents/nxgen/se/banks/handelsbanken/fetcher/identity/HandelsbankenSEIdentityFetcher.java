package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.identity;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class HandelsbankenSEIdentityFetcher {

    public static FetchIdentityDataResponse fetchIdentityData(
            PersistentStorage persistentStorage, Credentials credentials) {
        return persistentStorage
                .get(HandelsbankenConstants.Storage.AUTHORIZE_END_POINT, AuthorizeResponse.class)
                .map(BaseResponse::getCustomerName)
                .map(Optional::get)
                .map(name -> SeIdentityData.of(name, credentials.getField(Field.Key.USERNAME)))
                .map(FetchIdentityDataResponse::new)
                .orElse(new FetchIdentityDataResponse(null));
    }
}
