package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;
import se.tink.libraries.identitydata.IdentityData;

public class UserDataFetcher {

    private final HVBStorage storage;
    private final UserDataCall userDataCall;
    private final UserDataMapper mapper;

    public UserDataFetcher(HVBStorage storage, UserDataCall userDataCall, UserDataMapper mapper) {
        this.storage = storage;
        this.userDataCall = userDataCall;
        this.mapper = mapper;
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg) {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::isSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "There was an error while executing call"));
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        UserDataResponse userDataResponse = executeCall(userDataCall, storage.getAccessToken());
        return new FetchIdentityDataResponse(getIdentityData(userDataResponse));
    }

    private IdentityData getIdentityData(UserDataResponse userDataResponse) {
        return mapper.toIdentityData(userDataResponse);
    }
}
